package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import visad.MathType;
import visad.RealTupleType;
import visad.SetType;
import visad.Set;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinarySetType
  implements BinaryObject
{
  public static final int computeBytes(SetType st) { return 10; }

  public static final SetType read(BinaryReader reader, int index)
    throws IOException, VisADException
  {
    BinaryObjectCache cache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int dIndex = file.readInt();
if(DEBUG_RD_MATH&&DEBUG_RD_MATH)System.err.println("rdSetTy: domain index (" + dIndex + ")");
    MathType dom = (MathType )cache.get(dIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdSetTy: domain index (" + dIndex + "=" + dom + ")");

    final byte endByte = file.readByte();
if(DEBUG_RD_MATH)System.err.println("rdSetTy: read " + (endByte == FLD_END ? "FLD_END" : Integer.toString(endByte) + " (wanted FLD_END)"));
    if (endByte != FLD_END) {
      throw new IOException("Corrupted file (no SetType end-marker)");
    }

    SetType st = new SetType(dom);

    cache.add(index, st);

    return st;
  }

  public static final int write(BinaryWriter writer, SetType st, Set set,
                                Object token)
    throws IOException
  {
    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(st);
    if (index < 0) {
      index = cache.add(st);
      if (index < 0) {
        throw new IOException("Couldn't cache SetType " + st);
      }

      if (!st.getClass().equals(SetType.class)) {
if(DEBUG_WR_MATH)System.err.println("wrSetTy: serialized SetType (" + st.getClass().getName() + ")");
        BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, st, token);
        return index;
      }

      int dIndex;
      MathType domain = st.getDomain();

      boolean isRealTupleType = false;
      if (domain instanceof RealTupleType) {
        RealTupleType rtt = (RealTupleType )domain;

        if (rtt.getDimension() == 1 &&
            rtt.getCoordinateSystem() == null &&
            rtt.getDefaultSet() == null)
        {
          // just use the RealType
          try {
            domain = rtt.getComponent(0);
          } catch (VisADException ve) {
            throw new IOException("Couldn't get SetType domain: " +
                                  ve.getMessage());
          }
        } else {
          // must really be a multi-dimensional RealTupleType
          isRealTupleType = true;
        }
      }

      if (isRealTupleType) {
        dIndex = BinaryRealTupleType.write(writer, (RealTupleType )domain,
                                           token);
      } else {
        dIndex = BinaryMathType.write(writer, domain, token);
      }

      final int objLen = computeBytes(st);

      DataOutputStream file = writer.getOutputStream();

if(DEBUG_WR_MATH)System.err.println("wrSetTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_WR_MATH)System.err.println("wrSetTy: objLen (" + objLen + ")");
      file.writeInt(objLen);
if(DEBUG_WR_MATH)System.err.println("wrSetTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_WR_MATH)System.err.println("wrSetTy: MATH_SET (" + MATH_SET + ")");
      file.writeByte(MATH_SET);

if(DEBUG_WR_MATH)System.err.println("wrSetTy: domain index (" + dIndex + ")");
      file.writeInt(dIndex);

if(DEBUG_WR_MATH)System.err.println("wrSetTy: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    }

    return index;
  }
}
