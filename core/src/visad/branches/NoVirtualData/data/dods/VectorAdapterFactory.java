package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.data.BadFormException;
import visad.*;

/**
 */
public class VectorAdapterFactory
{
    private static final VectorAdapterFactory	instance =
	new VectorAdapterFactory();

    protected VectorAdapterFactory()
    {}

    public static VectorAdapterFactory vectorAdapterFactory()
    {
	return instance;
    }

    public VectorAdapter vectorAdapter(
	    PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	VectorAdapter	adapter;
	if (vector instanceof BooleanPrimitiveVector)
	    adapter =
		booleanVectorAdapter(
		    (BooleanPrimitiveVector)vector, table, factory);
	else if (vector instanceof BytePrimitiveVector)
	    adapter =
		byteVectorAdapter(
		    (BytePrimitiveVector)vector, table, factory);
	else if (vector instanceof UInt16PrimitiveVector)
	    adapter =
		uInt16VectorAdapter(
		    (UInt16PrimitiveVector)vector, table, factory);
	else if (vector instanceof Int16PrimitiveVector)
	    adapter =
		int16VectorAdapter(
		    (Int16PrimitiveVector)vector, table, factory);
	else if (vector instanceof UInt32PrimitiveVector)
	    adapter =
		uInt32VectorAdapter(
		    (UInt32PrimitiveVector)vector, table, factory);
	else if (vector instanceof Int32PrimitiveVector)
	    adapter =
		int32VectorAdapter(
		    (Int32PrimitiveVector)vector, table, factory);
	else if (vector instanceof Float32PrimitiveVector)
	    adapter =
		float32VectorAdapter(
		    (Float32PrimitiveVector)vector, table, factory);
	else if (vector instanceof Float64PrimitiveVector)
	    adapter =
		float64VectorAdapter(
		    (Float64PrimitiveVector)vector, table, factory);
	else
	    adapter =
		baseTypeVectorAdapter(
		    (BaseTypePrimitiveVector)vector, table, factory);
	return adapter;
    }

    public BooleanVectorAdapter booleanVectorAdapter(
	    BooleanPrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new BooleanVectorAdapter(vector, table, factory);
    }

    public ByteVectorAdapter byteVectorAdapter(
	    BytePrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new ByteVectorAdapter(vector, table, factory);
    }

    public UInt16VectorAdapter uInt16VectorAdapter(
	    UInt16PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new UInt16VectorAdapter(vector, table, factory);
    }

    public Int16VectorAdapter int16VectorAdapter(
	    Int16PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new Int16VectorAdapter(vector, table, factory);
    }

    public UInt32VectorAdapter uInt32VectorAdapter(
	    UInt32PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new UInt32VectorAdapter(vector, table, factory);
    }

    public Int32VectorAdapter int32VectorAdapter(
	    Int32PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new Int32VectorAdapter(vector, table, factory);
    }

    public Float32VectorAdapter float32VectorAdapter(
	    Float32PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new Float32VectorAdapter(vector, table, factory);
    }

    public Float64VectorAdapter float64VectorAdapter(
	    Float64PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new Float64VectorAdapter(vector, table, factory);
    }

    public BaseTypeVectorAdapter baseTypeVectorAdapter(
	    BaseTypePrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return
	    BaseTypeVectorAdapter.baseTypeVectorAdapter(vector, table, factory);
    }
}
