package visad.data.netcdf;


import java.util.Hashtable;
import visad.RealType;
import visad.VisADException;


/**
 * Class for managing the correspondence between netCDF dimensions and
 * VisAD RealTypes.
 */
public class
DimensionTable
{
    /**
     * Dimension-to-VisAD-RealType table.
     */
    protected Hashtable	table;


    /**
     * Construct.
     */
    public
    DimensionTable(int initialSize)
    {
	table = new Hashtable(initialSize);
    }


    /**
     * Add an entry to the table.
     */
    public RealType
    put(NcDim dim)
	throws VisADException
    {
	String		key = dim.getName();
	RealType	type = new RealType(key, null, null);

	return (RealType)table.put(key, type);
    }


    /**
     * Get an entry from the table.
     */
    public RealType
    getRealType(NcDim dim)
    {
	String		key = dim.getName();

	return (RealType)table.get(key);
    }


    /**
     * Get an entry from the table by name.
     */
    public RealType
    getRealType(String name)
    {
	return (RealType)table.get(name);
    }
}
