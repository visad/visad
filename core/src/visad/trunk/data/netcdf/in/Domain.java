/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Domain.java,v 1.4 1998-03-31 20:35:37 visad Exp $
 */

package visad.data.netcdf.in;

import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;


/**
 * The Domain class associates a VisAD domain with netCDF variables.
 */
class
Domain
    implements	Comparable
{
    /**
     * The rank of the domain:
     */
    protected final int		rank;

    /**
     * The VisAD MathType of the manifold domain:
     */
    protected final MathType	mathType;

    /**
     * The netCDF dimensions of the domain:
     */
    protected final NcDim[]	dims;


    /**
     * Construct from an array of adapted, netCDF dimensions.
     *
     * @param dims			netCDF dimensions
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    Domain(NcDim[] dims)
	throws VisADException
    {
	this.dims = dims;

	rank = dims.length;

	mathType = getMathType(dims);
    }


    /**
     * Get the VisAD math type of the given, adapted, netCDF dimensions.
     *
     * @param dims			The netCDF dimensions of the domain.
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    protected static MathType
    getMathType(NcDim[] dims)
	throws VisADException
    {
	MathType	type;

	if (dims.length == 0)
	{
	    type = null;		// scalar
	}
	else
	if (dims.length == 1)
	{
	    type = dims[0].getMathType();
	}
	else
	{
	    RealType[]	types = new RealType[dims.length];

	    for (int i = 0; i < dims.length; ++i)
		types[i] = dims[visadIdim(dims.length, i)].getMathType();

	    type = new RealTupleType(types);
	}

	return type;
    }


    /**
     * Convert a netCDF dimension index to a VisAD one.
     *
     * @param netcdfIdim	The netCDF dimension index to be converted.
     * @return			The VisAD dimension index.
     */
    protected static int
    visadIdim(int rank, int netcdfIdim)
    {
	return rank - netcdfIdim - 1;
    }


    /**
     * Return the dimensions associated with this domain.
     *
     * @return	The netCDF dimensions associated with the domain.
     */
    NcDim[]
    getDimensions()
    {
	return dims;
    }


    /**
     * Return the rank of this domain.
     *
     * @return	The dimensionality of the domain (i.e. number of dimensions).
     */
    int
    getRank()
    {
	return rank;
    }


    /**
     * Return the VisAD MathType of the domain.
     *
     * @return	The VisAD MathType of the domain.
     */
    MathType
    getMathType()
    {
	return mathType;
    }


    /**
     * Indicate whether or not the domain is the same as another.
     *
     * @param that	The other Domain.
     * @return		<code>true</code> if and only if the Domain and 
     *			another are semantically identical.
     */
    public boolean
    equals(Domain that)
    {
	if (dims.length == 0 && this != that)
	    return false;

	if (dims.length != that.dims.length)
	    return false;

	for (int i = 0; i < rank; ++i)
	    if (!dims[i].equals(that.dims[i]))
		return false;

	return true;
    }


    /**
     * Compare this domain to another.
     *
     * @param other	The other Domain.
     * @return		Less than 0, zero, or greater than zero depending on
     *			whether this domain is less than, equal to, or greater
     *			than the other domain, respectively.
     */
    public int
    compareTo(Object other)
    {
	Domain	that = (Domain)other;
	int	n = dims.length - that.dims.length;

	for (int i = 0; n == 0 && i < rank; ++i)
	{
	    n = dims[i].compareTo(that.dims[i]);
	}

	return n;
    }


    /**
     * Return the hash code of the domain.
     *
     * @return	The hash code of the domain.
     */
    public int
    hashCode()
    {
	int	hashCode = 0;

	for (int i = 0; i < rank; ++i)
	    hashCode ^= dims[i].hashCode();

	return hashCode;
    }


    /**
     * Return a string representation of the domain.
     */
    public String
    toString()
    {
	StringBuffer	rep = new StringBuffer(80);

	rep.append("(");

	if (dims.length > 0)
	{
	    rep.append(dims[0].toString());

	    for (int i = 1; i < dims.length; ++i)
	    {
		rep.append(", ");
		rep.append(dims[i].toString());
	    }
	}

	rep.append(")");

	return rep.toString();
    }
}
