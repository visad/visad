package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DFloat64 variables to the
 * {@link visad.data.in} context.
 */
public class Float64VariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private Float64VariableAdapter(DFloat64 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	realType = realType(var, table);
	valuator = Valuator.valuator(table);
	repSets = new SimpleSet[] {new DoubleSet(realType)};
    }

    public static Float64VariableAdapter float64VariableAdapter(
	    DFloat64 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return new Float64VariableAdapter(var, table);
    }

    public MathType getMathType()
    {
	return realType;
    }

    /**
     * Do not modify the returned array.
     */
    public SimpleSet[] getRepresentationalSets()
	throws VisADException
    {
	return repSets;
    }

    public DataImpl data(DFloat64 var)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
