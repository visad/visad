
//
// ProductSet.java
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

/**
   ProductSet is the cross-product of an array of SampledSets.<P>
*/
public class ProductSet extends SampledSet {

  public SampledSet[] Sets;

  /** construct a ProductSet with an array of SampledSets */
  public ProductSet(MathType type, SampledSet[] sets) throws VisADException {
    this(type, sets, null, null, null, true);
  }

  /** construct a ProductSet with an array of SampledSets and
      non-default CoordinateSystem */
  public ProductSet(MathType type, SampledSet[] sets,
                    CoordinateSystem coord_sys, Unit[] units,
                    ErrorEstimate[] errors) throws VisADException {
    this(type, sets, coord_sys, units, errors, true);
  }

  ProductSet(MathType type, SampledSet[] sets, CoordinateSystem coord_sys,
             Unit[] units, ErrorEstimate[] errors, boolean copy)
             throws VisADException {
    super(type, find_manifold_dim(sets), coord_sys, units, errors);
    int dim = 0;
    for (int i=0; i<sets.length; i++) {
      dim += sets[i].DomainDimension;
    }
    if (dim != DomainDimension) {
      throw new SetException("ProductSet: DomainDimension does not match");
    }
    if (copy) {
      Sets = new SampledSet[sets.length];
      for (int i=0; i<sets.length; i++) {
      	Sets[i] = (SampledSet) sets[i].clone();
      }
    }
    else Sets = sets;
    Length = 1;
    for (int i=0; i<sets.length; i++) {
      Length *= sets[i].Length;
    }
  }

  private static int find_manifold_dim(SampledSet[] sets) {
    int dim = 0;
    for (int i=0; i<sets.length; i++) {
      dim += sets[i].getManifoldDimension();
    }
    return dim;
  }

  /** convert an array of 1-D indices to an
      array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    int nsets = Sets.length;
    int npts = index.length;
    int[][] indices = new int[nsets][npts];
    float[][] value = new float[DomainDimension][];
    for (int j=0; j<npts; j++) {
      int num = index[j];
      for (int i=0; i<nsets; i++) {
        indices[i][j] = num % Sets[i].Length;
        num /= Sets[i].Length;
      }
    }
    int curdim = 0;
    for (int i=0; i<nsets; i++) {
      float[][] temp_vals = Sets[i].indexToValue(indices[i]);
      for (int k=0; k<temp_vals.length; k++) {
        value[curdim++] = temp_vals[k];
      }
    }
    return value;
  }

  /** convert an array of values in R^DomainDimension
      to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    int nsets = Sets.length;
    int npts = value[0].length;
    int[] index = new int[npts];
    float[][][] vals = new float[nsets][][];
    int curdim = 0;
    int[][] temp_inds = new int[nsets][];
    for (int i=0; i<nsets; i++) {
      vals[i] = new float[Sets[i].DomainDimension][];
      for (int k=0; k<Sets[i].DomainDimension; k++) {
        vals[i][k] = value[curdim++];
      }
      temp_inds[i] = Sets[i].valueToIndex(vals[i]);
    }
    for (int j=0; j<npts; j++) {
      int ind_j = 0;
      int num = 1;
      for (int i=0; i<nsets; i++) {
        ind_j += temp_inds[i][j]*num;
        num *= Sets[i].Length;
      }
      index[j] = ind_j;
    }
    return index;
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void valueToInterp(float[][] value, int[][] indices,
                            float[][] weights) throws VisADException {
    int nsets = Sets.length;
    int npts = value[0].length;
    float[][][] vals = new float[nsets][][];
    int curdim = 0;
    int[][][] temp_inds = new int[nsets][npts][];
    float[][][] temp_wgts = new float[nsets][npts][];

    // get valueToInterp results of individual sets
    for (int i=0; i<nsets; i++) {
      vals[i] = new float[Sets[i].DomainDimension][];
      for (int k=0; k<Sets[i].DomainDimension; k++) {
        vals[i][k] = value[curdim++];
      }
      Sets[i].valueToInterp(vals[i], temp_inds[i], temp_wgts[i]);
    }

    // merge valueToInterp results into indices and weights arrays
    for (int j=0; j<npts; j++) {
      int[] ptr = new int[nsets];
      int num_inds = 1;
      for (int i=0; i<nsets; i++) {
        ptr[i] = 0;
        num_inds *= temp_inds[i][j].length;
      }
      indices[j] = new int[num_inds];
      weights[j] = new float[num_inds];

      // take the entire cross-product of returned indices
      int ind_num = 0;
      while (ptr[0] < temp_inds[0][j].length) {

        // calculate index and weight values of current set values
        int ind_j = 0;
        float wgt_j = 1;
        int num = 1;
        for (int i=0; i<nsets; i++) {
          ind_j += temp_inds[i][j][ptr[i]]*num;
          wgt_j *= temp_wgts[i][j][ptr[i]];
          num *= Sets[i].Length;
        }
        indices[j][ind_num] = ind_j;
        weights[j][ind_num] = wgt_j;
        ind_num++;

        // advance cross-product pointers
        ptr[nsets-1]++;
        for (int i=nsets-2; i>=0; i--) {
          if (ptr[i+1] >= temp_inds[i+1][j].length) {
            ptr[i+1] = 0;
            ptr[i]++;
          }
          else i = 0;
        }
      }
    }
  }

  public Object clone() {
    try {
      return new ProductSet(Type, Sets, DomainCoordinateSystem,
                            SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("ProductSet.clone: "+e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new ProductSet(type, Sets, DomainCoordinateSystem,
                          SetUnits, SetErrors);
  }

  public boolean equals(Object set) {
    if (!(set instanceof ProductSet) || set == null) return false;
    if (this == set) return true;

    ProductSet pset = (ProductSet) set;
    if (pset.DomainDimension != DomainDimension
     || pset.ManifoldDimension != ManifoldDimension) return false;
    for (int i=0; i<Sets.length; i++) {
      if (!Sets[i].equals(pset.Sets[i])) return false;
    }
    return true;
  }

  public boolean isMissing() {
    for (int i=0; i<Sets.length; i++) {
      if (Sets[i].isMissing()) return true;
    }
    return false;
  }

  /* run 'java visad.ProductSet' to test the ProductSet class */
  public static void main(String[] argv) throws VisADException {
    RealType vis_xcoord = new RealType("x", null, null);
    RealType vis_ycoord = new RealType("y", null, null);

    // create Gridded2DSet
    RealType[] vis_arrayG = {vis_xcoord, vis_ycoord};
    RealTupleType vis_tupleG = new RealTupleType(vis_arrayG);
    float[][] sampG = { { 12.5f,  26.5f, 29.74f, 36.78f,
                         52.12f,  67.8f,  87.8f,  97.2f},
                        { 34.2f,  36.2f,  37.2f,  32.6f,
                         70.87f, 73.49f, 80.32f, 77.24f} };
    Gridded2DSet gSet = new Gridded2DSet(vis_tupleG, sampG, 4, 2);

    // create Irregular1DSet
    RealType[] vis_arrayI = {vis_xcoord};
    RealTupleType vis_tupleI = new RealTupleType(vis_arrayI);
    float[][] sampI = { {-874f, 345f, -102f, 902f, -769f, 96f} };
    Irregular1DSet iSet = new Irregular1DSet(vis_tupleI, sampI);

    // create ProductSet as cross-product of gSet and iSet
    RealType[] vis_arrayP = {vis_xcoord, vis_ycoord, vis_xcoord};
    RealTupleType vis_tupleP = new RealTupleType(vis_arrayP);
    SampledSet[] sets = {gSet, iSet};
    ProductSet pSet = new ProductSet(vis_tupleP, sets);

    // run some tests
    System.out.println("ProductSet created.");
    System.out.println("ManifoldDimension = "+pSet.getManifoldDimension());
    System.out.println("-----------------");
    System.out.println("indexToValue test:");
    int[] index1 = {0, 3, 6, 9, 12, 15, 18, 21};
    float[][] value1 = pSet.indexToValue(index1);
    for (int i=0; i<index1.length; i++) {
      System.out.print("index "+index1[i]+" \t==> ("+value1[0][i]);
      for (int j=1; j<value1.length; j++) {
        System.out.print(", "+value1[j][i]);
      }
      System.out.println(")");
    }

    System.out.println("-----------------");
    System.out.println("valueToIndex test:");
    float[][] value2 = { {  10f,   40f,   90f,   25f,
                            50f,  100f,   30f,   70f},
                         {  35f,   30f,   80f,   35f,
                            70f,   75f,   36f,   75f},
                         {-880f, -890f, -870f,  350f,
                           340f,  360f, -100f, -110f} };
    int[] index2 = pSet.valueToIndex(value2);
    for (int i=0; i<index2.length; i++) {
      System.out.print("("+value2[0][i]);
      for (int j=1; j<value2.length; j++) {
        System.out.print(", "+value2[j][i]);
      }
      System.out.println(")\t==> index "+index2[i]);
    }

    System.out.println("------------------");
    System.out.println("valueToInterp test:");
    float[][] value3 = { {  15f,   50f,   80f,   25f,
                            50f,  100f,   30f,   70f},
                         {  45f,   40f,   70f,   35f,
                            70f,   75f,   36f,   65f},
                         {-800f, -750f, -810f,  300f,
                           245f,  200f, -150f, -120f} };
    int[][] indexI = new int[value3[0].length][];
    float[][] weightI = new float[value3[0].length][];
    pSet.valueToInterp(value3, indexI, weightI);
    for (int l=0; l<value3[0].length; l++) {
      System.out.print("("+value3[0][l]);
      for (int k=1; k<value3.length; k++) {
        System.out.print(", "+value3[k][l]);
      }
      System.out.print(")\t==> indices ["+indexI[l][0]);
      for (int k=1; k<indexI[l].length; k++) {
        System.out.print(", "+indexI[l][k]);
      }
      System.out.print("], ");
      System.out.print(" weight total = ");
      float wtotal = 0;
      for (int k=0; k<weightI[l].length; k++) wtotal += weightI[l][k];
      System.out.println(wtotal);
    }

    System.out.println();
  }

/* Here's the output:

iris 21% java visad.ProductSet
ProductSet created.
ManifoldDimension = 3
-----------------
indexToValue test:
index 0         ==> (12.5, 34.2, -874.0)
index 3         ==> (36.78, 32.6, -874.0)
index 6         ==> (87.8, 80.32, -874.0)
index 9         ==> (26.5, 36.2, 345.0)
index 12        ==> (52.12, 70.87, 345.0)
index 15        ==> (97.2, 77.24, 345.0)
index 18        ==> (29.74, 37.2, -102.0)
index 21        ==> (67.8, 73.49, -102.0)
-----------------
valueToIndex test:
(10.0, 35.0, -880.0)    ==> index 0
(40.0, 30.0, -890.0)    ==> index 3
(90.0, 80.0, -870.0)    ==> index 6
(25.0, 35.0, 350.0)     ==> index 9
(50.0, 70.0, 340.0)     ==> index 12
(100.0, 75.0, 360.0)    ==> index 15
(30.0, 36.0, -100.0)    ==> index 18
(70.0, 75.0, -110.0)    ==> index 21
------------------
valueToInterp test:
(15.0, 45.0, -800.0)    ==> indices [32, 0, 36, 4],  weight total = 1.0
(50.0, 40.0, -750.0)    ==> indices [35, 19, 39, 23],  weight total = 1.0
(80.0, 70.0, -810.0)    ==> indices [38, 6, 39, 7, 34, 2, 35, 3],  weight total = 1.0
(25.0, 35.0, 300.0)     ==> indices [9, 41, 8, 40],  weight total = 1.0
(50.0, 70.0, 245.0)     ==> indices [12, 44, 8, 40],  weight total = 1.0
(100.0, 75.0, 200.0)    ==> indices [47, 15, 43, 11],  weight total = 1.0
(30.0, 36.0, -150.0)    ==> indices [18, 34, 19, 35],  weight total = 1.0
(70.0, 65.0, -120.0)    ==> indices [22, 38, 23, 39, 18, 34, 19, 35],  weight total = 1.0

iris 22% 

*/

}

