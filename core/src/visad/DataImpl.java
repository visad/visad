//
// DataImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;
import java.util.Vector;

import visad.java2d.DisplayImplJ2D;

/**
   DataImpl is the superclass for VisAD's data hierarchy, inheriting
   the Data interface.  Data objects are immutable except for the range
   values of Field objects.<p>

   VisAD Data objects are finite approximations to math objects
   that include scalars, tuples (i.e., n-dimensional vectors), functions,
   and certain forms of sets.  Hence, all Data objects possess a MathType,
   which identifies the corresponding concept and is <b>not</b> a synonym
   for the Data class, even though the class names for a Data object and
   its corresponding MathType object (Set and SetType, e.g.) may be
   similar.  In order to approximate their corresponding mathematical
   entities, Data objects may use text strings or finite representations
   of real numbers.  Also, any Data object may take the value 'missing',
   and any sub-object of a Data object may take the value 'missing'.<p>

   All of the Java arithmetical operations are defined for Data objects,
   to the extent that they make sense for the types involved.<p>
*/
public abstract class DataImpl extends ThingImpl
       implements Data {

  private static final long serialVersionUID = 1L;

  /** each VisAD data object has a VisAD mathematical type */
  MathType Type;

  /** parent is used to propogate notifyReferences;
      parent DataImpl object if parent is local;
      null if parent is remote;
      i.e., notifyReferences does not propogate to remote parents;
      only a single parent is supported - multiple parents are
      not correctly notified of data changes */
  private transient DataImpl parent;

  /**
   * construct a DataImpl with given MathType
   * @param type  MathType
   */
  public DataImpl(MathType type) {
    Type = type;
    parent = null;
  }

  /**
   * @return this (returns a local copy for RemoteData)
   */
  public DataImpl local() {
    return this;
  }

  /**
   * set the parent (i.e., containing Tuple or Field) of this DataImpl
   * @param p  parent DataImpl
   */
  void setParent(DataImpl p) {
    parent = p;
  }

  /**
   * @return MathType of this Data
   */
  public MathType getType() {
    return Type;
  }

  /**
   * notify local DataReferenceImpl-s that this DataImpl has changed;
   * incTick in RemoteDataImpl for RemoteDataReferenceImpl-s;
   * declared public because it is defined in the Data interface
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public void notifyReferences()
         throws VisADException, RemoteException {
    super.notifyReferences();
    // recursively propogate data change to parent
    if (parent != null) parent.notifyReferences();
  }

  /**
   * Pointwise binary operation between this and data. Applies
   * to Reals, Tuples (recursively to components), and to Field 
   * ranges (Field domains implicitly resampled if necessary). 
   * Does not apply to Field domains or Sets (regarded as domains
   * of Fields wthout ranges). Data.ADD is only op defined for
   * Text, interpreted as concatenate. MathTypes of this and data
   * must match, or one may match the range of the other if it is
   * a FunctionType.
   * @param data  other Data operand for binary operation
   * @param op  may be Data.ADD, Data.SUBTRACT, etc; these include all
   *             binary operations defined for Java primitive data types
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result, which takes the MathType of this unless the default
   *         Units of that MathType conflict with Units of the result,
   *         in which case a generic MathType with appropriate Units is
   *         constructed 
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    MathType dtype = data.getType();

    MathType new_type = Type.binary( dtype, op, new Vector() );
    return binary( data, op, new_type, sampling_mode, error_mode );
  }

  /**
   * Pointwise binary operation between this and data. Applies
   * to Reals, Tuples (recursively to components), and to Field 
   * ranges (Field domains implicitly resampled if necessary). 
   * Does not apply to Field domains or Sets (regarded as domains
   * of Fields wthout ranges). Data.ADD is only op defined for
   * Text, interpreted as concatenate. MathTypes of this and data
   * must match, or one may match the range of the other if it is
   * a FunctionType.
   * @param data  other Data operand for binary operation
   * @param op  may be Data.ADD, Data.SUBTRACT, etc; these include all
   *             binary operations defined for Java primitive data types
   * @param new_type  MathType of the result
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result, with MathType = new_type
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data binary( Data data, int op, MathType new_type,
                      int sampling_mode, int error_mode )
              throws VisADException, RemoteException {
    throw new TypeException("DataImpl.binary");
  }

  /**
   * call binary() to add data to this, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @param data other Data operand for binary operation
   * @return result of operation
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data add(Data data) throws VisADException, RemoteException {
    return binary(data, ADD, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call binary() to subtract data from this, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data subtract(Data data) throws VisADException, RemoteException {
    return binary(data, SUBTRACT, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call binary() to multiply this by data, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data multiply(Data data) throws VisADException, RemoteException {
    return binary(data, MULTIPLY, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call binary() to divide this by data, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data divide(Data data) throws VisADException, RemoteException {
    return binary(data, DIVIDE, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call binary() to raise this to data power, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data pow(Data data) throws VisADException, RemoteException {
    return binary(data, POW, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call binary() to take the max of this and data, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data max(Data data) throws VisADException, RemoteException {
    return binary(data, MAX, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call binary() to take the min of this and data, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data min(Data data) throws VisADException, RemoteException {
    return binary(data, MIN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call binary() to take the atan of this by data
   * producing radian Units, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data atan2(Data data) throws VisADException, RemoteException {
    return binary(data, ATAN2, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call binary() to take the atan of this by data
   * producing degree Units, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data atan2Degrees(Data data) throws VisADException, RemoteException {
    return binary(data, ATAN2_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call binary() to take the remainder of this divided by
   * data, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data remainder(Data data) throws VisADException, RemoteException {
    return binary(data, REMAINDER, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call binary() to add data to this
   * @param data  other Data operand for binary operation
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data add(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, ADD, sampling_mode, error_mode);
  }

  /**
   * call binary() to subtract data from this
   * @param data  other Data operand for binary operation
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data subtract(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, SUBTRACT, sampling_mode, error_mode);
  }

  /**
   * call binary() to multiply this by data
   * @param data  other Data operand for binary operation
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data multiply(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, MULTIPLY, sampling_mode, error_mode);
  }

  /**
   * call binary() to divide this by data
   * @param data  other Data operand for binary operation
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data divide(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, DIVIDE, sampling_mode, error_mode);
  }

  /**
   * call binary() to raise this to data power
   * @param data  other Data operand for binary operation
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data pow(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, POW, sampling_mode, error_mode);
  }

  /**
   * call binary() to take the max of this and data
   * @param data  other Data operand for binary operation
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data max(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, MAX, sampling_mode, error_mode);
  }

  /**
   * call binary() to take the min of this and data
   * @param data  other Data operand for binary operation
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data min(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, MIN, sampling_mode, error_mode);
  }

  /**
   * call binary() to take the atan of this by data
   * producing radian Units
   * @param data  other Data operand for binary operation
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data atan2(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, ATAN2, sampling_mode, error_mode);
  }

  /**
   * call binary() to take the atan of this by data
   * producing degree Units
   * @param data  other Data operand for binary operation
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data atan2Degrees(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, ATAN2_DEGREES, sampling_mode, error_mode);
  }

  /**
   * call binary() to take the remainder of this divided by data
   * @param data  other Data operand for binary operation
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data remainder(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, REMAINDER, sampling_mode, error_mode);
  }

  /**
   * Pointwise unary operation applied to this. Applies
   * to Reals, Tuples (recursively to components), and to Field 
   * ranges (Field domains implicitly resampled if necessary). 
   * Does not apply to Field domains, Sets (regarded as domains
   * of Fields wthout ranges) or Text.
   * @param op  may be Data.ABS, Data.ACOS, etc; these include all
   *             unary operations defined for Java primitive data types
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation, which takes the MathType of this unless
   *         the default Units of that MathType conflict with Units of
   *         the result, in which case a generic MathType with appropriate
   *         Units is constructed
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    MathType new_type = Type.unary( op, new Vector() );
    return unary( op, new_type, sampling_mode, error_mode );
  }

  /**
   * Pointwise unary operation applied to this. Applies
   * to Reals, Tuples (recursively to components), and to Field 
   * ranges (Field domains implicitly resampled if necessary). 
   * Does not apply to Field domains, Sets (regarded as domains
   * of Fields wthout ranges) or Text.
   * @param op  may be Data.ABS, Data.ACOS, etc; these include all
   *             unary operations defined for Java primitive data types
   * @param new_type  MathType of the result
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result, with MathType = new_type
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data unary( int op, MathType new_type, int sampling_mode,
                     int error_mode )
              throws VisADException, RemoteException {
    throw new TypeException("DataImpl: unary");
  }

  /** 
   * call unary() to clone this except with a new MathType
   * @param new_type  MathType of returned Data object
   * @return clone of this Data object except with new MathType
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data changeMathType(MathType new_type)
         throws VisADException, RemoteException {
    return unary(NOP, new_type, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the absolute value of this, using
   * default modes for sampling (Data.NEAREST_NEIGHBOR) and
   * error estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data abs() throws VisADException, RemoteException {
    return unary(ABS, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the arccos of this producing
   * radian Units, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data acos() throws VisADException, RemoteException {
    return unary(ACOS, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the arccos of this producing
   * degree Units, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data acosDegrees() throws VisADException, RemoteException {
    return unary(ACOS_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the arcsin of this producing
   * radian Units, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data asin() throws VisADException, RemoteException {
    return unary(ASIN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the arcsin of this producing
   * degree Units, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data asinDegrees() throws VisADException, RemoteException {
    return unary(ASIN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the arctan of this producing
   * radian Units, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data atan() throws VisADException, RemoteException {
    return unary(ATAN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the arctan of this producing
   * degree Units, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data atanDegrees() throws VisADException, RemoteException {
    return unary(ATAN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the ceiling of this, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and
   * error estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data ceil() throws VisADException, RemoteException {
    return unary(CEIL, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the cos of this assuming radian
   * Units unless this actual Units are degrees,
   * using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data cos() throws VisADException, RemoteException {
    return unary(COS, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the cos of this assuming degree
   * Units unless this actual Units are radians,
   * using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data cosDegrees() throws VisADException, RemoteException {
    return unary(COS_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the exponent of this, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and
   * error estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data exp() throws VisADException, RemoteException {
    return unary(EXP, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the floor of this, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and
   * error estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data floor() throws VisADException, RemoteException {
    return unary(FLOOR, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the log of this, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and
   * error estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data log() throws VisADException, RemoteException {
    return unary(LOG, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the rint (essentially round)
   * of this, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data rint() throws VisADException, RemoteException {
    return unary(RINT, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the round of this, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and error
   * estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data round() throws VisADException, RemoteException {
    return unary(ROUND, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the sin of this assuming radian
   * Units unless this actual Units are degrees,
   * using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data sin() throws VisADException, RemoteException {
    return unary(SIN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the sin of this assuming degree
   * Units unless this actual Units are radians,
   * using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data sinDegrees() throws VisADException, RemoteException {
    return unary(SIN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the square root of this, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and error
   * estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data sqrt() throws VisADException, RemoteException {
    return unary(SQRT, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the tan of this assuming radian
   * Units unless this actual Units are degrees,
   * using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data tan() throws VisADException, RemoteException {
    return unary(TAN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the tan of this assuming degree
   * Units unless this actual Units are radians,
   * using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data tanDegrees() throws VisADException, RemoteException {
    return unary(TAN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to negate this, using default modes for
   * sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data negate() throws VisADException, RemoteException {
    return unary(NEGATE, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /**
   * call unary() to take the absolute value of this
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data abs(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ABS, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the arccos of this producing
   * radian Units
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data acos(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ACOS, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the arccos of this producing
   * degree Units
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data acosDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ACOS_DEGREES, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the arcsin of this producing
   * radian Units
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data asin(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ASIN, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the arcsin of this producing
   * degree Units
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data asinDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ASIN_DEGREES, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the arctan of this producing
   * radian Units
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data atan(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ATAN, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the arctan of this producing
   * degree Units
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data atanDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ATAN_DEGREES, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the ceiling of this
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data ceil(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(CEIL, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the cos of this assuming radian
   * Units unless this actual Units are degrees
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data cos(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(COS, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the cos of this assuming degree
   * Units unless this actual Units are radians
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data cosDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(COS_DEGREES, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the exponent of this
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data exp(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(EXP, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the floor of this
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data floor(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(FLOOR, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the log of this
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data log(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(LOG, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the rint (essentially round)
   * of this
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data rint(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(RINT, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the round of this
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data round(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ROUND, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the sin of this assuming radian
   * Units unless this actual Units are degrees
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data sin(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(SIN, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the sin of this assuming degree
   * Units unless this actual Units are radians
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data sinDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(SIN_DEGREES, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the square root of this
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data sqrt(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(SQRT, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the tan of this assuming radian
   * Units unless this actual Units are degrees
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data tan(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(TAN, sampling_mode, error_mode);
  }

  /**
   * call unary() to take the tan of this assuming degree
   * Units unless this actual Units are radians
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data tanDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(TAN_DEGREES, sampling_mode, error_mode);
  }

  /**
   * call unary() to negate this
   * @param sampling_mode  may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public Data negate(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(NEGATE, sampling_mode, error_mode);
  }

  /**
   * returns new binary operator equivalent to op with
   * order of operands reversed
   * @param op an integer interpreted as an 'op' argument
   *           to binary()
   * @return an 'op' argument to binary() with order of
   *         operands reversed        
   * @throws VisADException  a VisAD error occurred
   */
  static int invertOp(int op) throws VisADException {
    switch(op) {
      case ADD:
        return ADD;
      case SUBTRACT:
        return INV_SUBTRACT;
      case INV_SUBTRACT:
        return SUBTRACT;
      case MULTIPLY:
        return MULTIPLY;
      case DIVIDE:
        return INV_DIVIDE;
      case INV_DIVIDE:
        return DIVIDE;
      case POW:
        return INV_POW;
      case INV_POW:
        return POW;
      case MAX:
        return MAX;
      case MIN:
        return MIN;
      case ATAN2:
        return INV_ATAN2;
      case ATAN2_DEGREES:
        return INV_ATAN2_DEGREES;
      case INV_ATAN2:
        return ATAN2;
      case INV_ATAN2_DEGREES:
        return ATAN2_DEGREES;
      case REMAINDER:
        return INV_REMAINDER;
      case INV_REMAINDER:
        return REMAINDER;
    }
    throw new ArithmeticException("DataImpl.invertOp: illegal operation");
  }

  /** dummy display for computeRanges() */
  private static DisplayImplJ2D rdisplay = null;
  private static Object lock = new Object();

  /** class used to synchronize with TRANSFORM_DONE events
      from dummy DisplayImplJ2D used by computeRanges() */
  public class Syncher extends Object implements DisplayListener {

    /**
     * construct Syncher, add as DisplayListener to dummy
     * DisplayImplJ2D, enableAction() and wait for TRANSFORM_DONE
     */
    Syncher() {
      try {
        synchronized (this) {
          rdisplay.addDisplayListener(this);
          rdisplay.enableAction();
          this.wait();
        }
      }
      catch(InterruptedException e) {
      }

      rdisplay.removeDisplayListener(this);
    }

    /**
     * look for TRANSFORM_DONE event from dummy DisplayImplJ2D
     * @param e DisplayEvent from dummy DisplayImplJ2D
     * @throws VisADException a VisAD error occurred
     * @throws RemoteException an RMI error occurred
     */
    public void displayChanged(DisplayEvent e)
           throws VisADException, RemoteException {
      if (e.getId() == DisplayEvent.TRANSFORM_DONE) {
        synchronized (this) {
          this.notify();
        }
      }
    }
  }

  /**
   * compute ranges of values in this of given RealTypes, using
   * a dummy DisplayImplJ2D
   * @param reals array of RealTypes whose value ranges to compute
   * @return double[reals.length][2] giving the low and high value
   *         in this for each RealType in reals
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public double[][] computeRanges(RealType[] reals)
         throws VisADException, RemoteException {
    synchronized (lock) {

      if (rdisplay == null) {
        // construct offscreen dummy display
        rdisplay = new DisplayImplJ2D("dummy", 4, 4);
      }

      if (reals == null || reals.length == 0) return null;
      int n = reals.length;
      ScalarMap[] maps = new ScalarMap[n];
      for (int i=0; i<n; i++) {
        maps[i] = new ScalarMap(reals[i], Display.Shape);
        rdisplay.addMap(maps[i]);
      }
      rdisplay.disableAction();
      DataReference ref = new DataReferenceImpl("dummy");
      ref.setData(this);
      rdisplay.reAutoScale();
      rdisplay.addReference(ref);
      new Syncher(); // wait for TRANSFORM_DONE
      double[][] ranges = new double[n][];
      for (int i=0; i<n; i++) {
        ranges[i] = maps[i].getRange();
      }
      rdisplay.removeReference(ref);
      rdisplay.clearMaps();
      return ranges;
    }
  }

  /** 
   * Compute ranges of values for each of 'n' RealTypes in
   * DisplayImpl.RealTypeVector. Called from DataRenderer 
   * with n = DisplayImpl.getScalarCount().
   * @param type ShadowType generated for MathType of this
   * @param n number of RealTypes in DisplayImpl.RealTypeVector
   * @return DataShadow instance containing double[][] array
   *         of RealType ranges, and an animation sampling Set
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException {
    double[][] ranges = new double[2][n];
    for (int i=0; i<n; i++) {
      ranges[0][i] = Double.MAX_VALUE; // init minimums
      ranges[1][i] = -Double.MAX_VALUE; // init maximums
    }
    DataShadow shadow = new DataShadow(ranges);
    return computeRanges(type, shadow);
  }

  /**
   * called by computeRanges() from RealTuple, SampledSet and
   * FlatField (used for range RealTuple in FlatField, domain is
   * handled by recursive domain Set call computeRanges()) to
   * recursively compute ranges of reference RealTupleType
   * @param shad_type ShadowRealTupleType of refering RealTuple
   *                  (refering RealTuple is implicit in cases of
   *                  SampledSet and FlatField)
   * @param coord_in CoordinateSystem connecting refering and
   *                 reference RealTuples
   * @param units_in default Units for RealTypes in refering RealTuple
   * @param shadow DataShadow instance whose contained double[][] 
   *               array and animation sampling Set are modified
   *               according to RealType values in reference
   *               RealTupleType, and used as return value
   * @param shad_ref ShadowRealTupleType of reference RealTupleType
   * @param ranges RealType value ranges in refering RealTuple
   * @return DataShadow instance containing double[][] array
   *         of RealType ranges, and an animation sampling Set
   * @throws VisADException a VisAD error occurred
   */
  DataShadow computeReferenceRanges(
             ShadowRealTupleType shad_type, CoordinateSystem coord_in,
             Unit[] units_in, DataShadow shadow,
             ShadowRealTupleType shad_ref, double[][] ranges)
             throws VisADException {
    RealTupleType type = (RealTupleType) shad_type.Type;
    RealTupleType ref = (RealTupleType) shad_ref.Type;
    int n = ranges[0].length;
    int len = 1;
    // indices is a 'base-5 integer' with n quints
    int[] indices = new int[n];
    for (int i=0; i<n; i++) {
      len = 5 * len;
      indices[i] = 0;
    }
    // len = 5 ^ n;
    double[][] vals = new double[n][len];
    for (int j=0; j<len; j++) {
      for (int i=0; i<n; i++) {
        switch(indices[i]) {
          case 0:
            vals[i][j] = ranges[0][i];
            break;
          case 1:
            vals[i][j] = 0.75 * ranges[0][i] + 0.25 * ranges[1][i];
            break;
          case 2:
            vals[i][j] = 0.5 * (ranges[0][i] + ranges[1][i]);
            break;
          case 3:
            vals[i][j] = 0.25 * ranges[0][i] + 0.75 * ranges[1][i];
            break;
          case 4:
            vals[i][j] = ranges[1][i];
            break;
        }
      }
      // increment 'base-5 integer' in indices array
      for (int i=0; i<n; i++) {
        indices[i]++;
        if (indices[i] == 5) {
          indices[i] = 0;
        }
        else {
          break;
        }
      }
    }

    // vals are the vertices of the n-dimensional box defined by ranges;
    // tranform them
    vals = CoordinateSystem.transformCoordinates(
                   ref, ref.getCoordinateSystem(), ref.getDefaultUnits(), null,
                   type, coord_in, units_in, null, vals);
    // mix vals into shadow.ranges
    for (int i=0; i<n; i++) {
      double min = Double.MAX_VALUE; // init minimum
      double max = -Double.MAX_VALUE; // init maximum
      for (int j=0; j<len; j++) {
        double val = vals[i][j];
        if (val == val) {
          min = Math.min(min, val);
          max = Math.max(max, val);
        }
      }
      int index = ((ShadowRealType) shad_ref.getComponent(i)).getIndex();
      if (index >= 0) {
        if (min == min) {
          shadow.ranges[0][index] = Math.min(shadow.ranges[0][index], min);
        }
        if (max == max) {
          shadow.ranges[1][index] = Math.max(shadow.ranges[1][index], max);
        }
      }
    }
    return shadow;
  }

  /**
   * return a clone of this, except with ErrorEstimates
   * combined with values in error, according to error_mode
   * @param error
   * @param error_mode  may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return clone of this, except with ErrorEstimates set
   *         according to values in error
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    return this;
  }

  /**
   * A wrapper around {@link #add(Data) add} for JPython
   */
  public Data __add__(Data data) throws VisADException, RemoteException {
    return add(data);
  }

  /**
   * A wrapper around {@link #subtract(Data) subtract} for JPython
   */
  public Data __sub__(Data data) throws VisADException, RemoteException {
    return subtract(data);
  }

  /**
   * A wrapper around {@link #multiply(Data) multiply} for JPython
   */
  public Data __mul__(Data data) throws VisADException, RemoteException {
    return multiply(data);
  }

  /**
   * A wrapper around {@link #divide(Data) divide} for JPython
   */
  public Data __div__(Data data) throws VisADException, RemoteException {
    return divide(data);
  }

  /**
   * A wrapper around {@link #pow(Data) pow} for JPython
   */
  public Data __pow__(Data data) throws VisADException, RemoteException {
    return pow(data);
  }

  /**
   * A wrapper around {@link #remainder(Data) remainder} for JPython
   */
  public Data __mod__(Data data) throws VisADException, RemoteException {
    return remainder(data);
  }

  /**
   * A wrapper around {@link #negate() negate} for JPython
   */
  public Data __neg__() throws VisADException, RemoteException {
    return negate();
  }

  /**
   * A wrapper around {@link #__add__(Data) __add__} for JPython
   */
  public Data __add__(double data) throws VisADException, RemoteException {
    return add(new Real(data));
  }

  /**
   * A wrapper around {@link #__add__(Data) __add__} for JPython
   */
  public Data __radd__(double data) throws VisADException, RemoteException {
    return add(new Real(data));
  }

  /**
   * A wrapper around {@link #__sub__(Data) __sub__} for JPython
   */
  public Data __sub__(double data) throws VisADException, RemoteException {
    return subtract(new Real(data));
  }

  /**
   * A wrapper around {@link #__sub__(Data) __sub__} for JPython
   */
  public Data __rsub__(double data) throws VisADException, RemoteException {
    return (new Real(data)).subtract(this);
  }


  /**
   * A wrapper around {@link #__mul__(Data) __mul__} for JPython
   */
  public Data __mul__(double data) throws VisADException, RemoteException {
    return multiply(new Real(data));
  }

  /**
   * A wrapper around {@link #__mul__(Data) __mul__} for JPython
   */
  public Data __rmul__(double data) throws VisADException, RemoteException {
    return multiply(new Real(data));
  }


  /**
   * A wrapper around {@link #__div__(Data) __div__} for JPython
   */
  public Data __div__(double data) throws VisADException, RemoteException {
    return divide(new Real(data));
  }

  /**
   * A wrapper around {@link #__div__(Data) __div__} for JPython
   */
  public Data __rdiv__(double data) throws VisADException, RemoteException {
    return (new Real(data)).divide(this);
  }


  /**
   * A wrapper around {@link #__pow__(Data) __pow__} for JPython
   * For low powers, do the multiply directly to preserve units.
   */
  public Data __pow__(double data) throws VisADException, RemoteException {
    if(data == 2.0) return multiply(this);
    if(data == 3.0) return multiply( multiply(this) );
    if(data == 4.0) return multiply( multiply( multiply(this) ) );
    return pow(new Real(data));
  }
  
  /**
   * A wrapper around {@link #__pow__(Data) __pow__} for JPython
   */
  public Data __rpow__(double data) throws VisADException, RemoteException {
    return (new Real(data)).pow(this);
  }


  /**
   * A wrapper around {@link #__mod__(Data) __mod__} for JPython
   */
  public Data __mod__(double data) throws VisADException, RemoteException {
    return remainder(new Real(data));
  }

  /**
   * A wrapper around {@link #__mod__(Data) __mod__} for JPython
   */
  public Data __rmod__(double data) throws VisADException, RemoteException {
    return (new Real(data)).remainder(this);
  }


  /**
   * A VisAD adaptation of clone that works for local or remote Data.
   * Catches CloneNotSupportedException and throws message in a
   * RuntimeException.
   * @return for DataImpl return clone(), and for RemoteDataImpl
   *         return clone() inherited from UnicastRemoteObject
   */
  public Object dataClone() {
    try {
      return clone();
    }
    catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex.toString());
    }
  }

  /**
   * <p>Clones this instance.  Information on the parent object of this instance
   * is not cloned, so -- following the general contract of the <code>clone()
   * </code> method -- subclasses should not test for equality of the parent
   * object in any <code>equals(Object)</code> method.</p>
   *
   * <p>This implementation never throws {@link CloneNotSupportedException}.</p>
   *
   * @return                            A clone of this instance.
   * @throws CloneNotSupportedException if cloning isn't supported.
   */
  public Object clone() throws CloneNotSupportedException {
    DataImpl clone = (DataImpl)super.clone();

    clone.parent = null;
    clone.rdisplay = null;
    clone.lock = new Object();

    return clone;
  }

  /**
   * @return a String representation of this
   */
  public String toString() {
    try {
      return longString("");
    }
    catch(VisADException e) {
      return e.toString();
    }
    catch(RemoteException e) {
      return e.toString();
    }
  }

  /** 
   * @return a longer String than returned by toString() 
   */
  public String longString()
         throws VisADException, RemoteException {
    return longString("");
  }

  /** 
   * @param pre String added to start of each line
   * @return a longer String than returned by toString(),
   *         indented by pre (a string of blanks)
   */
  public String longString(String pre)
         throws VisADException, RemoteException {
    throw new TypeException("DataImpl.longString");
  }

  /**
   * Simple DataImpl test, invoked as 'java visad.DataImpl'.
   * @param args array of command line arguments (not used)
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public static void main(String args[])
         throws VisADException, RemoteException {

    RealType[] types3d = {RealType.Latitude, RealType.Longitude, RealType.Radius};
    RealTupleType earth_location3d = new RealTupleType(types3d);
    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType grid_tuple = new FunctionType(earth_location3d, radiance);

    int size3d = 6;
    float level = 2.5f;
    FlatField grid3d = FlatField.makeField(grid_tuple, size3d, false);

    RealType[] types = {RealType.Latitude, RealType.Longitude, RealType.Radius,
                        vis_radiance, ir_radiance, RealType.Time};
    double[][] ranges = grid3d.computeRanges(types);
    for (int i=0; i<ranges.length; i++) {
      System.out.println(types[i] + ": " + ranges[i][0] + " to " + ranges[i][1]);
    }
    System.out.println(" ");

    FunctionType func = new FunctionType(radiance, RealType.Time);
    Integer2DSet fset = new Integer2DSet(2, 2);
    FlatField ff = new FlatField(func, fset);
    ff.setSamples(new float[][] {{0.0f, -1.0f, 1.0f, 2.0f}});
    ranges = ff.computeRanges(types);
    for (int i=0; i<ranges.length; i++) {
      System.out.println(types[i] + ": " + ranges[i][0] + " to " + ranges[i][1]);
    }
    System.exit(0);
  }

}

