
//
// EarthVectorType.java
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

package visad;

import java.rmi.*;

/* 5 May 99: TMW says this is the standard.  He also says that
winds not on a grid have components in earth coordinates */

/**
   EarthVectorType is the VisAD data type for 2-D and 3-D wind
   or current vectors in Units convertable with meter / second
   whose first component is parallel to longitude lines, positive
   east, and whose second component is parallel to latitude lines,
   positive north.  It assumes vertical coordinates transform
   nearly flat, so the optional third vertical wind component
   does not transform. <P>
*/
public abstract class EarthVectorType extends RealVectorType {

  public EarthVectorType(RealType[] types) throws VisADException {
    this(types, null);
  }

  public EarthVectorType(RealType[] types, CoordinateSystem coord_sys)
         throws VisADException {
    super(types, coord_sys);
    if (types.length != 2 && types.length != 3) {
      throw new TypeException("EarthVectorType must be 2-D or 3-D: " + types.length);
    }
    for (int i=0; i<types.length; i++) {
      if (!Unit.canConvert(CommonUnit.meterPerSecond, types[i].getDefaultUnit())) {
        throw new TypeException("EarthVectorType components must be convertable " +
                                "with meter / second: " + types[i].getDefaultUnit());
      }
    }
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
  public double[][] transformVectors(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] loc_errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, CoordinateSystem coord_vector,
                        ErrorEstimate[] errors_in,
                        ErrorEstimate[] errors_out,
                        double[][] inloc, double[][] outloc,
                        double[][] value)
         throws VisADException, RemoteException {
    return value;
  }

// ShadowType.java: need to account for spatial setRange scaling in flow

}

