package nom.tam.util;

/* Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 */

// What do we use in here?

import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/** This class is intended for high performance I/O in scientific applications.
  * It combines the functionality of the BufferedOutputStream and the
  * DataOutputStream as well as more efficient handling of arrays.
  * This minimizes the number of method calls that are required to
  * write data.  Informal tests of this method show that it can
  * be as much as 10 times faster than using a DataOutputStream layered
  * on a BufferedOutputStream for writing large arrays.  The performance
  * gain on scalars or small arrays will be less but there should probably
  * never be substantial degradation of performance.
  *
  * One routine is added to the public interface of DataOutput, writePrimitiveArray.
  * This routine provides efficient protocols for writing arrays.
  *
  * Note that there is substantial duplication of code to minimize method
  * invocations.
  */

public class BufferedDataOutputStream
               extends BufferedOutputStream
               implements DataOutput {

/** Use the BufferedOutputStream constructor
  * @param o An open output stream.
  */
public BufferedDataOutputStream(OutputStream o) {
    super(o);
}
/** Use the BufferedOutputStream constructor
  * @param o           An open output stream.
  * @param bufLength   The buffer size.
  */
public BufferedDataOutputStream(OutputStream o, int bufLength) {
    super(o, bufLength);
}


/** Write a boolean value
  * @param b  The value to be written.  Externally true is represented as
  *           a byte of 1 and false as a byte value of 0.
  */
public void writeBoolean(boolean b) throws IOException {

    if (b) {
        write( (byte) 1);
    } else {
        write( (byte) 0);
    }
}

/** Write a byte value.
  */
public void writeByte(int b) throws IOException {
    write((byte) b);
}

/** Write an integer value.
  */
public void writeInt(int i) throws IOException {
    byte[] b = new byte[4];

    b[0] = (byte) (i >>> 24);
    b[1] = (byte) (i >>> 16);
    b[2] = (byte) (i >>>  8);
    b[3] = (byte)  i;
    write(b, 0, 4);
}

/** Write a short value.
  */
public void writeShort(int s) throws IOException {
    byte[] b = new byte[2];

    b[0] = (byte) (s >>> 8);
    b[1] = (byte)  s;

    write(b, 0, 2);
}

/** Write a char value.
  */
public void writeChar(int c) throws IOException {
    byte[] b = new byte[2];
    b[0] = (byte) (c >>> 8);
    b[1] = (byte)  c;

    write(b, 0, 2);
}

/** Write a long value.
  */
public void writeLong(long l) throws IOException {
    byte[] b = new byte[8];

    b[0] = (byte) (l >>> 56);
    b[1] = (byte) (l >>> 48);
    b[2] = (byte) (l >>> 40);
    b[3] = (byte) (l >>> 32);
    b[4] = (byte) (l >>> 24);
    b[5] = (byte) (l >>> 16);
    b[6] = (byte) (l >>>  8);
    b[7] = (byte)  l;

    write(b, 0, 8);
}

/** Write a float value.
  */
public void writeFloat(float f) throws IOException {

    int i = Float.floatToIntBits(f);

    byte[] b = new byte[4];   // Repeat this from writeInt to save method call.

    b[0] = (byte) (i >>> 24);
    b[1] = (byte) (i >>> 16);
    b[2] = (byte) (i >>>  8);
    b[3] = (byte)  i;

    write(b, 0, 4);
}

/** Write a double value.
  */
public void writeDouble(double d) throws IOException {

    long l = Double.doubleToLongBits(d);
    byte[] b = new byte[8];

    b[0] = (byte) (l >>> 56);
    b[1] = (byte) (l >>> 48);
    b[2] = (byte) (l >>> 40);
    b[3] = (byte) (l >>> 32);
    b[4] = (byte) (l >>> 24);
    b[5] = (byte) (l >>> 16);
    b[6] = (byte) (l >>>  8);
    b[7] = (byte)  l;

    write(b, 0, 8);
}

/** Write a string using the local protocol to convert char's to bytes.
  *
  * @param s   The string to be written.
  */
public void writeBytes(String s) throws IOException {
    write(s.getBytes(),0,s.length());
}

/** Write a string as an array of chars.
  */
public void writeChars(String s) throws IOException {

    int len = s.length();
    char c;
    byte[] b = new byte[2*len];

    for (int i=0; i<len; i += 1) {
        c = s.charAt(i);
        b[2*i] = (byte) (c>>8);
        b[2*i+1] = (byte) c;
    }

    write(b, 0, 2*len);
}

/** Write a string as a UTF.  Note that this class does not
  * handle this situation efficiently since it creates
  * new DataOutputStream to handle each call.
  */
public void writeUTF(String s) throws IOException{

    // Punt on this one and use standard routines.
    DataOutputStream d = new DataOutputStream(this);
    d.writeUTF(s);
    d.flush();
}

/** This routine provides efficient writing of arrays of any primitive type.
  * The String class is also handled but it is an error to invoke this
  * method with an object that is not an array of these types.  If the
  * array is multidimensional, then it calls itself recursively to write
  * the entire array.  Strings are written using the standard
  * 1 byte format (i.e., as in writeBytes).
  *
  * If the array is an array of objects, then writePrimitiveArray will
  * be called for each element of the array.
  *
  * @param o  The object to be written.  It must be an array of a primitive
  *           type, Object, or String.
  */
public void writePrimitiveArray(Object o) throws IOException {
    String className = o.getClass().getName();

    if (className.charAt(0) != '[') {
        throw new IOException("Invalid object passed to BufferedDataOutputStream.writeArray:"+className);
    }

    // Is this a multidimensional array?  If so process recursively.
    if (className.charAt(1) == '[') {
        for (int i=0; i < ((Object[])o).length; i += 1) {
            writePrimitiveArray(((Object[])o)[i]);
        }
    } else {

        // This is a one-d array.  Process it using our special functions.
        switch (className.charAt(1)) {
        case 'Z': writeBooleanArray((boolean[])o);
             break;
        case 'B': write((byte[])o, 0, ((byte[])o).length);
             break;
        case 'C': writeCharArray((char[])o);
             break;
        case 'S': writeShortArray((short[])o);
             break;
        case 'I': writeIntArray((int[])o);
             break;
        case 'J': writeLongArray((long[])o);
             break;
        case 'F': writeFloatArray((float[])o);
             break;
        case 'D': writeDoubleArray((double[])o);
             break;
        case 'L':

             // Handle two exceptions: an array of strings, or an
             // array of objects. .
             if (className.equals("[Ljava.lang.String;") ) {
                 writeStringArray((String[])o);
             } else if (className.equals("[Ljava.lang.Object;")) {
                 for (int i=0; i< ((Object[])o).length; i += 1) {
                     writePrimitiveArray(((Object[])o)[i]);
                 }
             } else {
                 throw new IOException("Invalid object passed to BufferedDataOutputStream.writeArray: "+className);
             }
             break;
        default:
             throw new IOException("Invalid object passed to BufferedDataOutputStream.writeArray: "+className);
        }
    }

}

/** Write an array of booleans.
  */
protected void writeBooleanArray(boolean[] b) throws IOException {
    byte[] bx = new byte[b.length];
    for (int i=0; i<b.length; i += 1) {
        if (b[i]) {
             bx[i] = 1;
        } else {
             bx[i] = 0;
        }
    }
    write(bx, 0, bx.length);
}

/** Write an array of shorts.
  */
protected void writeShortArray(short[] s) throws IOException {
    byte[] b = new byte[2*s.length];

    for(int i=0; i<s.length; i += 1) {
        int t = s[i];
        b[2*i] = (byte) (t>>8);
        b[2*i + 1] = (byte) t;
    }
    write(b, 0, b.length);
}

/** Write an array of char's.
  */
protected void writeCharArray(char[] c) throws IOException {
    byte[] b = new byte[2*c.length];

    for(int i=0; i<c.length; i += 1) {
        int t = c[i];
        b[2*i] = (byte) (t>>8);
        b[2*i + 1] = (byte) t;
    }
    write(b, 0, b.length);
}

/** Write an array of int's.
  */
protected void writeIntArray(int[] i) throws IOException {
    byte[] b = new byte[4*i.length];

    for (int ii=0; ii<i.length; ii += 1) {
        int t = i[ii];
        b[4*ii]   = (byte)(t >>> 24);
        b[4*ii+1] = (byte)(t >>> 16);
        b[4*ii+2] = (byte)(t >>>  8);
        b[4*ii+3] = (byte) t;
    }

    write(b, 0, b.length);
}

/** Write an array of longs.
  */
protected void writeLongArray(long[] l) throws IOException {
    byte[] b = new byte[8*l.length];

    for (int i=0; i<l.length; i += 1) {
         long t = l[i];
         b[8*i]   = (byte)(t >>> 56);
         b[8*i+1] = (byte)(t >>> 48);
         b[8*i+2] = (byte)(t >>> 40);
         b[8*i+3] = (byte)(t >>> 32);
         b[8*i+4] = (byte)(t >>> 24);
         b[8*i+5] = (byte)(t >>> 16);
         b[8*i+6] = (byte)(t >>>  8);
         b[8*i+7] = (byte) t;
    }
    write(b, 0, b.length);
}

/** Write an array of floats.
  */
protected void writeFloatArray(float[] f) throws IOException {

    byte[] b = new byte[4*f.length];

    for (int i=0; i<f.length; i += 1) {
        int t = Float.floatToIntBits(f[i]);
        b[4*i]   = (byte)(t >>> 24);
        b[4*i+1] = (byte)(t >>> 16);
        b[4*i+2] = (byte)(t >>>  8);
        b[4*i+3] = (byte) t;
    }

    write(b, 0, b.length);
}

/** Write an array of doubles.
  */
protected void writeDoubleArray(double[] d) throws IOException {
    byte[] b = new byte[8*d.length];

    for (int i=0; i<d.length; i += 1) {
         long t = Double.doubleToLongBits(d[i]);
         b[8*i]   = (byte)(t >>> 56);
         b[8*i+1] = (byte)(t >>> 48);
         b[8*i+2] = (byte)(t >>> 40);
         b[8*i+3] = (byte)(t >>> 32);
         b[8*i+4] = (byte)(t >>> 24);
         b[8*i+5] = (byte)(t >>> 16);
         b[8*i+6] = (byte)(t >>>  8);
         b[8*i+7] = (byte) t;
    }
    write(b, 0, b.length);
}

/** Write an array of Strings -- equivalent to calling writeBytes for each string.
  */
protected void writeStringArray(String[] s) throws IOException {

    // Don't worry about buffering this specially since the
    // strings may be of differing lengths.

    for (int i=0; i<s.length; i += 1) {
        writeBytes(s[i]);
    }
}


/** Test this class */

public static void main(String[] args) throws Exception {

    // Call the test routines in BufferedDataInputStream.main.
    BufferedDataInputStream.main(args);
}

}
