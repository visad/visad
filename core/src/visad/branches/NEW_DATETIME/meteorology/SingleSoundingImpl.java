/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SingleSoundingImpl.java,v 1.3 1999-01-07 18:13:33 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.CoordinateSystem;
import visad.Data;
import visad.ErrorEstimate;
import visad.Field;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DSet;
import visad.GriddedSet;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.ScalarType;
import visad.Set;
import visad.SingletonSet;
import visad.TupleType;
import visad.TypeException;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for atmospheric soundings of a single parameter.
 * The single parameter may have more than one component, however (i.e.
 * the type of the range may be a RealTupleType).  Instances have their
 * own local data.
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


    /**
     * Gets the sounding value at a given pressure.
     * @param pressure		The pressure at which to get the
     *				sounding value.
     * @return			The value of the sounding at 
     *				<code>pressure</code>.
     * @throws TypeException	Range MathType is incompatible with SampledSet.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Data
    getValue(Real pressure)
	throws VisADException, RemoteException
    {
	return
	    resample(
		new SingletonSet(
		    new RealTuple(new Real[] {pressure}),
		    (CoordinateSystem)null,
		    new Unit[] {pressure.getUnit()},
		    new ErrorEstimate[] {pressure.getError()}),
		Data.WEIGHTED_AVERAGE,
		Data.NO_ERRORS).getSample(0);
    }


    /**
     * Gets the sounding value at a set of pressures.
     * @param pressure		The set of pressures at which to get the
     *				sounding value.
     * @return			The value of the sounding at 
     *				<code>pressure</code>.
     * @throws TypeException	Range MathType is incompatible with SampledSet.
     * @throws VisADException	Couldn't create necessary VisAD object.
    public GriddedSet
    getValue(Gridded1DSet pressure)
	throws VisADException, RemoteException
    {
	FlatField	field = (FlatField)resample(
	    pressure,
	    Data.WEIGHTED_AVERAGE,
	    Data.NO_ERRORS);
	/*
         * The following will throw a TypeException if the MathType
         * of the field's range is incompatible with SampledSet.  In
         * practice and as of 1998-01-05, this means that TypeException
         * will be thrown if the MathType of the field's range is other
         * than RealType or RealTupleType.
	return (GriddedSet)GriddedSet.create(
	    ((FunctionType)field.getType()).getRange(),
	    field.getValues(),
	    pressure.getLengths(),
	    field.getRangeCoordinateSystem()[0],
	    field.getDefaultRangeUnits(),
	    field.getRangeErrors());
    }
     */
}
