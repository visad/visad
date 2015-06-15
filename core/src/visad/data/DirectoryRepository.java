/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

$Id: DirectoryRepository.java,v 1.14 2009-03-02 23:35:46 curtis Exp $
*/

package visad.data;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * A data object repository implemented as files in a local directory.
 */
public class DirectoryRepository extends Repository
{
    /**
     * The directory.
     * Effectively "final" if the constructor succeeds.
     */
    protected File	dir;

    /**
     * Unambiguous representation of directory for error messages.
     * Effectively "final" if the constructor succeeds.
     */
    protected String	dirString;


    /**
     * Construct a directory repository with support for the default
     * forms of data.
     */
    public
    DirectoryRepository(String name, String location)
	throws BadRepositoryException, IOException
    {
	super(name, location);

	// Check the directory.
	try
	{
	    dir = new File(getLocation());
	}
	catch (NullPointerException e)
	{
	    throw new BadRepositoryException("Null repository name");
	}
	dirString = "\"" + getName() + "\" (path \"" + getLocation() + "\")";

	if (!dir.isDirectory())
	    throw new BadRepositoryException("Repository " + dirString +
		" is not a directory");
	if (!dir.canRead())
	    throw new BadRepositoryException("Repository " + dirString +
		" is not readable");
    }


    // TODO: method(s) for constructing the data form hierarchy


    /**
     * Return an enumeration of the data objects in this repository.
     */
    public Enumeration
    getEnumeration()
	throws	BadRepositoryException, SecurityException
    {
	return new Enumerator();
    }


    /**
     * Inner class for enumerating the files in the directory.
     */
    public class
    Enumerator
	implements	Enumeration
    {
	protected int			i;
	protected final String[]	list;

	protected
	Enumerator()
	    throws SecurityException
	{
	    list = dir.list();
	    i = 0;
	}

	public boolean
	hasMoreElements()
	{
	    return i < list.length;
	}

	public Object
	nextElement()
	    throws NoSuchElementException
	{
	    if (i == list.length)
		throw new NoSuchElementException();

	    return list[i++];
	}
    }


    /**
     * Return the fully-qualified name of a persistent data object.
     */
    protected String fullName(String id)
    {
	return getLocation() + File.separator + id;
    }


    /**
     * Test this class.
     */
    public static void main(String[] args)
	throws BadRepositoryException, IOException
    {
	DirectoryRepository	dir = new DirectoryRepository("Test", ".");

	for (Enumeration en = dir.getEnumeration(); en.hasMoreElements();)
	    System.out.println((String)en.nextElement());

	System.out.println("dir.fullName(\"foo.bar\") = " +
	    dir.fullName("foo.bar"));
    }
}
