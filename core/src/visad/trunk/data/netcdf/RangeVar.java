package visad.data.netcdf;


import ucar.netcdf.Dimension;
import visad.Field;
import visad.Set;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;


/**
 * Superclass for a netCDF variable that exists somewhere in the 
 * range of a Field.
 */
abstract class
RangeVar
    extends	ExportVar
{
    /**
     * The field in which the data resides.
     */
    protected final Field	field;

    /**
     * The shape of the netCDF variable in netCDF order (i.e. big
     * endian).
     */
    protected /*final*/ int[]	shape;


    /**
     * Construct from broken-out information.
     */
    protected
    RangeVar(String name, Dimension[] dims, Unit unit, Field field, Set set)
	throws BadFormException, VisADException
    {
	super(name, getJavaClass(set), dims, unit);

	this.field = field;

	shape = new int[dims.length];

	for (int idim = 0; idim < dims.length; ++idim)
	    shape[idim] = dims[dims.length-1-idim].getLength();
    }


    /**
     * Convert a netCDF index vector to a VisAD scalar index.
     *
     * @param netcdfIndexes	Vector of netCDF indexes; innermost dimension
     *			last.
     * @return		VisAD index corresponding to netCDF index.
     * @precondition	<code>netcdfIndex</code> != null && 
     * 			<code>netcdfIndex.length</code> == domain rank
     *			&& indexed point lies within the sampling set.
     * @postcondition	Returned VisAD index lies within sampling set.
     */
    protected int
    visadIndex(int[] netcdfIndexes)
    {
	int	visadIndex = netcdfIndexes[0];

	for (int i = 1; i < shape.length; ++i)
	    visadIndex = visadIndex * shape[i] + netcdfIndexes[i];

	return visadIndex;
    }
}
