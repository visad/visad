package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DFloat32 variables to the
 * {@link visad.data.in} context.
 */
public class Float32VariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private Float32VariableAdapter(DFloat32 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	realType = realType(var, table);
	valuator = Valuator.valuator(table);
	repSets = new SimpleSet[] {new FloatSet(realType)};
    }

    public static Float32VariableAdapter float32VariableAdapter(
	    DFloat32 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return new Float32VariableAdapter(var, table);
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

    public DataImpl data(DFloat32 var)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
