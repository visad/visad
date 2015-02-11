//
// RemoteDataImpl.java
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

import java.rmi.RemoteException;

/**
   RemoteDataImpl is the VisAD remote adapter for DataImpl.<P>
   Adapts methods from DataImpl, but not:
     equals, toString, hashCode or clone.<P>
*/
public class RemoteDataImpl extends RemoteThingImpl
       implements RemoteData {

  /** 'this' is the Remote adaptor for AdaptedData (which is local);
      AdaptedData is transient because UnicastRemoteObject is
      Serializable, but a copy of 'this' on another JVM will not
      be local to AdaptedData and cannot adapt it;
      the methods of RemoteDataImpl test for null AdaptedData */
  final transient DataImpl AdaptedData;

  /**
   * construct a RemoteDataImpl adapting local data
   * @param data adapted DataImpl
   * @throws RemoteException an RMI error occurred
   */
  public RemoteDataImpl(DataImpl data) throws RemoteException {
    super(data);
    AdaptedData = data;
  }

  // methods adapted from Data;
  //   do not adapt equals, toString, hashCode or clone

  /**
   * @return a local copy (AdaptedData, which is Serializable)
   */
  public DataImpl local() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.local " +
                                     "AdaptedData is null");
    }
    return AdaptedData;
  }

  /**
   * @return MathType of this Data
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public MathType getType() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.getType " +
                                     "AdaptedData is null");
    }
    return AdaptedData.getType();
  }

  /**
   * @return flag indicating whether this Data has a missing value
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public boolean isMissing() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.isMissing " +
                                     "AdaptedData is null");
    }
    return AdaptedData.isMissing();
  }

  /**
   * Pointwise binary operation between this (AdaptedData) and data.
   * Applies to Reals, Tuples (recursively to components), and to
   * Field ranges (Field domains implicitly resampled if necessary).
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
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.binary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.binary(data, op, sampling_mode, error_mode);
  }

  /**
   * Pointwise binary operation between this (AdaptedData) and data.
   * Applies to Reals, Tuples (recursively to components), and to
   * Field ranges (Field domains implicitly resampled if necessary).
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
  public Data binary(Data data, int op, MathType new_type,
                     int sampling_mode, int error_mode )
              throws VisADException, RemoteException {
    if (AdaptedData == null ) {
      throw new RemoteVisADException("RemoteDataImpl.binary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.binary(data, op, new_type, sampling_mode, error_mode);
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
   * Pointwise unary operation applied to this (AdaptedData). Applies
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
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.unary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.unary(op, sampling_mode, error_mode);
  }

  /**
   * Pointwise unary operation applied to this (AdaptedData). Applies
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
  public Data unary(int op, MathType new_type,
                    int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.unary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.unary(op, new_type, sampling_mode, error_mode);
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
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.computeRanges " +
                                     "AdaptedData is null");
    }
    return AdaptedData.computeRanges(reals);
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
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.computeRanges " +
                                     "AdaptedData is null");
    }
    return AdaptedData.computeRanges(type, n);
  }

  /**
   * Recursive version of computeRanges(), called down through
   * Data object tree.
   * @param type ShadowType generated for MathType of this
   * @param shadow DataShadow instance whose contained double[][]
   *               array and animation sampling Set are modified
   *               according to RealType values in this, and used
   *               as return value
   * @return DataShadow instance containing double[][] array
   *         of RealType ranges, and an animation sampling Set
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.computeRanges " +
                                     "AdaptedData is null");
    }
    return AdaptedData.computeRanges(type, shadow);
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
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.adjustSamplingError " +
                                     "AdaptedData is null");
    }
    return AdaptedData.adjustSamplingError(error, error_mode);
  }

  /**
   * @return a longer String than returned by toString()
   */
  public String longString() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.longString " +
                                     "AdaptedData is null");
    }
    return AdaptedData.longString();
  }

  /**
   * @param pre String added to start of each line
   * @return a longer String than returned by toString(),
   *         indented by pre (a string of blanks)
   */
  public String longString(String pre)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.longString " +
                                     "AdaptedData is null");
    }
    return AdaptedData.longString(pre);
  }

  /**
   * A VisAD adaptation of clone that works for local or remote Data.
   * Catches CloneNotSupportedException and throws message in a
   * RuntimeException.
   * @return for DataImpl return clone(), and for RemoteDataImpl
   *         return clone() inherited from UnicastRemoteObject
   */
  public Object dataClone() throws RemoteException {
    try {
      return clone();
    }
    catch (CloneNotSupportedException e) {
      throw new VisADError("RemoteDataImpl.dataClone: " +
                           "CloneNotSupportedException occurred");
    }
  }

}

