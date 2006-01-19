/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

import visad.MathType;
import visad.RealTupleType;
import visad.TupleType;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryTupleType
  implements BinaryObject
{
  public static final int computeBytes(TupleType tt)
  {
    return 5 +
      (tt.getDimension() * 4) +
      1;
  }

  public static final TupleType read(BinaryReader reader, int index,
                                     int objLen)
    throws IOException, VisADException
  {
    MathType[] list = BinaryMathType.readList(reader, (objLen - 1) / 4);

    BinaryObjectCache cache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final byte endByte = file.readByte();
    if (endByte != FLD_END) {
if(DEBUG_RD_MATH)System.err.println("rdTuTy: read " + endByte + " (wanted FLD_END)");
      throw new IOException("Corrupted file (no TupleType end-marker)");
    }
if(DEBUG_RD_MATH)System.err.println("rdTuTy: FLD_END (" + endByte + ")");

    TupleType tt = new TupleType(list);

    cache.add(index, tt);

    return tt;
  }

  public static final int write(BinaryWriter writer, TupleType tt,
                                Object token)
    throws IOException
  {
    if (tt instanceof RealTupleType) {
      return BinaryRealTupleType.write(writer, (RealTupleType )tt, token);
    }

    final int dim = tt.getDimension();

    int[] types = new int[dim];
    for (int i = 0; i < dim; i++) {
      MathType comp;
      try {
        comp = tt.getComponent(i);
      } catch (VisADException ve) {
        throw new IOException("Couldn't get TupleType component #" + i +
                              ": " + ve.getMessage());
      }

      types[i] = BinaryMathType.write(writer, comp, token);
    }

    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(tt);
    if (index < 0) {
      index = cache.add(tt);
      if (index < 0) {
        throw new IOException("Couldn't cache TupleType " + tt);
      }

      if (!tt.getClass().equals(TupleType.class) &&
          !(tt instanceof TupleType && tt instanceof Saveable))
      {
if(DEBUG_WR_MATH)System.err.println("wrTuTy: serialized TupleType (" + tt.getClass().getName() + ")");
        BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, tt, token);
        return index;
      }

      final int objLen = computeBytes(tt);

      DataOutput file = writer.getOutput();

if(DEBUG_WR_MATH)System.err.println("wrTuTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_WR_MATH)System.err.println("wrTuTy: objLen (" + objLen + ")");
      file.writeInt(objLen);
if(DEBUG_WR_MATH)System.err.println("wrTuTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_WR_MATH)System.err.println("wrTuTy: MATH_TUPLE (" + MATH_TUPLE + ")");
      file.writeByte(MATH_TUPLE);

      for (int i = 0; i < dim; i++) {
if(DEBUG_WR_MATH)System.err.println("wrTuTy: type index #" + i + " (" + types[i] + ")");
        file.writeInt(types[i]);
      }

if(DEBUG_WR_MATH)System.err.println("wrTuTy: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    }

    return index;
  }
}
