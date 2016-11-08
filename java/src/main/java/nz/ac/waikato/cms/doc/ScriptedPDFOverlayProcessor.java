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
 * ScriptedPDFOverlayProcessor.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.doc;

import java.io.File;
import java.util.Map;

/**
 * Interface for classes that process PDF templates and generate modified
 * PDF files from it.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface ScriptedPDFOverlayProcessor {

  /**
   * Interface for processors that generate overlays on PDFs.
   *
   * @param pdfTemplate	the template file to use
   * @param row		the row in the spreadsheet (excluding the header)
   * @param params	the parameters to use in the script
   * @param outputDir	the name of the output directory
   * @return		null if successful, otherwise error message
   */
  public String overlay(File pdfTemplate, int row, Map<String,String> params, File outputDir);
}
