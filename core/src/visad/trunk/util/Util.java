/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.util;

/**
 * A hodge-podge of general utility methods.
 */
public class Util
{
  /**
   * Determine whether two numbers are roughly the same.
   *
   * @param a First number
   * @param b Second number
   * @param epsilon Absolute amount by which they can differ.
   *
   * @return <TT>true</TT> if they're approximately equal.
   */
  public static boolean isApproximatelyEqual(float a, float b, float epsilon)
  {
    // deal with NaNs first
    if (Float.isNaN(a)) {
      return Float.isNaN(b);
    } else if (Float.isNaN(b)) {
      return false;
    }

    if (Math.abs(a - b) < epsilon) {
      return true;
    }

    if (a == 0.0 || b == 0.0) {
      return false;
    }

    final float FLT_EPSILON = 1.192092896E-07F;

    return Math.abs(1 - a / b) < FLT_EPSILON;
  }

  /**
   * Determine whether two numbers are roughly the same.
   *
   * @param a First number
   * @param b Second number
   *
   * @return <TT>true</TT> if they're approximately equal.
   */
  public static boolean isApproximatelyEqual(float a, float b)
  {
    return isApproximatelyEqual(a, b, 0.00001);
  }

  /**
   * Determine whether two numbers are roughly the same.
   *
   * @param a First number
   * @param b Second number
   * @param epsilon Absolute amount by which they can differ.
   *
   * @return <TT>true</TT> if they're approximately equal.
   */
  public static boolean isApproximatelyEqual(double a, double b,
                                             double epsilon)
  {
    // deal with NaNs first
    if (Double.isNaN(a)) {
      return Double.isNaN(b);
    } else if (Double.isNaN(b)) {
      return false;
    }

    if (Math.abs(a - b) < epsilon) {
      return true;
    }

    if (a == 0.0 || b == 0.0) {
      return false;
    }

    final double DBL_EPSILON = 2.2204460492503131E-16;

    return Math.abs(1 - a / b) < DBL_EPSILON;
  }

  /**
   * Determine whether two numbers are roughly the same.
   *
   * @param a First number
   * @param b Second number
   *
   * @return <TT>true</TT> if they're approximately equal.
   */
  public static boolean isApproximatelyEqual(double a, double b)
  {
    return isApproximatelyEqual(a, b, 0.000000001);
  }

  /**
   * Converts an array of ints to an array of bytes. Each integer is cut into
   * four byte-size pieces, making the resulting byte array four times the
   * length of the input int array.
   *
   * @param ints The array of ints to be converted to a byte array
   *
   * @return An array of bytes corresponding to the original int array.
   */
  public static byte[] intToBytes(int[] ints)
  {
    int len = ints.length;
    byte[] bytes = new byte[4 * len];
    for (int i=0; i<len; i++) {
      int q = ints[i];
      bytes[4 * i] = (byte) (q & 0x000000ff);
      bytes[4 * i + 1] = (byte) ((q & 0x0000ff00) >> 8);
      bytes[4 * i + 2] = (byte) ((q & 0x00ff0000) >> 16);
      bytes[4 * i + 3] = (byte) ((q & 0xff000000) >> 24);
    }
    return bytes;
  }

  /**
   * Converts an array of bytes to an array of ints. Each group of four bytes
   * form a single int, making the resulting int array one fourth the length
   * of the input byte array. Note that trailing elements of the bytes array
   * will be ignored.
   *
   * @param bytes The array of bytes to be converted to an int array
   *
   * @return An array of ints corresponding to the original byte array.
   */
  public static int[] bytesToInt(byte[] bytes)
  {
    int len = bytes.length / 4;
    int[] ints = new int[len];
    for (int i=0; i<len; i++) {
      // This byte decoding method is not very good; is there a better way?
      int q3 = bytes[4 * i + 3] << 24;
      int q2 = bytes[4 * i + 2] << 16;
      int q1 = bytes[4 * i + 1] << 8;
      int q0 = bytes[4 * i];
      if (q2 < 0) q2 += 16777216;
      if (q1 < 0) q1 += 65536;
      if (q0 < 0) q0 += 256;
      ints[i] = q3 | q2 | q1 | q0;
    }
    return ints;
  }

  /**
   * Escape value for an RLE encoded sequence.
   */
  private static final int RLE_ESCAPE = Integer.MIN_VALUE;

  /**
   * Encodes the given array of ints using a run-length scheme.
   *
   * @param array The array of ints to RLE-encode
   *
   * @return An RLE-encoded array of ints.
   */
  public static int[] encodeRLE(int[] array) {
    int len = array.length;
    int[] temp = new int[len];
    int p = 0;

    for (int i=0; i<len;) {
      int q = array[i];
      int count = 0;
      while (i < len && q == array[i]) {
        count++;
        i++;
      }

      if (count < 4) {
        // no gain from RLE; save values directly
        for (int z=0; z<count; z++) temp[p++] = q;
      }
      else {
        // compress data using RLE
        temp[p++] = RLE_ESCAPE;
        temp[p++] = q;
        temp[p++] = count;
      }
    }

    // trim encoded array
    int[] encoded = new int[p];
    System.arraycopy(temp, 0, encoded, 0, p);
    return encoded;
  }

  /**
   * Decodes the given array of ints from a run-length encoding.
   *
   * @param array The RLE-encoded array of ints to decode
   *
   * @return A decoded array of ints.
   */
  public static int[] decodeRLE(int[] array) {
    // compute size of decoded array
    int count = 0;
    int i = 0;
    while (i < array.length) {
      if (array[i] == RLE_ESCAPE) {
        count += array[i + 2];
        i += 3;
      }
      else {
        count++;
        i++;
      }
    }

    // allocate decoded array
    int[] decoded = new int[count];
    int p = 0;

    // decode RLE sequence
    for (i=0; i<array.length; i++) {
      int q = array[i];
      if (q == RLE_ESCAPE) {
        int val = array[++i];
        int cnt = array[++i];
        for (int z=0; z<cnt; z++) decoded[p++] = val;
      }
      else decoded[p++] = q;
    }
    return decoded;
  }

}
