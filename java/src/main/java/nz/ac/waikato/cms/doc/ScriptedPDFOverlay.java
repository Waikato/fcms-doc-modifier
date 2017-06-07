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
 * ScriptedPDFOverlay.java
 * Copyright (C) 2016-2017 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.doc;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import nz.ac.waikato.cms.core.Utils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses a Groovy script and parameters in a CSV spreadsheet to generate
 * PDFs from a template.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ScriptedPDFOverlay {

  public static final String PDF_TEMPLATE = "pdf_template";

  public static final String PARAMS = "params";

  public static final String GROOVY = "groovy";

  public static final String OUTPUT_DIR = "output_dir";

  /** the classname of the Groovy classloader. */
  public final static String CLASS_GROOVYCLASSLOADER = "groovy.lang.GroovyClassLoader";

  /** for logging. */
  protected Logger m_Logger;

  /** the pdf template. */
  protected File m_PdfTemplate;

  /** the csv file with the parameters. */
  protected File m_Params;

  /** the groovy script to use. */
  protected File m_Groovy;

  /** the pdf template. */
  protected File m_OutputDir;

  /**
   * Initializes the overlay.
   *
   * @param pdfTemplate the template to use
   * @param params 	the CSV spreadsheet file with the parameters
   * @param groovy	the Groovy script to execute
   * @param outputDir	the output directory for the generated files
   */
  public ScriptedPDFOverlay(File pdfTemplate, File params, File groovy, File outputDir) {
    if (!pdfTemplate.exists())
      throw new IllegalArgumentException("PDF template does not exist: " + pdfTemplate);
    if (pdfTemplate.isDirectory())
      throw new IllegalArgumentException("PDF template points to a directory: " + pdfTemplate);

    if (!params.exists())
      throw new IllegalArgumentException("CSV spreadsheet with parameters does not exist: " + params);
    if (params.isDirectory())
      throw new IllegalArgumentException("CSV spreadsheet with parameters points to a directory: " + params);

    if (!groovy.exists())
      throw new IllegalArgumentException("Groovy script does not exist: " + groovy);
    if (groovy.isDirectory())
      throw new IllegalArgumentException("Groovy script points to a directory: " + groovy);

    if (!outputDir.exists())
      throw new IllegalArgumentException("Output directory does not exist: " + outputDir);
    if (!outputDir.isDirectory())
      throw new IllegalArgumentException("Output directory does not point to a directory: " + outputDir);

    m_PdfTemplate = pdfTemplate;
    m_Params      = params;
    m_Groovy      = groovy;
    m_OutputDir   = outputDir;
    m_Logger      = Logger.getLogger(this.getClass().getName());
  }

  /**
   * initializes and returns a Groovy Interpreter.
   *
   * @return			the interpreter or null if Groovy classes not present
   */
  protected Object newClassLoader() {
    Object	result;
    Class<?>	cls;
    Constructor constr;

    try {
      cls    = Class.forName(CLASS_GROOVYCLASSLOADER);
      constr = cls.getConstructor(new Class[]{ClassLoader.class});
      result = constr.newInstance(getClass().getClassLoader());
    }
    catch (Exception e) {
      m_Logger.log(Level.SEVERE, "Failed to instantiate new classloader!", e);
      result = null;
    }

    return result;
  }

  /**
   * executes the specified method and returns the result, if any.
   *
   * @param o			the object the method should be called from,
   * 				e.g., a Groovy Interpreter
   * @param methodName		the name of the method
   * @param paramClasses	the classes of the parameters
   * @param paramValues		the values of the parameters
   * @return			the return value of the method, if any (in that case null)
   */
  protected Object invoke(Object o, String methodName, Class[] paramClasses, Object[] paramValues) {
    Method m;
    Object      result;

    try {
      m      = o.getClass().getMethod(methodName, paramClasses);
      result = m.invoke(o, paramValues);
    }
    catch (Exception e) {
      m_Logger.log(Level.SEVERE, "Failed to invoke method '" + methodName + "' (" + Utils.arrayToString(paramClasses) + " with " + Utils.arrayToString(paramValues) + ")!", e);
      result = null;
    }

    return result;
  }

  /**
   * loads the module and returns a new instance of it as instance of the
   * provided Java class template.
   *
   * @param file		the Groovy module file
   * @param template		the template for the returned Java object
   * @return			the Groovy object
   */
  protected Object newInstance(File file, Class template) {
    Object 	result;
    Object	interpreter;
    Class	cls;

    interpreter = newClassLoader();
    if (interpreter == null)
      return null;

    try {
      cls    = (Class) invoke(interpreter, "parseClass", new Class[]{File.class}, new Object[]{file.getAbsoluteFile()});
      result = cls.newInstance();
    }
    catch (Exception e) {
      m_Logger.log(Level.SEVERE, "Failed to instantiate script from '" + file + "' as '" + template.getName() + "'!", e);
      result = null;
    }

    return result;
  }

  /**
   * Applies the groovy script to the PDF template, one time per row.
   *
   * @return		null if successful, otherwise error message
   */
  public String execute() {
    String			result;
    ScriptedPDFOverlayProcessor	processor;
    Reader 			in;
    Iterable<CSVRecord> 	records;
    int 			row;

    result = null;

    // initialize groovy script
    processor = (ScriptedPDFOverlayProcessor) newInstance(m_Groovy, ScriptedPDFOverlayProcessor.class);
    if (processor == null)
      return "Failed to instantiate Groovy script: " + m_Groovy;

    // process spreadsheet
    try {
      in      = new FileReader(m_Params);
      records = CSVFormat.EXCEL.withHeader().parse(in);
      row     = 0;
      for (CSVRecord record : records) {
	row++;
	result     = processor.overlay(m_PdfTemplate, row, record.toMap(), m_OutputDir);
	if (result != null) {
	  result = "Failed to process row #" + row + ":\n" + result;
	  break;
	}
      }
    }
    catch (Exception e) {
      result = "Failed to process!\n" + Utils.throwableToString(e);
    }

    return result;
  }

  /**
   * Runs the PDF overlay from command-line.
   *
   * @param args	the arguments, use -h for help
   * @throws Exception	if something goes wrong
   */
  public static void main(String[] args) throws Exception {
    ArgumentParser parser;

    parser = ArgumentParsers.newArgumentParser("ScriptedPDFOverlay");
    parser.description(
      "Applies a Groovy script to a PDF template using parameters from a CSV spreadsheet file.\n"
	+ "One row in the spreadsheet corresponds to one generated output file.\n"
	+ "The header names are used as keys in the Map that is provided to the Groovy script.");
    parser.addArgument(PDF_TEMPLATE)
      .metavar(PDF_TEMPLATE)
      .type(String.class)
      .help("The PDF template file to process with the groovy script.");
    parser.addArgument(PARAMS)
      .metavar(PARAMS)
      .type(String.class)
      .help("The CSV spreadsheet file with the parameters (one row per output).");
    parser.addArgument(GROOVY)
      .metavar(GROOVY)
      .type(String.class)
      .help("The Groovy file script to execute, must implement the " + ScriptedPDFOverlayProcessor.class.getName() + " interface.");
    parser.addArgument(OUTPUT_DIR)
      .metavar(OUTPUT_DIR)
      .type(String.class)
      .help("The output directory to store the generate PDFs in.");

    Namespace namespace;
    try {
      namespace = parser.parseArgs(args);
    }
    catch (Exception e) {
      parser.printHelp();
      return;
    }

    ScriptedPDFOverlay overlay = new ScriptedPDFOverlay(
      new File(namespace.getString(PDF_TEMPLATE)),
      new File(namespace.getString(PARAMS)),
      new File(namespace.getString(GROOVY)),
      new File(namespace.getString(OUTPUT_DIR))
    );
    String result = overlay.execute();
    if (result != null)
      throw new Exception("Failed to process:\n" + result);
  }
}
