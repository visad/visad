/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

import visad.Text;
import visad.TextType;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryText
  implements BinaryObject
{
  public static final int computeBytes(String value)
  {
    return 1 + 4 + 1 + 4 +
      BinaryString.computeBytes(value) +
      1;
  }

  public static final Text read(BinaryReader reader)
    throws IOException, VisADException
  {
    BinaryObjectCache cache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdTxt: type index (" + typeIndex + ")");
    TextType tt = (TextType )cache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdTxt: type index (" + typeIndex + "=" + tt + ")");

    // read the value
    String value = BinaryString.read(reader);
if(DEBUG_RD_DATA&&!DEBUG_RD_STR)System.err.println("rdTxt: value (" + value + ")");

    final byte endByte = file.readByte();
    if (endByte != FLD_END) {
if(DEBUG_RD_MATH)System.err.println("rdTxt: read " + endByte + " (wanted FLD_END)");
      throw new IOException("Corrupted file (no Text end-marker)");
    }
if(DEBUG_RD_MATH)System.err.println("rdTxt: FLD_END (" + endByte + ")");

    return new Text(tt, value);
  }

  public static final void writeDependentData(BinaryWriter writer,
                                              TextType type, Object token)
    throws IOException
  {
if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrTxt: MathType (" + type + ")");
    BinaryTextType.write(writer, type, SAVE_DATA);
  }

  public static final void write(BinaryWriter writer, TextType type,
                                 String value, boolean missing, Text text,
                                 Object token)
    throws IOException
  {
    writeDependentData(writer, type, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    int typeIndex = writer.getTypeCache().getIndex(type);
    if (typeIndex < 0) {
      throw new IOException("TextType " + type + " not cached");
    }

    final int objLen = computeBytes(value);

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrTxt: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrTxt: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrTxt: DATA_TEXT (" + DATA_TEXT + ")");
    file.writeByte(DATA_TEXT);

if(DEBUG_WR_DATA)System.err.println("wrTxt: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

if(DEBUG_WR_DATA)System.err.println("wrTxt: value (" + value + ")");
    BinaryString.write(writer, value, token);

    file.writeByte(FLD_END);
  }
}
