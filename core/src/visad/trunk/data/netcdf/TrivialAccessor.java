/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TrivialAccessor.java,v 1.1 1998-03-11 16:21:55 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Data;


/**
 * The trivial VisADAccessor.  It is used to terminate the linked-list of
 * data accessors at the outermost, VisAD data object.
 */
class
TrivialAccessor
    implements	VisADAccessor
{
    /**
     * The VisAD data object.
     */
    protected final Data	data;


    /**
     * Construct from a VisAD data object.
     */
    protected
    TrivialAccessor(Data data)
    {
	this.data = data;
    }


    /**
     * Return the number of netCDF dimensions at the current level.
     */
    public int
    getRank()
    {
	return 0;
    }


    /**
     * Return the netCDF dimensions at the level of the data object.
     * Include all dimensions in more outer data objects.
     */
    public Dimension[]
    getDimensions()
    {
	return new Dimension[0];
    }


    /**
     * Return the netCDF dimensional lengths.
     */
    public int[]
    getLengths()
    {
	return new int[0];
    }


    /**
     * Return a datum given its location as netCDF indexes.
     */
    public Object
    get(int[] indexes)
	throws IOException
    {
	return data;
    }
}
