package visad.data.netcdf;


import ucar.netcdf.Variable;
import ucar.netcdf.Attribute;
import visad.data.BadFormException;
import visad.MathType;
import visad.RealType;
import visad.VisADException;


/**
 * Abstract adaptor/decorator class for netCDF integer variables.
 */
public abstract class
NcInteger
    extends NcNumber
{
    /**
     * Construct.
     */
    public
    NcInteger(Variable var, NcFile file)
    {
	super(var, file);
    }


    /**
     * Construct.
     */
    public
    NcInteger(String name, Class type, NcDim[] dims)
    {
	super(name, type, dims);
    }


    /**
     * Return the default, minimum valid value.
     */
     protected double
     getDefaultValidMin(double fillValue)
     {
	return fillValue < 0
		    ? fillValue + 1
		    : getMinValue();
     }


    /**
     * Return the default, maximum valid value.
     */
     protected double
     getDefaultValidMax(double fillValue)
     {
	return fillValue > 0
		    ? fillValue - 1
		    : getMaxValue();
     }


     /**
      * Return the minimum possible value.
      */
    protected abstract double
    getMinValue();


     /**
      * Return the maximum possible value.
      */
    protected abstract double
    getMaxValue();
}
