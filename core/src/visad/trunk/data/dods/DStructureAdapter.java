package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;
import visad.data.in.*;

/**
 * Provides support for adapting DODS {@link DStructure} variables to the
 * {@link visad.data.in} context.
 */
public class DStructureAdapter
    extends	VariableAdapter
{
    private final ImmutableVirtualTuple	virtualData;

    private DStructureAdapter(
	    DStructure var, AttributeTable table, AdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	int		count = var.elementCount();
	VirtualData[]	components = new VirtualData[count];
	for (int i = 0; i < count; ++i)
	{
	    try
	    {
		BaseType	variable = var.getVar(i);
		components[i] =
		    factory.variableAdapter(
			variable, DODSUtil.getAttributeTable(table, variable))
		    .getVirtualData();
	    }
	    catch (NoSuchVariableException e)
	    {
		throw new BadFormException(
		    getClass().getName() + ".DStructureAdapter(...): " +
		    "Missing variable: " + i + ": " + e);
	    }

	}
	virtualData = new ImmutableVirtualTuple(components);
    }

    public static DStructureAdapter instance(
	    DStructure var, AttributeTable table, AdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new DStructureAdapter(var, table, factory);
    }

    public static MathType mathType(DSequence sequence, AttributeTable table)
    {
	return null;	// TODO
    }

    public VirtualData getVirtualData()
    {
	return virtualData;
    }
}
