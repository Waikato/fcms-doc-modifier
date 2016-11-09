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
 * ScriptedPDFOverlayGUI.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.gui;

import nz.ac.waikato.cms.core.Project;
import nz.ac.waikato.cms.doc.ScriptedPDFOverlay;
import nz.ac.waikato.cms.gui.core.BaseDirectoryChooser;
import nz.ac.waikato.cms.gui.core.BaseFileChooser;
import nz.ac.waikato.cms.gui.core.BaseFrame;
import nz.ac.waikato.cms.gui.core.ExtensionFileFilter;
import nz.ac.waikato.cms.gui.core.GUIHelper;
import nz.ac.waikato.cms.gui.core.SetupPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * For generating PDFs from a template using Groovy and overlay data from
 * a CSV spreadsheet file.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ScriptedPDFOverlayGUI
  extends SetupPanel {

  public static final String PDF_TEMPLATE = "PdfTemplate";

  public static final String PARAMS = "Params";

  public static final String GROOVY = "Groovy";

  public static final String OUTPUT_DIR = "OutputDir";

  /** the file chooser to use for the pdf template. */
  protected BaseFileChooser m_FileChooserPdfTemplate;

  /** the file chooser to use for the CSV spreadsheet. */
  protected BaseFileChooser m_FileChooserParams;

  /** the file chooser to use for the groovy file. */
  protected BaseFileChooser m_FileChooserGroovy;

  /** the directory chooser to use. */
  protected BaseDirectoryChooser m_DirChooser;

  /** the PDF template. */
  protected JTextField m_TextPdfTemplate;

  /** the button for selecting the PDF template. */
  protected JButton m_ButtonPdfTemplate;

  /** the CSV spreadsheet. */
  protected JTextField m_TextParams;

  /** the button for selecting the csv spreadsheet. */
  protected JButton m_ButtonParams;

  /** the groovy script. */
  protected JTextField m_TextGroovy;

  /** the button for selecting the groovy script. */
  protected JButton m_ButtonGroovy;

  /** the output directory. */
  protected JTextField m_TextOutputDir;

  /** the button for selecting the output directory. */
  protected JButton m_ButtonOutputDir;

  /** the button for overlaying the files. */
  protected JButton m_ButtonGenerate;

  /** the button for closing the frame. */
  protected JButton m_ButtonClose;

  /** whether files are being processed. */
  protected boolean m_Processing;

  /**
   * Initializes the members.
   */
  protected void initialize() {
    ExtensionFileFilter		filter;

    super.initialize();



    m_FileChooserPdfTemplate = new BaseFileChooser();
    m_FileChooserPdfTemplate.addChoosableFileFilter(ExtensionFileFilter.getPdfFileFilter());
    m_FileChooserPdfTemplate.setAcceptAllFileFilterUsed(false);
    m_FileChooserPdfTemplate.setMultiSelectionEnabled(false);

    m_FileChooserParams = new BaseFileChooser();
    m_FileChooserParams.addChoosableFileFilter(ExtensionFileFilter.getCsvFileFilter());
    m_FileChooserParams.setAcceptAllFileFilterUsed(false);
    m_FileChooserParams.setMultiSelectionEnabled(false);

    filter = new ExtensionFileFilter("Groovy script", "groovy");
    m_FileChooserGroovy = new BaseFileChooser();
    m_FileChooserGroovy.addChoosableFileFilter(filter);
    m_FileChooserGroovy.setAcceptAllFileFilterUsed(false);
    m_FileChooserGroovy.setMultiSelectionEnabled(false);
    
    m_DirChooser = new BaseDirectoryChooser();

    m_Processing = false;
  }

  /**
   * Initializes the widgets.
   */
  protected void initGUI() {
    JPanel		panelParams;
    List<JLabel>	labels;

    setLayout(new BorderLayout());

    // the parameters
    panelParams = new JPanel(new GridLayout(4, 1));
    panelParams.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    labels      = new ArrayList<>();
    add(panelParams, BorderLayout.CENTER);
    // PDF template
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_TextPdfTemplate = new JTextField(30);
      m_TextPdfTemplate.getDocument().addDocumentListener(new DocumentListener() {
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
          if (m_TextPdfTemplate.getText().trim().isEmpty() || isValidFile(m_TextPdfTemplate.getText()))
            m_TextPdfTemplate.setForeground(Color.BLACK);
          else
            m_TextPdfTemplate.setForeground(Color.RED);
          updateButtons();
        }
      });
      JLabel label = new JLabel("PDF Template");
      label.setDisplayedMnemonic('P');
      label.setLabelFor(m_TextPdfTemplate);

      m_ButtonPdfTemplate = new JButton("...");
      m_ButtonPdfTemplate.setPreferredSize(new Dimension((int) m_ButtonPdfTemplate.getPreferredSize().getWidth(), (int) m_TextPdfTemplate.getPreferredSize().getHeight()));
      m_ButtonPdfTemplate.addActionListener((ActionEvent e) -> {
	if (!m_TextPdfTemplate.getText().isEmpty())
	  m_FileChooserPdfTemplate.setCurrentDirectory(new File(m_TextPdfTemplate.getText()));
	int retVal = m_FileChooserPdfTemplate.showOpenDialog(ScriptedPDFOverlayGUI.this);
	if (retVal != BaseFileChooser.APPROVE_OPTION)
	  return;
	m_TextPdfTemplate.setText(m_FileChooserPdfTemplate.getSelectedFile().getAbsolutePath());
      });

      panel.add(label);
      panel.add(m_TextPdfTemplate);
      panel.add(m_ButtonPdfTemplate);
      labels.add(label);
    }
    // Params (CSV spreadshet)
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_TextParams = new JTextField(30);
      m_TextParams.getDocument().addDocumentListener(new DocumentListener() {
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
          if (m_TextParams.getText().trim().isEmpty() || isValidFile(m_TextParams.getText()))
            m_TextParams.setForeground(Color.BLACK);
          else
            m_TextParams.setForeground(Color.RED);
          updateButtons();
        }
      });
      JLabel label = new JLabel("CSV Spreadsheet");
      label.setDisplayedMnemonic('C');
      label.setLabelFor(m_TextParams);

      m_ButtonParams = new JButton("...");
      m_ButtonParams.setPreferredSize(new Dimension((int) m_ButtonParams.getPreferredSize().getWidth(), (int) m_TextParams.getPreferredSize().getHeight()));
      m_ButtonParams.addActionListener((ActionEvent e) -> {
	if (!m_TextParams.getText().isEmpty())
	  m_FileChooserParams.setCurrentDirectory(new File(m_TextParams.getText()));
	int retVal = m_FileChooserParams.showOpenDialog(ScriptedPDFOverlayGUI.this);
	if (retVal != BaseFileChooser.APPROVE_OPTION)
	  return;
	m_TextParams.setText(m_FileChooserParams.getSelectedFile().getAbsolutePath());
      });

      panel.add(label);
      panel.add(m_TextParams);
      panel.add(m_ButtonParams);
      labels.add(label);
    }
    // Groovy
    {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
      panelParams.add(panel);
      m_TextGroovy = new JTextField(30);
      m_TextGroovy.getDocument().addDocumentListener(new DocumentListener() {
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
          if (m_TextGroovy.getText().trim().isEmpty() || isValidFile(m_TextGroovy.getText()))
            m_TextGroovy.setForeground(Color.BLACK);
          else
            m_TextGroovy.setForeground(Color.RED);
          updateButtons();
        }
      });
      JLabel label = new JLabel("Groovy script");
      label.setDisplayedMnemonic('G');
      label.setLabelFor(m_TextGroovy);

      m_ButtonGroovy = new JButton("...");
      m_ButtonGroovy.setPreferredSize(new Dimension((int) m_ButtonGroovy.getPreferredSize().getWidth(), (int) m_TextGroovy.getPreferredSize().getHeight()));
      m_ButtonGroovy.addActionListener((ActionEvent e) -> {
	if (!m_TextGroovy.getText().isEmpty())
	  m_FileChooserGroovy.setCurrentDirectory(new File(m_TextGroovy.getText()));
	int retVal = m_FileChooserGroovy.showOpenDialog(ScriptedPDFOverlayGUI.this);
	if (retVal != BaseFileChooser.APPROVE_OPTION)
	  return;
	m_TextGroovy.setText(m_FileChooserGroovy.getSelectedFile().getAbsolutePath());
      });

      panel.add(label);
      panel.add(m_TextGroovy);
      panel.add(m_ButtonGroovy);
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
      m_ButtonOutputDir.addActionListener((ActionEvent e) -> {
        if (!m_TextOutputDir.getText().isEmpty())
          m_DirChooser.setCurrentDirectory(new File(m_TextOutputDir.getText()));
        int retVal = m_DirChooser.showOpenDialog(ScriptedPDFOverlayGUI.this);
        if (retVal != BaseDirectoryChooser.APPROVE_OPTION)
          return;
        m_TextOutputDir.setText(m_DirChooser.getSelectedFile().getAbsolutePath());
      });

      panel.add(label);
      panel.add(m_TextOutputDir);
      panel.add(m_ButtonOutputDir);
      labels.add(label);
    }

    // the buttons at the bottom
    {
      JPanel panelBottom = new JPanel(new BorderLayout());
      add(panelBottom, BorderLayout.SOUTH);

      JPanel panelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      panelBottom.add(panelRight, BorderLayout.EAST);

      m_ButtonGenerate = new JButton("Generate", GUIHelper.getIcon("run.gif"));
      m_ButtonGenerate.addActionListener((ActionEvent e) -> process());
      panelRight.add(m_ButtonGenerate);

      m_ButtonClose = new JButton("Close", GUIHelper.getIcon("stop.gif"));
      m_ButtonClose.addActionListener((ActionEvent e) -> GUIHelper.closeParent(ScriptedPDFOverlayGUI.this));
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
	ScriptedPDFOverlay overlay = new ScriptedPDFOverlay(
	  new File(m_TextPdfTemplate.getText()),
	  new File(m_TextParams.getText()),
	  new File(m_TextGroovy.getText()),
	  new File(m_TextOutputDir.getText())
	);
	String errors = overlay.execute();
	if (errors != null)
	  m_Errors.append(errors);
        return null;
      }
      @Override
      protected void done() {
        m_Processing = false;
        updateButtons();
        if (m_Errors.length() > 0) {
          JOptionPane.showMessageDialog(
            ScriptedPDFOverlayGUI.this,
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
   * Returns whether the file is valid.
   *
   * @return		true if valid
   */
  protected boolean isValidFile(String fileStr) {
    File	file;

    if (fileStr.trim().isEmpty())
      return false;

    try {
      file = new File(fileStr);
      return file.exists() && !file.isDirectory();
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
      && isValidFile(m_TextPdfTemplate.getText())
      && isValidFile(m_TextParams.getText())
      && isValidFile(m_TextGroovy.getText())
      && isValidOutputDir();

    return result;
  }

  /**
   * Updates the state of the buttons.
   */
  protected void updateButtons() {
    m_ButtonClose.setEnabled(!m_Processing);
    m_ButtonGenerate.setEnabled(canOverlayFiles());
  }

  /**
   * Maps the properties back to the GUI.
   *
   * @param props       the properties to use
   */
  protected void propsToGUI(Properties props) {
    m_TextPdfTemplate.setText(props.getProperty(PDF_TEMPLATE, ""));
    m_TextParams.setText(props.getProperty(PARAMS, ""));
    m_TextGroovy.setText(props.getProperty(GROOVY, ""));
    m_TextOutputDir.setText(props.getProperty(OUTPUT_DIR, ""));
  }

  /**
   * Maps the GUI to a properties object.
   *
   * @return            the properties
   */
  protected Properties guiToProps() {
    Properties result;

    result = new Properties();
    result.setProperty(PDF_TEMPLATE, m_TextPdfTemplate.getText());
    result.setProperty(PARAMS, m_TextParams.getText());
    result.setProperty(GROOVY, m_TextGroovy.getText());
    result.setProperty(OUTPUT_DIR, m_TextOutputDir.getText());

    return result;
  }

  /**
   * Creates a new frame with the GUI.
   *
   * @return		the frame
   */
  public static BaseFrame createFrame() {
    BaseFrame 	frame;

    frame = new BaseFrame("Scripted PDF Overlay");
    ScriptedPDFOverlayGUI panel = new ScriptedPDFOverlayGUI();
    frame.getRootPane().setLayout(new BorderLayout());
    frame.getRootPane().add(panel);
    frame.setSize(550, 200);

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
