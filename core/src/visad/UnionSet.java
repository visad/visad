//
// UnionSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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
 * UnionSet is the union of an array of SampledSets.  They
 * must match in domain dimension, manifold dimension,
 * CoordinateSystem, and Units.  No sets in the array can be null,
 * and there must be at least one SampledSet in the array
 * (i.e. array.length != 0).<P>
 */
public class UnionSet extends SampledSet {

  private static final long serialVersionUID = 1L;
  SampledSet[] Sets;

  /**
   * Construct a UnionSet with an array of SampledSets with null errors.
   * CoordinateSystem and Units are defaults from type.
   *
   * @param  type  MathType for the sets.  Sets the CoordinateSystem and Units.
   * @param  sets  array of SampledSets.  All sets must match in domain
   *               dimension and manifold dimension and no sets in the
   *               array can be null.  There must be at least one SampledSet
   *               in the array (i.e. sets.length != 0).
   *
   * @exception  VisADException  problem creating the UnionSet
   */
  public UnionSet(MathType type, SampledSet[] sets) throws VisADException {
    this(type, sets, null, null, null, true);
  }

  /** create the union of the sets array; coordinate_system
      and units must be compatible with defaults for type,
      or may be null; errors may be null */
  private UnionSet(MathType type, SampledSet[] sets,
                  CoordinateSystem coord_sys, Unit[] units,
                  ErrorEstimate[] errors) throws VisADException {
    this(type, sets, coord_sys, units, errors, true);
  }

  public UnionSet(MathType type, SampledSet[] sets, CoordinateSystem coord_sys,
             Unit[] units, ErrorEstimate[] errors, boolean copy)
             throws VisADException {
    super(type, find_manifold_dim(sets), sets[0].getCoordinateSystem(),
          sets[0].getSetUnits(), null);
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
    if (sets == null || sets.length == 0 || sets[0] == null) {
      throw new SetException("UnionSet: Sets cannot be missing");
    }
    if (sets.length < 2) {
    /* DRM - 03-Jan-2000
      throw new SetException("UnionSet: must be at least 2 sets");
    */
      return sets[0].ManifoldDimension;
    }
    int dim = sets[0].DomainDimension;
    int mdim = sets[0].ManifoldDimension;
    CoordinateSystem cs0 = sets[0].getCoordinateSystem();
    Unit[] units0 = sets[0].getSetUnits();
    for (int i=1; i<sets.length; i++) {
      if (sets[i] == null) {
        throw new SetException("UnionSet: Sets cannot be missing");
      }
      if (sets[i].DomainDimension != dim) {
        throw new SetException("UnionSet: set #" + i +
                               " Domain dimension is " +
                               sets[i].DomainDimension + ", not " + dim);
      }
      if (sets[i].ManifoldDimension != mdim) {
        throw new SetException("UnionSet: set #" + i +
                               " Manifold dimension is " +
                               sets[i].ManifoldDimension + ", not " + mdim);
      }
      CoordinateSystem cs = sets[i].getCoordinateSystem();
      if (cs0 != null || cs != null) {
        if (cs0 == null || cs == null || !cs0.equals(cs)) {
          throw new CoordinateSystemException("UnionSet: Coordinate system #" +
                                              i + " (" + (cs == null ? null :
                                                          cs.getReference()) +
                                              " must match #0 " +
                                              (cs0 == null ? null :
                                               cs0.getReference()));
        }
      }
      Unit[] units = sets[i].getSetUnits();
      if (units0 != null || units != null) {
        if (units0 == null || units == null ||
            units0.length != units.length) {
          throw new SetException("UnionSet: Expected " +
                                 (units0 == null ? "null" :
                                  Integer.toString(units0.length)) +
                                 " units for set " + i + ", not " +
                                 (units == null ? "null" :
                                  Integer.toString(units.length)));
        }
        for (int j=0; j<units0.length; j++) {
          if (units0[j] != null || units[j] != null) {
            if (units0[j] == null || units[j] == null ||
                !units0[j].equals(units[j])) {
              throw new SetException("UnionSet: Expected set " + i +
                                     ", element " + j + " units to be " +
                                     units0[j] + " not " + units[j]);
            }
          }
        }
      }
    }
    return mdim;
  }

  /**
   * Returns the {@link SampledSet}s that constitute this instance.  The
   * returned array may be modified without affecting the behavior of this
   * instance.
   *
   * @return                        The {@link SampledSet}s that constitute this
   *                                instance.
   */
  public SampledSet[] getSets() {
    return (SampledSet[]) Sets.clone();
    // return Sets; WLH 28 Nov 2000
  }

  /**
   * Construct a UnionSet with an array of SampledSets
   *
   * @param  sets  array of SampledSets.  All sets must match in domain
   *               dimension and manifold dimension, CoordinateSystem,
   *               and Units. and no sets in the array can be null.
   *               There must be at least one SampledSet
   *               in the array (i.e. sets.length != 0).
   *
   * @exception  VisADException  problem creating the UnionSet
   */
  public UnionSet(SampledSet[] sets) throws VisADException {
    this(sets[0].getType(), sets, null, null, null, true);
  }

  /**
   * Return a SampledSet that is a UnionSet of ProductSets of
   * GriddedSets and IrregularSets
   *
   * @return  resulting UnionSet of ProductSets
   * @exception  VisADException  problem creating the UnionSet
   */
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
        throw new UnimplementedException("UnionSet.product: " +
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

  /**
   * Create a UnionSet that is the cross product of this UnionSet and
   * the input SampledSet.
   *
   * @param  set   input SampledSet
   * @return a SampledSet that is a UnionSet of ProductSets of
   *           this UnionSet and the input SampledSet
   * @exception   VisADException  error creating necessary VisAD object
   */
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

  /**
   * Create a UnionSet that is the inverse cross product of this UnionSet and
   * the input SampledSet.
   *
   * @param  set   input SampledSet
   *
   * @return       a SampledSet that is a UnionSet of inverse ProductSets of
   *               this UnionSet and the input SampledSet
   * @exception    VisADException  error creating necessary VisAD object
   */
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
    UnionSet set = new UnionSet((SetType) sets[0].getType(), sets);
    return set;
  }

  public void cram_missing(boolean[] range_select) {
    int rl = range_select.length;
    int n = Sets.length;
    int k = 0;
    for (int i=0; i<n; i++) {
      int len = 0;
      try {
        len = Sets[i].getLength();
      }
      catch (VisADException e) {
        return;
      }
      if ((k + len) > rl) return;
      boolean[] ri = new boolean[len];
      System.arraycopy(range_select, k, ri, 0, len);
      Sets[i].cram_missing(ri);
      k += len;
    }
  }

  /** create a 2-D GeometryArray from this Set and color_values */
  public VisADGeometryArray make2DGeometry(byte[][] color_values,
         boolean indexed) throws VisADException {
    if (DomainDimension != 3) {
      throw new SetException("UnionSet.make2DGeometry: " +
                              "DomainDimension must be 3, not " +
                             DomainDimension);
    }
    if (ManifoldDimension != 2) {
      throw new SetException("UnionSet.make2DGeometry: " +
                              "ManifoldDimension must be 2, not " +
                             ManifoldDimension);
    }
    int n = Sets.length;
    int dim = (color_values != null) ? color_values.length : 0;
    if (indexed) {
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
        VisADGeometryArray array = Sets[i].make2DGeometry(c, indexed);
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
    else { // if (!indexed)
      VisADTriangleStripArray[] arrays =
        new VisADTriangleStripArray[n];
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
        VisADGeometryArray array = Sets[i].make2DGeometry(c, indexed);
        if (array instanceof VisADTriangleStripArray) {
          arrays[i] = (VisADTriangleStripArray) array;
        }
        else {
          arrays[i] = null;
        }
        base += len;
      }
      return VisADTriangleStripArray.merge(arrays);
    }
  }

  /** create a 1-D GeometryArray from this Set and color_values */
  public VisADGeometryArray make1DGeometry(byte[][] color_values)
         throws VisADException {
    if (DomainDimension != 3) {
      throw new SetException("UnionSet.make1DGeometry: " +
                              "DomainDimension must be 3, not " +
                             DomainDimension);
    }
    if (ManifoldDimension != 1) {
      throw new SetException("UnionSet.make1DGeometry: " +
                             "ManifoldDimension must be 1, not " +
                             ManifoldDimension);
    }
    int n = Sets.length;
    int dim = (color_values != null) ? color_values.length : 0;
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
    VisADLineStripArray array = VisADLineStripArray.merge(arrays);
    return array;
  }

  /** return basic lines in array[0], fill-ins in array[1]
      and labels in array[2] */
  public VisADGeometryArray[][] makeIsoLines(float[] intervals,
                  float low, float hi, float base,
                  float[] fieldValues, byte[][] color_values,
                  boolean[] swap, boolean dash,
                  boolean fill, ScalarMap[] smap,
                  double[] scale, double label_size,
                  boolean sphericalDisplayCS) throws VisADException {
    if (DomainDimension != 3) {
      throw new DisplayException("UnionSet.makeIsoLines: " +
                                 "DomainDimension must be 3, not " +
                                 DomainDimension);
    }
    if (ManifoldDimension != 2) {
      throw new DisplayException("UnionSet.makeIsoLines: " +
                                 "ManifoldDimension must be 2, not " +
                                 ManifoldDimension);
    }
    int n = Sets.length;
    int dim = color_values.length;
    VisADLineArray[][][] arrays = new VisADLineArray[n][][];
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
        (VisADLineArray[][]) Sets[i].makeIsoLines(intervals, low, hi, base,
                                                  f, c, swap, dash, fill, smap,
                                                  scale, label_size, sphericalDisplayCS);
      kbase += len;
    }
    VisADLineArray[][] arrays2 = new VisADLineArray[4][];
    for (int j=0; j<2; j++) { //- merge lines/fill
      arrays2[j] = new VisADLineArray[1];
      VisADLineArray[] arrays3 = new VisADLineArray[n];
      for (int i=0; i<n; i++) {
        arrays3[i] = arrays[i][j][0];
      }
      arrays2[j][0] = VisADLineArray.merge(arrays3);
    }
    for (int j=2; j<4; j++) { //- don't merge labels
      int cnt = 0;
      for (int i=0; i<n; i++) {
        cnt += arrays[i][j].length;
      }
      arrays2[j] = new VisADLineArray[cnt];
      cnt = 0;
      for (int i=0; i<n; i++) {
        for (int j2=0; j2<arrays[i][j].length; j2++) {
          arrays2[j][cnt++] = arrays[i][j][j2];
        }
      }
    }
    return arrays2;
  }

  public VisADGeometryArray makeIsoSurface(float isolevel,
         float[] fieldValues, byte[][] color_values, boolean indexed)
         throws VisADException {
    if (DomainDimension != 3) {
      throw new DisplayException("UnionSet.makeIsoSurface: " +
                                 "DomainDimension must be 3, not " +
                                 DomainDimension);
    }
    if (ManifoldDimension != 3) {
      throw new DisplayException("UnionSet.makeIsoSurface: " +
                                 "ManifoldDimension must be 3, not " +
                                 ManifoldDimension);
    }
    int n = Sets.length;
    int dim = color_values.length;
    if (indexed) {
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
        arrays[i] = (VisADIndexedTriangleStripArray)
          Sets[i].makeIsoSurface(isolevel, f, c, indexed);
        base += len;
      }
      return VisADIndexedTriangleStripArray.merge(arrays);
    }
    else { // if (!indexed)
      VisADTriangleStripArray[] arrays =
        new VisADTriangleStripArray[n];
      int base = 0;
      for (int i=0; i<n; i++) {
        int len = Sets[i].Length;
        byte[][] c = new byte[dim][len];
        float[] f = new float[len];
        for (int j=0; j<dim; j++) {
          for (int k=0; k<len; k++) c[j][k] = color_values[j][base + k];
        }
        for (int k=0; k<len; k++) f[k] = fieldValues[base + k];
        arrays[i] = (VisADTriangleStripArray)
          Sets[i].makeIsoSurface(isolevel, f, c, indexed);
        base += len;
      }
      return VisADTriangleStripArray.merge(arrays);
    } // end if (!indexed)
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
    if (npts == getLength()) {
      boolean ramp = true;
      for (int i=0; i<npts; i++) {
        if (index[i] != i) {
          ramp = false;
          break;
        }
      }
      if (ramp) {
        int k = 0;
        for (int j=0; j<nsets; j++) {
          int sub_length = Sets[j].getLength();
          int[] sub_inds = new int[sub_length];
          for (int i=0; i<sub_length; i++) sub_inds[i] = i;
          float[][] sub_vals = Sets[j].indexToValue(sub_inds);
          for (int m=0; m<DomainDimension; m++) {
            System.arraycopy(sub_vals[m], 0, value[m], k, sub_length);
/*
try {
            System.arraycopy(sub_vals[m], 0, value[m], k, sub_length);
}
catch (ArrayIndexOutOfBoundsException e) {
  System.out.println("m = " + m + " k = " + k + " sub_length = " + sub_length +
                     " sub_vals[m].length = " + sub_vals[m].length +
                     " value[m].length = " + value[m].length);
  System.out.println("npts = " + npts + " getLength = " + getLength() +
                     " nsets = " + nsets + " j = " + j);
  for (int i=0; i<nsets; i++) System.out.print(Sets[i].getLength() + " ");
  System.out.print("\n");

m = 0 k = 1 sub_length = 2 sub_vals[m].length = 2 value[m].length = 2
npts = 2 getLength = 2 nsets = 2 j = 1
1 2 
m = 1 k = 1 sub_length = 2 sub_vals[m].length = 2 value[m].length = 2
npts = 2 getLength = 2 nsets = 2 j = 1
1 2 
java.lang.ArrayIndexOutOfBoundsException
        at visad.UnionSet.makeSpatial(UnionSet.java:321)
}
*/
/*
            for (int i=0; i<sub_length; i++) {
              value[m][k+i] = sub_vals[m][i];
            }
*/
          }
          k += sub_length;
        }
        return value;
      } // end if (ramp)
    } // end if (npts == getLength())

    int[][] subindex = new int[nsets][];

    // classify each index point into its proper subset
    int[] ind_lens = new int[nsets];
    int[] set_num = new int[npts];
    int[] new_ind = new int[npts];
    for (int j=0; j<npts; j++) {
      int q = 0;
      int curind = index[j];
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
    int nsets = Sets.length;
    int length = indices.length;
    int offset = 0;
    for (int i=0; i<nsets; i++) {
      int[][] temp_indices = new int[length][];
      float[][] temp_weights = new float[length][];
      Sets[i].valueToInterp(value, temp_indices, temp_weights);
      for (int j=0; j<length; j++) {
        if (indices[j] == null && temp_indices[j] != null) {
          int m = temp_indices[j].length;
          indices[j] = new int[m];
          weights[j] = new float[m];
          for (int k=0; k<m; k++) {
            indices[j][k] = temp_indices[j][k] + offset;
            weights[j][k] = temp_weights[j][k];
          }
        }
      }
      offset += Sets[i].getLength();
    }
  }

  /**
   * Clones this instance.
   *
   * @return                        A clone of this instance.
   */
  public Object clone() {
    UnionSet clone = (UnionSet)super.clone();
    
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

  /**
   * Clone this UnionSet, but give it a new MathType; this is safe,
   * since constructor checks consistency of DomainCoordinateSystem
   * and SetUnits with type.
   *
   * @param   type   new MathType for the UnionSet
   * @return  UnionSet with the new MathType
   *
   * @exception  VisADException  couldn't create the new UnionSet
   */
  public Object cloneButType(MathType type) throws VisADException {
    int n = Sets.length;
    SampledSet[] sets = new SampledSet[n];
    for (int i=0; i<n; i++) {
      sets[i] = (SampledSet) Sets[i].cloneButType(type);
    }
    return new UnionSet(type, sets, DomainCoordinateSystem,
                        SetUnits, SetErrors);
/* WLH 3 April 2003
    return new UnionSet(type, Sets, DomainCoordinateSystem,
                        SetUnits, SetErrors);
*/
  }

  /**
   * Check to see if two UnionSets are equal.
   *
   * @return  true if each of the sets in set is equal to the sets in this.
   */
  public boolean equals(Object set) {
    if (!(set instanceof UnionSet) || set == null) return false;
    if (this == set) return true;

    UnionSet uset = (UnionSet) set;
    if (uset.DomainDimension != DomainDimension
     || uset.ManifoldDimension != ManifoldDimension) return false;

    //Added this to make sure both arrays are equal length
    if (Sets.length != uset.Sets.length) return false;    

    for (int i=0; i<Sets.length; i++) {
      if (!Sets[i].equals(uset.Sets[i])) return false;
    }
    return true;
  }

  /**
   * Returns the hash code of this instance.
   * @return		The hash code of this instance.
   */
  public int hashCode() {
    if (!hashCodeSet)
    {
      hashCode = DomainDimension ^ ManifoldDimension;
      for (int i=0; i<Sets.length; i++)
	hashCode ^= Sets[i].hashCode();
      hashCodeSet = true;
    }
    return hashCode;
  }

  /**
   * Check to see if any of the sets in this UnionSet has missing data.
   *
   * @return  true if any of the sets has missing data, otherwise false
   */
  public boolean isMissing() {
    for (int i=0; i<Sets.length; i++) {
      if (Sets[i].isMissing()) return true;
    }
    return false;
  }

  /* run 'java visad.UnionSet' to test the UnionSet class */
  public static void main(String[] argv) throws VisADException {
    RealType vis_xcoord = RealType.getRealType("1D");
    RealType[] vis_array = {vis_xcoord};
    RealTupleType vis_tuple = new RealTupleType(vis_array);

    // create Gridded1DSet
    float[][] sampG = { {2f, 4f, 6f, 8f, 10f, 12f, 14f, 16f} };
    Gridded1DSet gSet = new Gridded1DSet(vis_tuple, sampG, 8);

    // create Irregular1DSet
    float[][] sampI = { {100f, 90f, 110f, 80f, 120f, 70f, 130f, 60f} };
    Irregular1DSet iSet = new Irregular1DSet(vis_tuple, sampI);

    // create UnionSet as the union of gSet and iSet
    SampledSet[] sets = {gSet, iSet};
    UnionSet uSet = new UnionSet(vis_tuple, sets);

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
    // float[][] value2 = { {10f, 40f, 90f, 25f, 50f, 100f, 30f, 70f} };
    float[][] value2 = { {15f, 40f, 92f, 25f, 50f, 103f, 37f, 77f} };
    int[] index2 = uSet.valueToIndex(value2);
    for (int i=0; i<index2.length; i++) {
      System.out.print("("+value2[0][i]);
      for (int j=1; j<value2.length; j++) {
        System.out.print(", "+value2[j][i]);
      }
      System.out.println(")\t==> index "+index2[i]);
    }

    System.out.println("-----------------");
    System.out.println("valueToInterp test:");
    int n = value2[0].length;
    int[][] indices = new int[n][];
    float[][] weights = new float[n][];
    uSet.valueToInterp(value2, indices, weights);
    for (int i=0; i<n; i++) {
      System.out.print("("+value2[0][i]);
      for (int j=1; j<value2.length; j++) {
        System.out.print(", "+value2[j][i]);
      }
      System.out.print(")\t==>");
      if (indices[i] == null || indices[i].length == 0) {
        System.out.println(" missing");
      }
      else {
        int m = indices[i].length;
        for (int j=0; j<m; j++) {
          System.out.print(" (" + indices[i][j] + "," + weights[i][j] + ")");
        }
        System.out.println(" ");
      }
    }

    System.out.println();
  }

  public String longString(String pre) throws VisADException {
    String s = pre + "UnionSet: Dimension = " + DomainDimension +
               " Length = " + getLength() + "\n";
    int n = Sets.length;
    for (int i=0; i<n; i++) {
      s = s + Sets[i].toString();
    }
    return s;
  }

}

