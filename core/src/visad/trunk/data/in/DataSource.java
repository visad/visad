package visad.data.in;

/**
 * Interface for sources of VisAD data objects.
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
     * @return			<code>true</code> if and only if the specified
     *				dataset was successfully converted into a
     *				VisAD data object.
     */
    public abstract boolean open(String spec);
}
