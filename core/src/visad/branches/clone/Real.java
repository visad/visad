//
// Real.java
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

package visad;

import java.rmi.*;

/**
 * Real is the class of VisAD scalar data for real numbers represented
 * as double precision floating point values.  Double.NaN is used to
 * indicate missing values, because it has the appropriate arithmetic
 * semantics.  Real objects are immutable.<P>
 */
public class Real
  extends	Scalar
  implements	RealIface
{

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
   * @param error		Error estimate of the Real.  May be
   *				<code>null</code>.
   * @throws UnitException      if the default unit of the type is inconvertible
   *                            with the unit argument (i.e. if <code>
   *                            Unit.canConvert(u, type.getDefaultUnit())</code>
   *                            returns false).
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public Real(RealType type, double value, Unit u, ErrorEstimate error)
         throws VisADException {
    super(type);
    if (!Unit.canConvert(u, type.getDefaultUnit())) {
      throw new UnitException("Real: Unit \"" + u +
                              "\" must be convertable" +
                              " with Type default Unit \"" +
                              type.getDefaultUnit() + "\"");
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
   */
  public Real(RealType type, double value) {
    this(type, value, type.getDefaultUnit(), null, true);
  }

  /**
   * Constructs a Real object.  The value will be missing, the unit of the
   * Real will be the default unit of the RealType, and the error estimate
   * will be <code>null</code>.
   * @param type		The type of the Real.
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
      if (((RealType)getType()).isInterval())
	unit_out = unit_out.getAbsoluteUnit();
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
      Unit thisUnit =
	unit == null
	  ? null
	  : Unit.canConvert(CommonUnit.dimensionless, unit)
	    ? CommonUnit.dimensionless
	    : unit.getAbsoluteUnit();
      Unit thatUnit =
	data_unit == null
	  ? null
	  : Unit.canConvert(CommonUnit.dimensionless, data_unit)
	    ? CommonUnit.dimensionless
	    : data_unit.getAbsoluteUnit();
      double thatValue = ((Real) data).getValue(thatUnit);
      double thisValue = getValue(thisUnit);
      double opValue;
      ErrorEstimate dError = ((Real) data).getError();
      switch (op) {
        case ADD:
        case SUBTRACT:
        case INV_SUBTRACT:
        case MAX:
        case MIN:
          if (thisUnit == null || thatUnit == null) {
            u = null;
          }
          else if (thisUnit == CommonUnit.promiscuous) {
            u = thatUnit;
          }
          else if (thatUnit == CommonUnit.promiscuous) {
            u = thisUnit;
          }
          else {
	    try {
	      u = thisUnit;
	      thatValue = u.toThis(thatValue, thatUnit);
	      // scale data.ErrorEstimate for Unit.toThis
	      if (error_mode != NO_ERRORS && dError != null) {
		Unit	errorUnit = dError.getUnit();
		if (errorUnit == null)
		  errorUnit = thatUnit;
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
          if (thisUnit == null || thatUnit == null) {
            u = null;
          }
          else {
            u = thisUnit.multiply(thatUnit);
          }
          break;
        case DIVIDE:
          opValue = thisValue / thatValue;
          if (thisUnit == null || thatUnit == null) {
            u = null;
          }
          else {
            u = thisUnit.divide(thatUnit);
          }
          break;
        case INV_DIVIDE:
          opValue = thatValue / thisValue;
          if (thisUnit == null || thatUnit == null) {
            u = null;
          }
          else {
            u = thatUnit.divide(thisUnit);
          }
          break;
        case POW:
          opValue = Math.pow(thisValue, thatValue);
          u = CommonUnit.dimensionless.equals(thisUnit)
	    ? CommonUnit.dimensionless : null;
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
          u = thisUnit;
          break;
        case INV_REMAINDER:
          opValue = thatValue % thisValue;
          u = thatUnit;
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
    else if (data instanceof TupleIface) {
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
      on Unit; transcental functions destroy dimensionfull Unit */
  public Data unary(int op, MathType new_type, int sampling_mode, int error_mode)
              throws VisADException {
    Unit thisUnit;	// input unit
    double thisValue;	// input value
    if (unit == null) {
      thisUnit = null;
      thisValue = Value;
    }
    else {
      /*
       * Condition input numeric value and unit.  If the input unit is
       * dimensionless, then the input numeric value is converted to be in units
       * of the dimensionless unit 1; otherwise, the input numeric value is
       * converted to be in units of the absolute unit of the input unit.
       */
      thisUnit = Unit.canConvert(CommonUnit.dimensionless, unit)
	? CommonUnit.dimensionless : unit.getAbsoluteUnit();
      thisValue = thisUnit.toThis(Value, unit);
    }
    double value;	// output value
    Unit u;		// output unit
    /*- TDR  June 1998  */
    if ( new_type == null ) {
      throw new TypeException("unary: new_type may not be null");
    }
    switch (op) {
      case ABS:
        value = Math.abs(thisValue);
        u = thisUnit;
        break;
      case ACOS:
        value = Math.acos(thisValue);
        u = CommonUnit.radian;
        break;
      case ACOS_DEGREES:
        value = Data.RADIANS_TO_DEGREES * Math.acos(thisValue);
        u = CommonUnit.degree;
        break;
      case ASIN:
        value = Math.asin(thisValue);
        u = CommonUnit.radian;
        break;
      case ASIN_DEGREES:
        value = Data.RADIANS_TO_DEGREES * Math.asin(thisValue);
        u = CommonUnit.degree;
        break;
      case ATAN:
        value = Math.atan(thisValue);
        u = CommonUnit.radian;
        break;
      case ATAN_DEGREES:
        value = Data.RADIANS_TO_DEGREES * Math.atan(thisValue);
        u = CommonUnit.degree;
        break;
      case CEIL:
        value = Math.ceil(thisValue);
        u = thisUnit;
        break;
      case COS:
        // do cos in radians, unless unit is degrees
        value = CommonUnit.degree.equals(thisUnit) ?
                Math.cos(Data.DEGREES_TO_RADIANS * thisValue) : Math.cos(thisValue);
        u = CommonUnit.dimensionless.equals(thisUnit) ? thisUnit : null;
        break;
      case COS_DEGREES:
        // do cos in degrees, unless unit is radians
        value = CommonUnit.radian.equals(thisUnit) ?
                Math.cos(thisValue) : Math.cos(Data.DEGREES_TO_RADIANS * thisValue);
        u = CommonUnit.dimensionless.equals(thisUnit) ? thisUnit : null;
        break;
      case EXP:
	value = Math.exp(thisValue);
	u = CommonUnit.dimensionless.equals(thisUnit) ? thisUnit : null;
        break;
      case FLOOR:
        value = Math.floor(thisValue);
        u = thisUnit;
        break;
      case LOG:
	value = Math.log(thisValue);
	u = CommonUnit.dimensionless.equals(thisUnit) ? thisUnit : null;
        break;
      case RINT:
        value = Math.rint(thisValue);
        u = thisUnit;
        break;
      case ROUND:
        value = Math.round(thisValue);
        u = thisUnit;
        break;
      case SIN:
        // do sin in radians, unless unit is degrees
        value = CommonUnit.degree.equals(thisUnit) ?
                Math.sin(Data.DEGREES_TO_RADIANS * thisValue) : Math.sin(thisValue);
        u = CommonUnit.dimensionless.equals(thisUnit) ? thisUnit : null;
        break;
      case SIN_DEGREES:
        // do sin in degrees, unless unit is radians
        value = CommonUnit.radian.equals(thisUnit) ?
                Math.sin(thisValue) : Math.sin(Data.DEGREES_TO_RADIANS * thisValue);
        u = CommonUnit.dimensionless.equals(thisUnit) ? thisUnit : null;
        break;
      case SQRT:
        value = Math.sqrt(thisValue);
        // WLH 26 Nov 2001
        // u = CommonUnit.dimensionless.equals(thisUnit) ? thisUnit : null;
        if (thisUnit == null) {
          u = null;
        }
        else {
          try {
            u = thisUnit.sqrt();
          }
          catch (IllegalArgumentException e) {
            u = null;
          }
          catch (UnitException e) {
            u = null;
          }
        }
        break;
      case TAN:
        // do tan in radians, unless unit is degrees
        value = CommonUnit.degree.equals(thisUnit) ?
                Math.tan(Data.DEGREES_TO_RADIANS * thisValue) : Math.tan(thisValue);
        u = CommonUnit.dimensionless.equals(thisUnit) ? thisUnit : null;
        break;
      case TAN_DEGREES:
        // do tan in degrees, unless unit is radians
        value = CommonUnit.radian.equals(thisUnit) ?
                Math.tan(thisValue) : Math.tan(Data.DEGREES_TO_RADIANS * thisValue);
        u = CommonUnit.dimensionless.equals(thisUnit) ? thisUnit : null;
        break;
      case NEGATE:
        value = -thisValue;
	u = thisUnit;
        break;
      case NOP:
        value = thisValue;
        u = thisUnit;
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

  /** clone this, but with a new value */
  public Real cloneButValue(double value) throws VisADException {
    return new Real((RealType) Type, value, unit, Error);
  }

  /** clone this, but with a new Unit */
  public Real cloneButUnit(Unit u) throws VisADException {
    return new Real((RealType) Type, Value, u, Error);
  }

  public String toString() {
    try {
      if (Double.isNaN(Value)) {
        return "missing";
      }
      else {
        return
	  (Unit.canConvert(getUnit(), CommonUnit.secondsSinceTheEpoch) &&
	  !getUnit().getAbsoluteUnit().equals(getUnit()))
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
	  if (Unit.canConvert(getUnit(), CommonUnit.secondsSinceTheEpoch) &&
	      !getUnit().getAbsoluteUnit().equals(getUnit())) {
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
    else if (Unit.canConvert(getUnit(), CommonUnit.secondsSinceTheEpoch) &&
	!getUnit().getAbsoluteUnit().equals(getUnit())) {
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
   *                            to break the tie.
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
	if (Error == null) {
	  comp = that.Error == null ? 0 : -1;
	}
	else if (that.Error == null) {
	  comp = 1;
	}
	else {
	  comp = Error.compareTo(that.Error);
	}
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

  /**
   * Returns the hash code of this Real.
   * @return			The hash code of this Real.  If two Real-s are
   *				semantically identical, then their hash codes
   *				are equal.
   */
  public int hashCode() {
    RealType	realType = (RealType)getType();
    int		hashCode = realType.hashCode();
    try
    {
      hashCode ^= new Double(getValue(realType.getDefaultUnit())).hashCode();
    }
    catch (VisADException e)
    {}	// ignore because can't happen
    if (Error != null)
      hashCode ^= Error.hashCode();
    return hashCode;
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
	RealType.getRealType(
	  "FahrenheitTemperature",
	  new OffsetUnit(459.67,
	    new ScaledUnit(1/1.8, SI.kelvin, "degR"),
	      "degF"),
	  null),
	32.0);
    Real kelvin = new Real(RealType.getRealType("Temperature", SI.kelvin, null), 300);
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
	RealType.getRealType(
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
	RealType.getRealType( "DeltaTemperature", SI.kelvin, null, RealType.INTERVAL),
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

    System.out.println("");

    Unit	foot = new ScaledUnit(3.048, SI.meter);
    Unit	yard = new ScaledUnit(3, (ScaledUnit)foot);
    System.out.println("log(1 yard / 3 feet) = " +
      ((Real)new Real(RealType.getRealType("OneYard", SI.meter, null), 1, yard)
	.divide(new Real(RealType.getRealType("ThreeFeet", SI.meter, null), 3, foot))
	.log()).toValueString());
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

32 fahrenheit + 300 kelvin = 1031.67 degR
32 fahrenheit - 300 kelvin = -48.33 degR
max(32 fahrenheit, 300 kelvin) = 540.0 degR
min(32 fahrenheit, 300 kelvin) = 491.67 degR

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

300 kelvin + -(32 fahrenheit) = 26.85 K
32 fahrenheit + -(300 kelvin) = -48.33 degR

log(1 yard / 3 feet) = 0.0 

*/

}

