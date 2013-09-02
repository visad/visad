/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InputNetcdf.java,v 1.3 2002-09-20 18:16:33 steve Exp $
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
     * The quantity database property.
     */
    private QuantityDB                  quantityDB;

    /**
     * Support for property changes.
     */
    private final PropertyChangeSupport	changes;


    /**
     * Construct.  The pathname and data object properties will be 
     * <code>null</code>; the quantity database property will be {@link
     * StandardQuantityDB}.
     */
    public
    InputNetcdf()
    {
	pathname = null;
	data = null;
	changes = new PropertyChangeSupport(this);
	quantityDB = StandardQuantityDB.instance();
    }


    /**
     * Set the quantity database property.  The quantity database is used
     * to transform the incoming netCDF variables into their canonical
     * {@link visad.RealType}s.  If no transformation is desired, then use {@link
     * QuantityDB#emptyDB}.  A {@link java.beans.PropertyChangeEvent} for <code>quantityDB
     * </code> will be fired, if appropriate.  If the pathname property is
     * non-<code>null</code>, then the netCDF database will be read and a {@link
     * java.beans.PropertyChangeEvent} for the data property will be fired, if appropriate.
     *
     * @param db                    The new quantity database.
     * @throws NullPointerException if the argument is <code>null</code>.
     * @throws BadFormException     if the netCDF dataset doesn't have the 
     *                              right form.
     * @throws IOException          if an error occurs while reading the netCDF
     *                              dataset.
     * @throws VisADException       if a VisAD failure occurs.
     */
    public void
    setQuantityDB(QuantityDB db)
	throws BadFormException, IOException, VisADException
    {
	if (db == null)
	    throw new NullPointerException();

	QuantityDB oldDB;
	DataImpl oldData;
	String   name;

	synchronized(this) {
	    oldDB = quantityDB;
	    oldData = data;
	    name = pathname;
	}

	DataImpl newData = new Plain(db).open(name);

	synchronized(this) {
	    quantityDB = db;
	    data = newData;
	}

        changes.firePropertyChange("quantityDB", oldDB, db);
        changes.firePropertyChange("data", oldData, newData);
    }


    /**
     * Sets the dataset name property.  If the name is <code>null</code>,
     * then the data property will be set to <code>null</code>; otherwise,
     * the dataset will be read. {@link java.beans.PropertyChangeEvent}s for the
     * pathname and data properties will be fired when appropriate.
     *
     * @param name                  The new name of the dataset or 
     *                              <code>null</code>.
     */
    public void
    setPathname(String name)
	throws IOException, VisADException, BadFormException
    {
        String   oldName;
	DataImpl oldData;
        DataImpl newData;

        if (name == null) {
            synchronized(this) {
		oldName = pathname;
		oldData = data;
	    }
	    newData = null;
        }
        else {
            QuantityDB      db;

            synchronized(this) {
                db = quantityDB;
                oldData = data;
                oldName = pathname;
            }

            newData = new Plain(db).open(name);

            synchronized(this) {
                pathname = name;
                data = newData;
            }
        }

        changes.firePropertyChange("pathname", oldName, name);
        changes.firePropertyChange("data", oldData, newData);
    }


    /**
     * Returns the dataset pathname property.  Returns <code>null</code> if the
     * property has no value.
     */
    public synchronized String
    getPathname()
    {
	return pathname;
    }


    /**
     * Returns the VisAD data object property.  Returns <code>null</code> if the
     * property has no value.
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
