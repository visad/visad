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
import java.io.IOException;

import visad.FunctionType;
import visad.MathType;
import visad.ScalarType;
import visad.SetType;
import visad.TupleType;
import visad.VisADException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryWriter;

public class BinaryMathType
  implements BinaryObject
{
  public static final MathType read(BinaryReader reader)
    throws IOException, VisADException
  {
    DataInput file = reader.getInput();

    final int objLen = file.readInt();
if(DEBUG_RD_MATH)System.err.println("cchTy: objLen (" + objLen + ")");

    // read the index number for this MathType
    final int index = file.readInt();
if(DEBUG_RD_MATH)System.err.println("cchTy: index (" + index + ")");

    final byte mathType = file.readByte();
    switch (mathType) {
    case MATH_FUNCTION:
if(DEBUG_RD_MATH)System.err.println("rdMthTy: MATH_FUNCTION (" + MATH_FUNCTION + ")");
      return BinaryFunctionType.read(reader, index);
    case MATH_QUANTITY:
if(DEBUG_RD_MATH)System.err.println("rdMthTy: MATH_QUANTITY (" + MATH_QUANTITY + ")");
      return BinaryQuantity.read(reader, index);
    case MATH_REAL_TUPLE:
if(DEBUG_RD_MATH)System.err.println("rdMthTy: MATH_REAL_TUPLE (" + MATH_REAL_TUPLE + ")");
      return BinaryRealTupleType.read(reader, index);
    case MATH_REAL:
if(DEBUG_RD_MATH)System.err.println("rdMthTy: MATH_REAL (" + MATH_REAL + ")");
      return BinaryRealType.read(reader, index);
    case MATH_SET:
if(DEBUG_RD_MATH)System.err.println("rdMthTy: MATH_SET (" + MATH_SET + ")");
      return BinarySetType.read(reader, index);
    case MATH_TEXT:
if(DEBUG_RD_MATH)System.err.println("rdMthTy: MATH_TEXT (" + MATH_TEXT + ")");
      return BinaryTextType.read(reader, index);
    case MATH_TUPLE:
if(DEBUG_RD_MATH)System.err.println("rdMthTy: MATH_TUPLE (" + MATH_TUPLE + ")");
      return BinaryTupleType.read(reader, index, objLen - 5);
    default:
      throw new VisADException("Unknown Math type " + mathType);
    }
  }

  public static final MathType[] readList(BinaryReader reader, int dim)
    throws IOException, VisADException
  {
    if (dim < 1) {
      throw new IOException("Corrupted file (bad MathType list length)");
    }

    BinaryObjectCache cache = reader.getTypeCache();
    DataInput file = reader.getInput();

    MathType[] list = new MathType[dim];
    for (int i = 0; i < dim; i++) {
      final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdMTyS: type #" + i + " index (" + typeIndex + ")");
      list[i] = (MathType )cache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdMTyS: type #" + i + " index (" + typeIndex + "=" + list[i] + ")");
    }

    return list;
  }

  public static final int write(BinaryWriter writer, MathType mt,
                                Object token)
    throws IOException
  {
    int index;

    if (mt instanceof FunctionType) {
      index = BinaryFunctionType.write(writer, (FunctionType )mt, token);
    } else if (mt instanceof ScalarType) {
      index = BinaryScalarType.write(writer, (ScalarType )mt, token);
    } else if (mt instanceof SetType) {
      index = BinarySetType.write(writer, (SetType )mt, null, token);
    } else if (mt instanceof TupleType) {
      index = BinaryTupleType.write(writer, (TupleType )mt, token);
    } else {
      BinaryObjectCache cache = writer.getTypeCache();

      index = cache.getIndex(mt);
      if (index < 0) {
        index = cache.add(mt);
        if (index < 0) {
          throw new IOException("Couldn't cache MathType " + mt);
        }

        BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, mt, token);
      }
    }

    return index;
  }
}
