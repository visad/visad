//
// AxisScale.java
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

package visad;

import visad.*;
import java.awt.Color;
import java.rmi.RemoteException;
import java.util.*;
import java.awt.Font;

/**
 * Class which defines the scales displayed along the spatial axes
 * of a display.  Each ScalarMap that has a DisplayScalar of the
 * X, Y, or Z axis will have a non-null AxisScale.
 * @see ScalarMap#getAxisScale()
 * @author Don Murray
 */
public class AxisScale implements java.io.Serializable 
{
  /** X_AXIS identifier */
  public final static int X_AXIS = 0;
  /** Y_AXIS identifier */
  public final static int Y_AXIS = 1;
  /** Z_AXIS identifier */
  public final static int Z_AXIS = 2;

  private VisADLineArray scaleArray;
  private VisADTriangleArray labelArray;
  private ScalarMap scalarMap;
  private Color myColor = Color.white;
  private double[] dataRange = new double[2];
  private int myAxis = -1;
  private int axisOrdinal = -1;
  private String myLabel;
  private Hashtable labelTable;
  protected double majorTickSpacing = 0.0;
  protected double minorTickSpacing = 0.0;
  protected boolean autoComputeTicks = true;
  protected boolean baseLineVisible = true;
  protected boolean snapToBox = false;
  private Font labelFont = null;
  private int labelSize = 12;

  /**
   * Construct a new AxisScale for the given ScalarMap
   * @param map  ScalarMap to monitor.  Must be mapped to one of
   *       Display.XAxis, Display.YAxis, Display.ZAxis
   * @throws  VisADException  bad ScalarMap or other VisAD problem
   */
  public AxisScale(ScalarMap map)
    throws VisADException
  {
    scalarMap = map;
    DisplayRealType displayScalar = scalarMap.getDisplayScalar();
    if (!displayScalar.equals(Display.XAxis) &&
      !displayScalar.equals(Display.YAxis) &&
      !displayScalar.equals(Display.ZAxis)) 
    throw new DisplayException("AxisSale: DisplayScalar " +
                   "must be XAxis, YAxis or ZAxis");
    myAxis = (displayScalar.equals(Display.XAxis)) ? X_AXIS :
       (displayScalar.equals(Display.YAxis)) ? Y_AXIS : Z_AXIS;
    myLabel = scalarMap.getScalarName();
    labelTable = new Hashtable();
    boolean ok = makeScale();
  }

  /**
   * Get the position of this AxisScale on the Axis (first, second, third).
   *
   * @return  position from the axis (first = 0, second = 1, etc)
   */
  public int getAxisOrdinal()
  {
    return axisOrdinal;
  }

  /**
   * Set the position of this AxisScale on the axis.  Should only
   * be called by ScalarMap
   * @param  ordinalValue  axis position (0 = first, 1 = second, etc)
   */
  void setAxisOrdinal(int ordinalValue)
  {
    axisOrdinal = ordinalValue;
  }

  /** 
   * Set the label to be used for this axis.  The default is the
   * ScalarName of the ScalarMap.
   * @param  label  label to be used
   */
  public void setLabel(String label)
  {
    String oldLabel = myLabel;
    myLabel = label;
    if (!myLabel.equals(oldLabel) ) {
      try {
        scalarMap.setScalarName(myLabel);
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * Get the label of the AxisScale.
   * @return label
   */
  public String getLabel()
  {
    return myLabel;
  }

  /**
   * Get axis that the scale will be displayed on.
   * @return  axis  (X_AXIS, Y_AXIS or Z_AXIS)
   */
  public int getAxis()
  {
    return myAxis;
  }

  /**
   * Get the Scale to pass to the renderer.
   * @return  VisADLineArray representing the scale
   */
  public VisADLineArray getScaleArray()
  {
    return scaleArray;
  }

  /**
   * Get the labels rendered with a font to pass to the renderer.
   * @return  VisADTriangleArray representing the labels
   */
  public VisADTriangleArray getLabelArray()
  {
    return labelArray;
  }

  /**
   * Create the scale.
   * @return  true if scale was successfully created, otherwise false
   */
  public boolean makeScale()
      throws VisADException {
    DisplayImpl display = scalarMap.getDisplay();
    if (display == null) return false;
    DisplayRenderer displayRenderer = display.getDisplayRenderer();
    if (displayRenderer == null) return false;
    if (axisOrdinal < 0) {
      axisOrdinal = displayRenderer.getAxisOrdinal(myAxis);
    }
    dataRange = scalarMap.getRange();
    Vector lineArrayVector = new Vector(4);
    Vector labelArrayVector = new Vector();
    boolean twoD = displayRenderer.getMode2D();
  
  // now create scale along axis at axisOrdinal position in array
  // twoD may help define orientation
  
    // compute graphics positions
    // these are {x, y, z} vectors
    double[] base = null; // vector from one character to another
    double[] up = null; // vector from bottom of character to top
    double[] startn = null; // -1.0 position along axis
    double[] startp = null; // +1.0 position along axis
  
// WLH 24 Nov 2000
    ProjectionControl pcontrol = display.getProjectionControl();
    double[] aspect = pcontrol.getAspectCartesian();

/* WLH 24 Nov 2000
    double XMIN = -1.0;
    double YMIN = -1.0;
    double ZMIN = -1.0;
*/
    double XMIN = -aspect[0];
    double YMIN = -aspect[1];
    double ZMIN = -aspect[2];

    // set scale according to labelSize
    double SCALE =  labelSize/200.;
    double OFFSET = 1.05;

    int position = axisOrdinal;
    if (snapToBox) {
      OFFSET = 1.0;
      position = 0;
    }
    double line = 2.0 * position * SCALE;
  
    double ONE = 1.0;
    if (dataRange[0] > dataRange[1]) ONE = -1.0; // inverted range
    if (myAxis == X_AXIS) {
      base = new double[] {SCALE, 0.0, 0.0};
      up = new double[] {0.0, SCALE, SCALE};
/* WLH 24 Nov 2000
      startp = new double[] {ONE, YMIN * (OFFSET + line), ZMIN * (OFFSET + line)};
      startn = new double[] {-ONE, YMIN * (OFFSET + line), ZMIN * (OFFSET + line)};
*/
      startp = new double[] {-ONE * XMIN,
                             YMIN - ((OFFSET - 1.0) + line),
                             ZMIN - ((OFFSET - 1.0) + line)};
      startn = new double[] {ONE * XMIN,
                             YMIN - ((OFFSET - 1.0) + line),
                             ZMIN - ((OFFSET - 1.0) + line)};
    }
    else if (myAxis == Y_AXIS) {
      base = new double[] {0.0, -SCALE, 0.0};
      up = new double[] {SCALE, 0.0, SCALE};
/* WLH 24 Nov 2000
      startp = new double[] {XMIN * (OFFSET + line), ONE, ZMIN * (OFFSET + line)};
      startn = new double[] {XMIN * (OFFSET + line), -ONE, ZMIN * (OFFSET + line)};
*/
      startp = new double[] {XMIN - ((OFFSET - 1.0) + line),
                             -ONE * YMIN,
                             ZMIN - ((OFFSET - 1.0) + line)};
      startn = new double[] {XMIN - ((OFFSET - 1.0) + line),
                             ONE * YMIN,
                             ZMIN - ((OFFSET - 1.0) + line)};
    }
    else if (myAxis == Z_AXIS) {
      base = new double[] {0.0, 0.0, -SCALE};
      up = new double[] {SCALE, SCALE, 0.0};
/* WLH 24 Nov 2000
      startp = new double[] {XMIN * (OFFSET + line), YMIN * (OFFSET + line), ONE};
      startn = new double[] {XMIN * (OFFSET + line), YMIN * (OFFSET + line), -ONE};
*/
      startp = new double[] {XMIN - ((OFFSET - 1.0) + line),
                             YMIN - ((OFFSET - 1.0) + line),
                             -ONE * ZMIN};
      startn = new double[] {XMIN - ((OFFSET - 1.0) + line),
                             YMIN - ((OFFSET - 1.0) + line),
                             ONE * ZMIN};
    }
    if (twoD) {
      // zero out z coordinates
      base[2] = 0.0;
      up[2] = 0.0;
      startn[2] = 0.0;
      startp[2] = 0.0;
      if (myAxis == 2) return false;
    }
  
    // compute tick mark values
    double range = Math.abs(dataRange[1] - dataRange[0]);
    double min = Math.min(dataRange[0], dataRange[1]);
    double max = Math.max(dataRange[0], dataRange[1]);
    /*  Change DRM 24-Jan-2001 */
    if (autoComputeTicks || majorTickSpacing <= 0)
    {
      double tens = 1.0;
      if (range < tens) {
        tens /= 10.0;
        while (range < tens) tens /= 10.0;
      }
      else {
        while (10.0 * tens <= range) tens *= 10.0;
      }
      // now tens <= range < 10.0 * tens;
      double ratio = range / tens;
      if (ratio < 2.0) {
        tens /= 5.0;
      }
      else if (ratio < 4.0) {
        tens /= 2.0;
      }
      majorTickSpacing = tens;
    }

    // now tens = interval between major tick marks (majorTickSpacing)
  
    /* Change DRM 24-Jan-2001
    long bot = (int) Math.ceil(min / tens);
    long top = (int) Math.floor(max / tens);
    */
    long bot = (int) Math.ceil(min / majorTickSpacing);
    long top = (int) Math.floor(max / majorTickSpacing);

    if (bot == top) {
      if (bot < 0) top++;
      else bot--;
    }
    // now bot * majorTickSpacing = value of lowest tick mark, and
    // top * majorTickSpacing = values of highest tick mark

    // base line for axis
    // coordinates has three entries for (x, y, z) of each point
    // two points determine a line segment,
    // hence 6 coordinates entries per segment
    if (baseLineVisible) // draw base line
    {
      VisADLineArray baseLineArray = new VisADLineArray();
      float[] lineCoordinates = new float[6];
      for (int i=0; i<3; i++) { // loop over x, y & z coordinates
        lineCoordinates[i] = (float) startn[i];
        lineCoordinates[3 + i] = (float) startp[i];
      }
      baseLineArray.vertexCount = 2;
      baseLineArray.coordinates = lineCoordinates;
      lineArrayVector.add(baseLineArray);
    }
  
    // draw major tick marks
    VisADLineArray majorTickArray = new VisADLineArray();
    int nticks = (int) (top - bot) + 1;
    float[] majorCoordinates = new float[6 * nticks];
    int k = 0;
    for (long j=bot; j<=top; j++) { // loop over x, y & z coordinates
      double val = j * majorTickSpacing;  // DRM 24-Jan-2001
      double a = (val - min) / (max - min);
      for (int i=0; i<3; i++) {
        if ((k + 3 + i) < majorCoordinates.length) {
          // guard against error that cannot happen, but was seen?
          majorCoordinates[k + i] = 
            (float) ((1.0 - a) * startn[i] + a * startp[i]);
          majorCoordinates[k + 3 + i] = 
            (float) (majorCoordinates[k + i] - 0.5 * up[i]);
        }
      }
      k += 6;
    }
    /* Change DRM 24-Jan-2001
    arrays[0].vertexCount = 2 * (nticks + 1);
    arrays[0].coordinates = coordinates;
    */
    majorTickArray.vertexCount = 2 * (nticks);
    majorTickArray.coordinates = majorCoordinates;
    lineArrayVector.add(majorTickArray);
  
    if (getMinorTickSpacing() > 0)  // create an array for the minor ticks
    {
      long lower = (int) Math.ceil(min / minorTickSpacing);
      long upper = (int) Math.floor(max / minorTickSpacing);

      if (lower == upper) {
        if (lower < 0) upper++;
        else lower--;
      }
      // now lower * minorTickSpacing = value of lowest tick mark, and
      // upper * minorTickSpacing = values of highest tick mark
  
      VisADLineArray minorTickArray = new VisADLineArray();
      nticks = (int) (upper - lower) + 1;
      // coordinates has three entries for (x, y, z) of each point
      // two points determine a line segment,
      // hence 6 coordinates entries per segment
      float[] minorCoordinates = new float[6 * (nticks + 1)];
      /*
      // draw base line
      for (int i=0; i<3; i++) { // loop over x, y & z coordinates
        minorCoordinates[i] = (float) startn[i];
        minorCoordinates[3 + i] = (float) startp[i];
      }
      */
      // now minorCoordinates[0], [1] and [2]
    
      // draw tick marks
      k = 0;
      for (long j=lower; j<=upper; j++) {
        double val = j * minorTickSpacing;  // DRM 24-Jan-2001
        double a = (val - min) / (max - min);
        for (int i=0; i<3; i++) {
          if ((k + 3 + i) < minorCoordinates.length) {
            // guard against error that cannot happen, but was seen?
            minorCoordinates[k + i] = 
              (float) ((1.0 - a) * startn[i] + a * startp[i]);
            minorCoordinates[k + 3 + i] = 
              (float) (minorCoordinates[k + i] - 0.25 * up[i]);
          }
        }
        k += 6;
      }
      minorTickArray.vertexCount = 2 * (nticks + 1);
      minorTickArray.coordinates = minorCoordinates;
      lineArrayVector.add(minorTickArray);
    }
  
    // labels
    double[] startbot = new double[3];
    double[] starttop = new double[3];
    double[] startlabel = new double[3];
    // compute positions along axis of low and high tick marks
    double botval = bot * majorTickSpacing;  // DRM 24-Jan-2001
    double topval = top * majorTickSpacing;  // DRM 24-Jan-2001
    double abot = (botval - min) / (max - min);
    double atop = (topval - min) / (max - min);
    for (int i=0; i<3; i++) {
      startbot[i] = (1.0 - abot) * startn[i] + abot * startp[i] - 1.5 * up[i];
      starttop[i] = (1.0 - atop) * startn[i] + atop * startp[i] - 1.5 * up[i];
      startlabel[i] = 0.5 * (startn[i] + startp[i]) - 1.5 * up[i];
    }
    // all labels rendered with 'true' for centered
  
    // draw RealType name
    /* Change DRM 24-Jan-2001
    arrays[1] = PlotText.render_label(myLabel, startlabel,
                    base, up, true);
    */
    if (labelFont == null)
    {
      VisADLineArray plotArray = 
        PlotText.render_label(myLabel, startlabel, base, up, true);
      lineArrayVector.add(plotArray);
    }
    else
    {
      VisADTriangleArray nameArray = 
        PlotText.render_font(myLabel, labelFont, startlabel, base, up, true);
      labelArrayVector.add(nameArray);
    }
  
    labelTable.clear();
    String botstr = PlotText.shortString(botval);
    String topstr = PlotText.shortString(topval);
    if (RealType.Time.equals(scalarMap.getScalar())) {
      RealType rtype = (RealType) scalarMap.getScalar();
      botstr = new Real(rtype, botval).toValueString();
      topstr = new Real(rtype, topval).toValueString();
    }
    /* change DRM 24-Jan-2001
    // draw number at bottom tick mark
    arrays[2] = PlotText.render_label(botstr, startbot, base, up, true);
    // draw number at top tick mark
    arrays[3] = PlotText.render_label(topstr, starttop, base, up, true);
    */
    labelTable.put(startbot, botstr);
    labelTable.put(starttop, topstr);
    for (Enumeration e = labelTable.keys(); e.hasMoreElements();)
    {
      double[] val = (double[]) e.nextElement();
      if (labelFont == null)
      {
        VisADLineArray label = 
            PlotText.render_label(
              (String) labelTable.get(val), val, base, up, true);
        lineArrayVector.add(label);
      }
      else
      {
        VisADTriangleArray label = 
            PlotText.render_font(
              (String) labelTable.get(val), labelFont, val, base, up, true);
        labelArrayVector.add(label);
      }
    }
  
    // merge the line arrays
    VisADLineArray[] arrays = 
        (VisADLineArray[]) lineArrayVector.toArray(
          new VisADLineArray[lineArrayVector.size()]);
    scaleArray = VisADLineArray.merge(arrays);

    // merge the label arrays
    if ( !(labelArrayVector.isEmpty()) )
    {
      VisADTriangleArray[] labelArrays = 
          (VisADTriangleArray[]) labelArrayVector.toArray(
            new VisADTriangleArray[labelArrayVector.size()]);
      labelArray = VisADTriangleArray.merge(labelArrays);
      // set the color for the label arrays
      float[] rgb = myColor.getColorComponents(null);
      byte red = ShadowType.floatToByte(rgb[0]);
      byte green = ShadowType.floatToByte(rgb[1]);
      byte blue = ShadowType.floatToByte(rgb[2]);
      int n = 3 * labelArray.vertexCount;
      byte[] colors = new byte[n];
      for (int i=0; i<n; i+=3) {
        colors[i] = red;
        colors[i+1] = green;
        colors[i+2] = blue;
      }
      labelArray.colors = colors;
    }

    return true;
  }
  
  /**
   * Get the color of this axis scale.
   *
   * @return  Color of the scale.
   */
  public Color getColor()
  {
    return myColor;
  }

  /**
   * Set the color of this axis scale.
   * @param  color  Color to use
   */
  public void setColor(Color color) 
  {
    Color oldColor = myColor;
    myColor = color;
    if (myColor != null && !myColor.equals(oldColor)) {
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }
  
  /** 
   * Set the color of this axis scale.
   * @param   color   array of red, green, and blue values in 
   *          the range (0.0 - 1.0). color must be float[3].
   */
  public void setColor(float[] color) 
  {
    setColor(new Color(color[0], color[1], color[2]));
  }

  /**
   * Clone the properties of this AxisScale.  Should only be used
   * by ScalarMap and map should have the same DisplayScalar as
   * this scalar's
   * @param map  map to use for creating the new Axis
   * @throws VisADException  display scalars are not equal
   */
  AxisScale clone(ScalarMap map)
    throws VisADException
  {
    AxisScale newScale = new AxisScale(map);
    if (!(map.getDisplayScalar().equals(scalarMap.getDisplayScalar())))
      throw new VisADException(
        "AxisScale: DisplayScalar for map is not" + 
          scalarMap.getDisplayScalar());
    newScale.myColor = myColor;
    newScale.axisOrdinal = axisOrdinal;
    newScale.myAxis = myAxis;
    newScale.myLabel = myLabel;
    newScale.labelTable = (Hashtable) labelTable.clone();
    newScale.majorTickSpacing = majorTickSpacing;
    newScale.minorTickSpacing = minorTickSpacing;
    newScale.autoComputeTicks = autoComputeTicks;
    newScale.baseLineVisible = baseLineVisible;
    newScale.snapToBox = snapToBox;
    newScale.labelFont = labelFont;
    return newScale;
  }

  /**
   * Set major tick mark spacing. The number that is passed-in represents 
   * the distance, measured in values, between each major tick mark. If you 
   * have a ScalarMap with a range from 0 to 50 and the major tick spacing 
   * is set to 10, you will get major ticks next to the following values: 
   * 0, 10, 20, 30, 40, 50.  This value will always be used unless
   * you call <CODE>setAutoComputeTicks</CODE> with a <CODE>true</CODE> value.
   * @param spacing  spacing between major tick marks (must be > 0)
   * @see #getMajorTickSpacing
   * @see #autoComputeTicks
   */
  public void setMajorTickSpacing(double spacing)
  {
    double oldValue = majorTickSpacing;
    majorTickSpacing = Math.abs(spacing);
    autoComputeTicks = false;
    if (majorTickSpacing != oldValue) {
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * This method returns the major tick spacing.  The number that is returned
   * represents the distance, measured in values, between each major tick mark.
   *
   * @return the number of values between major ticks
   * @see #setMajorTickSpacing
   */
  public double getMajorTickSpacing() {
    return majorTickSpacing;
  }

  /**
   * Set minor tick mark spacing. The number that is passed-in represents 
   * the distance, measured in values, between each minor tick mark. If you 
   * have a ScalarMap with a range from 0 to 50 and the minor tick spacing 
   * is set to 10, you will get minor ticks next to the following values: 
   * 0, 10, 20, 30, 40, 50.  This value will always be used unless
   * you call <CODE>setAutoComputeTicks</CODE> with a <CODE>true</CODE> value.
   * @param spacing  spacing between minor tick marks (must be > 0)
   * @see #getMinorTickSpacing
   * @see #autoComputeTicks
   */
  public void setMinorTickSpacing(double spacing)
  {
    double oldValue = minorTickSpacing;
    minorTickSpacing = Math.abs(spacing);
    if (minorTickSpacing != oldValue) {
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * This method returns the minor tick spacing.  The number that is returned
   * represents the distance, measured in values, between each minor tick mark.
   *
   * @return the number of values between minor ticks
   * @see #setMinorTickSpacing
   */
  public double getMinorTickSpacing() {
    return minorTickSpacing;
  }

  /**
   * Allow the AxisScale to automatically compute the desired majorTickSpacing
   * based on the range of the ScalarMap.
   * @param true  have majorTickSpacing automatically computed.
   */
  public void setAutoComputeTicks(boolean b)
  {
    boolean oldValue = autoComputeTicks;
    autoComputeTicks = b;
    if (autoComputeTicks != oldValue) {
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * Creates a hashtable that will draw text labels starting at the
   * starting point specified using the increment field.
   * If you call createStandardLabels(10.0, 2.0), then it will
   * make labels for the values 2, 12, 22, 32, etc.
   * @see #setLabelTable
  public Hashtable createStandardLabels(double increment, double start)
  {
      return labelTable;
  }
   */

  /**
   * Used to specify what label will be drawn at any given value.
   * The key-value pairs are of this format: 
   *     <B>{ Double value, java.lang.String}</B>
   *
   * @param  labels  map of value/label pairs
   * @throws VisADException  invalid hashtable
   * @see #getLabelTable
   */
  public void setLabelTable( Hashtable labels ) 
    throws VisADException
  {
      Map oldTable = labelTable;
      labelTable = labels;
      if (labels != oldTable) {
          scalarMap.makeScale();  // update the display
      }
  }

  /**
   * Set the font used for rendering the labels
   * @param font  new font to use
   */
  public void setFont(Font font)
  {
    Font oldFont = labelFont;
    labelFont = font;
    if ((labelFont == null && oldFont != null) || !labelFont.equals(oldFont)) 
    {
      labelSize = labelFont.getSize();
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * Get the font used for rendering the labels
   * @return  font use or null if using default text plot
   */
  public Font getFont()
  {
    return labelFont;
  }

  /**
   * Set visibility of base line.
   * @param  visible   true to display (default), false to turn off
   */
  public void setBaseLineVisible(boolean visible)
  {
    boolean oldValue = baseLineVisible;
    baseLineVisible = visible;
    if (baseLineVisible != oldValue) {
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * Determine whether the base line for the scale should be visible
   * @return  true if line is visible, otherwise false;
   */
  public boolean getBaseLineVisible()
  {
    return baseLineVisible;
  }

  /**
   * Toggle whether the scale is along the box edge or not
   * @param b   true to snap to the box
   */
  public void setSnapToBox(boolean b)
  {
    boolean oldValue = snapToBox;
    snapToBox = b;
    if (snapToBox != oldValue) {
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * Determine whether this property is set.
   * @return  true if property is set, otherwise false;
   */
  public boolean getSnapToBox()
  {
    return snapToBox;
  }

  /**
   * Sets the size of the labels.  You can use this to change the label
   * size when a <CODE>Font</CODE> is not being used.  If a <CODE>Font</CODE>
   * is being used and you call setLabelSize(), a new <CODE>Font</CODE> is
   * created using the old <CODE>Font</CODE> name and style, but with the 
   * new size.
   * @param  size  font size to use
   * @see #setFont
   */
  public void setLabelSize(int size)
  {
    int oldSize = labelSize;
    labelSize = size;
    if (labelSize != oldSize) {
      if (labelFont != null) {
        labelFont = 
          new Font(labelFont.getName(), labelFont.getStyle(), labelSize);
      }
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * Gets the size of the labels.
   * @return  relative size of labels
   */ 
  public int getLabelSize()
  {
    return labelSize;
  }
}
