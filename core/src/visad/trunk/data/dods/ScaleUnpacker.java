package visad.data.dods;

import dods.dap.*;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import visad.data.BadFormException;

public final class ScaleUnpacker
    extends	ValueUnpacker
{
    private final float			floatScale;
    private final double		doubleScale;
    private static final WeakHashMap	map = new WeakHashMap();

    private ScaleUnpacker(double scale)
    {
	floatScale = (float)scale;
	doubleScale = scale;
    }

    public static synchronized ScaleUnpacker scaleUnpacker(double scale)
    {
	ScaleUnpacker	unpacker = new ScaleUnpacker(scale);
	WeakReference	ref = (WeakReference)map.get(unpacker);
	if (ref == null)
	{
	    map.put(unpacker, new WeakReference(unpacker));
	}
	else
	{
	    ScaleUnpacker	oldUnpacker = (ScaleUnpacker)ref.get();
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
	    values[i] *= floatScale;
	return values;
    }

    public double[] process(double[] values)
    {
	for (int i = 0; i < values.length; ++i)
	    values[i] *= doubleScale;
	return values;
    }

    public boolean equals(Object obj)
    {
	boolean	equals;
	if (!(obj instanceof ScaleUnpacker))
	{
	    equals = false;
	}
	else
	{
	    ScaleUnpacker	that = (ScaleUnpacker)obj;
	    equals = this == that || doubleScale == that.doubleScale;
	}
	return equals;
    }

    public int hashCode()
    {
	return new Double(doubleScale).hashCode();
    }
}
