package visad.data.visad.object;

import java.io.IOException;

import visad.DataImpl;

import visad.data.visad.BinaryWriter;

public class BinaryUnknown
  implements BinaryObject
{
  public static final int computeBytes(DataImpl data)
  {
    return BinarySerializedObject.computeBytes(data);
  }

  public static final void write(BinaryWriter writer, DataImpl data,
                                 Object token)
    throws IOException
  {
    BinarySerializedObject.write(writer, OBJ_DATA_SERIAL, data, token);
  }
}
