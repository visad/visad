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
  private RealTupleType functionDomainType;
  private CoordinateSystem functionCoordinateSystem;
  private Unit[] functionUnits;

  private final static int DEFAULT_TABLE_LENGTH = 256;

  private Object lock = new Object();

  private final int components;

  public BaseColorControl(DisplayImpl d, int components) {
    super(d);
    this.components = components;
    tableLength = DEFAULT_TABLE_LENGTH;
    table = new float[components][tableLength + 1];
    initTableVis5D(table, components);
    table[0][DEFAULT_TABLE_LENGTH] = table[0][DEFAULT_TABLE_LENGTH - 1];
    table[1][DEFAULT_TABLE_LENGTH] = table[1][DEFAULT_TABLE_LENGTH - 1];
    table[2][DEFAULT_TABLE_LENGTH] = table[2][DEFAULT_TABLE_LENGTH - 1];
    if (components > 3) {
      table[3][DEFAULT_TABLE_LENGTH] = table[3][DEFAULT_TABLE_LENGTH - 1];
    }
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
      table = new float[components][tableLength + 1];
      for (int j=0; j<components; j++) {
        System.arraycopy(t[j], 0, table[j], 0, tableLength);
        // guard for table overflow on scaling in lookupValues
        table[j][tableLength] = t[j][tableLength - 1];
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

  public float[][] lookupValues(float[] values)
         throws VisADException, RemoteException {
    int len = values.length;
    float[][] colors = null;
    synchronized (lock) {
      if (table != null) {
        colors = new float[components][len];
        float scale = (float) tableLength;
        for (int i=0; i<len; i++) {
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
  /* WLH 27 April 99
            if (j < 0 || tableLength < j) {
              colors[0][i] = Float.NaN;
              colors[1][i] = Float.NaN;
              colors[2][i] = Float.NaN;
              if (components > 3) {
                colors[3][i] = Float.NaN;
              }
            }
  */
            // WLH 27 April 99
            // extend first and last table entries to 'infinity'
            if (j < 0) {
              colors[0][i] = table[0][0];
              colors[1][i] = table[1][0];
              colors[2][i] = table[2][0];
              if (components > 3) {
                colors[3][i] = table[3][0];
              }
            }
            else if (tableLength < j) {
              colors[0][i] = table[0][tableLength];
              colors[1][i] = table[1][tableLength];
              colors[2][i] = table[2][tableLength];
              if (components > 3) {
                colors[3][i] = table[3][tableLength];
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
}
