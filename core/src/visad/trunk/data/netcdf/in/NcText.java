/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcText.java,v 1.3 1998-04-02 20:49:46 visad Exp $
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
	super(var, netcdf, new TextType(var.getName()));
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
     * Return the variable's data as a packed array of Strings.
     *
     * @return			The variable's values.
     * @exception IOException	Data access I/O failure.
     */
    String[]
    getStrings()
	throws IOException
    {
	String[]	strings;

	if (getRank() == 0)
	{
	    /* Scalar text variable (i.e. a String). */

	    StringBuffer	strbuf = new StringBuffer(var.getLengths()[0]);

	    for (IndexIterator iter = new IndexIterator(var.getLengths());
		 iter.notDone();
		 iter.incr())
	    {
		strbuf.append(var.getChar(iter.value()));
	    }

	    strings = new String[] {strbuf.toString()};
	}
	else
	{
	    /* Non-scalar text variable (i.e. an array of Strings). */
	    strings = null;	// TODO: support array of Strings
	}

	return strings;
    }


    /**
     * Return the values of this variable as a packed array of VisAD
     * DataImpl objects.  It would be really, really stupid to use this
     * method on a variable of any length.
     *
     * @return			The variable's values.
     * @exception IOException	Data access I/O failure.
     */
    DataImpl[]
    getData()
	throws IOException, VisADException
    {
	Text[]	texts;

	if (getRank() == 0)
	{
	    /* Scalar text variable (i.e. a String). */

	    StringBuffer	strbuf = new StringBuffer(var.getLengths()[0]);

	    for (IndexIterator iter = new IndexIterator(var.getLengths());
		 iter.notDone();
		 iter.incr())
	    {
		strbuf.append(var.getChar(iter.value()));
	    }

	    texts = new Text[]
		{new Text((TextType)mathType, strbuf.toString())};
	}
	else
	{
	    /* Non-scalar text variable (i.e. an array of Strings). */
	    texts = null;	// TODO: support array of Strings
	}

	return texts;
    }
}
