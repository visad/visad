/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcScalar.java,v 1.1 1998-03-20 20:56:59 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.Data;
import visad.DataImpl;
import visad.MathType;
import visad.Real;
import visad.RealTupleType;
import visad.RealType;
import visad.Scalar;
import visad.Text;
import visad.TextType;
import visad.Tuple;
import visad.TupleType;
import visad.VisADException;


/**
 * The NcScalar class adapts scalars in a netCDF dataset to a VisAD API.  
 * A scalar can be a single datum or a collection of data defined over the
 * rank 0 domain (i.e. a VisAD Tuple).
 */
abstract class
NcScalar
    extends	NcData
{
    protected
    NcScalar()
    {
    }

    /**
     * Factory method for constructing an NcScalar from netCDF variables.
     *
     * @parm vars	The netCDF variables defined over the same, rank-0, 
     *			domain.
     * @precondition	The rank of every variable is zero.
     * @precondition	<code>vars.length >= 1</code>.
     * @return		The NcData corresponding to <code>vars</code>.
     */
    static NcData
    newNcScalar(NcVar[] vars)
	throws VisADException, IOException
    {
	return vars.length == 1
		    ? (NcData)vars[0]
		    : (NcData)new NcTuple(vars);
    }
}
