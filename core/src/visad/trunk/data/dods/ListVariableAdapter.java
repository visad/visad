package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.data.in.*;

/**
 * Provides support for adapting a DODS {@link DList} variable to the
 * {@link visad.data.in} context.
 */
public class ListVariableAdapter
    extends	VariableAdapter
{
    private final FunctionType	funcType;
    private final VectorAdapter	vectorAdapter;

    private ListVariableAdapter(
	    DList list,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	vectorAdapter =
	    factory.vectorAdapter(list.getPrimitiveVector(), table);
	funcType =
	    new FunctionType(RealType.Generic, vectorAdapter.getMathType());
    }

    public static ListVariableAdapter listVariableAdapter(
	    DList list, AttributeTable table, VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new ListVariableAdapter(list, table, factory);
    }

    public MathType getMathType()
    {
	return funcType;
    }

    public SimpleSet[] getRepresentationalSets()
    {
	return vectorAdapter.getRepresentationalSets();
    }

    public DataImpl data(DList list)
	throws VisADException, RemoteException
    {
	SampledSet	domain = new Integer1DSet(list.getLength());
	PrimitiveVector	vector = list.getPrimitiveVector();
	FieldImpl	field;
	if (funcType.getFlat())
	{
	    field =
		new FileFlatField(
		    new VectorAccessor(funcType, vectorAdapter, domain, vector),
		    getCacheStrategy());
	}
	else
	{
	    field = new FieldImpl(funcType, domain);
	    vectorAdapter.setField(vector, field);
	}
	return field;
    }
}
