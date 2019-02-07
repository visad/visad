//
//  LatLonTuple.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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
 * RealTuple implementation of LatLonPoint for defining lat/lon points
 */
public class LatLonTuple extends RealTuple
    implements LatLonPoint
{
    private Real lat;
    private Real lon;

    /* Default units (degree, degree, meter) */
    public static final Unit[] DEFAULT_UNITS = 
      new Unit[] {CommonUnit.degree, CommonUnit.degree};

    /**
     * Construct a LatLonTuple with missing values
     *
     * @throws  VisADException   couldn't create the necessary VisAD object
     * @throws  RemoteException  couldn't create the necessary remote object
     */
    public LatLonTuple()
        throws VisADException, RemoteException
    {
        this(Double.NaN, Double.NaN);
    }

    /**
     * Construct a LatLonTuple from double values of latitude and
     * longitude.
     *
     * @param  lat  latitude (degrees North positive)
     * @param  lon  longitude (degrees East positive)
     *
     * @throws  VisADException   couldn't create the necessary VisAD object
     * @throws  RemoteException  couldn't create the necessary remote object
     */
    public LatLonTuple(double lat, double lon)
        throws VisADException, RemoteException
    {
        this(new Real(RealType.Latitude, lat),
             new Real(RealType.Longitude, lon),
             DEFAULT_UNITS, false);
    }

    /**
     * Construct a LatLonTuple from Reals representing the latitude and
     * longitude.
     *
     * @param  lat  Real representing latitude 
     *              (must have MathType RealType.Latitude)
     * @param  lon  Real representing longitude 
     *              (must have MathType RealType.Longitude)
     *
     * @throws  VisADException   couldn't create the necessary VisAD object
     * @throws  RemoteException  couldn't create the necessary remote object
     */
    public LatLonTuple(Real lat, Real lon)
        throws VisADException, RemoteException
    {
        this( lat, lon, (Unit[]) null, true);
    }

    /**
     * Construct a LatLonTuple from Reals representing the latitude and
     * longitude.
     *
     * @param  lat  Real representing latitude 
     *              (must have MathType RealType.Latitude)
     * @param  lon  Real representing longitude 
     *              (must have MathType RealType.Longitude)
     * @param  units  units for the reals (can be null)
     * @param  checkUnits  true to make sure units is convertible with lat/lon
     *
     * @throws  VisADException   couldn't create the necessary VisAD object
     * @throws  RemoteException  couldn't create the necessary remote object
     */
    public LatLonTuple(Real lat, Real lon, Unit[] units, boolean checkUnits)
        throws VisADException, RemoteException
    {
        super( RealTupleType.LatitudeLongitudeTuple,
              new Real[] { lat, lon}, 
              (CoordinateSystem) null, units, checkUnits);
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Get the latitude of this point
     *
     * @return  Real representing the latitude
     */
    public Real getLatitude()
    {
        return lat;
    }

    /**
     * Get the longitude of this point
     *
     * @return  Real representing the longitude
     */
    public Real getLongitude()
    {
        return lon;
    }


    public String toString() {
       StringBuffer buf = new StringBuffer();
       buf.append("Lat: ");
       try {
         buf.append(
           visad.browser.Convert.shortString(lat.getValue(CommonUnit.degree)));
       } catch (VisADException ve) {
         buf.append(
           visad.browser.Convert.shortString(lat.getValue()));
       }
       buf.append(" Lon: ");
       try {
         buf.append(
           visad.browser.Convert.shortString(lon.getValue(CommonUnit.degree)));
       } catch (VisADException ve) {
         buf.append(
           visad.browser.Convert.shortString(lon.getValue()));
       }
       return buf.toString();
    }

    /* uncomment to test 
    public static void main(String[] args)
        throws VisADException, RemoteException
    {
        double lat = 40.1;
        double lon = -105.5;
        double newLat = 
            (args.length > 0) ? new Double(args[0]).doubleValue() : lat;
        double newLon = 
            (args.length > 1) ? new Double(args[1]).doubleValue() : lon;

        LatLonTuple ll = new LatLonTuple(lat, lon);
        System.out.println("Point 1 = " + ll);

        LatLonTuple newll = new LatLonTuple(newLat, newLon);
        System.out.println("Point 2 = " + newll);

        System.out.println("Points are " +
                           (ll.equals(newll) ? "" : "NOT ")  +
                           "equal");
    }
    */
}
