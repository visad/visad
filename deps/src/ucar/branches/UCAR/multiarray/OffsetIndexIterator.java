/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.lang.reflect.Array;

/**
 * An IndexIterator where the lower bound is non-zero.
 *
 * @see IndexIterator
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:43:06 $
 */

public class
OffsetIndexIterator
	extends IndexIterator
{

	public
	OffsetIndexIterator(int [] theOffset, int [] theLimits)
	{
		super(theOffset, theLimits);
		offset = theOffset; // N.B. Not a copy
	}

	/**
	 * Increment the odometer
	 */
	public void
	incr()
	{
		int digit = counter.length -1;
		if(digit < 0)
		{
			// counter is zero length array <==> scalar
			ncycles++;
			return;
		}
		while(digit >= 0)
		{
			counter[digit]++;
			if(counter[digit] < limits[digit])
			{
				break; // normal exit
			}
			// else, carry
			counter[digit] = offset[digit];
			if(digit == 0)
			{
				ncycles++; // rolled over
				break;
			}
			// else
			digit--;
		}
	}

	private final int[] offset;
}
