package visad.data.visad;

import java.util.ArrayList;

public class BinaryObjectCache
{
  private ArrayList cache = null;

  public BinaryObjectCache()
  {
  }

  public int add(Object obj)
  {
    return add(-1, obj);
  }

  public int add(int index, Object obj)
  {
    // don't bother adding null objects
    if (obj == null) {
      return -1;
    }

    // build a new list if necessary
    if (cache == null) {
      cache = new ArrayList();
    }

    final int cacheLen = cache.size();
    if (index < 0 || index == cacheLen) {
      cache.add(obj);
      index = cache.lastIndexOf(obj);
//System.err.println("BOC.add: Added #"+index+": "+obj);
    } else if (index < cacheLen) {
      cache.set(index, obj);
//System.err.println("BOC.add: Reset #"+index+": "+obj);
    } else {
      for (int i = cacheLen; i < index; i++) {
        cache.add(null);
//System.err.println("BOC.add: Padded #"+i+" with null");
      }
      cache.add(obj);
//System.err.println("BOC.add: Added #"+index+": "+obj);int i2 = cache.lastIndexOf(obj);if(index!=i2)System.err.println("Wanted "+index+", got "+i2);
    }
//System.err.println("Cached "+obj+" at "+index+" ("+cache.lastIndexOf(obj)+" of "+cache.size()+")");

    // since we just added this, start from end of list to find it
    return index;
  }

  public Object get(int index)
    throws IndexOutOfBoundsException
  {
    // if they're asking for an invalid index, don't give 'em anything
    if (index < 0) {
      throw new IndexOutOfBoundsException("Negative index");
    }

    // if there's no list, there's nothing to return
    if (cache == null) {
      throw new IndexOutOfBoundsException("No entries in cache");
    }

    return cache.get(index);
  }

  public int getIndex(Object obj)
  {
    // don't bother looking for null objects
    if (obj == null) {
      return -1;
    }

    // if there's no list, there's nothing to find
    if (cache == null) {
      return -1;
    }

    // return index (if any)
    return cache.indexOf(obj);
  }
}
