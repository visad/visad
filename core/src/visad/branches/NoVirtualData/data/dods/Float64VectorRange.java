package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.*;

public class Float64VectorRange
    extends DoubleRange
{
    private final Float64PrimitiveVector	vector;
    private final Valuator			valuator;
    private final int				offset;

    private Float64VectorRange(
	    Float64PrimitiveVector vector,
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

    public static Float64VectorRange instance(
	    Float64PrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	return instance(vector, table, 0, vector.getLength());
    }

    public static Float64VectorRange instance(
	    Float64PrimitiveVector vector,
	    AttributeTable table,
	    int offset,
	    int length)
	throws VisADException
    {
	return
	    new Float64VectorRange(
		vector,
		DODSUtil.getRealType(vector.getTemplate(), table),
		offset,
		length,
		Valuator.instance(table));
    }

    public double[][] getDoubles()
    {
	double[]	values = new double[getLength()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = vector.getValue(offset+i);
	return new double[][] {valuator.process(values)};
    }

    public double getDouble(int index)
    {
	return valuator.process(vector.getValue(offset+index));
    }

    public Range getSubRange(int offset, int length)
	throws VisADException
    {
	return
	    new Float64VectorRange(
		vector, getRealType(), this.offset+offset, length, valuator);
    }
}
