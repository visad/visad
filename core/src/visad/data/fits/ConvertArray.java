/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.fits;

class BooleanArrayConverter
	extends GenericArrayConverter
{
  public BooleanArrayConverter(int[] lengths)
  {
    super(Boolean.TYPE, lengths);
  }

  void assign(Object obj, int i, double v)
  {
    ((boolean[] )obj)[i] = v == 0.0 ? false : true;
  }
}

class ByteArrayConverter
	extends GenericArrayConverter
{
  boolean unsigned;

  public ByteArrayConverter(int[] lengths, boolean unsigned)
  {
    super(Byte.TYPE, lengths);

    this.unsigned = unsigned;
  }

  void assign(Object obj, int i, double v)
  {
    if (unsigned && v > Byte.MAX_VALUE) {
      v = Byte.MAX_VALUE - v;
    }

    ((byte[] )obj)[i] = (byte )v;
  }
}

class ShortArrayConverter
	extends GenericArrayConverter
{
  boolean unsigned;

  public ShortArrayConverter(int[] lengths, boolean unsigned)
  {
    super(Short.TYPE, lengths);

    this.unsigned = unsigned;
  }

  void assign(Object obj, int i, double v)
  {
    if (unsigned && v > Short.MAX_VALUE) {
      v = Short.MAX_VALUE - v;
    }

    ((short[] )obj)[i] = (short )v;
  }
}

class IntegerArrayConverter
	extends GenericArrayConverter
{
  boolean unsigned;

  public IntegerArrayConverter(int[] lengths, boolean unsigned)
  {
    super(Integer.TYPE, lengths);

    this.unsigned = unsigned;
  }

  void assign(Object obj, int i, double v)
  {
    if (unsigned && v > Integer.MAX_VALUE) {
      v = Integer.MAX_VALUE - v;
    }

    ((int[] )obj)[i] = (int )v;
  }
}

class LongArrayConverter
	extends GenericArrayConverter
{
  public LongArrayConverter(int[] lengths)
  {
    super(Long.TYPE, lengths);
  }

  void assign(Object obj, int i, double v)
  {
    ((long[] )obj)[i] = (long )v;
  }
}

class FloatArrayConverter
	extends GenericArrayConverter
{
  public FloatArrayConverter(int[] lengths)
  {
    super(Float.TYPE, lengths);
  }

  void assign(Object obj, int i, double v)
  {
    ((float[] )obj)[i] = (float )v;
  }
}

class DoubleArrayConverter
	extends GenericArrayConverter
{
  public DoubleArrayConverter(int[] lengths)
  {
    super(Double.TYPE, lengths);
  }

  void assign(Object obj, int i, double v)
  {
    ((double[] )obj)[i] = v;
  }
}

public abstract class ConvertArray
{
  private static final int UNSIGNED_ARRAY =	0x1000;
  private static final int NONINTEGRAL_ARRAY =	0x2000;

  private static final int BOOLEAN_ARRAY =	0x0001;
  private static final int BYTE_ARRAY =		0x0002;
  private static final int UBYTE_ARRAY =	0x1002;
  private static final int SHORT_ARRAY =	0x0004;
  private static final int USHORT_ARRAY =	0x1004;
  private static final int INT_ARRAY =		0x0008;
  private static final int UINT_ARRAY =		0x1008;
  private static final int LONG_ARRAY =		0x0010;
  private static final int FLOAT_ARRAY =	0x2001;
  private static final int DOUBLE_ARRAY =	0x2002;

  private boolean analyzed = false;

  int[] lengths = null;

  private int arrayType;

  int getArrayType(double min, double max, boolean integral)
  {
    // is it an array of real numbers?
    if (!integral) {
      // WLH 2 May 2000
      // if (min >= Float.MIN_VALUE && max <= Float.MAX_VALUE) {
      if (min >= -Float.MAX_VALUE && max <= Float.MAX_VALUE) {
	return FLOAT_ARRAY;
      }

      return DOUBLE_ARRAY;
    }

    // is it possibly unsigned?
    if (min >= 0) {
      if (max <= 1) {
	return BOOLEAN_ARRAY;
      }

      if (max <= (Byte.MAX_VALUE * 2) + 1) {
	return UBYTE_ARRAY;
      }

      if (max <= (Short.MAX_VALUE * 2) + 1) {
	return USHORT_ARRAY;
      }

      if (max <= ((long )Integer.MAX_VALUE * 2) + 1) {
	return UINT_ARRAY;
      }
    }

    if (min >= Byte.MIN_VALUE && max <= Byte.MAX_VALUE) {
      return BYTE_ARRAY;
    }

    if (min >= Short.MIN_VALUE && max <= Short.MAX_VALUE) {
      return SHORT_ARRAY;
    }

    if (min >= Integer.MIN_VALUE && max <= Integer.MAX_VALUE) {
      return INT_ARRAY;
    }

    return LONG_ARRAY;
  }

  abstract int analyzeArray();

  private void analyze()
  {
    if (analyzed) {
      return;
    }

    arrayType = analyzeArray();
  }

  public GenericArrayConverter getConverter()
  {
    analyze();

    switch (arrayType) {
    case BOOLEAN_ARRAY:
      return new BooleanArrayConverter(lengths);
    case BYTE_ARRAY:
      return new ByteArrayConverter(lengths, false);
    case UBYTE_ARRAY:
      return new ByteArrayConverter(lengths, true);
    case SHORT_ARRAY:
      return new ShortArrayConverter(lengths, false);
    case USHORT_ARRAY:
      return new ShortArrayConverter(lengths, true);
    case INT_ARRAY:
      return new IntegerArrayConverter(lengths, false);
    case UINT_ARRAY:
      return new IntegerArrayConverter(lengths, true);
    case LONG_ARRAY:
      return new LongArrayConverter(lengths);
    case FLOAT_ARRAY:
      return new FloatArrayConverter(lengths);
    case DOUBLE_ARRAY:
      return new DoubleArrayConverter(lengths);
    default:
      break;
    }

    return null;
  }
}
