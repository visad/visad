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

import dods.dap.*;
import java.rmi.RemoteException;
import java.util.Enumeration;
import visad.data.BadFormException;
import visad.data.in.*;
import visad.*;

/**
 * Provides support for generating a stream of VisAD data objects from a DODS
 * dataset.
 *
 * <P>Instances are mutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class DODSSource
    extends DataSource
{
    private DataFactory	dataFactory;

    /**
     * Constructs from a downstream data sink.  The default factory for creating
     * VisAD data objects will be used.
     *
     * @param downstream	The downstream data sink.
     */
    public DODSSource(DataSink downstream)
    {
	this(downstream, DataFactory.dataFactory());
    }

    /**
     * Constructs from a downstream data sink and a factory for creating VisAD
     * data objects.
     *
     * @param downstream	The downstream data sink.
     * @param factory		A factory for creating VisAD data objects.
     */
    public DODSSource(DataSink downstream, DataFactory factory)
    {
	super(downstream);
	dataFactory = factory;
    }

    /**
     * Opens an existing DODS dataset.
     *
     * @param spec		The string specification of the DODS dataset (
     *				i.e. a URL specification).
     * @return			<code>true</code> if and only if the specified
     *				DODS dataset was successfully converted into
     *				a VisAD data object.
     */
    public boolean open(String spec)
    {
	boolean	success;
	System.gc();
	try
	{
	    DConnect	dConnect = new DConnect(spec);
	    DAS		das = dConnect.getDAS();
	    handleGlobalAttributes(das);
	    handleVariables(dConnect.getData(null), das);
	    success = true;
	}
	catch (Exception e)
	{
	    System.err.println(
		getClass().getName() + ".open(String): " +
		"Unable to open dataset \"" + spec + "\": " + e);
	    success = false;
	}
	return success;
    }

    /**
     * Closes the currently open DODS dataset.
     *
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public synchronized void close()
	throws VisADException, RemoteException
    {
	flush();
	System.gc();
    }

    /**
     * Generates a stream of VisAD data objects corresponding to the DODS
     * global attributes in the currently open dataset.
     *
     * @param das		The DODS DAS of the open dataset.
     * @throws BadFormException	The DODS datset is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    protected void handleGlobalAttributes(DAS das)
	throws BadFormException, VisADException, RemoteException
    {
	AttributeTable	globalTable = das.getAttributeTable("NC_GLOBAL");
	if (globalTable == null)
	    globalTable = das.getAttributeTable("nc_global");
	if (globalTable != null)
	{
	    for (Enumeration enum = globalTable.getNames();
		enum.hasMoreElements(); )
	    {
		String		name = (String)enum.nextElement();
		DataImpl	data =
		    dataFactory.data(name, globalTable.getAttribute(name));
		if (data != null)
		    send(data);
	    }
	}
    }

    /**
     * Generates a stream of VisAD data objects corresponding to the DODS
     * variables in the currently open dataset.
     *
     * @param dataDDS		The DODS dataset.
     * @param das		The associated DAS of the dataset.
     * @throws BadFormException	The DODS datset is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    protected void handleVariables(DataDDS dataDDS, DAS das)
	throws BadFormException, VisADException, RemoteException
    {
	for (Enumeration enum = dataDDS.getVariables();
	    enum.hasMoreElements(); )
	{
	    BaseType	baseType = (BaseType)enum.nextElement();
	    send(
		dataFactory.data(
		    baseType, das.getAttributeTable(baseType.getName())));
	}
    }
}
