package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DInt32 variables to the
 * {@link visad.data.in} context.
 */
public class Int32VariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private Int32VariableAdapter(DInt32 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	realType = realType(var, table);
	valuator = Valuator.valuator(table);
	int	min = (int)Math.max(valuator.getMin(), Integer.MIN_VALUE);
	int	max = (int)Math.min(valuator.getMax(), Integer.MAX_VALUE);
	long	length = ((long)max - (long)min) + 1;
	repSets =
	    new SimpleSet[] {
		length > Integer.MAX_VALUE
		    ? (SimpleSet)new DoubleSet(realType)
		    : new Linear1DSet(realType, min, max, (int)length)};
    }

    public static Int32VariableAdapter int32VariableAdapter(
	    DInt32 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return new Int32VariableAdapter(var, table);
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

    public DataImpl data(DInt32 var)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
