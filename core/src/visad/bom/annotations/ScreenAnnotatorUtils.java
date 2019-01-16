//
// ScreenAnnotatorUtils.java
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

package visad.bom.annotations;

import visad.PlotText;
import visad.TextControl;
import visad.VisADException;
import visad.VisADGeometryArray;
import visad.java3d.VisADCanvasJ3D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DisplayRendererJ3D;

import visad.util.HersheyFont;

import java.awt.Font;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Font3D;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.View;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

/**
 *  This is a collection of static methods to help with the construction
 *  of java3D objects used for annotating a VisAD display. The objects are
 *  intended to be drawn directly to the screen using screen pixel
 *  locations. See {@link ScreenAnnotator}.
 *
 *  This is meant to contain as much of the Java3D dependencies as posible.
 */
public class ScreenAnnotatorUtils
{

  /**
   *  Construct a {@link BranchGroup} object from a routine
   *  description of a Label using {@link Text3D}. A label is a Text
   *  string with attributes used for drawing it.
   *
   *  @param display  the VisAD display for this Label.
   *  @param text  the string.
   *  @param x  x screen coordinate of the text.
   *  @param y  y screen coordinate of the text.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param font  {@link java.awt.Font} to use.
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param fontSizeInPixels  height of text.
   *  @param align  one of: <ul>
   *         <li>Text3D.ALIGN_FIRST
   *         <li>Text3D.ALIGN_CENTER
   *         <li>Text3D.ALIGN_LAST  </ul>
   *  @param path  one of: <ul>
   *         <li>Text3D.PATH_RIGHT
   *         <li>Text3D.PATH_LEFT
   *         <li>Text3D.PATH_DOWN
   *         <li>Text3D.PATH_UP  </ul>
   *  @return the {@link BranchGroup}, suitably scaled, representing the 
   *          label.
   */
  public static BranchGroup makeJLabelShape3D(DisplayImplJ3D display,
    String text, int x, int y, float[] colour, Font font,
    double zValue, double fontSizeInPixels, int align, int path)
  {
    DisplayRendererJ3D renderer =
      (DisplayRendererJ3D)display.getDisplayRenderer();
    VisADCanvasJ3D canvas = renderer.getCanvas();

    // get the start point  and one pixel up in vworld coordinates
    int[][] screenXY = { {x, x},
                         {y, y-1}  // just one pixel up
    };
    Point3d[] points = screenToVworld(screenXY, canvas);

    //adjust z
    ditherPoints(canvas, 0.4, points);
    adjustZ(display, canvas, zValue, points);

    // scale by distance in vworld representing one pixel
    double scale = fontSizeInPixels*
      (points[1].y - points[0].y)/font.getSize2D();
 
    // make the Text3D geometry object
    Font3D font3D = new Font3D(font, null); // no extrusion
    // create at Origin - scale and move later
    Text3D text3D = new Text3D(font3D, text,
      new Point3f((float)0, (float)0, (float)0), align, path);

    // make some colours
    ColoringAttributes textColor = new ColoringAttributes();
    textColor.setColor(colour[0], colour[1], colour[2]);

    Appearance textAppearance = new Appearance();
    textAppearance.setColoringAttributes(textColor);

    Shape3D textShape = new Shape3D(text3D, textAppearance);

    // Build a Transformation 
    // create at Origin, Scale and move to point
    Transform3D tMove = new Transform3D();
    tMove.set(new Vector3d(points[0].x, points[0].y, points[0].z));
    Transform3D tScale = new Transform3D();
    tScale.set(scale);
    tMove.mul(tScale);

    TransformGroup tg = new TransformGroup();
    Transform3D t3D = new Transform3D();
    tg.setTransform(tMove);

    tg.addChild(textShape);

    BranchGroup bg = new BranchGroup();
    bg.addChild(tg);

    return bg;
  } // makeJLabelShape3D

  /**
   *  Construct a {@link Shape3D} object from a routine description
   *  of a Label using 2D fonts. A label is a Text string with
   *  attributes used drawing it.
   *
   *  @param display  the VisAD display for this Label.
   *  @param text  the string.
   *  @param x  x screen coordinate of the text.
   *  @param y  y screen coordinate of the text.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param font  {@link java.awt.Font} to use.
   *  @param hfont  Hershey font to use; if both fonts are null
   *         then use the default VisAD line font.
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param scaleFactor  scale the font by this factor; by default
   *                       characters are 1 pixel in size.
   *  @param filled  if <code>true</code> the font is rendered as filled,
   *         if <code>false</code> just the triangles are drawn.
   *  @param thickness  line width to use if just drawing triangles;
   *          usually 1.0 is the most useful.
   *  @param orientation  angle of rotation of the text anticlockwise
   *         from the horizontal.
   *  @param horizontal  one of:<ul>
   *         <li> TextControl.Justification.LEFT - Left justified
   *              text (ie: normal text)
   *         <li> TextControl.Justification.CENTER - Centered text
   *         <li> TextControl.Justification.RIGHT - Right justified text
   *         </ul>
   *  @param vertical  one of:<ul>
   *         <li> TextControl.Justification.BOTTOM - Bottom justified
   *              text (normal).
   *         <li> TextControl.Justification.TOP - Top justified text
   *         <li> TextControl.Justification.CENTER - Centered text
   *         </ul>
   *  @param charRotation  rotate each character
   *         <code>charRotation</code> degrees clockwise from base line.
   *
   *  @return the Label description as a {@link Shape3D}.
   *
   *  @throws VisADException  VisAD couldn't make the geometry array.
   */
  public static Shape3D makeLabelShape3D(DisplayImplJ3D display,
    String text, int x, int y, float[] colour,
    Font font, HersheyFont hfont, double zValue,
    double scaleFactor, boolean filled, double thickness,
    double orientation,
    TextControl.Justification horizontal,
    TextControl.Justification vertical,
    double charRotation)
    throws VisADException
  {
    Shape3D shape = null;
    DisplayRendererJ3D renderer =
      (DisplayRendererJ3D)display.getDisplayRenderer();
    VisADCanvasJ3D canvas = renderer.getCanvas();

    // set up some stuff in the Virtual World
    Point3d position1 = new Point3d();
    Point3d position2 = new Point3d();
    Point3d position3 = new Point3d();
    canvas.getPixelLocationInImagePlate(x, y, position1);
    // possible different scale in x and y direction?
    // use default of 1 pixel for font height
    canvas.getPixelLocationInImagePlate(x+1, y, position2);
    double pixelInImagePlate = position2.x - position1.x;
    position2.x = position1.x + pixelInImagePlate*
      Math.cos(Math.toRadians(orientation));
    position2.y = position1.y + pixelInImagePlate*
      Math.sin(Math.toRadians(orientation));
    position2.z = position1.z;
    position3.x = position1.x - pixelInImagePlate*
      Math.sin(Math.toRadians(orientation));
    position3.y = position1.y + pixelInImagePlate*
      Math.cos(Math.toRadians(orientation));
    position3.z = position1.z;

    Transform3D t = new Transform3D();
    canvas.getImagePlateToVworld(t);
    t.transform(position1);
    t.transform(position2);
    t.transform(position3);

    // adjust z
    Point3d[] textStart = {position1, position2, position3};
    ditherPoints(canvas, 0.4, textStart);
    adjustZ(display, canvas, zValue, textStart);

    position1 = textStart[0];
    position2 = textStart[1];
    position3 = textStart[2];
    double[] start = {(double) position1.x,
                      (double) position1.y,
                      (double) position1.z};
    double[] base =  {(double) (position2.x - position1.x),
                      (double) (position2.y - position1.y),
                      (double) (position2.z - position1.z)};
    double[] up =    {(double) (position3.x - position1.x),
                      (double) (position3.y - position1.y),
                      (double) (position3.z - position1.z)};

    // scale is now in visad PlotText.java but do here
    // and put scale = 1 in call to PlotText
    for (int i=0; i<3; i++) {
      up[i] = scaleFactor*up[i];
      base[i] = scaleFactor*base[i];
    }

    // always use offset of zero. Just change x y if offset wanted
    double[] offset = {0.0, 0.0, 0.0};
    VisADGeometryArray array = null;
    if (font != null) {
      // to be compatable with versions of VisAD with old
      // PlotText call the old interface if charRotation
      // is zero
      if (charRotation == 0.0) {
        array = PlotText.render_font(text, font, start,
          base, up, horizontal, vertical);
      } else {
        array = PlotText.render_font(text, font, start,
          base, up, horizontal, vertical, charRotation, 1.0, offset);
      }
    } else if (hfont != null) {
      if (charRotation == 0.0) {
        array = PlotText.render_font(text, hfont, start,
          base, up, horizontal, vertical);
      } else {
        array = PlotText.render_font(text, hfont, start,
          base, up, horizontal, vertical, charRotation, 1.0, offset);
      }
    } else { // font && hfont are null
      if (charRotation == 0.0) {
        array = PlotText.render_label(text, start, base,
          up, horizontal, vertical);
      } else {
        array = PlotText.render_label(text, start, base,
          up, horizontal, vertical, charRotation, 1.0, offset);
      }
    }

    GeometryArray geom = display.makeGeometry(array);

    Appearance appearance = new Appearance();

    ColoringAttributes coloringAttributes =
      new ColoringAttributes();
    coloringAttributes.setColor(colour[0], colour[1], colour[2]);
    appearance.setColoringAttributes(coloringAttributes);

    LineAttributes lineAttributes = new LineAttributes();
    lineAttributes.setLineWidth((float)thickness);
    appearance.setLineAttributes(lineAttributes);

    PolygonAttributes polygonAttributes = new PolygonAttributes();
    polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
    // should be this by default but didn't render the font
    if (filled) {
      polygonAttributes.setPolygonMode(PolygonAttributes.POLYGON_FILL);
    } else {
      polygonAttributes.setPolygonMode(PolygonAttributes.POLYGON_LINE);
    }
    appearance.setPolygonAttributes(polygonAttributes);

    // change this to return a branch group

    return new Shape3D(geom, appearance);
  }
  
  /**
   * This adjusts a set of points so that the drawn object has the
   * same appearance even though the Z value is adjusted to a user
   * given setting. Important for Perspective view.
   *
   * @param display  the VisAD display.
   * @param canvas  Java3D canvas being drawn on
   * @param z  Virtual world value; larger z is in front.
   * @param points  array of values to have their z value adjusted.
   */
  public static void adjustZ(DisplayImplJ3D display, Canvas3D canvas,
    double z, Point3d[] points)
  {
    if (display.getGraphicsModeControl().getProjectionPolicy()
      == View.PERSPECTIVE_PROJECTION)
    {
      Point3d leftEye = new Point3d();
      Point3d rightEye = new Point3d();
      canvas.getLeftEyeInImagePlate(leftEye);
      canvas.getRightEyeInImagePlate(rightEye);
      double xLoc = (leftEye.x + rightEye.x)/2.0;
      double yLoc = (leftEye.y + rightEye.y)/2.0;
      double zLoc = (leftEye.z + rightEye.z)/2.0;
      Point3d eye = new Point3d(xLoc, yLoc, zLoc);
      Transform3D t = new Transform3D();
      canvas.getImagePlateToVworld(t);
      t.transform(eye);

      double a;
      for (int i=0; i<points.length; i++) {
        a = (z - points[i].z)/(eye.z - points[i].z);
        points[i].x = a*(eye.x - points[i].x) + points[i].x;
        points[i].y = a*(eye.y - points[i].y) + points[i].y;
        points[i].z = z;
      }
    } else { // PARALLEL_PROJECTION
      for (int i=0; i<points.length; i++) {
        points[i].z = z;
      }
    }
  } // adjustZ

  /**
   *  Construct a {@link Shape3D} object from a routine description
   *  of a Quadrilateral.
   *
   *  @param display  the VisAD display for this Quadrilateral.
   *  @param style  one of: <ul>
   *                 <li> QuadrilateralJ3D.FILL
   *                 <li> QuadrilateralJ3D.LINE
   *                 <li> QuadrilateralJ3D.POINT </ul>
   *  @param x1  x screen coordinate of the first point.
   *  @param y1  y screen coordinate of the first point.
   *  @param x2  x screen coordinate of the second point.
   *  @param y2  y screen coordinate of the second point.
   *  @param x3  x screen coordinate of the third point.
   *  @param y3  y screen coordinate of the third point.
   *  @param x4  x screen coordinate of the fourth point.
   *  @param y4  y screen coordinate of the fourth point.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param z  Virtual world value; larger z is in front.
   *  @param thickness  used for LINE and POINT node.
   *
   *  @return the QuadrilateralJ3D description as a {@link Shape3D}.
   */
  public static Shape3D makeQuadrilateralShape3D(DisplayImplJ3D display,
    int style, int x1, int y1, int x2, int y2, int x3, int y3,
    int x4, int y4, float[] colour, double z, double thickness)
  {
    DisplayRendererJ3D renderer =
      (DisplayRendererJ3D)display.getDisplayRenderer();
    VisADCanvasJ3D canvas = renderer.getCanvas();

    int[][] screenXY = { {x1, x2, x3, x4},
                         {y1, y2, y3, y4}
    };
    Point3d[] points = screenToVworld(screenXY, canvas);

    //adjust z
    ditherPoints(canvas, 0.4, points);
    adjustZ(display, canvas, z, points);

    QuadArray quadArray = new QuadArray(points.length,
      GeometryArray.COORDINATES);
    quadArray.setCoordinates(0, points);

    // Make an appearance for outline
    PointAttributes pointAttributes = new PointAttributes();
    pointAttributes.setPointSize((float)thickness);

    LineAttributes lineAttributes = new LineAttributes();
    lineAttributes.setLineWidth((float)thickness);

    PolygonAttributes pointPolygon = new PolygonAttributes();
    pointPolygon.setCullFace(PolygonAttributes.CULL_NONE);
    if (style == QuadrilateralJ3D.POINT) {
      pointPolygon.setPolygonMode(PolygonAttributes.POLYGON_POINT);
    } else if (style == QuadrilateralJ3D.LINE) {
      pointPolygon.setPolygonMode(PolygonAttributes.POLYGON_LINE);
    } else {
      pointPolygon.setPolygonMode(PolygonAttributes.POLYGON_FILL);
    }

    // make some colours
    ColoringAttributes pointColor = new ColoringAttributes();
    pointColor.setColor(colour[0], colour[1], colour[2]);

    Appearance pointAppearance = new Appearance();
    pointAppearance.setPointAttributes(pointAttributes);
    pointAppearance.setLineAttributes(lineAttributes);
    pointAppearance.setPolygonAttributes(pointPolygon);
    pointAppearance.setColoringAttributes(pointColor);

    return new Shape3D(quadArray, pointAppearance);
  } // makeQuadrilateralShape3D

  /**
   *  Construct a {@link Shape3D} object from a routine description
   *  of a Triangle.
   *
   *  @param display  the VisAD display for this Point.
   *  @param style  one of <ul>
   *         <li> TriangleJ3D.FILL, </li>
   *         <li> TriangleJ3D.LINE, </li>
   *         <li> TriangleJ3D.POINT </li> </ul>
   *  @param x1  x screen coordinate of the first point.
   *  @param y1  y screen coordinate of the first point.
   *  @param x2  x screen coordinate of the second point.
   *  @param y2  y screen coordinate of the second point.
   *  @param x3  x screen coordinate of the third point.
   *  @param y3  y screen coordinate of the third point.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param z  Virtual world value; larger z is in front.
   *  @param thickness  used for LINE and POINT node.
   *
   *  @return the Triangle description as a {@link Shape3D}.
   */
  public static Shape3D makeTriangleShape3D(DisplayImplJ3D display,
    int style, int x1, int y1, int x2, int y2, int x3, int y3,
    float[] colour, double z, double thickness)
  {
    DisplayRendererJ3D renderer =
      (DisplayRendererJ3D)display.getDisplayRenderer();
    VisADCanvasJ3D canvas = renderer.getCanvas();

    // this becomes a call to screenToVworld()
    
    int[][] screenXY = { {x1, x2, x3},
                         {y1, y2, y3}
    };
    Point3d[] points = screenToVworld(screenXY, canvas);

    //adjust z
    ditherPoints(canvas, 0.4, points);
    adjustZ(display, canvas, z, points);

    TriangleArray triangleArray = new TriangleArray(
      points.length, GeometryArray.COORDINATES);
    triangleArray.setCoordinates(0, points);

    // Make an appearance for outline
    PointAttributes pointAttributes = new PointAttributes();
    pointAttributes.setPointSize((float)thickness);

    LineAttributes lineAttributes = new LineAttributes();
    lineAttributes.setLineWidth((float)thickness);

    PolygonAttributes pointPolygon = new PolygonAttributes();
    pointPolygon.setCullFace(PolygonAttributes.CULL_NONE);
    if (style == TriangleJ3D.POINT) {
      pointPolygon.setPolygonMode(PolygonAttributes.POLYGON_POINT);
    } else if (style == TriangleJ3D.LINE) {
      pointPolygon.setPolygonMode(PolygonAttributes.POLYGON_LINE);
    } else {
      pointPolygon.setPolygonMode(PolygonAttributes.POLYGON_FILL);
    }

    // make some colours
    ColoringAttributes pointColor = new ColoringAttributes();
    pointColor.setColor(colour[0], colour[1], colour[2]);

    Appearance pointAppearance = new Appearance();
    pointAppearance.setPointAttributes(pointAttributes);
    pointAppearance.setLineAttributes(lineAttributes);
    pointAppearance.setPolygonAttributes(pointPolygon);
    pointAppearance.setColoringAttributes(pointColor);

    return  new Shape3D(triangleArray, pointAppearance);
  } // makeTriangleShape3D

  /**
   *  Construct a {@link Shape3D} object from a routine description
   *  of a Line.
   *
   *  @param display  the VisAD display for this Point.
   *  @param style  one of: <ul>
   *         <li> LineJ3D.SOLID
   *         <li> LineJ3D.DASH
   *         <li> LineJ3D.DOT
   *         <li> LineJ3D.DASH_DOT </ul>
   *  @param x1  x screen coordinate of the first point.
   *  @param y1  y screen coordinate of the first point.
   *  @param x2  x screen coordinate of the second point.
   *  @param y2  y screen coordinate of the second point.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param z  Virtual world value; larger z is in front.
   *  @param thickness  for the line.
   *
   *  @return the LineJ3D description as a {@link Shape3D}.
   */
  public static Shape3D makeLineShape3D(DisplayImplJ3D display, int style,
    int x1, int y1, int x2, int y2,
    float[] colour, double z, double thickness)
  {
    DisplayRendererJ3D renderer =
      (DisplayRendererJ3D)display.getDisplayRenderer();
    VisADCanvasJ3D canvas = renderer.getCanvas();

    int[][] screenXY = { {x1, x2},
                         {y1, y2}
    };
    Point3d[] points = screenToVworld(screenXY, canvas);

    //adjust z
    ditherPoints(canvas, 0.4, points);
    adjustZ(display, canvas, z, points);

    LineArray lineArray = new LineArray(
      points.length, GeometryArray.COORDINATES);
    lineArray.setCoordinates(0, points);

    // Make an appearance for outline
    PointAttributes pointAttributes = new PointAttributes();
    pointAttributes.setPointSize((float)thickness);

    LineAttributes lineAttributes = new LineAttributes();
    lineAttributes.setLineWidth((float)thickness);
    if (style == LineJ3D.SOLID) {
      lineAttributes.setLinePattern(LineAttributes.PATTERN_SOLID);
    } else if (style == LineJ3D.DASH) {
      lineAttributes.setLinePattern(LineAttributes.PATTERN_DASH);
    } else if (style == LineJ3D.DOT) {
      lineAttributes.setLinePattern(LineAttributes.PATTERN_DOT);
    } else if (style == LineJ3D.DASH_DOT) {
      lineAttributes.setLinePattern(LineAttributes.PATTERN_DASH_DOT);
    }

    // make some colours
    ColoringAttributes pointColor = new ColoringAttributes();
    pointColor.setColor(colour[0], colour[1], colour[2]);

    Appearance pointAppearance = new Appearance();
    pointAppearance.setPointAttributes(pointAttributes);
    pointAppearance.setLineAttributes(lineAttributes);
    pointAppearance.setColoringAttributes(pointColor);

    return  new Shape3D(lineArray, pointAppearance);
  } // makeLineShape3D

  /**
   *  Construct a {@link Shape3D} object from a routine description
   *  of a Point.
   *
   *  @param display  the VisAD display for this Point.
   *  @param x1  x screen coordinate of the point.
   *  @param y1  y screen coordinate of the point.
   *  @param colour  red green blue triple; each value in [0.0, 1.0].
   *  @param z  Virtual world value; larger z is in front.
   *  @param thickness  gives the size of the Point.
   *
   *  @return the Point description as a {@link Shape3D}.
   */
  public static Shape3D makePointShape3D(DisplayImplJ3D display,
    int x1, int y1, float[] colour, double z, double thickness)
  {
    DisplayRendererJ3D renderer =
      (DisplayRendererJ3D)display.getDisplayRenderer();
    VisADCanvasJ3D canvas = renderer.getCanvas();

    int[][] screenXY = { {x1},
                         {y1}
    };
    Point3d[] points = screenToVworld(screenXY, canvas);

    //adjust z
    ditherPoints(canvas, 0.4, points);
    adjustZ(display, canvas, z, points);

    PointArray pointArray = new PointArray(
      points.length, GeometryArray.COORDINATES);
    pointArray.setCoordinates(0, points);

    // Make an appearance for outline
    PointAttributes pointAttributes = new PointAttributes();
    pointAttributes.setPointSize((float)thickness);

    // make some colours
    ColoringAttributes pointColor = new ColoringAttributes();
    pointColor.setColor(colour[0], colour[1], colour[2]);

    Appearance pointAppearance = new Appearance();
    pointAppearance.setPointAttributes(pointAttributes);
    pointAppearance.setColoringAttributes(pointColor);

    return  new Shape3D(pointArray, pointAppearance);
  } // makePointShape3D

  /**
   *  Convert an array of Pixel points to Point3d array in the
   *  Virtual World.
   *
   *  @param canvas  for the virtual world of interest.
   *  @param screenXY  the screen points in x, y pairs.
   *
   *  @return - array of {@link Point3d} values representing the 
   *          Virtual World values for the Screen points.
   */
  public static Point3d[] screenToVworld(int[][] screenXY,
    Canvas3D canvas)
  {
    int numPoints = screenXY[0].length;
    Point3d[] points = new Point3d[numPoints];
    Transform3D t = new Transform3D();
    canvas.getImagePlateToVworld(t);
    for (int i=0; i< numPoints; i++) {
      points[i] = new Point3d();
      canvas.getPixelLocationInImagePlate(screenXY[0][i],
        screenXY[1][i], points[i]);
      t.transform(points[i]);
    }

    return points;
  } // screenToVWorld

  /**
   *  Convert an array of Vworld points to int[][] array in 
   *  Screen coordinates.
   *
   *  @param canvas  for the virtual world of interest.
   *  @param points  array of {@link Point3d} of Vworld coordinates.
   *
   *  @return - int[2][] array of values representing the Screen points.
   */
  public static int[][] vworldToScreen(Point3d[] points,
    Canvas3D canvas)
  {
    int numPoints = points.length;
    Point2d point2d = new Point2d();
    Point3d point3d = new Point3d();
    int[][] screenXY = new int[2][numPoints];
    Transform3D t = new Transform3D();
    canvas.getVworldToImagePlate(t);

    for (int j=0; j<numPoints; j++) {
      // Copy so that the input is unchanged
      point3d.x = points[j].x;
      point3d.y = points[j].y;
      point3d.z = points[j].z;
      t.transform(point3d);
      canvas.getPixelLocationFromImagePlate(point3d, point2d);
      screenXY[0][j] = (int)point2d.x;
      screenXY[1][j] = (int)point2d.y;
    }

    return screenXY;
  } // vworldToScreen

  /**
   *  Java3D seems to calculate a floating point pixel value and
   *  then take the integer part for the screen coordinate.
   *
   *  This routine allows the user to move the x, y value of a
   *  Vworld set of points. If these points represent Vworld values
   *  for some pixels then it allows the coordinate to be moved to
   *  the interior of the pixel rather than the top left. Consequently
   *  converting back from Vworld values to floating point screen
   *  values should ensure the integer part is mapping back to the
   *  original pixel.
   *
   *  The intended use is in mapping screen coordinates to Vworld,
   *  allow some adjustments, and then be able to map back to the same
   *  screen coordinates. 
   *
   *  An example is adjusting the z value in Vworld so the point is
   *  closer to (or further from) the eye but have the image occupy
   *  the same pixel. Get the original Vworld coordinate, use this
   *  routine to adjust to a Vworld point interior (centre?) of the
   *  pixel, and then adjust the z value.
   *
   *  Java 3D uses positive y down the screen for pixel values, and
   *  negative y up the screen in Vworld coordinates. So this routine
   *  is written accordinagly for doing fractional adjustments.
   *
   *  @param canvas  where the points are displayed.
   *  @param frac  amount of interpoint spacing to use for adjustment;
   *                it is clamped between [0.1, 0.9].
   *  @param points  array of values to have their x, y values adjusted.
   */
  public static void ditherPoints(Canvas3D canvas, double frac,
    Point3d[] points)
  {
    double deltaX;
    double deltaY;

    // clamp frac to [(0.1, 0.9]
    if (frac < 0.1) { frac = 0.1; }
    if (frac > 0.9) { frac = 0.9; }

    // get interpixel spacing in Vworld corrdinates
    Point3d p1 = new Point3d();
    Point3d p2 = new Point3d();
    Point3d p3 = new Point3d();
    canvas.getPixelLocationInImagePlate(1, 1, p1);
    canvas.getPixelLocationInImagePlate(2, 1, p2);
    canvas.getPixelLocationInImagePlate(1, 2, p3);
    Transform3D t = new Transform3D();
    canvas.getImagePlateToVworld(t);
    t.transform(p1);
    t.transform(p2);
    t.transform(p3);
    deltaX = p2.x - p1.x;
    deltaY = p1.y - p3.y;
    // use this to get the dithered points
    for (int i=0; i<points.length; i++) {
      points[i].x = points[i].x + frac*deltaX;
      points[i].y = points[i].y - frac*deltaY;
    }
  } // ditherPoints

  /**
   *  Transforms an Image object into a {@link Shape3D}. The image
   *  object may be scaled.
   *
   *  @param display  the VisAD display for this Point.
   *  @param image  to be converted to {@link Shape3D}.
   *  @param position  how to place the image relative to (x, y);
   *         one of: <ul>
   *                <li> Image.TOP_LEFT (default)
   *                <li> Image.TOP_RIGHT
   *                <li> Image.BOTTOM_RIGHT
   *                <li> Image.BOTTOM_LEFT
   *                <li> Image.CENTER </ul>
   *  @param x  x screen coordinate to place the image.
   *  @param y  y screen coordinate to place the image.
   *  @param width  of image in pixels.
   *  @param height  of image in pixels.
   *  @param zValue  Virtual world value; larger z is in front.
   *  @param scale  scale factor for image magnification; greater
   *         than 0.0.
   *
   *  @return the Image description as a {@link Shape3D}.
   *
   *  @throws VisADException  if the image can't be accessed
   *          for some reason.
   */
  public static Shape3D makeImageShape3D(DisplayImplJ3D display,
    Image image, int position, int x, int y, int width, int height,
    double zValue, double scale)
    throws VisADException
  {
    int index = 0;
    int ind = 0;
    int scaledWidth;
    int scaledHeight;

    DisplayRendererJ3D renderer =
      (DisplayRendererJ3D)display.getDisplayRenderer();
    VisADCanvasJ3D canvas = renderer.getCanvas();

    // Adjust for scaling factor
    scaledWidth = Math.round((float)(width*scale));
    scaledHeight = Math.round((float)(height*scale));
    double scaleW = (double)scaledWidth/(double)width;
    double scaleH = (double)scaledHeight/(double)height;

    // Adjust x, y according to  position
    // do nothing for TOP_LEFT
    if (position == ImageJ3D.TOP_RIGHT) { // adjust x
      x = x - (scaledWidth-1);
    } else if (position == ImageJ3D.BOTTOM_RIGHT) { // adjust x & y
      x = x - (scaledWidth-1);
      y = y - (scaledHeight-1);
    } else if (position == ImageJ3D.BOTTOM_LEFT) { // adjust y
      y = y - (scaledHeight-1);
    } else if (position == ImageJ3D.CENTER) { // adjust x & y
      x = x - (scaledWidth-1)/2;
      y = y - (scaledHeight-1)/2;
    }

    // now calculate the VWorld coordinates for each pixel
    Transform3D t = new Transform3D();
    canvas.getImagePlateToVworld(t);
    index = 0;
    Point3d[] points = new Point3d[scaledWidth*scaledHeight];
    for (int i=0; i<scaledHeight; i++) {
      for (int j=0; j<scaledWidth; j++) { // row first
        points[index] = new Point3d();
        canvas.getPixelLocationInImagePlate(x + j, y + i, points[index]);
        t.transform(points[index]);
        index++;
      }
    }

    ditherPoints(canvas, 0.4, points);
    adjustZ(display, canvas, zValue, points);

    // now get the colours from the image 
    int[] pixels = new int[width*height];
    PixelGrabber pixelGrabber = new PixelGrabber(image.getSource(),
      0, 0,  width, height, pixels, 0, width);

    // get the pixels
    try {
      pixelGrabber.grabPixels();
    } catch (InterruptedException ie) {
      throw new VisADException(
        "ScreenAnnotatorUtils.makeImageShape3D():" + " failed to grabPixels()");
    }

    float[] colours = new float[4*scaledWidth*scaledHeight];
    ind = 0;
    index = 0;
    ColorModel cm = pixelGrabber.getColorModel();
    for (int i=0; i<scaledHeight; i++) { // down column last
      for (int j=0; j<scaledWidth; j++) { // row first
        ind = (int)((double)j/scaleW) + width*(int)((double)i/scaleH);
        colours[4*index] = (float)cm.getRed(pixels[ind])/255;
        colours[4*index+1] = (float)cm.getGreen(pixels[ind])/255;
        colours[4*index+2] = (float)cm.getBlue(pixels[ind])/255;
        colours[4*index+3] = (float)cm.getAlpha(pixels[ind])/255;
        index++;
      }
    }

    PointArray picture = new PointArray(scaledWidth*scaledHeight,
      GeometryArray.COORDINATES | GeometryArray.COLOR_4);
    picture.setCoordinates(0, points);
    picture.setColors(0, colours);

    Appearance appearance = new Appearance();
    appearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST, 0));

    // Use default appearance

     return new Shape3D(picture, appearance);
  } // makeImageShape3D
} // class ScreenAnnotatorUtils
