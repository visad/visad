/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DewPointSoundingImpl.java,v 1.2 1998-11-16 18:23:47 steve Exp $
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
 * Provides support for atmospheric dew-point soundings.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public class
DewPointSoundingImpl
    extends	SingleSoundingImpl
    implements	DewPointSounding
{
    /**
     * The range type.
     */
    private static final RealType	rangeType = DEW_POINT_TYPE;


    /**
     * Constructs from nothing.  The data will be missing.
     *
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    public
    DewPointSoundingImpl()
	throws VisADException, IllegalArgumentException
    {
	this(new Integer1DSet(DOMAIN_TYPE, 2), rangeType.getDefaultUnit());
    }


    /**
     * Constructs from a domain set and range unit.  The data will be missing.
     *
     * @param domainSet			The domain set of the function.
     * @param dewPointUnit		The unit for the dew-point values.
     * @throws VisADException		Couldn't create necessary VisAD object.
     */
    public
    DewPointSoundingImpl(Set domainSet, Unit dewPointUnit)
	throws VisADException, IllegalArgumentException
    {
	super(rangeType, domainSet, dewPointUnit);
    }


    /**
     * Constructs from a FlatField and a component index.
     *
     * @param field		The VisAD FlatField to emulate.
     * @param index		The range-index of the dew-point component.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    protected
    DewPointSoundingImpl(FlatField field, int index)
	throws VisADException, RemoteException
    {
	super(rangeType, field, index);
    }


    /**
     * Gets the dew-point sounding.
     *
     * @return			<code>this</code>.
     */
    public DewPointSounding
    getDewPointSounding()
    {
	return this;
    }
}
