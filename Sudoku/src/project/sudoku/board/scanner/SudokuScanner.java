/*******************************************************************************
 * Copyright 2017 M.S.Khan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package project.sudoku.board.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import project.sudoku.board.model.LikelyValue;
import project.sudoku.board.model.Sudoku;
import project.sudoku.exception.ResourceLoadingException;
import project.sudoku.util.ImageUtil;

/**
 * <p>
 * It scans Sudoku image to create a {@link Sudoku} object. It uses OpenCV library
 * for pre-processing image and extracting pixel values of digits in each of the
 * grid in a Sudoku box. The pixel values are then fed to a trained neural network
 * for digit classification.
 * </p>
 * 
 * @since
 * Dated - 16-Jun-2017
 * 
 * @author S.Khan
 * 
 */
public class SudokuScanner
{
    private static Logger logger = Logger.getLogger(SudokuScanner.class.getName());

    // a classifier for digit classification from pixel values
    private static IDigitClassifier digitClassifier = null;

    // minimum sudoku image resolution - 80x80 pixels
    private static final int SUDOKU_MINIMUM_HEIGHT = 80;
    private static final int SUDOKU_MINIMUM_WIDTH = 80;

    // after pre-processing the fixed height of each cell in Sudoku box
    private static final int CELL_HEIGHT = 32;
    private static final int CELL_WIDTH = 32;

    // minimum rectangle area for considering a digit
    private static final int MIN_RECT_AREA_FOR_DIGIT = 16;
    // parallel lines within 10 degree
    private static final double PARALLEL_THETA_MARGIN = Math.toRadians(10);

    /**
     * load resources for the Scanner
     * @throws ResourceLoadingException
     */
    public static void loadResources() throws ResourceLoadingException
    {
        digitClassifier = NNClassifier.getInstance();
    }

    /**
     * It loads Sudoku from an image file. Before calling this method for the first time
     * one should load the neural network by calling its static method
     * {@link SudokuScanner#loadTrainedNetwork()}
     * or else will result in NullPointerException.
     * While extracting numbers from Sudoku, the values that could
     * not be recognized are added as 0 while the unfilled values as -1.
     * @param imageFile the file containing the Sudoku
     * @return Sudoku that is read from the file
     */
    public static Sudoku loadSudoku(File imageFile)
    {
        logger.info("loading sudoku from image file : " + imageFile.getName());

        // -> READ IMAGE FILE IN GRAYSCALE
        Mat grayScaleMat = Imgcodecs.imread(imageFile.getAbsoluteFile().getAbsolutePath(),
                Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        // -> CONVERT GRAY SCALE IMAGE TO INVERTED BINARY IMAGE - USE ADAPTIVE THRESHOLDIING
        Mat binaryInvertedMat = ImageUtil.getBinaryInvFromGrayScale(grayScaleMat);

        // -> EXTRACT MAT OF EACH CELL IN SUDOKU
        Sudoku sudoku = null;
        List<Mat> allMats = getSudokuGridMatsFromImage(binaryInvertedMat);
        if(allMats != null)
        {
            sudoku = getSudoku(allMats);
            sudoku.setSudokuImageFile(imageFile);
        }

        return sudoku;
    }

    private static List<Mat> getSudokuGridMatsFromImage(Mat binaryInvertedMat)
    {
        logger.info("Extract Sudoku grids from given entire image");

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint heirarchy = new MatOfPoint();
        Imgproc.findContours(binaryInvertedMat, contours,
                heirarchy, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_NONE);

        List<Mat> sudokuGridMats = null;
        for(MatOfPoint contour : contours) // iterate through each contour.
        {
            double contourArea = Imgproc.contourArea(contour);
            logger.info("Contour Area : " + contourArea);
            // a grid with lower than this resolution will not work
            if(contourArea < SUDOKU_MINIMUM_HEIGHT*SUDOKU_MINIMUM_WIDTH)
                continue;

            // try looking for grids in this submat of the contour
            sudokuGridMats = getSudokuGridMats(binaryInvertedMat.submat(
                    Imgproc.boundingRect(contour)).clone());

            if(sudokuGridMats != null)
            {
                break;
            }
        }

        return sudokuGridMats;
    }

    private static List<Mat> getSudokuGridMats(Mat binaryInvertedMat)
    {
        logger.info("Extract Sudoku grids from part of image possibly containing Sudoku");

        logger.info("Mat size : " + binaryInvertedMat.rows() +
                " x " + binaryInvertedMat.cols());

        // Sudoku grid with resolution lower than this is not 
        // usable in current version
        if(binaryInvertedMat.height() < SUDOKU_MINIMUM_HEIGHT
                || binaryInvertedMat.width() < SUDOKU_MINIMUM_WIDTH)
            return null;

        int minPtLineThreshold = (int) (0.6*binaryInvertedMat.height());
        logger.info("Minimum points for it to be a grid line : " + minPtLineThreshold);

        Mat houghLines = new Mat();
        Imgproc.HoughLines(binaryInvertedMat, houghLines, 1,
                Math.toRadians(1), minPtLineThreshold);

        // for collecting thetas of rhos of similar thetas
        Map<Double, Map<Double, Double>> similarThetaRhoThetaMaps =
                new HashMap<Double, Map<Double, Double>>();

        double[] lineProperties;
        double rho, theta;
        for(int i = 0; i < houghLines.rows(); i++)
        {
            lineProperties = houghLines.get(i, 0);
            rho = lineProperties[0];
            theta = lineProperties[1];
            logger.info("rho " + rho + " theta " + theta);

            double keyTheta = theta;
            // if there is a key that is similar to this theta
            // then use the keyTheta
            for(Double uniqueTheta : similarThetaRhoThetaMaps.keySet())
            {
                if(Math.abs(uniqueTheta - theta) <= PARALLEL_THETA_MARGIN
                        || Math.abs(Math.PI - Math.abs(uniqueTheta - theta)) <= PARALLEL_THETA_MARGIN)
                {
                    keyTheta = uniqueTheta;
                    break;
                }
            }

            // add a new theta to map
            if(similarThetaRhoThetaMaps.get(keyTheta) == null)
            {
                similarThetaRhoThetaMaps.put(keyTheta, new HashMap<Double, Double>());
            }
            similarThetaRhoThetaMaps.get(keyTheta).put(rho, theta);
        }

        if(similarThetaRhoThetaMaps.keySet().size() != 2) // exactly two thetas in the grid
        {
            return null;
        }

        // get average values for similar thetas
        Map<Double, Double> thetaAverageThetaMap = getThetaAverageThetaMap(similarThetaRhoThetaMaps);
        Iterator<Double> similarThetaIterator = similarThetaRhoThetaMaps.keySet().iterator();
        double averageAngle1 = thetaAverageThetaMap.get(similarThetaIterator.next());
        double averageAngle2 = thetaAverageThetaMap.get(similarThetaIterator.next());

        logger.info("Average Angle 1 : " + averageAngle1 + " Average Angle 2 : " + averageAngle2);

        // check if they are perpendicular angles
        // absolute angle difference should be close to 90 degrees
        // and consequently sin value should be close to 1
        if(Math.abs(Math.sin(averageAngle2 - averageAngle1)) < 0.98)
            return null;

        // swap angles if angle1 is not the horizontal one
        // (i.e. if it is between 45deg - 135deg)
        if(Math.abs(Math.sin(averageAngle1)) > 0.7)
        {
            double tempAngle = averageAngle1;
            averageAngle1 = averageAngle2;
            averageAngle2 = tempAngle;
        }

        Double uniqueTheta1 = -1.0, uniqueTheta2 = -1.0;
        for(Double uniqueTheta : similarThetaRhoThetaMaps.keySet())
        {
            if(thetaAverageThetaMap.get(uniqueTheta) == averageAngle1)
            {
                uniqueTheta1 = uniqueTheta;
            }
            if(thetaAverageThetaMap.get(uniqueTheta) == averageAngle2)
            {
                uniqueTheta2 = uniqueTheta;
            }
        }

        // change keys  for maps to average theta values obtained by averaging the list of thetas
        Double averageTheta;
        Map<Double, List<Double>> avgThetaRhoMap = new HashMap<Double, List<Double>>();
        for(Double uniqueTheta : similarThetaRhoThetaMaps.keySet())
        {
            averageTheta = thetaAverageThetaMap.get(uniqueTheta);
            if(avgThetaRhoMap.get(averageTheta) == null)
                avgThetaRhoMap.put(averageTheta, new ArrayList<Double>());

            // add the set of rho values for this average theta
            avgThetaRhoMap.get(averageTheta).addAll(
                    similarThetaRhoThetaMaps.get(uniqueTheta).keySet());
        }

        // theta would be close to zero for vertical lines
        // get 9 equidistant rho values for parallel vertical lines
        List<Double> rhoList1 = avgThetaRhoMap.get(averageAngle1);
        Collections.sort(rhoList1);
        TreeMap<Double, Double> fromToRhoMap1 =
                getCosecutiveNineEquidistantFromToRhos(rhoList1);

        // theta would be close to 90 for horizontal lines
        // 9 equidistant rho values for parallel horizontal lines
        List<Double> rhoList2 = avgThetaRhoMap.get(averageAngle2);
        Collections.sort(rhoList2);
        TreeMap<Double, Double> fromToRhoMap2 =
                getCosecutiveNineEquidistantFromToRhos(rhoList2);

        logger.info("parallel lines set 1 size : " + fromToRhoMap1.size());
        logger.info("parallel lines set 2 size : " + fromToRhoMap2.size());

        if(fromToRhoMap1.size() != 9 || fromToRhoMap2.size() != 9)
        {
            return null;
        }

        // display the Sudoku rectangle
        Point point1 = new Point(fromToRhoMap1.firstKey(), fromToRhoMap2.firstKey());
        Point point2 = new Point(fromToRhoMap1.lastEntry().getValue(), fromToRhoMap2.lastEntry().getValue());
        logger.info("pt 1" + point1.toString());
        logger.info("pt 2" + point2.toString());
//        ImageUtil.loadImage(binaryInvertedMat.submat((new Rect(point1, point2))), "Mat Grids", true);

        double[] constantDistances = new double[2];
        constantDistances[0] = Math.abs(fromToRhoMap1.keySet().iterator().next()
                - fromToRhoMap1.get(fromToRhoMap1.keySet().iterator().next()));
        constantDistances[1] = Math.abs(fromToRhoMap2.keySet().iterator().next()
                - fromToRhoMap2.get(fromToRhoMap2.keySet().iterator().next()));

        logger.info("Absolute Rhos : " + fromToRhoMap1.keySet().iterator().next()
                + " " + fromToRhoMap2.keySet().iterator().next());
        logger.info("Constant Distances : " + Arrays.toString(constantDistances));

        List<Mat> sudokuMats = new ArrayList<Mat>();
        Iterator<Double> rho1Iterator = fromToRhoMap1.keySet().iterator();
        Iterator<Double> rho2Iterator = fromToRhoMap2.keySet().iterator();
        int matCounter = 0;
        // iterate over each horizontal line
        rho2Iterator = fromToRhoMap2.keySet().iterator();
        while(rho2Iterator.hasNext())
        {
            Double rho2 = rho2Iterator.next();
            Double theta2 = similarThetaRhoThetaMaps.get(uniqueTheta2).get(rho2);
            Double sinTheta2 = Math.sin(theta2);
            int rowStart = (int) (rho2*sinTheta2) + 1;
            int rowEnd = (int) (fromToRhoMap2.get(rho2)*sinTheta2) - 1;

            // move along column lines
            rho1Iterator = fromToRhoMap1.keySet().iterator();
            while(rho1Iterator.hasNext())
            {
                Double rho1 = rho1Iterator.next();
                Double theta1 = similarThetaRhoThetaMaps.get(uniqueTheta1).get(rho1);
                Double cosTheta1 = Math.cos(theta1);

                int colStart = (int) (rho1*cosTheta1) + 1;
                int colEnd = (int) (fromToRhoMap1.get(rho1)*cosTheta1) - 1;
                logger.fine("Submat " + matCounter +
                        " Values of rowStart, rowEnd, colStart, colEnd " +
                        rowStart + ", " + rowEnd + ", " + colStart + ", " + colEnd);
                sudokuMats.add(binaryInvertedMat.submat(rowStart, rowEnd, colStart, colEnd).clone());
                ImageUtil.displayMat(binaryInvertedMat.submat(rowStart, rowEnd, colStart, colEnd).clone());
                matCounter++;
            }
        }

        return sudokuMats;

    }

    private static TreeMap<Double, Double> getCosecutiveNineEquidistantFromToRhos(List<Double> rhoList)
    {
        logger.info(rhoList.toString());

        // parameter for considering rho differences as equals
        // if their mutual difference is this much of fraction
        final Double SAME_RHO_ERROR_TOLERANCE = 0.2;

        TreeMap<Double, Double> fromToRhoMapForApproxEqual = new TreeMap<Double, Double>();
        Double rhoDifference, constantRhoDifference;
        for(int i = 0; i < rhoList.size() - 9; i++)
        {
            constantRhoDifference = Math.abs(rhoList.get(i) - rhoList.get(i + 1));
            logger.fine("Constant rho difference " + constantRhoDifference);
            for(int j = 1; j < rhoList.size(); j++)
            {
                rhoDifference = Math.abs(rhoList.get(j - 1) - rhoList.get(j));
                logger.fine("Rho difference " + rhoDifference);
                // lines are almost equal
                if(Math.abs(constantRhoDifference - rhoDifference)
                        <= SAME_RHO_ERROR_TOLERANCE*constantRhoDifference)
                {
                    fromToRhoMapForApproxEqual.put(rhoList.get(j - 1), rhoList.get(j));
                }
                // lines are too unequal
                else if(rhoDifference > SAME_RHO_ERROR_TOLERANCE*constantRhoDifference)
                {
                    fromToRhoMapForApproxEqual.clear();
                }

                // check if remaining rhos in rho list
                // along with current equal rhos wont add to 9
                if(rhoList.size() - j + fromToRhoMapForApproxEqual.size() < 9)
                {
                    break;
                }
            }

            // if exactly 9 approximately equidistant rhos were found
            // then do not try other rho differences
            if(fromToRhoMapForApproxEqual.keySet().size() == 9)
            {
                break;
            }

            fromToRhoMapForApproxEqual.clear();
        }

        return fromToRhoMapForApproxEqual;
    }

    private static Map<Double, Double> getThetaAverageThetaMap(
            Map<Double, Map<Double, Double>> similarThetaRhoThetaMaps)
    {
        // map for theta keys to average of thetas they represent
        Map<Double, Double> thetaAverageThetaMap = new HashMap<>();
        Double averageValue;
        for(Double thetaKey : similarThetaRhoThetaMaps.keySet())
        {
            averageValue = getSineAverageThetaValue(new ArrayList<Double>(
                    similarThetaRhoThetaMaps.get(thetaKey).values()));
            thetaAverageThetaMap.put(thetaKey, averageValue);
        }

        return thetaAverageThetaMap;
    }

    private static Double getSineAverageThetaValue(List<Double> listOfThetas)
    {
        if(listOfThetas == null || listOfThetas.isEmpty())
            return null;

        // getting sinAverage which is not same as averaging angles
        Double totalSineSum = 0.0;
        for(Double thetaValue : listOfThetas)
        {
            totalSineSum += Math.sin(thetaValue);
        }

        Double avgTheta = Math.asin(totalSineSum/listOfThetas.size());

        return avgTheta;
    }

    private static Sudoku getSudoku(List<Mat> allMats)
    {
        logger.info("Detecting characters from Sudoku grids");

        Sudoku sudoku = new Sudoku();
        LikelyValue likelyValue = null;
        int positionIndex = 0;

        for(Mat thisMat : allMats)
        {
            logger.info("Grid position - " + positionIndex);
            Imgproc.resize(thisMat, thisMat, new Size(CELL_WIDTH, CELL_HEIGHT));
            ImageUtil.displayMat(thisMat);

            // remove noise from a Sudoku cell
            ImageUtil.filterCellNoiseAndRepair(thisMat);

            // first zoom-in
            thisMat = ImageUtil.zoomIn(thisMat, MIN_RECT_AREA_FOR_DIGIT);

            // get likely value for this grid
            likelyValue = digitClassifier.getLikelyValue(thisMat);
            if(likelyValue.getValue() > 0) //add only non-empty values
            {
                sudoku.setFixedValueAt(positionIndex, likelyValue);
            }
            positionIndex++;
        }
        return sudoku;
    }
}
