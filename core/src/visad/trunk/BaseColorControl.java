//
// BaseColorControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;
import java.util.StringTokenizer;

import visad.browser.Convert;
import visad.util.Util;

/**
   BaseColorControl is the VisAD class for controlling N-component Color
   DisplayRealType-s.<P>
*/
public class BaseColorControl
  extends Control
{

  /** The index of the color red */
  public static final int RED = 0;
  /** The index of the color green */
  public static final int GREEN = 1;
  /** The index of the color blue */
  public static final int BLUE = 2;
  /**
   * The index of the alpha channel.
   * <P>
   * <B>NOTE:</B> ALPHA will always be the last index.
   */
  public static final int ALPHA = 3;

  /** The default number of colors */
  public final static int DEFAULT_NUMBER_OF_COLORS = 256;

  // color map represented by either table or function
  private float[][] table;
  private int tableLength; // = table[0].length - 1
  private Function function;
  private transient RealTupleType functionDomainType;
  private transient CoordinateSystem functionCoordinateSystem;
  private transient Unit[] functionUnits;

  private transient Object lock = new Object();

  private final int components;

  /**
   * Create a basic color control.
   *
   * @param d The display with which this control is associated.
   * @param components Either 3 (if this is a red/green/blue control)
   *        or 4 (if there is also an alpha component).
   */
  public BaseColorControl(DisplayImpl d, int components)
  {
    super(d);

    // constrain number of components to known range
    if (components < 3) {
      components = 3;
    } else if (components > 4) {
      components = 4;
    }
    this.components = components;

    tableLength = DEFAULT_NUMBER_OF_COLORS;
    table = initTableVis5D(new float[components][tableLength]);
  }

  /**
   * Initialize table to a grey wedge.
   *
   * @param table Table to be initialized.
   *
   * @return the initialized table.
   */

  public static float[][] initTableGreyWedge(float[][] table) 
  {
    return initTableGreyWedge(table, false);
  }

  public static float[][] initTableGreyWedge(float[][] table, boolean invert)
  {
    if (table == null || table[0] == null) {
      return null;
    }

    boolean hasAlpha = table.length > 3;

    final int numColors = table[0].length;
    float scale = (float) (1.0f / (float) (numColors - 1));
    for (int i=0; i<numColors; i++) {
      int idx = invert ? (numColors-1)-i : i;
      table[RED][idx]   = scale * i;
      table[GREEN][idx] = scale * i;
      table[BLUE][idx]  = scale * i;
      if (hasAlpha) {
        table[ALPHA][idx] = scale * i;
      }
    }

    return table;
  }


  /**
   * Initialize the colormap to a grey wedge
   */
  public void initGreyWedge()
  {
    initTableGreyWedge(table);
  }

  public void initGreyWedge(boolean invert)
  { 
    initTableGreyWedge(table, invert);
  }

  /**
   * Initialize table to the Vis5D colormap (opaque
   *   blue-green-red rainbow).
   *
   * @param table Table to be initialized.
   *
   * @return the initialized table.
   */
  public static float[][] initTableVis5D(float[][] table)
  {
    if (table == null || table[0] == null) {
      return null;
    }

    boolean hasAlpha = table.length > 3;

    float curve = 1.4f;
    float bias = 1.0f;
    float rfact = 0.5f * bias;

    final int numColors = table[0].length;
    for (int i=0; i<numColors; i++) {

      /* compute s in [0,1] */
      float s = (float) i / (float) (numColors-1);
      float t = curve * (s - rfact);   /* t in [curve*-0.5,curve*0.5) */

      table[RED][i] = (float) (0.5 + 0.5 * Math.atan( 7.0*t ) / 1.57);
      table[GREEN][i] = (float) (0.5 + 0.5 * (2 * Math.exp(-7*t*t) - 1));
      table[BLUE][i] = (float) (0.5 + 0.5 * Math.atan( -7.0*t ) / 1.57);
      if (hasAlpha) {
        table[ALPHA][i] = 1.0f;
      }
    }

    return table;
  }

  /**
   * Initialize the colormap to the VisAD sine waves
   */
  public void initVis5D()
  {
    initTableVis5D(table);
  }

  /**
   * Initialize table to the Hue-Saturation-Value colormap.
   *
   * @param table Table to be initialized.
   *
   * @return the initialized table.
   */
  public static float[][] initTableHSV(float[][] table)
  {
    if (table == null || table[0] == null) {
      return null;
    }

    boolean hasAlpha = table.length > 3;

    float s = 1;
    float v = 1;

    final int numColors = table[0].length;
    for (int i=0; i<numColors; i++) {

      float h = i * 6 / (float )(numColors - 1);

      int hFloor = (int )Math.floor(h);
      float hPart = h - hFloor;

      // if hFloor is even
      if ((hFloor & 1) == 0) {
        hPart = 1 - hPart;
      }

      float m = v * (1 - s);
      float n = v * (1 - s*hPart);

      switch (hFloor) {
      case 0:
      case 6:
        table[RED][i] = v;
        table[GREEN][i] = n;
        table[BLUE][i] = m;
        break;
      case 1:
        table[RED][i] = n;
        table[GREEN][i] = v;
        table[BLUE][i] = m;
        break;
      case 2:
        table[RED][i] = m;
        table[GREEN][i] = v;
        table[BLUE][i] = n;
        break;
      case 3:
        table[RED][i] = m;
        table[GREEN][i] = n;
        table[BLUE][i] = v;
        break;
      case 4:
        table[RED][i] = n;
        table[GREEN][i] = m;
        table[BLUE][i] = v;
        break;
      case 5:
        table[RED][i] = v;
        table[GREEN][i] = m;
        table[BLUE][i] = n;
        break;
      }

      if (hasAlpha) {
        table[ALPHA][i] = 1.0f;
      }
    }

    return table;
  }

  /**
   * Initialize the colormap to Hue-Saturation-Value
   */
  public void initHSV() {
    initTableHSV(table);
  }

  /**
   * Get the number of components of the range.
   *
   * @return Either 3 or 4
   */
  public int getNumberOfComponents() { return components; }

  /**
   * Get the number of colors in the table.
   *
   * @return The number of colors in the colormap.
   */
  public int getNumberOfColors() { return tableLength; }

  /**
   * Define the color lookup by a <CODE>Function</CODE>, whose
   * <CODE>MathType</CODE> must have a 1-D domain and a 3-D or
   * 4-D <CODE>RealTupleType</CODE> range; the domain and range
   * <CODE>Real</CODE>s must vary over the range (0.0, 1.0)
   *
   * @param func The new <CODE>Function</CODE>.
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception VisADException If there was a problem with the function.
   */
  public void setFunction(Function func)
    throws RemoteException, VisADException
  {
    FunctionType baseType;
    if (components == 4) {
      baseType = FunctionType.REAL_1TO4_FUNCTION;
    } else {
      baseType = FunctionType.REAL_1TO3_FUNCTION;
    }
    if (func == null ||
        !func.getType().equalsExceptName(baseType)) {
      throw new DisplayException("BaseColorControl.setFunction: " +
                                 "function must be 1D-to-" + components + "D");
    }
    synchronized (lock) {
      function = func;
      functionDomainType = ((FunctionType) function.getType()).getDomain();
      functionCoordinateSystem = function.getDomainCoordinateSystem();
      functionUnits = function.getDomainUnits();
      table = null;
    }
    changeControl(true);
  }

  /**
   * Return the color lookup <CODE>Function</CODE>.
   *
   * @return The function which defines this object's colors.
   */
  public Function getFunction() { return function; }

  /**
   * Define the color lookup by an array of <CODE>float</CODE>s
   * which must have the form <CODE>float[components][table_length]</CODE>;
   * values should be in the range (0.0, 1.0)
   *
   * @param t The new table of colors.
   *
   * @exception RemoteException If there was a problem changing the control.
   * @exception VisADException If there is a problem with the table.
   */
  public void setTable(float[][] t)
    throws RemoteException, VisADException
  {
    if (t == null || t[0] == null) {
      throw new DisplayException(getClass().getName() + ".setTable: " +
                                 "Null table");
    }

    if (t.length != components) {
      if (t[0].length == components) {
        throw new DisplayException(getClass().getName() + ".setTable: " +
                                   " Table may be inverted");
      }
      throw new DisplayException(getClass().getName() + ".setTable: " +
                                 "Unusable table [" + t.length + "][" +
                                 t[0].length + "], expected [" + components +
                                 "][]");
    }

    if (t[RED] == null || t[GREEN] == null || t[BLUE] == null ||
        (t.length > ALPHA && t[ALPHA] == null))
    {
      throw new DisplayException(getClass().getName() + ".setTable: " +
                                 "One or more component lists is null");
    }

    if (t[RED].length != t[GREEN].length || t[RED].length != t[BLUE].length ||
        (components > ALPHA && t[RED].length != t[ALPHA].length))
    {
      throw new DisplayException("BaseColorControl.setTable: " +
                                 "Inconsistent table lengths");
    }

    synchronized (lock) {
      tableLength = t[0].length;
      table = new float[components][tableLength];
      for (int j=0; j<components; j++) {
        System.arraycopy(t[j], 0, table[j], 0, tableLength);
      }
      function = null;
    }
    changeControl(true);
  }

  /**
   * Get the table of colors.
   *
   * @return The color table.
   */
  public float[][] getTable()
  {
    if (table == null) return null;
    float[][] t = new float[components][tableLength];
    for (int j=0; j<components; j++) {
      System.arraycopy(table[j], 0, t[j], 0, tableLength);
    }
    return t;
  }

  /**
   * If the colors are defined using a color table, get a
   * <CODE>String</CODE> that can be used to reconstruct this
   * object later. If the colors are defined using a
   * <CODE>Function</CODE>, return null.
   *
   * @return The save string describing this object.
   */
  public String getSaveString()
  {
    if (table == null) return null;
    int len = table.length;
    int len0 = table[0].length;
    StringBuffer sb = new StringBuffer(15 * len * len0);
    sb.append(len);
    sb.append(" x ");
    sb.append(len0);
    sb.append('\n');
    for (int j=0; j<len0; j++) {
      sb.append(table[RED][j]);
      for (int i=1; i<len; i++) {
        sb.append(' ');
        sb.append(table[i][j]);
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  /**
   * Reconstruct this control using the specified save string.
   *
   * @param save The save string.
   *
   * @exception VisADException If the save string is not valid.
   * @exception RemoteException If there was a problem setting the table.
   */
  public void setSaveString(String save)
    throws RemoteException, VisADException
  {
    if (save == null) throw new VisADException("Invalid save string");
    StringTokenizer st = new StringTokenizer(save);
    int numTokens = st.countTokens();
    if (numTokens < 3) throw new VisADException("Invalid save string");

    // get table size
    int len = Convert.getInt(st.nextToken());
    if (len < 1) {
      throw new VisADException("First dimension is not positive");
    }
    if (!st.nextToken().equalsIgnoreCase("x")) {
      throw new VisADException("Invalid save string");
    }
    int len0 = Convert.getInt(st.nextToken());
    if (len0 < 1) {
      throw new VisADException("Second dimension is not positive");
    }
    if (numTokens < 3 + len * len0) {
      throw new VisADException("Not enough table entries");
    }

    // get table entries
    float[][] t = new float[len][len0];
    for (int j=0; j<len0; j++) {
      for (int i=0; i<len; i++) t[i][j] = Convert.getFloat(st.nextToken());
    }
    setTable(t);
  }

  /**
   * Return a list of colors for specified values.
   *
   * @param values		The values to look up.  It is expected that
   *				they nominally lie in the range of 0 through 1.
   *				Values outside this range will be assigned the
   *				color of the nearest end.  NaN values will be
   *				assigned NaN colors.
   * @return			The list of colors.  Element <code>[i][j]</code>
   *				is the value of the <code>i</code>-th color
   *				component for <code>values[j]</code>, where
   *				<code>i</code> is {@link #RED}, {@link #GREEN},
   *				{@link #BLUE}, or {@link #ALPHA}.  A component
   *				value is in the range from 0 through 1, or is
   *				NaN.
   * @throws RemoteException	If there was an RMI-related problem.
   * @throws VisADException	If the function encountered a problem.
   */
  public float[][] lookupValues(float[] values)
    throws RemoteException, VisADException
  {
    if (values == null) {
      return null;
    }

    final int tblEnd = tableLength - 1;
    final int valLen = values.length;

    float[][] colors = null;
    synchronized (lock) {
      if (table != null) {
        colors = new float[components][valLen];
        float scale = (float) tableLength;
        try {
          for (int i=0; i<valLen; i++) {
            if (values[i] != values[i]) {
              colors[RED][i] = Float.NaN;
              colors[GREEN][i] = Float.NaN;
              colors[BLUE][i] = Float.NaN;
              if (components > ALPHA) {
                colors[ALPHA][i] = Float.NaN;
              }
            }
            else {
              int j = (int) (scale * values[i]);
              // note actual table length is tableLength + 1
              // extend first and last table entries to 'infinity'
              if (j < 0) {
                colors[RED][i] = table[RED][0];
                colors[GREEN][i] = table[GREEN][0];
                colors[BLUE][i] = table[BLUE][0];
                if (components > ALPHA) {
                  colors[ALPHA][i] = table[ALPHA][0];
                }
              }
              else if (tableLength <= j) {
                colors[RED][i] = table[RED][tblEnd];
                colors[GREEN][i] = table[GREEN][tblEnd];
                colors[BLUE][i] = table[BLUE][tblEnd];
                if (components > ALPHA) {
                  colors[ALPHA][i] = table[ALPHA][tblEnd];
                }
              }
              else {
                colors[RED][i] = table[RED][j];
                colors[GREEN][i] = table[GREEN][j];
                colors[BLUE][i] = table[BLUE][j];
                if (components > ALPHA) {
                  colors[ALPHA][i] = table[ALPHA][j];
                }
              }
            }
          } // end for (int i=0; i<valLen; i++)
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
      }
      else if (function != null) {
        List1DSet set = new List1DSet(values, functionDomainType,
                                      functionCoordinateSystem,
                                      functionUnits);
        Field field =
          function.resample(set, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);
        colors = Set.doubleToFloat(field.getValues());
      }
    }
    return colors;
  }

  /**
   * Return a list of colors for the specified range.
   */
  public float[][] lookupRange(int left, int right)
    throws VisADException, RemoteException
  {
    if (left < 0 || right >= tableLength || left > right) {
      throw new VisADException("Bad left/right value");
    }

    final int tblEnd = tableLength - 1;
    final int valLen = (right - left) + 1;

    float[][] colors = null;
    synchronized (lock) {
      if (table != null) {
        colors = new float[components][valLen];
        for (int i=0; i<valLen; i++) {
          colors[RED][i] = table[RED][i+left];
          colors[GREEN][i] = table[GREEN][i+left];
          colors[BLUE][i] = table[BLUE][i+left];
          if (components > ALPHA) {
            colors[ALPHA][i] = table[ALPHA][i+left];
          }
        }
      } else if (function != null) {
        double scale = (double) tableLength;
        Linear1DSet set = new Linear1DSet(functionDomainType,
                                          (double ) (left / scale),
                                          (double ) (right / scale), valLen,
                                          functionCoordinateSystem,
                                          functionUnits, null);
        Field field =
          function.resample(set, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
        colors = Set.doubleToFloat(field.getValues());
      }
    }
    return colors;
  }

  /**
   * Set the specified range to the specified colors.
   */
  public void setRange(int left, int right, float[][] colors)
    throws VisADException, RemoteException
  {
    if (left < 0 || right >= tableLength || left > right) {
      throw new VisADException("Bad left/right value");
    }

    if (colors == null || colors.length != components ||
        colors[RED] == null || colors[GREEN] == null ||
        colors[BLUE] == null ||
        (colors.length > ALPHA && colors[ALPHA] == null))
    {
      throw new VisADException("Bad range table!");
    }

    if (table == null) {
      throw new VisADException("Cannot set values for function!");
    }

    final int valLen = (right - left) + 1;

    if (colors[RED].length != valLen || colors[GREEN].length != valLen ||
        colors[BLUE].length != valLen ||
        (colors.length > ALPHA && colors[ALPHA].length != valLen))
    {
      throw new VisADException("Array does not contain " + valLen +
                               " colors!");
    }

    synchronized (lock) {
      for (int i=0; i<valLen; i++) {
        table[RED][i+left] = colors[RED][i];
        table[GREEN][i+left] = colors[GREEN][i];
        table[BLUE][i+left] = colors[BLUE][i];
        if (components > ALPHA) {
          table[ALPHA][i+left] = colors[ALPHA][i];
        }
      }
    }
    changeControl(true);
  }

  /**
   * Compare the specified table to this object's table.
   *
   * @param newTable Table to compare.
   *
   * @return <CODE>true</CODE> if <CODE>newTable</CODE> is the
   *         same as this object's table.
   */
  private boolean tableEquals(float[][] newTable)
  {
    if (table == null) {
      if (newTable != null) {
        return false;
      }
    } else if (newTable == null) {
      return false;
    } else if (table != newTable) {
      if (table.length != newTable.length) {
        return false;
      } else {
        int i;
        for (i = 0; i < table.length; i++) {
          if (table[i].length != newTable[i].length) {
            return false;
          }
        }
        for (i = 0; i < table.length; i++) {
          for (int j = 0; j < table[i].length; j++) {
            if (!Util.isApproximatelyEqual(table[i][j], newTable[i][j])) {
              return false;
            }
          }
        }
      }
    }

    return true;
  }

  /**
   * Compare the specified function to this object's function.
   *
   * @param newFunc Function to compare.
   *
   * @return <CODE>true</CODE> if <CODE>newFunc</CODE> is the
   *         same as this object's function.
   */
  private boolean functionEquals(Function newFunc)
  {
    if (function == null) {
      if (newFunc != null) {
        return false;
      }
    } else if (newFunc == null) {
      return false;
    } else if (!function.equals(newFunc)) {
      return false;
    }

    return true;
  }

  /**
   * Copy the state of a remote control to this control.
   *
   * @param rmt The control to be copied.
   *
   * @exception VisADException If the remote control cannot be copied.
   */
  public void syncControl(Control rmt)
    throws VisADException
  {
    if (rmt == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(rmt instanceof BaseColorControl)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    BaseColorControl bcc = (BaseColorControl )rmt;

    boolean changed = false;

    boolean tableChanged = !tableEquals(bcc.table);
    boolean functionChanged = !functionEquals(bcc.function);

    if (tableChanged) {
      if (bcc.table == null) {
        if (functionChanged ? bcc.function == null : function == null) {
          throw new VisADException("BaseColorControl has null Table," +
                                   " but no Function");
        }

        table = null;
      } else {
        if (bcc.table.length != components) {
          throw new VisADException("Table must be float[" + components +
                                   "][], not float[" + bcc.table.length +
                                   "][]");
        }
        synchronized (lock) {
          tableLength = bcc.table[0].length;
          for (int i = 0; i < components; i++) {
            if (table[i].length != bcc.table[i].length) {
              table[i] = new float[bcc.table[i].length];
            }
            System.arraycopy(bcc.table[i], 0, table[i], 0,
                             bcc.table[i].length);
          }
          tableLength = table[0].length;
          function = null;
        }
        try {
          changeControl(true);
        } catch (RemoteException re) {
          throw new VisADException("Could not indicate that control" +
                                   " changed: " + re.getMessage());
        }
      }
    }
    if (functionChanged) {
      if (bcc.function == null) {
        if (table == null) {
          throw new VisADException("ColorControl has null Function," +
                                   " but no Table");
        }

        function = null;
      } else {
        try {
          setFunction(bcc.function);
        } catch (RemoteException re) {
          throw new VisADException("Could not set function: " +
                                   re.getMessage());
        }
      }
    }
  }

  /**
   * Return <CODE>true</CODE> if this object is "equal" to the parameter.
   *
   * @param o Object to compare.
   *
   * @return <CODE>true</CODE> if this object "equals" <CODE>o</CODE>.
   */
  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    BaseColorControl bcc = (BaseColorControl )o;

    if (tableLength != bcc.tableLength) {
      return false;
    }
    if (!tableEquals(bcc.table)) {
      return false;
    }
    if (!functionEquals(bcc.function)) {
      return false;
    }

    return true;
  }

  public Object clone()
  {
    BaseColorControl bcc = (BaseColorControl )super.clone();
    if (table != null) {
      bcc.table = new float[table.length][];
      for (int i = table.length - 1; i >= 0; i--) {
        bcc.table[i] = (float[] )table[i].clone();
      }
    }

    return bcc;
  }

  private static char dirChar(int down, int same, int up)
  {
    char ch;

    if (down == 0 || same == 0 || up == 0) {
      if (down > 0) {
        if (up > 0) {
          if (down > up) {
            return 'v';
          }

          return '^';
        }

        if (same > 0) {
          return '~';
        }

        return '\\';
      } else if (up > 0) {
        if (same > 0) {
          return '~';
        }

        return '/';
      } else {
        return '_';
      }
    }

    if (down > same) {
      if (down > (same + up)) {
        return '\\';
      }

      if (up > (down + same)) {
        return '/';
      }

      if (up > same) {
        return '^';
      }
    }

    if (up > same) {
      if (up > (down + same)) {
        return '/';
      }

      if (down > (same + up)) {
        return '\\';
      }
    }

    if (same > (down + up)) {
      return '-';
    }

    return '~';
  }

  public String toString()
  {
    int binLen = tableLength;
    int binSize = 1;
    while (binLen > 32) {
      binLen >>= 1;
      binSize <<= 1;
    }

    String className = getClass().getName();
    int dot = className.lastIndexOf('.');
    if (dot >= 0) {
      className = className.substring(dot+1);
    }

    StringBuffer buf = new StringBuffer(className);
    buf.append('[');

    String colorInitial = "RGBA";
    for (int c = 0; c < components; c++) {
      if (c > 0) {
        buf.append(',');
      }
      buf.append(colorInitial.charAt(c));
      buf.append('=');

      float prev = table[c][0];

      int tot = 0;
      while (tot < tableLength) {
        int trendDown, trendSame, trendUp;
        trendDown = trendSame = trendUp = 0;

        for (int i = 0; i < binSize; i++) {
          float curr = table[c][tot+i];

          if (Math.abs(curr - prev) <= 0.0001) {
            trendSame++;
          } else if (curr < prev) {
            trendDown++;
          } else {
            trendUp++;
          }

          prev = curr;
        }

        buf.append(dirChar(trendDown, trendSame, trendUp));

        tot += binSize;
      }
    }

    buf.append(']');
    return buf.toString();
  }
}
