package visad.data.netcdf;


import java.io.IOException;
import visad.data.BadFormException;
import visad.data.Form;
import visad.Data;
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
     *
     * @exception BadFormException	netCDF couldn't handle VisAD object.
     * @exception VisADException	Couldn't create necessary VisAD object.
     * @exception IOException		I/O error.
     */
    public abstract DataImpl
    open(String path)
	throws BadFormException, IOException, VisADException;
}
