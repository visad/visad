/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: CommonTypes.java,v 1.4 1998-11-16 18:23:47 steve Exp $
 */

package visad.meteorology;

import visad.CommonUnit;
import visad.RealTupleType;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.QuantityDB;


/**
 * Provides support for VisAD types common to meteorology.
 */
public class
CommonTypes
{
    public static final Quantity	PRESSURE;
    public static final Quantity	TEMPERATURE;
    public static final Quantity	DEW_POINT;
    public static final Quantity	THETA;
    public static final Quantity	THETA_ES;
    public static final Quantity	R_SAT;
    public static final Quantity	SPEED;
    public static final Quantity	DIRECTION;
    public static final Quantity	U;
    public static final Quantity	V;
    public static final Quantity	W;
    public static final RealTupleType	UV_WIND;
    public static final RealTupleType	UVW_WIND;
    public static final RealTupleType	POLAR_WIND;
    public static final RealTupleType	WIND;

    static
    {
	Quantity	pressure = null;
	Quantity	temp = null;
	Quantity	dewPoint = null;
	Quantity	theta = null;
	Quantity	thetaES = null;
	Quantity	rSat = null;
	Quantity	speed = null;
	Quantity	direction = null;
	Quantity	u = null;
	Quantity	v = null;
	Quantity	w = null;
	RealTupleType	uvWind = null;
	RealTupleType	uvwWind = null;
	RealTupleType	polarWind = null;

	try
	{
	    QuantityDB	db = MetQuantityDB.instance();
	    pressure = db.getFirst("Pressure");;
	    temp = db.getFirst("Temperature");
	    dewPoint = db.getFirst("DewPoint");
	    theta = db.getFirst("Theta");
	    thetaES = db.getFirst("ThetaES");
	    rSat = db.getFirst("Rsat");
	    speed = db.getFirst("Speed");
	    direction = db.getFirst("Direction");
	    u = db.getFirst("U");
	    v = db.getFirst("V");
	    w = db.getFirst("W");
	    uvWind = new RealTupleType(u, v);
	    uvwWind = new RealTupleType(u, v, w);
	    polarWind = new RealTupleType(speed, direction);
	}
	catch (Exception e)
	{
	    String	reason = e.getMessage();

	    System.err.println("Couldn't initialize class CommonTypes" +
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
	W = w;
	UV_WIND = uvWind;
	UVW_WIND = uvwWind;
	POLAR_WIND = polarWind;
	WIND = UV_WIND;
    }
}
