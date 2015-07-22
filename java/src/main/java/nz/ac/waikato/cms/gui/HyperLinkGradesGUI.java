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

import nz.ac.waikato.cms.core.FileUtils;
import nz.ac.waikato.cms.doc.HyperLinkGrades;
import nz.ac.waikato.cms.doc.HyperLinkGrades.Location;
import nz.ac.waikato.cms.gui.core.BaseDirectoryChooser;
import nz.ac.waikato.cms.gui.core.BaseFileChooser;
import nz.ac.waikato.cms.gui.core.BaseFrame;
import nz.ac.waikato.cms.gui.core.BasePanel;
import nz.ac.waikato.cms.gui.core.BaseScrollPane;
import nz.ac.waikato.cms.gui.core.ExtensionFileFilter;
import nz.ac.waikato.cms.gui.core.GUIHelper;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

  /** the input files. */
  protected JList m_ListInputFiles;

  /** the model for the input files. */
  protected DefaultListModel<File> m_ModelInputFiles;

  /** the button for adding files. */
  protected JButton m_ButtonAddFiles;

  /** the button for removing selected files. */
  protected JButton m_ButtonRemoveFiles;

  /** the button for removing all files. */
  protected JButton m_ButtonRemoveAllFiles;

  /** the expression. */
  protected JTextField m_TextExpression;

  /** the output directory. */
  protected JTextField m_TextOutputDir;

  /** the button for selecting the output directory. */
  protected JButton m_ButtonOutputDir;

  /** the suffix. */
  protected JTextField m_TextSuffix;

  /** the checkbox for case-sensitive search. */
  protected JCheckBox m_CheckBoxCaseSensitive;

  /** the checkbox for excluding completions. */
  protected JCheckBox m_CheckBoxExcludeCompletions;

  /** the button for indexing the files. */
  protected JButton m_ButtonIndex;

  /** the button for closing the frame. */
  protected JButton m_ButtonClose;

  /** the label for the progress. */
  protected JLabel m_LabelProgress;

  /** whether files are being processed. */
  protected boolean m_Processing;

  /**
   * Initializes the members.
   */
  protected void initialize() {
    m_FileChooser = new BaseFileChooser();
    m_FileChooser.addChoosableFileFilter(ExtensionFileFilter.getPdfFileFilter());
    m_FileChooser.setAcceptAllFileFilterUsed(false);
    m_FileChooser.setMultiSelectionEnabled(true);

    m_DirChooser = new BaseDirectoryChooser();

    m_Processing = false;
  }

  /**
   * Initializes the widgets.
   */
  protected void initGUI() {
    JPanel		panelFiles;
    JPanel		panelParams;
    List<JLabel>	labels;

    setLayout(new BorderLayout());

    panelFiles = new JPanel(new BorderLayout());
    panelFiles.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(panelFiles, BorderLayout.CENTER);

    // input files
    {
      JLabel label = new JLabel("Input files");
      panelFiles.add(label, BorderLayout.NORTH);
      JPanel panel = new JPanel(new BorderLayout());
      panelFiles.add(panel, BorderLayout.CENTER);
      m_ModelInputFiles = new DefaultListModel<>();
      m_ListInputFiles  = new JList(m_ModelInputFiles);
      m_ListInputFiles.addListSelectionListener(new ListSelectionListener() {
	@Override
	public void valueChanged(ListSelectionEvent e) {
	  updateButtons();
	}
      });
      panel.add(new BaseScrollPane(m_ListInputFiles), BorderLayout.CENTER);

      JPanel panelRight = new JPanel(new BorderLayout());
      panel.add(panelRight, BorderLayout.EAST);
      JPanel panelButtons = new JPanel(new GridLayout(3, 1));
      panelButtons.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      panelRight.add(panelButtons, BorderLayout.NORTH);

      m_ButtonAddFiles = new JButton("Add...");
      m_ButtonAddFiles.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  int retVal = m_FileChooser.showOpenDialog(HyperLinkGradesGUI.this);
	  if (retVal != BaseFileChooser.APPROVE_OPTION)
	    return;
	  File[] files = m_FileChooser.getSelectedFiles();
	  for (File file: files)
	    m_ModelInputFiles.addElement(file);
	  updateButtons();
	}
      });
      panelButtons.add(m_ButtonAddFiles);

      m_ButtonRemoveFiles = new JButton("Remove");
      m_ButtonRemoveFiles.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  int[] indices = m_ListInputFiles.getSelectedIndices();
	  for (int i = indices.length - 1; i >= 0; i--)
	    m_ModelInputFiles.remove(indices[i]);
	  updateButtons();
	}
      });
      panelButtons.add(m_ButtonRemoveFiles);

      m_ButtonRemoveAllFiles = new JButton("Remove all");
      m_ButtonRemoveAllFiles.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  m_ModelInputFiles.removeAllElements();
	  updateButtons();
	}
      });
      panelButtons.add(m_ButtonRemoveAllFiles);
    }

    // the parameters
    labels      = new ArrayList<>();
    panelParams = new JPanel(new GridLayout(5, 1));
    panelFiles.add(panelParams, BorderLayout.SOUTH);
    // expression
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_TextExpression = new JTextField(30);
      m_TextExpression.getDocument().addDocumentListener(new DocumentListener() {
	@Override
	public void insertUpdate(DocumentEvent e) {
	  checkExpression();
	}
	@Override
	public void removeUpdate(DocumentEvent e) {
	  checkExpression();
	}
	@Override
	public void changedUpdate(DocumentEvent e) {
	  checkExpression();
	}
	protected void checkExpression() {
	  if (m_TextExpression.getText().trim().isEmpty() || isValidExpression())
	    m_TextExpression.setForeground(Color.BLACK);
	  else
	    m_TextExpression.setForeground(Color.RED);
	  updateButtons();
	}
      });
      JLabel label = new JLabel("Expression");
      label.setDisplayedMnemonic('x');
      label.setLabelFor(m_TextExpression);
      panel.add(label);
      panel.add(m_TextExpression);
      labels.add(label);
    }
    // output dir
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_TextOutputDir = new JTextField(30);
      m_TextOutputDir.getDocument().addDocumentListener(new DocumentListener() {
	@Override
	public void insertUpdate(DocumentEvent e) {
	  checkDir();
	}
	@Override
	public void removeUpdate(DocumentEvent e) {
	  checkDir();
	}
	@Override
	public void changedUpdate(DocumentEvent e) {
	  checkDir();
	}
	protected void checkDir() {
	  if (m_TextOutputDir.getText().trim().isEmpty() || isValidOutputDir())
	    m_TextOutputDir.setForeground(Color.BLACK);
	  else
	    m_TextOutputDir.setForeground(Color.RED);
	  updateButtons();
	}
      });
      JLabel label = new JLabel("Output directory");
      label.setDisplayedMnemonic('O');
      label.setLabelFor(m_TextOutputDir);

      m_ButtonOutputDir = new JButton("...");
      m_ButtonOutputDir.setPreferredSize(new Dimension((int) m_ButtonOutputDir.getPreferredSize().getWidth(), (int) m_TextOutputDir.getPreferredSize().getHeight()));
      m_ButtonOutputDir.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  if (!m_TextOutputDir.getText().isEmpty())
	    m_DirChooser.setCurrentDirectory(new File(m_TextOutputDir.getText()));
	  int retVal = m_DirChooser.showOpenDialog(HyperLinkGradesGUI.this);
	  if (retVal != BaseDirectoryChooser.APPROVE_OPTION)
	    return;
	  m_TextOutputDir.setText(m_DirChooser.getSelectedFile().getAbsolutePath());
	}
      });

      panel.add(label);
      panel.add(m_TextOutputDir);
      panel.add(m_ButtonOutputDir);
      labels.add(label);
    }
    // suffix
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_TextSuffix = new JTextField(30);
      JLabel label = new JLabel("Suffix");
      label.setDisplayedMnemonic('S');
      label.setLabelFor(m_TextSuffix);
      panel.add(label);
      panel.add(m_TextSuffix);
      labels.add(label);
    }
    // case sensitive
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_CheckBoxCaseSensitive = new JCheckBox("");
      JLabel label = new JLabel("Case-sensitive matching");
      label.setDisplayedMnemonic('m');
      label.setLabelFor(m_CheckBoxCaseSensitive);
      panel.add(label);
      panel.add(m_CheckBoxCaseSensitive);
      labels.add(label);
    }
    // no completions
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_CheckBoxExcludeCompletions = new JCheckBox("");
      JLabel label = new JLabel("Exclude completions");
      label.setDisplayedMnemonic('p');
      label.setLabelFor(m_CheckBoxExcludeCompletions);
      panel.add(label);
      panel.add(m_CheckBoxExcludeCompletions);
      labels.add(label);
    }

    // the buttons at the bottom
    {
      JPanel panelBottom = new JPanel(new BorderLayout());
      add(panelBottom, BorderLayout.SOUTH);

      JPanel panelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
      panelBottom.add(panelLeft, BorderLayout.WEST);
      m_LabelProgress = new JLabel("");
      panelLeft.add(m_LabelProgress);

      JPanel panelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      panelBottom.add(panelRight, BorderLayout.EAST);

      m_ButtonIndex = new JButton("Index");
      m_ButtonIndex.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  indexFiles();
	}
      });
      panelRight.add(m_ButtonIndex);

      m_ButtonClose = new JButton("Close");
      m_ButtonClose.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  GUIHelper.closeParent(HyperLinkGradesGUI.this);
	}
      });
      panelRight.add(m_ButtonClose);
    }

    // adjust labels
    validate();
    int max = 0;
    for (JLabel label: labels) {
      if (label.getPreferredSize().getWidth() > max)
	max = (int) label.getPreferredSize().getWidth();
    }
    max += 5;
    for (JLabel label: labels)
      label.setPreferredSize(new Dimension(max, (int) label.getPreferredSize().getHeight()));
  }

  /**
   * Finishes up the initialization.
   */
  @Override
  protected void finishInit() {
    super.finishInit();
    updateButtons();
  }

  /**
   * Indexes the files.
   */
  protected void indexFiles() {
    SwingWorker		worker;

    m_Processing = true;
    updateButtons();

    worker = new SwingWorker() {
      protected StringBuilder m_Errors;
      @Override
      protected Object doInBackground() throws Exception {
	m_Errors = new StringBuilder();
	for (int i = 0; i < m_ModelInputFiles.getSize(); i++) {
	  File fileIn = m_ModelInputFiles.get(i);
	  File fileOut = FileUtils.replaceExtension(fileIn, m_TextSuffix.getText() + ".pdf");
	  m_LabelProgress.setText("Processing " + (i+1) + "/" + m_ModelInputFiles.getSize() + "...");
	  try {
	    List<Location> locations = HyperLinkGrades.locate(
	      fileIn,
	      m_TextExpression.getText(),
	      m_CheckBoxCaseSensitive.isSelected(),
	      m_CheckBoxExcludeCompletions.isSelected());
	    HyperLinkGrades.addIndex(
	      locations,
	      fileIn,
	      fileOut);
	  }
	  catch (Exception e) {
	    m_Errors.append("Failed to process: " + fileIn + "\n");
	    System.err.println("Failed to process: " + fileIn);
	    e.printStackTrace();
	  }
	}
	return null;
      }
      @Override
      protected void done() {
	m_LabelProgress.setText("");
	m_Processing = false;
	updateButtons();
	if (m_Errors.length() > 0) {
	  JOptionPane.showMessageDialog(
	    HyperLinkGradesGUI.this,
	    m_Errors.toString(),
	    "Error",
	    JOptionPane.ERROR_MESSAGE);
	}
	super.done();
      }
    };

    worker.execute();
  }

  /**
   * Returns whether the expression is valid.
   *
   * @return		true if valid
   */
  protected boolean isValidExpression() {
    if (m_TextExpression.getText().trim().isEmpty())
      return false;

    try {
      Pattern.compile(m_TextExpression.getText());
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  /**
   * Returns whether the output dir is valid.
   *
   * @return		true if valid
   */
  protected boolean isValidOutputDir() {
    File	file;

    if (m_TextOutputDir.getText().trim().isEmpty())
      return false;

    try {
      file = new File(m_TextOutputDir.getText());
      return file.exists() && file.isDirectory();
    }
    catch (Exception e) {
      return false;
    }
  }

  /**
   * Returns whether files can be indexed.
   *
   * @return		true if possible
   */
  protected boolean canIndexFiles() {
    boolean	result;

    result = !m_Processing
      && (m_ModelInputFiles.getSize() > 0)
      && isValidExpression()
      && isValidOutputDir();

    return result;
  }

  /**
   * Updates the state of the buttons.
   */
  protected void updateButtons() {
    m_ButtonAddFiles.setEnabled(!m_Processing);
    m_ButtonRemoveFiles.setEnabled(!m_Processing && (m_ListInputFiles.getSelectedIndices().length > 0));
    m_ButtonRemoveAllFiles.setEnabled(!m_Processing && (m_ModelInputFiles.getSize() > 0));

    m_ButtonClose.setEnabled(!m_Processing);
    m_ButtonIndex.setEnabled(canIndexFiles());
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
