package visad.data;


/**
 * A leaf-node in the data form hierarchy for the storage of 
 * persistent data objects.
 */
public abstract class
Form
    extends FormNode
{
    /**
     * Construct a data form of the given name.
     */
    public Form(String name)
    {
	super(name);
    }
}
