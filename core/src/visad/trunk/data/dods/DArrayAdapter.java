package visad.data.dods;

import dods.dap.*;
import dods.dap.Server.InvalidParameterException;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;
import visad.data.in.*;

/**
 * Provides support for adapting DODS {@link DArray} variables to the
 * {@link visad.data.in} context.
 */
public class DArrayAdapter
    extends	VariableAdapter
{
    private final VirtualData	virtualData;

    private DArrayAdapter(
	    DArray array, AttributeTable table, AdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	virtualData = null;	// TODO
    }

    public static DArrayAdapter instance(
	    DArray array, AttributeTable table, AdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new DArrayAdapter(array, table, factory);
    }

    public static FunctionType functionType(DArray array, AttributeTable table)
	throws BadFormException, VisADException
    {
	return
	    new FunctionType(
		domainType(array, table),
		VariableAdapter.mathType(
		    array.getPrimitiveVector().getTemplate(), table));
    }

    public static MathType domainType(DArray array, AttributeTable table)
	throws BadFormException, VisADException
    {
	int		rank = array.numDimensions();
	RealType[]	realTypes = new RealType[rank];
	for (int i = 0; i < rank; ++i)
	{
	    try
	    {
		String	dimName = array.getDimension(i).getName();
		realTypes[rank-1-i] =// reverse innermost/outermost dimensions
		    realType(
			dimName, DODSUtil.getAttributeTable(table, dimName));
	    }
	    catch (InvalidParameterException e)
	    {
		throw new BadFormException(
		    "DArrayAdapter.domainMathType(...): " +
		    "Couldn't get DArray dimension: " + e);
	    }
	}
	return mathType(realTypes);
    }

    public VirtualData getVirtualData()
    {
	return virtualData;
    }
}
