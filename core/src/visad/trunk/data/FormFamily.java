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
 * $Id: FormFamily.java,v 1.5 1998-02-23 14:33:10 steve Exp $
 */

package visad.data;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;
import visad.Data;
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
    private Vector	forms = new Vector();
}
