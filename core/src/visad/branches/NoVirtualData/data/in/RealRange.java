package visad.data.in;

import java.rmi.RemoteException;
import visad.*;

/**
 * NB: This class is <em>not</em> a subclass of {@link VirtualData}.
 *
 * Instances are immutable.
 */
abstract public class RealRange
    extends	ScalarRange
{
    protected RealRange(RealType type, int length)
    {
	super(type, length);
    }

    public RealType getRealType()
    {
	return (RealType)getScalarType();
    }

    /**
     * Returns the VisAD data object at a sample position.
     */
    public Data getDatum(int index)
	throws VisADException
    {
	return new Real(getRealType(), getDouble(index));
    }

    protected abstract double getDouble(int index);
}
