package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryString
  implements BinaryObject
{
  public static final int computeBytes(String str)
  {
    return 4 + (str == null ? 0 : str.getBytes().length);
  }

  public static final String read(BinaryReader reader)
    throws IOException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_STR)System.err.println("rdStr: len (" + len + ")");
    if (len < 0) {
      return null;
    } else if (len == 0) {
      return "";
    }

    byte[] buf = new byte[len];
    file.readFully(buf);
if(DEBUG_RD_STR)System.err.println("rdStr: str (" + new String(buf) + ")");

    return new String(buf);
  }

  public static final void write(BinaryWriter writer, String str,
                                 Object token)
    throws IOException
  {
    DataOutputStream file = writer.getOutputStream();

    if (str == null) {
      file.writeInt(-1);
    } else {
      byte[] bytes = str.getBytes();

if(DEBUG_WR_DATA)System.err.println("wrStr: num bytes (" + bytes.length + ")");
      file.writeInt(bytes.length);
      if (bytes.length > 0) {
if(DEBUG_WR_DATA)System.err.println("wrStr: str (" + str + ")");
        file.write(bytes);
      }
    }
  }
}
