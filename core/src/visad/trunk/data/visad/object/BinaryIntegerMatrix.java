package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

class BinaryIntegerMatrix
  implements BinaryObject
{
  static final int computeBytes(int[][] matrix)
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

  static final int[][] read(DataInput file)
    throws IOException
  {
    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdIntMtx: len (" + len + ")");
    if (len < 0) {
      return null;
    }

    int[][] matrix = new int[len][];
    for (int i = 0; i < len; i++) {
      final int len2 = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdIntMtx: #" + i + " len (" + len2 + ")");
      matrix[i] = new int[len2];
      for (int j = 0; j < len2; j++) {
        matrix[i][j] = file.readInt();
if(DEBUG_RD_DATA_DETAIL)System.err.println("rdIntMtx: #" + i + "," + j +" (" + matrix[i][j] + ")");
      }
    }

    return matrix;
  }

  static final void write(DataOutputStream file, int[][] matrix)
    throws IOException
  {
    if (matrix == null) {
if(DEBUG_WR_DATA)System.err.println("wrIntMtx: null (" + -1 + ")");
      file.writeInt(-1);
    } else {
if(DEBUG_WR_DATA)System.err.println("wrIntMtx: row len (" + matrix.length + ")");
      file.writeInt(matrix.length);
      for (int i = 0; i < matrix.length; i++) {
        final int len = matrix[i].length;
if(DEBUG_WR_DATA)System.err.println("wrIntMtx: #" + i + " len (" + matrix[i].length + ")");
        file.writeInt(len);
        for (int j = 0; j < len; j++) {
if(DEBUG_WR_DATA_DETAIL)System.err.println("wrIntMtx: #" + i + "," + j + " (" + matrix[i][j] + ")");
          file.writeInt(matrix[i][j]);
        }
      }
    }
  }
}
