
//
// CMYCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

package visad;

/**
   CMYCoordinateSystem is the VisAD CoordinateSystem class for
   (Cyan, Magenta, Yellow).  Algorithm from Foley and van Dam.<P>
*/
class CMYCoordinateSystem extends CoordinateSystem {

  private static Unit[] coordinate_system_units = {null, null, null};

  public CMYCoordinateSystem(RealTupleType reference) throws VisADException {
    super(reference, coordinate_system_units);
  }

  /** trusted constructor for initializers */
  CMYCoordinateSystem(RealTupleType reference, boolean b) {
    super(reference, coordinate_system_units, b);
  }

  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("CMYCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[3][len];
    for (int i=0; i<len ;i++) {
      value[0][i] = 1.0 - tuples[0][i];
      value[1][i] = 1.0 - tuples[1][i];
      value[2][i] = 1.0 - tuples[2][i];
    }
    return value;
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("CMYCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[3][len];
    for (int i=0; i<len ;i++) {
      value[0][i] = 1.0 - tuples[0][i];
      value[1][i] = 1.0 - tuples[1][i];
      value[2][i] = 1.0 - tuples[2][i];
    }
    return value;
  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("CMYCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[3][len];
    for (int i=0; i<len ;i++) {
      value[0][i] = 1.0f - tuples[0][i];
      value[1][i] = 1.0f - tuples[1][i];
      value[2][i] = 1.0f - tuples[2][i];
    }
    return value;
  }
 
  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("CMYCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[3][len];
    for (int i=0; i<len ;i++) {
      value[0][i] = 1.0f - tuples[0][i];
      value[1][i] = 1.0f - tuples[1][i];
      value[2][i] = 1.0f - tuples[2][i];
    }
    return value;
  }

  public boolean equals(Object cs) {
    return (cs instanceof CMYCoordinateSystem);
  }

}

