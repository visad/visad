package visad.data.dods;

import dods.dap.*;
import visad.data.BadFormException;
import visad.data.in.ValueProcessor;

public class Valuator
    extends	ValueProcessor
{
    private final ValueVetter	vetter;
    private final ValueUnpacker	unpacker;

    protected Valuator(AttributeTable table)
	throws BadFormException
    {
	vetter = ValueVetter.instance(table);
	unpacker = ValueUnpacker.instance(table);
    }

    public static Valuator instance(AttributeTable table)
	throws BadFormException
    {
	return new Valuator(table);
    }

    public float process(float value)
    {
	return unpacker.process(vetter.process(value));
    }

    public float[] process(float[] values)
    {
	return unpacker.process(vetter.process(values));
    }

    public double process(double value)
    {
	return unpacker.process(vetter.process(value));
    }

    public double[] process(double[] values)
    {
	return unpacker.process(vetter.process(values));
    }
}
