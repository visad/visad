/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

import visad.CoordinateSystem;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryCoordinateSystem
  implements BinaryObject
{
  public static final int computeBytes(CoordinateSystem cSys)
  {
    try {
      return computeBytes(BinarySerializedObject.getBytes(cSys));
    } catch (IOException ioe) {
      return 0;
    }
  }

  private static final int computeBytes(byte[] serialObj)
  {
    return 4 + 1 +
      serialObj.length +
      1;
  }

  public static final int computeBytes(CoordinateSystem[] array)
  {
    return BinaryIntegerArray.computeBytes(array);
  }

  public static final int[] lookupList(BinaryObjectCache cache,
                                       CoordinateSystem[] cSys)
  {
    // make sure there's something to write
    boolean empty = true;
    for (int i = 0; i < cSys.length; i++) {
      if (cSys[i] != null) {
        empty = false;
        break;
      }
    }
    if (empty) {
      return null;
    }

    int[] indices = new int[cSys.length];

    for (int i = 0; i < cSys.length; i++) {
      if (cSys[i] == null) {
        indices[i] = -1;
      } else {
        indices[i] = cache.getIndex(cSys[i]);
      }
    }

    return indices;
  }

  public static final CoordinateSystem read(BinaryReader reader)
    throws IOException
  {
    BinaryObjectCache cache = reader.getCoordinateSystemCache();
    DataInput file = reader.getInput();

    final int objLen = file.readInt();
if(DEBUG_RD_CSYS)System.err.println("cchCS: objLen (" + objLen + ")");
    final int index = file.readInt();
if(DEBUG_RD_CSYS)System.err.println("cchCS: index (" + index + ")");

    final byte cSysSerial = file.readByte();
    if (cSysSerial != FLD_COORDSYS_SERIAL) {
      throw new IOException("Corrupted file (no CoordinateSystem serial marker)");
    }
if(DEBUG_RD_CSYS)System.err.println("cchCS: FLD_COORDSYS_SERIAL (" + FLD_COORDSYS_SERIAL + ")");

    // read the CoordinateSystem data
if(DEBUG_RD_CSYS)System.err.println("cchCS: serialObj (" + (objLen-6) + " bytes)");
    CoordinateSystem cs;
    cs = (CoordinateSystem )BinarySerializedObject.read(file, (objLen-6)+1);

    cache.add(index, cs);

    return cs;
  }

  public static final CoordinateSystem[] readList(BinaryReader reader)
    throws IOException
  {
    BinaryObjectCache cache = reader.getCoordinateSystemCache();
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_CSYS)System.err.println("rdCSysS: len ("+len+")");
    if (len < 1) {
      throw new IOException("Corrupted file" +
                            " (bad CoordinateSystem array length " + len +
                            ")");
    }

    CoordinateSystem[] cSys = new CoordinateSystem[len];
    for (int i = 0; i < len; i++) {
      final int uIndex = file.readInt();
if(DEBUG_RD_CSYS)System.err.println("rdCSysS: cSys index ("+uIndex+")");
      cSys[i] = (CoordinateSystem )cache.get(uIndex);
if(DEBUG_RD_CSYS)System.err.println("rdCSysS: === #"+i+": "+cSys[i]+")");
    }

    return cSys;
  }

  public static final int write(BinaryWriter writer, CoordinateSystem cSys,
                                Object token)
    throws IOException
  {
    BinaryObjectCache cache = writer.getCoordinateSystemCache();

    int index = cache.getIndex(cSys);
    if (index >= 0) {
      return index;
    }

    // cache the CoordinateSystem so we can find its index number
    index = cache.add(cSys);
    if (index < 0) {
      throw new IOException("Couldn't cache CoordinateSystem " + cSys);
    }

    DataOutput file = writer.getOutput();

    byte[] serialObj = BinarySerializedObject.getBytes(cSys);

    // this copies the code from computeBytes()
    final int objLen = computeBytes(serialObj);

if(DEBUG_WR_CSYS)System.err.println("wrCSys: OBJ_COORDSYS (" + OBJ_COORDSYS + ")");
    file.writeByte(OBJ_COORDSYS);
if(DEBUG_WR_CSYS)System.err.println("wrCSys: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_CSYS)System.err.println("wrCSys: index (" + index + ")");
    file.writeInt(index);

if(DEBUG_WR_CSYS)System.err.println("wrCSys: FLD_COORDSYS_SERIAL (" + FLD_COORDSYS_SERIAL + ")");
    file.writeByte(FLD_COORDSYS_SERIAL);
if(DEBUG_WR_CSYS)System.err.println("wrCSys: serialObj (" + serialObj.length + " bytes)");
    file.write(serialObj);

if(DEBUG_WR_CSYS)System.err.println("wrCSys: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);

    return index;
  }

  public static final int[] writeList(BinaryWriter writer,
                                      CoordinateSystem[] cSys, Object token)
    throws IOException
  {
    // make sure there's something to write
    boolean empty = true;
    for (int i = 0; i < cSys.length; i++) {
      if (cSys[i] != null) {
        empty = false;
        break;
      }
    }
    if (empty) {
      return null;
    }

    int[] indices = new int[cSys.length];

    for (int i = 0; i < cSys.length; i++) {
      if (cSys[i] == null) {
        indices[i] = -1;
      } else {
        indices[i] = write(writer, cSys[i], token);
      }
    }

    return indices;
  }
}
