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
 * Class for associating a VisAD domain and netCDF variables.
 */
public class
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
     * Construct.
     */
    public
    Domain(NcVar[] vars, DimensionTable dimTable)
	throws UnimplementedException, VisADException, IOException
    {
	if (vars.length == 0)
	    throw new UnimplementedException(
		"Domain.Domain(): no scalar support");

	this.vars = vars;

	dims = vars[0].getDimensions();

	rank = dims.length;

	setMathType(dimTable);

	setSet();
    }


    /**
     * Set the VisAD math type of the domain.
     */
    protected void
    setMathType(DimensionTable dimTable)
	throws VisADException
    {
	if (dims.length == 1)
	    mathType = dimTable.getRealType(dims[0]);
	else
	{
	    RealType[]	types = new RealType[dims.length];

	    for (int i = 0; i < dims.length; ++i)
		types[i] = dimTable.getRealType(dims[visadIdim(i)]);

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
     * Set the VisAD sampled set of the domain.
     */
    protected void
    setSet()
	throws VisADException, IOException, BadFormException
    {
	boolean		coordinateVariables = false;
	boolean	 	arithmeticProgressions = true;
	ArithProg[]	aps = new ArithProg[rank];

	for (int idim = 0; idim < rank; ++idim)
	{
	    NcVar	var = dims[visadIdim(idim)].getCoordinateVariable();

	    if (var != null)
	    {
		coordinateVariables = true;

		if (var.isLongitude())
		    aps[idim] = new LonArithProg();
		else
		    aps[idim] = new ArithProg();

		if (!aps[idim].accumulate((double[])var.getValues()))
		{
		    arithmeticProgressions = false;
		    break;
		}
	    }
	}

	if (!coordinateVariables)
	{
	    /*
	     * This domain has no co-ordinate variables.
	     */

	    int[]	lengths = new int[rank];

	    /*
	     * Reverse the order of the dimensional lengths for VisAD.
	     */
	    for (int idim = 0; idim < rank; ++idim)
		lengths[idim] = dims[visadIdim(idim)].getLength();

	    set = IntegerSet.create(mathType, lengths);
	}
	else
	if (arithmeticProgressions)
	{
	    /*
	     * This domain has co-ordinate variables -- all of which are
	     * arithmetic progressions.
	     */

	    double[]	firsts = new double[rank];
	    double[]	lasts = new double[rank];
	    int[]	lengths = new int[rank];

	    for (int idim = 0; idim < rank; ++idim)
	    {
		NcDim	dim = dims[visadIdim(idim)];
		NcVar	var = dim.getCoordinateVariable();

		if (var == null)
		{
		    /*
		     * The dimension doesn't have a co-ordinate variable.
		     */
		    firsts[idim] = 0;
		    lengths[idim] = dim.getLength();
		    lasts[idim] = lengths[idim] - 1;
		}
		else
		{
		    /*
		     * The dimension has a co-ordinate variable.
		     */
		    firsts[idim] = aps[idim].getFirst();
		    lasts[idim] = aps[idim].getLast();
		    lengths[idim] = aps[idim].getNumber();
		}
	    }

	    set = LinearSet.create(mathType, firsts, lasts, lengths);
	}
	else
	{
	    /*
	     * This domain has at least one co-ordinate variable which is 
	     * not an arithmetic progression.  This is the general case.
	     */

	    int[]	lengths = new int[rank];
	    double[][]	values = new double[rank][];
	    int		ntotal = 1;

	    for (int idim = 0; idim < rank; ++idim)
	    {
		lengths[idim] = dims[visadIdim(idim)].getLength();
		ntotal *= lengths[idim];
	    }

	    for (int idim = 0; idim < rank; ++idim)
	    {
		NcDim		dim = dims[visadIdim(idim)];
		NcVar		var = dim.getCoordinateVariable();
		double[]	vals;

		values[idim] = new double[ntotal];

		if (var != null)
		    vals = (double[])var.getValues();
		else
		{
		    int	npts = lengths[idim];

		    vals = new double[npts];

		    for (int ipt = 0; ipt < npts; ++ipt)
			vals[ipt] = ipt;
		}

		for (int pos = 0; pos < ntotal/vals.length; pos += vals.length)
		    System.arraycopy(vals, 0, values[idim], pos, vals.length);
	    }

	    // System.out.println("lengths[0]=" + lengths[0]);
	    // System.out.println("lengths[1]=" + lengths[1]);
	    // System.out.println("lengths[2]=" + lengths[2]);

	    set = GriddedSet.create(mathType, values, lengths);
	}
    }


    /**
     * Return the variables associated with the domain.
     */
    public NcVar[]
    getVariables()
    {
	return vars;
    }


    /**
     * Return the VisAD MathType of the domain.
     */
    public MathType
    getMathType()
    {
	return mathType;
    }


    /**
     * Return the VisAD sampled set of the domain.
     */
    public Set
    getSet()
	throws VisADException
    {
	return set;
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
