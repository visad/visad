/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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
import visad.List1DSet;
import visad.MathType;
import visad.SetType;
import visad.Unit;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryList1DSet
  implements BinaryObject
{
  public static final int computeBytes(float[] list, CoordinateSystem cs,
                                       Unit[] units)
  {
    final int unitsLen = BinaryUnit.computeBytes(units);
    return 1 + 4 + 1 + 4 +
      1 + BinaryFloatArray.computeBytes(list) +
      (cs == null ? 0 : 5) +
      (unitsLen == 0 ? 0 : unitsLen + 1) +
      1;
  }

  public static final List1DSet read(BinaryReader reader)
    throws IOException, VisADException
  {
    BinaryObjectCache cSysCache = reader.getCoordinateSystemCache();
    BinaryObjectCache typeCache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdL1DSet: type index (" + typeIndex + ")");
    MathType mt = (MathType )typeCache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdL1DSet: type index (" + typeIndex + "=" + mt + ")");

    float[] list = null;
    CoordinateSystem cs = null;
    Unit[] units = null;

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_FLOAT_LIST:
if(DEBUG_RD_DATA)System.err.println("rdL1DSet: FLD_FLOAT_LIST (" + FLD_FLOAT_LIST + ")");
        list = BinaryFloatArray.read(reader);
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_RD_DATA)System.err.println("rdL1DSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        final int index = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_CSYS)System.err.println("rdL1DSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_RD_DATA&&!DEBUG_RD_CSYS)System.err.println("rdL1DSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_RD_DATA)System.err.println("rdL1DSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = BinaryUnit.readList(reader);
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdL1DSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown List1DSet directive " +
                              directive);
      }
    }

    if (mt == null) {
      throw new IOException("No MathType found for List1DSet");
    }
    if (list == null) {
      throw new IOException("No list found for List1DSet");
    }

    return new List1DSet(list, mt, cs, units);
  }

  public static final void writeDependentData(BinaryWriter writer,
                                              SetType type,
                                              CoordinateSystem cs,
                                              Unit[] units, List1DSet set,
                                              Object token)
    throws IOException
  {
    if (!set.getClass().equals(List1DSet.class) &&
        !(set instanceof List1DSet && set instanceof Saveable))
    {
      return;
    }

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrL1DSet: type (" + type + ")");
    BinarySetType.write(writer, type, set, SAVE_DATA);

    if (cs != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_CSYS)System.err.println("wrL1DSet: coordSys (" + cs + ")");
      BinaryCoordinateSystem.write(writer, cs, SAVE_DATA);
    }

    if (units != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_UNIT){
  System.err.println("wrL1DSet: List of " + units.length + " Units");
  for(int x=0;x<units.length;x++){
    System.err.println("wrL1DSet:    #"+x+": "+units[x]);
  }
}
      BinaryUnit.writeList(writer, units, SAVE_DATA);
    }
  }

  public static final void write(BinaryWriter writer, SetType type,
                                 float[] list, CoordinateSystem cs,
                                 Unit[] units, List1DSet set, Object token)
    throws IOException
  {
    writeDependentData(writer, type, cs, units, set, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    if (!set.getClass().equals(List1DSet.class) &&
        !(set instanceof List1DSet && set instanceof Saveable))
    {
if(DEBUG_WR_DATA)System.err.println("wrL1DSet: punt "+set.getClass().getName());
      BinaryUnknown.write(writer, set, token);
      return;
    }

    if (list == null) {
      throw new IOException("Null List1DSet list");
    }

    int typeIndex = writer.getTypeCache().getIndex(type);
    if (typeIndex < 0) {
      throw new IOException("SetType " + type + " not cached");
    }

    int csIndex = -1;
    if (cs != null) {
      csIndex = writer.getCoordinateSystemCache().getIndex(cs);
      if (csIndex < 0) {
        throw new IOException("CoordinateSystem " + cs + " not cached");
      }
    }

    int[] unitsIndex = null;
    if (units != null) {
      unitsIndex = BinaryUnit.lookupList(writer.getUnitCache(), units);
    }

    final int objLen = computeBytes(list, cs, units);

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrL1DSet: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrL1DSet: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrL1DSet: DATA_LIST1D_SET (" + DATA_LIST1D_SET + ")");
    file.writeByte(DATA_LIST1D_SET);

if(DEBUG_WR_DATA)System.err.println("wrL1DSet: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

if(DEBUG_WR_DATA)System.err.println("wrL1DSet: FLD_FLOAT_LIST (" + FLD_FLOAT_LIST + ")");
    file.writeByte(FLD_FLOAT_LIST);
    BinaryFloatArray.write(writer, list, token);

    if (csIndex >= 0) {
if(DEBUG_WR_DATA)System.err.println("wrL1DSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
      file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_WR_DATA)System.err.println("wrL1DSet: coord sys Index (" + csIndex + ")");
      file.writeInt(csIndex);
    }

    if (unitsIndex != null) {
if(DEBUG_WR_DATA)System.err.println("wrL1DSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
      file.writeByte(FLD_INDEX_UNITS);
      BinaryIntegerArray.write(writer, unitsIndex, token);
    }

if(DEBUG_WR_DATA)System.err.println("wrL1DSet: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
