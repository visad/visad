/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

import visad.FlatField;
import visad.GriddedSet;
import visad.Set;
import visad.VisADException;

public class ConvertDoubleArray
	extends ConvertArray
{
  private double[][] values;

  public ConvertDoubleArray(FlatField fld)
	throws VisADException
  {
    Set set = fld.getDomainSet();
    if (!(set instanceof GriddedSet)) {
      throw new VisADException("Cannot convert non-GriddedSet FlatField");
    }

    lengths = ((GriddedSet )set).getLengths();
    values = fld.getValues();
  }

  public ConvertDoubleArray(int[] lengths, double[][] values)
	throws VisADException
  {
    this.lengths = lengths;
    this.values = values;
  }

  int analyzeArray()
  {
    double max = values[0][0];
    double min = values[0][0];

    boolean integral = true;
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values[i].length; j++) {
	double v = values[i][j];
	if (v > max) {
	  max = v;
	}
	if (v < min) {
	  min = v;
	}
	if (v >= Long.MIN_VALUE && v <= Long.MAX_VALUE) {
	  if (v % 1 > 0) {
	    integral = false;
	  }
	} else {
	  integral = false;
	}
      }
    }

    return getArrayType(min, max, integral);
  }
}
