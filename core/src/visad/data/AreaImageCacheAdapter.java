// FileFlatField.java
//

/*
 * VisAD system for interactive analysis and visualization of numerical data.
 * Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom Rink, Dave
 * Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and Tommy Jasmin.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 */

package visad.data;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import visad.CoordinateSystem;
import visad.Data;
import visad.DataShadow;
import visad.ErrorEstimate;
import visad.Field;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.ShadowType;
import visad.SingletonSet;
import visad.TypeException;
import visad.Unit;
import visad.VisADError;
import visad.VisADException;
import visad.meteorology.SatelliteImage;

/**
 * Adapts a <code>FlatField</code> backed by a <code>AreaImageAccessor</code> to work
 * with a <code>FlatFieldCache</code>.
 */
public class AreaImageCacheAdapter extends SatelliteImage implements FlatFieldCache.CacheOwner {
  // note FileFlatField extends FlatField but may not inherit
  // any of its methods - it must re-implement all of them
  // through the adapted FlatField

  private static Logger log = Logger.getLogger(AreaImageCacheAdapter.class.getName());
  
  // this is the FileAccessor for reading and writing values from
  // and to the adapted file

  private final FlatFieldCacheAccessor fileAccessor;
  private final FlatFieldCache cache;
  private final SatelliteImage adapted;

  public AreaImageCacheAdapter(SatelliteImage template, AreaImageAccessor accessor, 
       FlatFieldCache cache) throws VisADException, RemoteException {
    super((FunctionType)template.getType(), 
        getNullDomainSet(((FunctionType)template.getType()).getDomain()), template.getStartTime(), 
        template.getDescription(), template.getSensorName());
    this.adapted = (SatelliteImage) template.clone();
    fileAccessor = accessor;
    this.cache = cache;
  }
  
  public String getId() {
    return ((AreaImageAccessor) fileAccessor).getSource();
  }

  public static Set getNullDomainSet(RealTupleType type) throws VisADException {
    int n = type.getDimension();
    double[] values = new double[n];
    for (int i = 0; i < n; i++)
      values[i] = 0.0;
    RealTuple tuple;
    try {
      tuple = new RealTuple(type, values);
      return new SingletonSet(tuple);
    } catch (RemoteException e) {
      throw new VisADError("FileFlatField.getNullDomainSet: " + e.toString());
    }
  }

  protected SatelliteImage getAdaptedFlatField() {
    try {
      adapted.setSamples(cache.getData(this, fileAccessor), false);
    } catch (Exception e) {
      log.log(Level.SEVERE, "error setting samples", e);
      throw new FlatFieldCacheError("Error retrieving cached FlatField", e);
    }
    return adapted;
  }

  public Data getSample(int index) throws VisADException, RemoteException {
    log.finest("getSample");
    FlatField fld = getAdaptedFlatField();
    return fld.getSample(index);
  }

  public int getLength() {
    log.finest("getLength");
    int length = 0;
    try {
      length = adapted.getLength();
    } catch (Exception e) {
      throw new FlatFieldCacheError("Error accessing accessor template", e);
    }
    return length;
  }

  public Unit[] getDomainUnits() {
    log.finest("getDomainUnits");
    Unit[] units = null;
    try {
      units = adapted.getDomainUnits();
    } catch (Exception e) {
      throw new FlatFieldCacheError("Error accessing accessor template", e);
    }
    return units;
  }

  public CoordinateSystem getDomainCoordinateSystem() {
    log.finest("getDomainCoordinateSystem");
    CoordinateSystem coordSystem = null;
    try {
      adapted.getDomainCoordinateSystem();
    } catch (Exception e) {
      throw new FlatFieldCacheError("Error accessing accessor template", e);
    }
    return coordSystem;
  }

  public CoordinateSystem[] getRangeCoordinateSystem() throws TypeException {
    log.finest("getRangeCoordinateSystem");
    FlatField fld = getAdaptedFlatField();
    return fld.getRangeCoordinateSystem();
  }

  public CoordinateSystem[] getRangeCoordinateSystem(int component) throws TypeException {
    log.finest("getRangeCoordinateSystem");
    FlatField fld = getAdaptedFlatField();
    return fld.getRangeCoordinateSystem(component);
  }

  public Unit[][] getRangeUnits() {
    log.finest("getRangeUnits");
    FlatField fld = getAdaptedFlatField();
    return fld.getRangeUnits();
  }

  public Unit[] getDefaultRangeUnits() {
    log.finest("getDefaultRangeUnits");
    FlatField fld = getAdaptedFlatField();
    return fld.getDefaultRangeUnits();
  }

  public double[][] getValues() throws VisADException {
    log.finest("getValues");
    FlatField fld = getAdaptedFlatField();
    return fld.getValues();
  }

  public double[][] getValues(boolean copy) throws VisADException {
    log.finest("getValues");
    FlatField fld = getAdaptedFlatField();
    return fld.getValues(copy);
  }

  public double[] getValues(int index) throws VisADException {
    log.finest("getValues");
    FlatField fld = getAdaptedFlatField();
    return fld.getValues(index);
  }

  public float[][] getFloats(boolean copy) throws VisADException {
    log.finest("getFloats");
    FlatField fld = getAdaptedFlatField();
    return fld.getFloats(copy);
  }

  public Set getDomainSet() {
    log.finest("getDomainSet");
    Set domainSet = null;
    try {
      domainSet = adapted.getDomainSet();
    } catch (Exception e) {
      throw new FlatFieldCacheError("Error accessing accessor template", e);
    }
    return domainSet;
  }

  // setSample is typical of methods that involve changing the
  // contents of this Field
  public void setSample(int index, Data range) throws VisADException, RemoteException {
    log.finest("setSample");
    synchronized (cache) {
      FlatField fld = getAdaptedFlatField();
      cache.setDirty(this, true);
      fld.setSample(index, range);
    }
  }

  public void setSample(RealTuple domain, Data range) throws VisADException, RemoteException {
    log.finest("setSample");
    synchronized (cache) {
      FlatField fld = getAdaptedFlatField();
      cache.setDirty(this, true);
      fld.setSample(domain, range);
    }
  }

  public void setSample(int index, Data range, boolean copy) throws VisADException, RemoteException {
    log.finest("getSample");
    synchronized (cache) {
      FlatField fld = getAdaptedFlatField();
      cache.setDirty(this, true);
      fld.setSample(index, range, copy);
    }
  }
  
  public void setSamples(double[][] data) throws RemoteException, VisADException {
    log.finest("getSamples");
    synchronized (cache) {
      FlatField fld = getAdaptedFlatField();
      cache.setDirty(this, true);
      fld.setSamples(data, false);
    }
  }
  
  public void setSamples(double[][] range, ErrorEstimate[] errors,
      boolean copy) throws VisADException, RemoteException {
    setSamples(range);
  }

  public boolean isMissing() {
    log.finest("isMissing");
    FlatField fld = getAdaptedFlatField();
    return fld.isMissing();
  }

  public Data binary(Data data, int op, int sampling_mode, int error_mode) throws VisADException,
      RemoteException {
    log.finest("binary");
    FlatField fld = getAdaptedFlatField();
    return fld.binary(data, op, sampling_mode, error_mode);
  }

  public Data binary(Data data, int op, MathType new_type, int sampling_mode, int error_mode)
      throws VisADException, RemoteException {
    log.finest("binary");
    FlatField fld = getAdaptedFlatField();
    return fld.binary(data, op, new_type, sampling_mode, error_mode);
  }

  public Data unary(int op, int sampling_mode, int error_mode) throws VisADException,
      RemoteException {
    log.finest("unary");
    FlatField fld = getAdaptedFlatField();
    return fld.unary(op, sampling_mode, error_mode);
  }

  public Data unary(int op, MathType new_type, int sampling_mode, int error_mode)
      throws VisADException {
    log.finest("unary");
    FlatField fld = getAdaptedFlatField();
    return fld.unary(op, new_type, sampling_mode, error_mode);
  }

  /**
   * unpack an array of doubles from field sample values according to the
   * RangeSet-s; returns a copy
   */
  public double[][] unpackValues() throws VisADException {
    log.finest("unpackValues");
    FlatField fld = getAdaptedFlatField();
    return fld.unpackValues();
  }

  /**
   * unpack an array of floats from field sample values according to the
   * RangeSet-s; returns a copy
   */
  public float[][] unpackFloats() throws VisADException {
    log.finest("unpackFloats");
    FlatField fld = getAdaptedFlatField();
    return fld.unpackFloats();
  }

  public Field extract(int component) throws VisADException, RemoteException {
    log.finest("extract");
    FlatField fld = getAdaptedFlatField();
    return fld.extract(component);
  }

  public Field domainFactor(RealType factor) throws VisADException, RemoteException {
    log.finest("domainFactor");
    FlatField fld = getAdaptedFlatField();
    return fld.domainFactor(factor);
  }

  public Field resample(Set set, int sampling_mode, int error_mode) throws VisADException,
      RemoteException {
    log.finest("resample");
    FlatField fld = getAdaptedFlatField();
    return fld.resample(set, sampling_mode, error_mode);
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow) throws VisADException {
    log.finest("computeRanges");
    FlatField fld = getAdaptedFlatField();
    return fld.computeRanges(type, shadow);
  }

  public Data adjustSamplingError(Data error, int error_mode) throws VisADException,
      RemoteException {
    log.finest("adjustSamplingError");
    FlatField fld = getAdaptedFlatField();
    return fld.adjustSamplingError(error, error_mode);
  }

  public boolean isFlatField() {
    return true;
  }

  /**
   * Clones this instance. This implementation violates the general <code>
   * clone()</code> contract in that the returned object will compare unequal to
   * this instance. As such, this method should probably not be invoked.
   * 
   * @return A clone of this instance.
   */
  public Object clone() {
    /*
     * This implementation should probably just throw a
     * CloneNotSupportedException but can't because FlatField.clone() doesn't.
     */

    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return null;
    }

    return fld.clone();
  }

  public String longString(String pre) {
    log.finest("longString");
    FlatField fld = getAdaptedFlatField();
    try {
      return fld.longString(pre);
    } catch (VisADException e) {
      return pre + e.getMessage();
    }
  }
  
  public String toString() {
    return this.getClass().toString();
  }
}
