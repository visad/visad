//
// Display.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

import java.util.*;
import java.rmi.*;

/**
   Display is the VisAD interface for displays.  It is runnable.<P>

   The display architecture is based on three goals:<P>
<OL>

<LI>Display data according to a set of mappings from RealType's
    (e.g., Latitude, Time, Pressure) to DisplayRealType's (e.g.,
    XAxis, RGB, Animation).<P>

<LI>Allow user extensions, to define new DisplayRealType's,
    new DisplayRealTuple's (and hence new display
    CoordinateSsystem's), and new rendering algorithms.<P>

<LI>Support direct manipulation through 3-D user interface
    widgets embedded in 3-D data displays.<P>

</OL>
*/
public interface Display extends Action {

  /** system intrinsic DisplayRealType objects */
  /** spatial display scalars */
  DisplayRealType XAxis =
    new DisplayRealType("XAxis", true, -1.0, 1.0, 0.0, true);
  DisplayRealType YAxis =
    new DisplayRealType("YAxis", true, -1.0, 1.0, 0.0, true);
  DisplayRealType ZAxis =
    new DisplayRealType("ZAxis", true, -1.0, 1.0, 0.0, true);

  DisplayRealType Latitude =
    new DisplayRealType("Latitude", true, -90.0, 90.0, 0.0,
                        CommonUnit.degree, true);
  DisplayRealType Longitude =
    new DisplayRealType("Longitude", true, 0.0, 360.0, 0.0,
                        CommonUnit.degree, true);
  DisplayRealType Radius =
    new DisplayRealType("Radius", true, 0.01, 2.0, 1.0, true);

  /** Cylindrical radius scalar */
  DisplayRealType CylRadius =
    new DisplayRealType("CylRadius", true, 0.01, 2.0, 1.0, true);
  /** Cylindrical azimuth scalar */
  DisplayRealType CylAzimuth =
    new DisplayRealType("CylAzimuth", true, 0.0, 360.0, 0.0,
                        CommonUnit.degree, true);
  /** Cylindrical ZAxis scalar */
  DisplayRealType CylZAxis =
    new DisplayRealType("CylZAxis", true, -1.0, 1.0, 0.0, true);

  /** list display scalar (default domain of DisplayField) */
  DisplayRealType List =
    new DisplayRealType("List", false, 0.0, true);

  /** color display scalars */
  DisplayRealType Red =
    new DisplayRealType("Red", false, 0.0, 1.0, 1.0, null, true);
  DisplayRealType Green =
    new DisplayRealType("Green", false, 0.0, 1.0, 1.0, null, true);
  DisplayRealType Blue =
    new DisplayRealType("Blue", false, 0.0, 1.0, 1.0, null, true);

  DisplayRealType RGB =
    new DisplayRealType("RGB", false, 0.0, 1.0, 0.0, true);

  DisplayRealType RGBA =
    new DisplayRealType("RGBA", false, 0.0, 1.0, 0.0, true);

  DisplayRealType Hue =
    new DisplayRealType("Hue", false, 0.0, 360.0, 0.0,
                       CommonUnit.degree, true);
  DisplayRealType Saturation =
    new DisplayRealType("Saturation", false, 0.0, 1.0, 0.0, null, true);
  DisplayRealType Value =
    new DisplayRealType("Value", false, 0.0, 1.0, 1.0, null, true);

  DisplayRealType HSV =
    new DisplayRealType("HSV", false, 0.0, 1.0, 0.0, true);

  DisplayRealType Cyan =
    new DisplayRealType("Cyan", false, 0.0, 1.0, 1.0, null, true);
  DisplayRealType Magenta =
    new DisplayRealType("Magenta", false, 0.0, 1.0, 1.0, null, true);
  DisplayRealType Yellow =
    new DisplayRealType("Yellow", false, 0.0, 1.0, 1.0, null, true);

  DisplayRealType CMY =
    new DisplayRealType("CMY", false, 0.0, 1.0, 0.0, true);

  /** Alpha is transparency */
  DisplayRealType Alpha =
    new DisplayRealType("Alpha", false, 0.0, 1.0, 1.0, null, true);

  /** animation display scalar */
  DisplayRealType Animation =
    new DisplayRealType("Animation", true, 0.0, true);

  /** display scalar for selecting by a single value */
  DisplayRealType SelectValue =
    new DisplayRealType("SelectValue", false, 0.0, true);

  /** display scalar for selecting by a range of values */
  DisplayRealType SelectRange =
    new DisplayRealType("SelectRange", false, 0.0, true);

  /** iso-contour display scalar */
  DisplayRealType IsoContour =
    new DisplayRealType("IsoContour", false, 0.0, true);

  /** three flow display scalars */
  DisplayRealType Flow1X =
    new DisplayRealType("Flow1X", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);
  DisplayRealType Flow1Y =
    new DisplayRealType("Flow1Y", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);
  DisplayRealType Flow1Z =
    new DisplayRealType("Flow1Z", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);

  DisplayRealType Flow1Elevation =
    new DisplayRealType("Flow1Elevation", true, -90.0, 90.0, 0.0,
                        CommonUnit.degree, true);
  DisplayRealType Flow1Azimuth =
    new DisplayRealType("Flow1Azimuth", true, 0.0, 360.0, 0.0,
                        CommonUnit.degree, true);
  DisplayRealType Flow1Radial =
    new DisplayRealType("Flow1Radial", true, 0.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);

  /** second set of three flow display scalars */
  DisplayRealType Flow2X =
    new DisplayRealType("Flow2X", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);
  DisplayRealType Flow2Y =
    new DisplayRealType("Flow2Y", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);
  DisplayRealType Flow2Z =
    new DisplayRealType("Flow2Z", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);

  DisplayRealType Flow2Elevation =
    new DisplayRealType("Flow2Elevation", true, -90.0, 90.0, 0.0,
                        CommonUnit.degree, true);
  DisplayRealType Flow2Azimuth =
    new DisplayRealType("Flow2Azimuth", true, 0.0, 360.0, 0.0,
                        CommonUnit.degree, true);
  DisplayRealType Flow2Radial =
    new DisplayRealType("Flow2Radial", true, 0.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);

  /** spatial offset display scalars */
  DisplayRealType XAxisOffset =
    new DisplayRealType("XAxisOffset", false, -1.0, 1.0, 0.0, null, true);
  DisplayRealType YAxisOffset =
    new DisplayRealType("YAxisOffset", false, -1.0, 1.0, 0.0, null, true);
  DisplayRealType ZAxisOffset =
    new DisplayRealType("ZAxisOffset", false, -1.0, 1.0, 0.0, null, true);

  /** shape display scalar
      WLH - this should be a DisplayEnumeratedType */
  DisplayRealType Shape =
    new DisplayRealType("Shape", false, 0.0, true);

  /** scale for Shape */
  DisplayRealType ShapeScale =
    new DisplayRealType("ShapeScale", true, 0.01, 1.0, 1.0, true);

  /** text display scalar
      WLH - this should be a DisplayTextType */
  DisplayRealType Text =
    new DisplayRealType("Text", true, true);

  /** point size, line width and line style - ConstantMap only */
  DisplayRealType LineWidth =
    new DisplayRealType("LineWidth", true, 1.0, true);
  DisplayRealType PointSize =
    new DisplayRealType("PointSize", true, 1.0, true);
  DisplayRealType LineStyle =
    new DisplayRealType("LineStyle", true, 1.0, true);

  /** array of system intrinsic display scalars */
  DisplayRealType[] DisplayRealArray =
    {XAxis, YAxis, ZAxis, Latitude, Longitude, Radius, List, Red, Green, Blue,
     RGB, RGBA, Hue, Saturation, Value, HSV, Cyan, Magenta, Yellow, CMY, Alpha,
     Animation, SelectValue, SelectRange, IsoContour, Flow1X, Flow1Y, Flow1Z,
     Flow2X, Flow2Y, Flow2Z, XAxisOffset, YAxisOffset, ZAxisOffset, Shape,
     Text, ShapeScale, LineWidth, PointSize, CylRadius, CylAzimuth, CylZAxis,
     Flow1Elevation, Flow1Azimuth, Flow1Radial,
     Flow2Elevation, Flow2Azimuth, Flow2Radial, LineStyle};

  /** system intrinsic DisplayTupleType objects */
  /** system intrinsic DisplayTupleType for 3D Cartesian Spatial Coordinates */
  DisplayRealType[] components3c =
          {Display.XAxis, Display.YAxis, Display.ZAxis};
  DisplayTupleType DisplaySpatialCartesianTuple =
    new DisplayTupleType(components3c, true);

  /** system intrinsic DisplayTupleType for 3D Spherical Spatial Coordinates,
      this defines a CoordinateSystem with Reference
      DisplaySpatialCartesianTuple */
  CoordinateSystem DisplaySphericalCoordSys =
    new SphericalCoordinateSystem(DisplaySpatialCartesianTuple, true);
  DisplayRealType[] components3s =
          {Latitude, Longitude, Radius};
  DisplayTupleType DisplaySpatialSphericalTuple =
    new DisplayTupleType(components3s, DisplaySphericalCoordSys, true);

  /**
   * defines a CoordinateSystem with Reference DisplaySpatialCartesianTuple
   */
  CoordinateSystem DisplayCylindricalCoordSys =
    new CylindricalCoordinateSystem(DisplaySpatialCartesianTuple, true);
  /**
   * DisplayRealType array of CylRadius, CylAzimuth, CylZAxis
   * for Cylindrical Coordinates
   */
  DisplayRealType[] componentscyl =
          {CylRadius, CylAzimuth, CylZAxis};
  /**
   * System intrinsic DisplayTupleType for Cylindrical Coordinates
   */
  DisplayTupleType DisplaySpatialCylindricalTuple =
    new DisplayTupleType(componentscyl, DisplayCylindricalCoordSys, true);

  /** system intrinsic DisplayTupleType for RGB Color Coordinates */
  DisplayRealType[] componentsrgb =
          {Red, Green, Blue};
  DisplayTupleType DisplayRGBTuple =
    new DisplayTupleType(componentsrgb, true);

  /** system intrinsic DisplayTupleType for HSV Color Coordinates */
  CoordinateSystem DisplayHSVCoordSys =
    new HSVCoordinateSystem(DisplayRGBTuple, true);
  DisplayRealType[] componentshsv =
          {Hue, Saturation, Value};
  DisplayTupleType DisplayHSVTuple =
    new DisplayTupleType(componentshsv, DisplayHSVCoordSys, true);

  /** system intrinsic DisplayTupleType for CMY Color Coordinates */
  CoordinateSystem DisplayCMYCoordSys =
    new CMYCoordinateSystem(DisplayRGBTuple, true);
  DisplayRealType[] componentscmy =
          {Cyan, Magenta, Yellow};
  DisplayTupleType DisplayCMYTuple =
    new DisplayTupleType(componentscmy, DisplayCMYCoordSys, true);

  /** system intrinsic DisplayTupleType for first set of Flow components */
  DisplayRealType[] componentsflow1 =
          {Flow1X, Flow1Y, Flow1Z};
  DisplayTupleType DisplayFlow1Tuple =
    new DisplayTupleType(componentsflow1, true);

  /** system intrinsic DisplayTupleType for second set of Flow components */
  DisplayRealType[] componentsflow2 =
          {Flow2X, Flow2Y, Flow2Z};
  DisplayTupleType DisplayFlow2Tuple =
    new DisplayTupleType(componentsflow2, true);

  /** system intrinsic DisplayTupleType for first set of 3D Spherical
      Flow Coordinates, this defines a CoordinateSystem with Reference
      DisplayFlow1Tuple */
  CoordinateSystem DisplayFlow1SphericalCoordSys =
    new FlowSphericalCoordinateSystem(DisplayFlow1Tuple, true);
  DisplayRealType[] componentsflow1s =
          {Flow1Elevation, Flow1Azimuth, Flow1Radial};
  DisplayTupleType DisplayFlow1SphericalTuple =
    new DisplayTupleType(componentsflow1s, DisplayFlow1SphericalCoordSys, true);

  /** system intrinsic DisplayTupleType for second set of 3D Spherical
      Flow Coordinates, this defines a CoordinateSystem with Reference
      DisplayFlow2Tuple */
  CoordinateSystem DisplayFlow2SphericalCoordSys =
    new FlowSphericalCoordinateSystem(DisplayFlow2Tuple, true);
  DisplayRealType[] componentsflow2s =
          {Flow2Elevation, Flow2Azimuth, Flow2Radial};
  DisplayTupleType DisplayFlow2SphericalTuple =
    new DisplayTupleType(componentsflow2s, DisplayFlow2SphericalCoordSys, true);

  /** system intrinsic DisplayTupleType for Spatial Offset Coordinates */
  DisplayRealType[] componentsso =
          {XAxisOffset, YAxisOffset, ZAxisOffset};
  DisplayTupleType DisplaySpatialOffsetTuple =
    new DisplayTupleType(componentsso, true);


  /** create link to DataReference;
      invokes ref.addThingChangedListener(ThingChangedListener l, long id) */
  void addReference(DataReference ref,
         ConstantMap[] constant_maps) throws VisADException, RemoteException;

  /** create link to DataReference;
      invokes ref.addThingChangedListener(ThingChangedListener l, long id) */
  void addReferences(DataRenderer renderer, DataReference ref,
                            ConstantMap[] constant_maps)
         throws VisADException, RemoteException;

  /** link map to this Display; this method may not be invoked
      after any links to DataReferences have been made */
  void addMap(ScalarMap map)
         throws VisADException, RemoteException;

  /** clear set of ScalarMap-s associated with this display */
  void clearMaps() throws VisADException, RemoteException;

  /** destroy this display */
  void destroy() throws VisADException, RemoteException;

  Vector getConstantMapVector()
         throws VisADException, RemoteException;

  /**
   * Send a message to all </tt>MessageListener</tt>s.
   *
   * @param msg Message being sent.
   */
  void sendMessage(MessageEvent msg)
    throws RemoteException;
}

