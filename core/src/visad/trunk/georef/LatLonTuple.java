//
//  LatLonTuple.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

    public LatLonTuple(double lat, double lon)
        throws VisADException, RemoteException
    {
        this(new Real(RealType.Latitude, lat),
             new Real(RealType.Longitude, lon));
    }

    public LatLonTuple(Real lat, Real lon)
        throws VisADException, RemoteException
    {
        super ( RealTupleType.LatitudeLongitudeTuple,
              new Real[] {lat, lon}, (CoordinateSystem) null);
        this.lat =  (Real) getComponent(0);
        this.lon =  (Real) getComponent(1);
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


    /*  Uncomment to test
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
