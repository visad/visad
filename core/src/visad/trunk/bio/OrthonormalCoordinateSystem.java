//
// OrthonormalCoordinateSystem.java
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

package visad.bio;

import visad.*;

/**
 * A CoordinateSystem defined by three orthonormal vectors in R^3,
 * that will convert between world coordinates and reference coordinates
 * relative to those vectors.
 */
public class OrthonormalCoordinateSystem extends CoordinateSystem {

  private static int count = 0;

  /** Set of orthonormal vectors defining the coordinate system. */
  private double[] u, v, w;

  /**
   * Constructs a new <CODE>OrthonormalCoordinateSystem</CODE> by
   * deriving an orthonormal basis from the given vectors.
   */
  public OrthonormalCoordinateSystem(double[] v1, double[] v2)
    throws VisADException
  {
    this(new RealTupleType(
      RealType.getRealType("ortho_u" + ++count),
      RealType.getRealType("ortho_v" + count),
      RealType.getRealType("ortho_w" + count)), v1, v2);
  }

  /**
   * Constructs a new <CODE>OrthonormalCoordinateSystem</CODE> for
   * values of the type specified, by deriving an orthonormal basis
   * from the given vectors.
   * @param  type  type of the values
   */
  public OrthonormalCoordinateSystem(RealTupleType type,
    double[] v1, double[] v2) throws VisADException
  {
    super(type, type.getDefaultUnits());

    // construct orthonormal basis
    u = BioUtil.normalize(v1);
    v = BioUtil.normalize(BioUtil.cross(u, v2));
    w = BioUtil.normalize(BioUtil.cross(v2, v));
  }

  /**
   * Converts from reference coordinates to world coordinates.
   * @throws VisADException  values are null or wrong dimension
   */
  public double[][] fromReference(double[][] values) throws VisADException {
    if (values == null || values.length != getDimension()) {
      throw new VisADException("values are null or wrong dimension");
    }
    int len = values[0].length;
    double[][] vals = new double[3][len];
    for (int i=0; i<len; i++) {
      for (int j=0; j<3; j++) {
        vals[j][i] = u[j] * values[0][i] +
          v[j] * values[1][i] +
          w[j] * values[2][i];
      }
    }
    return vals;
  }
      
  /**
   * Converts from world coordinates to reference coordinates.
   * @throws VisADException  values are null or wrong dimension
   */
  public double[][] toReference(double[][] values) throws VisADException {
    if (values == null || values.length != getDimension()) {
      throw new VisADException("values are null or wrong dimension");
    }
    int len = values[0].length;
    double[][] vals = new double[3][len];
    double[] origin = {0, 0, 0};
    for (int i=0; i<len; i++) {
      double[] pt = {values[0][i], values[1][i], values[2][i]};
      double[] q = {BioUtil.project(origin, u, pt)[0],
        BioUtil.project(origin, v, pt)[1],
        BioUtil.project(origin, w, pt)[2]};
      for (int j=0; j<3; j++) vals[j][i] = q[j];
    }
    return vals;
  }

  /**
   * Check to see if the object in question is equal to this.
   * @param  o  object in question
   * @return  true if they are equal, otherwise false.
   */
  public boolean equals(Object o) {
    if (!(o instanceof OrthonormalCoordinateSystem)) return false;
    RealTupleType ref = ((OrthonormalCoordinateSystem) o).getReference();
    return getReference().equals(ref);
  }

}
