/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

$Id: ArithProg.java,v 1.11 2006-01-19 19:12:14 curtis Exp $
*/

package visad.data.in;

import visad.VisADException;


/**
 * Provides support for determining if a sequence of numeric values corresponds
 * to an arithmetic progression and, if so, the progression parameters.
 *
 * @author Steven R. Emmerson
 */
public class ArithProg
{
    protected long		n;
    protected double		first = Double.NaN;
    protected double		last = Double.NaN;
    protected double            meanDel = Double.NaN;
    protected boolean		isConsistent = true;
    protected final double	fEps = 5e-5f;	// 5 * C FLT_EPS
    protected final double	dEps = 5e-9;	// 5 * C DBL_EPS
    private final double[]      dValues = new double[1];
    private final float[]       fValues = new float[1];

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
		meanDel = value - first;
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
		meanDel = value - first;
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
	double err = value - (last + meanDel);
	if (err*err > var)
	{
	    isConsistent = false;
	}
	else
	{
	    meanDel = (value - first) / n;
	}
    }

    /**
     * Accumulates a value.  Indicate whether or not the value is
     * consistent with the arithmetic progression so far.
     *
     * @param value		The value to accumulate.
     * @return			False if the difference between any current
     *				and previous value normalized by the current
     *				increment differs from unity by more than the
     *				nearness threshold; otherwise, true.
     * @throws VisADException	The sequence isn't an arithmetic progression.
     * @precondition		isConsistent() is true.
     * @postcondition		A subsequent getNumber() will return
     *				<code>values.length</code> more than previously
     *				if the function returns true.
     * @postcondition		A subsequent getLast() will return the
     *				value argument if the function returns true.
     */
    public final synchronized boolean accumulate(float value)
	throws VisADException
    {
	if (!isConsistent())
	    throw new VisADException(
		getClass().getName() + ".accumulate(double): " +
		"Sequence isn't an arithmetic progression");
        fValues[0] = value;
	return accumulate(fValues);
    }

    /**
     * Accumulates a value.  Indicate whether or not the value is
     * consistent with the arithmetic progression so far.
     *
     * @param value		The value to accumulate.
     * @return			False if the difference between any current
     *				and previous value normalized by the current
     *				increment differs from unity by more than the
     *				nearness threshold; otherwise, true.
     * @throws VisADException	The sequence isn't an arithmetic progression.
     * @precondition		isConsistent() is true.
     * @postcondition		A subsequent getNumber() will return
     *				<code>values.length</code> more than previously
     *				if the function returns true.
     * @postcondition		A subsequent getLast() will return the
     *				value argument if the function returns true.
     */
    public final synchronized boolean accumulate(double value)
	throws VisADException
    {
	if (!isConsistent())
	    throw new VisADException(
		getClass().getName() + ".accumulate(double): " +
		"Sequence isn't an arithmetic progression");
        dValues[0] = value;
	return accumulate(dValues);
    }

    /**
     * Indicates whether or not the sequence so far is consistent with an
     * arithmetic progression.
     *
     * @return			<code>true</code> if and only if the sequence
     *				of values seen so far is consistent with an
     *				arithmetic progression.
     */
    public synchronized final boolean isConsistent() 
    {
	return isConsistent;
    }

    /**
     * Gets the number of values.  Only meaningfull if {@link #isConsistent()}
     * is true.
     *
     * @return			The number of values accumulated so far.
     * @throws VisADException	The sequence isn't an arithmetic progression.
     * @require			isConsistent() is true.
     */
    public synchronized final long getNumber() 
	throws VisADException
    {
	if (!isConsistent())
	    throw new VisADException(
		getClass().getName() + ".getNumber(): " +
		"Sequence isn't an arithmetic progression");
	return n;
    }

    /**
     * Gets the first value.
     *
     * @return			The first accumulated value.
     * @throws VisADException	The sequence isn't an arithmetic progression.
     * @require			isConsistent() is true.
     */
    public synchronized final double getFirst() 
	throws VisADException
    {
	if (!isConsistent())
	    throw new VisADException(
		getClass().getName() + ".getFirst(): " +
		"Sequence isn't an arithmetic progression");
	return first;
    }

    /**
     * Returns the "last" value accumulated.  It is only meaningfull if
     * {@link #isConsistent} is true.
     *
     * @return			The last accumulated value.
     * @throws VisADException	The sequence isn't an arithmetic progression.
     */
    public synchronized final double getLast()
	throws VisADException
    {
	if (!isConsistent())
	    throw new VisADException(
		getClass().getName() + ".getLast(): " +
		"Sequence isn't an arithmetic progression");
	return last;
    }

    /**
     * Gets the current common difference.  Only meaningfull if {@link 
     * #isConsistent()} is true.
     *
     * @return			The computed common difference so far.
     * @throws VisADException	The sequence isn't an arithmetic progression.
     * @require			isConsistent() is true.
     */
    public synchronized final double getCommonDifference() 
	throws VisADException
    {
	if (!isConsistent())
	    throw new VisADException(
		getClass().getName() + ".getCommonDifference(): " +
		"Sequence isn't an arithmetic progression");
	return meanDel;
    }

    public static void main(String[] args)
        throws Exception
    {
	double[] lats = {
	    39.2, 39.21, 39.22, 39.23, 39.24, 39.25, 39.26, 39.27, 39.28, 39.29,
	    39.3, 39.31, 39.32, 39.33, 39.34, 39.35, 39.36, 39.37, 39.38, 39.39,
	    39.4, 39.41, 39.42, 39.43, 39.44, 39.45, 39.46, 39.47, 39.48, 39.49,
	    39.5, 39.51, 39.52, 39.53, 39.54, 39.55, 39.56, 39.57, 39.58, 39.59,
	    39.6, 39.61, 39.62, 39.63, 39.64, 39.65, 39.66, 39.67, 39.68, 39.69,
	    39.7, 39.71, 39.72, 39.73, 39.74, 39.75, 39.76, 39.77, 39.78, 39.79,
	    39.8, 39.81, 39.82, 39.83, 39.84, 39.85, 39.86, 39.87, 39.88, 39.89,
	    39.9, 39.91, 39.92, 39.93, 39.94, 39.95, 39.96, 39.97, 39.98, 39.99,
	    40 };
	ArithProg ap = new ArithProg();
	ap.accumulate(lats);
    }
}
