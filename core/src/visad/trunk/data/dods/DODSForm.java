/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
*/

package visad.data.dods;

import java.net.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.data.in.*;

/**
 * Provides support for accessing the DODS form of data from VisAD.
 *
 * <P>Instances are mutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class DODSForm
    extends	Form
    implements	FormFileInformer
{
    /**
     * The suffix in the path-component of a URL specification that identifies
     * a dataset specification as being a DODS dataset specification.
     */
    public final static String		SUFFIX = ".dods";

    private static DODSForm		instance;
    private final DODSSource		source;
    private final Consolidator		consolidator;

    static
    {
	try
	{
	    instance = new DODSForm();
	}
	catch (VisADException e)
	{
	    throw new VisADError(
		"visad.data.dods.DODSForm.<clinit>: " +
		"Can't initialize class: " + e);
	}
    }

    /**
     * Constructs from nothing.
     *
     * @throws VisADException	VisAD failure.
     */
    protected DODSForm()
	throws VisADException
    {
	super("DODS");
	consolidator = new Consolidator();
	// source = new DODSSource(new TimeFactorer(consolidator));
	source = new DODSSource(consolidator);
    }

    /**
     * Returns an instance of this class.
     *
     * @return			An instance of this class.
     */
    public static DODSForm dodsForm()
    {
	return instance;
    }

    /**
     * Throws an exception.
     *
     * @param id		An identifier.
     * @param data		A VisAD data object.
     * @param replace		Whether or not to replace an existing object.
     * @throws UnimplementedException	Always.
     */
    public void
    save(String id, Data data, boolean replace)
	throws UnimplementedException
    {
	throw new UnimplementedException(
	    getClass().getName() + ".save(String,Data,boolean): " +
	    "Can't save data to a DODS server");
    }

    /**
     * Throws an exception.
     *
     * @param id		An identifier.
     * @param data		A VisAD data object.
     * @param replace		Whether or not to replace an existing object.
     * @throws BadFormException	Always.
     */
    public void add(String id, Data data, boolean replace)
	throws BadFormException
    {
	throw new BadFormException(
	    getClass().getName() + ".add(String,Data,boolean): " +
	    "Can't add data to a DODS server");
    }

    /**
     * Opens an existing data object.
     *
     * @param id		The URL for a DODS dataset.  The path component
     *				should have a {@link #SUFFIX} suffix.
     * @return			The VisAD data object corresponding to the 
     *				specified DODS dataset.
     * @throws BadFormException	The DODS dataset is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl open(String id)
	throws BadFormException, RemoteException, VisADException
    {
	source.open(id);
	DataImpl	data = consolidator.getData();
	source.close();		// clears consolidator
	return data;
    }

    /**
     * Opens an existing data object.
     *
     * @param url		The URL for a DODS dataset.  The path component
     *				should have a {@link #SUFFIX} suffix.
     * @return			The VisAD data object corresponding to the 
     *				DODS dataset.
     * @throws BadFormException	The DODS dataset is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl open(URL url)
	throws BadFormException, VisADException, RemoteException
    {
	return open(url.toString());
    }

    /**
     * Returns <code>null</code>.
     *
     * @param data		A VisAD data object.
     * @return			<code>null</code>.
     */
    public FormNode getForms(Data data)
    {
	return null;	// can't save data to a DODS server
    }

    /*
     * FormFileInformer method implementations:
     */

    /**
     * Indicates if a pathname dataset specification refers to a DODS dataset.
     *
     * @param spec		A pathname dataset specification.
     * @return			<code>true</code> if and only if the pathname 
     *				dataset specification refers to a DODS dataset.
     */
    public boolean isThisType(String pathname)
    {
	return pathname.endsWith(SUFFIX);
    }

    /**
     * Does nothing.  Because the initial block of data in a DODS dataset can't
     * be obtained from a DODS server, this routine does nothing and always
     * returns false.
     *
     * @param block		A block of data.
     * @return			<code>false</code> always.
     */
    public boolean isThisType(byte[] block)
    {
	return false;
    }

    /**
     * Returns the path-component suffixes that identifies a dataset 
     * specification as being a DODS dataset specification.
     *
     * @return			The array of relevant suffixes.
     */
    public String[] getDefaultSuffixes()
    {
	return new String[] {SUFFIX};
    }
}
