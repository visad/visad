package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.*;

public class Float32VectorRange
    extends FloatRange
{
    private final Float32PrimitiveVector	vector;
    private final Valuator			valuator;
    private final int				offset;

    private Float32VectorRange(
	    Float32PrimitiveVector vector,
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

    public static Float32VectorRange instance(
	    Float32PrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	return instance(vector, table, 0, vector.getLength());
    }

    public static Float32VectorRange instance(
	    Float32PrimitiveVector vector,
	    AttributeTable table,
	    int offset,
	    int length)
	throws VisADException
    {
	return
	    new Float32VectorRange(
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
	    new Float32VectorRange(
		vector, getRealType(), this.offset+offset, length, valuator);
    }
}
