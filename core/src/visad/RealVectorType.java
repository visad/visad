//
// RealVectorType.java
//

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

package visad;

import java.rmi.*;

/**
   RealVectorType is the VisAD data type for vector field tuple
   in R^n, for n>0.<P>
*/
public abstract class RealVectorType extends RealTupleType {

  public RealVectorType(RealType[] types) throws VisADException {
    super(types);
  }

  public RealVectorType(RealType[] types, CoordinateSystem coord_sys)
         throws VisADException {
    super(types, coord_sys, null);
  }

  public RealVectorType(RealType a) throws VisADException {
    super(a);
  }

  public RealVectorType(RealType a, RealType b) throws VisADException {
    super(a, b);
  }

  public RealVectorType(RealType a, RealType b, RealType c)
         throws VisADException {
    super(a, b, c);
  }

  public RealVectorType(RealType a, RealType b, RealType c, RealType d)
         throws VisADException {
    super(a, b, c, d);
  }

  /** transform an array of vector values from a field, based on a
      coordinate transform of the field domain.  This may use the
      Jacobean of the coordinate transform, but may be more complex.
      For example, vectors in m/s would not transform for a simple
      rescaling transform.  Or the transform may be to a moving
      coordinate system.

      out, coord_out, units_out, in, coord_in, units_in are the
      arguments to the corresponding call to transformCoordinates;
      loc_errors_out are the ErrorEstimates for loc from that call;
      inloc and outloc contain the input and output values from the
      corresponding call to transformCoordinates;
      coord_vector and errors_in are the CoordinateSystem and ErrorEstimates
      associated with values;
      value are the vector values (already resampled at loc);
      return new value array;
      return transformed ErrorEstimates in errors_out array */
  public abstract double[][] transformVectors(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] loc_errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, CoordinateSystem coord_vector,
                        ErrorEstimate[] errors_in,
                        ErrorEstimate[] errors_out,
                        double[][] inloc, double[][] outloc,
                        double[][] value)
         throws VisADException, RemoteException;

  public float[][] transformVectors(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] loc_errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, CoordinateSystem coord_vector,
                        ErrorEstimate[] errors_in,
                        ErrorEstimate[] errors_out,
                        float[][] inloc, float[][] outloc,
                        float[][] value)
         throws VisADException, RemoteException {
    double[][] dinloc = Set.floatToDouble(inloc);
    double[][] doutloc = Set.floatToDouble(outloc);
    double[][] dvalue = Set.floatToDouble(value);
    dvalue = transformVectors(out, coord_out, units_out, loc_errors_out,
                              in, coord_in, units_in, coord_vector,
                              errors_in, errors_out, dinloc, doutloc,
                              dvalue);
    return Set.doubleToFloat(dvalue);
  }

  /** transform a single vector in a RealTuple, based on a coordinate
      transform of the field domain.  Similar to the previous
      definition of transformVectors. */
  public RealTuple transformVectors(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] loc_errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, CoordinateSystem coord_vector,
                        double[][] inloc, double[][] outloc,
                        RealTuple tuple)
         throws VisADException, RemoteException {
    if (!tuple.getType().equals(this)) {
      throw new TypeException("RealVectorType.transformVectors");
    }
    int n = getDimension();
    double[][] value = new double[n][1];
    ErrorEstimate[] errors_in = new ErrorEstimate[n];
    for (int j=0; j<n; j++) {
      value[j][0] = ((Real) tuple.getComponent(j)).getValue();
      errors_in[j] = ((Real) tuple.getComponent(j)).getError();
    }
    ErrorEstimate[] errors_out = new ErrorEstimate[n];
    value = transformVectors(out, coord_out, units_out, loc_errors_out,
                             in, coord_in, units_in, coord_vector,
                             errors_in, errors_out, inloc, outloc, value);
    double[] vals = new double[n];
    Real[] reals = new Real[n];
    for (int j=0; j<n; j++) {
      reals[j] = new Real(
        (RealType) ((Real) tuple.getComponent(j)).getType(),
        value[j][0],
        ((Real) tuple.getComponent(j)).getUnit(),
        errors_out[j]);
    }
    return new RealTuple((RealTupleType) tuple.getType(), reals,
                         tuple.getCoordinateSystem());
  }

  public RealTuple transformVectors(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] loc_errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, CoordinateSystem coord_vector,
                        float[][] inloc, float[][] outloc, RealTuple tuple)
         throws VisADException, RemoteException {
    double[][] dinloc = Set.floatToDouble(inloc);
    double[][] doutloc = Set.floatToDouble(outloc);
    return transformVectors(out, coord_out, units_out, loc_errors_out,
                            in, coord_in, units_in, coord_vector,
                            dinloc, doutloc, tuple);
  }

}

