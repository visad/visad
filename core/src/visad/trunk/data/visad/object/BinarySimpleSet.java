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
import visad.DoubleSet;
import visad.FloatSet;
import visad.SetType;
import visad.SimpleSet;
import visad.Unit;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinarySimpleSet
  implements BinaryObject
{
  public static final int computeBytes(CoordinateSystem cs, Unit[] units)
  {
    final int unitsLen = BinaryUnit.computeBytes(units);
    return 1 + 4 + 1 + 4 +
      (cs == null ? 0 : 5) +
      (unitsLen == 0 ? 0 : unitsLen + 1) +
      1;
  }

  public static final SimpleSet read(BinaryReader reader, byte dataType)
    throws IOException, VisADException
  {
    BinaryObjectCache typeCache = reader.getTypeCache();
    BinaryObjectCache cSysCache = reader.getCoordinateSystemCache();
    DataInput file = reader.getInput();

    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdSimSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdSimSet: type index (" + typeIndex + "=" + st + ")");

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
      case FLD_INDEX_COORDSYS:
if(DEBUG_RD_DATA)System.err.println("rdSimSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        final int index = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_CSYS)System.err.println("rdSimSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_RD_DATA&&!DEBUG_RD_CSYS)System.err.println("rdSimSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_RD_DATA)System.err.println("rdSimSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = BinaryUnit.readList(reader);
if(DEBUG_RD_DATA&&!DEBUG_RD_UNIT){System.err.println("rdSimSet: array length ("+units.length+")");for(int i=0;i<units.length;i++)System.err.println("rdSimSet:    #"+i+" unit ("+units[i]+")");}
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdSimSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown SimpleSet directive " +
                              directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for SimpleSet");
    }

    switch (dataType) {
    case DATA_FLOAT_SET:
      return new FloatSet(st, cs, units);
    case DATA_DOUBLE_SET:
      return new DoubleSet(st, cs, units);
    default:
      throw new IOException("Unknown SimpleSet type " + dataType);
    }
  }

  public static final void writeDependentData(BinaryWriter writer,
                                              SetType type,
                                              CoordinateSystem cs,
                                              Unit[] units, SimpleSet set,
                                              Class canonicalClass,
                                              Object token)
    throws IOException
  {
    DataOutput file = writer.getOutput();

    if (!set.getClass().equals(canonicalClass) &&
        !(set instanceof SimpleSet && set instanceof Saveable))
    {
      return;
    }

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrSimSet: type (" + type + ")");
    BinarySetType.write(writer, type, set, SAVE_DATA);

    if (cs != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_CSYS)System.err.println("wrSimSet: coordSys (" + cs + ")");
      BinaryCoordinateSystem.write(writer, cs, SAVE_DATA);
    }

    if (units != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_UNIT){
  System.err.println("wrSimSet: List of " + units.length + " Units");
  for(int x=0;x<units.length;x++){
    System.err.println("wrSimSet:    #"+x+": "+units[x]);
  }
}
      BinaryUnit.writeList(writer, units, SAVE_DATA);
    }
  }

  public static final void write(BinaryWriter writer, SetType type,
                                 CoordinateSystem cs, Unit[] units,
                                 SimpleSet set, Class canonicalClass,
                                 byte dataType, Object token)
    throws IOException
  {
    writeDependentData(writer, type, cs, units, set, canonicalClass, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    if (!set.getClass().equals(canonicalClass) &&
        !(set instanceof SimpleSet && set instanceof Saveable))
    {
if(DEBUG_WR_DATA)System.err.println("wrSimSet: punt "+set.getClass().getName());
      BinaryUnknown.write(writer, set, token);
      return;
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
      unitsIndex = BinaryUnit.lookupList(writer.getCoordinateSystemCache(),
                                         units);
    }

    final int objLen = computeBytes(cs, units);

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrSimSet: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrSimSet: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrSimSet: " +
                                    (dataType == DATA_DOUBLE_SET ?
                                     "DATA_DOUBLE_SET" :
                                     (dataType == DATA_FLOAT_SET ?
                                      "DATA_FLOAT_SET" : "DATA_???")) +
                                    "(" + dataType + ")");
    file.writeByte(dataType);

if(DEBUG_WR_DATA)System.err.println("wrSimSet: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

    if (csIndex >= 0) {
if(DEBUG_WR_DATA)System.err.println("wrSimSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
      file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_WR_DATA)System.err.println("wrSimSet: coord sys Index (" + csIndex + ")");
      file.writeInt(csIndex);
    }

    if (unitsIndex != null) {
if(DEBUG_WR_DATA)System.err.println("wrSimSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
      file.writeByte(FLD_INDEX_UNITS);
if(DEBUG_WR_DATA)System.err.println("wrSimSet: array length ("+unitsIndex.length+")");
if(DEBUG_WR_DATA)for(int i=0;i<unitsIndex.length;i++)System.err.println("wrSimSet:    unit #"+i+" index ("+unitsIndex[i]+"="+units[i]+")");
      BinaryIntegerArray.write(writer, unitsIndex, token);
    }

if(DEBUG_WR_DATA)System.err.println("wrSimSet: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
