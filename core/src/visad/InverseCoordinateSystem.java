//
// InverseCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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
   InverseCoordinateSystem is the VisAD CoordinateSystem class
   for inverting other CoordinateSystems.<P>
*/
public class InverseCoordinateSystem extends CoordinateSystem {

  private CoordinateSystem inverse;
  private int dimension;

  /** construct a CoordinateSystem that whose transforms invert
      the transforms of inverse (i.e., toReference and
      fromReference are switched); for example, this could be
      used to define Cartesian coordinates releative to a
      refernce in spherical coordinates */
  public InverseCoordinateSystem(RealTupleType reference, CoordinateSystem inv)
         throws VisADException {
    super(reference, inv.getReference().getDefaultUnits());
    inverse = inv;
    dimension = reference.getDimension();
    Unit[] inv_units = inv.getCoordinateSystemUnits();
    Unit[] ref_units = reference.getDefaultUnits();
    if (inv_units.length != dimension) {
      throw new CoordinateSystemException("InverseCoordinateSystem: " +
                                          "dimensions don't match");
    }
    for (int i=0; i<inv_units.length; i++) {
      if ((inv_units[i] == null && ref_units[i] != null) ||
          (inv_units[i] != null && !inv_units[i].equals(ref_units[i]))) {
        throw new CoordinateSystemException("InverseCoordinateSystem: " +
          "Units don't match " + i + " " + inv_units[i] + " " + ref_units[i]);
      }
    }
  }

  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != dimension) {
      throw new CoordinateSystemException("InverseCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    return inverse.fromReference(tuples);
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != dimension) {
      throw new CoordinateSystemException("InverseCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    return inverse.toReference(tuples);
  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != dimension) {
      throw new CoordinateSystemException("InverseCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    return inverse.fromReference(tuples);
  }

  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != dimension) {
      throw new CoordinateSystemException("InverseCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    return inverse.toReference(tuples);
  }

  public boolean equals(Object cs) {
    return (cs instanceof InverseCoordinateSystem &&
            inverse.equals(((InverseCoordinateSystem) cs).inverse));
  }

}

