
//
// FlatField.java
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

import java.util.*;
import java.rmi.*;

/**
   FlatField is the VisAD class for finite samplings of functions whose
   range type and range coordinate systems are simple enough to allow
   efficient representation.  The DomainSet, DomainCoordinateSystem,
   RangeSet, RangeCoordinateSystem and RangeCoordinateSystems variables
   of FlatField are immutable.<P>

   A FlatField range type may be either a RealType (for a function with
   range = R), a RealTupleType (for a function with range = R^n for n > 0),
   or a Flat Tuple.<P>

   VisAD avoids invoking methods once per datum through the use of
   FlatField's.  These are logically Field's of Tuple's of RealType's
   and RealTupleType's.  Internally FlatField's are stored as arrays of
   numerical values, rather than the arrays of data objects stored in
   Field's.  Many of the methods in the FlatField class and in other
   classes (e.g., CoordinateTransform, Set, Unit) process data in the
   form double[Dimension][Length] where Length is the number of samples
   in a Field and Dimension is the number of Tuple elements in the
   Field range values.  Note that the order of the Length and Dimension
   indices are reversed as array indices.  This allows efficient
   processing of long columns of Field value components.  For example,
   if Latitude is one component of Field values, then any computation
   involving Latitude can be applied in a tight loop to all Latitude's
   in the Field.<P>
   
   FlatField's support range types more general than RealTuple's.  To
   understand the motive, consider a set of observations that include
   Latitude, Longitude, Altitude, Pressure, Temperature, etc.  We can
   organize these as a Field whose range values have the Tuple type:<P>
 <PRE>
  
     (Latitude, Longitude, Altitude, Pressure, Temperature, ...)

</PRE>  
   However, in order to declare that (Latitude, Longitude, Altitude)
   is a coordinate system with coordinate transform functions to other
   spatial coordinate systems, we need to organize:<P>
<PRE>
   
     (Latitude, Longitude, Altitude)
   
</PRE>  
   as a RealTupleType.  Hence the range type of the Field of observations
   must be:<P>
<PRE>
   
     ((Latitude, Longitude, Altitude), Pressure, Temperature, ...)
   
</PRE>  
   which is not a RealTupleType (since one of its components is a
   RealTupleType).  In order to process such data efficiently, FlatField's
   must support range types that are Tuple's of RealType's and
   RealTupleType's.<P>
*/
public class FlatField extends FieldImpl {

  private int TupleDimension; // dimension of Type.getFlatRange()
  private Set RangeSet[]; // one 1-D Set per range components
  private int RangeMode[]; // DOUBLE, FLOAT, INT, SHORT or BYTE
  // coordinate system of the function range R^n
  private CoordinateSystem RangeCoordinateSystem; // used if Type.Real
  private CoordinateSystem[] RangeCoordinateSystems; // used for Flat, not for Real
  private boolean MissingFlag;

  private Unit[] RangeUnits;

  // RangeErrors, like range values, are not immutable
  private ErrorEstimate[] RangeErrors;

  // for tuple memeber i, only one of these is not null, depending on RangeSet[i]
  private double[][] DoubleRange;
  private float[][] FloatRange;
  private long[][] LongRange; // not currently used because array
                            // indices are int's
  private int[][] IntRange;
  private short[][] ShortRange;
  private byte[][] ByteRange;

  private static final int MISSING1 = Byte.MIN_VALUE;      // least byte
  private static final int MISSING2 = Short.MIN_VALUE;     // least short
  private static final int MISSING4 = Integer.MIN_VALUE;   // least int
  // private static final int MISSING8 = Long.MIN_VALUE;   // least long

  private static final int DOUBLE = 1;
  private static final int FLOAT = 2;
  private static final int LONG = 3; // not currently used because array 
                                     // indices are int's
  private static final int INT = 4;
  private static final int SHORT = 5;
  private static final int BYTE = 6;

  /** construct a FlatField from type;
      use default Set of FunctionType domain */
  public FlatField(FunctionType type) throws VisADException {
    this(type, null, null, null, null, null);
  }

  /** construct a FlatField with non-default domain set */
  public FlatField(FunctionType type, Set domain_set) throws VisADException {
    this(type, domain_set, null, null, null, null);
  }

  /** construct a FlatField with a Real Function Type */
  public FlatField(FunctionType type, Set domain_set,
                   CoordinateSystem range_coord_sys, Set[] range_sets,
                   Unit[] units) throws VisADException {
    this(type, domain_set, range_coord_sys, null, range_sets, units);
  }

  /** construct a FlatField with a non-Real Function Type */
  public FlatField(FunctionType type, Set domain_set,
                   CoordinateSystem[] range_coord_syses, Set[] range_sets,
                   Unit[] units) throws VisADException {
    this(type, domain_set, null, range_coord_syses, range_sets, units);
  }

  /* this is the most general FlatField constructor */
  public FlatField(FunctionType type, Set domain_set,
                   CoordinateSystem range_coord_sys,
                   CoordinateSystem[] range_coord_syses,
                   Set[] range_sets, Unit[] units)
          throws VisADException {
    super(type, domain_set);
    if (!type.getFlat()) {
      throw new FieldException("FlatField: FunctionType must be Flat");
    }
    MathType RangeType = type.getRange();
    RealTupleType FlatRange = type.getFlatRange();
    TupleDimension = FlatRange.getDimension();
    DoubleRange = new double[TupleDimension][];

    // set RangeSet
    RangeSet = new Set[TupleDimension];
    RangeMode = new int[TupleDimension];
    if (range_sets == null) {
      // set the default range sampling; if no default, use double
      for (int i=0; i<TupleDimension; i++) {
        RangeSet[i] = ((RealType) FlatRange.getComponent(i)).getDefaultSet();
        if (RangeSet[i] == null) {
          RangeSet[i] = new FloatSet(new SetType(FlatRange.getComponent(i)));
          // WLH 1 Feb 98
          // RangeSet[i] = new DoubleSet(new SetType(FlatRange.getComponent(i)));
        }
      }
    }
    else {
      // set explicit range sets
      if (TupleDimension != range_sets.length) {
        throw new SetException("FlatField: range set dimensions don't match");
      }
      for (int i=0; i<TupleDimension; i++) {
        if (range_sets[i] == null || range_sets[i].getDimension() != 1) {
          throw new SetException("FlatField: each range set dimension must be 1");
        }
      }
      // force RangeSet Type-s to match FlatRange
      for (int i=0; i<TupleDimension; i++) {
        if (FlatRange.getComponent(i).equals(
            ((SetType) range_sets[i].getType()).getDomain())) {
          RangeSet[i] = range_sets[i];
        }
        else {
          RangeSet[i] = (Set) range_sets[i].cloneButType(
                        new SetType(FlatRange.getComponent(i)));
        }
      }
    }
    nullRanges();

    // set RangeCoordinateSystem or RangeCoordinateSystems
    // also set RangeUnits
    if (type.getReal()) {
      // only one RangeCoordinateSystem
      Unit[] type_units;
      if (range_coord_syses != null) {
        throw new CoordinateSystemException("FlatField: Real Function" +
               " Type requires single range coordinate syste");
      }
      RangeCoordinateSystems = null;
      RangeCoordinateSystem = FlatRange.getCoordinateSystem();
      if (range_coord_sys != null) {
        if (!(RangeType instanceof RealTupleType)) {
          throw new CoordinateSystemException("FlatField: " +
                    "range_coord_sys but RangeType is not RealTupleType");
        }
        if (RangeCoordinateSystem == null ||
            !RangeCoordinateSystem.getReference().equals(
             range_coord_sys.getReference())) {
          throw new CoordinateSystemException("FlatField: " +
            "range_coord_sys must match Range DefaultCoordinateSystem");
        }
        RangeCoordinateSystem = range_coord_sys;
      }
      if (units == null) {
        RangeUnits = (RangeCoordinateSystem == null) ?
                     FlatRange.getDefaultUnits() :
                     RangeCoordinateSystem.getCoordinateSystemUnits();
      }
      else {
        if (units.length != TupleDimension) {
          throw new UnitException("FlatField: units dimension does not match");
        }
        RangeUnits = new Unit[TupleDimension];
        for (int i=0; i<TupleDimension; i++) RangeUnits[i] = units[i];
      }
      if (RangeType instanceof RealTupleType) {
        type_units = ((RealTupleType) RangeType).getDefaultUnits();
      }
      else {
        type_units = new Unit[1];
        type_units[0] = ((RealType) RangeType).getDefaultUnit();
      }   
      if (RangeCoordinateSystem != null &&
          !Unit.canConvertArray(RangeCoordinateSystem.getCoordinateSystemUnits(),
                                type_units)) {
        throw new UnitException("FlatField: RangeCoordinateSystem Units must be " +
                                "convertable with RangeType default Units");
      }
      if (RangeCoordinateSystem != null &&
          !Unit.canConvertArray(RangeCoordinateSystem.getCoordinateSystemUnits(),
                                RangeUnits)) {
        throw new UnitException("FlatField: RangeUnits must be convertable " +
                                "with RangeCoordinateSystem Units");
      }
      if (!Unit.canConvertArray(type_units, RangeUnits)) {
        throw new UnitException("FlatField: RangeUnits must be convertable " +
                                "with RangeType default Units");
      }
    }
    else { // !type.getReal()
      // multiple RangeCoordinateSystems
      Unit[] sub_range_units;
      Unit[] sub_type_units;
      if (range_coord_sys != null) {
        throw new CoordinateSystemException("FlatField: non-Real Function" +
               " Type requires multiple range coordinate systems");
      }
      RangeCoordinateSystem = null;
      int n = ((TupleType) RangeType).getDimension();
      RangeCoordinateSystems = new CoordinateSystem[n];
      for (int i=0; i<n; i++) {
        MathType component = ((TupleType) RangeType).getComponent(i);
        if (component instanceof RealTupleType) {
          RangeCoordinateSystems[i] =
            ((RealTupleType) component).getCoordinateSystem();
          if (range_coord_syses != null && range_coord_syses[i] != null) {
            if (RangeCoordinateSystems[i] == null ||
                RangeCoordinateSystems[i].getReference() !=
                range_coord_syses[i].getReference()) {
              throw new TypeException("FlatField: range_coord_syses must" +
                                      " match Range DefaultCoordinateSystem");
            }
            RangeCoordinateSystems[i] = range_coord_syses[i];
          }
        }
        else {
          RangeCoordinateSystems[i] = null;
        }
      }
      if (units == null) {
        RangeUnits = FlatRange.getDefaultUnits();
        int j = 0;
        for (int i=0; i<n; i++) {
          if (RangeCoordinateSystems[i] != null) {
            sub_range_units =
              RangeCoordinateSystems[i].getCoordinateSystemUnits();
            for (int k=0; k<sub_range_units.length; k++) {
              RangeUnits[j + k] = sub_range_units[k];
            }
          }
          j += n;
        }
      }
      else {
        if (units.length != TupleDimension) {
          throw new UnitException("FlatField: units dimension does not match");
        }
        RangeUnits = new Unit[TupleDimension];
        for (int i=0; i<TupleDimension; i++) RangeUnits[i] = units[i];
      }

      int j = 0;
      for (int i=0; i<n; i++) {
        int m;
        MathType component = ((TupleType) RangeType).getComponent(i);
        if (component instanceof RealTupleType) {
          sub_type_units = ((RealTupleType) component).getDefaultUnits();
          m = ((RealTupleType) component).getDimension();
          sub_range_units = new Unit[m];
          for (int k=0; k<m; k++) {
            sub_range_units[k] = RangeUnits[j + k];
          }
        }
        else {
          sub_type_units = new Unit[1]; 
          sub_type_units[0] = ((RealType) component).getDefaultUnit();
          m = 1;
          sub_range_units = new Unit[1];
          sub_range_units[0] = RangeUnits[j];
        }

        if (RangeCoordinateSystems[i] != null &&
            !Unit.canConvertArray(sub_type_units,
                  RangeCoordinateSystems[i].getCoordinateSystemUnits())) {
          throw new UnitException("FlatField: RangeCoordinateSystems Units must " +
                                  "be convertable with RangeType default Units");
        }
        if (RangeCoordinateSystems[i] != null &&
            !Unit.canConvertArray(sub_range_units,
                  RangeCoordinateSystems[i].getCoordinateSystemUnits())) {
          throw new UnitException("FlatField: RangeUnits must be convertable " +
                                  "with RangeCoordinateSystems Units");
        }
        if (!Unit.canConvertArray(sub_type_units, sub_range_units)) {
          throw new UnitException("FlatField: RangeUnits must be convertable " +
                                  "with RangeType default Units");
        }
        j += m;
      }
    } // end !type.getReal()
    if (RangeUnits == null) RangeUnits = new Unit[TupleDimension];

    // initialize RangeErrors to all null
    RangeErrors = new ErrorEstimate[TupleDimension];

    // initially all values are missing
    MissingFlag = true;
  }

  public CoordinateSystem[] getRangeCoordinateSystem()
         throws VisADException {
    MathType RangeType = ((FunctionType) Type).getRange();
    if (!((FunctionType) Type).getReal()) {
      throw new TypeException("FlatField.getRangeCoordinateSystem: " +
        "Range is not Real, need DefaultCoordinateSystem index");
    }
    CoordinateSystem[] cs = {RangeCoordinateSystem};
    return cs;
  }

  public CoordinateSystem[] getRangeCoordinateSystem(int i)
         throws VisADException {
    if (((FunctionType) Type).getReal()) {
      throw new TypeException("FlatField.getRangeCoordinateSystem: " +
        "Range is Real, cannot specify CoordinateSystem index");
    }
    CoordinateSystem[] cs = {RangeCoordinateSystems[i]};
    return cs;
  }

  public Unit[][] getRangeUnits() {
    Unit[][] units = new Unit[RangeUnits.length][1];
    for (int i=0; i<RangeUnits.length; i++) {
      units[i][0] = RangeUnits[i];
    }
    return units;
  }

  public ErrorEstimate[] getRangeErrors() {
    synchronized (RangeErrors) {
      return ErrorEstimate.copyErrorsArray(RangeErrors);
    }
  }

  public void setRangeErrors(ErrorEstimate[] errors) throws VisADException {
    synchronized (RangeErrors) {
      if (errors == null) {
        for (int i=0; i<TupleDimension; i++) {
          RangeErrors[i] = null;
        }
      }
      else {
        if (errors.length != TupleDimension) {
          throw new FieldException("FlatField.setRangeErrors: errors " +
                                   "dimension does not match");
        }
        for (int i=0; i<TupleDimension; i++) {
          RangeErrors[i] = errors[i];
        }
      }
    }
  }

  /** set the range values of the function; the order of range values
      must be the same as the order of domain indices in the DomainSet;
      copy argument included for consistency with Field, but ignored */
  public void setSamples(Data[] range, boolean copy)
         throws VisADException, RemoteException {
    if (range.length != Length) {
      throw new FieldException("setSamples: bad Data[] length");
    }
    for (int i=0; i<Length; i++) {
      setSample(i, range[i]);
    }
  }

  /** set the range values of the function; the order of range values
      must be the same as the order of domain indices in the DomainSet */
  public void setSamples(double[][] range)
         throws VisADException, RemoteException {
    setSamples(range, null, true);
  }
 
  /** set the range values of the function; the order of range values
      must be the same as the order of domain indices in the DomainSet */
  public void setSamples(float[][] range)
         throws VisADException, RemoteException {
    setSamples(range, null, true);
  }

  /** set the range values of the function; the order of range values
      must be the same as the order of domain indices in the DomainSet */
  public void setSamples(double[][] range, boolean copy)
         throws VisADException, RemoteException {
    setSamples(range, null, copy);
  }

  /** set the range values of the function; the order of range values
      must be the same as the order of domain indices in the DomainSet */
  public void setSamples(float[][] range, boolean copy)
         throws VisADException, RemoteException {
    setSamples(range, null, copy);
  }

  /** set the range values of the function including ErrorEstimate-s;
      the order of range values must be the same as the order of
      domain indices in the DomainSet */
  public void setSamples(double[][] range, ErrorEstimate[] errors,
              boolean copy) throws VisADException, RemoteException {
    if(range.length != TupleDimension ||
       (errors != null && errors.length != TupleDimension)) {
      throw new FieldException("FlatField.setSamples: bad tuple length");
    }

    for (int i=0; i<TupleDimension; i++) {
      if (range[i].length != Length) {
        throw new FieldException("setSamples: bad array length");
      }
    }
    packValues(range, copy);
    setRangeErrors(errors);
    notifyReferences();
  }

  /** set the range values of the function including ErrorEstimate-s;
      the order of range values must be the same as the order of
      domain indices in the DomainSet */
  public void setSamples(float[][] range, ErrorEstimate[] errors,
              boolean copy) throws VisADException, RemoteException {
    if(range.length != TupleDimension ||
       (errors != null && errors.length != TupleDimension)) {
      throw new FieldException("FlatField.setSamples: bad tuple length");
    }
 
    for (int i=0; i<TupleDimension; i++) {
      if (range[i].length != Length) {
        throw new FieldException("setSamples: bad array length");
      }
    }
    packValues(range, copy);
    setRangeErrors(errors);
    notifyReferences();
  }

  /** pack an array of doubles into field sample values according to the
      RangeSet-s; copies data */
  private void packValues(double[][] range, boolean copy)
          throws VisADException {
    // NOTE INVERTED ORDER OF range ARRAY INDICES !!!
    int[] index;
    synchronized (DoubleRange) {
      nullRanges();
      for (int i=0; i<TupleDimension; i++) {
        double[] rangeI = range[i];
        double[][] range1 = new double[1][];
        range1[0] = rangeI;
        switch (RangeMode[i]) {
          case DOUBLE:
            if (copy) {
              DoubleRange[i] = new double[Length];
              double[] DoubleRangeI = DoubleRange[i];
              System.arraycopy(rangeI, 0, DoubleRangeI, 0, Length);
              // for (int j=0; j<Length; j++) DoubleRangeI[j] = rangeI[j];
            }
            else {
              DoubleRange[i] = rangeI;
            }
            break;
          case FLOAT:
            FloatRange[i] = new float[Length];
            float[] FloatRangeI = FloatRange[i];
            for (int j=0; j<Length; j++) FloatRangeI[j] = (float) rangeI[j];
            break;
          case BYTE:
            index = RangeSet[i].valueToIndex(Set.doubleToFloat(range1));
            ByteRange[i] = new byte[Length];
            byte[] ByteRangeI = ByteRange[i];
            for (int j=0; j<Length; j++) {
              ByteRangeI[j] = (byte) (index[j] + MISSING1 + 1);
            }
            break;
          case SHORT:
            index = RangeSet[i].valueToIndex(Set.doubleToFloat(range1));
            ShortRange[i] = new short[Length];
            short[] ShortRangeI = ShortRange[i];
            for (int j=0; j<Length; j++) {
              ShortRangeI[j] = (short) (index[j] + MISSING2 + 1);
            }
            break;
          case INT:
            index = RangeSet[i].valueToIndex(Set.doubleToFloat(range1));
            IntRange[i] = new int[Length];
            int[] IntRangeI = IntRange[i];
            for (int j=0; j<Length; j++) {
              IntRangeI[j] = index[j] + MISSING4 + 1;
            }
            break;
          default:
            throw new SetException("FlatField.packValues: bad RangeMode");
        }
      }
      clearMissing();
    }
  }

  /** pack an array of floats into field sample values according to the
      RangeSet-s; copies data */
  private void packValues(float[][] range, boolean copy)
          throws VisADException {
    // NOTE INVERTED ORDER OF range ARRAY INDICES !!!
    int[] index;
    synchronized (DoubleRange) {
      nullRanges();
      for (int i=0; i<TupleDimension; i++) {
        float[] rangeI = range[i];
        float[][] range1 = new float[1][];
        range1[0] = rangeI;
        switch (RangeMode[i]) {
          case DOUBLE:
            DoubleRange[i] = new double[Length];
            double[] DoubleRangeI = DoubleRange[i];
            for (int j=0; j<Length; j++) DoubleRangeI[j] = rangeI[j];
            break;
          case FLOAT:
            if (copy) {
              FloatRange[i] = new float[Length];
              float[] FloatRangeI = FloatRange[i];
              System.arraycopy(rangeI, 0, FloatRangeI, 0, Length);
              // for (int j=0; j<Length; j++) FloatRangeI[j] = (float) rangeI[j];
            }
            else {
              FloatRange[i] = rangeI;
            }
            break;
          case BYTE:
            index = RangeSet[i].valueToIndex(range1);
            ByteRange[i] = new byte[Length];
            byte[] ByteRangeI = ByteRange[i];
            for (int j=0; j<Length; j++) {
              ByteRangeI[j] = (byte) (index[j] + MISSING1 + 1);
            }
            break;
          case SHORT:
            index = RangeSet[i].valueToIndex(range1);
            ShortRange[i] = new short[Length];
            short[] ShortRangeI = ShortRange[i];
            for (int j=0; j<Length; j++) {
              ShortRangeI[j] = (short) (index[j] + MISSING2 + 1);
            }
            break;
          case INT:
            index = RangeSet[i].valueToIndex(range1);
            IntRange[i] = new int[Length];
            int[] IntRangeI = IntRange[i];
            for (int j=0; j<Length; j++) {
              IntRangeI[j] = index[j] + MISSING4 + 1;
            }
            break;
          default:
            throw new SetException("FlatField.packValues: bad RangeMode");
        }
      }
      clearMissing();
    }
  }

  /** unpack an array of doubles from field sample values according to the
      RangeSet-s; returns a copy */
  private double[][] unpackValues() throws VisADException {
    double[][] range;
    synchronized (DoubleRange) {
      if (isMissing()) {
        range = new double[TupleDimension][Length];
        for (int i=0; i<TupleDimension; i++) {
          for (int j=0; j<Length; j++) {
            range[i][j] = Double.NaN;
          }
        }
        return range;
      }
      int[] index;
      range = new double[TupleDimension][];
      double[][] range0;
      double[] rangeI;
      for (int i=0; i<TupleDimension; i++) {
        switch (RangeMode[i]) {
          case DOUBLE:
            range[i] = new double[Length];
            rangeI = range[i];
            double[] DoubleRangeI = DoubleRange[i];
            System.arraycopy(DoubleRangeI, 0, rangeI, 0, Length);
            // for (int j=0; j<Length; j++) rangeI[j] = DoubleRangeI[j];
            break;
          case FLOAT:
            range[i] = new double[Length];
            rangeI = range[i];
            float[] FloatRangeI = FloatRange[i];
            for (int j=0; j<Length; j++) {
              rangeI[j] = (double) FloatRangeI[j];
            }
            break;
          case BYTE:
            index = new int[Length];
            byte[] ByteRangeI = ByteRange[i];
            for (int j=0; j<Length; j++) {
              index[j] = ((int) ByteRangeI[j]) - MISSING1 - 1;
            }
            range0 = Set.floatToDouble(RangeSet[i].indexToValue(index));
            range[i] = range0[0];
            break;
          case SHORT:
            index = new int[Length];
            short[] ShortRangeI = ShortRange[i];
            for (int j=0; j<Length; j++) {
              index[j] = ((int) ShortRangeI[j]) - MISSING2 - 1;
            }
            range0 = Set.floatToDouble(RangeSet[i].indexToValue(index));
            range[i] = range0[0];
            break;
          case INT:
            index = new int[Length];
            int[] IntRangeI = IntRange[i];
            for (int j=0; j<Length; j++) {
              index[j] = ((int) IntRangeI[j]) - MISSING4 - 1;
            }
            range0 = Set.floatToDouble(RangeSet[i].indexToValue(index));
            range[i] = range0[0];
            break;
          default:
            throw new SetException("FlatField.unpackValues: bad RangeMode");
        }
      }
    }
    return range;
  }

  /** get values for 'Flat' components in default range Unit-s */
  public double[][] getValues() throws VisADException {
    double[][] values = unpackValues();
    Unit[] units_out =
      ((FunctionType) Type).getFlatRange().getDefaultUnits();
    return Unit.convertTuple(values, RangeUnits, units_out);
  }

  /** get default range Unit-s for 'Flat' components */
  public Unit[] getDefaultRangeUnits() {
    return ((FunctionType) Type).getFlatRange().getDefaultUnits();
  }

  /** get the range value at the index-th sample;
      FlatField does not override evaluate, but the correctness
      of FlatField.evaluate depends on overriding getSample */
  public Data getSample(int index)
         throws VisADException, RemoteException {
    int[] inds;
    if (isMissing() || index < 0 || index >= Length) {
      return ((FunctionType) Type).getRange().missingData();
    }
    double[][] range = new double[TupleDimension][1];
    double[][] range1;
    synchronized (DoubleRange) {
      for (int i=0; i<TupleDimension; i++) {
        switch (RangeMode[i]) {
          case DOUBLE:
            range[i][0] = DoubleRange[i][index];
            break;
          case FLOAT:
            range[i][0] = (double) FloatRange[i][index];
            break;
          case BYTE:
            inds = new int[1];
            inds[0] = ((int) ByteRange[i][index]) - MISSING1 - 1;
            range1 = Set.floatToDouble(RangeSet[i].indexToValue(inds));
            range[i] = range1[0];
            break;
          case SHORT:
            inds = new int[1];
            inds[0] = ((int) ShortRange[i][index]) - MISSING2 - 1;
            range1 = Set.floatToDouble(RangeSet[i].indexToValue(inds));
            range[i] = range1[0];
            break;
          case INT:
            inds = new int[1];
            inds[0] = ((int) IntRange[i][index]) - MISSING4 - 1;
            range1 = Set.floatToDouble(RangeSet[i].indexToValue(inds));
            range[i] = range1[0];
            break;
          default:
            throw new SetException("FlatField.unpackValues: bad RangeMode");
        }
      }
    }

    MathType RangeType = ((FunctionType) Type).getRange();
    if (RangeType instanceof RealType) { 
      return new Real((RealType) RangeType, range[0][0],
                      RangeUnits[0], RangeErrors[0]);
    }
    else if (RangeType instanceof RealTupleType) {
      Real[] reals = new Real[TupleDimension];
      for (int j=0; j<TupleDimension; j++) {
        MathType type = ((RealTupleType) RangeType).getComponent(j);
        reals[j] = new Real((RealType) type, range[j][0],
                            RangeUnits[j], RangeErrors[j]);
      }
      return new RealTuple((RealTupleType) RangeType, reals,
                           RangeCoordinateSystem);
    }
    else { // RangeType is a Flat TupleType
      int n = ((TupleType) RangeType).getDimension();
      int j = 0;
      Data[] datums = new Data[n];
      for (int i=0; i<n; i++) {
        MathType type = ((TupleType) RangeType).getComponent(i);
        if (type instanceof RealType) {
          datums[i] = new Real((RealType) type, range[j][0],
                               RangeUnits[j], RangeErrors[j]);
          j++;    
        }
        else { // type instanceof RealTupleType
          int m = ((RealTupleType) type).getDimension();
          Real[] reals = new Real[m];
          for (int k=0; k<m; k++) {
            RealType ctype = (RealType) ((RealTupleType) type).getComponent(k);
            reals[k] = new Real(ctype, range[j][0],
                                RangeUnits[j], RangeErrors[j]);
            j++;
          }
          datums[i] = new RealTuple((RealTupleType) type, reals,
                                    RangeCoordinateSystems[i]);
        }
      }
      return new Tuple(datums);
    }
  }

  /** set the range value at the index-th sample */
  public void setSample(int index, Data range)
         throws VisADException, RemoteException {
    double[][] values;
    int[] indices;
    if (DomainSet == null) {
      throw new FieldException("Field.setSample: DomainSet undefined");
    }
    if (!((FunctionType) Type).getRange().equalsExceptName(range.getType())) {
      throw new TypeException("Field.setSample: bad range type");
    }
    if (index < 0 || index >= Length) return;

    // disect range into doubles
    double[] vals = new double[TupleDimension];
    // holder for errors of transformed values;
    ErrorEstimate[] errors_out = new ErrorEstimate[TupleDimension];
    if (range instanceof Real) {
      vals[0] = ((Real) range).getValue();
      vals = Unit.transformUnits(
                        RangeUnits[0], errors_out,
                        ((Real) range).getUnit(), ((Real) range).getError(),
                        vals);
    }
    else if (range instanceof RealTuple) {
      double[][] value = new double[TupleDimension][1];
      for (int j=0; j<TupleDimension; j++) {
        value[j][0] = ((Real) ((RealTuple) range).getComponent(j)).getValue();
      }
      value = CoordinateSystem.transformCoordinates(
                        (RealTupleType) ((FunctionType) Type).getRange(),
                        RangeCoordinateSystem, RangeUnits, errors_out,
                        (RealTupleType) range.getType(),
                        ((RealTuple) range).getCoordinateSystem(),
                        ((RealTuple) range).getTupleUnits(),
                        ((RealTuple) range).getErrors(), value);
      for (int j=0; j<TupleDimension; j++) {
        vals[j] = value[j][0];
      }
    }
    else { // range is Flat Tuple
      MathType RangeType = ((FunctionType) Type).getRange();
      int n = ((Tuple) range).getDimension();
      int j = 0;
      for (int i=0; i<n; i++) {
        Data component = ((Tuple) range).getComponent(i);
        if (component instanceof Real) {
          double[] value = new double[1];
          value[0] = ((Real) component).getValue();
          ErrorEstimate[] sub_errors_out = new ErrorEstimate[1];
          value = Unit.transformUnits(
                            RangeUnits[0], sub_errors_out,
                            ((Real) range).getUnit(), ((Real) range).getError(),
                            value);
          vals[j] = value[0];
          errors_out[j] = sub_errors_out[0];
          j++;
        }
        else {
          int m = ((RealTuple) component).getDimension();
          double[][] value = new double[m][1];
          Unit[] units_out = new Unit[m];
          ErrorEstimate[] sub_errors_out = ((RealTuple) component).getErrors();
          for (int k=0; k<m; k++) {
            value[k][0] =
              ((Real) ((RealTuple) component).getComponent(k)).getValue();
            units_out[k] = RangeUnits[j + k];
          }
          value = CoordinateSystem.transformCoordinates(
                        (RealTupleType) ((TupleType) RangeType).getComponent(i),
                        RangeCoordinateSystems[i], units_out, sub_errors_out,
                        (RealTupleType) component.getType(),
                        ((RealTuple) component).getCoordinateSystem(),
                        ((RealTuple) component).getTupleUnits(),
                        ((RealTuple) component).getErrors(), value);
          for (int k=0; k<m; k++) {
            vals[j] = value[k][0];
            errors_out[j] = sub_errors_out[k];
            j++;
          }
        }
      }
    }
    // now errors_out contains the transformed errors for the sample
    // in range - these may be mixed with RangeErrors
    // incs is counter for increase / decreas in NumberNotMissing
    int[] incs = new int[TupleDimension];

    synchronized (DoubleRange) {
      for (int i=0; i<TupleDimension; i++) {
        // test for missing
        incs[i] = (vals[i] != vals[i]) ? 0 : 1;
        switch (RangeMode[i]) {
          case DOUBLE:
            if (DoubleRange[i] == null) {
              DoubleRange[i] = new double[Length];
              for (int j=0; j<Length; j++) DoubleRange[i][j] = Double.NaN;
            }
            // test for missing
            incs[i] -= (DoubleRange[i][index] != DoubleRange[i][index]) ? 0 : 1;
            DoubleRange[i][index] = vals[i];
            break;
          case FLOAT:
            if (FloatRange[i] == null) {
              FloatRange[i] = new float[Length];
              for (int j=0; j<Length; j++) FloatRange[i][j] = Float.NaN;
            }
            // test for missing
            incs[i] -= (FloatRange[i][index] != FloatRange[i][index]) ? 0 : 1;
            FloatRange[i][index] = (float) vals[i];
            break;
          case BYTE:
            values = new double[1][1];
            values[0][0] = vals[i];
            indices = RangeSet[i].valueToIndex(Set.doubleToFloat(values));
            if (ByteRange[i] == null) {
              ByteRange[i] = new byte[Length];
              for (int j=0; j<Length; j++) ByteRange[i][j] = (byte) MISSING1;
            }
            incs[i] -= (ByteRange[i][index] == (byte) MISSING1) ? 0 : 1;
            ByteRange[i][index] = (byte) (indices[0] + MISSING1 + 1);
            break;
          case SHORT:
            values = new double[1][1];
            values[0][0] = vals[i];
            indices = RangeSet[i].valueToIndex(Set.doubleToFloat(values));
            if (ShortRange[i] == null) {
              ShortRange[i] = new short[Length];
              for (int j=0; j<Length; j++) ShortRange[i][j] = (short) MISSING2;
            }
            incs[i] -= (ShortRange[i][index] == (short) MISSING2) ? 0 : 1;
            ShortRange[i][index] = (short) (indices[0] + MISSING2 + 1);
            break;
          case INT:
            values = new double[1][1];
            values[0][0] = vals[i];
            indices = RangeSet[i].valueToIndex(Set.doubleToFloat(values));
            if (IntRange[i] == null) {
              IntRange[i] = new int[Length];
              for (int j=0; j<Length; j++) IntRange[i][j] = MISSING4;
            }
            incs[i] -= (IntRange[i][index] == (int) MISSING4) ? 0 : 1;
            IntRange[i][index] = indices[0] + MISSING4 + 1;
            break;
          default:
            throw new SetException("FlatField.unpackValues: bad RangeMode");
        }
      }
      synchronized (RangeErrors) {
        for (int i=0; i<TupleDimension; i++) {
          RangeErrors[i] = new ErrorEstimate(RangeErrors[i],
                               errors_out[i], vals[i], incs[i]);
        }
      }
    }
    clearMissing();
    notifyReferences();
  }

  /** set various arrays of range values to missing */
  private void nullRanges() throws VisADException {
    synchronized (DoubleRange) {
      // DoubleRange = new double[TupleDimension][];
      FloatRange = new float[TupleDimension][];
      LongRange = new long[TupleDimension][];
      IntRange = new int[TupleDimension][];
      ShortRange = new short[TupleDimension][];
      ByteRange = new byte[TupleDimension][];
  
      for (int i=0; i<TupleDimension; i++) {
        if (RangeSet[i] instanceof DoubleSet) {
          RangeMode[i] = DOUBLE;
        }
        else if (RangeSet[i] instanceof FloatSet) {
          RangeMode[i] = FLOAT;
        }
        else {
          int SetLength = RangeSet[i].getLength();
          if (SetLength < 256) {
            RangeMode[i] = BYTE;
          }
          else if (SetLength < 65536) {
            RangeMode[i] = SHORT;
          }
          else {
            RangeMode[i] = INT;
          }
        }
      }
    }
  }

  /** test whether Field value is missing */
  public boolean isMissing() {
    synchronized (DoubleRange) {
      return MissingFlag;
    }
  }

  /** mark this FlatField as non-missing */
  public void clearMissing() {
    synchronized (DoubleRange) {
      MissingFlag = false;
    }
  }

  /** return new Field with value 'this op data';
      test for various relations between types of this and data;
      note return type may not be FlatField,
      in case data is a Field and this matches its range */
  /*- TDR May 1998
  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
   */
  public Data binary(Data data, int op, MathType new_type,
                     int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if ( new_type == null ) {
      throw new TypeException("binary: new_type may not be null");
    }
    if (data instanceof Field) {
      /*- TDR June  1998 */
      FunctionType data_type = (FunctionType) data.getType();
      if ((data_type.getRange()).equalsExceptName(Type)) {
        if ( !data_type.equalsExceptName(new_type)) {
          throw new TypeException("binary: new_type doesn't match return type" );
        }
      /*- end   */
        // this matches range type of data;
        // note invertOp to reverse order of operands
        /*- TDR June  1998
        return data.binary(this, invertOp(op), sampling_mode, error_mode);
        */
        return data.binary(this, invertOp(op), new_type, sampling_mode, error_mode);
      }
      else if (!Type.equalsExceptName(data.getType())) {
        throw new TypeException("FlatField.binary: types don't match");
      }
      /*- TDR June 1998 */
      if ( !Type.equalsExceptName(new_type) ) {
        throw new TypeException();
      }
      /*- end */
      if (((Field) data).isFlatField()) {
        // force (data instanceof FlatField) to be true
        data = data.local();
      }
      else {
        // this and data have same type, but this is Flat and data is not
        /*- TDR June  1998
        return convertToField().binary(data, op, sampling_mode, error_mode);
        */
        return convertToField().binary(data, op, new_type, sampling_mode, error_mode);
      }

      // use DoubleSet rather than RangeSet for intermediate computation results
      if (isMissing() || data.isMissing()) return cloneMissing();

      // resample data if needed
      data = ((FlatField) data).resample(DomainSet, sampling_mode, error_mode);

      // get values from two FlatField's
      double[][] values = unpackValues();
      double[][] value2 = ((FlatField) data).unpackValues();

      // initialize for Unit and ErrorEstimate calculations
      Unit[][] temp_units = ((FlatField) data).getRangeUnits();
      Unit[] units_in = new Unit[temp_units.length];
      for (int i=0; i<temp_units.length; i++) {
        units_in[i] = temp_units[i][0];
      }
      ErrorEstimate[] errors_in = ((FlatField) data).getRangeErrors();
      // substitute arrays of nulls for null arrays
      if (units_in == null) units_in = new Unit[TupleDimension];
      if (errors_in == null) errors_in = new ErrorEstimate[TupleDimension];
      Unit[] units_out = new Unit[TupleDimension];
      ErrorEstimate[] errors_out = new ErrorEstimate[TupleDimension];

      // apply any range coordinate transformations
      if (((FunctionType) Type).getReal()) {
        // transformCoordinatesFreeUnits does not impose any
        // particular Units on the final value2
        CoordinateSystem[] cs =
          ((FlatField) data).getRangeCoordinateSystem();
        value2 = CoordinateSystem.transformCoordinatesFreeUnits(
                   ((FunctionType) Type).getFlatRange(),
                   RangeCoordinateSystem, units_out, errors_out,
                   ((FunctionType) data.getType()).getFlatRange(),
                   cs[0], units_in, errors_in, value2);
      }      else if (RangeCoordinateSystems != null) {
        TupleType rtype =
          (TupleType) ((FunctionType) Type).getRange();
        TupleType dtype =
          (TupleType) ((FunctionType) data.getType()).getRange();
        int n = rtype.getDimension();
        int j = 0;
        for (int i=0; i<n; i++) {
          MathType crtype = rtype.getComponent(i);
          MathType cdtype = dtype.getComponent(i);
          if (crtype instanceof RealTupleType) {
            int m = ((RealTupleType) crtype).getDimension();
            double[][] vals = new double[m][];
            Unit[] sub_units_out = new Unit[m];
            Unit[] sub_units_in = new Unit[m];
            ErrorEstimate[] sub_errors_out = new ErrorEstimate[m];
            ErrorEstimate[] sub_errors_in = new ErrorEstimate[m];
            for (int k=0; k<m; k++) {
              vals[k] = value2[j + k];
              sub_units_in[k] = units_in[j + k];
              sub_errors_in[k] = errors_in[j + k];
            }
            CoordinateSystem[] cs =
              ((FlatField) data).getRangeCoordinateSystem(i);
            vals = CoordinateSystem.transformCoordinatesFreeUnits(
                     (RealTupleType) crtype, RangeCoordinateSystems[i],
                     sub_units_out, sub_errors_out,
                     (RealTupleType) cdtype,
                     cs[0], sub_units_in, sub_errors_in, vals);
            for (int k=0; k<m; k++) {
              value2[j + k] = vals[k];
              errors_out[j + k] = sub_errors_out[k];
              units_out[j + k] = sub_units_out[k];
            }
            j += m;
          }
          else {
            errors_out[j] = errors_in[j];
            units_out[j] = units_in[j];
            j++;
          }
        }
      }

/*
 roles from Real.binary:
   RangeUnits[j]     --  unit
   units_out[j]     --  data_unit, u
   RangeErrors[j]   --  Error
   errors_out[j]    --  dError
*/

      int i, j; // loop indices
      double[] valuesJ, value2J;
      switch (op) {
        case ADD:
        case SUBTRACT:
        case INV_SUBTRACT:
        case MAX:
        case MIN:
          for (j=0; j<TupleDimension; j++) {
            Unit u;

            if (RangeUnits[j] == null || units_out[j] == null) {
              u = null;
            }
            else if (RangeUnits[j] == CommonUnit.promiscuous) {
              u = units_out[j];
            }
            else if (units_out[j] == CommonUnit.promiscuous) {
              u = RangeUnits[j];
            }
            else if (Unit.canConvert(RangeUnits[j], units_out[j])) {
              u = RangeUnits[j];
              value2[j] = RangeUnits[j].toThis(value2[j], units_out[j]);
              // scale data.ErrorEstimate for Unit.toThis
              if (error_mode != NO_ERRORS && errors_out[j] != null) {
                double error = 0.5 * errors_out[j].getErrorValue();
                double mean = errors_out[j].getMean();
                double a = RangeUnits[j].toThis(mean + error, units_out[j]);
                double b = RangeUnits[j].toThis(mean - error, units_out[j]);
                double new_error = Math.abs(a - b);
                double new_mean = 0.5 * (a + b);
                errors_out[j] =
                  new ErrorEstimate(new_mean, new_error, RangeUnits[j]);
              }
            }
            else {
              u = null;
            }
            units_out[j] = u;
          }
          switch (op) {
            case ADD:
              for (j=0; j<TupleDimension; j++) {
                valuesJ = values[j];
                value2J = value2[j];
                for (i=0; i<Length; i++) {
                  valuesJ[i] += value2J[i];
                }
              }
              break;
            case SUBTRACT:
              for (j=0; j<TupleDimension; j++) {
                valuesJ = values[j];
                value2J = value2[j];
                for (i=0; i<Length; i++) {
                  valuesJ[i] -= value2J[i];
                }
              }
              break;
            case INV_SUBTRACT:
              for (j=0; j<TupleDimension; j++) {
                valuesJ = values[j];
                value2J = value2[j];
                for (i=0; i<Length; i++) {
                  valuesJ[i] = value2J[i] - valuesJ[i];
                }
              }
              break;
            case MAX:
              for (j=0; j<TupleDimension; j++) {
                valuesJ = values[j];
                value2J = value2[j];
                for (i=0; i<Length; i++) {
                  valuesJ[i] = Math.max(valuesJ[i], value2J[i]);
                }
              }
              break;
            case MIN:
            default:
              for (j=0; j<TupleDimension; j++) {
                valuesJ = values[j];
                value2J = value2[j];
                for (i=0; i<Length; i++) {
                  valuesJ[i] = Math.min(valuesJ[i], value2J[i]);
                }
              }
              break;
          }
          break;
        case MULTIPLY:
          for (j=0; j<TupleDimension; j++) {
            valuesJ = values[j];
            value2J = value2[j];
            for (i=0; i<Length; i++) {
              valuesJ[i] *= value2J[i];
            }
            if (RangeUnits[j] == null || units_out[j] == null) {
              units_out[j] = null;
            }
            else {
              units_out[j] = RangeUnits[j].multiply(units_out[j]);
            }
          }
          break;
        case DIVIDE:
          for (j=0; j<TupleDimension; j++) {
            valuesJ = values[j];
            value2J = value2[j];
            for (i=0; i<Length; i++) {
              valuesJ[i] /= value2J[i];
            }
            if (RangeUnits[j] == null || units_out[j] == null) {
              units_out[j] = null;
            }
            else {
              units_out[j] = RangeUnits[j].divide(units_out[j]);
            }
          }
          break;
        case INV_DIVIDE:
          for (j=0; j<TupleDimension; j++) {
            valuesJ = values[j];
            value2J = value2[j];
            for (i=0; i<Length; i++) {
              valuesJ[i] = value2J[i] / valuesJ[i];
            }
            if (RangeUnits[j] == null || units_out[j] == null) {
              units_out[j] = null;
            }
            else {
              units_out[j] = units_out[j].divide(RangeUnits[j]);
            }
          }
          break;
        case POW:
          for (j=0; j<TupleDimension; j++) {
            valuesJ = values[j];
            value2J = value2[j];
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.pow(valuesJ[i], value2J[i]);
            }
            units_out[j] = null;
          }
          break;
        case INV_POW:
          for (j=0; j<TupleDimension; j++) {
            valuesJ = values[j];
            value2J = value2[j];
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.pow(value2J[i], valuesJ[i]);
            }
            units_out[j] = null;
          }
          break;
        case ATAN2:
          for (j=0; j<TupleDimension; j++) {
            valuesJ = values[j];
            value2J = value2[j];
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.atan2(valuesJ[i], value2J[i]);
            }
            units_out[j] = CommonUnit.radian;
          }
          break;
        case ATAN2_DEGREES:
          for (j=0; j<TupleDimension; j++) {
            valuesJ = values[j];
            value2J = value2[j];
            for (i=0; i<Length; i++) {
              valuesJ[i] = Data.RADIANS_TO_DEGREES *
                           Math.atan2(valuesJ[i], value2J[i]);
            }
            units_out[j] = CommonUnit.degree;
          }
          break;
        case INV_ATAN2:
          for (j=0; j<TupleDimension; j++) {
            valuesJ = values[j];
            value2J = value2[j];
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.atan2(value2J[i], valuesJ[i]);
            }
            units_out[j] = CommonUnit.radian;
          }
          break;
        case INV_ATAN2_DEGREES:
          for (j=0; j<TupleDimension; j++) {
            valuesJ = values[j];
            value2J = value2[j];
            for (i=0; i<Length; i++) {
              valuesJ[i] = Data.RADIANS_TO_DEGREES *
                           Math.atan2(value2J[i], valuesJ[i]);
            }
            units_out[j] = CommonUnit.degree;
          }
          break;
        case REMAINDER:
          for (j=0; j<TupleDimension; j++) {
            valuesJ = values[j];
            value2J = value2[j];
            for (i=0; i<Length; i++) {
              valuesJ[i] %= value2J[i];
            }
            units_out[j] = RangeUnits[j];
          }
          break;
        case INV_REMAINDER:
          for (j=0; j<TupleDimension; j++) {
            valuesJ = values[j];
            value2J = value2[j];
            for (i=0; i<Length; i++) {
              valuesJ[i] = value2J[i] % valuesJ[i];
            }
          }
          break;
      }

      // compute ErrorEstimates for result
      for (j=0; j<TupleDimension; j++) {
        if (error_mode == NO_ERRORS ||
            RangeErrors[j] == null || errors_out[j] == null) {
          errors_out[j] = null;
        }
        else {
          errors_out[j] =
            new ErrorEstimate(values[j], units_out[j], op, RangeErrors[j],
                              errors_out[j], error_mode);
        }
      }

      // create a FlatField for return
      /*- TDR June  1998
      FlatField new_field = cloneDouble(units_out, errors_out);
      */
      FlatField new_field = cloneDouble( new_type, units_out, errors_out);

      new_field.packValues(values, false);
      // new_field.DoubleRange = values;
      new_field.clearMissing();
      return new_field;
    }
    else if (data instanceof Real || data instanceof RealTuple ||
             (data instanceof Tuple && ((TupleType) data.getType()).getFlat())) {
      MathType RangeType = ((FunctionType) Type).getRange();
      if (!RangeType.equalsExceptName(data.getType())) {
        throw new TypeException("FlatField.binary: types don't match");
      }
      /*- TDR June 1998 */
      if ( !Type.equalsExceptName(new_type)) {
        throw new TypeException("binary: new_type doesn't match return type");
      }
      /*- end */

      // use DoubleSet rather than RangeSet for intermediate computation results
      if (isMissing() || data.isMissing()) return cloneDouble();

      double[][] values = unpackValues();
      // get data values and possibly apply coordinate transform
      double[][] vals = new double[TupleDimension][1];

      Unit[] units_out = new Unit[TupleDimension];
      ErrorEstimate[] errors_out = new ErrorEstimate[TupleDimension];


      if (data instanceof Real) {
        // no need for Unit conversion - just pass Unit into binary ops
        for (int j=0; j<TupleDimension; j++) {
          vals[j][0] = ((Real) data).getValue();
          units_out[j] = ((Real) data).getUnit();
          errors_out[j] = ((Real) data).getError();
        }
      }
      else if (data instanceof RealTuple) {
        for (int j=0; j<TupleDimension; j++) {
          vals[j][0] = ((Real) ((RealTuple) data).getComponent(j)).getValue();
        }
        vals = CoordinateSystem.transformCoordinatesFreeUnits(
                        ((FunctionType) Type).getFlatRange(),
                        RangeCoordinateSystem, units_out, errors_out,
                        (RealTupleType) data.getType(),
                        ((RealTuple) data).getCoordinateSystem(),
                        ((RealTuple) data).getTupleUnits(),
                        ((RealTuple) data).getErrors(), vals);
      }
      else { // (data instanceof Tuple && !(data instanceof RealTuple))
        int n = ((Tuple) data).getDimension();
        int j = 0;
        for (int i=0; i<n; i++) {
          Data component = ((Tuple) data).getComponent(i);
          if (component instanceof Real) {
            // no need for Unit conversion - just pass Unit into binary ops
            vals[j][0] = ((Real) component).getValue();
            units_out[j] = ((Real) component).getUnit();
            errors_out[j] = ((Real) component).getError();
            j++;
          }
          else { // (component instanceof RealTuple)
            int m = ((Tuple) component).getDimension();
            double[][] tvals = new double[m][1];
            Unit[] sub_units_out = new Unit[m];
            ErrorEstimate[] sub_errors_out = new ErrorEstimate[m];
            for (int k=0; k<m; k++) {
              tvals[k][0] = ((Real) ((Tuple) component).getComponent(k)).getValue();
            }
            tvals = CoordinateSystem.transformCoordinatesFreeUnits(
                        (RealTupleType) ((TupleType) RangeType).getComponent(i),
                        RangeCoordinateSystems[i], sub_units_out, sub_errors_out,
                        (RealTupleType) component.getType(),
                        ((RealTuple) component).getCoordinateSystem(),
                        ((RealTuple) component).getTupleUnits(),
                        ((RealTuple) component).getErrors(), tvals);
            for (int k=0; k<m; k++) {
              vals[j + k][0] = tvals[k][0];
              units_out[j + k] = sub_units_out[k];
              errors_out[j + k] = sub_errors_out[k];
            }
            j += m;
          }
        }
      }

/*
 roles from Real.binary:
   RangeUnits[j]     --  unit
   units_out[j]     --  data_unit, u
   RangeErrors[j]   --  Error
   errors_out[j]    --  dError
*/

      for (int j=0; j<TupleDimension; j++) {
        double value = vals[j][0];
        double[] valuesJ = values[j];
        switch (op) {
          case ADD:
          case SUBTRACT:
          case INV_SUBTRACT:
          case MAX:
          case MIN:
            Unit u;

            if (RangeUnits[j] == null || units_out[j] == null) {
              u = null;
            }
            else if (RangeUnits[j] == CommonUnit.promiscuous) {
              u = units_out[j];
            }
            else if (units_out[j] == CommonUnit.promiscuous) {
              u = RangeUnits[j];
            }
            else if (Unit.canConvert(RangeUnits[j], units_out[j])) {
              u = RangeUnits[j];
              value = RangeUnits[j].toThis(value, units_out[j]);
              if (error_mode == NO_ERRORS && errors_out[j] != null) {
                // scale data.ErrorEstimate for Unit.toThis
                double error = 0.5 * errors_out[j].getErrorValue();
                double mean = errors_out[j].getMean();
                double a = RangeUnits[j].toThis(mean + error, units_out[j]);
                double b = RangeUnits[j].toThis(mean - error, units_out[j]);
                double new_error = Math.abs(a - b);
                double new_mean = 0.5 * (a + b);
                errors_out[j] =
                  new ErrorEstimate(new_mean, new_error, RangeUnits[j]);
              }
            }
            else {
              u = null;
            }
            units_out[j] = u;
            switch (op) {
              case ADD:
                for (int i=0; i<Length; i++) {
                  valuesJ[i] += value;
                }
                break;
              case SUBTRACT:
                for (int i=0; i<Length; i++) {
                  valuesJ[i] -= value;
                }
                break;
              case INV_SUBTRACT:
                for (int i=0; i<Length; i++) {
                  valuesJ[i] = value - valuesJ[i];
                }
                break;
              case MAX:
                for (int i=0; i<Length; i++) {
                  valuesJ[i] = Math.max(valuesJ[i], value);
                }
                break;
              case MIN:
              default:
                for (int i=0; i<Length; i++) {
                  valuesJ[i] = Math.min(valuesJ[i], value);
                }
                break;
            }
            break;
          case MULTIPLY:
            for (int i=0; i<Length; i++) {
              valuesJ[i] *= value;
            }
            if (RangeUnits[j] == null || units_out[j] == null) {
              units_out[j] = null;
            }
            else {
              units_out[j] = RangeUnits[j].multiply(units_out[j]);
            }
            break;
          case DIVIDE:
            for (int i=0; i<Length; i++) {
              valuesJ[i] /= value;
            }
            if (RangeUnits[j] == null || units_out[j] == null) {
              units_out[j] = null;
            }
            else {
              units_out[j] = RangeUnits[j].divide(units_out[j]);
            }
            break;
          case INV_DIVIDE:
            for (int i=0; i<Length; i++) {
              valuesJ[i] = value / valuesJ[i];
            }
            if (RangeUnits[j] == null || units_out[j] == null) {
              units_out[j] = null;
            }
            else {
              units_out[j] = units_out[j].divide(RangeUnits[j]);
            }
            break;
          case POW:
            for (int i=0; i<Length; i++) {
              valuesJ[i] = Math.pow(valuesJ[i], value);
            }
            units_out[j] = null;
            break;
          case INV_POW:
            for (int i=0; i<Length; i++) {
              valuesJ[i] = Math.pow(value, valuesJ[i]);
            }
            units_out[j] = null;
            break;
          case ATAN2:
            for (int i=0; i<Length; i++) {
              valuesJ[i] = Math.atan2(valuesJ[i], value);
            }
            units_out[j] = CommonUnit.radian;
            break;
          case ATAN2_DEGREES:
            for (int i=0; i<Length; i++) {
              valuesJ[i] = Data.RADIANS_TO_DEGREES *
                           Math.atan2(valuesJ[i], value);
            }
            units_out[j] = CommonUnit.degree;
            break;
          case INV_ATAN2:
            for (int i=0; i<Length; i++) {
              valuesJ[i] = Math.atan2(value, valuesJ[i]);
            }
            units_out[j] = CommonUnit.radian;
            break;
          case INV_ATAN2_DEGREES:
            for (int i=0; i<Length; i++) {
              valuesJ[i] = Data.RADIANS_TO_DEGREES *
                           Math.atan2(value, valuesJ[i]);
            }
            units_out[j] = CommonUnit.degree;
            break;
          case REMAINDER:
            for (int i=0; i<Length; i++) {
              valuesJ[i] %= value;
            }
            units_out[j] = RangeUnits[j];
            break;
          case INV_REMAINDER:
            for (int i=0; i<Length; i++) {
              valuesJ[i] = value % valuesJ[i];
            }
            // units_out[j] = units_out[j];
            break;
        }
      } // end for (int j=0; j<TupleDimension; j++)

      // compute ErrorEstimates for result
      for (int j=0; j<TupleDimension; j++) {
        if (error_mode == NO_ERRORS ||
            RangeErrors[j] == null || errors_out[j] == null) {
          errors_out[j] = null;
        }
        else {
          errors_out[j] =
            new ErrorEstimate(values[j], units_out[j], op, RangeErrors[j],
                              errors_out[j], error_mode);
        }
      }

      // create a FlatField for return
      /*- TDR June  1998
      FlatField new_field = cloneDouble(units_out, errors_out);
      */
      FlatField new_field = cloneDouble( new_type, units_out, errors_out);

      new_field.packValues(values, false);
      // new_field.DoubleRange = values;
      new_field.clearMissing();
      return new_field;
    }
    else {
      throw new TypeException("Field.binary");
    }
  }


  /** return new FlatField with value 'op this' */
  public Data unary(int op, MathType new_type, int sampling_mode, int error_mode)
              throws VisADException {
    // use DoubleSet rather than RangeSet for intermediate computation results
    if (isMissing()) return cloneDouble();

    /*- TDR July 1998  */
    if ( new_type == null ) {
      throw new TypeException("unary: new_type may not be null");
    }

    double[][] values = unpackValues();

    Unit[] units_out = new Unit[TupleDimension];

    int i, j; // loop indices
    double[] valuesJ;
    switch (op) {
      case ABS:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Math.abs(valuesJ[i]);
          }
          units_out[j] = RangeUnits[j];
        }
        break;
      case ACOS:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Math.acos(valuesJ[i]);
          }
          units_out[j] = CommonUnit.radian;
        }
        break;
      case ACOS_DEGREES:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Data.RADIANS_TO_DEGREES * Math.acos(valuesJ[i]);
          }
          units_out[j] = CommonUnit.degree;
        }
        break;
      case ASIN:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Math.asin(valuesJ[i]);
          }
          units_out[j] = CommonUnit.radian;
        }
        break;
      case ASIN_DEGREES:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Data.RADIANS_TO_DEGREES * Math.asin(valuesJ[i]);
          }
          units_out[j] = CommonUnit.degree;
        }
        break;
      case ATAN:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Math.atan(valuesJ[i]);
          }
          units_out[j] = CommonUnit.radian;
        }
        break;
      case ATAN_DEGREES:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Data.RADIANS_TO_DEGREES * Math.atan(valuesJ[i]);
          }
          units_out[j] = CommonUnit.degree;
        }
        break;
      case CEIL:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Math.ceil(valuesJ[i]);
          }
          units_out[j] = RangeUnits[j];
        }
        break;
      case COS:
        // do cos in degrees, unless unit is radians
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          if (CommonUnit.degree.equals(RangeUnits[j])) {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.cos(Data.DEGREES_TO_RADIANS * valuesJ[i]);
            }
          }
          else {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.cos(valuesJ[i]);
            }
          }
          units_out[j] =
            CommonUnit.dimensionless.equals(RangeUnits[j]) ? RangeUnits[j] : null;
        }
        break;
      case COS_DEGREES:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          if (CommonUnit.radian.equals(RangeUnits[j])) {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.cos(valuesJ[i]);
            }
          }
          else {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.cos(Data.DEGREES_TO_RADIANS * valuesJ[i]);
            }
          }
          units_out[j] =
            CommonUnit.dimensionless.equals(RangeUnits[j]) ? RangeUnits[j] : null;
        }
        break;
      case EXP:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Math.exp(valuesJ[i]);
          }
          units_out[j] =
            CommonUnit.dimensionless.equals(RangeUnits[j]) ? RangeUnits[j] : null;
        }
        break;
      case FLOOR:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Math.floor(valuesJ[i]);
          }
          units_out[j] = RangeUnits[j];
        }
        break;
      case LOG:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Math.log(valuesJ[i]);
          }
          units_out[j] =
            CommonUnit.dimensionless.equals(RangeUnits[j]) ? RangeUnits[j] : null;
        }
        break;
      case RINT:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Math.rint(valuesJ[i]);
          }
          units_out[j] = RangeUnits[j];
        }
        break;
      case ROUND:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Math.round(valuesJ[i]);
          }
          units_out[j] = RangeUnits[j];
        }
        break;
      case SIN:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          if (CommonUnit.degree.equals(RangeUnits[j])) {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.sin(Data.DEGREES_TO_RADIANS * valuesJ[i]);
            }
          }
          else {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.sin(valuesJ[i]);
            }
          }
          units_out[j] =
            CommonUnit.dimensionless.equals(RangeUnits[j]) ? RangeUnits[j] : null;
        }
        break;
      case SIN_DEGREES:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          if (CommonUnit.radian.equals(RangeUnits[j])) {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.sin(valuesJ[i]);
            }
          }
          else {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.sin(Data.DEGREES_TO_RADIANS * valuesJ[i]);
            }
          }
          units_out[j] =
            CommonUnit.dimensionless.equals(RangeUnits[j]) ? RangeUnits[j] : null;
        }
        break;
      case SQRT:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = Math.sqrt(valuesJ[i]);
          }
          units_out[j] =
            CommonUnit.dimensionless.equals(RangeUnits[j]) ? RangeUnits[j] : null;
        }
        break;
      case TAN:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          if (CommonUnit.degree.equals(RangeUnits[j])) {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.tan(Data.DEGREES_TO_RADIANS * valuesJ[i]);
            }
          }
          else {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.tan(valuesJ[i]);
            }
          }
          units_out[j] =
            CommonUnit.dimensionless.equals(RangeUnits[j]) ? RangeUnits[j] : null;
        }
        break;
      case TAN_DEGREES:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          if (CommonUnit.radian.equals(RangeUnits[j])) {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.tan(valuesJ[i]);
            }
          }
          else {
            for (i=0; i<Length; i++) {
              valuesJ[i] = Math.tan(Data.DEGREES_TO_RADIANS * valuesJ[i]);
            }
          }
          units_out[j] =
            CommonUnit.dimensionless.equals(RangeUnits[j]) ? RangeUnits[j] : null;
        }
        break;
      case NEGATE:
        for (j=0; j<TupleDimension; j++) {
          valuesJ = values[j];
          for (i=0; i<Length; i++) {
            valuesJ[i] = -valuesJ[i];
          }
          units_out[j] = RangeUnits[j];
        }
        break;
    }

    // compute ErrorEstimates for result
    ErrorEstimate[] errors_out = new ErrorEstimate[TupleDimension];
    for (j=0; j<TupleDimension; j++) {
      if (error_mode == NO_ERRORS || RangeErrors[j] == null) {
        errors_out[j] = null;
      }
      else {
        errors_out[j] = new ErrorEstimate(values[j], units_out[j], op,
                                          RangeErrors[j], error_mode);
      }
    }

    // create a FlatField for return
    /*- TDR July 1998
    FlatField new_field = cloneDouble(units_out, errors_out);
    */
    FlatField new_field = cloneDouble(new_type, units_out, errors_out);

    new_field.packValues(values, false);
    // new_field.DoubleRange = values; 
    new_field.clearMissing();
    return new_field;
  }

  /** combine an array of FlatFields;
      they must have the same Domain type;
      this takes the place of 'insert' in the C-based VisAD */
  public Field combine(FlatField[] fields) throws VisADException {
    throw new UnimplementedException("FlatField.combine");
  }

  /** extract field from this[].component;
      this is OK, when we get around to it */
  public Field extract(int component) throws VisADException {
    throw new UnimplementedException("FlatField.insert");
  }

  /** resample range values of this to domain samples in set,
      either by nearest neighbor or mulit-linear interpolation;
      RangeSet objects in result are set to DoubleSet
      NOTE may return this (i.e., not a copy);
      NOTE this code is very similar to resample in Field.java */
  public Field resample(Set set, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (DomainSet.equals(set)) {
      // nothing to do
      return this;
    }

    int dim = DomainSet.getDimension();
    if (dim != set.getDimension()) {
      throw new SetException("FlatField.resample: bad Set Dimension");
    }

    CoordinateSystem coord_sys = set.getCoordinateSystem();
    Unit[] units = set.getSetUnits();
    ErrorEstimate[] errors =
      (error_mode == NO_ERRORS) ? new ErrorEstimate[dim] : set.getSetErrors();

    // create (initially missing) FlatField for return
    // use DoubleSet rather than RangeSet for intermediate computation results
    Set[] sets = new Set[TupleDimension];
    for (int i=0; i<TupleDimension; i++) {
      SetType set_type = 
        new SetType(((FunctionType) Type).getFlatRange().getComponent(i));
      sets[i] = new DoubleSet(set_type);
    }
    FlatField new_field =
      new FlatField((FunctionType) Type, set, RangeCoordinateSystem,
                    RangeCoordinateSystems, sets, RangeUnits);
    if (isMissing()) return new_field;

    float[][] values = Set.doubleToFloat(unpackValues());
    ErrorEstimate[] range_errors_in =
      (error_mode == NO_ERRORS) ? new ErrorEstimate[TupleDimension] :
                                  RangeErrors;
    ErrorEstimate[] range_errors_out = range_errors_in;

    int i, j, k; // loop indices

    // create an array containing all indices of 'this'
    int length = set.getLength();
    int[] wedge = set.getWedge();

    double[][] new_values = new double[TupleDimension][length];
    double[] new_valuesJ;
    float[] valuesJ;

    // get values from wedge and possibly transform coordinates
    float[][] vals = set.indexToValue(wedge);
    // holder for sampling errors of transformed set; these are
    // only useful to help estmate range errors due to resampling
    ErrorEstimate[] errors_out = new ErrorEstimate[dim];
    float[][] oldvals = vals;
    vals = CoordinateSystem.transformCoordinates(
                      ((FunctionType) Type).getDomain(), DomainCoordinateSystem,
                      DomainUnits, errors_out,
                      ((SetType) set.getType()).getDomain(), coord_sys,
                      units, errors, vals);
    boolean coord_transform = (vals == oldvals);
    oldvals = null; // enable oldvals to be garbage collected

    // check whether we need to do sampling error calculations
    boolean sampling_errors = (error_mode != NO_ERRORS);
    if (sampling_errors) {
      for (i=0; i<dim; i++) {
        if (errors_out[i] == null) sampling_errors = false;
      }
      boolean any_range_error = false;
      for (i=0; i<TupleDimension; i++) {
        if (range_errors_in[i] != null) any_range_error = true;
      }
      if (!any_range_error) sampling_errors = false;
    }
    float[][] sampling_partials = new float[TupleDimension][dim];
    float[][] error_values = new float[1][1];
    if (sampling_errors) {
      error_values = Set.doubleToFloat(
                     ErrorEstimate.init_error_values(errors_out) );
    }

    if (sampling_mode == WEIGHTED_AVERAGE && DomainSet instanceof SimpleSet) {
      // resample by interpolation
      int[][] indices = new int[length][];
      float[][] coefs = new float[length][];
      ((SimpleSet) DomainSet).valueToInterp(vals, indices, coefs);

/* DEBUG
System.out.println("DomainSet = " + DomainSet);
System.out.println("set = " + set);

for (i=0; i<length; i++) {
  System.out.println("vals[0][" + i + "] = " + vals[0][i] +
                    " vals[1][" + i + "] = " + vals[1][i]);
  String s = "indices[" + i + "] = ";
  for (j=0; j<indices[i].length; j++) s = s + indices[i][j] + " ";
  System.out.println(s);
  s = "coefs[" + i + "] = ";
  for (j=0; j<coefs[i].length; j++) s = s + coefs[i][j] + " ";
  System.out.println(s);
}
*/
      for (j=0; j<TupleDimension; j++) {
        valuesJ = values[j];
        new_valuesJ = new_values[j];
        for (i=0; i<length; i++) {
          float v = Float.NaN;
          int len = indices[i].length;
          if (len > 0) {
            v = valuesJ[indices[i][0]] * coefs[i][0];
            for (k=1; k<len; k++) {
              v += valuesJ[indices[i][k]] * coefs[i][k];
            }
            new_valuesJ[wedge[i]] = v;
          }
          else { // values outside grid
            new_valuesJ[wedge[i]] = Double.NaN;
          }
        }
      }

      if (sampling_errors) {
        int[][] error_indices = new int[2 * dim][];
        float[][] error_coefs = new float[2 * dim][];
        ((SimpleSet) DomainSet).valueToInterp(error_values, error_indices,
                                              error_coefs);

        for (j=0; j<TupleDimension; j++) {
          for (i=0; i<dim; i++) {
            float a = Float.NaN;
            float b = Float.NaN;;
            int len = error_indices[2*i].length;
            if (len > 0) {
              a = values[j][error_indices[2*i][0]] * error_coefs[2*i][0];
              for (k=1; k<len; k++) {
                a += values[j][error_indices[2*i][k]] * error_coefs[2*i][k];
              }
            }
            len = error_indices[2*i+1].length;
            if (len > 0) {
              b = values[j][error_indices[2*i+1][0]] * error_coefs[2*i+1][0];
              for (k=1; k<len; k++) {
                b += values[j][error_indices[2*i+1][k]] * error_coefs[2*i+1][k];
              }
            }
            sampling_partials[j][i] = Math.abs(b - a);
          }
        }
      }

    }
    else { // NEAREST_NEIGHBOR or set is not SimpleSet
      // simple resampling
      int[] indices = DomainSet.valueToIndex(vals);
      for (j=0; j<TupleDimension; j++) {
        valuesJ = values[j];
        new_valuesJ = new_values[j];
        for (i=0; i<length; i++) {
          new_valuesJ[wedge[i]] =
            ((indices[i] >= 0) ? valuesJ[indices[i]]: Double.NaN);
        }
      }

      if (sampling_errors) {
        int[] error_indices = DomainSet.valueToIndex(error_values);
        for (j=0; j<TupleDimension; j++) {
          for (i=0; i<dim; i++) {
            float a = (float) ((error_indices[2*i] >= 0) ?
                       values[j][error_indices[2*i]]: Double.NaN);
            float b = (float) ((error_indices[2*i+1] >= 0) ?
                       values[j][error_indices[2*i+1]]: Double.NaN);
            sampling_partials[j][i] = Math.abs(b - a);
          }
        }
      }

    }

    if (sampling_errors) {
      for (j=0; j<TupleDimension; j++) {
        if (range_errors_in[j] != null) {
          float error = (float) range_errors_in[j].getErrorValue();
          if (error_mode == Data.INDEPENDENT) {
            error = error * error;
            for (i=0; i<dim; i++) {
              error += sampling_partials[j][i] * sampling_partials[j][i];
            }
            error = (float) Math.sqrt(error);
          }
          else { // error_mode == Data.DEPENDENT
            for (i=0; i<dim; i++) {
              error += sampling_partials[j][i];
            }
          }
          range_errors_out[j] =
            new ErrorEstimate(new_values[j], error, RangeUnits[j]); 
        }
      }
    }
    else if (error_mode != NO_ERRORS) {
      for (j=0; j<TupleDimension; j++) {
        if (range_errors_in[j] != null) {
          range_errors_out[j] =
            new ErrorEstimate(new_values[j], range_errors_in[j].getErrorValue(),
                              RangeUnits[j]); 
        }
      }
    }

    if (coord_transform) {
      range_errors_in = range_errors_out;
      MathType Range = ((FunctionType) Type).getRange();
      if (Range instanceof RealVectorType) {
        new_values = ((RealVectorType) Range).transformVectors(
                      ((FunctionType) Type).getDomain(),
                      DomainCoordinateSystem, DomainUnits, errors_out,
                      ((SetType) set.getType()).getDomain(),
                      coord_sys, units, RangeCoordinateSystem,
                      range_errors_in, range_errors_out,
                      Set.floatToDouble(vals), new_values);
      }
      else if (Range instanceof TupleType && !(Range instanceof RealTupleType)) {
        int offset = 0;
        int m = ((TupleType) Range).getDimension();
        for (j=0; j<m; j++) {
          MathType comp_type = ((TupleType) Range).getComponent(j);
          if (comp_type instanceof RealVectorType) {
            int mm = ((RealVectorType) comp_type).getDimension();
            double[][] comp_vals = new double[mm][];
            for (int jj=0; jj<mm; jj++) {
              comp_vals[jj] = new_values[offset + jj];
            }
            ErrorEstimate[] comp_errors_in = new ErrorEstimate[mm];
            for (int jj=0; jj<mm; jj++) {
              comp_errors_in[jj] = range_errors_in[offset + jj];
            }
            ErrorEstimate[] comp_errors_out = comp_errors_in;
            comp_vals = ((RealVectorType) comp_type).transformVectors(
                        ((FunctionType) Type).getDomain(),
                        DomainCoordinateSystem, DomainUnits, errors_out,
                        ((SetType) set.getType()).getDomain(), coord_sys, units,
                        RangeCoordinateSystems[j],
                        comp_errors_in, comp_errors_out,
                        Set.floatToDouble(vals), comp_vals);
            for (int jj=0; jj<mm; jj++) {
              new_values[offset + jj] = comp_vals[jj];
            }
            for (int jj=0; jj<mm; jj++) {
              range_errors_out[offset + jj] = comp_errors_out[jj];
            }
          }
          if (comp_type instanceof RealType) {
            offset++;
          }
          else {
            offset += ((RealTupleType) comp_type).getDimension();
          }
        }
      }
    }
    new_field.packValues(new_values, false);
    // new_field.DoubleRange = new_values; 
    new_field.setRangeErrors(range_errors_out);
    new_field.clearMissing();
    return new_field;
  }

  /** convert this FlatField to a (non-Flat) Field */
  public Field convertToField() throws VisADException, RemoteException {
    Field new_field = new FieldImpl((FunctionType) Type, DomainSet);
    if (isMissing()) return new_field;
    for (int i=0; i<Length; i++) {
      new_field.setSample(i, getSample(i));
    }
    return new_field;
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException {
    if (isMissing()) return shadow;

    ShadowRealTupleType domain_type = ((ShadowFunctionType) type).getDomain();
    int n = domain_type.getDimension();
    double[][] ranges = new double[2][n];
    // DomainSet.computeRanges handles Reference
    shadow = DomainSet.computeRanges(domain_type, shadow, ranges, true);
    ShadowRealTupleType shad_ref;
    // skip range if no range components are mapped
    int[] indices = ((ShadowFunctionType) type).getRangeDisplayIndices();
    boolean any_mapped = false;
    for (int i=0; i<TupleDimension; i++) {
      if (indices[i] >= 0) any_mapped = true;
    }
    if (!any_mapped) return shadow;

    // check for any range coordinate systems
    boolean anyRangeRef = (RangeCoordinateSystem != null);
    if (RangeCoordinateSystems != null) {
      for (int i=0; i<RangeCoordinateSystems.length; i++) {
        anyRangeRef |= (RangeCoordinateSystems[i] != null);
      }
    }
    ranges = anyRangeRef ? new double[2][TupleDimension] : null;

    // get range values
    double[][] values = unpackValues();
    for (int i=0; i<TupleDimension; i++) {
      double[] valuesI = values[i];
      int k = indices[i];
      if (k >= 0 || anyRangeRef) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int j=0; j<Length; j++) {
          if (valuesI[j] == valuesI[j]) {
            min = Math.min(min, valuesI[j]);
            max = Math.max(max, valuesI[j]);
          }
        }
        Unit dunit = ((RealType)
          ((FunctionType) Type).getFlatRange().getComponent(i)).getDefaultUnit();
        if (dunit != null && !dunit.equals(RangeUnits[i])) {
          min = dunit.toThis(min, RangeUnits[i]);
          max = dunit.toThis(max, RangeUnits[i]);
        }
        if (anyRangeRef) {
          ranges[0][i] = Math.min(ranges[0][i], min);
          ranges[1][i] = Math.max(ranges[1][i], max);
        }
        if (k >= 0) {
          shadow.ranges[0][k] = Math.min(shadow.ranges[0][k], min);
          shadow.ranges[1][k] = Math.max(shadow.ranges[1][k], max);
        }
      }
    }
    if (RangeCoordinateSystem != null) {
      // computeRanges for Reference (relative to range) RealTypes
      ShadowRealTupleType range_type =
        (ShadowRealTupleType) ((ShadowFunctionType) type).getRange();
      shad_ref = range_type.getReference();
      shadow = computeReferenceRanges(range_type, RangeCoordinateSystem,
                                      RangeUnits, shadow, shad_ref, ranges);
    }
    else if (RangeCoordinateSystems != null) {
      TupleType RangeType = (TupleType) ((FunctionType) Type).getRange();
      int j = 0;
      for (int i=0; i<RangeCoordinateSystems.length; i++) {
        MathType component = RangeType.getComponent(i);
        if (component instanceof RealType) {
          j++;
        }
        else { // (component instanceof RealTupleType)
          int m = ((RealTupleType) component).getDimension();
          if (RangeCoordinateSystems[i] != null) {
            // computeRanges for Reference (relative to range
            // component) RealTypes
            double[][] sub_ranges = new double[2][m];
            Unit[] sub_units = new Unit[m];
            for (int k=0; k<m; k++) {
              sub_ranges[0][k] = ranges[0][j];
              sub_ranges[1][k] = ranges[1][j];
              sub_units[k] = RangeUnits[j];
              j++;
            }
            ShadowRealTupleType range_type = (ShadowRealTupleType)
              ((ShadowTupleType) ((ShadowFunctionType) type).getRange()).
                                                             getComponent(i);
            shad_ref = range_type.getReference();
            shadow = computeReferenceRanges(range_type, RangeCoordinateSystems[i],
                                       sub_units, shadow, shad_ref, sub_ranges);
          }
          else { // (RangeCoordinateSystems[i] == null)
            j += m;
          }
        } // end if (component instanceof RealTupleType)
      } // end for (int i=0; i<RangeCoordinateSystems.length; i++)
    } // end if (RangeCoordinateSystems != null)
    return shadow;
  }

  /** return a FlatField that clones this, except its ErrorEstimate-s
      are adjusted for sampling errors in error */
  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    if (isMissing() || error == null || error.isMissing()) return this;
    FlatField field =
      new FlatField((FunctionType) Type, DomainSet, RangeCoordinateSystem,
                    RangeCoordinateSystems, RangeSet, RangeUnits);
    if (isMissing()) return field;
    FlatField new_error = (FlatField)
      ((FlatField) error).resample(DomainSet, NEAREST_NEIGHBOR, NO_ERRORS);
    double[][] values = unpackValues();
    field.packValues(values, false);

    ErrorEstimate[] errors = new ErrorEstimate[TupleDimension];
    double[][] error_values = new_error.unpackValues();
    for (int i=0; i<TupleDimension; i++) {
      double a = 0.0;
      for (int k=0; k<error_values[i].length; k++) {
        a += error_values[i][k];
      }
      a = a / error_values.length;
      double b = RangeErrors[i].getErrorValue();
      double e = (error_mode == INDEPENDENT) ? Math.sqrt(a * a + b * b) :
                                               Math.abs(a) + Math.abs(b);
      errors[i] = new ErrorEstimate(values[i], e, RangeUnits[i]);
    }
    field.setRangeErrors(errors);
    return field;
  }

  public boolean isFlatField() {
    return true;
  }

  /** clone this FlatField, except substitute a DoubleSet for each
      component of RangeSet */
  private FlatField cloneDouble() throws VisADException {
    return cloneDouble(RangeUnits, RangeErrors);
  }

  /** clone this FlatField, except substitute a DoubleSet for each
      component of RangeSet, and substitute units and errors */
  private FlatField cloneDouble(Unit[] units, ErrorEstimate[] errors)
          throws VisADException {
    return cloneDouble( null, units, errors );
    /*- TDR June 1998
    // create (initially missing) FlatField for return
    // use DoubleSet rather than RangeSet for intermediate computation results
    Set[] sets = new Set[TupleDimension];
    for (int i=0; i<TupleDimension; i++) {
      SetType set_type =
        new SetType(((FunctionType) Type).getFlatRange().getComponent(i));
      sets[i] = new DoubleSet(set_type);
    }
    FlatField field =
      new FlatField((FunctionType) Type, DomainSet, RangeCoordinateSystem,
                    RangeCoordinateSystems, sets, units);
    double[][] values = unpackValues();
    field.packValues(values, false);
    // field.DoubleRange = values;
    field.setRangeErrors(errors); 
    field.clearMissing();
    return field;
    */
  }

  /*- TDR June 1998  */
  private FlatField cloneDouble( MathType f_type, Unit[] units,
                                 ErrorEstimate[] errors )
          throws VisADException
  {
    MathType N_type = ((f_type == null) ? Type : f_type );

    // create (initially missing) FlatField for return
    // use DoubleSet rather than RangeSet for intermediate computation results
    Set[] sets = new Set[TupleDimension];
    for (int i=0; i<TupleDimension; i++) {
      SetType set_type =
        new SetType(((FunctionType) N_type).getFlatRange().getComponent(i));
      sets[i] = new DoubleSet(set_type);
    }
    FlatField field =
      new FlatField((FunctionType) N_type, DomainSet, RangeCoordinateSystem,
                    RangeCoordinateSystems, sets, units);
    double[][] values = unpackValues();
    field.packValues(values, false);
    // field.DoubleRange = values;
    field.setRangeErrors(errors);
    field.clearMissing();
    return field;
  }

  /** clone metadata but return missing values */
  private FlatField cloneMissing() throws VisADException {
    return new FlatField((FunctionType) Type, DomainSet, RangeCoordinateSystem,
                         RangeCoordinateSystems, RangeSet, RangeUnits);
  }

  /** deep copy values but shallow copy Type, Set-s, CoordinateSystem-s,
      Unit-s and ErrorEstimate-s (they are all immutable) */
  public Object clone() {
    FlatField field;
    try {
      field =
        new FlatField((FunctionType) Type, DomainSet, RangeCoordinateSystem,
                      RangeCoordinateSystems, RangeSet, RangeUnits);
      if (isMissing()) return field;
      double[][] values = unpackValues();
      field.packValues(values, true);
      field.setRangeErrors(RangeErrors); 
    }
    catch (VisADException e) {
      throw new VisADError("FlatField.clone: VisADException occurred");
    }
    return field;
  }

  String valuesString() throws VisADException {
    int rowlength;
    StringBuffer s = new StringBuffer("");
    int ncolumns = 8 / TupleDimension;
    if (ncolumns < 1) ncolumns = 1;
    if (DomainSet instanceof GriddedSet) {
        // && ((GriddedSet) DomainSet).ManifoldDimension == 2) {
      rowlength = ((GriddedSet) DomainSet).getLength(0);
    }
    else {
      rowlength = Length;
    }
    RealTupleType range = ((FunctionType) Type).getFlatRange();
    RealType[] types = range.getRealComponents();
    double[][] values = unpackValues();
    int rl = rowlength;
    int i = 0;
    while (i<Length) {
      int nc = Math.min(rl, Math.min(ncolumns, Length-i));
      int ip = i + nc;
      for (int k=i; k<ip; k++) {
        if (k > i) s.append(", ");
        if (TupleDimension == 1) {
          s.append(new Real(types[0], values[0][k]).toString());
        }
        else if (((FunctionType) Type).getReal()) {
          String t = "(" + new Real(types[0], values[0][k]);
          for (int j=1; j<TupleDimension; j++) {
            t = t + ", " + new Real(types[j], values[j][k]);
          }
          t = t + ")";
          s.append(t);
        }
        else { // Flat Tuple
          TupleType RangeType = (TupleType) ((FunctionType) Type).getRange();
          String t = "(";
          int j = 0;
          for (int l=0; l<RangeType.getDimension(); l++) {
            if (j > 0) t = t + ", ";
            MathType type = RangeType.getComponent(l);
            if (type instanceof RealType) {
              t = t + new Real(types[j], values[j][k]);
              j++;
            }
            else {
              int mm = ((TupleType) type).getDimension();
              t = t + "(" + new Real(types[j], values[j][k]);
              j++;
              for (int kk=1; kk<mm; kk++) {
                t = t + ", " + new Real(types[j], values[j][k]);
                j++;
              }
              t = t + ")";
            }
          }
          t = t + ")";
          s.append(t);
        }
      } // end for (int k=i; k<ip; k++)
      s.append("\n");
      i = ip;
      rl -= nc;
      if (rl <= 0) {
        rl = rowlength;
        s.append("\n");
      }
    } // end while (i<Length)
    return s.toString();
  }

  public String toString() {
    try {
      if (isMissing()) {
        return "FlatField  missing\n";
      }
      else {
        return "FlatField\n    " + Type + "\n" + valuesString();
      }
    }
    catch (VisADException e) {
      return e.toString();
    }
  }

  public String longString(String pre) throws VisADException {
    String t = pre + "FlatField\n" + pre + "  Type: " +
               Type.toString() + "\n";
    if (DomainSet != null) {
      t = t + pre + "  DomainSet:\n" + DomainSet.longString(pre + "    ");
    }
    else {
      t = t + pre + "  DomainSet: undefined\n";
    }
    for (int i=0; i<TupleDimension; i++) {
      if (RangeSet[i] != null) {
        t = t + pre + "  RangeSet[" + i + "]:\n" + RangeSet[i].longString(pre + "    ");
      }
      else {
        t = t + pre + "  RangeSet[" + i + "]: undefined\n";
      }
    }
    if (isMissing()) {
      return t + "  missing\n";
    }
    else {
      return t + valuesString();
    }
  }

  public int getRangeDimension() {
     return TupleDimension;
  }

  /** construct a FlatField of given type; used for testing */
  public static FlatField makeField(FunctionType type, int length, boolean irregular)
         throws VisADException, RemoteException {
    double first = 0.0;
    double last = length - 1.0;
    double step = 1.0;
    double half = 0.5 * last;
    RealTupleType dtype = type.getDomain();
    RealTupleType rtype = type.getFlatRange();
    int domain_dim = dtype.getDimension();
    int range_dim = rtype.getDimension();
    SampledSet domain_set = null;
    int dsize = 0;
    Random random = new Random();
    if (irregular) {
      if (domain_dim == 1) {
        dsize = length;
        float[][] samples = new float[1][dsize];
        for (int i=0; i<dsize; i++) {
          samples[0][i] = (float) (last * random.nextFloat());
        }
        domain_set = new Irregular1DSet(dtype, samples);
      }
      else if (domain_dim == 2) {
        dsize = length * length;
        float[][] samples = new float[2][dsize];
        for (int i=0; i<dsize; i++) {
          samples[0][i] = (float) (last * random.nextFloat());
          samples[1][i] = (float) (last * random.nextFloat());
        }
        domain_set = new Irregular2DSet(dtype, samples);
      }
      else if (domain_dim == 3) {
        dsize = length * length * length;
        float[][] samples = new float[3][dsize];

        // random Irregular3DSet
        for (int i=0; i<dsize; i++) {
          samples[0][i] = (float) (last * random.nextFloat());
          samples[1][i] = (float) (last * random.nextFloat());
          samples[2][i] = (float) (last * random.nextFloat());
        }
/*
        // jittered linear Irregular3DSet
        Linear3DSet square_set = new Linear3DSet(dtype, first, last, length,
                                                 first, last, length,
                                                 first, last, length);
        samples = square_set.getSamples();
        for (int i=0; i<dsize; i++) {
          samples[0][i] += 0.05 * random.nextFloat();
          samples[1][i] += 0.05 * random.nextFloat();
          samples[2][i] += 0.05 * random.nextFloat();
        }
*/

        domain_set = new Irregular3DSet(dtype, samples);
      }
      else {
        throw new FieldException("FlatField.makeField: bad domain dimension");
      }
    }
    else { // if (!irregular)
      if (domain_dim == 1) {
        domain_set = new Linear1DSet(dtype, first, last, length);
        dsize = length;
      }
      else if (domain_dim == 2) {
        domain_set = new Linear2DSet(dtype, first, last, length,
                                            first, last, length);
        dsize = length * length;
      }
      else if (domain_dim == 3) {
        domain_set = new Linear3DSet(dtype, first, last, length,
                                            first, last, length,
                                            first, last, length);
        dsize = length * length * length;
      }
      else {
        throw new FieldException("FlatField.makeField: bad domain dimension");
      }
    }
    FlatField image = new FlatField(type, domain_set);
    fillField(image, step, half);
    return image;
  }

  public static void fillField(FlatField image, double step, double half)
         throws VisADException, RemoteException {
    Random random = new Random();
    FunctionType type = (FunctionType) image.getType();
    RealTupleType dtype = type.getDomain();
    RealTupleType rtype = type.getFlatRange();
    int domain_dim = dtype.getDimension();
    int range_dim = rtype.getDimension();
    SampledSet domain_set = (SampledSet) image.getDomainSet();
    int dsize = domain_set.getLength();

    double[][] data = new double[range_dim][dsize];
    float[][] samples = domain_set.getSamples();
    for (int k=0; k<range_dim; k++) {
      if (domain_dim == 1) {
        for (int i=0; i<dsize; i++) {
          float x = samples[0][i];
          if (k == 0) {
            data[k][i] = (float) Math.abs(step * (x - half));
          }
          else if (k == 1) {
            data[k][i] = x;
          }
          else {
            data[k][i] = random.nextDouble();
          }
        }
      }
      else if (domain_dim == 2) {
        for (int i=0; i<dsize; i++) {
          float x = samples[0][i];
          float y = samples[1][i];
          if (k == 0) {
            data[k][i] = (float) (step * Math.sqrt(
              (x - half) * (x - half) +
              (y - half) * (y - half)));
          }
          else if (k == 1) {
            data[k][i] = x;
          }
          else if (k == 2) {
            data[k][i] = y;
          }
          else {
            data[k][i] = random.nextDouble();
          }
        }
      }
      else if (domain_dim == 3) {
        for (int i=0; i<dsize; i++) {
          float x = samples[0][i];
          float y = samples[1][i];
          float z = samples[2][i];
          if (k == 0) {
            data[k][i] = (float) (step * Math.sqrt(
              (x - half) * (x - half) +
              (y - half) * (y - half) +
              (z - half) * (z - half)));
          }
          else if (k == 1) {
            data[k][i] = x;
          }
          else if (k == 2) {
            data[k][i] = y;
          }
          else if (k == 3) {
            data[k][i] = z;
          }
          else {
            data[k][i] = random.nextDouble();
          }
        }
      }
    }
    image.setSamples(data);
  }


  /** construct a FlatField with a 2-D domain and a 1-D range;
      used for testing */
  public static FlatField makeField1(FunctionType type,
                              double first1, double last1, int length1,
                              double first2, double last2, int length2)
          throws VisADException, RemoteException {

    double step1 = (last1 - first1) / (length1 - 1);
    double step2 = (last2 - first2) / (length2 - 1);

    Linear2DSet imageset =
      new Linear2DSet(type.getDomain(), first1, last1, length1,
                                        first2, last2, length2);

    FlatField image = new FlatField(type, imageset);

    double[][] data = new double[1][length1 * length2];
    for (int i=0; i<length1; i++) {
      for (int j=0; j<length2; j++) {
        data[0][i + length1 * j] =
          (first1 + step1 * i) + (first2 + step2 * j);
      }
    }
    image.setSamples(data);
    return image;
  }

  /** construct a FlatField with a 2-D domain and a 2-D range;
      used for testing */
  public static FlatField makeField2(FunctionType type,
                              double first1, double last1, int length1,
                              double first2, double last2, int length2)
          throws VisADException, RemoteException {

    double step1 = (last1 - first1) / (length1 - 1);
    double step2 = (last2 - first2) / (length2 - 1);

    Linear2DSet imageset =
      new Linear2DSet(type.getDomain(), first1, last1, length1,
                                        first2, last2, length2);

    FlatField image = new FlatField(type, imageset);

    double[][] data = new double[2][length1 * length2];
    for (int i=0; i<length1; i++) {
      for (int j=0; j<length2; j++) {
        data[0][i + length1 * j] = first1 + step1 * i;
        data[1][i + length1 * j] = first2 + step2 * j;
      }
    }
    image.setSamples(data);
    return image;
  }

  /** construct a FlatField with a 2-D domain and a 2-D range
      and random values; used for testing */
  static FlatField makeRandomField2(FunctionType type,
                                    double first1, double last1, int length1,
                                    double first2, double last2, int length2)
          throws VisADException, RemoteException {
 
    double step1 = (last1 - first1) / (length1 - 1);
    double step2 = (last2 - first2) / (length2 - 1);
 
    Linear2DSet imageset =
      new Linear2DSet(type.getDomain(), first1, last1, length1,
                                        first2, last2, length2);
 
    FlatField image = new FlatField(type, imageset);
 
    Random random = new Random();
    double[][] data = new double[2][length1 * length2];
    for (int i=0; i<length1; i++) {
      for (int j=0; j<length2; j++) {
        data[0][i + length1 * j] = random.nextDouble();
        data[1][i + length1 * j] = random.nextDouble();
      }
    }
    image.setSamples(data);
    return image;
  }

  /** run 'java visad.FlatField' to test the FlatField class */
  public static void main(String args[])
         throws VisADException, RemoteException {

    byte b = 10;
    Real w = new Real(b);

    RealType X = new RealType("X", null, null);
    RealType Y = new RealType("Y", null, null);
    RealType Z = new RealType("Z", null, null);

    RealType A = new RealType("A", null, null);
    RealType B = new RealType("B", null, null);

    RealType[] domain2d = {X, Y};
    RealTupleType Domain2d = new RealTupleType(domain2d, null, null);
    Integer2DSet Domain2dSet = new Integer2DSet(Domain2d, 4, 4);
    Domain2d.setDefaultSet(Domain2dSet);

    RealType[] range2d = {A, B};
    RealTupleType Range2d = new RealTupleType(range2d);

    FunctionType Field2d1 = new FunctionType(Domain2d, A);
    FunctionType Field2d2 = new FunctionType(Domain2d, Range2d);

    double first11 = 0.0;
    double last11 = 3.0;
    int length11 = 4;
    double first12 = 0.0;
    double last12 = 3.0;
    int length12 = 4;
    FlatField image1 = makeField1(Field2d1, first11, last11, length11,
                                            first12, last12, length12);
    FlatField image3 = makeField2(Field2d2, first11, last11, length11,
                                            first12, last12, length12);
    Real[] reals = {new Real(X ,1.5), new Real(Y, 2.5)};
    RealTuple val = new RealTuple(reals);

    double first21 = 0.0;
    double last21 = 3.0;
    int length21 = 7;
    double first22 = 0.0;
    double last22 = 3.0;
    int length22 = 7;
    FlatField image2 = makeField1(Field2d1, first21, last21, length21,
                                            first22, last22, length22);
    FlatField image4 = makeField2(Field2d2, first21, last21, length21,
                                            first22, last22, length22);

    System.out.println("image1 = " + image1);
    System.out.println("image2 = " + image2);
    System.out.println("image3 = " + image3);
    System.out.println("image4 = " + image4);

    // do some computations in NEAREST_NEIGHBOR sampling mode
    System.out.println("sampling mode is NEAREST_NEIGHBOR");
    System.out.println("image3 + image4 = " + image3.add(image4));
    System.out.println("image4 - image3 = " + image4.subtract(image3));
    System.out.println("image3 * image4 = " + image3.multiply(image4));
    System.out.println("image4 / image3 = " + image4.divide(image3));
    System.out.println("sqrt(image3) = " + image3.sqrt());
    System.out.println("val = " + val + " image1(val) = " +
                       image1.evaluate(val));
    System.out.println("val = " + val + " image3(val) = " +
                       image3.evaluate(val) + "\n");
    System.out.println("image3 + val = " + image3.add(val));
    System.out.println("val - image3 = " + val.subtract(image3));
    System.out.println("image3 * val = " + image3.multiply(val));
    System.out.println("val / image3 = " + val.divide(image3));

    // now do some computations in WEIGHTED_AVERAGE sampling mode
    System.out.println("Field.Mode is WEIGHTED_AVERAGE");
    System.out.println("image3 + image4 = " +
                       image3.add(image4, WEIGHTED_AVERAGE, INDEPENDENT));
    System.out.println("image4 - image3 = " +
                       image4.subtract(image3, WEIGHTED_AVERAGE, INDEPENDENT));
    System.out.println("image3 * image4 = " +
                       image3.multiply(image4, WEIGHTED_AVERAGE, INDEPENDENT));
    System.out.println("image4 / image3 = " +
                       image4.divide(image3, WEIGHTED_AVERAGE, INDEPENDENT));
    System.out.println("val = " + val + " image1(val) = " +
                       image1.evaluate(val, WEIGHTED_AVERAGE, INDEPENDENT));
    System.out.println("val = " + val + " image3(val) = " +
                       image3.evaluate(val, WEIGHTED_AVERAGE, INDEPENDENT) + "\n");
    System.out.println("image3 + val = " +
                       image3.add(val, WEIGHTED_AVERAGE, INDEPENDENT));
    System.out.println("val - image3 = " +
                       val.subtract(image3, WEIGHTED_AVERAGE, INDEPENDENT));
    System.out.println("image3 * val = " +
                       image3.multiply(val, WEIGHTED_AVERAGE, INDEPENDENT));
    System.out.println("val / image3 = " +
                       val.divide(image3, WEIGHTED_AVERAGE, INDEPENDENT));

    // do some more computations in NEAREST_NEIGHBOR sampling mode
    System.out.println("sampling mode is NEAREST_NEIGHBOR");

    System.out.println("image1 + w = " + image1.add(w));
    System.out.println("image1 - w = " + image1.subtract(w));
    System.out.println("image1 * w = " + image1.multiply(w));
    System.out.println("image1 / w = " + image1.divide(w));

    System.out.println("w + image2 = " + w.add(image2));
    System.out.println("w - image2 = " + w.subtract(image2));
    System.out.println("w * image2 = " + w.multiply(image2));
    System.out.println("w / image2 = " + w.divide(image2));

    // test DateTime printing
    RealType[] range2t = {A, RealType.Time};
    RealTupleType Range2t = new RealTupleType(range2t);
    FunctionType Field2t2 = new FunctionType(Domain2d, Range2t);
    FlatField imaget = makeField2(Field2t2, first11, last11, length11,
                                            first12, last12, length12);
    System.out.println("imaget = " + imaget);

  }

/* Here's the output:

iris 251% java visad.FlatField
image1 = FlatField
    FunctionType (Real): (X, Y) -> A
0, 1, 2, 3

1, 2, 3, 4

2, 3, 4, 5

3, 4, 5, 6


image2 = FlatField
    FunctionType (Real): (X, Y) -> A
0, 0.5, 1, 1.5, 2, 2.5, 3

0.5, 1, 1.5, 2, 2.5, 3, 3.5

1, 1.5, 2, 2.5, 3, 3.5, 4

. . .

imaget = FlatField
    FunctionType (Real): (X, Y) -> (A, Time(seconds))
(0, 00:00:00 GMT  1 Jan 0001 (Sat)), (1, 00:00:00 GMT  1 Jan 0001 (Sat)), (2, 00:00:00 GMT  1 Jan 0001 (Sat)), (3, 00:00:00 GMT  1 Jan 0001 (Sat))

(0, 00:00:01 GMT  1 Jan 0001 (Sat)), (1, 00:00:01 GMT  1 Jan 0001 (Sat)), (2, 00:00:01 GMT  1 Jan 0001 (Sat)), (3, 00:00:01 GMT  1 Jan 0001 (Sat))

(0, 00:00:02 GMT  1 Jan 0001 (Sat)), (1, 00:00:02 GMT  1 Jan 0001 (Sat)), (2, 00:00:02 GMT  1 Jan 0001 (Sat)), (3, 00:00:02 GMT  1 Jan 0001 (Sat))

(0, 00:00:03 GMT  1 Jan 0001 (Sat)), (1, 00:00:03 GMT  1 Jan 0001 (Sat)), (2, 00:00:03 GMT  1 Jan 0001 (Sat)), (3, 00:00:03 GMT  1 Jan 0001 (Sat))

*/

}

