package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryFloatMatrix
  implements BinaryObject
{
  public static final int computeBytes(float[][] matrix)
  {
    if (matrix == null) {
      return 4;
    }

    int len = 4;
    for (int i = 0; i < matrix.length; i++) {
      len += 4 + (matrix[i].length * 4);
    }

    return len;
  }

  public static final float[][] read(BinaryReader reader)
    throws IOException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdFltMtx: len (" + len + ")");
    if (len < 0) {
      return null;
    }

    float[][] matrix = new float[len][];
    for (int i = 0; i < len; i++) {
      final int len2 = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdFltMtx: #" + i + " len (" + len2 + ")");
      matrix[i] = new float[len2];
      for (int j = 0; j < len2; j++) {
        matrix[i][j] = file.readFloat();
if(DEBUG_RD_DATA_DETAIL)System.err.println("rdFltMtx: #" + i + "," + j +" (" + matrix[i][j] + ")");
      }
    }

    return matrix;
  }

  public static final void write(BinaryWriter writer, float[][] matrix,
                                 Object token)
    throws IOException
  {
    DataOutputStream file = writer.getOutputStream();

    if (matrix == null) {
if(DEBUG_WR_DATA)System.err.println("wrFltMtx: null (" + -1 + ")");
      file.writeInt(-1);
    } else {
if(DEBUG_WR_DATA)System.err.println("wrFltMtx: row len (" + matrix.length + ")");
      file.writeInt(matrix.length);
      for (int i = 0; i < matrix.length; i++) {
        final int len = matrix[i].length;
if(DEBUG_WR_DATA)System.err.println("wrFltMtx: #" + i + " len (" + matrix[i].length + ")");
        file.writeInt(len);
        for (int j = 0; j < len; j++) {
if(DEBUG_WR_DATA_DETAIL)System.err.println("wrFltMtx: #" + i + "," + j + " (" + matrix[i][j] + ")");
          file.writeFloat(matrix[i][j]);
        }
      }
    }
  }
}
