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

package visad.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A write-only cache of objects which are slowly purged over time.
 */
public class ObjectCache
  implements Runnable
{
  /**
   * Each generation lives for <CODE>delay*numGenerations</CODE>
   * milliseconds.
   */
  private static final int delay = 5000;
  /**
   * The number of generations each object remains in the cache.
   */
  private static final int numGenerations = 6;
  /**
   * The thread which purges objects from all of the caches.
   */
  private static final transient Thread reaper;
  /**
   * The list of caches.
   */
  private static final transient ArrayList reaperList = new ArrayList();
  /**
   * The current maximum generation.
   */
  private static int reaperGeneration = 0;

  /**
   * The name of this cache.
   */
  private transient String name = null;
  /**
   * The list of objects in this cache.
   */
  private transient HashMap hash = new HashMap();
  /**
   * The current generation number for objects added to the cache.
   */
  private transient int cacheGeneration;

  static {
    // start up reaper thread when cache is loaded
    reaper = new Thread(new ObjectCache(null));
    reaper.setName("ObjectCache Reaper");
    reaper.start();
  }

  /**
   * Wrapper for an object in the cache.
   */
  private class QueueMember {
    int generation;
    Object object;

    /**
     * Creates a wrapper which binds an object with its generation.
     *
     * @param gen The generation number for this object.
     * @param obj The object to wrap.
     */
    public QueueMember(int gen, Object obj)
    {
      generation = gen;
      object = obj;
    }

    /**
     * Returns the string representation of the wrapped object.
     */
    public String toString()
    {
      return "QueueMember[" + object.toString() + "]";
    }
  }

  /**
   * Creates an object cache.
   *
   * @param name The name of the new cache.
   */
  public ObjectCache(String name)
  {
    this.name = name;

    setGeneration(reaperGeneration);

    synchronized (reaperList) {
      reaperList.add(this);
    }
  }

  /**
   * Adds an object to the cache.
   *
   * @param obj The object to add.
   */
  public synchronized void add(Object obj)
  {
    // don't try to cache null objects
    if (obj == null) {
      return;
    }

    QueueMember qm = new QueueMember(cacheGeneration, obj);
    hash.put(obj.getClass(), qm);
  }

/*
  public void dump()
  {
    synchronized (hash) {
      Iterator iter = hash.keySet().iterator();
      System.err.println(name + " CACHE DUMP:");
      while (iter.hasNext()) {
        System.err.println("\t" + ((QueueMember )hash.get(iter.next())).object);
      }
    }
  }
*/

  public Object get(Object key)
  {
    return hash.get(key);
  }

  /**
   * Returns <CODE>true</CODE> if this object is in the cache.
   *
   * @param obj The object to find
   */
  public synchronized boolean isCached(Object obj)
  {
    // don't even bother if they passed in a null object
    if (obj == null) {
      return true;
    }

    if (hash.containsKey(obj.getClass())) {
      Object q = ((QueueMember )hash.get(obj.getClass())).object;

      // if the objects are equal...
      if (obj.equals(q)) {

        // we've seen this object already
        return true;
      }
    }

    // didn't match any cached objects
    return false;
  }

  public Iterator keys()
  {
    return hash.keySet().iterator();
  }

  /**
   * Removes all objects of a given generation.
   *
   * @param generation The generation to purge.
   */
  private synchronized void purge(int generation)
  {
    synchronized (hash) {
      Iterator iter = hash.keySet().iterator();
      while (iter.hasNext()) {
        QueueMember member = (QueueMember )hash.get(iter.next());
        if (member.generation == generation) {
          iter.remove();
        }
      }
    }
  }

  public Object remove(Object key)
  {
    synchronized (hash) {
      return hash.remove(key);
    }
  }

  /**
   * Code used by the reaper thread to periodically wake up and purge
   * the next generation of objects.
   */
  public void run()
  {
    while (true) {
      try {
        synchronized (this) {
          wait(delay);
        }
      } catch (InterruptedException e) {
      }

      int nextGeneration = reaperGeneration + 1;
      int purgeGeneration = reaperGeneration - numGenerations;
      for (int i = reaperList.size() - 1; i >= 0; i--) {
        ObjectCache cache = (ObjectCache )reaperList.get(i);
        cache.setGeneration(nextGeneration);
        cache.purge(purgeGeneration);
      }
      reaperGeneration = nextGeneration;
    }
  }

  /**
   * Sets the generation number for this cache.
   *
   * @param generation The new generation number.
   */
  private void setGeneration(int generation) { cacheGeneration = generation; }

  /**
   * Gets the cache size.
   */
  private int size() { return hash.size(); }
}
