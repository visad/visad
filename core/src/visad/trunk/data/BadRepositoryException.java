package visad.data;


import visad.VisADException;


/**
 * Exception thrown when there's something wrong with the repository.
 */
public class BadRepositoryException extends VisADException
{
    /**
     * Construct an exception with a message.
     */
    public BadRepositoryException(String msg)
    {
	super(msg);
    }
}
