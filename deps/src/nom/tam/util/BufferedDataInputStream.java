/** This class is intended for high performance I/O in scientific applications.
  * It combines the functionality of the BufferedInputStream and the
  * DataInputStream as well as more efficient handling of arrays.
  * This minimizes the number of method calls that are required to
  * read data.  Informal tests of this method show that it can
  * be as much as 10 times faster than using a DataInputStream layered
  * on a BufferedInputStream for writing large arrays.  The performance
  * gain on scalars or small arrays will be less but there should probably
  * never be substantial degradation of performance.
  *
  * One routine is added to the public interface of DataInput, readPrimitiveArray.
  * This routine provides efficient protocols for writing arrays.  Note that
  * they will create temporaries of a size equal to the array (if the array
  * is one dimensional).
  *
  * Note that there is substantial duplication of code to minimize method
  * invocations.  E.g., the floating point read routines read the data
  * as integer values and then convert to float.  However the integer
  * code is duplicated rather than invoked.
  */

// Member of the utilities package.

package nom.tam.util;

/* Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 */

// What do we use in here?

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;

public class BufferedDataInputStream
               extends BufferedInputStream
               implements DataInput {


private long bufferOffset=0;
private int primitiveArrayCount;

/** Use the BufferedInputStream constructor
  */
public BufferedDataInputStream(InputStream o) {
    super(o);
}
/** Use the BufferedInputStream constructor
  */
public BufferedDataInputStream(InputStream o, int bufLength) {
    super(o, bufLength);
}


public int read(byte[] buf, int offset, int len) throws IOException {


    int total = 0;

    // Ensure that the entire buffer is read.
    while (len > 0) {
        int xlen= super.read(buf, offset+total, len);
        if (xlen <= 0) {
            if (total == 0) {
                throw new EOFException();
            } else {
                return total;
            }
        } else {
            len -= xlen;
            total += xlen;
        }
    }
    return total;

}

public int read() throws IOException {
    return super.read();
}

public long skip(long offset) throws IOException {

    long total = 0;

    while (offset > 0) {
        long xoff = super.skip(offset);
        if (xoff == 0) {
            return total;
        }
        offset -= xoff;
        total += xoff;
    }
    return total;
}

/** Read a boolean value
  * @param b  The value to be written.  Externally true is represented as
  *           a byte of 1 and false as a byte value of 0.
  */
public boolean readBoolean() throws IOException {

    int b = read();
    if (b == 1) {
        return true;
    } else {
        return false;
    }
}

public byte readByte() throws IOException {
    return (byte) read();
}

public int readUnsignedByte() throws IOException {
    return read() | 0x00ff;
}

public int readInt() throws IOException {
    byte[] b = new byte[4];

    if (read(b, 0, 4) < 4 ) {
        throw new EOFException();
    }
    int i = b[0] << 24  | (b[1]&0xFF) << 16 | (b[2]&0xFF) << 8 | (b[3]&0xFF);
    return i;
}

public short readShort() throws IOException {
    byte[] b = new byte[2];

    if (read(b, 0, 2) < 2) {
        throw new EOFException();
    }

    short s = (short) (b[0] << 8 | (b[1]&0xFF));
    return s;
}

public int readUnsignedShort() throws IOException {
    byte[] b = new byte[2];

    if (read(b,0,2) < 2) {
        throw new EOFException();
    }

    return (b[0]&0xFF) << 8  |  (b[1]&0xFF);
}


public char readChar() throws IOException {
    byte[] b = new byte[2];

    if (read(b, 0, 2)  <  2) {
        throw new EOFException();
    }

    char c = (char) (b[0] << 8 | (b[1]&0xFF));
    return c;
}

public long readLong() throws IOException {
    byte[] b = new byte[8];

    // Let's use two int's as intermediarys so that we don't
    // have a lot of casts of bytes to longs...
    if (read(b, 0, 8) < 8) {
        throw new EOFException();
    }
    int i1 =  b[0] << 24 | (b[1]&0xFF) << 16 | (b[2]&0xFF) << 8 | (b[3]&0xFF);
    int i2 =  b[4] << 24 | (b[5]&0xFF) << 16 | (b[6]&0xFF) << 8 | (b[7]&0xFF);
    return  (((long) i1) << 32) | (((long)i2)&0x00000000ffffffffL);
}

public float readFloat() throws IOException {

    byte[] b = new byte[4];   // Repeat this from readInt to save method call.
    if (read(b, 0, 4) < 4) {
        throw new EOFException();
    }

    int i = b[0] << 24  | (b[1]&0xFF) << 16 | (b[2]&0xFF) << 8 | (b[3]&0xFF);
    return Float.intBitsToFloat(i);

}

public double readDouble() throws IOException {

    byte[] b = new byte[8];
    if (read(b, 0, 8) < 8) {
        throw new EOFException();
    }

    int i1 =  b[0] << 24 | (b[1]&0xFF) << 16 | (b[2]&0xFF) << 8 | (b[3]&0xFF);
    int i2 =  b[4] << 24 | (b[5]&0xFF) << 16 | (b[6]&0xFF) << 8 | (b[7]&0xFF);

    return Double.longBitsToDouble( ((long) i1) << 32 | ((long)i2&0x00000000ffffffffL) );
}

public void readFully(byte[] b) throws IOException {
    readFully(b, 0, b.length);
}

public void readFully(byte[] b, int off, int len) throws IOException {

    if (off < 0 || len < 0 || off+len > b.length) {
        throw new IOException("Attempt to read outside byte array");
    }

    if (read(b, off, len) < len) {
        throw new EOFException();
    }
}

public int skipBytes(int toSkip) throws IOException {

    if (skip(toSkip) < toSkip) {
        throw new EOFException();
    } else {
        return toSkip;
    }
}


public String readUTF() throws IOException{

    // Punt on this one and use DataInputStream routines.
    DataInputStream d = new DataInputStream(this);
    return d.readUTF();

}

/** This routine uses the deprecated DataInputStream.readLine() method.
  * However, we really want to simulate the behavior of that
  * method so that's what we used.
  * @deprecated
  */
public String readLine() throws IOException {
    // Punt on this and use DataInputStream routines.
    DataInputStream d = new DataInputStream(this);
    return d.readLine();
}

/** This routine provides efficient reading of arrays of any primitive type.
  * It is an error to invoke this method with an object that is not an array
  * of some primitive type.  Note that there is no corresponding capability
  * to writePrimitiveArray in BufferedDataOutputStream to read in an
  * array of Strings.
  *
  * @param o  The object to be read.  It must be an array of a primitive type,
  *           or an array of Object's.
  */

public int readPrimitiveArray(Object o) throws IOException {

    // Note that we assume that only a single thread is
    // doing a primitive Array read at any given time.  Otherwise
    // primitiveArrayCount can be wrong and also the
    // input data can be mixed up.  If this assumption isn't
    // true we need to synchronize this call.

    primitiveArrayCount = 0;
    return primitiveArrayRecurse(o);
}

protected int primitiveArrayRecurse(Object o) throws IOException {

    if (o == null) {
	return primitiveArrayCount;
    }

    String className = o.getClass().getName();

    if (className.charAt(0) != '[') {
        throw new IOException("Invalid object passed to BufferedDataInputStream.readArray:"+className);
    }

    // Is this a multidimensional array?  If so process recursively.
    if (className.charAt(1) == '[') {
        for (int i=0; i < ((Object[])o).length; i += 1) {
            primitiveArrayRecurse(((Object[])o)[i]);
        }
    } else {

        // This is a one-d array.  Process it using our special functions.
        switch (className.charAt(1)) {
        case 'Z':
             primitiveArrayCount += readBooleanArray((boolean[])o);
             break;
        case 'B':
             int len=read((byte[])o, 0, ((byte[])o).length);
             if (len < ((byte[])o).length){
                 primitiveArrayCount += len;
                 primitiveEOFThrower();
             }
             primitiveArrayCount += len;
             break;
        case 'C':
             primitiveArrayCount += readCharArray((char[])o);
             break;
        case 'S':
             primitiveArrayCount += readShortArray((short[])o);
             break;
        case 'I':
             primitiveArrayCount += readIntArray((int[])o);
             break;
        case 'J':
             primitiveArrayCount += readLongArray((long[])o);
             break;
        case 'F':
             primitiveArrayCount += readFloatArray((float[])o);
             break;
        case 'D':
             primitiveArrayCount += readDoubleArray((double[])o);
             break;
        case 'L':

             // Handle an array of Objects by recursion.  Anything
             // else is an error.
             if (className.equals("[Ljava.lang.Object;") ) {
                 for (int i=0; i < ((Object[])o).length; i += 1) {
                      primitiveArrayRecurse( ((Object[]) o)[i] );
                 }
             } else {
                 throw new IOException("Invalid object passed to BufferedDataInputStream.readArray: "+className);
             }
             break;
        default:
             throw new IOException("Invalid object passed to BufferedDataInputStream.readArray: "+className);
        }
    }
    return primitiveArrayCount;
}

protected int readBooleanArray(boolean[] b) throws IOException {
    byte[] bx = new byte[b.length];

    if (read(bx, 0, bx.length) < bx.length) {
        primitiveEOFThrower();
    }

    for (int i=0; i < b.length; i += 1) {
        if (bx[i] == 1) {
             b[i] = true;
        } else {
             b[i] = false;
        }
    }
    return bx.length;
}

protected int readShortArray(short[] s) throws IOException {

    if (s.length == 0) {
      return 0;
    }

    byte[] b = new byte[2*s.length];

    if (read(b, 0, b.length) < b.length) {
        primitiveEOFThrower();
    }
    char c = (char) (b[0] << 8 | b[1]);

    for(int i=0; i<s.length; i += 1) {
        s[i] = (short) (b[2*i] << 8 | (b[2*i+1]&0xFF));
    }
    return b.length;
}

protected int readCharArray(char[] c) throws IOException {
    byte[] b = new byte[2*c.length];
    if (read(b, 0, b.length) < b.length) {
        primitiveEOFThrower();
    }

    for(int i=0; i<c.length; i += 1) {
        c[i] = (char) (b[2*i] << 8 | (b[2*i+1]&0xFF));
    }
    return b.length;
}

protected int readIntArray(int[] i) throws IOException {
    byte[] b = new byte[4*i.length];

    if (read(b, 0, b.length) < b.length) {
        primitiveEOFThrower();
    }


    for (int ii=0; ii<i.length; ii += 1) {
        i[ii] = b[4*ii] << 24 | (b[4*ii+1]&0xFF) << 16 | (b[4*ii+2]&0xFF) << 8 | (b[4*ii+3]&0xFF);
    }
    return b.length;
}

protected int readLongArray(long[] l) throws IOException {
    byte[] b = new byte[8*l.length];

    if (read(b, 0, b.length) < b.length) {
        primitiveEOFThrower();
    }

    for (int i=0; i<l.length; i += 1) {
         int i1  = b[8*i]   << 24 | (b[8*i+1]&0xFF) << 16 | (b[8*i+2]&0xFF) << 8 | (b[8*i+3]&0xFF);
         int i2  = b[8*i+4] << 24 | (b[8*i+5]&0xFF) << 16 | (b[8*i+6]&0xFF) << 8 | (b[8*i+7]&0xFF);
         l[i] = ( (long) i1) << 32 | ((long)i2&0x00000000FFFFFFFFL);
    }
    return b.length;
}

protected int readFloatArray(float[] f) throws IOException {

    byte[] b = new byte[4*f.length];

    if (read(b, 0, b.length) < b.length) {
        primitiveEOFThrower();
    }

    for (int i=0; i<f.length; i += 1) {
        int t = b[4*i] << 24 |
               (b[4*i+1]&0xFF) << 16 |
               (b[4*i+2]&0xFF) <<  8 |
               (b[4*i+3]&0xFF);
        f[i] = Float.intBitsToFloat(t);
    }
    return b.length;
}

protected int readDoubleArray(double[] d) throws IOException {
    byte[] b = new byte[8*d.length];

    if(read(b, 0, b.length) < b.length) {
         primitiveEOFThrower();
    }

    for (int i=0; i<d.length; i += 1) {
         int i1  = b[8*i]   << 24 | (b[8*i+1]&0xFF) << 16 | (b[8*i+2]&0xFF) << 8 | (b[8*i+3]&0xFF);
         int i2  = b[8*i+4] << 24 | (b[8*i+5]&0xFF) << 16 | (b[8*i+6]&0xFF) << 8 | (b[8*i+7]&0xFF);
         d[i] = Double.longBitsToDouble(
                ((long) i1) << 32 | ((long)i2&0x00000000FFFFFFFFL));
    }
    return b.length;
}

protected void primitiveEOFThrower() throws EOFException {
    throw new EOFException("EOF on primitive array read after "+primitiveArrayCount+" bytes.");
}

public void printStatus() {

    System.out.println("BufferedDataInputStream:");
    System.out.println("    count="+count);
    System.out.println("      pos="+pos);
}

public String toString() {
    return "BufferedDataInputStream[count="+count+",pos="+pos+"]";
}


/** This method is used to test and time the buffered data methods.
  * Note that the BufferedDataOutputStream.main simply calls
  * this method which is used to test both classes in conjunction.
  */
public static void main(String args[]) throws Exception {

    // Test data.
    boolean booleanScalar = true;
    byte    byteScalar    = (byte) 0x12;
    short   shortScalar   = (short) 0x1234;
    char    charScalar    = 'p';
    int     intScalar     = 0x12345678;
    long    longScalar    = 0x1234567890abcdeL;
    float   floatScalar   = (float)1.1;
    double  doubleScalar  = 1.2;
    String  stringScalar  = "This is a string";

    boolean[] booleanArray = new boolean[50];
    byte[][]  byteArray    = new byte[50][50];
    short[]   shortArray   = new short[50];
    char[][]  charArray    = new char[50][50];
    int[][][] intArray     = new int[50][50][50];
    long[]    longArray    = new long[50];
    float[]   floatArray   = new float[50];
    double[][]doubleArray  = new double[50][50];


    for (int i=0; i<50; i += 1) {
        int sign = 1;
        if (i%2 > 0) {
            sign = -1;
        }
        booleanArray[i] = (i%2 == 1);
        shortArray[i] = (short) (sign * i|0x1234);
        longArray[i] = (long) sign * (i|0x1234567890abcdeL);
        floatArray[i] = i+sign*(float)2.33;
        for (int j=0; j<50; j += 1) {
            byteArray[i][j] = (byte) (i-j);
            charArray[i][j] = (char) (i+j);
            doubleArray[i][j] = sign*i*j*3.97;
            for (int k=0; k<50; k += 1) {
                intArray[i][j][k] = sign*(i*j + i*k + j*k + 0x1234567);
            }
        }
    }

    // Write and read back data.

    BufferedDataOutputStream o = new nom.tam.util.BufferedDataOutputStream (
                                     new java.io.FileOutputStream("BufferedData.test") );

    o.writeBoolean(booleanScalar);
    o.writePrimitiveArray(booleanArray);

    o.writeByte(byteScalar);

    // Write the byte array three different ways.
    o.writePrimitiveArray(byteArray);
    for (int i=0; i<50; i+= 1) {
        o.write(byteArray[i]);
    }

    for(int i=0; i<50; i += 1) {
        o.write(byteArray[i],  0, 25);
        o.write(byteArray[i], 25, 25);
    }

    o.writeShort(shortScalar);
    o.writePrimitiveArray(shortArray);

    o.writeChar(charScalar);
    o.writePrimitiveArray(charArray);

    o.writeInt(intScalar);
    o.writePrimitiveArray(intArray);

    o.writeLong(longScalar);
    o.writePrimitiveArray(longArray);

    o.writeFloat(floatScalar);
    o.writePrimitiveArray(floatArray);

    o.writeDouble(doubleScalar);
    o.writePrimitiveArray(doubleArray);

    o.flush();
    o.close();

    o = null;


    BufferedDataInputStream in = new BufferedDataInputStream(
             new java.io.FileInputStream("BufferedData.test") );



    System.out.println("Functionality tests (Note String I/O not checked)");
    System.out.println("");
    passes(booleanScalar == in.readBoolean(), "boolean scalar");
    boolean[] ba = new boolean[50];
    in.readPrimitiveArray(ba);
    passes(ba[0] == booleanArray[0], "boolean array (start)");
    passes(ba[49] == booleanArray[49], "boolean array (end)");
    passes(ba[22] == booleanArray[22], "boolean array (middle)");

    passes(byteScalar == in.readByte(), "byte scalar");


    byte[][] binp1 = new byte[50][50];
    byte[][] binp2 = new byte[50][50];
    byte[][] binp3 = new byte[50][50];
    // read the byte array three different ways --
    // we deliberately do this differently than above.
    for(int i=0; i<50; i += 1) {
        in.read(binp3[i],  0, 25);
        in.read(binp3[i], 25, 25);
    }
    for (int i=0; i<50; i+= 1) {
        in.readFully(binp2[i]);
    }
    in.readPrimitiveArray(binp1);

    passes(binp1[0][0] == byteArray[0][0], "byte array(start-method1)");
    passes(binp1[49][49] == byteArray[49][49], "byte array(end-method1)");
    passes(binp1[22][22] == byteArray[22][22], "byte array(middle-method1");
    passes(binp2[0][0] == byteArray[0][0], "byte array(start-method2)");
    passes(binp2[49][49] == byteArray[49][49], "byte array(end-method2)");
    passes(binp2[22][22] == byteArray[22][22], "byte array(middle-method2");
    passes(binp3[0][0] == byteArray[0][0], "byte array(start-method3)");
    passes(binp3[49][49] == byteArray[49][49], "byte array(end-method3)");
    passes(binp3[22][22] == byteArray[22][22], "byte array(middle-method3");


    passes(shortScalar == in.readShort(), "short scalar");

    short[] sa = new short[50];
    in.readPrimitiveArray(sa);

    passes(sa[0] == shortArray[0], "short array (start)");
    passes(sa[49] == shortArray[49], "short array (end)");
    passes(sa[22] == shortArray[22], "short array (middle)");

    passes(charScalar == in.readChar(), "char scalar");

    char[][] ca = new char[50][50];
    in.readPrimitiveArray(ca);

    passes(ca[0][0] == charArray[0][0], "char array (start)");
    passes(ca[49][49] == charArray[49][49], "char array (end)");
    passes(ca[22][22] == charArray[22][22], "char array (middle)");

    passes(intScalar == in.readInt(), "int scalar");
    int[][][] ia = new int[50][50][50];
    in.readPrimitiveArray(ia);
    passes(ia[0][0][0] == intArray[0][0][0], "int array (start)");
    passes(ia[49][49][49] == intArray[49][49][49], "int array (end)");
    passes(ia[22][22][22] == intArray[22][22][22], "int array (middle)");


    passes (longScalar == in.readLong(), "long scalar");
    long[] la = new long[50];
    in.readPrimitiveArray(la);
    passes(la[0] == longArray[0], "long array (start)");
    passes(la[49] == longArray[49], "long array (end)");
    passes(la[22] == longArray[22], "long array (middle)");

    passes (floatScalar == in.readFloat(), "float scalar");
    float[] fa = new float[50];
    in.readPrimitiveArray(fa);
    passes(fa[0] == floatArray[0], "float array (start)");
    passes(fa[49] == floatArray[49], "float array (end)");
    passes(fa[22] == floatArray[22], "float array (middle)");

    passes(doubleScalar == in.readDouble(), "double scalar");
    double[][] da = new double[50][50];
    in.readPrimitiveArray(da);
    passes(da[0][0] == doubleArray[0][0], "double array (start)");
    passes(da[49][49] == doubleArray[49][49], "double array (end)");
    passes(da[22][22] == doubleArray[22][22], "double array (middle)");

    in = null;

    System.out.println("");
    System.out.println("Timing test:  Write and read an 800x800 int array");
    System.out.println("");
    System.out.println("Initializing array");
    int[][] data = new int[800][800];
    int[][] indata = new int[800][800];

    for (int i=0; i<data.length; i += 1) {
         for (int j=0; j<data[0].length; j += 1) {
              data[i][j] = i*j * (i-j);
         }
    }


    System.out.println("");

    System.out.println("Using DataXputStream(BufferedXputStream) at "+new java.util.Date());
    java.io.DataOutputStream ds = new java.io.DataOutputStream(
                              new java.io.BufferedOutputStream(
                                  new java.io.FileOutputStream("test_std.data")) );
    for (int i=0; i<800; i += 1) {
        for (int j=0; j<800; j += 1) {
             ds.writeInt(data[i][j]);
        }
    }

    ds.flush();
    ds.close();

    ds = null;

    System.out.println("                          Finished write at:"+new java.util.Date());

    java.io.DataInputStream is =  new java.io.DataInputStream(
                              new BufferedInputStream(
                                  new java.io.FileInputStream("test_std.data")) );

    for (int i=0; i<800; i += 1) {
        for (int j=0; j<800; j += 1) {
            indata[i][j] = is.readInt();
        }
    }
    is = null;
    System.out.println("                          Finished read at: "+new java.util.Date());

    System.out.println("");
    System.out.println("Using BufferedDataXputStream at             "+new java.util.Date());

    BufferedDataOutputStream ob = new nom.tam.util.BufferedDataOutputStream(
                                      new java.io.FileOutputStream("test_bd.data"));

    ob.writePrimitiveArray(data);
    ob.flush();
    ob.close();

    ob = null;

    System.out.println("                          Finished write at:"+new java.util.Date());


    BufferedDataInputStream ib = new BufferedDataInputStream(
                                     new java.io.FileInputStream("test_bd.data"));
    ib.readPrimitiveArray(indata);
    ib = null;
    System.out.println("                          Finished read at: " + new java.util.Date());

}

private static void passes (boolean status, String msg) {

     System.out.print(msg+":");
     if (msg.length() < 30) {
         System.out.print("                              ".substring(0,30-msg.length()));
     }
     if (status) {
          System.out.println(" passes");
     } else {
          System.out.println(" fails");
     }
}

}
