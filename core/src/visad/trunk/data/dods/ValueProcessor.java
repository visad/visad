package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.data.BadFormException;
import visad.*;

public abstract class ValueProcessor
    extends	visad.data.in.ValueProcessor
{
    protected static double decode(String name, AttributeTable table, int index)
	throws BadFormException, VisADException, RemoteException
    {
	double		value = Double.NaN;	// default value
	Attribute	attr = table.getAttribute(name);
	if (attr != null)
	{
	    DataImpl	data =
		AttributeAdapterFactory.attributeAdapterFactory()
		    .attributeAdapter(name, attr).data();
	    if (data instanceof Real && index == 0)
		value = ((Real)data).getValue();
	    else if (data instanceof Gridded1DDoubleSet)
		value =
		    ((Gridded1DSet)data).indexToDouble(new int[] {index})[0][0];
	    else if (data instanceof Gridded1DSet)
		value =
		    ((Gridded1DSet)data).indexToValue(new int[] {index})[0][0];
	}
	return value;
    }
}
