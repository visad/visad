
//
// ColorControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
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

import java.rmi.*;

/**
   ColorControl is the VisAD class for controlling 3-component Color
   DisplayRealType-s (e.g., RGB, HSV, CMY).<P>
*/
public class ColorControl extends Control {

  // color map represented by either table or function
  private float[][] table;
  private int tableLength; // = table[0].length - 1
  private Function function;
  private RealTupleType functionDomainType;
  private CoordinateSystem functionCoordinateSystem;
  private Unit[] functionUnits;

  private final static int DEFAULT_TABLE_LENGTH = 256;

  public ColorControl(DisplayImpl d) {
    super(d);
    tableLength = DEFAULT_TABLE_LENGTH;
    table = new float[3][tableLength + 1];
    float scale = (float) (1.0f / (float) (DEFAULT_TABLE_LENGTH - 1));
    // default table is a grey wedge
    for (int i=0; i<DEFAULT_TABLE_LENGTH; i++) {
      table[0][i] = scale * i;
      table[1][i] = scale * i;
      table[2][i] = scale * i;
    }
    table[0][DEFAULT_TABLE_LENGTH] = table[0][DEFAULT_TABLE_LENGTH - 1];
    table[1][DEFAULT_TABLE_LENGTH] = table[1][DEFAULT_TABLE_LENGTH - 1];
    table[2][DEFAULT_TABLE_LENGTH] = table[2][DEFAULT_TABLE_LENGTH - 1];
  }
 
  /** define the color lookup by a Function, whose MathType must
      have a 1-D domain and a 3-D RealTupleType range; the domain
      and range Reals must vary over the range (0.0, 1.0) */
  public synchronized void setFunction(Function func)
         throws VisADException, RemoteException {
    if (func == null ||
        !func.getType().equalsExceptName(FunctionType.REAL_1TO3_FUNCTION)) {
      throw new DisplayException("ColorControl.setFunction: " +
                                 "function must be 1D-to-3D");
    }
    function = func;
    functionDomainType = ((FunctionType) function.getType()).getDomain();
    functionCoordinateSystem = function.getDomainCoordinateSystem();
    functionUnits = function.getDomainUnits();
    table = null;
    changeControl(true);
  }

  /** define the color lookup by an array of floats which must
      have the form float[3][table_length]; values should be in
      the range (0.0, 1.0) */
  public synchronized void setTable(float[][] t)
         throws VisADException, RemoteException {
    if (t == null || t.length != 3 ||
        t[0] == null || t[1] == null || t[2] == null ||
        t[0].length != t[1].length || t[0].length != t[2].length) {
      throw new DisplayException("ColorControl.setTable: " +
                                 "table must be float[3][Length]");
    }
    tableLength = t[0].length;
    table = new float[3][tableLength + 1];
    for (int j=0; j<3; j++) {
      System.arraycopy(t[j], 0, table[j], 0, tableLength);
      // guard for table overflow on scaling in lookupValues
      table[j][tableLength] = t[j][tableLength - 1];
    }
    function = null;
    changeControl(true);
  }

  public float[][] getTable() {
    if (table == null) return null;
    float[][] t = new float[3][tableLength];
    for (int j=0; j<3; j++) {
      System.arraycopy(table[j], 0, t[j], 0, tableLength);
    }
    return t;
  }

  public synchronized float[][] lookupValues(float[] values)
         throws VisADException, RemoteException {
    int len = values.length;
    float[][] colors = null;
    if (table != null) {
      colors = new float[3][len];
      float scale = (float) tableLength;
      for (int i=0; i<len; i++) {
        if (values[i] != values[i]) {
          colors[0][i] = Float.NaN;
          colors[1][i] = Float.NaN;
          colors[2][i] = Float.NaN;
        }
        else {
          int j = (int) (scale * values[i]);
          // note actual table length is tableLength + 1
/* WLH 27 April 99
          if (j < 0 || tableLength < j) {
            colors[0][i] = Float.NaN;
            colors[1][i] = Float.NaN;
            colors[2][i] = Float.NaN;
          }
*/
          // WLH 27 April 99
          // extend first and last table entries to 'infinity'
          if (j < 0) {
            colors[0][i] = table[0][0];
            colors[1][i] = table[1][0];
            colors[2][i] = table[2][0];
          }
          else if (tableLength < j) {
            colors[0][i] = table[0][tableLength];
            colors[1][i] = table[1][tableLength];
            colors[2][i] = table[2][tableLength];
          }
          else {
            colors[0][i] = table[0][j];
            colors[1][i] = table[1][j];
            colors[2][i] = table[2][j];
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
    return colors;
  }

}

