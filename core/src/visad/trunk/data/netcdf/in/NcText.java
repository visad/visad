/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcText.java,v 1.6 1998-09-14 13:51:39 billh Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import ucar.multiarray.IndexIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.Data;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FunctionType;
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
     * The VisAD rank (one less than the number of netCDF dimensions).
     */
    private final int	visadRank;


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
     * @param var		The netCDF character variable to be adapted.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @precondition		<code>isRepresentable(var).
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD 
     *				object couldn't be created.
     */
    NcText(Variable var, Netcdf netcdf)
	throws VisADException
    {
	super(var, netcdf, new TextType(var.getName()));

	if (!isRepresentable(var))
	    throw new VisADException("Variable not textual");

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
     * Indicate if this variable is latitude.
     *
     * @return	<code>false</code> always.
     */
    boolean
    isLatitude()
    {
	return false;
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
     * Gets the netCDF dimensions of this variable.  NB: The innermost
     * dimension is omitted.
     */
    NcDim[]
    getDimensions()
	throws VisADException
    {
	NcDim[]		dims = new NcDim[visadRank];

	System.arraycopy(super.getDimensions(), 0, dims, 0, visadRank);

	return dims;
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
     * @throws IOException		I/O error always.
     */
    float[]
    getFloats()
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return the values of this variable as a packed array of doubles.
     *
     * @throws IOException		I/O error always.
     */
    public double[]
    getDoubles()
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return the values of this variable -- at a given point of the outermost
     * dimension -- as a packed array of doubles.
     *
     * @throws IOException		I/O error always.
     */
    double[]
    getDoubles(int ipt)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return the variable's data as a packed array of Strings.
     *
     * @return			The variable's values.
     * @throws IOException	Data access I/O failure.
     */
    String[]
    getStrings()
	throws IOException
    {
	String[]	strings;

	if (getRank() == 0)
	{
	    /* Scalar text variable (i.e. a String). */

	    Variable		var = getVar();
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
     * Gets the VisAD data object corresponding to this variable.
     *
     * @return			The variable.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getData()
	throws IOException, VisADException
    {
	return getData(getValues());
    }


    /**
     * Gets all the values of this netCDF variables as an array of Text.
     */
    protected Text[]
    getValues()
    {
	return null;	// STUB
    }


    /**
     * Gets the VisAD data object corresponding to this variable with the
     * given values.
     *
     * @precondition		<code>getRank() >= 1</code>
     * @return			The values of the variable at the given 
     *				position.
     * @throws IOException	I/O error.
     */
    protected DataImpl
    getData(Text[] values)
	throws IOException, VisADException
    {
	if (getRank() < 1)
	    throw new VisADException("Variable is scalar");

	TextType	type = (TextType)getMathType();
	DataImpl	data;

	if (values.length == 1)
	{
	    data = values[0];
	}
	else
	{
          /* WLH 13 Sept 98 */
          NcDomain domain = new NcDomain(this, getDimensions());

/* WLH 13 Sept 98
	    NcDomain		domain = new NcDomain(getDimensions());
*/
	    FunctionType	funcType =
		new FunctionType(domain.getType(), getMathType());
	    FieldImpl		field =
		new FieldImpl(funcType, domain.getSet());

	    field.setSamples(values, /*copy=*/false);

	    data = field;
	}

	return data;
    }


	/*
	Text[]	texts;

	if (getRank() == 0)
	{
	    /* Scalar text variable (i.e. a String). */
	    /*

	    Variable		var = getVar();
	    StringBuffer	strbuf = new StringBuffer(var.getLengths()[0]);

	    for (IndexIterator iter = new IndexIterator(var.getLengths());
		 iter.notDone();
		 iter.incr())
	    {
		strbuf.append(var.getChar(iter.value()));
	    }

	    texts = new Text[]
		{new Text((TextType)getMathType(), strbuf.toString())};
	}
	else
	{
	    /* Non-scalar text variable (i.e. an array of Strings). */
	    /*
	    texts = null;	// TODO: support array of Strings
	}

	return texts;
	*/


    /**
     * Gets a proxy for the VisAD data object corresponding to this data 
     * object.
     */
    public DataImpl
    getProxy()
	throws VisADException
    {
	throw new VisADException("Not supported yet");
    }


    /**
     * Return the value of this variable at a given point of the outermost
     * dimension.
     *
     * @return			The variable's values.
     * @throws IOException	I/O error.
     */
    public DataImpl
    getData(int ipt)
	throws IOException, VisADException
    {
	return null;	// STUB
    }
}
