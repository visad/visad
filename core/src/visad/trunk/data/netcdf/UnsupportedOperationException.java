/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnsupportedOperationException.java,v 1.2 1998-02-23 15:58:31 steve Exp $
 */

package visad.data.netcdf;

class
UnsupportedOperationException
    extends NoSuchMethodError
{
    public
    UnsupportedOperationException()
    {
	super();
    }

    public
    UnsupportedOperationException(String msg)
    {
	super(msg);
    }
}
