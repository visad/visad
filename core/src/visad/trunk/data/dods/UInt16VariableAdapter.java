package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DUInt16 variables to the
 * {@link visad.data.in} context.
 */
public class UInt16VariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private UInt16VariableAdapter(DUInt16 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	realType = realType(var, table);
	valuator = Valuator.valuator(table);
	int	min = (int)Math.max(valuator.getMin(),     0);
	int	max = (int)Math.min(valuator.getMax(), 2*Short.MAX_VALUE+1);
	repSets =
	    new SimpleSet[] {
		min == 0
		    ? (SimpleSet)new Integer1DSet(realType, max)
		    : new Linear1DSet(realType, min, max, (max-min)+1)};
    }

    public static UInt16VariableAdapter uInt16VariableAdapter(
	    DUInt16 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return new UInt16VariableAdapter(var, table);
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

    public DataImpl data(DUInt16 var)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
