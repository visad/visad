/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: CommonTypes.java,v 1.3 1998-11-03 22:27:33 steve Exp $
 */

package visad.meteorology;

import visad.RealTupleType;
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
    public static final RealType	U;
    public static final RealType	V;
    public static final RealTupleType	WIND;

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
	RealType	u = null;
	RealType	v = null;
	RealTupleType	wind = null;

	try
	{
	    pressure = new RealType("Pressure", CommonUnits.MILLIBAR, null);
	    temp = new RealType("Temperature", CommonUnits.CELSIUS, null);
	    dewPoint = new RealType("Dew_Point", CommonUnits.CELSIUS, null);
	    theta = new RealType("Theta", CommonUnits.CELSIUS, null);
	    thetaES = new RealType("ThetaES", CommonUnits.CELSIUS, null);
	    rSat = new RealType("Rsat", CommonUnits.G_PER_KG, null);
	    speed = new RealType("Speed", CommonUnits.KNOT, null);
	    direction = new RealType("Direction", CommonUnit.degree, null);
	    u = new RealType("U", CommonUnits.KNOT, null);
	    v = new RealType("V", CommonUnits.KNOT, null);
	    wind = new RealTupleType(u, v);
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
	U = u;
	V = v;
	WIND = wind;
    }
}
