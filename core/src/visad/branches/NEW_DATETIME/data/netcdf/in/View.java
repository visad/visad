/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: View.java,v 1.1 1998-09-23 17:31:37 steve Exp $
 */

package visad.data.netcdf.in;

import ucar.netcdf.Netcdf;


/**
 * Provides support for viewing a netCDF dataset through a set of 
 * conventions.
 */
public abstract class
View
{
    /**
     * The netCDF dataset that is being viewed through these conventions.
     */
    protected final Netcdf	netcdf;


    /**
     * Constructs from a netCDF dataset.
     *
     * @param netcdf		The netCDF dataset.
     */
    public
    View(Netcdf netcdf)
    {
	this.netcdf = netcdf;
    }


    /**
     * Gets the netCDF dataset being viewed.
     *
     * @return			The netCDF dataset.
     */
    public Netcdf
    getNetcdf()
    {
	return netcdf;
    }


    /**
     * Gets an iterator over the virtual VisAD data objects determined by
     * this view.
     *
     * @return			An iterator for the virtual VisAD data objects
     *				in the view.
     */
    public abstract VirtualDataIterator
    getVirtualDataIterator();
}
