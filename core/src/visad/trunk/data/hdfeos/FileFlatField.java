package visad.data.hdfeos;


import java.rmi.RemoteException;

import visad.*;

public class FileFlatField extends FlatField {
  // note FileFlatField extends FlatField but may not inherit
  // any of its methods - it must re-implement all of them
  // through the adapted FlatField
 
  // number of FlatFields in cache

       private static final int MAX_FILE_FLAT_FIELDS = 7;

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

       hdfeosAccessor fileAccessor;

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
 
  public FileFlatField( hdfeosAccessor accessor, CacheStrategy strategy )
    throws VisADException 
  {

    super( accessor.getFunctionType(), 
           (accessor.getFunctionType()).getDomain().getDefaultSet() );

    fileAccessor = accessor;
    cacheStrategy = strategy;
 
    synchronized (adaptedFlatFields) {
      // '0' is in legal range of adaptedFlatFieldIndex,
      // but not owned by this
      adaptedFlatFieldIndex = 0;
    }
  }
 
  private FlatField getadaptedFlatField()
    throws VisADException, RemoteException
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

        if (adaptedFlatFieldDirty[adaptedFlatFieldIndex]) {
          adaptedFlatFieldOwner[adaptedFlatFieldIndex].flushCache();
        }
 
        // create a new entry in adaptedFlatFields at adaptedFlatFieldIndex
        // and read data values from fileAccessor at fileLocation

          adaptedFlatFields[adaptedFlatFieldIndex] = fileAccessor.getFlatField();
 
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
         throws VisADException, RemoteException {
    synchronized (adaptedFlatFields) {
      return getadaptedFlatField().getSample(index);
    }
  }

  public Set getDomainSet() {

    synchronized ( adaptedFlatFields ) {

      try {
        return getadaptedFlatField().getDomainSet();
      }
      catch( VisADException e1 ) {
       
        return null;
      }
      catch( RemoteException e2 ) {

        return null;
      }
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
}
