package visad.data.dods;

import dods.dap.*;
import dods.dap.Server.InvalidParameterException;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS {@link DArray} variables to the
 * {@link visad.data.in} context.
 */
public class ArrayVariableAdapter
    extends	VariableAdapter
{
    private final FunctionType		funcType;
    private final VectorAdapter		vectorAdapter;

    private ArrayVariableAdapter(
	    DArray array,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	vectorAdapter =
	    factory.vectorAdapter(array.getPrimitiveVector(), table);
	int		rank = array.numDimensions();
	RealType[]	realTypes = new RealType[rank];
	for (int i = 0; i < rank; ++i)
	{
	    try
	    {
		String	dimName = array.getDimension(i).getName();
		realTypes[rank-1-i] =	// reverse dimension order
		    realType(dimName, attributeTable(table, dimName));
	    }
	    catch (InvalidParameterException e)
	    {
		throw new BadFormException(
		    getClass().getName() + ".<init>: " +
		    "Couldn't get DArray dimension: " + e);
	    }
	}
	funcType =
	    new FunctionType(mathType(realTypes), vectorAdapter.getMathType());
    }

    public static ArrayVariableAdapter arrayVariableAdapter(
	    DArray array, AttributeTable table, VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new ArrayVariableAdapter(array, table, factory);
    }

    public MathType getMathType()
    {
	return funcType;
    }

    public FunctionType getFunctionType()
    {
	return funcType;
    }

    public SimpleSet[] getRepresentationalSets()
    {
	return vectorAdapter.getRepresentationalSets();
    }

    public DataImpl data(DArray array)
	throws VisADException, RemoteException
    {
	RealTupleType	domainType = funcType.getDomain();
	int		rank = domainType.getDimension();
	int[]		firsts = new int[rank];
	int[]		lasts = new int[rank];
	int[]		lengths = new int[rank];
	boolean		allIntegerSets = true;
	for (int i = 0; i < rank; ++i)
	{
	    int			j = rank - 1 - i;	// reverse dimensions
	    DArrayDimension	dim;
	    try
	    {
		dim = array.getDimension(i);
	    }
	    catch (InvalidParameterException e)
	    {
		throw new BadFormException(
		    getClass().getName() + ".data(DArray,...): " +
		    "Couldn't get DArray dimension: " + e);
	    }
	    int			first = dim.getStart();
	    int			last = dim.getStop();
	    int			stride = dim.getStride();
	    firsts[j] = first;
	    lasts[j] = last;
	    lengths[j] = 1 + (last - first) / stride;
	    allIntegerSets &=
		((stride == 1 && first == 0) || (stride == -1 && last == 0));
	}
	SampledSet	domain =
	    allIntegerSets
		? (SampledSet)IntegerNDSet.create(domainType, lengths)
		: (SampledSet)LinearNDSet.create(
		    domainType, doubleArray(firsts), doubleArray(lasts),
		    lengths);
	FieldImpl	field;
	PrimitiveVector	vector = array.getPrimitiveVector();
	if (vectorAdapter.isFlat())
	{
	    field =
		new FileFlatField(
		    new VectorAccessor(funcType, vectorAdapter, domain, vector),
		    getCacheStrategy());
	}
	else
	{
	    field = new FieldImpl(funcType, domain);
	    vectorAdapter.setField(vector, field);
	}
	return field;
    }

    /**
     * Useful for handling the DArray portion of a DODS DGrid.
     */
    public void setField(DArray array, Field field)
	throws VisADException, RemoteException
    {
	vectorAdapter.setField(array.getPrimitiveVector(), field);
    }

    private double[] doubleArray(int[] ints)
    {
	double[]	doubles = new double[ints.length];
	for (int i = 0; i < ints.length; ++i)
	    doubles[i] = ints[i];
	return doubles;
    }
}
