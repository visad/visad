//
// ByteVector.java
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
 * A growable array of bytes.
 * @author Wayne Rasband wsr at nih.gov
 */
public class ByteVector {
  private byte[] data;
  private int size;

  public ByteVector() {
    data = new byte[10];
    size = 0;
  }

  public ByteVector(int initialSize) {
    data = new byte[initialSize];
    size = 0;
  }

  public ByteVector(byte[] byteBuffer) {
    data = byteBuffer;
    size = 0;
  }

  public void add(byte x) {
    if (size>=data.length) {
      doubleCapacity();
      add(x);
    }
    else data[size++] = x;
  }

  public int size() {
    return size;
  }

  public void add(byte[] array) {
    int length = array.length;
    while (data.length-size<length) doubleCapacity();
    System.arraycopy(array, 0, data, size, length);
    size += length;
  }

  void doubleCapacity() {
    byte[] tmp = new byte[data.length*2 + 1];
    System.arraycopy(data, 0, tmp, 0, data.length);
    data = tmp;
  }

  public void clear() {
    size = 0;
  }

  public byte[] toByteArray() {
    byte[] bytes = new byte[size];
    System.arraycopy(data, 0, bytes, 0, size);
    return bytes;
  }

}
