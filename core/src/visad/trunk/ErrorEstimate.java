
//
// ErrorEstimate.java
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

/**
   ErrorEstimate is the VisAD class for statistics about a value
   or array of values.<P>
*/
public class ErrorEstimate extends Object implements java.io.Serializable {

  double Error;
  double Mean;
  long NumberNotMissing;
  Unit unit;

  /** these are bounds used in estimates of derivatives;
      they should be reciprocals;
      they are not applied in all cases, or uniformly */
  private static final double DERIVATIVE_LOW_LIMIT = 0.01;
  private static final double DERIVATIVE_HI_LIMIT = 1.0 / DERIVATIVE_LOW_LIMIT;

  /** simple constructor */
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

  /** construct Error for a single value with an error */
  public ErrorEstimate(double value, double error, Unit u) {
    unit = u;
    if (Double.isNaN(value)) {
      NumberNotMissing = 0;
      Mean = Double.NaN;
      Error = Double.NaN;
    }
    else {
      NumberNotMissing = 1;
      Mean = value;
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
    NumberNotMissing = nf + inc;
    if (NumberNotMissing > 0) {
      double mean = 0.0;
      double error = 0.0;
      if (!Double.isNaN(ef)) error += nf * ef;
      if (!Double.isNaN(mf)) mean += nf * mf;
      if (!Double.isNaN(es)) error += ns * es;
      if (!Double.isNaN(ms)) mean += ns * ms;
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
                       int error_mode) {
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
    NumberNotMissing = 0;
    double sum = 0.0;
/* TDR May 18 1998
    for (int i=0; i<NumberNotMissing; i++) {
*/
    for (int i=0; i<value.length; i++) {
      if (!Double.isNaN(value[i])) {
        NumberNotMissing++;
        sum += value[i];
      }
    }
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
    NumberNotMissing = 0;
    double sum = 0.0;
/* TDR May 18 1998
    for (int i=0; i<NumberNotMissing; i++) {
*/
    for (int i=0; i<value.length; i++) {
      if (!Float.isNaN(value[i])) {
        NumberNotMissing++;
        sum += value[i];
      }
    }
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
    NumberNotMissing = 0;
    double sum = 0.0;
/* TDR May 18 1998
    for (int i=0; i<NumberNotMissing; i++) {
*/
    for (int i=0; i<value.length; i++) {
      if (!Double.isNaN(value[i])) {
        NumberNotMissing++;
        sum += value[i];
      }
    }
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
    NumberNotMissing = 0;
    double sum = 0.0;
/* TDR May 18 1998
    for (int i=0; i<NumberNotMissing; i++) {
*/
    for (int i=0; i<value.length; i++) {
      if (!Float.isNaN(value[i])) {
        NumberNotMissing++;
        sum += value[i];
      }
    }
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
                       int error_mode) {
    unit = u;
    NumberNotMissing = 0;
    double sum = 0.0;
/* TDR May 18 1998
    for (int i=0; i<NumberNotMissing; i++) {
*/
    for (int i=0; i<value.length; i++) {
      if (!Double.isNaN(value[i])) {
        NumberNotMissing++;
        sum += value[i];
      } 
    } 
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
                       int error_mode) {
    unit = u;
    NumberNotMissing = 0;
    double sum = 0.0;
/* TDR May 18 1998
    for (int i=0; i<NumberNotMissing; i++) {
*/
    for (int i=0; i<value.length; i++) {
      if (!Float.isNaN(value[i])) {
        NumberNotMissing++;
        sum += value[i];
      }
    }
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
    double bError, bMean;
    double error = Double.NaN;
    if (a.isMissing() || b.isMissing() ||
        error_mode == Data.NO_ERRORS) return error;
    if (a.unit != null && b.unit != null && !a.unit.equals(b.unit)) {
      // apply Unit conversion to b Error and Mean
      bMean = a.unit.toThis(b.Mean, b.unit);
      bError = Math.abs(a.unit.toThis(b.Mean + 0.5 * b.Error, b.unit) -
                        a.unit.toThis(b.Mean - 0.5 * b.Error, b.unit));
    }
    else {
      bMean = b.Mean;
      bError = b.Error;
    }
    switch (op) {
      case Data.ADD:
      case Data.SUBTRACT:
      case Data.INV_SUBTRACT:
      case Data.MAX:
      case Data.MIN:
        am = a.Error;
        bm = bError;
        break;
      case Data.MULTIPLY:
        am = a.Error * bMean;
        bm = bError * bMean;
        break;
      case Data.DIVIDE:
        factor = Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(bMean));
        am = a.Error / factor;
        bm = bError * Mean / factor;
        break;
      case Data.INV_DIVIDE:
        factor = Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(a.Mean));
        bm = a.Error * Mean / factor;
        am = bError / factor;
        break;
      case Data.POW:
        am = a.Error * Math.abs(Mean) * (bMean /
                        Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(a.Mean)));
        factor = Math.log(Math.abs(a.Mean));
        if (Double.isNaN(factor)) factor = 1.0;
        factor = Math.max(DERIVATIVE_LOW_LIMIT,
                          Math.min(DERIVATIVE_HI_LIMIT, factor));
        bm = bError * Math.abs(Mean) * factor;
        break;
      case Data.INV_POW:
        factor = Math.log(Math.abs(bMean));
        if (Double.isNaN(factor)) factor = 1.0;
        factor = Math.max(DERIVATIVE_LOW_LIMIT,
                          Math.min(DERIVATIVE_HI_LIMIT, factor));
        am = a.Error * Math.abs(Mean) * factor;
        bm = bError * Math.abs(Mean) * (a.Mean /
             Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(bMean)));
        break;
      case Data.ATAN2:
        factor = Math.min(DERIVATIVE_HI_LIMIT, 1.0 + Mean * Mean) /
                 Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(bMean));
        am = a.Error * factor;
        bm = bError * Mean * factor;
        break;
      case Data.ATAN2_DEGREES:
        factor = Math.min(DERIVATIVE_HI_LIMIT, 1.0 + Mean * Mean) /
                 Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(bMean));
        am = a.Error * factor;
        bm = bError * Mean * factor;
        break;
      case Data.INV_ATAN2:
        factor = Math.min(DERIVATIVE_HI_LIMIT, 1.0 + Mean * Mean) /
                 Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(a.Mean));
        am = a.Error * Mean * factor;
        bm = bError * factor;
        break;
      case Data.INV_ATAN2_DEGREES:
        factor = Math.min(DERIVATIVE_HI_LIMIT, 1.0 + Mean * Mean) /
                 Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(a.Mean));
        am = a.Error * Mean * factor;
        bm = bError * factor;
        break;
      case Data.REMAINDER:
        am = a.Error;
        bm = bError * a.Mean /
             Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(bMean));
        break;
      case Data.INV_REMAINDER:
        am = a.Error * bMean /
             Math.max(DERIVATIVE_LOW_LIMIT, Math.abs(a.Mean));
        bm = bError;
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
    return error;
  }

  /** estimate Error for a unary operator with operand a;
      actually, more of a WAG than an estimate;
      these formulas are a bit of a hack and
      suggestions for improvements are welcome */
  private double unary(int op, ErrorEstimate a, int error_mode) {
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
        error = a.Error;
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

  public double getMean() {
    return Mean;
  }

  public double getErrorValue() {
    return Error;
  }

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

}

