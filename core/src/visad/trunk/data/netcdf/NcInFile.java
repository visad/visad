package visad.data.netcdf;


import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import ucar.netcdf.ArrayInput;
import ucar.netcdf.Dimension;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.DimensionSet;
import ucar.netcdf.NetcdfV1File;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import visad.data.BadFormException;


/**
 * A class that adapts the behavior of an existing netCDF file for the 
 * purposes of this package.
 */
public class
NcInFile
    extends	NcFile
{
    /**
     * Construct.
     */
    public
    NcInFile(String path)
	throws BadFormException, IOException
    {
	super(path);

	try
	{
	    file = new NetcdfV1File(path, /*readonly=*/ true);
	}
	catch (IllegalArgumentException e)
	{
	    close();
	    throw new BadFormException("\"" + path + "\" is not a netCDF file");
	}
	catch (IOException e)
	{
	    close();
	    throw e;
	}

	dimSet = file.getDimensions();
	numDims = dimSet.size();
	numVars = 0;
	for (VariableIterator iter = file.iterator(); iter.hasNext(); )
	{
	    Variable	var = iter.next();

	    // TODO: support textual variables
	    if (!var.getComponentType().equals(Character.TYPE))
		++numVars;
	}
    }
}
