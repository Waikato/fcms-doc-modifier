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

/*
 * FileUtils.java
 * Copyright (C) 2009-2015 University of Waikato, Hamilton, New Zealand
 */

package nz.ac.waikato.cms.core;

import java.io.File;

/**
 * Utility class for I/O related actions.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 10824 $
 */
public class FileUtils {

  /**
   * Replaces the extension of the given file with the new one. Leave the
   * new extension empty if you want to remove the extension.
   * Always removes ignored extension suffixes first from the filename.
   * 
   * @param file	the file to replace the extension for
   * @param newExt	the new extension (incl dot), empty string to remove extension
   * @return		the updated file
   */
  public static File replaceExtension(File file, String newExt) {
    return new File(replaceExtension(file.getAbsolutePath(), newExt));
  }

  /**
   * Replaces the extension of the given file with the new one. Leave the
   * new extension empty if you want to remove the extension.
   * Always removes ignored extension suffixes first from the filename.
   * 
   * @param file	the file to replace the extension for
   * @param newExt	the new extension (incl dot), empty string to remove extension
   * @return		the updated file
   */
  public static String replaceExtension(String file, String newExt) {
    String	result;
    int		index;
    
    result = file;
    
    index = file.lastIndexOf('.');
    if (index > -1) {
      if (newExt.length() > 0)
	result = file.substring(0, index) + newExt;
      else
	result = file.substring(0, index);
    }
    
    return result;
  }
}
