//
// Display.java
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

package visad;

import java.util.*;
import java.rmi.*;

/**
   Display is the VisAD interface for displays.  It is runnable.<P>

   The display architecture is based on three goals:<P>
<OL>

<LI>Display data according to a set of mappings from RealType's
    (e.g., Latitude, Time, Pressure) to DisplayRealTypes (e.g.,
    XAxis, RGB, Animation).<P>

<LI>Allow user extensions, to define new DisplayRealTypes,
    new DisplayTupleTypes (and hence new display
    CoordinateSystems), and new rendering algorithms.<P>

<LI>Support direct manipulation, where users modify Data
    values by re-drawing their depictions.<P>

</OL>
*/
public interface Display extends Action {

  // system intrinsic DisplayRealType objects

  /** display spatial Cartesian X axis coordinate */
  DisplayRealType XAxis =
    new DisplayRealType("XAxis", true, -1.0, 1.0, 0.0, true);

  /** display spatial Cartesian Y axis coordinate */
  DisplayRealType YAxis =
    new DisplayRealType("YAxis", true, -1.0, 1.0, 0.0, true);

  /** display spatial Cartesian Z axis coordinate */
  DisplayRealType ZAxis =
    new DisplayRealType("ZAxis", true, -1.0, 1.0, 0.0, true);


  /** display spatial spherical Latitude coordinate */
  DisplayRealType Latitude =
    new DisplayRealType("Latitude", true, -90.0, 90.0, 0.0,
                        CommonUnit.degree, true);

  /** display spatial spherical Longitude coordinate */
  DisplayRealType Longitude =
    new DisplayRealType("Longitude", true, 0.0, 360.0, 0.0,
                        CommonUnit.degree, true);

  /** display spatial spherical Radius coordinate */
  DisplayRealType Radius =
    new DisplayRealType("Radius", true, 0.01, 2.0, 1.0, true);


  /** display spatial cylindrical radius coordinate */
  DisplayRealType CylRadius =
    new DisplayRealType("CylRadius", true, 0.01, 2.0, 1.0, true);

  /** display spatial cylindrical azimuth coordinate */
  DisplayRealType CylAzimuth =
    new DisplayRealType("CylAzimuth", true, 0.0, 360.0, 0.0,
                        CommonUnit.degree, true);

  /** display spatial cylindrical Z axis coordinate */
  DisplayRealType CylZAxis =
    new DisplayRealType("CylZAxis", true, -1.0, 1.0, 0.0, true);


  /** display spatial offset Cartesian X axis coordinate */
  DisplayRealType XAxisOffset =
    new DisplayRealType("XAxisOffset", false, -1.0, 1.0, 0.0, null, true);

  /** display spatial offset Cartesian Y axis coordinate */
  DisplayRealType YAxisOffset =
    new DisplayRealType("YAxisOffset", false, -1.0, 1.0, 0.0, null, true);

  /** display spatial offset Cartesian Z axis coordinate */
  DisplayRealType ZAxisOffset =
    new DisplayRealType("ZAxisOffset", false, -1.0, 1.0, 0.0, null, true);


  /** list display scalar (not used) */
  DisplayRealType List =
    new DisplayRealType("List", false, 0.0, true);

  /** display color red coordinate (in RGB) */
  DisplayRealType Red =
    new DisplayRealType("Red", false, 0.0, 1.0, 1.0, null, true);

  /** display color green coordinate (in RGB) */
  DisplayRealType Green =
    new DisplayRealType("Green", false, 0.0, 1.0, 1.0, null, true);

  /** display color blue coordinate (in RGB) */
  DisplayRealType Blue =
    new DisplayRealType("Blue", false, 0.0, 1.0, 1.0, null, true);


  /** display color RGB lookup table index */
  DisplayRealType RGB =
    new DisplayRealType("RGB", false, 0.0, 1.0, 0.0, true);


  /** display color RGBA lookup table index */
  DisplayRealType RGBA =
    new DisplayRealType("RGBA", false, 0.0, 1.0, 0.0, true);


  /** display color hue coordinate (in HSV) */
  DisplayRealType Hue =
    new DisplayRealType("Hue", false, 0.0, 360.0, 0.0,
                       CommonUnit.degree, true);

  /** display color saturation coordinate (in HSV) */
  DisplayRealType Saturation =
    new DisplayRealType("Saturation", false, 0.0, 1.0, 0.0, null, true);

  /** display color value coordinate (in HSV) */
  DisplayRealType Value =
    new DisplayRealType("Value", false, 0.0, 1.0, 1.0, null, true);


  /** display color HSV lookup table index */
  DisplayRealType HSV =
    new DisplayRealType("HSV", false, 0.0, 1.0, 0.0, true);


  /** display color cyan coordinate (in CMY) */
  DisplayRealType Cyan =
    new DisplayRealType("Cyan", false, 0.0, 1.0, 1.0, null, true);

  /** display color magenta coordinate (in CMY) */
  DisplayRealType Magenta =
    new DisplayRealType("Magenta", false, 0.0, 1.0, 1.0, null, true);

  /** display color yellow coordinate (in CMY) */
  DisplayRealType Yellow =
    new DisplayRealType("Yellow", false, 0.0, 1.0, 1.0, null, true);


  /** display color CMY lookup table index */
  DisplayRealType CMY =
    new DisplayRealType("CMY", false, 0.0, 1.0, 0.0, true);


  /** display alpha (transparency) */
  DisplayRealType Alpha =
    new DisplayRealType("Alpha", false, 0.0, 1.0, 1.0, null, true);


  /** display animation */
  DisplayRealType Animation =
    new DisplayRealType("Animation", true, 0.0, true);


  /** display scalar for selecting by a single value */
  DisplayRealType SelectValue =
    new DisplayRealType("SelectValue", false, 0.0, true);


  /** display scalar for selecting by a range of values */
  DisplayRealType SelectRange =
    new DisplayRealType("SelectRange", false, 0.0, true);


  /** display iso-contour */
  DisplayRealType IsoContour =
    new DisplayRealType("IsoContour", false, 0.0, true);


  /** display flow set 1 Cartesian X axis coordinate */
  DisplayRealType Flow1X =
    new DisplayRealType("Flow1X", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);

  /** display flow set 1 Cartesian Y axis coordinate */
  DisplayRealType Flow1Y =
    new DisplayRealType("Flow1Y", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);

  /** display flow set 1 Cartesian Z axis coordinate */
  DisplayRealType Flow1Z =
    new DisplayRealType("Flow1Z", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);


  /** display flow set 1 spherical elevation coordinate */
  DisplayRealType Flow1Elevation =
    new DisplayRealType("Flow1Elevation", true, -90.0, 90.0, 0.0,
                        CommonUnit.degree, true);

  /** display flow set 1 spherical azimuth coordinate */
  DisplayRealType Flow1Azimuth =
    new DisplayRealType("Flow1Azimuth", true, 0.0, 360.0, 0.0,
                        CommonUnit.degree, true);

  /** display flow set 1 spherical radial coordinate */
  DisplayRealType Flow1Radial =
    new DisplayRealType("Flow1Radial", true, 0.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);


  /** display flow set 2 Cartesian X axis coordinate */
  DisplayRealType Flow2X =
    new DisplayRealType("Flow2X", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);

  /** display flow set 2 Cartesian Y axis coordinate */
  DisplayRealType Flow2Y =
    new DisplayRealType("Flow2Y", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);

  /** display flow set 2 Cartesian Z axis coordinate */
  DisplayRealType Flow2Z =
    new DisplayRealType("Flow2Z", true, -1.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);


  /** display flow set 2 spherical elevation coordinate */
  DisplayRealType Flow2Elevation =
    new DisplayRealType("Flow2Elevation", true, -90.0, 90.0, 0.0,
                        CommonUnit.degree, true);

  /** display flow set 2 spherical azimuth coordinate */
  DisplayRealType Flow2Azimuth =
    new DisplayRealType("Flow2Azimuth", true, 0.0, 360.0, 0.0,
                        CommonUnit.degree, true);

  /** display flow set 2 spherical radial coordinate */
  DisplayRealType Flow2Radial =
    new DisplayRealType("Flow2Radial", true, 0.0, 1.0, 0.0,
                        CommonUnit.meterPerSecond, true);


  /** index into a set of display shapes */
  DisplayRealType Shape =
    new DisplayRealType("Shape", false, 0.0, true);

  /** scale for display shapes */
  DisplayRealType ShapeScale =
    new DisplayRealType("ShapeScale", true, 0.01, 1.0, 1.0, true);


  /** display scalar for text */
  DisplayRealType Text =
    new DisplayRealType("Text", true, true);


  /** line width - ConstantMap only */
  DisplayRealType LineWidth =
    new DisplayRealType("LineWidth", true, 1.0, true);

  /** point size - ConstantMap only */
  DisplayRealType PointSize =
    new DisplayRealType("PointSize", true, 1.0, true);

  /** line style - ConstantMap only */
  DisplayRealType LineStyle =
    new DisplayRealType("LineStyle", true, 1.0, true);

  /** texture enable - ConstantMap only */
  DisplayRealType TextureEnable =
    new DisplayRealType("TextureEnable", true, -1.0, true);

  /** missing transparent - ConstantMap only */
  DisplayRealType MissingTransparent =
    new DisplayRealType("MissingTransparent", true, -1.0, true);

  /** polygon mode - ConstantMap only */
  DisplayRealType PolygonMode =
    new DisplayRealType("PolygonMode", true, -1.0, true);

  /** curved size - ConstantMap only, values must be > 0 */
  DisplayRealType CurvedSize =
    new DisplayRealType("CurvedSize", true, 0.0, true);

  /** color mode - ConstantMap only, values must be > 0 */
  DisplayRealType ColorMode =
    new DisplayRealType("ColorMode", true, -1.0, true);

  /** Polygon offset - ConstantMap only */
  DisplayRealType PolygonOffset =
    new DisplayRealType("PolygonOffset", true, Double.NaN, true);

  /** Polygon offset factor - ConstantMap only */
  DisplayRealType PolygonOffsetFactor =
    new DisplayRealType("PolygonOffsetFactor", true, Double.NaN, true);

  /** Adjust along projection seams - ConstantMap only */
  DisplayRealType AdjustProjectionSeam =
    new DisplayRealType("AdjustProjectionSeam", true, -1.0, true);

  /** texture 3D mode - ConstantMap only, values must be > 0 */
  DisplayRealType Texture3DMode =
    new DisplayRealType("Texture3DMode", true, -1.0, true);

  /** cache appearances - ConstantMap only */
  DisplayRealType CacheAppearances =
    new DisplayRealType("CacheAppearances", true, -1.0, true);

  /** cache appearances - ConstantMap only */
  DisplayRealType MergeGeometries =
    new DisplayRealType("MergeGeometries", true, -1.0, true);

  /** point mode - ConstantMap only */
  DisplayRealType PointMode =
    new DisplayRealType("PointMode", true, -1.0, true);

  /** array of system intrinsic DisplayRealTypes */
  DisplayRealType[] DisplayRealArray =
    {XAxis, YAxis, ZAxis, Latitude, Longitude, Radius, List, Red, Green, Blue,
     RGB, RGBA, Hue, Saturation, Value, HSV, Cyan, Magenta, Yellow, CMY, Alpha,
     Animation, SelectValue, SelectRange, IsoContour, Flow1X, Flow1Y, Flow1Z,
     Flow2X, Flow2Y, Flow2Z, XAxisOffset, YAxisOffset, ZAxisOffset, Shape,
     Text, ShapeScale, LineWidth, PointSize, CylRadius, CylAzimuth, CylZAxis,
     Flow1Elevation, Flow1Azimuth, Flow1Radial,
     Flow2Elevation, Flow2Azimuth, Flow2Radial, 
     LineStyle, TextureEnable, MissingTransparent, 
     PolygonMode, CurvedSize, ColorMode, 
     PolygonOffset, PolygonOffsetFactor, 
     AdjustProjectionSeam, Texture3DMode,
     CacheAppearances, MergeGeometries, PointMode
     };

  // system intrinsic DisplayTupleType objects

  /** array of 3D Cartesian spatial coordinates */
  DisplayRealType[] components3c =
          {Display.XAxis, Display.YAxis, Display.ZAxis};

  /** system intrinsic DisplayTupleType for 3D Cartesian
      spatial coordinates */
  DisplayTupleType DisplaySpatialCartesianTuple =
    new DisplayTupleType(components3c, true);


  /** CoordinateSystem for DisplaySpatialSphericalTuple, with
      reference DisplaySpatialCartesianTuple */
  CoordinateSystem DisplaySphericalCoordSys =
    new SphericalCoordinateSystem(DisplaySpatialCartesianTuple, true);

  /** array of 3D spherical spatial coordinates */
  DisplayRealType[] components3s =
          {Latitude, Longitude, Radius};

  /** system intrinsic DisplayTupleType for 3D spherical
      spatial coordinates */
  DisplayTupleType DisplaySpatialSphericalTuple =
    new DisplayTupleType(components3s, DisplaySphericalCoordSys, true);


  /** CoordinateSystem for DisplaySpatialCylindricalTuple, with
      reference DisplaySpatialCartesianTuple */
  CoordinateSystem DisplayCylindricalCoordSys =
    new CylindricalCoordinateSystem(DisplaySpatialCartesianTuple, true);

  /** array of 3D cylindrical Coordinates */
  DisplayRealType[] componentscyl =
          {CylRadius, CylAzimuth, CylZAxis};

  /** system intrinsic DisplayTupleType for 3D cylindrical
      spatial coordinates */
  DisplayTupleType DisplaySpatialCylindricalTuple =
    new DisplayTupleType(componentscyl, DisplayCylindricalCoordSys, true);


  /** array of 3D RGB coordinates */
  DisplayRealType[] componentsrgb = {Red, Green, Blue};

  /** system intrinsic DisplayTupleType for RGB color coordinates */
  DisplayTupleType DisplayRGBTuple =
    new DisplayTupleType(componentsrgb, true);


  /** CoordinateSystem for DisplayHSVTuple, with reference
      DisplayRGBTuple */
  CoordinateSystem DisplayHSVCoordSys =
    new HSVCoordinateSystem(DisplayRGBTuple, true);

  /** array of 3D HSV coordinates */
  DisplayRealType[] componentshsv = {Hue, Saturation, Value};

  /** system intrinsic DisplayTupleType for HSV color coordinates */
  DisplayTupleType DisplayHSVTuple =
    new DisplayTupleType(componentshsv, DisplayHSVCoordSys, true);


  /** CoordinateSystem for DisplayCMYTuple, with reference
      DisplayRGBTuple */
  CoordinateSystem DisplayCMYCoordSys =
    new CMYCoordinateSystem(DisplayRGBTuple, true);

  /** array of 3D CMY coordinates */
  DisplayRealType[] componentscmy =
          {Cyan, Magenta, Yellow};

  /** system intrinsic DisplayTupleType for CMY color coordinates */
  DisplayTupleType DisplayCMYTuple =
    new DisplayTupleType(componentscmy, DisplayCMYCoordSys, true);

  /** array of 3D Cartesian flow set 1 coordinates */
  DisplayRealType[] componentsflow1 = {Flow1X, Flow1Y, Flow1Z};

  /** system intrinsic DisplayTupleType for 3D Cartesian
      flow set 1 coordinates */
  DisplayTupleType DisplayFlow1Tuple =
    new DisplayTupleType(componentsflow1, true);

  /** array of 3D Cartesian flow set 2 coordinates */
  DisplayRealType[] componentsflow2 = {Flow2X, Flow2Y, Flow2Z};

  /** system intrinsic DisplayTupleType for 3D Cartesian
      flow set 2 coordinates */
  DisplayTupleType DisplayFlow2Tuple =
    new DisplayTupleType(componentsflow2, true);


  /** CoordinateSystem for DisplayFlow1SphericalTuple, with reference
      DisplayFlow1Tuple */
  CoordinateSystem DisplayFlow1SphericalCoordSys =
    new FlowSphericalCoordinateSystem(DisplayFlow1Tuple, true);

  /** array of 3D spherical flow set 1 coordinates */
  DisplayRealType[] componentsflow1s =
          {Flow1Elevation, Flow1Azimuth, Flow1Radial};

  /** system intrinsic DisplayTupleType for 3D spherical
      flow set 1 coordinates */
  DisplayTupleType DisplayFlow1SphericalTuple =
    new DisplayTupleType(componentsflow1s, DisplayFlow1SphericalCoordSys,
                         true);


  /** CoordinateSystem for DisplayFlow2SphericalTuple, with reference
      DisplayFlow2Tuple */
  CoordinateSystem DisplayFlow2SphericalCoordSys =
    new FlowSphericalCoordinateSystem(DisplayFlow2Tuple, true);

  /** array of 3D spherical flow set 2 coordinates */
  DisplayRealType[] componentsflow2s =
          {Flow2Elevation, Flow2Azimuth, Flow2Radial};

  /** system intrinsic DisplayTupleType for 3D spherical
      flow set 2 coordinates */
  DisplayTupleType DisplayFlow2SphericalTuple =
    new DisplayTupleType(componentsflow2s, DisplayFlow2SphericalCoordSys,
                         true);


  /** array of 3D Cartesian spatial offset coordinates */
  DisplayRealType[] componentsso =
          {XAxisOffset, YAxisOffset, ZAxisOffset};

  /** system intrinsic DisplayTupleType for 3D Cartesian
      spatial offset coordinates */
  DisplayTupleType DisplaySpatialOffsetTuple =
    new DisplayTupleType(componentsso, true);


  /**
   * create link to DataReference, with ConstantMaps;
   * invokes ref.addThingChangedListener(ThingChangedListener l, long id)
   * @param ref DataReference to link to
   * @param constant_maps array of ConstantMaps applied to linked Data
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void addReference(DataReference ref,
         ConstantMap[] constant_maps) throws VisADException, RemoteException;

  /** 
   * create link to DataReference, with ConstantMaps and DataRenderer;
   * invokes ref.addThingChangedListener(ThingChangedListener l, long id) 
   * @param renderer DataRenderer used to depict linked Data
   * @param ref DataReference to link to
   * @param constant_maps array of ConstantMaps applied to this Data
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void addReferences(DataRenderer renderer, DataReference ref,
                            ConstantMap[] constant_maps)
         throws VisADException, RemoteException;

  /**
   * link a ScalarMap (may be a ConstantMap) to this Display
   * @param map ScalarMap to link
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void addMap(ScalarMap map)
         throws VisADException, RemoteException;

  /** 
   * remove a ScalarMap (may be a ConstantMap) from this Display 
   * @param map ScalarMap to remove
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void removeMap(ScalarMap map)
         throws VisADException, RemoteException;

  /** 
   * remove all ScalarMaps (and ConstantMaps) from this Display
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void clearMaps() throws VisADException, RemoteException;

  /**
   * destroy this display: break all links, stop Threads and
   * clear references for garbage collection
   */
  void destroy() throws VisADException, RemoteException;

  /**
   * @return Vector of linked ConstantMaps
   */
  Vector getConstantMapVector()
         throws VisADException, RemoteException;

  /**
   * Send a message to all </tt>MessageListener</tt>s.
   *
   * @param msg Message being sent.
   * @throws RemoteException an RMI error occurred
   */
  void sendMessage(MessageEvent msg)
    throws RemoteException;

  /**
   * link a slave display to this display
   * @param display RemoteSlaveDisplay to link
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void addSlave(RemoteSlaveDisplay display)
        throws VisADException, RemoteException;

  /** 
   * remove a slave display from this display
   * @param display RemoteSlaveDisplay to remove
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void removeSlave(RemoteSlaveDisplay display)
        throws VisADException, RemoteException;

  /** 
   * remove all slave displays from this display
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void removeAllSlaves() throws VisADException, RemoteException;

  /**
   * @return true if this display has any slave displays
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  boolean hasSlaves() throws VisADException, RemoteException;

}

