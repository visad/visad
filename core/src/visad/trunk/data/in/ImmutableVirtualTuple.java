package visad.data.in;

import java.util.List;
import visad.*;

/**
 * Instances are immutable.
 */
public class ImmutableVirtualTuple
    extends VirtualTuple
{
    /**
     * Copies the array but not the elements.
     */
    public ImmutableVirtualTuple(VirtualData[] components)
	throws VisADException
    {
	this(newList(components));
    }

    /**
     * Uses the actual list argument.
     */
    public ImmutableVirtualTuple(List components)
	throws VisADException
    {
	super(components);
    }
}
