//
// BaseColorControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

/**
   BaseColorControl is the VisAD class for controlling N-component Color
   DisplayRealType-s.<P>
*/
public abstract class BaseColorControl extends Control {

  // color map represented by either table or function
  private float[][] table;
  private int tableLength; // = table[0].length - 1
  private Function function;
  private transient RealTupleType functionDomainType;
  private transient CoordinateSystem functionCoordinateSystem;
  private transient Unit[] functionUnits;

  private final static int DEFAULT_TABLE_LENGTH = 256;

  private transient Object lock = new Object();

  private final int components;

  public BaseColorControl(DisplayImpl d, int components) {
    super(d);
    this.components = components;
    tableLength = DEFAULT_TABLE_LENGTH;
    table = new float[components][tableLength];
    initTableVis5D(table, components);
  }
 
  // initialize table to a grey wedge
  private static void initTableGreyWedge(float[][] table, int components)
  {
    float scale = (float) (1.0f / (float) (DEFAULT_TABLE_LENGTH - 1));
    for (int i=0; i<DEFAULT_TABLE_LENGTH; i++) {
      table[0][i] = scale * i;
      table[1][i] = scale * i;
      table[2][i] = scale * i;
      if (components > 3) {
        table[3][i] = scale * i;
      }
    }
  }

  // initialize table to the Vis5D colormap
  private static void initTableVis5D(float[][] table, int components)
  {
    float curve = 1.4f;
    float bias = 1.0f;
    float rfact = 0.5f * bias;

    for (int i=0; i<DEFAULT_TABLE_LENGTH; i++) {

      /* compute s in [0,1] */
      float s = (float) i / (float) (DEFAULT_TABLE_LENGTH-1);
      float t = curve * (s - rfact);   /* t in [curve*-0.5,curve*0.5) */

      table[0][i] = (float) (0.5 + 0.5 * Math.atan( 7.0*t ) / 1.57);
      table[1][i] = (float) (0.5 + 0.5 * (2 * Math.exp(-7*t*t) - 1));
      table[2][i] = (float) (0.5 + 0.5 * Math.atan( -7.0*t ) / 1.57);
      if (components > 3) {
        table[3][i] = 1.0f;
      }
    }
  }

  public void initGreyWedge() {
    initTableGreyWedge(table, components);
  }

  public void initVis5D() {
    initTableVis5D(table, components);
  }

  /** Get the number of components of the range */
  public int getNumberOfComponents() { return components; }

  /** define the color lookup by a Function, whose MathType must
      have a 1-D domain and an N-D RealTupleType range; the domain
      and range Reals must vary over the range (0.0, 1.0) */
  public void setFunction(Function func)
         throws VisADException, RemoteException {
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

  /** return the color lookup Function */
  public Function getFunction() { return function; }

  /** define the color lookup by an array of floats which must
      have the form float[components][table_length]; values should be in
      the range (0.0, 1.0) */
  public void setTable(float[][] t) throws VisADException, RemoteException {
    if (t == null || t.length != components ||
        t[0] == null || t[1] == null || t[2] == null ||
        (components > 3 && t[3] == null) ||
        t[0].length != t[1].length || t[0].length != t[2].length ||
        (components > 3 && t[0].length != t[3].length)) {
      throw new DisplayException("BaseColorControl.setTable: " +
                                 "table must be float[" + components +
                                 "][Length]");
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

  public float[][] getTable() {
    if (table == null) return null;
    float[][] t = new float[components][tableLength];
    for (int j=0; j<components; j++) {
      System.arraycopy(table[j], 0, t[j], 0, tableLength);
    }
    return t;
  }

  /** if this BaseColorControl is defined using a color table, get
      a String that can be used to reconstruct this BaseColorControl
      later. If this BaseColorControl is defined using a Function,
      return null */
  public String getSaveString() {
    if (table == null) return null;
    int len = table.length;
    int len0 = table[0].length;
    String s = len + " x " + len0 + "\n";
    for (int i=0; i<len; i++) {
      s = s + table[i][0];
      for (int j=1; j<len0; j++) s = s + " " + table[i][j];
      s = s + "\n";
    }
    return s;
  }

  /** reconstruct this BaseColorControl using the specified save string */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    int e1 = save.indexOf(' ');
    int s2 = save.indexOf('x') + 2;
    int e2 = save.indexOf('\n');
    int len = Integer.parseInt(save.substring(0, e1));
    int len0 = Integer.parseInt(save.substring(s2, e2));
    float[][] t = new float[len][len0];
    int x = e2 + 1;
    for (int i=0; i<len; i++) {
      for (int j=0; j<len0-1; j++) {
        int ox = x;
        x = save.indexOf(' ', x + 1);
        t[i][j] = Float.parseFloat(save.substring(ox, x));
      }
      int ox = x;
      x = save.indexOf('\n', x + 1);
      t[i][len0 - 1] = Float.parseFloat(save.substring(ox, x));
    }
    setTable(t);
  }

  public float[][] lookupValues(float[] values)
         throws VisADException, RemoteException {
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
        for (int i=0; i<valLen; i++) {
          if (values[i] != values[i]) {
            colors[0][i] = Float.NaN;
            colors[1][i] = Float.NaN;
            colors[2][i] = Float.NaN;
            if (components > 3) {
              colors[3][i] = Float.NaN;
            }
          }
          else {
            int j = (int) (scale * values[i]);
            // note actual table length is tableLength + 1
            // extend first and last table entries to 'infinity'
            if (j < 0) {
              colors[0][i] = table[0][0];
              colors[1][i] = table[1][0];
              colors[2][i] = table[2][0];
              if (components > 3) {
                colors[3][i] = table[3][0];
              }
            }
            else if (tableLength <= j) {
              colors[0][i] = table[0][tblEnd];
              colors[1][i] = table[1][tblEnd];
              colors[2][i] = table[2][tblEnd];
              if (components > 3) {
                colors[3][i] = table[3][tblEnd];
              }
            }
            else {
              colors[0][i] = table[0][j];
              colors[1][i] = table[1][j];
              colors[2][i] = table[2][j];
              if (components > 3) {
                colors[3][i] = table[3][j];
              }
            }
          }
        }
      }
      else if (function != null) {
        List1DSet set = new List1DSet(values, functionDomainType,
                                      functionCoordinateSystem,
                                      functionUnits);
        Field field =
          function.resample(set, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
        colors = Set.doubleToFloat(field.getValues());
      }
    }
    return colors;
  }

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
            if (Math.abs(table[i][j] - newTable[i][j]) > 0.0001) {
              return false;
            }
          }
        }
      }
    }

    return true;
  }

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

  /** copy the state of a remote control to this control */
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
          table = bcc.table;
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
}
