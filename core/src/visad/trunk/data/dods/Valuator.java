package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.data.BadFormException;
import visad.data.in.ValueProcessor;
import visad.VisADException;

public class Valuator
    extends	ValueProcessor
{
    private final ValueVetter	vetter;
    private final ValueUnpacker	unpacker;

    protected Valuator(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	vetter = ValueVetter.valueVetter(table);
	unpacker = ValueUnpacker.valueUnpacker(table);
    }

    public static Valuator valuator(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	return new Valuator(table);
    }

    public double getMin()
    {
	return vetter.getMin();
    }

    public double getMax()
    {
	return vetter.getMax();
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
