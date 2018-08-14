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
package project.sudoku.config;

import java.io.File;

/**
 * Config class for storing various project configuration information
 * 
 * @since
 * Dated - 15-Jun-2017
 * 
 * @author S.Khan
 * 
 */
public interface Config
{
    // version of the project
    public static final String PROJECT_VERSION = "1.0.0";

    // name of opencv library to be loaded
    public static final String OPENCV_LIBRARY = "opencv_java320";

    // Font for displaying Sudoku
    public static final String SUDOKU_FONT = "Comic Sans MS";

    // name of log file
    public static final String LOG_FILE = ".sudoku-log.txt";

    // folder containing resources for running the application
    public static final String RESOURCES_FOLDER = ".resources";

    // Sudoku icon for window
    public static final String SUDOKU_ICON = RESOURCES_FOLDER
            + File.separator + "sudoku_icon.png";

    // default Sudoku image for loading in the application
    public static final String DEFAULT_SUDOKU_IMAGE_FILE = RESOURCES_FOLDER
            + File.separator + "sudoku_default.png";

    // trained neural network file
    public static final String NETWORK_FILE = RESOURCES_FOLDER
            + File.separator + "trained_network.nnet";

    // Prolog file for solving Sudoku
    public static final String SUDOKU_PL_FILE = RESOURCES_FOLDER
            + File.separator + "solver.pl";

}
