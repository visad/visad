//
// ErrorEstimate.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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
   ErrorEstimate is the immutable VisAD class for statistics about a value
   or array of values.<P>
*/
public class ErrorEstimate extends Object implements java.io.Serializable, Comparable {

  final double Error;
  final double Mean;
  final long NumberNotMissing;
  final Unit unit;

  /** these are bounds used in estimates of derivatives;
      they should be reciprocals;
      they are not applied in all cases, or uniformly */
  private static final double DERIVATIVE_LOW_LIMIT = 0.01;
  private static final double DERIVATIVE_HI_LIMIT = 1.0 / DERIVATIVE_LOW_LIMIT;

  /** construct an error distribution of number values with
      given mean and error (variance), in Unit unit
      <br><br><em>Note that the <code>mean</code> and <code>error</code>
      parameters are reversed in this method</em>
  */
  public ErrorEstimate(double error, double mean, long number, Unit u) {
    unit = u;
    if (Double.isNaN(error) || Double.isNaN(mean) || number <= 0) {
      Error = Double.NaN;
      Mean = Double.NaN;
      NumberNotMissing = 0;
    }
    else {
      Error = error;
      Mean = mean;
      NumberNotMissing = number;
    }
  }

  /** construct an error distribution of 1 value with
      given mean and error (variance), in Unit unit */
  public ErrorEstimate(double mean, double error, Unit u) {
    unit = u;
    if (Double.isNaN(mean)) {
      NumberNotMissing = 0;
      Mean = Double.NaN;
      Error = Double.NaN;
    }
    else {
      NumberNotMissing = 1;
      Mean = mean;
      Error = error;
    }
  }

  /** construct an ErrorEstimate from a Field ErrorEstimate, a sample
      ErrorEstimate, the sample value, and an increment for NumberNotMissing;
      used by FlatField.setSample */
  public ErrorEstimate(ErrorEstimate field_error, ErrorEstimate sample_error,
                       double val, int inc) throws VisADException {
    double ef;
    double mf;
    long nf;
    Unit uf;
    if (field_error == null) {
      ef = Double.NaN;
      mf = Double.NaN;
      nf = 0;
      uf = null;
    }
    else {
      ef = field_error.Error;
      mf = field_error.Mean;
      nf = field_error.NumberNotMissing;
      uf = field_error.unit;
    }

    double es;
    double ms;
    long ns = Double.isNaN(val) ? 0 : 1;
    Unit us;
    if (sample_error == null) {
      us = null;
      es = Double.NaN;
      ms = Double.NaN;
    }
    else {
      us = sample_error.unit;
      if (uf == null || uf == us) {
        es = sample_error.Error;
        ms = val;
      }
      else {
        es = uf.toThis(sample_error.Error, us);
        ms = uf.toThis(val, us);
      }
    }

    unit = (field_error != null) ? uf : us;
    long number = nf + inc;
    if (number > 0) {
      double mean = 0.0;
      double error = 0.0;
      if (!Double.isNaN(ef)) error += (number - ns) * ef;
      if (!Double.isNaN(mf)) mean += (number - ns) * mf;

      if (!Double.isNaN(es)) error += ns * es;
      if (!Double.isNaN(ms)) mean += ns * ms;
      NumberNotMissing = number;
//if(NumberNotMissing==1800)Thread.dumpStack();
      Error = error / NumberNotMissing;
      Mean = mean / NumberNotMissing;
    }
    else {
      NumberNotMissing = 0;
      Mean = Double.NaN;
      Error = Double.NaN;
    }
  }

  /** construct an ErrorEstimate for a value that is the result of a
      binary operator; a and b are the ErrorEstimate-s for the operands */
  public ErrorEstimate(double value, Unit u, int op, ErrorEstimate a,
                       ErrorEstimate b, int error_mode)
         throws VisADException {
    unit = u;
    if (Double.isNaN(value)) {
      NumberNotMissing = 0;
      Mean = Double.NaN;
      Error = Double.NaN;
    }
    else {
      NumberNotMissing = 1;
      Mean = value;
      Error = binary(op, a, b, error_mode);
    }
  }

  /** construct an ErrorEstimate for a value that is the result of a
      unary operator; a is the ErrorEstimate for the operand */
  public ErrorEstimate(double value, Unit u, int op, ErrorEstimate a,
                       int error_mode) throws VisADException {
    unit = u;
    if (Double.isNaN(value)) {
      NumberNotMissing = 0;
      Mean = Double.NaN;
      Error = Double.NaN;
    }
    else {
      NumberNotMissing = 1;
      Mean = value;
      Error = unary(op, a, error_mode);
    }
  }

  /** construct an ErrorEstimate for an array of values with an error */
  public ErrorEstimate(double[] value, double error, Unit u) {
    unit = u;
    int number = 0;
    double sum = 0.0;
    for (int i=0; i<value.length; i++) {
      if (!Double.isNaN(value[i])) {
        number++;
        sum += value[i];
      }
    }
    NumberNotMissing = number;
    if (NumberNotMissing > 0) {
      Mean = sum / NumberNotMissing;
      Error = error;
    }
    else {
      Mean = Double.NaN;
      Error = Double.NaN;
    }
  }

  /** construct an ErrorEstimate for an array of values with an error */
  public ErrorEstimate(float[] value, double error, Unit u) {
    unit = u;
    int number = 0;
    double sum = 0.0;
    for (int i=0; i<value.length; i++) {
      if (!Float.isNaN(value[i])) {
        number++;
        sum += value[i];
      }
    }
    NumberNotMissing = number;
    if (NumberNotMissing > 0) {
      Mean = sum / NumberNotMissing;
      Error = error;
    }
    else {
      Mean = Double.NaN;
      Error = Double.NaN;
    }
  }

  /** construct Error for an array of values that is the result of a binary
      operator; a and b are the ErrorEstimate-s for the operands */
  public ErrorEstimate(double[] value, Unit u, int op, ErrorEstimate a,
                       ErrorEstimate b, int error_mode)
         throws VisADException {
    unit = u;
    int number = 0;
    double sum = 0.0;
    for (int i=0; i<value.length; i++) {
      if (!Double.isNaN(value[i])) {
        number++;
        sum += value[i];
      }
    }
    NumberNotMissing = number;
    if (NumberNotMissing > 0) {
      Mean = sum / NumberNotMissing;
      Error = binary(op, a, b, error_mode);
    }
    else {
      Mean = Double.NaN;
      Error = Double.NaN;
    }
  }

  /** construct Error for an array of values that is the result of a binary
      operator; a and b are the ErrorEstimate-s for the operands */
  public ErrorEstimate(float[] value, Unit u, int op, ErrorEstimate a,
                       ErrorEstimate b, int error_mode)
         throws VisADException {
    unit = u;
    int number = 0;
    double sum = 0.0;
    for (int i=0; i<value.length; i++) {
      if (!Float.isNaN(value[i])) {
        number++;
        sum += value[i];
      }
    }
    NumberNotMissing = number;
    if (NumberNotMissing > 0) {
      Mean = sum / NumberNotMissing;
      Error = binary(op, a, b, error_mode);
    }
    else {
      Mean = Double.NaN;
      Error = Double.NaN;
    }
  }

  /** construct Error for an array of values that is the result of a unary
      operator; a is the ErrorEstimate for the operand */
  public ErrorEstimate(double[] value, Unit u, int op, ErrorEstimate a,
                       int error_mode) throws VisADException {
    unit = u;
    int number = 0;
    double sum = 0.0;
    for (int i=0; i<value.length; i++) {
      if (!Double.isNaN(value[i])) {
        number++;
        sum += value[i];
      }
    }
    NumberNotMissing = number;
    if (NumberNotMissing > 0) {
      Mean = sum / NumberNotMissing;
      Error = unary(op, a, error_mode);
    }
    else {
      Mean = Double.NaN;
      Error = Double.NaN;
    }
  }

  /** construct Error for an array of values that is the result of a unary
      operator; a is the ErrorEstimate for the operand */
  public ErrorEstimate(float[] value, Unit u, int op, ErrorEstimate a,
                       int error_mode) throws VisADException {
    unit = u;
    int number = 0;
    double sum = 0.0;
    for (int i=0; i<value.length; i++) {
      if (!Float.isNaN(value[i])) {
        number++;
        sum += value[i];
      }
    }
    NumberNotMissing = number;
    if (NumberNotMissing > 0) {
      Mean = sum / NumberNotMissing;
      Error = unary(op, a, error_mode);
    }
    else {
      Mean = Double.NaN;
      Error = Double.NaN;
    }
  }

  /** copy a ErrorEstimate[] array; this is a helper for Set,
      FlatField, etc */
  public static ErrorEstimate[] copyErrorsArray(ErrorEstimate[] errors) {
    if (errors == null) return null;
    int n = errors.length;
    ErrorEstimate[] ret_errors = new ErrorEstimate[n];
    for (int i=0; i<n; i++) ret_errors[i] = errors[i];
    return ret_errors;
  }

  /** estimate Error for a binary operator with operands a & b;
      actually, more of a WAG than an estimate;
      these formulas are a bit of a hack and
      suggestions for improvements are welcome */
  private double binary(int op, ErrorEstimate a, ErrorEstimate b,
                        int error_mode) throws VisADException {
    double am, bm, factor;
    double error = Double.NaN;
    if (a.isMissing() || b.isMissing() ||
        error_mode == Data.NO_ERRORS) return error;
    Unit u = null;
    switch (op) {
      case Data.ADD:
      case Data.SUBTRACT:
      case Data.INV_SUBTRACT:
      case Data.MAX:
      case Data.MIN:
        if (unit != null && a.unit != null && !unit.equals(a.unit)) {
          // apply Unit conversion to a Error and Mean
          am = Math.abs(unit.toThis(a.Mean + 0.5 * a.Error, a.unit) -
                            unit.toThis(a.Mean - 0.5 * a.Error, a.unit));
        }
        else {
          am = a.Error;
        }
        if (unit != null && b.unit != null && !unit.equals(b.unit)) {
          // apply Unit conversion to a Error and Mean
          bm = Math.abs(unit.toThis(b.Mean + 0.5 * b.Error, b.unit) -
                            unit.toThis(b.Mean - 0.5 * b.Error, b.unit));
        }
        else {
          bm = b.Error;
        }

        am = a.Error;
        bm = b.Error;
        break;
      case Data.MULTIPLY:
        if (a.unit != null && b.unit != null) {
          u = a.unit.multiply(b.unit);
        }
        am = a.Error * b.Mean;
        bm = b.Error * a.Mean;
        break;
      case Data.DIVIDE:
        if (a.unit != null && b.unit != null) {
          u = a.unit.divide(b.unit);
        }
        factor = Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(b.Mean));
        am = a.Error / factor;
        bm = b.Error * Mean / factor;
        break;
      case Data.INV_DIVIDE:
        if (a.unit != null && b.unit != null) {
          u = b.unit.divide(a.unit);
        }
        factor = Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(a.Mean));
        bm = a.Error * Mean / factor;
        am = b.Error / factor;
        break;
      case Data.POW:
        am = a.Error * Math.abs(Mean) * (b.Mean /
                        Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(a.Mean)));
        factor = Math.log(Math.abs(a.Mean));
        if (Double.isNaN(factor)) factor = 1.0;
        factor = Math.max(DERIVATIVE_LOW_LIMIT,
                          Math.min(DERIVATIVE_HI_LIMIT, factor));
        bm = b.Error * Math.abs(Mean) * factor;
        break;
      case Data.INV_POW:
        factor = Math.log(Math.abs(b.Mean));
        if (Double.isNaN(factor)) factor = 1.0;
        factor = Math.max(DERIVATIVE_LOW_LIMIT,
                          Math.min(DERIVATIVE_HI_LIMIT, factor));
        am = a.Error * Math.abs(Mean) * factor;
        bm = b.Error * Math.abs(Mean) * (a.Mean /
             Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(b.Mean)));
        break;
      case Data.ATAN2:
        factor = Math.min(DERIVATIVE_HI_LIMIT, 1.0 + Mean * Mean) /
                 Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(b.Mean));
        am = a.Error * factor;
        bm = b.Error * Mean * factor;
        break;
      case Data.ATAN2_DEGREES:
        factor = Math.min(DERIVATIVE_HI_LIMIT, 1.0 + Mean * Mean) /
                 Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(b.Mean));
        am = a.Error * factor;
        bm = b.Error * Mean * factor;
        break;
      case Data.INV_ATAN2:
        factor = Math.min(DERIVATIVE_HI_LIMIT, 1.0 + Mean * Mean) /
                 Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(a.Mean));
        am = a.Error * Mean * factor;
        bm = b.Error * factor;
        break;
      case Data.INV_ATAN2_DEGREES:
        factor = Math.min(DERIVATIVE_HI_LIMIT, 1.0 + Mean * Mean) /
                 Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(a.Mean));
        am = a.Error * Mean * factor;
        bm = b.Error * factor;
        break;
      case Data.REMAINDER:
        am = a.Error;
        bm = b.Error * a.Mean /
             Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(b.Mean));
        break;
      case Data.INV_REMAINDER:
        am = a.Error * b.Mean /
             Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(a.Mean));
        bm = b.Error;
        break;
      default:
        throw new ArithmeticException("ErrorEstimate.binary: illegal " +
                                      "operation");
    }
    if (error_mode == Data.INDEPENDENT) {
      error = Math.sqrt(am * am + bm * bm);
    }
    else {
      error = Math.abs(am) + Math.abs(bm);
    }
    if (unit != null && u != null && !unit.equals(u)) {
      // apply Unit conversion to a Error and Mean
      error = Math.abs(unit.toThis(Mean + 0.5 * error, u) -
                       unit.toThis(Mean - 0.5 * error, u));
    }
    return error;
  }

  /** estimate Error for a unary operator with operand a;
      actually, more of a WAG than an estimate;
      these formulas are a bit of a hack and
      suggestions for improvements are welcome */
  private double unary(int op, ErrorEstimate a, int error_mode)
          throws UnitException {
    double factor;
    double error = Double.NaN;
    if (a.isMissing() || error_mode == Data.NO_ERRORS) return error;
    // no difference between Data.INDEPENDENT and Data.DEPENDENT for unary

    switch (op) {
      case Data.ABS:
      case Data.CEIL: // least int greater, represented as a double
      case Data.FLOOR: // greatest int less, represented as a double
      case Data.RINT: // nearest int, represented as a double
      case Data.ROUND: // round double to long
      case Data.NEGATE:
      case Data.NOP:
        if (unit != null && a.unit != null && !unit.equals(a.unit)) {
          // apply Unit conversion to a Error and Mean
          error = Math.abs(unit.toThis(a.Mean + 0.5 * a.Error, a.unit) -
                            unit.toThis(a.Mean - 0.5 * a.Error, a.unit));
        }
        else {
          error = a.Error;
        }
        break;
      case Data.ACOS:
      case Data.ASIN:
        factor = Math.sqrt(1.0 - a.Mean * a.Mean);
        if (Double.isNaN(factor)) factor = 1.0;
        factor = Math.max(DERIVATIVE_LOW_LIMIT,
                          Math.min(DERIVATIVE_HI_LIMIT, factor));
        error = a.Error / factor;
        break;
      case Data.ACOS_DEGREES:
      case Data.ASIN_DEGREES:
        factor = Math.sqrt(1.0 - a.Mean * a.Mean);
        if (Double.isNaN(factor)) factor = 1.0;
        factor = Math.max(DERIVATIVE_LOW_LIMIT,
                          Math.min(DERIVATIVE_HI_LIMIT, factor));
        error = a.Error * Data.RADIANS_TO_DEGREES / factor;
        break;
      case Data.ATAN:
        error = a.Error / Math.min(DERIVATIVE_HI_LIMIT, 1.0 + a.Mean * a.Mean);
        break;
      case Data.ATAN_DEGREES:
        error = a.Error * Data.RADIANS_TO_DEGREES /
                Math.min(DERIVATIVE_HI_LIMIT, 1.0 + a.Mean * a.Mean);
        break;
      case Data.COS:
      case Data.SIN:
        factor = Math.sqrt(1.0 - Mean * Mean);
        if (Double.isNaN(factor)) factor = 1.0;
        factor = Math.max(DERIVATIVE_LOW_LIMIT,
                          Math.min(DERIVATIVE_HI_LIMIT, factor));
        error = a.Error * factor;
        break;
      case Data.COS_DEGREES:
      case Data.SIN_DEGREES:
        factor = Math.sqrt(1.0 - Mean * Mean);
        if (Double.isNaN(factor)) factor = 1.0;
        factor = Math.max(DERIVATIVE_LOW_LIMIT,
                          Math.min(DERIVATIVE_HI_LIMIT, factor));
        error = a.Error * Data.DEGREES_TO_RADIANS * factor;
        break;
      case Data.EXP:
        error = a.Error * Math.abs(Mean);
        break;
      case Data.LOG:
        factor = Math.max(DERIVATIVE_LOW_LIMIT,
                          Math.min(DERIVATIVE_HI_LIMIT, Math.abs(a.Mean)));
        error = a.Error / factor;
        break;
      case Data.SQRT:
        factor = Math.max(DERIVATIVE_LOW_LIMIT,
                          Math.min(DERIVATIVE_HI_LIMIT, 2.0 * Math.abs(Mean)));
        error = a.Error / factor;
        break;
      case Data.TAN:
        error = a.Error * Math.min(DERIVATIVE_HI_LIMIT, 1.0 + Mean * Mean);
        break;
      case Data.TAN_DEGREES:
        error = a.Error * Data.DEGREES_TO_RADIANS *
                Math.min(DERIVATIVE_HI_LIMIT, 1.0 + Mean * Mean);
        break;
      default:
        throw new ArithmeticException("ErrorEstimate.unary: illegal " +
                                      "operation");
    }
    return error;
  }

  public boolean isMissing() {
    return Double.isNaN(Error);
  }

  /**
   * Get the mean value for this error distribution
   */
  public double getMean() {
    return Mean;
  }

  /**
   * Get the variance of this error distribution
   */
  public double getErrorValue() {
    return Error;
  }

  /**
   * Get the number of values in this error distribution
   */
  public long getNumberNotMissing() {
    return NumberNotMissing;
  }

  /**
   * Get the Unit for this error distribution.
   */
  public Unit getUnit() {
    return unit;
  }

  /** initialize matrix for applying (approximate) Jacobean to errors_in */
  static double[][] init_error_values(ErrorEstimate[] errors_in) {
    int n = errors_in.length;
    double[] means = new double[n];
    for (int j=0; j<n; j++) {
      means[j] = errors_in[j].getMean();
    }
    return init_error_values(errors_in, means);
  }

  /** initialize matrix for applying (approximate) Jacobean to errors_in */
  static double[][] init_error_values(ErrorEstimate[] errors_in,
                                      double[] means) {
    int n = errors_in.length;
    double[][] error_values = new double[n][2 * n];
    for (int j=0; j<n; j++) {
      double mean = means[j];
      double error = 0.5 * errors_in[j].getErrorValue();
      for (int i=0; i<n; i++) {
        if (i == j) {
          error_values[j][2 * i] = mean - error;
          error_values[j][2 * i + 1] = mean + error;
        }
        else {
          error_values[j][2 * i] = mean;
          error_values[j][2 * i + 1] = mean;
        }
      }
    }
    return error_values;
  }

  public String toString() {
    return
      "NumberNotMissing = " + NumberNotMissing + "  Error = " +
      (Double.isNaN(Error) ? "missing" : Double.toString(Error)) +
      "  Mean = " +
      (Double.isNaN(Mean) ? "missing" : Double.toString(Mean));
  }

  /**
   * Compares this error estimate to another.
   * @param obj			The other error estimate.  May be <code>null
   *				</code>.
   * @return                    A negative integer, zero, or a positive integer
   *                            depending on whether this ErrorEstimate is
   *                            considered less than, equal to, or greater than
   *                            the other ErrorEstimate, respectively.  If
   *                            <code>obj == null</code>, then a positive value
   *                            is returned.  An ErrorEstimate with no unit is
   *                            considered less than an ErrorEstimate with a
   *                            unit.
   */
  public int compareTo(Object obj) {
    int		comp;
    if (obj == null) {
      comp = 1;		// null ErrorEstimate considered smallest
    }
    else {
      ErrorEstimate	that = (ErrorEstimate)obj;
      if (unit == null)
      {
	if (that.unit == null) {
	  comp = new Double(Error).compareTo(new Double(that.Error));
	}
	else {
	  comp = -1;	// no-unit ErrorEstimate considered smaller than
			// one with unit
	}
      }
      else {
	if (that.unit == null) {
	  comp = 1;	// no-unit ErrorEstimate considered smaller than
			// one with unit
	}
	else {
	  try {
	    comp = new Double(Error).compareTo(
	      new Double(unit.toThis(that.Error, that.unit)));
	  }
	  catch (UnitException e) {
	    comp = +1;	// put problem ErrorEstimate-s at the end
	  }
	}
      }
    }
    return comp;
  }

}

