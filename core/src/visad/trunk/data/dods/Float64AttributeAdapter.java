package visad.data.dods;

import dods.dap.*;
import java.util.*;
import visad.*;

/**
 * Instances are immutable;
 */
public class Float64AttributeAdapter
    extends	NumericAttributeAdapter
{
    protected Float64AttributeAdapter(String name, Attribute attr)
	throws VisADException
    {
	super(name, attr);
    }

    protected Number number(String spec)
    {
	return new Double(doubleValue(spec));
    }

    protected double doubleValue(String spec)
    {
	return Double.parseDouble(spec);
    }

    protected visad.Set visadSet(List list)
	throws VisADException
    {
	double[]	values = new double[list.size()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = ((Double)list.get(i)).doubleValue();
	boolean		isSorted;
	{
	    int		i = 1;
	    if (values[0] < values[1])
		while (i < values.length-1)
		    if (values[i] > values[++i])
			break;
	    else
		while (i < values.length-1)
		    if (values[i] < values[++i])
			break;
	    isSorted = i == values.length - 1;
	}
	return
	    isSorted
		? (visad.Set)new Gridded1DDoubleSet(
		    getRealType(), new double[][] {values}, values.length)
		: new List1DDoubleSet(values, getRealType(), null, null);
    }
}
