/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Consolidator.java,v 1.1.4.1 2000-06-07 20:38:00 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.DataImpl;
import visad.VisADException;


/**
 * Supports consolidation of VisAD data items.
 *
 * Instances are mutable.
 */
public abstract class
Consolidator
{
    /**
     * Adds a data item.
     *
     * @param data		The virtual, VisAD data item to be added.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public abstract void
    add(VirtualData data)
	throws VisADException;


    /**
     * Gets the consolidated, VisAD data object.
     *
     * @return			The consolidated, VisAD data object.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote access failure.
     * @throws IOException	I/O failure.
     */
    public abstract DataImpl
    getData()
	throws VisADException, RemoteException, IOException;


    /**
     * Clears the consolidated, VisAD data object.
     */
    public abstract void
    clear();


    /**
     * Gets an proxy for the consolidated, VisAD data object.
     *
     * @return			A proxy for the consolidated, VisAD data object.
     */
    public abstract DataImpl
    getProxy();
}
