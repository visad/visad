//
// GriddedDoubleSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

/**
   GriddedDoubleSet is an interface for GriddedSets that have
   double-precision samples rather than single-precision samples.<P>
*/
public interface GriddedDoubleSet extends GriddedSetIface 
{

  /*
  In addition to implementing all the methods given here, an implementation
  of GriddedDoubleSet should also override the following methods:

  void init_samples(double[][] samples, boolean copy) throws VisADException;
  void cram_missing(boolean[] range_select);
  boolean isMissing();
  public boolean equals(Object set);
  public Object clone();
  public Object cloneButType(MathType type) throws VisADException;

  See Gridded1DDoubleSet.java for an example.
  */

  double[][] getDoubles(boolean copy) throws VisADException;

  double[][] indexToDouble(int[] index) throws VisADException;

  int[] doubleToIndex(double[][] value) throws VisADException;

  double[][] gridToDouble(double[][] grid) throws VisADException;

  double[][] doubleToGrid(double[][] value) throws VisADException;

  void doubleToInterp(double[][] value, int[][] indices, double[][] weights)
    throws VisADException;

}

