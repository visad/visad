package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.ArrayInput;
import ucar.netcdf.Dimension;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Variable;
import visad.data.BadFormException;
import visad.MathType;
import visad.VisADException;


/**
 * Abstract adaptor/decorator class for netCDF coordinate variables.
 */
public class
NcCoordVar
{
    /**
     * The netCDF variable:
     * Effectively "final".
     */
    protected Variable	var;

    /**
     * The name of the variable:
     * Effectively "final".
     */
    protected String	name;

    /**
     * The netCDF file:
     * Effectively "final".
     */
    protected NcFile	file;

    /**
     * The number of points.
     */
    protected final int	length;


    /**
     * Construct.
     */
    public
    NcCoordVar(String name, int length)
    {
	this.name = name;
	this.length = length;
    }


    /**
     * Return the rank of this variable.
     */
    public int
    getRank()
    {
	return 1;
    }


    /**
     * Return the shape of this variable.
     */
    public int[]
    getLengths()
    {
	return new int[] = {length};
    }


    /**
     * Indicate if this variable is double.
     */
    public boolean
    isDouble()
    {
	return true;
    }


    /**
     * Return the dimensions of this variable.
     */
    public NcDim[]
    getDimensions()
    {
	int			rank = var.getRank();
	NcDim[]			dims = new NcDim[rank];
	DimensionIterator	iter = var.getDimensionIterator();

	for (int i = 0; i < rank; ++i)
	    dims[i] = new NcDim(iter.next(), file);

	return dims;
    }


    /**
     * Return the VisAD math type of this variable.
     */
    public abstract MathType
    getMathType()
	throws VisADException;


    /**
     * Indicate whether or not this variable is the same as another.
     */
    public boolean
    equals(Object that)
    {
	return equals((NcVar)that);
    }


    /**
     * Indicate whether or not this variable is the same as another.
     */
    public boolean
    equals(NcVar that)
    {
	return name.equals(that.name);
    }


    /**
     * Return the hash code of this variable.
     */
    public int
    hashCode()
    {
	return name.hashCode();
    }


    /**
     * Return the name of this variable.
     */
    public String
    getName()
    {
	return name;
    }


    /**
     * Convert this variable to a string.
     */
    public String
    toString()
    {
	return getName();
    }


    /**
     * Indicate whether or not this variable is a co-ordinate variable.
     */
    public boolean
    isCoordinateVariable()
    {
	NcDim[]	dims = getDimensions();

	for (int i = 0; i < dims.length; ++i)
	    if (name.equals(dims[i].getName()))
		return true;

	return false;
    }


    /**
     * Return the values of this variable.
     */
    public Object
    getValues()
	throws IOException, BadFormException
    {
	int		rank = var.getRank();
	int		ioRank = getIORank();
	int[]		shape = var.getLengths();
	Object		values = createValues();
	Unraveler	unraveler = getUnraveler(values);
	int[]		origin = new int[rank];

	for (int i = 0; i < origin.length; ++i)
	    origin[i] = 0;

	recurse(0, ioRank, origin, unraveler, shape);

	vet(values);

	unpack(values);

	return values;
    }


    /**
     * Recursively traverse the I/O points of the array.
     */
    protected void
    recurse(int idim, int ioRank, int[] origin, Unraveler unraveler, 
	    int[] shape)
	throws IOException
    {
	if (idim == ioRank)
	    unraveler.unravel(origin);
	else
	{
	    for (int i = 0; i < shape[idim]; ++i)
	    {
		recurse(idim+1, ioRank, origin, unraveler, shape);
		origin[idim]++;
	    }
	    origin[idim] = 0;
	}
    }


    /**
     * Return the rank of the I/O vector.
     */
    protected abstract int
    getIORank();


    /**
     * Create the buffer to hold the values.
     */
    protected abstract Object
    createValues();


    /**
     * Return the appropriate unraveler for the nested, netCDF array.
     */
    protected abstract Unraveler
    getUnraveler(Object values)
	throws BadFormException;


    /**
     * Vet the values.
     */
    protected abstract void
    vet(Object values);


    /**
     * Unpack the values.
     */
    protected abstract void
    unpack(Object values);


    /**
     * Abstract inner class for unraveling a nested, netCDF array.
     */
    public abstract class
    Unraveler
    {
	/**
	 * The netCDF input array
	 */
	protected final	ArrayInput	input;

	/**
	 * The index of the location in the value buffer for the next value.
	 */
	protected int			next = 0;

	/**
	 * The "shape" vector for the I/O transfer.
	 */
	protected int[]			ioShape;


	/**
	 * Construct.
	 */
	protected
	Unraveler()
	    throws BadFormException
	{
	    input = file.getArrayInput(name);

	    if (input == null)
		throw new BadFormException("variable \"" + name + 
		    "\" doesn't exist");
	}


	/**
	 * Unravel a netCDF variable beginning at an I/O point.
	 */
	public void
	unravel(int[] origin)
	    throws IOException
	{
	    Object	array = input.read(origin, ioShape);

	    for (int i = 0; i < ioShape.length-1; ++i)
		array = ((Object[])array)[0];

	    // System.out.println(this.getClass().getName() + 
		// ".unravel(): next = " + next);

	    copy(array);
	}


	/**
	 * Copy a 1-D vector of values into the value buffer.
	 */
	protected abstract void
	copy(Object array);
    }
}
