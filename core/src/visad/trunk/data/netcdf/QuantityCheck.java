package visad.data.netcdf;

import java.util.Iterator;
import visad.Unit;

class 
QuantityCheck
{
    public static void
    main(String[] args)
	throws Exception
    {
	StandardQuantityDB	db = StandardQuantityDB.instance();
	Iterator		iter = db.getIterator();

	while (iter.hasNext())
	{
	    Quantity	quantity = (Quantity)iter.next();
	    Unit	unit	= quantity.getDefaultUnit();
	    Quantity[]	quantities = db.get(unit);

	    if (quantities.length >= 2)
	    {
		for (int i = 0; i < quantities.length; ++i)
		    System.out.print(quantities[i].toString() + " ");
		System.out.println("");
	    }
	}
    }
}
