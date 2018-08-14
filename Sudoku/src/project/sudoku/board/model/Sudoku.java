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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Container class for Sudoku
 * <p>
 * It uses two maps, one to store fixed values and another to store values
 * that can be reset. The position of a grid in Sudoku is used as a key in
 * each of the two maps.
 * </p>
 * 
 * @since
 * Dated - 16-Jun-2017
 * 
 * @author S.Khan
 * 
 */
public class Sudoku
{
    private static final double LOW_CONFIDENCE_CUTOFF = 0.8;
    private static final double LOW_CONFIDENCE_MARGIN_CUTOFF = 0.7;

    private File sudokuImageFile = null;
    private Map<Integer, LikelyValue> fixedValueMap = new HashMap<Integer, LikelyValue>();
    private Map<Integer, Integer> otherValueMap = new HashMap<Integer, Integer>();

    /**
     * It clears values in otherValueMap (value that is not fixed).
     */
    public void clear()
    {
        otherValueMap.clear();
    }

    /**
     * It sets values to otherValueMap (only if the position has no value
     * set in fixedValueMap)
     * @param position position at which value is to be set
     * @param integerValue value to be set
     */
    public void setValueAt(Integer position, Integer integerValue)
    {
        if(!fixedValueMap.containsKey(position))
        {
            otherValueMap.put(position, integerValue);
        }
    }

    /**
     * It sets fixed value for Sudoku.
     * @param positionIndex
     * @param likelyValue
     */
    public void setFixedValueAt(Integer position, LikelyValue likelyValue)
    {
        this.fixedValueMap.put(position, likelyValue);
    }

    /**
     * It returns value at a given position. It first checks
     * fixed value map and then the other value map
     * @param position position for which value is to be found
     * @return value at given position,
     * <b>default value is -1</b> (if position is not found in either map)
     */
    public Integer getValueAt(Integer position)
    {
        Integer value = -1;
        if(fixedValueMap.containsKey(position))
        {
            value = fixedValueMap.get(position).getValue();
        }
        else if(otherValueMap.containsKey(position))
        {
            value = otherValueMap.get(position);
        }
        return value;
    }

    /**
     * It sets the sudoku image file that this sudoku is supposed to represent.
     * Note - In current version sudoku image file is not verified with existing sudoku.
     * @param sudokuImageFile file that this sudoku is supposedly extracted from.
     */
    public void setSudokuImageFile(File sudokuImageFile)
    {
        this.sudokuImageFile = sudokuImageFile;
    }

    /**
     * It returns sudoku image file that this sudoku is supposed to represent
     * Note - In current version sudoku image file is not synchronized with the sudoku.
     * @return sudoku image file
     */
    public File getSudokuImageFile()
    {
        return sudokuImageFile;
    }

    /**
     * It returns likely value (containing value, confidence and confidence margin)
     * for a low confidence position or null if it does not qualify as low confidence
     * position.
     * @param position
     * @return likely value or null if empty position or value at this position
     * is not a low confidence value
     */
    public LikelyValue getLowConfidenceValue(Integer position)
    {
        LikelyValue likelyValue = fixedValueMap.get(position);

        if(likelyValue != null)
        {
            // check if it is below low confidence cutoffs
            if(likelyValue.getConfidence() < LOW_CONFIDENCE_CUTOFF
                    || likelyValue.getConfidenceMargin() < LOW_CONFIDENCE_MARGIN_CUTOFF)
            {
                return likelyValue;
            }
        }

        // return null for empty positions or value at this position
        // is not a low confidence value
        return null;
    }

    /**
     * It returns true if the position value has low confidence for accuracy
     * @param position position which is to be checked
     * @return true if it is low confidence position
     */
    public boolean isLowConfidencePosition(Integer position)
    {
        LikelyValue likelyValue = fixedValueMap.get(position);

        // mark low confidence positions
        return likelyValue != null &&
                (likelyValue.getConfidence() < LOW_CONFIDENCE_CUTOFF
                || likelyValue.getConfidenceMargin() < LOW_CONFIDENCE_MARGIN_CUTOFF);
    }

    /**
     * It returns true if the given position has a fixed value
     * else returns false.
     * @param position position which is to be checked for fixed value
     * @return true if the position has fixed value else returns false
     */
    public boolean hasFixedValueAt(Integer position)
    {
        return fixedValueMap.containsKey(position);
    }
}
