package visad.data.dods;

import dods.dap.*;
import java.util.WeakHashMap;
import visad.data.BadFormException;

public final class ScaleAndOffsetUnpacker
    extends	ValueUnpacker
{
    private final float			floatScale;
    private final double		doubleScale;
    private static final WeakHashMap	map = new WeakHashMap();

    private ScaleAndOffsetUnpacker(double scale)
    {
	floatScale = (float)scale;
	doubleScale = scale;
    }

    public static synchronized ScaleAndOffsetUnpacker instance(
	double scale, double offset)
    {
	Double[]	key =
	    new Double[] {new Double(scale), new Double(offset)};
	ScaleAndOffsetUnpacker	unpacker = (ScaleAndOffsetUnpacker)map.get(key);
	if (unpacker == null)
	{
	    unpacker = ScaleAndOffsetUnpacker.instance(scale, offset);
	    map.put(key, unpacker);
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
}
