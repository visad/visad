/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: AbstractSounding.java,v 1.1 1998-10-28 17:16:46 steve Exp $
 */

package visad.meteorology;

import visad.CoordinateSystem;
import visad.FlatField;
import visad.FunctionType;
import visad.Set;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for atmospheric soundings.
 *
 * @author Steven R. Emmerson
 */
public abstract class
AbstractSounding
    extends	FlatField
    implements	Sounding
{
    /**
     * Constructs from a FlatField.
     *
     * @param field		The VisAD FlatField to emulate.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected
    AbstractSounding(FlatField field)
	throws VisADException
    {
	this(
	    (FunctionType)field.getType(),
	    field.getDomainSet(),
	    field.getDefaultRangeUnits());
    }


    /**
     * Constructs from the elements of a FlatField.
     *
     * @param funcType		The type of the VisAD function.
     * @param domainSet		The sampling set of the domain.
     * @param rangeUnits	The units of the range components.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected
    AbstractSounding(FunctionType funcType, Set domainSet, Unit[] rangeUnits)
	throws VisADException
    {
	super(funcType, domainSet, (CoordinateSystem)null, (Set[])null,
	    rangeUnits);
    }


    /**
     * Gets the temperature sounding.  Should be overridden by temperature
     * soundings.
     *
     * @return			<code>null</code>, always.
     */
    public FlatField
    getTemperatureSounding()
    {
	return null;
    }


    /**
     * Gets the dew-point sounding.  Should be overridden by dew-point
     * soundings.
     *
     * @return			<code>null</code>, always.
     */
    public FlatField
    getDewPointSounding()
    {
	return null;
    }


    /**
     * Gets the wind profile.  Should be overridden by wind profiles.
     *
     * @return			<code>null</code>, always.
     */
    public FlatField
    getWindProfile()
    {
	return null;
    }
}
