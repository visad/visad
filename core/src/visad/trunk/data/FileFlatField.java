// FileFlatField.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

       private static FlatField[] adaptedFlatFields =
       new FlatField[MAX_FILE_FLAT_FIELDS];

  // true if cache entry differs from file contents

       private static boolean adaptedFlatFieldDirty[] =
       new boolean[MAX_FILE_FLAT_FIELDS];

  // the FileFlatField that owns this cache entry

       private static FileFlatField[] adaptedFlatFieldOwner =
       new FileFlatField[MAX_FILE_FLAT_FIELDS];
 
  // adaptedFlatFieldSizes and adaptedFlatFieldTimes
  // may be useful for cache allocation algorithms
  // approximate sizes of FlatFields in cache

       private static long[] adaptedFlatFieldSizes =
       new long[MAX_FILE_FLAT_FIELDS];

  // times of most recent accesses to FlatFields in cache

       private static long[] adaptedFlatFieldTimes =
       new long[MAX_FILE_FLAT_FIELDS];
 
  // index of cache entry owned by this FileFlatField;
  // but only if
  // this == adaptedFlatFieldOwner[adaptedFlatFieldIndex]
 
       private int adaptedFlatFieldIndex;
 

  // this is the FileAccessor for reading and writing values from
  // and to the adapted file

       FileAccessor fileAccessor;

  // this implements a strategy for cache replacement;
  // this separates the cahce strategy algorithm from the logic
  // of FileFlatField

       private CacheStrategy cacheStrategy;
 
  static {
    // initialize cache of FlatFields
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
 
  // all methods lock on adaptedFlatFields cache
  // to ensure thread safe access
 
  public FileFlatField( FileAccessor accessor, CacheStrategy strategy )
    throws VisADException 
  {
    super( accessor.getFunctionType(), 
           getNullDomainSet(accessor.getFunctionType().getDomain()) );

    fileAccessor = accessor;
    cacheStrategy = strategy;
 
    synchronized (adaptedFlatFields) {
      // '0' is in legal range of adaptedFlatFieldIndex,
      // but not owned by this
      adaptedFlatFieldIndex = 0;
    }
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

  private FlatField getadaptedFlatField()
  {
    // does not lock adaptedFlatFields since it is always
    // invoked from methods that have locked adaptedFlatFields

    for ( int ii = 0; ii < MAX_FILE_FLAT_FIELDS; ii++ ) 
    {
      if (this == adaptedFlatFieldOwner[ii]) {

        // mark time of most recent access

           adaptedFlatFieldTimes[adaptedFlatFieldIndex] =
           System.currentTimeMillis();
 
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
 
  private void flushCache()
      throws VisADException
  {
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
  // bodies of all must be inside
  // 'synchronized (adaptedFlatFields) { ... }'
  //
  // most are simple adapters, like this:
  //

  public Data getSample(int index)
         throws VisADException, RemoteException 
  {
    synchronized (adaptedFlatFields) 
    {
      return getadaptedFlatField().getSample(index);
    }
  }

  public int getLength() 
  {
     synchronized (adaptedFlatFields) 
     {
       return getadaptedFlatField().getLength();
     }
  }

  public Unit[] getDomainUnits()
  {
     synchronized (adaptedFlatFields)
     {
       return getadaptedFlatField().getDomainUnits();
     }
  }

  public CoordinateSystem getDomainCoordinateSystem()
  {
     synchronized (adaptedFlatFields)
     {
       return getadaptedFlatField().getDomainCoordinateSystem();
     }
  }

  public CoordinateSystem[] getRangeCoordinateSystem()
         throws VisADException
  {
     synchronized (adaptedFlatFields)
     {
       return getadaptedFlatField().getRangeCoordinateSystem();
     }
  }

  public CoordinateSystem[] getRangeCoordinateSystem( int component )
         throws VisADException
  { 
     synchronized (adaptedFlatFields)
     {
       return getadaptedFlatField().getRangeCoordinateSystem( component );
     }
  }

  public Unit[][] getRangeUnits()
  {
     synchronized (adaptedFlatFields)
     {
       return getadaptedFlatField().getRangeUnits();
     }
  }

  public Unit[] getDefaultRangeUnits()
  {
     synchronized (adaptedFlatFields)
     {
       return getadaptedFlatField().getDefaultRangeUnits();
     }
  }

  public double[][] getValues()
         throws VisADException
  {
    synchronized (adaptedFlatFields) 
    {
      return getadaptedFlatField().getValues();
    }
  } 

  public Set getDomainSet() 
  {
    synchronized ( adaptedFlatFields ) 
    {
      return getadaptedFlatField().getDomainSet();
    }
  }
 
  // setSample is typical of methods that involve changing the
  // contents of this Field
  public void setSample(int index, Data range)
         throws VisADException, RemoteException {
    synchronized (adaptedFlatFields) {
      getadaptedFlatField().setSample(index, range);
      adaptedFlatFieldDirty[adaptedFlatFieldIndex] = true;
    }
  }

  public void setSample( RealTuple domain, Data range )
         throws VisADException, RemoteException 
  {
    synchronized (adaptedFlatFields) 
    {
      getadaptedFlatField().setSample( domain, range );
      adaptedFlatFieldDirty[adaptedFlatFieldIndex] = true;
    }
  }

  public void setSample( int index, Data range, boolean copy )
         throws VisADException, RemoteException 
  {
    synchronized (adaptedFlatFields)
    {
      getadaptedFlatField().setSample( index, range, copy );
      adaptedFlatFieldDirty[adaptedFlatFieldIndex] = true;
    }
  }

  public boolean isMissing()
  {
    synchronized (adaptedFlatFields)
    {
      return getadaptedFlatField().isMissing();
    }
  }

  public Data binary( Data data, int op, int sampling_mode, int error_mode )
         throws VisADException, RemoteException
  {
    synchronized (adaptedFlatFields)
    {
      return getadaptedFlatField().binary( data, op, sampling_mode, error_mode);
    }
  }

  public Data unary( int op, int sampling_mode, int error_mode )
         throws VisADException, RemoteException
  {
    synchronized (adaptedFlatFields)
    {
      return getadaptedFlatField().unary( op, sampling_mode, error_mode );
    }
  }

  public Field extract( int component )
         throws VisADException, RemoteException
  {
    synchronized (adaptedFlatFields)
    {
      return getadaptedFlatField().extract( component );
    }
  }

  public Field resample( Set set, int sampling_mode, int error_mode )
         throws VisADException, RemoteException 
  {
    synchronized (adaptedFlatFields)
    {
      return getadaptedFlatField().resample( set, sampling_mode, error_mode );
    }
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException
  {
    synchronized (adaptedFlatFields)
    {
      return getadaptedFlatField().computeRanges( type, shadow );
    }
  }

  public Data adjustSamplingError( Data error, int error_mode )
         throws VisADException, RemoteException 
  {
     synchronized (adaptedFlatFields)
     {
       return getadaptedFlatField().adjustSamplingError( error, error_mode );
     }
  }

  public boolean isFlatField() 
  {
     return true;
  }

  public Object clone() 
  {
    synchronized (adaptedFlatFields )
    {
      return getadaptedFlatField().clone();
    }
  }
}
