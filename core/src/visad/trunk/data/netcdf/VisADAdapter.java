package visad.data.netcdf;


import java.rmi.RemoteException;
import ucar.multiarray.Accessor;
import ucar.netcdf.AbstractNetcdf;
import ucar.netcdf.ProtoVariable;
import visad.Data;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.DataNode;


/**
 * Class for adapting a VisAD data object to the Netcdf interface.
 */
public class
VisADAdapter
    extends	AbstractNetcdf
{
    /**
     * Construct from a VisAD data object.
     */
    VisADAdapter(Data data)
	throws BadFormException, VisADException, RemoteException
    {
	/*
	 * Create a visitor for the VisAD data object.
	 */
	DataNode	node = DataNode.create(data);

	/*
	 * Define the netCDF variables.
	 */
	node.accept(new VarDefiner(this));
    }


    /**
     * Add a netCDF variable (called by VarDefiner).
     */
    void
    add(ExportVar var)
	throws BadFormException
    {
	try
	{
	    add(var.getProtoVariable(), (Accessor)var);
	}
	catch (Exception e)
	{
	    // Can't happen.  (Yeah.  Right. ;-)
	    throw new BadFormException("internal error");
	}
    }


    /**
     * Return an Accessor for a variable.
     *
     * This method should not be called -- so it always throws an error.
     */
    protected Accessor
    ioFactory(ProtoVariable protoVar)
    {
	throw new UnsupportedOperationException();
    }
}
