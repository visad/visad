package visad.data.dods;

import dods.dap.*;
import java.lang.ref.WeakReference;
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

    public static synchronized OffsetUnpacker offsetUnpacker(double offset)
    {
	OffsetUnpacker	unpacker = new OffsetUnpacker(offset);
	WeakReference	ref = (WeakReference)map.get(unpacker);
	if (ref == null)
	{
	    map.put(unpacker, new WeakReference(unpacker));
	}
	else
	{
	    OffsetUnpacker	oldUnpacker = (OffsetUnpacker)ref.get();
	    if (oldUnpacker == null)
		map.put(unpacker, new WeakReference(unpacker));
	    else
		unpacker = oldUnpacker;
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

    public boolean equals(Object obj)
    {
	boolean	equals;
	if (!(obj instanceof OffsetUnpacker))
	{
	    equals = false;
	}
	else
	{
	    OffsetUnpacker	that = (OffsetUnpacker)obj;
	    equals = this == that || doubleOffset == that.doubleOffset;
	}
	return equals;
    }

    public int hashCode()
    {
	return new Double(doubleOffset).hashCode();
    }
}
