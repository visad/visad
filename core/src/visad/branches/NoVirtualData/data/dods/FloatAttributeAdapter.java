package visad.data.dods;

import dods.dap.*;
import java.util.*;
import visad.*;

/**
 * Instances are immutable;
 */
public abstract class FloatAttributeAdapter
    extends	NumericAttributeAdapter
{
    protected FloatAttributeAdapter(String name, Attribute attr)
	throws VisADException
    {
	super(name, attr);
    }

    protected Number number(String spec)
    {
	return new Float(floatValue(spec));
    }

    protected abstract float floatValue(String spec);

    protected visad.Set visadSet(List list)
	throws VisADException
    {
	float[]	values = new float[list.size()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = ((Float)list.get(i)).floatValue();
	boolean		isSorted = true;
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
	float[][]	samples = new float[][] {values};
	return
	    isSorted
		? (visad.Set)new Gridded1DSet(
		    getRealType(), samples, values.length)
		: new Irregular1DSet(getRealType(), samples);
    }
}
