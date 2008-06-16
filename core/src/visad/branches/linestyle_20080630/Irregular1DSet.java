//
// Irregular1DSet.java
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

/**
   Irregular1DSet represents a finite set of samples of R.
   Note that Irregular1DSet reorders samples given in the
   constructor;  to convert between values and indices,
   always use the methods valueToIndex and indexToValue.<P>
*/
public class Irregular1DSet extends IrregularSet {

  float LowX, HiX;

  Gridded1DSet SortedSet;

  /** a 1-D irregular set with null errors, CoordinateSystem
      and Units are defaults from type */
  public Irregular1DSet(MathType type, float[][] samples)
         throws VisADException {
    this(type, samples, null, null, null, true);
  }

  /** a 1-D irregular set; samples array is organized
      float[1][number_of_samples]; samples need not be
      sorted - the constructor sorts samples to define
      a 1-D "triangulation";
      coordinate_system and units must be compatible with
      defaults for type, or may be null; errors may be null */
  public Irregular1DSet(MathType type, float[][] samples,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, coord_sys, units, errors, true);
  }

  public Irregular1DSet(MathType type, float[][] samples,
                 CoordinateSystem coord_sys, Unit[] units,
                 ErrorEstimate[] errors, boolean copy)
         throws VisADException {
    super(type, samples, samples.length, coord_sys,
          units, errors, null, copy);
    LowX = Low[0];
    HiX = Hi[0];

    // sort samples so that creation of a Gridded1DSet is possible
    float[][] sortedSamples = new float[1][Length];
    for (int i=0; i<Length; i++) {
      sortedSamples[0][i] = Samples[0][i];
    }
    newToOld = QuickSort.sort(sortedSamples[0]);
    oldToNew = new int[Length];
    for (int i=0; i<Length; i++) oldToNew[newToOld[i]] = i;
    SortedSet = new Gridded1DSet(type, sortedSamples, Length,
                                 coord_sys, units, errors, false);
  }

  public Set makeSpatial(SetType type, float[][] samples) throws VisADException {
    if (samples.length == 3) {
      return new Irregular3DSet(type, samples, newToOld, oldToNew,
                                null, null, null, false);
    }
    else if (samples.length == 2) {
      return new Irregular2DSet(type, samples, newToOld, oldToNew,
                                null, null, null, false);
    }
    else if (samples.length == 1) {
      // may need new sort (CoordinateSystem may change order)
      return new Irregular1DSet(type, samples, null, null, null, false);
    }
    else {
      throw new SetException("Irregular1DSet.makeSpatial: bad samples length");
    }
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    int[] newIndex = new int[index.length];
    for (int i=0; i<index.length; i++) {
      newIndex[i] =
          (0 <= index[i] && index[i] < Length) ? oldToNew[index[i]] : -1;
    }
    float[][] value = SortedSet.indexToValue(newIndex);
    return value;
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    int[] index = SortedSet.valueToIndex(value);
    int[] newIndex = new int[index.length];
    for (int i=0; i<index.length; i++) {
      newIndex[i] = (index[i] == -1) ? -1 : newToOld[index[i]];
    }
    return newIndex;
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if no interpolation is possible */
  public void valueToInterp(float[][] value, int[][] indices,
                            float[][] weights) throws VisADException {
    SortedSet.valueToInterp(value, indices, weights);
    for (int j=0; j<indices.length; j++) {
      if (indices[j] != null) {
        int[] newIndex = new int[indices[j].length];
        for (int i=0; i<indices[j].length; i++) newIndex[i] = newToOld[indices[j][i]];
        indices[j] = newIndex;
      }
    }
  }

  public float getLowX() {
    return LowX;
  }

  public float getHiX() {
    return HiX;
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Irregular1DSet(type, Samples, DomainCoordinateSystem,
                             SetUnits, SetErrors);
  }

  /* run 'java visad.Irregular1DSet' to test the Irregular1DSet class */
  public static void main(String[] argv) throws VisADException {
    // set up samples
    float[][] samp = { {130, 55, 37, 28, 61, 40, 104, 52, 65, 12} };
    int length = samp[0].length;
    int[] index = new int[length];
    for (int i=0; i<length; i++) {
      index[i] = i;
    }
    // instantiate Irregular1DSet
    RealType test1 = RealType.getRealType("x");
    RealType[] t_array = {test1};
    RealTupleType t_tuple = new RealTupleType(t_array);
    Irregular1DSet iSet1D = new Irregular1DSet(t_tuple, samp);

    // print out samples and test indexToValue
    System.out.println("Samples (indexToValue test):");
    float[][] value = iSet1D.indexToValue(index);
    for (int i=0; i<iSet1D.Length; i++) {
      System.out.println("#"+i+":\t"+value[0][i]);
    }

    for (int i=0; i<iSet1D.Length; i++) {
      System.out.println("#"+i+":\t"+iSet1D.oldToNew[i]+" "+iSet1D.newToOld[i]);
    }

    // test valueToIndex
    System.out.println("\nvalueToIndex test:");
    float[][] value2 = { {10, 20,  30,  40,  50,  60, 70,
                           80, 90, 100, 110, 120, 130} };
    int[] index2 = iSet1D.valueToIndex(value2);
    for (int i=0; i<index2.length; i++) {
      System.out.println(value2[0][i]+"\t--> "+index2[i]);
    }

    // test valueToInterp
    System.out.println("\nvalueToInterp test:");
    int[][] indices = new int[value2[0].length][];
    float[][] weights = new float[value2[0].length][];
    iSet1D.valueToInterp(value2, indices, weights);
    for (int i=0; i<value2[0].length; i++) {
      System.out.print(value2[0][i]+"\t--> ["+indices[i][0]);
      for (int j=1; j<indices[i].length; j++) {
        System.out.print(", "+indices[i][j]);
      }
      System.out.print("]\tweight total: ");
      float total = 0;
      for (int j=0; j<weights[i].length; j++) {
        total += weights[i][j];
      }
      System.out.println(total);
    }
  }

/* Here's the output:

iris 56% java visad.Irregular1DSet
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

