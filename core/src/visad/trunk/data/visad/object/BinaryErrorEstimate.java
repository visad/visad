/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

import visad.ErrorEstimate;
import visad.Unit;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryErrorEstimate
  implements BinaryObject
{
  public static final int computeBytes(int uIndex)
  {
    return 28 + (uIndex < 0 ? 0 : 5) + 1;
  }

  public static final int computeBytes(ErrorEstimate[] array)
  {
    return BinaryIntegerArray.computeBytes(array);
  }

  public static final int[] lookupList(BinaryObjectCache cache,
                                       ErrorEstimate[] errors)
  {
    // make sure there's something to write
    boolean empty = true;
    for (int i = 0; i < errors.length; i++) {
      if (errors[i] != null) {
        empty = false;
        break;
      }
    }
    if (empty) {
      return null;
    }

    int[] indices = new int[errors.length];

    for (int i = 0; i < errors.length; i++) {
      if (errors[i] == null) {
        indices[i] = -1;
      } else {
        indices[i] = cache.getIndex(errors[i]);
      }
    }

    return indices;
  }

  public static final ErrorEstimate read(BinaryReader reader)
    throws IOException, VisADException
  {
    BinaryObjectCache errorCache = reader.getErrorEstimateCache();
    BinaryObjectCache unitCache = reader.getUnitCache();
    DataInput file = reader.getInput();

    final int objLen = file.readInt();
if(DEBUG_RD_ERRE)System.err.println("cchErrEst: objLen (" + objLen + ")");
    // read the index number for this ErrorEstimate
    final int index = file.readInt();
if(DEBUG_RD_ERRE)System.err.println("cchErrEst: index (" + index + ")");

    // read the ErrorEstimate data
    final double errValue = file.readDouble();
if(DEBUG_RD_ERRE)System.err.println("cchErrEst: value (" + errValue + ")");
    final double mean = file.readDouble();
if(DEBUG_RD_ERRE)System.err.println("cchErrEst: mean (" + mean + ")");
    final long number = file.readLong();
if(DEBUG_RD_ERRE)System.err.println("cchErrEst: number (" + number + ")");

    Unit u = null;

    boolean reading = true;
    while (reading) {
      final byte directive = file.readByte();

      switch (directive) {
      case FLD_INDEX_UNIT:
        final int uIndex = file.readInt();
if(DEBUG_RD_ERRE&&DEBUG_RD_UNIT)System.err.println("cchErrEst: unit index ("+uIndex+")");
        u = (Unit )unitCache.get(uIndex);
if(DEBUG_RD_ERRE&&!DEBUG_RD_UNIT)System.err.println("cchErrEst: unit index ("+uIndex+"="+u+")");
        break;
      case FLD_END:
if(DEBUG_RD_ERRE)System.err.println("cchErrEst: FLD_END ("+FLD_END+")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown ErrorEstimate directive " + directive);
      }
    }

    ErrorEstimate err = new ErrorEstimate(errValue, mean, number, u);

    errorCache.add(index, err);

    return err;
  }

  public static final ErrorEstimate[] readList(BinaryReader reader)
    throws IOException
  {
    BinaryObjectCache cache = reader.getErrorEstimateCache();
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_ERRE)System.err.println("rdErrEstS: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file" +
                            " (bad ErrorEstimate array length " + len + ")");
    }

    ErrorEstimate[] errs = new ErrorEstimate[len];
    for (int i = 0; i < len; i++) {
      final int index = file.readInt();
if(DEBUG_RD_ERRE)System.err.println("rdErrEstS:    #"+i+" index ("+index+")");

      if (index < 0) {
        errs[i] = null;
      } else {
        errs[i] = (ErrorEstimate )cache.get(index);
      }
if(DEBUG_RD_ERRE)System.err.println("rdErrEstS:    === #"+i+" ErrorEstimate ("+errs[i]+")");
    }

    return errs;
  }

  public static final int write(BinaryWriter writer, ErrorEstimate error,
                                Object token)
    throws IOException
  {
    BinaryObjectCache cache = writer.getErrorEstimateCache();

    int index = cache.getIndex(error);
    if (index >= 0) {
      return index;
    }

    // cache the ErrorEstimate so we can find its index number
    index = cache.add(error);
    if (index < 0) {
      throw new IOException("Couldn't cache ErrorEstimate " + error);
    }

    double errValue = error.getErrorValue();
    double mean = error.getMean();
    long number = error.getNumberNotMissing();
    Unit unit = error.getUnit();

    int uIndex = -1;
    if (unit != null) {
      uIndex = BinaryUnit.write(writer, unit, token);
    }

    final int objLen = computeBytes(uIndex);

    DataOutput file = writer.getOutput();

if(DEBUG_WR_ERRE)System.err.println("wrErrEst: OBJ_ERROR (" + OBJ_ERROR + ")");
    file.writeByte(OBJ_ERROR);
if(DEBUG_WR_ERRE)System.err.println("wrErrEst: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_ERRE)System.err.println("wrErrEst: index (" + index + ")");
    file.writeInt(index);

if(DEBUG_WR_ERRE)System.err.println("wrErrEst: error value (" + errValue + ")");
    file.writeDouble(errValue);
if(DEBUG_WR_ERRE)System.err.println("wrErrEst: error mean (" + mean + ")");
    file.writeDouble(mean);
if(DEBUG_WR_ERRE)System.err.println("wrErrEst: error number (" + number + ")");
    file.writeLong(number);

    if (uIndex >= 0) {
if(DEBUG_WR_ERRE)System.err.println("wrErrEst: FLD_INDEX_UNIT (" + FLD_INDEX_UNIT + ")");
      file.writeByte(FLD_INDEX_UNIT);
if(DEBUG_WR_ERRE)System.err.println("wrErrEst: unit index (" + uIndex + ")");
      file.writeInt(uIndex);
    }

if(DEBUG_WR_ERRE)System.err.println("wrErrEst: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);

    return index;
  }

  public static final int[] writeList(BinaryWriter writer,
                                      ErrorEstimate[] errors, Object token)
    throws IOException
  {
    // make sure there's something to write
    boolean empty = true;
    for (int i = 0; i < errors.length; i++) {
      if (errors[i] != null) {
        empty = false;
        break;
      }
    }
    if (empty) {
      return null;
    }

    int[] indices = new int[errors.length];

    for (int i = 0; i < errors.length; i++) {
      if (errors[i] == null) {
        indices[i] = -1;
      } else {
        indices[i] = BinaryErrorEstimate.write(writer, errors[i], token);
      }
    }

    return indices;
  }
}
