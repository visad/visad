package visad.data.netcdf;


import visad.Data;
import visad.FlatField;
import visad.Tuple;
import visad.UnimplementedException;
import visad.VisADException;
import visad.data.BadFormException;


/**
 * Abstract superclass for decorating a VisAD data object with 
 * inquiries about netCDF dimensions and variables.
 */
abstract class
DataAdapter
{
    /**
     * Default constructor for use by subclasses.  Protected to ensure 
     * use of the create() factory method.
     */
    protected
    DataAdapter()
    {
    }


    /**
     * Factory method for creating the proper instance.
     */
    static DataAdapter
    create(Data data, DimSet dimSet, VarSet varSet)
	throws BadFormException, UnimplementedException, VisADException
    {
	DataAdapter	adapter;

	// TODO: Handle a Field with a time domain and a FlatField or Tuple
	// range

	if (data instanceof FlatField)
	    adapter = new FlatFieldAdapter((FlatField)data, dimSet, varSet);
	else
	if (data instanceof Tuple)
	    adapter = new TupleAdapter((Tuple)data, dimSet, varSet);
	else
	    throw new UnimplementedException("Can't adapt VisAD type");

	return adapter;
    }
}
