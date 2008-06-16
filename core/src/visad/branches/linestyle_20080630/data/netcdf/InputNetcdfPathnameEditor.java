/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InputNetcdfPathnameEditor.java,v 1.3 1998-06-29 19:47:12 visad Exp $
 */

package visad.data.netcdf;


public class
InputNetcdfPathnameEditor
    extends	InputPathnameEditor
{
    /**
     * Construct.
     */
    public
    InputNetcdfPathnameEditor()
    {
	super("*.nc", "dummy.nc");
    }
}
