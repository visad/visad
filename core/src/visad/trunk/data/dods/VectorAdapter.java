package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.data.BadFormException;
import visad.*;

/**
 */
public abstract class VectorAdapter
    extends	Adapter
{
    private final SimpleSet[]	repSets;

    protected VectorAdapter(
	    PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	repSets =
	    factory.variableAdapter(
		vector.getTemplate(), table).getRepresentationalSets();
    }

    public abstract MathType getMathType();

    public boolean isFlat()
    {
	return isFlat(getMathType());
    }

    public SimpleSet[] getRepresentationalSets()
    {
	return repSets;
    }

    public void setField(PrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	if (vector instanceof BooleanPrimitiveVector)
	    setField((BooleanPrimitiveVector)vector, field);
	else if (vector instanceof BytePrimitiveVector)
	    setField((BytePrimitiveVector)vector, field);
	else if (vector instanceof UInt16PrimitiveVector)
	    setField((UInt16PrimitiveVector)vector, field);
	else if (vector instanceof Int16PrimitiveVector)
	    setField((Int16PrimitiveVector)vector, field);
	else if (vector instanceof UInt32PrimitiveVector)
	    setField((UInt32PrimitiveVector)vector, field);
	else if (vector instanceof Int32PrimitiveVector)
	    setField((Int32PrimitiveVector)vector, field);
	else if (vector instanceof Float32PrimitiveVector)
	    setField((Float32PrimitiveVector)vector, field);
	else if (vector instanceof Float64PrimitiveVector)
	    setField((Float64PrimitiveVector)vector, field);
	else
	    setField((BaseTypePrimitiveVector)vector, field);
    }

    public void setField(BooleanPrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(BooleanPrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    public void setField(BytePrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(BytePrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    public void setField(UInt16PrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(UInt16PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    public void setField(Int16PrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(Int16PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    public void setField(UInt32PrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(UInt32PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    public void setField(Int32PrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(Int32PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    public void setField(Float32PrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(Float32PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    public void setField(Float64PrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(Float64PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    public void setField(BaseTypePrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(BaseTypePrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    public GriddedSet griddedSet(PrimitiveVector vector)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".griddedSet(PrimitiveVector): " +
	    "Wrong type of vector");
    }
}
