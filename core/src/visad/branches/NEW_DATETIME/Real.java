
//
// Real.java
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

/**
   Real is the class of VisAD scalar data for real numbers represented
   as double precision floating point values.  Double.NaN is used to
   indicate missing values, because it has the appropriate arithmetic
   semantics.  Real objects are immutable.<P>
*/
public class Real extends Scalar {

  private final double Value;
  private final Unit unit;
  private final ErrorEstimate Error;

  /**
   * Constructs a Real object.  This is the most general constructor.
   * @param type		The type of the Real.
   * @param value		The value of the Real.  May be 
   *				<code>Double.NaN</code>.
   * @param u			The unit of the Real.  May be <code>null</code>.
   *				If non-<code>null</code> and 
   *				<code>type.isInterval()</code> returns true,
   *				then the unit will actually be
   *				<code>u.getAbsoluteUnit()</code>.
   a @param error		Error estimate of the Real.  May be 
   *				<code>null</code>.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public Real(RealType type, double value, Unit u, ErrorEstimate error)
         throws VisADException {
    super(type);
    if (!Unit.canConvert(u, type.getDefaultUnit())) {
      throw new UnitException("Real: Unit must be convertable with " +
                              "Type default Unit");
    }
    unit = u != null && type.isInterval() ? u.getAbsoluteUnit() : u;
    Value = value;
    Error = Double.isNaN(value) ? null : error;
  }

  /**
   * Constructs a Real object.  The error estimate will be based on a numeric
   * value.
   * @param type		The type of the Real.
   * @param value		The value of the Real.  May be 
   *				<code>Double.NaN</code>.
   * @param u			The unit of the Real.  May be <code>null</code>.
   *				If non-<code>null</code> and 
   *				<code>type.isInterval()</code> returns true,
   *				then the unit will actually be
   *				<code>u.getAbsoluteUnit()</code>.
   * @param error		Value for constructing an error estimate for the
   *				Real in units of <code>u != null &&
   *				type.isInterval() ? u.getAbsoluteUnit() :
   *				u</code>.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public Real(RealType type, double value, Unit u, double error)
         throws VisADException {
    this(type, value, u,
      new ErrorEstimate(value, Math.abs(error),
	u != null && type.isInterval() ? u.getAbsoluteUnit() : u));
  }

  /**
   * Constructs a Real object.  The error estimate will be <code>null</code>.
   * @param type		The type of the Real.
   * @param value		The value of the Real.  May be 
   *				<code>Double.NaN</code>.
   * @param u			The unit of the Real.  May be <code>null</code>.
   *				If non-<code>null</code> and 
   *				<code>type.isInterval()</code> returns true,
   *				then the unit will actually be
   *				<code>u.getAbsoluteUnit()</code>.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public Real(RealType type, double value, Unit u)
         throws VisADException {
    this(type, value, u, null);
  }

  /**
   * Constructs a Real object.  The unit of the Real will be the default unit of
   * the RealType and the error estimate will be <code>null</code>.
   * @param type		The type of the Real.
   * @param value		The value of the Real in units of
   *				<code>type.getDefaultUnit()</code>.  May be 
   *				<code>Double.NaN</code>.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public Real(RealType type, double value) {
    this(type, value, type.getDefaultUnit(), null, true);
  }

  /**
   * Constructs a Real object.  The value will be missing, the unit of the
   * Real will be the default unit of the RealType, and the error estimate
   * will be <code>null</code>.
   * @param type		The type of the Real.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public Real(RealType type) {
    this(type, Double.NaN, type.getDefaultUnit(), null, true);
  }

  /**
   * Constructs a generic Real object.  The RealType of the Real will be
   * <code>RealType.Generic</code>, the unit of the Real will be
   * <code>RealType.Generic.getDefaultUnit()</code>, and the error estimate
   * will be based on a numeric value.
   * @param value		The value of the Real.  May be 
   *				<code>Double.NaN</code>.
   * @param error		Value for constructing an error estimate for the
   *				Real in units of
   *				<code>RealType.Generic.getDefaultUnit()</code>.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public Real(double value, double error) {
    this(RealType.Generic, value, RealType.Generic.getDefaultUnit(),
         new ErrorEstimate(value, Math.abs(error), RealType.Generic.getDefaultUnit()),
         true);
  }

  /**
   * Constructs a generic Real object.  The RealType of the Real will be 
   * <code>RealType.Generic</code>, the unit of the Real will be
   * <code>RealType.Generic.getDefaultUnit()</code>, and the error estimate
   * will be 0.0.
   * @param value		The value of the Real.  May be 
   *				<code>Double.NaN</code>.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public Real(double value) {
    this(RealType.Generic, value, RealType.Generic.getDefaultUnit(),
         new ErrorEstimate(value, 0.0, RealType.Generic.getDefaultUnit()), true);
  }

  /** trusted constructor for clone and other constructors */
  private Real(RealType type, double value, Unit u, ErrorEstimate error,
               boolean b) {
    super(type);
    unit = u != null && type.isInterval() ? u.getAbsoluteUnit() : u;
    Value = value;
    Error = Double.isNaN(value) ? null : error;
  }

  public final double getValue() {
    return Value;
  }

  /** get double value converted to unit_out */
  public final double getValue(Unit unit_out) throws VisADException {
    if (unit_out == null) {
      if (unit != null) {
        throw new UnitException("Real.getValue: illegal Unit conversion");
      }
      return Value;
    }
    else {
      return unit_out.toThis(Value, unit);
    }
  }

  public boolean isMissing() {
    // note inf and -inf have proper semantics and are not missing
    return (Double.isNaN(Value));
  }

/*- TDR  May 1998
  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
 */

  public Data binary(Data data, int op, MathType new_type,
                     int sampling_mode, int error_mode )
              throws VisADException, RemoteException {
    /*- TDR May 1998 */
    if ( new_type == null ) {
      throw new TypeException("binary: new_type may not be null");
    }
    /*- end */
    if (data instanceof Real) {

  /*- TDR May 28 1998 */
      if ( !(new_type instanceof RealType) ) {
        throw new TypeException("binary: new_type doesn't match return type");
      }
  /*- end */

      Unit u;	// output unit
      Unit data_unit = ((Real) data).getUnit();
      double thatValue = ((Real) data).getValue();
      double thisValue = Value;
      double opValue;
      ErrorEstimate dError = ((Real) data).getError();
      switch (op) {
        case ADD:
        case SUBTRACT:
        case INV_SUBTRACT:
        case MAX:
        case MIN:
          if (unit == null || data_unit == null) {
            u = null;
          }
          else if (unit == CommonUnit.promiscuous) {
            u = data_unit;
          }
          else if (data_unit == CommonUnit.promiscuous) {
            u = unit;
          }
          else {
	    try {
	      u = unit.getAbsoluteUnit();
	      thisValue = u.toThis(thisValue, unit);
	      thatValue = u.toThis(thatValue, data_unit);
	      // scale data.ErrorEstimate for Unit.toThis
	      if (error_mode != NO_ERRORS && dError != null) {
		Unit	errorUnit = dError.getUnit();
		if (errorUnit == null)
		  errorUnit = data_unit;
		double new_error = u.toThis(dError.getErrorValue(), errorUnit);
		dError = new ErrorEstimate(thatValue, new_error, u);
	      }
	    }
	    catch (UnitException e) {		// inconvertible units
	      u = null;
	    }
          }
          switch (op) {
            case ADD:
              opValue = thisValue + thatValue;
              break;
            case SUBTRACT:
              opValue = thisValue - thatValue;
              break;
            case INV_SUBTRACT:
              opValue = thatValue - thisValue;
              break;
            case MAX:
              opValue = Math.max(thisValue, thatValue);
              break;
            case MIN:
            default:
              opValue = Math.min(thisValue, thatValue);
              break;
          }
          break;
        case MULTIPLY:
          opValue = thisValue * thatValue;
          if (unit == null || data_unit == null) {
            u = null;
          }
          else {
            u = unit.multiply(data_unit);
          }
          break;
        case DIVIDE:
          opValue = thisValue / thatValue;
          if (unit == null || data_unit == null) {
            u = null;
          }
          else {
            u = unit.divide(data_unit);
          }
          break;
        case INV_DIVIDE:
          opValue = thatValue / thisValue;
          if (unit == null || data_unit == null) {
            u = null;
          }
          else {
            u = data_unit.divide(unit);
          }
          break;
        case POW:
          opValue = Math.pow(thisValue, thatValue);
          u = null;
          break;
        case INV_POW:
          opValue = Math.pow(thatValue, thisValue);
          u = null;
          break;
        case ATAN2:
          opValue = Math.atan2(thisValue, thatValue);
          u = CommonUnit.radian;
          break;
        case ATAN2_DEGREES:
          opValue = Data.RADIANS_TO_DEGREES * Math.atan2(thisValue, thatValue);
          u = CommonUnit.degree;
          break;
        case INV_ATAN2:
          opValue = Math.atan2(thatValue, thisValue);
          u = CommonUnit.radian;
          break;
        case INV_ATAN2_DEGREES:
          opValue = Data.RADIANS_TO_DEGREES * Math.atan2(thatValue, thisValue);
          u = CommonUnit.degree;
          break;
        case REMAINDER:
          opValue = thisValue % thatValue;
          u = unit;
          break;
        case INV_REMAINDER:
          opValue = thatValue % thisValue;
          u = data_unit;
          break;
        default:
          throw new ArithmeticException("Real.binary: illegal operation");
      }
      if (error_mode == NO_ERRORS || Error == null || dError == null) {
        return new Real(((RealType) new_type), opValue, u, null);
      }
      else {
        return new Real(((RealType) new_type), opValue, u,
                   new ErrorEstimate(opValue, u, op, Error, dError, error_mode));
      }
    }
    else if (data instanceof Text) {
      throw new TypeException("Real.binary: types don't match");
    }
    else if (data instanceof Tuple) {
      /* BINARY - TDR May 28, 1998
      return data.binary(this, invertOp(op), sampling_mode, error_mode);
      */
      /* BINARY - TDR June 5, 1998 */
      if ( !(data.getType()).equalsExceptName(new_type) ) {
        throw new TypeException();
      }
      return data.binary(this, invertOp(op), new_type, sampling_mode, error_mode);
      /* BINARY - end  */
    }
    else if (data instanceof Field) {
      /* BINARY - TDR May 28, 1998
      return data.binary(this, invertOp(op), sampling_mode, error_mode);
      */
      /* BINARY - TDR June 5, 1998 */
      if ( !(data.getType()).equalsExceptName(new_type) ) {
        throw new TypeException();
      }
      return data.binary(this, invertOp(op), new_type, sampling_mode, error_mode);
      /* BINARY - end  */
    }
    else {
      throw new TypeException("Real.binary");
    }
  }

  /** unary function on a Real; override some trig functions based
      on Unit; transcental functions destroy Unit */
  public Data unary(int op, MathType new_type, int sampling_mode, int error_mode)
              throws VisADException {
    double value;
    Unit u;
    /*- TDR  June 1998  */
    if ( new_type == null ) {
      throw new TypeException("unary: new_type may not be null");
    }
    switch (op) {
      case ABS:
        value = Math.abs(Value);
        u = unit;
        break;
      case ACOS:
        value = Math.acos(Value);
        u = CommonUnit.radian;
        break;
      case ACOS_DEGREES:
        value = Data.RADIANS_TO_DEGREES * Math.acos(Value);
        u = CommonUnit.degree;
        break;
      case ASIN:
        value = Math.asin(Value);
        u = CommonUnit.radian;
        break;
      case ASIN_DEGREES:
        value = Data.RADIANS_TO_DEGREES * Math.asin(Value);
        u = CommonUnit.degree;
        break;
      case ATAN:
        value = Math.atan(Value);
        u = CommonUnit.radian;
        break;
      case ATAN_DEGREES:
        value = Data.RADIANS_TO_DEGREES * Math.atan(Value);
        u = CommonUnit.degree;
        break;
      case CEIL:
        value = Math.ceil(Value);
        u = unit;
        break;
      case COS:
        // do cos in radians, unless unit is degrees
        value = unit.equals(CommonUnit.degree) ?
                Math.cos(Data.DEGREES_TO_RADIANS * Value) : Math.cos(Value);
        u = CommonUnit.dimensionless.equals(unit) ? unit : null;
        break;
      case COS_DEGREES:
        // do cos in degrees, unless unit is radians
        value = unit.equals(CommonUnit.radian) ?
                Math.cos(Value) : Math.cos(Data.DEGREES_TO_RADIANS * Value);
        u = CommonUnit.dimensionless.equals(unit) ? unit : null;
        break;
      case EXP:
        value = Math.exp(Value);
        u = CommonUnit.dimensionless.equals(unit) ? unit : null;
        break;
      case FLOOR:
        value = Math.floor(Value);
        u = unit;
        break;
      case LOG:
        value = Math.log(Value);
        u = CommonUnit.dimensionless.equals(unit) ? unit : null;
        break;
      case RINT:
        value = Math.rint(Value);
        u = unit;
        break;
      case ROUND:
        value = Math.round(Value);
        u = unit;
        break;
      case SIN:
        // do sin in radians, unless unit is degrees
        value = unit.equals(CommonUnit.degree) ?
                Math.sin(Data.DEGREES_TO_RADIANS * Value) : Math.sin(Value);
        u = CommonUnit.dimensionless.equals(unit) ? unit : null;
        break;
      case SIN_DEGREES:
        // do sin in degrees, unless unit is radians
        value = unit.equals(CommonUnit.radian) ?
                Math.sin(Value) : Math.sin(Data.DEGREES_TO_RADIANS * Value);
        u = CommonUnit.dimensionless.equals(unit) ? unit : null;
        break;
      case SQRT:
        value = Math.sqrt(Value);
        u = CommonUnit.dimensionless.equals(unit) ? unit : null;
        break;
      case TAN:
        // do tan in radians, unless unit is degrees
        value = unit.equals(CommonUnit.degree) ?
                Math.tan(Data.DEGREES_TO_RADIANS * Value) : Math.tan(Value);
        u = CommonUnit.dimensionless.equals(unit) ? unit : null;
        break;
      case TAN_DEGREES:
        // do tan in degrees, unless unit is radians
        value = unit.equals(CommonUnit.radian) ?
                Math.tan(Value) : Math.tan(Data.DEGREES_TO_RADIANS * Value);
        u = CommonUnit.dimensionless.equals(unit) ? unit : null;
        break;
      case NEGATE:
	u = unit.getAbsoluteUnit();
        value = -u.toThis(Value, unit);
        break;
      case NOP:
        value = Value;
        u = unit;
        break;
      default:
        throw new ArithmeticException("Real.unary: illegal operation");
    }
    if (error_mode == NO_ERRORS || Error == null) {
      /*- TDR June 1998
      return new Real(((RealType) Type), value, u, null);
      */
      return new Real((RealType) new_type, value, u, null);
    }
    else {
      /*- TDR June 1998
      return new Real(((RealType) Type), value, u,
                      new ErrorEstimate(value, u, op, Error, error_mode));
      */
      return new Real((RealType) new_type, value, u,
                      new ErrorEstimate(value, u, op, Error, error_mode));
    }
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    if (Double.isNaN(Value)) return shadow;
    int i = ((ShadowRealType) type).getIndex();
    if (i >= 0) {
      double value;
      Unit dunit = ((RealType) Type).getDefaultUnit();
      if (dunit != null && !dunit.equals(unit)) {
        value = dunit.toThis(Value, unit);
      }
      else {
        value = Value;
      }
      if (value == value) {
        shadow.ranges[0][i] = Math.min(shadow.ranges[0][i], value);
        shadow.ranges[1][i] = Math.max(shadow.ranges[1][i], value);
      }
    }
    return shadow;
  }

  public Unit getUnit() {
    return unit;
  }

  public ErrorEstimate getError() {
    return Error;
  }

  /** return a Real that clones this, except its ErrorEstimate
      is adjusted for the sampling error in error */
  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    if (isMissing() || Error == null ||
        error == null || error.isMissing()) return this;
    double a = ((Real) error).getValue();
    double b = Error.getErrorValue();
    double e = (error_mode == INDEPENDENT) ? Math.sqrt(a * a + b * b) :
                                             Math.abs(a) + Math.abs(b);
    return new Real((RealType) Type, Value, unit,
                    new ErrorEstimate(Value, e, unit));
  }

  /** clone this, but with a new Unit */
  public Real cloneButUnit(Unit u) throws VisADException {
    return new Real((RealType) Type, Value, u, Error);
  }

  public Object clone() {
    return new Real((RealType) Type, Value, unit, Error, true);
  }

  public String toString() {
    try {
      if (Double.isNaN(Value)) {
        return "missing";
      }
      else {
        return
	  Type.equals(RealType.DateTime)
	    ? new DateTime(this).toString()
	    : Double.toString(Value);
      }
    }
    catch (VisADException e) {
      return e.toString();
    }
  }

  /**
   * Gets a string that represents just the value portion of this Real -- but
   * with full semantics (e.g. numeric value and unit).
   * @return			A string representation of just the value
   *				portion of this Real.
   */
  public String toValueString() {
    String	result;
    try {
      if (Double.isNaN(Value)) {
        result = "missing";
      }
      else {
	  if (Type.equals(RealType.DateTime)) {
	    result = new DateTime(this).toValueString();
	  }
	  else {
	    Unit u =
	      unit != null ? unit : ((RealType)getType()).getDefaultUnit();
	    result = Float.toString((float)Value) + (u == null ? "" : " " + u);
	  }
      }
    }
    catch (VisADException e) {
      result = e.toString();
    }
    return result;
  }

  public String longString(String pre) throws VisADException {
    if (Double.isNaN(Value)) {
      return pre + "missing\n";
    }
    else if (Type.equals(RealType.DateTime)) {
      return pre + "Real.Time: Value = " +
             new DateTime(this).toString() + "\n";
    }
    else {
      return pre + "Real: Value = " + Value +
             "  (TypeName: " + ((RealType) Type).getName() + ")\n";
    }
  }

  /**
   * Compares this Real to another.
   * @param object		The other Real to compare against.  It shall be
   *				a Real with a compatible (i.e. convertible)
   *				unit.
   * @return                    A negative integer, zero, or a positive integer
   *                            depending on whether this Real is considered
   *                            less than, equal to, or greater than the other
   *                            Real, respectively.  If the values of the Real-s
   *                            in the default unit are equal, then the <code>
   *                            ErrorEstimate.compareTo()</code> method is used
   *                            to break a tie.
   */
  public int compareTo(Object object)
  {
    Real	that = (Real)object;
    int		comp;
    try
    {
      Unit	defaultUnit = ((RealType)getType()).getDefaultUnit();
      comp = new Double(getValue(defaultUnit)).compareTo(
	     new Double(that.getValue(defaultUnit)));
      if (comp == 0) {
	comp = 
	  Error != null
	    ? Error.compareTo(that.Error)
	    : (that.Error == null ? 0 : -that.Error.compareTo(Error));
      }
    }
    catch (VisADException e)
    {
      comp = 1;	// make problem Real-s greater than anything
    }
    return comp;
  }

  /**
   * Indicates if this Real is semantically identical to an object.
   * @param obj			The object.
   * @return			<code>true</code> if and only if this Real
   *				is semantically identical to the object.
   */
  public boolean equals(Object obj) {
    return obj != null && obj instanceof Real && 
      getType().equals(((Real)obj).getType()) && compareTo(obj) == 0;
  }

  /** run 'java visad.Real' to test the Real class */
  public static void main(String args[])
         throws VisADException, RemoteException {

    byte b = 10;
    Real w = new Real(b);

    int ii = 14;
    short s = 12;
    Real t = new Real(1.0);
    Real x = new Real(12);
    Real y = new Real(12L);
    Real u = new Real(ii);
    Real v = new Real(s);

    System.out.println("x = " + x + "\nw = " + w);

    System.out.println("x + w = " + x.add(w));
    System.out.println("x - w = " + x.subtract(w));
    System.out.println("x * w = " + x.multiply(w));
    System.out.println("x / w = " + x.divide(w));
    System.out.println("sqrt(x) = " + x.sqrt());

    System.out.println("");

    Real fahrenheit =
      new Real(
	new RealType(
	  "FahrenheitTemperature",
	  new OffsetUnit(459.67,
	    new ScaledUnit(1/1.8, SI.kelvin, "degR"),
	      "degF"),
	  null),
	32.0);
    Real kelvin = new Real(new RealType("Temperature", SI.kelvin, null), 300);
    System.out.println("300 kelvin + 32 fahrenheit = " +
      ((Real)kelvin.add(fahrenheit)).toValueString());
    System.out.println("300 kelvin - 32 fahrenheit = " +
      ((Real)kelvin.subtract(fahrenheit)).toValueString());
    System.out.println("max(300 kelvin, 32 fahrenheit) = " +
      ((Real)kelvin.max(fahrenheit)).toValueString());
    System.out.println("min(300 kelvin, 32 fahrenheit) = " +
      ((Real)kelvin.min(fahrenheit)).toValueString());

    System.out.println("");

    System.out.println("32 fahrenheit + 300 kelvin = " +
      ((Real)fahrenheit.add(kelvin)).toValueString());
    System.out.println("32 fahrenheit - 300 kelvin = " +
      ((Real)fahrenheit.subtract(kelvin)).toValueString());
    System.out.println("max(32 fahrenheit, 300 kelvin) = " +
      ((Real)fahrenheit.max(kelvin)).toValueString());
    System.out.println("min(32 fahrenheit, 300 kelvin) = " +
      ((Real)fahrenheit.min(kelvin)).toValueString());

    System.out.println("");

    Real deltaF =
      new Real(
	new RealType(
	  "DeltaFahrenheitTemperature",
	  new OffsetUnit(459.67,
	    new ScaledUnit(1/1.8, SI.kelvin, "degR"),
	      "degF"),
	  null,
	  RealType.INTERVAL),
	32.0);
    System.out.println("300 kelvin + 32 deltaF = " +
      ((Real)kelvin.add(deltaF)).toValueString());
    System.out.println("300 kelvin - 32 deltaF = " +
      ((Real)kelvin.subtract(deltaF)).toValueString());
    System.out.println("max(300 kelvin, 32 deltaF) = " +
      ((Real)kelvin.max(deltaF)).toValueString());
    System.out.println("min(300 kelvin, 32 deltaF) = " +
      ((Real)kelvin.min(deltaF)).toValueString());

    System.out.println("");

    System.out.println("32 deltaF + 300 kelvin = " +
      ((Real)deltaF.add(kelvin)).toValueString());
    System.out.println("32 deltaF - 300 kelvin = " +
      ((Real)deltaF.subtract(kelvin)).toValueString());
    System.out.println("max(32 deltaF, 300 kelvin) = " +
      ((Real)deltaF.max(kelvin)).toValueString());
    System.out.println("min(32 deltaF, 300 kelvin) = " +
      ((Real)deltaF.min(kelvin)).toValueString());

    System.out.println("");

    Real deltaK =
      new Real(
	new RealType( "DeltaTemperature", SI.kelvin, null, RealType.INTERVAL),
	100.0);
    System.out.println("300 kelvin + 100 deltaK = " +
      ((Real)kelvin.add(deltaK)).toValueString());
    System.out.println("300 kelvin - 100 deltaK = " +
      ((Real)kelvin.subtract(deltaK)).toValueString());
    System.out.println("max(300 kelvin, 100 deltaK) = " +
      ((Real)kelvin.max(deltaK)).toValueString());
    System.out.println("min(300 kelvin, 100 deltaK) = " +
      ((Real)kelvin.min(deltaK)).toValueString());

    System.out.println("");

    System.out.println("100 deltaK + 300 kelvin = " +
      ((Real)deltaK.add(kelvin)).toValueString());
    System.out.println("100 deltaK - 300 kelvin = " +
      ((Real)deltaK.subtract(kelvin)).toValueString());
    System.out.println("max(100 deltaK, 300 kelvin) = " +
      ((Real)deltaK.max(kelvin)).toValueString());
    System.out.println("min(100 deltaK, 300 kelvin) = " +
      ((Real)deltaK.min(kelvin)).toValueString());

    System.out.println("");

    System.out.println("100 deltaK + 32 deltaF = " +
      ((Real)deltaK.add(deltaF)).toValueString());
    System.out.println("100 deltaK - 32 deltaF = " +
      ((Real)deltaK.subtract(deltaF)).toValueString());
    System.out.println("max(100 deltaK, 32 deltaF) = " +
      ((Real)deltaK.max(deltaF)).toValueString());
    System.out.println("min(100 deltaK, 32 deltaF) = " +
      ((Real)deltaK.min(deltaF)).toValueString());

    System.out.println("");

    System.out.println("32 deltaF + 100 deltaK = " +
      ((Real)deltaF.add(deltaK)).toValueString());
    System.out.println("32 deltaF - 100 deltaK = " +
      ((Real)deltaF.subtract(deltaK)).toValueString());
    System.out.println("max(32 deltaF, 100 deltaK) = " +
      ((Real)deltaF.max(deltaK)).toValueString());
    System.out.println("min(32 deltaF, 100 deltaK) = " +
      ((Real)deltaF.min(deltaK)).toValueString());

    System.out.println("");

    System.out.println("300 kelvin + -(32 fahrenheit) = " +
      ((Real)kelvin.add(fahrenheit.negate())).toValueString());
    System.out.println("32 fahrenheit + -(300 kelvin) = " +
      ((Real)fahrenheit.add(kelvin.negate())).toValueString());
  }

/* Here's the output:

x = 12.0
w = 10.0
x + w = 22.0
x - w = 2.0
x * w = 120.0
x / w = 1.2
sqrt(x) = 3.4641016151377544

300 kelvin + 32 fahrenheit = 573.15 K
300 kelvin - 32 fahrenheit = 26.85 K
max(300 kelvin, 32 fahrenheit) = 300.0 K
min(300 kelvin, 32 fahrenheit) = 273.15 K

32 fahrenheit + 300 kelvin = 112.33 degF
32 fahrenheit - 300 kelvin = -48.33 degF
max(32 fahrenheit, 300 kelvin) = 80.33 degF
min(32 fahrenheit, 300 kelvin) = 32.0 degF

300 kelvin + 32 deltaF = 317.77777 K
300 kelvin - 32 deltaF = 282.22223 K
max(300 kelvin, 32 deltaF) = 300.0 K
min(300 kelvin, 32 deltaF) = 17.777779 K

32 deltaF + 300 kelvin = 572.0 degR
32 deltaF - 300 kelvin = -508.0 degR
max(32 deltaF, 300 kelvin) = 540.0 degR
min(32 deltaF, 300 kelvin) = 32.0 degR

300 kelvin + 100 deltaK = 400.0 K
300 kelvin - 100 deltaK = 200.0 K
max(300 kelvin, 100 deltaK) = 300.0 K
min(300 kelvin, 100 deltaK) = 100.0 K

100 deltaK + 300 kelvin = 400.0 K
100 deltaK - 300 kelvin = -200.0 K
max(100 deltaK, 300 kelvin) = 300.0 K
min(100 deltaK, 300 kelvin) = 100.0 K

100 deltaK + 32 deltaF = 117.77778 K
100 deltaK - 32 deltaF = 82.22222 K
max(100 deltaK, 32 deltaF) = 100.0 K
min(100 deltaK, 32 deltaF) = 17.777779 K

32 deltaF + 100 deltaK = 212.0 degR
32 deltaF - 100 deltaK = -148.0 degR
max(32 deltaF, 100 deltaK) = 180.0 degR
min(32 deltaF, 100 deltaK) = 32.0 degR
*/

}

