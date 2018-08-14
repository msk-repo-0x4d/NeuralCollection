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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import project.sudoku.config.Config;

/**
 * Window displaying About information for Project
 * 
 * @since
 * Dated - 05-Aug-2017
 * 
 * @author S.Khan
 * 
 */
public class AboutProjectFrame extends JFrame
{
    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(AboutProjectFrame.class.getName());

    private static final String URI_LINK = "https://github.com/msk-repo-0x4d";

    /**
     * @throws HeadlessException
     */
    public AboutProjectFrame() throws HeadlessException
    {
        super("Sudoku Board - v" + Config.PROJECT_VERSION);

        setMinimumSize(new Dimension(420, 300));
        setIconImage(Toolkit.getDefaultToolkit().getImage(Config.SUDOKU_ICON));

        addContent();
        pack();
    }

    /**
     * adds contents to frame
     */
    protected void addContent()
    {
        logger.info("Loading frame for About Project information");

        setAlwaysOnTop(true);

        // set box layout
        getContentPane().setLayout(new BoxLayout(
                getContentPane(), BoxLayout.Y_AXIS));

        // for displaying version information
        JLabel versionLabel = new JLabel("Version              -   1.0.0");
        versionLabel.setBorder(new EmptyBorder(10, 10, 0, 10));
        
        // for displaying author information
        JLabel authorLabel = new JLabel("Author                -   M.S.Khan");
        authorLabel.setBorder(new EmptyBorder(5, 10, 0, 10));

        // for displaying license information
        JLabel licenseLabel = new JLabel("Copyright           -   (c) 2017 M.S.Khan (Apache License 2.0)");
        licenseLabel.setBorder(new EmptyBorder(5, 10, 0, 10));

        // for displaying source code information
        JLabel githubLabel = new JLabel("<html>Source code   &emsp;-&ensp;"
                                  + "<a href=\"" + URI_LINK
                                  + "\">github.com/msk-repo-0x4d</a></html>");
        githubLabel.setBorder(new EmptyBorder(5, 10, 0, 10));
        // open link in browser
        githubLabel.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                try
                {
                    // check if platform supports browse action
                    if(Desktop.isDesktopSupported() &&
                            Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                    {
                        // browse to the link
                        Desktop.getDesktop().browse(new URI(URI_LINK));
                    }
                } catch (IOException e1)
                {
                    logger.severe(e1.getMessage());
                } catch (URISyntaxException e1)
                {
                    logger.severe(e1.getMessage());
                }
            }
        });

        // for displaying details about the project
        JLabel detailLabel = new JLabel("Details");
        detailLabel.setBorder(new EmptyBorder(5, 10, 0, 10));

        // text area for details
        JTextArea detailText = new JTextArea();
        detailText.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        detailText.setBorder(new EmptyBorder(5, 5, 5, 5));
        detailText.setText("This is an open source project that uses Prolog and "
                + "Neural Network for playing Sudoku. It uses Prolog's "
                + "CLP-FD (Constraint Logic Programming over Finite Domain) library to solve Sudoku as "
                + "a constraint satisfaction problem. For uploading a Sudoku, it uses "
                + "image files as its input and extracts Sudoku from the image. It uses "
                + "OpenCV library to preprocess image file and extract digit pixels from a Sudoku box. "
                + "The digit pixel values are then fed to a trained Neural Network (Neuroph library) for "
                + "recognizing the digits. Below is a list of third party libraries that "
                + "were used in this project."
                + "\n\n------------------LIBRARIES------------------\n"
                + "\n- OpenCV-3.2.0 ( http://www.opencv.org )"
                + "\n- Neuroph-2.93 ( http://www.neuroph.sourceforge.net )"
                + "\n- SWI-Prolog-7.4.2 ( http://www.swi-prolog.org )"
                + "\n");
        detailText.setColumns(30);
        detailText.setRows(10);
        detailText.setLineWrap(true);
        detailText.setWrapStyleWord(true);
        detailText.setEditable(false);

        // wrap the text area inside a scrolling pane
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(new CompoundBorder(new EmptyBorder(2, 10, 10, 10),
                BorderFactory.createLineBorder(Color.BLACK)));
        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(detailText);

        // set same alignment for all of the components
        versionLabel.setAlignmentX(LEFT_ALIGNMENT);
        authorLabel.setAlignmentX(LEFT_ALIGNMENT);
        licenseLabel.setAlignmentX(LEFT_ALIGNMENT);
        githubLabel.setAlignmentX(LEFT_ALIGNMENT);
        detailLabel.setAlignmentX(LEFT_ALIGNMENT);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);

        // add components directly to frame
        add(versionLabel);
        add(authorLabel);
        add(licenseLabel);
        add(githubLabel);
        add(detailLabel);
        add(scrollPane);
    }
}
