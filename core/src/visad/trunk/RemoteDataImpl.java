//
// RemoteDataImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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
import java.rmi.server.UnicastRemoteObject;

/**
   RemoteDataImpl is the VisAD remote adapter for DataImpl.<P>
*/
public abstract class RemoteDataImpl extends RemoteThingImpl
       implements RemoteData {

  /** 'this' is the Remote adaptor for AdaptedData (which is local);
      AdaptedData is transient because UnicastRemoteObject is
      Serializable, but a copy of 'this' on another JVM will not
      be local to AdaptedData and cannot adapt it;
      the methods of RemoteDataImpl test for null AdaptedData */
  final transient DataImpl AdaptedData;

  public RemoteDataImpl(DataImpl data) throws RemoteException {
    super(data);
    AdaptedData = data;
  }

  /** methods adapted from Data;
      do not adapt equals, toString, hashCode or clone */

  /** DataImpl.local() returns 'this'
      RemoteDataImpl.local() returns 'AdaptedData' */
  public DataImpl local() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.local " +
                                     "AdaptedData is null");
    }
    return AdaptedData;
  }

  public MathType getType() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.getType " +
                                     "AdaptedData is null");
    }
    return AdaptedData.getType();
  }

  public boolean isMissing() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.isMissing " +
                                     "AdaptedData is null");
    }
    return AdaptedData.isMissing();
  }

  /** binary operations adapted to AdaptedData */
  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.binary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.binary(data, op, sampling_mode, error_mode);
  }

  /*- TDR June 1998  */
  public Data binary(Data data, int op, MathType new_type,
                     int sampling_mode, int error_mode )
              throws VisADException, RemoteException {
    if (AdaptedData == null ) {
      throw new RemoteVisADException("RemoteDataImpl.binary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.binary(data, op, new_type, sampling_mode, error_mode);
  }

/* WLH 8 July 2000
  // a list of binary operations using default modes for
  // sampling and error estimation
  public Data add(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.add " +
                                     "AdaptedData is null");
    }
    return AdaptedData.add(data);
  }

  public Data subtract(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.subtract " +
                                     "AdaptedData is null");
    }
    return AdaptedData.subtract(data);
  }

  public Data multiply(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.multiply " +
                                     "AdaptedData is null");
    }
    return AdaptedData.multiply(data);
  }

  public Data divide(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.divide " +
                                     "AdaptedData is null");
    }
    return AdaptedData.divide(data);
  }

  public Data pow(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.pow " +
                                     "AdaptedData is null");
    }
    return AdaptedData.pow(data);
  }

  public Data max(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.max " +
                                     "AdaptedData is null");
    }
    return AdaptedData.max(data);
  }

  public Data min(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.min " +
                                     "AdaptedData is null");
    }
    return AdaptedData.min(data);
  }

  public Data atan2(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.atan2 " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan2(data);
  }

  public Data atan2Degrees(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.atan2Degrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan2Degrees(data);
  }

  public Data remainder(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.remainder " +
                                     "AdaptedData is null");
    }
    return AdaptedData.remainder(data);
  }

  // a list of binary operations supporting non-default modes for
  // sampling and error estimation
  public Data add(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.add " +
                                     "AdaptedData is null");
    }
    return AdaptedData.add(data, sampling_mode, error_mode);
  }

  public Data subtract(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.subtract " +
                                     "AdaptedData is null");
    }
    return AdaptedData.subtract(data, sampling_mode, error_mode);
  }

  public Data multiply(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.multiply " +
                                     "AdaptedData is null");
    }
    return AdaptedData.multiply(data, sampling_mode, error_mode);
  }

  public Data divide(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.divide " +
                                     "AdaptedData is null");
    }
    return AdaptedData.divide(data, sampling_mode, error_mode);
  }

  public Data pow(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.pow " +
                                     "AdaptedData is null");
    }
    return AdaptedData.pow(data, sampling_mode, error_mode);
  }

  public Data max(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.max " +
                                     "AdaptedData is null");
    }
    return AdaptedData.max(data, sampling_mode, error_mode);
  }

  public Data min(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.min " +
                                     "AdaptedData is null");
    }
    return AdaptedData.min(data, sampling_mode, error_mode);
  }

  public Data atan2(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.atan2 " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan2(data, sampling_mode, error_mode);
  }

  public Data atan2Degrees(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.atan2Degrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan2Degrees(data, sampling_mode, error_mode);
  }

  public Data remainder(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.remainder " +
                                     "AdaptedData is null");
    }
    return AdaptedData.remainder(data, sampling_mode, error_mode);
  }
*/

/* WLH 8 July 2000 */
  public Data add(Data data) throws VisADException, RemoteException {
    return binary(data, ADD, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data subtract(Data data) throws VisADException, RemoteException {
    return binary(data, SUBTRACT, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data multiply(Data data) throws VisADException, RemoteException {
    return binary(data, MULTIPLY, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data divide(Data data) throws VisADException, RemoteException {
    return binary(data, DIVIDE, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data pow(Data data) throws VisADException, RemoteException {
    return binary(data, POW, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data max(Data data) throws VisADException, RemoteException {
    return binary(data, MAX, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data min(Data data) throws VisADException, RemoteException {
    return binary(data, MIN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data atan2(Data data) throws VisADException, RemoteException {
    return binary(data, ATAN2, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data atan2Degrees(Data data) throws VisADException, RemoteException {
    return binary(data, ATAN2_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data remainder(Data data) throws VisADException, RemoteException {
    return binary(data, REMAINDER, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  // a list of binary operations supporting non-default modes for
  // sampling and error estimation
  public Data add(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, ADD, sampling_mode, error_mode);
  }

  public Data subtract(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, SUBTRACT, sampling_mode, error_mode);
  }

  public Data multiply(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, MULTIPLY, sampling_mode, error_mode);
  }

  public Data divide(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, DIVIDE, sampling_mode, error_mode);
  }

  public Data pow(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, POW, sampling_mode, error_mode);
  }

  public Data max(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, MAX, sampling_mode, error_mode);
  }

  public Data min(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, MIN, sampling_mode, error_mode);
  }

  public Data atan2(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, ATAN2, sampling_mode, error_mode);
  }

  public Data atan2Degrees(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, ATAN2_DEGREES, sampling_mode, error_mode);
  }

  public Data remainder(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, REMAINDER, sampling_mode, error_mode);
  }
/* end WLH 8 July 2000 */

  /** unary operations adapted to AdaptedData */
  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.unary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.unary(op, sampling_mode, error_mode);
  }

  /*- TDR July 1998  */
  public Data unary(int op, MathType new_type,
                    int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.unary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.unary(op, new_type, sampling_mode, error_mode);
  }

/* WLH 8 July 2000
  // WLH 5 Sept 98
  public Data changeMathType(MathType new_type)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.changeMathType " +
                                     "AdaptedData is null");
    }
    return AdaptedData.changeMathType(new_type);
  }

  // a list of unary operations using default modes for
  // sampling and error estimation
  public Data abs() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.abs " +
                                     "AdaptedData is null");
    }
    return AdaptedData.abs();
  }

  public Data acos() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.acos " +
                                     "AdaptedData is null");
    }
    return AdaptedData.acos();
  }

  public Data acosDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.acosDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.acosDegrees();
  }

  public Data asin() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.asin " +
                                     "AdaptedData is null");
    }
    return AdaptedData.asin();
  }

  public Data asinDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.asinDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.asinDegrees();
  }

  public Data atan() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.atan " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan();
  }

  public Data atanDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.atanDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atanDegrees();
  }

  public Data ceil() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.ceil " +
                                     "AdaptedData is null");
    }
    return AdaptedData.ceil();
  }

  public Data cos() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.cos " +
                                     "AdaptedData is null");
    }
    return AdaptedData.cos();
  }

  public Data cosDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.cosDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.cosDegrees();
  }

  public Data exp() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.exp " +
                                     "AdaptedData is null");
    }
    return AdaptedData.exp();
  }

  public Data floor() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.floor " +
                                     "AdaptedData is null");
    }
    return AdaptedData.floor();
  }

  public Data log() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.log " +
                                     "AdaptedData is null");
    }
    return AdaptedData.log();
  }

  public Data rint() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.rint " +
                                     "AdaptedData is null");
    }
    return AdaptedData.rint();
  }

  public Data round() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.round " +
                                     "AdaptedData is null");
    }
    return AdaptedData.round();
  }

  public Data sin() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.sin " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sin();
  }

  public Data sinDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.sinDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sinDegrees();
  }

  public Data sqrt() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.sqrt " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sqrt();
  }

  public Data tan() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.tan " +
                                     "AdaptedData is null");
    }
    return AdaptedData.tan();
  }

  public Data tanDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.tanDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.tanDegrees();
  }

  public Data negate() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.negate " +
                                     "AdaptedData is null");
    }
    return AdaptedData.negate();
  }

  // a list of unary operations supporting non-default modes for
  // sampling and error estimation
  public Data abs(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.abs " +
                                     "AdaptedData is null");
    }
    return AdaptedData.abs(sampling_mode, error_mode);
  }

  public Data acos(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.acos " +
                                     "AdaptedData is null");
    }
    return AdaptedData.acos(sampling_mode, error_mode);
  }

  public Data acosDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.acosDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.acosDegrees(sampling_mode, error_mode);
  }

  public Data asin(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.asin " +
                                     "AdaptedData is null");
    }
    return AdaptedData.asin(sampling_mode, error_mode);
  }

  public Data asinDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.asinDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.asinDegrees(sampling_mode, error_mode);
  }

  public Data atan(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.atan " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan(sampling_mode, error_mode);
  }

  public Data atanDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.atanDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atanDegrees(sampling_mode, error_mode);
  }

  public Data ceil(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.ceil " +
                                     "AdaptedData is null");
    }
    return AdaptedData.ceil(sampling_mode, error_mode);
  }

  public Data cos(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.cos " +
                                     "AdaptedData is null");
    }
    return AdaptedData.cos(sampling_mode, error_mode);
  }

  public Data cosDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.cosDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.cosDegrees(sampling_mode, error_mode);
  }

  public Data exp(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.exp " +
                                     "AdaptedData is null");
    }
    return AdaptedData.exp(sampling_mode, error_mode);
  }

  public Data floor(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.floor " +
                                     "AdaptedData is null");
    }
    return AdaptedData.floor(sampling_mode, error_mode);
  }

  public Data log(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.log " +
                                     "AdaptedData is null");
    }
    return AdaptedData.log(sampling_mode, error_mode);
  }

  public Data rint(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.rint " +
                                     "AdaptedData is null");
    }
    return AdaptedData.rint(sampling_mode, error_mode);
  }

  public Data round(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.round " +
                                     "AdaptedData is null");
    }
    return AdaptedData.round(sampling_mode, error_mode);
  }

  public Data sin(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.sin " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sin(sampling_mode, error_mode);
  }

  public Data sinDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.sinDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sinDegrees(sampling_mode, error_mode);
  }

  public Data sqrt(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.sqrt " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sqrt(sampling_mode, error_mode);
  }

  public Data tan(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.tan " +
                                     "AdaptedData is null");
    }
    return AdaptedData.tan(sampling_mode, error_mode);
  }

  public Data tanDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.tanDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.tanDegrees(sampling_mode, error_mode);
  }

  public Data negate(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.negate " +
                                     "AdaptedData is null");
    }
    return AdaptedData.negate(sampling_mode, error_mode);
  }
*/
/* WLH 8 July 2000 */
  public Data changeMathType(MathType new_type)
         throws VisADException, RemoteException {
    return unary(NOP, new_type, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  // a list of unary operations using default modes for
  // sampling and error estimation
  public Data abs() throws VisADException, RemoteException {
    return unary(ABS, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data acos() throws VisADException, RemoteException {
    return unary(ACOS, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data acosDegrees() throws VisADException, RemoteException {
    return unary(ACOS_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data asin() throws VisADException, RemoteException {
    return unary(ASIN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data asinDegrees() throws VisADException, RemoteException {
    return unary(ASIN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data atan() throws VisADException, RemoteException {
    return unary(ATAN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data atanDegrees() throws VisADException, RemoteException {
    return unary(ATAN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data ceil() throws VisADException, RemoteException {
    return unary(CEIL, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data cos() throws VisADException, RemoteException {
    return unary(COS, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data cosDegrees() throws VisADException, RemoteException {
    return unary(COS_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data exp() throws VisADException, RemoteException {
    return unary(EXP, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data floor() throws VisADException, RemoteException {
    return unary(FLOOR, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data log() throws VisADException, RemoteException {
    return unary(LOG, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data rint() throws VisADException, RemoteException {
    return unary(RINT, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data round() throws VisADException, RemoteException {
    return unary(ROUND, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data sin() throws VisADException, RemoteException {
    return unary(SIN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data sinDegrees() throws VisADException, RemoteException {
    return unary(SIN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data sqrt() throws VisADException, RemoteException {
    return unary(SQRT, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data tan() throws VisADException, RemoteException {
    return unary(TAN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data tanDegrees() throws VisADException, RemoteException {
    return unary(TAN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data negate() throws VisADException, RemoteException {
    return unary(NEGATE, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  // a list of unary operations supporting non-default modes for
  // sampling and error estimation
  public Data abs(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ABS, sampling_mode, error_mode);
  }

  public Data acos(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ACOS, sampling_mode, error_mode);
  }

  public Data acosDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ACOS_DEGREES, sampling_mode, error_mode);
  }

  public Data asin(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ASIN, sampling_mode, error_mode);
  }

  public Data asinDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ASIN_DEGREES, sampling_mode, error_mode);
  }

  public Data atan(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ATAN, sampling_mode, error_mode);
  }

  public Data atanDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ATAN_DEGREES, sampling_mode, error_mode);
  }

  public Data ceil(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(CEIL, sampling_mode, error_mode);
  }

  public Data cos(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(COS, sampling_mode, error_mode);
  }

  public Data cosDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(COS_DEGREES, sampling_mode, error_mode);
  }

  public Data exp(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(EXP, sampling_mode, error_mode);
  }

  public Data floor(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(FLOOR, sampling_mode, error_mode);
  }

  public Data log(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(LOG, sampling_mode, error_mode);
  }

  public Data rint(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(RINT, sampling_mode, error_mode);
  }

  public Data round(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ROUND, sampling_mode, error_mode);
  }

  public Data sin(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(SIN, sampling_mode, error_mode);
  }

  public Data sinDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(SIN_DEGREES, sampling_mode, error_mode);
  }

  public Data sqrt(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(SQRT, sampling_mode, error_mode);
  }

  public Data tan(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(TAN, sampling_mode, error_mode);
  }

  public Data tanDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(TAN_DEGREES, sampling_mode, error_mode);
  }

  public Data negate(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(NEGATE, sampling_mode, error_mode);
  }
/* END WLH 8 July 2000 */

  public double[][] computeRanges(RealType[] reals)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.computeRanges " +
                                     "AdaptedData is null");
    }
    return AdaptedData.computeRanges(reals);
  }

  public DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.computeRanges " +
                                     "AdaptedData is null");
    }
    return AdaptedData.computeRanges(type, n);
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.computeRanges " +
                                     "AdaptedData is null");
    }
    return AdaptedData.computeRanges(type, shadow);
  }

  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.adjustSamplingError " +
                                     "AdaptedData is null");
    }
    return AdaptedData.adjustSamplingError(error, error_mode);
  }

  public String longString() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.longString " +
                                     "AdaptedData is null");
    }
    return AdaptedData.longString();
  }

  public String longString(String pre)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.longString " +
                                     "AdaptedData is null");
    }
    return AdaptedData.longString(pre);
  }

  /** a VisAD adaptation of clone that works for local or remote Data;
      DataImpl.dataClone returns clone; RemoteDataImpl.dataClone
      returns clone inherited from UnicastRemoteObject */
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

