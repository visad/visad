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

$Id: Repository.java,v 1.14 2009-03-02 23:35:46 curtis Exp $
*/

package visad.data;


import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Enumeration;
import visad.Data;
import visad.DataImpl;
import visad.VisADException;


/**
 * A repository of persistent data objects.
 * This class implements the "abstract factory" design pattern.
 * The concrete implementation of this class could either be
 * "DirectoryRepository" (for accessing files residing on local disk)
 * or "RemoteRepository" (for accessing remote files via a server) or
 * something else.  The concrete class of the "Form" objects will be
 * determined by this class's concrete class (and, hence, so will the
 * concrete class of any constructed "FileAccessor").
 */
public abstract class Repository
{
    /**
     * The name of this repository.
     */
    private final String	name;

    /**
     * The location of this repository.
     */
    private final String	location;

    /**
     * The data forms supported by this repository.
     */
    protected FormNode		forms;


    /**
     * Construct a data repository.
     */
    public Repository(String name, String location)
    {
	this.name = name;
	this.location = location;
    }

    /**
     * Return the name of this repository.
     */
    public String getName()
    {
	return name;
    }

    /**
     * Return the location of this repository.
     */
    public String getLocation()
    {
	return location;
    }

    /**
     * Return the forms of data that are supported by this repository.
     */
    public FormNode getForms()
    {
	return forms;
    }

    /**
     * Return the forms of data that are both supported by this repository
     * and compatible with a data object.
     */
    public FormNode getForms(Data data)
	throws VisADException, IOException, RemoteException
    {
	return forms.getForms(data);
    }

    /**
     * Return an enumeration of the data objects in this repository.
     */
    public abstract Enumeration getEnumeration()
	throws	BadRepositoryException, SecurityException;

    /**
     * Save a data object in the first compatible data form.
     */
    public void save(String id, Data data, boolean replace)
	throws VisADException, IOException, RemoteException
    {
	forms.save(fullName(id), data, replace);
    }

    /**
     * Save a data object in a particular form.
     */
    public void save(String id, Data data, FormNode form, boolean replace)
	throws VisADException, RemoteException, IOException
    {
	form.save(fullName(id), data, replace);
    }

    /**
     * Add a data object to an existing data object in the repository.
     */
    public void add(String id, Data data, boolean replace)
	throws VisADException
    {
	forms.add(fullName(id), data, replace);
    }

    /**
     * Open an existing data object in the repository.
     */
    public DataImpl open(String id)
	throws VisADException, IOException
    {
	return forms.open(fullName(id));
    }

    /**
     * Open a data object specified as a URL.  Strictly speaking, this
     * shouldn't be here because a URL can lie outside the domain of the
     * repository.  A repository, however, is characterized by the
     * data forms that it handles as well as its "location".
     * Consequently, we have this method.
     */
    public DataImpl open(URL url)
	throws VisADException, IOException
    {
	return forms.open(url);
    }


    /**
     * Return the fully-qualified name of a persistent data object.
     */
    protected abstract String fullName(String id);
}
