/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcDim.java,v 1.5 1998-04-03 20:35:18 visad Exp $
 */

package visad.data.netcdf.in;

import ucar.netcdf.Dimension;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.FloatSet;
import visad.RealType;
import visad.Set;
import visad.Unit;
import visad.VisADException;


/**
 * The NcDim class decorates a netCDF dimension.  It's useful when importing
 * a netCDF dataset.
 */
class
NcDim
    extends	Dimension
    implements	Comparable
{
    /**
     * Factory method for constructing the right type of dimension decorator.
     *
     * @param dim	The netCDF dimension to be decorated.
     * @param netcdf	The netCDF dataset that contains <code>dim</code>.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    static NcDim
    create(Dimension dim, Netcdf netcdf)
	throws VisADException
    {
	Variable	var = netcdf.get(dim.getName());

	return (var == null || var.getRank() != 1 ||
		var.getComponentType().equals(Character.TYPE))
		    ? new NcDim(dim)
		    : new NcCoordDim(dim, netcdf);
    }


    /**
     * Construct from a netCDF dimension.  Protected to ensure use of
     * the NcDim factory method.
     *
     * @param dim	The netCDF dimension to be decorated.
     */
    protected
    NcDim(Dimension dim)
    {
	super(dim.getName(), dim.getLength());
    }


    /**
     * Return the VisAD MathType for this dimension.
     *
     * @return		The VisAD MathType for the dimension.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    RealType
    getMathType()
	throws VisADException
    {
	RealType	mathType = RealType.getRealTypeByName(getName());

	if (mathType == null)
	{
	    mathType = new RealType(getName());

	    // TODO: add coordinate system
	    Set	set = new FloatSet(mathType);

	    mathType.setDefaultSet(set);
	}
	
	return mathType;
    }


    /**
     * Indicate whether or not this dimension is the same as another.
     *
     * @param that	The other, decorated, netCDF dimension.
     * @return		<code>true</code> if and only if the dimension and
     *			the other dimension are semantically identical.
     */
    boolean
    equals(NcDim that)
    {
	return getName().equals(that.getName());
    }


    /**
     * Compare this dimension to another.
     *
     * @param other	The other, decorated, netCDF dimension.
     * @return		Less than 0, zero, or greater than zero depending on
     *			whether this domain is less than, equal to, or greater
     *			than the other dimension, respectively.
     */
    public int
    compareTo(Object other)
    {
	NcDim	that = (NcDim)other;

	return getName().compareTo(that.getName());
    }


    /**
     * Indicate whether or not this dimension is temporal in nature.
     *
     * @return	<code>true</code> if and only if the dimension represents
     *		time.
     */
    boolean
    isTime()
    {
	String	name = getName();

	return name.equals("time") ||
	       name.equals("Time") ||
	       name.equals("TIME");
    }


    /**
     * Return the hash code of this dimension.
     *
     * @return	The hash code of the dimension.
     */
    public int
    hashCode()
    {
	return getName().hashCode();
    }


    /**
     * Convert this dimension to a string.
     *
     * @return	The dimension represented as a string.
     * @deprecated
     */
    public String
    toString()
    {
	return getName();
    }


    /**
     * Return the co-ordinate variable associated with this dimension.
     *
     * @return	The netCDF coordinate variable associated with the dimension
     *		or <code>null</code> if there isn't one.
     */
    NcVar
    getCoordVar()
    {
	return null;
    }
}


/**
 * The NcCoordDim class decorates a netCDF dimension that has a netCDF
 * coordinate variable.
 */
class
NcCoordDim
    extends	NcDim
{
    /**
     * The associated coordinate variable.
     */
    protected final NcVar	coordVar;


    /**
     * Construct from a netCDF dimension and dataset.  Protected to ensure
     * use of the NcDim factory method.
     *
     * @param dim	The netCDF dimension that has a coordinate variable.
     * @param netcdf	The netCDF dataset that contains <code>dim</code>.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    protected
    NcCoordDim(Dimension dim, Netcdf netcdf)
	throws VisADException
    {
	super(dim);
	coordVar = NcVar.newNcVar(netcdf.get(dim.getName()), netcdf);
    }


    /**
     * Indicate whether or not this dimension is temporal in nature.
     *
     * @return	<code>true</code> if and only if the dimension represents
     *		time.
     */
    boolean
    isTime()
    {
	return super.isTime() || coordVar.isTime();
    }


    /**
     * Return the VisAD MathType for this dimension.
     *
     * @return		The VisAD MathType for the dimension.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    RealType
    getMathType()
	throws VisADException
    {
	return (RealType)coordVar.getMathType();
    }


    /**
     * Return the co-ordinate variable associated with this dimension.
     *
     * @return	The coordinate variable associated with the dimension.
     */
    NcVar
    getCoordVar()
    {
	return coordVar;
    }
}
