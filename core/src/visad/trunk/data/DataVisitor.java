package visad.data;


import visad.FlatField;
import visad.Tuple;
import visad.VisADException;


/**
 * Abstract class for visiting a VisAD data object.  The derived,
 * concrete subclasses are data-form dependent.  The default action
 * upon visiting a VisAD data object is to do nothing and tell the caller
 * to continue.
 */
public abstract class
DataVisitor
{
    /**
     * Visit a VisAD Tuple.
     *
     * @param tuple	The VisAD Tuple being visited.
     * @precondition	<code>tuple</code> is non-null.
     * @postcondition	<code>tuple</code> has been visited.
     * @exception BadFormException	The Tuple doesn't fit the data model
     *					used by the visitor.
     * @exception VisADException	Core VisAD problem (probably couldn't
     *					create a VisAD object).
     * @see visad.data.DataNode
     */
    public boolean
    visit(Tuple tuple)
	throws BadFormException, VisADException
    {
	return true;
    }


    /**
     * Visit a VisAD FlatField.
     *
     * @param field	The VisAD FlatField being visited.
     * @precondition	<code>field</code> is non-null.
     * @postcondition	<code>field</code> has been visited.
     * @exception BadFormException	The Tuple doesn't fit the data model
     *					used by the visitor.
     * @exception VisADException	Core VisAD problem (probably couldn't
     *					create a VisAD object).
     * @see visad.data.DataNode
     */
    public boolean
    visit(FlatField field)
	throws BadFormException, VisADException
    {
	return true;
    }
}
