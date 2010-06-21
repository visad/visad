// $Id: IndexIterator.java,v 1.2 2002-05-29 20:32:39 steve Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package ucar.multiarray;

/**
 * An IndexIterator is a helper class used for stepping through the
 * index values of a MultiArray.
 * <p>
 * This is like an odometer. The number of columns or rings on the odometer
 * is the length of the constructor argument. The number of values on
 * each ring of the odometer is specified in the limits argument of
 * the constructor.
 * <p>
 * Currently no synchronized methods.
 * 
 * @see MultiArray
 * @author $Author: steve $
 * @version $Revision: 1.2 $ $Date: 2002-05-29 20:32:39 $
 */
public class IndexIterator {

	/**
	 * Return <code>true</code> iff the argument
	 * is the zero index.
	 */
	static public boolean
	isZero(int [] iv)
	{
		for(int ii = 0; ii < iv.length; ii++)
			if(iv[ii] != 0)
				return false;
		return true;
	}

	/**
	 * Return <code>true</code> iff the arguments have
	 * same values.
	 */
	static public boolean
	equals(int [] lhs, int [] rhs)
	{
		if(lhs == rhs)
			return true;
		// else
		if(lhs.length != rhs.length)
			return false;
		// else
		for(int ii = 0; ii < lhs.length; ii++)
			if(lhs[ii] != rhs[ii])
				return false;
		return true;
	}

	/**
         * Creates a new IndexIterator whose variation is bounded by the
	 * component values of the argument.
	 * @param theLimits typically <code>ma.getLengths()</code>
	 * for some MultiArray <code>ma</code>
	 */
	public
	IndexIterator(int [] theLimits)
	{
		counter = new int[theLimits.length];
		limits = theLimits; // N.B. Not a copy
		ncycles = 0;
	}

	/**
         * Creates a new IndexIterator with initial counter value,
	 * whose variation is bounded by the
	 * component values of the <code>limits</code> argument.
	 * @param initCounter the initial value.
	 * @param theLimits typically <code>ma.getLengths()</code>
	 * for some MultiArray <code>ma</code>
	 */
	public
	IndexIterator(int [] initCounter, int [] theLimits)
	{
		if(initCounter == null)
			counter = new int[theLimits.length];
		else
			counter = (int []) initCounter.clone();
		limits = theLimits; // N.B. Not a copy
		ncycles = 0;
	}

	/**
	 * If the IndexIterator has not yet "rolled over",
	 * return <code>true</code>.
	 * Useful for loop end detection.
	 */
    	public boolean
	notDone()
	{
		if(ncycles > 0)
			return false;
		return true;
	}

	/**
	 * Return the current counter value.
	 * N.B. Not a copy!
	 */
    	public int []
	value()
	{
		return counter;
	}

	/**
	 * Increment the counter value
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
				break; // normal exit
			// else, carry
			counter[digit] = 0;
			if(digit == 0)
			{
				ncycles++; // rolled over
				break;
			}
			// else
			digit--;
		}
	}

	/**
         * Increment the counter value 
	 * @param nsteps the number of times to increment the value.
	 */
	public void
	advance(int nsteps)
	{
		// TODO: make this smarter and faster;
		while(nsteps-- > 0)
			incr();
	}

	public String
	toString()
	{
		StringBuffer buf = new StringBuffer();
		final int last = counter.length -1;
		for(int ii = 0; ii <= last; ii++)  
		{
			buf.append(counter[ii]);
			if(ii == last)
				break; // normal loop exit
			buf.append(" ");
		}
		return buf.toString();
	}

	/**
	 * Test
	 */
	public static void
	main (String args[])
	{
		/*
		 * Translate the command line args into an array of int.
		 */
		int [] edges = new int[args.length];
		int ii;
		for(ii = 0; ii < args.length; ii++)
		{
			final Integer av = new Integer(args[ii]);
			edges[ii] = av.intValue();
		}
		System.out.println(edges);

		
		/*
		 * Example usage
		 */
		IndexIterator odo = new IndexIterator(edges);
		for(ii = 0; odo.notDone(); odo.incr(), ii++)
		{
			System.out.println(odo);
		}
		System.out.print("\t");
		System.out.println(ii);
	}

	/**
	 * The counter value. Initialized to zero. The length
	 * is the same as limits.length.
         */
	protected final int[] counter;

	/**
	 * (Reference to) the constructor argument which determines
	 * counter value variation.
	 * <code>for 0 <= ii < limits.length, counter[ii] < limits[ii]</code>
	 */
	protected final int[] limits;

	/**
	 * A "carry" indicator,
	 * the number of times the counter value has rolled over.
	 */
	protected int ncycles;
}
