package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS {@link DGrid} variables to the
 * {@link visad.data.in} context.
 */
public class GridVariableAdapter
    extends	VariableAdapter
{
    private final ArrayVariableAdapter		arrayAdapter;
    private final FunctionType			funcType;
    private final boolean			isFlat;
    private final GridVariableMapAdapter[]	domainAdapters;

    private GridVariableAdapter(
	    GridVariableMapAdapter[] domainAdapters,
	    ArrayVariableAdapter arrayAdapter)
	throws BadFormException, VisADException, RemoteException
    {
	MathType	rangeType = arrayAdapter.getFunctionType().getRange();
	funcType = new FunctionType(mathType(domainAdapters), rangeType);
	this.arrayAdapter = arrayAdapter;
	isFlat = isFlat(rangeType);
	this.domainAdapters = domainAdapters;
    }

    public static GridVariableAdapter gridVariableAdapter(
	    DGrid grid, AttributeTable table, VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	ArrayVariableAdapter		arrayAdapter;
	MathType			domainType;
	GridVariableMapAdapter[]	domainAdapters;
	try
	{
	    DArray	array = (DArray)grid.getVar(0);
	    int		rank = array.numDimensions();
	    arrayAdapter = factory.arrayVariableAdapter(array, table);
	    domainAdapters = new GridVariableMapAdapter[rank];
	    for (int i = 1; i <= rank; ++i)
	    {
		array = (DArray)grid.getVar(i);
		BaseType	template =
		    array.getPrimitiveVector().getTemplate();
		domainAdapters[rank-i] =	// reverse dimensions
		    factory.gridVariableMapAdapter(
			array, attributeTable(table, template));
	    }
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
	    "visad.data.dods.GridVariableAdapter.gridVariableAdapter(...): " +
		"No such variable: " + e);
	}
	return new GridVariableAdapter(domainAdapters, arrayAdapter);
    }

    public MathType getMathType()
    {
	return funcType;
    }

    public SimpleSet[] getRepresentationalSets()
    {
	return arrayAdapter.getRepresentationalSets();
    }

    public DataImpl data(DGrid grid)
	throws VisADException, RemoteException
    {
	FieldImpl	field;
	try
	{
	    int		rank = domainAdapters.length;
	    SampledSet	domain;
	    if (rank == 1)
	    {
		domain =
		    (SampledSet)domainAdapters[0].data((DArray)grid.getVar(1));
	    }
	    else
	    {
		SampledSet[]	domainSets = new SampledSet[rank];
		for (int i = 0; i < rank; ++i)
		    domainSets[i] = (SampledSet)
			domainAdapters[i].data(
			    (DArray)grid.getVar(rank-i));
		domain = new ProductSet(funcType.getDomain(), domainSets);
	    }
	    DArray	array = (DArray)grid.getVar(0);
	    if (isFlat)
	    {
		field =
		    new FileFlatField(
			new GridAccessor(domain, array), getCacheStrategy());
	    }
	    else
	    {
		field = new FieldImpl(funcType, domain);
		arrayAdapter.setField(array, field);
	    }
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		getClass().getName() + ".data(...): " +
		"No such variable: " + e);
	}
	return field;
    }

    public class GridAccessor
	extends	FileAccessor
    {
	private final SampledSet	domain;
	private final DArray		array;

	public GridAccessor(SampledSet domain, DArray array)
	{
	    this.domain = domain;
	    this.array = array;
	}

	public FunctionType getFunctionType()
	{
	    return funcType;
	}

	public FlatField getFlatField()
	    throws VisADException, RemoteException
	{
	    FlatField	field =
		new FlatField(
		    funcType,
		    domain,
		    (CoordinateSystem[])null,
		    arrayAdapter.getRepresentationalSets(),
		    (Unit[])null);
	    arrayAdapter.setField(array, field);
	    return field;
	}

	public void writeFlatField(
	    double[][] values, FlatField template, int[] fileLocation)
	{}

	public double[][] readFlatField(FlatField template, int[] fileLocation)
	{
	    return null;
	}

	public void writeFile(int[] fileLocation, Data range)
	{}
    }
}
