/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InputNetcdfPathnameEditor.java,v 1.2 1998-06-26 18:39:20 visad Exp $
 */

package visad.data.netcdf;


public abstract class
InputNetcdfPathnameEditor
    extends	InputPathnameEditor
{
    /**
     * Construct.
     */
    public
    InputNetcdfPathnameEditor()
    {
	super("dummy.nc");
    }
}
