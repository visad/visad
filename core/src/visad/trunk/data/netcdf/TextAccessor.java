/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TextAccessor.java,v 1.1 1998-03-11 16:21:54 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Text;


/**
 * Class for accessing data in a VisAD Text that's been adapted to a 
 * netCDF API.
 */
class
TextAccessor
    extends	DataAccessor
{
    /**
     * The missing-value character.
     */
    protected final Character		space = new Character(' ');


    /**
     * Construct from a netCDF Dimension and an outer VisADAccessor.
     */
    protected
    TextAccessor(Dimension charDim, VisADAccessor outerAccessor)
    {
	super(new Dimension[] {charDim}, outerAccessor);
    }


    /**
     * Return a datum given the split, netCDF indexes.
     */
    protected Object
    get()
	throws IOException, StringIndexOutOfBoundsException
    {
	return new Character(((Text)outerAccessor.get(outerIndexes)).
	    getValue().charAt(localIndexes[0]));
    }
}
