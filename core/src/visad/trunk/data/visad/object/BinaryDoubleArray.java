package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryDoubleArray
  implements BinaryObject
{
  public static final int computeBytes(double[] array)
  {
    return (array == null ? 0 : 4 + (array.length * 8));
  }

  public static final double[] read(BinaryReader reader)
    throws IOException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdDblRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad double array length " +
                            len + ")");
    }

    double[] array = new double[len];
    for (int i = 0; i < len; i++) {
      array[i] = file.readDouble();
if(DEBUG_RD_DATA_DETAIL)System.err.println("rdDblRA: #" + i +" (" + array[i] + ")");
    }

    return array;
  }

  public static final void write(BinaryWriter writer, double[] array,
                                 Object token)
    throws IOException
  {
    DataOutputStream file = writer.getOutputStream();

if(DEBUG_WR_DATA)System.err.println("wrDblRA: len (" + array.length + ")");
    file.writeInt(array.length);
    for (int i = 0; i < array.length; i++) {
if(DEBUG_WR_DATA_DETAIL)System.err.println("wrDblRA: #" + i + " (" + array[i] + ")");
      file.writeDouble(array[i]);
    }
  }
}
