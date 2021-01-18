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

import visad.Data;
import visad.Tuple;
import visad.TupleType;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryTuple
  implements BinaryObject
{
  public static final int computeBytes(Data[] components)
  {
    final int compsLen;
    if (components == null) {
      compsLen = 0;
    } else {
      compsLen = 1 + BinaryDataArray.computeBytes(components);
    }

    return 1 + 4 + 1 + 4 +
      compsLen +
      1;
  }

  public static final Tuple read(BinaryReader reader)
    throws IOException, VisADException
  {
    BinaryObjectCache cache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdTpl: type index (" + typeIndex + ")");
    TupleType tt = (TupleType )cache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdTpl: type index (" + typeIndex + "=" + tt + ")");

    Data[] components = null;

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_DATA_SAMPLES:
if(DEBUG_RD_DATA)System.err.println("rdTpl: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
        components = BinaryDataArray.read(reader);
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdTpl: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown Tuple directive " + directive);
      }
    }

    return new Tuple(tt, components);
  }

  private static final void writeDependentData(BinaryWriter writer,
                                               TupleType type,
                                               Data[] components, Tuple t,
                                               Object token)
    throws IOException
  {
    if (!t.getClass().equals(Tuple.class) &&
        !(t instanceof Tuple && t instanceof Saveable))
    {
      return;
    }

    Object dependToken;
    if (token == SAVE_DEPEND_BIG) {
      dependToken = token;
    } else {
      dependToken = SAVE_DEPEND;
    }

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrTpl: type (" + type + ")");
    BinaryTupleType.write(writer, type, SAVE_DATA);


    if (components != null) {
      BinaryDataArray.write(writer, components, dependToken);
    }
  }

  public static final void write(BinaryWriter writer, TupleType type,
                                 Data[] components, Tuple t, Object token)
    throws IOException
  {
    writeDependentData(writer, type, components, t, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    if (!t.getClass().equals(Tuple.class) &&
        !(t instanceof Tuple && t instanceof Saveable))
    {
if(DEBUG_WR_DATA)System.err.println("wrTup: punt "+t.getClass().getName());
      BinaryUnknown.write(writer, t, token);
      return;
    }

    int typeIndex = writer.getTypeCache().getIndex(type);
    if (typeIndex < 0) {
      throw new IOException("TupleType " + type + " not cached");
    }

    final int objLen = computeBytes(components);

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrTup: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrTup: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrTup: DATA_TUPLE (" + DATA_TUPLE + ")");
    file.writeByte(DATA_TUPLE);

if(DEBUG_WR_DATA)System.err.println("wrTup: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

    if (components != null) {
if(DEBUG_WR_DATA)System.err.println("wrTup: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
      file.writeByte(FLD_DATA_SAMPLES);
      BinaryDataArray.write(writer, components, token);
    }

if(DEBUG_WR_DATA)System.err.println("wrTup: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
