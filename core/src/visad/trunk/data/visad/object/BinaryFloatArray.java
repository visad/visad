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

package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryFloatArray
  implements BinaryObject
{
  public static final int computeBytes(float[] array)
  {
    return (array == null ? 0 : 4 + (array.length * 4));
  }

  public static final float[] read(BinaryReader reader)
    throws IOException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdFltRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad float array length " +
                            len + ")");
    }

    float[] array = new float[len];
    for (int i = 0; i < len; i++) {
      array[i] = file.readFloat();
if(DEBUG_RD_DATA_DETAIL)System.err.println("rdFltRA: #" + i +" (" + array[i] + ")");
    }

    return array;
  }

  public static final void write(BinaryWriter writer, float[] array,
                                 Object token)
    throws IOException
  {
    DataOutputStream file = writer.getOutputStream();

if(DEBUG_WR_DATA)System.err.println("wrFltRA: len (" + array.length + ")");
    file.writeInt(array.length);
    for (int i = 0; i < array.length; i++) {
if(DEBUG_WR_DATA_DETAIL)System.err.println("wrFltRA: #" + i + " (" + array[i] + ")");
      file.writeFloat(array[i]);
    }
  }
}
