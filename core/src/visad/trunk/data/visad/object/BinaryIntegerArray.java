package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryIntegerArray
  implements BinaryObject
{
  public static final int computeBytes(int[] array)
  {
    return (array == null ? 0 : 4 + (array.length * 4));
  }

  public static final int computeBytes(Object[] array)
  {
    if (array != null) {
      boolean empty = true;
      for (int i = 0; i < array.length; i++) {
        if (array[i] != null) {
          empty = false;
          break;
        }
      }

      if (empty) {
        array = null;
      }
    }

    return (array == null ? 0 : 4 + (array.length * 4));
  }

  public static final int[] read(BinaryReader reader)
    throws IOException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdIntRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad double array length " +
                            len + ")");
    }

    int[] array = new int[len];
    for (int i = 0; i < len; i++) {
      array[i] = file.readInt();
if(DEBUG_RD_DATA_DETAIL)System.err.println("rdIntRA: #" + i +" (" + array[i] + ")");
    }

    return array;
  }

  public static final void write(BinaryWriter writer, int[] array,
                                 Object token)
    throws IOException
  {
    DataOutputStream file = writer.getOutputStream();

if(DEBUG_WR_DATA)System.err.println("wrIntRA: len (" + array.length + ")");
    file.writeInt(array.length);
    for (int i = 0; i < array.length; i++) {
if(DEBUG_WR_DATA_DETAIL)System.err.println("wrIntRA: #" + i + " (" + array[i] + ")");
      file.writeInt(array[i]);
    }
  }
}
