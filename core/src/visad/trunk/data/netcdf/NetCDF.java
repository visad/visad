package visad.data.netcdf;


import java.io.IOException;
import visad.data.BadFormException;
import visad.data.Form;
import visad.DataImpl;
import visad.VisADException;


/**
 * The family of netCDF data forms for files in a local directory.
 */
public abstract class
NetCDF
    extends Form
{
    /**
     * Construct a netCDF data form.
     */
    public
    NetCDF(String name)
    {
	super(name);
    }


    /**
     * Open an existing file.
     */
    public abstract DataImpl
    open(String path)
	throws BadFormException, IOException, VisADException;
}
