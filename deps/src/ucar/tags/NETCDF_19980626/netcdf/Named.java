/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;

/**
 * An interface for Named objects in netcdf.
 * <p>
 * It supports retrieval of the name.
 * In the classes which implement this interface,
 * the private name data is final; the name is the constant
 * for the lifetime of an object.
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */

interface Named {
    /**
     * returns the name which identifies this thing.
     * @return String which identifies this thing.
     */
    public String getName();
}
