package visad.data;

import java.util.logging.Logger;

import visad.VisADException;

/**
 * Memory cache for <code>FlatField</code>s.  Maintains a fixed number of cache
 * arrays in memory.  When a cache member is requested which is not currently 
 * available in the cache it is loaded directly into one of the cache arrays.
 * This prevents the allocation or garbage collection of data arrays when they
 * are created. This allows caching to happen close to the data, rather than the
 * rendering and is intended to allow for large animation loops with as little 
 * an affect as possible on rendering. This can however lead to large latency at
 * startup due to reading data sources and copying them to the cache arrays.
 * <p>
 * Most of this class was modeled after the <code>FileFlatField</code>. This cache,
 * however, is not a static cache, it is a instance cache. The affect of this is 
 * that if you create several large caches you can very quickly run out of memory.
 */
public class FlatFieldCache {

  private static Logger log = Logger.getLogger(FlatFieldCache.class.getName());
  
  static interface CacheOwner {
    public String getId();
  };
  
  /**
   * Simple cache strategy based on the time a <code>FlatField</code> was last accessed.
   */
  static class AccessTimeCacheStrategy implements FlatFieldCacheStrategy {

    public int allocate(Entry[] entries) {

      if (entries == null || entries.length == 0) return -1;
      
      int availableIdx = 0;
      long oldest = entries[0] != null ? entries[0].lastAccessed : 0;

      for (int ii = 0; ii < entries.length; ii++) {

        if (entries[ii] == null) {
          availableIdx = ii;
          return availableIdx;
        } else if (entries[ii].lastAccessed < oldest) {
          oldest = entries[ii].lastAccessed;
          availableIdx = ii;
        }
      }

      return availableIdx;
    }
  }

  /**
   * Cache entry metadata.
   */
  static class Entry {

    CacheOwner owner;
    float[][] data;
    boolean dirty = false;
    long lastAccessed;
    long size;

    Entry(CacheOwner owner, float[][] data) {
      this.data = data;
      this.owner = owner;
      lastAccessed = System.currentTimeMillis();
    }
    
    public String toString() {
      return "<Entry lastAccessed="+lastAccessed+" dirty="+dirty+" owner="+owner.getId()+">";
    }
  }
  
  private final Entry[] cache;

  private final int cacheSize;
  private FlatFieldCacheStrategy strategy;

  /**
   * Create a cache with a fixed size and the default strategy.
   * @param cacheSize
   */
  public FlatFieldCache(int cacheSize) {
    this(cacheSize, new AccessTimeCacheStrategy());
  }
  
  /**
   * Initialize cache.
   * @param cacheSize Number of <code>FlatFields</code> to maintain in cache, &gt;= 1.
   * @param strategy How cache allocation is performed.
   */
  public FlatFieldCache(int cacheSize, FlatFieldCacheStrategy strategy) {
    if (cacheSize < 1) throw new IllegalArgumentException("cache size must be >= 1");
    this.cacheSize = cacheSize;
    this.strategy = strategy;
    cache = new Entry[cacheSize];
  }
  
  protected void updateEntry(Entry entry, float[][] data, CacheOwner owner) {
    for (int ii = 0; ii < data.length; ii++) {
      System.arraycopy(data[ii], 0, entry.data[ii], 0, data[ii].length);
    }
    entry.owner = owner;
    entry.dirty = false;
    entry.lastAccessed = System.currentTimeMillis();
  }
  
  /**
   * Does the work of getting data from the cache.
   * 
   * @param owner
   * @param fileAccessor
   * @return
   */
  protected synchronized float[][] getData(AreaImageCacheAdapter owner, FlatFieldCacheAccessor fileAccessor) {
    
    // if owner array is null,
    // assume this object got serialized & unserialized
    if (cache == null) {
      log.fine("Cache was null, returning");
      return null;
    }
    
    for (int ii = 0; ii < cache.length; ii++) {
      // if the owner is the same as the entries owner
      // we have the right entry
      if (cache[ii] != null && owner == cache[ii].owner) {
        cache[ii].lastAccessed = System.currentTimeMillis();
        return cache[ii].data;
      }
    }

    // this FileFlatField does not own a cache entry, so invoke
    // CahceStrategy.allocate to allocate one, possibly by taking
    // one, possibly by taking one from another FileFlatField;
    // this will be an area for lots of thought and experimentation;
    
    float[][] range = null;
    
    int idx = strategy.allocate(cache);
    
    // cannot allocate
    if (idx == -1) {
      range = fileAccessor.readFlatFieldFloats();
      
    } else {
      
      // entry should only be null once, whence we should create a new one
      if (cache[idx] == null) {
        cache[idx] = new Entry(owner, fileAccessor.readFlatFieldFloats());
      
      } else {
        if (cache[idx].dirty) {
          try {
            flushCache(cache[idx], fileAccessor);
          } catch (VisADException e) {
            throw new FlatFieldCacheError("Could not flush to cache", e);
          }
        }
        
        // update the cached entry with the new values
        try {
          float[][] data = fileAccessor.readFlatFieldFloats();
          if (cache[idx].data == null) {
            cache[idx].data = data;
          } else {
            updateEntry(cache[idx], data, owner);
          }
        } catch (Exception e) {
          throw new FlatFieldCacheError("Could not update cache entry", e);
        }
      }
      range = cache[idx] != null ? cache[idx].data : null;
    }

    return range;
  }

  /**
   * Not currently implemented.
   * @param entry
   * @param fileAccessor
   * @throws UnsupportedOperationException All the time.
   */
  public void flushCache(Entry entry, FlatFieldCacheAccessor fileAccessor) throws VisADException {
    throw new UnsupportedOperationException("FlatFieldCache.flushCache not implemented");
  }

  /**
   * Don't do this.
   * 
   * @param owner The owner of the cache entry to mark as dirty.
   * @param dirty 
   */
  public void setDirty(AreaImageCacheAdapter owner, boolean dirty) {
    for (int ii = 0; ii < cache.length; ii++) {
      if (cache[ii].owner == owner) {
        cache[ii].dirty = dirty;
      }
    }
  }
  
  public String toString() {
    StringBuffer buf = new StringBuffer("<FlatFieldCache size=" + this.cacheSize + "\n");
    for (Entry entry : cache) {
      buf.append("\t" + (entry == null ? null : entry.toString()) + "\n");
    }
    buf.append(">");
    return buf.toString();
  }

  public int getSize() {
    return cacheSize;
  }
}
