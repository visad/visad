package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.GriddedSet;
import visad.Unit;
import visad.data.BadFormException;




/*
 * Class for independent variables.  An independent variable is a netCDF
 * variable that has been created because it was necessary to define
 * the netCDF variables in terms of an "index" co-ordinate.  This can
 * happen, for example, in a Fleld with a domain that's a GriddedSet
 * but not a LinearSet.
 */
class
IndependentVar
    extends ExportVar
{
    /**
     * The dimension index of the "variable" in the domain.
     */
    protected final int		idim;


    /**
     * The sampling domain set.
     */
    protected final GriddedSet	set;


    /**
     * Construct from broken-out information.
     */
    protected
    IndependentVar(String name, Dimension dim, Unit unit,
	    GriddedSet set, int idim)
	throws BadFormException
    {
	super(name, Float.TYPE, new Dimension[] {dim}, unit);
	this.set = set;
	this.idim = idim;
    }


    /**
     * Return the fill-value object for an independent variable.  This is
     * necessarily null because VisAD domain sample sets don't have missing
     * values.
     *
     * @param type	netCDF type (e.g. <code>Character.TYPE</code>, 
     *			<code>Float.TYPE</code>).
     * @return		The default fill-value object for the given netCDF
     *			type.
     */
    Number
    getFillValueNumber(Class type)
    {
	return null;
    }


    /**
     * Return an array element identified by position.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	int	index = indexes[indexes.length-1];

	try
	{
	    return getExportObject(
		set.indexToValue(new int[] {index})[idim][0]);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}
