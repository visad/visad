/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data;

import java.io.IOException;
import java.io.File;

/**
 * Standard routines used to write a {@link visad.Data Data} object.
 */
public interface DataWriter
  extends DataProcessor
{
  /**
   * Close the file
   *
   * @exception IOException If there is a problem.
   */
  void close()
    throws IOException;

  /**
   * Flush all data to disk.
   *
   * @exception IOException If there is a problem.
   */
  void flush()
    throws IOException;

  /**
   * Open the named file.  If a file is already being written to,
   * all data will be flushed and the file will be closed.
   *
   * @param name The path used to open the file.
   *
   * @exception IOException If there is a problem.
   */
  void setFile(String name)
    throws IOException;

  /**
   * Open the specified file.  If a file is already being written to,
   * all data will be flushed and the file will be closed.
   *
   * @param file The file.
   *
   * @exception IOException If there is a problem.
   */
  void setFile(File file)
    throws IOException;
}
