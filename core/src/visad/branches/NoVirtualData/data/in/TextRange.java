package visad.data.in;

import java.rmi.RemoteException;
import visad.*;

/**
 * NB: This class is <em>not</em> a subclass of {@link VirtualData}.
 */
abstract public class TextRange
    extends	ScalarRange
{
    protected TextRange(TextType type, int length)
    {
	super(type, length);
    }

    public TextType getTextType()
    {
	return (TextType)getScalarType();
    }

    /**
     * Returns the VisAD data object at a sample position.
     */
    public Data getDatum(int index)
	throws VisADException
    {
	return new Text(getTextType(), getText(index));
    }

    protected abstract String getText(int index);
}
