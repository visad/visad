//
// GVARCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.data.mcidas;
import visad.*;
import visad.CoordinateSystem;

/**
   GVARCoordinateSystem is the VisAD CoordinateSystem class
   for conversions to (Latitude, Longitude) from a Cartesian (line,element),
   and with Latitude and Longitude in degrees.<P>
*/
public class GVARCoordinateSystem extends CoordinateSystem {
  private GVARnav ng = null;

  private static Unit[] coordinate_system_units =
    {null, null};

  public GVARCoordinateSystem(RealTupleType reference, int[] dir,
                                 int[] nav) throws VisADException {

    super(reference, coordinate_system_units);
    setNav(dir, nav);
  }

  /** trusted constructor for initializers */
  /*
  GVARCoordinateSystem(RealTupleType reference, boolean b,
                                      int[]dir, int[] nav) {

    super(reference, coordinate_system_units, b);
    setNav(dir, nav);
  }
  */

  private void setNav(int[] dir, int[] nav) {
    ng = new GVARnav(1, nav);
    ng.setImageStart(dir[5], dir[6]);
    ng.setRes(dir[11], dir[12]);
    ng.setStart(1,1);
    ng.setMag(1,1);
    ng.setFlipLineCoordinates(dir[8]);
    return;
  }


  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("GVARCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }

    if (ng == null) {
      throw new CoordinateSystemException("GVAR O & A data not availble");
    }

    return ng.toLatLon(tuples);

  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("GVARCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }

    if (ng == null) {
      throw new CoordinateSystemException("GVAR O & A data not availble");
    }

    return ng.toLinEle(tuples);

  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("GVARCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }

    if (ng == null) {
      throw new CoordinateSystemException("GVAR O & A data not availble");
    }

    double[][] val = Set.floatToDouble(tuples);
    val = ng.toLatLon(val);
    return Set.doubleToFloat(val);

  }
 
  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("GVARCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }

    if (ng == null) {
      throw new CoordinateSystemException("GVAR O & A data not availble");
    }

    double[][] val = Set.floatToDouble(tuples);
    val = ng.toLinEle(val);
    return Set.doubleToFloat(val);

  }

  public boolean equals(Object cs) {
    return (cs instanceof GVARCoordinateSystem);
  }

}

