package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;

/**
 */
public abstract class FloatVectorAdapter
    extends NumericVectorAdapter
{
    protected FloatVectorAdapter(
	    PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	super(vector, table, factory);
    }

    public final void setField(PrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	field.setSamples(new float[][] {getFloats(vector)});
    }

    public GriddedSet griddedSet(PrimitiveVector vector)
	throws VisADException, RemoteException
    {
	return
	    Gridded1DSet.create(
		getMathType(), getFloats(vector), null, null, null);
    }

    /**
     * Values have been vetted.
     */
    protected abstract float[] getFloats(PrimitiveVector vector);
}
