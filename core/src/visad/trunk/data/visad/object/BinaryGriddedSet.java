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
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import visad.CoordinateSystem;
import visad.ErrorEstimate;
import visad.GriddedSet;
import visad.Gridded1DSet;
import visad.Gridded2DSet;
import visad.Gridded3DSet;
import visad.SetType;
import visad.Unit;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryGriddedSet
  implements BinaryObject
{
  public static final int computeBytes(float[][] samples, int[] lengths,
                                       CoordinateSystem cs, Unit[] units,
                                       ErrorEstimate[] errors)
  {
    final int unitsLen = BinaryUnit.computeBytes(units);
    final int errorsLen = BinaryErrorEstimate.computeBytes(errors);
    return 1 + 4 + 1 + 4 +
      1 + BinaryFloatMatrix.computeBytes(samples) +
      1 + BinaryIntegerArray.computeBytes(lengths) +
      (cs == null ? 0 : 5) +
      (unitsLen == 0 ? 0 : unitsLen + 1) +
      (errorsLen == 0 ? 0 : errorsLen + 1) +
      1;
  }

  public static final GriddedSet read(BinaryReader reader, byte dataType)
    throws IOException, VisADException
  {
    BinaryObjectCache cSysCache = reader.getCoordinateSystemCache();
    BinaryObjectCache typeCache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdGrSet: type index (" + typeIndex + ")");
    SetType st = (SetType )typeCache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdGrSet: type index (" + typeIndex + "=" + st + ")");

    float[][] samples = null;
    int[] lengths = null;
    CoordinateSystem cs = null;
    Unit[] units = null;
    ErrorEstimate[] errs = null;

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_FLOAT_SAMPLES:
if(DEBUG_RD_DATA)System.err.println("rdGrSet: FLD_FLOAT_SAMPLES (" + FLD_FLOAT_SAMPLES + ")");
        samples = BinaryFloatMatrix.read(reader);
        break;
      case FLD_LENGTHS:
if(DEBUG_RD_DATA)System.err.println("rdGrSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
        lengths = BinaryIntegerArray.read(reader);
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_RD_DATA)System.err.println("rdGrSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        final int index = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_CSYS)System.err.println("rdGrSet: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_RD_DATA&&!DEBUG_RD_CSYS)System.err.println("rdGrSet: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_RD_DATA)System.err.println("rdGrSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = BinaryUnit.readList(reader);
        break;
      case FLD_INDEX_ERRORS:
if(DEBUG_RD_DATA)System.err.println("rdGrSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        errs = BinaryErrorEstimate.readList(reader);
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdGrSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown GriddedSet directive " +
                              directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for GriddedSet");
    }
    if (lengths == null) {
      throw new IOException("No lengths found for GriddedSet");
    }

    int dim;
    switch (dataType) {
    case DATA_GRIDDED_1D_SET:
      dim = 1;
      break;
    case DATA_GRIDDED_2D_SET:
      dim = 2;
      break;
    case DATA_GRIDDED_3D_SET:
      dim = 3;
      break;
    case DATA_GRIDDED_SET:
      dim = -1;
      break;
    default:
      throw new IOException("Unknown GriddedSet type " + dataType);
    }

    if (samples != null && dim > 0 && samples.length != dim) {
      throw new VisADException("Expected " + dim +
                               "D sample array, not " +
                               samples.length + "D");
    }

    switch (dataType) {
    case DATA_GRIDDED_1D_SET:
      return new Gridded1DSet(st, samples, lengths[0], cs, units, errs);
    case DATA_GRIDDED_2D_SET:
      if (lengths.length == 1) {
        return new Gridded2DSet(st, samples, lengths[0], cs, units, errs);
      } else {
        return new Gridded2DSet(st, samples, lengths[0], lengths[1],
                                cs, units, errs);
      }
    case DATA_GRIDDED_3D_SET:
      if (lengths.length == 1) {
        return new Gridded3DSet(st, samples, lengths[0], cs, units, errs);
      } else if (lengths.length == 2) {
        return new Gridded3DSet(st, samples, lengths[0], lengths[1],
                                cs, units, errs);
      } else {
        return new Gridded3DSet(st, samples,
                                lengths[0], lengths[1], lengths[2],
                                cs, units, errs);
      }
    case DATA_GRIDDED_SET:
      return new GriddedSet(st, samples, lengths, cs, units, errs);
    default:
      throw new IOException("Unknown GriddedSet type " + dataType);
    }
  }

  public static final void writeDependentData(BinaryWriter writer,
                                              SetType type,
                                              CoordinateSystem cs,
                                              Unit[] units,
                                              ErrorEstimate[] errors,
                                              GriddedSet set,
                                              Class canonicalClass,
                                              Object token)
    throws IOException
  {
    if (!set.getClass().equals(canonicalClass) &&
        !(set instanceof GriddedSet && set instanceof Saveable))
    {
      return;
    }

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrGrSet: type (" + type + ")");
    BinarySetType.write(writer, type, set, SAVE_DATA);

    if (cs != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_CSYS)System.err.println("wrGrSet: coordSys (" + cs + ")");
      BinaryCoordinateSystem.write(writer, cs, SAVE_DATA);
    }

    if (units != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_UNIT){
  System.err.println("wrGrSet: List of " + units.length + " Units");
  for(int x=0;x<units.length;x++){
    System.err.println("wrGrSet:    #"+x+": "+units[x]);
  }
}
      BinaryUnit.writeList(writer, units, SAVE_DATA);
    }

    if (errors != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_ERRE){
  System.err.println("wrGrSet: List of " + errors.length + " ErrorEstimates");
  for(int x=0;x<errors.length;x++){
    System.err.println("wrGrSet:    #"+x+": "+errors[x]);
  }
}
      BinaryErrorEstimate.writeList(writer, errors, SAVE_DATA);
    }
  }

  public static final void write(BinaryWriter writer, SetType type,
                                 float[][] samples, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, GriddedSet set,
                                 Class canonicalClass, byte dataType,
                                 Object token)
    throws IOException
  {
    writeDependentData(writer, type, cs, units, errors, set,
                       canonicalClass, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    if (!set.getClass().equals(canonicalClass) &&
        !(set instanceof GriddedSet && set instanceof Saveable))
    {
if(DEBUG_WR_DATA)System.err.println("wrGrSet: punt "+set.getClass().getName());
      BinaryUnknown.write(writer, set, token);
      return;
    }

    if (lengths == null) {
      throw new IOException("Null " + canonicalClass.getName() + " lengths");
    }

    final int validLen;
    switch (dataType) {
    case DATA_GRIDDED_1D_SET:
      validLen = 1;
      break;
    case DATA_GRIDDED_2D_SET:
      validLen = 2;
      break;
    case DATA_GRIDDED_3D_SET:
      validLen = 3;
      break;
    case DATA_GRIDDED_SET:
      validLen = -1;
      break;
    default:
      throw new IOException("Invalid type " + dataType);
    }

    if (samples != null && validLen > 0 && samples.length != validLen) {
      throw new IOException("Expected " + validLen + " sample list" +
                            (validLen > 1 ? "s" : "") + ", not " +
                            samples.length);
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

    final int objLen = computeBytes(samples, lengths, cs, units, errors);

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrGrSet: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrGrSet: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrGrSet: " +
                                    (dataType == DATA_GRIDDED_SET ?
                                     "DATA_GRIDDED" :
                                     (dataType == DATA_GRIDDED_1D_SET ?
                                      "DATA_GRIDDED_1D" :
                                      (dataType == DATA_GRIDDED_2D_SET ?
                                       "DATA_GRIDDED_2D" :
                                       (dataType == DATA_GRIDDED_3D_SET ?
                                        "DATA_GRIDDED_3D" : "DATA_???")))) +
                                    "(" + dataType + ")");
    file.writeByte(dataType);

if(DEBUG_WR_DATA)System.err.println("wrGrSet: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

if(DEBUG_WR_DATA)System.err.println("wrGrSet: FLD_FLOAT_SAMPLES (" + FLD_FLOAT_SAMPLES + ")");
    file.writeByte(FLD_FLOAT_SAMPLES);
    BinaryFloatMatrix.write(writer, samples, token);

if(DEBUG_WR_DATA)System.err.println("wrGrSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
    file.writeByte(FLD_LENGTHS);
    BinaryIntegerArray.write(writer, lengths, token);

    if (csIndex >= 0) {
if(DEBUG_WR_DATA)System.err.println("wrGrSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
      file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_WR_DATA)System.err.println("wrGrSet: coord sys Index (" + csIndex + ")");
      file.writeInt(csIndex);
    }

    if (unitsIndex != null) {
if(DEBUG_WR_DATA)System.err.println("wrGrSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
      file.writeByte(FLD_INDEX_UNITS);
      BinaryIntegerArray.write(writer, unitsIndex, token);
    }

    if (errorsIndex != null) {
if(DEBUG_WR_DATA)System.err.println("wrGrSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
      file.writeByte(FLD_INDEX_ERRORS);
      BinaryIntegerArray.write(writer, errorsIndex, token);
    }

if(DEBUG_WR_DATA)System.err.println("wrGrSet: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
