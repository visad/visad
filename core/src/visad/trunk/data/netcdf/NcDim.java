package visad.data.netcdf;


import ucar.netcdf.Dimension;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.RealType;
import visad.VisADException;


/**
 * Decorator class for a netCDF dimension.
 */
class
NcDim
    extends	Dimension
{
    /**
     * Factory method for constructing the right type of dimension decorator.
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
     * @precondition	The given dimension is non-null.
     */
    protected
    NcDim(Dimension dim)
    {
	super(dim.getName(), dim.getLength());
    }


    /**
     * Return the VisAD MathType for this dimension.
     */
    RealType
    getMathType()
	throws VisADException
    {
	RealType	mathType = RealType.getRealTypeByName(getName());

	// TODO: support "units" attribute
	if (mathType == null)
	    mathType = new RealType(getName(), null, null);
	
	return mathType;
    }


    /**
     * Indicate whether or not this dimension is the same as another.
     */
    boolean
    equals(NcDim that)
    {
	return getName().equals(that.getName());
    }


    /**
     * Indicate whether or not this dimension is temporal in nature.
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
     */
    public int
    hashCode()
    {
	return getName().hashCode();
    }


    /**
     * Convert this dimension to a string.
     *
     * @deprecated
     */
    public String
    toString()
    {
	return getName();
    }


    /**
     * Return the co-ordinate variable associated with this dimension.
     */
    ImportVar
    getCoordVar()
    {
	return null;
    }
}


/**
 * Decorator class for a netCDF dimension that has a co-ordinate variable.
 */
class
NcCoordDim
    extends	NcDim
{
    /**
     * The associated coordinate variable.
     */
    protected final ImportVar	coordVar;


    /**
     * Construct from a netCDF dimension and dataset.  Protected to ensure
     * use of the NcDim factory method.
     *
     * @precondition	The dimension has a co-ordinate variable in the
     *			dataset.
     */
    protected
    NcCoordDim(Dimension dim, Netcdf netcdf)
	throws VisADException
    {
	super(dim);
	coordVar = ImportVar.create(netcdf.get(dim.getName()), netcdf);
    }


    /**
     * Indicate whether or not this dimension is temporal in nature.
     */
    boolean
    isTime()
    {
	return super.isTime() || coordVar.isTime();
    }


    /**
     * Return the co-ordinate variable associated with this dimension.
     */
    ImportVar
    getCoordVar()
    {
	return coordVar;
    }
}
