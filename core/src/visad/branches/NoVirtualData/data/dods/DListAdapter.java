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
public class DListAdapter
    extends	VariableAdapter
{
    private final VirtualField	virtualData;

    private DListAdapter(
	    DList list, AttributeTable table, DataMakerFactory factory)
	throws VisADException, RemoteException
    {
	virtualData =
	    VirtualField.instance(
		new Domain(new VirtualSet(new Integer1DSet(list.getLength()))),
		primitiveVectorRange(
		    list.getPrimitiveVector(), table, factory));
    }

    public static DListAdapter instance(
	    DList list,
	    AttributeTable table,
	    DataMakerFactory factory)
	throws VisADException, RemoteException
    {
	return new DListAdapter(list, table, factory);
    }

    public static FunctionType functionType(DList list, AttributeTable table)
	throws BadFormException, VisADException
    {
	return
	    new FunctionType(
		RealType.Generic,
		mathType(list.getPrimitiveVector().getTemplate(), table));
    }

    public VirtualData getVirtualData()
    {
	return virtualData;
    }
}
