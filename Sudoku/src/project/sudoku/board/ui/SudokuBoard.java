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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import project.sudoku.board.model.LikelyValue;
import project.sudoku.board.model.Sudoku;
import project.sudoku.board.scanner.SudokuScanner;
import project.sudoku.board.solver.SudokuSolver;
import project.sudoku.config.Config;
import project.sudoku.exception.ResourceLoadingException;

/**
 * Frame for Sudoku Window
 * 
 * @since
 * Dated - 02-Aug-2017
 * 
 * @author S.Khan
 * 
 */
public class SudokuBoard extends JFrame
{
    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(SudokuBoard.class.getName());

    // 9x9 grid for Sudoku
    private JPanel sudokuGrid = null;
    // Sudoku object
    private Sudoku sudoku = null;

    /**
     * @throws HeadlessException
     */
    public SudokuBoard() throws HeadlessException
    {
        super("Sudoku Board - v" + Config.PROJECT_VERSION);

//        getContentPane().setBackground(Color.WHITE);
        setMinimumSize(new Dimension(500, 410));
        setIconImage(Toolkit.getDefaultToolkit().getImage(Config.SUDOKU_ICON));

        addContent();
        pack();
    }

    /**
     * adds content to frame
     */
    protected void addContent()
    {
        logger.info("Loading Sudoku Frame");

        // create a new window

        setLayout(new GridBagLayout());

        // create the part containing the grid for sudoku
        sudokuGrid  = createSudokuGrid();
        GridBagConstraints sudokuGridConstraints = createGridBagConstraints(
                GridBagConstraints.BOTH, 0, 0, 1, 5);
        sudokuGridConstraints.insets = new Insets(0, 10, 0, 0);
        add(sudokuGrid, sudokuGridConstraints);

        // add buttons
        // add load button
        JButton loadButton = createButton("Load", "Load a Sudoku Image");
        GridBagConstraints loadButtonGridConstraints = createGridBagConstraints(
                GridBagConstraints.HORIZONTAL, 1, 0, 1, 1);
        loadButtonGridConstraints.insets = new Insets(5, 10, 0, 10);
        loadButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    logger.info("load a Sudoku image");
                    // choose Sudoku image file
                    chooseSudoku();
                } catch (IOException e1)
                {
                    logger.log(Level.SEVERE, e1.getMessage(), e1);
                }
            }
        });
        add(loadButton, loadButtonGridConstraints);

        // add clear button
        JButton clearButton = createButton("Clear", "Clear filled values");
        GridBagConstraints clearButtonGridConstraints = createGridBagConstraints(
                GridBagConstraints.HORIZONTAL, 1, 1, 1, 1);
        clearButtonGridConstraints.insets = new Insets(10, 10, 0, 10);
        clearButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                logger.info("clear filled sudoku values");
                // clear map for values that are not fixed
                sudoku.clear();
                // reset the board to display these values
                resetSudokuBoard();
            }
        });
        add(clearButton, clearButtonGridConstraints);

        // add solve button
        JButton solveButton = createButton("Solve", "Solve this sudoku");
        GridBagConstraints solveButtonGridConstraints = createGridBagConstraints(
                GridBagConstraints.HORIZONTAL, 1, 2, 1, 1);
        solveButtonGridConstraints.insets = new Insets(10, 10, 0, 10);
        solveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    // sudoku.clear();
                    logger.info("solve this Sudoku with given values");
                    // solve sudoku from the current state of sudoku
                    SudokuSolver.solve(sudoku);
                } catch (ResourceLoadingException e1)
                {
                    logger.severe(e1.getMessage());
                } catch (Exception e1)
                {
                    logger.severe(e1.getMessage());
                }
                resetSudokuBoard();
            }
        });
        add(solveButton, solveButtonGridConstraints);

        // add solve button
        JButton aboutButton = createButton("About", "About Sudoku");
        GridBagConstraints aboutButtonGridConstraints = createGridBagConstraints(
                GridBagConstraints.HORIZONTAL, 1, 3, 1, 1);
        aboutButtonGridConstraints.insets = new Insets(10, 10, 0, 10);
        aboutButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    logger.info("Opening frame for About Project information");
                    AboutProjectFrame aboutFrame = new AboutProjectFrame();
                    aboutFrame.setVisible(true);
                    aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                } catch (Exception e1)
                {
                    logger.severe(e1.getMessage());
                }
            }
        });
        add(aboutButton, aboutButtonGridConstraints);

        // initialize with default sudoku
        setSudokuImageFile(new File(Config.DEFAULT_SUDOKU_IMAGE_FILE));
    }

    // creates a bag constraint of given parameters
    private GridBagConstraints createGridBagConstraints(
            int fill, int gridx, int gridy, int gridWidth, int gridHeight)
    {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = fill;
        gridBagConstraints.gridx = gridx;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.gridheight = gridHeight;
        gridBagConstraints.gridwidth = gridWidth;
        return gridBagConstraints;
    }

    // creates standard 100x40 pixel buttons
    private JButton createButton(String buttonName, String toolTip)
    {
        JButton button = new JButton(buttonName);
        button.setToolTipText(toolTip);
        button.setPreferredSize(new Dimension(100, 40));
        button.setMinimumSize(new Dimension(100, 40));
        return button;
    }

    /**
     * create a Sudoku board
     * @return
     */
    private JPanel createSudokuGrid()
    {
        // panel with 9x9 grid
        JPanel sudokuBox = new JPanel(new GridLayout(9, 9));
        sudokuBox.setPreferredSize(new Dimension(350, 350));
        sudokuBox.setMinimumSize(new Dimension(350, 350));

        JTextField textField = null;
        for(int row = 0; row < 9; row++)
        {
            for(int col = 0; col < 9; col++)
            {
                textField = createSudokuGrid(sudokuBox, row, col);
                textField.addKeyListener(new SudokuGridListener(this));
                sudokuBox.add(textField);
            }
        }

        return sudokuBox;
    }

    // grid is a textfield with 4 borders
    // bold borders for 3x3 blocks and outside of Sudoku box
    private JTextField createSudokuGrid(JPanel board, int row, int col)
    {
        JTextField textField = new JTextField();

        // thicker top and left borders for 3x3 blocks
        // and for remaining bottom row and right columns of Sudoku box
        int topBorder = row%3 == 0 ? 4 : 1;
        int leftBorder = col%3 == 0 ? 4 : 1;
        int rightBorder = col == 8 ? 4 : 1;
        int bottomBorder = row == 8 ? 4 : 1;

        textField.setBorder(BorderFactory.createMatteBorder(
                topBorder, leftBorder, bottomBorder, rightBorder, Color.BLACK));

        Font font = new Font(Config.SUDOKU_FONT, Font.BOLD, 20);
        textField.setFont(font);
        textField.setForeground(Color.BLACK);
        textField.setBackground(Color.WHITE);
        textField.setOpaque(true);

        textField.setHorizontalAlignment(JTextField.CENTER);

        return textField;
    }

    /**
     * It lets user choose Sudoku image file
     * @throws IOException
     */
    private void chooseSudoku() throws IOException
    {
        // open a file dialog for selecting sudoku image
        FileDialog  fileDialog = new FileDialog(this,
                "Choose a file", FileDialog.LOAD);
        fileDialog.setDirectory(System.getProperty("user.dir"));
        fileDialog.setVisible(true);
        if(fileDialog.getFiles().length >= 1
                && fileDialog.getFiles()[0] != null)
        {
            try
            {
                File selectedFile = fileDialog.getFiles()[0];
                logger.info(selectedFile.getAbsolutePath());

                // first check if it is a png image file
                if(selectedFile.getName().endsWith(".png")
                        || selectedFile.getName().endsWith(".svg"))
                {
                    setSudokuImageFile(selectedFile);
                    sudokuGrid.requestFocus();
                }
                else
                {
                    logger.severe("selected file is not a png or svg file : "
                            + selectedFile.getName());
                    JOptionPane.showMessageDialog(new JFrame(),
                            "Selected file type is not \"png\". Currently only \"png\""
                                    + " files are supported. Please select another file.",
                                    "Invalid selected Sudoku image",
                                    JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (Exception e1)
            {
                logger.log(Level.SEVERE, e1.getMessage(), e1);
                throw e1;
            }
        }
        else
        {
            logger.info("No file selected");
        }
    }

    /**
     * resets sudoku board to display the current sudoku
     */
    private void resetSudokuBoard()
    {
        // check if there is a board
        if(sudokuGrid == null)
        {
            logger.severe("No Sudoku board found to reset");
            return;
        }

//        sudokuBoardResetter.run();
        EventQueue.invokeLater(sudokuBoardResetter);
    }

    /**
     * It resets sudoku board with sudoku from image file.
     * @param sudokuImageFile
     */
    public void setSudokuImageFile(File sudokuImageFile)
    {
        try
        {
            if(sudokuImageFile.exists())
            {
                // read image file to extract sudoku
                Sudoku currentSudoku = SudokuScanner.loadSudoku(sudokuImageFile);

                if(currentSudoku != null)
                {
                    // set sudoku and reset board
                    sudoku = currentSudoku;
                    resetSudokuBoard();
                }
                else
                {
                    logger.severe("Sudoku could not be located in the image file.");
                    JOptionPane.showMessageDialog(new JFrame(),
                            "No Sudoku could be found in the image file."
                                    + " Please check if the selected file has a Sudoku - \n\""
                                    + sudokuImageFile.getName() + "\"",
                                    "Error finding Sudoku",
                                    JOptionPane.ERROR_MESSAGE);
                }
            }
            else
            {
                logger.severe("Sudoku image file does not exist : "
                        + sudokuImageFile.getAbsolutePath());
            }
        } catch (Exception e)
        {
            logger.severe("Error loading Sudoku image file : " + sudokuImageFile.getName());
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * It returns current sudoku for the Sudoku board
     * @return
     */
    public Sudoku getSudoku()
    {
        return sudoku;
    }

    private Runnable sudokuBoardResetter = new Runnable()
    {
        public void run()
        {
            // set appropriate colors for values
            JTextField textField = null;
            Integer valueAtPosition = -1;
            for(int index = 0; index < 81; index++)
            {
                textField = ((JTextField) sudokuGrid.getComponent(index));

                // default settings for empty values in Sudoku
                textField.setText("");
                textField.setToolTipText("");
                textField.setEditable(true);
                textField.setForeground(Color.GRAY);

                valueAtPosition = sudoku.getValueAt(index);
                if(valueAtPosition > 0)
                {
                    // set the value if it is non-empty
                    textField.setText(valueAtPosition + "");

                    if(sudoku.hasFixedValueAt(index)) // given fixed values
                    {
                        // fixed values are not editable
                        textField.setEditable(false);

                        LikelyValue lowConfidenceLikelyValue = sudoku.getLowConfidenceValue(index);
                        if(lowConfidenceLikelyValue != null)
                            // fixed values classified with low confidence
                        {
                            textField.setForeground(Color.RED);
                            String tooltip = String.format("    "
                                    + "Confidence : %.2f",
                                    lowConfidenceLikelyValue.getConfidence())
                                    + String.format("       "
                                            + "Confidence Margin : %.2f"
                                            + "    ",
                                            lowConfidenceLikelyValue.getConfidenceMargin());
                            textField.setToolTipText(tooltip);
                        }
                        else
                        {
                            // confidently classified fixed values
                            textField.setForeground(Color.BLACK);
                        }
                    }
                }
            }
        }
    };

}
