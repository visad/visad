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

import visad.data.visad.BinaryWriter;

public class BinarySerializedObject
  implements BinaryObject
{
  public static final int computeBytes(Object obj)
  {
    byte[] bytes;
    try {
      bytes = getBytes(obj);
    } catch (IOException ioe) {
      return 0;
    }

    return 5 + bytes.length + 1;
  }

  public static byte[] getBytes(Object obj)
    throws IOException
  {
    java.io.ByteArrayOutputStream outBytes;
    outBytes = new java.io.ByteArrayOutputStream();

    java.io.ObjectOutputStream outStream;
    outStream = new java.io.ObjectOutputStream(outBytes);

    outStream.writeObject(obj);
    outStream.flush();
    outStream.close();

    return outBytes.toByteArray();
  }

  public static final Object read(DataInput file)
    throws IOException
  {
    final int len = file.readInt();
    return read(file, len);
  }

  public static final Object read(DataInput file, int len)
    throws IOException
  {
    if (len <= 1) {
      throw new IOException("Corrupted file (bad serialized object length)");
    }

    byte[] bytes = new byte[len - 1];
    file.readFully(bytes);

    // make sure we see the FLD_END marker byte
    final byte endByte = file.readByte();
    if (endByte != FLD_END) {
      throw new IOException("Corrupted file (no serialized object end-marker)");
    }

    java.io.ByteArrayInputStream inBytes;
    inBytes = new java.io.ByteArrayInputStream(bytes);

    java.io.ObjectInputStream inStream;
    inStream = new java.io.ObjectInputStream(inBytes);

    Object obj;
    try {
      obj = inStream.readObject();
    } catch (ClassNotFoundException cnfe) {
      throw new IOException("Couldn't read serialized object: " +
                            cnfe.getMessage());
    }

    inStream.close();

    return obj;
  }

  public static final void write(BinaryWriter writer, byte objType,
                                 Object obj, Object token)
    throws IOException
  {
    byte[] bytes = getBytes(obj);

    DataOutput file = writer.getOutput();

    file.writeByte(objType);
    file.writeInt(bytes.length + 1);
    file.write(bytes);
    file.writeByte(FLD_END);
  }
}
