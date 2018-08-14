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
package project.sudoku;

import java.awt.EventQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import project.sudoku.board.scanner.SudokuScanner;
import project.sudoku.board.ui.SudokuBoard;
import project.sudoku.config.Config;
import project.sudoku.exception.ResourceLoadingException;

/**
 * Main class for starting the application
 * 
 * @since
 * Dated - 15-Jun-2017
 * 
 * @author S.Khan
 * 
 */
public class SudokuMain
{
    private static Logger logger = Logger.getLogger(SudokuMain.class.getName());

    /**
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            // set log file for logging messages
            FileHandler fileHandler = new FileHandler(Config.LOG_FILE, true);
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fileHandler);

            logger.info("\n\n"
                    + "1-2-3-4-5-6-7-8-9-1-2-3-4-5-6-7-8-9-1-2-3-4-5-6-7-8-9-1-2-3-4-5-6-7-8-9\n"
                    + "1-2-3-4-5-6-7-8-9                                     1-2-3-4-5-6-7-8-9\n"
                    + "1-2-3-4-5-6-7-8-9    S T A R T I N G   S U D O K U    1-2-3-4-5-6-7-8-9\n"
                    + "1-2-3-4-5-6-7-8-9                                     1-2-3-4-5-6-7-8-9\n"
                    + "1-2-3-4-5-6-7-8-9-1-2-3-4-5-6-7-8-9-1-2-3-4-5-6-7-8-9-1-2-3-4-5-6-7-8-9\n\n");

            // load OpenCV library
            logger.info("loading OpenCV library : " + Config.OPENCV_LIBRARY);
            try
            {
                System.loadLibrary(Config.OPENCV_LIBRARY);
            } catch (UnsatisfiedLinkError e)
            {
                logger.severe("Error loading OpenCV");
                logger.log(Level.SEVERE, e.getMessage(), e);
                // show error message before closing
                JOptionPane.showMessageDialog(new JPanel(),
                        "There was an error while loading OpenCV library. "
                                + "Check if \"lib\" folder is added to the path. Error :\n"
                                + e.getMessage(),
                                "Error Loading OpenCV", JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }

            // load scanner resources e.g. neural network
            try
            {
                SudokuScanner.loadResources();
            } catch (ResourceLoadingException e)
            {
                logger.severe("Error loading neural network");
                logger.log(Level.SEVERE, e.getMessage(), e);
                // show error before closing
                JOptionPane.showMessageDialog(new JPanel(),
                        "Neural network file could not be found :\n" + e.getMessage(),
                        "Error Loading Files", JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }

            // load sudoku window
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    SudokuBoard sudokuBoard = new SudokuBoard();
                    sudokuBoard.setLocation(600, 250);
                    sudokuBoard.validate();
                    sudokuBoard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    sudokuBoard.setVisible(true);
                }
            });
        }
        catch (Exception e)
        {
            logger.severe("Error starting application");
            logger.log(Level.SEVERE, e.getMessage(), e);
            JOptionPane.showMessageDialog(new JPanel(),
                    "Something went wrong :\n" + e.getMessage(),
                    "Closing Application", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }
}
