
//
// Gridded1DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

import java.io.*;

/**
   Gridded1DSet represents a finite set of samples of R.<P>
*/
public class Gridded1DSet extends GriddedSet {

  int LengthX;
  double LowX, HiX;

  public Gridded1DSet(MathType type, double[][] samples, int lengthX)
         throws VisADException {
    this(type, samples, lengthX, null, null, null);
  }

  public Gridded1DSet(MathType type, double[][] samples, int lengthX,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    super(type, samples, make_lengths(lengthX), coord_sys, units, errors);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];

    if (Samples != null && Lengths[0] > 1) {
      // samples consistency test
      boolean Pos = (Samples[0][1] <= Samples[0][0]);
      for (int i=1; i<LengthX; i++) {
        if ((Samples[0][i] <= Samples[0][i-1]) != Pos) {
          throw new SetException(
           "Gridded1DSet: samples do not form a valid grid ("+i+")");
        }
      }
    }
  }

  static int[] make_lengths(int lengthX) {
    int[] lens = new int[1];
    lens[0] = lengthX;
    return lens;
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public double[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    if (Samples == null) {
      // not used - over-ridden by Linear1DSet.indexToValue
      double[][] grid = new double[1][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          grid[0][i] = (double) index[i];
        }
        else {
          grid[0][i] = -1;
        }
      }
      return gridToValue(grid);
    }
    else {
      double[][] values = new double[1][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          values[0][i] = Samples[0][index[i]];
        }
        else {
          values[0][i] = Double.NaN;
        }
      }
      return values;
    }
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(double[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("Gridded1DSet.valueToIndex: bad dimension");
    }
    int length = value[0].length;
    int[] index = new int[length];

    double[][] grid = valueToGrid(value);
    double[] grid0 = grid[0];
    double g;
    for (int i=0; i<length; i++) {
      g = grid0[i];
      index[i] = Double.isNaN(g) ? -1 : ((int) (g + 0.5));
    }
    return index;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public double[][] gridToValue(double[][] grid) throws VisADException {
    if (grid.length < DomainDimension) {
      throw new SetException("Gridded1DSet.gridToValue: bad dimension");
    }
    if (Lengths[0] < 2) {
      throw new SetException("Gridded1DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = grid[0].length;
    double[][] value = new double[1][length];
    for (int i=0; i<length; i++) {
      // let g be the current grid coordinate
      double g = grid[0][i];
      if ( (g < -0.5) || (g > LengthX-0.5) ) {
        value[0][i] = Double.NaN;
        continue;
      }
      // calculate closest integer variable
      int ig;
      if (g < 0) ig = 0;
      else if (g >= LengthX-1) ig = LengthX - 2;
      else ig = (int) g;
      double A = g - ig;  // distance variable
      // do the linear interpolation
      value[0][i] = (1-A)*Samples[0][ig] + A*Samples[0][ig+1];
    }
    return value;
  }

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public double[][] valueToGrid(double[][] value) throws VisADException {
    if (value.length < DomainDimension) {
      throw new SetException("Gridded1DSet.valueToGrid: bad dimension");
    }
    if (Lengths[0] < 2) {
      throw new SetException("Gridded1DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = value[0].length;
    double[][] grid = new double[1][length];
    double gridguess = LengthX/2;
    for (int i=0; i<length; i++) {
      double upper = LengthX-1;
      double lower = 0;
      // gridguess starts at previous value unless there was no solution
      if ( (i != 0) && (Double.isNaN(grid[0][i-1])) ) {
        gridguess = LengthX/2;
      }
      // grid value should default to NaN in case the algorithm fails
      grid[0][i] = Double.NaN;
      // don't try to solve missing values
      if (Double.isNaN(value[0][i])) continue;
      for (int itnum=0; itnum<LengthX; itnum++) {
        // calculate closest integer variable
        int ig;
        if (gridguess < 0) ig = 0;
        else if (gridguess > LengthX-2) ig = LengthX - 2;
        else ig = (int) gridguess;
        // calculate distance variables
        double A = gridguess - ig;
        double B = 1-A;
        // Linear interpolation algorithm for the value of gridguess
        double fg = B*Samples[0][ig] + A*Samples[0][ig+1];
        if (fg == value[0][i]) {
          // The guess hit it right on the mark
          grid[0][i] = gridguess;
          break;
        }
        else if (  ( (Samples[0][ig] <= value[0][i])
                  && (value[0][i] <= Samples[0][ig+1]) )
                || ( (ig == 0) && (value[0][i] <= Samples[0][1]) )
                || ( (ig == LengthX-2)
                  && (value[0][i] >= Samples[0][ig]) )  ) {
          // Solve with Newton's Method
          double solv = gridguess - ((fg-value[0][i])
                       /(Samples[0][ig+1]-Samples[0][ig]));
          if ( (solv > LengthX-0.5) || (solv < -0.5) ) {
            solv = Double.NaN;
          }
          grid[0][i] = solv;
          break;
        }
        else if (fg < value[0][i]) lower = gridguess;
        else if (fg > value[0][i]) upper = gridguess;
        gridguess = (upper+lower)/2;
      }
    }
    return grid;
  }

  public double getLowX() {
    return LowX;
  }

  public double getHiX() {
    return HiX;
  }

  public Object clone() {
    try {
      return new Gridded1DSet(Type, Samples, Length, DomainCoordinateSystem,
                              SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Gridded1DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Gridded1DSet(type, Samples, Length, DomainCoordinateSystem,
                             SetUnits, SetErrors);
  }

  /* run 'java visad.Gridded1DSet < formatted_input_stream'
     to test the Gridded1DSet class */
  public static void main(String[] args) throws VisADException {

    // Define input stream
    InputStreamReader inStr = new InputStreamReader(System.in);

    // Define temporary integer array
    int[] ints = new int[80];
    try {
      ints[0] = inStr.read();
    }
    catch(Exception e) {
      System.out.println("Gridded1DSet: "+e);
    }
    int l = 0;
    while (ints[l] != 10) {
      try {
        ints[++l] = inStr.read();
      }
      catch (Exception e) {
        System.out.println("Gridded1DSet: "+e);
      }
    }
    // convert array of integers to array of characters
    char[] chars = new char[l];
    for (int i=0; i<l; i++) {
      chars[i] = (char) ints[i];
    }
    int num_coords = Integer.parseInt(new String(chars));

    // Define size of Samples array
    double[][] samp = new double[1][num_coords];
    System.out.println("num_dimensions = 1, num_coords = "+num_coords+"\n");

    // Skip blank line
    try {
      ints[0] = inStr.read();
    }
    catch (Exception e) {
      System.out.println("Gridded1DSet: "+e);
    }

    for (int c=0; c<num_coords; c++) {
      l = 0;
      try {
        ints[0] = inStr.read();
      }
      catch (Exception e) {
        System.out.println("Gridded1DSet: "+e);
      }
      while (ints[l] != 32) {
        try {
          ints[++l] = inStr.read();
        }
        catch (Exception e) {
          System.out.println("Gridded1DSet: "+e);
        }
      }
      chars = new char[l];
      for (int i=0; i<l; i++) {
        chars[i] = (char) ints[i];
      }
      samp[0][c] = (Double.valueOf(new String(chars))).doubleValue();
    }

    // do EOF stuff
    try {
      inStr.close();
    }
    catch (Exception e) {
      System.out.println("Gridded1DSet: "+e);
    }

    // Set up instance of Gridded1DSet
    RealType vis_data = new RealType("vis_data", null, null);
    RealType[] vis_array = {vis_data};
    RealTupleType vis_tuple = new RealTupleType(vis_array);
    Gridded1DSet gSet1D = new Gridded1DSet(vis_tuple, samp, num_coords);

    System.out.println("Lengths = " + num_coords + " wedge = ");
    int[] wedge = gSet1D.getWedge();
    for (int i=0; i<wedge.length; i++) System.out.println(" " + wedge[i]);

    // Print out Samples information
    System.out.println("Samples ("+gSet1D.LengthX+"):");
    for (int i=0; i<gSet1D.LengthX; i++) {
      System.out.println("#"+i+":\t"+gSet1D.Samples[0][i]);
    }

    // Test gridToValue function
    System.out.println("\ngridToValue test:");
    int myLength = gSet1D.LengthX+1;
    double[][] myGrid = new double[1][myLength];
    for (int i=0; i<myLength; i++) {
      myGrid[0][i] = i-0.5;
    }
    myGrid[0][0] += 0.1;          // don't let grid values get too
    myGrid[0][myLength-1] -= 0.1; // close to interpolation limits
    double[][] myValue = gSet1D.gridToValue(myGrid);
    for (int i=0; i<myLength; i++) {
        System.out.println("("+((double) Math.round(1000000
                                        *myGrid[0][i]) /1000000)+")\t-->  "
                              +((double) Math.round(1000000
                                        *myValue[0][i]) /1000000));
    }

    // Test valueToGrid function
    System.out.println("\nvalueToGrid test:");
    double[][] gridTwo = gSet1D.valueToGrid(myValue);
    for (int i=0; i<gridTwo[0].length; i++) {
      System.out.println(((double) Math.round(1000000
                                  *myValue[0][i]) /1000000)+"  \t-->  ("
                        +((double) Math.round(1000000
                                  *gridTwo[0][i]) /1000000)+")");
    }
    System.out.println();

  }

/* Here's the output with sample file Gridded1D.txt:

iris 25% java visad.Gridded1DSet < Gridded1D.txt
num_dimensions = 1, num_coords = 20
 
Lengths = 20 wedge = 
 0
 1
 2
 3
. . .

Samples (20):
#0:     -40.548489
#1:     -39.462049
. . .
#18:    26.026153
#19:    38.301201
 
gridToValue test:
(-0.4)  -->  -40.983065
(0.5)   -->  -40.005269
. . .
(18.5)  -->  32.163677
(19.4)  -->  43.21122
 
valueToGrid test:
-40.983065      -->  (-0.4)
-40.005269      -->  (0.5)
. . .
32.163677       -->  (18.5)
43.21122        -->  (19.4)
 
iris 26% 

*/

}

