package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import java.util.Enumeration;
import visad.*;
import visad.data.BadFormException;
import visad.data.in.*;

public class AdapterFactory
{
    private static final AdapterFactory	instance =
	new AdapterFactory(DataMakerFactory.instance());
    private final DataMakerFactory	factory;

    public static AdapterFactory instance()
    {
	return instance;
    }

    public static AdapterFactory instance(DataMakerFactory factory)
    {
	return new AdapterFactory(factory);
    }

    protected AdapterFactory(DataMakerFactory factory)
    {
	this.factory = factory;
    }

    public VariableAdapter variableAdapter(BaseType var, AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	VariableAdapter	adapter;
	if (var instanceof DBoolean)
	    adapter = DBooleanAdapter.instance((DBoolean)var, table);
	else if (var instanceof DByte)
	    adapter = DByteAdapter.instance((DByte)var, table);
	else if (var instanceof DInt16)
	    adapter = DInt16Adapter.instance((DInt16)var, table);
	else if (var instanceof DInt32)
	    adapter = DInt32Adapter.instance((DInt32)var, table);
	else if (var instanceof DFloat32)
	    adapter = DFloat32Adapter.instance((DFloat32)var, table);
	else if (var instanceof DFloat64)
	    adapter = DFloat64Adapter.instance((DFloat64)var, table);
	else if (var instanceof DStructure)
	    adapter = DStructureAdapter.instance((DStructure)var, table, this);
	else if (var instanceof DList)
	    adapter = DListAdapter.instance((DList)var, table, factory);
	/* TODO
	else if (var instanceof DSequence)
	    adapter = DSequenceAdapter.instance((DSequence)var, table, this);
	else if (var instanceof DArray)
	    adapter = DArrayAdapter.instance((DArray)var, table, this);
	else if (var instanceof DGrid)
	    adapter = DGridAdapter.instance((DGrid)var, table, this);
	*/
	else 
	    throw new BadFormException(
		getClass().getName() + 
		".variableAdapter(BaseType,AttributeTable): " +
		"Unknown DODS type: " + var.getTypeName());
	return adapter;
    }
}
