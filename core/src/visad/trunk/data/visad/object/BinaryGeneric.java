package visad.data.visad.object;

import java.io.IOException;

import visad.DataImpl;
import visad.VisADException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinarySizer;
import visad.data.visad.BinaryWriter;

public class BinaryGeneric
  implements BinaryObject
{
  public static final int computeBytes(DataImpl data)
  {
    BinarySizer sizer = new BinarySizer();
    try {
      sizer.process(data, null);
    } catch (VisADException ve) {
      return -1;
    }
    return sizer.getSize();
  }

  public static final DataImpl read(BinaryReader reader)
    throws IOException
  {
    try {
      return reader.getData();
    } catch (VisADException ve) {
      throw new IOException("Couldn't read file: " + ve.getMessage());
    }
  }

  public static final void write(BinaryWriter writer, DataImpl data,
                                 Object token)
    throws IOException
  {
    try {
      writer.process(data, token);
    } catch (VisADException ve) {
      throw new IOException("Couldn't write " + data.getClass().getName() +
                            ": " + ve.getMessage());
    }
  }
}
