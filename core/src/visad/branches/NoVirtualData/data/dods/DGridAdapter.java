package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;
import visad.data.in.*;

/**
 * Provides support for adapting DODS {@link DGrid} variables to the
 * {@link visad.data.in} context.
 */
public class DGridAdapter
    extends	VariableAdapter
{
    private final VirtualData	virtualData;

    private DGridAdapter(
	    DGrid grid, AttributeTable table, AdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	virtualData = null;	// TODO
    }

    public static DGridAdapter instance(
	    DGrid grid, AttributeTable table, AdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new DGridAdapter(grid, table, factory);
    }

    public static FunctionType functionType(DGrid grid, AttributeTable table)
	throws BadFormException, VisADException
    {
	FunctionType	functionType;
	try
	{
	    functionType =
		new FunctionType(
		    domainType(grid, table),
		    VariableAdapter.mathType(
			((DArray)grid.getVar(0)).getPrimitiveVector()
			    .getTemplate(), table));
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		"DGridAdapter.<init>(...): No such variable: " + e);
	}
	return functionType;
    }

    public static MathType domainType(DGrid grid, AttributeTable table)
	throws BadFormException, VisADException
    {
	MathType	mathType;
	try
	{
	    DArray	array = (DArray)grid.getVar(0);
	    int		rank = array.numDimensions();
	    RealType[]	realTypes = new RealType[rank];
	    for (int i = 1; i <= rank; ++i)
	    {
		DArray	array = (DList)grid.getVar(i);
		BaseType	template =
		    array.getPrimitiveVector().getTemplate();
		realTypes[rank-i] =	// reverse innermost/outermost dims
		    DODSUtil.getRealType(
			template, DODSUtil.getAttributeTable(table, template));
	    }
	    mathType = mathType(realTypes);
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		"DGridAdapter.domainType(...): DGrid inquiry failure: " + e);
	}
	return mathType;
    }

    public VirtualData getVirtualData()
    {
	return virtualData;
    }
}
