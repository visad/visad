/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

public abstract class GenericArrayConverter
{
  Object o;
  int[] lengths;

  public GenericArrayConverter(Class type, int[] lengths)
  {
    o = java.lang.reflect.Array.newInstance(type, lengths);

    this.lengths = lengths;
  }

  abstract void assign(Object obj, int index, double value);

  private Object getBottomArray(Object o, int[] which)
  {
    int d = which.length - 1;

    int i = 0;
    while (i < d) {
      o = java.lang.reflect.Array.get(o, which[i++]);
    }

    return o;
  }

  private Object getNextRMBottomArray(Object o, int[] coord)
  {
    int l = coord.length - 1;

    while (coord[l] >= lengths[l]) {
      coord[l] = 0;

      l--;
      if (l < 0) {
	return null;
      }

      coord[l]++;
    }

    return getBottomArray(o, coord);
  }

  private Object getNextCMBottomArray(Object o, int[] coord)
  {
    int l = 0;

    while (coord[l] >= lengths[lengths.length - (l+1)]) {
      coord[l] = 0;

      l++;
      if (l >= coord.length) {
	return null;
      }

      coord[l]++;
    }

    return getBottomArray(o, coord);
  }

  public Object getRowMajor(double[][] values)
  {
    int[] coord = new int[lengths.length];
    for (int i = 0; i < lengths.length; i++) {
      coord[i] = 0;
    }

    final int lastCoord = lengths.length - 1;

    Object ra = getBottomArray(o, coord);
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values[i].length; j++) {
        assign(ra, lengths[lastCoord] - ++coord[lastCoord], values[i][j]);

	if (coord[lastCoord] >= lengths[lastCoord]) {
	  ra = getNextRMBottomArray(o, coord);
	  if (ra == null) {
	    return o;
	  }
	}
      }
    }

    return o;
  }

  public Object getColumnMajor(double[][] values)
  {
    int[] coord = new int[lengths.length];
    for (int i = 0; i < lengths.length; i++) {
      coord[i] = 0;
    }

    final int lastCoord = lengths.length - 1;

    Object ra;
    for (int i = 0; i < values[0].length; i++) {
      assign(getBottomArray(o, coord), coord[lastCoord], values[0][i]);
      coord[0]++;

      if (coord[0] >= lengths[0]) {
        if (getNextCMBottomArray(o, coord) == null) {
          return o;
	}
      }
    }

    return o;
  }
}
