//
// CoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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
   CoordinateSystem is the VisAD abstract superclass for coordinate systems for
   vectors in R^n for n>0.  Specific coordinate systems are defined by
   extending this class and providing coordinate transformation logic in
   the toReference and fromReference methods.<P>

   CoordinateSystem objects should be immutable.<P>
*/
public abstract class CoordinateSystem extends Object
       implements java.io.Serializable {

  /** reference coordinate system (e.g., (Latitude, Longitude, Radius) ) */
  private final RealTupleType Reference;

  private final int DomainDimension;

  /** not required to be convertable with Reference.DefaultUnits */
  private final Unit[] CoordinateSystemUnits;

  /**
   * Constructs from the type of the reference coordinate system and units for
   * values in this coordinate system.  Subclasses must supply reference type
   * and units.
   * @param reference           The type of the reference coordinate
   *                            system.  Numeric values in the reference
   *                            coordinate system shall be in units of
   *                            <code>reference.getDefaultUnits()</code> unless
   *                            specified otherwise.
   * @param units               The default units for this coordinate system.
   *                            Numeric values in this coordinate system shall
   *                            be in units of <code>units</code> unless
   *                            specified otherwise.  May be <code>null</code>
   *				or an array of <code>null</code>-s.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public CoordinateSystem(RealTupleType reference, Unit[] units)
         throws VisADException {
    if (reference == null) {
      throw new CoordinateSystemException(
        "CoordinateSystem: Reference may not be null");
    }
    if (reference.getCoordinateSystem() != null) {
      throw new CoordinateSystemException(
        "CoordinateSystem: Reference may not have a DefaultCoordinateSystem");
    }
    Reference = reference;
    DomainDimension = Reference.getDimension();
    if (units != null && DomainDimension != units.length) {
      throw new UnitException("CoordinateSystem: units dimension does not match");
    }
    CoordinateSystemUnits = new Unit[DomainDimension];
    if (units != null) {
      for (int i=0; i<DomainDimension; i++) CoordinateSystemUnits[i] = units[i];
    }
  }

  /** trusted constructor for initializers */
  CoordinateSystem(RealTupleType reference, Unit[] units, boolean b) {
    Reference = reference;
    DomainDimension = Reference.getDimension();
    CoordinateSystemUnits = new Unit[DomainDimension];
    if (units != null) {
      for (int i=0; i<DomainDimension; i++) CoordinateSystemUnits[i] = units[i];
    }
  }

  public RealTupleType getReference() {
    return Reference;
  }

  public int getDimension() {
    return DomainDimension;
  }

  public Unit[] getReferenceUnits() {
    return Reference.getDefaultUnits();
  }

  public Unit[] getCoordinateSystemUnits() {
    return Unit.copyUnitsArray(CoordinateSystemUnits);
  }

  /** convert RealTuple values to Reference coordinates;
      for efficiency, input and output values are passed as
      double[][] arrays rather than RealTuple[] arrays; the array
      organization is double[tuple_dimension][number_of_tuples];
      can modify and return argument array */
  public abstract double[][] toReference(double[][] value) throws VisADException;

  /** convert RealTuple values from Reference coordinates;
      for efficiency, input and output values are passed as
      double[][] arrays rather than RealTuple[] arrays; the array
      organization is double[tuple_dimension][number_of_tuples];
      can modify and return argument array */
  public abstract double[][] fromReference(double[][] value) throws VisADException;

  /** convert RealTuple values to Reference coordinates;
      for efficiency, input and output values are passed as
      double[][] arrays rather than RealTuple[] arrays; the array
      organization is double[tuple_dimension][number_of_tuples];
      can modify and return argument array;
      for efficiency, subclasses should override this implementation */
  public float[][] toReference(float[][] value) throws VisADException {
    double[][] val = Set.floatToDouble(value);
    val = toReference(val);
    return Set.doubleToFloat(val);
  }

  /** convert RealTuple values from Reference coordinates;
      for efficiency, input and output values are passed as
      double[][] arrays rather than RealTuple[] arrays; the array
      organization is double[tuple_dimension][number_of_tuples];
      can modify and return argument array
      for efficiency, subclasses should override this implementation */
  public float[][] fromReference(float[][] value) throws VisADException {
    double[][] val = Set.floatToDouble(value);
    val = fromReference(val);
    return Set.doubleToFloat(val);
  }

  /** transform coordinates between two RealTupleType-s;
      in, coord_in, units_in and errors_in are the Type, CoordinateSystem, Units
      and ErrorEstimates associated with value;
      out, coord_out and units_out and are the target type, CoordinateSystem and
      Units;
      value is the array of values to transform;
      return new value array;
      return transformed ErrorEstimates in errors_out array;
      do Unit conversion even if no coordinate transform needed */
  public static double[][] transformCoordinates(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, ErrorEstimate[] errors_in,
                        double[][] value) throws VisADException {
    int n = out.getDimension();
    Unit[] units_free = new Unit[n];
    value =
      transformCoordinatesFreeUnits(out, coord_out, units_free, errors_out,
                                    in, coord_in, units_in, errors_in, value);

    ErrorEstimate[] sub_errors_out = new ErrorEstimate[1];
    if (errors_out == null) {
      for (int i=0; i<n; i++) {
        value[i] = Unit.transformUnits(units_out[i], sub_errors_out, units_free[i],
                                       null, value[i]);
      }
    }
    else {
      for (int i=0; i<n; i++) {
        value[i] = Unit.transformUnits(units_out[i], sub_errors_out, units_free[i],
                                       errors_out[i], value[i]);
        errors_out[i] = sub_errors_out[0];
      }
    }
    return value;

  }

  /** this is just like Unit.transformCoordinates, except that
      final Unit conversion to units_out is not done;
      rather, units_out[i] is set to the final Unit of value[i] */
  public static double[][] transformCoordinatesFreeUnits(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, ErrorEstimate[] errors_in,
                        double[][] value) throws VisADException {

    int n = in.getDimension();

    // prepare for error calculations, if any
    double[][] error_values = new double[1][1];
    boolean any_transform = false;
    boolean any_errors = false;
    if (errors_in != null && errors_out != null) {
      any_errors = true;
      for (int i=0; i<n; i++) {
        if (errors_in[i] == null) any_errors = false;
      }
    }
    if (errors_out != null) {
      // set default errors_out in case no transform
      if (errors_in != null) {
        for (int i=0; i<n; i++) errors_out[i] = errors_in[i];
      }
      else {
        for (int i=0; i<n; i++) errors_out[i] = null;
      }
    }

    // prepare for Unit calculations
    Unit[] units = Unit.copyUnitsArray(units_in);
    if (units == null) units = new Unit[n];
    Unit[] error_units = Unit.copyUnitsArray(units);;

    if (units_out != null) {
      // set default units_out in case no transform
      for (int i=0; i<n; i++) units_out[i] = units[i];
    }

    if (out.equals(in)) {
      if (coord_in == null && coord_out == null) return value;
      if (coord_in == null || coord_out == null) {
        throw new CoordinateSystemException(
          "CoordinateSystem.transformCoordinates: inconsistency");
      }
      if (!coord_in.equals(coord_out)) {
        if (any_errors) {
          if (!any_transform) {
            error_values = ErrorEstimate.init_error_values(errors_in);
          }
          any_transform = true;
          error_values = coord_in.toReference(error_values, error_units);
          error_values = coord_out.fromReference(error_values, error_units);
        }
        value = coord_in.toReference(value, units);
        value = coord_out.fromReference(value, units);
      }
    }
    else { // !out.equals(in)
      RealTupleType ref_out = out;
      if (coord_out == null) {
        coord_out = out.getCoordinateSystem();
      }
      if (coord_out != null) {
        ref_out = coord_out.getReference();
        // WLH - this check for testing only - may eliminate later
        if (out.getCoordinateSystem() == null ||
            !out.getCoordinateSystem().getReference().equals(ref_out)) {
          throw new CoordinateSystemException(
            "CoordinateSystem.transformCoordinates: out Reference-s don't match");
        }
      }

      RealTupleType ref_in = in;
      if (coord_in == null) {
        coord_in = in.getCoordinateSystem();
      }
      if (coord_in != null) {
        ref_in = coord_in.getReference();
        // WLH - this check for testing only - may eliminate later
        if (in.getCoordinateSystem() == null ||
            !in.getCoordinateSystem().getReference().equals(ref_in)) {
          throw new CoordinateSystemException(
            "CoordinateSystem.transformCoordinates: in Reference-s don't match");
        }
      }

      if (ref_out.equals(ref_in)) {
        if (!in.equals(ref_in)) {
          if (any_errors) {
            if (!any_transform) {
              error_values = ErrorEstimate.init_error_values(errors_in);
            }
            any_transform = true;
            error_values = coord_in.toReference(error_values, error_units);
          }
          value = coord_in.toReference(value, units);
        }
        if (!out.equals(ref_out)) {
          if (any_errors) {
            if (!any_transform) {
              error_values = ErrorEstimate.init_error_values(errors_in);
            }
            any_transform = true;
            error_values = coord_out.fromReference(error_values, error_units);
          }
          value = coord_out.fromReference(value, units);
        }
      }
    }

    // set return Unit-s
    if (units_out != null) {
      for (int i=0; i<n; i++) units_out[i] = units[i];
    }
    // set return ErrorEstimate-s
    if (any_errors && any_transform) {
      for (int i=0; i<n; i++) {
        double error = Math.abs( error_values[i][2 * i + 1] -
                                 error_values[i][2 * i] );
        errors_out[i] = new ErrorEstimate(value[i], error, units_out[i]);
      }
    }
    return value;
  }

  /** float version of transformCoordinates */
  public static float[][] transformCoordinates(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, ErrorEstimate[] errors_in,
                        float[][] value) throws VisADException {
    int n = out.getDimension();
    Unit[] units_free = new Unit[n];
    value =
      transformCoordinatesFreeUnits(out, coord_out, units_free, errors_out,
                                    in, coord_in, units_in, errors_in, value);
    ErrorEstimate[] sub_errors_out = new ErrorEstimate[1];
    double[][] val = Set.floatToDouble(value);
    if (errors_out == null) {
      for (int i=0; i<n; i++) {
        val[i] = Unit.transformUnits(units_out[i], sub_errors_out, units_free[i],
                                     null, val[i]);
      }
    }
    else {
      for (int i=0; i<n; i++) {
        val[i] = Unit.transformUnits(units_out[i], sub_errors_out, units_free[i],
                                     errors_out[i], val[i]);
        errors_out[i] = sub_errors_out[0];
      }
    }
    value = Set.doubleToFloat(val);
    return value;
  }

  /** float version of transformCoordinatesFreeUnits */
  public static float[][] transformCoordinatesFreeUnits(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, ErrorEstimate[] errors_in,
                        float[][] value) throws VisADException {
    return Set.doubleToFloat(
      transformCoordinatesFreeUnits(out, coord_out, units_out, errors_out,
                                    in, coord_in, units_in, errors_in,
                                    Set.floatToDouble(value) ) );
  }

  /** if units are non-null, they are both the Unit[] of input value,
      and a holder for Unit[] of output */
  public double[][] toReference(double[][] value, Unit[] units)
         throws VisADException {
    int n = value.length;
    if (CoordinateSystemUnits != null) {
      for (int i=0; i<n; i++) {
        if (CoordinateSystemUnits[i] != null) {
          value[i] = CoordinateSystemUnits[i].toThis(value[i], units[i]);
        }
      }
    }
    Unit[] us = Reference.getDefaultUnits();
    if (us != null) {
      for (int i=0; i<n; i++) units[i] = us[i];
    }
    else {
      for (int i=0; i<n; i++) units[i] = null;
    }
    return toReference(value);
  }

  /** if units are non-null, they are both the Unit[] of input value,
      and a holder for Unit[] of output */
  public double[][] fromReference(double[][] value, Unit[] units)
         throws VisADException {
    int n = value.length;
    Unit[] us = Reference.getDefaultUnits();
    if (us != null) {
      for (int i=0; i<n; i++) {
        if (us[i] != null) {
          value[i] = us[i].toThis(value[i], units[i]);
        }
      }
    }
    if (CoordinateSystemUnits != null) {
      for (int i=0; i<n; i++) units[i] = CoordinateSystemUnits[i];
    }
    else {
      for (int i=0; i<n; i++) units[i] = null;
    }
    return fromReference(value);
  }

  /** is this needed ???? */
  public boolean checkTable(FlatField table) throws VisADException {
    if (table == null) {
      throw new CoordinateSystemException(
        "CoordinateSystem.checkTable: table is null");
    }
    return
      ((RealTupleType)
        ((FunctionType) table.getType()).getDomain()).getDimension() !=
          DomainDimension ||
      ((RealTupleType)
        ((FunctionType) table.getType()).getRange()).getDimension() !=
          DomainDimension;
  }

  /** check for equality of CoordinateSystem objects;
      must test for cs == null */
  public abstract boolean equals(Object cs);

}

