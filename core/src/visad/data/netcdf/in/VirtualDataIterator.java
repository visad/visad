/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualDataIterator.java,v 1.4 2001-11-27 22:29:36 dglo Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.util.NoSuchElementException;
import visad.VisADException;
import visad.data.BadFormException;


/**
 * Supports iteration over the virtual VisAD data objects in a
 * netCDF dataset.
 */
public abstract class
VirtualDataIterator
{
    /**
     * The next virtal data object.
     */
    private VirtualData		data;

    /**
     * The view of the netCDF dataset that's being iterated over.
     */
    protected final View	view;


    /**
     * Constructs from a view of a netCDF dataset.
     *
     * @param view		A view of a netCDF dataset.
     */
    public
    VirtualDataIterator(View view)
    {
	this.view = view;
    }


    /**
     * Indicates if there's another virtual VisAD data object.
     *
     * @return			<code>true</code> <=> <code>hasNext()
     *				</code> will return the next virtual
     *				VisAD data object.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	I/O failure.
     */
    public boolean
    hasNext()
	throws VisADException, IOException
    {
	if (data == null)
	    data = getData();

	return data != null;
    }


    /**
     * Gets the next virtual VisAD data object.  The next object will be either
     * a VirtualScalar, a VirtualField, or a VirtualFlatField.
     *
     * @return			The next virtual VisAD data object.
     * @throws BadFormException	Non-conforming netCDF dataset.
     * @throws NoSuchElementException
     *				No more virtual VisAD data objects.
     * @throws IOException	I/O failure.
     */
    public VirtualData
    next()
	throws BadFormException, NoSuchElementException, VisADException,
	    IOException
    {
	VirtualData	next = data == null
				? getData()
				: data;

	data = null;

	return next;
    }


    /**
     * Gets a clone of the next virtual VisAD data object.  This method must be
     * overridden in subclasses.
     *
     * @return			A clone of the next virtual VisAD data object or
     *				<code>null</code> if there is none.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected abstract VirtualData
    getData()
	throws VisADException, IOException;


    /**
     * Gets the view of the netCDF dataset.
     */
    public View
    getNetcdf()
    {
	return view;
    }
}
