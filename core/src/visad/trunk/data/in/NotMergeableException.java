package visad.data.in;

import visad.VisADException;

/**
 * Exception thrown when {@link VirtualData} objects can't be merged.
 */
public class
NotMergeableException
    extends VisADException
{
    /**
     * Construct an exception with a message.
     */
    public NotMergeableException(String msg)
    {
	super(msg);
    }
}
