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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.neuroph.nnet.MultiLayerPerceptron;
import org.opencv.core.Mat;

import project.sudoku.board.model.LikelyValue;
import project.sudoku.config.Config;
import project.sudoku.exception.ResourceLoadingException;
import project.sudoku.util.ImageUtil;

/**
 * <p>
 * Singleton class with a trained network for digit classification.
 * </p>
 * 
 * <p>Implements {@link IDigitClassifier}</p>
 * 
 * @since
 * Dated - 22-Nov-2017
 *
 * @author S.Khan
 *
 */
public class NNClassifier implements IDigitClassifier
{
    private static Logger logger = Logger.getLogger(NNClassifier.class.getName());

    // the Singleton class instance
    private static NNClassifier instance = null;

    // the trained neural network
    private static MultiLayerPerceptron trainedNetwork = null;

    // window size for counting pixels with value as 1
    private static final int WINDOW_SIZE = 2;

    private NNClassifier() throws ResourceLoadingException
    {
        loadTrainedNetwork();
    }

    private static void loadTrainedNetwork() throws ResourceLoadingException
    {
        // load neural network
        logger.info("loading neural network file : " + Config.NETWORK_FILE);

        try
        {
            trainedNetwork = (MultiLayerPerceptron) MultiLayerPerceptron.load(
                    new FileInputStream(new File(Config.NETWORK_FILE)));
        } catch (FileNotFoundException e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ResourceLoadingException(
                    "Error loading Neural network file : "
                    + new File(Config.NETWORK_FILE).getAbsolutePath());
        }
    }

    /**
     * return the Singleton class instance
     * @return shared instance of this class
     * @throws ResourceLoadingException 
     */
    public static NNClassifier getInstance() throws ResourceLoadingException
    {
        if(instance == null)
        {
            instance = new NNClassifier();
        }

        return instance;
    }

    @Override
    public LikelyValue getLikelyValue(Mat thisMat)
    {
        LikelyValue likelyValue = null;
        if(!thisMat.empty())
        {
            // convert mat as input attributes
            double[] inputVector = getFeatureVector(thisMat);
            logger.fine(Arrays.toString(inputVector));

            // feed input to neural network
            trainedNetwork.setInput(inputVector);
            trainedNetwork.calculate();

            // get the most likely value from the neural network output
            likelyValue = getLikelyValueForMax(trainedNetwork.getOutput());

            logger.fine(likelyValue.getValue() + "");
        }
        else
        {
            likelyValue = new LikelyValue(-1);
            logger.fine("mat is empty");
        }

        return likelyValue;
    }

    @Override
    public LikelyValue getLikelyValueForMax(double[] confidenceValues)
    {
        double maxConfidenceValue = 0d, smallestConfidenceMargin = 0d;
        // index starts with 1, default digit is - 1
        int index = 1, digit = -1;
        for(double confidenceValue : confidenceValues)
        {
            if(maxConfidenceValue < confidenceValue)
            {
                // digit is the index (index starting from 1)
                // corresponding to the maximum confidence value
                digit = index;
                smallestConfidenceMargin = confidenceValue - maxConfidenceValue;
                maxConfidenceValue = confidenceValue;
            }

            index++;
        }

        LikelyValue likelyValue = new LikelyValue(digit,
                maxConfidenceValue, smallestConfidenceMargin);

        logger.info("Digit : " + digit
                + " Confidence : " + maxConfidenceValue
                + " Confidence Margin : " + smallestConfidenceMargin);

        return likelyValue;
    }

    private static double[] getFeatureVector(Mat thisMat)
    {
        // get count of pixels in a 2x2 window
        return ImageUtil.getWindowedCount(thisMat, WINDOW_SIZE);
    }
}
