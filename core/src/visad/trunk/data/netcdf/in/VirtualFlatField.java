/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualFlatField.java,v 1.2 2000-04-26 15:45:21 dglo Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.CoordinateSystem;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.RealTupleType;
import visad.SampledSet;
import visad.Set;
import visad.Unit;
import visad.VisADException;


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
     * @param type		The MathType of the FlatField.
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
	VirtualTuple	rangeTuple = getRangeTuple();
	int		componentCount = rangeTuple.size();
	Set[]		rangeSets = new Set[componentCount];
	Unit[]		rangeUnits = new Unit[componentCount];

	for (int i = 0; i < componentCount; ++i)
	{
	    VirtualScalar	component =
		(VirtualScalar)rangeTuple.get(i);

	    rangeSets[i] = component.getRangeSet();
	    rangeUnits[i] = component.getUnit();
	}

	FlatField	field = new FlatField(
	    getFunctionType(),
	    getDomainSet(),
	    (CoordinateSystem)null,
	    rangeSets,
	    rangeUnits);

	double[][]	values = new double[componentCount][];

	for (int i = 0; i < componentCount; ++i)
	{
	    VirtualScalar	component =
		(VirtualScalar)rangeTuple.get(i);

	    values[i] = component.getDoubles(context);
	}

	field.setSamples(values, /*copy=*/false);

	return field;
    }
}
