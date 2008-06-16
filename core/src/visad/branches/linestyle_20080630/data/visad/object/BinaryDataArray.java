/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
import java.io.DataOutput;
import java.io.IOException;

import visad.Data;
import visad.DataImpl;
import visad.VisADException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryDataArray
  implements BinaryObject
{
  public static final int computeBytes(Data[] array)
  {
    int len = 4;
    for (int i = 0; i < array.length; i++) {
      len += BinaryGeneric.computeBytes((DataImpl )array[i]);
    }
    return len;
  }

  public static final Data[] read(BinaryReader reader)
    throws IOException, VisADException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdDataRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad Data array length " +
                            len + ")");
    }

long t = (DEBUG_RD_TIME ? System.currentTimeMillis() : 0);
    Data[] array = new Data[len];
    for (int i = 0; i < len; i++) {

if(DEBUG_WR_DATA)System.err.println("rdDataRA#"+i);
      array[i] = BinaryGeneric.read(reader);
if(DEBUG_WR_DATA_DETAIL)System.err.println("rdDataRA: #" + i + " (" + array[i] + ")");

if(DEBUG_WR_DATA)System.err.println("rdDataRA#"+i+": "+array[i].getClass().getName());
    }
if(DEBUG_RD_TIME)System.err.println("rdDataRA: "+len+" elements "+(System.currentTimeMillis()-t));

    return array;
  }

  private static final void writeDependentData(BinaryWriter writer,
                                               Data[] array, Object token)
    throws IOException
  {
    if (token != SAVE_DEPEND_BIG) {
      token = SAVE_DEPEND;
    }

    if (array != null) {
      for (int i = 0; i < array.length; i++) {
        BinaryGeneric.write(writer, (DataImpl )array[i], token);
      }
    }
  }

  public static final void write(BinaryWriter writer, Data[] array,
                                 Object token)
    throws IOException
  {
    writeDependentData(writer, array, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrDataRA: len (" + array.length + ")");
    file.writeInt(array.length);
    for (int i = 0; i < array.length; i++) {
if(DEBUG_WR_DATA_DETAIL)System.err.println("wrDataRA: #" + i + " (" + array[i] + ")");

if(DEBUG_WR_DATA)System.err.println("wrDataRA#"+i+": "+array[i].getClass().getName());
      BinaryGeneric.write(writer, (DataImpl )array[i], token);
    }
  }
}
