/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DataAccessor.java,v 1.1 1998-03-11 16:21:48 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Field;


/**
 * Class for accessing data in a VisAD data object that's been adapted
 * to a netCDF variable API.
 */
abstract class
DataAccessor
    implements	VisADAccessor
{
    /**
     * The number of netCDF dimensions in the VisAD data object.
     */
    protected final int			localRank;

    /**
     * The netCDF Dimensions of the VisAD data object.
     */
    protected final Dimension[]		localDims;

    /**
     * The netCDF indexes for the local VisAD data object.
     */
    protected volatile int[]		localIndexes;

    /**
     * The VisADAccessor of the outer VisAD data object.
     */
    protected final VisADAccessor	outerAccessor;

    /**
     * The number of netCDF dimensions in the outer VisAD data object.
     */
    protected final int			outerRank;

    /**
     * The netCDF indexes for the outer VisAD data object.
     */
    protected volatile int[]		outerIndexes;


    /**
     * Construct from an outer VisADAccessor and netCDF Dimensions.
     *
     * @precondition	<code>outerAccessor != null</code>
     */
    protected
    DataAccessor(Dimension[] localDims, VisADAccessor outerAccessor)
    {
	localRank = localDims.length;
	outerRank = outerAccessor.getRank();
	this.localDims = localDims;
	localIndexes = new int[localRank];
	outerIndexes = new int[outerRank];
	this.outerAccessor = outerAccessor;
    }


    /**
     * Return the number of netCDF dimensions at the current level.
     */
    public int
    getRank()
    {
	return outerRank + localRank;
    }


    /**
     * Return the netCDF dimensions at the level of the data object.
     * Include all dimensions in more outer data objects.
     */
    public Dimension[]
    getDimensions()
    {
	Dimension[]	dims = new Dimension[getRank()];

	if (outerAccessor != null)
	    System.arraycopy(outerAccessor.getDimensions(), 0,
		dims, 0, outerRank);
	System.arraycopy(localDims, 0, dims, outerRank, localRank);

	return dims;
    }


    /**
     * Return the netCDF dimensional lengths.
     */
    public int[]
    getLengths()
    {
	int[]		lengths = new int[getRank()];
	Dimension[]	outerDims = getDimensions();

	for (int i = 0; i < lengths.length; ++i)
	    lengths[i] = outerDims[i].getLength();

	return lengths;
    }


    /**
     * Return a datum given its location as netCDF indexes.
     */
    public Object
    get(int[] indexes)
	throws IOException
    {
	System.arraycopy(indexes, 0, outerIndexes, 0, outerRank);
	System.arraycopy(indexes, outerRank, localIndexes, 0, localRank);

	return get();
    }


    /**
     * Return a datum given the split, netCDF indexes.
     */
    protected abstract Object
    get()
	throws IOException;
}
