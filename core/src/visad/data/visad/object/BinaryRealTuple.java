/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryRealTuple
  implements BinaryObject
{
  public static final int computeBytes(Real[] components, CoordinateSystem cs,
                                       boolean trivialTuple)
  {
    int compsLen;
    if (components == null) {
      compsLen = 0;
    } else {
      if (trivialTuple) {
        compsLen = 1 + 4 + (components.length * 8);
      } else {
        compsLen = 1 + 4;
        for (int i = 0; i < components.length; i++) {
          compsLen += BinaryReal.computeBytes(components[i].getUnit(),
                                              components[i].getError());
        }
      }
    }

    return 1 + 4 + 1 + 4 +
      compsLen +
      (cs == null ? 0 : 5) +
      1;
  }

  public static final boolean isTrivialTuple(RealTupleType type,
                                             Real[] components)
  {
    if (components == null) {
      return true;
    }

    for (int i = 0; i < components.length; i++) {
      if (components[i] != null) {
        MathType comp;
        try {
          comp = type.getComponent(i);
        } catch (VisADException ve) {
          return false;
        }

        if (!comp.equals(components[i].getType()) ||
            components[i].getUnit() != null ||
            components[i].getError() != null)
        {
          return false;
        }
      }
    }

    return true;
  }

  public static final RealTuple read(BinaryReader reader)
    throws IOException, VisADException
  {
    BinaryObjectCache cSysCache = reader.getCoordinateSystemCache();
    BinaryObjectCache typeCache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdRlTpl: type index (" + typeIndex + ")");
    RealTupleType rtt = (RealTupleType )typeCache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdRlTpl: type index (" + typeIndex + "=" + rtt + ")");

    Real[] components = null;
    double[] values = null;
    CoordinateSystem cs = null;

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
if(DEBUG_RD_DATA)System.err.println("rdRlTpl: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        final int index = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_CSYS)System.err.println("rdRlTpl: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_RD_DATA&&!DEBUG_RD_CSYS)System.err.println("rdRlTpl: cSys index (" + index + "=" + cs + ")");
        break;
      case FLD_REAL_SAMPLES:
if(DEBUG_RD_DATA)System.err.println("rdRlTpl: FLD_REAL_SAMPLES (" + FLD_REAL_SAMPLES + ")");
        components = readRealArray(reader);
        break;
      case FLD_TRIVIAL_SAMPLES:
if(DEBUG_RD_DATA)System.err.println("rdRlTpl: FLD_TRIVIAL_SAMPLES (" + FLD_TRIVIAL_SAMPLES + ")");
        values = BinaryDoubleArray.read(reader);
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdRlTpl: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown RealTuple directive " + directive);
      }
    }

    if (components != null && values != null) {
      throw new IOException("Found both RealTuple Real[] and double[] values");
    }

    if (values != null) {
      if (cs == null) {
        return new RealTuple(rtt, values);
      }

      // build a Real[] array from values,
      components = new Real[values.length];
      for (int i = 0; i < values.length; i++) {
        components[i] = new Real((RealType )rtt.getComponent(i), values[i],
                                 null, null);
      }
    }

    return new RealTuple(rtt, components, cs);
  }

  private static final Real[] readRealArray(BinaryReader reader)
    throws IOException, VisADException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdRlRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad Real array length " +
                            len + ")");
    }

long t = (DEBUG_RD_TIME ? System.currentTimeMillis() : 0);
    Real[] array = new Real[len];
    for (int i = 0; i < len; i++) {
      array[i] = (Real )BinaryGeneric.read(reader);
    }
if(DEBUG_RD_TIME)System.err.println("rdRlRA: "+len+" arrays "+(System.currentTimeMillis()-t));

    return array;
  }

  public static final void writeDependentData(BinaryWriter writer,
                                              RealTupleType type,
                                              Real[] components,
                                              CoordinateSystem cs,
                                              RealTuple rt, Object token)
    throws IOException
  {
    if (!rt.getClass().equals(RealTuple.class) &&
        !(rt instanceof RealTuple && rt instanceof Saveable))
    {
      return;
    }

    Object dependToken;
    if (token == SAVE_DEPEND_BIG) {
      dependToken = token;
    } else {
      dependToken = SAVE_DEPEND;
    }

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrRlTpl: MathType (" + type + ")");
    BinaryRealTupleType.write(writer, type, SAVE_DATA);

    if (cs != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_CSYS)System.err.println("wrRlTpl: coordSys (" + cs + ")");
      BinaryCoordinateSystem.write(writer, cs, SAVE_DATA);
    }

    if (components != null) {
      for (int i = 0; i < components.length; i++) {
        BinaryGeneric.write(writer, components[i], dependToken);
      }
    }
  }

  public static final void write(BinaryWriter writer, RealTupleType type,
                                 Real[] components, CoordinateSystem cs,
                                 RealTuple rt, Object token)
    throws IOException
  {
    writeDependentData(writer, type, components, cs, rt, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    if (!rt.getClass().equals(RealTuple.class) &&
        !(rt instanceof RealTuple && rt instanceof Saveable))
    {
if(DEBUG_WR_DATA)System.err.println("wrRlTpl: punt "+rt.getClass().getName());
      BinaryUnknown.write(writer, rt, token);
      return;
    }

    int typeIndex = writer.getTypeCache().getIndex(type);
    if (typeIndex < 0) {
      throw new IOException("RealTupleType " + type + " not cached");
    }

    int csIndex = -1;
    if (cs != null) {
      csIndex = writer.getCoordinateSystemCache().getIndex(cs);
      if (csIndex < 0) {
        throw new IOException("CoordinateSystem " + cs + " not cached");
      }
    }

    boolean trivialTuple = isTrivialTuple(type, components);

    final int objLen = computeBytes(components, cs, trivialTuple);

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrRlTpl: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrRlTpl: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrRlTpl: DATA_REAL_TUPLE (" + DATA_REAL_TUPLE + ")");
    file.writeByte(DATA_REAL_TUPLE);

if(DEBUG_WR_DATA)System.err.println("wrRlTpl: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

    if (components != null) {
      if (trivialTuple) {
if(DEBUG_WR_DATA)System.err.println("wrRlTpl: FLD_TRIVIAL_SAMPLES (" + FLD_REAL_SAMPLES + ")");
        file.writeByte(FLD_TRIVIAL_SAMPLES);
if(DEBUG_WR_DATA)System.err.println("wrRlTpl: len (" + components.length + ")");
        file.writeInt(components.length);
        for (int i = 0; i < components.length; i++) {
          file.writeDouble(components[i].getValue());
        }
      } else {
if(DEBUG_WR_DATA)System.err.println("wrRlTpl: FLD_REAL_SAMPLES (" + FLD_REAL_SAMPLES + ")");
        file.writeByte(FLD_REAL_SAMPLES);
if(DEBUG_WR_DATA)System.err.println("wrRlTpl: len (" + components.length + ")");
        file.writeInt(components.length);
        for (int i = 0; i < components.length; i++) {
          BinaryReal.write(writer, (RealType )components[i].getType(),
                           components[i].getValue(), components[i].getUnit(),
                           components[i].getError(), components[i], token);
        }
      }
    }

    if (csIndex >= 0) {
if(DEBUG_WR_DATA)System.err.println("wrRlTpl: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
      file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_WR_DATA)System.err.println("wrRlTpl: coord sys Index (" + csIndex + ")");
      file.writeInt(csIndex);
    }

if(DEBUG_WR_DATA)System.err.println("wrRlTpl: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
