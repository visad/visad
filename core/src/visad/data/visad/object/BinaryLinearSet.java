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
import java.io.EOFException;
import java.io.IOException;

import visad.CoordinateSystem;
import visad.ErrorEstimate;
import visad.GriddedSet;
import visad.LinearSet;
import visad.Linear1DSet;
import visad.Linear2DSet;
import visad.Linear3DSet;
import visad.LinearLatLonSet;
import visad.LinearNDSet;
import visad.MathType;
import visad.SetType;
import visad.Unit;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryLinearSet
  implements BinaryObject
{
  public static final int computeBytes(boolean matchedTypes, double[] firsts,
                                       double[] lasts, int[] lengths,
                                       Linear1DSet[] comps,
                                       CoordinateSystem cs, Unit[] units,
                                       ErrorEstimate[] errors)
  {
    int compsLen;
    if (matchedTypes) {
      compsLen = 1 + BinaryDoubleArray.computeBytes(firsts) +
        1 + BinaryDoubleArray.computeBytes(lasts) +
        1 + BinaryIntegerArray.computeBytes(lengths);
    } else {
      compsLen = 1 + 4;
      for (int i = 0; i < comps.length; i++) {
        int size = BinaryGeneric.computeBytes(comps[i]);
        if (size < 0) {
          return -1;
        }

        compsLen += size;
      }
    }

    final int unitsLen = BinaryUnit.computeBytes(units);
    final int errorsLen = BinaryErrorEstimate.computeBytes(errors);
    return 1 + 4 + 1 + 4 +
      compsLen +
      (cs == null ? 0 : 5) +
      (unitsLen == 0 ? 0 : unitsLen + 1) +
      (errorsLen == 0 ? 0 : errorsLen + 1) +
      1;
  }

  public static boolean hasMatchedTypes(SetType type, Linear1DSet[] comps)
  {
    if (comps == null) {
      return true;
    }

    MathType[] dComp = type.getDomain().getComponents();
    if (dComp == null || dComp.length != comps.length) {
      return false;
    }

    boolean matchedTypes = true;
    for (int i = 0; i < dComp.length; i++) {
      if (!dComp[i].equals(comps[i].getType())) {
        matchedTypes = false;
        break;
      }
    }

    return matchedTypes;
  }

  private static final Linear1DSet[] readLinear1DSets(BinaryReader reader)
    throws IOException, VisADException
  {
    DataInput file = reader.getInput();

    Linear1DSet[] sets = new Linear1DSet[file.readInt()];
    for (int i = 0; i < sets.length; i++) {
      sets[i] = (Linear1DSet )BinaryGeneric.read(reader);
    }
    return sets;
  }

  public static final GriddedSet read(BinaryReader reader, byte dataType)
    throws IOException, VisADException
  {
    BinaryObjectCache cSysCache = reader.getCoordinateSystemCache();
    BinaryObjectCache typeCache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdLinSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdLinSet: type index (" + typeIndex + "=" + st + ")");

    double[] firsts = null;
    double[] lasts = null;
    int[] lengths = null;
    CoordinateSystem cs = null;
    Unit[] units = null;
    ErrorEstimate[] errs = null;
    Linear1DSet[] comps = null;

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_FIRSTS:
if(DEBUG_RD_DATA)System.err.println("rdLinSet: FLD_FIRSTS (" + FLD_FIRSTS + ")");
        firsts = BinaryDoubleArray.read(reader);
        break;
      case FLD_LASTS:
if(DEBUG_RD_DATA)System.err.println("rdLinSet: FLD_LASTS (" + FLD_LASTS + ")");
        lasts = BinaryDoubleArray.read(reader);
        break;
      case FLD_LENGTHS:
if(DEBUG_RD_DATA)System.err.println("rdLinSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
        lengths = BinaryIntegerArray.read(reader);
        break;
      case FLD_LINEAR_SETS:
if(DEBUG_RD_DATA)System.err.println("rdLinSet: FLD_LINEAR_SETS (" + FLD_LINEAR_SETS + ")");
        comps = readLinear1DSets(reader);
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_RD_DATA)System.err.println("rdLinSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        final int index = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_CSYS)System.err.println("rdLinSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_RD_DATA&&!DEBUG_RD_CSYS)System.err.println("rdLinSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_RD_DATA)System.err.println("rdLinSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = BinaryUnit.readList(reader);
        break;
      case FLD_INDEX_ERRORS:
if(DEBUG_RD_DATA)System.err.println("rdLinSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        errs = BinaryErrorEstimate.readList(reader);
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdLinSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown LinearSet directive " + directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for LinearSet");
    }

    if (comps != null) {
      if (firsts != null) {
        throw new IOException("Both components and firsts found for LinearSet");
      }
      if (lasts != null) {
        throw new IOException("Both components and lasts found for LinearSet");
      }
      if (lengths != null) {
        throw new IOException("Both components and lengths found for LinearSet");
      }

      switch (dataType) {
      case DATA_LINEAR_1D_SET:
        throw new IOException("Components specified for Linear1DSet");
      case DATA_LINEAR_2D_SET:
        return new Linear2DSet(st, comps, cs, units, errs);
      case DATA_LINEAR_3D_SET:
        return new Linear3DSet(st, comps, cs, units, errs);
      case DATA_LINEAR_ND_SET:
        return new LinearNDSet(st, comps, cs, units, errs);
      case DATA_LINEAR_LATLON_SET:
        return new LinearLatLonSet(st, comps, cs, units, errs);
      default:
        throw new IOException("Unknown LinearSet type " + dataType);
      }
    } else {

      if (firsts == null) {
        throw new IOException("No firsts found for LinearSet");
      }
      if (lasts == null) {
        throw new IOException("No lasts found for LinearSet");
      }
      if (lengths == null) {
        throw new IOException("No lengths found for LinearSet");
      }

      final int dim;
      switch (dataType) {
      case DATA_LINEAR_1D_SET:
        dim = 1;
        break;
      case DATA_LINEAR_2D_SET:
        dim = 2;
        break;
      case DATA_LINEAR_3D_SET:
        dim = 3;
        break;
      case DATA_LINEAR_LATLON_SET:
        dim = 2;
        break;
      default:
        dim = -1;
        break;
      }

      if (dim > 0 && firsts.length != dim) {
        throw new VisADException("Expected " + dim + " first value" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 firsts.length);
      }
      if (dim > 0 && lasts.length != dim) {
        throw new VisADException("Expected " + dim + " last value" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 lasts.length);
      }
      if (dim > 0 && lengths.length != dim) {
        throw new VisADException("Expected " + dim + " length" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 lengths.length);
      }

      switch (dataType) {
      case DATA_LINEAR_1D_SET:
        return new Linear1DSet(st, firsts[0], lasts[0], lengths[0], cs, units,
                               errs);
      case DATA_LINEAR_2D_SET:
        return new Linear2DSet(st, firsts[0], lasts[0], lengths[0],
                               firsts[1], lasts[1], lengths[1], cs, units, errs);
      case DATA_LINEAR_3D_SET:
        return new Linear3DSet(st, firsts[0], lasts[0], lengths[0],
                               firsts[1], lasts[1], lengths[1],
                               firsts[2], lasts[2], lengths[2], cs, units, errs);
      case DATA_LINEAR_ND_SET:
        return new LinearNDSet(st, firsts, lasts, lengths, cs, units, errs);
      case DATA_LINEAR_LATLON_SET:
        return new LinearLatLonSet(st, firsts[0], lasts[0], lengths[0],
                                   firsts[1], lasts[1], lengths[1],
                                   cs, units, errs);
      default:
        throw new IOException("Unknown LinearSet type " + dataType);
      }
    }
  }

  public static final void writeDependentData(BinaryWriter writer,
                                              SetType type,
                                              Linear1DSet[] comps,
                                              CoordinateSystem cs,
                                              Unit[] units,
                                              ErrorEstimate[] errors,
                                              GriddedSet set,
                                              Class canonicalClass,
                                              Object token)
    throws IOException
  {
    if (!set.getClass().equals(canonicalClass) &&
        !(set instanceof LinearSet && set instanceof Saveable))
    {
      return;
    }

    Object dependToken;
    if (token == SAVE_DEPEND_BIG) {
      dependToken = token;
    } else {
      dependToken = SAVE_DEPEND;
    }

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrLinSet: type (" + type + ")");
    BinarySetType.write(writer, type, set, SAVE_DATA);

    if (cs != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_CSYS)System.err.println("wrLinSet: coordSys (" + cs + ")");
      BinaryCoordinateSystem.write(writer, cs, SAVE_DATA);
    }

    if (units != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_UNIT){
  System.err.println("wrLinSet: List of " + units.length + " Units");
  for(int x=0;x<units.length;x++){
    System.err.println("wrLinSet:    #"+x+": "+units[x]);
  }
}
      BinaryUnit.writeList(writer, units, SAVE_DATA);
    }

    if (errors != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_ERRE){
  System.err.println("wrLinSet: List of " + errors.length + " ErrorEstimates");
  for(int x=0;x<errors.length;x++){
    System.err.println("wrLinSet:    #"+x+": "+errors[x]);
  }
}
      BinaryErrorEstimate.writeList(writer, errors, SAVE_DATA);
    }

    if (comps != null) {
      for (int i = 0; i < comps.length; i++) {
        BinaryGeneric.write(writer, comps[i], dependToken);
      }
    }
  }

  public static final void write(BinaryWriter writer, SetType type,
                                 double[] firsts, double[] lasts,
                                 int[] lengths, Linear1DSet[] comps,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, GriddedSet set,
                                 Class canonicalClass, byte dataType,
                                 Object token)
    throws IOException
  {
    writeDependentData(writer, type, comps, cs, units, errors, set,
                       canonicalClass, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    if (!set.getClass().equals(canonicalClass) &&
        !(set instanceof LinearSet && set instanceof Saveable))
    {
if(DEBUG_WR_DATA)System.err.println("wrLinSet: punt "+set.getClass().getName());
      BinaryUnknown.write(writer, set, token);
      return;
    }

    // see if domain types and component types match
    boolean matchedTypes = hasMatchedTypes(type, comps);

    final int dim = set.getDimension();

    if (!matchedTypes) {
      if (dataType == DATA_LINEAR_1D_SET) {
        throw new IOException("Components specified for Linear1DSet");
      }

      if (comps.length != dim) {
        throw new IOException("Expected " + dim + " LinearSet component" +
                              (dim > 1 ? "s" : "") + ", not " +
                              comps.length);
      }
    } else {
      if (firsts == null) {
        throw new IOException("Null " + canonicalClass.getName() + " firsts");
      }
      if (lasts == null) {
        throw new IOException("Null " + canonicalClass.getName() + " lasts");
      }
      if (lengths == null) {
        throw new IOException("Null " + canonicalClass.getName() +
                              " lengths");
      }

      if (firsts.length != dim) {
        throw new IOException("Expected " + dim + " LinearSet first value" +
                              (dim > 1 ? "s" : "") + ", not " +
                              firsts.length);
      }
      if (lasts.length != dim) {
        throw new IOException("Expected " + dim + " LinearSet last value" +
                              (dim > 1 ? "s" : "") + ", not " +
                              lasts.length);
      }
      if (lengths.length != dim) {
        throw new IOException("Expected " + dim + " LinearSet length" +
                              (dim > 1 ? "s" : "") + ", not " +
                              lengths.length);
      }
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

    int[] errorsIndex = null;
    if (errors != null) {
      errorsIndex = BinaryErrorEstimate.lookupList(writer.getErrorEstimateCache(),
                                                   errors);
    }

    final int objLen = computeBytes(matchedTypes, firsts, lasts, lengths,
                                    comps, cs, units, errors);

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrLinSet: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrLinSet: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrLinSet: " +
                                 (dataType == DATA_LINEAR_1D_SET ?
                                  "DATA_LINEAR_1D" :
                                  (dataType == DATA_LINEAR_2D_SET ?
                                   "DATA_LINEAR_2D" :
                                   (dataType == DATA_LINEAR_3D_SET ?
                                    "DATA_LINEAR_3D" :
                                    (dataType == DATA_LINEAR_ND_SET ?
                                     "DATA_LINEAR_ND" :
                                     (dataType == DATA_LINEAR_LATLON_SET ?
                                      "DATA_LINEAR_LATLON" :
                                      "DATA_???"))))) +
                                 "(" + dataType + ")");
    file.writeByte(dataType);

if(DEBUG_WR_DATA)System.err.println("wrLinSet: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

    if (matchedTypes) {
if(DEBUG_WR_DATA)System.err.println("wrLinSet: FLD_FIRSTS (" + FLD_FIRSTS + ")");
      file.writeByte(FLD_FIRSTS);
      BinaryDoubleArray.write(writer, firsts, token);

if(DEBUG_WR_DATA)System.err.println("wrLinSet: FLD_LASTS (" + FLD_LASTS + ")");
      file.writeByte(FLD_LASTS);
      BinaryDoubleArray.write(writer, lasts, token);

if(DEBUG_WR_DATA)System.err.println("wrLinSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
      file.writeByte(FLD_LENGTHS);
      BinaryIntegerArray.write(writer, lengths, token);
    } else {
if(DEBUG_WR_DATA)System.err.println("wrLinSet: FLD_LINEAR_SETS (" + FLD_LINEAR_SETS + ")");
      file.writeByte(FLD_LINEAR_SETS);
if(DEBUG_WR_DATA)System.err.println("wrLinSet: length (" + comps.length + ")");
      file.writeInt(comps.length);
      for (int i = 0; i < comps.length; i++) {
        BinaryGeneric.write(writer, comps[i], token);
      }
    }

    if (csIndex >= 0) {
if(DEBUG_WR_DATA)System.err.println("wrLinSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
      file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_WR_DATA)System.err.println("wrLinSet: csIndex (" + csIndex + ")");
      file.writeInt(csIndex);
    }

    if (unitsIndex != null) {
if(DEBUG_WR_DATA)System.err.println("wrLinSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
      file.writeByte(FLD_INDEX_UNITS);
      BinaryIntegerArray.write(writer, unitsIndex, token);
    }

    if (errorsIndex != null) {
if(DEBUG_WR_DATA)System.err.println("wrLinSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
      file.writeByte(FLD_INDEX_ERRORS);
      BinaryIntegerArray.write(writer, errorsIndex, token);
    }

if(DEBUG_WR_DATA)System.err.println("wrLinSet: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
