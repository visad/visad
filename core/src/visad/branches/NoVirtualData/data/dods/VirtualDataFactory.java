package visad.data.dods;

import dods.dap.*;
import dods.dap.Server.InvalidParameterException;
import java.rmi.RemoteException;
import java.util.*;
import visad.*;
import visad.data.BadFormException;
import visad.data.in.*;

public class VirtualDataFactory
{
    private static final VirtualDataFactory	instance =
	new VirtualDataFactory();

    protected VirtualDataFactory()
    {}

    public static VirtualDataFactory instance()
    {
	return instance;
    }

    public VirtualData virtualData(BaseType variable, AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	VirtualData	data;
	if (variable instanceof DBoolean)
	    data = virtualData((DBoolean)variable, table);
	else if (variable instanceof DByte)
	    data = virtualData((DByte)variable, table);
	else if (variable instanceof DInt16)
	    data = virtualData((DInt16)variable, table);
	else if (variable instanceof DInt32)
	    data = virtualData((DInt32)variable, table);
	else if (variable instanceof DFloat32)
	    data = virtualData((DFloat32)variable, table);
	else if (variable instanceof DFloat64)
	    data = virtualData((DFloat64)variable, table);
	else if (variable instanceof DStructure)
	    data = virtualData((DStructure)variable, table);
	else if (variable instanceof DList)
	    data = virtualData((DList)variable, table);
	else if (variable instanceof DSequence)
	    data = virtualData((DSequence)variable, table);
	else if (variable instanceof DArray)
	    data = virtualData((DArray)variable, table);
	else if (variable instanceof DGrid)
	    data = virtualData((DGrid)variable, table);
	else 
	    throw new BadFormException(
		getClass().getName() + 
		".virtualData(BaseType,AttributeTable): " +
		"Unknown DODS type: " + variable.getTypeName());
	return data;
    }

    public VirtualData virtualData(DBoolean variable, AttributeTable table)
	throws VisADException
    {
	return
	    VirtualReal.instance(
		DODSUtil.getRealType(variable, table),
		variable.getValue() ? 1 : 0);
    }

    public VirtualData virtualData(DByte variable, AttributeTable table)
	throws BadFormException, VisADException
    {
	return
	    VirtualReal.instance(
		DODSUtil.getRealType(variable, table),
		Valuator.instance(table).process(variable.getValue()));
    }

    public VirtualData virtualData(DInt16 variable, AttributeTable table)
	throws BadFormException, VisADException
    {
	return
	    VirtualReal.instance(
		DODSUtil.getRealType(variable, table),
		Valuator.instance(table).process(variable.getValue()));
    }

    public VirtualData virtualData(DInt32 variable, AttributeTable table)
	throws BadFormException, VisADException
    {
	return
	    VirtualReal.instance(
		DODSUtil.getRealType(variable, table),
		Valuator.instance(table).process(variable.getValue()));
    }

    public VirtualData virtualData(DFloat32 variable, AttributeTable table)
	throws BadFormException, VisADException
    {
	return
	    VirtualReal.instance(
		DODSUtil.getRealType(variable, table),
		Valuator.instance(table).process(variable.getValue()));
    }

    public VirtualData virtualData(DFloat64 variable, AttributeTable table)
	throws BadFormException, VisADException
    {
	return
	    VirtualReal.instance(
		DODSUtil.getRealType(variable, table),
		Valuator.instance(table).process(variable.getValue()));
    }

    public VirtualData virtualData(DStructure dStructure, AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	int		count = dStructure.elementCount();
	VirtualData[]	components = new VirtualData[count];
	for (int i = 0; i < count; ++i)
	{
	    BaseType	variable;
	    try
	    {
		variable = dStructure.getVar(i);
	    }
	    catch (NoSuchVariableException e)
	    {
		throw new BadFormException(
		    getClass().getName() + 
		    ".virtualData(DStructure,AttributeTable): " +
		    "Couldn't get structure-variable " + i);
	    }
	    components[i] =
		virtualData(
		    variable, DODSUtil.getAttributeTable(table, variable));
	}
	return new ImmutableVirtualTuple(components);
    }

    public VirtualData virtualData(DList list, AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	return
	    VirtualField.instance(
		new Domain(new VirtualSet(new Integer1DSet(list.getLength()))),
		primitiveVectorRange(list.getPrimitiveVector(), table));
    }

    public VirtualData virtualData(DSequence sequence, AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	return
	    VirtualField.instance(
		new Domain(
		    new VirtualSet(new Integer1DSet(sequence.getRowCount()))),
		DSequenceRange.instance(sequence, table));
    }

    public VirtualData virtualData(DArray dArray, AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	int		rank = dArray.numDimensions();
	int[]		firsts = new int[rank];
	int[]		lasts = new int[rank];
	int[]		lengths = new int[rank];
	RealType[]	realTypes = new RealType[rank];
	for (int i = 0; i < rank; ++i)
	{
	    /* accomodate reversed innermost to outermost */
	    int			j = rank - 1 - i;
	    DArrayDimension	dim;
	    try
	    {
		dim = dArray.getDimension(i);
	    }
	    catch (InvalidParameterException e)
	    {
		throw new BadFormException(
		    getClass().getName() + 
		    ".virtualData(DArray,AttributeTable): " +
		    "Couldn't get DArray dimension " + i);
	    }
	    firsts[j] = dim.getStart();
	    lasts[j] = dim.getStop();
	    lengths[j] = 1 + (firsts[j] - lasts[j]) / dim.getStride();
	    String		dimName = dim.getName();
	    AttributeTable	newTable =
		DODSUtil.getAttributeTable(table, dimName);
	    realTypes[j] = DODSUtil.realType(dimName, newTable);
	}
	return
	    VirtualField.instance(
		new Domain(
		    new VirtualSet(
			(SampledSet)LinearNDSet.create(
			    new RealTupleType(realTypes),
			    doubleArray(firsts),
			    doubleArray(lasts),
			    lengths))),
		primitiveVectorRange(dArray.getPrimitiveVector(), table));
    }

    private double[] doubleArray(int[] ints)
    {
	double[]	doubles = new double[ints.length];
	for (int i = 0; i < ints.length; ++i)
	    doubles[i] = ints[i];
	return doubles;
    }

    public VirtualData virtualData(
	    DGrid dGrid, AttributeTable table, DataMakerFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	try
	{
	    return
		VirtualField.instance(
		    new DGridDomain(dGrid, table),
		    primitiveVectorRange(
			((DArray)dGrid.getVar(0)).getPrimitiveVector(), table));
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		getClass().getName() + 
		".virtualData(DGrid,AttributeTable,DataMakerFactory): " +
		"Couldn't get DArray from DGrid: " + e);
	}
    }

    protected Range primitiveVectorRange(
	    PrimitiveVector vector, AttributeTable table)
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
		    (BaseTypePrimitiveVector)vector,
		    table,
		    /*DataMakerFactory=TODO*/null);
	return range;
    }

    /**
     * A DODS attribute of type {@link Attribute#UNKNOWN} is silently ignored.
     */
    public VirtualData virtualData(Attribute attr, String name)
	throws BadFormException, VisADException
    {
	VirtualData	data;
	int		type = attr.getType();
	if (type == Attribute.BYTE ||
	    type == Attribute.INT16 || type == Attribute.UINT16 ||
	    type == Attribute.INT32 || type == Attribute.UINT32)
	{
	    data = handleIntegralAttribute(attr, name);
	}
	else if (type == Attribute.FLOAT32 || type == Attribute.FLOAT64)
	{
	    data = handleFloatingPointAttribute(attr, name);
	}
	else if (type == Attribute.STRING || type == Attribute.URL)
	{
	    data = handleStringAttribute(attr, name);
	}
	else if (type == Attribute.CONTAINER)
	{
	    data = handleContainerAttribute(attr);
	}
	else if (type == Attribute.UNKNOWN)
	{
	    data = null;
	}
	else
	{
	    throw new BadFormException(
		getClass().getName() + ".virtualData(Attribute,String): " +
		"Unknown DODS attribute type: " + attr.getTypeString());
	}
	return data;
    }

    protected VirtualData handleIntegralAttribute(Attribute attr, String name)
	throws VisADException
    {
	VirtualData	data;
	RealType	realType = RealType.getRealType(name);
	int		count = 0;
	for (Enumeration enum = attr.getValues(); enum.hasMoreElements();
	    enum.nextElement())
	{
	    count++;
	}
	try
	{
	    if (count == 0)
	    {
		data = VirtualReal.instance(realType);
	    }
	    else if (count == 1)
	    {
		data =
		    VirtualReal.instance(
			realType,
			Long.decode(attr.getValueAt(0)).doubleValue());
	    }
	    else
	    {
		float[]	values = new float[count];
		for (int i = 0; i < count; ++i)
		    values[i] = Long.decode(attr.getValueAt(i)).floatValue();
		data =
		    new VirtualSet(
			new Gridded1DSet(
			    realType, new float[][] {values}, count));
	    }
	}
	catch (NumberFormatException e)
	{
	    throw new BadFormException(
		getClass().getName() + ".handleIntegralAttribute(...): " +
		"Invalid encoding: " + e);
	}
	return data;
    }

    protected VirtualData handleFloatingPointAttribute(
	    Attribute attr, String name)
	throws VisADException
    {
	VirtualData	data;
	RealType	realType = RealType.getRealType(name);
	int		count = 0;
	for (Enumeration enum = attr.getValues(); enum.hasMoreElements();
	    enum.nextElement())
	{
	    count++;
	}
	try
	{
	    if (count == 0)
	    {
		data = VirtualReal.instance(realType);
	    }
	    else if (count == 1)
	    {
		data =
		    VirtualReal.instance(
			realType, Double.parseDouble(attr.getValueAt(0)));
	    }
	    else
	    {
		if (attr.getType() == Attribute.FLOAT32)
		{
		    float[]	values = new float[count];
		    for (int i = 0; i < count; ++i)
			values[i] = Float.parseFloat(attr.getValueAt(i));
		    data =
			new VirtualSet(
			    new Gridded1DSet(
				realType, new float[][] {values}, count));
		}
		else
		{
		    double[]	values = new double[count];
		    for (int i = 0; i < count; ++i)
			values[i] = Double.parseDouble(attr.getValueAt(i));
		    data =
			new VirtualSet(
			    new Gridded1DDoubleSet(
				realType, new double[][] {values}, count));
		}
	    }
	}
	catch (NumberFormatException e)
	{
	    throw new BadFormException(
		getClass().getName() + ".handleFloatingPointAttribute(...): " +
		"Invalid encoding: " + e);
	}
	return data;
    }

    protected VirtualData handleStringAttribute(Attribute attr, String name)
	throws VisADException
    {
	TextType	textType = TextType.getTextType(name);
	ArrayList	datums = new ArrayList();
	for (Enumeration enum = attr.getValues(); enum.hasMoreElements(); )
	    datums.add(
		VirtualText.instance(textType, (String)enum.nextElement()));
	return new ImmutableVirtualTuple(datums);
    }

    protected VirtualData handleContainerAttribute(Attribute attr)
	throws VisADException
    {
	AttributeTable	table = attr.getContainer();
	ArrayList	datums = new ArrayList();
	for (Enumeration enum = table.getNames(); enum.hasMoreElements(); )
	{
	    String	name = (String)enum.nextElement();
	    datums.add(virtualData(table.getAttribute(name), name));
	}
	return new ImmutableVirtualTuple(datums);
    }
}
