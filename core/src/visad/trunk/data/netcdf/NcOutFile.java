package visad.data.netcdf;


import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;
import ucar.netcdf.ArrayInput;
import ucar.netcdf.Dimension;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.DimensionSet;
import ucar.netcdf.NetcdfV1File;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import visad.data.BadFormException;


/**
 * A class that adapts the behavior of a new netCDF file for the 
 * purposes of this package.
 */
public class
NcOutFile
    extends	NcFile
{
    /**
     * Whether or not we're in define mode.
     */
    protected boolean		inDefineMode = true;

    /**
     * Whether or not to overwrite a previously existing file with the same
     * name.
     */
    protected final boolean	clobber;

    /**
     * The dimensions of the netCDF file.
     */
    protected final Vector	dims = new Vector(5);

    /**
     * The variables of the netCDF file.
     */
    protected final Vector	vars = new Vector(5);


    /**
     * Construct.
     */
    public
    NcOutFile(String path, boolean clobber)
    {
	super(path);
	this.clobber = clobber;
    }


    /**
     * Define a netCDF dimension.
     */
    public void
    define(NcDim dim)
    {
	dims.addElement(dim);
    }


    /**
     * Define a netCDF variable.
     */
    public void
    define(NcVar var)
    {
	vars.addElement(var);
    }
}
