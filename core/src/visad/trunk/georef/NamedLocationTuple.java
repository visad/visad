//
//  NamedLocationTuple.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
 * Tuple implementation of NamedLocation for representing a 
 * location on the earth's surface in terms of latitude,  longitude 
 * and altitude above sea level and some sort of identifier.  In
 * this implementation, the Tuple has two components - the identifier
 * and an EarthLocation.
 *
 * @author  Don Murray, Unidata
 */
public class NamedLocationTuple extends Tuple
    implements NamedLocation
{

    private EarthLocation location;
    private Text id;

    /** 
     * TextType associated with the identifier that is returned by
     * getIdentifier().
     */
    public static TextType IDENTIFIER_TYPE;

    /* 
    Instantiate the IDENTIFIER_TYPE.  Wish I could do this some other way
    because what happens if there is an exception? 
    */
    static
    {
        try
        {
            IDENTIFIER_TYPE = new TextType("Identifier");
        }
        catch (Exception e)
        {
            System.err.println("NamedLocationTuple: Can't instatiate type");
        }
    }
        
    /**
     * Construct an NamedLocationTuple from a Text and an EarthLocation
     *
     * @param  identifier   Text representing the identifier
     *                      (must be of type NamedLocation.IDENTIFIER_TYPE)
     * @param  location     EarthLocation
     *
     * @throws  VisADException   unable to create necessary VisAD object
     * @throws  RemoteException  unable to create necessary remote object
     */
    public NamedLocationTuple(Text identifier, EarthLocation location)
        throws VisADException, RemoteException
    {
        super(new TupleType(
                new MathType[]
                    {IDENTIFIER_TYPE, 
                     RealTupleType.LatitudeLongitudeAltitude}),
              new Data[] {
                  identifier,
                  new EarthLocationTuple(location.getLatitude(),
                                         location.getLongitude(),
                                         location.getAltitude())}
              );
        this.id = identifier;
        this.location = location;
    }

    /**
     * Construct an NamedLocationTuple from an identifier and values of 
     * lat, lon, alt
     *
     * @param  id    identifier
     * @param  lat   latitude (degrees North positive)
     * @param  lon   longitude (degrees East positive)
     * @param  alt   altitude (meters above sea level)
     *
     * @throws  VisADException   unable to create necessary VisAD object
     * @throws  RemoteException  unable to create necessary remote object
     */
    public NamedLocationTuple(String id, double lat, double lon, double alt)
        throws VisADException, RemoteException
    {
        this(new Text(IDENTIFIER_TYPE, id),
             new EarthLocationTuple(lat, lon, alt));
    }

    /**
     * Construct an NamedLocationTuple from an identifier and an EarthLocation
     *
     * @param  id    identifier
     * @param  location     EarthLocation
     *
     * @throws  VisADException   unable to create necessary VisAD object
     * @throws  RemoteException  unable to create necessary remote object
     */
    public NamedLocationTuple(String id, EarthLocation location)
        throws VisADException, RemoteException
    {
        this(new Text(IDENTIFIER_TYPE, id), location);
    }

    /**
     * Get the latitude of this location
     *
     * @return  Real representing the latitude
     */
    public Real getLatitude()
    {
        return location.getLatitude();
    }

    /**
     * Get the longitude of this location
     *
     * @return  Real representing the longitude
     */
    public Real getLongitude()
    {
        return location.getLongitude();
    }

    /**
     * Get the altitude of this location
     *
     * @return  Real representing the altitude
     */
    public Real getAltitude()
    {
        return location.getAltitude();
    }

    /**
     * Get the lat/lon of this location as a LatLonPoint
     *
     * @return  location of this point.
     */
    public LatLonPoint getLatLonPoint()
    {
        return location.getLatLonPoint();
    }

    /**
     * Get the lat/lon/alt of this location as an EarthLocation
     *
     * @return  location of this point.
     */
    public EarthLocation getEarthLocation()
    {
        return location;
    }

    /**
     * Return a unique identifier. This might be a Text object 
     * representing the name of the station (e.g.: "Denver"), the ICAO 
     * 4 letter id (e.g., "KDEN"), a WMO block and station number 
     * as a string (e.g., "72565"), or some other identifying string 
     * (i.e., "intersection of Mitchell and 47th" or "Point A", or "A")
     * The TextType for this object is <CODE>IDENTIFIER_TYPE</CODE>.
     *
     * @return  Text whose getValue() method returns the identifier
     */
    public Text getIdentifier()
    {
        return id;
    }

    /*   Uncomment to test
    public static void main (String[] args)
        throws VisADException, RemoteException
    {
        double lat = 40.1;
        double lon = -105.5;
        double alt = 1660.0;
        String name = "KDEN";
        double newLat = 
            (args.length > 0) ? new Double(args[0]).doubleValue() : lat;
        double newLon = 
            (args.length > 1) ? new Double(args[1]).doubleValue() : lon;
        double newAlt = 
            (args.length > 2) ? new Double(args[2]).doubleValue() : alt;
        String newName = 
            (args.length > 3) ? args[3] : name;

        NamedLocationTuple nlt = new NamedLocationTuple(name, lat, lon, alt);
        System.out.println("NamedLocation 1 = " + nlt);

        NamedLocationTuple newnlt = 
            new NamedLocationTuple(newName, newLat, newLon, newAlt);
        System.out.println("NamedLocation 2 = " + newnlt);

        System.out.println("Points are " +
                           (nlt.equals(newnlt) ? "" : "NOT ")  +
                           "equal");

        Text t = new Text(IDENTIFIER_TYPE, newnlt.getIdentifier().getValue());
        newnlt = new NamedLocationTuple(t, nlt.getEarthLocation());
        System.out.println("\nNamedLocation 3 = " + newnlt);

        System.out.println("Points are " +
                           (nlt.equals(newnlt) ? "" : "NOT ")  +
                           "equal");

    }
    */
}
