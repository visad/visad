package visad.data.netcdf;


import visad.data.BadFormException;
import ucar.netcdf.Variable;


/**
 * Adaptor/decorator class for netCDF short variables.
 */
public class
NcShort
    extends NcInteger
{
    /**
     * Construct.
     */
    public
    NcShort(Variable var, NcFile file)
    {
	super(var, file);
    }


    /**
     * Indicate if this variable is short.
     */
    public boolean
    isShort()
    {
	return true;
    }


    /**
     * Return the default fill value.
     */
    protected double
    getDefaultFillValue()
    {
	return -32767;
    }


     /**
      * Return the minimum possible value.
      */
    protected double
    getMinValue()
    {
	return Short.MIN_VALUE;
    }


     /**
      * Return the maximum possible value.
      */
    protected double
    getMaxValue()
    {
	return Short.MAX_VALUE;
    }


    /**
     * Return an unraveler.
     */
    public Unraveler
    getUnraveler(Object values)
	throws BadFormException
    {
	return new ShortUnraveler(values);
    }


    /**
     * Inner class for unraveling an arithmetic nested, netCDF,
     * short array.
     */
    public class
    ShortUnraveler
	extends NumberUnraveler
    {
        /**
         * Construct.
         */
        protected
        ShortUnraveler(Object values)
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
	    short[]	vals = (short[])array;

	    for (int i = 0; i < vals.length; ++i)
		buf[next++] = vals[i];
	}
    }
}
