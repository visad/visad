//
// MetadataReader.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.Hashtable;
import visad.VisADException;

/**
 * MetadataReader is the VisAD interface for reading in
 * a file's associated metadata (other than pixel data).
 */
public interface MetadataReader {

  /**
   * Obtains the specified metadata field's value for the given file.
   * @param field the name associated with the metadata field
   * @return the value, or null should the field not exist
   */
  Object getMetadataValue(String id, String field)
    throws BadFormException, IOException, VisADException;

  /**
   * Obtains a hashtable containing all metadata field/value pairs from
   * the given file.
   * @param id the filename
   * @return the hashtable containing all metadata associated with the file
   */
  Hashtable getMetadata(String id)
    throws BadFormException, IOException, VisADException;

}
