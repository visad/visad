/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DataAccessor.java,v 1.4 2001-11-27 22:29:38 dglo Exp $
 */


package visad.data.netcdf.out;

import java.io.IOException;
import ucar.netcdf.Dimension;


/**
 * The DataAccessor class provides the top-level abstraction for accessing
 * the data in a VisAD data object via the netCDF variable API.
 */
abstract class DataAccessor implements VisADAccessor
{
    /*
     * The following fields are not private because I decided that they
     * were used so much by the subclasses that using implementing
     * access via get...()/set...() accessors would be too inefficient.
     */

    /** The number of netCDF dimensions in the VisAD data object. */
    protected final int			localRank;

    /**
     * The netCDF Dimensions of the VisAD data object in netCDF order
     * (outermost dimension first).
     */
    protected final Dimension[]		localDims;

    /**
     * The netCDF indexes for the local VisAD data object in netCDF order
     * (outermost dimension first).
     */
    protected volatile int[]		localIndexes;

    /** The VisADAccessor of the outer VisAD data object. */
    protected final VisADAccessor	outerAccessor;

    /** The number of netCDF dimensions in the outer VisAD data object. */
    protected final int			outerRank;

    /**
     * The netCDF indexes for the outer VisAD data object in netCDF order
     * (outermost dimension first).
     */
    protected volatile int[]		outerIndexes;

    /**
     * Construct from an outer VisADAccessor and netCDF Dimensions.
     *
     * @param localDims		The netCDF dimensions of the VisAD data
     *				object being adapted (in netCDF API order).
     * @param outerAccessor	The DataAccessor of the enclosing VisAD data
     *				object (may not be <code>null</code>).
     */
    protected DataAccessor(Dimension[] localDims, VisADAccessor outerAccessor)
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
     *
     * @return	The netCDF rank (i.e. the number of netCDF dimensions) of the
     *		VisAD data object being adapted.  Includes the dimensions of
     *		all enclosing VisAD data objects.
     */
    public int getRank() {
	return outerRank + localRank;
    }

    /**
     * Return the netCDF dimensions at the level of the data object.
     * Include all dimensions in more outer data objects.
     *
     * @return	The array of netCDF Dimensions of the VisAD data object
     *		being adapted.  Includes the dimensions of all enclosing
     *		VisAD data objects.
     * @postcondition	<code>getDimensions().length == getRank()</code>.
     */
    public Dimension[] getDimensions() {
	Dimension[]	dims = new Dimension[getRank()];

	System.arraycopy(outerAccessor.getDimensions(), 0, dims, 0, outerRank);
	System.arraycopy(localDims, 0, dims, outerRank, localRank);

	return dims;
    }

    /**
     * Return the netCDF dimensional lengths.
     *
     * @return	The lengths of the netCDF Dimensions of the VisAD data object
     *		being adapted.  Includes the dimensions of all enclosing
     *		VisAD data objects.
     * @postcondition	<code>getLengths().length == getRank()</code>.
     * @postcondition	<code>getLengths()[i] ==
     *			getDimensions()[i].getLength()</code>.
     */
    public int[] getLengths() {
	int[]		lengths = new int[getRank()];
	Dimension[]	outerDims = getDimensions();

	for (int i = 0; i < lengths.length; ++i)
	    lengths[i] = outerDims[i].getLength();

	return lengths;
    }

    /**
     * Return a datum given its location as netCDF indexes.
     *
     * @param indexes	The netCDF indexes for the datum.
     * @precondition	<code>indexes</code> lies within the adapted object.
     * @return	The VisAD data object (e.g. <code>Tuple</code>) or Java
     *		primitive (e.g. <code>Double</code>) corresponding to
     *		the given position.
     * @exception IOException	The corresponding datum couldn't be accessed.
     */
    public Object get(int[] indexes) throws IOException {
	System.arraycopy(indexes, 0, outerIndexes, 0, outerRank);
	System.arraycopy(indexes, outerRank, localIndexes, 0, localRank);

	return get();
    }

    /**
     * Return a datum given the split, netCDF indexes.
     *
     * @precondition	The point given by <code>outerIndexes</code> and
     *			<code>localIndexes</code> lies within the adapted
     *			data object.
     * @return	The VisAD data object (e.g. <code>Tuple</code>) or Java
     *		primitive (e.g. <code>Double</code>) corresponding to
     *		<code>outerIndexes</code> and <code>localIndexes</code>.
     * @exception IOException	The corresponding datum couldn't be accessed.
     */
    protected abstract Object get() throws IOException;
}
