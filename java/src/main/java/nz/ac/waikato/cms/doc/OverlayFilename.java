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
 * OverlayFilename.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.doc;

import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Overlays the file name of the PDF on the pages.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class OverlayFilename {

  public static final String INPUT = "input";

  public static final String OUTPUT = "output";

  public static final String VPOS = "vpos";

  public static final String HPOS = "hpos";

  public static final String STRIPPATH = "strippath";

  public static final String STRIPEXT = "stripext";

  /**
   * Performs the overlay.
   *
   * @param input	the input file/dir
   * @param output	the output file/dir
   * @param vpos	the vertical position
   * @param hpos	the horizontal position
   * @param stripPath	whether to strip the path
   * @param stripExt	whether to strip the extension
   * @param pages	the array of pages (1-based) to add the overlay to, null for all
   * @return		true if successfully overlay
   */
  public boolean overlay(File input, File output, int vpos, int hpos, boolean stripPath, boolean stripExt, int[] pages) {
    PdfReader 		reader;
    PdfStamper 		stamper;
    FileOutputStream 	fos;
    PdfContentByte 	canvas;
    int 		i;
    String		text;

    reader  = null;
    stamper = null;
    fos     = null;
    try {
      reader  = new PdfReader(input.getAbsolutePath());
      fos     = new FileOutputStream(output.getAbsolutePath());
      stamper = new PdfStamper(reader, fos);
      if (pages == null) {
	pages = new int[reader.getNumberOfPages()];
	for (i = 0; i < pages.length; i++)
	  pages[i] = i + 1;
      }

      if (stripPath)
	text = input.getName();
      else
        text = input.getAbsolutePath();
      if (stripExt)
	text = text.replaceFirst("\\.[pP][dD][fF]$", "");

      for (i = 0; i < pages.length; i++) {
	canvas = stamper.getOverContent(pages[i]);
	ColumnText.showTextAligned(
	  canvas,
	  Element.ALIGN_LEFT,
	  new Paragraph(text),
	  hpos,
	  vpos,
	  0.0f);
      }
    }
    catch (Exception e) {
      System.err.println("Failed to process " + input + ":");
      e.printStackTrace();
      return false;
    }
    finally {
      try {
	if (stamper != null)
	  stamper.close();
      }
      catch (Exception e) {
	// ignored
      }
      try {
	if (reader != null)
	  reader.close();
      }
      catch (Exception e) {
	// ignored
      }
      try {
	if (fos != null) {
	  fos.flush();
	  fos.close();
	}
      }
      catch (Exception e) {
	 // ignored
      }
    }

    return true;
  }

  /**
   * Expects the following arguments:
   * <ul>
   *   <li>input -- the input PDF file or dir with PDFs<li/>
   *   <li>output -- the output PDF file or dir if input is dir<li/>
   *   <li>[optional] --vpos {pos} -- the vertical position of the overlay<li/>
   *   <li>[optional] --hpos {pos} -- the horizontal position of the overlay<li/>
   *   <li>[optional] --strippath {true|false} -- whether to strip the path from the filename<li/>
   *   <li>[optional] --stripext {true|false} -- whether to strip the extension from the filename<li/>
   * </ul>
   * Use -h/--help to see full help.
   *
   * @param args	the commandline arguments
   * @throws Exception	if processing fails
   */
  public static void main(String[] args) throws Exception {
    ArgumentParser parser;

    parser = ArgumentParsers.newArgumentParser("OverlayFilename");
    parser.description("Overlays the filename of the PDF on the pages.");
    parser.addArgument(INPUT)
      .metavar(INPUT)
      .type(String.class)
      .help("The PDF file (or directory with PDFs) to add the filename overlay to.");
    parser.addArgument(OUTPUT)
      .metavar(OUTPUT)
      .type(String.class)
      .help("The file to save the modified PDF to (must be a directory if input is a directory).");
    parser.addArgument("--" + VPOS)
      .metavar(VPOS)
      .type(Integer.class)
      .setDefault(10)
      .help("The vertical position of the overlay.");
    parser.addArgument("--" + HPOS)
      .metavar(HPOS)
      .type(Integer.class)
      .setDefault(10)
      .help("The horizontal position of the overlay.");
    parser.addArgument("--" + STRIPPATH)
      .metavar(STRIPPATH)
      .type(Boolean.class)
      .dest(STRIPPATH)
      .setDefault(false)
      .help("Whether to strip the path from the filename.");
    parser.addArgument("--" + STRIPEXT)
      .metavar(STRIPEXT)
      .type(Boolean.class)
      .dest(STRIPEXT)
      .setDefault(false)
      .help("Whether to strip the extension from the filename.");

    Namespace namespace;
    try {
      namespace = parser.parseArgs(args);
    }
    catch (Exception e) {
      parser.printHelp();
      return;
    }

    OverlayFilename of = new OverlayFilename();
    of.overlay(
      new File(namespace.getString(INPUT)),
      new File(namespace.getString(OUTPUT)),
      namespace.getInt(VPOS),
      namespace.getInt(HPOS),
      namespace.getBoolean(STRIPPATH),
      namespace.getBoolean(STRIPEXT),
      null);
  }
}
