package visad.data.visad.object;

import java.io.IOException;

import visad.RealVectorType;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryWriter;

public class BinaryRealVectorType
  implements BinaryObject
{
  public static final int write(BinaryWriter writer, RealVectorType rvt,
                                Object token)
    throws IOException
  {
    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(rvt);
    if (index < 0) {
      index = cache.add(rvt);
      if (index < 0) {
        throw new IOException("Couldn't cache RealVectorType " + rvt);
      }

if(DEBUG_WR_MATH)System.err.println("wrRlVeTy: serialized RealVectorType (" + rvt.getClass().getName() + ")");
      BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, rvt, token);
    }

    return index;
  }
}
