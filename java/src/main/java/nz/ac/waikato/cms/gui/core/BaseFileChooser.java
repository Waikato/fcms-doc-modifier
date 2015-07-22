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
 * BaseFileChooser.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.gui.core;

import javax.swing.JFileChooser;
import java.io.File;

/**
 * FileChooser with a bookmarks panel.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class BaseFileChooser
  extends JFileChooser {

  /**
   * Initializes the file chooser.
   */
  public BaseFileChooser() {
    super();
    initialize();
    initGUI();
    finishInit();
  }

  /**
   * Initializes the file chooser with the current directory.
   *
   * @param currentDir	the current directory to use
   */
  public BaseFileChooser(String currentDir) {
    super(currentDir);
    initialize();
    initGUI();
    finishInit();
  }

  /**
   * Initializes the file chooser with the current directory.
   *
   * @param currentDir	the current directory to use
   */
  public BaseFileChooser(File currentDir) {
    super(currentDir);
    initialize();
    initGUI();
    finishInit();
  }

  /**
   * Initializes the members.
   */
  protected void initialize() {
  }

  /**
   * Initializes the widgets.
   */
  protected void initGUI() {
    DirectoryBookmarks.FileChooserBookmarksPanel 	panel;

    panel = new DirectoryBookmarks.FileChooserBookmarksPanel();
    panel.setOwner(this);
    setAccessory(panel);
  }

  /**
   * Finishes up the initialization.
   */
  protected void finishInit() {
  }
}
