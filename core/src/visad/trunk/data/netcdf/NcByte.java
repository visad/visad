package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Variable;
import visad.data.BadFormException;


/**
 * Adaptor/decorator class for netCDF byte variables.
 */
public class
NcByte
    extends NcInteger
{
    /**
     * Construct.
     */
    public
    NcByte(Variable var, NcFile file)
    {
	super(var, file);
    }


    /**
     * Indicate whether or not value-vetting is the default.
     */
    protected boolean
    isVettingTheDefault()
    {
	return false;
    }


    /**
     * Return the default fill value.
     */
    protected double
    getDefaultFillValue()
    {
	return Double.MAX_VALUE;	// there is no default fill value
    }


    /**
     * Indicate if this variable is byte.
     */
    public boolean
    isByte()
    {
	return true;
    }


    /**
     * Return the default, minimum valid value.
     */
     protected double
     getDefaultValidMin(double fillValue)
     {
	return getMinValue();
     }


    /**
     * Return the default, maximum valid value.
     */
     protected double
     getDefaultValidMax(double fillValue)
     {
	return getMaxValue();
     }


     /**
      * Return the minimum possible value.
      */
    protected double
    getMinValue()
    {
	return Byte.MIN_VALUE;
    }


     /**
      * Return the maximum possible value.
      */
    protected double
    getMaxValue()
    {
	return Byte.MAX_VALUE;
    }


    /**
     * Return an unraveler.
     */
    public Unraveler
    getUnraveler(Object values)
	throws BadFormException
    {
	return new ByteUnraveler(values);
    }


    /**
     * Inner class for unraveling an arithmetic nested, netCDF,
     * byte array.
     */
    public class
    ByteUnraveler
	extends NumberUnraveler
    {
        /**
         * Construct.
         */
        protected
        ByteUnraveler(Object values)
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
	    byte[]	vals = (byte[])array;

	    for (int i = 0; i < vals.length; ++i)
		buf[next++] = vals[i];
	}
    }
}
