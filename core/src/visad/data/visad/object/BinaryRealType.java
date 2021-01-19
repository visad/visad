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
import java.io.EOFException;

import visad.DisplayRealType;
import visad.RealType;
import visad.Set;
import visad.TypeException;
import visad.Unit;
import visad.VisADException;

import visad.data.netcdf.Quantity;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryRealType
  implements BinaryObject
{
  public static final int computeBytes(RealType rt)
  {
    return 9 +
      BinaryString.computeBytes(rt.getName()) +
      (rt.getDefaultUnit() == null ? 0 : 5) +
      (rt.getDefaultSet() == null ? 0 : 1) +
      1;
  }

  public static final RealType read(BinaryReader reader, int index)
    throws IOException, VisADException
  {
    BinaryObjectCache typeCache = reader.getTypeCache();
    BinaryObjectCache unitCache = reader.getUnitCache();
    DataInput file = reader.getInput();

    final int attrMask = file.readInt();
if(DEBUG_RD_MATH)System.err.println("rdRlTy: attrMask (" + attrMask + ")");

    // read the name
    String name = BinaryString.read(reader);
if(DEBUG_RD_MATH&&!DEBUG_RD_STR)System.err.println("rdRlTy: name (" + name + ")");

    Unit u = null;
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
      case FLD_INDEX_UNIT:
if(DEBUG_RD_MATH)System.err.println("rdRlTy: FLD_INDEX_UNIT (" + FLD_INDEX_UNIT + ")");
        final int uIndex = file.readInt();
if(DEBUG_RD_MATH&&DEBUG_RD_UNIT)System.err.println("rdRlTy: unit index (" + index + ")");
        u = (Unit )unitCache.get(uIndex);
if(DEBUG_RD_MATH&&!DEBUG_RD_UNIT)System.err.println("rdRlTy: unit index (" + index + "=" + u + ")");
        break;
      case FLD_SET_FOLLOWS_TYPE:
if(DEBUG_RD_MATH)System.err.println("rdRlTy: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        setFollowsType = true;
        break;
      case FLD_END:
if(DEBUG_RD_MATH)System.err.println("rdRlTy: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown RealType directive " + directive);
      }
    }

    RealType rt = RealType.getRealType(name, u, null, attrMask);

    typeCache.add(index, rt);

    if (setFollowsType) {
      Set set = (Set )BinaryGeneric.read(reader);
      try {
        rt.setDefaultSet(set);
      } catch (TypeException te) {
        // ignore failure to set type
      }
    }

    return rt;
  }

  public static final int write(BinaryWriter writer, RealType rt, Object token)
    throws IOException
  {
    if (rt instanceof DisplayRealType) {
      return BinaryDisplayRealType.write(writer, (DisplayRealType )rt, token);
    } else if (rt instanceof Quantity) {
      return BinaryQuantity.write(writer, (Quantity )rt, token);
    }

    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(rt);
    if (index < 0) {
      index = cache.add(rt);
      if (index < 0) {
        throw new IOException("Couldn't cache RealType " + rt);
      }

      if (!rt.getClass().equals(RealType.class) &&
          !(rt instanceof RealType && rt instanceof Saveable))
      {
if(DEBUG_WR_MATH)System.err.println("wrRlTy: serialized RealType (" + rt.getClass().getName() + ")");
        BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, rt, token);
        return index;
      }

      String name = rt.getName();

      Set dfltSet = rt.getDefaultSet();

      int uIndex = -1;
      Unit u = rt.getDefaultUnit();
      if (u != null) {
if(DEBUG_WR_MATH&&!DEBUG_WR_UNIT)System.err.println("wrRlTy: Unit (" + u + ")");
        uIndex = BinaryUnit.write(writer, u, token);
      }

      final int objLen = computeBytes(rt);

      DataOutput file = writer.getOutput();

if(DEBUG_WR_MATH)System.err.println("wrRlTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_WR_MATH)System.err.println("wrRlTy: objLen (" + objLen + ")");
      file.writeInt(objLen);
if(DEBUG_WR_MATH)System.err.println("wrRlTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_WR_MATH)System.err.println("wrRlTy: MATH_REAL (" + MATH_REAL + ")");
      file.writeByte(MATH_REAL);

if(DEBUG_WR_MATH)System.err.println("wrRlTy: attrMask (" + rt.getAttributeMask() + ")");
      file.writeInt(rt.getAttributeMask());

if(DEBUG_WR_MATH)System.err.println("wrRlTy: name (" + name + ")");
      BinaryString.write(writer, name, token);

      if (uIndex >= 0) {
if(DEBUG_WR_MATH)System.err.println("wrRlTy: FLD_INDEX_UNIT (" + FLD_INDEX_UNIT + ")");
        file.writeByte(FLD_INDEX_UNIT);
if(DEBUG_WR_MATH)System.err.println("wrRlTy: unit index ("+uIndex+"="+u+")");
        file.writeInt(uIndex);
      }

      if (dfltSet != null) {
if(DEBUG_WR_MATH)System.err.println("wrRlTy: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        file.writeByte(FLD_SET_FOLLOWS_TYPE);
      }

if(DEBUG_WR_MATH)System.err.println("wrRlTy: FLD_END (" + FLD_END + ")");
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
