//
// Data.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;
import java.rmi.*;

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

  /** if remote (i.e., RemoteData), return a local copy;
      if local (i.e., DataImpl), return this */
  DataImpl local() throws VisADException, RemoteException;

  MathType getType() throws VisADException, RemoteException;

  /** a method to tell whether data object has a missing value */
  boolean isMissing()
         throws VisADException, RemoteException;

  /** general binary operation between this and data; op may
      be Data.ADD, Data.SUBTRACT, etc; these include all binary
      operations defined for Java primitive data types; sampling_mode
      may be Data.NEAREST_NEIGHBOR or Data.WEIGHTED_AVERAGE; error_mode
      may be Data.INDEPENDENT, Data.DEPENDENT or Data.NO_ERRORS;
      result takes the MathType of this unless the default Units of
      that MathType conflict with Units of the result, in which case
      a generic MathType with appropriate Units is constructed */
  Data binary(Data data, int op, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  /*- BINARY - TDR June 1998  */
  /** general binary operation between this and data; op may
      be Data.ADD, Data.SUBTRACT, etc; these include all binary
      operations defined for Java primitive data types; new_type
      is the MathType of the result; sampling_mode may be
      Data.NEAREST_NEIGHBOR or Data.WEIGHTED_AVERAGE; error_mode
      may be Data.INDEPENDENT, Data.DEPENDENT or Data.NO_ERRORS */
  Data binary(Data data, int op, MathType new_type,
                              int sampling_mode, int error_mode )
         throws VisADException, RemoteException;

  /** a list of binary operations using default modes for
      sampling (Data.NEAREST_NEIGHBOR) and error estimation
      (Data.NO_ERRORS) */
  Data add(Data data)
         throws VisADException, RemoteException;

  Data subtract(Data data)
         throws VisADException, RemoteException;

  Data multiply(Data data)
         throws VisADException, RemoteException;

  Data divide(Data data)
         throws VisADException, RemoteException;

  Data pow(Data data)
         throws VisADException, RemoteException;

  Data max(Data data)
         throws VisADException, RemoteException;

  Data min(Data data)
         throws VisADException, RemoteException;

  Data atan2(Data data)
         throws VisADException, RemoteException;

  Data atan2Degrees(Data data)
         throws VisADException, RemoteException;

  Data remainder(Data data)
         throws VisADException, RemoteException;

  /** a list of binary operations supporting non-default modes for
      sampling and error estimation */
  Data add(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data subtract(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data multiply(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  Data divide(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data pow(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data max(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data min(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data atan2(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data atan2Degrees(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  Data remainder(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  /** general unary operation; operation may be Data.ABS, Data.ACOS, etc;
      these include all unary operations defined for Java primitive data
      types; sampling_mode may be Data.NEAREST_NEIGHBOR or
      Data.WEIGHTED_AVERAGE; error_mode may be Data.INDEPENDENT,
      Data.DEPENDENT or Data.NO_ERRORS; result takes
      the MathType of this unless the default Units of that MathType
      conflict with Units of the result, in which case a generic
      MathType with appropriate Units is constructed */
  Data unary(int op, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  /*- TDR June 1998  */
  /** general unary operation; operation may be Data.ABS, Data.ACOS, etc;
      these include all unary operations defined for Java primitive data
      types; new_type is the MathType of the result; sampling_mode may be
      Data.NEAREST_NEIGHBOR or Data.WEIGHTED_AVERAGE; error_mode may be
      Data.INDEPENDENT, Data.DEPENDENT or Data.NO_ERRORS */
  Data unary(int op, MathType new_type, int sampling_mode,
                             int error_mode )
         throws VisADException, RemoteException;

  /** clone this Data object except give it new_type */
  Data changeMathType(MathType new_type)
         throws VisADException, RemoteException;

  /** a list of unary operations using default modes for
      sampling (Data.NEAREST_NEIGHBOR) and error estimation
      (Data.NO_ERRORS) */
  Data abs() throws VisADException, RemoteException;

  Data acos() throws VisADException, RemoteException;

  Data acosDegrees() throws VisADException, RemoteException;

  Data asin() throws VisADException, RemoteException;

  Data asinDegrees() throws VisADException, RemoteException;

  Data atan() throws VisADException, RemoteException;

  Data atanDegrees() throws VisADException, RemoteException;

  Data ceil() throws VisADException, RemoteException;

  Data cos() throws VisADException, RemoteException;

  Data cosDegrees() throws VisADException, RemoteException;

  Data exp() throws VisADException, RemoteException;

  Data floor() throws VisADException, RemoteException;

  Data log() throws VisADException, RemoteException;

  Data rint() throws VisADException, RemoteException;

  Data round() throws VisADException, RemoteException;

  Data sin() throws VisADException, RemoteException;

  Data sinDegrees() throws VisADException, RemoteException;

  Data sqrt() throws VisADException, RemoteException;

  Data tan() throws VisADException, RemoteException;

  Data tanDegrees() throws VisADException, RemoteException;

  Data negate() throws VisADException, RemoteException;

  /** a list of unary operations supporting non-default modes for
      sampling and error estimation */
  Data abs(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data acos(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data acosDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data asin(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data asinDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data atan(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data atanDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data ceil(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data cos(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data cosDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data exp(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data floor(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data log(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data rint(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data round(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data sin(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data sinDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data sqrt(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data tan(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data tanDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data negate(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  /** return range of values of RealType real[i] in
      return[i][0], return[i][1] */
  double[][] computeRanges(RealType[] reals)
         throws VisADException, RemoteException;

  /** compute ranges of values for each of 'n' RealType-s in
      DisplayImpl.RealTypeVector;
      would like 'default' visibility here, but must be declared
      'public' because it is defined in the Data interface */
  DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException;

  /** recursive version of computeRanges;
      would like 'default' visibility here, but must be declared
      'public' because it is defined in the Data interface */
  DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException;

  /** adjust ErrorEstimate-s for sampling errors in error;
      would like 'default' visibility here, but must be declared
      'public' because it is defined in the Data interface */
  Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException;

  /** generates a longer string than generated by toString */
  String longString()
         throws VisADException, RemoteException;

  /** generates a longer string than generated by toString,
      indented by pre (a string of blanks) */
  String longString(String pre)
         throws VisADException, RemoteException;

  /** DataImpl.dataClone returns clone;
      RemoteDataImpl.dataClone returns clone inherited from
      UnicastRemoteObject */
  Object dataClone() throws RemoteException;

}

