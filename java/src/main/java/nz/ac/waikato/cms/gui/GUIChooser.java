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
 * GUIChooser.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.gui;

import nz.ac.waikato.cms.core.BrowserHelper;
import nz.ac.waikato.cms.core.Project;
import nz.ac.waikato.cms.gui.core.BaseFrame;
import nz.ac.waikato.cms.gui.core.BasePanel;
import nz.ac.waikato.cms.gui.core.GUIHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Allows user to choose tools.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class GUIChooser
  extends BasePanel {

  /** the button for hyperlinking grades. */
  protected JButton m_ButtonHyperLinkGrades;

  /** the button for overlaying filenames. */
  protected JButton m_ButtonOverlayFilename;

  /** the button for help. */
  protected JButton m_ButtonHelp;

  /** the button for closing the application. */
  protected JButton m_ButtonClose;

  /**
   * Initializes the widgets.
   */
  @Override
  protected void initGUI() {
    JPanel	panel;
    JPanel	panelRight;

    super.initGUI();

    setLayout(new BorderLayout(5, 5));

    add(new JLabel(GUIHelper.getIcon("waikato_large.png")), BorderLayout.CENTER);

    panelRight = new JPanel(new BorderLayout());
    add(panelRight, BorderLayout.EAST);

    panel = new JPanel(new GridLayout(0, 1, 5, 5));
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panelRight.add(panel, BorderLayout.NORTH);

    panel.add(new JLabel("Please choose"));

    m_ButtonHyperLinkGrades = new JButton("Hyperlink Grades");
    m_ButtonHyperLinkGrades.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
	BaseFrame frame = HyperLinkGradesGUI.createFrame();
	frame.setDefaultCloseOperation(BaseFrame.DISPOSE_ON_CLOSE);
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
      }
    });
    panel.add(m_ButtonHyperLinkGrades);

    m_ButtonOverlayFilename = new JButton("Overlay filename");
    m_ButtonOverlayFilename.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
	BaseFrame frame = OverlayFilenameGUI.createFrame();
	frame.setDefaultCloseOperation(BaseFrame.DISPOSE_ON_CLOSE);
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
      }
    });
    panel.add(m_ButtonOverlayFilename);

    panel.add(new JLabel());

    m_ButtonHelp = new JButton("Help", GUIHelper.getIcon("help.gif"));
    m_ButtonHelp.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
	BrowserHelper.openURL("https://github.com/fracpete/fcms-doc-modifier/wiki/Java-Tools");
      }
    });
    panel.add(m_ButtonHelp);

    m_ButtonClose = new JButton("Close", GUIHelper.getIcon("stop.gif"));
    m_ButtonClose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
	GUIHelper.closeParent(GUIChooser.this);
      }
    });
    panel.add(m_ButtonClose);
  }

  /**
   * Starts the GUI.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    Project.initialize();
    BaseFrame frame = new BaseFrame("FCMS Tools");
    frame.setDefaultCloseOperation(BaseFrame.EXIT_ON_CLOSE);
    GUIChooser panel = new GUIChooser();
    frame.getRootPane().setLayout(new BorderLayout());
    frame.getRootPane().add(panel);
    frame.pack();
    frame.setVisible(true);
  }
}
