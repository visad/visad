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

package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import visad.TextType;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryTextType
  implements BinaryObject
{
  public static final int computeBytes(TextType tt)
  {
    return 5 +
      BinaryString.computeBytes(tt.getName()) +
      1;
  }

  public static final TextType read(BinaryReader reader, int index)
    throws IOException, VisADException
  {
    // read the name
    String name = BinaryString.read(reader);
if(DEBUG_RD_MATH&&!DEBUG_RD_STR)System.err.println("rdTxTy: name (" + name + ")");

    DataInput file = reader.getInput();

    final byte endByte = file.readByte();
    if (endByte != FLD_END) {
if(DEBUG_RD_MATH)System.err.println("rdTxTy: read " + endByte + " (wanted FLD_END)");
      throw new IOException("Corrupted file (no TextType end-marker)");
    }
if(DEBUG_RD_MATH)System.err.println("rdTxTy: FLD_END (" + endByte + ")");

    TextType tt = TextType.getTextType(name);

    BinaryObjectCache cache = reader.getTypeCache();

    cache.add(index, tt);

    return tt;
  }

  public static final int write(BinaryWriter writer, TextType tt,
                                Object token)
    throws IOException
  {
    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(tt);
    if (index < 0) {
      index = cache.add(tt);
      if (index < 0) {
        throw new IOException("Couldn't cache TextType " + tt);
      }

      if (!tt.getClass().equals(TextType.class) &&
          !(tt instanceof TextType && tt instanceof Saveable))
      {
if(DEBUG_WR_MATH)System.err.println("wrTxTy: serialized TextType (" + tt.getClass().getName() + ")");
        BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, tt, token);
        return index;
      }

      String name = tt.getName();

      final int objLen = computeBytes(tt);

      DataOutput file = writer.getOutput();

if(DEBUG_WR_MATH)System.err.println("wrTxTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_WR_MATH)System.err.println("wrTxTy: objLen (" + objLen + ")");
      file.writeInt(objLen);
if(DEBUG_WR_MATH)System.err.println("wrTxTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_WR_MATH)System.err.println("wrTxTy: MATH_TEXT (" + MATH_TEXT + ")");
      file.writeByte(MATH_TEXT);

if(DEBUG_WR_MATH)System.err.println("wrTxTy: name (" + name + ")");
      BinaryString.write(writer, name, token);

if(DEBUG_WR_MATH)System.err.println("wrTxTy: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    }

    return index;
  }
}
