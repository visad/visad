package visad.data.netcdf;


import java.io.IOException;
import visad.data.BadFormException;
import visad.GriddedSet;
import visad.IntegerSet;
import visad.LinearSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.Set;
import visad.UnimplementedException;
import visad.VisADException;


/**
 * Class for associating a VisAD domain with netCDF variables.
 */
class
Domain
{
    /**
     * The rank of the domain:
     */
    protected final int		rank;

    /**
     * The VisAD MathType of the manifold domain:
     * Effectively "final".
     */
    protected MathType		mathType;

    /**
     * The netCDF dimensions of the domain:
     */
    protected final NcDim[]	dims;

    /**
     * The netCDF variables of the domain:
     */
    protected final NcVar[]	vars;

    /**
     * The VisAD sampled set of the domain:
     * Effectively "final".
     */
    protected SampledSet	set;


    /**
     * Construct from an array of adapted, netCDF variables.
     *
     * @exception UnimplementedException	Not yet!
     * @exception VisADException	Couldn't create necessary VisAD object.
     * @exception IOException		I/O error.
     */
    Domain(NcVar[] vars)
	throws UnimplementedException, VisADException, IOException
    {
	if (vars.length == 0)
	    throw new UnimplementedException(
		"Domain.Domain(): no scalar support");

	this.vars = vars;

	dims = vars[0].getDimensions();

	rank = dims.length;

	setMathType(dims);
    }


    /**
     * Set the VisAD math type of the domain.
     *
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    protected void
    setMathType(NcDim[] dims)
	throws VisADException
    {
	if (dims.length == 1)
	    mathType = dims[0].getMathType();
	else
	{
	    RealType[]	types = new RealType[dims.length];

	    for (int i = 0; i < dims.length; ++i)
		types[i] = dims[visadIdim(i)].getMathType();

	    mathType = new RealTupleType(types);
	}
    }


    /**
     * Convert a netCDF dimension index to a VisAD one.
     */
    protected int
    visadIdim(int netcdfIdim)
    {
	return rank - netcdfIdim - 1;
    }


    /**
     * Return the variables associated with this domain.
     */
    NcVar[]
    getVariables()
    {
	return vars;
    }


    /**
     * Return the dimensions associated with this domain.
     */
    NcDim[]
    getDimensions()
    {
	return dims;
    }


    /**
     * Return the rank of this domain.
     */
    int
    getRank()
    {
	return rank;
    }


    /**
     * Return the VisAD MathType of the domain.
     */
    MathType
    getMathType()
    {
	return mathType;
    }


    /**
     * Indicate whether or not the domain is the same as another.
     */
    public boolean
    equals(Domain that)
    {
	if (dims.length != that.dims.length)
	    return false;

	for (int i = 0; i < rank; ++i)
	    if (!dims[i].equals(that.dims[i]))
		return false;

	return true;
    }


    /**
     * Return the hash code of the domain.
     */
    public int
    hashCode()
    {
	int	hashCode = 0;

	for (int i = 0; i < rank; ++i)
	    hashCode ^= dims[i].hashCode();

	return hashCode;
    }
}
