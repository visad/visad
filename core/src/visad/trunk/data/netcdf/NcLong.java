package visad.data.netcdf;


import visad.data.BadFormException;
import ucar.netcdf.Variable;


/**
 * Adaptor/decorator class for netCDF "long" variables.
 */
public class
NcLong
    extends NcInteger
{
    /**
     * Construct.
     */
    public
    NcLong(Variable var, NcFile file)
    {
	super(var, file);
    }


    /**
     * Indicate if this variable is netCDF "long".
     */
    public boolean
    isLong()
    {
	return true;
    }


    /**
     * Return the default fill value.
     */
    protected double
    getDefaultFillValue()
    {
	return -2147483647;
    }


     /**
      * Return the minimum possible value.
      */
    protected double
    getMinValue()
    {
	return Integer.MIN_VALUE;
    }


     /**
      * Return the maximum possible value.
      */
    protected double
    getMaxValue()
    {
	return Integer.MAX_VALUE;
    }


    /**
     * Return an unraveler.
     */
    public Unraveler
    getUnraveler(Object values)
	throws BadFormException
    {
	return new LongUnraveler(values);
    }


    /**
     * Inner class for unraveling an arithmetic nested, netCDF,
     * integer array.
     */
    public class
    LongUnraveler
	extends NumberUnraveler
    {
        /**
         * Construct.
         */
        protected
        LongUnraveler(Object values)
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
	    int[]	vals = (int[])array;

	    for (int i = 0; i < vals.length; ++i)
		buf[next++] = vals[i];
	}
    }
}
