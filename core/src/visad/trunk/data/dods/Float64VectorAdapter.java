package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;

/**
 */
public class Float64VectorAdapter
    extends NumericVectorAdapter
{
    private final Valuator	valuator;

    public Float64VectorAdapter(
	    Float64PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	super(vector, table, factory);
	valuator = Valuator.valuator(table);
    }

    public final void setField(PrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	field.setSamples(new double[][] {getDoubles(vector)});
    }

    public GriddedSet griddedSet(PrimitiveVector vector)
	throws VisADException, RemoteException
    {
	return
	    Gridded1DDoubleSet.create(
		getMathType(), getDoubles(vector), null, null, null);
    }

    /**
     * Values have been vetted.
     */
    public double[] getDoubles(PrimitiveVector vec)
    {
	Float64PrimitiveVector	vector = (Float64PrimitiveVector)vec;
	double[]		values = new double[vector.getLength()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = vector.getValue(i);
	return valuator.process(values);
    }
}
