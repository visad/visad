package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;

public final class Int16VectorAdapter
    extends FloatVectorAdapter
{
    private final Valuator	valuator;

    public Int16VectorAdapter(
	    Int16PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	super(vector, table, factory);
	valuator = Valuator.valuator(table);
    }

    public float[] getFloats(PrimitiveVector vec)
    {
	Int16PrimitiveVector	vector = (Int16PrimitiveVector)vec;
	float[]			values = new float[vector.getLength()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = vector.getValue(i);
	return valuator.process(values);
    }
}
