//
// BitBuffer.java
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

import java.io.*;

/** A class for reading arbitrary numbers of bits from an input stream. */
public class BitBuffer {

  private InputStream in;
  private int currentbyte;
  private int currentbit;
  private byte[] bytebuffer;
  private int eofByte;
  private int[] backmask;
  private int[] frontmask;
  private boolean eofFlag;

  public BitBuffer(InputStream i) throws IOException {
    bytebuffer = new byte[8192];
    in = i;
    currentbyte = 0;
    currentbit = 0;
    eofByte = in.read(bytebuffer);
    // System.out.println(" eofByte: " + eofByte);
    eofFlag = false;
    if (eofByte < 1) {
      eofFlag = true;
    }
    backmask = new int[] {0x0000, 0x0001, 0x0003, 0x0007,
                          0x000F, 0x001F, 0x003F, 0x007F};
    frontmask = new int[] {0x0000, 0x0080, 0x00C0, 0x00E0,
                           0x00F0, 0x00F8, 0x00FC, 0x00FE};
  }

  public long skipBits(long bitstoskip) throws IOException {
    long skipbytes = (long) bitstoskip / 8;
    long skipbits = bitstoskip % 8;
    long newbyte = currentbyte + skipbytes;
    long newbit = currentbit + skipbits;
    long toreturn = bitstoskip;
    if (newbit > 8) {
      newbit -= 8;
      newbyte++;
    }
    if (newbyte >= eofByte) {
      // The byte to skip to is out of the current block.
      if (eofByte != 8192) {
        // meaning yeah, we actually reached the end of the file.
//        System.out.println("1");
        eofFlag = true;
        currentbyte = eofByte;
        currentbit = 0;
        toreturn = (8 - currentbit) + 8 * (eofByte - currentbyte);
      }
      else {
        // meaning maybe we haven't, but we don't know, so trying to skip the
        // correct number of bytes.
//        System.out.println("2");
        newbyte -= 8192; // need to account for the current buffer.
        long skipped = -1;
        // This part may not suffice. Why would in.skip() fail?
        while(skipped != 0) {
          skipped = in.skip(newbyte);
          newbyte -= skipped;
        }
        if (newbyte != 0) {
          // When we are unable to skip all of the bytes, the
          // file is assumed to be finished.
//          System.out.println("3");
          eofFlag = true;
        }
        else {
          // Otherwise, we have bytes we can still read:
//          System.out.println("4");
          currentbyte = 0;
          currentbit = (int) newbit;
          eofByte = in.read(bytebuffer);
        }
      }
    }
    else {
      // The byte to skip to is in the current block, and readable
      currentbyte = (int) newbyte;
      currentbit = (int) newbit;
    }
    return toreturn;
  }

  public int getBits(int bitstoread)
    throws IOException, FileNotFoundException
  {
    if (bitstoread == 0) {
      return 0;
    }
    if (eofFlag) {
      return -1; // Already at end of file
    }
    int tostore = 0;
    while(bitstoread != 0  && !eofFlag) {
//      System.out.println("byte: " + currentbyte + " bit: " + currentbit);
      if (bitstoread >= 8 - currentbit) {
        if (currentbit == 0) { // special
          tostore = tostore << 8;
          int cb = ((int) bytebuffer[currentbyte]);
          tostore += (cb<0 ? (int) 256 + cb : (int) cb);
          bitstoread -= 8;
          currentbyte++;
        }
        else {
          tostore = tostore << (8 - currentbit);
          tostore += ((int) bytebuffer[currentbyte]) &
            backmask[8 - currentbit];
          bitstoread -= (8 - currentbit);
          currentbit = 0;
          currentbyte++;
        }
      }
      else {
//        System.out.println(bitstoread);
        tostore = tostore << bitstoread;

        int cb = ((int) bytebuffer[currentbyte]);
        cb = (cb<0 ? (int) 256 + cb : (int) cb);
        tostore += ((cb) & (0x00FF - frontmask[currentbit])) >>
          (8 - (currentbit + bitstoread));
//        System.out.println("Byte : " + cb);
//        System.out.println("Mask : " + (0x00FF - frontmask[currentbit] -
//          backmask[8 - (currentbit + bitstoread)]));
//        System.out.println("Shift: " + (8 - (currentbit + bitstoread)));
//        System.out.println("Res 1: " + ((cb) & (0x00FF -
//          frontmask[currentbit] - backmask[8 - (currentbit + bitstoread)])));
//        System.out.println("Res 2: " + (((cb) & (0x00FF -
//          frontmask[currentbit])) >> (8 - (currentbit + bitstoread))));

        currentbit += bitstoread;
        bitstoread = 0;
      }
      if (currentbyte == 8192) {
        eofByte = in.read(bytebuffer);
        currentbyte = 0;
      }
      if (currentbyte == eofByte) {
        eofFlag = true;
        return tostore;
      }
    }
    return tostore;
  }

// Test method.

/*
  public static void main(String[] args) throws Exception {
    BitBuffer b = new BitBuffer(new FileInputStream(new File(args[0])));
    int i = 1;
    int current = 0;
    int numbits = Integer.parseInt(args[1]);
//    System.out.println(args[0]);
//    System.out.println(args[1]);
    while(current != -1) {
      current = b.getBits(numbits);
      System.out.println(i + ": " + current);
      i++;
//      b.skipBits(65536);
    }
  }
*/

}
