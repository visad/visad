package visad.data;


import visad.VisADException;


/**
 * Exception thrown when the form that the data is in is incorrect.
 */
public class
BadFormException
    extends VisADException
{
    /**
     * Construct an exception with a message.
     */
    public BadFormException(String msg)
    {
	super(msg);
    }
}
