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

/**
   UnionSet is the union of an array of SampledSets.<P>
*/
public class UnionSet extends SampledSet {

  public SampledSet[] Sets;

  /** construct a UnionSet with an array of SampledSets */
  public UnionSet(MathType type, SampledSet[] sets) throws VisADException {
    this(type, sets, null, null, null, true);
  }

  /** construct a UnionSet with an array of SampledSets and
      non-default CoordinateSystem */
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
  }

  private static int find_manifold_dim(SampledSet[] sets)
                                        throws VisADException {
    if (sets == null || sets[0] == null) {
      throw new SetException("UnionSet: Sets cannot be missing");
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
      while (q < nsets && index[j] > Sets[q].Length) {
        curind -= Sets[q++].Length;
      }
      if (q == nsets) curind += Sets[--q].Length;
      ind_lens[q]++;
      set_num[j] = q;
      new_ind[j] = curind;
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
        value[k][j] = subvals[set_num[j]][k][ind_lens[set_num[j]]++];
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

}

