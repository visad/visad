package visad.data.netcdf;


import ucar.netcdf.Dimension;


/**
 * Decorator class for netCDF dimensions.
 */
public class
NcDim
{
    /**
     * The netCDF dimension:
     */
    protected final Dimension	dim;

    /**
     * The name of the dimension:
     */
    protected final String	name;

    /**
     * The netCDF file:
     */
    protected final NcFile	file;


    /**
     * Construct.
     */
    public
    NcDim(Dimension dim, NcFile file)
    {
	this.dim = dim;
	this.file = file;
	name = dim.getName();
    }


    /**
     * Construct.
     */
    public
    NcDim(String name, int length)
    {
	file = null;
	this.name = name;
	dim = new Dimension(name, length);
    }


    /**
     * Return the length of this dimension.
     */
    public int
    getLength()
    {
	return dim.getLength();
    }


    /**
     * Return the netCDF dimension.
     */
    Dimension
    getDimension()
    {
	return dim;
    }


    /**
     * Return the co-ordinate variable of the dimension.
     */
    NcVar
    getCoordinateVariable()
    {
	return file.getVariable(name);
    }


    /**
     * Indicate whether or not this dimension is the same as another.
     */
    public boolean
    equals(NcDim that)
    {
	return name.equals(that.name);
    }


    /**
     * Return the hash code of this dimension.
     */
    public int
    hashCode()
    {
	return name.hashCode();
    }


    /**
     * Return the name of this dimension.
     */
    public String
    getName()
    {
	return name;
    }


    /**
     * Convert this dimension to a string.
     */
    public String
    toString()
    {
	return getName();
    }
}
