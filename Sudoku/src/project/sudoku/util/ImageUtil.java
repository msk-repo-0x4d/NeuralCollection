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
package project.sudoku.util;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Class for loading image from {@link org.opencv.core.Mat} object
 * and other helpful methods for Mat object and arrays
 * 
 * @since
 * Dated - 17-Jul-2017
 * 
 * @author S.Khan
 *
 */
public class ImageUtil
{
    private static Logger logger = Logger.getLogger(ImageUtil.class.getName());

    /**
     * It calls {@link #loadImage(Mat, String, boolean)} internally whose third parameter is false.
     * @param mat the opencv Mat to be displayed
     * @param label label of the window in which image is open
     * @return a buffered image that was loaded or null if not loaded
     * 
     * @see #loadImage(Mat, String, boolean)
     */
    public static BufferedImage loadImage(Mat mat, String label)
    {
        logger.info("calling loadImage3");
        return loadImage(mat, label, false);
    }

    /**
     * It opens an image in window from {@link org.opencv.core.Mat} object.
     * It internally uses {@link #getBufferedImage(Mat, boolean)}
     * to get a BufferedImage which is then open in a JFrame.
     * @param mat the opencv Mat to be displayed
     * @param label label for the window in which image is open
     * @param binaryImage flag to indicate image is binary (to distinguish
     * from 1-channel grayscale image)
     * @return a buffered image that was loaded or null if not loaded
     */
    public static BufferedImage loadImage(Mat mat, String label, boolean binaryImage)
    {
        logger.info("load image - " + label);
        logger.info("image dimensions (channels x width x height) - "
                + mat.channels() + " x "
                + mat.width() + " x "
                + mat.height());

        BufferedImage image = getBufferedImage(mat, binaryImage);

        if(image != null)
        {
            logger.info("creating image icon from buffered image");
            final ImageIcon imageIcon = new ImageIcon(image);

            // use EDT thread to open a new window
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    logger.info("show image in a window");
                    JFrame frame = new JFrame(label);
                    frame.setSize(image.getWidth() + 80, image.getHeight() + 80);

                    // add image
                    JLabel imageIconLabel = new JLabel();
                    imageIconLabel.setIcon(imageIcon);
                    frame.add(imageIconLabel);

                    // set layout and dispose on close
                    frame.setLayout(new FlowLayout());
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    frame.pack();
                    frame.setVisible(true);
                }
            });
        }
        return image;
    }

    /**
     * It returns a buffered image from {@link org.opencv.core.Mat} object of an image.
     * Currently it only supports Grayscale and RGB image interpreted
     * from the number of channels in Mat.
     * @param mat mat of the image
     * @param binaryImage true if mat is a single channel binary image
     * @return bufferedImage or null if there was error e.g. number of channels not compatible
     */
    public static BufferedImage getBufferedImage(Mat mat, boolean binaryImage)
    {
        logger.info("load image");
        logger.info("image dimensions (channels x width x height) - "
                + mat.channels() + " x "
                + mat.width() + " x "
                + mat.height());
        logger.info("binary image - " + binaryImage);

        int numOfChannels = mat.channels();

        // set type from number of channels in mat
        int matType = BufferedImage.TYPE_3BYTE_BGR;
        if(numOfChannels == 1)
        {
            matType = binaryImage ? BufferedImage.TYPE_BYTE_BINARY
                    : BufferedImage.TYPE_BYTE_GRAY;
        }
        else if(numOfChannels == 3)
        {
            matType = BufferedImage.TYPE_3BYTE_BGR;
        }
        else
        {
            logger.info("number of channels not compatible " + numOfChannels);
            return null;
        }

        logger.info("copying bytes");
        // get mat to byte array
        byte[] data = new byte[mat.channels()*mat.width()*mat.height()];
        mat.get(0, 0, data);

        // copy byte array to BufferedImage
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), matType);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);

        return image;
      }

    /**
     * It returns a binary inverted image from grayscale image using
     * adaptive thresholding.
     * @param grayScaleMat
     * @return inverted binary mat of the grayscale mat
     */
    public static Mat getBinaryInvFromGrayScale(Mat grayScaleMat)
    {
        if(grayScaleMat == null || grayScaleMat.channels() != 1)
            return null;

        // for adaptive thresholding first choose a block size as per size of the image
        // here arbitrarily chosen 10% of the larger side (width or height)
        int largerSide = grayScaleMat.width() > grayScaleMat.height() ? grayScaleMat.width() : grayScaleMat.height();
        int blockSize = (int) (0.1*largerSide);
        // make sure it is odd else opencv will have error
        blockSize = blockSize % 2 == 0 ? blockSize + 1 : blockSize;
        // for smallest value of block size set minimum block size to 3
        blockSize = blockSize < 3 ? 3 : blockSize;
        logger.info("Block size for adaptive thresholding : " + blockSize);

        // Choose c parameter accordingly for getting inverted binary image
        // for well and poorly lit images.
        // For poorly lit images, pixels are dark, average pixel value is low
        // and cParameter would be high.
        // Hence, here it is approximated by using an arbitrary chosen formula that
        // negatively correlates to average intensity of pixels in an image
        double cParameter = 0.3*Math.pow((255 - Core.mean(grayScaleMat).val[0]), 1);
        logger.info("C parameter for adaptive thresholding : " + cParameter);

        // use adaptive thresholding to get binary inverted image
        Mat binaryInvertedMat = new Mat();
        Imgproc.adaptiveThreshold(grayScaleMat, binaryInvertedMat, 1,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY_INV, blockSize, cParameter);

//        loadImage(binaryInvertedMat, "Binary", true);

        return binaryInvertedMat;
    }

    /**
     * It displays single channel mat through logger.
     * @param mat single channel mat to be displayed
     */
    public static void displayMat(Mat mat)
    {
        if(mat.channels() != 1)
            return;

        StringBuilder line = new StringBuilder(System.lineSeparator());
        for(int j = 0; j < mat.height(); j++)
        {
            for(int k = 0; k < mat.width(); k++)
            {
                line.append((int) mat.get(j, k)[0]);
            }
            line.append(System.lineSeparator());
        }

        logger.fine(line.toString());
    }

    /**
     * It zooms to non-zero pixel rectangle in the given Mat (if area of non-zero pixel
     * rectangle is atleast equal to the given minimum non-zero pixel area).
     * @param thisMat
     * @param minNonZeroPixelArea, only zooms to non zero pixel rectangle
     * if it is atleast equal to this minimum area
     * @return a Mat zoomed to non-zero pixel rectangle (with same size as original Mat)
     * or an empty Mat if the rectangle of non-zero pixels in the original mat have an area
     * less than the given minNonZeroPixelArea
     */
    public static Mat zoomIn(Mat thisMat, Integer minNonZeroPixelArea)
    {
        // remember size for zooming back to this size
        Size originalSize = thisMat.size();

        MatOfPoint matOfNonZeroPoints = new MatOfPoint();
        Core.findNonZero(thisMat, matOfNonZeroPoints);
        Rect boundingRectangle = Imgproc.boundingRect(matOfNonZeroPoints);
        logger.info("Area : " + boundingRectangle.area());

        Mat digitBlock = new Mat(new Size(0, 0), thisMat.type());
        if(boundingRectangle.area() >= minNonZeroPixelArea)
        {
            digitBlock = thisMat.submat(boundingRectangle).clone();
            Imgproc.resize(digitBlock, digitBlock, originalSize);
        }

        displayMat(digitBlock);

        return digitBlock;
    }

    /**
     * It returns an array with pixel count by moving a window on mat.
     * It internally uses the method {@link #getWindowedCount(double[], int, int, int)}
     * by converting the single channel mat into a 1D array and using
     * mat width and height as second and third parameters.
     * @param thisMat mat whose pixels are to be counted
     * @param windowSize size of the window that is moved while counting
     * pixel with pixel value equal to 1 (size is the length of the side of the square window)
     * @return an array containing pixel count (scaled between [0, 1]) that is 
     * obtained by moving the window or returns null if mat is not a single channel mat
     * (or returns null if the method {@link #getWindowedCount(double[], int, int, int)}
     * returns null)
     * 
     * @see #getWindowedCount(double[], int, int, int)
     */
    public static double[] getWindowedCount(Mat thisMat, int windowSize)
    {
        if(thisMat.channels() != 1)
            return null;

        return getWindowedCount(matToArray(thisMat),
                thisMat.width(), thisMat.height(), windowSize);
    }

    /**
     * It returns an array with pixel count by moving a window on the
     * matArray (matArray is a 1D array of a single channel mat
     * of width "matWidth" and height "matHeight").
     * @param matArray mat as a 1D array whose pixels are to be counted
     * @param matWidth width of the mat whose array is used
     * @param matHeight height of the mat whose array is used
     * @param windowSize size of the window that is moved while counting
     * pixel with pixel value equal to 1 (size is the length of the side of the square window)
     * @return  an array containing pixel count (scaled between [0, 1]) that is 
     * obtained by moving the window or returns null if window size is less than 1
     */
    public static double[] getWindowedCount(double[] matArray,
            int matWidth, int matHeight, int windowSize)
    {
        if(windowSize < 1)
            return null;

        int windowedRowCount = matHeight/windowSize;
        int windowedColCount = matWidth/windowSize;
        double[] windowedMatArray = new double[windowedRowCount*windowedColCount];
        int windowArea = windowSize*windowSize;

        int startRow, startCol, pixelCount;
        for(int newIndex = 0; newIndex < windowedMatArray.length; newIndex++)
        {
            // row and column values in original mat array
            startRow = (newIndex/windowedColCount)*windowSize;
            startCol = (newIndex%windowedColCount)*windowSize;
            // count pixels in each window
            pixelCount = 0;
            for(int rowIndex = startRow; rowIndex < startRow + windowSize; rowIndex++)
            {
                for(int colIndex = startCol; colIndex < startCol + windowSize; colIndex++)
                {
                    // cellHeight*rowIndex is for moving in one dimensional array
                    if(matArray[matHeight*rowIndex + colIndex] == 1)
                        pixelCount++;
                }
            }

            // normalize the pixel count
            windowedMatArray[newIndex] = ((double) pixelCount/windowArea);
        }

        return windowedMatArray;
    }

    // converts single channel mat to a 1D array of size row x col
    private static double[] matToArray(Mat thisMat)
    {
        if(thisMat.channels() != 1)
            return null;

        double[] mattArray = new double[thisMat.rows()*thisMat.cols()];
        for(int i = 0; i < thisMat.rows(); i++)
        {
            for(int j = 0; j < thisMat.cols(); j++)
            {
                // i*thisMat.cols() moves the index
                // by i rows in one dimensional array 
                mattArray[i*thisMat.cols() + j] = thisMat.get(i, j)[0];
            }
        }
        return mattArray;
    }

    /**
     * It filters noise from a Sudoku cell and then repairs the remaining pixels.
     * For noise filtering, it would try to remove everything except the digit pixels
     * (in the current version it only removes the border pixels). After filtering
     * it would try to do repair to get the digit pixels as accurate as possible.
     * @param thisMat mat of the Sudoku cell (a single channel Mat)
     */
    public static void filterCellNoiseAndRepair(Mat thisMat)
    {
        // assumed width ratio of border to square box for removing borders
        final double BORDER_WIDTH_RATIO = 0.1;

        ImageUtil.resetBorders(thisMat, BORDER_WIDTH_RATIO);

        // fill eroded pixels 
        Imgproc.dilate(thisMat, thisMat, new Mat(1, 1, thisMat.type()));
        ImageUtil.displayMat(thisMat);
    }

    /**
     * It resets the border (changes pixel values to 0). Border pixels are calculated
     * from the borderWidthRatio parameter.
     * @param mat whose borders are to be reset (pixel values changed to 0). Mat object
     * should be single channel.
     * @param borderWidthRatio ratio of border width on each side
     * (left, right, top, bottom) to the cell width for finding border pixels 
     */
    public static void resetBorders(Mat thisMat, final double borderWidthRatio)
    {
        // reset top and bottom border
        int borderWidth = (int) (borderWidthRatio*thisMat.height());
        for(int i = 0, k = borderWidth; i < borderWidth; i++, k--)
        {
            resetRow(thisMat, i);
            resetRow(thisMat, thisMat.height() - k);
        }

        // reset left and right border
        borderWidth = (int) (borderWidthRatio*thisMat.width());
        for(int i = 0, k = borderWidth; i < borderWidth; i++, k--)
        {
            resetColumn(thisMat, i);
            resetColumn(thisMat, thisMat.width() - k);
        }
    }

    // reset given row to 0
    private static void resetRow(Mat thisMat, int rowIndex)
    {
        for(int col = 0; col < thisMat.width(); col++)
        {
            thisMat.put(rowIndex, col, 0);
        }
    }

    // reset given column to 0
    private static void resetColumn(Mat thisMat, int columnIndex)
    {
        for(int row = 0; row < thisMat.height(); row++)
        {
            thisMat.put(row, columnIndex, 0);
        }
    }
}
