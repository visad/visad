package visad.data.netcdf;


import ucar.netcdf.Variable;
import visad.Unit;
import visad.data.BadFormException;


/**
 * Adaptor/decorator class for netCDF float variables.
 */
public class
NcFloat
    extends NcReal
{
    /**
     * Construct.
     */
    public
    NcFloat(Variable var, NcFile file)
    {
	super(var, file);
    }


    /**
     * Construct.
     */
    public
    NcFloat(String name, NcDim[] dims)
    {
	super(name, Float.TYPE, dims);
    }


    /**
     * Construct.
     */
    public
    NcFloat(String name, NcDim[] dims, Unit unit)
    {
	super(name, Float.TYPE, dims, unit);
    }


    /**
     * Indicate if this variable is float.
     */
    public boolean
    isFloat()
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
	return new FloatUnraveler(values);
    }


    /**
     * Inner class for unraveling an arithmetic nested, netCDF,
     * float array.
     */
    public class
    FloatUnraveler
	extends NumberUnraveler
    {
        /**
         * Construct.
         */
        protected
        FloatUnraveler(Object values)
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
	    float[]	vals = (float[])array;

	    for (int i = 0; i < vals.length; ++i)
		buf[next++] = vals[i];
	}
    }
}
