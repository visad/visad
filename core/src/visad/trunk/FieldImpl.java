
//
// FieldImpl.java
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
   FieldImpl is the VisAD class for finite samplings of functions
   from R^n to a range type, where  n>0.  The DomainSet, DomainUnits
   and DomainCoordinateSystem variables of FieldImpl are immutable.<P>

   A FieldImpl domain type may be either a RealType (for a function with
   domain = R) or a RealTupleType (for a function with domain = R^n
   for n > 0).<P>
*/
public class FieldImpl extends FunctionImpl implements Field {

  /** the sampling of the function domain R^n */
  Set DomainSet;

  /** this is DomainSet.DomainCoordinateSystem */
  CoordinateSystem DomainCoordinateSystem;

  /** this is DomainSet.SetUnits */
  Unit[] DomainUnits;

  /** the number of samples */
  int Length;

  /** the array of function values */
  private Data[] Range;

  private boolean MissingFlag;

  /** construct a FieldImpl from type;
      use default Set of FunctionType domain */
  public FieldImpl(FunctionType type) throws VisADException {
    this(type, null);
  }

  /** construct a FieldImpl from type and domain Set */
  public FieldImpl(FunctionType type, Set set) throws VisADException {
    super(type);
    RealTupleType DomainType = type.getDomain();

    if (set == null) set = DomainType.getDefaultSet();
    if (set == null) {
      throw new SetException("FieldImpl: set cannot be null");
    }
    if (set instanceof DoubleSet || set instanceof FloatSet) {
      throw new SetException("FieldImpl: set may not be DoubleSet " +
                             "or FloatSet");
    }
    if (DomainType.getDimension() != set.getDimension()) {
      throw new SetException("FieldImpl: set and type dimensions don't match");
    }
    // force DomainSet Type to match DomainType
    if (DomainType.equals(((SetType) set.getType()).getDomain())) {
      DomainSet = set;
    }
    else {
      DomainSet = (Set) set.cloneButType(new SetType(DomainType));
    }
    DomainCoordinateSystem = DomainSet.getCoordinateSystem();
    DomainUnits = DomainSet.getSetUnits();
    Length = DomainSet.getLength();
    Range = new Data[Length];
    MissingFlag = true;
  }

  /** set the range samples of the function; the order of range samples
      must be the same as the order of domain indices in the DomainSet;
      copy range objects if copy is true;
      should use same MathType object in each Data object in range array */
  public void setSamples(Data[] range, boolean copy)
         throws VisADException, RemoteException {
    if (range.length != Length) {
      throw new FieldException("FieldImpl.setSamples: bad array length");
    }

    synchronized (Range) {
      MissingFlag = false;
      MathType t = ((FunctionType) Type).getRange();
      for (int i=0; i<Length; i++) {
        if (range != null && !t.equalsExceptName(range[i].getType())) {
          throw new TypeException("FieldImpl.setSamples: types don't match");
        }
        if (range[i] != null) {
          if (copy) Range[i] = (Data) range[i].dataClone();
          else Range[i] = range[i];
        }
        else Range[i] = null;
      }
      for (int i=0; i<Length; i++) {
        if (Range[i] instanceof DataImpl) {
          ((DataImpl) Range[i]).setParent(this);
        }
      }
    }
    notifyReferences();
  }

  public Set getDomainSet() {
    return DomainSet;
  }

  /** get number of samples */
  public int getLength() {
    return Length;
  }

  /** get SetUnits of DomainSet */
  public Unit[] getDomainUnits() {
    return DomainUnits;
  }

  public CoordinateSystem getDomainCoordinateSystem() {
    return DomainCoordinateSystem;
  }

  /** get values for 'Flat' components in default range Unit-s */
  public double[][] getValues()
         throws VisADException, RemoteException {
    RealType[] realComponents = ((FunctionType) Type).getRealComponents();
    if (realComponents == null) return null;
    int n = realComponents.length;
    double[][] values = new double[n][];
    Unit[] units = getDefaultRangeUnits();
    int len = getLength();

    if (isMissing()) {
      for (int k=0; k<n; k++) {
        for (int i=0; i<len; i++) values[k][i] = Double.NaN;
      }
      return values;
    }

    MathType RangeType = ((FunctionType) Type).getRange();

    synchronized (Range) {
      for (int i=0; i<len; i++) {
        Data range = Range[i];
        if (range == null || range.isMissing()) {
          for (int k=0; k<n; k++) values[k][i] = Double.NaN;
        }
        else {
          if (RangeType instanceof RealType) {
            values[0][i] = ((Real) range).getValue(units[0]);
          }
          else if (RangeType instanceof TupleType) {
            int k = 0;
            for (int j=0; j<((TupleType) RangeType).getDimension(); j++) {
              MathType component_type = ((TupleType) RangeType).getComponent(j);
              Data component = ((Tuple) range).getComponent(j);
              if (component_type instanceof RealType) {
                values[k][i] = ((Real) component).getValue(units[k]);
                k++;
              }
              else if (component_type instanceof RealTupleType) {
                for (int m=0; m<((TupleType) component_type).getDimension(); m++) {
                  Data comp_comp = ((Tuple) component).getComponent(m);
                  values[k][i] = ((Real) comp_comp).getValue(units[k]);
                  k++;
                }
              }
            }
          }
        }
      } // end for (int i=0; i<len; i++)
    }
    return values;
  }
 
  /** get range Unit-s for 'Flat' components;
      second index enumerates samples */
  public Unit[][] getRangeUnits()
         throws VisADException, RemoteException {
    RealType[] realComponents = ((FunctionType) Type).getRealComponents();
    if (realComponents == null) return null;
    int n = realComponents.length;
    Unit[][] units = new Unit[n][Length];
    Unit[] default_units = getDefaultRangeUnits();

    MathType RangeType = ((FunctionType) Type).getRange();
 
    for (int i=0; i<Length; i++) {
      Data range = Range[i];
      if (range == null || range.isMissing()) {
        for (int k=0; k<n; k++) units[k][i] = default_units[k];
      }
      else {
        if (RangeType instanceof RealType) {
          units[0][i] = ((Real) range).getUnit();
        }
        else if (RangeType instanceof TupleType) {
          int k = 0;
          for (int j=0; j<((TupleType) RangeType).getDimension(); j++) {
            MathType component_type = ((TupleType) RangeType).getComponent(i);
            Data component = ((Tuple) range).getComponent(j);
            if (component_type instanceof RealType) {
              units[k][i] = ((Real) component).getUnit();
              k++;
            }
            else if (component_type instanceof RealTupleType) {
              for (int m=0; m<((TupleType) component_type).getDimension(); m++) {
                Data comp_comp = ((Tuple) component).getComponent(m);
                units[k][i] = ((Real) comp_comp).getUnit();
                k++;
              }
            }
          }
        }
      }
    }
    return units;
  }
 
  /** get range CoordinateSystem for 'RealTuple' range;
      second index enumerates samples */
  public CoordinateSystem[] getRangeCoordinateSystem()
         throws VisADException, RemoteException {
    MathType RangeType = ((FunctionType) Type).getRange();
    if (!(RangeType instanceof RealTupleType)) {
      throw new TypeException("FieldImpl.getRangeCoordinateSystem: " +
        "Range is not RealTupleType");
    }

    CoordinateSystem[] cs = new CoordinateSystem[Length];
    CoordinateSystem default_cs =
      ((RealTupleType) RangeType).getCoordinateSystem();

    for (int i=0; i<Length; i++) {
      Data range = Range[i];
      if (range == null || range.isMissing()) {
        cs[i] = default_cs;
      }
      else {
        cs[i] = ((RealTuple) range).getCoordinateSystem();
      }
    }
    return cs;
  }
 
  /** get range CoordinateSystem for 'RealTuple' components;
      second index enumerates samples */
  public CoordinateSystem[] getRangeCoordinateSystem(int component)
         throws VisADException, RemoteException {
    MathType RangeType = ((FunctionType) Type).getRange();
    if ( (!(RangeType instanceof TupleType)) ||
         (RangeType instanceof RealTupleType) ) {
      throw new TypeException("FieldImpl.getRangeCoordinateSystem: " +
        "Range must be TupleType but not RealTupleType");
    }

    MathType component_type =
      ((TupleType) RangeType).getComponent(component);

    if (!(component_type instanceof RealTupleType)) {
      throw new TypeException("FieldImpl.getRangeCoordinateSystem: " +
        "selected Range component must be RealTupleType");
    }

    CoordinateSystem[] cs = new CoordinateSystem[Length];

    CoordinateSystem default_cs =
      ((RealTupleType) component_type).getCoordinateSystem();

    for (int i=0; i<Length; i++) {
      Data range = Range[i];
      if (range == null || range.isMissing()) {
        cs[i] = default_cs;
      }
      else {
        Data comp = ((Tuple) range).getComponent(component);
        if (comp == null || comp.isMissing()) {
          cs[i] = default_cs;
        }
        else {
          cs[i] = ((RealTuple) comp).getCoordinateSystem();
        }
      }
    }
    return cs;
  }

  /** get default range Unit-s for 'Flat' components */
  public Unit[] getDefaultRangeUnits() {
    RealType[] realComponents = ((FunctionType) Type).getRealComponents();
    if (realComponents == null) return null;
    int n = realComponents.length;
    Unit[] units = new Unit[n];
    for (int i=0; i<n; i++) {
      units[i] = realComponents[i].getDefaultUnit();
    }
    return units;
  }

  /** get the range value at the index-th sample */
  public Data getSample(int index)
         throws VisADException, RemoteException {
    synchronized (Range) {
      if (isMissing() || index < 0 || index >= Length || Range[index] == null) {
        return ((FunctionType) Type).getRange().missingData();
      }
      else return Range[index];
    }
  }

  /** set the range value at the sample nearest to domain */
  public void setSample(RealTuple domain, Data range)
         throws VisADException, RemoteException {
    if (DomainSet == null) {
      throw new FieldException("FieldImpl.setSample: DomainSet undefined");
    }
    if (!((FunctionType) Type).getDomain().equalsExceptName(domain.getType())) {
      throw new TypeException("FieldImpl.setSample: bad domain type");
    }

    int dimension = DomainSet.getDimension();
    float[][] vals = new float[dimension][1];
    for (int j=0; j<dimension; j++) {
      vals[j][0] = ((float) ((Real) ((RealTuple) domain).getComponent(j)).getValue());
    }
    // always use simple resampling for set
    int[] indices = DomainSet.valueToIndex(vals);
    setSample(indices[0], range);
  }

  public void setSample(int index, Data range)
         throws VisADException, RemoteException {
    setSample(index, range, true);
  }
 
  /** set the range value at the index-th sample */
  public void setSample(int index, Data range, boolean copy)
         throws VisADException, RemoteException {
    if (DomainSet == null) {
      throw new FieldException("FieldImpl.setSample: DomainSet undefined");
    }
    if (!((FunctionType) Type).getRange().equalsExceptName(range.getType())) {
      throw new TypeException("FieldImpl.setSample: bad range type");
    }
    if (index >= 0 && index < Length) {
      synchronized (Range) {
        MissingFlag = false;
        if ( copy ) {
          Range[index] = (Data) range.dataClone();
        }
        else {
          Range[index] = range;
        }
        if (Range[index] instanceof DataImpl) {
          ((DataImpl) Range[index]).setParent(this);
        }
      }
    }
    notifyReferences();
  }

/* WLH 9 March 98
  public void setSample(int index, Data range)
         throws VisADException, RemoteException {
    if (DomainSet == null) {
      throw new FieldException("FieldImpl.setSample: DomainSet undefined");
    }
    if (!((FunctionType) Type).getRange().equalsExceptName(range.getType())) {
      throw new TypeException("FieldImpl.setSample: bad range type");
    }
    if (index >= 0 && index < Length) {
      synchronized (Range) {
        MissingFlag = false;
        Range[index] = (Data) range.dataClone();
        if (Range[index] instanceof DataImpl) {
          ((DataImpl) Range[index]).setParent(this);
        }
      }
    }
    notifyReferences();
  }
*/

  /** test whether Field value is missing */
  public boolean isMissing() {
    synchronized (Range) {
      return MissingFlag;
    }
  }

  /** return new Field with value 'this op data';
      test for various relations between types of this and data */
  /*- TDR  May 1998 
  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
   */
  public Data binary(Data data, int op, MathType new_type,
                     int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    boolean field_flag; // true if this and data have same type
    if ( new_type == null ) {
      throw new TypeException("binary: new_type may not be null" );
    }
    if (Type.equalsExceptName(data.getType())) {
      /*-  TDR  May 1998  */
      if ( !Type.equalsExceptName( new_type )) {
        throw new TypeException("binary: new_type doesn't match return type");
      }
      /*- end  */
      // type of this and data match, so normal Field operation
      field_flag = true;
      if (((Field) data).isFlatField()) {
        // force (data instanceof FlatField) to be true
        data = data.local();
        // this and data have same type, but data is Flat and this is not
        data = ((FlatField) data).convertToField();
      }
    }
    else if (data instanceof Real ||
             ((FunctionType) Type).getRange().equalsExceptName(data.getType())) {
      // data is real or matches range type of this
      field_flag = false;
      /*-  TDR May 1998  */
      if ( !Type.equalsExceptName( new_type )) {
        throw new TypeException("binary: new_type doesn't match return type");
      }
      /*-  end  */
    }
    else if (data instanceof Field &&
             ((FunctionType) data.getType()).getRange().equalsExceptName(Type)) {

      /*- TDR  May 1998 */
      if ( !((FunctionType) data.getType()).getRange().equalsExceptName(new_type)) {
        throw new TypeException("binary: new_type doesn't match return type");
      }
      /*- end */
      // this matches range type of data
      // note invertOp to reverse order of operands
      /*- TDR  May 1998
      return data.binary(this, invertOp(op), sampling_mode, error_mode);
       */
      return data.binary(this, invertOp(op), new_type, sampling_mode, error_mode);
    }
    else {
      throw new TypeException("FieldImpl.binary: types don't match");
    }
    // create (initially missing) Field for return
    Field new_field = new FieldImpl((FunctionType) new_type, DomainSet);
    if (isMissing() || data.isMissing()) return new_field;
    Data[] range = new Data[Length];
    /*- TDR  May 1998  */
    MathType m_type = ((FunctionType)new_type).getRange();
    if (field_flag) {
      // resample data if needed
      data = ((Field) data).resample(DomainSet, sampling_mode, error_mode);
      // apply operation to each range object
      for (int i=0; i<Length; i++) {
        synchronized (Range) {
          range[i] = (Range[i] == null) ? null :
                    /*-  TDR May 1998
                     Range[i].binary(((Field) data).getSample(i), op,
                                     sampling_mode, error_mode);
                     */
                     Range[i].binary(((Field) data).getSample(i), op, m_type,
                                     sampling_mode, error_mode);

        }
      }
    }
    else { // !field_flag
      for (int i=0; i<Length; i++) {
        synchronized (Range) {
          range[i] = (Range[i] == null) ? null :
                     /*- TDR  May 1998
                     Range[i].binary(data, op, sampling_mode, error_mode);
                      */
                     Range[i].binary(data, op, m_type, sampling_mode, error_mode);
        }
      }
    }
    new_field.setSamples(range, false);
    return new_field;
  }


  /** return new Field with value 'op this' */
  /*- TDR  July  1998 
  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
  */
  public Data unary(int op, MathType new_type, int sampling_mode, 
                    int error_mode )
              throws VisADException, RemoteException {
    if ( new_type == null ) {
      throw new TypeException("unary: new_type may not be null");
    }
    if ( !Type.equalsExceptName(new_type)) {
      throw new TypeException("unary: new_type doesn't match return type");
    }
    MathType m_type = ((FunctionType)new_type).getRange();
    // create (initially missing) Field for return
    Field new_field = new FieldImpl((FunctionType) Type, DomainSet);
    if (isMissing()) return new_field;
    Data[] range = new Data[Length];
    // apply operation to each range object
    for (int i=0; i<Length; i++) {
      synchronized (Range) {
        range[i] = (Range[i] == null) ? null :
                   Range[i].unary(op, m_type, sampling_mode, error_mode);
      }
    }
    new_field.setSamples(range, false);
    return new_field;
  }

  /** combine an array of Field-s;
      they must have the same Domain type;
      this takes the place of 'insert' in the C-based VisAD */
  public Field combine(Field[] fields)
         throws VisADException, RemoteException {
    throw new UnimplementedException("FlatField.combine");
  }

  /** extract field from this[].component */
  public Field extract(int component)
         throws VisADException, RemoteException {
    throw new UnimplementedException("FieldImpl.insert");
  }

  /** resample range values of this to domain samples in set,
      either by nearest neighbor or mulit-linear interpolation
      NOTE may return this (i.e., not a copy);
      NOTE this code is very similar to resample in FlatField.java */
  public Field resample(Set set, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (DomainSet.equals(set)) {
      // nothing to do
      return this;
    }
    Field field = new FieldImpl((FunctionType) Type, set);
    if (isMissing()) return field;

    int dim = DomainSet.getDimension();
    if (dim != set.getDimension()) {
      throw new SetException("FieldImpl.resample: bad Set Dimension");
    }

    CoordinateSystem coord_sys = set.getCoordinateSystem();
    Unit[] units = set.getSetUnits();
    ErrorEstimate[] errors =
      (error_mode == NO_ERRORS) ? new ErrorEstimate[dim] : set.getSetErrors();

    // create an array containing all indices of 'this'
    int length = set.getLength();
    int[] wedge = set.getWedge();

    // array of Data objects to receive resampled Range objects
    Data[] range = new Data[length];

    // get values from wedge and possibly transform coordinates
    float[][] vals = set.indexToValue(wedge);
    // holder for sampling errors of transformed set - these are
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
      for (int i=0; i<dim; i++) {
        if (errors_out[i] == null) sampling_errors = false;
      }
    }
    Data[] sampling_partials = new Data[dim];
    float[][] error_values;
    double[] means = new double[dim];

    if (sampling_mode == WEIGHTED_AVERAGE && DomainSet instanceof SimpleSet) {
      // resample by interpolation
      int[][] indices = new int[length][];
      float[][] coefs = new float[length][];
      ((GriddedSet) DomainSet).valueToInterp(vals, indices, coefs);
      for (int i=0; i<length; i++) {
        int len;
        len = indices[i].length;
        if (len > 0) {
          Data r = null;
          // WLH
          for (int k=0; k<len; k++) {
            Data RangeIK;
            synchronized (Range) {
              RangeIK = Range[indices[i][k]];
            }
            if (RangeIK != null) {
              r = (r == null) ? RangeIK.multiply(new Real(coefs[i][k])) :
                                r.add(RangeIK.multiply(new Real(coefs[i][k])));
            }
            else {
              r = null;
              break;
            }
          }
          range[wedge[i]] = r;
        }
        else {
          // set range[wedge[i]] to a missing Data object
          range[wedge[i]] = ((FunctionType) Type).getRange().missingData();
        }

        if (sampling_errors && !range[wedge[i]].isMissing()) {
          for (int j=0; j<dim; j++) means[j] = vals[j][i];
          error_values = Set.doubleToFloat(
                           ErrorEstimate.init_error_values(errors_out, means) );
          int[][] error_indices = new int[2 * dim][];
          float[][] error_coefs = new float[2 * dim][];
          coefs = new float[2 * dim][];
          ((SimpleSet) DomainSet).valueToInterp(error_values, error_indices,
                                                error_coefs);
   
          for (int j=0; j<dim; j++) {
            Data a = null;
            Data b = null;
            len = error_indices[2*j].length;
            if (len > 0) {
              for (int k=0; k<len; k++) {
                Data RangeIK;
                synchronized (Range) {
                  RangeIK = Range[error_indices[2*j][k]];
                }
                if (RangeIK != null) {
                  a = (a == null) ?
                      RangeIK.multiply(new Real(error_coefs[2*j][k])) :
                      a.add(RangeIK.multiply(new Real(error_coefs[2*j][k])));
                }
                else {
                  a = null;
                  break;
                }
              }
            }
            len = error_indices[2*j+1].length;
            if (len > 0) {
              for (int k=0; k<len; k++) {
                Data RangeIK;
                synchronized (Range) {
                  RangeIK = Range[error_indices[2*j+1][k]];
                }
                if (RangeIK != null) {
                  b = (b == null) ?
                      RangeIK.multiply(new Real(error_coefs[2*j+1][k])) :
                      b.add(RangeIK.multiply(new Real(error_coefs[2*j+1][k])));
                }
                else {
                  b = null;
                  break;
                }
              }
            }
            if (a == null || b == null) {
              sampling_partials[j] = null;
            }
            else {
              sampling_partials[j] = b.subtract(a).abs();
            }
          }

          Data error = null;
          if (error_mode == Data.INDEPENDENT) {
            for (int j=0; j<dim; j++) {
              Data e = sampling_partials[j].multiply(sampling_partials[j]);
              error = (error == null) ? e : error.add(e);
            }
            error = error.sqrt();
          }
          else { // error_mode == Data.DEPENDENT
            for (int j=0; j<dim; j++) {
              Data e = sampling_partials[j];
              error = (error == null) ? e : error.add(e);
            }
          }
          range[wedge[i]] =
            range[wedge[i]].adjustSamplingError(error, error_mode);
        } // end if (sampling_errors && !range[wedge[i]].isMissing())
      } // end for (int i=0; i<length; i++)
    }
    else { // Mode is NEAREST_NEIGHBOR or set is not GriddedSet
      // simple resampling
      int[] indices = DomainSet.valueToIndex(vals);
      for (int i=0; i<length; i++) {
        synchronized (Range) {
          range[wedge[i]] = (indices[i] >= 0 && Range[indices[i]] != null) ?
                            Range[indices[i]] :
                            ((FunctionType) Type).getRange().missingData();
        }

        if (sampling_errors && !range[wedge[i]].isMissing()) {
          for (int j=0; j<dim; j++) means[j] = vals[j][i];
          error_values = Set.doubleToFloat(
                           ErrorEstimate.init_error_values(errors_out, means) );
          int[] error_indices = DomainSet.valueToIndex(error_values);
          for (int j=0; j<dim; j++) {
            synchronized (Range) {
              if (error_indices[2*j] < 0 || Range[error_indices[2*j]] == null ||
                  error_indices[2*j+1] < 0 || Range[error_indices[2*j+1]] == null) {
                sampling_partials[j] = null;
              }
              else {
                sampling_partials[j] = Range[error_indices[2*j+1]].
                                         subtract(Range[error_indices[2*j]]).abs();
              }
            }
          }

          Data error = null;
          if (error_mode == Data.INDEPENDENT) {
            for (int j=0; j<dim; j++) {
              Data e = sampling_partials[j].multiply(sampling_partials[j]);
              error = (error == null) ? e : error.add(e);
            }
            error = error.sqrt();
          }
          else { // error_mode == Data.DEPENDENT
            for (int j=0; j<dim; j++) {
              Data e = sampling_partials[j];
              error = (error == null) ? e : error.add(e);
            }
          }
          range[wedge[i]] =
            range[wedge[i]].adjustSamplingError(error, error_mode);
        } // end if (sampling_errors && !range[wedge[i]].isMissing())
      } // end for (int i=0; i<length; i++)
    }

    if (coord_transform) {
      // domain coordinates were transformed, so make corresponding
      // vector transform to any RealVectorType-s in range
      MathType RangeType = ((FunctionType) Type).getRange();
      if (RangeType instanceof RealVectorType) {
        int n = vals.length;
        float[][] loc = new float[n][1];
        for (int i=0; i<length; i++) {
          for (int k=0; k<n; k++) loc[k][0] = vals[k][i];
          range[i] = ((RealVectorType) RangeType).transformVectors(
                      ((FunctionType) Type).getDomain(),
                      DomainCoordinateSystem, DomainUnits, errors_out,
                      ((SetType) set.getType()).getDomain(),
                      coord_sys, units,
                      ((RealTuple) range[i]).getCoordinateSystem(),
                      loc, (RealTuple) range[i]);
        }
      }
      else if (RangeType instanceof TupleType &&
               !(RangeType instanceof RealTupleType)) {
        int m = ((TupleType) RangeType).getDimension();
        boolean any_vector = false;
        for (int j=0; j<m; j++) {
          if (((TupleType) RangeType).getComponent(j) instanceof RealVectorType) {
            any_vector = true;
          }
        }
        if (any_vector) {
          int n = vals.length;
          float[][] loc = new float[n][1];
          Data[] datums = new Data[m];
          for (int i=0; i<length; i++) {
            for (int k=0; k<n; k++) loc[k][0] = vals[k][i];
            for (int j=0; j<m; j++) {
              MathType comp_type = ((TupleType) RangeType).getComponent(j);
              if (comp_type instanceof RealVectorType) {
                RealTuple component = (RealTuple) ((Tuple) range[i]).getComponent(j);
                datums[j] = ((RealVectorType) comp_type).transformVectors(
                            ((FunctionType) Type).getDomain(),
                            DomainCoordinateSystem, DomainUnits, errors_out,
                            ((SetType) set.getType()).getDomain(),
                            coord_sys, units, component.getCoordinateSystem(),
                            loc, component);
              }
              else {
                datums[j] = ((Tuple) range[i]).getComponent(j);
              }
            }
            range[i] = new Tuple(datums);

          }
        }
      }
    }
    field.setSamples(range, false);
    return field;
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    if (isMissing()) return shadow;

    ShadowRealTupleType domain_type = ((ShadowFunctionType) type).getDomain();
    int n = domain_type.getDimension();
    double[][] ranges = new double[2][n];
    // DomainSet.computeRanges handles Reference
    shadow = DomainSet.computeRanges(domain_type, shadow, ranges, true);
    ShadowType rtype = ((ShadowFunctionType) type).getRange();
    for (int i=0; i<Range.length; i++) {
      synchronized (Range) {
        if (Range[i] != null) shadow = Range[i].computeRanges(rtype, shadow);
      }
    }
    return shadow;
  }

  /** return a Field that clones this, except its ErrorEstimate-s
      are adjusted for sampling errors in error */
  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    if (isMissing() || error == null || error.isMissing()) return this;
    Field field = new FieldImpl((FunctionType) Type, DomainSet);
    if (isMissing()) return field;
    Field new_error =
      ((Field) error).resample(DomainSet, NEAREST_NEIGHBOR, NO_ERRORS);
    Data[] range = new Data[Length];
    for (int i=0; i<Length; i++) {
      synchronized (Range) {
        range[i] = Range[i].adjustSamplingError(new_error.getSample(i), error_mode);
      }
    }
    field.setSamples(Range, true);
    return field;
  }

  public boolean isFlatField() {
    return false;
  }

  /** deep copy values but shallow copy Type, Set and CoordinateSystem */
  public Object clone() {
    Field field;
    try {
      field = new FieldImpl((FunctionType) Type, DomainSet);
      if (isMissing()) return field;
      synchronized (Range) {
        field.setSamples(Range, true);
      }
    }
    catch (VisADException e) {
      throw new VisADError("FieldImpl.clone: VisADException occured");
    }
    catch (RemoteException e) {
      throw new VisADError("FieldImpl.clone: RemoteException occured");
    }
    return field;
  }

  public String longString(String pre)
         throws VisADException, RemoteException {
    StringBuffer s = new StringBuffer(pre + "FieldImpl\n" + pre + "  Type: " +
                                      Type.toString() + "\n");
    if (DomainSet != null) {
      s.append(pre + "  DomainSet:\n" + DomainSet.longString(pre + "    "));
    }
    else {
      s.append(pre + "  DomainSet: undefined\n");
    }
    if (isMissing()) {
      s.append("  missing\n");
      return s.toString();
    }
    for (int i=0; i<Length; i++) {
      s.append(pre + "  Range value " + i + ":\n" + ((Range[i] == null) ?
               (pre + "missing\n") : Range[i].longString(pre + "    ")));
    }
    return s.toString();
  }



/**
<PRE>
   Here's how to use this:

   for (Enumeration e = field.domainEnumeration() ; e.hasMoreElements(); ) {
     RealTuple domain_sample = (RealTuple) e.nextElement();
     Data range = field.evaluate(domain_sample);
   }
</PRE>
*/
  public Enumeration domainEnumeration()
         throws VisADException, RemoteException {
    return new FieldEnumerator(this);
  }

}

class FieldEnumerator implements Enumeration {
  Field field;
  int[] index;
  int dimension;
  RealTupleType type;
  RealType[] types;

  FieldEnumerator(Field f) throws VisADException, RemoteException {
    if (field.getDomainSet() == null) {
      throw new FieldException("FieldImplEnumerator: DomainSet undefined");
    }
    field = f;
    index = new int[1];
    index[0] = 0;
    dimension = field.getDomainSet().getDimension();
    type = ((FunctionType) field.getType()).getDomain();
    types = new RealType[dimension];
    for (int j=0; j<dimension; j++) {
      types[j] = (RealType) type.getComponent(j);
    }
  }

  public boolean hasMoreElements() {
    try {
      return index[0] < field.getLength();
    }
    catch (RemoteException e) {
      return false;
    }
    catch (VisADException e) {
      return false;
    }
  }

  public Object nextElement() {
    try {
      if (index[0] < field.getLength()) {
        float[][] vals = field.getDomainSet().indexToValue(index);
        index[0]++;
        Real[] reals = new Real[dimension];
        for (int j=0; j<dimension; j++) {
          reals[j] = new Real(types[j], (double) vals[j][0]);
        }
        return new RealTuple(reals);
      }
      else {
        return null;
        // throw new NoSuchElementException("FieldImplEnumerator.nextElement");
      }
    }
    catch (VisADException e) {
      return null;
    }
    catch (RemoteException e) {
      return null;
    }
  }

}

