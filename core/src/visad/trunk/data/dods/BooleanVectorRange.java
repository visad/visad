package visad.data.dods;

import dods.dap.*;
import visad.data.in.*;
import visad.*;

/**
 * Instances are immutable.
 */
public final class BooleanVectorRange
    extends FloatRange
{
    private final BooleanPrimitiveVector	vector;
    private final int				offset;

    private BooleanVectorRange(
	    BooleanPrimitiveVector vector,
	    RealType realType,
	    int offset,
	    int length)
	throws VisADException
    {
	super(realType, length);
	this.vector = vector;
	this.offset = offset;
    }

    public static BooleanVectorRange instance(
	    BooleanPrimitiveVector vector, AttributeTable table)
	throws VisADException
    {
	return instance(vector, table, 0, vector.getLength());
    }

    public static BooleanVectorRange instance(
	    BooleanPrimitiveVector vector,
	    AttributeTable table,
	    int offset,
	    int length)
	throws VisADException
    {
	return
	    new BooleanVectorRange(
		vector,
		DODSUtil.getRealType(vector.getTemplate(), table),
		offset,
		length);
    }

    public float[][] getFloats()
    {
	float[]	values = new float[getLength()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = vector.getValue(offset+i) ? 1 : 0;
	return new float[][] {values};
    }

    protected double getDouble(int index)
	throws IndexOutOfBoundsException
    {
	if (index < 0 || index >= getLength())
	    throw new IndexOutOfBoundsException(
		getClass().getName() + ".getDouble(int): " +
		"Invalid index: " + index);
	return vector.getValue(offset+index) ? 1 : 0;
    }

    public Range getSubRange(int offset, int length)
	throws VisADException
    {
	return
	    new BooleanVectorRange(
		vector, getRealType(), this.offset+offset, length);
    }
}
