package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryFloatArray
  implements BinaryObject
{
  public static final int computeBytes(float[] array)
  {
    return (array == null ? 0 : 4 + (array.length * 4));
  }

  public static final float[] read(BinaryReader reader)
    throws IOException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdFltRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad float array length " +
                            len + ")");
    }

    float[] array = new float[len];
    for (int i = 0; i < len; i++) {
      array[i] = file.readFloat();
if(DEBUG_RD_DATA_DETAIL)System.err.println("rdFltRA: #" + i +" (" + array[i] + ")");
    }

    return array;
  }

  public static final void write(BinaryWriter writer, float[] array,
                                 Object token)
    throws IOException
  {
    DataOutputStream file = writer.getOutputStream();

if(DEBUG_WR_DATA)System.err.println("wrFltRA: len (" + array.length + ")");
    file.writeInt(array.length);
    for (int i = 0; i < array.length; i++) {
if(DEBUG_WR_DATA_DETAIL)System.err.println("wrFltRA: #" + i + " (" + array[i] + ")");
      file.writeFloat(array[i]);
    }
  }
}
