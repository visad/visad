package visad.data.in;

import java.rmi.RemoteException;
import java.util.*;
import visad.*;

/**
 * NB: This class is <em>not</em> a subclass of {@link VirtualData}.
 */
abstract public class Range 
    implements	Cloneable
{
    private final int		length;

    /**
     * @param type		May be <code>null</code>.
     * @param length		Must be non-negative; may be zero.
     */
    protected Range(int length)
	throws VisADException
    {
	if (length < 0)
	    throw new VisADException(
		getClass().getName() + ".<init>(int): Negative length");
	this.length = length;
    }

    /**
     * @return			May be zero.
     */
    public final int getLength()
    {
	return length;
    }

    /**
     * @return			May be <code>null</code>.
     */
    abstract public MathType getMathType();

    public Object clone()
    {
	try
	{
	    return super.clone();
	}
	catch (CloneNotSupportedException e)
	{
	    return null;	// can't happen because Cloneable implemented
	}
    }

    /**
     * The default action is to throw an exception.
     * Override this method as necessary in composite subclasses.
     */
    public void add(Range range)
	throws VisADException
    {
	throw new NotMergeableException(
	    getClass().getName() + ".add(Range): Immutable range");
    }

    /**
     * The default action is to return this object only.
     * Override this method as necessary in appropriate composite subclasses.
     */
    public List getComponents()
    {
	List	list = new ArrayList(1);
	list.add(this);
	return list;
    }

    /**
     * Returns the VisAD data object at a sample position.
     */
    public abstract Data getDatum(int index)
	throws VisADException, RemoteException;

    /**
     * This default implementation is potentially slow.
     */
    protected void set(Field field)
	throws VisADException, RemoteException
    {
	for (int i = 0; i < length; ++i)
	    field.setSample(i, getDatum(i), false);
    }

    protected abstract Range getSubRange(int offset, int length)
        throws VisADException;
}
