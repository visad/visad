/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VisADAccessor.java,v 1.2 1998-03-11 16:21:59 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Data;
import visad.Field;
import visad.Real;
import visad.Text;
import visad.Tuple;


/**
 * Interface for accessing data in a VisAD data object that's been 
 * adapted to a netCDF variable API.
 */
interface
VisADAccessor
{
    /**
     * Return the number of netCDF dimensions at the current level.
     */
    int
    getRank();


    /**
     * Return the netCDF dimensions at the level of the data object.
     * Include all dimensions in more outer data objects.
     */
    Dimension[]
    getDimensions();


    /**
     * Return the netCDF dimensional lengths.
     */
    int[]
    getLengths();


    /**
     * Return a datum given its location as netCDF indexes.
     */
    Object
    get(int[] indexes)
	throws IOException;
}
