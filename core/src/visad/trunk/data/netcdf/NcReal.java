package visad.data.netcdf;


import ucar.netcdf.Variable;
import ucar.netcdf.Attribute;
import visad.data.BadFormException;
import visad.MathType;
import visad.RealType;
import visad.Unit;
import visad.VisADException;


/**
 * Abstract adaptor/decorator class for netCDF floating-point variables.
 */
public abstract class
NcReal
    extends NcNumber
{
    /**
     * Construct.
     */
    public
    NcReal(Variable var, NcFile file)
    {
	super(var, file);
    }


    /**
     * Construct.
     */
    public
    NcReal(String name, Class type, NcDim[] dims)
    {
	super(name, type, dims);
    }


    /**
     * Construct.
     */
    public
    NcReal(String name, Class type, NcDim[] dims, Unit unit)
    {
	super(name, type, dims, unit);
    }


    /**
     * Return the default fill value.
     */
    protected double
    getDefaultFillValue()
    {
	return 9.9692099683868690e+36;
    }


    /**
     * Return the default minimum value.
     */
     protected double
     getDefaultValidMin(double fillValue)
     {
	return fillValue < 0
		    ? fillValue/2
		    : -Double.MAX_VALUE;
     }


    /**
     * Return the default maximum value.
     */
     protected double
     getDefaultValidMax(double fillValue)
     {
	return fillValue > 0
		    ? fillValue/2
		    : Double.MAX_VALUE;
     }
}
