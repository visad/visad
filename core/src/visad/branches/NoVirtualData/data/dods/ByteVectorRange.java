package visad.data.dods;

import dods.dap.*;
import visad.data.in.*;
import visad.*;

public final class ByteVectorRange
    extends FloatRange
{
    private final BytePrimitiveVector	vector;
    private final Valuator		valuator;
    private final int			offset;

    private ByteVectorRange(
	    BytePrimitiveVector vector,
	    RealType realType,
	    int offset,
	    int length,
	    Valuator valuator)
	throws VisADException
    {
	super(realType, length);
	this.vector = vector;
	this.offset = offset;
	this.valuator = valuator;
    }

    public static ByteVectorRange instance(
	    BytePrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	return instance(vector, table, 0, vector.getLength());
    }

    public static ByteVectorRange instance(
	    BytePrimitiveVector vector,
	    AttributeTable table,
	    int offset,
	    int length)
	throws VisADException
    {
	return
	    new ByteVectorRange(
		vector,
		DODSUtil.getRealType(vector.getTemplate(), table),
		offset,
		length,
		Valuator.instance(table));
    }

    public float[][] getFloats()
    {
	float[]	values = new float[getLength()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = vector.getValue(offset+i);
	return new float[][] {valuator.process(values)};
    }

    protected double getDouble(int index)
	throws IndexOutOfBoundsException
    {
	if (index < 0 || index >= getLength())
	    throw new IndexOutOfBoundsException(
		getClass().getName() + ".getDouble(int): " +
		"Invalid index: " + index);
	return valuator.process(vector.getValue(offset+index));
    }

    public Range getSubRange(int offset, int length)
	throws VisADException
    {
	return
	    new ByteVectorRange(
		vector, getRealType(), this.offset+offset, length, valuator);
    }
}
