
//
// Display.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.util.*;
import java.rmi.*;


/*
   WLH - NOTE

   need to do something about ConstantMap-s, DefaultValue-s
   and ShadowRealTupleType.permutation

   WLH - NOTE

*/


/**
   Display is the VisAD interface for displays.  It is runnable.<P>

   The display architecture is based on three goals:<P>
<UL>

<LI> 1. Display data according to a set of mappings from RealType's
        (e.g., Latitude, Time, Pressure) to DisplayRealType's (e.g.,
        XAxis, RGB, Animation).
   
<LI> 2. Allow user extensions, to define new DisplayRealType's,
        new DisplayRealTuple's (and hence new display
        CoordinateSsystem's), and new rendering algorithms.
   
<LI> 3. Support direct manipulation through 3-D user interface
        widgets embedded in 3-D data displays.

</UL>
*/
public interface Display extends Action {

  /** system intrinsic DisplayRealType objects */
  /** spatial display scalars */
  public final static DisplayRealType XAxis =
    new DisplayRealType("XAxis", true, -1.0, 1.0, 0.0,
                        ProjectionControl.prototype, true);
  public final static DisplayRealType YAxis =
    new DisplayRealType("YAxis", true, -1.0, 1.0, 0.0,
                        ProjectionControl.prototype, true);
  public final static DisplayRealType ZAxis =
    new DisplayRealType("ZAxis", true, -1.0, 1.0, 0.0,
                        ProjectionControl.prototype, true);

  public final static DisplayRealType Latitude =
    new DisplayRealType("Latitude", true, -180.0, 180.0, 0.0,
                        ProjectionControl.prototype, Unit.degree, true);
  public final static DisplayRealType Longitude =
    new DisplayRealType("Longitude", true, 0.0, 360.0, 0.0,
                        ProjectionControl.prototype, Unit.degree, true);
  public final static DisplayRealType Radius =
    new DisplayRealType("Radius", true, 0.0, 2.0, 1.0,
                        ProjectionControl.prototype, true);

  /** list display scalar (default domain of DisplayField) */
  public final static DisplayRealType List =
    new DisplayRealType("List", false, 0.0, null, true);

  /** color display scalars */
  public final static DisplayRealType Red =
    new DisplayRealType("Red", false, 0.0, 1.0, 1.0, null, true);
  public final static DisplayRealType Green =
    new DisplayRealType("Green", false, 0.0, 1.0, 1.0, null, true);
  public final static DisplayRealType Blue =
    new DisplayRealType("Blue", false, 0.0, 1.0, 1.0, null, true);

  public final static DisplayRealType RGB =
    new DisplayRealType("RGB", false, 0.0, 1.0, 0.0,
                        ColorControl.prototype, true);

  public final static DisplayRealType Hue =
    new DisplayRealType("Hue", false, 0.0, 1.0, 0.0, null, true);
  public final static DisplayRealType Saturation =
    new DisplayRealType("Saturation", false, 0.0, 1.0, 0.0, null, true);
  public final static DisplayRealType Value =
    new DisplayRealType("Value", false, 0.0, 1.0, 1.0, null, true);

  public final static DisplayRealType HSV =
    new DisplayRealType("HSV", false, 0.0, 1.0, 0.0,
                        ColorControl.prototype, true);

  public final static DisplayRealType Cyan =
    new DisplayRealType("Cyan", false, 0.0, 1.0, 1.0, null, true);
  public final static DisplayRealType Magenta =
    new DisplayRealType("Magenta", false, 0.0, 1.0, 1.0, null, true);
  public final static DisplayRealType Yellow =
    new DisplayRealType("Yellow", false, 0.0, 1.0, 1.0, null, true);

  public final static DisplayRealType CMY =
    new DisplayRealType("CMY", false, 0.0, 1.0, 0.0,
                        ColorControl.prototype, true);

  /** Alpha is transparency */
  public final static DisplayRealType Alpha =
    new DisplayRealType("Alpha", false, 0.0, 1.0, 1.0, null, true);

  /** animation display scalar */
  public final static DisplayRealType Animation =
    new DisplayRealType("Animation", true, 0.0, 1.0, 0.0,
                        AnimationControl.prototype, true);

  /** display scalar for selecting by a single value */
  public final static DisplayRealType SelectValue =
    new DisplayRealType("SelectValue", false, 0.0, ValueControl.prototype, true);

  /** display scalar for selecting by a range of values */
  public final static DisplayRealType SelectRange =
    new DisplayRealType("SelectRange", false, 0.0, RangeControl.prototype, true);

  /** iso-contour display scalar */
  public final static DisplayRealType IsoContour =
    new DisplayRealType("IsoContour", false, 0.0, ContourControl.prototype, true);

  /** three flow display scalars */
  public final static DisplayRealType Flow1X =
    new DisplayRealType("Flow1X", true, -1.0, 1.0, 0.0,
                        Flow1Control.prototype, true);
  public final static DisplayRealType Flow1Y =
    new DisplayRealType("Flow1Y", true, -1.0, 1.0, 0.0,
                        Flow1Control.prototype, true);
  public final static DisplayRealType Flow1Z =
    new DisplayRealType("Flow1Z", true, -1.0, 1.0, 0.0,
                        Flow1Control.prototype, true);

  /** second set of three flow display scalars */
  public final static DisplayRealType Flow2X =
    new DisplayRealType("Flow2X", true, -1.0, 1.0, 0.0,
                        Flow2Control.prototype, true);
  public final static DisplayRealType Flow2Y =
    new DisplayRealType("Flow2Y", true, -1.0, 1.0, 0.0,
                        Flow2Control.prototype, true);
  public final static DisplayRealType Flow2Z =
    new DisplayRealType("Flow2Z", true, -1.0, 1.0, 0.0,
                        Flow2Control.prototype, true);

  /** shape display scalar
      WLH - this should be a DisplayEnumeratedType */
  public final static DisplayRealType Shape =
    new DisplayRealType("Shape", false, 0.0, null, true);

  /** spatial offset display scalars */
  public final static DisplayRealType XAxisOffset =
    new DisplayRealType("XAxisOffset", false, -1.0, 1.0, 0.0, null, true);
  public final static DisplayRealType YAxisOffset =
    new DisplayRealType("YAxisOffset", false, -1.0, 1.0, 0.0, null, true);
  public final static DisplayRealType ZAxisOffset =
    new DisplayRealType("ZAxisOffset", false, -1.0, 1.0, 0.0, null, true);

  /** array of system intrinsic display scalars */
  final static DisplayRealType[] DisplayRealArray =
    {XAxis, YAxis, ZAxis, Latitude, Longitude, Radius, List, Red, Green, Blue,
     RGB, Hue, Saturation, Value, HSV, Cyan, Magenta, Yellow, CMY, Alpha,
     Animation, SelectValue, SelectRange, IsoContour, Flow1X, Flow1Y, Flow1Z,
     Flow2X, Flow2Y, Flow2Z, Shape, XAxisOffset, YAxisOffset, ZAxisOffset};


  /** system intrinsic DisplayTupleType objects */
  /** system intrinsic DisplayTupleType for 3D Cartesian Spatial Coordinates */
  public final static DisplayRealType[] components3c =
          {Display.XAxis, Display.YAxis, Display.ZAxis};
  public final static DisplayTupleType DisplaySpatialCartesianTuple =
    new DisplayTupleType(components3c, true);

  /** system intrinsic DisplayTupleType for 3D Spherical Spatial Coordinates,
      this defines a CoordinateSystem with Reference
      DisplaySpatialCartesianTuple */
  public final static CoordinateSystem DisplaySphericalCoordSys =
    new SphericalCoordinateSystem(DisplaySpatialCartesianTuple, true);
  public static DisplayRealType[] components3s =
          {Latitude, Longitude, Radius};
  public final static DisplayTupleType DisplaySpatialSphericalTuple =
    new DisplayTupleType(components3s, DisplaySphericalCoordSys, true);

  /** system intrinsic DisplayTupleType for RGB Color Coordinates */
  public final static DisplayRealType[] componentsrgb =
          {Red, Green, Blue};
  public final static DisplayTupleType DisplayRGBTuple =
    new DisplayTupleType(componentsrgb, true);

  /** system intrinsic DisplayTupleType for HSV Color Coordinates */
  public final static CoordinateSystem DisplayHSVCoordSys =
    new HSVCoordinateSystem(DisplayRGBTuple, true);
  public final static DisplayRealType[] componentshsv =
          {Hue, Saturation, Value};
  public final static DisplayTupleType DisplayHSVTuple =
    new DisplayTupleType(componentshsv, DisplayHSVCoordSys, true);

  /** system intrinsic DisplayTupleType for CMY Color Coordinates */
  public final static CoordinateSystem DisplayCMYCoordSys =
    new CMYCoordinateSystem(DisplayRGBTuple, true);
  public final static DisplayRealType[] componentscmy =
          {Cyan, Magenta, Yellow};
  public final static DisplayTupleType DisplayCMYTuple =
    new DisplayTupleType(componentscmy, DisplayCMYCoordSys, true);

  /** system intrinsic DisplayTupleType for first set of Flow components */
  public final static DisplayRealType[] componentsflow1 =
          {Flow1X, Flow1Y, Flow1Z};
  public final static DisplayTupleType DisplayFlow1Tuple =
    new DisplayTupleType(componentsflow1, true);

  /** system intrinsic DisplayTupleType for second set of Flow components */
  public final static DisplayRealType[] componentsflow2 =
          {Flow2X, Flow2Y, Flow2Z};
  public final static DisplayTupleType DisplayFlow2Tuple =
    new DisplayTupleType(componentsflow2, true);

  /** system intrinsic DisplayTupleType for Spatial Offset Coordinates */
  public final static DisplayRealType[] componentsso =
          {XAxisOffset, YAxisOffset, ZAxisOffset};
  public final static DisplayTupleType DisplaySpatialOffsetTuple =
    new DisplayTupleType(componentsso, true);


  /** create link to DataReference;
      invokes ref.addDataChangedListener(Action a) */
  public abstract void addReference(DataReference ref,
         ConstantMap[] constant_maps) throws VisADException, RemoteException;

  /** add a ScalarMap to this Display */
  public abstract void addMap(ScalarMap map)
         throws VisADException, RemoteException;

  /** clear set of SalarMap-s associated with this display */
  public abstract void clearMaps() throws VisADException, RemoteException;

}

