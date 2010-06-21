// $Id: CacheDataset.java,v 1.3 2004-02-06 15:23:49 donm Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package dods.servers.agg;

import thredds.catalog.InvCatalog;
import java.util.*;

import dods.util.Debug;

/**
 * This keeps a cache of Datasets, up to maxCached, and closes old ones based on
 * a simple LRU algorithm. Supposed to be thread safe.
 * The dataset mutex lock is acquired if successful.
 *
 * @author John Caron
 * @version $Id: CacheDataset.java,v 1.3 2004-02-06 15:23:49 donm Exp $
 */

public class CacheDataset {
  private DatasetFactory factory = null;
  private String cacheName;
  private int maxCached;
  private long wait = 250; // msec

  private int numCached = 0;
  private LinkedList cache;
  private boolean debug = Debug.isSet("cache");
  private boolean debugAdd = Debug.isSet("cacheAdd");

  /**
   * @param cacheName name of cache (for debug messages)
   * @param maxCached maximum number to cache (<= 0 means unlimited)
   */
  public CacheDataset(DatasetFactory factory, String cacheName, int maxCached) {
    this.factory = factory;
    this.cacheName = cacheName;
    this.maxCached = maxCached;

    cache = new LinkedList(); // switch to LinkedHashMap in 1.4
    if (debug) System.out.println("CACHE "+cacheName+" created");
  }

  /**
   * set maximum size of cache.
   */
  public void setCacheMax( int maxCached) { this.maxCached = maxCached; }

  /**
   * set maximum time to wait before opening another copy of the dataset.
   * @param wait : time in msec
   */
  public void setWaitTime( long wait) { this.wait = wait; }

  /**
   * get current size of the cache.
   */
  public int getCacheSize() { return numCached; }

  /** FOR DEBUGUGGING ONLY **/
  public Iterator getCache() { return cache.iterator(); }

  /**
   * This finds the named dataset and gets a lock on it.
   * WARNING: you better call ds.release() when you are done or you are SOL!!!
   *
   * @param extPath : external URL of dataset
   * @param intPath : internal URL of dataset
   * @param invDS : InvCatalog.Dataset object
   * @return locked dataset, or null if no room in cache for it.
  */
  public Dataset acquire( String extPath, String intPath, InvCatalog.Dataset invDS) throws java.io.IOException {
    Dataset ds = null;

    synchronized (this) {

      // look for it in the cache, no waiting for locks
      if (null != (ds = search( intPath, 0))) {
        if (debug) System.out.println("CACHE "+cacheName+" found file = "+intPath);
      }
        // not found, try again, waiting up to wait msec
      if (null == ds) {
        if (null != (ds = search( intPath, wait))) {
          if (debug) System.out.println("CACHE "+cacheName+" found file (waited) = "+intPath);
        }
      }
      if (debug && (ds == null)) System.out.println("CACHE "+cacheName+" miss = "+intPath);

      // not in cache, see if theres room to add it
      if (ds == null) {
        if (!reserveRoomInCache()) {
          if (debug) System.out.println("CACHE "+cacheName+" IS FULL "+numCached);
          return null;
        }
        if (debug || debugAdd) System.out.println("CACHE "+cacheName+" reserved room for "+intPath+" "
          +numCached+" "+Thread.currentThread());
      }
    } // synch

      // open new file
    if (ds == null) {
      ds = factory.factory( extPath, intPath, invDS);
      if (debug || debugAdd) System.out.println( "CACHE "+cacheName+" opened new dataset = "+intPath);
      ds.acquire();
      synchronized (this) {
        cache.add( 0, ds); // add to the top of the list
      }
    }

    return ds;
  }

  // search for the dataset named "want"; if found try to
  // acquire its mutex, waiting up to wait msec.
  // move it to the top of the list if successful and maxCached > 0.
  // return locked dataset, or null if failed
  private Dataset search( String want, long wait) {
    int tries = 1, count = -1;
    Iterator iter = cache.iterator();
    while (iter.hasNext()) {
      Dataset ds = (Dataset) iter.next();
      count++;

      if (ds.getInternalPath().equals(want)) { // found it
        if (!ds.attempt(wait)) {  // but its locked
          if ((debug) && (wait > 0)) System.out.println("CACHE "+cacheName+" waited in vain ("+tries+") for file "+want);
          tries++;
          continue;
        }

        // move it up if in bottom half of cache
        if ((maxCached > 0) && (count > maxCached/2)) {
          cache.remove(count); // remove from list
          cache.add( 0, ds); // add at the top
          if (debug) System.out.println("CACHE "+cacheName+" moved file = "+want);
        }
        return ds;
      }
    }

    return null; // never found it
  }

  private boolean reserveRoomInCache() {
    numCached++;
    if ((maxCached <= 0) || (numCached <= maxCached))
      return true;
    numCached--;

    if (cache.size() == 0)
      return false; // cache is empty, but all slots are reserved

    // see if we can find a non-locked dataset to bump out
    ListIterator iter = cache.listIterator( cache.size()-1);
    while (iter.hasPrevious()) {
      Dataset ds = (Dataset) iter.previous();
      if (ds.attempt(0)) {
        ds.release();
        iter.remove();
        try {
          ds.close();  // release resources
          if (debug || debugAdd) System.out.println("CACHE "+cacheName+" closed old file = "+ds.getInternalPath());
        } catch (java.io.IOException e) {
          System.out.println("ERROR closing file "+ds.getInternalPath()+"/n"+e);
        } finally {
          return true;
        }
      }
    }

    // cache is full
    return false;
  }


}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.2  2001/10/24 23:00:47  ndp
   added Makefile

   Revision 1.1.1.1  2001/09/26 15:36:47  caron
   checkin beta1


 */
