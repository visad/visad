//
// UnionSet.java
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

//
// TO_DO
// getWedge
//

/**
   UnionSet is the union of an array of SampledSets.<P>
*/
public class UnionSet extends SampledSet {

  SampledSet[] Sets;

  /** create the union of the sets array, with null errors,
      CoordinateSystem and Units are defaults from type */
  public UnionSet(MathType type, SampledSet[] sets) throws VisADException {
    this(type, sets, null, null, null, true);
  }

  /** create the union of the sets array; coordinate_system
      and units must be compatible with defaults for type,
      or may be null; errors may be null */
  public UnionSet(MathType type, SampledSet[] sets,
                  CoordinateSystem coord_sys, Unit[] units, 
                  ErrorEstimate[] errors) throws VisADException {
    this(type, sets, coord_sys, units, errors, true);
  }

  UnionSet(MathType type, SampledSet[] sets, CoordinateSystem coord_sys,
             Unit[] units, ErrorEstimate[] errors, boolean copy)
             throws VisADException {
    super(type, find_manifold_dim(sets), coord_sys, units, errors);
    DomainDimension = sets[0].DomainDimension;
    if (copy) {
      Sets = new SampledSet[sets.length];
      for (int i=0; i<sets.length; i++) {
      	Sets[i] = (SampledSet) sets[i].clone();
      }
    }
    else Sets = sets;
    Length = 0;
    for (int i=0; i<sets.length; i++) {
      Length += Sets[i].Length;
    }
    Low = new float[DomainDimension];
    Hi = new float[DomainDimension];
    for (int i=0; i<DomainDimension; i++) {
      Low[i] = Sets[0].Low[i];
      Hi[i] = Sets[0].Hi[i];
      for (int j=1; j<sets.length; j++) {
        if (sets[j].Low[i] < Low[i]) Low[i] = sets[j].Low[i];
        if (sets[j].Hi[i] > Hi[i]) Hi[i] = sets[j].Hi[i];
      }
    }
  }

  private static int find_manifold_dim(SampledSet[] sets)
                                        throws VisADException {
    if (sets == null || sets[0] == null) {
      throw new SetException("UnionSet: Sets cannot be missing");
    }
    if (sets.length < 2) {
      throw new SetException("UnionSet: must be at least 2 sets");
    }
    int dim = sets[0].DomainDimension;
    int mdim = sets[0].ManifoldDimension;
    for (int i=1; i<sets.length; i++) {
      if (sets[i] == null) {
        throw new SetException("UnionSet: Sets cannot be missing");
      }
      if (sets[i].DomainDimension != dim
       || sets[i].ManifoldDimension != mdim) {
        throw new SetException("UnionSet: dimensions do not match!");
      }
    }
    return mdim;
  }

  /** construct a UnionSet with an array of SampledSets */
  public UnionSet(SampledSet[] sets) throws VisADException {
    this(sets[0].getType(), sets, null, null, null, true);
  }

  /** return a SampledSet that is a UnionSet of ProductSets of
      GriddedSets and IrregularSets */
  public SampledSet product() throws VisADException {
    int n = Sets.length;
    SampledSet[] sets = new SampledSet[n];
    int count = 0;
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
      if (sets[i] instanceof UnionSet) {
        count += ((UnionSet) sets[i]).Sets.length;
      }
      else {
        count++;
      }
    } // end for (int i=0; i<n; i++)
    SampledSet[] summands = new SampledSet[count];
    int k = 0;
    for (int i=0; i<n; i++) {
      if (sets[i] instanceof UnionSet) {
        for (int j=0; j<((UnionSet) sets[i]).Sets.length; j++) {
          summands[k++] = ((UnionSet) sets[i]).Sets[j];
        }
      }
      else {
        summands[k++] = sets[i];
      }
    }
    return new UnionSet(getType(), summands);
  }

  public SampledSet product(SampledSet set) throws VisADException {
    int n = Sets.length;
    SampledSet[] sets = new SampledSet[n];
    boolean union_of_union = false;
    for (int i=0; i<n; i++) {
      if (Sets[i] instanceof ProductSet) {
        sets[i] = ((ProductSet) Sets[i]).product(set);
      }
      else {
        if (set instanceof ProductSet) {
          sets[i] = ((ProductSet) set).inverseProduct(Sets[i]);
        }
        else if (set instanceof UnionSet) {
          sets[i] = ((UnionSet) set).inverseProduct(Sets[i]);
          union_of_union = true;
        }
        else {
          sets[i] = new ProductSet(new SampledSet[] {Sets[i], set});
        }
      }
    }
    SampledSet union = new UnionSet(sets);
    if (union_of_union) {
      union = ((UnionSet) union).product();
    }
    return union;
  }

  public SampledSet inverseProduct(SampledSet set) throws VisADException {
    int n = Sets.length;
    SampledSet[] sets = new SampledSet[n];
    boolean union_of_union = false;
    for (int i=0; i<n; i++) {
      if (Sets[i] instanceof ProductSet) {
        sets[i] = ((ProductSet) Sets[i]).inverseProduct(set);
      }
      else {
        if (set instanceof ProductSet) {
          sets[i] = ((ProductSet) set).product(Sets[i]);
        }
        else if (set instanceof UnionSet) {
          sets[i] = ((UnionSet) set).product(Sets[i]);
          union_of_union = true;
        }
        else {
          sets[i] = new ProductSet(new SampledSet[] {set, Sets[i]});
        }
      }
    }
    SampledSet union = new UnionSet(sets);
    if (union_of_union) {
      union = ((UnionSet) union).product();
    }
    return union;
  }

  public Set makeSpatial(SetType type, float[][] samples) throws VisADException {
    int n = Sets.length;
    int dim = samples.length;
/* WLH 28 Aug 98
    if (dim != DomainDimension) {
      throw new SetException("UnionSet.makeSpatial: samples bad dimension");
    }
*/
    SampledSet[] sets = new SampledSet[n];
    int base = 0;
    for (int i=0; i<n; i++) {
      int len = Sets[i].Length;
      float[][] s = new float[dim][len];
      for (int j=0; j<dim; j++) {
        for (int k=0; k<len; k++) s[j][k] = samples[j][base + k];
      }
      sets[i] = (SampledSet) Sets[i].makeSpatial(type, s);
      base += len;
    }
    return new UnionSet((SetType) sets[0].getType(), sets);
  }

  /** create a 2-D GeometryArray from this Set and color_values */
  public VisADGeometryArray make2DGeometry(byte[][] color_values)
         throws VisADException {
    if (DomainDimension != 3) {
      throw new SetException("UnionSet.make2DGeometry: " +
                              "DomainDimension must be 3");
    }
    if (ManifoldDimension != 2) {
      throw new SetException("UnionSet.make2DGeometry: " +
                              "ManifoldDimension must be 2");
    }
    int n = Sets.length;
    int dim = DomainDimension;
    if (color_values != null && dim != color_values.length) {
      throw new SetException("UnionSet.make2DGeometry: color_values bad dimension");
    }
    VisADIndexedTriangleStripArray[] arrays =
      new VisADIndexedTriangleStripArray[n];
    int base = 0;
    for (int i=0; i<n; i++) {
      int len = Sets[i].Length;
      byte[][] c = null;
      if (color_values != null) {
        c = new byte[dim][len];
        for (int j=0; j<dim; j++) {
          for (int k=0; k<len; k++) c[j][k] = color_values[j][base + k];
        }
      }
      VisADGeometryArray array = Sets[i].make2DGeometry(c);
      if (array instanceof VisADIndexedTriangleStripArray) {
        arrays[i] = (VisADIndexedTriangleStripArray) array;
      }
      else {
        arrays[i] = null;
      }
      base += len;
    }
    return VisADIndexedTriangleStripArray.merge(arrays);
  }

  /** create a 1-D GeometryArray from this Set and color_values */
  public VisADGeometryArray make1DGeometry(byte[][] color_values)
         throws VisADException {
    if (DomainDimension != 3) {
      throw new SetException("UnionSet.make1DGeometry: " +
                              "DomainDimension must be 3");
    }
    if (ManifoldDimension != 1) {
      throw new SetException("UnionSet.make1DGeometry: " +
                              "ManifoldDimension must be 1");
    }
    int n = Sets.length;
    int dim = DomainDimension;
    if (color_values != null && dim != color_values.length) {
      throw new SetException("UnionSet.make1DGeometry: color_values bad dimension");
    }
    VisADLineStripArray[] arrays =
      new VisADLineStripArray[n];
    int base = 0;
    for (int i=0; i<n; i++) {
      int len = Sets[i].Length;
      byte[][] c = null;
      if (color_values != null) {
        c = new byte[dim][len];
        for (int j=0; j<dim; j++) {
          for (int k=0; k<len; k++) c[j][k] = color_values[j][base + k];
        }
      }
      VisADGeometryArray array = Sets[i].make1DGeometry(c);
      if (array instanceof VisADLineStripArray) {
        arrays[i] = (VisADLineStripArray) array;
      }
      else {
        arrays[i] = null;
      }
      base += len;
    }
    return VisADLineStripArray.merge(arrays);
  }

  /** return basic lines in array[0], fill-ins in array[1]
      and labels in array[2] */
  public VisADGeometryArray[] makeIsoLines(float interval, float low,
                      float hi, float base, float[] fieldValues,
                      byte[][] color_values, boolean[] swap)
         throws VisADException {
    if (DomainDimension != 3) {
      throw new DisplayException("UnionSet.makeIsoLines: " +
                                 "DomainDimension must be 3");
    }
    if (ManifoldDimension != 2) {
      throw new DisplayException("UnionSet.makeIsoLines: " +
                                 "ManifoldDimension must be 2");
    }
    int n = Sets.length;
    int dim = color_values.length;
    if (dim != DomainDimension) {
      throw new DisplayException("UnionSet.makeIsoLines: " +
                                 "color_values bad dimension");
    }
    VisADLineArray[][] arrays = new VisADLineArray[n][];
    int kbase = 0;
    for (int i=0; i<n; i++) {
      int len = Sets[i].Length;
      byte[][] c = new byte[dim][len];
      float[] f = new float[len];
      for (int j=0; j<dim; j++) {
        for (int k=0; k<len; k++) c[j][k] = color_values[j][kbase + k];
      }
      for (int k=0; k<len; k++) f[k] = fieldValues[kbase + k];
      arrays[i] =
        (VisADLineArray[]) Sets[i].makeIsoLines(interval, low, hi, base,
                                                f, c, swap);
      kbase += len;
    }
    VisADLineArray[] arrays2 = new VisADLineArray[3];
    for (int j=0; j<3; j++) {
      VisADLineArray[] arrays3 = new VisADLineArray[n];
      for (int i=0; i<n; i++) {
        arrays3[i] = arrays[i][j];
      }
      arrays2[j] = VisADLineArray.merge(arrays3);
    }
    return arrays2;
  }
 
  public VisADGeometryArray makeIsoSurface(float isolevel,
         float[] fieldValues, byte[][] color_values)
         throws VisADException {
    if (DomainDimension != 3) {
      throw new DisplayException("UnionSet.makeIsoSurface: " +
                                 "DomainDimension must be 3");
    }
    if (ManifoldDimension != 3) {
      throw new DisplayException("UnionSet.makeIsoSurface: " +
                                 "ManifoldDimension must be 3");
    }
    int n = Sets.length;
    int dim = color_values.length;
    if (dim != DomainDimension) {
      throw new DisplayException("UnionSet.makeIsoSurface: " +
                                 "color_values bad dimension");
    }
    VisADIndexedTriangleStripArray[] arrays =
      new VisADIndexedTriangleStripArray[n];
    int base = 0;
    for (int i=0; i<n; i++) {
      int len = Sets[i].Length;
      byte[][] c = new byte[dim][len];
      float[] f = new float[len];
      for (int j=0; j<dim; j++) {
        for (int k=0; k<len; k++) c[j][k] = color_values[j][base + k];
      }
      for (int k=0; k<len; k++) f[k] = fieldValues[base + k];
      arrays[i] =
        (VisADIndexedTriangleStripArray) Sets[i].makeIsoSurface(isolevel, f, c);
      base += len;
    }
    return VisADIndexedTriangleStripArray.merge(arrays);
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
    float[][] value = new float[DomainDimension][npts];
    int[][] subindex = new int[nsets][];

    // classify each index point into its proper subset
    int[] ind_lens = new int[nsets];
    int[] set_num = new int[npts];
    int[] new_ind = new int[npts];
    for (int j=0; j<npts; j++) {
      int q = 0;
      int curind = index[j];
/* WLH 28 Aug 98
      while (q < nsets && index[j] > Sets[q].Length) {
*/
      while (q < nsets && curind >= Sets[q].Length) {
        curind -= Sets[q++].Length;
/*
System.out.println("curind = " + curind +
                   " Sets[" + (q-1) + "].Length = " + Sets[q-1].Length);
*/
      }
      if (q == nsets) curind += Sets[--q].Length;
      ind_lens[q]++;
      set_num[j] = q;
      new_ind[j] = curind;
/*
System.out.println("index[" + j + "] = " + index[j] +
                   " ind_lens[" + q + "] = " + ind_lens[q] +
                   " set_num[" + j + "] = " + set_num[j] +
                   " new_ind[" + j + "] = " + new_ind[j]);
*/
    }

    // allocate subset space
    for (int i=0; i<nsets; i++) {
      subindex[i] = (ind_lens[i] > 0) ? new int[ind_lens[i]] : null;
      ind_lens[i] = 0;
    }

    // copy each index point into its proper subset
    for (int j=0; j<npts; j++) {
      subindex[set_num[j]][ind_lens[set_num[j]]++] = new_ind[j];
    }

    // call Sets indexToValue methods
    float[][][] subvals = new float[nsets][][];
    for (int i=0; i<nsets; i++) {
      if (subindex[i] != null) {
        subvals[i] = Sets[i].indexToValue(subindex[i]);
      }
    }

    // compile value array
    for (int i=0; i<nsets; i++) ind_lens[i] = 0;
    for (int j=0; j<npts; j++) {
      for (int k=0; k<DomainDimension; k++) {
/* WLH 28 Aug 98
        value[k][j] = subvals[set_num[j]][k][ind_lens[set_num[j]]++];
*/
        value[k][j] = subvals[set_num[j]][k][ind_lens[set_num[j]]];
/*
System.out.println("set_num[" + j + "] = " + set_num[j] +
                   " ind_lens[set_num[" + j + "]] = " + ind_lens[set_num[j]]);
*/
      }
      ind_lens[set_num[j]] += 1;
    }

    return value;
  }

  /** convert an array of values in R^DomainDimension
      to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    int nsets = Sets.length;
    int npts = value[0].length;
    int[] index = new int[npts];

    // get closest indices in each set
    int[][] subindex = new int[nsets][];
    for (int i=0; i<nsets; i++) {
      subindex[i] = Sets[i].valueToIndex(value);
    }

    // get actual values of returned indices
    float[][][] subvalue = new float[nsets][][];
    for (int i=0; i<nsets; i++) {
      subvalue[i] = Sets[i].indexToValue(subindex[i]);
    }

    // compute actual indices
    for (int j=0; j<npts; j++) {
      float[] dist_sqr = new float[nsets];
      int best_set = 0;
      for (int i=0; i<nsets; i++) {
        // compute distance between subvalue[i] and value[i]
        dist_sqr[i] = 0;
        for (int k=0; k<DomainDimension; k++) {
          if (subindex[i][j] == -1) {
            dist_sqr[i] = Float.MAX_VALUE;
            break;
          }
          else {
            float d = subvalue[i][k][j] - value[k][j];
            dist_sqr[i] += d*d;
          }
        }
        if (dist_sqr[best_set] > dist_sqr[i]) best_set = i;
      }
      int ind = subindex[best_set][j];
      while (best_set > 0) ind += Sets[--best_set].Length;
      index[j] = ind;
    }

    return index;
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void valueToInterp(float[][] value, int[][] indices,
                            float[][] weights) throws VisADException {
    throw new UnimplementedException("UnionSet.valueToInterp");
  }

  public Object clone() {
    try {
      return new UnionSet(Type, Sets, DomainCoordinateSystem,
                          SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("UnionSet.clone: "+e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new UnionSet(type, Sets, DomainCoordinateSystem,
                        SetUnits, SetErrors);
  }

  public boolean equals(Object set) {
    if (!(set instanceof UnionSet) || set == null) return false;
    if (this == set) return true;

    UnionSet uset = (UnionSet) set;
    if (uset.DomainDimension != DomainDimension
     || uset.ManifoldDimension != ManifoldDimension) return false;
    for (int i=0; i<Sets.length; i++) {
      if (!Sets[i].equals(uset.Sets[i])) return false;
    }
    return true;
  }

  public boolean isMissing() {
    for (int i=0; i<Sets.length; i++) {
      if (Sets[i].isMissing()) return true;
    }
    return false;
  }

  /* run 'java visad.UnionSet' to test the UnionSet class */
  public static void main(String[] argv) throws VisADException {
    RealType vis_xcoord = new RealType("1D", null, null);
    RealType[] vis_array = {vis_xcoord};
    RealTupleType vis_tuple = new RealTupleType(vis_array);

    // create Gridded1DSet
    float[][] sampG = { {2f, 4f, 6f, 8f, 10f, 12f, 14f, 16f} };
    Gridded1DSet gSet = new Gridded1DSet(vis_tuple, sampG, 8);

    // create Irregular1DSet
    float[][] sampI = { {100f, 90f, 110f, 80f, 120f, 70f, 130f, 60f} };
    Irregular1DSet iSet = new Irregular1DSet(vis_tuple, sampI);

    // create UnionSet as the union of gSet and iSet
    RealType[] vis_arrayP = {vis_xcoord, vis_xcoord};
    RealTupleType vis_tupleP = new RealTupleType(vis_arrayP);
    SampledSet[] sets = {gSet, iSet};
    UnionSet uSet = new UnionSet(vis_tupleP, sets);

    // run some tests
    System.out.println("UnionSet created.");
    System.out.println("ManifoldDimension = "+uSet.getManifoldDimension());
    System.out.println("-----------------");
    System.out.println("indexToValue test:");
    int[] index1 = {0, 3, 9, 12, 6, 15};
    float[][] value1 = uSet.indexToValue(index1);
    for (int i=0; i<index1.length; i++) {
      System.out.print("index "+index1[i]+" \t==> ("+value1[0][i]);
      for (int j=1; j<value1.length; j++) {
        System.out.print(", "+value1[j][i]);
      }
      System.out.println(")");
    }

    System.out.println("-----------------");
    System.out.println("valueToIndex test:");
    float[][] value2 = { {10f, 40f, 90f, 25f, 50f, 100f, 30f, 70f} };
    int[] index2 = uSet.valueToIndex(value2);
    for (int i=0; i<index2.length; i++) {
      System.out.print("("+value2[0][i]);
      for (int j=1; j<value2.length; j++) {
        System.out.print(", "+value2[j][i]);
      }
      System.out.println(")\t==> index "+index2[i]);
    }

    System.out.println();
  }

  public String longString(String pre) throws VisADException {
    return pre + "UnionSet: Dimension = " + DomainDimension + "\n";
  }

}

