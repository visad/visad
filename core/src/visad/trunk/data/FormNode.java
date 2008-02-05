/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

$Id: FormNode.java,v 1.13 2008-02-05 20:26:06 curtis Exp $
*/

package visad.data;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import visad.Data;
import visad.DataImpl;
import visad.VisADException;


/**
 * A node in the data form hierarchy for the storage of persistent data.
 *
 * This class implements the "composite" design pattern; the node will
 * actually be either a "Form" or a "FormFamily".
 */
public abstract class
FormNode
{
    /**
     * Construct a data-form node with the given name.
     */
    public FormNode(String name)
    {
	this.name = name;
    }


    /**
     * Return the name of this node.
     */
    public String getName()
    {
	return name;
    }


    /**
     * Save a VisAD data object in this form.
     */
    public abstract void
    save(String id, Data data, boolean replace)
	throws BadFormException, IOException, RemoteException, VisADException;


    /**
     * Add data to an existing data object.
     */
    public abstract void add(String id, Data data, boolean replace)
	throws BadFormException;


    /**
     * Open an existing data object.
     */
    public abstract DataImpl open(String id)
	throws BadFormException, IOException, VisADException;


    /**
     * Open a data object specified as a URL.
     */
    public abstract DataImpl open(URL url)
	throws BadFormException, VisADException, IOException;


    /**
     * Return the data forms that are compatible with a data object.
     */
    public abstract FormNode getForms(Data data)
	throws VisADException, RemoteException, IOException;


    /**
     * The name of this node.
     */
    private final String	name;
}
