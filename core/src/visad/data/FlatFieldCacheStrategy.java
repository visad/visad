package visad.data;

/**
 * Strategy used to allocate space in the <code>FlatFieldCache</code> for
 * a new <code>FlatFieldCache.Entry</code>.
 */
interface FlatFieldCacheStrategy {
  
  /**
   * Allocate space in the cache containing <code>entries</code>.  It is up to the caller
   * to ensure the entry at the provided index is saved to a persistent state if necessary. 
   * @param entries The existing entries in the cache.
   * @return An index into the cache (<code>FlatFieldCache.Entry</code> array) 
   *  available for use. 
   */
  public int allocate(FlatFieldCache.Entry[] entries);
}
