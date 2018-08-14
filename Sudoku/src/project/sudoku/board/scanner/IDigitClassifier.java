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

import org.opencv.core.Mat;

import project.sudoku.board.model.LikelyValue;

/**
 * <p>
 * Interface for digit classification
 * </p>
 * <p>
 * Any class implementing this interface should implement :
 * <li>{@link #getLikelyValue(Mat)}</li>
 * <li>{@link #getLikelyValueForMax(double[])}</li>
 * </p>
 *
 * @since
 * Dated - 01-Nov-2017
 *
 * @author S.Khan
 *
 */
public interface IDigitClassifier
{
    /**
     * It returns the most likely value for the Mat object
     * (classifies binary pixels in Mat into a digit
     * whose values can be one of the digit 1,2,...,9)
     * @param thisMat mat containing the digit pixels
     * @return likelyValue the most {@linkplain LikelyValue likely value}
     * between [1, 9] or else returns -1 as the likely value
     * (if classification is not possible)
     */
    public abstract LikelyValue getLikelyValue(Mat thisMat);

    /**
     * It returns the digit corresponding to the maximum confidence value
     * @param confidenceValues confidence values corresponding to digits [1, 9]
     * @return likelyValue the {@linkplain LikelyValue likely value}
     * with the digit corresponding to the maximum confidence value
     * (digit starts from 1) or else returns -1 as the likely value
     * if confidence values array is empty
     */
    public abstract LikelyValue getLikelyValueForMax(double[] confidenceValues);
}
