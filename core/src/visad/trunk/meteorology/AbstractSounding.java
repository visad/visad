/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: AbstractSounding.java,v 1.2 1998-11-03 22:27:33 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.CoordinateSystem;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for atmospheric soundings.  This class exists to provide
 * a parent class for classes like SoundingImpl and SoundingProxy.
 *
 * @author Steven R. Emmerson
 */
public abstract class
AbstractSounding
    extends	FlatField
    implements	Sounding
{
    /**
     * The math type of the domain.
     */
    public static final RealType	domainType = CommonTypes.PRESSURE;

    /**
     * The math type of the temperature variable.
     */
    public static final RealType	TEMPERATURE_TYPE =
	CommonTypes.TEMPERATURE;

    /**
     * The math type of the dew-point variable.
     */
    public static final RealType	DEW_POINT_TYPE = CommonTypes.DEW_POINT;

    /**
     * The math type of the wind variable.
     */
    public static final RealTupleType	WIND_TYPE = CommonTypes.WIND;

    /**
     * The types of the (non-flat) range components.
     */
    private MathType[]			componentTypes;


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
     * Constructs from a function type, domain set, and range units.
     *
     * @param funcType		The type of the VisAD function.
     * @param domainSet		The sampling set of the domain.
     * @param rangeUnits	The units of the range components.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected
    AbstractSounding(MathType rangeType, Set domainSet, Unit[] rangeUnits)
	throws VisADException
    {
	super(
	    new FunctionType(domainType, rangeType),
	    domainSet,
	    (CoordinateSystem)null,
	    (Set[])null,
	    rangeUnits);

	if (rangeType instanceof RealType)
	{
	    componentTypes = new MathType[] {(RealType)rangeType};
	}
	else
	{
	    TupleType	rangeTuple = (TupleType)rangeType;
	    int	count = rangeTuple.getDimension();
	    componentTypes = new MathType[count];
	    for (int i = 0; i < count; ++i)
		componentTypes[i] = rangeTuple.getComponent(i);
	}
    }


    /**
     * Gets the type of the domain.
     */
    public RealType
    getDomainType()
    {
	return domainType;
    }


    /**
     * Gets the type of the range.
     */
    public MathType
    getRangeType()
    {
	return ((FunctionType)getType()).getRange();
    }


    /**
     * Finds the component of the given type.
     *
     * @param rangeType		The type of the component.
     * @return			The index of the component or -1.
     */
    protected int
    findComponent(MathType componentType)
    {
	for (int i = 0; i < componentTypes.length; ++i)
	    if (componentTypes[i].equals(componentType))
		return i;

	return -1;
    }


    /**
     * Gets the temperature sounding.
     *
     * @return			The temperature sounding or <code>null</code>
     *				if none exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    public TemperatureSounding
    getTemperatureSounding()
	throws VisADException, RemoteException
    {
	return null;
    }


    /**
     * Gets the dew-point sounding.
     *
     * @return			The dew-point sounding or <code>null</code>
     *				if none exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    public DewPointSounding
    getDewPointSounding()
	throws VisADException, RemoteException
    {
	return null;
    }


    /**
     * Gets the wind profile.
     *
     * @return			The wind profile or <code>null</code>
     *				if none exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    public WindProfile
    getWindProfile()
	throws VisADException, RemoteException
    {
	return null;
    }
}
