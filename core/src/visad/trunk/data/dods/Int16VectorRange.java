package visad.data.dods;

import dods.dap.*;
import visad.data.in.*;
import visad.*;

public class Int16VectorRange
    extends FloatRange
{
    private final Int16PrimitiveVector	vector;
    private final Valuator		valuator;
    private final int			offset;

    private Int16VectorRange(
	    Int16PrimitiveVector vector,
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

    public static Int16VectorRange instance(
	    Int16PrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	return instance(vector, table, 0, vector.getLength());
    }

    public static Int16VectorRange instance(
	    Int16PrimitiveVector vector,
	    AttributeTable table,
	    int offset,
	    int length)
	throws VisADException
    {
	return
	    new Int16VectorRange(
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
	    new Int16VectorRange(
		vector, getRealType(), this.offset+offset, length, valuator);
    }
}
