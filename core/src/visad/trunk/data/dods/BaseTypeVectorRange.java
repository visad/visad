package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.in.Range;

/**
 * Instances are immutable.
 */
public class BaseTypeVectorRange
    extends Range
{
    private final BaseTypePrimitiveVector	vector;
    private final int				offset;
    private final DataMaker			dataMaker;

    public static BaseTypeVectorRange instance(
	    BaseTypePrimitiveVector vector,
	    AttributeTable table,
	    DataMakerFactory factory)
	throws VisADException, RemoteException
    {
	return instance(vector, table, factory, 0, vector.getLength());
    }

    public static BaseTypeVectorRange instance(
	    BaseTypePrimitiveVector vector,
	    AttributeTable table,
	    DataMakerFactory factory,
	    int offset,
	    int length)
	throws VisADException, RemoteException
    {
	return
	    new BaseTypeVectorRange(
		vector,
		offset,
		length, 
		factory.dataMaker(vector.getTemplate(), table));
    }

    private BaseTypeVectorRange(
	    BaseTypePrimitiveVector vector,
	    int offset,
	    int length,
	    DataMaker dataMaker)
	throws VisADException
    {
	super(length);
	if (offset < 0)
	    throw new VisADException(
		getClass().getName() + ".<init>(...): Negative offset");
	if (offset + length > vector.getLength())
	    throw new VisADException(
		getClass().getName() + ".<init>(...): " +
		"Offset + length is greater than size of vector");
	this.vector = vector;
	this.dataMaker = dataMaker;
	this.offset = offset;
    }

    public MathType getMathType()
    {
	return dataMaker.getMathType();
    }

    public Data getDatum(int index)
	throws IndexOutOfBoundsException, VisADException, RemoteException
    {
	if (index < 0 || index >= getLength())
	    throw new IndexOutOfBoundsException(
		getClass().getName() + ".getDatum(int): " +
		"Invalid index: " + index);
	return dataMaker.data(vector.getValue(offset+index));
    }

    public Range getSubRange(int offset, int length)
	throws VisADException
    {
	return new BaseTypeVectorRange(
	    vector, this.offset+offset, length, dataMaker);
    }
}
