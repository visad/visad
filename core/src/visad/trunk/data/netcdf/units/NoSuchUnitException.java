package visad.data.netcdf.units;


import visad.VisADException;


/**
 * Exception thrown when a unit specification can't be parsed because of an
 * unknown unit.
 */
public class
NoSuchUnitException
    extends ParseException
{
    /**
     * Construct an exception with a message.
     */
    public NoSuchUnitException(String msg)
    {
	super(msg);
    }
}
