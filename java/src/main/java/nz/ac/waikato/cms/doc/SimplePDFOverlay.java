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
 * SimplePDFOverlay.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.doc;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import nz.ac.waikato.cms.core.FileUtils;
import nz.ac.waikato.cms.core.Utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.logging.Logger;

/**
 * Uses a instructions from a text file for overlaying text on a PDF.
 * Use -h on the commandline to see more details on instructions set.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SimplePDFOverlay {

  public static final String PDF = "pdf";

  public static final String INSTRUCTIONS = "instructions";

  public static final String OUTPUT = "output";

  public static final String PREFIX_COMMENT = "#";

  public static final String PREFIX_UNITS = "units:";

  public static final String PREFIX_PAGE = "page:";

  public static final String PREFIX_FONT = "font:";

  public static final String PREFIX_TEXT = "text:";

  public static final String PREFIX_LINE = "line:";

  public static final String PREFIX_RECT = "rect:";

  public static final String PREFIX_OVAL = "oval:";

  public static final String FORMAT_FONT = "<name> <size> <color>";

  public static final String FORMAT_TEXT = "<llx> <lly> <urx> <ury> <leading> <alignment> <text>";

  public static final String FORMAT_LINE = "<x1> <y1> <x2> <x2> <line-width> <stroke-color>";

  public static final String FORMAT_RECT = "<x> <y> <w> <h> <line-width> <stroke-color> [fill-color]";

  public static final String FORMAT_OVAL = "<x1> <y1> <x2> <y2> <line-width> <stroke-color> [fill-color]";

  public static final String VALUES_ALIGN = "UNDEFINED|LEFT|CENTER|RIGHT|JUSTIFIED";

  /** for logging. */
  protected Logger m_Logger;

  /** the pdf input. */
  protected File m_Pdf;

  /** the file with the instructions. */
  protected File m_Instructions;

  /** the pdf output. */
  protected File m_Output;

  /**
   * Initializes the overlay.
   *
   * @param pdf             the template to use
   * @param instructions 	the text file with the instructions
   * @param output	        the output directory for the generated files
   */
  public SimplePDFOverlay(File pdf, File instructions, File output) {
    if (!pdf.exists())
      throw new IllegalArgumentException("PDF does not exist: " + pdf);
    if (pdf.isDirectory())
      throw new IllegalArgumentException("PDF points to a directory: " + pdf);

    if (!instructions.exists())
      throw new IllegalArgumentException("Instructions file does not exist: " + instructions);
    if (instructions.isDirectory())
      throw new IllegalArgumentException("Instructions file points to a directory: " + instructions);

    if (output.isDirectory())
      throw new IllegalArgumentException("Output file points to a directory: " + output);

    m_Pdf          = pdf;
    m_Instructions = instructions;
    m_Output       = output;
    m_Logger       = Logger.getLogger(this.getClass().getName());
  }

  /**
   * Parses the color string (#RRGGBB).
   *
   * @param str		the color string
   * @return		the color, BLACK if failed to parse
   */
  protected Color parseColor(String str) {
    Color	result;

    str = str.replaceAll("#", "");
    if (str.length() == 6) {
      result = new Color(
	Integer.parseInt(str.substring(0, 2), 16),
	Integer.parseInt(str.substring(2, 4), 16),
	Integer.parseInt(str.substring(4, 6), 16));
    }
    else {
      m_Logger.warning("Failed to parse color, falling back to black: " + str);
      result = Color.BLACK;
    }

    return result;
  }

  /**
   * Parses the alignment string.
   *
   * @param str		the alignment string
   * @return		the alignment, see {@link Element}
   */
  protected int parseAlignment(String str) {
    switch (str) {
      case "UNDEFINED":
	return Element.ALIGN_UNDEFINED;
      case "LEFT":
	return Element.ALIGN_LEFT;
      case "CENTER":
	return Element.ALIGN_CENTER;
      case "RIGHT":
	return Element.ALIGN_RIGHT;
      case "JUSTIFIED":
	return Element.ALIGN_JUSTIFIED;
      default:
	m_Logger.warning("Unhandled alignment, falling back to LEFT: " + str);
	return Element.ALIGN_LEFT;
    }
  }

  /**
   * Parses the location string.
   *
   * @param str		the string to parse
   * @param max		the maximum to use if position string is -1
   * @param units	the units the location is in (in|mm|pt)
   * @return		the position in points
   */
  protected float parseLocation(String str, float max, String units) {
    float	result;

    result = Float.parseFloat(str);

    if (result >= 0) {
      switch (units) {
	case "mm":
	  result = Utilities.millimetersToPoints(result);
	  break;
	case "in":
	  result = Utilities.inchesToPoints(result);
	  break;
	case "pt":
	  // nothing to do
	  break;
	default:
	  m_Logger.warning("Unknown units: " + units);
      }
    }
    else {
      result = max;
    }

    return result;
  }

  /**
   * Applies the instructions to the input PDF.
   *
   * @return		null if successful, otherwise error message
   */
  public String execute() {
    String		result;
    String		line;
    BufferedReader	breader;
    FileReader		freader;
    int			i;
    int 		lineNo;
    String		units;
    int			pageNo;
    PdfReader 		reader;
    PdfStamper 		stamper;
    PdfContentByte 	cb;
    ColumnText 		ct;
    Font 		font;
    String[]		parts;
    StringBuilder	text;

    result = null;

    freader = null;
    breader = null;
    try {
      reader  = new PdfReader(new FileInputStream(m_Pdf.getAbsolutePath()));
      stamper = new PdfStamper(reader, new FileOutputStream(m_Output.getAbsolutePath()));
      freader = new FileReader(m_Instructions);
      breader = new BufferedReader(freader);
      lineNo  = 0;
      units   = "pt";
      pageNo  = 1;
      cb      = stamper.getOverContent(pageNo);
      font    = null;
      while ((line = breader.readLine()) != null) {
	lineNo++;
	if (line.trim().startsWith(PREFIX_COMMENT))
	  continue;
	if (line.trim().length() == 0)
	  continue;
	if (line.startsWith(PREFIX_UNITS)) {
	  units = line.substring(PREFIX_UNITS.length()).trim().toLowerCase();
	}
	else if (line.startsWith(PREFIX_PAGE)) {
	  pageNo = Integer.parseInt(line.substring(PREFIX_PAGE.length()).trim());
	  cb     = stamper.getOverContent(pageNo);
	}
	else if (line.startsWith(PREFIX_FONT)) {
	  parts = line.substring(PREFIX_FONT.length()).trim().split(" ");
	  if (parts.length == 3)
	    font = FontFactory.getFont(
	      parts[0],
	      Float.parseFloat(parts[1]),
	      new BaseColor(parseColor(parts[2]).getRGB()));
	  else
	    m_Logger.warning("Font instruction not in expected format (" + FORMAT_FONT + "):\n" + line);
	}
	else if (line.startsWith(PREFIX_TEXT)) {
	  parts = line.substring(PREFIX_TEXT.length()).trim().split(" ");
	  if (parts.length >= 7) {
	    ct = new ColumnText(cb);
	    ct.setSimpleColumn(
	      parseLocation(parts[0], reader.getPageSize(pageNo).getWidth(), units),  // llx
	      parseLocation(parts[1], reader.getPageSize(pageNo).getHeight(), units),  // lly
	      parseLocation(parts[2], reader.getPageSize(pageNo).getWidth(), units),  // urx
	      parseLocation(parts[3], reader.getPageSize(pageNo).getHeight(), units),  // ury
	      Float.parseFloat(parts[4]),                                              // leading
	      parseAlignment(parts[5]));                                               // alignment
	    text = new StringBuilder();
	    for (i = 6; i < parts.length; i++) {
	      if (text.length() > 0)
		text.append(" ");
	      text.append(parts[i]);
	    }
	    if (font == null)
	      ct.setText(new Phrase(text.toString()));
	    else
	      ct.setText(new Phrase(text.toString(), font));
	    ct.go();
	  }
	  else {
	    m_Logger.warning("Text instruction not in expected format (" + FORMAT_TEXT + "):\n" + line);
	  }
	}
	else if (line.startsWith(PREFIX_LINE)) {
	  parts = line.substring(PREFIX_LINE.length()).trim().split(" ");
	  if (parts.length >= 6) {
	    cb.saveState();
	    cb.setLineWidth(Float.parseFloat(parts[4]));  // line width
	    cb.setColorStroke(new BaseColor(parseColor(parts[5]).getRGB()));  // color
	    cb.moveTo(
	      parseLocation(parts[0], reader.getPageSize(pageNo).getWidth(), units),  // x
	      parseLocation(parts[1], reader.getPageSize(pageNo).getWidth(), units)); // y
	    cb.lineTo(
	      parseLocation(parts[2], reader.getPageSize(pageNo).getWidth(), units),  // w
	      parseLocation(parts[3], reader.getPageSize(pageNo).getWidth(), units)); // h
	    cb.stroke();
	    cb.restoreState();
	  }
	  else {
	    m_Logger.warning("Line instruction not in expected format (" + FORMAT_LINE + "):\n" + line);
	  }
	}
	else if (line.startsWith(PREFIX_RECT)) {
	  parts = line.substring(PREFIX_RECT.length()).trim().split(" ");
	  if (parts.length >= 6) {
	    cb.saveState();
	    cb.rectangle(
	      parseLocation(parts[0], reader.getPageSize(pageNo).getWidth(), units),  // x
	      parseLocation(parts[1], reader.getPageSize(pageNo).getWidth(), units),  // y
	      parseLocation(parts[2], reader.getPageSize(pageNo).getWidth(), units),  // w
	      parseLocation(parts[3], reader.getPageSize(pageNo).getWidth(), units)   // h
	    );
	    cb.setLineWidth(Float.parseFloat(parts[4]));  // line width
	    cb.setColorStroke(new BaseColor(parseColor(parts[5]).getRGB()));  // stroke
	    if (parts.length >= 7) {
	      cb.setColorFill(new BaseColor(parseColor(parts[6]).getRGB()));  // fill
	      cb.fillStroke();
	    }
	    else {
	      cb.stroke();
	    }
	    cb.restoreState();
	  }
	  else {
	    m_Logger.warning("Rectangle instruction not in expected format (" + FORMAT_RECT + "):\n" + line);
	  }
	}
	else if (line.startsWith(PREFIX_OVAL)) {
	  parts = line.substring(PREFIX_OVAL.length()).trim().split(" ");
	  if (parts.length >= 6) {
	    cb.saveState();
	    cb.ellipse(
	      parseLocation(parts[0], reader.getPageSize(pageNo).getWidth(), units),  // x1
	      parseLocation(parts[1], reader.getPageSize(pageNo).getWidth(), units),  // y1
	      parseLocation(parts[2], reader.getPageSize(pageNo).getWidth(), units),  // x2
	      parseLocation(parts[3], reader.getPageSize(pageNo).getWidth(), units)   // y2
	    );
	    cb.setLineWidth(Float.parseFloat(parts[4]));  // line width
	    cb.setColorStroke(new BaseColor(parseColor(parts[5]).getRGB()));  // stroke
	    if (parts.length >= 7) {
	      cb.setColorFill(new BaseColor(parseColor(parts[6]).getRGB()));  // fill
	      cb.fillStroke();
	    }
	    else {
	      cb.stroke();
	    }
	    cb.restoreState();
	  }
	  else {
	    m_Logger.warning("Oval instruction not in expected format (" + FORMAT_OVAL + "):\n" + line);
	  }
	}
	else {
	  m_Logger.warning("Unknown command on line #" + lineNo + ":\n" + line);
	}
      }
      stamper.close();
    }
    catch (Exception e) {
      result = "Failed to process!\n" + Utils.throwableToString(e);
    }
    finally {
      FileUtils.closeQuietly(breader);
      FileUtils.closeQuietly(freader);
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

    parser = ArgumentParsers.newArgumentParser("SimplePDFOverlay");
    parser.description(
      "Applies the specified instructions file to the PDF as overlay.\n\n"
	+ "Instructions:\n"
	+ "- empty lines and lines starting with # are ignored\n"
	+ "- The units used for the locations (can be supplied multiple times):\n"
	+ "  units: pt|mm|in\n"
	+ "- Selecting a page (page numbers are 1-based):\n"
	+ "  page: <int>\n"
	+ "- Setting a font:\n"
	+ "  font: " + FORMAT_FONT + "\n"
	+ "- Placing text in a rectangle (ll=lower left, ur=upper right):\n"
	+ "  text: " + FORMAT_TEXT + "\n"
	+ "  align: " + VALUES_ALIGN + "\n"
        + "- Drawing a line (color in #RRGGBB or RRGGBB):\n"
        + "  line: " + FORMAT_LINE + "\n"
        + "- Drawing a rectangle (colors in #RRGGBB or RRGGBB):\n"
        + "  rect: " + FORMAT_RECT + "\n"
        + "- Drawing an oval (colors in #RRGGBB or RRGGBB):\n"
        + "  oval: " + FORMAT_OVAL + "\n"
    );
    parser.addArgument(PDF)
      .metavar(PDF)
      .type(String.class)
      .help("The PDF file to overlay.");
    parser.addArgument(INSTRUCTIONS)
      .metavar(INSTRUCTIONS)
      .type(String.class)
      .help("The text file with the overlay instructions.");
    parser.addArgument(OUTPUT)
      .metavar(OUTPUT)
      .type(String.class)
      .help("The output to store the generate PDF in.");

    Namespace namespace;
    try {
      namespace = parser.parseArgs(args);
    }
    catch (Exception e) {
      parser.printHelp();
      return;
    }

    SimplePDFOverlay overlay = new SimplePDFOverlay(
      new File(namespace.getString(PDF)),
      new File(namespace.getString(INSTRUCTIONS)),
      new File(namespace.getString(OUTPUT))
    );
    String result = overlay.execute();
    if (result != null)
      throw new Exception("Failed to process:\n" + result);
  }
}
