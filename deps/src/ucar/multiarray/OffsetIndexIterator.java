// $Id: OffsetIndexIterator.java,v 1.2 2002-05-29 20:32:40 steve Exp $
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
import java.lang.reflect.Array;

/**
 * An IndexIterator where the lower bound is non-zero.
 *
 * @see IndexIterator
 *
 * @author $Author: steve $
 * @version $Revision: 1.2 $ $Date: 2002-05-29 20:32:40 $
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
