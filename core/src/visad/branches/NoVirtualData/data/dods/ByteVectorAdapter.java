package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;

public final class ByteVectorAdapter
    extends FloatVectorAdapter
{
    private final Valuator	valuator;

    public ByteVectorAdapter(
	    BytePrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	super(vector, table, factory);
	valuator = Valuator.valuator(table);
    }

    public float[] getFloats(PrimitiveVector vec)
    {
	BytePrimitiveVector	vector = (BytePrimitiveVector)vec;
	float[]			values = new float[vec.getLength()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = vector.getValue(i);
	return valuator.process(values);
    }
}
