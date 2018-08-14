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
package project.sudoku.board.ui;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Listener for editing a value in Sudoku
 * 
 * @since
 * Dated - 13-Jul-2017
 * 
 * @author S.Khan
 * 
 */
public class SudokuGridListener extends KeyAdapter
{
    private static Logger logger = Logger.getLogger(SudokuGridListener.class.getName());
    private SudokuBoard sudokuBoard = null;

    public SudokuGridListener(SudokuBoard sudokuBoard)
    {
        this.sudokuBoard = sudokuBoard;
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        JTextField textField = (JTextField) e.getSource();
        if(textField.getText().isEmpty())
        {
            int charValue = (int) e.getKeyChar();

            // set values between 1-9
            if(charValue >= (int) '1' && charValue <= (int) '9')
            {
                setValueAndMove(e);
            }
            else // number not between 1-9, do nothing
            {
                logger.fine((int) e.getKeyChar() + "");
                e.consume();
            }
        }
        else // text field is not empty, do nothing
        {
            e.consume();
        }
    }

    /**
     * It sets value and focuses to next empty grid
     * @param e
     */
    private void setValueAndMove(KeyEvent e)
    {
        JTextField textField = (JTextField) e.getSource();
        JPanel sudokuGrid = (JPanel) textField.getParent();

        int index = 0;
        boolean valueSet = false;
        for(Component component : sudokuGrid.getComponents())
        {
            if(valueSet) // if value was set then move to next empty grid
            {
                if(((JTextField) component).getText().isEmpty())
                {
                    component.requestFocusInWindow();
                    break;
                }
            }
            // set value at current position
            else if(component.equals(textField))
            {
                Integer value = Integer.parseInt((char) e.getKeyChar() + "");
                sudokuBoard.getSudoku().setValueAt(index, value);
                valueSet = true;
            }
            index++;
        }
    }
}
