package visad.data.in;

import java.rmi.RemoteException;
import visad.data.BadFormException;
import visad.VisADException;

/**
 * Supports sources of VisAD data objects.
 *
 * @author Steven R. Emmerson
 */
abstract public class DataSource
    extends DataFilter
{
    /**
     * Constructs from a downstream data sink.
     *
     * @param downstream	The downstream data sink.
     */
    protected DataSource(DataSink downstream)
    {
	super(downstream);
    }

    /**
     * Opens an existing dataset.
     *
     * @param spec		The specification of the existing dataset.
     * @return			The VisAD data object corresponding to the
     *				specified dataset.
     * @throws BadFormException	The DODS dataset is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public abstract void open(String spec)
	throws BadFormException, RemoteException, VisADException;
}
