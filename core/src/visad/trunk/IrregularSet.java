//
// IrregularSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
   IrregularSet is implemented by those Set sub-classes whose samples
   do not form any ordered pattern.  It is a M-dimensional array of
   points in R^N where ManifoldDimension = M <= N = DomainDimension.<P>

   The order of the samples is the rasterization of the orders of
   the 1D components, with the first component increasing fastest.
   For more detail, see the example in Linear2DSet.java.<P>
*/
public class IrregularSet extends SampledSet {

  public Delaunay Delan = null;

  /** oldToNew and newToOld used when ManifoldDimension = 1
      but DomainDimension > 1 */
  /** maps old samples indices to sorted samples indices */
  int[] oldToNew;
  /** maps sorted samples indices to old samples indices */
  int[] newToOld;

  /** construct an IrregularSet */
  public IrregularSet(MathType type, float[][] samples)
         throws VisADException {
    this(type, samples, samples.length, null, null, null, null, true);
  }

  /** construct an IrregularSet with non-default CoordinateSystem */
  public IrregularSet(MathType type, float[][] samples,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this (type, samples, samples.length, coord_sys,
          units, errors, null, true);
  }

  /** construct an IrregularSet with non-default Delaunay */
  public IrregularSet(MathType type, float[][] samples, Delaunay delan)
         throws VisADException {
    this(type, samples, samples.length, null, null, null, delan, true);
  }

  /** construct an IrregularSet with non-default
      CoordinateSystem and non-default Delaunay */
  public IrregularSet(MathType type, float[][] samples,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors, Delaunay delan)
         throws VisADException {
    this(type, samples, samples.length, coord_sys,
         units, errors, delan, true);
  }

  /** construct an IrregularSet with ManifoldDimension != DomainDimension,
      with non-default CoordinateSystem, and with non-default Delaunay */
  public IrregularSet(MathType type, float[][] samples,
                      int manifold_dimension, CoordinateSystem coord_sys,
                      Unit[] units, ErrorEstimate[] errors, Delaunay delan)
         throws VisADException {
    this(type, samples, manifold_dimension, coord_sys,
         units, errors, delan, true);
  }

  public IrregularSet(MathType type, float[][] samples,
                      int manifold_dimension, CoordinateSystem coord_sys,
                      Unit[] units, ErrorEstimate[] errors, Delaunay delan,
                      boolean copy) throws VisADException {
    super(type, manifold_dimension, coord_sys, units, errors);
    if (samples == null ) {
      throw new SetException("IrregularSet: samples cannot be null");
    }
    init_samples(samples, copy);

    // initialize Delaunay triangulation structure
    if (ManifoldDimension > 1) {
      if (delan != null) {
        if (copy) Delan = (Delaunay) delan.clone();
        else Delan = delan;
      }
      else Delan = Delaunay.factory(samples, false);
    }
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    float[][] value = new float[DomainDimension][index.length];
    float[][]mySamples = getMySamples();
    for (int i=0; i<index.length; i++) {
      if ( (index[i] >= 0) && (index[i] < Length) ) {
        for (int j=0; j<DomainDimension; j++) {
          value[j][i] = mySamples[j][index[i]];
        }
      }
      else {
        for (int j=0; j<DomainDimension; j++) {
          value[j][i] = Float.NaN;
        }
      }
    }
    return value;
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    throw new UnimplementedException("IrregularSet.valueToIndex");
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if no interpolation is possible */
  public void valueToInterp(float[][] value, int[][] indices,
                            float weights[][]) throws VisADException {
    throw new UnimplementedException("IrregularSet.valueToInterp");
  }

  /**
   * Returns the indexes of neighboring samples for all samples.
   *
   * @param neighbors		The indexes of the neighboring points.	On
   *				input, <code>neighbors.length</code>
   *				must be greater than or equal to
   *				<code>getLength()</code>.  On output,
   *				<code>neighbors[i][j]</code> will be the index
   *				of the <code>j</code>th neighboring sample of
   *				sample <code>i</code>.
   * @throws VisADException     if a VisAD failure occurs.
   */
  public void getNeighbors( int[][] neighbors )
         throws VisADException
  {
    if ( ManifoldDimension == 1 )
    {
      neighbors[0] = new int[2];
      neighbors[Length - 1] = new int[2];
      neighbors[0][0] = 1;
      neighbors[Length - 1][0] = Length - 2;

      for ( int ii = 1; ii < (Length - 1); ii++ ) {
        neighbors[ii] = new int[2];
        neighbors[ii][0] = ii - 1;
        neighbors[ii][0] = ii + 1;
      }
    }
    else if ( ManifoldDimension < 4 )
    {

      int[][] Vertices = Delan.Vertices;
      int[][] Tri = Delan.Tri;
      int n_samples = Vertices.length;
      int n_triangles;
      int cnt, ii, jj, kk, tt, index;
      int[] indeces;

      for ( ii = 0; ii < n_samples; ii++ )
      {
        indeces = new int[n_samples];
        n_triangles = Vertices[ii].length;
        for ( jj = 0; jj < n_triangles; jj++ )
        {
          for ( kk = 0; kk < Tri[ Vertices[ii][jj] ].length; kk++ )
          {
            index = Tri[ Vertices[ii][jj] ][kk];
            if ( index != ii ) indeces[ index ]++;
          }
        }
        cnt = 0;
        for ( tt = 0; tt < n_samples; tt++ ) {
          if ( indeces[tt] > 0 ) cnt++;
        }
        neighbors[ii] = new int[ cnt ];
        cnt = 0;
        for ( tt = 0; tt < n_samples; tt++ ) {
          if ( indeces[tt] > 0 ) {
            neighbors[ii][cnt] = tt;
            cnt++;
          }
        }
        indeces = null;
      }
    }
    else
    {
      throw new UnimplementedException("getNeighbors(): ManifoldDimension > 3 ");
    }
  }

  public boolean equals(Object set) {
    if (!(set instanceof IrregularSet) || set == null ||
        set instanceof LinearSet) return false;
    if (this == set) return true;
    if (testNotEqualsCache((Set) set)) return false;
    if (testEqualsCache((Set) set)) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    try {
      int i, j;
      if (DomainDimension != ((IrregularSet) set).getDimension() ||
          ManifoldDimension != ((IrregularSet) set).getManifoldDimension() ||
          Length != ((IrregularSet) set).getLength()) return false;
      // Sets are immutable, so no need for 'synchronized'
      float[][]mySamples = getMySamples();
      float[][] samples = ((IrregularSet) set).getSamples(false);
      for (j=0; j<DomainDimension; j++) {
        for (i=0; i<Length; i++) {
          if (mySamples[j][i] != samples[j][i]) {
            addNotEqualsCache((Set) set);
            return false;
          }
        }
      }
      addEqualsCache((Set) set);
      return true;
    }
    catch (VisADException e) {
      return false;
    }
  }

  /**
   * Clones this instance.
   *
   * @return                            A clone of this instance.
   */
  public Object clone() {
    IrregularSet clone = (IrregularSet)super.clone();

    if (Delan != null)
      clone.Delan = (Delaunay)Delan.clone();

    return clone;
  }

  public Object cloneButType(MathType type) throws VisADException {
     return new IrregularSet(type, getMySamples(), DomainCoordinateSystem,
                          SetUnits, SetErrors, Delan);
  }

  public String longString(String pre) throws VisADException {
    String s;
    int j;
    if (DomainDimension == ManifoldDimension) {
      s = pre + getClass().getName() + ": Dimension = " +
          DomainDimension + " Length = " + Length + "\n";
      for (j=0; j<DomainDimension; j++) {
        s = s + pre + "  Dimension " + j + ":" +
                " Range = " + Low[j] + " to " + Hi[j] + "\n";
      }
    }
    else {
      s = pre + getClass().getName() + ": DomainDimension = " +
          DomainDimension + " ManifoldDimension = " + ManifoldDimension +
          " Length = " + Length + "\n";
      for (j=0; j<DomainDimension; j++) {
        s = s + pre + "  DomainDimension " + j + ":" + " Range = " +
            Low[j] + " to " + Hi[j] + "\n";
      }
    }
    return s;
  }

}

