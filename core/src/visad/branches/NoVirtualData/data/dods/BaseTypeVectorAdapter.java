package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.in.Range;

/**
 */
public class BaseTypeVectorAdapter
    extends VectorAdapter
{
    private final VariableAdapter	adapter;

    protected BaseTypeVectorAdapter(
	    BaseTypePrimitiveVector vector, 
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	super(vector, table, factory);
	adapter = factory.variableAdapter(vector.getTemplate(), table);
    }

    public MathType getMathType()
    {
	return adapter.getMathType();
    }

    public static BaseTypeVectorAdapter baseTypeVectorAdapter (
	    BaseTypePrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new BaseTypeVectorAdapter(vector, table, factory);
    }

    public void setField(BaseTypePrimitiveVector vector, Field field)
	throws VisADException, RemoteException
    {
	int		length = vector.getLength();
	for (int i = 0; i < length; ++i)
	    field.setSample(
		i, adapter.data(vector.getValue(i)), /*copy=*/false);
    }
}
