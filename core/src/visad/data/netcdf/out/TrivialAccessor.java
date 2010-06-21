/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TrivialAccessor.java,v 1.3 2000-04-26 15:45:26 dglo Exp $
 */

package visad.data.netcdf.out;

import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Data;


/**
 * The TrivialAccessor class terminates the linked-list of
 * DataAccessors at the outermost, VisAD data object.
 */
class
TrivialAccessor
    implements	VisADAccessor
{
    /**
     * The VisAD data object.
     */
    private final Data	data;


    /**
     * Construct from a VisAD data object.
     *
     * @param data	The outermost VisAD data object.
     */
    protected
    TrivialAccessor(Data data)
    {
	this.data = data;
    }


    /**
     * Return the number of netCDF dimensions at the current level.
     *
     * @return		The rank of the data object.
     */
    public int
    getRank()
    {
	return 0;
    }


    /**
     * Return the netCDF dimensions at the level of the data object.
     * Include all dimensions in more outer data objects.
     *
     * @return		The netCDF dimensions of the data object.
     */
    public Dimension[]
    getDimensions()
    {
	return new Dimension[0];
    }


    /**
     * Return the netCDF dimensional lengths.
     *
     * @return		The dimensional lengths of the data object.
     */
    public int[]
    getLengths()
    {
	return new int[0];
    }


    /**
     * Return a datum given its location as netCDF indexes.
     *
     * @return		The data object at the position given by
     *			<code>localIndexes</code> and
     *			<code>outerIndexes</code>.
     * @exception IOException
     *			Data access I/O failure.
     */
    public Object
    get(int[] indexes)
	throws IOException
    {
	return data;
    }
}
