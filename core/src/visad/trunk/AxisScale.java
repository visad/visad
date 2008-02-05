//
// AxisScale.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Color;
import java.util.*;
import java.awt.Font;
import visad.util.HersheyFont;
import java.text.*;

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
  public static final int X_AXIS = 0;
  /** Y_AXIS identifier */
  public static final int Y_AXIS = 1;
  /** Z_AXIS identifier */
  public static final int Z_AXIS = 2;
  /** identifier for primary label side of axis*/
  public static final int PRIMARY = 0;
  /** identifier for secondary label side of axis*/
  public static final int SECONDARY = 1;
  /** identifier for tertiary label side of axis*/
  public static final int TERTIARY = 2;
  /** identifier for quaternary label side of axis*/
  public static final int QUATERNARY = 3;

  // WLH 12 July 2001
  // true indicates axis is stationary relative to screen
  // rather than graphics coordinates
  private boolean screenBased = false;
  private boolean gridLinesVisible = false;
  private boolean ticksVisible = true;
  private boolean labelBothSides = false;

  private VisADLineArray scaleArray;
  private VisADTriangleArray labelArray;
  private ScalarMap scalarMap;
  private Color myColor = Color.white;
  private double[] dataRange = new double[2];
  private int myAxis = -1;
  private int axisOrdinal = -1;
  private String myTitle;
  private Hashtable labelTable;
  private double majorTickSpacing = 0.0;
  private double minorTickSpacing = 0.0;
  private double tickBase = 0.0;
  private boolean autoComputeTicks = true;
  private boolean baseLineVisible = true;
  private boolean snapToBox = false;
  private boolean userLabels = false;
  private boolean visibility = true;
  private boolean labelAllTicks = false;  // label major ticks
  private Object labelFont = null;
  private int labelSize = 12;
  private int axisSide = PRIMARY;
  private int tickOrient = PRIMARY;
  private static final double TICKSIZE = .5;  // major ticks are 1/2 char ht.
  private NumberFormat labelFormat = null;

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
      !displayScalar.equals(Display.ZAxis)) {
        throw new DisplayException("AxisSale: DisplayScalar " +
                                   "must be XAxis, YAxis or ZAxis");
    }
    myAxis = (displayScalar.equals(Display.XAxis)) ? X_AXIS :
       (displayScalar.equals(Display.YAxis)) ? Y_AXIS : Z_AXIS;
    myTitle = scalarMap.getScalarName();
    visibility = scalarMap.getScaleEnable();
    labelTable = new Hashtable();
    DisplayImpl display = scalarMap.getDisplay();
    if (display != null) {
      DisplayRenderer displayRenderer = display.getDisplayRenderer();
      if (displayRenderer != null) {
        float[] rgb = displayRenderer.getRendererControl().getForegroundColor();
        myColor = new Color(rgb[0], rgb[1], rgb[2]);
        boolean ok = makeScale();
      }
    }
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
   * @deprecated
   * Set the label to be used for this axis.  The default is the
   * ScalarName of the ScalarMap.
   * @param  label  label to be used
   * @see #setTitle(String)
   */
  public void setLabel(String label)
  {
    setTitle(label);
  }

  /**
   * @deprecated
   * Get the label of the AxisScale.
   * @return label
   * @see #getTitle()
   */
  public String getLabel()
  {
    return getTitle();
  }

  /**
   * Set the title to be used for this axis.  The default is the
   * ScalarName of the ScalarMap.
   * @param  title  title to be used
   */
  public void setTitle(String title)
  {
    String oldTitle = myTitle;
    myTitle = title;
    if (!myTitle.equals(oldTitle) ) {
      try {
        // check for case where this was called from scalarmap.setScalarName()
        if ( !myTitle.equals(scalarMap.getScalarName()) )
        {
          scalarMap.setScalarName(myTitle);
        }
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * Get the title of the AxisScale.
   * @return title
   */
  public String getTitle()
  {
    return myTitle;
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
   * set screenBased mode
   *   true indicates axis is stationary relative to screen
  */
  public void setScreenBased(boolean sb) {
    screenBased = sb;
  }

  /**
   * return screenBased mode
   * @return  true if axis is stationary relative to screen
  */
  public boolean getScreenBased() {
    return screenBased;
  }

  /**
   * Create the scale for screen based.
   * @return  true if scale was successfully created, otherwise false
   */
  public boolean makeScreenBasedScale(double xmin, double ymin,
                                      double xmax, double ymax,
                                      double XTMIN, double YTMIN,
                                      double XTMAX, double YTMAX)
         throws VisADException {
    DisplayImpl display = scalarMap.getDisplay();
    if (display == null) return false;
    DisplayRenderer displayRenderer = display.getDisplayRenderer();
    if (displayRenderer == null) return false;
    if (axisOrdinal < 0) return false;
    dataRange = scalarMap.getRange();
    // boolean twoD = displayRenderer.getMode2D();
    if (!displayRenderer.getMode2D()) return false;
    boolean twoD = true;

    ProjectionControl pcontrol = display.getProjectionControl();
    double[] aspect = pcontrol.getAspectCartesian();
    double oldMax = 1.0;
    double oldMin = -1.0;
    double newMax = 1.0;
    double newMin = -1.0;
    if (myAxis == X_AXIS) {
      oldMax = aspect[0];
      oldMin = -aspect[0];
      newMax = XTMAX;
      newMin = XTMIN;
    }
    else if (myAxis == Y_AXIS) {
      oldMax = aspect[1];
      oldMin = -aspect[1];
      newMax = YTMAX;
      newMin = YTMIN;
    }

    double mult = (dataRange[1] - dataRange[0]) / (oldMax - oldMin);
    double d1 = (newMax - oldMin) * mult + dataRange[0];
    double d0 = (newMin - oldMin) * mult + dataRange[0];
    double[] dr = {d0, d1};

    double zmin = 0.0;
    double zmax = -zmin;

    // set scale according to labelSize
    double scale =  labelSize/200.;
    double offset = 1.05;

    // WLH 20 Feb 2003 keep screen-based YAxis label in Frame
    scale *= 0.6; // hack size for screen based
    // scale *= 0.8; // hack size for screen based

    // Add 16-APR-2001 DRM
    int position = 0;
    int myPosition = 0;
    // Snap to the box edge instead of being offset
    if (snapToBox) {
      offset = 1.0;
    }
    else
    {
      for (Enumeration e = display.getMapVector().elements();
            e.hasMoreElements();)
      {
        ScalarMap map = (ScalarMap) e.nextElement();
        if (map.getDisplayScalar().equals(scalarMap.getDisplayScalar()))
        {
          if (getSide() == map.getAxisScale().getSide()) 
          {
            if (!map.equals(scalarMap)) // same side someone else
            {
              position++;
            } else {
              myPosition = position;
            }
          }
        }
      }
    }
    /*
    System.out.println(scalarMap + "is at position " + (myPosition+1) +
                       " out of " + (position + 1));
    */
    // End Add 16-APR-2001 DRM

    // position of baseline for this scale
    double line = 4.0 * myPosition * scale;  // DRM 17-APR-2001

    return makeScale(twoD, xmin, ymin, zmin, xmax, ymax, zmax,
                     scale, offset, line, dr);
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
    boolean twoD = displayRenderer.getMode2D();

  // now create scale along axis at axisOrdinal position in array
  // twoD may help define orientation

// WLH 24 Nov 2000
    ProjectionControl pcontrol = display.getProjectionControl();
    double[] aspect = pcontrol.getAspectCartesian();

    double xmin = -aspect[0];
    double ymin = -aspect[1];
    double zmin = -aspect[2];

    double xmax = -xmin;
    double ymax = -ymin;
    double zmax = -zmin;

    // set scale according to labelSize
    double scale =  labelSize/200.;
    double offset = 1.05;

    // Add 16-APR-2001 DRM
    int position = 0;
    int myPosition = 0;
    // Snap to the box edge instead of being offset
    if (snapToBox) {
      offset = 1.0;
    }
    else
    {
      for (Enumeration e = display.getMapVector().elements();
            e.hasMoreElements();)
      {
        ScalarMap map = (ScalarMap) e.nextElement();
        if (map.getDisplayScalar().equals(scalarMap.getDisplayScalar()))
        {
          if (getSide() == map.getAxisScale().getSide()) {
            if (!map.equals(scalarMap)) // same side someone else
            {
              position++;
            } else {
              myPosition = position;// this is me
            }
          }
        }
      }
    }
    /*
    System.out.println(scalarMap + "is at position " + (myPosition+1) +
                       " out of " + (position + 1));
    */
    // End Add 16-APR-2001 DRM

    // position of baseline for this scale
    double line = 4.0 * myPosition * scale;  // DRM 17-APR-2001

    /*  Remove 16-APR-2001
    double one = 1.0;
    if (dataRange[0] > dataRange[1]) one = -1.0; // inverted range
    int position = axisOrdinal;
    if (snapToBox) {
      offset = 1.0;
      position = 0;
    }
    double line = 2.0 * position * scale;
    */


    return makeScale(twoD, xmin, ymin, zmin, xmax, ymax, zmax,
                     scale, offset, line, dataRange);
  }

  /** inner logic of makeScale with no references to display, displayRenderer
      or scalarMap, allwoing more flexible placement of scales */
  public boolean makeScale(boolean twoD, double xmin, double ymin, double zmin,
                           double xmax, double ymax, double zmax,
                           double scale, double offset, double line,
                           double[] dataRange)
         throws VisADException {

// start new method here
// no references to display, scalarMap, or displayRenderer past this point


    // compute graphics positions
    // these are {x, y, z} vectors
    double[] base = null; // vector from one character to another
    double[] up = null; // vector from bottom of character to top
    double[] startn = null; // -1.0 position along axis
    double[] startp = null; // +1.0 position along axis
    double[] gridstartn = null; // -1.0 position along axis
    double[] gridstartp = null; // +1.0 position along axis

    int numSides = (getLabelBothSides()) ? 2 : 1;
    Vector lineArrayVector = new Vector(4*numSides);
    Vector labelArrayVector = new Vector();

    double one = 1.0;
    if (dataRange[0] > dataRange[1]) one = -1.0; // inverted range

    for (int l = 0; l < numSides; l++) {
      int side = getSide();
      side = (side + l) % (twoD ? 2 : 4);
      // set up the defaults for each of the axes.  startp and startn are the
      // endpoints of the axis line.  base and up determine which way the
      // tick marks are drawn along that line.  For 2-D, base and up are changed
      // later on so that the labels are right side up. DRM 16-APR-2001
      if (myAxis == X_AXIS) {
        if (side == PRIMARY) {
          base = new double[] {scale, 0.0, 0.0};
          up = new double[] {0.0, scale, scale};
          startp = new double[] {one * xmax,
                                 ymin - ((offset - 1.0) + line),
                                 zmin - ((offset - 1.0) + line)};
          startn = new double[] {one * xmin,
                                 ymin - ((offset - 1.0) + line),
                                 zmin - ((offset - 1.0) + line)};
          gridstartp = new double[] {one * xmax, ymin, zmin};
          gridstartn = new double[] {one * xmin, ymin, zmin};
        }
        else if (side == SECONDARY) {
          base = new double[] {-scale, 0.0, 0.0};
          up = new double[] {0.0, -scale, scale};
          startp = new double[] {one * xmax,
                                 ymax + ((offset - 1.0) + line),
                                 zmin - ((offset - 1.0) + line)};
          startn = new double[] {one * xmin,
                                 ymax + ((offset - 1.0) + line),
                                 zmin - ((offset - 1.0) + line)};
          gridstartp = new double[] {one * xmax, ymax, zmin};
          gridstartn = new double[] {one * xmin, ymax, zmin};
        }
        else if (side == TERTIARY) {
          base = new double[] {scale, 0.0, 0.0};
          up = new double[] {0.0, scale, -scale};
          startp = new double[] {one * xmax,
                                 ymin - ((offset - 1.0) + line),
                                 zmax + ((offset - 1.0) + line)};
          startn = new double[] {one * xmin,
                                 ymin - ((offset - 1.0) + line),
                                 zmax + ((offset - 1.0) + line)};
          gridstartp = new double[] {one * xmax, ymin, zmax};
          gridstartn = new double[] {one * xmin, ymin, zmax};
        }
        else { // side == QUATERNARY
          base = new double[] {-scale, 0.0, 0.0};
          up = new double[] {0.0, -scale, -scale};
          startp = new double[] {one * xmax,
                                 ymax + ((offset - 1.0) + line),
                                 zmax + ((offset - 1.0) + line)};
          startn = new double[] {one * xmin,
                                 ymax + ((offset - 1.0) + line),
                                 zmax + ((offset - 1.0) + line)};
          gridstartp = new double[] {one * xmax, ymax, zmax};
          gridstartn = new double[] {one * xmin, ymax, zmax};
        }
      }
      else if (myAxis == Y_AXIS) {
        if (side == PRIMARY) {
          base = new double[] {0.0, -scale, 0.0};
          up = new double[] {scale, 0.0, scale};
          startp = new double[] {xmin - ((offset - 1.0) + line),
                                 one * ymax,
                                 zmin - ((offset - 1.0) + line)};
          startn = new double[] {xmin - ((offset - 1.0) + line),
                                 one * ymin,
                                 zmin - ((offset - 1.0) + line)};
          gridstartp = new double[] {xmin, one * ymax, zmin};
          gridstartn = new double[] {xmin, one * ymin, zmin};
        }
        else if (side == SECONDARY) {
          base = new double[] {0.0, scale, 0.0};
          up = new double[] {-scale, 0.0, scale};
          startp = new double[] {xmax + ((offset - 1.0) + line),
                                 one * ymax,
                                 zmin - ((offset - 1.0) + line)};
          startn = new double[] {xmax + ((offset - 1.0) + line),
                                 one * ymin,
                                 zmin - ((offset - 1.0) + line)};
          gridstartp = new double[] {xmax, one * ymax, zmin};
          gridstartn = new double[] {xmax, one * ymin, zmin};
        }
        else if (side == TERTIARY) {
          base = new double[] {0.0, -scale, 0.0};
          up = new double[] {scale, 0.0, -scale};
          startp = new double[] {xmin - ((offset - 1.0) + line),
                                 one * ymax,
                                 zmax + ((offset - 1.0) + line)};
          startn = new double[] {xmin - ((offset - 1.0) + line),
                                 one * ymin,
                                 zmax + ((offset - 1.0) + line)};
          gridstartp = new double[] {xmin, one * ymax, zmax};
          gridstartn = new double[] {xmin, one * ymin, zmax};
        }
        else { // side == QUATERNARY
          base = new double[] {0.0, scale, 0.0};
          up = new double[] {-scale, 0.0, -scale};
          startp = new double[] {xmax + ((offset - 1.0) + line),
                                 one * ymax,
                                 zmax + ((offset - 1.0) + line)};
          startn = new double[] {xmax + ((offset - 1.0) + line),
                                 one * ymin,
                                 zmax + ((offset - 1.0) + line)};
          gridstartp = new double[] {xmax, one * ymax, zmax};
          gridstartn = new double[] {xmax, one * ymin, zmax};
        }
      }
      else if (myAxis == Z_AXIS) {
        if (side == PRIMARY) {
          base = new double[] {0.0, 0.0, -scale};
          up = new double[] {scale, scale, 0.0};
          startp = new double[] {xmin - ((offset - 1.0) + line),
                                 ymin - ((offset - 1.0) + line),
                                 one * zmax};
          startn = new double[] {xmin - ((offset - 1.0) + line),
                                 ymin - ((offset - 1.0) + line),
                                 one * zmin};
          gridstartp = new double[] {xmin, ymin, one * zmax};
          gridstartn = new double[] {xmin, ymin, one * zmin};
        }
        else if (side == SECONDARY) {
          base = new double[] {0.0, 0.0, scale};
          up = new double[] {-scale, scale, 0.0};
          startp = new double[] {xmax + ((offset - 1.0) + line),
                                 ymin - ((offset - 1.0) + line),
                                 one * zmax};
          startn = new double[] {xmax + ((offset - 1.0) + line),
                                 ymin - ((offset - 1.0) + line),
                                 one * zmin};
          gridstartp = new double[] {xmax, ymin, one * zmax};
          gridstartn = new double[] {xmax, ymin, one * zmin};
        }
        else if (side == TERTIARY) {
          base = new double[] {0.0, 0.0, -scale};
          up = new double[] {scale, -scale, 0.0};
          startp = new double[] {xmin - ((offset - 1.0) + line),
                                 ymax + ((offset - 1.0) + line),
                                 one * zmax};
          startn = new double[] {xmin - ((offset - 1.0) + line),
                                 ymax + ((offset - 1.0) + line),
                                 one * zmin};
          gridstartp = new double[] {xmin, ymax, one * zmax};
          gridstartn = new double[] {xmin, ymax, one * zmin};
        }
        else { // side == QUATERNARY
          base = new double[] {0.0, 0.0, scale};
          up = new double[] {-scale, -scale, 0.0};
          startp = new double[] {xmax + ((offset - 1.0) + line),
                                 ymax + ((offset - 1.0) + line),
                                 one * zmax};
          startn = new double[] {xmax + ((offset - 1.0) + line),
                                 ymax + ((offset - 1.0) + line),
                                 one * zmin};
          gridstartp = new double[] {xmax, ymax, one * zmax};
          gridstartn = new double[] {xmax, ymax, one * zmin};
        }
      }
  
      if (twoD) {
        if (myAxis == Z_AXIS) return false;  // can't have Z in 2D
        // zero out z coordinates
        base[2] = 0.0;
        up[2] = 0.0;
        startn[2] = 0.0;
        startp[2] = 0.0;
      }
  
      // VisADLineArray coordinates have three entries for (x, y, z) of each point
      // two points determine a line segment,
      // hence 6 coordinates entries per segment
  
      // base line for axis
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
  
      double range = Math.abs(dataRange[1] - dataRange[0]);
      double min = Math.min(dataRange[0], dataRange[1]);
      double max = Math.max(dataRange[0], dataRange[1]);
      //System.out.println(
      //  "range = " + range + " min = " + min + " max = " + max);
  
      // compute tick mark values
      double tens = 1.0;
      if (range < tens) {
        tens /= 10.0;
        while (range < tens) tens /= 10.0;
      }
      else {
        while (10.0 * tens <= range) tens *= 10.0;
      }
      // now tens <= range < 10.0 * tens;
      if (autoComputeTicks || majorTickSpacing <= 0)
      {
        double ratio = range / tens;
        if (ratio < 2.0) {
          tens = tens/5.0;
        }
        else if (ratio < 4.0) {
          tens = tens/2.0;
        }
        majorTickSpacing = tens;
      }
      // now tens = interval between major tick marks (majorTickSpacing)
      //System.out.println("computed ticks " + majorTickSpacing);
  
      double[] hilo = computeTicks(max, min, tickBase, majorTickSpacing);
      // firstValue is the first Tick mark value
      double firstValue = hilo[0];
      double botval = hilo[0];
      double topval = hilo[hilo.length-1];
  
      // draw major tick marks
      VisADLineArray majorTickArray = new VisADLineArray();
      int nticks = (int) ((topval-botval)/majorTickSpacing) + 1;
      float[] majorCoordinates = new float[6 * nticks];
      double[] tickup = up;
      if (getTickOrientation() != PRIMARY)
      {
        if (myAxis == X_AXIS) {
          tickup = new double[] {up[0], -up[1], -up[2]};
        }
        else if (myAxis == Y_AXIS) {
          tickup = new double[] {-up[0], up[1], -up[2]};
        }
        else if (myAxis == Z_AXIS) {
          tickup = new double[] {-up[0], -up[1], up[2]};
        }
      }
      // initialize some stuff
      int k = 0;
      if (ticksVisible) {
        for (int j = 0; j< nticks; j++) //Change DRM 21-Feb-2001
        {
          double value = firstValue + (j * majorTickSpacing);
          double a = (value - min) / (max - min);
          for (int i=0; i<3; i++) {
            if ((k + 3 + i) < majorCoordinates.length) {
              // guard against error that cannot happen, but was seen?
              majorCoordinates[k + i] =
                (float) ((1.0 - a) * startn[i] + a * startp[i]);
              majorCoordinates[k + 3 + i] =
                (float) (majorCoordinates[k + i] - TICKSIZE * tickup[i]);
            }
          }
          k += 6;
        }
  
        majorTickArray.vertexCount = 2 * (nticks);
        majorTickArray.coordinates = majorCoordinates;
        lineArrayVector.add(majorTickArray);
      }
  
      if (gridLinesVisible && l == 0) {
        VisADLineArray gridArray = new VisADLineArray();
        float[] gridCoordinates = new float[6 * nticks];
        // initialize some stuff
        k = 0;
        double[] gridup = null;
        double gridLength = 1.0;
        if (myAxis == X_AXIS) {
          gridup = new double[] {0, up[1], 0};
          gridLength = (ymax-ymin)/scale;
        }
        else if (myAxis == Y_AXIS) {
          gridup = new double[] {up[0], 0, 0};
          gridLength = (xmax-xmin)/scale;
        }
        else if (myAxis == Z_AXIS) {
          gridup = new double[] {up[0], 0, 0};
          gridLength = (xmax-xmin)/scale;
        }
        for (int j = 0; j< nticks; j++) //Change DRM 21-Feb-2001
        {
          double value = firstValue + (j * majorTickSpacing);
          double a = (value - min) / (max - min);
          for (int i=0; i<3; i++) {
            if ((k + 3 + i) < gridCoordinates.length) {
              // guard against error that cannot happen, but was seen?
              gridCoordinates[k + i] =
                (float) ((1.0 - a) * gridstartn[i] + a * gridstartp[i]);
              gridCoordinates[k + 3 + i] =
                (float) (gridCoordinates[k + i] + gridLength*gridup[i]);
            }
          }
          k += 6;
        }
        gridArray.vertexCount = 2 * (nticks);
        gridArray.coordinates = gridCoordinates;
        lineArrayVector.add(gridArray);
      }
  
      // create an array for the minor ticks
      if (getMinorTickSpacing() > 0 && ticksVisible)  
      {
        hilo = computeTicks(max, min, tickBase, minorTickSpacing);
        // now lower * minorTickSpacing = value of lowest tick mark, and
        // upper * minorTickSpacing = values of highest tick mark
  
        VisADLineArray minorTickArray = new VisADLineArray();
        // Change DRM 21-Feb-2001
        nticks = (int) ((hilo[hilo.length-1]-hilo[0])/minorTickSpacing) + 1;
        float[] minorCoordinates = new float[6 * nticks];
  
        // draw tick marks
        k = 0;
        //for (long j=lower; j<=upper; j++) {  // Change DRM 21-Feb-2001
        for (int j = 0; j < nticks; j++)
        {
          double val = hilo[0] + (j * minorTickSpacing);
          double a = (val - min) / (max - min);
          for (int i=0; i<3; i++) {
            if ((k + 3 + i) < minorCoordinates.length) {
              // guard against error that cannot happen, but was seen?
              minorCoordinates[k + i] =
                (float) ((1.0 - a) * startn[i] + a * startp[i]);
              // minor ticks are half the size of the major ticks
              minorCoordinates[k + 3 + i] =
                (float) (minorCoordinates[k + i] - TICKSIZE/2 * tickup[i]);
            }
          }
          k += 6;
        }
        minorTickArray.vertexCount = 2 * (nticks);
        minorTickArray.coordinates = minorCoordinates;
        lineArrayVector.add(minorTickArray);
      }
  
      // Title and labels
      // by default, all labels rendered centered
       TextControl.Justification justification =
         TextControl.Justification.CENTER;
  
      // PlotText is controlled by the initial starting point, base (controls
      // direction) and up (which way is up).  We handle 2D and 3D differently.
      // In 2-D, titles are drawn along the positive direction of the axis.
      // Labels are drawn in the Y-positive direction.
  
  
      // Labels first
      if (twoD) {
        if (myAxis == X_AXIS) {
           up = new double[] {0.0, scale, 0.0};
        }
        else if (myAxis == Y_AXIS) {
           up = new double[] {-scale, 0.0, 0.0};
        }
      }
  
      // Draw the labels.  If user hasn't defined their own, make defaults.
      if (!userLabels) {
        createStandardLabels(topval, botval, botval, 
                             (labelAllTicks == false)
                                ?(topval - botval):majorTickSpacing, 
                             false);
      }
  
      double dist = 1.0 + TICKSIZE;   // dist from the line in the up direction;
      double[] updir = (twoD != true) ? up : new double[] {0.0, scale, 0.0};
      if (twoD) {
        base = new double[] {scale, 0.0, 0.0};
        if (myAxis == X_AXIS) {
           dist = (side == PRIMARY)
             ? (1.0 + TICKSIZE + .15)
             : -(TICKSIZE + .15);
        }
        else if (myAxis == Y_AXIS) {
           dist = (side == PRIMARY)
             ? -(TICKSIZE + .15)
             : (TICKSIZE + .15);
           justification =
             (side == PRIMARY)
                 ? TextControl.Justification.RIGHT
                 : TextControl.Justification.LEFT;
        }
      }
  
      // Added by Luke Catania on 05/07/2002
      // Added maximumYAxisTickLabelSize & yAxisLabelLength to calculate 
      // offset for Y-Axis label.
      //
      int maximumYAxisTickLabelSize = 1;
      int yAxisLabelLength=0;
      Hashtable localTable;
      synchronized(labelTable) {
          localTable = new Hashtable(labelTable);
      }
      for (Enumeration e = localTable.keys(); e.hasMoreElements();)
      {
        Double value;
        try {
          value = (Double) e.nextElement();
        } catch (ClassCastException cce) {
          throw new VisADException("Invalid keys in label hashtable");
        }
        double test = value.doubleValue();
        if (test > max || test < min) continue; // don't draw labels beyond range
  
        // Added by Luke Catania on 05/07/2002 - mods by DRM 28-Oct-2002
        // For Y-Axis only, calculate offset for axis label, so it does 
        // not overlap the tick labels.
        if (myAxis == Y_AXIS) {
          yAxisLabelLength = ((String) localTable.get(value)).length();
          if (yAxisLabelLength > maximumYAxisTickLabelSize)
            maximumYAxisTickLabelSize = yAxisLabelLength;
        }
        double val = (test - min) / (max - min);
        // center label on tick if Y axis and 2D
        if ((myAxis == Y_AXIS) && (twoD == true)) val -= .2 * scale; // HACK!!!!!
  
        double[] point = new double[3];
        for (int j=0; j < 3; j++) {
          point[j] = (1.0 - val) * startn[j] + val * startp[j] - dist * up[j];
  
  //        if (myAxis == Y_AXIS) System.out.println("Axis & Tick Label Position for " + test + ": " + startn[j] + ":" + startp[j] + ":" + point[j]);
        }
  
        /*
        System.out.println("For label = " + value.doubleValue() + "(" + val + "), point is (" + point[0] + "," + point[1] + "," + point[2] + ")");
        */
  
        if (labelFont == null)
        {
          VisADLineArray label =
            PlotText.render_label((String) localTable.get(value), point, base, updir, justification);
          lineArrayVector.add(label);
        }
        else if (labelFont instanceof Font)
        {
          VisADTriangleArray label =
            PlotText.render_font(
                (String) localTable.get(value), (Font) labelFont, point, base,
                updir, justification);
          labelArrayVector.add(label);
  
        } else if (labelFont instanceof HersheyFont) {
          VisADLineArray label =
            PlotText.render_font(
                (String) localTable.get(value), (HersheyFont) labelFont,
                   point, base, updir, justification);
          lineArrayVector.add(label);
        }
      }
  
      // Title
      double[] startlabel = new double[3];
      dist = 2.0 + TICKSIZE;   // dist from the line in the up direction;
      justification =
         TextControl.Justification.CENTER;
      if (twoD) {
        if (myAxis == X_AXIS) {
           base = new double[] {scale, 0.0, 0.0};
           up = new double[] {0.0, scale, 0.0};
           dist = (side == PRIMARY)
             ? 2.5 + TICKSIZE
             : -(1.5 + TICKSIZE - .05);
        }
        else if (myAxis == Y_AXIS) {
           base = new double[] {0.0, scale, 0.0};
           up = new double[] {-scale, 0.0, 0.0};
           dist = (side == PRIMARY)
             ? -(.5 + TICKSIZE + maximumYAxisTickLabelSize)
             : (.5 + TICKSIZE + maximumYAxisTickLabelSize) ;
        }
      }
      for (int i=0; i<3; i++) {
        startlabel[i] = 0.5 * (startn[i] + startp[i]) - dist * up[i];
      }
      /*
      System.out.println("For title, point is (" +
        startlabel[0] + "," + startlabel[1] + "," + startlabel[2] + ")");
      */
  
      if (labelFont == null)
      {
        VisADLineArray plotArray =
          PlotText.render_label(myTitle, startlabel, base, up, justification);
        lineArrayVector.add(plotArray);
      }
      else if (labelFont instanceof java.awt.Font)
      {
        VisADTriangleArray nameArray =
          PlotText.render_font(myTitle, (Font) labelFont,
                               startlabel, base, up, justification);
        labelArrayVector.add(nameArray);
      } else if (labelFont instanceof visad.util.HersheyFont) {
        VisADLineArray plotArray =
          PlotText.render_font(myTitle, (HersheyFont) labelFont,
                               startlabel, base, up, justification);
        lineArrayVector.add(plotArray);
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
    newScale.myTitle = myTitle;
    newScale.labelTable = (Hashtable) labelTable.clone();
    newScale.majorTickSpacing = majorTickSpacing;
    newScale.minorTickSpacing = minorTickSpacing;
    newScale.autoComputeTicks = autoComputeTicks;
    newScale.baseLineVisible = baseLineVisible;
    newScale.snapToBox = snapToBox;
    newScale.labelFont = labelFont;
    newScale.labelSize = labelSize;
    newScale.axisSide = axisSide;
    newScale.tickOrient = tickOrient;
    newScale.userLabels = userLabels;
    newScale.labelAllTicks = labelAllTicks;
    newScale.gridLinesVisible = gridLinesVisible;
    newScale.ticksVisible = ticksVisible;
    newScale.labelBothSides = labelBothSides;
    return newScale;
  }

  /**
   * Set major tick mark spacing. The number that is passed-in represents
   * the distance, measured in values, between each major tick mark. If you
   * have a ScalarMap with a range from 0 to 50 and the major tick spacing
   * is set to 10, you will get major ticks next to the following values:
   * 0, 10, 20, 30, 40, 50.  This value will always be used unless
   * you call {@link #setAutoComputeTicks(boolean) setAutoComputeTicks}
   * with a <CODE>true</CODE> value.
   * @param spacing  spacing between major tick marks (must be > 0)
   * @see #getMajorTickSpacing
   * @see #setAutoComputeTicks
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
   * you call {@link #setAutoComputeTicks(boolean) setAutoComputeTicks}
   * with a <CODE>true</CODE> value.
   * @param spacing  spacing between minor tick marks (must be > 0)
   * @see #getMinorTickSpacing
   * @see #setAutoComputeTicks
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
   * If you call createStandardLabels(100, 0, 2.0, 10.0), then it will
   * make labels for the values 2, 12, 22, 32, etc.
   *
   * @see #setLabelTable
   * @throws IllegalArgumentException  if min > max, or increment is
   *                                   greater than max-min
   */
  public void createStandardLabels(
    double max, double min, double base, double increment)
  {
    if (min > max) {
      throw new IllegalArgumentException("max must be greater than min");
    }
    if (increment > (max-min)) {
      throw new IllegalArgumentException(
        "increment must be less than or equal to range (max-min)");
    }
    createStandardLabels(max, min, base, increment, true);
  }

  /**
   * private copy to allow program to create table, but not remake scale
   */
  private void createStandardLabels(
    double max, double min, double base, double increment, boolean byuser)
  {
    synchronized(labelTable) {
       labelTable.clear();
       double[] values = computeTicks(max, min, base, increment);
       if (values != null) {
          for (int i = 0; i < values.length; i++) {
            labelTable.put(new Double(values[i]), createLabelString(values[i]));
          }
        }
    }
    if (byuser) {
      try {
        userLabels = true;
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

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
      userLabels = true;
      scalarMap.makeScale();  // update the display
    }
  }

  /**
   * Get the Hashtable used for labels
   */
  public Hashtable getLabelTable()
  {
    return labelTable;
  }

  /**
   * Set the font used for rendering the labels
   * @param font  new font to use
   */
  public void setFont(Font font)
  {
    Object oldFont = labelFont;
    labelFont = font;
    //if ((labelFont == null && oldFont != null) || !labelFont.equals(oldFont))
    if (labelFont != null && !labelFont.equals(oldFont))
    {
      if (labelFont instanceof java.awt.Font) labelSize = ((Font) labelFont).getSize();
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * Set the font used for rendering the labels
   * @param font  new font to use
   */
  public void setFont(HersheyFont font)
  {
    Object oldFont = labelFont;
    labelFont = font;
    //if ((labelFont == null && oldFont != null) || !labelFont.equals(oldFont))
    if (labelFont != null && !labelFont.equals(oldFont))
    {
      labelSize = 12;
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
    return (labelFont instanceof Font) ? (Font)labelFont : null;
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
        if (labelFont instanceof java.awt.Font) labelFont =
            new Font( ((Font)labelFont).getName(),
                      ((Font)labelFont).getStyle(), labelSize);
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

  /**
   * Sets the base value for tick marks.  This only applies when
   * <CODE>setMajorTickSpacing</CODE> or <CODE>setMinorTickSpacing</CODE>
   * have been called.
   * @param  base  base value for drawing tick marks.  For example, if
   *               your scale ranges from -4 to 18 and you set the
   *               major tick spacing to 5, you will get ticks at
   *               -4, 1, 6, 11, and 16 by default.  If you set the tick
   *               base value to 0, you will get ticks at 0, 5, 10, 15.
   */
  public void setTickBase(double base)
  {
    double oldBase = tickBase;
    tickBase = base;
    if (tickBase != oldBase) {
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * Set side for axis (PRIMARY, SECONDARY)
   * @param side side for axis to appear on
   */
  public void setSide(int side)
  {
    // sanity check
    if (side != PRIMARY && side != SECONDARY &&
      side != TERTIARY && side != QUATERNARY)
    {
      return;
    }
    if (axisSide == side) return;
    axisSide = side;
    try {
      scalarMap.makeScale();  // update the display
    }
    catch (VisADException ve) {;}
  }

  /**
   * Get the alignment for the axis
   * @return  axis alignment (PRIMARY or SECONDARY)
   */
  public int getSide()
  {
    return axisSide;
  }

  /**
   * Set orientation of tick marks along the axis line.
   * @param orient (PRIMARY or SECONDARY)
   */
  public void setTickOrientation(int orient)
  {
    double oldOrient = tickOrient;
    tickOrient =
      (orient == SECONDARY) ? SECONDARY : PRIMARY;  // sanity check
    if (tickOrient != oldOrient) {
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * Get the orientation for the ticks along the axis
   * @return  tick orientation (PRIMARY or SECONDARY)
   */
  public int getTickOrientation()
  {
    return tickOrient;
  }

  /**
   * Set the formatting for all labels
   * @param format  format string
   */
  public void setNumberFormat(NumberFormat format)
  {
    labelFormat = format;
  }

  /**
   * Get the formatting for labels.  May be null (if not set)
   * @return format used for labeling
   */
  public NumberFormat getNumberFormat() { return labelFormat; }

  /**
   * Set the visibility of the AxisScale
   * @param visibile  true to display the AxisScale
   */
  public void setVisible(boolean visible) {
    boolean oldVisibility = visibility;
    visibility = visible;
    if (!(oldVisibility == visibility) ) {
      try {
        // check for case if this was called from scalarmap.setScaleEnable()
        if ( !(visible == scalarMap.getScaleEnable()) ) {
          scalarMap.setScaleEnable(visible);
	}
        scalarMap.makeScale();  // update the display
      } catch (VisADException ve) {;}
    }
  }

  /**
   * Get the visibility of the AxisScale
   * @return true if AxisScale is being rendered
   */
  public boolean isVisible() {
    return scalarMap.getScaleEnable();
  }

  /**
   * Set the visibility of the grid lines; Grid lines are placed
   * at major tick marks.
   * @param visibile  true to display the grid lines
   */
  public void setGridLinesVisible(boolean show) {
    boolean oldShow = gridLinesVisible;
    gridLinesVisible = show;
    if (!(oldShow == show) ) {
      try {
        scalarMap.makeScale();  // update the display
      } catch (VisADException ve) {;}
    }
  }

  /**
   * Get the visibility of the grid lines
   * @return true if grid lines are being rendered
   */
  public boolean getGridLinesVisible() {
    return gridLinesVisible;
  }

  /**
   * Set whether both sides are labeled.
   * @param visibile  true to label both sides
   */
  public void setLabelBothSides(boolean both) {
    boolean oldBoth = labelBothSides;
    labelBothSides = both;
    if (!(oldBoth == both) ) {
      try {
        scalarMap.makeScale();  // update the display
      } catch (VisADException ve) {;}
    }
  }

  /**
   * See if both sides are labeled
   * @return true if labelling is on both sides
   */
  public boolean getLabelBothSides() {
    return labelBothSides;
  }


  /**
   * Set whether ticks are visible
   * @param visibile  true to show ticks
   */
  public void setTicksVisible(boolean visible) {
    boolean oldValue = ticksVisible;
    ticksVisible = visible;
    if (!(oldValue == visible) ) {
      try {
        scalarMap.makeScale();  // update the display
      } catch (VisADException ve) {;}
    }
  }

  /**
   * See if ticks are visible
   * @return true if labelling is on both sides
   */
  public boolean getTicksVisible() {
    return ticksVisible;
  }


  /**
   * Set whether all major ticks should be labeled.  The default is
   * to only label the first and last major tick.  This setting is 
   * ignored if user labels are being used or if user manually
   * calls {@link #createStandardLabels(double, double, double, double) 
   * createStandardLabels} or {@link #setLabelTable(Hashtable) 
   * setLabelTable}
   * @see #getLabelAllTicks()
   * @see #createStandardLabels(double, double, double, double)
   * @see #setLabelTable(Hashtable)
   *
   * @param labelAll  true to label all (major) ticks.  Overridden
   */
  public void setLabelAllTicks(boolean labelAll) {
    boolean oldValue = labelAllTicks;
    labelAllTicks = labelAll;
    if (labelAllTicks != oldValue) {
      try {
        scalarMap.makeScale();  // update the display
      }
      catch (VisADException ve) {;}
    }
  }

  /**
   * Return whether all major ticks are to be labeled.
   * @return true if ticks are to be labeled.
   */
  public boolean getLabelAllTicks() {
     return labelAllTicks;
  }

  /** compute the tick mark values */
  private double[] computeTicks(double high, double low,
                                double base, double interval)
  {
    double[] vals = null;

    // compute nlo and nhi, for low and high contour values in the box
    long nlo = Math.round((Math.ceil((low - base) / Math.abs(interval))));
    long nhi = Math.round((Math.floor((high - base) / Math.abs(interval))));

    // how many contour lines are needed.
    int numc = (int) (nhi - nlo) + 1;
    if (numc < 1) return new double[] {low, high};

    vals = new double[numc];

    for(int i = 0; i < numc; i++) {
      vals[i] = base + (nlo + i) * interval;
    }

    return vals;
  }

  /** create the default string for a value */
  private String createLabelString(double value)
  {
    String     label = null;
    ScalarType sType = scalarMap.getScalar();
    if (sType instanceof RealType)
    {
      RealType rType = (RealType)sType;
      Unit     unit = rType.getDefaultUnit();
      if (Unit.canConvert(CommonUnit.secondsSinceTheEpoch, unit) &&
        !unit.getAbsoluteUnit().equals(unit))
      {
         label = new Real(rType, value).toValueString();
      }
      else
      {
        label =
          (labelFormat != null)
            ? labelFormat.format(value)
            : PlotText.shortString(value);
      }
    }
    else
    {
      label =
        (labelFormat != null)
          ? labelFormat.format(value)
          : PlotText.shortString(value);
    }
    return label;
  }

}
