package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.data.BadFormException;
import visad.*;

/**
 */
public abstract class NumericVectorAdapter
    extends VectorAdapter
{
    private final RealType	realType;

    protected NumericVectorAdapter(
	    PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	super(vector, table, factory);
	realType = realType(vector.getTemplate(), table);
    }

    public MathType getMathType()
    {
	return realType;
    }

    public abstract void setField(PrimitiveVector vector, Field field)
	throws VisADException, RemoteException;

    public abstract GriddedSet griddedSet(PrimitiveVector vector)
	throws VisADException, RemoteException;
}
