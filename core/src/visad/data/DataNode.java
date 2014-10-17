/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA

$Id: DataNode.java,v 1.14 2009-03-02 23:35:46 curtis Exp $
*/

package visad.data;


import java.rmi.RemoteException;
import visad.Data;
import visad.FlatField;
import visad.TupleIface;
import visad.UnimplementedException;
import visad.VisADException;


/**
 * Abstract class for adapting a VisAD data object to the "Visitor"
 * design pattern.  This class knows how to traverse an arbitrary VisAD
 * data object.
 */
public abstract class
DataNode
{
    /**
     * Construct.  Protected to ensure use of the create() factory method.
     */
    protected
    DataNode()
    {
    }


    /**
     * Factory method for creating an instance of the appropriate type.
     *
     * @param data	The VisAD data object to be traversed.
     * @return	A DataNode that knows how to traverse the VisAD data object.
     * @precondition	<code>data</code> is non-null.
     * @exception UnimplementedException	A (soon to be implemented)
     *		method isn't implemented yet.
     */
    public static DataNode
    create(Data data)
	throws UnimplementedException
    {
	DataNode	node;

	/*
	 * Watch the ordering in the following: the first match will be
	 * taken.
	 */
	if (data instanceof TupleIface)
	    node = new TupleNode((TupleIface)data);
	else
	if (data instanceof FlatField)
	    node = new FlatFieldNode((FlatField)data);
	else
	    throw new UnimplementedException(
		"VisAD data type not yet supported: " +
		data.getClass().getName());

	return node;
    }


    /**
     * Accept a visitor and traverse the data object.
     *
     * @param visitor	The object that will have it's <code>visit()</code>
     *			method called for each subcomponent of the VisAD data
     *			object.
     * @return		<code>visitor</code> for convenience.
     * @precondition	<code>visitor</code> is non-null.
     * @postcondition	<code>visitor</code> has visited the VisAD data object
     *			<code>data</code>.
     * @exception UnimplementedException	A (soon to be implemented)
     *		method isn't implemented yet.
     * @exception BadFormException	The VisAD data object doesn't "fit"
     *		the data model used by <code>visitor</code>.
     * @exception VisADException	Problem in core VisAD (probably
     *		couldn't create some VisAD object).
     * @exception RemoteException	Problem accessing the VisAD data
     *		object.
     * @see visad.data.DataVisitor
     */
    public abstract DataVisitor
    accept(DataVisitor visitor)
	throws UnimplementedException, BadFormException, VisADException,
	    RemoteException;
}


/**
 * Concrete class for traversing a VisAD Tuple.
 */
class
TupleNode
    extends	DataNode
{
    /**
     * The VisAD Tuple
     */
    protected final TupleIface	tuple;


    /**
     * Construct from a VisAD Tuple.
     *
     * @param tuple	The VisAD Tuple to be traversed.
     * @precondition	<code>tuple</code> is non-null.
     */
    protected
    TupleNode(TupleIface tuple)
    {
	this.tuple = tuple;
    }


    /**
     * Accept a visitor and traverse the Tuple.
     *
     * @param visitor	The object that will have it's <code>visit()</code>
     *			method called for each component of the VisAD
     *			Tuple.
     * @precondition	<code>visitor</code> is non-null.
     * @postcondition	<code>visitor</code> has visited <code>tuple</code>.
     * @exception UnimplementedException	A (soon to be implemented)
     *		method isn't implemented yet.
     * @exception BadFormException	The VisAD data object doesn't "fit"
     *		the data model used by <code>visitor</code>.
     * @exception VisADException	Problem in core VisAD (probably
     *		couldn't create some VisAD object).
     * @see visad.data.DataVisitor
     */
    public DataVisitor
    accept(DataVisitor visitor)
	throws UnimplementedException, BadFormException, VisADException,
	RemoteException
    {
	if (visitor.visit(tuple))
	{
	    int	ncomp = tuple.getDimension();

	    for (int icomp = 0; icomp < ncomp; ++icomp)
		DataNode.create(tuple.getComponent(icomp)).accept(visitor);
	}

	return visitor;
    }
}


/**
 * Concrete class for traversing a VisAD FlatField.
 */
class
FlatFieldNode
    extends	DataNode
{
    /**
     * The VisAD FlatField
     */
    protected final FlatField	field;


    /**
     * Construct from a VisAD FlatField.
     *
     * @param field	The VisAD FlatField to be traversed.
     * @precondition    <code>field</code> is non-null.
     */
    protected
    FlatFieldNode(FlatField field)
    {
	this.field = field;
    }


    /**
     * Accept a visitor and traverse the FlatField.
     *
     * @param visitor   The object that will have it's <code>visit()</code>
     *			method called for each component of the VisAD
     *			FlatField.
     * @precondition    <code>visitor</code> is non-null.
     * @postcondition	<code>visitor</code> has visited <code>field</code>.
     * @exception UnimplementedException        A (soon to be implemented)
     *		method isn't implemented yet.
     * @exception BadFormException      The VisAD data object doesn't "fit"
     *		the data model used by <code>visitor</code>.
     * @exception VisADException        Problem in core VisAD (probably
     *		couldn't create some VisAD object).
     * @see visad.data.DataVisitor
     */
    public DataVisitor
    accept(DataVisitor visitor)
	throws UnimplementedException, BadFormException, VisADException,
	    RemoteException
    {
	visitor.visit(field);

	return visitor;
    }
}

// TODO: Add more types of DataNodes.
