/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SingleSoundingImpl.java,v 1.1 1998-11-03 22:27:35 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.ScalarType;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for atmospheric soundings of a single parameter.
 * Instances have their own local data.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public class
SingleSoundingImpl
    extends	SingleSounding
{
    /**
     * Constructs from a range type, domain set and range unit.
     * The data will be missing.
     *
     * @param rangeType		The type of the range.
     * @param domainSet		The domain set of the function.
     * @param rangeUnit		The unit for the range values.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    SingleSoundingImpl(MathType rangeType, Set domainSet, Unit rangeUnit)
	throws VisADException
    {
	super(rangeType, domainSet, rangeUnit);
    }


    /**
     * Constructs from a range type, domain set and range units.
     * The data will be missing.
     *
     * @param rangeType         The type of the range.
     * @param domainSet		The domain set of the function.
     * @param rangeUnits	The units for the range values.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    SingleSoundingImpl(MathType rangeType, Set domainSet, Unit[] rangeUnits)
	throws VisADException
    {
	super(rangeType, domainSet, rangeUnits);
    }


    /**
     * Constructs from a range type, FlatField, and a component index.
     *
     * @param rangeType		The type of the range.
     * @param field		The VisAD FlatField.
     * @param index		The range-index of the component.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    protected
    SingleSoundingImpl(MathType rangeType, FlatField field, int index)
	throws VisADException, RemoteException
    {
	this(
	    rangeType,
	    field.getDomainSet(),
	    getDefaultUnits(field, index));

	double[][]	values = field.extract(index).getValues();

	setSamples(values, /*copy=*/false);
    }
}
