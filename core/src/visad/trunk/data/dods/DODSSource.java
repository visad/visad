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
*/

package visad.data.dods;

import dods.dap.*;
import dods.dap.parser.ParseException;
import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.util.Enumeration;
import visad.*;
import visad.data.BadFormException;
import visad.data.in.*;

/**
 * Provides support for generating a stream of VisAD data objects from a DODS
 * dataset.
 *
 * <P>Instances are mutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class DODSSource
    implements  DataInputSource
{
    private DataFactory         dataFactory;
    private DConnect            dConnect;
    private DAS                 das;
    private AttributeTable      globalTable;
    private Enumeration         attrEnum;
    private Enumeration         varEnum;

    /**
     * Constructs from nothing.  The default factory for creating VisAD data
     * objects will be used.
     */
    public DODSSource()
    {
        this(DataFactory.dataFactory());
    }

    /**
     * Constructs from a factory for creating VisAD data objects.
     *
     * @param factory           A factory for creating VisAD data objects.
     */
    public DODSSource(DataFactory factory)
    {
        dataFactory = factory;
    }

    /**
     * Opens an existing DODS dataset.
     *
     * @param spec              The URL string specification of the DODS dataset
     *                          The path component should have a ".dods" suffix.
     * @return                  The VisAD data object corresponding the DODS
     *                          dataset specification.
     * @throws BadFormException The DODS dataset is corrupt.
     * @throws VisADException   VisAD failure.
     * @throws RemoteException  Java RMI failure.
     */
    public synchronized void open(String spec)
        throws BadFormException, RemoteException, VisADException
    {
        try
        {
            URL         url = new URL(spec);
            String      path = url.getFile();
            String      query = null;
            int         i = path.lastIndexOf('?');
            if (i != -1)
            {
                query = path.substring(i);
                path = path.substring(0, i);
            }
            /*
             * Because the DConnect class won't construct an instance
             * from a DODS dataset specification whose path component has a
             * ".dods" suffix, such a suffix is removed.
             */
            String      suffix = ".dods";
            if (path.toLowerCase().endsWith(suffix))
            {
                path    = path.substring(0, path.length()-suffix.length());
                spec =
                    new URL(
                        url.getProtocol(),
                        url.getHost(),
                        url.getPort(),
                        // Change 2004-01-22 query already contains ?
                        // query == null ? path : path + "?" + query)
                        query == null ? path : path + query)
                    .toString();
            }
            dConnect = new DConnect(spec);
            das = dConnect.getDAS();
            globalTable = das.getAttributeTable("NC_GLOBAL");
            if (globalTable == null)
                globalTable = das.getAttributeTable("nc_global");
            if (globalTable != null)
            {
                attrEnum = globalTable.getNames();
            }
            else
            {
                attrEnum = null;
                varEnum = dConnect.getData(null).getVariables();
            }
        }
        catch (MalformedURLException e)
        {
            throw new BadFormException(e.getMessage());
        }
        catch (FileNotFoundException e)
        {
            throw new BadFormException(e.getMessage());
        }
        catch (ParseException e)
        {
            throw new BadFormException(e.getMessage());
        }
        catch (DODSException e)
        {
            throw new BadFormException(e.getMessage());
        }
        catch (IOException e)
        {
            throw new BadFormException(e.getMessage());
        }
    }

    /**
     * Returns the next VisAD data object from the DODS dataset.  Returns
     * <code>null</code> if there is no more objects.
     *
     * @return                  A VisAD data object or <code>null</code> if 
     *                          there are no more such objects.
     * @throws VisADException   VisAD failure.
     * @throws RemoteException  Java RMI failure.
     */
    public synchronized DataImpl readData() throws VisADException, RemoteException
    {
        DataImpl        data;
        if (attrEnum != null)
        {
            if (attrEnum.hasMoreElements())
            {
                String  name = (String)attrEnum.nextElement();
                data =
                    dataFactory.data(
                        name, globalTable.getAttribute(name), true);
            }
            else
            {
                attrEnum = null;
                try
                {
                    varEnum = dConnect.getData(null).getVariables();
                }
                catch (DODSException e)
                {
                    throw new RemoteException(
                        getClass().getName() + ".readData(): " +
                        "Couldn't get DDS of DODS dataset: " + e);
                }
                catch (ParseException e)
                {
                    throw new RemoteException(
                        getClass().getName() + ".readData(): " +
                        "Couldn't get DDS of DODS dataset: " + e);
                }
                catch (IOException e)
                {
                    throw new RemoteException(
                        getClass().getName() + ".readData(): " +
                        "Couldn't get DDS of DODS dataset: " + e);
                }
                data = readData();
            }
        }
        else if (varEnum != null)
        {
            if (varEnum.hasMoreElements())
            {
                data =
                    dataFactory.data(
                        (BaseType)varEnum.nextElement(), das, true);
            }
            else
            {
                data = null;
                varEnum = null;
                dConnect = null;
                das = null;
                globalTable = null;
            }
        }
        else
        {
            data = null;
        }
        return data;
    }

    /**
     * Returns a VisAD data object corresponding to the next DODS global
     * attribute in the currently open dataset.  Returns <code>null</code> if
     * there isn't another attribute.
     *
     * @param name              The name of the attribute.
     * @return                  A VisAD data object corresponding to the next
     *                          DODS global attribute or <code>null</code> if
     *                          no more attributes.
     * @throws BadFormException The DODS datset is corrupt.
     * @throws VisADException   VisAD failure.
     * @throws RemoteException  Java RMI failure.
     */
    protected synchronized DataImpl readAttribute(String name)
        throws BadFormException, VisADException, RemoteException
    {
        return dataFactory.data(name, globalTable.getAttribute(name), true);
    }

    /**
     * Returns a VisAD data object corresponding to the next DODS variable in
     * the currently open dataset.  Returns <code>null</code> if there isn't
     * another variable.
     *
     * @return                  A VisAD data object corresponding to the next
     *                          DODS variable or <code>null</code> if no more
     *                          variables.
     * @throws BadFormException The DODS datset is corrupt.
     * @throws VisADException   VisAD failure.
     * @throws RemoteException  Java RMI failure.
     */
    protected synchronized DataImpl readVariable()
        throws BadFormException, VisADException, RemoteException
    {
        DataImpl        data;
        if (varEnum == null)
        {
            data = null;
        }
        else if (!varEnum.hasMoreElements())
        {
            varEnum = null;
            data = null;
        }
        else
        {
            data = dataFactory.data((BaseType)varEnum.nextElement(), das, true);
        }
        return data;
    }
}
