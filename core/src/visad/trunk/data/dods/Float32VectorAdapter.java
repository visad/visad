package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;

public final class Float32VectorAdapter
    extends FloatVectorAdapter
{
    private final Valuator	valuator;

    public Float32VectorAdapter(
	    Float32PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	super(vector, table, factory);
	valuator = Valuator.valuator(table);
    }

    public float[] getFloats(PrimitiveVector vec)
    {
	Float32PrimitiveVector	vector = (Float32PrimitiveVector)vec;
	float[]			values = new float[vector.getLength()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = vector.getValue(i);
	return valuator.process(values);
    }
}
