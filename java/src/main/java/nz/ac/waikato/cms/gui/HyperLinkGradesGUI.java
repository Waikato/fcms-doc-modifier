/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * HyperLinkGradesGUI.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.gui;

import nz.ac.waikato.cms.gui.core.BaseDirectoryChooser;
import nz.ac.waikato.cms.gui.core.BaseFileChooser;
import nz.ac.waikato.cms.gui.core.BaseFrame;
import nz.ac.waikato.cms.gui.core.BasePanel;
import nz.ac.waikato.cms.gui.core.ExtensionFileFilter;

import java.awt.BorderLayout;

/**
 * For hyperlinking pages in PDFs where Grades were found that matched a
 * regular expression.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class HyperLinkGradesGUI
  extends BasePanel {

  /** the file chooser to use. */
  protected BaseFileChooser m_FileChooser;

  /** the directory chooser to use. */
  protected BaseDirectoryChooser m_DirChooser;

  /**
   * Initializes the members.
   */
  protected void initialize() {
    m_FileChooser = new BaseFileChooser();
    m_FileChooser.addChoosableFileFilter(ExtensionFileFilter.getPdfFileFilter());
    m_FileChooser.setAcceptAllFileFilterUsed(false);
    m_FileChooser.setMultiSelectionEnabled(true);

    m_DirChooser = new BaseDirectoryChooser();
  }

  /**
   * Initializes the widgets.
   */
  protected void initGUI() {
    // TODO
  }

  /**
   * Starts the GUI.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    BaseFrame frame = new BaseFrame("Hyperlink Grades");
    frame.setDefaultCloseOperation(BaseFrame.EXIT_ON_CLOSE);
    HyperLinkGradesGUI panel = new HyperLinkGradesGUI();
    frame.getRootPane().setLayout(new BorderLayout());
    frame.getRootPane().add(panel);
    frame.setSize(600, 400);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
