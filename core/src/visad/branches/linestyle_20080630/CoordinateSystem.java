//
// CoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
   *                            <code>reference.getDefaultUnits()</code>
   *                            unless specified otherwise.
   * @param units               The default units for this coordinate system.
   *                            Numeric values in this coordinate system shall
   *                            be in units of <code>units</code> unless
   *                            specified otherwise.  May be <code>null</code>
   *                            or an array of <code>null</code>s.
   * @throws VisADException     Couldn't create necessary VisAD object.
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

  /**
   * trusted constructor for initializers (does not throw 
   * any declared Exceptions)
   * @param reference           The type of the reference coordinate
   *                            system.  Numeric values in the reference
   *                            coordinate system shall be in units of
   *                            <code>reference.getDefaultUnits()</code>
   *                            unless specified otherwise.
   * @param units               The default units for this coordinate system.
   *                            Numeric values in this coordinate system shall
   *                            be in units of <code>units</code> unless
   *                            specified otherwise.  May be <code>null</code>
   *                            or an array of <code>null</code>s.
   * @param b dummy argument for trusted constructor signature
   */
  CoordinateSystem(RealTupleType reference, Unit[] units, boolean b) {
    Reference = reference;
    DomainDimension = Reference.getDimension();
    CoordinateSystemUnits = new Unit[DomainDimension];
    if (units != null) {
      for (int i=0; i<DomainDimension; i++) CoordinateSystemUnits[i] = units[i];
    }
  }

  /**
   * Return the reference RealTupleType for this CoordinateSystem.
   * @return reference RealTupleType
   */
  public RealTupleType getReference() {
    return Reference;
  }

  /**
   * Return the number of components in the reference RealTupleType.
   * @return dimension of the reference.
   */
  public int getDimension() {
    return DomainDimension;
  }

  /**
   * Return the Units for this CoordinateSystem's reference 
   * RealTupleType.  These are the units of the return values from 
   * {@link #toReference}.
   * @return  copy of the Units array used at construction.
   */
  public Unit[] getReferenceUnits() {
    return Reference.getDefaultUnits();
  }

  /**
   * Return the Units for this CoordinateSystem.  The Units
   * are what's expected for the input data for the 
   * {@link #toReference} method.
   * @return  copy of the Units array used at construction.
   */
  public Unit[] getCoordinateSystemUnits() {
    return Unit.copyUnitsArray(CoordinateSystemUnits);
  }

  /** 
   *  Convert RealTuple values to Reference coordinates;
   *  for efficiency, input and output values are passed as
   *  double[][] arrays rather than RealTuple[] arrays; the array
   *  organization is double[tuple_dimension][number_of_tuples];
   *  can modify and return argument array.
   *  @param  value  array of values assumed to be in coordinateSystem
   *                 units. Input array is not guaranteed to be immutable
   *                 and could be used for return.
   *  @return array of double values in reference coordinates and Units.  
   *  @throws VisADException  if problem with conversion.
   */
  public abstract double[][] toReference(double[][] value) throws VisADException;

  /** 
   *  Convert RealTuple values from Reference coordinates;
   *  for efficiency, input and output values are passed as
   *  double[][] arrays rather than RealTuple[] arrays; the array
   *  organization is double[tuple_dimension][number_of_tuples];
   *  can modify and return argument array.
   *  @param  value  array of values assumed to be in reference
   *                 Units. Input array is not guaranteed to be immutable
   *                 and could be used for return.
   *  @return array of double values in CoordinateSystem Units.  
   *  @throws VisADException  if problem with conversion.
   */
  public abstract double[][] fromReference(double[][] value) throws VisADException;

  /** 
   *  Convert RealTuple values to Reference coordinates;
   *  for efficiency, input and output values are passed as
   *  float[][] arrays rather than RealTuple[] arrays; the array
   *  organization is float[tuple_dimension][number_of_tuples];
   *  can modify and return argument array.  This implementation
   *  converts the input array to doubles and calls {@link
   *  #toReference(double[][])} and then returns that converted 
   *  double array back as a float array.  For efficiency, subclasses 
   *  should override this implementation.
   *  @param  value  array of values assumed to be in coordinateSystem
   *                 units. Input array is not guaranteed to be immutable
   *                 and could be used for return.
   *  @return array of float values in reference coordinates and Units.  
   *  @throws VisADException  if problem with conversion.
   */
  public float[][] toReference(float[][] value) throws VisADException {
    double[][] val = Set.floatToDouble(value);
    val = toReference(val);
    return Set.doubleToFloat(val);
  }

  /** 
   *  Convert RealTuple values from Reference coordinates;
   *  for efficiency, input and output values are passed as
   *  float[][] arrays rather than RealTuple[] arrays; the array
   *  organization is float[tuple_dimension][number_of_tuples];
   *  can modify and return argument array.  This implementation
   *  converts the input array to doubles and calls {@link
   *  #toReference(double[][])} and then returns that converted 
   *  double array back as a float array.  For efficiency, subclasses 
   *  should override this implementation.
   *  @param  value  array of values assumed to be in reference
   *                 Units. Input array is not guaranteed to be immutable
   *                 and could be used for return.
   *  @return array of float values in this CoordinateSystem Units.  
   *  @throws VisADException  if problem with conversion.
   */
  public float[][] fromReference(float[][] value) throws VisADException {
    double[][] val = Set.floatToDouble(value);
    val = fromReference(val);
    return Set.doubleToFloat(val);
  }

  /**
   * Check to see if a conversion can be done between values
   * of one RealTupleType and another given the CoordinateSystems
   * supplied.
   * @param out              The output {@link RealTupleType}.
   * @param coord_out        The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param in               The input {@link RealTupleType}.
   * @param coord_in         The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   *
   * @return true if conversion is possible.
   */
  public static boolean canConvert(RealTupleType out, CoordinateSystem coord_out,
                                   RealTupleType in, CoordinateSystem coord_in) {
    if (out == null) return (in == null);
    if (out.equals(in)) return true;
    RealTupleType ref_out = out;
    if (coord_out != null) ref_out = coord_out.getReference();
    RealTupleType ref_in = in;
    if (coord_in != null) ref_in = coord_in.getReference();
    return ref_out.equals(ref_in);
  }

  /**
   * <p>Transforms double-valued coordinates between two {@link RealTupleType}s.
   * Unit conversion is always performed even if no coordinate transformation
   * is done.</p>
   *
   * <p>This implementation uses {@link #transformCoordinatesFreeUnits} to do
   * most of the transformation.</p>
   *
   * <p>If both {@link RealTupleType}s have a reference coordinate system, then
   * this implementation <em>always</em> transforms the input domain values by
   * first transforming them according to the input reference coordinate system
   * and then inverse transforming them according to the output reference
   * coordinate system -- even if the input and output {@link RealTupleType}s
   * are equal.</p>
   *
   * @param out              The output {@link RealTupleType}.
   * @param coord_out        The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_out        The output units.
   * @param errors_out       The output error estimates or <code>null</code>.
   * @param in               The input {@link RealTupleType}.
   * @param coord_in         The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_in         The input units or <code>null</code>.
   * @param errors_in        The input error estimates or <code>null</code>.
   * @param value            The input coordinate values.  <code>value[i][j]
   *                         </code> is the <code>j</code>-th sample of the
   *                         <code>i</code>-th component.  The values might
   *                         be modified upon return from this method.
   * @return                 The transformed coordinate values not in the input
   *                         array.
   * @throws VisADException  if a VisAD failure occurs.
   * @throws NullPointerException if <code>units_out</code> is 
   *                         <code>null</code>.
   */
  public static double[][] transformCoordinates(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, ErrorEstimate[] errors_in,
                        double[][] value) throws VisADException {
    return transformCoordinates(out, coord_out, units_out, errors_out,
                                in, coord_in, units_in, errors_in,
                                value, true);
  }

  /**
   * <p>Transforms double-valued coordinates between two {@link RealTupleType}s.
   * Unit conversion is always performed even if no coordinate transformation
   * is done.</p>
   *
   * <p>This implementation uses {@link #transformCoordinatesFreeUnits} to do
   * most of the transformation.</p>
   *
   * <p>If both {@link RealTupleType}s have a reference coordinate system, then
   * this implementation <em>always</em> transforms the input domain values by
   * first transforming them according to the input reference coordinate system
   * and then inverse transforming them according to the output reference
   * coordinate system -- even if the input and output {@link RealTupleType}s
   * are equal.</p>
   *
   * @param out              The output {@link RealTupleType}.
   * @param coord_out        The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_out        The output units.
   * @param errors_out       The output error estimates or <code>null</code>.
   * @param in               The input {@link RealTupleType}.
   * @param coord_in         The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_in         The input units or <code>null</code>.
   * @param errors_in        The input error estimates or <code>null</code>.
   * @param value            The input coordinate values.  <code>value[i][j]
   *                         </code> is the <code>j</code>-th sample of the
   *                         <code>i</code>-th component.  The values might
   *                         be modified upon return from this method.
   * @param copy             if false, the underlying <code>value</code> array
   *                         transformations may be done in place.
   * @return                 The transformed coordinate values not in the input
   *                         array, if copy is true.
   * @throws VisADException  if a VisAD failure occurs.
   * @throws NullPointerException if <code>units_out</code> is 
   *                         <code>null</code>.
   */
  public static double[][] transformCoordinates(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, ErrorEstimate[] errors_in,
                        double[][] value, boolean copy) throws VisADException {
    int n = out.getDimension();
    Unit[] units_free = new Unit[n];
    double[][] old_value = value;
    value =
      transformCoordinatesFreeUnits(out, coord_out, units_free, errors_out,
                                    in, coord_in, units_in, errors_in, value);
    // WLH 21 Nov 2001
    if (value == old_value) {
      value = new double[n][];
      for (int i=0; i<n; i++) value[i] = old_value[i];
    }

    ErrorEstimate[] sub_errors_out = new ErrorEstimate[1];
    if (errors_out == null) {
      for (int i=0; i<n; i++) {
        value[i] = Unit.transformUnits(units_out[i], sub_errors_out, units_free[i],
                                       null, value[i], copy);
      }
    }
    else {
      for (int i=0; i<n; i++) {
        value[i] = Unit.transformUnits(units_out[i], sub_errors_out, units_free[i],
                                       errors_out[i], value[i], copy);
        errors_out[i] = sub_errors_out[0];
      }
    }
    return value;
  }

  /**
   * <p>Transforms double-valued coordinates between two {@link RealTupleType}s.
   * This is just like {@link #transformCoordinates}, except that final Unit
   * conversion to <code>units_out</code> is not done; rather, 
   * <code>units_out[i]</code> is set to the final {@link Unit} of 
   * <code>value[i]</code>.</p>
   *
   * <p>If both {@link RealTupleType}s have a reference coordinate system, then
   * this implementation <em>always</em> transforms the input domain values by
   * first transforming them according to the input reference coordinate system
   * and then inverse transforming them according to the output reference
   * coordinate system -- even if the input and output {@link RealTupleType}s
   * are equal.</p>
   *
   * @param out              The output {@link RealTupleType}.
   * @param coord_out        The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_out        The output units or <code>null</code>.
   * @param errors_out       The output error estimates or <code>null</code>.
   * @param in               The input {@link RealTupleType}.
   * @param coord_in         The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_in         The input units or <code>null</code>.
   * @param errors_in        The input error estimates or <code>null</code>.
   * @param value            The input coordinate values.  <code>value[i][j]
   *                         </code> is the <code>j</code>-th sample of the
   *                         <code>i</code>-th component.
   * @return                 The transformed coordinate values. Possibly
   *                         in the input array.
   * @throws VisADException  if a VisAD failure occurs.
   */
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
    Unit[] error_units = Unit.copyUnitsArray(units);

    if (units_out != null) {
      // set default units_out in case no transform
      for (int i=0; i<n; i++) units_out[i] = units[i];
    }

    // WLH 28 March 2000
    // ensure coord_out and coord_in include any RealTupleType defaults
    if (coord_out == null) coord_out = out.getCoordinateSystem();
    if (coord_in == null) coord_in = in.getCoordinateSystem();

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
      if (coord_out != null) {
        ref_out = coord_out.getReference();
        // WLH - this check for testing only - may eliminate later
        if (out.getCoordinateSystem() == null ||
            !out.getCoordinateSystem().getReference().equals(ref_out)) {
          throw new CoordinateSystemException(
            "CoordinateSystem.transformCoordinates: out References don't match");
        }
      }

      RealTupleType ref_in = in;
      if (coord_in != null) {
        ref_in = coord_in.getReference();
        // WLH - this check for testing only - may eliminate later
        if (in.getCoordinateSystem() == null ||
            !in.getCoordinateSystem().getReference().equals(ref_in)) {
          throw new CoordinateSystemException(
            "CoordinateSystem.transformCoordinates: in References don't match");
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
      else { // !(ref_out.equals(ref_in)
// WLH 4 July 2000 - should throw an Exception here -
//                   but breaks too many things, so don't do it
      }
    } // end if (!out.equals(in))

    // set return Units
    if (units_out != null) {
      for (int i=0; i<n; i++) units_out[i] = units[i];
    }
    // set return ErrorEstimates
    if (any_errors && any_transform) {
      for (int i=0; i<n; i++) {
        double error = Math.abs( error_values[i][2 * i + 1] -
                                 error_values[i][2 * i] );
        errors_out[i] = new ErrorEstimate(value[i], error, units_out[i]);
      }
    }
    return value;
  }

  /**
   * <p>Transforms float-valued coordinates between two {@link RealTupleType}s.
   * Unit conversion is always performed even if no coordinate transformation
   * is done.</p>
   *
   * <p>This implementation uses {@link #transformCoordinatesFreeUnits} to do
   * most of the transformation.</p>
   *
   * <p>If both {@link RealTupleType}s have a reference coordinate system, then
   * this implementation <em>always</em> transforms the input domain values by
   * first transforming them according to the input reference coordinate system
   * and then inverse transforming them according to the output reference
   * coordinate system -- even if the input and output {@link RealTupleType}s
   * are equal.</p>
   *
   * @param out              The output {@link RealTupleType}.
   * @param coord_out        The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_out        The output units.
   * @param errors_out       The output error estimates or <code>null</code>.
   * @param in               The input {@link RealTupleType}.
   * @param coord_in         The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_in         The input units or <code>null</code>.
   * @param errors_in        The input error estimates or <code>null</code>.
   * @param value            The input coordinate values.  <code>value[i][j]
   *                         </code> is the <code>j</code>-th sample of the
   *                         <code>i</code>-th component.  The values might
   *                         be modified upon return from this method.
   * @return                 The transformed coordinate values not in the input
   *                         array.
   * @throws VisADException  if a VisAD failure occurs.
   * @throws NullPointerException if <code>units_out</code> is 
   *                         <code>null</code>.
   */
  public static float[][] transformCoordinates(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, ErrorEstimate[] errors_in,
                        float[][] value) throws VisADException {
    return transformCoordinates(out, coord_out, units_out, errors_out,
                                in, coord_in, units_in, errors_in,
                                value, true);
  }


  /**
   * <p>Transforms float-valued coordinates between two {@link RealTupleType}s.
   * Unit conversion is always performed even if no coordinate transformation
   * is done.</p>
   *
   * <p>This implementation uses {@link #transformCoordinatesFreeUnits} to do
   * most of the transformation.</p>
   *
   * <p>If both {@link RealTupleType}s have a reference coordinate system, then
   * this implementation <em>always</em> transforms the input domain values by
   * first transforming them according to the input reference coordinate system
   * and then inverse transforming them according to the output reference
   * coordinate system -- even if the input and output {@link RealTupleType}s
   * are equal.</p>
   *
   * @param out              The output {@link RealTupleType}.
   * @param coord_out        The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_out        The output units.
   * @param errors_out       The output error estimates or <code>null</code>.
   * @param in               The input {@link RealTupleType}.
   * @param coord_in         The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_in         The input units or <code>null</code>.
   * @param errors_in        The input error estimates or <code>null</code>.
   * @param value            The input coordinate values.  <code>value[i][j]
   *                         </code> is the <code>j</code>-th sample of the
   *                         <code>i</code>-th component.  The values might
   *                         be modified upon return from this method.
   * @param copy             if false, the underlying <code>value</code> array
   *                         transformations may be done in place.
   * @return                 The transformed coordinate values not in the input
   *                         array, if copy is true.
   * @throws VisADException  if a VisAD failure occurs.
   * @throws NullPointerException if <code>units_out</code> is 
   *                         <code>null</code>.
   */
  public static float[][] transformCoordinates(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, ErrorEstimate[] errors_in,
                        float[][] value, boolean copy) throws VisADException {

    int n = out.getDimension();
    Unit[] units_free = new Unit[n];
    float[][] old_value = value;
    value =
      transformCoordinatesFreeUnits(out, coord_out, units_free, errors_out,
                                    in, coord_in, units_in, errors_in, value);
    // WLH 21 Nov 2001
    if (value == old_value) {
      value = new float[n][];
      for (int i=0; i<n; i++) value[i] = old_value[i];
    }

    ErrorEstimate[] sub_errors_out = new ErrorEstimate[1];
    if (errors_out == null) {
      for (int i=0; i<n; i++) {
        value[i] = Unit.transformUnits(units_out[i], sub_errors_out, units_free[i],
                                       null, value[i], copy);
      }
    }
    else {
      for (int i=0; i<n; i++) {
        value[i] = Unit.transformUnits(units_out[i], sub_errors_out, units_free[i],
                                       errors_out[i], value[i], copy);
        errors_out[i] = sub_errors_out[0];
      }
    }
    return value;
  }

  /**
   * <p>Transforms float-valued coordinates between two {@link RealTupleType}s.
   * This is just like {@link #transformCoordinates}, except that final Unit
   * conversion to <code>units_out</code> is not done; rather, 
   * <code>units_out[i]</code> is set to the final {@link Unit} of 
   * <code>value[i]</code>.</p>
   *
   * <p>If both {@link RealTupleType}s have a reference coordinate system, then
   * this implementation <em>always</em> transforms the input domain values by
   * first transforming them according to the input reference coordinate system
   * and then inverse transforming them according to the output reference
   * coordinate system -- even if the input and output {@link RealTupleType}s
   * are equal.</p>
   *
   * @param out              The output {@link RealTupleType}.
   * @param coord_out        The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_out        The output units or <code>null</code>.
   * @param errors_out       The output error estimates or <code>null</code>.
   * @param in               The input {@link RealTupleType}.
   * @param coord_in         The coordinate system transformation associated
   *                         with the output {@link RealTupleType} or <code>
   *                         null</code>.
   * @param units_in         The input units or <code>null</code>.
   * @param errors_in        The input error estimates or <code>null</code>.
   * @param value            The input coordinate values.  <code>value[i][j]
   *                         </code> is the <code>j</code>-th sample of the
   *                         <code>i</code>-th component.
   * @return                 The transformed coordinate values. Possibly
   *                         in the input array.
   * @throws VisADException  if a VisAD failure occurs.
   */
  public static float[][] transformCoordinatesFreeUnits(
                        RealTupleType out, CoordinateSystem coord_out,
                        Unit[] units_out, ErrorEstimate[] errors_out,
                        RealTupleType in, CoordinateSystem coord_in,
                        Unit[] units_in, ErrorEstimate[] errors_in,
                        float[][] value) throws VisADException {
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
    Unit[] error_units = Unit.copyUnitsArray(units);

    if (units_out != null) {
      // set default units_out in case no transform
      for (int i=0; i<n; i++) units_out[i] = units[i];
    }

    // WLH 28 March 2000
    // ensure coord_out and coord_in include any RealTupleType defaults
    if (coord_out == null) coord_out = out.getCoordinateSystem();
    if (coord_in == null) coord_in = in.getCoordinateSystem();

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
      if (coord_out != null) {
        ref_out = coord_out.getReference();
        // WLH - this check for testing only - may eliminate later
        if (out.getCoordinateSystem() == null ||
            !out.getCoordinateSystem().getReference().equals(ref_out)) {
          throw new CoordinateSystemException(
            "CoordinateSystem.transformCoordinates: out References don't match");
        }
      }

      RealTupleType ref_in = in;
      if (coord_in != null) {
        ref_in = coord_in.getReference();
        // WLH - this check for testing only - may eliminate later
        if (in.getCoordinateSystem() == null ||
            !in.getCoordinateSystem().getReference().equals(ref_in)) {
          throw new CoordinateSystemException(
            "CoordinateSystem.transformCoordinates: in References don't match");
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
      else { // !(ref_out.equals(ref_in)
// WLH 4 July 2000 - should throw an Exception here -
//                   but breaks too many things, so don't do it
      }
    } // end if (!out.equals(in))

    // set return Units
    if (units_out != null) {
      for (int i=0; i<n; i++) units_out[i] = units[i];
    }
    // set return ErrorEstimates
    if (any_errors && any_transform) {
      for (int i=0; i<n; i++) {
        double error = Math.abs( error_values[i][2 * i + 1] -
                                 error_values[i][2 * i] );
        errors_out[i] = new ErrorEstimate(value[i], error, units_out[i]);
      }
    }
    return value;
  }

  /** 
   *  Convert values in Units specified to Reference coordinates.
   *  If units are non-null, they are both the Unit[] of input value,
   *  and a holder for Unit[] of output.  
   *  @param  value  array of values assumed to be in the Units
   *                 specified or CoordinateSystem units if null.
   *  @param  units  Units of input values.  If non-null, input values
   *                 are converted to CoordinateSystem Units (if they
   *                 are non-null) before calling 
   *                 {@link #toReference(double[][])}.
   *  @return array of double values in reference coordinates and Units.  
   *  @throws VisADException  if problem with conversion.
   */
  public double[][] toReference(double[][] value, Unit[] units)
         throws VisADException {
    int n = value.length;
    if (CoordinateSystemUnits != null) {
      for (int i=0; i<n; i++) {
        if (CoordinateSystemUnits[i] != null) {
          if (units[i] != null && !CoordinateSystemUnits[i].equals(units[i])) {
            value[i] = CoordinateSystemUnits[i].toThis(value[i], units[i], false);
          }
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

  /** 
   *  Convert values in Units specified to Reference coordinates.
   *  If units are non-null, they are both the Unit[] of input value,
   *  and a holder for Unit[] of output.  
   *  @param  value  array of values assumed to be in the Units
   *                 specified or CoordinateSystem units if null.
   *  @param  units  Units of input values.  If non-null, input values
   *                 are converted to CoordinateSystem Units (if they
   *                 are non-null) before calling 
   *                 {@link #toReference(float[][])}.
   *  @return array of float values in reference coordinates and Units.  
   *  @throws VisADException  if problem with conversion.
   */
  public float[][] toReference(float[][] value, Unit[] units)
         throws VisADException {
    int n = value.length;
    if (CoordinateSystemUnits != null) {
      for (int i=0; i<n; i++) {
        if (CoordinateSystemUnits[i] != null) {
          if (units[i] != null && !CoordinateSystemUnits[i].equals(units[i])) {
            value[i] = CoordinateSystemUnits[i].toThis(value[i], units[i], false);
          }
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

  /** 
   *  Convert values in Units specified to this CoordinateSystem's
   *  Units. If units are non-null, they are both the Unit[] of input value,
   *  and a holder for Unit[] of output.  
   *  @param  value  array of values assumed to be in the Units
   *                 specified or Reference units if null.
   *  @param  units  Units of input values.  If non-null, input values
   *                 are converted to Reference Units (if they
   *                 are non-null) before calling 
   *                 {@link #fromReference(double[][])}.
   *  @return array of double values in CoordinateSystem Units.  
   *  @throws VisADException  if problem with conversion.
   */
  public double[][] fromReference(double[][] value, Unit[] units)
         throws VisADException {
    int n = value.length;
    Unit[] us = Reference.getDefaultUnits();
    if (us != null) {
      for (int i=0; i<n; i++) {
        if (us[i] != null) {
          if (units[i] != null && !us[i].equals(units[i])) {
            value[i] = us[i].toThis(value[i], units[i], false);
          }
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

  /** 
   *  Convert values in Units specified to this CoordinateSystem's
   *  Units. If units are non-null, they are both the Unit[] of input value,
   *  and a holder for Unit[] of output.  
   *  @param  value  array of values assumed to be in the Units
   *                 specified or Reference units if null.
   *  @param  units  Units of input values.  If non-null, input values
   *                 are converted to Reference Units (if they
   *                 are non-null) before calling 
   *                 {@link #fromReference(float[][])}.
   *  @return array of float values in CoordinateSystem Units.  
   *  @throws VisADException  if problem with conversion.
   */
  public float[][] fromReference(float[][] value, Unit[] units)
         throws VisADException {
    int n = value.length;
    Unit[] us = Reference.getDefaultUnits();
    if (us != null) {
      for (int i=0; i<n; i++) {
        if (us[i] != null) {
          if (units[i] != null && !us[i].equals(units[i])) {
            value[i] = us[i].toThis(value[i], units[i], false);
          }
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

  /**
   * Indicates whether or not this instance is equal to an object
   * (note must test for cs == null).
   * @param cs the object in question.
   * @return <code>true</code> if and only if this instance equals cs.
   */
  public abstract boolean equals(Object cs);

}

