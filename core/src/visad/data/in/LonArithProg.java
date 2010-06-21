/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: LonArithProg.java,v 1.3 2001-11-06 17:37:41 steve Exp $
 */

package visad.data.in;

import visad.VisADException;

/**
 * Provides support for determining if a sequence of values is an arithmetic
 * progression of longitude values and, if so, the progression parameters.
 *
 * @author Steven R. Emmerson
 */
public final class LonArithProg
    extends	ArithProg
{
    private double              sumDel = Double.NaN;

    /**
     * Accumulates a set of floats.  Indicates whether or not the sequence is
     * consistent with the arithmetic progression so far.
     *
     * @param values		The values to accumulate.
     * @return			False if the difference between any current
     *				and previous value normalized by the current
     *				increment differs from unity by more than the
     *				nearness threshold; otherwise, true.
     * @throws VisADException	{@link #isConsistent()} was false before this
     *				method was invoked.
     * @precondition		isConsistent() is true.
     * @postcondition		A subsequent getNumber() will return
     *				<code>values.length</code> more than previously
     *				if the function returns true.
     * @postcondition		A subsequent getLast() will return the
     *				value argument if the function returns true.
     */
    public synchronized boolean accumulate(float[] values)
	throws VisADException
    {
	if (!isConsistent())
	    throw new VisADException(
		getClass().getName() + ".accumulate(float[]): " +
		"Sequence isn't an arithmetic progression");
	for (int i = 0; i < values.length; i++)
	{
	    double value = values[i];
	    if (n == 0)
	    {
		first = value;
	    }
	    else if (n == 1)
	    {
		sumDel = meanDel = delta(first, value);
	    }
	    else if (isConsistent)
	    {
		accum(value, fEps);
	    }
	    last = value;
	    n++;
	}
	return isConsistent;
    }

    /**
     * Accumulates a set of doubles.  Indicates whether or not the sequence is
     * consistent with the arithmetic progression so far.
     *
     * @param values		The values to accumulate.
     * @return			False if the difference between any current
     *				and previous value normalized by the current
     *				increment differs from unity by more than the
     *				nearness threshold; otherwise, true.
     * @throws VisADException	{@link #isConsistent()} was false before this
     *				method was invoked.
     * @precondition		isConsistent() is true.
     * @postcondition		A subsequent getNumber() will return
     *				<code>values.length</code> more than previously
     *				if the function returns true.
     * @postcondition		A subsequent getLast() will return the
     *				value argument if the function returns true.
     */
    public synchronized boolean accumulate(double[] values)
	throws VisADException
    {
	if (!isConsistent())
	    throw new VisADException(
		getClass().getName() + ".accumulate(double[]): " +
		"Sequence isn't an arithmetic progression");
	for (int i = 0; i < values.length; i++)
	{
	    double value = values[i];
	    if (n == 0)
	    {
		first = value;
	    }
	    else if (n == 1)
	    {
		sumDel = meanDel = delta(first, value);
	    }
	    else if (isConsistent)
	    {
		accum(value, dEps);
	    }
	    last = value;
	    n++;
	}
	return isConsistent;
    }

    private void accum(double value, double eps)
    {
	double uncLast = last*eps;
	double uncValue = value*eps;
	double var = uncLast*uncLast + uncValue*uncValue;
	double err = delta(last + meanDel, value);
	if (err*err > var)
	{
	    isConsistent = false;
	}
	else
	{
	    sumDel += delta(last, value);
	    meanDel = sumDel / n;
	}
    }

    /**
     * Returns the difference between two values.
     *
     * @param value1		The first value.
     * @param value2		The second value.
     * @return			The increment from the first value to the
     *				second value.
     */
    private double delta(double value1, double value2)
    {
	double	delta = (value2 - value1) % 360.0;
	if (delta < -180.0)
	    delta += 360.0;
	else if (delta > 180.0)
	    delta -= 360.0;
	return delta;
    }

    /**
     * Tests this class.
     *
     * @param args		Runtime arguments.  Ignored.
     * @throws Exception	Something went wrong.
     */
    public static void main(String[] args)
	throws Exception
    {
	double[] lons = {
	    179.2, 179.21, 179.22, 179.23, 179.24, 179.25, 179.26, 179.27, 179.28, 179.29,
	    179.3, 179.31, 179.32, 179.33, 179.34, 179.35, 179.36, 179.37, 179.38, 179.39,
	    179.4, 179.41, 179.42, 179.43, 179.44, 179.45, 179.46, 179.47, 179.48, 179.49,
	    179.5, 179.51, 179.52, 179.53, 179.54, 179.55, 179.56, 179.57, 179.58, 179.59,
	    179.6, 179.61, 179.62, 179.63, 179.64, 179.65, 179.66, 179.67, 179.68, 179.69,
	    179.7, 179.71, 179.72, 179.73, 179.74, 179.75, 179.76, 179.77, 179.78, 179.79,
	    179.8, 179.81, 179.82, 179.83, 179.84, 179.85, 179.86, 179.87, 179.88, 179.89,
	    179.9, 179.91, 179.92, 179.93, 179.94, 179.95, 179.96, 179.97, 179.98, 179.99,
	    180,
	    -179.99, -179.98, -179.97, -179.96, -179.95, -179.94, -179.93, -179.92, -179.91, -179.9,
	    -179.89, -179.88, -179.87, -179.86, -179.85, -179.84, -179.83, -179.82, -179.81, -179.8,
	    -179.79, -179.78, -179.77, -179.76, -179.75, -179.74, -179.73, -179.72, -179.71, -179.7,
	    -179.69, -179.68, -179.67, -179.66, -179.65, -179.64, -179.63, -179.62, -179.61, -179.6,
	    -179.59, -179.58, -179.57, -179.56, -179.55, -179.54, -179.53, -179.52, -179.51, -179.5,
	    -179.49, -179.48, -179.47, -179.46, -179.45, -179.44, -179.43, -179.42, -179.41, -179.4,
	    -179.39, -179.38, -179.37, -179.36, -179.35, -179.34, -179.33, -179.32, -179.31, -179.3,
	    -179.29, -179.28, -179.27, -179.26, -179.25, -179.24, -179.23, -179.22, -179.21, -179.2};
	LonArithProg	ap = new LonArithProg();

	ap.accumulate(lons);

	System.out.println("ap.isConsistent()=" + ap.isConsistent());
	System.out.println("ap.getFirst()=" + ap.getFirst());
	System.out.println("ap.getLast()=" + ap.getLast());
	System.out.println("ap.getNumber()=" + ap.getNumber());
	System.out.println("ap.getCommonDifference()=" + 
	    ap.getCommonDifference());
    }
}
