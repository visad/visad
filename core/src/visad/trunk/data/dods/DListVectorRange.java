package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.in.Range;

/**
 * Instances are immutable.
 */
public class DListVectorRange
    extends BaseTypeVectorRange
{
    private final BaseTypePrimitiveVector	vector;
    private final int				offset;

    public static DListVectorRange instance(
	    BaseTypePrimitiveVector vector, AttributeTable table)
	throws VisADException, RemoteException
    {
	return instance(vector, table, factory, 0, vector.getLength());
    }

    public static DListVectorRange instance(
	    BaseTypePrimitiveVector vector,
	    AttributeTable table,
	    int offset,
	    int length)
	throws VisADException, RemoteException
    {
	return new DListVectorRange(vector, offset, length);
    }

    private DListVectorRange(
	    BaseTypePrimitiveVector vector, int offset, int length)
	throws VisADException
    {
	super(vector, offset, length);
    }

    public MathType getMathType()
    {
	return null;	// TODO
    }

    public Data getDatum(int index)
	throws IndexOutOfBoundsException, VisADException, RemoteException
    {
	if (index < 0 || index >= getLength())
	    throw new IndexOutOfBoundsException(
		getClass().getName() + ".getDatum(int): " +
		"Invalid index: " + index);
	return dataMaker.data((DList)vector.getValue(getOffset()+index));
    }

    public Range getSubRange(int offset, int length)
	throws VisADException
    {
	return new BaseTypeVectorRange(
	    vector, this.offset+offset, length, dataMaker);
    }
}
