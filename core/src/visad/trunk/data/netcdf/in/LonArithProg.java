/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: LonArithProg.java,v 1.2 1998-06-17 20:30:24 visad Exp $
 */

package visad.data.netcdf.in;


/**
 * The LonArithProg class provides a way to determine if a sequence of
 * values is an arithmetic progression of longitude values and, if so, just
 * what that progression is.
 */
class
LonArithProg
    extends	ArithProg
{
    /**
     * The sum of the individual deltas.
     */
    private double	sumDelta = 0;


    /**
     * Construct with a default nearness threshold.
     */
    LonArithProg()
    {
    }


    /**
     * Construct with a caller-supplied nearness threshold.
     *
     * @param epsilon	Nearness threshold.
     * @exception IllegalArgumentException
     *			The given nearness threshold is negative.
     */
    LonArithProg(double epsilon)
    {
	super(epsilon);
    }


    /*
     * TODO: Turn ArithProg into a Template Method:
     *     Add getDelta() method to ArithProg.
     *     Modify ArithProg.accumulate() accordingly.
     *     Eliminate LonArithProg.accumulate()
     */


    /**
     * Accumulate another value.  Indicate whether or not the value is
     * consistent with the arithmetic progression so far.
     *
     * @param value	The current value to accumulate.
     * @return		False if the difference between the
     *			current and previous values normalized by the current
     *			increment differs from unity by more than the
     *			nearness threshold; otherwise, true.
     * @precondition	isConsistent() is true.
     * @postcondition	A subsequent getNumber() will return one more than 
     *			previously if the function returns true.
     * @postcondition	A subsequent getLast() will return the transformed
     *			value argument if the function returns true.
     */
    boolean
    accumulate(double value)
    {
	if (isConsistent())
	{
	    long	n = getNumber();

	    if (n == 0)
		setFirst(value);
	    else
	    if (n == 1)
	    {
		double	increment = getDelta(value, getLast());

		setIncrement(increment);
		sumDelta = increment;
	    }
	    else
	    {
		double	delta = getDelta(value, getLast());
		double	eps = getIncrement() == 0
					? delta
					: 1.0 - delta / getIncrement();

		if (Math.abs(eps) <= getEpsilon())
		{
		    sumDelta += delta;
		    setIncrement(sumDelta / n);
		}
		else
		{
		    setConsistent(false);
		    setIncrement(Double.NaN);
		}
	    }
	}

	setLast(value);
	incrementNumber();

	return isConsistent();
    }


    /**
     * Compute the delta from a previous value.
     *
     * @param value	The current value.
     * @param last	The previous value.
     * @return		The minimum magnitude difference between the current
     *			and previous values.
     */
    protected static double
    getDelta(double value, double last)
    {
	double	delta = (value - last) % 360.0;

	if (delta < -180.0)
	    delta += 360.0;
	else
	if (delta >  180.0)
	    delta -= 360.0;

	return delta;
    }


    /**
     * Return the (transformed) "last" value.  This value is equivalent
     * to the last value given to accumulate() after adding up all
     * the increments.  It is only meaningfull if isConsistent is true.
     *
     * @return	The last value.
     */
    double
    getLast()
    {
	return getFirst() + sumDelta;
    }


    /**
     * Test this class.
     *
     * @param args		Runtime arguments.  Ignored.
     * @exception Exception	Something went wrong.
     */
    public static void main(String[] args)
	throws Exception
    {
	LonArithProg	ap = new LonArithProg();

	ap.accumulate(175.0);
	ap.accumulate(180.0);
	ap.accumulate(-175.0);

	System.out.println("ap.isConsistent()=" + ap.isConsistent());
	System.out.println("ap.getFirst()=" + ap.getFirst());
	System.out.println("ap.getLast()=" + ap.getLast());
	System.out.println("ap.getNumber()=" + ap.getNumber());
    }
}
