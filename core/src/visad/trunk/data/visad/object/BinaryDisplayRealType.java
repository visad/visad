package visad.data.visad.object;

import java.io.DataOutputStream;
import java.io.IOException;

import visad.DisplayRealType;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryWriter;

public class BinaryDisplayRealType
  implements BinaryObject
{
  public static final int computeBytes(DisplayRealType drt)
  {
    return BinarySerializedObject.computeBytes(drt);
  }

  public static final int write(BinaryWriter writer, DisplayRealType drt,
                                Object token)
    throws IOException
  {
    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(drt);
    if (index < 0) {
      index = cache.add(drt);
      if (index < 0) {
        throw new IOException("Couldn't cache DisplayRealType " + drt);
      }

if(DEBUG_WR_MATH)System.err.println("wrDpyRTy: serialized DisplayRealType");
      BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, drt, token);
    }

    return index;
  }
}
