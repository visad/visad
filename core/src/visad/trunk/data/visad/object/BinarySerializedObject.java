package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.EOFException;
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

    DataOutputStream file = writer.getOutputStream();

    file.writeByte(objType);
    file.writeInt(bytes.length + 1);
    file.write(bytes);
    file.writeByte(FLD_END);
  }
}
