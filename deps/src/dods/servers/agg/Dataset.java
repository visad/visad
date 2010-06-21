// $Id: Dataset.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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
import thredds.util.Mutex;
import dods.dap.*;
import dods.util.Debug;
import dods.servlet.GuardedDataset;

// debug
import dods.servers.netcdf.NcDataset;

public abstract class Dataset implements GuardedDataset {

  public abstract dods.dap.Server.ServerDDS getDDS();
  public abstract dods.dap.DAS getDAS();
  public abstract void close() throws java.io.IOException;

  protected abstract dods.dap.DDS getClientDDS(); // because DODSDataset nor ServerDDS

  //////////////////////////////////////////////////////////////////////////////
  /* debugging
  static private int countId = 0;
  static private java.util.ArrayList list = new java.util.ArrayList(50);
  static private synchronized void add( Dataset ds) {
    ds.id = ++countId;
    list.add(ds);
  }
  static private synchronized void trackAcquire( Dataset ds) {
    if (ds.who != 0) {
      try {
        throw new RuntimeException("DATASET trackAcquire!! "+ds.getInternalPath()+" "+ds.who);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    ds.who = Thread.currentThread().hashCode();
  }
  static private synchronized boolean trackRelease( Dataset ds) {
    boolean ok = true;
    if (ds.who != Thread.currentThread().hashCode()) {
      ok = false;
      try {
        throw new RuntimeException("DATASET trackRelease!! "+ds.getInternalPath()+" "+ds.who+" "+Thread.currentThread());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    ds.who = 0;
    return ok;
  }
  private Thread who = 0;
  private int id = 0;

  synchronized void check( ) {
    java.util.HashSet set = new java.util.HashSet(200);
    for (int i=0, n=list.size(); i<n; i++) {
      Dataset ds = (Dataset) list.get(i);
      System.out.println("Dataset "+ ds.getInternalPath()+" "+ds.who+" "+mutex.isLocked()+" "+ds.hashCode());
      if (!set.add(ds))
        System.out.println("DUPLICATE Entry !!!");
    }

    set.clear();
    for (int i=0, n=list.size(); i<n; i++) {
      Dataset ds = (Dataset) list.get(i);
      if (!(ds instanceof NcDataset)) continue;
      NcDataset ncds = (NcDataset) ds;
      Object sysObj = ncds.getNetcdfFile().getSystemObject();

      System.out.println("NC Dataset "+ ncds.getNetcdfFile().getPathName()+" "+ncds.getNetcdfFile().hashCode());
      if (!set.add(sysObj))
        System.out.println("DUPLICATE Entry !!!");
    }

  } */

  //////////////////////////////////////////////////////////////////////////////
  private String extPath, intPath;
  private InvCatalog.Dataset invDS;
  protected Dataset( String extPath, String intPath, InvCatalog.Dataset invDS) {
    this.extPath = extPath;
    this.intPath = intPath;
    this.invDS = invDS;
    //add(this);
  }
  public String getExternalPath() { return extPath; }
  public String getInternalPath() { return intPath; }
  public InvCatalog.Dataset getCatalogDataset() { return invDS; }

  public void setDebug( boolean b) { debugMutex = b; }

  // implement locking
  private boolean /* debug = true, /*Debug.isSet("mutexTrack"),*/ debugMutex = Debug.isSet("mutex");
  private Mutex mutex = new Mutex();
  public void acquire() {
    try {
      mutex.acquire();
      //if (debug) trackAcquire( this);
      if (debugMutex) System.out.println("ACQUIRE "+intPath+" "+Thread.currentThread());
    } catch (InterruptedException e) {
      throw new RuntimeException("InterruptedException"+e.getMessage());
    }
  }

  public boolean attempt(long msecs) {
    try {
      boolean success = mutex.attempt(msecs);
      //if (success && debug) trackAcquire(this);
      if (debugMutex) {
        if (success)
          System.out.println("ATTEMPT OK("+msecs+")"+intPath+" "+Thread.currentThread());
        else
          System.out.println("ATTEMPT FAIL("+msecs+")"+intPath+" "+Thread.currentThread());
      }
      return success;
    } catch (InterruptedException e) {
      throw new RuntimeException("InterruptedException"+e.getMessage());
    }
  }

  public void release() {
    if (debugMutex) System.out.println("RELEASE "+intPath+" "+Thread.currentThread());
    /* if (debug) {
      if (trackRelease(this)) // track before the actual release is done
         mutex.release(); // only release if ok (?)
    } else { */
      mutex.release();
    //}
  }

  public synchronized boolean isLockedByMe() {
    return mutex.isLockedByMe();
  }

  public String whoHasLock() {
    return Integer.toString(mutex.getWho()) ;
  }
}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.4  2001/10/26 19:07:10  caron
   getClientDDS()

   Revision 1.3  2001/10/25 21:22:32  ndp
   Fixed up test server to work with Caron's ParsedRequest object...

   Revision 1.2  2001/10/24 23:00:47  ndp
   added Makefile

   Revision 1.1.1.1  2001/09/26 15:36:47  caron
   checkin beta1


 */
