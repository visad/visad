/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TextAccessor.java,v 1.3 2000-04-26 15:45:26 dglo Exp $
 */

package visad.data.netcdf.out;

import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Text;


/**
 * The TextAccessor class accesses character data in a VisAD Text that's
 * being adapted to a netCDF API.  It's useful for exporting data to a netCDF
 * dataset.
 */
class
TextAccessor
    extends	DataAccessor
{
    /**
     * The missing-value character.
     */
    private final Character		space = new Character(' ');


    /**
     * Construct from a netCDF Dimension and an outer VisADAccessor.
     *
     * @param charDim		The netCDF character dimension (i.e. innermost
     *				netCDF dimension).
     * @param outerAccessor	The DataAccessor for the encompasing VisAD
     *				data object.  Returns Text objects.
     */
    protected
    TextAccessor(Dimension charDim, VisADAccessor outerAccessor)
    {
	super(new Dimension[] {charDim}, outerAccessor);
    }


    /**
     * Return a datum given the split, netCDF indexes.
     *
     * @return		The datum at the position given by
     *			<code>localIndexes</code> and
     *			<code>outerIndexes</code>.
     * @exception IOException
     *			Data access I/O failure.
     * @exception StringIndexOutOfBoundsException
     *			The character position given by
     *			<code>localIndexes</code> was out-of-bounds.
     */
    protected Object
    get()
	throws IOException, StringIndexOutOfBoundsException
    {
	return new Character(((Text)outerAccessor.get(outerIndexes)).
	    getValue().charAt(localIndexes[0]));
    }
}
