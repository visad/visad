
//
// BaseColorControl.java
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
   BaseColorControl is the VisAD class for controlling 4-component Color
   DisplayRealType-s (e.g., RGBA).<P>
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

  public BaseColorControl(DisplayImpl d) {
    super(d);
    tableLength = DEFAULT_TABLE_LENGTH;
    table = new float[4][tableLength + 1];
    float scale = (float) (1.0f / (float) (DEFAULT_TABLE_LENGTH - 1));
    // default table is a grey wedge
    for (int i=0; i<DEFAULT_TABLE_LENGTH; i++) {
      table[0][i] = scale * i;
      table[1][i] = scale * i;
      table[2][i] = scale * i;
      table[3][i] = scale * i;
    }
    table[0][DEFAULT_TABLE_LENGTH] = table[0][DEFAULT_TABLE_LENGTH - 1];
    table[1][DEFAULT_TABLE_LENGTH] = table[1][DEFAULT_TABLE_LENGTH - 1];
    table[2][DEFAULT_TABLE_LENGTH] = table[2][DEFAULT_TABLE_LENGTH - 1];
    table[3][DEFAULT_TABLE_LENGTH] = table[3][DEFAULT_TABLE_LENGTH - 1];
  }
 
  public synchronized void setFunction(Function func)
         throws VisADException, RemoteException {
    if (func == null ||
        !func.getType().equalsExceptName(FunctionType.REAL_1TO4_FUNCTION)) {
      throw new DisplayException("BaseColorControl.setFunction: " +
                                 "function must be 1D-to-4D");
    }
    function = func;
    functionDomainType = ((FunctionType) function.getType()).getDomain();
    functionCoordinateSystem = function.getDomainCoordinateSystem();
    functionUnits = function.getDomainUnits();
    table = null;
    changeControl(true);
  }

  public synchronized void setTable(float[][] t)
         throws VisADException, RemoteException {
    if (t == null || t.length != 4 ||
        t[0] == null || t[1] == null || t[2] == null || t[3] == null ||
        t[0].length != t[1].length || t[0].length != t[2].length ||
        t[0].length != t[3].length) {
      throw new DisplayException("BaseColorControl.setTable: " +
                                 "table must be float[4][Length]");
    }
    tableLength = t[0].length;
    table = new float[4][tableLength + 1];
    for (int j=0; j<4; j++) {
      System.arraycopy(t[j], 0, table[j], 0, tableLength);
      // guard for table overflow on scaling in lookupValues
      table[j][tableLength] = t[j][tableLength - 1];
    }
    function = null;
    changeControl(true);
  }

  public synchronized float[][] lookupValues(float[] values)
         throws VisADException, RemoteException {
    int len = values.length;
    float[][] colors = null;
    if (table != null) {
      colors = new float[4][len];
      float scale = (float) tableLength;
      for (int i=0; i<len; i++) {
        int j = (int) (scale * values[i]);
        // note actual table length is tableLength + 1
        if (j < 0 || tableLength < j) {
          colors[0][i] = Float.NaN;
          colors[1][i] = Float.NaN;
          colors[2][i] = Float.NaN;
          colors[3][i] = Float.NaN;
        }
        else {
          colors[0][i] = table[0][j];
          colors[1][i] = table[1][j];
          colors[2][i] = table[2][j];
          colors[3][i] = table[3][j];
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

