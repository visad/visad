package visad.data.dods;

import dods.dap.*;
import java.util.ArrayList;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;
import visad.data.in.*;

/**
 * Provides support for adapting DODS {@link DStructure} variables to the
 * {@link visad.data.in} context.
 */
public class StructureVariableAdapter
    extends	VariableAdapter
{
    private final MathType		mathType;
    private final VariableAdapter[]	adapters;
    private final boolean		isFlat;
    private final SimpleSet[]		repSets;

    private StructureVariableAdapter(
	    DStructure structure,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	ArrayList	setList = new ArrayList();
	adapters = new VariableAdapter[structure.elementCount()];
	for (int i = 0; i < adapters.length; ++i)
	{
	    BaseType	var;
	    try
	    {
		var = structure.getVar(i);
	    }
	    catch (NoSuchVariableException e)
	    {
		throw new BadFormException(
		    getClass().getName() + ".data(...): " +
		    "DStructure is missing variable " + i + ": " + e);
	    }
	    adapters[i] =
		factory.variableAdapter(var, attributeTable(table, var));
	    SimpleSet[]	setArray = adapters[i].getRepresentationalSets();
	    for (int j = 0; j < setArray.length; ++j)
		setList.add(setArray[j]);
	}
	mathType = mathType(adapters);
	isFlat = isFlat(mathType);
	repSets = (SimpleSet[])setList.toArray(new SimpleSet[0]);
    }

    public static StructureVariableAdapter structureVariableAdapter(
	    DStructure structure,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	return new StructureVariableAdapter(structure, table, factory);
    }

    public MathType getMathType()
    {
	return mathType;
    }

    public SimpleSet[] getRepresentationalSets()
    {
	return repSets;
    }

    public DataImpl data(DStructure structure)
	throws BadFormException, VisADException, RemoteException
    {
	DataImpl	data;
	try
	{
	    if (adapters.length == 0)
	    {
		data = null;
	    }
	    else if (adapters.length == 1)
	    {
		data = adapters[0].data(structure.getVar(0));
	    }
	    else
	    {
		if (isFlat)
		{
		    Real[]	components = new Real[adapters.length];
		    for (int i = 0; i < adapters.length; ++i)
			components[i] = 
			    (Real)adapters[i].data(structure.getVar(i));
		    data = new RealTuple(components);
		}
		else
		{
		    DataImpl[]	components = new DataImpl[adapters.length];
		    for (int i = 0; i < adapters.length; ++i)
			components[i] = adapters[i].data(structure.getVar(i));
		    data = new Tuple(components);
		}
	    }
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		getClass().getName() + ".data(...): " +
		"DStructure is missing variable: " + e);
	}
	return data;
    }
}
