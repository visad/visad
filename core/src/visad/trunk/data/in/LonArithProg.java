/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: LonArithProg.java,v 1.2 2001-03-29 21:08:59 steve Exp $
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
	LonArithProg	ap = new LonArithProg();

	ap.accumulate(new double[] {175.0, 180.0, -175.0});

	System.out.println("ap.isConsistent()=" + ap.isConsistent());
	System.out.println("ap.getFirst()=" + ap.getFirst());
	System.out.println("ap.getLast()=" + ap.getLast());
	System.out.println("ap.getNumber()=" + ap.getNumber());
	System.out.println("ap.getCommonDifference()=" + 
	    ap.getCommonDifference());
    }
}
