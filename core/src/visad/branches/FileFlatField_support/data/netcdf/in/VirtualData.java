/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualData.java,v 1.2 2000-04-26 15:45:20 dglo Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.DataImpl;
import visad.MathType;
import visad.VisADException;


/**
 * Provides support for a virtual VisAD data object.
 */
public abstract class
VirtualData
{
    /**
     * Gets the VisAD MathType of this virtual, data object.
     *
     * @return			The VisAD MathType of this virtual, data object.
     */
    public abstract MathType
    getType()
	throws VisADException;


    /**
     * Gets the VisAD data object corresponding to this top-level, virtual,
     * data object.
     *
     * @return			The VisAD data object corresponding to this
     *				top-level, virtual, data object.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * throws RemoteException	Remote access failure.
     * throws IOException	I/O failure.
     */
    public DataImpl
    getData()
	throws VisADException, RemoteException, IOException
    {
	return getData(new Context());
    }


    /**
     * Gets the string that represents this object.
     *
     * @return			The string that represents this object.
     */
    public String
    toString()
    {
	String	string;

	try
	{
	    string = getType().toString();
	}
	catch (VisADException e)
	{
	    string = "VisADException: " + e.getMessage();
	}

	return string;
    }


    /**
     * Gets the VisAD data object corresponding to this virtual, data
     * object, in context.
     *
     * @param context		The context in which the data is to be
     *				gotten.
     * @return			The VisAD data object corresponding to this
     *				virtual, data object.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * throws RemoteException	Remote access failure.
     * throws IOException	I/O failure.
     */
    public abstract DataImpl
    getData(Context context)
	throws VisADException, RemoteException, IOException;
}
