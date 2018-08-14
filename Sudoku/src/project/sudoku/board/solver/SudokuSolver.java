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
package project.sudoku.board.solver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jpl7.Atom;
import org.jpl7.JPL;
import org.jpl7.Query;
import org.jpl7.Term;

import project.sudoku.board.model.Sudoku;
import project.sudoku.config.Config;
import project.sudoku.exception.ResourceLoadingException;

/**
 * Class that solves a given sudoku
 * <p>
 * It queries a
 * {@link <a href=http://www.swi-prolog.org/>SWI-PROLOG</a>} system which uses its built-in
 * {@link <a href="http://www.swi-prolog.org/man/clpfd.html">CLP(FD)</a>} library
 * to solve Sudoku as a constraint satisfaction problem.
 * 
 * Querying to Prolog system is done using Java API provided by SWI-PROLOG's
 * {@link <a href="http://www.swi-prolog.org/FAQ/Java.html">JPL</a>}. JPL
 * is integrated in SWI-Prolog distribution starting from 5.4.x (this project
 * uses SWI-Prolog-7.4.2).</p>
 * 
 * @since
 * Dated - 30-Jun-2017
 * 
 * @author S.Khan
 * 
 */
public class SudokuSolver
{
    private static Logger logger = Logger.getLogger(SudokuSolver.class.getName());

    private static boolean consulted = false;

    /**
     * It solves Sudoku. It queries SWI-PROLOG after consulting 'sudoku.pl',
     * which uses in-built CLP(FD) library to find a solution. The solution are
     * the instantiated row variables and each of these rows is parsed for integers.
     * <p>
     * The query is build as a text and passed to the constructor of {@link org.jpl7.Query}.
     * An example sudoku query text is shown below:
     * <pre>
     *   String queryText = ""
     *           + "Row0 = [3, _, _, _, _, _, _, _, 9],"
     *           + "Row1 = [_, _, _, 4, _, 3, _, _, _],"
     *           + "Row2 = [7, _, 4, 6, _, _, _, _, 5],"
     *           + "Row3 = [9, _, _, _, 2, _, _, 6, _],"
     *           + "Row4 = [_, _, 5, 8, 3, _, _, 4, _],"
     *           + "Row5 = [_, _, 8, _, 1, _, 3, _, _],"
     *           + "Row6 = [_, 8, 3, _, _, _, 6, _, _],"
     *           + "Row7 = [6, _, 9, _, _, 5, 2, _, 4],"
     *           + "Row8 = [_, _, _, _, _, 9, _, _, _],"
     *           + "sudoku([Row0, Row1, Row2, Row3, Row4, Row5, Row6, Row7, Row8]).";
     * </pre>
     * 
     * The returned solution is a map of String to Term, where key String are
     * the variable names from the query. Here is an example piece of code:
     * 
     * <pre>
     * Map<String, Term> solutions = sudokuQuery.oneSolution();
     * Term row0Term = solutions.get("Row0");
     * </pre>
     *	
     * </p>
     * 
     * @param sudoku the Sudoku to be solved.
     * @throws ResourceLoadingException throws this Exception when SWI-Prolog-JPL
     * native library could not be loaded or the prolog file is not loaded for solving Sudoku.
     * 
     */
    public static void solve(Sudoku sudoku) throws ResourceLoadingException
    {
        logger.info("Solve Sudoku");
        try
        {
            // first consult prolog file
            if(!consulted)
                tryConsulting();

            if(!consulted)
            {
                // consulting the prolog file failed
                logger.severe("consult to Prolog failed");
                return;
            }

            logger.info("consult succeeded");

            // build Sudoku query in String
            StringBuilder queryTextBuilder = new StringBuilder(System.lineSeparator());
            for(int rowNum = 0; rowNum < 9; rowNum++)
            {
                //add row variable for each row
                queryTextBuilder.append("Row").append(rowNum).append(" = [");
                for(int colNum = 0; colNum < 9; colNum++)
                {
                    // get values for index 0 to 80 by using index = 9*row + col
                    Integer intValue = sudoku.getValueAt(9*rowNum + colNum);

                    // add "_" if value is < 1 else add the number
                    if(intValue < 1)
                    {
                        queryTextBuilder.append("_");
                    }
                    else
                    {
                        queryTextBuilder.append(intValue);
                    }

                    // append "," for column values unless it is the last column 
                    if(colNum < 8)
                        queryTextBuilder.append(", ");
                }

                // end row with "], " and add a new line
                queryTextBuilder.append("], ").append(System.lineSeparator());
            }
            queryTextBuilder.append("sudoku([Row0, Row1, Row2, Row3, Row4, Row5, Row6, Row7, Row8]).");

            String queryText = queryTextBuilder.toString();
            logger.info("Sudoku query = " + queryText);

            // make a query to Prolog
            Query sudokuQuery = new Query(queryText);
            Map<String, Term> solutions = sudokuQuery.oneSolution();
            sudokuQuery.close();

            // return if there is no solution
            if(solutions == null)
            {
                logger.info("NO SOLUTION FOUND FOR THIS SUDOKU");
                return;
            }

            logger.info("Solution found");

            // iterate over solution for each variable
            List<Integer> rowValues;
            Term term = null;
            int colNum = 0;
            for(int rowIndex = 0; rowIndex < 9; rowIndex++)
            {
                term = solutions.get("Row" + rowIndex);
                if(term != null)
                {
                    rowValues = getIntegers(term.toTermArray());

                    logger.info("Row" + rowIndex + " = " + rowValues.toString());

                    // re-initialize columns for each row
                    colNum = 0;
                    for(Integer colValue : rowValues)
                    {
                        // set this value at position index = 9*row + column
                        sudoku.setValueAt(9*rowIndex + colNum, colValue);
                        colNum++;
                    }
                }
                else
                {
                    logger.severe("Null value for Row" + rowIndex);
                }
            }

        }
        catch (ResourceLoadingException rle)
        {
            logger.log(Level.SEVERE, rle.getMessage(), rle);
            throw rle;
        }
    }

    /**
     * loads JPL library and consults prolog file
     * @throws ResourceLoadingException 
     */
    private static void tryConsulting() throws ResourceLoadingException
    {
        if(!consulted)
        {
            // load SWI-Prolog-JPL library
            try
            {
                logger.info("load Prolog JPL Library");
                JPL.loadNativeLibrary();
            }
            catch (UnsatisfiedLinkError e)
            {
                throw new ResourceLoadingException("Error Loading JPL library", e);
            }

            // consult Prolog file
            File plFile = new File(Config.SUDOKU_PL_FILE);

            if(!plFile.exists())
            {
                logger.severe("Error Loading Prolog file : " + plFile.getPath());
                throw new ResourceLoadingException("Error Loading Prolog file "
                        + plFile.getAbsolutePath());
            }

            logger.info("Prolog File : " + plFile.getPath());
            Query consultQuery = new Query("consult",
                    new Term[] {new Atom(plFile.getAbsolutePath())});

            // set the variable if successfully consulted
            consulted = consultQuery.hasSolution();
        }
    }

    /**
     * It returns list of integers from an array of Prolog terms.
     * It adds 0 to the list when a term is not an integer.
     * @param termArray array of {@link org.jpl7.Term}
     * @return list of integers
     */
    private static List<Integer> getIntegers(Term[] termArray)
    {
        List<Integer> integerTerms = new ArrayList<Integer>();
        for(Term thisTerm : termArray)
        {
            if(thisTerm.isInteger())
            {
                integerTerms.add(thisTerm.intValue());
            }
            else // 0 symbolizes unknown value
            {
                integerTerms.add(Integer.valueOf(0));
            }
        }
        return integerTerms;
    }
}
