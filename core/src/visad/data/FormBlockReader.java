//
// FormBlockReader.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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
import visad.*;
import visad.data.BadFormException;

/**
 * FormBlockReader is the VisAD interface for reading in
 * subsets of data, or "blocks," from a data file.
 */
public interface FormBlockReader {

  /**
   * Obtains the specified block from the given file.
   * @param id The file from which to load data blocks.
   * @param block_number The block number of the block to load.
   * @throws VisADException If the block number is invalid.
   */
  DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException;

  /**
   * Determines the number of blocks in the given file.
   * @param id The file for which to get a block count.
   */
  int getBlockCount(String id)
    throws BadFormException, IOException, VisADException;

  /** Closes any open files. */
  void close() throws BadFormException, IOException, VisADException;

}
