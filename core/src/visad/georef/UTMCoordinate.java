//
//  UTMCoordinate.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
 * RealTuple implementation of a Universal Transverse Mercator
 * (UTM) coordinate
 */
public class UTMCoordinate extends RealTuple {

  /**
   * The <code>RealType</code> for the easting component of the UTM
   * grid.
   */
  public static RealType EASTING = 
      RealType.getRealType("UTM_Easting", CommonUnit.meter);

  /**
   * The <code>RealType</code> for the easting component of the UTM
   * grid.
   */
  public static RealType NORTHING =
      RealType.getRealType("UTM_Northing", CommonUnit.meter);

  /**
   * The <code>RealType</code> for the zone component of the UTM
   * grid.
   */
  public static RealType ZONE = RealType.getRealType("UTM_Zone");
  
  /**
   * The <code>RealType</code> for the zone component of the UTM
   * grid.
   */
  public static RealType HEMISPHERE = RealType.getRealType("UTM_Hemisphere");
  
  /**
   * The hemisphere identifier for the northern hemisphere
   */
  public static final int NORTH = 0;

  /**
   * The hemisphere identifier for the southern hemisphere
   */
  public static final int SOUTH = 1;

  private Real easting;
  private Real northing;
  private Real altitude;
  private Real zone;
  private Real hemisphere;

  /**
   * Construct a UTMCoordinate with missing values
   *
   * @throws  VisADException   couldn't create the necessary VisAD object
   * @throws  RemoteException  couldn't create the necessary remote object
   */
  public UTMCoordinate() throws VisADException, RemoteException
  {
    this(Double.NaN, Double.NaN);
  }

  /**
   * Construct a UTMCoordinate from double values of easting and
   * northing.
   *
   * @param  east   easting component (meters) in the zone
   * @param  north  northing component (meters) in the zone
   *
   * @throws  VisADException   couldn't create the necessary VisAD object
   * @throws  RemoteException  couldn't create the necessary remote object
   */
  public UTMCoordinate(double east, double north)
      throws VisADException, RemoteException
  {
    this(east, north, 0.);
  }

  /**
   * Construct a UTMCoordinate from double values of easting and
   * northing and an altitude.
   *
   * @param  east   easting component (meters) in the zone
   * @param  north  northing component (meters) in the zone
   * @param  alt    altitude of the point
   *
   * @throws  VisADException   couldn't create the necessary VisAD object
   * @throws  RemoteException  couldn't create the necessary remote object
   */
  public UTMCoordinate(double east, double north, double alt)
      throws VisADException, RemoteException
  {
    this(east, north, Double.NaN, 0);
  }

  /**
   * Construct a UTMCoordinate from double values of easting and
   * northing.  Unknown zone and northern hemisphere used
   *
   * @param  east   easting component (meters) in the zone
   * @param  north  northing component (meters) in the zone
   * @param  alt    altitude of the point
   * @param  zone   UTM zone
   *
   * @throws  VisADException   couldn't create the necessary VisAD object
   * @throws  RemoteException  couldn't create the necessary remote object
   */
  public UTMCoordinate(double east, double north, double alt, int zone)
      throws VisADException, RemoteException
  {
    this(east, north, alt, zone, NORTH);
  }

  /**
   * Construct a UTMCoordinate from double values of easting and
   * northing, the zone and the hemisphere.
   *
   * @param  east   easting component (meters) in the zone
   * @param  north  northing component (meters) in the zone
   * @param  alt    altitude of the point
   * @param  zone   UTM zone
   * @param  hemi   UTM hemisphere
   *
   * @throws  VisADException   couldn't create the necessary VisAD object
   * @throws  RemoteException  couldn't create the necessary remote object
   */
  public UTMCoordinate(double east, double north, double alt, 
                       int zone, int hemi)
      throws VisADException, RemoteException
  {
    this(new Real(EASTING, east),
         new Real(NORTHING, north),
         new Real(RealType.Altitude, alt),
         (zone == 0) ? new Real(ZONE) :new Real(ZONE, zone),
         new Real(HEMISPHERE, hemi));
  }

  /**
   * Construct a UTMCoordinate from Reals representing the easting and
   * northing.
   *
   * @param  east   Real representing easting (must have MathType EASTING)
   * @param  north  Real representing northing (must have MathType NORTHING)
   *
   * @throws  VisADException   couldn't create the necessary VisAD object
   * @throws  RemoteException  couldn't create the necessary remote object
   */
  public UTMCoordinate(Real east, Real north)
      throws VisADException, RemoteException
  {
    this(east, north, new Real(ZONE));
  }

  /**
   * Construct a UTMCoordinate from Reals representing the easting and
   * northing and the zone.
   *
   * @param  east   Real representing easting (must have MathType EASTING)
   * @param  north  Real representing northing (must have MathType NORTHING)
   *
   * @throws  VisADException   couldn't create the necessary VisAD object
   * @throws  RemoteException  couldn't create the necessary remote object
   */
  public UTMCoordinate(Real east, Real north, Real alt)
      throws VisADException, RemoteException
  {
    this(east, north, alt, new Real(ZONE, 0));
  }

  /**
   * Construct a UTMCoordinate from Reals representing the easting and
   * northing and the zone.
   *
   * @param  east   Real representing easting (must have MathType EASTING)
   * @param  north  Real representing northing (must have MathType NORTHING)
   *
   * @throws  VisADException   couldn't create the necessary VisAD object
   * @throws  RemoteException  couldn't create the necessary remote object
   */
  public UTMCoordinate(Real east, Real north, Real alt, Real zone)
      throws VisADException, RemoteException
  {
    this(east, north, alt, zone, new Real(HEMISPHERE, NORTH));
  }

  /**
   * Construct a UTMCoordinate from Reals representing the easting and
   * northing, the zone and the hemisphere.
   *
   * @param  east   Real representing easting (must have MathType EASTING)
   * @param  north  Real representing northing (must have MathType NORTHING)
   * @param  zone   Real representing UTM zone
   * @param  hemi   Real representing hemisphere
   *
   * @throws  VisADException   couldn't create the necessary VisAD object
   * @throws  RemoteException  couldn't create the necessary remote object
   */
  public UTMCoordinate(Real east, Real north, Real alt, Real zone, Real hemi)
      throws VisADException, RemoteException
  {
    this(east, north, alt, zone, hemi, null);
  }

  /**
   * Construct a UTMCoordinate from Reals representing the easting and
   * northing, the zone and the hemisphere.  Use the CoordinateSystem
   * supplied to do any transforms.
   *
   * @param  east   Real representing easting (must have MathType EASTING)
   * @param  north  Real representing northing (must have MathType NORTHING)
   * @param  zone   Real representing UTM zone
   * @param  hemi   Real representing hemisphere
   * @param  cs     CoordinateSystem
   *
   * @throws  VisADException   couldn't create the necessary VisAD object
   * @throws  RemoteException  couldn't create the necessary remote object
   */
  public UTMCoordinate(Real east, Real north, Real alt, Real zone, Real hemi,
      CoordinateSystem cs)
      throws VisADException, RemoteException
  {
    super (new RealTupleType(new RealType[] {EASTING, NORTHING, 
                            RealType.Altitude, ZONE, HEMISPHERE}),
        new Real[] {east, north, alt, zone, hemi}, (CoordinateSystem) cs);
    this.easting = east;
    this.northing = north;
    this.altitude = alt;
    this.zone = zone;
    this.hemisphere = hemi;
  }

  /**
   * Get the easting value of this point as a Real
   *
   * @return  Real representing the easting
   */
  public Real getEasting()
  {
    return easting;
  }

  /**
   * Get the northing of this point as a Real
   *
   * @return  Real representing the northing
   */
  public Real getNorthing()
  {
    return northing;
  }

  /**
   * Get the altitude of this point as a Real
   *
   * @return  Real representing the altitude.  May be missing.
   */
  public Real getAltitude()
  {
    return altitude;
  }


  /**
   * Get the UTM zone of this point as a Real
   *
   * @return  Real representing the UTM zone
   */
  public Real getZone()
  {
    return zone;
  }

  /**
   * Get the UTM hemisphere of this point as a Real
   *
   * @return  Real representing the UTM hemisphere
   */
  public Real getHemisphere()
  {
    return zone;
  }

  /**
   * Get the easting value of this point as a Real
   *
   * @return  double representing the easting in meters
   */
  public double getEastingValue()
  {
    try {
      return easting.getValue(CommonUnit.meter);
    } catch (VisADException ve) {
      return easting.getValue();
    }
  }

  /**
   * Get the northing of this point
   *
   * @return  double representing the northing
   */
  public double getNorthingValue()
  {
    try {
      return northing.getValue(CommonUnit.meter);
    } catch (VisADException ve) {
      return northing.getValue();
    }
  }

  /**
   * Get the altitude value of this point.
   *
   * @return  double representing the altitude in meters
   */
  public double getAltitudeValue()
  {
    try {
      return altitude.getValue(CommonUnit.meter);
    } catch (VisADException ve) {
      return altitude.getValue();
    }
  }

  /**
   * Get the UTM zone of this point
   *
   * @return  int representing the UTM zone
   */
  public int getZoneValue()
  {
    return (int) zone.getValue();
  }

  /**
   * Get the UTM zone of this point
   *
   * @return  int representing the UTM zone
   */
  public int getHemisphereValue()
  {
    return (int) hemisphere.getValue();
  }


  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("East: ");
    try {
      buf.append(
        visad.browser.Convert.shortString(easting.getValue(CommonUnit.degree)));
    } catch (VisADException ve) {
      buf.append(
        visad.browser.Convert.shortString(easting.getValue()));
    }
    buf.append(" North: ");
    try {
      buf.append(
        visad.browser.Convert.shortString(northing.getValue(CommonUnit.degree)));
    } catch (VisADException ve) {
      buf.append(
        visad.browser.Convert.shortString(northing.getValue()));
    }
    buf.append(" Zone: ");
    buf.append(getZoneValue());
    buf.append(" Hemisphere: ");
    buf.append(
      (getHemisphereValue() == NORTH)?"Northern":"Southern");
    return buf.toString();
  }

}
