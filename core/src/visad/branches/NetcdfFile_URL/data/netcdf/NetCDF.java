/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetCDF.java,v 1.4 1998-03-12 22:03:12 steve Exp $
 */

package visad.data.netcdf;

import java.io.IOException;
import visad.data.BadFormException;
import visad.data.Form;
import visad.Data;
import visad.DataImpl;
import visad.VisADException;


/**
 * The NetCDF class provides an abstract class for the family of netCDF
 * data forms for files in a local directory.
 */
public abstract class
NetCDF
    extends Form
{
    /**
     * Construct a netCDF data form.
     *
     * @param name	The name for the family of netCDF data forms.
     */
    public
    NetCDF(String name)
    {
	super(name);
    }


    /**
     * Open an existing file.
     *
     * @param path			The pathname of the file.
     * @exception BadFormException	netCDF couldn't handle VisAD object.
     * @exception VisADException	Couldn't create necessary VisAD object.
     * @exception IOException		I/O error.
     */
    public abstract DataImpl
    open(String path)
	throws BadFormException, IOException, VisADException;
}
