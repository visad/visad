package visad.data.visad.object;

import java.io.IOException;

import visad.RealType;
import visad.ScalarType;
import visad.TextType;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryWriter;

public class BinaryScalarType
  implements BinaryObject
{
  public static final int computeBytes(ScalarType st)
  {
    if (st instanceof RealType) {
      return BinaryRealType.computeBytes((RealType )st);
    } else if (st instanceof TextType) {
      return BinaryTextType.computeBytes((TextType )st);
    }

    return BinarySerializedObject.computeBytes(st);
  }

  public static final int write(BinaryWriter writer, ScalarType st,
                                Object token)
    throws IOException
  {
    if (st instanceof RealType) {
      return BinaryRealType.write(writer, (RealType )st, token);
    } else if (st instanceof TextType) {
      return BinaryTextType.write(writer, (TextType )st, token);
    }

    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(st);
    if (index < 0) {
      index = cache.add(st);
      if (index < 0) {
        throw new IOException("Couldn't cache ScalarType " + st);
      }

if(DEBUG_WR_MATH)System.err.println("wrScTy: serialized ScalarType (" + st.getClass().getName() + ")");
      BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, st, token);
    }

    return index;
  }
}
