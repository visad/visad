package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DInt16 variables to the
 * {@link visad.data.in} context.
 */
public class Int16VariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private Int16VariableAdapter(DInt16 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	realType = realType(var, table);
	valuator = Valuator.valuator(table);
	int	min = (int)Math.max(valuator.getMin(), Short.MIN_VALUE);
	int	max = (int)Math.min(valuator.getMax(), Short.MAX_VALUE);
	repSets =
	    new SimpleSet[] {new Linear1DSet(realType, min, max, (max-min)+1)};
    }

    public static Int16VariableAdapter int16VariableAdapter(
	    DInt16 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return new Int16VariableAdapter(var, table);
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

    public DataImpl data(DInt16 var)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
