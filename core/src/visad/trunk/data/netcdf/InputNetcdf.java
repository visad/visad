/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InputNetcdf.java,v 1.1 1998-06-26 14:25:57 visad Exp $
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
 * A Java bean for importing a netCDF file.
 */
public class
InputNetcdf
    implements	Serializable
{
    /**
     * The pathname property of the netCDF dataset.
     */
    private String			pathname;

    /**
     * The VisAD data object property.
     */
    private DataImpl			data;

    /**
     * Support for property changes.
     */
    private final PropertyChangeSupport	changes;


    /**
     * Construct.
     */
    public
    InputNetcdf()
    {
	pathname = "dummy.nc";
	data = null;
	changes = new PropertyChangeSupport(this);
    }


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
