package visad.data.netcdf;


import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import ucar.netcdf.NetcdfFile;
import ucar.netcdf.Schema;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import visad.Data;
import visad.DataImpl;
import visad.UnimplementedException;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.FormNode;


/**
 * A moderately stupid implementation of a netCDF data form for the
 * storage of persistent data objects on local disk.
 */
public class
Plain
    extends NetCDF
{
    /**
     * Construct a default, netCDF data form.
     */
    public
    Plain()
    {
	super("Plain");
    }


    /**
     * Save a VisAD data object in this form.
     *
     * @exception BadFormException	netCDF can't handle data object
     * @exception VisADException	Couldn't create necessary VisAD object
     * @exception IOException		I/O error
     * @exception RemoteException	Remote execution error
     * @exception UnimplementedException	Not yet!
     */
    public void
    save(String path, Data data, boolean replace)
	throws BadFormException, IOException, RemoteException, VisADException,
	    UnimplementedException
    {
	VisADAdapter	adapter = new VisADAdapter(data);
	Schema		schema = new Schema(adapter);
	NetcdfFile	file = new NetcdfFile(path, replace, /*fill=*/false,
					schema);

	try
	{
	    VariableIterator	iter = file.iterator();

	    while (iter.hasNext())
	    {
		Variable	outVar = iter.next();
		Variable	inVar = adapter.get(outVar.getName());
		int		rank = outVar.getRank();
		int[]		origin = new int[rank];

		for (int i = 0; i < rank; ++i)
		    origin[i] = 0;

		outVar.set(origin, inVar);
	    }
	}
	finally
	{
	    file.close();
	}
    }


    /**
     * Add data to an existing data object.
     *
     * @exception BadFormException	netCDF can't handle data object
     */
    public void
    add(String id, Data data, boolean replace)
	throws BadFormException
    {
    }


    /**
     * Open an existing netCDF file and return a VisAD data object.
     */
    public DataImpl
    open(String path)
	throws BadFormException, IOException, VisADException
    {
	DataImpl	data;
	NetcdfFile	file = new NetcdfFile(path, /*readonly=*/true);

	try
	{
	    data = new NetcdfAdapter(file).getData();
	}
	finally
	{
	    file.close();
	}

	return data;
    }


    /**
     * Open a URL.
     */
    public DataImpl
    open (URL url)
	throws UnimplementedException
    {
	throw new UnimplementedException("open(URL)");
    }


    /**
     * Return the data forms that are compatible with a data object.
     */
    public FormNode
    getForms(Data data)
    {
	return this;		// TODO
    }


    /**
     * Test this class.
     *
     * @exception Exception	Something went wrong.
     */
    public static void main(String[] args)
	throws Exception
    {
	String	inPath;
	String	outPath = "plain.nc";

	if (args.length == 0)
	    inPath = "big.nc";
	else
	    inPath = args[0];

	Plain	plain = new Plain();

	System.out.println("Opening netCDF dataset \"" + inPath + "\"");

	Data	data = plain.open(inPath);

	// System.out.println("Data:\n" + data.longString());
	// System.out.println("data.getType().toString():\n" +
	    // data.getType());

	System.out.println("Writing netCDF dataset \"" + outPath + "\"");

	plain.save(outPath, data, /*replace=*/true);
    }
}
