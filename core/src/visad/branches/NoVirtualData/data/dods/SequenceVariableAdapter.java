package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;
import visad.*;
import visad.data.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS {@link DSequence} variables to the
 * {@link visad.data.in} context.
 */
public class SequenceVariableAdapter
    extends	VariableAdapter
{
    private final FunctionType		funcType;
    private final VariableAdapter[]	adapters;
    private final SimpleSet[]		repSets;

    private SequenceVariableAdapter(
	    DSequence sequence,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	int		count = sequence.elementCount();
	ArrayList	setList = new ArrayList();
	adapters = new VariableAdapter[count];
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
		    getClass().getName() + ".data(DSequence,...): " +
		    "Couldn't get sequence-variable " + i);
	    }
	    adapters[i] =
		factory.variableAdapter(
		    template, attributeTable(table, template));
	    SimpleSet[]	setArray = adapters[i].getRepresentationalSets();
	    for (int j = 0; j < setArray.length; ++j)
		setList.add(setArray[j]);
	}
	funcType = new FunctionType(RealType.Generic, mathType(adapters));
	repSets = (SimpleSet[])setList.toArray(new SimpleSet[0]);
    }

    public static SequenceVariableAdapter sequenceVariableAdapter(
	    DSequence sequence,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new SequenceVariableAdapter(sequence, table, factory);
    }

    public MathType getMathType()
    {
	return funcType;
    }

    public SimpleSet[] getRepresentationalSets()
    {
	return repSets;
    }

    public DataImpl data(DSequence sequence)
	throws VisADException, RemoteException
    {
	SampledSet	domain = new Integer1DSet(sequence.getRowCount());
	FieldImpl	field;
	if (funcType.getFlat())
	{
	    field =
		new FileFlatField(
		    new SequenceAccessor(domain, sequence), getCacheStrategy());
	}
	else
	{
	    field = new FieldImpl(funcType, domain);
	    setField(domain, sequence, field);
	}
	return field;
    }

    protected void setField(
	    SampledSet domain, DSequence sequence, FieldImpl field)
	throws VisADException, RemoteException
    {
	int		sampleCount = domain.getLength();
	DataImpl	data;
	MathType	rangeType = funcType.getRange();
	for (int i = 0; i < sampleCount; ++i)
	{
	    Vector	row = sequence.getRow(i);
	    if (adapters.length == 1)
	    {
		data = adapters[0].data((BaseType)row.get(0));
	    }
	    else if (rangeType instanceof RealTupleType)
	    {
		Real[]	components = new Real[adapters.length];
		for (int j = 0; j < components.length; ++j)
		    components[j] =
			(Real)adapters[j].data((BaseType)row.get(j));
		data =
		    new RealTuple(
			(RealTupleType)rangeType, components, null);
	    }
	    else
	    {
		Data[]	components = new Data[adapters.length];
		for (int j = 0; j < components.length; ++j)
		    components[j] = adapters[j].data((BaseType)row.get(j));
		data = new Tuple((TupleType)rangeType, components);
	    }
	    field.setSample(i, data, /*copy=*/false);
	}
    }

    protected class SequenceAccessor
	extends	FileAccessor
    {
	private final SampledSet	domain;
	private final DSequence		sequence;

	public SequenceAccessor(SampledSet domain, DSequence sequence)
	{
	    this.domain = domain;
	    this.sequence = sequence;
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
		    repSets,
		    (Unit[])null);
	    setField(domain, sequence, field);
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
