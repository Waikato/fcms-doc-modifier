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
 * HyperLinkGrades.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.doc;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import de.intarsys.pdf.content.CSDeviceBasedInterpreter;
import de.intarsys.pdf.content.text.CSTextExtractor;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageTree;
import de.intarsys.pdf.tools.kernel.PDFGeometryTools;
import de.intarsys.tools.locator.FileLocator;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Adds an index with locations found that matched a regular expression.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class HyperLinkGrades
  implements Serializable {

  private static final long serialVersionUID = 3325293041459735332L;

  public static final String INPUT = "input";

  public static final String REGEXP = "regexp";

  public static final String OUTPUT = "output";

  public static final String CASESENSITIVE = "casesensitive";

  public static final String NOCOMPLETIONS = "nocompletions";

  /**
   * Container for storing location information.
   *
   * @author FracPete (fracpete at waikato dot ac dot nz)
   * @version $Revision$
   */
  public static class Location
    implements Serializable {

    private static final long serialVersionUID = 4124023243007050257L;

    /** the page (0-based) of the match. */
    protected int m_Page;

    /** the text that matched. */
    protected String m_Text;

    /**
     * Initializes the container.
     *
     * @param page	the page
     * @param text	the text that matched
     */
    public Location(int page, String text) {
      m_Page = page;
      m_Text = text;
    }

    /**
     * Returns the page (0-based).
     *
     * @return		the page
     */
    public int getPage() {
      return m_Page;
    }

    /**
     * Returns the text that matched.
     *
     * @return		the text
     */
    public String getText() {
      return m_Text;
    }

    /**
     * Outputs page and text.
     *
     * @return		the string
     */
    public String toString() {
      return m_Page + ": " + m_Text;
    }
  }

  /**
   * Loads the specified PDF document.
   *
   * @param file	the file to load
   * @return		the PDF document, null in case of an error
   */
  public static PDDocument load(File file) {
    PDDocument		result;
    FileLocator locator;

    locator = new FileLocator(file.getAbsolutePath());
    try {
      result = PDDocument.createFromLocator(locator);
    }
    catch (Exception e) {
      System.err.println("Failed to open PDF file '" + file + "':");
      e.printStackTrace();
      result = null;
    }

    return result;
  }

  /**
   * Closes the document again.
   *
   * @param document	the document to close, can be null
   */
  public static void close(PDDocument document) {
    if (document != null) {
      try {
	document.close();
      }
      catch (Exception e) {
	// ignored
      }
    }
  }

  /**
   * Returns the student data.
   *
   * @param lines	the current page
   * @param index	the line index to work backwards from
   * @return		true if already completed
   */
  protected static String getStudentData(String[] lines, int index) {
    String	result;
    String	toMatch;
    int		m;

    result = "";

    for (m = index - 1; m >= 0; m--) {
      toMatch = lines[m].toLowerCase();
      if (toMatch.contains("pass") && toMatch.contains("fail") && toMatch.contains("other")) {
	result = lines[m].replaceAll("(.*)([0-9][0-9][0-9][0-9][0-9][0-9][0-9]*).*", "$1$2").replaceAll("  ", "");
	break;
      }
      else if (toMatch.contains("----")) {
	break;
      }
    }

    return result;
  }

  /**
   * Checks whether the student has already completed the studies.
   *
   * @param lines	the current page
   * @param index	the line index to work backwards from
   * @return		true if already completed
   */
  protected static boolean hasCompleted(String[] lines, int index) {
    boolean	result;
    String	toMatch;
    int		m;

    result = false;

    for (m = index - 1; m >= 0; m--) {
      toMatch = lines[m].toLowerCase();
      if (toMatch.contains("pass") && toMatch.contains("fail") && toMatch.contains("other")) {
	result = (toMatch.contains("completion confirmed"));
	break;
      }
      else if (toMatch.contains("----")) {
	break;
      }
    }

    return result;
  }

  /**
   * Extracts locations of text from the specified PDF file that matches the
   * regular expression.
   *
   * @param file	the PDF file to extract the content from
   * @param expr	the regular expression to use for matching
   * @param caseSens	whether the matching is case-sensitive, if not matched against lower case
   * @param noCompletions	whether to exclude students that completed their studies
   * @return		the locations
   */
  public static List<Location> locate(File file, String expr, boolean caseSens, boolean noCompletions) {
    List<Location>		locations;
    PDDocument 			document;
    int				i;
    int				n;
    int				m;
    PDPageTree 			tree;
    CSTextExtractor 		extractor;
    PDPage 			page;
    AffineTransform 		pageTx;
    CSDeviceBasedInterpreter 	interpreter;
    String[]			lines;
    String			line;
    String 			toMatch;
    Pattern			pattern;

    locations = new ArrayList<>();
    pattern = Pattern.compile(expr);

    document = load(file);
    if (document != null) {
      try {
	tree = document.getPageTree();
	for (i = 0; i < tree.getCount(); i++) {
	  extractor = new CSTextExtractor();
	  page      = tree.getPageAt(i);
	  pageTx    = new AffineTransform();
	  PDFGeometryTools.adjustTransform(pageTx, page);
	  extractor.setDeviceTransform(pageTx);
	  interpreter = new CSDeviceBasedInterpreter(null, extractor);
	  interpreter.process(page.getContentStream(), page.getResources());
	  lines = extractor.getContent().split("\n");
	  for (n = 0; n < lines.length; n++) {
	    line    = lines[n];
	    toMatch = line;
	    if (!caseSens)
	      toMatch = toMatch.toLowerCase();
	    if (!pattern.matcher(toMatch).matches())
	      continue;
	    // check whether student already completed studies
	    if (noCompletions && hasCompleted(lines, n))
	      continue;
	    locations.add(new Location(i, line + " [" + getStudentData(lines, n) + "]"));
	  }
	}
      }
      catch (Exception e) {
	System.err.println("Failed to extract locations from '" + file + "': ");
	e.printStackTrace();
      }
      close(document);
    }

    return locations;
  }

  /**
   * Adds the index with locations to the existing PDF.
   *
   * @param locations	the locations to index
   * @param input	the input PDF
   * @param output	the output PDF
   * @return		true if successfully generated
   */
  public static boolean addIndex(List<Location> locations, File input, File output) {
    try {
      // copy pages, add target
      PdfReader reader = new PdfReader(input.getAbsolutePath());
      Document document = new Document();
      PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(output.getAbsolutePath()));
      document.open();
      PdfContentByte canvas = writer.getDirectContent();
      PdfImportedPage page;
      float height = 0;
      for (int i = 1; i <= reader.getNumberOfPages(); i++) {
	document.newPage();
	page = writer.getImportedPage(reader, i);
	canvas.addTemplate(page, 1f, 0, 0, 1, 0, 0);
	Chunk loc = new Chunk(" ");
	loc.setLocalDestination("loc" + i);
	height = page.getHeight();
	ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(loc), 50, height - 50, 0);
      }
      // add index
      document.newPage();
      for (int i = 0; i < locations.size(); i++) {
	Chunk loc = new Chunk("Page " + (locations.get(i).getPage()+1) + ": " + locations.get(i).getText());
	loc.setAction(PdfAction.gotoLocalPage("loc" + (locations.get(i).getPage()+1), false));
	ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(loc), 50, height - 100 - i*20, 0);
      }
      document.close();

      return true;
    }
    catch (Exception e) {
      System.err.println("Failed to overlay locations!");
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Expects the following parameters:
   * <ol>
   *   <li>input PDF</li>
   *   <li>the regular expression for matching the text</li>
   *   <li>output PDF</li>
   *   <li>[optional] case-sensitive [true|false] (using lower case if insensitive)</li>
   *   <li>[optional] no completions [true|false]</li>
   * </ol>
   * Use -h/--help to display help:
   *
   * @param args the arguments
   * @throws Exception if processing fails (eg file not preset)
   */
  public static void main(String[] args) throws Exception {
    ArgumentParser	parser;

    parser = ArgumentParsers.newArgumentParser("HyperLinkGrades");
    parser.description("Adds hyperlinks to grade PDFs.");
    parser.addArgument(INPUT)
      .metavar(INPUT)
      .type(String.class)
      .help("The PDF file to add the hyperlinks to.");
    parser.addArgument(REGEXP)
      .metavar(REGEXP)
      .type(String.class)
      .help("The regular expression for matching the text.");
    parser.addArgument(OUTPUT)
      .metavar(OUTPUT)
      .type(String.class)
      .help("The file to save the modified PDF to.");
    parser.addArgument(CASESENSITIVE)
      .metavar("case-sensitive")
      .type(Boolean.class)
      .dest(CASESENSITIVE)
      .setDefault(false)
      .help("Whether to use case-sensitive matching (uses lower-case if insensitive).");
    parser.addArgument(NOCOMPLETIONS)
      .metavar(NOCOMPLETIONS)
      .type(Boolean.class)
      .dest(NOCOMPLETIONS)
      .setDefault(false)
      .help("Whether to exclude completions.");

    Namespace namespace;
    try {
      namespace = parser.parseArgs(args);
    }
    catch (Exception e) {
      parser.printHelp();
      return;
    }

    // 1. locate
    List<Location> locations = locate(
      new File(namespace.getString(INPUT)),
      namespace.getString(REGEXP),
      namespace.getBoolean(CASESENSITIVE),
      namespace.getBoolean(NOCOMPLETIONS));

    // 2. add index
    addIndex(
      locations,
      new File(namespace.getString(INPUT)),
      new File(namespace.getString(OUTPUT)));
  }
}
