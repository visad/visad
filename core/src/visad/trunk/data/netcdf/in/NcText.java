/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcText.java,v 1.1 1998-03-20 20:57:00 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import ucar.multiarray.IndexIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.Data;
import visad.DataImpl;
import visad.MathType;
import visad.Text;
import visad.TextType;
import visad.VisADException;


/**
 * The NcText class adapts a netCDF character variable that's being
 * imported to a VisAD API.  It will represent the netCDF textual variable
 * as an array of strings.  The rank of the array will be one less than the
 * number of netCDF dimensions.
 */
final class
NcText
    extends NcVar
{
    /**
     * The VisAD rank (one less than the number of netCDF dimensions.
     */
    protected final int	visadRank;


    /**
     * Indicate whether or not a netCDF variable can be represented as 
     * an NcText.
     *
     * @param var	The netCDF variable to be examined.
     * @return		<code>true</code> if and only if <code>var</code> can
     *			be represented as an NcText object.
     */
    static boolean
    isRepresentable(Variable var)
    {
	return var.getComponentType().equals(Character.TYPE);
    }


    /**
     * Construct.
     *
     * @param var	The netCDF character variable to be adapted.
     * @param netcdf	The netCDF dataset that contains <code>var</code>.
     * @precondition	<code>isRepresentable(var).
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    NcText(Variable var, Netcdf netcdf)
	throws VisADException
    {
	super(var, netcdf);
	mathType = new TextType(var.getName());
	set = null;
	visadRank = var.getRank() - 1;
    }


    /**
     * Indicate if this variable is textual.
     *
     * @return	<code>true</code> always.
     */
    boolean
    isText()
    {
	return true;
    }

    /**
     * Indicate if this variable is longitude.
     *
     * @return	<code>false</code> always.
     */
    boolean
    isLongitude()
    {
	return false;
    }


    /**
     * Indicate whether or not the variable is temporal in nature.
     *
     * @return	<code>false</code> always.
     */
    boolean
    isTime()
    {
	return false;
    }


    /**
     * Return the VisAD rank of this variable.
     *
     * @return	The VisAD rank (i.e. number of netCDF dimensions - 1) of the
     *		variable.
     */
    int
    getRank()
    {
	return visadRank;
    }


    /**
     * Indicate whether or not the variable is a co-ordinate variable.
     *
     * @return	<code>false</code> always.
     */
    boolean
    isCoordinateVariable()
    {
	return false;
    }


    /**
     * Return the values of this variable as a packed array of floats.
     *
     * @exception IOException		I/O error always.
     */
    float[]
    getFloatValues()
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return the values of this variable as a packed array of doubles.
     *
     * @exception IOException		I/O error always.
     */
    double[]
    getDoubleValues()
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return the values of this variable -- at a given point of the outermost
     * dimension -- as a packed array of doubles.
     *
     * @exception IOException		I/O error always.
     */
    double[]
    getDoubleValues(int ipt)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return the string of the variable at the given position.
     *
     * @param indexes	The netCDF indexes of the string (missing innermost
     *			index).
     */
    String
    getString(int[] indexes)
    {
	throw new UnsupportedOperationException("getString()");
    }


    /**
     * Return the variable as a VisAD data object.
     *
     * @return			The VisAD data object corresponding to the
     *				Variable.
     * @exception IOException   I/O error.
     */
    DataImpl
    getData()
	throws IOException, VisADException
    {
	DataImpl	data;

	if (getRank() == 0)
	{
	    /* Scalar text variable (i.e. a String). */

	    StringBuffer	string = new StringBuffer(var.getLengths()[0]);

	    for (IndexIterator iter = new IndexIterator(var.getLengths());
		 iter.notDone();
		 iter.incr())
	    {
		string.append(var.getChar(iter.value()));
	    }

	    data = new Text((TextType)getMathType(), string.toString());
	}
	else
	{
	    /* Non-scalar text variable (i.e. an array of Strings). */
	    data = null;	// TODO
	}

	return data;
    }
}
