//
//  EarthLocationTuple.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.georef;

import visad.*;
import java.rmi.RemoteException;

/**
 * RealTuple implementation of EarthLocation for representing a 
 * location on the earth's surface in terms of latitude,  longitude 
 * and altitude above sea level.  In this implementation, the RealTuple
 * is (latitude, longitude, altitude) and has a MathType of
 * RealTupleType.LatitudeLongitudeAltitude.
 *
 * @author  Don Murray, Unidata
 */
public class EarthLocationTuple extends RealTuple
    implements EarthLocation
{

    LatLonTuple latlon;
    Real alt;

    /* Default units (degree, degree, meter) */
    public static final Unit[] DEFAULT_UNITS = 
      new Unit[] {CommonUnit.degree, CommonUnit.degree, CommonUnit.meter};

    /**
     * Construct an EarthLocationTuple with missing values
     *
     * @throws  VisADException   unable to create necessary VisAD object
     * @throws  RemoteException  unable to create necessary remote object
     */
    public EarthLocationTuple()
        throws VisADException, RemoteException
    {
        this(Double.NaN, Double.NaN, Double.NaN);
    }

    /**
     * Construct an EarthLocationTuple from Reals of lat, lon, alt
     *
     * @param  lat   Real representing the latitude
     * @param  lon   Real representing the longitude
     * @param  alt   Real representing the altitude
     *
     * @throws  VisADException   unable to create necessary VisAD object
     * @throws  RemoteException  unable to create necessary remote object
     */
    public EarthLocationTuple(Real lat, Real lon, Real alt)
        throws VisADException, RemoteException
    {
        this(lat, lon, alt, (Unit[]) null, true);
    }

    /**
     * Construct an EarthLocationTuple from Reals of lat, lon, alt
     *
     * @param  lat   Real representing the latitude
     * @param  lon   Real representing the longitude
     * @param  alt   Real representing the altitude
     * @param  units   array of Units.  Must be same as Real units or null
     * @param  checkUnits   true if should check the units
     *
     * @throws  VisADException   unable to create necessary VisAD object
     * @throws  RemoteException  unable to create necessary remote object
     */
    public EarthLocationTuple(Real lat, Real lon, Real alt, Unit[] units, boolean checkUnits)
        throws VisADException, RemoteException
    {
        this(lat, lon, alt, units, checkUnits, false);
    }

    /**
     * Trusted Construct an EarthLocationTuple from Reals of lat, lon, alt
     *
     * @param  lat   Real representing the latitude
     * @param  lon   Real representing the longitude
     * @param  alt   Real representing the altitude
     * @param  units   array of Units.  Must be same as Real units or null
     * @param  checkUnits   true if should check the units
     * @param  useLLTUnits   true to use the LatLonTuple units 
     *
     * @throws  VisADException   unable to create necessary VisAD object
     * @throws  RemoteException  unable to create necessary remote object
     */
    EarthLocationTuple(Real lat, Real lon, Real alt, Unit[] units, boolean checkUnits, boolean useLLTUnits)
        throws VisADException, RemoteException
    {
        super(RealTupleType.LatitudeLongitudeAltitude,
              new Real[] {lat, lon, alt}, 
              (CoordinateSystem) null, units, checkUnits);
        latlon = (useLLTUnits)
            ? new LatLonTuple(lat, lon, LatLonTuple.DEFAULT_UNITS, checkUnits)
            : new LatLonTuple(lat, lon);
        this.alt = alt;
    }

    /**
     * Construct an EarthLocationTuple from double values of lat, lon, alt
     *
     * @param  lat   latitude (degrees North positive)
     * @param  lon   longitude (degrees East positive)
     * @param  alt   altitude (meters above sea level)
     *
     * @throws  VisADException   unable to create necessary VisAD object
     * @throws  RemoteException  unable to create necessary remote object
     */
    public EarthLocationTuple(double lat, double lon, double alt)
        throws VisADException, RemoteException
    {
        this(new Real(RealType.Latitude, lat),
             new Real(RealType.Longitude, lon),
             new Real(RealType.Altitude, alt), 
             DEFAULT_UNITS, false, true);
    }

    /**
     * Construct an EarthLocationTuple from a LatLonPoint and an altitude
     *
     * @param  latlon   LatLonPoint
     * @param  alt      Real representing the altitude
     *
     * @throws  VisADException   unable to create necessary VisAD object
     * @throws  RemoteException  unable to create necessary remote object
     */
    public EarthLocationTuple(LatLonPoint latlon, Real alt)
        throws VisADException, RemoteException
    {
        this(latlon.getLatitude(), latlon.getLongitude(), alt);
    }

    /**
     * Get the latitude of this location
     *
     * @return  Real representing the latitude
     */
    public Real getLatitude()
    {
        return latlon.getLatitude();
    }

    /**
     * Get the longitude of this location
     *
     * @return  Real representing the longitude
     */
    public Real getLongitude()
    {
        return latlon.getLongitude();
    }

    /**
     * Get the altitude of this location
     *
     * @return  Real representing the altitude
     */
    public Real getAltitude()
    {
        return alt;
    }

    /**
     * Get the lat/lon of this location as a LatLonPoint
     *
     * @return  location of this point.
     */
    public LatLonPoint getLatLonPoint()
    {
        return (LatLonPoint) latlon;
    }

    /*   Uncomment to test
    public static void main (String[] args)
        throws VisADException, RemoteException
    {
        double lat = 40.1;
        double lon = -105.5;
        double alt = 1660.0;
        double newLat = 
            (args.length > 0) ? new Double(args[0]).doubleValue() : lat;
        double newLon = 
            (args.length > 1) ? new Double(args[1]).doubleValue() : lon;
        double newAlt = 
            (args.length > 2) ? new Double(args[2]).doubleValue() : alt;

        EarthLocationTuple elt = new EarthLocationTuple(lat, lon, alt);
        System.out.println("EarthLocation 1 = " + elt);

        EarthLocationTuple newelt = 
            new EarthLocationTuple(newLat, newLon, newAlt);
        System.out.println("EarthLocation 2 = " + newelt);

        System.out.println("Points are " +
                           (elt.equals(newelt) ? "" : "NOT ")  +
                           "equal");

    }
    */

    public String toString() {
       StringBuffer buf = new StringBuffer();
       buf.append(latlon.toString());
       buf.append(" Alt: ");
       try {
         buf.append(
           visad.browser.Convert.shortString(alt.getValue(CommonUnit.meter)));
       } catch (VisADException ve) {
         buf.append(
           visad.browser.Convert.shortString(alt.getValue()));
       }
       return buf.toString();
    }
}
