/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.io.IOException;

/**
 *  Interface for multidimensional arrays.
 *  Includes introspection by extending MultiArrayInfo and
 *  data acccess by extending Accessor.
 *  <p>
 *  These are more general and abstract than Netcdf Variables.
 *  Netcdf Variables implement this, but more general objects,
 *  such as java arrays, can be simply wrapped to provide
 *  this interface.
 *
 * @see MultiArrayInfo
 * @see Accessor
 * @see ucar.netcdf.Variable
 * @see MultiArrayImpl
 * @see ArrayMultiArray
 * @see ScalarMultiArray
 * @see MultiArrayProxy
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public interface
MultiArray
	extends MultiArrayInfo, Accessor
{
	/* The super interfaces say it all */
}
