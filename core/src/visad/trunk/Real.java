
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

  private double Value;
  private Unit unit;
  private ErrorEstimate Error;

  /** construct a Real object with Unit and ErrorEstimate */
  public Real(RealType type, double value, Unit u, ErrorEstimate error)
         throws VisADException {
    super(type);
    if (!Unit.canConvert(u, type.getDefaultUnit())) {
      throw new UnitException("Real: Unit must be convertable with " +
                              "Type default Unit");
    }
    unit = u;
    Value = value;
    Error = Double.isNaN(value) ? null : error;
  }

  /** construct a Real object with Unit and numerical error */
  public Real(RealType type, double value, Unit u, double error)
         throws VisADException {
    this(type, value, u, new ErrorEstimate(value, error, u));
  }

  /** construct a Real object with Unit */
  public Real(RealType type, double value, Unit u)
         throws VisADException {
    this(type, value, u, null);
  }

  /** construct a Real object with default Unit */
  public Real(RealType type, double value) {
    this(type, value, type.getDefaultUnit(), null, true);
  }

  /** construct a Real object with missing value and default Unit */
  public Real(RealType type) {
    this(type, Double.NaN, type.getDefaultUnit(), null, true);
  }

  /** construct a Real object with the generic REAL type, and error */
  public Real(double value, double error) {
    this(RealType.Generic, value, RealType.Generic.getDefaultUnit(),
         new ErrorEstimate(value, Math.abs(error), RealType.Generic.getDefaultUnit()),
         true);
  }

  /** construct a Real object with the generic REAL type, and ErrorEstimate 0.0 */
  public Real(double value) {
    this(RealType.Generic, value, RealType.Generic.getDefaultUnit(),
         new ErrorEstimate(value, 0.0, RealType.Generic.getDefaultUnit()), true);
  }

  /** trusted constructor for clone and other constructors */
  private Real(RealType type, double value, Unit u, ErrorEstimate error,
               boolean b) {
    super(type);
    unit = u;
    Value = value;
    Error = Double.isNaN(value) ? null : error;
  }

  public final double getValue() {
    return Value;
  }

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

  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (data instanceof Real) {
      Unit u;
      Unit data_unit = ((Real) data).getUnit();
      double value = ((Real) data).getValue();
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
          else if (Unit.canConvert(unit, data_unit)) {
            value = unit.toThis(value, data_unit);
            // scale data.ErrorEstimate for Unit.toThis
            if (error_mode != NO_ERRORS && dError != null) {
              double error = 0.5 * dError.getErrorValue();
              double new_error =
                Math.abs( unit.toThis(value + error, data_unit) -
                          unit.toThis(value - error, data_unit) );
              dError = new ErrorEstimate(value, new_error, unit);
            }
            u = unit;
          }
          else {
            u = null;
          }
          switch (op) {
            case ADD:
              value = Value + value;
              break;
            case SUBTRACT:
              value = Value - value;
              break;
            case INV_SUBTRACT:
              value = value - Value;
              break;
            case MAX:
              value = Math.max(Value, value);
              break;
            case MIN:
            default:
              value = Math.min(Value, value);
              break;
          }
          break;
        case MULTIPLY:
          value = Value * value;
          if (unit == null || data_unit == null) {
            u = null;
          }
          else {
            u = unit.multiply(data_unit);
          }
          break;
        case DIVIDE:
          value = Value / value;
          if (unit == null || data_unit == null) {
            u = null;
          }
          else {
            u = unit.divide(data_unit);
          }
          break;
        case INV_DIVIDE:
          value = value / Value;
          if (unit == null || data_unit == null) {
            u = null;
          }
          else {
            u = data_unit.divide(unit);
          }
          break;
        case POW:
          value = Math.pow(Value, value);
          u = null;
          break;
        case INV_POW:
          value = Math.pow(value, Value);
          u = null;
          break;
        case ATAN2:
          value = Math.atan2(Value, value);
          u = CommonUnit.radian;
          break;
        case ATAN2_DEGREES:
          value = Data.RADIANS_TO_DEGREES * Math.atan2(Value, value);
          u = CommonUnit.degree;
          break;
        case INV_ATAN2:
          value = Math.atan2(value, Value);
          u = CommonUnit.radian;
          break;
        case INV_ATAN2_DEGREES:
          value = Data.RADIANS_TO_DEGREES * Math.atan2(value, Value);
          u = CommonUnit.degree;
          break;
        case REMAINDER:
          value = Value % value;
          u = unit;
          break;
        case INV_REMAINDER:
          value = value % Value;
          u = data_unit;
          break;
        default:
          throw new ArithmeticException("Real.binary: illegal operation");
      }
      if (error_mode == NO_ERRORS || Error == null || dError == null) {
        return new Real(((RealType) Type), value, u, null);
      }
      else {
        return new Real(((RealType) Type), value, u,
                   new ErrorEstimate(value, u, op, Error, dError, error_mode));
      }
    }
    else if (data instanceof Text) {
      throw new TypeException("Real.binary: types don't match");
    }
    else if (data instanceof Tuple) {
      return data.binary(this, invertOp(op), sampling_mode, error_mode);
    }
    else if (data instanceof Field) {
      return data.binary(this, invertOp(op), sampling_mode, error_mode);
    }
    else {
      throw new TypeException("Real.binary");
    }
  }

  /** unary function on a Real; override some trig functions based
      on Unit; transcental functions destroy Unit */
  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException {
    double value;
    Unit u;
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
        value = -Value;
        u = unit;
        break;
      default:
        throw new ArithmeticException("Real.unary: illegal operation");
    }
    if (error_mode == NO_ERRORS || Error == null) {
      return new Real(((RealType) Type), value, u, null);
    }
    else {
      return new Real(((RealType) Type), value, u,
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
      else if (Type.equals(RealType.Time)) {
        return new DateTime(this).toString();
      }
      else {
        return Double.toString(Value);
      }
    }
    catch (VisADException e) {
      return e.toString();
    }
  }

  public String longString(String pre) throws VisADException {
    if (Double.isNaN(Value)) {
      return pre + "missing\n";
    }
    else if (Type.equals(RealType.Time)) {
      return pre + "Real.Time: Value = " +
             new DateTime(this).toString() + "\n";
    }
    else {
      return pre + "Real: Value = " + Value +
             "  (TypeName: " + ((RealType) Type).getName() + ")\n";
    }
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
  }

/* Here's the output:

iris 200% java visad.Real
x = 12
w = 10
x + w = 22
x - w = 2
x * w = 120
x / w = 1.2
sqrt(x) = 3.4641
iris 201%

*/

}

