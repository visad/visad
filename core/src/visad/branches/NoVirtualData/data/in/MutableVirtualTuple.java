package visad.data.in;

import java.util.*;
import visad.*;

/**
 * Instances are mutable.
 */
public class MutableVirtualTuple
    extends VirtualTuple
{
    public MutableVirtualTuple()
	throws VisADException
    {
	this(new VirtualData[0]);
    }

    public MutableVirtualTuple(VirtualData[] components)
	throws VisADException
    {
	this(newList(components));
    }

    /**
     * Doesn't clone the list.
     */
    public MutableVirtualTuple(List components)
	throws VisADException
    {
	super(components);
    }

    /**
     */
    public void add(VirtualData other)
	throws NotMergeableException, VisADException
    {
	List	datums = other.getComponents();
	components.addAll(datums);
	allReals &= isAllReals(datums);
	setMathType();
    }

    /**
     * Doesn't clone the virtual datums.
     */
    public List getComponents()
    {
	return components;
    }
}
