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
 * Copyright (C) 2015-2016 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.doc;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Overlays the file name of the PDF on the pages.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class OverlayFilename {

  public static final String INPUT = "input";

  public static final String OUTPUT = "output";

  public static final String VPOS = "vpos";

  public static final String HPOS = "hpos";

  public static final String STRIPPATH = "strippath";

  public static final String STRIPEXT = "stripext";

  public static final String EVENPAGES = "event-pages";

  /**
   * For filtering PDF files in a directory.
   *
   * @author FracPete (fracpete at waikato dot ac dot nz)
   */
  public static class PdfFilenameFilter
    implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
      return name.toLowerCase().endsWith(".pdf");
    }
  }

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
   * @param evenPages	whether to enforce even pages in the document
   * @return		true if successfully overlay
   */
  public boolean overlay(File input, File output, int vpos, int hpos, boolean stripPath, boolean stripExt, int[] pages, boolean evenPages) {
    PdfReader 		reader;
    PdfStamper 		stamper;
    FileOutputStream 	fos;
    PdfContentByte 	canvas;
    int 		i;
    String		text;
    int			numPages;
    File		tmpFile;
    Document		document;
    PdfWriter		writer;
    PdfImportedPage 	page;
    PdfContentByte	cb;

    reader   = null;
    stamper  = null;
    fos      = null;
    numPages = -1;
    try {
      reader   = new PdfReader(input.getAbsolutePath());
      fos      = new FileOutputStream(output.getAbsolutePath());
      stamper  = new PdfStamper(reader, fos);
      numPages = reader.getNumberOfPages();
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

    // enforce even pages?
    if (evenPages && (numPages > 0) && (numPages % 2 == 1)) {
      reader  = null;
      fos     = null;
      writer  = null;
      tmpFile = new File(output.getAbsolutePath() + "tmp");
      try {
	if (!output.renameTo(tmpFile)) {
	  System.err.println("Failed to rename '" + output + "' to '" + tmpFile + "'!");
	  return false;
	}
	reader   = new PdfReader(tmpFile.getAbsolutePath());
	document = new Document(reader.getPageSize(1));
	fos      = new FileOutputStream(output.getAbsoluteFile());
	writer   = PdfWriter.getInstance(document, fos);
	document.open();
	document.addCreationDate();
	document.addAuthor(System.getProperty("user.name"));
	cb      = writer.getDirectContent();
	for (i = 0; i < reader.getNumberOfPages(); i++) {
	  page = writer.getImportedPage(reader, i+1);
	  document.newPage();
	  cb.addTemplate(page, 0, 0);
	}
	document.newPage();
	document.add(new Paragraph(" "));  // fake content
	document.close();
      }
      catch (Exception e) {
	System.err.println("Failed to process " + tmpFile + ":");
	e.printStackTrace();
	return false;
      }
      finally {
	try {
	  if (fos != null) {
	    fos.flush();
	    fos.close();
	  }
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
	  if (writer != null)
	    writer.close();
	}
	catch (Exception e) {
	  // ignored
	}
	if (tmpFile.exists()) {
	  try {
	    tmpFile.delete();
	  }
	  catch (Exception e) {
	    // ignored
	  }
	}
      }
    }

    return true;
  }

  /**
   * Determines the input and output files and returns them as array (0=input, 1=output).
   *
   * @param input the input file/dir
   * @param output the output file/dir
   * @return the matched input/output files
   */
  public File[][] determineFiles(File input, File output) {
    File[][]	result;
    List<File>	files;
    int		i;

    if (input.isFile()) {
      if (output.isFile()) {
	return new File[][]{
	  new File[]{input},
	  new File[]{output}
	};
      }
      else {
	return new File[][]{
	  new File[]{input},
	  new File[]{new File(output.getAbsolutePath() + File.separator + input.getName())}
	};
      }
    }

    if (output.isFile())
      output = output.getParentFile();
    files = new ArrayList<>();
    for (String fname : input.list(new PdfFilenameFilter())) {
      files.add(new File(input.getAbsolutePath() + File.separator + fname));
    }
    result = new File[2][files.size()];
    for (i = 0; i < files.size(); i++) {
      result[0][i] = files.get(i);
      result[1][i] = new File(output.getAbsolutePath() + File.separator + files.get(i).getName());
    }

    return result;
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
      .setDefault(20)
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
    parser.addArgument(EVENPAGES)
      .metavar(EVENPAGES)
      .type(Boolean.class)
      .dest(EVENPAGES)
      .setDefault(false)
      .help("Whether to enforce even pages in the document (simply adds an empty one).");

    Namespace namespace;
    try {
      namespace = parser.parseArgs(args);
    }
    catch (Exception e) {
      parser.printHelp();
      return;
    }

    OverlayFilename of = new OverlayFilename();

    File[][] files = of.determineFiles(
      new File(namespace.getString(INPUT)),
      new File(namespace.getString(OUTPUT)));

    for (int i = 0; i < files[0].length; i++) {
      System.out.println(files[0][i] + "\n--> " + files[1][i]);
      of.overlay(
	files[0][i],
	files[1][i],
	namespace.getInt(VPOS),
	namespace.getInt(HPOS),
	namespace.getBoolean(STRIPPATH),
	namespace.getBoolean(STRIPEXT),
	null,
	namespace.getBoolean(EVENPAGES));
    }
  }
}
