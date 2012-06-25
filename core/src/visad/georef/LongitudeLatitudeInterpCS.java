/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import visad.CoordinateSystem;
import visad.GridCoordinateSystem;
import visad.VisADException;
import visad.SetType;
import visad.RealTupleType;
import visad.Linear2DSet;
import visad.Gridded2DSet;
import visad.Linear1DSet;
import visad.Unit;
import visad.Set;
import visad.georef.MapProjection;
import java.awt.geom.Rectangle2D;

/**
 * For 2D arrays of earth observations when the navigation is not provided analytically,
 * but a set of navigated points is given to interpolate.  Particularly useful for
 * polar swath observations with point-by-point navigation. 
 */
public class LongitudeLatitudeInterpCS extends MapProjection {

   Linear2DSet domainSet;   // The CoordinateSystem
   Linear2DSet transSet;    // Set to transform between domainSet and lonlatSet
   Gridded2DSet lonlatSet;  // Set of earth locations (the Reference, to be interpolated)

   //- assumes incoming GriddedSet is (longitude,latitude) with range (-180,+180)
   boolean neg180pos180 = true;  //false: longitude range (0,+360)
   
   boolean extNeg180pos180 = false; 

   int lonIdx = 0;

   /**
    *
    * @param domainSet  The CoordinateSystem, eg. (line, element), (Track, XTrack) 
    *
    * @param lonlatSet  The set of earth locations to interpolate for (x,y) to/from (lon,lat).
    *                   May be sampled w/respect to domainSet, eg. every fifth point. 
    *                   SetType must be SpatialEarth2DTuple or LatitudeLongitudeTuple.
    *
    * @param neg180pos180  The range of longitude values in the lonlatSet. Default is true,
    *                      false is (0,+360).
    *                   
    * @exception VisADException  Couldn't create necessary VisAD object or reference type
    *                            (the lonlatSet type) is not SpatialEarth2DTuple or LatitudeLongitudeTuple
    *
    */
   public LongitudeLatitudeInterpCS(Linear2DSet domainSet, Gridded2DSet lonlatSet, boolean neg180pos180) throws VisADException {
     super(((SetType)domainSet.getType()).getDomain(), null);
     this.lonlatSet = lonlatSet;
     this.domainSet = domainSet;
     this.neg180pos180 = neg180pos180;
     int[] lengths = domainSet.getLengths();
     int[] gsetLens = lonlatSet.getLengths();
     transSet = new Linear2DSet(0.0, gsetLens[0]-1, lengths[0],
                                0.0, gsetLens[1]-1, lengths[1]);
     lonIdx = getLongitudeIndex();
   }

   public LongitudeLatitudeInterpCS(Linear2DSet domainSet, Gridded2DSet lonlatSet) throws VisADException {
     this(domainSet, lonlatSet, true);
   }

   public float[][] toReference(float[][] values) throws VisADException {
     float[][] coords = domainSet.valueToGrid(values);
     coords = transSet.gridToValue(coords);
     float[][] lonlat = lonlatSet.gridToValue(coords);

     if (!(neg180pos180 && extNeg180pos180)) { // if true lonRanges are same so don't do anything
       if (neg180pos180) {
         for (int t=0; t<lonlat[lonIdx].length; t++) {
           if (lonlat[lonIdx][t] > 180f) {
             lonlat[lonIdx][t] -= 360f;
           }
         }
       }
       else {
         for (int t=0; t<lonlat[lonIdx].length; t++) {
           if (lonlat[lonIdx][t] < 180f) {
             lonlat[lonIdx][t] += 360f;
           }
         }
       }
     }

     return lonlat;
   }

   public double[][] toReference(double[][] values) throws VisADException {
     return Set.floatToDouble(toReference(Set.doubleToFloat(values)));
   }

   public float[][] fromReference(float[][] lonlat) throws VisADException {
     if (!(neg180pos180 && extNeg180pos180)) { // if true lonRanges are same so don't do anything
       if (neg180pos180) {
         for (int t=0; t<lonlat[lonIdx].length; t++) {
           if (lonlat[lonIdx][t] > 180f) {
             lonlat[lonIdx][t] -= 360f;
           }
         }
       } 
       else {
         for (int t=0; t<lonlat[lonIdx].length; t++) {
           if (lonlat[lonIdx][t] < 180f) {
             lonlat[lonIdx][t] += 360f;
           }
         }
       }
     }

     float[][] grid_vals = lonlatSet.valueToGrid(lonlat);
     float[][] coords = transSet.valueToGrid(grid_vals);
     coords = domainSet.gridToValue(coords);
     return coords;
   }

   public double[][] fromReference(double[][] lonlat) throws VisADException {
     return Set.floatToDouble(fromReference(Set.doubleToFloat(lonlat)));
   }

   public void setExternalLongitudeRange(boolean isExtNeg180pos180) {
     this.extNeg180pos180 = isExtNeg180pos180;
   }

   public Rectangle2D getDefaultMapArea() {
     float[] lo = domainSet.getLow();
     float[] hi = domainSet.getHi();
     return new Rectangle2D.Float(lo[0], lo[1], hi[0] - lo[0], hi[1] - lo[1]);
   }

   public Linear2DSet getDomainSet() {
     return domainSet;
   }

   public Gridded2DSet getLonLatSet() {
     return lonlatSet;
   }

   public boolean getIsNeg180pos180() {
     return neg180pos180;
   }

   /**
    *  Will convert to external (incoming/outgoing) longitude 
    *  range, ie. -180 to 180 back/forth to 0 to 360.  Default is:
    *  external range is 0 to 360   
    */
   public void setIsExtNeg180pos180(boolean yesno) {
     extNeg180pos180 = yesno;
   }

   public boolean getIsExtNeg180pos180() {
     return extNeg180pos180;
   }

   public boolean equals(Object cs) {
     if ( !(cs instanceof LongitudeLatitudeInterpCS)) {
       return false;
     }

     LongitudeLatitudeInterpCS that = (LongitudeLatitudeInterpCS) cs;

     // Compare sets.  Note: comparing the lonlat GriddedSet(s) could be very costly
     if (that.getDomainSet().equals(domainSet) && that.getLonLatSet().equals(lonlatSet)) {
       return true;
     }
     if ((that.getIsNeg180pos180() == neg180pos180) && (that.getIsExtNeg180pos180() == extNeg180pos180)) {
       return true;
     }

     return false;
   } 
}
