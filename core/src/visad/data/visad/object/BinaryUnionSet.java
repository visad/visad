/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

import visad.SampledSet;
import visad.SetType;
import visad.UnionSet;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryUnionSet
  implements BinaryObject
{
  public static final int computeBytes(SampledSet[] sets)
  {
    int setsLen = 1 + 4;
    for (int i = 0; i < sets.length; i++) {
      int len = BinaryGeneric.computeBytes(sets[i]);
      if (len < 0) {
        return -1;
      }

      setsLen += len;
    }

    return 1 + 4 + 1 + 4 +
      BinarySampledSet.computeBytes(sets) +
      1;
  }

  public static final UnionSet read(BinaryReader reader)
    throws IOException, VisADException
  {
    BinaryObjectCache cache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdUSet: type index (" + typeIndex + ")");
    SetType st = (SetType )cache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdUSet: type index (" + typeIndex + "=" + st + ")");

    SampledSet[] sets = null;

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_SET_SAMPLES:
if(DEBUG_RD_DATA)System.err.println("rdLinSet: FLD_SET_SAMPLES (" + FLD_SET_SAMPLES + ")");
        sets = BinarySampledSet.readList(reader);
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdLinSet: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown UnionSet directive " +
                              directive);
      }
    }

    if (st == null) {
      throw new IOException("No SetType found for UnionSet");
    }
    if (sets == null) {
      throw new IOException("No sets found for UnionSet");
    }

    return new UnionSet(st, sets);
  }

  public static final void writeDependentData(BinaryWriter writer,
                                              SetType type, SampledSet[] sets,
                                              UnionSet set, Object token)
    throws IOException
  {
    if (!set.getClass().equals(UnionSet.class) &&
        !(set instanceof UnionSet && set instanceof Saveable))
    {
      return;
    }

    Object dependToken;
    if (token == SAVE_DEPEND_BIG) {
      dependToken = token;
    } else {
      dependToken = SAVE_DEPEND;
    }

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrUSet: type (" + type + ")");
    BinarySetType.write(writer, type, set, SAVE_DATA);

    if (sets != null) {
      for (int i = 0; i < sets.length; i++) {
        BinaryGeneric.write(writer, sets[i], dependToken);
      }
    }
  }

  public static final void write(BinaryWriter writer, SetType type,
                                 SampledSet[] sets, UnionSet set,
                                 Object token)
    throws IOException
  {
    writeDependentData(writer, type, sets, set, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    if (!set.getClass().equals(UnionSet.class) &&
        !(set instanceof UnionSet && set instanceof Saveable))
    {
if(DEBUG_WR_DATA)System.err.println("wrUSet: punt "+set.getClass().getName());
      BinaryUnknown.write(writer, set, token);
      return;
    }

    int typeIndex = writer.getTypeCache().getIndex(type);
    if (typeIndex < 0) {
      throw new IOException("SetType " + type + " not cached");
    }

    final int objLen = computeBytes(sets);

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrUSet: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrUSet: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrUSet: DATA_UNION_SET (" + DATA_UNION_SET + ")");
    file.writeByte(DATA_UNION_SET);

if(DEBUG_WR_DATA)System.err.println("wrUSet: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

    BinarySampledSet.writeList(writer, sets, token);

if(DEBUG_WR_DATA)System.err.println("wrUSet: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
