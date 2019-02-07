/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

import visad.*;

/**
 * Tests {@link DODSForm}.
 *
 * @author Steven R. Emmerson
 */
public class DODSFormTest
{
    public static void main(String[] args)
	throws Exception
    {
	if (args.length < 1 || args.length > 2)
	{
	    System.err.println("Usage: [-v] DODS_dataset_spec");
	    System.err.println(
		"e.g. http://www.unidata.ucar.edu/cgi-bin/dods/nph-nc/" +
		"packages/dods/data/nc_test/COADS-climatology.nc.dods");
	}
	else
	{
	    boolean	verbose;
	    String	spec;
	    if (args.length == 1)
	    {
		verbose = false;
		spec = args[0];
	    }
	    else
	    {
		verbose = args[0].equals("-v");
		spec = args[1];
	    }
	    Runtime	runtime = Runtime.getRuntime();
	    long	free1 = runtime.freeMemory();
	    long	total1 = runtime.totalMemory();
	    long	used1 = total1 - free1;
	    System.out.println("Before DODS total/free/used memory: " + 
		total1 + '/' + free1 + '/' + used1);
	    DODSForm	form = DODSForm.dodsForm();
            try {
	        DataImpl	data = form.open(spec);
	        long	free2 = runtime.freeMemory();
	        long	total2 = runtime.totalMemory();
	        long	used2 = total2 - free2;
	        if (verbose)
		    System.out.println(data.toString());
	        else
		    visad.jmet.DumpType.dumpDataType(data, System.out);
	        System.out.println("After DODS total/free/used memory:  " + 
		    total2 + '/' + free2 + '/' + used2);
	        System.out.println("Used difference = " + (used2 - used1));
            } catch (Exception e) {
                System.err.println("Unable to open \"" + spec + "\": " +
                    e.getMessage());
            }
	}
    }
}
