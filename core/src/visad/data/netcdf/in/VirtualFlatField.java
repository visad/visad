/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualFlatField.java,v 1.3 2000-06-08 19:13:46 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.*;


/**
 * Provides support for a virtual VisAD FlatField.
 */
public class
VirtualFlatField
    extends	VirtualField
{
    /**
     * Constructs from a function type, domain set, and range tuple.
     *
     * @param functionType	The MathType of the FlatField.
     * @param domainSet		The domain sampling set of the FlatField.
     * @param rangeTuple	The range of the FlatField.
     */
    protected
    VirtualFlatField(FunctionType functionType, SampledSet domainSet,
	VirtualTuple rangeTuple)
    {
	super(functionType, domainSet, rangeTuple);
    }


    /**
     * Gets the VisAD data object corresponding to this virtual, data
     * object.
     *
     * @param context		The context in which the data is to be
     *				retrieved.
     * @return			The VisAD data object corresponding to this
     *				virtual, data object.
     * @throws VisADException	Couldn't created necessary VisAD object.
     * @throws InvalidContextException
     *				Invalid context.
     * @throws IOException	I/O failure.
     */
    public DataImpl
    getData(Context context)
	throws VisADException, IOException
    {
	return getDataFactory().newData(context, this);
    }


    /**
     * Clones this instance.
     *
     * @return			A (deep) clone of this instance.
     */
    public Object clone()
    {
	return
	    new VirtualFlatField(
		getFunctionType(),
		getDomainSet(),
		(VirtualTuple)getRangeTuple().clone());
    }
}
