package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.ArrayInput;
import ucar.netcdf.Dimension;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.DimensionSet;
import ucar.netcdf.NetcdfV1File;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;


/**
 * A class that adapts the behavior of a netCDF file for the purposes 
 * of this package.
 */
public abstract class
NcFile
{
    /**
     * The pathname of the netCDF file
     */
    protected final String 		path;

    /**
     * The netCDF file
     */
    protected NetcdfV1File		file = null;

    /**
     * The number of variables in the file
     */
    protected int			numVars;

    /**
     * The number of dimensions in the file
     */
    protected int			numDims;

    /**
     * The set of dimensions.
     * Effectively "final".
     */
    protected DimensionSet		dimSet;


    /**
     * Construct.
     */
    NcFile(String path)
    {
	this.path = path;
    }


    /**
     * Return the pathname.
     */
    public String
    getPath()
    {
	return path;
    }


    /**
     * Return the hashcode.
     */
    public int
    hashCode()
    {
	return path.hashCode();
    }


    /**
     * Indicate whether or not this netCDF file is the same as another.
     */
    public boolean
    equals(NcFile that)
    {
	return path.equals(that.path);
    }


    /**
     * Return the number of variables in the netCDF file.
     */
    public int
    numVariables()
    {
	return numVars;
    }


    /**
     * Return the number of dimensions in the netCDF file.
     */
    public int
    numDimensions()
    {
	return numDims;
    }


    /**
     * Return the dimensions of the netCDF file.
     */
    public NcDim[]
    getDimensions()
    {
	NcDim[]			dims = new NcDim[numDims];
	DimensionIterator	iter = file.getDimensions().iterator();

	for (int i = 0; i < dims.length; ++i)
	    dims[i] = new NcDim(iter.next(), this);

	return dims;
    }


    /**
     * Return the variables of the netCDF file.
     */
    public NcVar[]
    getVariables()
    {
	NcVar[]			vars = new NcVar[numVars];
	int			ivar = 0;

	for (VariableIterator iter = file.iterator(); iter.hasNext(); )
	{
	    Variable	var = iter.next();

	    // TODO: support textual variables
	    if (!var.getComponentType().equals(Character.TYPE))
		vars[ivar++] = NcVar.instantiate(var, this);
	}

	return vars;
    }


    /**
     * Return the named dimension.
     */
    public NcDim
    getDimension(String name)
    {
	Dimension	dim = dimSet.get(name);

	return dim == null
		? null
		: new NcDim(dim, this);
    }


    /**
     * Return the named variable.
     */
    public NcVar
    getVariable(String name)
    {
	Variable	var = file.get(name);

	return var == null
		? null
		: var.getComponentType().equals(Character.TYPE)
		    // TODO: support textual variables
		    ? null
		    : NcVar.instantiate(var, this);
    }


    /**
     * Return an input object for a variable.
     */
    public ArrayInput
    getArrayInput(String name)
    {
	ArrayInput	input;
	Variable	var = file.get(name);

	// TODO: support textual variables
	if (var.getComponentType().equals(Character.TYPE))
	    input = null;
	else
	    input = file.getArrayInput(name);

	return input;
    }


    /**
     * Close a netCDF file.
     */
    public void
    close()
	throws IOException
    {
	if (file != null)
	{
	    file.close();
	    file = null;
	}
    }
}
