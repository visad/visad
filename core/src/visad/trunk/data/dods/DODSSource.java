package visad.data.dods;

import dods.dap.*;
import java.util.Enumeration;
import visad.data.BadFormException;
import visad.data.in.*;
import visad.RealType;

public class DODSSource
    extends VirtualDataSource
{
    public DODSSource(VirtualDataSink downstream)
    {
	super(downstream);
    }

    public boolean open(String spec)
    {
	return false;	// TODO
    }

    /* TODO
    protected void handleVariable(BaseType baseType, AttributeTable attrTable)
	throws BadFormException
    {
	if (baseType instanceof DBoolean)
	    send(DBooleanAdapter.instance((DBoolean)baseType, attrTable)
		.getVirtualData());
	else if (baseType instanceof DByte)
	    send(DByteAdapter.instance((DByte)baseType, attrTable)
		.getVirtualData());
	else if (baseType instanceof DInt16)
	    send(DInt16Adapter.instance((DInt16)baseType, attrTable)
		.getVirtualData());
	else if (baseType instanceof DInt32)
	    send(DInt32Adapter.instance((DInt32)baseType, attrTable)
		.getVirtualData());
	else if (baseType instanceof DFloat32)
	    send(DFloat32Adapter.instance((DFloat32)baseType, attrTable)
		.getVirtualData());
	else if (baseType instanceof DFloat64)
	    send(DFloat64Adapter.instance((DFloat64)baseType, attrTable)
		.getVirtualData());
	else if (baseType instanceof DStructure)
	    send(DStructureAdapter.instance((DStructure)baseType, attrTable)
		.getVirtualData());
	else if (baseType instanceof DList)
	    send(DListAdapter.instance((DList)baseType, attrTable)
		.getVirtualData());
	else if (baseType instanceof DSequence)
	    send(DSequenceAdapter.instance((DSequence)baseType, attrTable)
		.getVirtualData());
	else if (baseType instanceof DArray)
	    send(DArrayAdapter.instance((DArray)baseType, attrTable)
		.getVirtualData());
	else if (baseType instanceof DGrid)
	    send(DGridAdapter.instance((DGrid)baseType, attrTable)
		.getVirtualData());
	else
	    throw new BadFormException(
		getClass().getName() + 
		".handleVariable(BaseType, AttributeTable): " +
		"Unknown DODS type: " + baseType.getTypeName());
    }

    protected void handleVariable(DStructure variable, AttributeTable attrTable)
    {
	VirtualTuple	tuple = new VirtualTuple();
	for (Enumeration enum = variable.getVariables();
	    enum.hasMoreElements(); )
	{
	    tuple.add((BaseType)enum.nextElement());
	}
	send(tuple);
    }

    protected void handleVariable(DList variable, AttributeTable attrTable)
    {
	PrimitiveVector	vector = variable.getPrimitiveVector();
	if (vector instanceof BooleanPrimitiveVector)
	    handleVariable((BooleanPrimitiveVector)vector, attrTable);
	else if (vector instanceof BytePrimitiveVector)
	    handleVariable((BytePrimitiveVector)vector, attrTable);
	else if (vector instanceof Int16PrimitiveVector)
	    handleVariable((Int16PrimitiveVector)vector, attrTable);
	else if (vector instanceof Int32PrimitiveVector)
	    handleVariable((Int32PrimitiveVector)vector, attrTable);
	else if (vector instanceof Float32PrimitiveVector)
	    handleVariable((Float32PrimitiveVector)vector, attrTable);
	else if (vector instanceof Float64PrimitiveVector)
	    handleVariable((Float64PrimitiveVector)vector, attrTable);
	else if (vector instanceof BaseTypePrimitiveVector)
	    handleVariable((BaseTypePrimitiveVector)vector, attrTable);
    }

    protected RealType getRealType(BaseType baseType, AttributeTable attrTable)
    {
	Unit	unit;
	Attribute	unitSpec = attrTable.getAttribute("units");
	if (unitSpec == null)
	    unitSpec = attrTable.getAttribute("unit");
	if (unitSpec == null)
	    unitSpec = attrTable.getAttribute("UNITS");
	if (unitSpec == null)
	    unitSpec = attrTable.getAttribute("UNIT");
	Unit	unit =
	    unitSpec == null || unitSpec.getType() != Attribute.String
		? null
		: UnitParser.parse(unitSpec);
	return RealType.getRealType(baseType.getName(), unit);
    }
    */
}
