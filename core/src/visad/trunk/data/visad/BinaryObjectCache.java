package visad.data.visad;

import java.util.ArrayList;

/**
 * A list which allows objects to be added at a specific index,
 * padding with <tt>null</tt>s if necessary.
 */
public class BinaryObjectCache
{
  private ArrayList cache = null;

  /**
   * Create an empty list.
   */
  public BinaryObjectCache() { }

  /**
   * Add an object at the specified index.
   *
   * @param obj Object to be added.
   *
   * @return <tt>-1</tt> if the object is <tt>null</tt>, or
   *         the index at which the object was added.
   */
  public int add(Object obj)
  {
    return add(-1, obj);
  }

  /**
   * Add an object to the end of the list.
   * If the index is less than zero, the object will be
   * added to the end of the list.
   *
   * @param obj Object to be added.
   *
   * @return <tt>-1</tt> if the object is <tt>null</tt>, or
   *         the index at which the object was added.
   */
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
      // add to end of list, then find out where it was added
      cache.add(obj);
      index = cache.lastIndexOf(obj);
    } else if (index < cacheLen) {
      // overwrite the current entry
      cache.set(index, obj);
    } else {
      // pad with nulls
      for (int i = cacheLen; i < index; i++) {
        cache.add(null);
      }

      // add to end of list
      cache.add(obj);
    }

    return index;
  }

  /**
   * Return the object found at the specified index.
   *
   * @param index Object index.
   *
   * @return The requested object.
   * @exception IndexOutOfBoundsException If the index is outside the
   *                                      list bounds.
   */
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

  /**
   * Return the index of the specified object.
   *
   * @param obj Object to find in the list,
   *
   * @return <tt>-1</tt> if the object was not found,
   *         or the index of the object.
   */
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
