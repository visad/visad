
//
// Irregular1DSet.java
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

/**
   Irregular1DSet represents a finite set of samples of R.
   Note that Irregular1DSet reorders samples given in the
   constructor;  to convert between values and indices,
   always use the methods valueToIndex and indexToValue.<P>
*/
public class Irregular1DSet extends IrregularSet {

  double LowX, HiX;
  Gridded1DSet SortedSet;

  public Irregular1DSet(MathType type, double[][] samples)
         throws VisADException {
    this(type, samples, null, null, null);
  }

  public Irregular1DSet(MathType type, double[][] samples,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    super(type, samples, coord_sys, units, errors);
    LowX = Low[0];
    HiX = Hi[0];

    // sort samples so that creation of a Gridded1DSet is possible
    double[][] sortedSamples = new double[1][Length];
    for (int i=0; i<Length; i++) {
      sortedSamples[0][i] = Samples[0][i];
    }
    QuickSort.sort(sortedSamples[0]);
    SortedSet = new Gridded1DSet(type, sortedSamples, Length,
                                 coord_sys, units, errors);
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public double[][] indexToValue(int[] index) throws VisADException {
    double[][] value = SortedSet.indexToValue(index);
    return value;
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(double[][] value) throws VisADException {
    int[] index = SortedSet.valueToIndex(value);
    return index;
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if no interpolation is possible */
  public void valueToInterp(double[][] value, int[][] indices,
                            double[][] weights) throws VisADException {
    SortedSet.valueToInterp(value, indices, weights);
  }

  public double getLowX() {
    return LowX;
  }

  public double getHiX() {
    return HiX;
  }

  public Object clone() {
    try {
      return new Irregular1DSet(Type, Samples, DomainCoordinateSystem,
                              SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Irregular1DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Irregular1DSet(type, Samples, DomainCoordinateSystem,
                             SetUnits, SetErrors);
  }

  /* run 'java visad.Irregular1DSet' to test the Irregular1DSet class */
  public static void main(String[] argv) throws VisADException {
    // set up samples
    double[][] samp = { {130, 55, 37, 28, 61, 40, 104, 52, 65, 12} };
    int length = samp[0].length;
    int[] index = new int[length];
    for (int i=0; i<length; i++) {
      index[i] = i;
    }
    // instantiate Irregular1DSet
    RealType test1 = new RealType("x", null, null);
    RealType[] t_array = {test1};
    RealTupleType t_tuple = new RealTupleType(t_array);
    Irregular1DSet iSet1D = new Irregular1DSet(t_tuple, samp);

    // print out samples and test indexToValue
    System.out.println("Samples (indexToValue test):");
    double[][] value = iSet1D.indexToValue(index);
    for (int i=0; i<iSet1D.Length; i++) {
      System.out.println("#"+i+":\t"+value[0][i]);
    }

    // test valueToIndex
    System.out.println("\nvalueToIndex test:");
    double[][] value2 = { {10, 20,  30,  40,  50,  60, 70,
                           80, 90, 100, 110, 120, 130} };
    int[] index2 = iSet1D.valueToIndex(value2);
    for (int i=0; i<index2.length; i++) {
      System.out.println(value2[0][i]+"\t--> "+index2[i]);
    }

    // test valueToInterp
    System.out.println("\nvalueToInterp test:");
    int[][] indices = new int[value2[0].length][];
    double[][] weights = new double[value2[0].length][];
    iSet1D.valueToInterp(value2, indices, weights);
    for (int i=0; i<value2[0].length; i++) {
      System.out.print(value2[0][i]+"\t--> ["+indices[i][0]);
      for (int j=0; j<indices[i].length; j++) {
        System.out.print(", "+indices[i][j]);
      }
      System.out.print("]\tweight total: ");
      double total = 0;
      for (int j=0; j<weights[i].length; j++) {
        total += weights[i][j];
      }
      System.out.println(total);
    }
  }

/* Here's the output:

iris 56% java Irregular1DSet
Samples (indexToValue test):
#0:     12.0
#1:     28.0
#2:     37.0
#3:     40.0
#4:     52.0
#5:     55.0
#6:     61.0
#7:     65.0
#8:     104.0
#9:     130.0

valueToIndex test:
10.0    --> 0
20.0    --> 1
30.0    --> 1
40.0    --> 3
50.0    --> 4
60.0    --> 6
70.0    --> 7
80.0    --> 7
90.0    --> 8
100.0   --> 8
110.0   --> 8
120.0   --> 9
130.0   --> 9

valueToInterp test:
10.0    --> [0, 0]      weight total: 1.0
20.0    --> [1, 1, 0]   weight total: 1.0
30.0    --> [1, 1, 2]   weight total: 1.0
40.0    --> [3, 3, 4]   weight total: 1.0
50.0    --> [4, 4, 3]   weight total: 1.0
60.0    --> [6, 6, 5]   weight total: 1.0
70.0    --> [7, 7, 8]   weight total: 1.0
80.0    --> [7, 7, 8]   weight total: 1.0
90.0    --> [8, 8, 7]   weight total: 1.0
100.0   --> [8, 8, 7]   weight total: 1.0
110.0   --> [8, 8, 9]   weight total: 1.0
120.0   --> [9, 9, 8]   weight total: 1.0
130.0   --> [9, 9]      weight total: 1.0
iris 57% 

*/

}

