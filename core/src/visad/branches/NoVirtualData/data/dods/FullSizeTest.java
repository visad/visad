package visad.data.dods;

import dods.dap.*;
import visad.DataImpl;

public class FullSizeTest
{
    public static void main(String[] args)
	throws Exception
    {
	Runtime	runtime = Runtime.getRuntime();
	long	free1 = runtime.freeMemory();
	long	total1 = runtime.totalMemory();
	long	used1 = total1 - free1;
	System.out.println("Before DODS total/free/used memory: " + 
	    total1 + '/' + free1 + '/' + used1);
	DODSForm	form = DODSForm.dodsForm();
	DataImpl	data =
	    form.open(
		"http://www.unidata.ucar.edu/cgi-bin/dods/test2/nph-nc/" +
		"packages/dods/data/nc_test/COADS-climatology.nc");
	long	free2 = runtime.freeMemory();
	long	total2 = runtime.totalMemory();
	long	used2 = total2 - free2;
	System.out.println("After DODS total/free/used memory:  " + 
	    total2 + '/' + free2 + '/' + used2);
	System.out.println("Used difference = " + (used2 - used1));
    }
}
