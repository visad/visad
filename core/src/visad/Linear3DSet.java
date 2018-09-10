//
// Linear3DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
   Linear3DSet represents a finite set of samples of R^3 in
   a cross product of three arithmetic progressions.<P>

   The order of the samples is the rasterization of the orders of
   the 1D components, with the first component increasing fastest.
   For more detail, see the example in Linear2DSet.java.<P>
*/
public class Linear3DSet extends Gridded3DSet
       implements LinearSet {

  Linear1DSet X, Y, Z;
  private boolean cacheSamples;

  /**
   * Construct a 3-D cross product of <code>sets</code> with a
   * generic MathType.
   * @param sets     Linear1DSets that make up this Linear3DSet.
   * @throws VisADException illegal sets or other VisAD error.
   */
  public Linear3DSet(Linear1DSet[] sets) throws VisADException {
    this(RealTupleType.Generic3D, sets, null, null, null);
  }

  /**
   * Construct a 3-D cross product of <code>sets</code> with the
   * specified <code>type</code>.
   * @param type     MathType for this Linear3DSet.  Must be consistent
   *                 with MathType-s of sets.
   * @param sets     Linear1DSets that make up this Linear3DSet.
   * @throws VisADException illegal sets or other VisAD error.
   */
  public Linear3DSet(MathType type, Linear1DSet[] sets) throws VisADException {
    this(type, sets, null, null, null);
  }

  /** 
   * Construct a 3-D cross product of arithmetic progressions with
   * null errors and generic type.
   * @param first1     first value in first arithmetic progression
   * @param last1      last value in first arithmetic progression
   * @param length1    number of values in first arithmetic progression
   * @param first2     first value in second arithmetic progression
   * @param last2      last value in second arithmetic progression
   * @param length2    number of values in second arithmetic progression
   * @param first3     first value in third arithmetic progression
   * @param last3      last value in third arithmetic progression
   * @param length3    number of values in third arithmetic progression
   * @throws VisADException problem creating VisAD objects.
   */
  public Linear3DSet(double first1, double last1, int length1,
                     double first2, double last2, int length2,
                     double first3, double last3, int length3)
         throws VisADException {
    this(RealTupleType.Generic3D,
         LinearNDSet.get_linear1d_array(RealTupleType.Generic3D,
                                        first1, last1, length1,
                                        first2, last2, length2,
                                        first3, last3, length3, null),
         null, null, null);
  }

  /** 
   * Construct a 3-D cross product of arithmetic progressions with
   * null errors and the specified <code>type</code>.
   * @param type       MathType for this Linear3DSet.  
   * @param first1     first value in first arithmetic progression
   * @param last1      last value in first arithmetic progression
   * @param length1    number of values in first arithmetic progression
   * @param first2     first value in second arithmetic progression
   * @param last2      last value in second arithmetic progression
   * @param length2    number of values in second arithmetic progression
   * @param first3     first value in third arithmetic progression
   * @param last3      last value in third arithmetic progression
   * @param length3    number of values in third arithmetic progression
   * @throws VisADException problem creating VisAD objects.
   */
  public Linear3DSet(MathType type, double first1, double last1, int length1,
                                    double first2, double last2, int length2,
                                    double first3, double last3, int length3)
         throws VisADException {
    this(type, LinearNDSet.get_linear1d_array(type, first1, last1, length1,
                                              first2, last2, length2,
                                              first3, last3, length3, null),
         null, null, null);
  }

  /** 
   * Construct a 3-D cross product of arithmetic progressions with
   * the specified <code>type</code>, <code>coord_sys</code>, 
   * <code>units</code> and <code>errors</code>.
   * @param type       MathType for this Linear3DSet.  
   * @param first1     first value in first arithmetic progression
   * @param last1      last value in first arithmetic progression
   * @param length1    number of values in first arithmetic progression
   * @param first2     first value in second arithmetic progression
   * @param last2      last value in second arithmetic progression
   * @param length2    number of values in second arithmetic progression
   * @param first3     first value in third arithmetic progression
   * @param last3      last value in third arithmetic progression
   * @param length3    number of values in third arithmetic progression
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @throws VisADException problem creating VisAD objects.
   */
  public Linear3DSet(MathType type, double first1, double last1, int length1,
                     double first2, double last2, int length2, double first3,
                     double last3, int length3, CoordinateSystem coord_sys,
                     Unit[] units, ErrorEstimate[] errors) throws VisADException {
    this(type, first1, last1, length1, first2, last2, length2, 
         first3, last3, length3, coord_sys, units, errors, false);
  }

  /** 
   * Construct a 3-D cross product of arithmetic progressions with
   * the specified <code>type</code>, <code>coord_sys</code>, 
   * <code>units</code> and <code>errors</code>.
   * @param type       MathType for this Linear3DSet.  
   * @param first1     first value in first arithmetic progression
   * @param last1      last value in first arithmetic progression
   * @param length1    number of values in first arithmetic progression
   * @param first2     first value in second arithmetic progression
   * @param last2      last value in second arithmetic progression
   * @param length2    number of values in second arithmetic progression
   * @param first3     first value in third arithmetic progression
   * @param last3      last value in third arithmetic progression
   * @param length3    number of values in third arithmetic progression
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @param cache      if true, enumerate and cache the samples.  This will
   *                   result in a larger memory footprint, but will
   *                   reduce the time to return samples from calls to 
   *                   {@link #getSamples()}.
   * @throws VisADException problem creating VisAD objects.
   */
  public Linear3DSet(MathType type, double first1, double last1, int length1,
                     double first2, double last2, int length2, double first3,
                     double last3, int length3, CoordinateSystem coord_sys,
                     Unit[] units, ErrorEstimate[] errors,
                     boolean cache) throws VisADException {
    this(type, LinearNDSet.get_linear1d_array(type, first1, last1, length1,
                                              first2, last2, length2,
                                              first3, last3, length3, units),
         coord_sys, units, errors, cache);
  }

  /**
   * Construct a 3-D cross product of <code>sets</code>, with
   * the specified <code>type</code>, <code>coord_sys</code>, 
   * <code>units</code> and <code>errors</code>.
   * @param type       MathType for this Linear3DSet.  Must be consistent
   *                   with MathType-s of <code>sets</code>.
   * @param sets       Linear1DSets that make up this Linear3DSet.
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @throws VisADException illegal sets or other VisAD error.
   */
  public Linear3DSet(MathType type, Linear1DSet[] sets,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors) throws VisADException {
    this(type, sets, coord_sys, units, errors, false);
  }

  /**
   * Construct a 3-D cross product of <code>sets</code>, with
   * the specified <code>type</code>, <code>coord_sys</code>, 
   * <code>units</code> and <code>errors</code>.
   * @param type       MathType for this Linear3DSet.  Must be consistent
   *                   with MathType-s of <code>sets</code>.
   * @param sets       Linear1DSets that make up this Linear3DSet.
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @param cache      if true, enumerate and cache the samples.  This will
   *                   result in a larger memory footprint, but will
   *                   reduce the time to return samples from calls to 
   *                   {@link #getSamples()}.
   * @throws VisADException illegal sets or other VisAD error.
   */
  public Linear3DSet(MathType type, Linear1DSet[] sets,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors, boolean cache) throws VisADException {
    super(type, (float[][]) null, sets[0].getLength(), sets[1].getLength(),
          sets[2].getLength(), coord_sys,
          LinearNDSet.units_array_linear1d(sets, units), errors);
    if (DomainDimension != 3) {
      throw new SetException("Linear3DSet: DomainDimension must be 3, not " +
                             DomainDimension);
    }
    if (sets.length != 3) {
      throw new SetException("Linear3DSet: ManifoldDimension must be 3, not " +
                             sets.length);
    }
    Linear1DSet[] ss = LinearNDSet.linear1d_array_units(sets, units);
    X = ss[0];
    Y = ss[1];
    Z = ss[2];
    LengthX = X.getLength();
    LengthY = Y.getLength();
    LengthZ = Z.getLength();
    Length = LengthX * LengthY * LengthZ;
    Low[0] = X.getLowX();
    Hi[0] = X.getHiX();
    Low[1] = Y.getLowX();
    Hi[1] = Y.getHiX();
    Low[2] = Z.getLowX();
    Hi[2] = Z.getHiX();
    if (SetErrors[0] != null ) {
      SetErrors[0] =
        new ErrorEstimate(SetErrors[0].getErrorValue(), (Low[0] + Hi[0]) / 2.0,
                          Length, SetErrors[0].getUnit());
    }
    if (SetErrors[1] != null ) {
      SetErrors[1] =
        new ErrorEstimate(SetErrors[1].getErrorValue(), (Low[1] + Hi[1]) / 2.0,
                          Length, SetErrors[1].getUnit());
    }
    if (SetErrors[2] != null ) {
      SetErrors[2] =
        new ErrorEstimate(SetErrors[2].getErrorValue(), (Low[2] + Hi[2]) / 2.0,
                          Length, SetErrors[2].getUnit());
    }
    cacheSamples = cache;
  }

  /** 
   * Convert an array of 1-D indices to an array of values in 
   * R^3 space.
   * @param index  array of indices of values in R^3 space.
   * @return  values in R^3 space corresponding to indices.
   * @throws  VisADException  problem converting indices to values.
   */
  public float[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    int[] indexX = new int[length];
    int[] indexY = new int[length];
    int[] indexZ = new int[length];
    float[][] values = new float[3][];

    for (int i=0; i<length; i++) {
      if (0 <= index[i] && index[i] < Length) {
        indexX[i] = index[i] % LengthX;
        int k = index[i] / LengthX;
        indexY[i] = k % LengthY;
        indexZ[i] = k / LengthY;
      }
      else {
        indexX[i] = -1;
        indexY[i] = -1;
        indexZ[i] = -1;
      }
    }
    float[][] valuesX = X.indexToValue(indexX);
    float[][] valuesY = Y.indexToValue(indexY);
    float[][] valuesZ = Z.indexToValue(indexZ);
    values[0] = valuesX[0];
    values[1] = valuesY[0];
    values[2] = valuesZ[0];
    return values;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^3 */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    if (grid.length != ManifoldDimension) {
      throw new SetException("Linear3DSet.gridToValue: grid dimension " +
                             grid.length +
                             " not equal to Manifold dimension " +
                             ManifoldDimension);
    }
    if (ManifoldDimension != 3) {
      throw new SetException("Linear3DSet.gridToValue: ManifoldDimension " +
                             "must be 3, not " + ManifoldDimension);
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2 || Lengths[2] < 2)) {
      throw new SetException("Linear3DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = grid[0].length;
    float[][] gridX = new float[1][];
    gridX[0] = grid[0];
    float[][] gridY = new float[1][];
    gridY[0] = grid[1];
    float[][] gridZ = new float[1][];
    gridZ[0] = grid[2];
    float[][] valueX = X.gridToValue(gridX);
    float[][] valueY = Y.gridToValue(gridY);
    float[][] valueZ = Z.gridToValue(gridZ);
    float[][] value = new float[3][];
    value[0] = valueX[0];
    value[1] = valueY[0];
    value[2] = valueZ[0];
    return value;
  }

  /** transform an array of values in R^3 to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    if (value.length != 3) {
      throw new SetException("Linear3DSet.valueToGrid: value dimension" +
                             " must be 3, not " + value.length);
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2 || Lengths[2] < 2)) {
      throw new SetException("Linear3DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = value[0].length;
    float[][] valueX = new float[1][];
    valueX[0] = value[0];
    float[][] valueY = new float[1][];
    valueY[0] = value[1];
    float[][] valueZ = new float[1][];
    valueZ[0] = value[2];
    float[][] gridX = X.valueToGrid(valueX);
    float[][] gridY = Y.valueToGrid(valueY);
    float[][] gridZ = Z.valueToGrid(valueZ);
    float[][] grid = new float[3][];
    grid[0] = gridX[0];
    grid[1] = gridY[0];
    grid[2] = gridZ[0];
    return grid;
  }

  /**
   * Return the first arithmetic progression for this
   * cross product (X of XYZ).
   * @return first arithmetic progression as a Linear1DSet.
   */
  public Linear1DSet getX() {
    return X;
  }

  /**
   * Return the second arithmetic progression for this
   * cross product (Y of XYZ).
   * @return second arithmetic progression as a Linear1DSet.
   */
  public Linear1DSet getY() {
    return Y;
  }

  /**
   * Return the third arithmetic progression for this
   * cross product (Z of XYZ).
   * @return third arithmetic progression as a Linear1DSet.
   */
  public Linear1DSet getZ() {
    return Z;
  }

  /**
   * Check to see if this is an empty cross-product.
   * @return always false.
   */
  public boolean isMissing() {
    return false;
  }

  /**
   * Return the array of values in R^3 space corresponding to
   * this cross product of arithmetic progressions.
   * @param  copy  if true, return a copy of the samples.
   * @return  array of values in R^3 space.
   * @throws  VisADException  problem creating samples.
   */
  public float[][] getSamples(boolean copy) throws VisADException {
    /*  DRM 2003-01-16
    int n = getLength();
    int[] indices = new int[n];
    // do NOT call getWedge
    for (int i=0; i<n; i++) indices[i] = i;
    return indexToValue(indices);
    */
    float[][]mySamples = getMySamples();
    if (mySamples != null) {
      return copy ? Set.copyFloats(mySamples) : mySamples;
    }
    float[][] samples = makeSamples ();
    if (cacheSamples) {
      setMySamples(samples);
      return copy ? Set.copyFloats(samples) : samples;
    }
    return samples;
  }

  /** code to actually enumerate the samples from the Linear1DSets
      into an array in R^3 space. */
  private float[][] makeSamples () throws VisADException {
    float[][] xVals = X.getSamples(false);
    float[][] yVals = Y.getSamples(false);
    float[][] zVals = Z.getSamples(false);
    float[][] samples = new float[3][Length];
    int idx = 0;
    for (int k = 0; k < zVals[0].length; k++) {
      for (int j = 0; j < yVals[0].length; j++) {
        for (int i = 0; i < xVals[0].length; i++) {
          // set or load coordinate values
          samples[0][idx] = (float) xVals[0][i];
          samples[1][idx] = (float) yVals[0][j];
          samples[2][idx] = (float) zVals[0][k];
          idx++;
        }
      }
    }
    return samples;
  }

  /** note makeSpatial never returns a Linear3DSet,
      so this is not enough;
      must handle it like linear texture mapping;
      also, want to exploit Texture3D -
      so must figure out how to make texture alpha work */
  public VisADGeometryArray[] make3DGeometry(byte[][] color_values)
         throws VisADException {
    if (ManifoldDimension != 3) {
      throw new SetException("Linear3DSet.make3DGeometry: " +
                             "ManifoldDimension must be 3, not " +
                             ManifoldDimension);
    }
    int lengthX = X.getLength();
    int lengthY = Y.getLength();
    int lengthZ = Z.getLength();
    if (lengthX < 2 || lengthY < 2 || lengthZ < 2 ||
        color_values.length < 4) {
      VisADGeometryArray array = makePointGeometry(color_values);
      return new VisADGeometryArray[] {array, array, array};
    }
    double firstX = X.getFirst();
    double firstY = Y.getFirst();
    double firstZ = Z.getFirst();
    double lastX = X.getLast();
    double lastY = Y.getLast();
    double lastZ = Z.getLast();
/* Now the 'brick' has dimensions lengthX * lengthY * lengthZ
   and locations in each of X, Y and Z are in arithmetic
   progression, ranging from firstX to lastX in X, etc.

   The color component are laid out as follows: */
    float red, green, blue, alpha;
    for (int x=0; x<lengthX; x++) {
      for (int y=0; y<lengthY; y++) {
        for (int z=0; z<lengthZ; z++) {
          red = color_values[0][x + lengthX * (y + lengthY * z)];
          green = color_values[2][x + lengthX * (y + lengthY * z)];
          blue = color_values[3][x + lengthX * (y + lengthY * z)];
          alpha = color_values[4][x + lengthX * (y + lengthY * z)];
        }
      }
    }
    /* now construct 3 triangle strip arrays, for the planes
       perpendicular to X, Y and Z */
    VisADTriangleStripArray[] arrays = {new VisADTriangleStripArray(),
      new VisADTriangleStripArray(), new VisADTriangleStripArray()};

    return arrays;
  }

  /* almost identical with Gridded3DSet.makeIsoSurface, except:
     1. spatial_maps and permute arguments
     2. compute 'float[] sos' array
     3. call linear_isosurf rather than isosurf, with added sos argument
     4. permute VX, VY and VZ
  */
  public VisADGeometryArray makeLinearIsoSurface(float isolevel,
         float[] fieldValues, byte[][] color_values, boolean indexed,
         ScalarMap[] spatial_maps, int[] permute)
         throws VisADException {
    boolean debug = false;

    int      i, NVT, cnt;
    int      size_stripe;
    int      xdim_x_ydim, xdim_x_ydim_x_zdim;
    int      num_cubes, nvertex, npolygons;
    int      ix, iy, ii;
    int nvertex_estimate;

    if (ManifoldDimension != 3) {
      throw new DisplayException("Linear3DSet.makeLinearIsoSurface: " +
                                 "ManifoldDimension must be 3, not " +
                                 ManifoldDimension);
    }

    /* adapt isosurf algorithm to Gridded3DSet variables */
    // NOTE X & Y swap
    int xdim = LengthY;
    int ydim = LengthX;
    int zdim = LengthZ;

    float[] ptGRID = fieldValues;

    xdim_x_ydim = xdim * ydim;
    xdim_x_ydim_x_zdim = xdim_x_ydim * zdim;
    num_cubes = (xdim-1) * (ydim-1) * (zdim-1);

    int[]  ptFLAG = new int[ num_cubes ];
    int[]  ptAUX  = new int[ xdim_x_ydim_x_zdim ];
    int[]  pcube  = new int[ num_cubes+1 ];

    // System.out.println("pre-flags: isolevel = " + isolevel +
    //                    " xdim, ydim, zdim = " + xdim + " " + ydim + " " + zdim);

    npolygons = flags( isolevel, ptFLAG, ptAUX, pcube,
                       ptGRID, xdim, ydim, zdim );

    if (debug) System.out.println("npolygons= "+npolygons);

    if (npolygons == 0) return null;

    // take the garbage out
    pcube = null;

    nvertex_estimate = 4 * npolygons + 100;
    ix = 9 * (nvertex_estimate + 50);
    iy = 7 * npolygons;

    float[][] VX = new float[1][nvertex_estimate];
    float[][] VY = new float[1][nvertex_estimate];
    float[][] VZ = new float[1][nvertex_estimate];

    byte[][] color_temps = null;
    if (color_values != null) {
      color_temps = new byte[color_values.length][];
    }

    int[] Pol_f_Vert = new int[ix];
    int[] Vert_f_Pol = new int[iy];
    int[][] arg_Pol_f_Vert = new int[][] {Pol_f_Vert};

    // get scales and offsets from linear grid coordinates to graphics coordinates
    float[] sos = new float[6];
    Unit[] set_units = getSetUnits();
    RealTupleType domain_type = ((SetType) getType()).getDomain();
    Unit[] def_units = domain_type.getDefaultUnits();
    Linear1DSet[] sets = {getX(), getY(), getZ()};
    double[] so = new double[2];
    double[] data = new double[2];
    double[] display = new double[2];
    for (i=0; i<3; i++) {
      double first = sets[i].getFirst();
      double step = sets[i].getStep();
      if (set_units[i] != null && !set_units[i].equals(def_units[i])) {
        double uo = set_units[i].toThat(0.0, def_units[i]);
        double us = set_units[i].toThat(1.0, def_units[i]) - uo;
        first = uo + us * first;
        step = us * step;
      }
      spatial_maps[i].getScale(so, data, display);
      sos[2 * i + 0] = (float) (so[1] + so[0] * first); // overall X offset
      sos[2 * i + 1] = (float) (so[0] * step);          // overall X scale
    }

    // NOTE X & Y swap
    float t = sos[0];
    sos[0] = sos[2];
    sos[2] = t;
    t = sos[1];
    sos[1] = sos[3];
    sos[3] = t;
    nvertex = linear_isosurf( isolevel, ptFLAG, nvertex_estimate, npolygons,
                              ptGRID, xdim, ydim, zdim, VY, VX, VZ,
                              color_values, color_temps, arg_Pol_f_Vert,
                              Vert_f_Pol, sos );
    Pol_f_Vert = arg_Pol_f_Vert[0];

    if (nvertex == 0) return null;

    // take the garbage out
    ptFLAG = null;
    ptAUX = null;
/*
for (int j=0; j<nvertex; j++) {
  System.out.println("iso vertex[" + j + "] " + VX[0][j] + " " + VY[0][j] +
                     " " + VZ[0][j]);
}
*/
    float[][] fieldVertices = new float[3][nvertex];
    // NOTE - NO X & Y swap
    System.arraycopy(VX[0], 0, fieldVertices[permute[0]], 0, nvertex);
    System.arraycopy(VY[0], 0, fieldVertices[permute[1]], 0, nvertex);
    System.arraycopy(VZ[0], 0, fieldVertices[permute[2]], 0, nvertex);
    // take the garbage out
    VX = null;
    VY = null;
    VZ = null;

    byte[][] color_levels = null;
    if (color_values != null) {
      color_levels = new byte[color_values.length][nvertex];
      System.arraycopy(color_temps[0], 0, color_levels[0], 0, nvertex);
      System.arraycopy(color_temps[1], 0, color_levels[1], 0, nvertex);
      System.arraycopy(color_temps[2], 0, color_levels[2], 0, nvertex);
      if (color_temps.length > 3) {
        System.arraycopy(color_temps[3], 0, color_levels[3], 0, nvertex);
      }
      // take the garbage out
      color_temps = null;
    }

    if (debug) System.out.println("nvertex= "+nvertex);

    float[] NxA = new float[npolygons];
    float[] NxB = new float[npolygons];
    float[] NyA = new float[npolygons];
    float[] NyB = new float[npolygons];
    float[] NzA = new float[npolygons];
    float[] NzB = new float[npolygons];

    float[] Pnx = new float[npolygons];
    float[] Pny = new float[npolygons];
    float[] Pnz = new float[npolygons];

    float[] NX = new float[nvertex];
    float[] NY = new float[nvertex];
    float[] NZ = new float[nvertex];

    make_normals( fieldVertices[0], fieldVertices[1],  fieldVertices[2],
                  NX, NY, NZ, nvertex, npolygons, Pnx, Pny, Pnz,
                  NxA, NxB, NyA, NyB, NzA, NzB, Pol_f_Vert, Vert_f_Pol);

    // take the garbage out
    NxA = NxB = NyA = NyB = NzA = NzB = Pnx = Pny = Pnz = null;

    float[] normals = new float[3 * nvertex];
    int j = 0;
    for (i=0; i<nvertex; i++) {
      normals[j++] = (float) NX[i];
      normals[j++] = (float) NY[i];
      normals[j++] = (float) NZ[i];
    }
    // take the garbage out
    NX = NY = NZ = null;

    /* ----- Find PolyTriangle Stripe */
    // temporary array to hold maximum possible polytriangle strip
    int[] stripe = new int[6 * npolygons];
    int[] vet_pol = new int[npolygons];
    size_stripe = poly_triangle_stripe( vet_pol, stripe, nvertex,
                                        npolygons, Pol_f_Vert, Vert_f_Pol );

    // take the garbage out
    Pol_f_Vert = null;
    Vert_f_Pol = null;

    if (indexed) {
      VisADIndexedTriangleStripArray array =
        new VisADIndexedTriangleStripArray();

      // set up indices
      array.indexCount = size_stripe;
      array.indices = new int[size_stripe];
      System.arraycopy(stripe, 0, array.indices, 0, size_stripe);
      array.stripVertexCounts = new int[1];
      array.stripVertexCounts[0] = size_stripe;
      // take the garbage out
      stripe = null;

      // set coordinates and colors
      setGeometryArray(array, fieldVertices, 4, color_levels);
      // take the garbage out
      fieldVertices = null;
      color_levels = null;

      // array.vertexFormat |= NORMALS;
      array.normals = normals;

      if (debug) {
        System.out.println("size_stripe= "+size_stripe);
        for(ii=0;ii<size_stripe;ii++) System.out.println(+array.indices[ii]);
      }
      return array;
    }
    else { // if (!indexed)
      VisADTriangleStripArray array = new VisADTriangleStripArray();
      array.stripVertexCounts = new int[] {size_stripe};
      array.vertexCount = size_stripe;

      array.normals = new float[3 * size_stripe];
      int k = 0;
      for (i=0; i<3*size_stripe; i+=3) {
        j = 3 * stripe[k];
        array.normals[i] = normals[j];
        array.normals[i+1] = normals[j+1];
        array.normals[i+2] = normals[j+2];
        k++;
      }
      normals = null;

      array.coordinates = new float[3 * size_stripe];
      k = 0;
      for (i=0; i<3*size_stripe; i+=3) {
        j = stripe[k];
        array.coordinates[i] = fieldVertices[0][j];
        array.coordinates[i+1] = fieldVertices[1][j];
        array.coordinates[i+2] = fieldVertices[2][j];
        k++;
      }
      fieldVertices = null;

      if (color_levels != null) {
        int color_length = color_levels.length;
        array.colors = new byte[color_length * size_stripe];
        k = 0;
        if (color_length == 4) {
          for (i=0; i<color_length*size_stripe; i+=color_length) {
            j = stripe[k];
            array.colors[i] = color_levels[0][j];
            array.colors[i+1] = color_levels[1][j];
            array.colors[i+2] = color_levels[2][j];
            array.colors[i+3] = color_levels[3][j];
            k++;
          }
        }
        else { // if (color_length == 3)
          for (i=0; i<color_length*size_stripe; i+=color_length) {
            j = stripe[k];
            array.colors[i] = color_levels[0][j];
            array.colors[i+1] = color_levels[1][j];
            array.colors[i+2] = color_levels[2][j];
            k++;
          }
        }
      }
      color_levels = null;
      stripe = null;
      return array;
    } // end if (!indexed)
  }

  /* almost identical with Gridded3DSet.isosurf, except:
     1. sos argument
     2. compute xo, xs, yo, ys, zo, zs, and using them instead of samples
     3. not using samples array
  */
  private int linear_isosurf( float isovalue, int[] ptFLAG, int nvertex_estimate,
                              int npolygons, float[] ptGRID, int xdim, int ydim,
                              int zdim, float[][] VX, float[][] VY, float[][] VZ,
                              byte[][] auxValues, byte[][] auxLevels,
                              int[][] Pol_f_Vert, int[] Vert_f_Pol, float[] sos )
          throws VisADException {

    int  ix, iy, iz, caseA, above, bellow, front, rear, mm, nn;
    int  ii, jj, kk, ncube, cpl, pvp, pa, ve;
    int[] calc_edge = new int[13];
    int  xx, yy, zz;
    float    cp;
    float  vnode0 = 0;
    float  vnode1 = 0;
    float  vnode2 = 0;
    float  vnode3 = 0;
    float  vnode4 = 0;
    float  vnode5 = 0;
    float  vnode6 = 0;
    float  vnode7 = 0;
    int  pt = 0;
    int  n_pol;
    int  aa;
    int  bb;
    int  temp;
    float  nodeDiff;
    int xdim_x_ydim = xdim*ydim;
    int nvet;

    int t;

    float[][]mySamples = getMySamples();
    // use these instead of samples
    float xo = sos[0];
    float xs = sos[1];
    float yo = sos[2];
    float ys = sos[3];
    float zo = sos[4];
    float zs = sos[5];
    // don't use samples
    // float[][] samples = getSamples(false);

    int naux = (auxValues != null) ? auxValues.length : 0;
    if (naux > 0) {
      if (auxLevels == null || auxLevels.length != naux) {
        throw new SetException("Linear3DSet.isosurf: "
                              +"auxLevels length " + auxLevels.length +
                               " doesn't match expected " + naux);
      }
      for (int i=0; i<naux; i++) {
        if (auxValues[i].length != Length) {
          throw new SetException("Linear3DSet.isosurf: expected auxValues " +
                                " length#" + i + " to be " + Length +
                                 ", not " + auxValues[i].length);
        }
      }
    }
    else {
      if (auxLevels != null) {
        throw new SetException("Linear3DSet.isosurf: "
                              +"auxValues null but auxLevels not null");
      }
    }

    // temporary storage of auxLevels structure
    byte[][] tempaux = (naux > 0) ? new byte[naux][nvertex_estimate] : null;

    bellow = rear = 0;  above = front = 1;

    /* Initialize the Auxiliar Arrays of Pointers */
/* WLH 25 Oct 97
    ix = 9 * (npolygons*2 + 50);
    iy = 7 * npolygons;
    ii = ix + iy;
*/
    for (jj=0; jj<Pol_f_Vert[0].length; jj++) {
      Pol_f_Vert[0][jj] = BIG_NEG;
    }
    for (jj=8; jj<Pol_f_Vert[0].length; jj+=9) {
      Pol_f_Vert[0][jj] = 0;
    }
    for (jj=0; jj<Vert_f_Pol.length; jj++) {
      Vert_f_Pol[jj] = BIG_NEG;
    }
    for (jj=6; jj<Vert_f_Pol.length; jj+=7) {
      Vert_f_Pol[jj] = 0;
    }

    /* Allocate the auxiliar edge vectors
    size ixPlane = (xdim - 1) * ydim = xdim_x_ydim - ydim
    size iyPlane = (ydim - 1) * xdim = xdim_x_ydim - xdim
    size izPlane = xdim
    */

    xx = xdim_x_ydim - ydim;
    yy = xdim_x_ydim - xdim;
    zz = ydim;
    ii = 2 * (xx + yy + zz);

    int[] P_array = new int[ii];

    /* Calculate the Vertex of the Polygons which edges were
       calculated above */
    nvet = ncube = cpl = pvp = 0;


        for ( iz = 0; iz < zdim - 1; iz++ ) {

            for ( ix = 0; ix < xdim - 1; ix++ ) {

                for ( iy = 0; iy < ydim - 1; iy++ ) {
                    if ( (ptFLAG[ncube] != 0 & ptFLAG[ncube] != 0xFF) ) {

                        if (nvet + 12 > nvertex_estimate) {
                          // allocate more space
                          nvertex_estimate = 2 * (nvet + 12);
                          if (naux > 0) {
                            for (int i=0; i<naux; i++) {
                              byte[] tt = tempaux[i];
                              tempaux[i] = new byte[nvertex_estimate];
                              System.arraycopy(tt, 0, tempaux[i], 0, nvet);
                            }
                          }
                          float[] tt = VX[0];
                          VX[0] = new float[nvertex_estimate];
                          System.arraycopy(tt, 0, VX[0], 0, tt.length);
                          tt = VY[0];
                          VY[0] = new float[nvertex_estimate];
                          System.arraycopy(tt, 0, VY[0], 0, tt.length);
                          tt = VZ[0];
                          VZ[0] = new float[nvertex_estimate];
                          System.arraycopy(tt, 0, VZ[0], 0, tt.length);
                          int big_ix = 9 * (nvertex_estimate + 50);
                          int[] it = Pol_f_Vert[0];
                          Pol_f_Vert[0] = new int[big_ix];
                          for (jj=0; jj<Pol_f_Vert[0].length; jj++) {
                            Pol_f_Vert[0][jj] = BIG_NEG;
                          }
                          for (jj=8; jj<Pol_f_Vert[0].length; jj+=9) {
                            Pol_f_Vert[0][jj] = 0;
                          }
                          System.arraycopy(it, 0, Pol_f_Vert[0], 0, it.length);
                        }

           /* WLH 2 April 99 */
           vnode0 = ptGRID[pt];
           vnode1 = ptGRID[pt + ydim];
           vnode2 = ptGRID[pt + 1];
           vnode3 = ptGRID[pt + ydim + 1];
           vnode4 = ptGRID[pt + xdim_x_ydim];
           vnode5 = ptGRID[pt + ydim + xdim_x_ydim];
           vnode6 = ptGRID[pt + 1 + xdim_x_ydim];
           vnode7 = ptGRID[pt + 1 + ydim + xdim_x_ydim];

                        if ( (ptFLAG[ncube] < MAX_FLAG_NUM) ) {
                        /*  fill_Vert_f_Pol(ncube); */

                                  kk  = pol_edges[ptFLAG[ncube]][2];
                                  aa = ptFLAG[ncube];
                                  bb = 4;
                                  pa  = pvp;
                                  n_pol = pol_edges[ptFLAG[ncube]][1];
                                  for (ii=0; ii < n_pol; ii++) {
                                      Vert_f_Pol[pa+6] = ve = kk&MASK;
                                      ve+=pa;
                                      for (jj=pa; jj<ve && jj<pa+6; jj++) {

                                            Vert_f_Pol[jj] = pol_edges[aa][bb];
                                            bb++;
                                            if (bb >= 16) {
                                                aa++;
                                                bb -= 16;
                                            }
                                      }
                                           kk >>= 4;    pa += 7;
                                  }
                        /* end  fill_Vert_f_Pol(ncube); */
                        /* */

         /* find_vertex(); */
/* WLH 2 April 99
           vnode0 = ptGRID[pt];
           vnode1 = ptGRID[pt + ydim];
           vnode2 = ptGRID[pt + 1];
           vnode3 = ptGRID[pt + ydim + 1];
           vnode4 = ptGRID[pt + xdim_x_ydim];
           vnode5 = ptGRID[pt + ydim + xdim_x_ydim];
           vnode6 = ptGRID[pt + 1 + xdim_x_ydim];
           vnode7 = ptGRID[pt + 1 + ydim + xdim_x_ydim];
*/


   if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0002) != 0) ) {  /* cube vertex 0-1 */
         if ( (iz != 0) || (iy != 0) ) {
           calc_edge[1] = P_array[ bellow*xx + ix*ydim + iy ];
         }
         else {
             cp = ( ( isovalue - vnode0 ) / ( vnode1 - vnode0 ) );
             VX[0][nvet] = xo + xs * (cp + ix);
             VY[0][nvet] = yo + ys * iy;
             VZ[0][nvet] = zo + zs * iz;
/*
             VX[0][nvet] = (float) cp * samples[0][pt + ydim] + (1.0f-cp) * samples[0][pt];
             VY[0][nvet] = (float) cp * samples[1][pt + ydim] + (1.0f-cp) * samples[1][pt];
             VZ[0][nvet] = (float) cp * samples[2][pt + ydim] + (1.0f-cp) * samples[2][pt];
*/

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + ydim] < 0) ?
                     ((float) auxValues[j][pt + ydim]) + 256.0f :
                     ((float) auxValues[j][pt + ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt] < 0) ?
                     ((float) auxValues[j][pt]) + 256.0f :
                     ((float) auxValues[j][pt]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim] +
                                  (1.0f-cp) * auxValues[j][pt];
*/
             }

             calc_edge[1] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0004) != 0) ) {  /* cube vertex 0-2 */
         if ( (iz != 0) || (ix != 0) ) {
           calc_edge[2] = P_array[ 2*xx + bellow*yy + iy*xdim + ix ];
         }
         else {
             cp = ( ( isovalue - vnode0 ) / ( vnode2 - vnode0 ) );
             VX[0][nvet] = xo + xs * ix;
             VY[0][nvet] = yo + ys * (cp + iy);
             VZ[0][nvet] = zo + zs * iz;
/*
             VX[0][nvet] = (float) cp * samples[0][pt + 1] + (1.0f-cp) * samples[0][pt];
             VY[0][nvet] = (float) cp * samples[1][pt + 1] + (1.0f-cp) * samples[1][pt];
             VZ[0][nvet] = (float) cp * samples[2][pt + 1] + (1.0f-cp) * samples[2][pt];
*/

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + 1] < 0) ?
                     ((float) auxValues[j][pt + 1]) + 256.0f :
                     ((float) auxValues[j][pt + 1]) ) +
                   (1.0f - cp) * ((auxValues[j][pt] < 0) ?
                     ((float) auxValues[j][pt]) + 256.0f :
                     ((float) auxValues[j][pt]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1] +
                                  (1.0f-cp) * auxValues[j][pt];
*/
             }

             calc_edge[2] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0008) != 0) ) {  /* cube vertex 0-4 */
         if ( (ix != 0) || (iy != 0) ) {
           calc_edge[3] = P_array[ 2*xx + 2*yy + rear*zz + iy ];
         }
         else {
             cp = ( ( isovalue - vnode0 ) / ( vnode4 - vnode0 ) );
             VX[0][nvet] = xo + xs * ix;
             VY[0][nvet] = yo + ys * iy;
             VZ[0][nvet] = zo + zs * (cp + iz);
/*
             VX[0][nvet] = (float) cp * samples[0][pt + xdim_x_ydim] +
                        (1.0f-cp) * samples[0][pt];
             VY[0][nvet] = (float) cp * samples[1][pt + xdim_x_ydim] +
                        (1.0f-cp) * samples[1][pt];
             VZ[0][nvet] = (float) cp * samples[2][pt + xdim_x_ydim] +
                        (1.0f-cp) * samples[2][pt];
*/

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + xdim_x_ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt] < 0) ?
                     ((float) auxValues[j][pt]) + 256.0f :
                     ((float) auxValues[j][pt]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + xdim_x_ydim] +
                                  (1.0f-cp) * auxValues[j][pt];
*/
             }

             calc_edge[3] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0010) != 0) ) {  /* cube vertex 1-3 */
         if ( (iz != 0) ) {
           calc_edge[4] =  P_array[ 2*xx + bellow*yy + iy*xdim + (ix+1) ];
         }
         else {
             cp = ( ( isovalue - vnode1 ) / ( vnode3 - vnode1 ) );
             VX[0][nvet] = xo + xs * (ix+1);
             VY[0][nvet] = yo + ys * (cp + iy);
             VZ[0][nvet] = zo + zs * iz;
/*
             VX[0][nvet] = (float) cp * samples[0][pt + ydim + 1] +
                        (1.0f-cp) * samples[0][pt + ydim];
             VY[0][nvet] = (float) cp * samples[1][pt + ydim + 1] +
                        (1.0f-cp) * samples[1][pt + ydim];
             VZ[0][nvet] = (float) cp * samples[2][pt + ydim + 1] +
                        (1.0f-cp) * samples[2][pt + ydim];
*/

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + ydim + 1] < 0) ?
                     ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                     ((float) auxValues[j][pt + ydim + 1]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + ydim] < 0) ?
                     ((float) auxValues[j][pt + ydim]) + 256.0f :
                     ((float) auxValues[j][pt + ydim]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + 1] +
                                  (1.0f-cp) * auxValues[j][pt + ydim];
*/
             }

             calc_edge[4] = nvet;
             P_array[ 2*xx + bellow*yy + iy*xdim + (ix+1) ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0020) != 0) ) {  /* cube vertex 1-5 */
         if ( (iy != 0) ) {
           calc_edge[5] = P_array[ 2*xx + 2*yy + front*zz + iy ];
         }
         else {
             cp = ( ( isovalue - vnode1 ) / ( vnode5 - vnode1 ) );
             VX[0][nvet] = xo + xs * (ix+1);
             VY[0][nvet] = yo + ys * iy;
             VZ[0][nvet] = zo + zs * (cp + iz);
/*
             VX[0][nvet] = (float) cp * samples[0][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[0][pt + ydim];
             VY[0][nvet] = (float) cp * samples[1][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[1][pt + ydim];
             VZ[0][nvet] = (float) cp * samples[2][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[2][pt + ydim];
*/

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + ydim] < 0) ?
                     ((float) auxValues[j][pt + ydim]) + 256.0f :
                     ((float) auxValues[j][pt + ydim]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + xdim_x_ydim] +
                                  (1.0f-cp) * auxValues[j][pt + ydim];
*/
             }

             calc_edge[5] = nvet;
             P_array[ 2*xx + 2*yy + front*zz + iy ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0040) != 0) ) {  /* cube vertex 2-3 */
         if ( (iz != 0) ) {
           calc_edge[6] = P_array[ bellow*xx + ix*ydim + (iy+1) ];
         }
         else {
             cp = ( ( isovalue - vnode2 ) / ( vnode3 - vnode2 ) );
             VX[0][nvet] = xo + xs * (cp + ix);
             VY[0][nvet] = yo + ys * (iy+1);
             VZ[0][nvet] = zo + zs * iz;
/*
             VX[0][nvet] = (float) cp * samples[0][pt + ydim + 1] +
                        (1.0f-cp) * samples[0][pt + 1];
             VY[0][nvet] = (float) cp * samples[1][pt + ydim + 1] +
                        (1.0f-cp) * samples[1][pt + 1];
             VZ[0][nvet] = (float) cp * samples[2][pt + ydim + 1] +
                        (1.0f-cp) * samples[2][pt + 1];
*/

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + ydim + 1] < 0) ?
                     ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                     ((float) auxValues[j][pt + ydim + 1]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + 1] < 0) ?
                     ((float) auxValues[j][pt + 1]) + 256.0f :
                     ((float) auxValues[j][pt + 1]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + 1] +
                                  (1.0f-cp) * auxValues[j][pt + 1];
*/
             }

             calc_edge[6] = nvet;
             P_array[ bellow*xx + ix*ydim + (iy+1) ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0080) != 0) ) {  /* cube vertex 2-6 */
         if ( (ix != 0) ) {
           calc_edge[7] = P_array[ 2*xx + 2*yy + rear*zz + (iy+1) ];
         }
         else {
             cp = ( ( isovalue - vnode2 ) / ( vnode6 - vnode2 ) );
             VX[0][nvet] = xo + xs * ix;
             VY[0][nvet] = yo + ys * (iy+1);
             VZ[0][nvet] = zo + zs * (cp + iz);
/*
             VX[0][nvet] = (float) cp * samples[0][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[0][pt + 1];
             VY[0][nvet] = (float) cp * samples[1][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[1][pt + 1];
             VZ[0][nvet] = (float) cp * samples[2][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[2][pt + 1];
*/

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + 1] < 0) ?
                     ((float) auxValues[j][pt + 1]) + 256.0f :
                     ((float) auxValues[j][pt + 1]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + xdim_x_ydim] +
                                  (1.0f-cp) * auxValues[j][pt + 1];
*/
             }

             calc_edge[7] = nvet;
             P_array[ 2*xx + 2*yy + rear*zz + (iy+1) ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0100) != 0) ) {  /* cube vertex 3-7 */
         cp = ( ( isovalue - vnode3 ) / ( vnode7 - vnode3 ) );
         VX[0][nvet] = xo + xs * (ix+1);
         VY[0][nvet] = yo + ys * (iy+1);
         VZ[0][nvet] = zo + zs * (cp + iz);
/*
         VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[0][pt + ydim + 1];
         VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[1][pt + ydim + 1];
         VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[2][pt + ydim + 1];
*/

         for (int j=0; j<naux; j++) {
           t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
               (1.0f - cp) * ((auxValues[j][pt + ydim + 1] < 0) ?
                 ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                 ((float) auxValues[j][pt + ydim + 1]) ) );
           tempaux[j][nvet] = (byte)
             ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
           tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                              (1.0f-cp) * auxValues[j][pt + ydim + 1];
*/
         }

         calc_edge[8] = nvet;
         P_array[ 2*xx + 2*yy + front*zz + (iy+1) ] = nvet;
         nvet++;
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0200) != 0) ) {  /* cube vertex 4-5 */
         if ( (iy != 0) ) {
           calc_edge[9] = P_array[ above*xx + ix*ydim + iy ];
         }
         else {
             cp = ( ( isovalue - vnode4 ) / ( vnode5 - vnode4 ) );
             VX[0][nvet] = xo + xs * (cp + ix);
             VY[0][nvet] = yo + ys * iy;
             VZ[0][nvet] = zo + zs * (iz+1);
/*
             VX[0][nvet] = (float) cp * samples[0][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[0][pt + xdim_x_ydim];
             VY[0][nvet] = (float) cp * samples[1][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[1][pt + xdim_x_ydim];
             VZ[0][nvet] = (float) cp * samples[2][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[2][pt + xdim_x_ydim];
*/

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + xdim_x_ydim]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + xdim_x_ydim] +
                                  (1.0f-cp) * auxValues[j][pt + xdim_x_ydim];
*/
             }

             calc_edge[9] = nvet;
             P_array[ above*xx + ix*ydim + iy ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0400) != 0) ) {  /* cube vertex 4-6 */
         if ( (ix != 0) ) {
           calc_edge[10] = P_array[ 2*xx + above*yy + iy*xdim + ix ];
         }
         else {
             cp = ( ( isovalue - vnode4 ) / ( vnode6 - vnode4 ) );
             VX[0][nvet] = xo + xs * ix;
             VY[0][nvet] = yo + ys * (cp + iy);
             VZ[0][nvet] = zo + zs * (iz+1);
/*
             VX[0][nvet] = (float) cp * samples[0][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[0][pt + xdim_x_ydim];
             VY[0][nvet] = (float) cp * samples[1][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[1][pt + xdim_x_ydim];
             VZ[0][nvet] = (float) cp * samples[2][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[2][pt + xdim_x_ydim];
*/

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + xdim_x_ydim]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + xdim_x_ydim] +
                                  (1.0f-cp) * auxValues[j][pt + xdim_x_ydim];
*/
             }

             calc_edge[10] = nvet;
             P_array[ 2*xx + above*yy + iy*xdim + ix ] = nvet;
             nvet++;
         }
     }
    if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0800) != 0) ) {  /* cube vertex 5-7 */
         cp = ( ( isovalue - vnode5 ) / ( vnode7 - vnode5 ) );
         VX[0][nvet] = xo + xs * (ix+1);
         VY[0][nvet] = yo + ys * (cp + iy);
         VZ[0][nvet] = zo + zs * (iz+1);
/*
         VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[0][pt + ydim + xdim_x_ydim];
         VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[1][pt + ydim + xdim_x_ydim];
         VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[2][pt + ydim + xdim_x_ydim];
*/

         for (int j=0; j<naux; j++) {
           t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
               (1.0f - cp) * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                 ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                 ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) );
           tempaux[j][nvet] = (byte)
             ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
           tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                              (1.0f-cp) * auxValues[j][pt + ydim + xdim_x_ydim];
*/
         }

         calc_edge[11] = nvet;
         P_array[ 2*xx + above*yy + iy*xdim + (ix+1) ] = nvet;
         nvet++;
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x1000) != 0) ) {  /* cube vertex 6-7 */
         cp = ( ( isovalue - vnode6 ) / ( vnode7 - vnode6 ) );
         VX[0][nvet] = xo + xs * (cp + ix);
         VY[0][nvet] = yo + ys * (iy+1);
         VZ[0][nvet] = zo + zs * (iz+1);
/*
         VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[0][pt + 1 + xdim_x_ydim];
         VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[1][pt + 1 + xdim_x_ydim];
         VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[2][pt + 1 + xdim_x_ydim];
*/

         for (int j=0; j<naux; j++) {
           t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
               (1.0f - cp) * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                 ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                 ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) );
           tempaux[j][nvet] = (byte)
             ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
           tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                              (1.0f-cp) * auxValues[j][pt + 1 + xdim_x_ydim];
*/
         }

         calc_edge[12] = nvet;
         P_array[ above*xx + ix*ydim + (iy+1) ] = nvet;
         nvet++;
     }

         /* end  find_vertex(); */
                         /* update_data_structure(ncube); */
                             kk = pol_edges[ptFLAG[ncube]][2];
                             nn = pol_edges[ptFLAG[ncube]][1];
                             for (ii=0; ii<nn; ii++) {
                                  mm = pvp+(kk&MASK);
                                  for (jj=pvp; jj<mm; jj++) {
                                      Vert_f_Pol [jj] = ve = calc_edge[Vert_f_Pol [jj]];
                            //        Pol_f_Vert[0][ve*9 + (Pol_f_Vert[0][ve*9 + 8])++]  = cpl;
                                      temp = Pol_f_Vert[0][ve*9 + 8];
                                      Pol_f_Vert[0][ve*9 + temp] = cpl;
                                      Pol_f_Vert[0][ve*9 + 8] = temp + 1;
                                  }
                                  kk >>= 4;    pvp += 7;    cpl++;
                             }
                         /* end  update_data_structure(ncube); */
                        }
                        else { // !(ptFLAG[ncube] < MAX_FLAG_NUM)
       /* find_vertex_invalid_cube(ncube); */

    ptFLAG[ncube] &= 0x1FF;
    if ( (ptFLAG[ncube] != 0 & ptFLAG[ncube] != 0xFF) )
    { if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0010) != 0) )     /* cube vertex 1-3 */
/* WLH 24 Oct 97
      {   if (!(iz != 0 ) && vnode3 < INV_VAL && vnode1 < INV_VAL)
      {   if (!(iz != 0 ) && !Float.isNaN(vnode3) && !Float.isNaN(vnode1))
*/
      // test for not missing
      {   if (!(iz != 0 ) && vnode3 == vnode3 && vnode1 == vnode1)
        {
              cp = ( ( isovalue - vnode1 ) / ( vnode3 - vnode1 ) );
              VX[0][nvet] = xo + xs * (ix+1);
              VY[0][nvet] = yo + ys * (cp + iy);
              VZ[0][nvet] = zo + zs * iz;
/*
              VX[0][nvet] = (float) cp * mySamples[0][pt + ydim + 1] +
                         (1.0f-cp) * mySamples[0][pt + ydim];
              VY[0][nvet] = (float) cp * mySamples[1][pt + ydim + 1] +
                         (1.0f-cp) * mySamples[1][pt + ydim];
              VZ[0][nvet] = (float) cp * mySamples[2][pt + ydim + 1] +
                         (1.0f-cp) * mySamples[2][pt + ydim];
*/

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + ydim + 1] < 0) ?
                      ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + 1]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + ydim] < 0) ?
                      ((float) auxValues[j][pt + ydim]) + 256.0f :
                      ((float) auxValues[j][pt + ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + 1] +
                                   (1.0f-cp) * auxValues[j][pt + ydim];
*/
              }

              P_array[ 2*xx + bellow*yy + iy*xdim + (ix+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0020) != 0) )    /* cube vertex 1-5 */
/* WLH 24 Oct 97
      {   if (!(iy != 0) && vnode5 < INV_VAL && vnode1 < INV_VAL)
      {   if (!(iy != 0) && !Float.isNaN(vnode5) && !Float.isNaN(vnode1))
*/
      // test for not missing
      {   if (!(iy != 0) && vnode5 == vnode5 && vnode1 == vnode1)
        {
              cp = ( ( isovalue - vnode1 ) / ( vnode5 - vnode1 ) );
              VX[0][nvet] = xo + xs * (ix+1);
              VY[0][nvet] = yo + ys * iy;
              VZ[0][nvet] = zo + zs * (cp + iz);
/*
              VX[0][nvet] = (float) cp * samples[0][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + ydim];
              VY[0][nvet] = (float) cp * samples[1][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + ydim];
              VZ[0][nvet] = (float) cp * samples[2][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + ydim];
*/

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + ydim] < 0) ?
                      ((float) auxValues[j][pt + ydim]) + 256.0f :
                      ((float) auxValues[j][pt + ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + ydim];
*/
              }

              P_array[ 2*xx + 2*yy + front*zz + iy ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0040) != 0) )     /* cube vertex 2-3 */
/* WLH 24 Oct 97
      {   if (!(iz != 0) && vnode3 < INV_VAL && vnode2 < INV_VAL)
      {   if (!(iz != 0) && !Float.isNaN(vnode3) && !Float.isNaN(vnode2))
*/
      // test for not missing
      {   if (!(iz != 0) && vnode3 == vnode3 && vnode2 == vnode2)
        {
              cp = ( ( isovalue - vnode2 ) / ( vnode3 - vnode2 ) );
              VX[0][nvet] = xo + xs * (cp + ix);
              VY[0][nvet] = yo + ys * (iy+1);
              VZ[0][nvet] = zo + zs * iz;
/*
              VX[0][nvet] = (float) cp * samples[0][pt + ydim + 1] +
                         (1.0f-cp) * samples[0][pt + 1];
              VY[0][nvet] = (float) cp * samples[1][pt + ydim + 1] +
                         (1.0f-cp) * samples[1][pt + 1];
              VZ[0][nvet] = (float) cp * samples[2][pt + ydim + 1] +
                         (1.0f-cp) * samples[2][pt + 1];
*/

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + ydim + 1] < 0) ?
                      ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + 1]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + 1] < 0) ?
                      ((float) auxValues[j][pt + 1]) + 256.0f :
                      ((float) auxValues[j][pt + 1]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + 1] +
                                   (1.0f-cp) * auxValues[j][pt + 1];
*/
              }

              P_array[ bellow*xx + ix*ydim + (iy+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0080) != 0) )  /* cube vertex 2-6 */
/* WLH 24 Oct 97
      {   if (!(ix != 0) && vnode6 < INV_VAL && vnode2 < INV_VAL)
      {   if (!(ix != 0) && !Float.isNaN(vnode6) && !Float.isNaN(vnode2))
*/
      // test for not missing
      {   if (!(ix != 0) && vnode6 == vnode6 && vnode2 == vnode2)
        {
              cp = ( ( isovalue - vnode2 ) / ( vnode6 - vnode2 ) );
              VX[0][nvet] = xo + xs * ix;
              VY[0][nvet] = yo + ys * (iy+1);
              VZ[0][nvet] = zo + zs * (cp + iz);
/*
              VX[0][nvet] = (float) cp * samples[0][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + 1];
              VY[0][nvet] = (float) cp * samples[1][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + 1];
              VZ[0][nvet] = (float) cp * samples[2][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + 1];
*/

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + 1] < 0) ?
                      ((float) auxValues[j][pt + 1]) + 256.0f :
                      ((float) auxValues[j][pt + 1]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + 1];
*/
              }

              P_array[ 2*xx + 2*yy + rear*zz + (iy+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0100) != 0) )     /* cube vertex 3-7 */
/* WLH 24 Oct 97
      {   if (vnode7 < INV_VAL && vnode3 < INV_VAL)
      {   if (!Float.isNaN(vnode7) && !Float.isNaN(vnode3))
*/
      // test for not missing
      {   if (vnode7 == vnode7 && vnode3 == vnode3)
          {
              cp = ( ( isovalue - vnode3 ) / ( vnode7 - vnode3 ) );
              VX[0][nvet] = xo + xs * (ix+1);
              VY[0][nvet] = yo + ys * (iy+1);
              VZ[0][nvet] = zo + zs * (cp + iz);
/*
              VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + ydim + 1];
              VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + ydim + 1];
              VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + ydim + 1];
*/

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + ydim + 1] < 0) ?
                      ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + 1]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + ydim + 1];
*/
              }

              P_array[ 2*xx + 2*yy + front*zz + (iy+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0200) != 0) )    /* cube vertex 4-5 */
/* WLH 24 Oct 97
      {   if (!(iy != 0) && vnode5 < INV_VAL && vnode4 < INV_VAL)
      {   if (!(iy != 0) && !Float.isNaN(vnode5) && !Float.isNaN(vnode4))
*/
      // test for not missing
      {   if (!(iy != 0) && vnode5 == vnode5 && vnode4 == vnode4)
        {
              cp = ( ( isovalue - vnode4 ) / ( vnode5 - vnode4 ) );
              VX[0][nvet] = xo + xs * (cp + ix);
              VY[0][nvet] = yo + ys * iy;
              VZ[0][nvet] = zo + zs * (iz+1);
/*
              VX[0][nvet] = (float) cp * samples[0][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + xdim_x_ydim];
              VY[0][nvet] = (float) cp * samples[1][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + xdim_x_ydim];
              VZ[0][nvet] = (float) cp * samples[2][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + xdim_x_ydim];
*/

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + xdim_x_ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + xdim_x_ydim];
*/
              }

              P_array[ above*xx + ix*ydim + iy ] = nvet;
              nvet++;
          }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0400) != 0) )     /* cube vertex 4-6 */
/* WLH 24 Oct 97
      {   if (!(ix != 0) && vnode6 < INV_VAL && vnode4 < INV_VAL)
      {   if (!(ix != 0) && !Float.isNaN(vnode6) && !Float.isNaN(vnode4))
*/
      // test for not missing
      {   if (!(ix != 0) && vnode6 == vnode6 && vnode4 == vnode4)
          {
              cp = ( ( isovalue - vnode4 ) / ( vnode6 - vnode4 ) );
              VX[0][nvet] = xo + xs * ix;
              VY[0][nvet] = yo + ys * (cp + iy);
              VZ[0][nvet] = zo + zs * (iz+1);
/*
              VX[0][nvet] = (float) cp * samples[0][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + xdim_x_ydim];
              VY[0][nvet] = (float) cp * samples[1][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + xdim_x_ydim];
              VZ[0][nvet] = (float) cp * samples[2][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + xdim_x_ydim];
*/

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + xdim_x_ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + xdim_x_ydim];
*/
              }

              P_array[ 2*xx + above*yy + iy*xdim + ix ] = nvet;
              nvet++;
          }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0800) != 0) )     /* cube vertex 5-7 */
/* WLH 24 Oct 97
      {   if (vnode7 < INV_VAL && vnode5 < INV_VAL)
      {   if (!Float.isNaN(vnode7) && !Float.isNaN(vnode5))
*/
      // test for not missing
      {   if (vnode7 == vnode7 && vnode5 == vnode5)
        {
              cp = ( ( isovalue - vnode5 ) / ( vnode7 - vnode5 ) );
              VX[0][nvet] = xo + xs * (ix+1);
              VY[0][nvet] = yo + ys * (cp + iy);
              VZ[0][nvet] = zo + zs * (iz+1);
/*
              VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + ydim + xdim_x_ydim];
              VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + ydim + xdim_x_ydim];
              VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + ydim + xdim_x_ydim];
*/

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + ydim + xdim_x_ydim];
*/
              }

              P_array[ 2*xx + above*yy + iy*xdim + (ix+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x1000) != 0) )     /* cube vertex 6-7 */
/* WLH 24 Oct 97
      {   if (vnode7 < INV_VAL && vnode6 < INV_VAL)
      {   if (!Float.isNaN(vnode7) && !Float.isNaN(vnode6))
*/
      // test for not missing
      {   if (vnode7 == vnode7 && vnode6 == vnode6)
        {
              cp = ( ( isovalue - vnode6 ) / ( vnode7 - vnode6 ) );
              VX[0][nvet] = xo + xs * (cp + ix);
              VY[0][nvet] = yo + ys * (iy+1);
              VZ[0][nvet] = zo + zs * (iz+1);
/*
              VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + 1 + xdim_x_ydim];
              VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + 1 + xdim_x_ydim];
              VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + 1 + xdim_x_ydim];
*/

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + 1 + xdim_x_ydim];
*/
              }

              P_array[ above*xx + ix*ydim + (iy+1) ] = nvet;
              nvet++;
        }
      }
     }
        /* end  find_vertex_invalid_cube(ncube); */

                        }
                    } /* end  if (exist_polygon_in_cube(ncube)) */
                    ncube++; pt++;
                } /* end  for ( iy = 0; iy < ydim - 1; iy++ ) */
             /* swap_planes(Z,rear,front); */
                caseA = rear;
                rear = front;
                front = caseA;
                pt++;
             /* end  swap_planes(Z,rear,front); */
            } /* end  for ( ix = 0; ix < xdim - 1; ix++ ) */
           /*  swap_planes(XY,bellow,above); */
               caseA = bellow;
               bellow = above;
               above = caseA;
            pt += ydim;
           /* end  swap_planes(XY,bellow,above); */
        } /* end  for ( iz = 0; iz < zdim - 1; iz++ ) */

    // copy tempaux array into auxLevels array
    for (int i=0; i<naux; i++) {
      auxLevels[i] = new byte[nvet];
      System.arraycopy(tempaux[i], 0, auxLevels[i], 0, nvet);
    }

    return nvet;
  }


  /**
   * Check to see if this Linear3DSet is equal to the Object
   * in question.
   * @param  set  Object in question
   * @return true if <code>set</code> is a Linear3DSet and each
   *         of the Linear1DSet-s that make up this cross product
   *         are equal.
   */
  public boolean equals(Object set) {
    if (!(set instanceof Linear3DSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    return (X.equals(((Linear3DSet) set).getX()) &&
            Y.equals(((Linear3DSet) set).getY()) &&
            Z.equals(((Linear3DSet) set).getZ()));
  }

  /**
   * Returns the hash code for this instance.
   * @return                    The hash code for this instance.
   */
  public int hashCode()
  {
    if (!hashCodeSet)
    {
      hashCode =
        unitAndCSHashCode() ^ X.hashCode() ^ Y.hashCode() ^ Z.hashCode();
      hashCodeSet = true;
    }
    return hashCode;
  }

  /**
   * Get the indexed component (X is at 0, Y is at 1, and Z is at 2)
   *
   * @param i Index of component
   * @return The requested component
   * @exception ArrayIndexOutOfBoundsException If an invalid index is
   *                                           specified.
   */
  public Linear1DSet getLinear1DComponent(int i) {
    if (i == 0) return getX();
    else if (i == 1) return getY();
    else if (i == 2) return getZ();
    else throw new ArrayIndexOutOfBoundsException("Invalid component index");
  }

  /**
   * Return a clone of this object with a new MathType.
   * @param  type  new MathType.
   * @return  new Linear3DSet with <code>type</code>.
   * @throws VisADException  if <code>type</code> is not compatible
   *                         with MathType of component Linear1DSets.
   */
  public Object cloneButType(MathType type) throws VisADException {
    Linear1DSet[] sets = {(Linear1DSet) X.clone(),
                          (Linear1DSet) Y.clone(),
                          (Linear1DSet) Z.clone()};
    return new Linear3DSet(type, sets, DomainCoordinateSystem,
                           SetUnits, SetErrors, cacheSamples);
  }

  /**
   * Extended version of the toString() method.
   * @param  pre  prefix for string.
   * @return wordy string describing this Linear3DSet.
   */
  public String longString(String pre) throws VisADException {
    String s = pre + "Linear3DSet: Length = " + Length + "\n";
    s = s + pre + "  Dimension 1: Length = " + X.getLength() +
            " Range = " + X.getFirst() + " to " + X.getLast() + "\n";
    s = s + pre + "  Dimension 2: Length = " + Y.getLength() +
            " Range = " + Y.getFirst() + " to " + Y.getLast() + "\n";
    s = s + pre + "  Dimension 3: Length = " + Z.getLength() +
            " Range = " + Z.getFirst() + " to " + Z.getLast() + "\n";
    return s;
  }

}

