/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetCDF.java,v 1.6 2001-11-27 22:29:32 dglo Exp $
 */

package visad.data.netcdf;

import java.io.IOException;
import visad.data.BadFormException;
import visad.data.Form;
import visad.data.FormFileInformer;
import visad.DataImpl;
import visad.VisADException;


/**
 * The NetCDF class provides an abstract class for the family of netCDF
 * data forms for files in a local directory.
 */
public abstract class
NetCDF
    extends	Form
    implements	FormFileInformer
{
    private final static String		SUFFIX = "nc";
    private final static String		PERIOD_SUFFIX = "." + SUFFIX;

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

/*
 * FormFileInformer method implementations:
 */

    /**
     * Indicates if a dataset specification is consistent with a netCDF dataset
     * specification.
     *
     * @param spec		A dataset specification.  NB: Not a URL.
     * @return			<code>true</code> if and only if the dataset
     *				specification is consistent with a netCDF
     *				dataset specification.
     */
    public boolean isThisType(String spec)
    {
	return spec.toLowerCase().endsWith(PERIOD_SUFFIX);
    }

    /**
     * Indicates if a given block of bytes is the start of a netCDF dataset.
     *
     * @param block		A block of data.
     * @return			True if and only if the given block of bytes is
     *				the start of a netCDF dataset.
     */
    public boolean isThisType(byte[] block)
    {
	return block[0] == 'C' && block[1] == 'D' && block[2] == 'F';
    }

    /**
     * Returns the path-component suffixes that identifies a dataset
     * specification as being a netCDF dataset specification.  The suffixes
     * don't have a leading period.  The returned array can be safely modified.
     *
     * @return			A freshly-allocated array with the relevant 
     *				suffixes.
     */
    public String[] getDefaultSuffixes()
    {
	return new String[] {SUFFIX};
    }
}
