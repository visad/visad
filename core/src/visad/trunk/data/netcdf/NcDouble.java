package visad.data.netcdf;


import ucar.netcdf.Variable;
import visad.data.BadFormException;


/**
 * Adaptor/decorator class for netCDF double variables.
 */
public class
NcDouble
    extends NcReal
{
    /**
     * Construct.
     */
    public
    NcDouble(Variable var, NcFile file)
    {
	super(var, file);
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
     * Return an unraveler.
     */
    public Unraveler
    getUnraveler(Object values)
	throws BadFormException
    {
	return new DoubleUnraveler(values);
    }


    /**
     * Inner class for unraveling an arithmetic nested, netCDF,
     * double array.
     */
    public class
    DoubleUnraveler
	extends NumberUnraveler
    {
        /**
         * Construct.
         */
        protected
        DoubleUnraveler(Object values)
	    throws BadFormException
        {
	    super(values);
	}


	/**
	 * Copy values into the value buffer.
	 */
	protected void
	copy(Object array)
	{
	    double[]	vals = (double[])array;

	    for (int i = 0; i < vals.length; ++i)
		buf[next++] = vals[i];
	}
    }
}
