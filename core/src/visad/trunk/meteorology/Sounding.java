/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Sounding.java,v 1.1 1998-08-12 17:17:23 visad Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.RealType;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for atmospheric soundings.
 */
public class
Sounding
    extends	FlatField
{
    /**
     * The VisAD MathType of the pressure variable.
     */
    private final RealType	pressureType;

    /**
     * The VisAD MathType of the temperature variable.
     */
    private final RealType	temperatureType;


    /**
     * Constructs from a FlatField.
     */
    public
    Sounding(FlatField field)
	throws	VisADException, RemoteException
    {
	super((FunctionType)field.getType(), field.getDomainSet(), 
	    field.getRangeCoordinateSystem()[0], /*(CoordinateSystem[])*/null,
	    field.getRangeSets(), getDefaultRangeUnits(field));

	// System.out.println("Sounding(): input FlatField = {" + field + "}");

	FunctionType	funcType = (FunctionType)getType();
	pressureType = (RealType)funcType.getDomain().getComponent(0);
	MathType	rangeType = funcType.getRange();
	temperatureType = rangeType instanceof RealType
		? (RealType)rangeType
		: (RealType)((TupleType)rangeType).getComponent(0);

	/*
	 * NB: getValues() returns values in *default* units.
	 */
	setSamples(field.getValues());
    }


    /**
     * Gets the default range units of the given FlatField as a 1-D array.
     */
    protected static Unit[]
    getDefaultRangeUnits(FlatField field)
    {
	return field.getDefaultRangeUnits();
    }


    /**
     * Gets the VisAD RealType for the pressure variable.
     *
     * @return	VisAD RealType of the pressure variable.
     */
    public RealType
    getPressureType()
    {
	return pressureType;
    }


    /**
     * Gets the VisAD RealType for the temperature variable.
     *
     * @return	VisAD RealType of the temperature variable.
     */
    public RealType
    getTemperatureType()
    {
	return temperatureType;
    }
}
