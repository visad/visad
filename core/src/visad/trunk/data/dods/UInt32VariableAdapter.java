package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DUInt32 variables to the
 * {@link visad.data.in} context.
 */
public class UInt32VariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private UInt32VariableAdapter(DUInt32 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	realType = realType(var, table);
	valuator = Valuator.valuator(table);
	long		min = (long)Math.max(valuator.getMin(),          0);
	long		max = (long)Math.min(valuator.getMax(), 4294967295L);
	SimpleSet	repSet;
	if (min == 0 && max <= Integer.MAX_VALUE)
	{
	    repSet = new Integer1DSet(realType, (int)max);
	}
	else
	{
	    long	count = (max-min) + 1;
	    repSet =
		count > Integer.MAX_VALUE
		    ? (SimpleSet)new DoubleSet(realType)
		    : new Linear1DSet(realType, min, max, (int)count);
	}
	repSets = new SimpleSet[] {repSet};
    }

    public static UInt32VariableAdapter uInt32VariableAdapter(
	    DUInt32 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return new UInt32VariableAdapter(var, table);
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

    public DataImpl data(DUInt32 var)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
