
//
// HSVCoordinateSystem.java
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
   HSVCoordinateSystem is the VisAD class for cordinate
   systems for (Hue, Saturation, Value).<P>
*/
class HSVCoordinateSystem extends CoordinateSystem {

  private static Unit[] coordinate_system_units = {null, null, null};

  public HSVCoordinateSystem(RealTupleType reference)
         throws VisADException {
    super(reference, coordinate_system_units);
  }

  /** trusted constructor for initializers */
  HSVCoordinateSystem(RealTupleType reference, boolean b) {
    super(reference, coordinate_system_units, b);
  }

  public double[][] toReference(double[][] tuples) throws VisADException {
    throw new UnimplementedException(
      "HSVCoordinateSystem.toReference");
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    throw new UnimplementedException(
      "HSVCoordinateSystem.fromReference");
  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    throw new UnimplementedException(
      "HSVCoordinateSystem.toReference");
  }
 
  public float[][] fromReference(float[][] tuples) throws VisADException {
    throw new UnimplementedException(
      "HSVCoordinateSystem.fromReference");
  }

  public boolean equals(Object cs) {
    return (cs instanceof HSVCoordinateSystem);
  }

}

