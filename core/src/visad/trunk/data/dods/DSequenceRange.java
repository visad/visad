package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import java.util.*;
import visad.*;
import visad.data.BadFormException;
import visad.data.in.*;

public class DSequenceRange
    extends	Range
{
    private final DSequence	sequence;
    private MathType		mathType;
    private DataMaker[]		dataMakers;
    private final int		offset;

    public static DSequenceRange instance(
	    DSequence sequence, AttributeTable table)
	throws VisADException, RemoteException
    {
	return instance(sequence, table, 0, sequence.getRowCount());
    }

    public static DSequenceRange instance(
	    DSequence sequence, AttributeTable table, int offset, int length)
	throws BadFormException, VisADException, RemoteException
    {
	int		count = sequence.elementCount();
	MathType[]	mathTypes = new MathType[count];
	DataMaker[]	dataMakers = new DataMaker[count];
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
		    "DSequenceRange.instance(DSequence,AttributeTable,...): " +
		    "Couldn't get sequence-variable " + i);
	    }
	    dataMakers[i] =
		DataMakerFactory.instance().dataMaker(
		    template, DODSUtil.getAttributeTable(table, template));
	    mathTypes[i] = dataMakers[i].getMathType();
	}
	return new DSequenceRange(
	    sequence,
	    DODSUtil.mathType(mathTypes),
	    offset,
	    length,
	    dataMakers);
    }

    /**
     * Uses the actual DataMaker[] argument.
     */
    private DSequenceRange(
	    DSequence sequence,
	    MathType mathType,
	    int offset, int length,
	    DataMaker[] dataMakers)
	throws VisADException
    {
	super(length);
	if (offset < 0)
	    throw new VisADException(
		getClass().getName() + ".<init>(...): " +
		"Negative offset");
	if (offset + length > sequence.getRowCount())
	    throw new VisADException(
		getClass().getName() + ".<init>(...): " +
		"Offset + length is greater than DSequence row-count");
	this.sequence = sequence;
	this.mathType = mathType;
	this.offset = offset;
	this.dataMakers = dataMakers;
    }

    public MathType getMathType()
    {
	return mathType;
    }

    public Data getDatum(int index)
	throws IndexOutOfBoundsException, VisADException, RemoteException
    {
	Data	data;
	if (index < 0 || index >= getLength())
	    throw new IndexOutOfBoundsException(
		getClass().getName() + ".getDatum(int): " +
		"Invalid index: " + index);
	Vector	row = sequence.getRow(offset+index);
	int	count = row.size();
	if (count == 1)
	{
	    data = dataMakers[index].data((BaseType)row.get(0));
	}
	else if (mathType instanceof RealTupleType)
	{
	    Real[]	components = new Real[count];
	    for (int i = 0; i < components.length; ++i)
		components[i] = (Real)dataMakers[i].data((BaseType)row.get(i));
	    data = new RealTuple((RealTupleType)mathType, components, null);
	}
	else
	{
	    Data[]	components = new Data[count];
	    for (int i = 0; i < components.length; ++i)
		components[i] = dataMakers[i].data((BaseType)row.get(i));
	    data = new Tuple((TupleType)mathType, components);
	}
	return data;
    }

    public Range getSubRange(int offset, int length)
	throws VisADException
    {
	return new DSequenceRange(
	    sequence, mathType, this.offset+offset, length, dataMakers);
    }
}
