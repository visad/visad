//
// ProductSet.java
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

//
// TO_DO
// getWedge
//

/**
   ProductSet is the cross-product of an array of SampledSets.<P>
*/
public class ProductSet extends SampledSet {

  SampledSet[] Sets;

  /** construct a ProductSet with an array of SampledSets */
  public ProductSet(SampledSet[] sets) throws VisADException {
    this(makeType(sets), sets, null, null, null, true);
  }

  /** create the product of the sets array, with null errors,
      CoordinateSystem and Units are defaults from type */
  public ProductSet(MathType type, SampledSet[] sets) throws VisADException {
    this(type, sets, null, null, null, true);
  }

  /** create the product of the sets array; coordinate_system
      and units must be compatible with defaults for type,
      or may be null; errors may be null */
  public ProductSet(MathType type, SampledSet[] sets,
                    CoordinateSystem coord_sys, Unit[] units,
                    ErrorEstimate[] errors) throws VisADException {
    this(type, sets, coord_sys, units, errors, true);
  }

  ProductSet(MathType type, SampledSet[] sets, CoordinateSystem coord_sys,
             Unit[] units, ErrorEstimate[] errors, boolean copy)
             throws VisADException {
    super(type, find_manifold_dim(sets, units), coord_sys, units, errors);
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
    Low = new float[DomainDimension];
    Hi = new float[DomainDimension];
    int base = 0;
    for (int i=0; i<sets.length; i++) {
      float[] low = sets[i].getLow();
      float[] hi = sets[i].getHi();
      int set_dim = sets[i].getDimension();
      for (int j=0; j<set_dim; j++) {
        Low[base + j] = low[j];
        Hi[base + j] = hi[j];
      }
      base += set_dim;
    }
  }

  /**
   * Returns the {@link SampledSet}s that constitute this instance.  The
   * returned array may be modified without affecting this instance.
   *
   * @return                         The {@link SampledSet}s that constitute
   *                                 this instance.
   */
  public SampledSet[] getSets() {
    return (SampledSet[])Sets.clone();  // return defensive copy
  }

  private static int find_manifold_dim(SampledSet[] sets, Unit[] units)
          throws VisADException {
    if (sets == null || sets[0] == null) {
      throw new SetException("ProductSet: Sets cannot be missing");
    }
    if (sets.length < 2) {
      throw new SetException("ProductSet: must be at least 2 sets");
    }
    int manifold_dim = 0;
    int dim = 0;
    for (int i=0; i<sets.length; i++) {
      if (units != null) {
        int n = sets[i].getDimension();
        if ((dim + n) > units.length) {
          throw new SetException("ProductSet: Sets exceed ManifoldDimension " +
                                 units.length);
        }
        Unit[] su = sets[i].getSetUnits();
        if (su == null) {
          throw new SetException("ProductSet: Set#" + i + " is null");
        }
        for (int j=0; j<n; j++) {
          if (units[dim+j] != null || su[j] != null) {
            if (units[dim+j] == null || su[j] == null ||
                !units[dim+j].equals(su[j])) {
              throw new SetException("ProductSet: Expected set " + i +
                                     ", element " + j + " units to be " +
                                     units[dim+j] + " not " + su[j]);
            }
          }
        }
      }
      dim += sets[i].getDimension();
      manifold_dim += sets[i].getManifoldDimension();
    }
    return manifold_dim;
  }

  static MathType makeType(SampledSet[] sets) throws VisADException {
    int n = sets.length;
    RealTupleType[] types = new RealTupleType[n];
    int count = 0;
    for (int i=0; i<n; i++) {
      types[i] = ((SetType) sets[i].getType()).getDomain();
      count += types[i].getDimension();
    }
    RealType[] reals = new RealType[count];
    int k=0;
    for (int i=0; i<n; i++) {
      for (int j=0; j<types[i].getDimension(); j++) {
        reals[k++] = (RealType) types[i].getComponent(j);
      }
    }
    return new SetType(new RealTupleType(reals));
  }

  public SampledSet product() throws VisADException {
    int n = Sets.length;
    SampledSet[] sets = new SampledSet[n];
    for (int i=0; i<n; i++) {
      if (Sets[i] instanceof GriddedSet ||
          Sets[i] instanceof IrregularSet) {
        sets[i] = Sets[i];
      }
      else if (Sets[i] instanceof ProductSet) {
        sets[i] = ((ProductSet) Sets[i]).product();
      }
      else if (Sets[i] instanceof UnionSet) {
        sets[i] = ((UnionSet) Sets[i]).product();
      }
      else {
        throw new UnimplementedException("ProductSet.product: " +
                                         Sets[i].getClass());
      }
    } // end for (int i=0; i<n; i++)
    SampledSet prod = sets[0];
    for (int i=1; i<n; i++) {
      if (prod instanceof ProductSet) {
        prod = ((ProductSet) prod).product(sets[i]);
      }
      else if (prod instanceof UnionSet) {
        prod = ((UnionSet) prod).product(sets[i]);
      }
      /* WLH 10 January 2001 - add missing "else" to following */
      else if (sets[i] instanceof ProductSet) {
        prod = ((ProductSet) sets[i]).inverseProduct(prod);
      }
      else if (sets[i] instanceof UnionSet) {
        prod = ((UnionSet) sets[i]).inverseProduct(prod);
      }
      else {
        prod = new ProductSet(new SampledSet[] {prod, sets[i]});
      }
    }
    return prod;
  }

  public SampledSet product(SampledSet set) throws VisADException {
    int n = Sets.length;
    if (set instanceof ProductSet) {
      int m = ((ProductSet) set).Sets.length;
      SampledSet[] sets = new SampledSet[n + m];
      for (int i=0; i<n; i++) {
        sets[i] = Sets[i];
      }
      for (int j=0; j<m; j++) {
        sets[n + j] = ((ProductSet) set).Sets[j];
      }
      return new ProductSet(sets);
    }
    else if (set instanceof UnionSet) {
      int m = ((UnionSet) set).Sets.length;
      SampledSet[] sets = new SampledSet[m];
      for (int j=0; j<m; j++) {
        sets[j] = product(((UnionSet) set).Sets[j]);
      }
      return new UnionSet(sets);
    }
    else {
      SampledSet[] sets = new SampledSet[n + 1];
      for (int i=0; i<n; i++) sets[i] = Sets[i];
      sets[n] = set;
      return new ProductSet(sets);
    }
  }

  public SampledSet inverseProduct(SampledSet set) throws VisADException {
    int n = Sets.length;
    if (set instanceof ProductSet) {
      int m = ((ProductSet) set).Sets.length;
      SampledSet[] sets = new SampledSet[n + m];
      for (int j=0; j<m; j++) {
        sets[j] = ((ProductSet) set).Sets[j];
      }
      for (int i=0; i<n; i++) {
        sets[m + i] = Sets[i];
      }
      return new ProductSet(sets);
    }
    else if (set instanceof UnionSet) {
      int m = ((UnionSet) set).Sets.length;
      SampledSet[] sets = new SampledSet[m];
      for (int j=0; j<m; j++) {
        sets[j] = inverseProduct(((UnionSet) set).Sets[j]);
      }
      return new UnionSet(sets);
    }
    else {
      SampledSet[] sets = new SampledSet[n + 1];
      sets[0] = set;
      for (int i=0; i<n; i++) {
        sets[i + 1] = Sets[i];
      }
      return new ProductSet(sets);
    }
  }

  /** this should return Gridded3DSet or Irregular3DSet;
      no need for make*DGeometry or makeIso* in this class */
  public Set makeSpatial(SetType type, float[][] samples) throws VisADException {
    int n = Sets.length;
    int dim = samples.length;
    if (dim != DomainDimension || dim != 3) {
      throw new SetException("ProductSet.makeSpatial: samples bad dimension");
    }
    try {
      //
      // TO_DO
      // make a Gridded3DSet if possible
      // otherwise be smart in making an Irregular3DSet
      // note: cannot re-order samples
      //
      boolean any_product_or_union = false;
      for (int i=0; i<n; i++) {
        if (Sets[i] instanceof ProductSet ||
            Sets[i] instanceof UnionSet) {
          any_product_or_union = true;
        }
      }
      if (any_product_or_union) {
        return product().makeSpatial(type, samples);
      }
      else {
        boolean all_gridded = true;
        int[] lengths = new int[ManifoldDimension];
        int k = 0;
        for (int i=0; i<n; i++) {
          if (!(Sets[i] instanceof GriddedSet)) {
            all_gridded = false;
            break;
          }
          int[] ls = ((GriddedSet) Sets[i]).getLengths();
          for (int j=0; j<ls.length; j++) {
            lengths[k++] = ls[j];
          }
        }
        if (all_gridded) {
          return GriddedSet.create(type, samples, lengths);
        }
        //
        // TO_DO
        // can assume that no factors are ProductSet or UnionSet
        //
      }
      throw new UnimplementedException("ProductSet.makeSpatial");
    }
    catch (VisADException e) {
      return new Irregular3DSet(type, samples, null,
                                null, null, null, false);
    }
  }

  /** copied from Set */
  public float[][] getSamples(boolean copy) throws VisADException {
    int n = getLength();
    int[] indices = new int[n];
    // do NOT call getWedge
    for (int i=0; i<n; i++) indices[i] = i;
    return indexToValue(indices);
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

  /**
   * Clones this instance.
   *
   * @return                        A clone of this instance.
   */
  public Object clone() {
    ProductSet clone = (ProductSet)super.clone();
    
    /*
     * The array of sampled sets is cloned because getSamples(false) allows
     * clients to modify the values and the clone() general contract forbids
     * cross-clone effects.
     */
    if (Sets != null) {
        clone.Sets = new SampledSet[Sets.length];
        for (int i = 0; i < Sets.length; i++)
            clone.Sets[i] = (SampledSet)Sets[i].clone();
    }
    
    return clone;
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

  /**
   * Returns the hash code of this instance.
   * @param		The hash code of this instance.
   */
  public int hashCode() {
    if (!hashCodeSet)
    {
      for (int i=0; i<Sets.length; i++)
	hashCode ^= Sets[i].hashCode();
      hashCodeSet = true;
    }
    return hashCode;
  }

  public boolean isMissing() {
    for (int i=0; i<Sets.length; i++) {
      if (Sets[i].isMissing()) return true;
    }
    return false;
  }

  public String longString(String pre) throws VisADException {
    return pre + "ProductSet: Dimension = " + DomainDimension + "\n";
  }

  /* run 'java visad.ProductSet' to test the ProductSet class */
  public static void main(String[] argv) throws VisADException {
    RealType vis_xcoord = RealType.getRealType("x");
    RealType vis_ycoord = RealType.getRealType("y");

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

