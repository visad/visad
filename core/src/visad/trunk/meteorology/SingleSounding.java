/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SingleSounding.java,v 1.2 1999-01-07 16:13:18 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarType;
import visad.Set;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for atmospheric soundings of a single parameter.  NB:
 * "Wind" is a single parameter.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public abstract class
SingleSounding
    extends	AbstractSounding
{
    /**
     * Constructs from a range type, domain set and range unit.
     * The data will be missing.
     *
     * @param rangeType			The type of the range.
     * @param domainSet			The domain set of the function.
     * @param rangeUnit			The unit for the range values.
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    public
    SingleSounding(MathType rangeType, Set domainSet, Unit rangeUnit)
	throws VisADException
    {
	this(rangeType, domainSet, new Unit[] {rangeUnit});
    }


    /**
     * Constructs from a range type, domain set and range units.
     * The data will be missing.
     *
     * @param rangeType			The type of the range.
     * @param domainSet			The domain set of the function.
     * @param rangeUnits		The units for the range values.
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    public
    SingleSounding(MathType rangeType, Set domainSet, Unit[] rangeUnits)
	throws VisADException
    {
	super(rangeType, domainSet, rangeUnits);
    }


    /**
     * Gets the type of a given range component of a FlatField.
     *
     * @param field             The VisAD FlatField to examine.
     * @param index             The range-index of the component.
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    protected static MathType
    getRangeType(FlatField field, int index)
	throws VisADException
    {
	MathType	rangeType = ((FunctionType)field.getType()).getRange();
	MathType	componentType;

	if (rangeType instanceof TupleType)
	{
	    componentType = ((TupleType)rangeType).getComponent(index);
	}
	else
	if (rangeType instanceof ScalarType)
	{
	    componentType = rangeType;
	}
	else
	{
	    throw new VisADException(
		"FlatField range neither ScalarType nor TupleType");
	}

	return componentType;
    }


    /**
     * Gets the single sounding of the given component.
     *
     * @param rangeType		The type of the range component to get.
     * @return			<code>this</code> or null if <code>rangetype
     *				</code> doesn't equal the range component.
     */
    public SingleSounding
    getSingleSounding(MathType rangeType)
    {
	return rangeType.equals(getRangeType())
		? this
		: null;
    }


    /**
     * Gets the default units of a given range component of a FlatField.
     *
     * @param field             The VisAD FlatField to examine.
     * @param index             The range-index of the component.
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    protected static Unit[]
    getDefaultUnits(FlatField field, int index)
	throws VisADException
    {
	MathType	componentType = getRangeType(field, index);
	Unit[]		componentUnits;

	if (componentType instanceof RealTupleType)
	{
	    componentUnits = ((RealTupleType)componentType).getDefaultUnits();
	}
	else
	if (componentType instanceof RealType)
	{
	    componentUnits = 
		new Unit[] {((RealType)componentType).getDefaultUnit()};
	}
	else
	{
	    throw new VisADException(
		"Component neither RealType nor RealTupleType");
	}

	return componentUnits;
    }
}
