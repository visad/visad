package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;

public final class BooleanVectorAdapter
    extends FloatVectorAdapter
{
    public BooleanVectorAdapter(
	    BooleanPrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	super(vector, table, factory);
    }

    public float[] getFloats(PrimitiveVector vec)
    {
	BooleanPrimitiveVector	vector = (BooleanPrimitiveVector)vec;
	float[]			values = new float[vector.getLength()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = vector.getValue(i) ? 1 : 0;
	return values;
    }
}
