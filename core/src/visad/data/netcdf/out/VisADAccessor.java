/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VisADAccessor.java,v 1.2 2001-11-27 22:29:38 dglo Exp $
 */

package visad.data.netcdf.out;

import java.io.IOException;
import ucar.netcdf.Dimension;


/**
 * The VisADAccessor interface provides an abstraction for accessing data
 * in a VisAD data object that's being adapted to a netCDF variable API.
 */
interface
VisADAccessor
{
    /**
     * Return the number of netCDF dimensions at the current level.
     *
     * @return	The rank (i.e. number of netCDF dimensions) of the variable.
     */
    int
    getRank();


    /**
     * Return the netCDF dimensions at the level of the data object.
     * Include all dimensions in more outer data objects.
     *
     * @return		The dimensions of the variable.
     * @postcondition	<code>getRank() == getDimensions().length</code>.
     */
    Dimension[]
    getDimensions();


    /**
     * Return the netCDF dimensional lengths.
     *
     * @return		The dimensional lengths.
     * @postcondition	<code>getRank() == getLengths().length</code>.
     */
    int[]
    getLengths();


    /**
     * Return a datum given its location as netCDF indexes.
     *
     * @return		The data object at the given netCDF position.
     */
    Object
    get(int[] indexes)
	throws IOException;
}
