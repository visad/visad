
//
// Data.java
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
   Data is the top-level interface of the VisAD data hierarchy.
   See the DataImpl class for more information.<P>
*/
public interface Data extends Thing {

  /** NEAREST_NEIGHBOR resampling mode */
  public static final int NEAREST_NEIGHBOR = 100;
  /** WEIGHTED_AVERAGE resampling Mode */
  public static final int WEIGHTED_AVERAGE = 101;

  /** INDEPENDENT error estimation Mode */
  public static final int INDEPENDENT = 200;
  /** DEPENDENT error estimation Mode */
  public static final int DEPENDENT = 201;
  /** NO_ERRORS error estimation Mode */
  public static final int NO_ERRORS = 202;

  /** constants for various binary arithmetic operations */
  final static int ADD = 1;
  final static int SUBTRACT = 2; // this - operand
  final static int INV_SUBTRACT = 3; // operand - this
  final static int MULTIPLY = 4;
  final static int DIVIDE = 5; // this / operand
  final static int INV_DIVIDE = 6; // operand / this
  final static int POW = 7; // this ** operand
  final static int INV_POW = 8; // operand ** this
  final static int MAX = 9;
  final static int MIN = 10;
  final static int ATAN2 = 11; // atan2(this, operand)
  final static int ATAN2_DEGREES = 12; // atan2(this, operand)
  final static int INV_ATAN2 = 13; // atan2(operand, this)
  final static int INV_ATAN2_DEGREES = 14; // atan2(operand, this)
  final static int REMAINDER = 15; // this % operand
  final static int INV_REMAINDER = 16; // operand % this

  /** constants for various unary arithmetic operations */
  final static int ABS = 21;
  final static int ACOS = 22;
  final static int ACOS_DEGREES = 23;
  final static int ASIN = 24;
  final static int ASIN_DEGREES = 25;
  final static int ATAN = 26;
  final static int ATAN_DEGREES = 27;
  final static int CEIL = 28;
  final static int COS = 29;
  final static int COS_DEGREES = 30;
  final static int EXP = 31;
  final static int FLOOR = 32;
  final static int LOG = 33;
  final static int RINT = 34;
  final static int ROUND = 35;
  final static int SIN = 36;
  final static int SIN_DEGREES = 37;
  final static int SQRT = 38;
  final static int TAN = 39;
  final static int TAN_DEGREES = 40;
  final static int NEGATE = 41;

  /** constants for angle Unit conversions */
  final static double RADIANS_TO_DEGREES = 180.0 / Math.PI;
  final static double DEGREES_TO_RADIANS = Math.PI / 180.0;

  /** DataImpl.local() returns 'this'
      RemoteDataImpl.local() returns 'AdaptedData' */
  public abstract DataImpl local() throws VisADException, RemoteException;

  public abstract MathType getType() throws VisADException, RemoteException;

  /** a method to tell whether data object has a missing value */
  public abstract boolean isMissing()
         throws VisADException, RemoteException;

  /** binary operations */
  public abstract Data binary(Data data, int op, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  /*- BINARY - TDR June 1998  */
  public abstract Data binary(Data data, int op, MathType new_type, 
                              int sampling_mode, int error_mode )
         throws VisADException, RemoteException;

  /** a list of binary operations using default modes for
      sampling and error estimation */
  public abstract Data add(Data data)
         throws VisADException, RemoteException;

  public abstract Data subtract(Data data)
         throws VisADException, RemoteException;

  public abstract Data multiply(Data data)
         throws VisADException, RemoteException;

  public abstract Data divide(Data data)
         throws VisADException, RemoteException;

  public abstract Data pow(Data data)
         throws VisADException, RemoteException;

  public abstract Data max(Data data)
         throws VisADException, RemoteException;

  public abstract Data min(Data data)
         throws VisADException, RemoteException;

  public abstract Data atan2(Data data)
         throws VisADException, RemoteException;

  public abstract Data atan2Degrees(Data data)
         throws VisADException, RemoteException;

  public abstract Data remainder(Data data)
         throws VisADException, RemoteException;

  /** a list of binary operations supporting non-default modes for
      sampling and error estimation */
  public abstract Data add(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data subtract(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data multiply(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  public abstract Data divide(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data pow(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data max(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data min(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data atan2(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data atan2Degrees(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  public abstract Data remainder(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  /** unary operations */
  public abstract Data unary(int op, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  /** a list of unary operations using default modes for
      sampling and error estimation */
  public abstract Data abs() throws VisADException, RemoteException;

  public abstract Data acos() throws VisADException, RemoteException;

  public abstract Data acosDegrees() throws VisADException, RemoteException;

  public abstract Data asin() throws VisADException, RemoteException;

  public abstract Data asinDegrees() throws VisADException, RemoteException;

  public abstract Data atan() throws VisADException, RemoteException;

  public abstract Data atanDegrees() throws VisADException, RemoteException;

  public abstract Data ceil() throws VisADException, RemoteException;

  public abstract Data cos() throws VisADException, RemoteException;

  public abstract Data cosDegrees() throws VisADException, RemoteException;

  public abstract Data exp() throws VisADException, RemoteException;

  public abstract Data floor() throws VisADException, RemoteException;

  public abstract Data log() throws VisADException, RemoteException;

  public abstract Data rint() throws VisADException, RemoteException;

  public abstract Data round() throws VisADException, RemoteException;

  public abstract Data sin() throws VisADException, RemoteException;

  public abstract Data sinDegrees() throws VisADException, RemoteException;

  public abstract Data sqrt() throws VisADException, RemoteException;

  public abstract Data tan() throws VisADException, RemoteException;

  public abstract Data tanDegrees() throws VisADException, RemoteException;

  public abstract Data negate() throws VisADException, RemoteException;

  /** a list of unary operations supporting non-default modes for
      sampling and error estimation */
  public abstract Data abs(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data acos(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data acosDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data asin(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data asinDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data atan(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data atanDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data ceil(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data cos(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data cosDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data exp(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data floor(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data log(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data rint(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data round(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data sin(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data sinDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data sqrt(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data tan(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data tanDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data negate(int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  /** compute ranges of values for each of 'n' RealType-s in
      DisplayImpl.RealTypeVector;
      would like 'default' visibility here, but must be declared
      'public' because it is defined in the Data interface */
  public abstract DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException;

  /** recursive version of computeRanges;
      would like 'default' visibility here, but must be declared
      'public' because it is defined in the Data interface */
  public abstract DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException;

  /** adjust ErrorEstimate-s for sampling errors in error;
      would like 'default' visibility here, but must be declared
      'public' because it is defined in the Data interface */
  public abstract Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException;

  /** generates a longer string than generated by toString */
  public abstract String longString()
         throws VisADException, RemoteException;

  /** generates a longer string than generated by toString,
      indented by pre (a string of blanks) */
  public abstract String longString(String pre)
         throws VisADException, RemoteException;

  /** DataImpl.dataClone returns clone;
      RemoteDataImpl.dataClone returns clone inherited from
      UnicastRemoteObject */
  public abstract Object dataClone() throws RemoteException;

}

