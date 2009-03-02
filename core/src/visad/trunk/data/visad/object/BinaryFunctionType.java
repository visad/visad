/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

import visad.FunctionType;
import visad.MathType;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryFunctionType
  implements BinaryObject
{
  public static final int computeBytes(FunctionType ft) { return 14; }

  public static final FunctionType read(BinaryReader reader, int index)
    throws IOException, VisADException
  {
    BinaryObjectCache cache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int domainIndex = file.readInt();
if(DEBUG_RD_MATH)System.err.println("rdFuTy: domain index (" + domainIndex + ")");
    MathType domain = (MathType )cache.get(domainIndex);
if(DEBUG_RD_MATH)System.err.println("rdFuTy: === read domain " + domain);
    final int rangeIndex = file.readInt();
if(DEBUG_RD_MATH)System.err.println("rdFuTy: range index (" + rangeIndex + ")");
    MathType range = (MathType )cache.get(rangeIndex);
if(DEBUG_RD_MATH)System.err.println("rdFuTy: === read range " + range);

    final byte endByte = file.readByte();
    if (endByte != FLD_END) {
if(DEBUG_RD_MATH)System.err.println("rdFuTy: read " + endByte + " (wanted FLD_END)");
      throw new IOException("Corrupted file (no TupleType end-marker)");
    }
if(DEBUG_RD_MATH)System.err.println("rdFuTy: FLD_END (" + endByte + ")");

    FunctionType ft = new FunctionType(domain, range);

    cache.add(index, ft);

    return ft;
  }

  public static final int write(BinaryWriter writer, FunctionType ft,
                                Object token)
    throws IOException
  {
    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(ft);
    if (index < 0) {
      index = cache.add(ft);
      if (index < 0) {
        throw new IOException("Couldn't cache FunctionType " + ft);
      }

      int dIndex = BinaryMathType.write(writer, ft.getDomain(), token);
      int rIndex = BinaryMathType.write(writer, ft.getRange(), token);

      // total number of bytes written for this object
      final int objLen = computeBytes(ft);

      DataOutput file = writer.getOutput();

if(DEBUG_WR_MATH)System.err.println("wrFuTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_WR_MATH)System.err.println("wrFuTy: objLen (" + objLen + ")");
      file.writeInt(objLen);
if(DEBUG_WR_MATH)System.err.println("wrFuTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_WR_MATH)System.err.println("wrFuTy: MATH_FUNCTION (" + MATH_FUNCTION + ")");
      file.writeByte(MATH_FUNCTION);

if(DEBUG_WR_MATH)System.err.println("wrFuTy: domain index (" + dIndex + ")");
      file.writeInt(dIndex);
if(DEBUG_WR_MATH)System.err.println("wrFuTy: range index (" + rIndex + ")");
      file.writeInt(rIndex);

if(DEBUG_WR_MATH)System.err.println("wrFuTy: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    }

    return index;
  }
}
