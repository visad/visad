/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: CommonTypes.java,v 1.1 1998-10-21 15:27:57 steve Exp $
 */

package visad.meteorology;

import visad.RealType;
import visad.CommonUnit;


/**
 * Provides support for VisAD types common to meteorology.
 */
public class
CommonTypes
{
    public static final RealType	PRESSURE;
    public static final RealType	TEMPERATURE;
    public static final RealType	DEW_POINT;
    public static final RealType	THETA;
    public static final RealType	THETA_ES;
    public static final RealType	R_SAT;
    public static final RealType	SPEED;
    public static final RealType	DIRECTION;

    static
    {
	RealType	pressure = null;
	RealType	temp = null;
	RealType	dewPoint = null;
	RealType	theta = null;
	RealType	thetaES = null;
	RealType	rSat = null;
	RealType	speed = null;
	RealType	direction = null;

	try
	{
	    pressure = new RealType("Pressure", CommonUnits.MILLIBAR, null);
	    temp = new RealType("Temperature", CommonUnits.CELSIUS, null);
	    dewPoint = new RealType("Dew_Point", CommonUnits.CELSIUS, null);
	    theta = new RealType("Theta", CommonUnits.CELSIUS, null);
	    thetaES = new RealType("ThetaES", CommonUnits.CELSIUS, null);
	    rSat = new RealType("Rsat", CommonUnits.G_PER_KG, null);
	    speed = new RealType("Speed", CommonUnits.METERS_PER_SECOND, null);
	    direction = new RealType("Direction", CommonUnit.degree, null);
	}
	catch (Exception e)
	{
	    String	reason = e.getMessage();

	    System.err.println("Couldn't initialize CommonTypes class" +
		(reason == null ? "" : ": " + reason));
	    e.printStackTrace();
	}

	PRESSURE = pressure;
	TEMPERATURE = temp;
	DEW_POINT = dewPoint;
	THETA = theta;
	THETA_ES = thetaES;
	R_SAT = rSat;
	SPEED = speed;
	DIRECTION = direction;
    }
}
