package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;
import visad.data.in.*;

/**
 * Provides support for adapting DODS {@link DSequence} variables to the
 * {@link visad.data.in} context.
 */
public class DSequenceAdapter
    extends	VariableAdapter
{
    private final VirtualData	virtualData;

    private DSequenceAdapter(
	    DSequence sequence, AttributeTable table, AdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	virtualData = 
	    VirtualField.instance(
		new Domain(
		    new VirtualSet(new Integer1DSet(sequence.getRowCount()))),
		DSequenceRange.instance(sequence, table));
    }

    public static DSequenceAdapter instance(
	    DSequence var, AttributeTable table, AdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new DSequenceAdapter(var, table, factory);
    }

    public static FunctionType functionType(
	    DSequence sequence, AttributeTable table)
	throws BadFormException, VisADException
    {
	return new FunctionType(RealType.Generic, rangeType(sequence, table));
    }

    public static MathType rangeType(DSequence sequence, AttributeTable table)
	throws BadFormException, VisADException
    {
	try
	{
	    int		count = sequence.elementCount();
	    MathType[]	mathTypes = new MathType[count];
	    for (int i = 0; i < count; ++i)
	    {
		BaseType	template = sequence.getVar(i);
		mathTypes[i] =
		    DODSUtil.mathType(
			template, DODSUtil.getAttributeTable(table, template));
	    }
	    return mathType(mathTypes);
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		"DSequenceAdapter.rangeType(...): DSequence inquiry failure: " +
		e);
	}
    }

    public VirtualData getVirtualData()
    {
	return virtualData;
    }
}
