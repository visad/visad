package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;
import visad.VisADException;

public class VariableAdapterFactory
{
    private static final VariableAdapterFactory	instance =
	new VariableAdapterFactory();
    private static final VectorAdapterFactory	vectorAdapterFactory =
	VectorAdapterFactory.vectorAdapterFactory();

    protected VariableAdapterFactory()
    {}

    public static VariableAdapterFactory variableAdapterFactory()
    {
	return instance;
    }

    /**
     * Uses this instance as the VariableAdapterFactory.
     */
    public VariableAdapter variableAdapter(BaseType var, AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	VariableAdapter	adapter;
	if (var instanceof DString)
	    adapter = stringVariableAdapter((DString)var, table);
	else if (var instanceof DBoolean)
	    adapter = booleanVariableAdapter((DBoolean)var, table);
	else if (var instanceof DByte)
	    adapter = byteVariableAdapter((DByte)var, table);
	else if (var instanceof DUInt16)
	    adapter = uInt16VariableAdapter((DUInt16)var, table);
	else if (var instanceof DInt16)
	    adapter = int16VariableAdapter((DInt16)var, table);
	else if (var instanceof DUInt32)
	    adapter = uInt32VariableAdapter((DUInt32)var, table);
	else if (var instanceof DInt32)
	    adapter = int32VariableAdapter((DInt32)var, table);
	else if (var instanceof DFloat32)
	    adapter = float32VariableAdapter((DFloat32)var, table);
	else if (var instanceof DFloat64)
	    adapter = float64VariableAdapter((DFloat64)var, table);
	else if (var instanceof DStructure)
	    adapter = structureVariableAdapter((DStructure)var, table);
	else if (var instanceof DList)
	    adapter = listVariableAdapter((DList)var, table);
	else if (var instanceof DSequence)
	    adapter = sequenceVariableAdapter((DSequence)var, table);
	else if (var instanceof DArray)
	    adapter = arrayVariableAdapter((DArray)var, table);
	else if (var instanceof DGrid)
	    adapter = gridVariableAdapter((DGrid)var, table);
	else
	    throw new BadFormException(
		getClass().getName() + ".variableAdapter(...): " +
		"Unknown DODS type: " + var.getTypeName());
	return adapter;
    }

    public StringVariableAdapter stringVariableAdapter(
	    DString var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return StringVariableAdapter.stringVariableAdapter(var, table);
    }

    public BooleanVariableAdapter booleanVariableAdapter(
	    DBoolean var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return BooleanVariableAdapter.booleanVariableAdapter(var, table);
    }

    public ByteVariableAdapter byteVariableAdapter(
	    DByte var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return ByteVariableAdapter.byteVariableAdapter(var, table);
    }

    public UInt16VariableAdapter uInt16VariableAdapter(
	    DUInt16 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return UInt16VariableAdapter.uInt16VariableAdapter(var, table);
    }

    public Int16VariableAdapter int16VariableAdapter(
	    DInt16 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return Int16VariableAdapter.int16VariableAdapter(var, table);
    }

    public UInt32VariableAdapter uInt32VariableAdapter(
	    DUInt32 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return UInt32VariableAdapter.uInt32VariableAdapter(var, table);
    }

    public Int32VariableAdapter int32VariableAdapter(
	    DInt32 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return Int32VariableAdapter.int32VariableAdapter(var, table);
    }

    public Float32VariableAdapter float32VariableAdapter(
	    DFloat32 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return Float32VariableAdapter.float32VariableAdapter(var, table);
    }

    public Float64VariableAdapter float64VariableAdapter(
	    DFloat64 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return Float64VariableAdapter.float64VariableAdapter(var, table);
    }

    public StructureVariableAdapter structureVariableAdapter(
	    DStructure var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return
	    StructureVariableAdapter.structureVariableAdapter(var, table, this);
    }

    public ListVariableAdapter listVariableAdapter(
	    DList var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return ListVariableAdapter.listVariableAdapter(var, table, this);
    }

    public SequenceVariableAdapter sequenceVariableAdapter(
	    DSequence var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return
	    SequenceVariableAdapter.sequenceVariableAdapter(var, table, this);
    }

    public ArrayVariableAdapter arrayVariableAdapter(
	    DArray var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return ArrayVariableAdapter.arrayVariableAdapter(var, table, this);
    }

    public GridVariableAdapter gridVariableAdapter(
	    DGrid var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return GridVariableAdapter.gridVariableAdapter(var, table, this);
    }

    public GridVariableMapAdapter gridVariableMapAdapter(
	    DArray array, AttributeTable table)
	throws VisADException, RemoteException
    {
	return 
	    GridVariableMapAdapter.gridVariableMapAdapter(array, table, this);
    }

    public VectorAdapter vectorAdapter(
	    PrimitiveVector vector,
	    AttributeTable table)
	throws VisADException, RemoteException
    {
	return vectorAdapterFactory.vectorAdapter(vector, table, this);
    }
}
