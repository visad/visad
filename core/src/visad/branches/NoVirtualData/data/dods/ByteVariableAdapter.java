package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DByte variables to the
 * {@link visad.data.in} context.
 */
public class ByteVariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private ByteVariableAdapter(DByte var, AttributeTable table)
	throws VisADException, RemoteException
    {
	realType = realType(var, table);
	valuator = Valuator.valuator(table);
	int	min = (int)Math.max(valuator.getMin(), -128);
	int	max = (int)Math.min(valuator.getMax(), 255);
	repSets =
	    new SimpleSet[] {
		min == 0
		    ? (SimpleSet)new Integer1DSet(realType, max)
		    : new Linear1DSet(realType, min, max, (max-min)+1)};
    }

    public static ByteVariableAdapter byteVariableAdapter(
	    DByte var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return new ByteVariableAdapter(var, table);
    }

    public MathType getMathType()
    {
	return realType;
    }

    public SimpleSet[] getRepresentationalSets()
	throws VisADException
    {
	return repSets;
    }

    public DataImpl data(DByte var)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
