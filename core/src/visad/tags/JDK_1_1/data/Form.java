package visad.data;


import visad.MathType;


/**
 * A leaf-node in the data form hierarchy for the storage of 
 * persistent data objects.
 */
public abstract class
Form
    extends FormNode
{
    /**
     * The MathType of an existing data object.  Set by the
     * getForms(Data data) method.
     */
    protected MathType	mathType;


    /**
     * Construct a data form of the given name.
     */
    public Form(String name)
    {
	super(name);
    }


    /**
     * Get the MathType.
     */
    public MathType
    getMathType()
    {
	return mathType;
    }
}
