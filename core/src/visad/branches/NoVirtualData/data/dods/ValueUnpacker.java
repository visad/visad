package visad.data.dods;

import dods.dap.*;
import visad.data.BadFormException;
import visad.data.in.*;

public abstract class ValueUnpacker
    extends	ValueProcessor
{
    private static final ValueUnpacker	trivialUnpacker =
	new ValueUnpacker()
	{
	    public float process(float value)
	    {
		return value;
	    }
	    public double process(double value)
	    {
		return value;
	    }
	    public float[] process(float[] values)
	    {
		return values;
	    }
	    public double[] process(double[] values)
	    {
		return values;
	    }
	};

    protected ValueUnpacker()
    {}

    /**
     * @param table		The DODS attribute table.  May be 
     *				<code>null</code>, in which case a trivial
     *				unpacker is returned.
     */
    public static ValueUnpacker instance(AttributeTable table)
	throws BadFormException
    {
	ValueUnpacker	unpacker;
	if (table == null)
	{
	    unpacker = trivialUnpacker;
	}
	else
	{
	    double	scale = DODSUtil.decode("scale_factor", table, 0);
	    double	offset = DODSUtil.decode("add_offset", table, 0);
	    if (scale == scale && scale != 1 &&
		offset == offset && offset != 0)
	    {
		unpacker = ScaleAndOffsetUnpacker.instance(scale, offset);
	    }
	    else if (scale == scale && scale != 1)
	    {
		unpacker = ScaleUnpacker.instance(scale);
	    }
	    else if (offset == offset && offset != 0)
	    {
		unpacker = OffsetUnpacker.instance(offset);
	    }
	    else
	    {
		unpacker = trivialUnpacker;
	    }
	}
	return unpacker;
    }

    public final float unpack(float value)
    {
	return process(value);
    }

    public final double unpack(double value)
    {
	return process(value);
    }

    public final float[] unpack(float[] values)
    {
	return process(values);
    }

    public final double[] unpack(double[] values)
    {
	return process(values);
    }
}
