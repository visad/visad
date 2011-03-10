/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import visad.ErrorEstimate;
import visad.Real;
import visad.RealType;
import visad.Unit;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryReal
  implements BinaryObject
{
  public static final int computeBytes(Unit u, ErrorEstimate err)
  {
    return 1 + 4 + 1 + 4 + 8 +
        (u == null ? 0 : 5) +
        (err == null ? 0 : 5) +
        1;
  }

  public static final Real read(BinaryReader reader)
    throws IOException, VisADException
  {
    BinaryObjectCache errorCache = reader.getErrorEstimateCache();
    BinaryObjectCache typeCache = reader.getTypeCache();
    BinaryObjectCache unitCache = reader.getUnitCache();
    DataInput file = reader.getInput();

    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdRl: type index (" + typeIndex + ")");
    RealType rt = (RealType )typeCache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdRl: type index (" + typeIndex + "=" + rt + ")");

    // read the value
    final double value = file.readDouble();
if(DEBUG_RD_DATA)System.err.println("rdRl: value (" + value + ")");

    Unit u = null;
    ErrorEstimate error = null;

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
if(DEBUG_RD_DATA)System.err.println("rdRl: FLD_INDEX_UNIT (" + FLD_INDEX_UNIT + ")");
        final int uIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_UNIT)System.err.println("rdRl: unit index (" + uIndex + ")");
        u = (Unit )unitCache.get(uIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_UNIT)System.err.println("rdRl: unit index (" + uIndex + "=" + u + ")");
        break;
      case FLD_INDEX_ERROR:
if(DEBUG_RD_DATA)System.err.println("rdRl: FLD_INDEX_ERROR (" + FLD_INDEX_ERROR + ")");
        final int eIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_ERRE)System.err.println("rdRl: error index (" + eIndex + ")");
        error = (ErrorEstimate )errorCache.get(eIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_ERRE)System.err.println("rdRl: error index (" + eIndex + "=" + error + ")");
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdRl: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown Real directive " + directive);
      }
    }

    return new Real(rt, value, u, error);
  }

  public static final void writeDependentData(BinaryWriter writer,
                                              RealType type, Unit unit,
                                              ErrorEstimate error, Real real,
                                              Object token)
    throws IOException
  {
if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrRl: MathType (" + type + ")");
    BinaryRealType.write(writer, type, SAVE_DATA);

    if (unit != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_UNIT)System.err.println("wrRl: Unit (" + unit + ")");
      BinaryUnit.write(writer, unit, SAVE_DATA);
    }

    if (error != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_ERRE)System.err.println("wrRl: ErrEst (" + error + ")");
      BinaryErrorEstimate.write(writer, error, SAVE_DATA);
    }
  }

  public static final void write(BinaryWriter writer, RealType type,
                                 double value, Unit unit, ErrorEstimate error,
                                 Real real, Object token)
    throws IOException
  {
    writeDependentData(writer, type, unit, error, real, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    int typeIndex;
if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrRl: MathType (" + type + ")");
    typeIndex = BinaryRealType.write(writer, type, token);

    int uIndex = -1;
    if (unit != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_UNIT)System.err.println("wrRl: Unit (" + unit + ")");
      uIndex = BinaryUnit.write(writer, unit, token);
    }

    int errIndex = -1;
    if (error != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_ERRE)System.err.println("wrRl: ErrEst (" + error + ")");
      errIndex = BinaryErrorEstimate.write(writer, error, token);
    }

    final int objLen = computeBytes(unit, error);

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrRl: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrRl: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrRl: DATA_REAL (" + DATA_REAL + ")");
    file.writeByte(DATA_REAL);

if(DEBUG_WR_DATA)System.err.println("wrRl: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

if(DEBUG_WR_DATA)System.err.println("wrRl: value (" + value + ")");
    file.writeDouble(value);

    if (uIndex >= 0) {
if(DEBUG_WR_DATA)System.err.println("wrRl: FLD_INDEX_UNIT (" + FLD_INDEX_UNIT + ")");
      file.writeByte(FLD_INDEX_UNIT);
if(DEBUG_WR_DATA)System.err.println("wrRl: unit index (" + uIndex + ")");
      file.writeInt(uIndex);
    }

    if (errIndex >= 0) {
if(DEBUG_WR_DATA)System.err.println("wrRl: FLD_INDEX_ERROR (" + FLD_INDEX_ERROR + ")");
      file.writeByte(FLD_INDEX_ERROR);
if(DEBUG_WR_DATA)System.err.println("wrRl: err index (" + errIndex + ")");
      file.writeInt(errIndex);
    }

if(DEBUG_WR_DATA)System.err.println("wrRl: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
