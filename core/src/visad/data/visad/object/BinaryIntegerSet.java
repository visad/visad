/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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
import visad.IntegerSet;
import visad.Integer1DSet;
import visad.Integer2DSet;
import visad.Integer3DSet;
import visad.IntegerNDSet;
import visad.Linear1DSet;
import visad.LinearSet;
import visad.MathType;
import visad.SetType;
import visad.Unit;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryIntegerSet
  implements BinaryObject
{
  public static final int computeBytes(boolean matchedTypes, int[] lengths,
                                       Integer1DSet[] comps,
                                       CoordinateSystem cs, Unit[] units,
                                       ErrorEstimate[] errors)
  {
    int compsLen;
    if (matchedTypes) {
      compsLen = 1 + BinaryIntegerArray.computeBytes(lengths);
    } else {
      compsLen = 1 + 4;
      for (int i = 0; i < comps.length; i++) {
        int size = BinaryGeneric.computeBytes(comps[i]);
        if (size < 0) {
          compsLen = -1;
          break;
        }

        compsLen += size;
      }
    }

    if (compsLen < 0) {
      return compsLen;
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

  public static final Integer1DSet[] getComponents(LinearSet set)
  {
    final int dim = ((GriddedSet )set).getDimension();

    Integer1DSet[] comps = new Integer1DSet[dim];
    for (int i = 0; i < dim; i++) {
      Linear1DSet comp = set.getLinear1DComponent(i);
      if (comp instanceof Integer1DSet) {
        comps[i] = (Integer1DSet )comp;
      } else if (comp.getFirst() == 0.0) {
        // had to put this in because an old serialized object
        // had Linear1DSets instead of Integer1DSets
        try {
          comps[i] = new Integer1DSet(comp.getType(), comp.getLength(),
                                      comp.getCoordinateSystem(),
                                      comp.getSetUnits(),
                                      comp.getSetErrors());
        } catch (VisADException ve) {
          return null;
        }
      } else {
        // XXX what happens here?
        System.err.println("Ignoring comp#" + i + ": " + comp);
        comps[i] = null;
      }
    }

    return comps;
  }

  public static boolean hasMatchedTypes(SetType type, Integer1DSet[] comps)
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

  private static final Integer1DSet[] readInteger1DSets(BinaryReader reader)
    throws IOException, VisADException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdI1DSetS: len (" + len + ")");
    Integer1DSet[] sets = new Integer1DSet[len];
    for (int i = 0; i < len; i++) {
      sets[i] = (Integer1DSet )BinaryGeneric.read(reader);
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
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdIntSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdIntSet: type index (" + typeIndex + "=" + st + ")");

    int[] lengths = null;
    CoordinateSystem cs = null;
    Unit[] units = null;
    ErrorEstimate[] errs = null;
    Integer1DSet[] comps = null;

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_LENGTHS:
if(DEBUG_RD_DATA)System.err.println("rdIntSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
        lengths = BinaryIntegerArray.read(reader);
        break;
      case FLD_INTEGER_SETS:
if(DEBUG_RD_DATA)System.err.println("rdIntSet: FLD_INTEGER_SETS (" + FLD_INTEGER_SETS + ")");
        comps = readInteger1DSets(reader);
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_RD_DATA)System.err.println("rdIntSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        final int index = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_CSYS)System.err.println("rdIntSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_RD_DATA&&!DEBUG_RD_CSYS)System.err.println("rdIntSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_RD_DATA)System.err.println("rdIntSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = BinaryUnit.readList(reader);
        break;
      case FLD_INDEX_ERRORS:
if(DEBUG_RD_DATA)System.err.println("rdIntSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        errs = BinaryErrorEstimate.readList(reader);
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdIntSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown IntegerSet directive " + directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for IntegerSet");
    }

    if (comps != null) {
      if (lengths != null) {
        throw new IOException("Both components and lengths found for IntegerSet");
      }

      switch (dataType) {
      case DATA_INTEGER_1D_SET:
        throw new IOException("Components specified for Integer1DSet");
      case DATA_INTEGER_2D_SET:
        return new Integer2DSet(st, comps, cs, units, errs);
      case DATA_INTEGER_3D_SET:
        return new Integer3DSet(st, comps, cs, units, errs);
      case DATA_INTEGER_ND_SET:
        return new IntegerNDSet(st, comps, cs, units, errs);
      default:
        throw new IOException("Unknown IntegerSet type " + dataType);
      }
    } else {
      if (lengths == null) {
        throw new IOException("No lengths found for IntegerSet");
      }

      final int dim;
      switch (dataType) {
      case DATA_INTEGER_1D_SET:
        dim = 1;
        break;
      case DATA_INTEGER_2D_SET:
        dim = 2;
        break;
      case DATA_INTEGER_3D_SET:
        dim = 3;
        break;
      default:
        dim = -1;
        break;
      }

      if (dim > 0 && lengths.length != dim) {
        throw new VisADException("Expected " + dim + " length" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 lengths.length);
      }

      switch (dataType) {
      case DATA_INTEGER_1D_SET:
        return new Integer1DSet(st, lengths[0], cs, units, errs);
      case DATA_INTEGER_2D_SET:
        return new Integer2DSet(st, lengths[0], lengths[1], cs, units, errs);
      case DATA_INTEGER_3D_SET:
        return new Integer3DSet(st, lengths[0], lengths[1], lengths[2],
                                cs, units, errs);
      case DATA_INTEGER_ND_SET:
        return new IntegerNDSet(st, lengths, cs, units, errs);
      default:
        throw new IOException("Unknown IntegerSet type " + dataType);
      }
    }
  }

  public static final void writeDependentData(BinaryWriter writer,
                                              SetType type,
                                              Integer1DSet[] comps,
                                              CoordinateSystem cs,
                                              Unit[] units,
                                              ErrorEstimate[] errors,
                                              GriddedSet set,
                                              Class canonicalClass,
                                              Object token)
    throws IOException
  {
    if (!set.getClass().equals(canonicalClass) &&
        !(set instanceof IntegerSet && set instanceof Saveable))
    {
      return;
    }

    Object dependToken;
    if (token == SAVE_DEPEND_BIG) {
      dependToken = token;
    } else {
      dependToken = SAVE_DEPEND;
    }

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrIntSet: type (" + type + ")");
    BinarySetType.write(writer, type, set, SAVE_DATA);

    if (cs != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_CSYS)System.err.println("wrIntSet: coordSys (" + cs + ")");
      BinaryCoordinateSystem.write(writer, cs, SAVE_DATA);
    }

    if (units != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_UNIT){
  System.err.println("wrIntSet: List of " + units.length + " Units");
  for(int x=0;x<units.length;x++){
    System.err.println("wrIntSet:    #"+x+": "+units[x]);
  }
}
      BinaryUnit.writeList(writer, units, SAVE_DATA);
    }

    if (errors != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_ERRE){
  System.err.println("wrIntSet: List of " + errors.length + " ErrorEstimates");
  for(int x=0;x<errors.length;x++){
    System.err.println("wrIntSet:    #"+x+": "+errors[x]);
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
                                 int[] lengths, Integer1DSet[] comps,
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
        !(set instanceof IntegerSet && set instanceof Saveable))
    {
if(DEBUG_WR_DATA)System.err.println("wrIntSet: punt "+set.getClass().getName());
      BinaryUnknown.write(writer, set, token);
      return;
    }

    // see if domain types and component types match
    boolean matchedTypes = hasMatchedTypes(type, comps);

    final int dim = set.getDimension();

    if (!matchedTypes) {
      if (dataType == DATA_INTEGER_1D_SET) {
        throw new IOException("Components specified for Integer1DSet");
      }

      if (comps.length != dim) {
        throw new IOException("Expected " + dim + " IntegerSet component" +
                              (dim > 1 ? "s" : "") + ", not " + comps.length);
      }
    } else {
      if (lengths == null) {
        throw new IOException("Null " + canonicalClass.getName() +
                              " lengths");
      }

      if (lengths.length != dim) {
        throw new IOException("Expected " + dim + " IntegerSet length" +
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

    final int objLen = computeBytes(matchedTypes, lengths, comps, cs, units,
                                    errors);

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrIntSet: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrIntSet: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrIntSet: " +
                                    (dataType == DATA_INTEGER_1D_SET ?
                                     "DATA_INTEGER_1D_SET" :
                                     (dataType == DATA_INTEGER_2D_SET ?
                                      "DATA_INTEGER_2D_SET" :
                                      (dataType == DATA_INTEGER_3D_SET ?
                                       "DATA_INTEGER_3D_SET" :
                                       (dataType == DATA_INTEGER_ND_SET ?
                                        "DATA_INTEGER_ND_SET" : "DATA_???")))) +
                                    "(" + dataType + ")");
    file.writeByte(dataType);

if(DEBUG_WR_DATA)System.err.println("wrIntSet: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

    if (matchedTypes) {
if(DEBUG_WR_DATA)System.err.println("wrIntSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
      file.writeByte(FLD_LENGTHS);
      BinaryIntegerArray.write(writer, lengths, token);
    } else {
if(DEBUG_WR_DATA)System.err.println("wrIntSet: FLD_INTEGER_SETS (" + FLD_INTEGER_SETS + ")");
      file.writeByte(FLD_INTEGER_SETS);
if(DEBUG_WR_DATA)System.err.println("wrIntSet: set length (" + comps.length + ")");
      file.writeInt(comps.length);
      for (int i = 0; i < comps.length; i++) {
        BinaryGeneric.write(writer, comps[i], token);
      }
    }

    if (csIndex >= 0) {
if(DEBUG_WR_DATA)System.err.println("wrIntSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
      file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_WR_DATA)System.err.println("wrIntSet: coord sys Index (" + csIndex + ")");
      file.writeInt(csIndex);
    }

    if (unitsIndex != null) {
if(DEBUG_WR_DATA)System.err.println("wrIntSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
      file.writeByte(FLD_INDEX_UNITS);
      BinaryIntegerArray.write(writer, unitsIndex, token);
    }

    if (errorsIndex != null) {
if(DEBUG_WR_DATA)System.err.println("wrIntSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
      file.writeByte(FLD_INDEX_ERRORS);
      BinaryIntegerArray.write(writer, errorsIndex, token);
    }

if(DEBUG_WR_DATA)System.err.println("wrIntSet: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
