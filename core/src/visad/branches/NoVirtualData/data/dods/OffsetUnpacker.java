package visad.data.dods;

import dods.dap.*;
import java.util.WeakHashMap;
import visad.data.BadFormException;

public final class OffsetUnpacker
    extends	ValueUnpacker
{
    private final float			floatOffset;
    private final double		doubleOffset;
    private static final WeakHashMap	map = new WeakHashMap();

    private OffsetUnpacker(double offset)
    {
	floatOffset = (float)offset;
	doubleOffset = offset;
    }

    public static synchronized OffsetUnpacker instance(double offset)
    {
	Double	key = new Double(offset);
	OffsetUnpacker	unpacker = (OffsetUnpacker)map.get(key);
	if (unpacker == null)
	{
	    unpacker = new OffsetUnpacker(offset);
	    map.put(key, unpacker);
	}
	return unpacker;
    }

    public float process(float value)
    {
	return floatOffset + value;
    }

    public double process(double value)
    {
	return doubleOffset + value;
    }

    public float[] process(float[] values)
    {
	for (int i = 0; i < values.length; ++i)
	    values[i] += floatOffset;
	return values;
    }

    public double[] process(double[] values)
    {
	for (int i = 0; i < values.length; ++i)
	    values[i] += doubleOffset;
	return values;
    }
}
