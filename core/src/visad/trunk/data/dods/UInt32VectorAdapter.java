package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;

public final class UInt32VectorAdapter
    extends FloatVectorAdapter
{
    private final Valuator	valuator;

    public UInt32VectorAdapter(
	    UInt32PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	super(vector, table, factory);
	valuator = Valuator.valuator(table);
    }

    public float[] getFloats(PrimitiveVector vec)
    {
	UInt32PrimitiveVector	vector = (UInt32PrimitiveVector)vec;
	float[]			values = new float[vector.getLength()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = vector.getValue(i);
	return valuator.process(values);
    }
}
