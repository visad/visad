// EarthLocationLite.java
//
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
 * This provides a LoCal EarthLocation that is much faster to create than the
 * EarthLocationTuple.  Assumes units of lat/lon are degrees and Altitude
 * is meters.
 *
 * @author Jeff McWhirter
 */
public class EarthLocationLite extends RealTuple implements EarthLocation {

  /** The lat */
  Real lat;

  /** The lon */
  Real lon;

  /** The alt */
  Real alt;

  /** The LatLonPoint */
  RealTuple latlon;

  /** Holds the components as we create them */
  Data[] components;


  /**
   * Construct a new EarthLocationLite
   *
   * @param lat latitude
   * @param lon longitude
   * @param alt altitude
   *
   * @throws VisADException On badness
   */
  public EarthLocationLite(double lat, double lon, double alt)
          throws VisADException {
    this(new Real(RealType.Latitude, lat), new Real(RealType.Longitude, lon),
         new Real(RealType.Altitude, alt));
  }


  /**
   * Construct a new EarthLocationLite
   * @param lat latitude
   * @param lon longitude
   * @param alt altitude
   */
  public EarthLocationLite(Real lat, Real lon, Real alt) {
    super(RealTupleType.LatitudeLongitudeAltitude);
    this.lat = lat;
    this.lon = lon;
    this.alt = alt;
  }


  /**
   * is missing
   *
   * @return is missing
   */
  public boolean isMissing() {
    return lat.isMissing() || lon.isMissing() || alt.isMissing();
  }

  /**
   * get latitude
   *
   * @return latitude
   */
  public Real getLatitude() {
    return lat;
  }

  /**
   * get longitude
   *
   * @return longitude
   */
  public Real getLongitude() {
    return lon;
  }

  /**
   * get altitude
   *
   * @return altitude
   */
  public Real getAltitude() {
    return alt;
  }

  /**
   * This is an EarthLocation interface method. It just a LatLonTuple
   * made from getLatitude() and getLongitude();
   *
   * @return this
   */
  public LatLonPoint getLatLonPoint() {
    if (latlon == null) {
      try {
        latlon = new LatLonTuple(lat, lon);
      } catch (Exception e) {  // shouldn't happen
	  latlon = this;
	  throw new RuntimeException(e);      
      }
    }
    return (LatLonPoint) latlon;
  }

  /**
   * Get the i'th component.
   *
   * @param i Which one
   *
   * @return The component
   *
   * @throws RemoteException On badness
   * @throws VisADException On badness
   */
  public Data getComponent(int i) throws VisADException, RemoteException {
    if (i == 0) {
      return lat;
    }

    if (i == 1) {
      return lon;
    }

    if (i == 2) {
      return alt;
    }

    throw new IllegalArgumentException("Wrong component number:"+i);
  }



  /**
   * Create, if needed, and return the component array.
   *
   * @return components
   */
  public Data[] getComponents(boolean copy) {
    //Create the array and populate it if needed
    if (components == null) {
	Data []tmp = new Data[getDimension()];
	tmp[0] = lat;
	tmp[1] = lon;
	tmp[2] = alt;
	components = tmp;
    }
    return components;
  }


  /**
   * Indicates if this Tuple is identical to an object.
   *
   * @param obj         The object.
   * @return            <code>true</code> if and only if the object is
   *                    a Tuple and both Tuple-s have identical component
   *                    sequences.
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof EarthLocationLite)) {
      return false;
    }

    EarthLocationLite that = (EarthLocationLite) obj;

    return lat.equals(that.lat) && lon.equals(that.lon)
           && alt.equals(that.alt);
  }


  /**
   * Returns the hash code of this object.
   * @return            The hash code of this object.
   */
  public int hashCode() {
    return lat.hashCode() ^ lon.hashCode() & alt.hashCode();
  }


  /**
   * to string
   *
   * @return string of me
   */
  public String toString() {
    return getLatitude()+" "+getLongitude()+" "+getAltitude();
  }

  /**
   * run 'java ucar.visad.EarthLocationLite' to test the RealTuple class.
   * This does a performance comparison of creating this object and the
   * EarthLocationTuple
   *
   * @param args  ignored
   *
   * @throws RemoteException  Java RMI problem
   * @throws VisADException   Unable to create the VisAD objects
   */
  public static void main(String args[])
          throws VisADException, RemoteException {
    Real lat = new Real(RealType.Latitude);
    Real lon = new Real(RealType.Longitude);
    Real alt = new Real(RealType.Altitude);
    for (int j = 0; j < 10; j++) {
      long t1 = System.currentTimeMillis();
      for (int i = 0; i < 100000; i++) {
        EarthLocationTuple elt = new EarthLocationTuple(lat, lon, alt);
      }

      long t2 = System.currentTimeMillis();

      long t3 = System.currentTimeMillis();
      for (int i = 0; i < 100000; i++) {
        EarthLocationLite elt = new EarthLocationLite(lat, lon, alt);
      }

      long t4 = System.currentTimeMillis();
      System.err.println("time EathLocationTuple:"+(t2-t1)+
                         " EarthLocationLite:"+(t4-t3));
    }



  }

}

