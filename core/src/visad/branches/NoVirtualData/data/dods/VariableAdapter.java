package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.data.in.*;
import visad.data.units.Parser;

/**
 * Provides support for adapting DODS variables to the {@link visad.data.in}
 * context.
 */
public abstract class VariableAdapter
{
    protected VariableAdapter()
    {}

    public abstract VirtualData getVirtualData();

    public final Data getData()
	throws VisADException, RemoteException
    {
	return getVirtualData().getData();
    }

    public final MathType getMathType()
    {
	return getVirtualData().getMathType();
    }

    public static RealType realType(BaseType var, AttributeTable table)
    {
	return realType(var.getName(), table);
    }

    public static RealType realType(String name, AttributeTable table)
    {
	Unit	unit;
	if (table == null)
	{
	    unit = null;
	}
	else
	{
	    Attribute	attr = table.getAttribute("units");
	    if (attr == null)
		attr = table.getAttribute("unit");
	    if (attr == null)
		attr = table.getAttribute("UNITS");
	    if (attr == null)
		attr = table.getAttribute("UNIT");
	    if (attr == null)
	    {
		unit = null;
	    }
	    else
	    {
		if (attr.getType() == Attribute.STRING)
		{
		    try
		    {
			unit = Parser.instance().parse(attr.getValueAt(0));
		    }
		    catch (Exception e)
		    {
			System.err.println(
			    "VariableAdapter.getRealType(...): " +
			    "Ignoring variable \"" + name + 
			    "\" non-decodable unit-specification: " +
			    attr.getValueAt(0));
			unit = null;
		    }
		}
		else
		{
		    System.err.println(
			"VariableAdapter.getRealType(...): " +
			"Ignoring variable \"" + name + 
			"\" non-string unit-specification: " +
			attr.getTypeString());
		    unit = null;
		}
	    }
	}
	return
	    RealType.getRealType(VirtualScalar.scalarName(name), unit);
    }

    public static MathType mathType(BaseType var, AttributeTable table)
	throws BadFormException, VisADException
    {
	MathType	mathType;
	if (var instanceof DBoolean)
	    mathType = DBooleanAdapter.mathType((DBoolean)var, table);
	else if (var instanceof DByte)
	    mathType = DByteAdapter.mathType((DByte)var, table);
	else if (var instanceof DInt16)
	    mathType = DInt16Adapter.mathType((DInt16)var, table);
	else if (var instanceof DInt32)
	    mathType = DInt32Adapter.mathType((DInt32)var, table);
	else if (var instanceof DFloat32)
	    mathType = DFloat32Adapter.mathType((DFloat32)var, table);
	else if (var instanceof DFloat64)
	    mathType = DFloat64Adapter.mathType((DFloat64)var, table);
	else if (var instanceof DStructure)
	    mathType = DStructureAdapter.mathType((DStructure)var, table);
	else if (var instanceof DList)
	    mathType = DListAdapter.mathType((DList)var, table);
	/* TODO
	else if (var instanceof DSequence)
	    mathType = DSequenceAdapter.mathType((DSequence)var, table);
	else if (var instanceof DArray)
	    mathType = DArrayAdapter.mathType((DArray)var, table);
	else if (var instanceof DGrid)
	    mathType = DGridAdapter.mathType((DGrid)var, table);
	*/
	else
	    throw new VisADException(
		"VariableAdapter.mathType(BaseType,AttributeTable): " +
		"Unknown DODS type: " + var.getTypeName());
	return mathType;
    }

    /**
     * @return			The MathType of the input aggregate.  Will be
     *				<code>null</code> if zero-length input array.
     */
    public static MathType mathType(MathType[] mathTypes)
	throws VisADException
    {
	MathType	mathType;
	if (mathTypes.length == 0)
	{
	    mathType = null;
	}
	else if (mathTypes.length == 1)
	{
	    mathType = mathTypes[0];
	}
	else
	{
	    boolean	allReals = true;
	    for (int i = 0; i < mathTypes.length && allReals; ++i)
		allReals &= mathTypes[i] instanceof RealType;
	    if (allReals)
	    {
		mathType = new RealTupleType((RealType[])mathTypes);
	    }
	    else
	    {
		mathType = new TupleType(mathTypes);
	    }
	}
	return mathType;
    }

    public static Range primitiveVectorRange(
	    PrimitiveVector vector,
	    AttributeTable table,
	    DataMakerFactory factory)
	throws VisADException, RemoteException
    {
	Range	range;
	if (vector instanceof BooleanPrimitiveVector)
	    range =
		BooleanVectorRange.instance(
		    (BooleanPrimitiveVector)vector, table);
	else if (vector instanceof BytePrimitiveVector)
	    range = ByteVectorRange.instance(
		(BytePrimitiveVector)vector, table);
	else if (vector instanceof Int16PrimitiveVector)
	    range = Int16VectorRange.instance(
		(Int16PrimitiveVector)vector, table);
	else if (vector instanceof Int32PrimitiveVector)
	    range = Int32VectorRange.instance(
		(Int32PrimitiveVector)vector, table);
	else if (vector instanceof Float32PrimitiveVector)
	    range =
		Float32VectorRange.instance(
		    (Float32PrimitiveVector)vector, table);
	else if (vector instanceof Float64PrimitiveVector)
	    range =
		Float64VectorRange.instance(
		    (Float64PrimitiveVector)vector, table);
	else
	    range =
		BaseTypeVectorRange.instance(
		    (BaseTypePrimitiveVector)vector, table, factory);
	/*
	{
	    BaseType	template = vector.getTemplate();
	    if (template instanceof DString)
		range = DStringVectorRange(vector, table);
	    else if (template instanceof DBoolean ||
	        template instanceof DByte ||
	        template instanceof DInt16 ||
	        template instanceof DInt32 ||
	        template instanceof DFloat32 ||
	        template instanceof DFloat64)
	    {
		range = RealVectorRange.instance(vector, table);
	    }
	    else if (template instanceof DStructure)
		range = DStructureVectorRange.instance(vector, table);
	    else if (template instanceof DList)
		range = DListVectorRange.instance(vector, table);
	    else if (template instanceof DArray)
		range = DArrayVectorRange.instance(vector, table);
	    else if (template instanceof DGrid)
		range = DGridVectorRange.instance(vector, table);
	    else
		throw new VisADException(
		    getClass().getName() + "primitiveVectorRange(...): " +
		    "Unknown DODS type: " + template.getTypeName());
	}
	*/
	return range;
    }
}
