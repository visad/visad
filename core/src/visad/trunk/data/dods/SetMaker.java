package visad.data.dods;

import dods.dap.*;
import dods.dap.Server.InvalidParameterException;
import visad.data.BadFormException;
import visad.*;

public final class SetMaker
    extends	DataMaker
{
    private final SetType	setType;

    protected SetMaker(BooleanPrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	setType =
	    new SetType(DODSUtil.getRealType(vector.getTemplate(), table));
    }

    protected SetMaker(BytePrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	setType =
	    new SetType(DODSUtil.getRealType(vector.getTemplate(), table));
    }

    protected SetMaker(Int16PrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	setType =
	    new SetType(DODSUtil.getRealType(vector.getTemplate(), table));
    }

    protected SetMaker(Int32PrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	setType =
	    new SetType(DODSUtil.getRealType(vector.getTemplate(), table));
    }

    protected SetMaker(Float32PrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	setType =
	    new SetType(DODSUtil.getRealType(vector.getTemplate(), table));
    }

    protected SetMaker(Float64PrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	setType =
	    new SetType(DODSUtil.getRealType(vector.getTemplate(), table));
    }

    protected SetMaker(BaseTypePrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	setType = new SetType(DODSUtil.mathType(vector.getTemplate(), table));
    }

    public static SetMaker instance(BaseType variable, AttributeTable table)
	throws BadFormException, VisADException
    {
	SetMaker	setMaker;
	if (variable instanceof DList)
	    setMaker = instance(((DList)variable).getPrimitiveVector(), table);
	else
	    throw new BadFormException(
		"SetMaker.instance(BaseType,AttributeTable): " +
		"Invalid DODS type: " + variable.getTypeName());
	return setMaker;
    }

    public static SetMaker instance(
	    PrimitiveVector vector, AttributeTable table)
	throws BadFormException, VisADException
    {
	SetMaker	setMaker;
	if (vector instanceof BooleanPrimitiveVector)
	    setMaker = new SetMaker((BooleanPrimitiveVector)vector, table);
	else if (vector instanceof BytePrimitiveVector)
	    setMaker = new SetMaker((BytePrimitiveVector)vector, table);
	else if (vector instanceof Int16PrimitiveVector)
	    setMaker = new SetMaker((Int16PrimitiveVector)vector, table);
	else if (vector instanceof Int32PrimitiveVector)
	    setMaker = new SetMaker((Int32PrimitiveVector)vector, table);
	else if (vector instanceof Float32PrimitiveVector)
	    setMaker = new SetMaker((Float32PrimitiveVector)vector, table);
	else if (vector instanceof Float64PrimitiveVector)
	    setMaker = new SetMaker((Float64PrimitiveVector)vector, table);
	else if (vector instanceof BaseTypePrimitiveVector)
	    setMaker = new SetMaker((BaseTypePrimitiveVector)vector, table);
	else
	    throw new BadFormException(
		"SetMaker.instance(PrimitiveVector,AttributeTable): " +
		"Unknown DODS primitive vector");
	return setMaker;
    }

    public MathType getMathType()
    {
	return setType;
    }

    protected static MathType domainMathType(DArray array, AttributeTable table)
	throws BadFormException, VisADException
    {
	int		rank = array.numDimensions();
	RealType[]	realTypes = new RealType[rank];
	for (int i = 0; i < rank; ++i)
	{
	    String	dimName;
	    try
	    {
		dimName = array.getDimension(i).getName();
	    }
	    catch (InvalidParameterException e)
	    {
		throw new BadFormException(
		    "SetMaker.domainMathType(DArray,AttributeTable): " +
		    "Could get DArray dimension " + i + ": " + e);
	    }
	    realTypes[rank-1-i] =// accomodate reversed innermost to outermost
		DODSUtil.realType(
		    dimName, DODSUtil.getAttributeTable(table, dimName));
	}
	return DODSUtil.mathType(realTypes);
    }

    protected static MathType domainMathType(DGrid grid, AttributeTable table)
	throws BadFormException, VisADException
    {
	DArray		array;
	try
	{
	    array = (DArray)grid.getVar(0);
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		"SetMaker.domainMathType(DGrid,AttributeTable): " +
		"Couldn't get DGrid DArray: " + e);
	}
	int		rank = array.numDimensions();
	RealType[]	realTypes = new RealType[rank];
	for (int i = 1; i <= rank; ++i)
	{
	    DList	list;
	    try
	    {
		list = (DList)grid.getVar(i);
	    }
	    catch (NoSuchVariableException e)
	    {
		throw new BadFormException(
		    "SetMaker.domainMathType(DGrid,AttributeTable): " +
		    "Couldn't get DGrid variable " + i + ": " + e);
	    }
	    BaseType	template =
		list.getPrimitiveVector().getTemplate();
	    realTypes[rank-i] =	// reverse dimension order
		DODSUtil.getRealType(
		    template, DODSUtil.getAttributeTable(table, template));
	}
	return DODSUtil.mathType(realTypes);
    }

    protected static SetType setType(DSequence sequence, AttributeTable table)
	throws BadFormException, VisADException
    {
	int		count = sequence.elementCount();
	MathType[]	mathTypes = new MathType[count];
	for (int i = 0; i < count; ++i)
	{
	    BaseType	template;
	    try
	    {
		template = sequence.getVar(i);
	    }
	    catch (NoSuchVariableException e)
	    {
		throw new BadFormException(
		    "SetMaker.setType(DSequence,AttributeTable): " +
		    "Couldn't get DSequence variable " + i + ": " + e);
	    }
	    mathTypes[i] =
		DODSUtil.mathType(
		    template, DODSUtil.getAttributeTable(table, template));
	}
	return new SetType(DODSUtil.mathType(mathTypes));
    }

    public Gridded1DSet set(PrimitiveVector vector)
    {
	return null;	// TODO
    }
}
