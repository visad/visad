/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

$Id: ArithProg.java,v 1.1 2001-03-15 15:53:29 steve Exp $
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
    private long		n = 0;
    private double		first = Double.NaN;
    private double		last = Double.NaN;
    private double		sumDelta = 0;
    private double 		commDiff = Double.NaN;
    private boolean		isConsistent = true;
    private final double	epsilon;

    /**
     * Constructs with a default nearness threshold.  The default value
     * is 2e-9 (twice the C DBL_EPSILON).
     */
    public ArithProg()
    {
	this(2e-9);	// twice the C DBL_EPSILON
    }

    /**
     * Constructs with a caller-supplied nearness threshold.
     *
     * @param epsilon		Nearness threshold.
     * @throws IllegalArgumentException
     *				The given nearness threshold is negative.
     */
    public ArithProg(double epsilon)
    {
	if (epsilon < 0)
	    throw new IllegalArgumentException("epsilon < 0");
	this.epsilon = epsilon;
    }

    /**
     * Accumulates a set of values.  Indicate whether or not the values are
     * consistent with the arithmetic progression so far.
     *
     * @param values		The values to accumulate.
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
    public final boolean accumulate(float[] values)
	throws VisADException
    {
	if (!isConsistent())
	    throw new VisADException(
		getClass().getName() + ".accumulate(float[]): " +
		"Sequence isn't an arithmetic progression");
	for (int i = 0; i < values.length && isConsistent; ++i)
	    accum(values[i]);
	return isConsistent;
    }

    /**
     * Accumulates a set of values.  Indicate whether or not the values are
     * consistent with the arithmetic progression so far.
     *
     * @param values		The values to accumulate.
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
    public final boolean accumulate(double[] values)
	throws VisADException
    {
	if (!isConsistent())
	    throw new VisADException(
		getClass().getName() + ".accumulate(double[]): " +
		"Sequence isn't an arithmetic progression");
	for (int i = 0; i < values.length && isConsistent; ++i)
	    accum(values[i]);
	return isConsistent;
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
    public final boolean accumulate(double value)
	throws VisADException
    {
	if (!isConsistent())
	    throw new VisADException(
		getClass().getName() + ".accumulate(double): " +
		"Sequence isn't an arithmetic progression");
	return accum(value);
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
     * @precondition		isConsistent() is true.
     * @postcondition		A subsequent getNumber() will return
     *				<code>values.length</code> more than previously
     *				if the function returns true.
     * @postcondition		A subsequent getLast() will return the
     *				value argument if the function
     *				returns true.
     */
    protected synchronized final boolean accum(double value)
    {
	if (n == 0)
	{
	    first = value;
	}
	else if (n == 1)
	{
	    double	delta = delta(last, value);
	    commDiff = delta;
	    sumDelta = delta;
	}
	else
	{
	    double	delta = delta(last, value);
	    double	eps =
		commDiff == 0
		    ? delta
		    : 1.0 - delta / commDiff;
	    if (Math.abs(eps) <= epsilon)
	    {
		sumDelta += delta;
		commDiff = sumDelta / n;
	    }
	    else
	    {
		isConsistent = false;
		commDiff = Double.NaN;
	    }
	}
	last = value;
	n++;
	return isConsistent;
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
     * Gets the number of values.  Only meaningfull if {@link isConsistent()}
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
     * {@link isConsistent} is true.
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
     * isConsistent()} is true.
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
	return commDiff;
    }

    /**
     * Returns the nearness threshold value.
     *
     * @return			The nearness threshold used during construction.
     */
    public synchronized final double getEpsilon()
    {
	return epsilon;
    }

    /**
     * Returns the difference between two values.  This is a template method.
     *
     * @param value1		The first value.
     * @param value2		The second value.
     * @return			The increment from the first value to the
     *				second value.
     */
    protected double delta(double value1, double value2)
    {
	return value2 - value1;
    }
}
