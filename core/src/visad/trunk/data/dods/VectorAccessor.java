package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;

public class VectorAccessor
    extends	FileAccessor
{
    private final FunctionType		funcType;
    private final VectorAdapter		vectorAdapter;
    private final SampledSet		domain;
    private final PrimitiveVector	vector;

    public VectorAccessor(
	FunctionType funcType,
	VectorAdapter vectorAdapter,
	SampledSet domain,
	PrimitiveVector vector)
    {
	this.funcType = funcType;
	this.vectorAdapter = vectorAdapter;
	this.domain = domain;
	this.vector = vector;
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
		vectorAdapter.getRepresentationalSets(),
		(Unit[])null);
	vectorAdapter.setField(vector, field);
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
