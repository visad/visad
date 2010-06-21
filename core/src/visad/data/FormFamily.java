/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

$Id: FormFamily.java,v 1.16 2009-03-02 23:35:46 curtis Exp $
*/

package visad.data;

import java.io.IOException;
import java.rmi.RemoteException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import visad.Data;
import visad.DataImpl;
import visad.VisADException;


/**
 * A interior node in the data form hierarchy for the storage of
 * persistent data objects.
 */
public class
FormFamily
    extends FormNode
{
    /**
     * Construct an interior data-form node with the given name.
     */
    public FormFamily(String name)
    {
	super(name);
    }


    /**
     * Save a VisAD data object.
     */
    public void save(String id, Data data, boolean replace)
	throws BadFormException, RemoteException, IOException, VisADException
    {
	for (Enumeration e = forms.elements(); e.hasMoreElements(); )
	{
	    try
	    {
		((FormNode)e.nextElement()).save(id, data, replace);
	    }
	    catch (BadFormException xcpt)
	    {
		continue;
	    }
	    return;
	}
	throw new BadFormException("Data object not compatible with \"" +
					getName() + "\" data family");
    }


    /**
     * Add data to an existing data object.
     */
    public void add(String id, Data data, boolean replace)
	throws BadFormException
    {
	for (Enumeration e = forms.elements(); e.hasMoreElements(); )
	{
	    try
	    {
		((FormNode)e.nextElement()).add(id, data, replace);
	    }
	    catch (BadFormException xcpt)
	    {
		continue;
	    }
	    return;
	}

	throw new BadFormException("Data object not compatible with \"" +
					getName() + "\" data family");
    }


    /**
     * Open an existing data object.
     */
    public DataImpl open(String id)
	throws BadFormException, IOException, VisADException
    {
	for (Enumeration e = forms.elements(); e.hasMoreElements(); )
	{
	    try
	    {
		return ((FormNode)e.nextElement()).open(id);
	    }
	    catch (BadFormException xcpt)
	    {
	    }
	}

	throw new BadFormException("Data object \"" + id +
		"\" not compatible with \"" + getName() + "\" data family");
    }


    /**
     * Open an existing data object specified as a URL.
     */
    public DataImpl open(URL url)
	throws BadFormException, IOException, VisADException
    {
	for (Enumeration e = forms.elements(); e.hasMoreElements(); )
	{
	    try
	    {
		return ((FormNode)e.nextElement()).open(url);
	    }
	    catch (BadFormException xcpt)
	    {
	    }
	}

	throw new BadFormException("Data object \"" + url +
		"\" not compatible with \"" + getName() + "\" data family");
    }


    /**
     * Return the data forms that are compatible with a data object.
     */
    public FormNode getForms(Data data)
	throws RemoteException, VisADException, IOException
    {
	FormFamily	family = new FormFamily(getName());

	for (Enumeration e = forms.elements(); e.hasMoreElements(); )
	{
	    FormNode	node = ((FormNode)e.nextElement()).getForms(data);

	    if (node != null)
		family.addFormNode(node);
	}

	return family.forms.size() == 0
		    ? null
		    : family;
    }


    /**
     * Add a child node to this family of data forms.
     */
    public FormFamily addFormNode(FormNode node)
    {
	forms.addElement(node);
	return this;
    }


    /**
     * The children of this interior node.
     */
    protected Vector	forms = new Vector();
}
