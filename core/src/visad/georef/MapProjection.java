//
// MapProjection.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

/**
 * Abstract class for coordinate systems that support (lat,lon) <-> (x,y)
 * with a reference coordinate system of (lat, lon) or (lon, lat).
 *
 * @author Don Murray
 */
public abstract class MapProjection extends NavigatedCoordinateSystem
{

  /**
   * Constructs from the type of the reference coordinate system and 
   * units for values in this coordinate system. The reference coordinate
   * system must contain RealType.Latitude and RealType.Longitude only.
   *
   * @param reference  The type of the reference coordinate system. The
   *                   reference must contain RealType.Latitude and
   *                   RealType.Longitude.  Values in the reference 
   *                   coordinate system shall be in units of 
   *                   reference.getDefaultUnits() unless specified 
   *                   otherwise.
   * @param units      The default units for this coordinate system. 
   *                   Numeric values in this coordinate system shall be 
   *                   in units of units unless specified otherwise. 
   *                   May be null or an array of null-s.
   * @exception VisADException  Couldn't create necessary VisAD object or
   *                            reference does not contain RealType.Latitude
   *                            and/or RealType.Longitude or the reference
   *                            dimension is greater than 2.
   */
  public MapProjection(RealTupleType reference, Unit[] units)
      throws VisADException
  {
    super(reference, units);
    if ( !(reference.equals(RealTupleType.LatitudeLongitudeTuple) ||
           reference.equals(RealTupleType.SpatialEarth2DTuple)))
      throw new CoordinateSystemException(
        "MapProjection: Reference must be LatitudeLongitudeTuple or " +
        "SpatialEarth2DTuple");
  }

  /**
   * Get a reasonable bounding box in this coordinate system. MapProjections 
   * are typically specific to an area of the world; there's no bounding 
   * box that works for all projections so each subclass must implement
   * this method. For example, the bounding box for a satellite image 
   * MapProjection might have an upper left corner of (0,0) and the width 
   * and height of the Rectangle2D would be the number of elements and 
   * lines, respectively.
   *
   * @return the bounding box of the MapProjection
   *
   */
  public abstract java.awt.geom.Rectangle2D getDefaultMapArea();


  /**
   * Determine if the input to the toReference and output from the
   * fromReference is (x,y) or (y,x).  Subclasses should override
   * if (y,x).
   * @return true if (x,y)
   */
  public boolean isXYOrder() { return true;}

  /**
   * Determine if the fromReference and toReference expect the
   * to get the output and input values to be row/col ordered
   * or
   */
  public boolean isLatLonOrder() { return (getLatitudeIndex() == 0); }

  /**
   * Get the center lat/lon point for this MapProjection.
   * @return the lat/lon point at the center of the projection.
   */
  public LatLonPoint getCenterLatLon() 
      throws VisADException {
    java.awt.geom.Rectangle2D rect = getDefaultMapArea();
    return getLatLon(new double[][] { {rect.getCenterX()}, {rect.getCenterY()}});
  }

  /**
   * Get the X index
   * @return the index
   */
  public int getXIndex() {
     return isXYOrder() ? 0 : 1;
  }

  /**
   * Get the Y index
   * @return the index
   */
  public int getYIndex() {
     return isXYOrder() ? 1 : 0;
  }


  /**
   * Get the  lat/lon point for the given xy pairs.
   * this method will flip the order of the xy if it is not in xyorder
   * @return the lat/lon point for the given xy pairs
   */
  public LatLonPoint getLatLon(double[][]xy) 
      throws VisADException {
      //Flip them if needed
      if(!isXYOrder()) {
          double tmp;
          tmp = xy[0][0];
          xy[0][0] = xy[0][1];
          xy[0][1] = tmp;

          tmp = xy[1][0];
          xy[1][0] = xy[1][1];
          xy[1][1] = tmp;
      }


    double[][] latlon = toReference(xy);
    double lat = 
      (isLatLonOrder())
        ? CommonUnit.degree.toThis(latlon[0][0], getReferenceUnits()[0])
        : CommonUnit.degree.toThis(latlon[1][0], getReferenceUnits()[1]);
    double lon = 
      (isLatLonOrder())
        ? CommonUnit.degree.toThis(latlon[1][0], getReferenceUnits()[1])
        : CommonUnit.degree.toThis(latlon[0][0], getReferenceUnits()[0]);
     LatLonPoint llp = null;
     try {
       llp = new LatLonTuple(lat, lon);
     } catch (java.rmi.RemoteException re) {} // can't happen
     return llp;
  }

  /**
   * Print out a string representation of this MapProjection
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("MapProjection: \n");
    buf.append("  Reference = ");
    buf.append(getReference());
    buf.append("\n");
    buf.append("  DefaultMapArea = ");
    buf.append(getDefaultMapArea());
    return buf.toString();
  }
}
