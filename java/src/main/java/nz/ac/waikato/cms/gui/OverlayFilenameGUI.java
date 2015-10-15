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
 * OverlayFilenameGUI.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.gui;

import nz.ac.waikato.cms.core.Project;
import nz.ac.waikato.cms.doc.OverlayFilename;
import nz.ac.waikato.cms.gui.core.BaseDirectoryChooser;
import nz.ac.waikato.cms.gui.core.BaseFileChooser;
import nz.ac.waikato.cms.gui.core.BaseFrame;
import nz.ac.waikato.cms.gui.core.BaseScrollPane;
import nz.ac.waikato.cms.gui.core.ExtensionFileFilter;
import nz.ac.waikato.cms.gui.core.GUIHelper;
import nz.ac.waikato.cms.gui.core.SetupPanel;

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
import java.util.Properties;

/**
 * For overlaying the filename on PDF files.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class OverlayFilenameGUI
  extends SetupPanel {

  public static final String OUTPUT_DIR = "OutputDir";

  public static final String V_POS = "VPos";

  public static final String H_POS = "HPos";

  public static final String STRIP_PATH = "StripPath";

  public static final String STRIP_EXT = "StripExt";

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

  /** the output directory. */
  protected JTextField m_TextOutputDir;

  /** the button for selecting the output directory. */
  protected JButton m_ButtonOutputDir;

  /** the vertical position. */
  protected JTextField m_TextVPos;

  /** the horizontal position. */
  protected JTextField m_TextHPos;

  /** the checkbox for stripping the path. */
  protected JCheckBox m_CheckBoxStripPath;

  /** the checkbox for stripping the extension. */
  protected JCheckBox m_CheckBoxStripExt;

  /** the button for overlaying the files. */
  protected JButton m_ButtonOverlay;

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
	  int retVal = m_FileChooser.showOpenDialog(OverlayFilenameGUI.this);
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
          int retVal = m_DirChooser.showOpenDialog(OverlayFilenameGUI.this);
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
    // vertical position
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_TextVPos = new JTextField("20", 5);
      m_TextVPos.getDocument().addDocumentListener(new DocumentListener() {
	@Override
	public void insertUpdate(DocumentEvent e) {
	  checkPos();
	}
	@Override
	public void removeUpdate(DocumentEvent e) {
	  checkPos();
	}
	@Override
	public void changedUpdate(DocumentEvent e) {
	  checkPos();
	}
	protected void checkPos() {
	  if (m_TextVPos.getText().trim().isEmpty() || isValidVPos())
	    m_TextVPos.setForeground(Color.BLACK);
	  else
	    m_TextVPos.setForeground(Color.RED);
	  updateButtons();
	}
      });
      JLabel label = new JLabel("Vertical position");
      label.setDisplayedMnemonic('V');
      label.setLabelFor(m_TextVPos);
      panel.add(label);
      panel.add(m_TextVPos);
      labels.add(label);
    }
    // horizontal position
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_TextHPos = new JTextField("10", 5);
      m_TextHPos.getDocument().addDocumentListener(new DocumentListener() {
	@Override
	public void insertUpdate(DocumentEvent e) {
	  checkPos();
	}
	@Override
	public void removeUpdate(DocumentEvent e) {
	  checkPos();
	}
	@Override
	public void changedUpdate(DocumentEvent e) {
	  checkPos();
	}
	protected void checkPos() {
	  if (m_TextHPos.getText().trim().isEmpty() || isValidHPos())
	    m_TextHPos.setForeground(Color.BLACK);
	  else
	    m_TextHPos.setForeground(Color.RED);
	  updateButtons();
	}
      });
      JLabel label = new JLabel("Horizontal position");
      label.setDisplayedMnemonic('H');
      label.setLabelFor(m_TextHPos);
      panel.add(label);
      panel.add(m_TextHPos);
      labels.add(label);
    }
    // strip path
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_CheckBoxStripPath = new JCheckBox("");
      JLabel label = new JLabel("Strip path");
      label.setDisplayedMnemonic('p');
      label.setLabelFor(m_CheckBoxStripPath);
      panel.add(label);
      panel.add(m_CheckBoxStripPath);
      labels.add(label);
    }
    // strip extension
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_CheckBoxStripExt = new JCheckBox("");
      JLabel label = new JLabel("Strip extension");
      label.setDisplayedMnemonic('e');
      label.setLabelFor(m_CheckBoxStripExt);
      panel.add(label);
      panel.add(m_CheckBoxStripExt);
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

      m_ButtonOverlay = new JButton("Overlay", GUIHelper.getIcon("run.gif"));
      m_ButtonOverlay.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  process();
	}
      });
      panelRight.add(m_ButtonOverlay);

      m_ButtonClose = new JButton("Close", GUIHelper.getIcon("stop.gif"));
      m_ButtonClose.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  GUIHelper.closeParent(OverlayFilenameGUI.this);
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
   * Overlays the files.
   */
  protected void process() {
    SwingWorker		worker;

    m_Processing = true;
    updateButtons();

    worker = new SwingWorker() {
      protected StringBuilder m_Errors;
      @Override
      protected Object doInBackground() throws Exception {
	saveSetup();
	m_Errors = new StringBuilder();
        OverlayFilename of = new OverlayFilename();
        int vpos = Integer.parseInt(m_TextVPos.getText());
        int hpos = Integer.parseInt(m_TextHPos.getText());
	for (int i = 0; i < m_ModelInputFiles.getSize(); i++) {
	  File fileIn = m_ModelInputFiles.get(i);
	  File fileOut = new File(m_TextOutputDir.getText());
          m_LabelProgress.setText("Processing " + (i + 1) + "/" + m_ModelInputFiles.getSize() + "...");
          File files[][] = of.determineFiles(fileIn, fileOut);
          for (int n = 0; n < files[0].length; n++) {
            try {
              of.overlay(files[0][n], files[1][n], vpos, hpos, m_CheckBoxStripPath.isSelected(), m_CheckBoxStripExt.isSelected(), null);
            }
            catch (Exception e) {
              m_Errors.append("Failed to process: " + files[0][n] + " -> " + files[1][n] + "\n");
              System.err.println("Failed to process: " + files[0][n] + " -> " + files[1][n]);
              e.printStackTrace();
            }
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
	    OverlayFilenameGUI.this,
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
   * Returns whether the vertical position is valid.
   *
   * @return		true if valid
   */
  protected boolean isValidVPos() {
    if (m_TextVPos.getText().trim().isEmpty())
      return false;

    try {
      Integer.parseInt(m_TextVPos.getText());
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  /**
   * Returns whether the horizontal position is valid.
   *
   * @return		true if valid
   */
  protected boolean isValidHPos() {
    if (m_TextHPos.getText().trim().isEmpty())
      return false;

    try {
      Integer.parseInt(m_TextHPos.getText());
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
   * Returns whether files can be overlayed.
   *
   * @return		true if possible
   */
  protected boolean canOverlayFiles() {
    boolean	result;

    result = !m_Processing
      && (m_ModelInputFiles.getSize() > 0)
      && isValidVPos()
      && isValidHPos()
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
    m_ButtonOverlay.setEnabled(canOverlayFiles());
  }

  /**
   * Maps the properties back to the GUI.
   *
   * @param props       the properties to use
   */
  protected void propsToGUI(Properties props) {
    m_TextOutputDir.setText(props.getProperty(OUTPUT_DIR, ""));
    m_TextVPos.setText(props.getProperty(V_POS, "20"));
    m_TextHPos.setText(props.getProperty(H_POS, "10"));
    m_CheckBoxStripPath.setSelected(props.getProperty(STRIP_PATH, "false").equals("true"));
    m_CheckBoxStripExt.setSelected(props.getProperty(STRIP_EXT, "false").equals("true"));
  }

  /**
   * Maps the GUI to a properties object.
   *
   * @return            the properties
   */
  protected Properties guiToProps() {
    Properties result;

    result = new Properties();
    result.setProperty(OUTPUT_DIR, m_TextOutputDir.getText());
    result.setProperty(V_POS, m_TextVPos.getText());
    result.setProperty(H_POS, m_TextHPos.getText());
    result.setProperty(STRIP_PATH, "" + m_CheckBoxStripPath.isSelected());
    result.setProperty(STRIP_EXT, "" + m_CheckBoxStripExt.isSelected());

    return result;
  }

  /**
   * Creates a new frame with the GUI.
   *
   * @return		the frame
   */
  public static BaseFrame createFrame() {
    BaseFrame 	frame;

    frame = new BaseFrame("Overlay filename");
    OverlayFilenameGUI panel = new OverlayFilenameGUI();
    frame.getRootPane().setLayout(new BorderLayout());
    frame.getRootPane().add(panel);
    frame.setSize(600, 400);

    return frame;
  }

  /**
   * Starts the GUI.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    Project.initialize();
    BaseFrame frame = createFrame();
    frame.setDefaultCloseOperation(BaseFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
