
//
// RemoteDataImpl.java
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
import java.rmi.server.UnicastRemoteObject;

/**
   RemoteDataImpl is the VisAD remote adapter for DataImpl.<P>
*/
public abstract class RemoteDataImpl extends UnicastRemoteObject
       implements RemoteData {

  /** 'this' is the Remote adaptor for AdaptedData (which is local);
      AdaptedData is transient because UnicastRemoteObject is
      Serializable, but a copy of 'this' on another JVM will not
      be local to AdaptedData and cannot adapt it;
      the methods of RemoteDataImpl text for null AdaptedData */
  final transient DataImpl AdaptedData;

  /** Tick increments each time data changes;
      used in place of propogating notifyReferences
      to Remote parents */
  private long Tick;

  public RemoteDataImpl(DataImpl data) throws RemoteException {
    AdaptedData = data;
    Tick = Long.MIN_VALUE + 1;
  }

  /** Tick is incremented in a RemoteData object, rather than
      propogating Data changes to RemoteDataReference-s */
  public long incTick() {
    Tick += 1;
    if (Tick == Long.MAX_VALUE) Tick = Long.MIN_VALUE + 1;
    return Tick;
  }

  /** RemoteDataReference-s can (but don't currently) poll getTick() */
  public long getTick() {
    return Tick;
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

  /** add a DataReference to this RemoteDataImpl;
      must be RemoteDataReference;
      called by DataReference.setData */
  public void addReference(DataReference r) throws VisADException {
    if (r instanceof DataReferenceImpl) {
      throw new RemoteVisADException("RemoteDataImpl.addReference: must use " +
                                     "DataImpl for DataReferenceImpl");
    }
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.addReference " +
                                     "AdaptedData is null");
    }
    // RemoteDataReference recorded by a ReferenceDataPair
    // (RemoteDataReference, RemoteDataImpl)
    AdaptedData.adaptedAddReference(
      new ReferenceDataPair((RemoteDataReference) r, (RemoteData) this));
  }

  /** remove a DataReference to this RemoteDataImpl;
      must be RemoteDataReferenceImpl;
      called by DataReference.setData */
  public void removeReference(DataReference r) throws VisADException {
    if (r instanceof DataReferenceImpl) {
      throw new RemoteVisADException("RemoteDataImpl.addReference: must use " +
                                     "DataImpl for DataReferenceImpl");
    }
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.removeReference " +
                                     "AdaptedData is null");
    }
    // RemoteDataReference recorded by a ReferenceDataPair
    // (RemoteDataReference, RemoteDataImpl)
    AdaptedData.adaptedRemoveReference(
      new ReferenceDataPair((RemoteDataReference) r, (RemoteData) this));
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
 
  /** a list of binary operations using default modes for
      sampling and error estimation */
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

  /** a list of binary operations supporting non-default modes for
      sampling and error estimation */
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

  /** unary operations adapted to AdaptedData */
  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteDataImpl.unary " +
                                     "AdaptedData is null");
    }
    return AdaptedData.unary(op, sampling_mode, error_mode);
  }
 
  /** a list of unary operations using default modes for
      sampling and error estimation */
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
 
  /** a list of unary operations supporting non-default modes for
      sampling and error estimation */
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

