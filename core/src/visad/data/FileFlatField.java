// FileFlatField.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data;

import java.io.IOException;

import java.rmi.RemoteException;

import visad.FlatField;
import visad.*;

public class FileFlatField extends FlatField {
  // note FileFlatField extends FlatField but may not inherit
  // any of its methods - it must re-implement all of them
  // through the adapted FlatField

  // number of FlatFields in cache

       private static final int MAX_FILE_FLAT_FIELDS = 10;

  // array of cached FlatFields

       private static transient FlatField[] adaptedFlatFields =
       new FlatField[MAX_FILE_FLAT_FIELDS];

  // true if cache entry differs from file contents

       private static transient boolean adaptedFlatFieldDirty[] =
       new boolean[MAX_FILE_FLAT_FIELDS];

  // the FileFlatField that owns this cache entry

       private static transient FileFlatField[] adaptedFlatFieldOwner =
       new FileFlatField[MAX_FILE_FLAT_FIELDS];

  // adaptedFlatFieldSizes and adaptedFlatFieldTimes
  // may be useful for cache allocation algorithms
  // approximate sizes of FlatFields in cache

       private static transient long[] adaptedFlatFieldSizes =
       new long[MAX_FILE_FLAT_FIELDS];

  // times of most recent accesses to FlatFields in cache

       private static transient long[] adaptedFlatFieldTimes =
       new long[MAX_FILE_FLAT_FIELDS];

  // index of cache entry owned by this FileFlatField;
  // but only if
  // this == adaptedFlatFieldOwner[adaptedFlatFieldIndex]

       private transient int adaptedFlatFieldIndex;


  // this is the FileAccessor for reading and writing values from
  // and to the adapted file

       transient FileAccessor fileAccessor;

  // this implements a strategy for cache replacement;
  // this separates the cahce strategy algorithm from the logic
  // of FileFlatField

       private transient CacheStrategy cacheStrategy;

  static
  {
    // initialize cache of FlatFields
    if (adaptedFlatFieldOwner != null && adaptedFlatFields != null &&
        adaptedFlatFieldSizes != null && adaptedFlatFieldTimes != null &&
        adaptedFlatFieldDirty != null) {
      for (int i=0; i<MAX_FILE_FLAT_FIELDS; i++) {
        // mark Owners for all cache entries to indicate not
        // belonging to any FileFlatField
        adaptedFlatFieldOwner[i] = null;
        adaptedFlatFields[i] = null;
        adaptedFlatFieldSizes[i] = 0;
        adaptedFlatFieldTimes[i] = System.currentTimeMillis();
        adaptedFlatFieldDirty[i] = false;
      }
    }
  }

  // all methods lock on adaptedFlatFields cache
  // to ensure thread safe access

  public FileFlatField( FileAccessor accessor, CacheStrategy strategy )
    throws VisADException
  {
    super( accessor.getFunctionType(),
           getNullDomainSet(accessor.getFunctionType().getDomain()) );

    fileAccessor = accessor;
    cacheStrategy = strategy;

    // '0' is in legal range of adaptedFlatFieldIndex,
    // but not owned by this
    adaptedFlatFieldIndex = 0;
  }

  private static Set getNullDomainSet(RealTupleType type)
          throws VisADException {
    int n = type.getDimension();
    double[] values = new double[n];
    for (int i=0; i<n; i++) values[i] = 0.0;
    RealTuple tuple;
    try {
      tuple = new RealTuple(type, values);
      return new SingletonSet(tuple);
    }
    catch (RemoteException e) {
      throw new VisADError("FileFlatField.getNullDomainSet: " + e.toString());
    }
  }

  private FlatField getAdaptedFlatField()
  {
    // if owner array is null,
    //  assume this object got serialized & unserialized
    if (adaptedFlatFieldOwner == null) {
      return null;
    }

    synchronized (adaptedFlatFields) {
      for ( int ii = 0; ii < MAX_FILE_FLAT_FIELDS; ii++ )
      {
        if (this == adaptedFlatFieldOwner[ii]) {

          // mark time of most recent access

          adaptedFlatFieldTimes[ii] = System.currentTimeMillis();

          return adaptedFlatFields[ii];
        }
      }

      // this FileFlatField does not own a cache entry, so invoke
      // CahceStrategy.allocate to allocate one, possibly by taking
      // one, possibly by taking one from another FileFlatField;
      // this will be an area for lots of thought and experimentation;

      adaptedFlatFieldIndex =
        cacheStrategy.allocate(adaptedFlatFields, adaptedFlatFieldDirty,
                               adaptedFlatFieldSizes, adaptedFlatFieldTimes);

      // flush cache entry, if dirty

      if (adaptedFlatFieldDirty[adaptedFlatFieldIndex])
      {
        try
        {
          adaptedFlatFieldOwner[adaptedFlatFieldIndex].flushCache();
        }
        catch ( VisADException e )
        {
          System.out.println( e.getMessage() );
        }
      }

      // create a new entry in adaptedFlatFields at adaptedFlatFieldIndex
      // and read data values from fileAccessor at fileLocation
      try
      {
        adaptedFlatFields[adaptedFlatFieldIndex] = fileAccessor.getFlatField();
      }
      catch ( VisADException e1 )
      {
        System.out.println( e1.getMessage() );
      }
      catch ( RemoteException e2 )
      {
        System.out.println( e2.getMessage() );
      }

      // mark cache entry as belonging to this FileFlatField

      adaptedFlatFieldOwner[adaptedFlatFieldIndex] = this;

      // get size of adapted FlatField
      // (by calling a method that currently does not exist)

      /*adaptedFlatFields[adaptedFlatFieldIndex].getSize(); */

      adaptedFlatFieldTimes[adaptedFlatFieldIndex] =
        System.currentTimeMillis();

      return adaptedFlatFields[adaptedFlatFieldIndex];
    }
  }

  private void flushCache()
      throws VisADException
  {
    if (adaptedFlatFields == null) {
      throw new VisADException("Cannot access serialized FileFlatField");
    }

    // make sure this owns cache entry
    if (this == adaptedFlatFieldOwner[adaptedFlatFieldIndex]) {
      // unpackValues is currently private, would need default protection
      // for access from FileFlatField
   /* fileAccessor.writeFlatField(
        adaptedFlatFields[adaptedFlatFieldIndex].unpackValues(),
        templateFlatField, fileLocation); */
    }
  }

  // must implement all the methods of Data, Function and Field
  //
  // most are simple adapters, like this:
  //

  public Data getSample(int index)
         throws VisADException, RemoteException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.getSample(index);
  }

  public int getLength()
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return 0;
    }

    return fld.getLength();
  }

  public Unit[] getDomainUnits()
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return null;
    }

    return fld.getDomainUnits();
  }

  public CoordinateSystem getDomainCoordinateSystem()
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return null;
    }

    return fld.getDomainCoordinateSystem();
  }

  public CoordinateSystem[] getRangeCoordinateSystem()
         throws TypeException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return null;
    }

    return fld.getRangeCoordinateSystem();
  }

  public CoordinateSystem[] getRangeCoordinateSystem( int component )
         throws TypeException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return null;
    }

    return fld.getRangeCoordinateSystem( component );
  }

  public Unit[][] getRangeUnits()
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return null;
    }

    return fld.getRangeUnits();
  }

  public Unit[] getDefaultRangeUnits()
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return null;
    }

    return fld.getDefaultRangeUnits();
  }

  public double[][] getValues()
         throws VisADException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.getValues();
  }

  public double[][] getValues(boolean copy)
         throws VisADException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.getValues(copy);
  }

  public double[] getValues(int index)
         throws VisADException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.getValues(index);
  }

  public float[][] getFloats(boolean copy)
         throws VisADException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.getFloats(copy);
  }

  public Set getDomainSet()
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return null;
    }

    return fld.getDomainSet();
  }

  // setSample is typical of methods that involve changing the
  // contents of this Field
  public void setSample(int index, Data range)
         throws VisADException, RemoteException {
    if (adaptedFlatFields == null) {
      throw new VisADException("Cannot access serialized FileFlatField");
    }

    synchronized (adaptedFlatFields) {
      FlatField fld = getAdaptedFlatField();
      if (fld == null) {
        throw new VisADException("Cannot get cached FlatField");
      }

      adaptedFlatFieldDirty[adaptedFlatFieldIndex] = true;
      fld.setSample(index, range);
    }
  }

  public void setSample( RealTuple domain, Data range )
         throws VisADException, RemoteException
  {
    if (adaptedFlatFields == null) {
      throw new VisADException("Cannot access serialized FileFlatField");
    }

    synchronized (adaptedFlatFields)
    {
      FlatField fld = getAdaptedFlatField();
      if (fld == null) {
        throw new VisADException("Cannot get cached FlatField");
      }

      adaptedFlatFieldDirty[adaptedFlatFieldIndex] = true;
      fld.setSample( domain, range );
    }
  }

  public void setSample( int index, Data range, boolean copy )
         throws VisADException, RemoteException
  {
    if (adaptedFlatFields == null) {
      throw new VisADException("Cannot access serialized FileFlatField");
    }

    synchronized (adaptedFlatFields)
    {
      FlatField fld = getAdaptedFlatField();
      if (fld == null) {
        throw new VisADException("Cannot get cached FlatField");
      }

      adaptedFlatFieldDirty[adaptedFlatFieldIndex] = true;
      fld.setSample( index, range, copy );
    }
  }

  public boolean isMissing()
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return true;
    }

    return fld.isMissing();
  }

  public Data binary( Data data, int op, int sampling_mode, int error_mode )
         throws VisADException, RemoteException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.binary( data, op, sampling_mode, error_mode);
  }

  public Data binary( Data data, int op, MathType new_type, int sampling_mode, int error_mode )
         throws VisADException, RemoteException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.binary( data, op, new_type, sampling_mode, error_mode);
  }

  public Data unary( int op, int sampling_mode, int error_mode )
         throws VisADException, RemoteException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.unary( op, sampling_mode, error_mode );
  }

  public Data unary(int op, MathType new_type, int sampling_mode, int error_mode)
         throws VisADException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }
    return fld.unary(op, new_type, sampling_mode, error_mode);
  }

  /** unpack an array of doubles from field sample values according to the
      RangeSet-s; returns a copy */
  public double[][] unpackValues() throws VisADException {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.unpackValues();
  }

  /** unpack an array of floats from field sample values according to the
      RangeSet-s; returns a copy */
  public float[][] unpackFloats() throws VisADException {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.unpackFloats();
  }

  public Field extract( int component )
         throws VisADException, RemoteException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.extract( component );
  }

  public Field domainFactor( RealType factor )
         throws VisADException, RemoteException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.domainFactor( factor );
  }

  public Field resample( Set set, int sampling_mode, int error_mode )
         throws VisADException, RemoteException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.resample( set, sampling_mode, error_mode );
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.computeRanges( type, shadow );
  }

  public Data adjustSamplingError( Data error, int error_mode )
         throws VisADException, RemoteException
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      throw new VisADException("Cannot get cached FlatField");
    }

    return fld.adjustSamplingError( error, error_mode );
  }

  public boolean isFlatField()
  {
     return true;
  }

  /**
   * Clones this instance.  This implementation violates the general <code>
   * clone()</code> contract in that the returned object will compare unequal to
   * this instance.  As such, this method should probably not be invoked.
   *
   * @return                            A clone of this instance.
   */
  public Object clone()
  {
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

  public String toString()
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return "Cannot get cached FlatField";
    }

    return fld.toString();
  }

  public String longString(String pre)
  {
    FlatField fld = getAdaptedFlatField();
    if (fld == null) {
      return pre + "Cannot get cached FlatField";
    }

    try {
      return fld.longString(pre);
    } catch (VisADException e) {
      return pre + e.getMessage();
    }
  }

  private void readObject(java.io.ObjectInputStream oos)
    throws ClassNotFoundException, IOException
  {
    throw new java.io.NotSerializableException("FileFlatField is not serializable");
  }

  private void writeObject(java.io.ObjectOutputStream oos)
    throws IOException
  {
    throw new java.io.NotSerializableException("FileFlatField is not serializable");
  }
}
