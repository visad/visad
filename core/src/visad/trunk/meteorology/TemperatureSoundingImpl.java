/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TemperatureSoundingImpl.java,v 1.1 1998-11-03 22:27:36 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.FlatField;
import visad.Integer1DSet;
import visad.RealType;
import visad.Set;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for atmospheric temperature soundings.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public class
TemperatureSoundingImpl
    extends	SingleSoundingImpl
    implements	TemperatureSounding
{
    /**
     * The range type.
     */
    private static final RealType	rangeType = TEMPERATURE_TYPE;


    /**
     * Constructs from nothing.  The data will be missing.
     *
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    public
    TemperatureSoundingImpl()
	throws VisADException, IllegalArgumentException
    {
	this(new Integer1DSet(domainType, 2), rangeType.getDefaultUnit());
    }


    /**
     * Constructs from a domain set and range unit.  The data will be missing.
     *
     * @param domainSet			The domain set of the function.
     * @param temperatureUnit		The unit for the temperature values.
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    public
    TemperatureSoundingImpl(Set domainSet, Unit temperatureUnit)
	throws VisADException, IllegalArgumentException
    {
	super(rangeType, domainSet, temperatureUnit);
    }


    /**
     * Constructs from a FlatField and a component index.
     *
     * @param field		The VisAD FlatField to emulate.
     * @param index		The range-index of the temperature component.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    protected
    TemperatureSoundingImpl(FlatField field, int index)
	throws VisADException, RemoteException
    {
	super(rangeType, field, index);
    }


    /**
     * Gets the temperature sounding.
     *
     * @return			<code>this</code>.
     */
    public TemperatureSounding
    getTemperatureSounding()
    {
	return this;
    }
}
