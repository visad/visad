package visad.data.dods;

import dods.dap.*;
import visad.data.in.*;
import visad.*;

public class Int32VectorRange
    extends FloatRange
{
    private final Int32PrimitiveVector	vector;
    private final Valuator		valuator;
    private final int			offset;

    private Int32VectorRange(
	    Int32PrimitiveVector vector,
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

    public static Int32VectorRange instance(
	    Int32PrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	return instance(vector, table, 0, vector.getLength());
    }

    public static Int32VectorRange instance(
	    Int32PrimitiveVector vector,
	    AttributeTable table,
	    int offset,
	    int length)
	throws VisADException
    {
	return
	    new Int32VectorRange(
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

    public double getDouble(int index)
    {
	return valuator.process(vector.getValue(offset+index));
    }

    public Range getSubRange(int offset, int length)
	throws VisADException
    {
	return
	    new Int32VectorRange(
		vector, getRealType(), this.offset+offset, length, valuator);
    }
}
