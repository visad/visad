/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: LonArithProg.java,v 1.4 1998-02-23 15:58:20 steve Exp $
 */

package visad.data.netcdf;


/**
 * Arithmetic progression in longitude class.  Useful for determining if
 * a sequence of longitudes corresponds to an arithmetic progression and 
 * what that progression is.
 */
class
LonArithProg
    extends	ArithProg
{
    /**
     * The sum of the individual deltas.
     */
    protected double	sumDelta = 0;


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
     * @require		isConsistent() is true.
     * @promise		A subsequent getNumber() will return one more than 
     *			previously if the function returns true.
     * @promise		A subsequent getLast() will return the transformed
     *			value argument if the function returns true.
     */
    boolean
    accumulate(double value)
    {
	if (consistent)
	{
	    if (n == 0)
		first = value;
	    else
	    if (n == 1)
	    {
		increment = getDelta(value, last);
		sumDelta = increment;
	    }
	    else
	    {
		double	delta = getDelta(value, last);
		double	eps = increment == 0
					? delta
					: 1.0 - delta / increment;

		if (Math.abs(eps) <= epsilon)
		{
		    sumDelta += delta;
		    increment = sumDelta / n;
		}
		else
		{
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
     * Compute the delta from a previous value.
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
     */
    double
    getLast()
    {
	return first + sumDelta;
    }


    /**
     * Test this class.
     *
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
