//
// VisADCachingForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

import java.io.IOException;

import ucar.netcdf.RandomAccessFile;

import visad.DataImpl;
import visad.VisADException;

import visad.data.BadFormException;

/**
   VisADForm is the VisAD data format adapter for
   binary visad.Data objects.<P>
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

  public synchronized DataImpl open(String id)
    throws BadFormException, VisADException
  {
    String errMsg = null;

    // try to read a binary object
    BinaryReader rdr;
    try {
      return readData(new BinaryReader(new RandomAccessFile(id, "r")));
    } catch (Exception e) {
e.printStackTrace();
      errMsg = e.getMessage();
    }

    return super.open(id);
  }

  DataImpl readData(BinaryReader rdr)
    throws IOException, VisADException
  {
    // don't close the file here, it might be needed by a FileFlatField
    return rdr.getData();
  }
}
