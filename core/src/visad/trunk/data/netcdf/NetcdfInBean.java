/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfInBean.java,v 1.2 2001-11-27 22:29:32 dglo Exp $
 */

package visad.data.netcdf;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import visad.DataImpl;
import visad.VisADException;
import visad.data.BadFormException;


/**
 * Adapt a netCDF input file to a Java bean.
 */
public class
NetcdfInBean
    implements	Serializable
{
    /**
     * The pathname property of the netCDF dataset.
     */
    private String			pathname = null;

    /**
     * The VisAD data object.
     */
    private DataImpl			data = null;

    /**
     * Support for property changes.
     */
    private PropertyChangeSupport	changes =
	new PropertyChangeSupport(this);


    /**
     * Set the dataset pathname property.
     */
    public synchronized void
    setPathname(String pathname)
	throws IOException, VisADException, BadFormException
    {
	String		oldPathname = this.pathname;
	DataImpl	oldData = data;
	Plain		plain = new Plain();

	data = plain.open(pathname);
	this.pathname = pathname;

	changes.firePropertyChange("pathname", oldPathname, this.pathname);
	changes.firePropertyChange("data", oldData, this.data);
    }


    /**
     * Get the dataset pathname property.
     */
    public synchronized String
    getPathname()
    {
	return pathname;
    }


    /**
     * Get the VisAD data object property.
     */
    public synchronized DataImpl
    getData()
    {
	return data;
    }


    /**
     * Add a property change listener.
     */
    public synchronized void
    addPropertyChangeListener(PropertyChangeListener p)
    {
	changes.addPropertyChangeListener(p);
    }


    /**
     * Remove a property change listener.
     */
    public synchronized void
    removePropertyChangeListener(PropertyChangeListener p)
    {
	changes.removePropertyChangeListener(p);
    }
}
