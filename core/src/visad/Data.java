//
// Data.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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
   Data is the top-level interface of the VisAD data hierarchy.
   See the DataImpl class for more information.<P>
*/
public interface Data extends Thing {

  /** NEAREST_NEIGHBOR resampling mode */
  int NEAREST_NEIGHBOR = 100;
  /** WEIGHTED_AVERAGE resampling Mode */
  int WEIGHTED_AVERAGE = 101;

  /** INDEPENDENT error estimation Mode */
  int INDEPENDENT = 200;
  /** DEPENDENT error estimation Mode */
  int DEPENDENT = 201;
  /** NO_ERRORS error estimation Mode */
  int NO_ERRORS = 202;

  /** constants for various binary arithmetic operations */
  int ADD = 1;
  int SUBTRACT = 2; // this - operand
  int INV_SUBTRACT = 3; // operand - this
  int MULTIPLY = 4;
  int DIVIDE = 5; // this / operand
  int INV_DIVIDE = 6; // operand / this
  int POW = 7; // this ** operand
  int INV_POW = 8; // operand ** this
  int MAX = 9;
  int MIN = 10;
  int ATAN2 = 11; // atan2(this, operand)
  int ATAN2_DEGREES = 12; // atan2(this, operand)
  int INV_ATAN2 = 13; // atan2(operand, this)
  int INV_ATAN2_DEGREES = 14; // atan2(operand, this)
  int REMAINDER = 15; // this % operand
  int INV_REMAINDER = 16; // operand % this

  /** constants for various unary arithmetic operations */
  int ABS = 21;
  int ACOS = 22;
  int ACOS_DEGREES = 23;
  int ASIN = 24;
  int ASIN_DEGREES = 25;
  int ATAN = 26;
  int ATAN_DEGREES = 27;
  int CEIL = 28;
  int COS = 29;
  int COS_DEGREES = 30;
  int EXP = 31;
  int FLOOR = 32;
  int LOG = 33;
  int RINT = 34;
  int ROUND = 35;
  int SIN = 36;
  int SIN_DEGREES = 37;
  int SQRT = 38;
  int TAN = 39;
  int TAN_DEGREES = 40;
  int NEGATE = 41;
  int NOP = 42;

  /** constants for angle Unit conversions */
  double RADIANS_TO_DEGREES = 180.0 / Math.PI;
  double DEGREES_TO_RADIANS = Math.PI / 180.0;

  /**
   * @return a local copy if remote (i.e., this is RemoteData),
   *         else return this if local (i.e., this is DataImpl)
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  DataImpl local() throws VisADException, RemoteException;

  /**
   * @return MathType of this Data
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  MathType getType() throws VisADException, RemoteException;

  /**
   * @return flag indicating whether this Data has a missing value
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  boolean isMissing()
         throws VisADException, RemoteException;

  /**
   * Pointwise binary operation between this and data. Applies
   * to Reals, Tuples (recursively to components), and to Field
   * ranges (Field domains implicitly resampled if necessary).
   * Does not apply to Field domains or Sets (regarded as domains
   * of Fields wthout ranges). Data.ADD is only op defined for
   * Text, interpreted as concatenate. MathTypes of this and data
   * must match, or one may match the range of the other if it is
   * a FunctionType.
   * @param data other Data operand for binary operation
   * @param op may be Data.ADD, Data.SUBTRACT, etc; these include all
   *             binary operations defined for Java primitive data types
   * @param sampling_mode may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result of operation, which takes the MathType of this unless
   *         the default Units of that MathType conflict with Units of
   *         the result, in which case a generic MathType with appropriate
   *         Units is constructed
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  Data binary(Data data, int op, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  /**
   * Pointwise binary operation between this and data. Applies
   * to Reals, Tuples (recursively to components), and to Field 
   * ranges (Field domains implicitly resampled if necessary). 
   * Does not apply to Field domains or Sets (regarded as domains
   * of Fields wthout ranges). Data.ADD is only op defined for
   * Text, interpreted as concatenate. MathTypes of this and data
   * must match, or one may match the range of the other if it is
   * a FunctionType.
   * @param data other Data operand for binary operation
   * @param op may be Data.ADD, Data.SUBTRACT, etc; these include all
   *             binary operations defined for Java primitive data types
   * @param new_type MathType of the result
   * @param sampling_mode may be Data.NEAREST_NEIGHBOR or
   *                        Data.WEIGHTED_AVERAGE
   * @param error_mode may be Data.INDEPENDENT, Data.DEPENDENT or
   *                     Data.NO_ERRORS;
   * @return result, with MathType = new_type
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  Data binary(Data data, int op, MathType new_type,
                              int sampling_mode, int error_mode )
         throws VisADException, RemoteException;

  /**
   * call binary() to add data to this, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @param data other Data operand for binary operation
   * @return result of operation
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data add(Data data)
         throws VisADException, RemoteException;

  /**
   * call binary() to subtract data from this, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation 
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data subtract(Data data)
         throws VisADException, RemoteException;

  /**
   * call binary() to multiply this by data, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation 
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data multiply(Data data)
         throws VisADException, RemoteException;

  /**
   * call binary() to divide this by data, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation 
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data divide(Data data)
         throws VisADException, RemoteException;

  /**
   * call binary() to raise this to data power, using default modes
   * for sampling (Data.NEAREST_NEIGHBOR) and error estimation 
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data pow(Data data)
         throws VisADException, RemoteException;

  /**
   * call binary() to take the max of this and data, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and error estimation 
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data max(Data data)
         throws VisADException, RemoteException;

  /**
   * call binary() to take the min of this and data, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and error estimation 
   * (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data min(Data data)
         throws VisADException, RemoteException;

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
  Data atan2(Data data)
         throws VisADException, RemoteException;

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
  Data atan2Degrees(Data data)
         throws VisADException, RemoteException;

  /**
   * call binary() to take the remainder of this divided by
   * data, using default modes for sampling 
   * (Data.NEAREST_NEIGHBOR) and error estimation (Data.NO_ERRORS)
   * @param data  other Data operand for binary operation
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data remainder(Data data)
         throws VisADException, RemoteException;

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
  Data add(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data subtract(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data multiply(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

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
  Data divide(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data pow(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data max(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data min(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data atan2(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data atan2Degrees(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

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
  Data remainder(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

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
  Data unary(int op, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data unary(int op, MathType new_type, int sampling_mode,
                             int error_mode )
         throws VisADException, RemoteException;

  /**
   * call unary() to clone this except with a new MathType
   * @param new_type  MathType of returned Data object
   * @return clone of this Data object except with new MathType
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data changeMathType(MathType new_type)
         throws VisADException, RemoteException;

  /**
   * call unary() to take the absolute value of this, using
   * default modes for sampling (Data.NEAREST_NEIGHBOR) and
   * error estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data abs() throws VisADException, RemoteException;

  /**
   * call unary() to take the arccos of this producing
   * radian Units, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data acos() throws VisADException, RemoteException;

  /**
   * call unary() to take the arccos of this producing
   * degree Units, using default modes for sampling 
   * (Data.NEAREST_NEIGHBOR) and error estimation 
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data acosDegrees() throws VisADException, RemoteException;

  /**
   * call unary() to take the arcsin of this producing
   * radian Units, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data asin() throws VisADException, RemoteException;

  /**
   * call unary() to take the arcsin of this producing
   * degree Units, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data asinDegrees() throws VisADException, RemoteException;

  /**
   * call unary() to take the arctan of this producing
   * radian Units, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data atan() throws VisADException, RemoteException;

  /**
   * call unary() to take the arctan of this producing
   * degree Units, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data atanDegrees() throws VisADException, RemoteException;

  /**
   * call unary() to take the ceiling of this, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and
   * error estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data ceil() throws VisADException, RemoteException;

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
  Data cos() throws VisADException, RemoteException;

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
  Data cosDegrees() throws VisADException, RemoteException;

  /**
   * call unary() to take the exponent of this, using default 
   * modes for sampling (Data.NEAREST_NEIGHBOR) and 
   * error estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data exp() throws VisADException, RemoteException;

  /**
   * call unary() to take the floor of this, using default 
   * modes for sampling (Data.NEAREST_NEIGHBOR) and 
   * error estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data floor() throws VisADException, RemoteException;

  /**
   * call unary() to take the log of this, using default 
   * modes for sampling (Data.NEAREST_NEIGHBOR) and 
   * error estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data log() throws VisADException, RemoteException;

  /**
   * call unary() to take the rint (essentially round)
   * of this, using default modes for sampling
   * (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data rint() throws VisADException, RemoteException;

  /**
   * call unary() to take the round of this, using default
   * modes for sampling (Data.NEAREST_NEIGHBOR) and error
   * estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data round() throws VisADException, RemoteException;

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
  Data sin() throws VisADException, RemoteException;

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
  Data sinDegrees() throws VisADException, RemoteException;

  /**
   * call unary() to take the square root of this, using default 
   * modes for sampling (Data.NEAREST_NEIGHBOR) and error 
   * estimation (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data sqrt() throws VisADException, RemoteException;

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
  Data tan() throws VisADException, RemoteException;

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
  Data tanDegrees() throws VisADException, RemoteException;

  /**
   * call unary() to negate this, using default modes for
   * sampling (Data.NEAREST_NEIGHBOR) and error estimation
   * (Data.NO_ERRORS)
   * @return result of operation
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  Data negate() throws VisADException, RemoteException;

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
  Data abs(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data acos(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data acosDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data asin(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data asinDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data atan(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data atanDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data ceil(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data cos(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data cosDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data exp(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data floor(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data log(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data rint(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data round(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data sin(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data sinDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data sqrt(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data tan(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data tanDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

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
  Data negate(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  /**
   * compute ranges of values in this of given RealTypes, using
   * a dummy DisplayImplJ2D
   * @param reals array of RealTypes whose value ranges to compute
   * @return double[reals.length][2] giving the low and high value
   *         in this for each RealType in reals
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  double[][] computeRanges(RealType[] reals)
         throws VisADException, RemoteException;

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
  DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException;

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
  DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException;

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
  Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException;

  /**
   * @return a longer String than returned by toString()
   */
  String longString()
         throws VisADException, RemoteException;

  /** 
   * @param pre String added to start of each line
   * @return a longer String than returned by toString(),
   *         indented by pre (a string of blanks)
   */
  String longString(String pre)
         throws VisADException, RemoteException;

  /** 
   * A VisAD adaptation of clone that works for local or remote Data.
   * Catches CloneNotSupportedException and throws message in a
   * RuntimeException.
   * @return for DataImpl return clone(), and for RemoteDataImpl
   *         return clone() inherited from UnicastRemoteObject
   */
  Object dataClone() throws RemoteException;
}

