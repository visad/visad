package visad.data.visad.object;

import java.io.IOException;

import visad.DisplayTupleType;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryWriter;

public class BinaryDisplayTupleType
  implements BinaryObject
{
  public static final int write(BinaryWriter writer, DisplayTupleType dtt,
                                Object token)
    throws IOException
  {
    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(dtt);
    if (index < 0) {
      index = cache.add(dtt);
      if (index < 0) {
        throw new IOException("Couldn't cache DisplayTupleType " + dtt);
      }

if(DEBUG_WR_MATH)System.err.println("wrDpyTuTy: serialized DisplayTupleType");
      BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, dtt, token);
    }

    return index;
  }
}
