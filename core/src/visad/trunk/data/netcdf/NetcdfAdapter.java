package visad.data.netcdf;


import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfFile;
import ucar.netcdf.VariableIterator;
import visad.DataImpl;
import visad.FieldImpl;
import visad.MathType;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.netcdf.NcFunction;
import visad.data.netcdf.NcNestedFunction;
import visad.data.netcdf.NcDim;
import visad.data.netcdf.NcVar;


/**
 * Class for adapting a netCDF dataset to a VisAD API.
 */
public class
NetcdfAdapter
{
    /**
     * The netCDF dataset.
     */
    protected final Netcdf	netcdf;

    /**
     * The netCDF functions in the netCDF datset.
     */
    protected final Dictionary	functionSet;

    /**
     * The outermost, netCDF data object.
     */
    protected final NcData	ncData;


    /**
     * Construct from a netCDF dataset.
     */
    NetcdfAdapter(Netcdf netcdf)
	throws VisADException, RemoteException, IOException
    {
	this.netcdf = netcdf;

	functionSet = setFunctionSet(netcdf);

	ncData = setOutermost(functionSet);
    }


    /**
     * Set the VisAD data objects in the netCDF dataset.
     */
    protected static Hashtable
    setFunctionSet(Netcdf netcdf)
	throws VisADException, BadFormException, IOException
    {
	DomainTable		domTable = new DomainTable(netcdf.size());

	VariableIterator	varIter = netcdf.iterator();
	while (varIter.hasNext())
	{
	    NcVar	var = NcVar.create(varIter.next(), netcdf);

	    // TODO: support scalars
	    if (!var.isText() && !var.isCoordinateVariable() &&
		var.getRank() > 0)
	    {
		domTable.put(var);
	    }
	}

	Hashtable	table = new Hashtable(netcdf.size());

	DomainTable.Enumeration	domEnum = domTable.getEnumeration();
	while (domEnum.hasMoreElements())
	{
	    Domain	domain = domEnum.nextElement();
	    NcDim[]	dims = domain.getDimensions();
	    NcFunction	function = (!dims[0].isTime() || domain.getRank() <= 2)
			    ? new NcFunction(domain.getVariables())
			    : new NcNestedFunction(domain.getVariables());

	    table.put(function.getMathType(), function);
	}

	return table;
    }


    /**
     * Set the outermost, netCDF data object.
     */
    protected static NcData
    setOutermost(Dictionary functionSet)
	throws VisADException, RemoteException
    {
	NcData	data;
	int	numFunctions = functionSet.size();

	if (numFunctions == 0)
	    data = null;
	else
	if (numFunctions == 1)
	{
	    Enumeration	enum = functionSet.elements();

	    data = (NcFunction)enum.nextElement();
	}
	else
	{
	    Enumeration		enum = functionSet.elements();
	    NcFunction[]	functions = new NcFunction[numFunctions];

	    for (int i = 0; i < numFunctions; ++i)
		functions[i] = (NcFunction)enum.nextElement();

	    data = new NcTuple(functions);
	}

	return data;
    }


    /**
     * Return the outermost, VisAD data object.
     */
    DataImpl
    getData()
	throws IOException, VisADException
    {
	return ncData.getData();
    }


    /**
     * Return the VisAD data object corresponding to the given MathType.
     *
     * @prerequisite	<code>type</code> is a node in 
     *			<code>getMathType()</code>'s return-value.
     */
    protected DataImpl
    getData(MathType type)
	throws IOException, VisADException
    {
	if (type.equals(ncData.getMathType()))
	    return getData();

	NcFunction	function = (NcFunction)functionSet.get(type);

	if (function == null)
	    return null;

	return function.getData();
    }


    /**
     * Test this class.
     *
     * @exception Exception	Something went wrong.
     */
    public static void
    main(String[] args)
	throws Exception
    {
	String[]	pathnames;

	if (args.length == 0)
	    pathnames = new String[] {"test.nc"};
	else
	    pathnames = args;

	for (int i = 0; i < pathnames.length; ++i)
	{
	    NetcdfFile		file = new NetcdfFile(pathnames[i], 
				    /*readonly=*/true);
	    NetcdfAdapter	adapter = new NetcdfAdapter(file);
	    DataImpl		data = adapter.getData();

	    //System.out.println("data.getType().toString():\n" +
		//data.getType());
	    System.out.println("data.toString():\n" + data);
	}
    }
}
