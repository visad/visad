//
// RemoteClientDataImpl.java
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

package visad.cluster;

import visad.*;

import java.util.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

/**
   RemoteClientDataImpl is the VisAD remote adapter for DataImpl.<P>
*/
public abstract class RemoteClientDataImpl extends RemoteDataImpl
       implements RemoteClientData {

  /** 'this' is the Remote adaptor for AdaptedData (which is local);
      AdaptedData is transient because UnicastRemoteObject is
      Serializable, but a copy of 'this' on another JVM will not
      be local to AdaptedData and cannot adapt it;
      the methods of RemoteClientDataImpl test for null AdaptedData */
  final transient DataImpl AdaptedData;

  public RemoteClientDataImpl(DataImpl data) throws RemoteException {
    super(data);
    AdaptedData = data;
  }

  /** methods adapted from Data;
      do not adapt equals, toString, hashCode or clone */

  /** DataImpl.local() returns 'this'
      RemoteClientDataImpl.local() returns 'AdaptedData' */
  public DataImpl local() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.local " +
                                     "AdaptedData is null");
    }
    return AdaptedData;
  }

  public MathType getType() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.getType " +
                                     "AdaptedData is null");
    }
    return AdaptedData.getType();
  }

  public boolean isMissing() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.isMissing " +
                                     "AdaptedData is null");
    }
    return AdaptedData.isMissing();
  }

  /** binary operations adapted to AdaptedData */
  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.binary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.binary(data, op, sampling_mode, error_mode);
  }

  /*- TDR June 1998  */
  public Data binary(Data data, int op, MathType new_type,
                     int sampling_mode, int error_mode )
              throws VisADException, RemoteException {
    if (AdaptedData == null ) {
      throw new RemoteVisADException("RemoteClientDataImpl.binary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.binary(data, op, new_type, sampling_mode, error_mode);
  }

  /** a list of binary operations using default modes for
      sampling and error estimation */
  public Data add(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.add " +
                                     "AdaptedData is null");
    }
    return AdaptedData.add(data);
  }

  public Data subtract(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.subtract " +
                                     "AdaptedData is null");
    }
    return AdaptedData.subtract(data);
  }

  public Data multiply(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.multiply " +
                                     "AdaptedData is null");
    }
    return AdaptedData.multiply(data);
  }

  public Data divide(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.divide " +
                                     "AdaptedData is null");
    }
    return AdaptedData.divide(data);
  }

  public Data pow(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.pow " +
                                     "AdaptedData is null");
    }
    return AdaptedData.pow(data);
  }

  public Data max(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.max " +
                                     "AdaptedData is null");
    }
    return AdaptedData.max(data);
  }

  public Data min(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.min " +
                                     "AdaptedData is null");
    }
    return AdaptedData.min(data);
  }

  public Data atan2(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.atan2 " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan2(data);
  }

  public Data atan2Degrees(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.atan2Degrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan2Degrees(data);
  }

  public Data remainder(Data data) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.remainder " +
                                     "AdaptedData is null");
    }
    return AdaptedData.remainder(data);
  }

  /** a list of binary operations supporting non-default modes for
      sampling and error estimation */
  public Data add(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.add " +
                                     "AdaptedData is null");
    }
    return AdaptedData.add(data, sampling_mode, error_mode);
  }

  public Data subtract(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.subtract " +
                                     "AdaptedData is null");
    }
    return AdaptedData.subtract(data, sampling_mode, error_mode);
  }

  public Data multiply(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.multiply " +
                                     "AdaptedData is null");
    }
    return AdaptedData.multiply(data, sampling_mode, error_mode);
  }

  public Data divide(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.divide " +
                                     "AdaptedData is null");
    }
    return AdaptedData.divide(data, sampling_mode, error_mode);
  }

  public Data pow(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.pow " +
                                     "AdaptedData is null");
    }
    return AdaptedData.pow(data, sampling_mode, error_mode);
  }

  public Data max(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.max " +
                                     "AdaptedData is null");
    }
    return AdaptedData.max(data, sampling_mode, error_mode);
  }

  public Data min(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.min " +
                                     "AdaptedData is null");
    }
    return AdaptedData.min(data, sampling_mode, error_mode);
  }

  public Data atan2(Data data, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.atan2 " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan2(data, sampling_mode, error_mode);
  }

  public Data atan2Degrees(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.atan2Degrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan2Degrees(data, sampling_mode, error_mode);
  }

  public Data remainder(Data data, int sampling_mode,
         int error_mode) throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.remainder " +
                                     "AdaptedData is null");
    }
    return AdaptedData.remainder(data, sampling_mode, error_mode);
  }

  /** unary operations adapted to AdaptedData */
  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.unary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.unary(op, sampling_mode, error_mode);
  }

  /*- TDR July 1998  */
  public Data unary(int op, MathType new_type,
                    int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.unary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.unary(op, new_type, sampling_mode, error_mode);
  }

  /* WLH 5 Sept 98 */
  public Data changeMathType(MathType new_type)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.changeMathType " +
                                     "AdaptedData is null");
    }
    return AdaptedData.changeMathType(new_type);
  }

  /** a list of unary operations using default modes for
      sampling and error estimation */
  public Data abs() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.abs " +
                                     "AdaptedData is null");
    }
    return AdaptedData.abs();
  }

  public Data acos() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.acos " +
                                     "AdaptedData is null");
    }
    return AdaptedData.acos();
  }

  public Data acosDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.acosDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.acosDegrees();
  }

  public Data asin() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.asin " +
                                     "AdaptedData is null");
    }
    return AdaptedData.asin();
  }

  public Data asinDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.asinDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.asinDegrees();
  }

  public Data atan() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.atan " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan();
  }

  public Data atanDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.atanDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atanDegrees();
  }

  public Data ceil() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.ceil " +
                                     "AdaptedData is null");
    }
    return AdaptedData.ceil();
  }

  public Data cos() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.cos " +
                                     "AdaptedData is null");
    }
    return AdaptedData.cos();
  }

  public Data cosDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.cosDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.cosDegrees();
  }

  public Data exp() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.exp " +
                                     "AdaptedData is null");
    }
    return AdaptedData.exp();
  }

  public Data floor() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.floor " +
                                     "AdaptedData is null");
    }
    return AdaptedData.floor();
  }

  public Data log() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.log " +
                                     "AdaptedData is null");
    }
    return AdaptedData.log();
  }

  public Data rint() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.rint " +
                                     "AdaptedData is null");
    }
    return AdaptedData.rint();
  }

  public Data round() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.round " +
                                     "AdaptedData is null");
    }
    return AdaptedData.round();
  }

  public Data sin() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.sin " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sin();
  }

  public Data sinDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.sinDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sinDegrees();
  }

  public Data sqrt() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.sqrt " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sqrt();
  }

  public Data tan() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.tan " +
                                     "AdaptedData is null");
    }
    return AdaptedData.tan();
  }

  public Data tanDegrees() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.tanDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.tanDegrees();
  }

  public Data negate() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.negate " +
                                     "AdaptedData is null");
    }
    return AdaptedData.negate();
  }

  /** a list of unary operations supporting non-default modes for
      sampling and error estimation */
  public Data abs(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.abs " +
                                     "AdaptedData is null");
    }
    return AdaptedData.abs(sampling_mode, error_mode);
  }

  public Data acos(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.acos " +
                                     "AdaptedData is null");
    }
    return AdaptedData.acos(sampling_mode, error_mode);
  }

  public Data acosDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.acosDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.acosDegrees(sampling_mode, error_mode);
  }

  public Data asin(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.asin " +
                                     "AdaptedData is null");
    }
    return AdaptedData.asin(sampling_mode, error_mode);
  }

  public Data asinDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.asinDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.asinDegrees(sampling_mode, error_mode);
  }

  public Data atan(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.atan " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atan(sampling_mode, error_mode);
  }

  public Data atanDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.atanDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.atanDegrees(sampling_mode, error_mode);
  }

  public Data ceil(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.ceil " +
                                     "AdaptedData is null");
    }
    return AdaptedData.ceil(sampling_mode, error_mode);
  }

  public Data cos(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.cos " +
                                     "AdaptedData is null");
    }
    return AdaptedData.cos(sampling_mode, error_mode);
  }

  public Data cosDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.cosDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.cosDegrees(sampling_mode, error_mode);
  }

  public Data exp(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.exp " +
                                     "AdaptedData is null");
    }
    return AdaptedData.exp(sampling_mode, error_mode);
  }

  public Data floor(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.floor " +
                                     "AdaptedData is null");
    }
    return AdaptedData.floor(sampling_mode, error_mode);
  }

  public Data log(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.log " +
                                     "AdaptedData is null");
    }
    return AdaptedData.log(sampling_mode, error_mode);
  }

  public Data rint(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.rint " +
                                     "AdaptedData is null");
    }
    return AdaptedData.rint(sampling_mode, error_mode);
  }

  public Data round(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.round " +
                                     "AdaptedData is null");
    }
    return AdaptedData.round(sampling_mode, error_mode);
  }

  public Data sin(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.sin " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sin(sampling_mode, error_mode);
  }

  public Data sinDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.sinDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sinDegrees(sampling_mode, error_mode);
  }

  public Data sqrt(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.sqrt " +
                                     "AdaptedData is null");
    }
    return AdaptedData.sqrt(sampling_mode, error_mode);
  }

  public Data tan(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.tan " +
                                     "AdaptedData is null");
    }
    return AdaptedData.tan(sampling_mode, error_mode);
  }

  public Data tanDegrees(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.tanDegrees " +
                                     "AdaptedData is null");
    }
    return AdaptedData.tanDegrees(sampling_mode, error_mode);
  }

  public Data negate(int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.negate " +
                                     "AdaptedData is null");
    }
    return AdaptedData.negate(sampling_mode, error_mode);
  }

  public double[][] computeRanges(RealType[] reals)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.computeRanges " +
                                     "AdaptedData is null");
    }
    return AdaptedData.computeRanges(reals);
  }

  public DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.computeRanges " +
                                     "AdaptedData is null");
    }
    return AdaptedData.computeRanges(type, n);
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.computeRanges " +
                                     "AdaptedData is null");
    }
    return AdaptedData.computeRanges(type, shadow);
  }

  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.adjustSamplingError " +
                                     "AdaptedData is null");
    }
    return AdaptedData.adjustSamplingError(error, error_mode);
  }

  public String longString() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.longString " +
                                     "AdaptedData is null");
    }
    return AdaptedData.longString();
  }

  public String longString(String pre)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteClientDataImpl.longString " +
                                     "AdaptedData is null");
    }
    return AdaptedData.longString(pre);
  }

  /** a VisAD adaptation of clone that works for local or remote Data;
      DataImpl.dataClone returns clone; RemoteClientDataImpl.dataClone
      returns clone inherited from UnicastRemoteObject */
  public Object dataClone() throws RemoteException {
    try {
      return clone();
    }
    catch (CloneNotSupportedException e) {
      throw new VisADError("RemoteClientDataImpl.dataClone: " +
                           "CloneNotSupportedException occurred");
    }
  }

}

