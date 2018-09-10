/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
*/

package visad.data.dods;

import visad.DataImpl;

public class FullSizeTest
{
    public static void main(String[] args)
	throws Exception
    {
	String	urlSpec =
	    args.length > 0
		? args[0]
		: "http://www.unidata.ucar.edu/cgi-bin/dods/test2/nph-nc/" +
		  "packages/dods/data/nc_test/COADS-climatology.nc";
	Runtime	runtime = Runtime.getRuntime();
	long	free1 = runtime.freeMemory();
	long	total1 = runtime.totalMemory();
	long	used1 = total1 - free1;
	System.out.println("Before DODS total/free/used memory: " + 
	    total1 + '/' + free1 + '/' + used1);
	DODSForm	form = DODSForm.dodsForm();
	DataImpl	data = form.open(urlSpec);
	long	free2 = runtime.freeMemory();
	long	total2 = runtime.totalMemory();
	long	used2 = total2 - free2;
	System.out.println("After DODS total/free/used memory:  " + 
	    total2 + '/' + free2 + '/' + used2);
	System.out.println("Used difference = " + (used2 - used1));
    }
}
