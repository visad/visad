/*
 * Copyright 1998, University Corporation for Atmospheric Research.
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ArithProg.java,v 1.5 1998-03-12 22:02:53 steve Exp $
 */


package visad.data.netcdf;


/**
 * The arithmetic progression class is useful for determining if a sequence
 * of numeric values corresponds to an arithmetic progression and, if so,
 * just what that progression is.
 */
class ArithProg {
    /** Number of values accumulated. */
    protected int		n = 0;

    /** First value. */
    protected double		first = Double.NaN;

    /** Current last value. */
    protected double		last = Double.NaN;

    /** Current increment. */
    protected double 		increment = Double.NaN;

    /**
     * Whether the sequence is consistent with an arithmetic progression 
     * or not.
     */
    protected boolean		consistent = true;

    /** The difference threshold. */
    protected final double	epsilon;

    /**
     * Construct with a default nearness threshold.  The default value
     * is 2e-9 (twice the C DBL_EPSILON).
     */
    ArithProg() {
	this.epsilon = 2e-9;	// twice the C DBL_EPSILON
    }

    /**
     * Construct with a caller-supplied nearness threshold.
     *
     * @param epsilon	Nearness threshold.
     * @exception IllegalArgumentException
     *			The given nearness threshold is negative.
     */
    ArithProg(double epsilon) {
	if (epsilon < 0) {
	    throw new IllegalArgumentException("epsilon < 0");
	}

	this.epsilon = epsilon;
    }

    /**
     * Accumulate another double value.  Indicate whether or not the value is
     * consistent with the arithmetic progression so far.
     *
     * @param value	The current value to accumulate.
     * @return		False if the difference between the
     *			current and previous values normalized by the current
     *			increment differs from unity by more than the
     *			nearness threshold; otherwise, true.
     * @require		isConsistent() is true.
     * @promise		A subsequent getNumber() will return one more than 
     *			previously if the function returns true.
     * @promise		A subsequent getLast() will return the value argument
     *			if the function returns true.
     */
    boolean accumulate(double value) {
	if (consistent) {
	    if (n == 0) {
		first = value;
	    } else if (n == 1) {
		increment = value - first;
	    } else {
		double	delta = increment == 0
					? value - last
					: 1.0 - (value - last) / increment;

		if (Math.abs(delta) <= epsilon) {
		    increment = (value - first) / n;
		} else {
		    consistent = false;
		    increment = Double.NaN;
		}
	    }
	}

	last = value;
	n++;

	return consistent;
    }

    /**
     * Accumulate a bunch of double values.  Indicate whether or not the 
     * values are consistent with the arithmetic progression so far.
     *
     * @param values	The current values to accumulate.
     * @return		False if the sequence of values is inconsistent with
     *			the arithmetic progression seen so far.
     *			nearness threshold; otherwise, true.
     * @require		isConsistent() is true.
     * @promise		A subsequent getNumber() will return 
     *			<code>values.length</code> more than previously.
     * @promise		A subsequent getLast() will return the last value of
     *			the vector providing the function returns true.
     */
    boolean accumulate(double[] values) {
	for (int i = 0; (i < values.length) && accumulate(values[i]); ++i) ;

	return consistent;
    }

    /**
     * Accumulate a bunch of float values.  Indicate whether or not the 
     * values are consistent with the arithmetic progression so far.
     *
     * @param values	The current values to accumulate.
     * @return		False if the sequence of values is inconsistent with
     *			the arithmetic progression seen so far.
     *			nearness threshold; otherwise, true.
     * @require		isConsistent() is true.
     * @promise		A subsequent getNumber() will return 
     *			<code>values.length</code> more than previously.
     * @promise		A subsequent getLast() will return the last value of
     *			the vector providing the function returns true.
     */
    boolean accumulate(float[] values) {
	for (int i = 0; i < values.length && accumulate(values[i]); ++i) ;

	return consistent;
    }

    /**
     * Indicate whether or not the sequence so far is consistent with an
     * arithmetic progression.
     *
     * @return	True if and only if the sequence of values seen so far is
     *		consistent with an arithmetic progression.
     */
    boolean isConsistent() {
	return consistent;
    }

    /**
     * Get the number of values.
     *
     * @return	The number of values accumulated so far.
     * @require	isConsistent() is true.
     */
    int getNumber() {
	return n;
    }

    /**
     * Get the first value.
     *
     * @return	The first accumulated value.
     * @require	isConsistent() is true.
     */
    double getFirst() {
	return first;
    }

    /**
     * Get the last value.
     *
     * @return	The most recently accumulated value.
     * @require	isConsistent() is true.
     */
    double getLast() {
	return last;
    }

    /**
     * Get the current increment.
     *
     * @return	The computed increment so far.
     * @require	isConsistent() is true.
     */
    double getIncrement() {
	return increment;
    }
}
