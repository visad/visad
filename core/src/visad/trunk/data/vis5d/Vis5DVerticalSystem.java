//
// Vis5DVerticalSystem.java
//

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

package visad.data.vis5d;

import visad.*;
import visad.data.units.Parser;
import visad.data.units.ParseException;

public class Vis5DVerticalSystem
{
  private final int vert_sys;
  private int n_levels;
  private double[] vert_args;
  private Unit  vert_unit = null;

  SampledSet vertSet;
  RealType vert_type;

/*------------------------------------------------------
    A special 1-D RealTupleType for CoordinateSystem
    (row, col, height) with Reference (lat, lon, height)
    where the transform to the Reference is described via
    a CartesianProductCoordinateSystem.  This eliminates
    the circularity by defining a new RealType for height
    with ScalarName "Height_2" in the Reference tuple. */

  RealTupleType reference;
/*-----------------------------------------------------*/
  
  public Vis5DVerticalSystem( int vert_sys,
                              int n_levels,
                              double[] vert_args)
         throws VisADException
  {
    this.vert_sys = vert_sys;
    this.vert_args = vert_args;
    this.n_levels = n_levels;

    switch ( vert_sys )
    {
      case (0):
        vert_type = RealType.getRealType("Height");
        reference =
          new RealTupleType(RealType.getRealType("Height_2"));
        break;
      case (1):
      case (2):
        try {
          vert_unit = Parser.parse("km");
        }
        catch (ParseException e) {
        }
        vert_type = RealType.getRealType("Height", vert_unit);
        reference =
          new RealTupleType(RealType.getRealType("Height_2", vert_unit));
        break;
      case (3):
        try {
          vert_unit = Parser.parse("mb");
        }
        catch (ParseException e) {
        }
        vert_type = RealType.getRealType("Height", vert_unit);
        reference =
          new RealTupleType(RealType.getRealType("Height_2", vert_unit));
        break;
      default:
        throw new VisADException("vert_sys unknown");
    }

    switch ( vert_sys )
    {
      case (0):
      case (1):
        double first = vert_args[0];
        double last = first + vert_args[1]*(n_levels-1);
        vertSet = new Linear1DSet(vert_type, first, last, n_levels);
        break;
      case (2):
      case (3):
        double[][] values = new double[1][n_levels];
        System.arraycopy(vert_args, 0, values[0], 0, n_levels);
        vertSet =
          new Gridded1DSet(vert_type, Set.doubleToFloat(values), n_levels);
        break;
      default:
         throw new VisADException("vert_sys unknown");
    }
  }
}
