/*
 * VisAD system for interactive analysis and visualization of numerical
 * data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
 * Rink, Dave Glowacki, and Steve Emmerson.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License in file NOTICE for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * $Id: DataVisitor.java,v 1.3 1998-02-23 14:33:09 steve Exp $
 */

package visad.data;


import java.rmi.RemoteException;
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
	throws BadFormException, VisADException, RemoteException
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
	throws BadFormException, VisADException, RemoteException
    {
	return true;
    }
}
