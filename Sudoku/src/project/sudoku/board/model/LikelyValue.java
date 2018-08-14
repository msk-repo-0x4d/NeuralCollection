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
package project.sudoku.board.model;

/**
 *
 * Container class for storing an uncertain integer value
 * <p>
 * It stores an integer that has some confidence attached to it of being this value.
 * The confidence is characterized by two fields -
 * {@link #confidence} and {@link #confidenceMargin}.
 * The confidence is a double value between 0 and 1 and is related to the direct output
 * of a neural network. The confidence margin is also a double value between 0 and 1 and
 * is the difference in confidence from next higher confident value.
 * </p>
 * 
 * @see {@link #confidence}, {@link #confidenceMargin}
 * 
 * @since
 * Dated - 18-Jul-2017
 * 
 * @author S.Khan
 * 
 */
public class LikelyValue
{
    private Integer value = -1;

    /**
     * It is the measure of absolute confidence for being this value.
     * This value lies between 0 and 1. 
     */
    private Double confidence = 1.0;

    /** 
     * It is the difference in confidence from next higher confident value.
     * So if the confidence of digit being 8 is 0.9 and confidence of digit being
     * 3 is 0.02 then  the confidence margin is 0.88. The value also lies between 0 and 1. 
     */
    private Double confidenceMargin = 1.0;

    /**
     * The constructor for creating a likely value with confidence and confidence margin
     * @param value likely value
     * @param confidence confidence of the likely value
     * @param confidenceMargin it is the difference of confidence from next higher confident value
     */
    public LikelyValue(int value, double confidence, double confidenceMargin)
    {
        this.value = value;
        this.confidence = confidence;
        this.confidenceMargin = confidenceMargin;
    }

    /**
     * It calls the other constructor {@link #LikelyValue(int, double, double)}
     * and uses confidence = 1.0 and confidence margin = 1.0 for
     * 2nd and 3rd parameter
     * @param integerValue
     */
    public LikelyValue(Integer integerValue)
    {
        this(integerValue, 1.0, 1.0);
    }

    /**
     * @return the value
     */
    public Integer getValue()
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Integer value)
    {
        this.value = value;
    }

    /**
     * @return the confidence for a value
     * @see #confidence
     */
    public Double getConfidence()
    {
        return confidence;
    }

    /**
     * @param confidence the confidence to set
     * @see #confidence
     */
    public void setConfidence(Double confidence)
    {
        this.confidence = confidence;
    }

    /**
     * @return confidenceMargin - the confidence margin for a value
     * @see {@link #confidenceMargin}
     */
    public Double getConfidenceMargin()
    {
        return confidenceMargin;
    }

    /**
     * @param confidenceMargin the confidenceMargin to set.
     * @see {@link #confidenceMargin}
     */
    public void setConfidenceMargin(Double confidenceMargin)
    {
        this.confidenceMargin = confidenceMargin;
    }
}
