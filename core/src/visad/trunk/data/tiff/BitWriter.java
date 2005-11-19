//
// BitWriter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.tiff;

/**
 * A class for writing arbitrary numbers of bits to a byte array.
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class BitWriter {

  // -- Constants --

  private static final int[] BACK_MASK = new int[]
    {0x0000, 0x0001, 0x0003, 0x0007, 0x000F, 0x001F, 0x003F, 0x007F};

  private static final int[] FRONT_MASK = new int[]
    {0x0000, 0x0080, 0x00C0, 0x00E0, 0x00F0, 0x00F8, 0x00FC, 0x00FE};


  // -- Fields --

  /** Buffer storing all bits written thus far. */
  private byte[] buf;

  /** Byte index into the buffer. */
  private int index;

  /** Bit index into current byte of the buffer. */
  private int bit;


  // -- Constructors --

  /** Constructs a new bit writer. */
  public BitWriter() {
    this(10);
  }

  /** Constructs a new bit writer with the given initial buffer size. */
  public BitWriter(int size) {
    buf = new byte[size];
  }


  // -- BitWriter API methods --

  /** Writes the given value using the given number of bits. */
  public void write(int value, int numBits) {
    byte[] bits = new byte[numBits];
    for (int i=0; i<numBits; i++) {
      bits[i] = (byte) (value & 0x0001);
      value >>= 1;
    }
    for (int i=numBits-1; i>=0; i--) {
      int b = bits[i] << (7 - bit);
      buf[index] |= b;
      bit++;
      if (bit > 7) {
        bit = 0;
        index++;
        if (index >= buf.length) {
          // buffer is full; increase the size
          byte[] newBuf = new byte[buf.length * 2];
          System.arraycopy(buf, 0, newBuf, 0, buf.length);
          buf = newBuf;
        }
      }
    }
  }

  /** Gets an array containing all bits written thus far. */
  public byte[] toByteArray() {
    int size = index;
    if (bit > 0) size++;
    byte[] b = new byte[size];
    System.arraycopy(buf, 0, b, 0, size);
    return b;
  }


  // -- Main method --

  /** Tests the BitWriter class. */
  public static void main(String[] args) {
    int max = 500000;

    // write values out
    BitWriter out = new BitWriter();
    int num = 1, bits = 1, count = 0;
    for (int i=1; i<=max; i++) {
      out.write(i, bits);
      count++;
      if (count == num) {
        num *= 2;
        bits++;
        count = 0;
      }
    }

    // read values back in
    BitBuffer bb = new BitBuffer(out.toByteArray());
    num = 1; bits = 1; count = 0;
    for (int i=1; i<=max; i++) {
      int value = bb.getBits(bits);
      if (value != i) {
        System.out.println("Value #" + i + " does not match (" + value + ")");
      }
      count++;
      if (count == num) {
        num *= 2;
        bits++;
        count = 0;
      }
    }
  }
}
