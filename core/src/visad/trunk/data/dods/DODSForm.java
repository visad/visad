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

import java.lang.reflect.*;
import java.net.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.data.in.*;
import visad.util.ReflectedUniverse;

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
     * a dataset specification as being a DODS dataset specification.  It 
     * doesn't have a leading period.
     */
    public final static String		SUFFIX = "dods";

    private final static String		periodSuffix = "." + SUFFIX;
    private final static DODSForm	instance = new DODSForm();
    private final Consolidator		consolidator;
    private final static String		sourceMessage =
	"DODS data-import capability is not available -- " +
	"probably because the DODS package wasn't available when " +
	"this package was compiled.  If you want DODS data-import " +
	"capability, then you'll have to first obtain the DODS " +
	"package (see " +
	"<http://www.unidata.ucar.edu/packages/dods/index.html>) and " +
	"then recompile this package.";
    private final static String		contactMessage =
	".  This exception should not have occurred.  Contact VisAD support.";

    /**
     * Constructs from nothing.
     */
    protected DODSForm()
    {
	super("DODS");
	consolidator = new Consolidator();
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
     * Opens an existing DODS dataset.
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
	String		header = getClass().getName() + ".open(String): ";
	DataImpl	data;
	try
	{
	    Class	sourceClass = 
		Class.forName(
		    getClass().getPackage().getName() + ".DODSSource");
	    // source = new DODSSource(new TimeFactorer(consolidator));
	    Object	source =
		sourceClass.getConstructor(
		    new Class[] {DataSink.class})
		    .newInstance(new Object[] {consolidator});
	    sourceClass.getMethod("open", new Class[] {String.class})
		.invoke(source, new Object[] {id});
	    data = consolidator.getData();
	    /* Clears consolidator. */
	    sourceClass.getMethod("close", null).invoke(source, null);
	}
	catch (ClassNotFoundException e)
	{
	    throw new VisADException(header + e + ".  " + sourceMessage);
	}
	catch (NoSuchMethodException e)
	{
	    throw new VisADException(header + e + contactMessage);
	}
	catch (SecurityException e)
	{
	    throw new VisADException(header + e + contactMessage);
	}
	catch (InstantiationException e)
	{
	    throw new VisADException(header + e + contactMessage);
	}
	catch (IllegalAccessException e)
	{
	    throw new VisADException(header + e + contactMessage);
	}
	catch (IllegalArgumentException e)
	{
	    throw new VisADException(header + e + contactMessage);
	}
	catch (InvocationTargetException e)
	{
	    throw new VisADException(header + e + contactMessage);
	}
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
     * Indicates if a dataset specification is consistent with a DODS dataset
     * specification.
     *
     * @param spec		A dataset specification.
     * @return			<code>true</code> if and only if the dataset
     *				specification is consistent with a DODS dataset
     *				specification.
     */
    public boolean isThisType(String spec)
    {
	boolean	isThisType;
	try
	{
	    isThisType =
		new URL(spec).getPath().toLowerCase().endsWith(periodSuffix);
	}
	catch (MalformedURLException e)
	{
	    isThisType = false;
	}
	return isThisType;
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
     * specification as being a DODS dataset specification.  The suffixes don't
     * have a leading period.  The returned array can be safely modified.
     *
     * @return			A freshly-allocated array with the relevant 
     *				suffixes.
     */
    public String[] getDefaultSuffixes()
    {
	return new String[] {SUFFIX};
    }
}
