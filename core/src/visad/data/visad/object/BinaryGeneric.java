/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.visad.object;

import java.io.IOException;

import visad.DataImpl;
import visad.VisADException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinarySizer;
import visad.data.visad.BinaryWriter;

public class BinaryGeneric
  implements BinaryObject
{
  public static final int computeBytes(DataImpl data)
  {
    BinarySizer sizer = new BinarySizer();
    try {
      sizer.process(data, null);
    } catch (VisADException ve) {
      return -1;
    }
    return sizer.getSize();
  }

  public static final DataImpl read(BinaryReader reader)
    throws IOException
  {
    try {
      return reader.getData();
    } catch (VisADException ve) {
      throw new IOException("Couldn't read file: " + ve.getMessage());
    }
  }

  public static final void write(BinaryWriter writer, DataImpl data,
                                 Object token)
    throws IOException
  {
    try {
      writer.process(data, token);
    } catch (VisADException ve) {
      throw new IOException("Couldn't write " + data.getClass().getName() +
                            ": " + ve.getMessage());
    }
  }
}
