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

import visad.Set;
import visad.SimpleSet;
import visad.TypeException;
import visad.VisADException;

import visad.data.netcdf.Quantity;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryQuantity
  implements BinaryObject
{
  public static final int computeBytes(Quantity qt)
  {
    return 5 +
      BinaryString.computeBytes(qt.getName()) +
      BinaryString.computeBytes(qt.getDefaultUnitString()) +
      (qt.getDefaultSet() == null ? 0 : 1) +
      1;
  }

  public static final Quantity read(BinaryReader reader, int index)
    throws IOException, VisADException
  {
    // read the name
    String name = BinaryString.read(reader);
if(DEBUG_RD_MATH&&!DEBUG_RD_STR)System.err.println("rdQuant: name (" + name + ")");

    // read the name
    String unitSpec = BinaryString.read(reader);
if(DEBUG_RD_MATH&&!DEBUG_RD_STR)System.err.println("rdQuant: unitSpec (" + unitSpec + ")");

    boolean setFollowsType = false;

    BinaryObjectCache cache = reader.getTypeCache();
    DataInput file = reader.getInput();

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_SET_FOLLOWS_TYPE:
if(DEBUG_RD_MATH)System.err.println("rdQuant: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        setFollowsType = true;
        break;
      case FLD_END:
if(DEBUG_RD_MATH)System.err.println("rdQuant: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown RealType directive " + directive);
      }
    }

    Quantity q;
    try {
      q = Quantity.getQuantity(name, unitSpec);
    } catch (visad.data.units.ParseException pe) {
      throw new VisADException("Couldn't parse Quantity unitSpec \"" +
                               unitSpec + "\"");
    }

    if (q == null) {
      throw new VisADException("Couldn't create Quantity named \"" + name +
                               "\"");
    }

    cache.add(index, q);

    if (setFollowsType) {
      SimpleSet set = (SimpleSet )BinaryGeneric.read(reader);
      try {
        q.setDefaultSet(set);
      } catch (TypeException te) {
        // ignore failure to set type
      }
    }

    return q;
  }

  public static final int write(BinaryWriter writer, Quantity qt,
                                Object token)
    throws IOException
  {
    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(qt);
    if (index < 0) {
      index = cache.add(qt);
      if (index < 0) {
        throw new IOException("Couldn't cache Quantity " + qt);
      }

      if (!qt.getClass().equals(Quantity.class) &&
          !(qt instanceof Quantity && qt instanceof Saveable))
      {
if(DEBUG_WR_MATH)System.err.println("wrQuant: serialized Quantity (" + qt.getClass().getName() + ")");
        BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, qt, token);
        return index;
      }

      DataOutput file = writer.getOutput();

      String nameStr = qt.getName();
      String unitStr = qt.getDefaultUnitString();

      Set dfltSet = qt.getDefaultSet();

      // total number of bytes written for this object
      final int objLen = computeBytes(qt);

if(DEBUG_WR_MATH)System.err.println("wrQuant: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_WR_MATH)System.err.println("wrQuant: objLen (" + objLen + ")");
      file.writeInt(objLen);
if(DEBUG_WR_MATH)System.err.println("wrQuant: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_WR_MATH)System.err.println("wrQuant: MATH_QUANTITY (" + MATH_QUANTITY + ")");
      file.writeByte(MATH_QUANTITY);

if(DEBUG_WR_MATH)System.err.println("wrQuant: name (" + nameStr + ")");
      BinaryString.write(writer, nameStr, token);

if(DEBUG_WR_MATH)System.err.println("wrQuant: unitSpec (" + unitStr + ")");
      BinaryString.write(writer, unitStr, token);

      if (dfltSet != null) {
if(DEBUG_WR_MATH)System.err.println("wrQuant: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        file.writeByte(FLD_SET_FOLLOWS_TYPE);
      }

if(DEBUG_WR_MATH)System.err.println("wrQuant: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);

      if (dfltSet != null) {
        BinaryGeneric.write(writer, dfltSet, SAVE_DATA);
      }
    }

    return index;
  }
}
