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
import java.io.EOFException;
import java.io.IOException;

import visad.CoordinateSystem;
import visad.DisplayTupleType;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.RealVectorType;
import visad.Set;
import visad.TypeException;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryRealTupleType
  implements BinaryObject
{
  public static final int computeBytes(RealTupleType rtt)
  {
    return 9 +
      (rtt.getDimension() * 4) +
      (rtt.getCoordinateSystem() == null ? 0 : 5) +
      (rtt.getDefaultSet() == null ? 0 : 1) +
      1;
  }

  public static final RealTupleType read(BinaryReader reader, int index)
    throws IOException, VisADException
  {
    BinaryObjectCache cSysCache = reader.getCoordinateSystemCache();
    BinaryObjectCache typeCache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int dim = file.readInt();
if(DEBUG_RD_MATH)System.err.println("rdRlTuTy: dim (" + dim + ")");
    MathType[] mtList = BinaryMathType.readList(reader, dim);

    RealType[] list = new RealType[mtList.length];
    for (int i = 0; i < mtList.length; i++) {
      list[i] = (RealType )mtList[i];
    }

    CoordinateSystem cs = null;
    boolean setFollowsType = false;

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_INDEX_COORDSYS:
if(DEBUG_RD_MATH)System.err.println("rdRlTuTy: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        final int csIndex = file.readInt();
if(DEBUG_RD_MATH&&DEBUG_RD_CSYS)System.err.println("rdRlTuTy: cSys index (" + csIndex + ")");
        cs = (CoordinateSystem )cSysCache.get(csIndex);
if(DEBUG_RD_MATH&&!DEBUG_RD_CSYS)System.err.println("rdRlTuTy: cSys index (" + csIndex + "=" + cs + ")");
        break;
      case FLD_SET_FOLLOWS_TYPE:
if(DEBUG_RD_MATH)System.err.println("rdRlYuTy: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        setFollowsType = true;
        break;
      case FLD_END:
if(DEBUG_RD_MATH)System.err.println("rdRlTuTy: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown RealTupleType directive " + directive);
      }
    }

    RealTupleType rtt = new RealTupleType(list, cs, null);

    typeCache.add(index, rtt);

    if (setFollowsType) {
      Set set = (Set )BinaryGeneric.read(reader);
      try {
        rtt.setDefaultSet(set);
      } catch (TypeException te) {
        // ignore failure to set type
      }
    }

    return rtt;
  }

  public static final int write(BinaryWriter writer, RealTupleType rtt,
                                Object token)
    throws IOException
  {
    BinaryObjectCache cache = writer.getTypeCache();

    if (rtt instanceof DisplayTupleType) {
      return BinaryDisplayTupleType.write(writer, (DisplayTupleType )rtt,
                                          token);
    } else if (rtt instanceof RealVectorType) {
      return BinaryRealVectorType.write(writer, (RealVectorType )rtt, token);
    }

    int index = cache.getIndex(rtt);
    if (index < 0) {
      index = cache.add(rtt);
      if (index < 0) {
        throw new IOException("Couldn't cache RealTupleType " + rtt);
      }

      if (!rtt.getClass().equals(RealTupleType.class) &&
          !(rtt instanceof RealTupleType && rtt instanceof Saveable))
      {
if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: serialized RealTupleType (" + rtt.getClass().getName() + ")");
        BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, rtt, token);
        return index;
      }

      final int dim = rtt.getDimension();

      Set dfltSet = rtt.getDefaultSet();

      int[] types = new int[dim];
      for (int i = 0; i < dim; i++) {
        RealType comp;
        try {
          comp = (RealType )rtt.getComponent(i);
        } catch (VisADException ve) {
          throw new IOException("Couldn't get RealTupleType component #" + i +
                                ": " + ve.getMessage());
        }

        types[i] = BinaryRealType.write(writer, comp, token);
      }

      CoordinateSystem cs = rtt.getCoordinateSystem();

      int csIndex = -1;
      if (cs != null) {
if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: coordSys (" + cs + ")");
        csIndex = BinaryCoordinateSystem.write(writer, cs, token);
      }

      final int objLen = computeBytes(rtt);

      DataOutput file = writer.getOutput();

if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: objLen (" + objLen + ")");
      file.writeInt(objLen);
if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: MATH_REAL_TUPLE (" + MATH_REAL_TUPLE + ")");
      file.writeByte(MATH_REAL_TUPLE);

if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: dim (" + dim + ")");
      file.writeInt(dim);

      for (int i = 0; i < dim; i++) {
if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: tuple #" + i + " index (" + types[i] + ")");
        file.writeInt(types[i]);
      }

      if (csIndex >= 0) {
if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: coordSys index (" + csIndex + ")");
        file.writeInt(csIndex);
      }

      if (dfltSet != null) {
if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        file.writeByte(FLD_SET_FOLLOWS_TYPE);
      }

if(DEBUG_WR_MATH)System.err.println("wrRlTuTy: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);

      if (dfltSet != null) {
        Object dependToken;
        if (token == SAVE_DEPEND_BIG) {
          dependToken = token;
        } else {
          dependToken = SAVE_DEPEND;
        }

        BinaryGeneric.write(writer, dfltSet, dependToken);
        BinaryGeneric.write(writer, dfltSet, SAVE_DATA);
      }
    }

    return index;
  }
}
