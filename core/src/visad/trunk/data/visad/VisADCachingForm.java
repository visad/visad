//
// VisADCachingForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.visad;

import java.io.FileInputStream;
import java.io.IOException;

import java.net.URL;

import ucar.netcdf.RandomAccessFile;

import visad.DataImpl;
import visad.VisADException;

import visad.data.BadFormException;

/**
 * VisADCachingForm is the VisAD data format adapter
 * for large binary visad.Data objects which may not
 * fit in memory.<P>
 */
public class VisADCachingForm
  extends VisADForm
{
  public VisADCachingForm()
  {
    super(true);
  }

  public boolean isThisType(String name) { return false; }
  public boolean isThisType(byte[] block) { return false; }

  public String[] getDefaultSuffixes() { return null; }

  public synchronized DataImpl open(URL url)
    throws BadFormException, VisADException
  {
    throw new VisADException("Cannot cache URL " + url);
  }

  public synchronized DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    IOException savedIOE = null;
    VisADException savedVE = null;

    // try to read a binary object
    try {
      return readData(new BinaryReader(new RandomAccessFile(id, "r")));
    } catch (IOException ioe) {
      savedIOE = ioe;
    } catch (VisADException ve) {
      savedVE = ve;
    }

    // maybe it's a serialized object
    try {
      return readSerial(new FileInputStream(id));
    } catch (ClassNotFoundException cnfe) {
      if (savedIOE != null) {
        throw savedIOE;
      } else if (savedVE != null) {
        throw savedVE;
      }

      throw new BadFormException("Could not read file \"" + id + "\": " +
                                 cnfe.getMessage());
    } catch (IOException ioe) {
      if (savedIOE != null) {
        throw savedIOE;
      } else if (savedVE != null) {
        throw savedVE;
      }

      throw ioe;
    }
  }

  public DataImpl readData(BinaryReader rdr)
    throws IOException, VisADException
  {
    // don't close the file here, it might be needed by a FileFlatField
    return rdr.getData();
  }
}
