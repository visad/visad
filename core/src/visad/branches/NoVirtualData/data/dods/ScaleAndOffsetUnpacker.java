package visad.data.dods;

import dods.dap.*;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import visad.data.BadFormException;

public final class ScaleAndOffsetUnpacker
    extends	ValueUnpacker
{
    private final float			floatScale;
    private final double		doubleScale;
    private final float			floatOffset;
    private final double		doubleOffset;
    private static final WeakHashMap	map = new WeakHashMap();

    private ScaleAndOffsetUnpacker(double scale, double offset)
    {
	floatScale = (float)scale;
	doubleScale = scale;
	floatOffset = (float)offset;
	doubleOffset = offset;
    }

    public static synchronized ScaleAndOffsetUnpacker scaleAndOffsetUnpacker(
	double scale, double offset)
    {
	ScaleAndOffsetUnpacker	unpacker =
	    new ScaleAndOffsetUnpacker(scale, offset);
	WeakReference	ref = (WeakReference)map.get(unpacker);
	if (ref == null)
	{
	    map.put(unpacker, new WeakReference(unpacker));
	}
	else
	{
	    ScaleAndOffsetUnpacker	oldUnpacker =
		(ScaleAndOffsetUnpacker)ref.get();
	    if (oldUnpacker == null)
		map.put(unpacker, new WeakReference(unpacker));
	    else
		unpacker = oldUnpacker;
	}
	return unpacker;
    }

    public float process(float value)
    {
	return floatScale*value;
    }

    public double process(double value)
    {
	return doubleScale*value;
    }

    public float[] process(float[] values)
    {
	for (int i = 0; i < values.length; ++i)
	    values[i] = values[i]*floatScale + floatOffset;
	return values;
    }

    public double[] process(double[] values)
    {
	for (int i = 0; i < values.length; ++i)
	    values[i] = values[i]*doubleScale + doubleOffset;
	return values;
    }

    public boolean equals(Object obj)
    {
	boolean	equals;
	if (!(obj instanceof ScaleAndOffsetUnpacker))
	{
	    equals = false;
	}
	else
	{
	    ScaleAndOffsetUnpacker	that = (ScaleAndOffsetUnpacker)obj;
	    equals = this == that || (
		doubleOffset == that.doubleOffset &&
		doubleScale == that.doubleScale);
	}
	return equals;
    }

    public int hashCode()
    {
	return
	    new Double(doubleOffset).hashCode() ^
	    new Double(doubleScale).hashCode();
    }
}
