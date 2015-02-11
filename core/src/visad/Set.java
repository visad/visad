//
// Set.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;

//
// TO DO:
//
// finish GriddednDSet using VisAD Enumerated logic.
//
// add MultiGridded for union of grids
// Field evalualtion as mean of non-missing values in each grid
//
// Field and FlatField: options (mode?) for Interpolated and NotInterpolated
//
// Field and FlatField: test for operations on IntegerSet and optimize
//
// change lots of methods to default protection (visible within package only)
//
//

/**
 * <P>Set is the abstract superclass of the VisAD hierarchy of sets.<P>
 *
 * <P>Set-s are subsets of R^n for n>0.  For the most part, Set objects are
 * immutable (but see {@link SampledSet#getSamples(boolean)}).<P>
 */
public abstract class Set extends DataImpl implements SetIface {

  int DomainDimension; // this is a subset of R^DomainDimension
  int Length;          // number of samples
  CoordinateSystem DomainCoordinateSystem;
  transient boolean hashCodeSet = false;
  transient int hashCode = 0;

  Unit[] SetUnits;
  ErrorEstimate[] SetErrors;

  /** caches of Set-s that return false and true to equals();
      useful for GriddedSet and other Set subclasses which have
      expensive equals tests;
      WLH 6 Nov 97 - don't use these. they hinder garbage collection
      and won't accomplish much practical
  private static final int CACHE_LENGTH = 10;
  private Set[] NotEqualsCache;
  private int LastNotEquals;
  private Set[] EqualsCache;
  private int LastEquals;
*/

  /** construct a Set object */
  public Set(MathType type) throws VisADException {
    this(type, null, null, null);
  }

  /**
   * Constructs a Set object with a non-default CoordinateSystem.<p>
   *
   * @param type		The type of the Set.
   * @param coord_sys		The CoordinateSystem associated with this
   *				Set.
   */
  public Set(MathType type, CoordinateSystem coord_sys) throws VisADException {
    this(type, coord_sys, null, null);
  }

  /**
   * Constructs a Set object with a non-default CoordinateSystem, non-default
   * Unit-s, and non-default errors.  This is the most general constructor.<p>
   *
   * @param type		The MathType of the set.  May be a RealType,
   *				a RealTupleType, or a SetType.
   * @param coord_sys		Optional coordinate system for the domain of the
   *				set.  May be <code>null</code>, in which case
   *				the default coordinate system of the domain is
   *				used.
   * @param units               Optional units for the values.  May
   *                            be <code> null</code>, in which case
   *                            the default units of the domain are
   *                            used.  If the <code>i</code>th element is
   *                            non-<code>null</code> and the RealType of the
   *                            corresponding domain component is an interval,
   *                            then the unit that is actually used is <code>
   *                            units[i].getAbsoluteUnit()</code>.
   * @param errors              Error estimates.  May be <code>null</code>.
   *                            <code>errors[i]</code> is the error estimate
   *                            for the <code>i</code>-th component and may be
   *                            <code>null</code>.
   */
  public Set(MathType type, CoordinateSystem coord_sys, Unit[] units,
             ErrorEstimate[] errors) throws VisADException {
    super(adjustType(type));
    Length = 0;
    DomainDimension = getDimension(type);
    RealTupleType DomainType = ((SetType) Type).getDomain();
    CoordinateSystem cs = DomainType.getCoordinateSystem();
    if (coord_sys == null) {
      DomainCoordinateSystem = cs;
    }
    else {
      if (cs == null || !cs.getReference().equals(coord_sys.getReference())) {
        throw new CoordinateSystemException(
          "Set: coord_sys " + coord_sys.getReference() +
          " must match Type.DefaultCoordinateSystem " +
          (cs == null ? null : cs.getReference()));
      }
      DomainCoordinateSystem = coord_sys;
    }
    if (DomainCoordinateSystem != null &&
        !Unit.canConvertArray(DomainCoordinateSystem.getCoordinateSystemUnits(),
                              DomainType.getDefaultUnits())) {
      throw new UnitException("Set: CoordinateSystem Units must be " +
                              "convertable with DomainType default Units");
    }

    if (units == null) {
      SetUnits = (DomainCoordinateSystem == null) ?
                 DomainType.getDefaultUnits() :
                 DomainCoordinateSystem.getCoordinateSystemUnits();
    }
    else {
      if (units.length != DomainDimension) {
        throw new UnitException("Set: units dimension " + units.length +
                                " does not match Domain dimension " +
                                DomainDimension);
      }
      SetUnits = new Unit[DomainDimension];
      Unit[] dunits = DomainType.getDefaultUnits();
      for (int i=0; i<DomainDimension; i++) {
        if (units[i] == null && dunits[i] != null) {
          SetUnits[i] = dunits[i];
        }
        else {
          SetUnits[i] =
	    ((RealType)DomainType.getComponent(i)).isInterval()
	      ? units[i].getAbsoluteUnit()
	      : units[i];
        }
      }
    }
    if(!Unit.canConvertArray(SetUnits, DomainType.getDefaultUnits())) {
      throw new UnitException(
          "Set: Actual units not convertable with DomainType default units ");
    }
    if (SetUnits == null) SetUnits = new Unit[DomainDimension];

    SetErrors = new ErrorEstimate[DomainDimension];
    if (errors != null) {
      if (errors.length != DomainDimension) {
        throw new SetException("Set: errors dimension " + errors.length +
                               " does not match Domain dimension " +
                               DomainDimension);
      }
      for (int i=0; i<DomainDimension; i++) SetErrors[i] = errors[i];
    }
  }

  /** get DomainDimension (i.e., this is a subset of R^DomainDimension) */
  static int getDimension(MathType type) throws VisADException {
    if (type == null) {
      throw new TypeException("Set.getDimension: type cannot be null");
    }
    if (type instanceof SetType) {
      return ((SetType) type).getDomain().getDimension();
    }
    else if (type instanceof RealTupleType) {
      return ((RealTupleType) type).getDimension();
    }
    else if (type instanceof RealType) {
      return 1;
    }
    else {
      throw new TypeException("Set: Type must be SetType or RealTupleType," +
                              " not " + type.getClass().getName());
    }
  }

  static MathType adjustType(MathType type) throws VisADException {
    if (type == null) {
      throw new TypeException("Set.adjustType: type cannot be null");
    }
    if (type instanceof SetType) {
      return type;
    }
    else if (type instanceof RealTupleType) {
      return new SetType(type);
    }
    else if (type instanceof RealType) {
      return new SetType(type);
    }
    else {
      throw new TypeException("Set: Type must be SetType, RealTupleType" +
                              " or RealType, not " +
                              type.getClass().getName());
    }
  }

  /**
   * Returns the units of the values in the set.  The units may differ from the
   * default units of the underlying MathType but will be convertible with them.
   * @return                    The units of the values in the set.  Will not be
   *                            <code>null</code>.  RETURN_VALUE<code>[i]</code>
   *                            is the unit of the <code>i</code>-th component
   *                            and may be <code>null</code>.
   */
  public Unit[] getSetUnits() {
    return Unit.copyUnitsArray(SetUnits);
  }

  /**
   * Returns the error estimates of the values in the set.
   * @return                    Error estimates for the set.  Will not be
   *                            <code>null</code>.  RETURN_VALUE<code>[i]</code>
   *                            is the error estimate for the <code>i</code>-th
   *                            component and may be <code>null</code>.
   */
  public ErrorEstimate[] getSetErrors() {
    return ErrorEstimate.copyErrorsArray(SetErrors);
  }

  /**
   * Gets the coordinate system of this domain set (DomainCoordinateSystem).
   * @return                    The coordinate system of this domain
   *                            set.  This will be the coordinate
   *                            system passed to the constructor if
   *                            non-<code>null</code>; otherwise, the
   *                            (default) coordinate system of the
   *                            underlying RealTupleType (which may be
   *                            <code>null</code>).
   */
  public CoordinateSystem getCoordinateSystem() {
    return DomainCoordinateSystem;
  }

  /** get DomainDimension (i.e., this is a subset of R^DomainDimension) */
  public int getDimension() {
    return DomainDimension;
  }

  /** for non-SimpleSet, ManifoldDimension = DomainDimension */
  public int getManifoldDimension() {
    return DomainDimension;
  }

  /** get the number of samples */
  public int getLength() throws VisADException {
    return Length;
  }

  /**
   * A wrapper around {@link #getLength() getLength} for JPython.
   *
   * @return The number of elements in the Set
   */
  public int __len__() throws VisADException {
    return Length;
  }

  /** return an enumeration of sample indices in a spatially
      coherent order; this is useful for efficiency */
  public int[] getWedge() {
    int[] wedge = new int[Length];
    for (int i=0; i<Length; i++) wedge[i] = i;
    return wedge;
  }

  /** return an enumeration of sample values in index order
     (i.e., not in getWedge order); the return array is
     organized as float[domain_dimension][number_of_samples] */
  public float[][] getSamples() throws VisADException {
    return getSamples(true);
  }

  public float[][] getSamples(boolean copy) throws VisADException {
    int n = getLength();
    int[] indices = new int[n];
    // do NOT call getWedge
    for (int i=0; i<n; i++) indices[i] = i;
    return indexToValue(indices);
  }

  public double[][] getDoubles() throws VisADException {
    return getDoubles(true);
  }

  public double[][] getDoubles(boolean copy) throws VisADException {
    return floatToDouble(getSamples(true));
  }

  public void cram_missing(boolean[] range_select) {
  }

  /**
   * return Set values corresponding to Set indices
   * @param index array of integer indices
   * @return float[domain_dimension][indices.length] array of
   *         Set values
   * @throws VisADException a VisAD error occurred
   */
  public abstract float[][] indexToValue(int[] index) throws VisADException;

  /**
   * return Set indices of Set values closest to value elements
   *        (return -1 for any value outside Set range)
   * @param value float[domain_dimension][number_of_values] array of
   *        Set values
   * @return array of integer indices
   * @throws VisADException a VisAD error occurred
   */
  public abstract int[] valueToIndex(float[][] value) throws VisADException;

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException {
    return computeRanges(type, shadow, null, false);
  }

  /**
   * this default does not set ranges - it is used by FloatSet and
   * DoubleSet
   */
  public DataShadow computeRanges(ShadowType type, DataShadow shadow,
                                  double[][] ranges, boolean domain)
         throws VisADException {
    setAnimationSampling(type, shadow, domain);
    return shadow;
  }

  /** domain == true is this is the domain of a Field */
  void setAnimationSampling(ShadowType type, DataShadow shadow, boolean domain)
       throws VisADException {
    // default does nothing
  }

  /** merge 1D sets; used for default animation set */
  public Set merge1DSets(Set set) throws VisADException {
    if (DomainDimension != 1 || set.getDimension() != 1 ||
        equals(set)) return this;
    int length = getLength();
    // all indices in this
    int[] indices = getWedge();
    // all values in this
    double[][] old_values = indexToDouble(indices); // WLH 21 Nov 2001
    // transform values from this to set
    ErrorEstimate[] errors_out = new ErrorEstimate[1];

    double[][] values = CoordinateSystem.transformCoordinates(
                   ((SetType) set.getType()).getDomain(),
                   set.getCoordinateSystem(), set.getSetUnits(),
                   null /* set.getSetErrors() */,
                   ((SetType) Type).getDomain(),
                   DomainCoordinateSystem,
                   SetUnits, null /* SetErrors */, old_values);
    // find indices of set not covered by this
    int set_length = set.getLength();
    boolean[] set_indices = new boolean[set_length];
    for (int i=0; i<set_length; i++) set_indices[i] = true;
    if (set_length > 1) {
      // set indices for values in this
      int[] test_indices = set.doubleToIndex(values);
      try {
        for (int i=0; i<length; i++) {
          if (test_indices[i] > -1) set_indices[test_indices[i]] = false;
        }
      } catch (ArrayIndexOutOfBoundsException aioobe) {
        throw new VisADException("Cannot merge sets");
      }
    }
    else {
      double[][] set_values = set.getDoubles();
      double set_val = set_values[0][0];
      double min = Double.MAX_VALUE;
      // double max = Double.MIN_VALUE;
      double max = -Double.MAX_VALUE;
      for (int i=0; i<length; i++) {
        if (values[0][i] > max) max = values[0][i];
        if (values[0][i] < min) min = values[0][i];
      }
      double delt = (max - min) / length;
      //System.out.println("min = " + min + " max = " + max + " delt = " + delt);
      if ((min - delt) <= set_val && set_val <= (max + delt)) {
        set_indices[0] = false;
      }
    }

    // now set_indices = true for indices of set not covered by this
    int num_new = 0;
    for (int i=0; i<set_length; i++) if (set_indices[i]) num_new++;
    if (num_new == 0) return this; // all covered, so nothing to do
    // not all covered, so merge values of this with values of set
    // not covered; first get uncovered indices
    int[] new_indices = new int[num_new];
    num_new = 0;
    for (int i=0; i<set_length; i++) {
      if (set_indices[i]) {
        new_indices[num_new] = i;
        num_new++;
      }
    }

    // get uncovered values
    double[][] new_values = set.indexToDouble(new_indices);

    // transform values for Units and CoordinateSystem
    new_values = CoordinateSystem.transformCoordinates(
                     ((SetType) Type).getDomain(),
                     DomainCoordinateSystem, SetUnits, null /* errors_out */,
                     ((SetType) set.getType()).getDomain(),
                     set.getCoordinateSystem(), set.getSetUnits(),
                     null /* set.getSetErrors() */, new_values);

    // merge uncovered values with values of this
    double[][] all_values = new double[1][length + num_new];
    // WLH 21 Nov 2001
    for (int i=0; i<length; i++) all_values[0][i] = old_values[0][i];
    for (int i=0; i<num_new; i++) {
      all_values[0][length + i] = new_values[0][i];
    }

    // sort all_values then construct Gridded1DSet
    // just use ErrorEstimates from this
    QuickSort.sort(all_values[0]);
    return new Gridded1DDoubleSet(Type, all_values, all_values[0].length,
                                  DomainCoordinateSystem, SetUnits,
                                  SetErrors, false);
  }

  public Set makeSpatial(SetType type, float[][] samples) throws VisADException {
    throw new SetException("Set.makeSpatial: not valid for this Set");
  }

  public VisADGeometryArray make1DGeometry(byte[][] color_values)
         throws VisADException {
    throw new SetException("Set.make1DGeometry: not valid for this Set");
  }

  public VisADGeometryArray make2DGeometry(byte[][] color_values,
         boolean indexed) throws VisADException {
    throw new SetException("Set.make2DGeometry: not valid for this Set");
  }

  public VisADGeometryArray[] make3DGeometry(byte[][] color_values)
         throws VisADException {
    throw new SetException("Set.make3DGeometry: not valid for this Set");
  }

  public VisADGeometryArray makePointGeometry(byte[][] color_values)
         throws VisADException {
    throw new SetException("Set.makePointGeometry: not valid for this Set");
  }

  /** return basic lines in array[0], fill-ins in array[1]
      and labels in array[2] */
  public VisADGeometryArray[][] makeIsoLines(float[] intervals,
                  float lowlimit, float highlimit, float base,
                  float[] fieldValues, byte[][] color_values,
                  boolean[] swap, boolean dash,
                  boolean fill, ScalarMap[] smap,
                  double[] scale, double label_size, boolean sphericalDisplayCS
                  ) throws VisADException {
    throw new SetException("Set.makeIsoLines: not valid for this Set");
  }

  public VisADGeometryArray makeIsoSurface(float isolevel,
         float[] fieldValues, byte[][] color_values, boolean indexed)
         throws VisADException {
    throw new SetException("Set.makeIsoSurface: not valid for this Set");
  }

  /**
   * Returns an array of sample-point values corresponding to an array of 
   * sample-point indicies.
   *
   * @param index              The indicies of the sample points.
   * @return                   A corresponding array of sample-point values.
   *                           <em>RETURN_VALUE</em><code>[i][j]</code> is the
   *                           <code>j</code>th component of sample-point
   *                           <code>i</code>.
   * @throws VisADException    if a VisAD failure occurs.
   */
  public double[][] indexToDouble(int[] index) throws VisADException {
    return floatToDouble(indexToValue(index));
  }

  public int[] doubleToIndex(double[][] value) throws VisADException {
    return valueToIndex(doubleToFloat(value));
  }

  public static double[][] floatToDouble(float[][] value) {
    if (value == null) return null;
    double[][] val = new double[value.length][];
    for (int i=0; i<value.length; i++) {
      if (value[i] == null) {
        val[i] = null;
      }
      else {
        val[i] = new double[value[i].length];
        for (int j=0; j<value[i].length; j++) {
          val[i][j] = value[i][j];
        }
      }
    }
    return val;
  }

  public static float[][] doubleToFloat(double[][] value) {
    if (value == null) return null;
    float[][] val = new float[value.length][];
    for (int i=0; i<value.length; i++) {
      if (value[i] == null) {
        val[i] = null;
      }
      else {
        val[i] = new float[value[i].length];
        for (int j=0; j<value[i].length; j++) {
          val[i][j] = (float) value[i][j];
        }
      }
    }
    return val;
  }

  public static float[][] copyFloats(float[][] samples) {
    if (samples == null) return null;
    int dim = samples.length;
    float[][] s_copy = new float[dim][];
    for (int j=0; j<dim; j++) {
      int len = samples[j].length;
      s_copy[j] = new float[len];
      System.arraycopy(samples[j], 0, s_copy[j], 0, len);
    }
    return s_copy;
  }

  public static double[][] copyDoubles(double[][] samples) {
    if (samples == null) return null;
    int dim = samples.length;
    double[][] s_copy = new double[dim][];
    for (int j=0; j<dim; j++) {
      int len = samples[j].length;
      s_copy[j] = new double[len];
      System.arraycopy(samples[j], 0, s_copy[j], 0, len);
    }
    return s_copy;
  }

  /**
   * <p>Returns the indexes of the neighboring points for every point
   * in the set. <code>neighbors.length</code> should be at least
   * <code>getLength()</code>.  On return, <code>neighbors[i][j]</code>
   * will be the index of the <code>j </code>-th neighboring point of
   * point <code>i</code>.  This method will allocate and set the array
   * <code>neighbors[i]</code> for all <code>i</code>.</p>
   *
   * <p>This implementation always throws an {@link UnimplementedException}.</p>
   *
   * @param neighbors                The array to contain the indexes of the
   *                                 neighboring points.
   * @throws NullPointerException    if the array is <code>null</code>.
   * @throws ArrayIndexOutOfBoundsException
   *                                 if <code>neighbors.length < getLength()
   *                                 </code>.
   * @throws VisADException          if a VisAD failure occurs.
   */
  public void getNeighbors( int[][] neighbors )
              throws VisADException
  {
    throw new UnimplementedException("Set: getNeighbors()");
  }

  public void getNeighbors( int[][] neighbors, float[][] weights )
         throws VisADException
  {
    throw new UnimplementedException("Set: getNeighbors()");
  }

  /**
   * <p>Returns the indexes of the neighboring points along a manifold
   * dimesion for every point in the set. Elements <code>[i][0]</code>
   * and <code>[i][1]</code> of the returned array are the indexes of the
   * neighboring sample points in the direction of decreasing and increasing
   * manifold index, respectively, for sample point <code>i</code>.  If a sample
   * point doesn't have a neighboring point (because it is an exterior point,
   * for example) then the value of the corresponding index will be -1.</p>
   *
   * <p>This implementation always throws an {@link UnimplementedException}.</p>
   *
   * @param dimension	           The index of the manifold dimension along
   *                                which to return the neighboring points.
   * @throws ArrayIndexOutOfBoundsException
   *                                if <code>manifoldIndex < 0 || 
   *                                manifoldIndex >= getManifoldDimension()
   *                                </code>.
   * @throws VisADException         if a VisAD failure occurs.
   * @see #getManifoldDimension()
   */
  public int[][] getNeighbors(int dimension)
                 throws VisADException
  {
    throw new UnimplementedException("Set: getNeighbors()");
  }

  /** test set against a cache of Set-s not equal to this */
  public boolean testNotEqualsCache(Set set) {
/* WLH 6 Nov 97
    if (NotEqualsCache == null || set == null) return false;
    for (int i=0; i<CACHE_LENGTH; i++) {
      if (set == NotEqualsCache[i]) return true;
    }
*/
    return false;
  }

  /** add set to a cache of Set-s not equal to this */
  public void addNotEqualsCache(Set set) {
/* WLH 6 Nov 97
    if (NotEqualsCache == null) {
      NotEqualsCache = new Set[CACHE_LENGTH];
      for (int i=0; i<CACHE_LENGTH; i++) NotEqualsCache[i] = null;
      LastNotEquals = 0;
    }
    NotEqualsCache[LastNotEquals] = set;
    LastNotEquals = (LastNotEquals + 1) % CACHE_LENGTH;
*/
  }

  /** test set against a cache of Set-s equal to this */
  public boolean testEqualsCache(Set set) {
/* WLH 6 Nov 97
    if (EqualsCache == null || set == null) return false;
    for (int i=0; i<CACHE_LENGTH; i++) {
      if (set == EqualsCache[i]) return true;
    }
*/
    return false;
  }

  /** add set to a cache of Set-s equal to this */
  public void addEqualsCache(Set set) {
/* WLH 6 Nov 97
    if (EqualsCache == null) {
      EqualsCache = new Set[CACHE_LENGTH];
      for (int i=0; i<CACHE_LENGTH; i++) EqualsCache[i] = null;
      LastEquals = 0;
    }
    EqualsCache[LastEquals] = set;
    LastEquals = (LastEquals + 1) % CACHE_LENGTH;
*/
  }

  /** test equality of SetUnits and DomainCoordinateSystem
      between this and set */
  public boolean equalUnitAndCS(Set set) {
    if (DomainCoordinateSystem == null) {
      if (set.DomainCoordinateSystem != null) return false;
    }
    else {
      if (!DomainCoordinateSystem.equals(set.DomainCoordinateSystem)) {
        return false;
      }
    }
    if (SetUnits != null || set.SetUnits != null) {
      if (SetUnits == null || set.SetUnits == null) return false;
      int n = SetUnits.length;
      if (n != set.SetUnits.length) return false;
      for (int i=0; i<n; i++) {
        if (SetUnits[i] == null && set.SetUnits[i] == null) continue;
        if (SetUnits[i] == null || set.SetUnits[i] == null) return false;
        if (!SetUnits[i].equals(set.SetUnits[i])) return false;
      }
    }
    return true;
  }

  /**
   * Returns the hash code of the units and coordinate-system.  This is the
   * hash code analogue of {@link #equalUnitAndCS(Set)}.
   * @return			The hash code of the units and coordinate
   *				system.
   */
  public int unitAndCSHashCode()
  {
    int	hashCode = 0;
    if (DomainCoordinateSystem != null)
      hashCode ^= DomainCoordinateSystem.hashCode();
    if (SetUnits != null)
      for (int i=0; i<SetUnits.length; i++)
        if (SetUnits[i] != null)
	  hashCode ^= SetUnits[i].hashCode();
    return hashCode;
  }

  /** for JPython */
  public Data __getitem__(int index) throws VisADException, RemoteException {
    int[] indices = {index};
    double[][] values = indexToDouble(indices);
    RealType[] types = ((SetType) getType()).getDomain().getRealComponents();
    Real[] reals = new Real[DomainDimension];
    for (int i=0; i<DomainDimension; i++) {
      reals[i] = new Real(types[i], values[i][0], SetUnits[i]);
    }
    if (DomainDimension == 1) {
      return reals[0];
    }
    else {
      RealTupleType rtt = ((SetType) getType()).getDomain();
      return new RealTuple(rtt, reals, getCoordinateSystem());
    }
  }

  /** test for equality */
  public abstract boolean equals(Object set);

  /**
   * Clones this instance.
   *
   * @return                            A clone of this instance.
   */
  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException ex) {
      throw new RuntimeException("Assertion failure");  // can't happen
    }
  }

  // WLH 3 April 2003
  public Data unary( int op, MathType new_type, int sampling_mode, int error_mode )
              throws VisADException, RemoteException {
    if (op == Data.NOP) {
      return (Set) cloneButType(new_type);
    }
    else {
      throw new TypeException("Set: unary");
    }
  }

  /** copy this Set, but give it a new MathType;
      this is safe, since constructor checks consistency of
      DomainCoordinateSystem and SetUnits with Type */
  public abstract Object cloneButType(MathType type) throws VisADException;

  public String longString() throws VisADException {
    return longString("");
  }

  public String longString(String pre) throws VisADException {
    throw new TypeException("Set.longString");
  }

  /* run 'java visad.Set' to test the MathType and Set classes */
  public static void main(String args[]) throws VisADException {

    // visad.type.Crumb crumb_type = new visad.type.Crumb(); // this works
    // visad.data.Crumb crumb_data = new visad.data.Crumb(); // this works
    // but this does not work:   Crumb crumb = new Crumb();

    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType count = RealType.getRealType("count");

    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);

    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);

    try {
      TupleType radiancexxx = new TupleType(types2);
    }
    catch (TypeException e) {
      // System.out.println(e);
      e.printStackTrace(); // same as e.printStackTrace(System.out);
    }
    try {
      FunctionType image_tuplexxx = new FunctionType(earth_location, radiance);
    }
    catch (TypeException e) {
      System.out.println(e);
    }
    try {
      FunctionType image_visxxx = new FunctionType(earth_location, vis_radiance);
    }
    catch (TypeException e) {
      System.out.println(e);
    }

    FunctionType image_tuple = new FunctionType(earth_location, radiance);
    FunctionType image_vis = new FunctionType(earth_location, vis_radiance);
    FunctionType image_ir = new FunctionType(earth_location, ir_radiance);

    FunctionType ir_histogram = new FunctionType(ir_radiance, count);

    System.out.println(image_tuple);
    System.out.println(ir_histogram);

    Linear2DSet set2d = new Linear2DSet(earth_location,
                                        0.0, 127.0, 128, 0.0, 127.0, 128);
    Linear1DSet set1d = new Linear1DSet(ir_radiance, 0.0, 255.0, 256);
    Integer2DSet iset2d = new Integer2DSet(earth_location, 128, 128);
    Integer1DSet iset1d = new Integer1DSet(ir_radiance, 256);

    FlatField imaget1 = new FlatField(image_tuple, set2d);
    FlatField imagev1 = new FlatField(image_vis, set2d);
    FlatField imager1 = new FlatField(image_ir, set2d);
    FlatField histogram1 = new FlatField(ir_histogram, set1d);

    System.out.println(imaget1);
    System.out.println(histogram1);

    System.out.println(set2d);
    System.out.println(set1d);
    System.out.println(iset2d);
    System.out.println(iset1d);

    if (set1d instanceof IntegerSet) System.out.println(" set1d ");
    if (set2d instanceof IntegerSet) System.out.println(" set2d ");
    if (iset1d instanceof IntegerSet) System.out.println(" iset1d ");
    if (iset2d instanceof IntegerSet) System.out.println(" iset2d ");
    System.out.println("");

    int i = 14;
    short s = 12;
    byte b = 10;
    Real t = new Real(1.0);
    Real x = new Real(12);
    Real y = new Real(12L);
    Real u = new Real(i);
    Real v = new Real(s);
    Real w = new Real(b);

    System.out.println(t);
    System.out.println("" + x + " " + y + " " + u + " " + v + " " + w);
  }

/* Here's the output:

iris 235% java visad.Set
visad.TypeException: TupleType: all components are RealType, must use RealTupleType
        at visad.TupleType.<init>(TupleType.java:47)
        at visad.Set.main(Set.java:155)
FunctionType (Real): (Latitude(degrees), Longitude(degrees)) -> (vis_radiance, ir_radiance)
FunctionType (Real): (ir_radiance) -> count
FlatField  missing

FlatField  missing

Linear2DSet: Length = 16384
  Dimension 1: Length = 128 Range = 0 to 127
  Dimension 2: Length = 128 Range = 0 to 127

Linear1DSet: Length = 256 Range = 0 to 255

Integer2DSet: Length = 16384
  Dimension 1: Length = 128
  Dimension 2: Length = 128

Integer1DSet: Length = 256

 iset1d
 iset2d

1
12 12 14 12 10
iris 236%

*/

}

