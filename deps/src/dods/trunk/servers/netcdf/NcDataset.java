// $Id: NcDataset.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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

package dods.servers.netcdf;

import thredds.catalog.InvCatalog;

import dods.dap.*;
import dods.servers.agg.Dataset;
import dods.servers.agg.CacheDataset;
import dods.servers.agg.DatasetFactory;

import ucar.nc2.*;
import java.net.URL;

public class NcDataset extends Dataset {
  private static String datasetClassName = "netcdf";
  private static CacheDataset dsCache = new CacheDataset(new NetcdfFactory(), datasetClassName, 100);

  /**
   * set the size of the open dataset cache. Default is 100.
   */
  public static void setCacheMax( int maxCached) { dsCache.setCacheMax(maxCached); }

  /**
   * set maximum time to wait before opening another copy of the dataset.
   * @param wait : time in msec
   */
  public static void setWaitTime( long wait) { dsCache.setWaitTime(wait); }

  /**
   * get current size of the cache.
   */
  public static int getCacheSize() { return dsCache.getCacheSize(); }

    // debugging
  public static java.util.Iterator getCache() { return dsCache.getCache(); }

  /**
   * This is public as an artifact of implementing an interface.
   */
  // this is passed into CacheDataset for opening new datasets
  public static class NetcdfFactory implements DatasetFactory {
    public Dataset factory( String extPath, String intPath, InvCatalog.Dataset invDS) throws java.io.IOException {
      // open it
      NcDataset ds = new NcDataset( extPath, intPath, invDS);
      return ds;
    }
  }

  /**
   * This finds the named dataset and gets a lock on it. This is the only way to obtain
   * an NcDataset object.
   * WARNING: you better call ds.release() when you are done or you are SOL!!!
   *
   * @param extPath : external URL of dataset
   * @param intPath : internal URL of dataset
   * @param invDS : InvCatalog.Dataset object
   * @param block : if true, dont return till acquired. if false, return if cannot acquire
   * @return locked dataset, or null if no room in cache for it (can only happen if block == false)
  */
  public static Dataset acquire(String extPath, String intPath, InvCatalog.Dataset invDS,
         boolean block) throws java.io.IOException {
    Dataset ds = null;
    while (ds == null) {
      ds = dsCache.acquire( extPath, intPath, invDS);
      if (ds == null) {
        if (!block) return null;
        System.out.println("NcDataset waiting = "+intPath+" "+Thread.currentThread());
        try {
          Thread.currentThread().sleep(1000);  // notify would be sweeter
        } catch (InterruptedException e) {}
      }
    }
    return ds;
  }

  ////////////////////////////////////////////////////////////////////////////////
  private URL url;
  private NetcdfFile ncfile;
  private NcDDS dds;
  private NcDAS das;

  private boolean debug = false, debugURL = false;

  private NcDataset( String extPath, String intPath, InvCatalog.Dataset invDS) throws java.io.IOException {
    super( extPath, intPath, invDS);
    if (debug) System.out.println("NcDataset open new file = "+intPath);

    url = new URL( intPath);
    if (debugURL) {
      System.out.println("   URL = "+url.toString());
      System.out.println("   external form = "+url.toExternalForm());
      System.out.println("   protocol = "+url.getProtocol());
      System.out.println("   host = "+url.getHost());
      System.out.println("   path = "+url.getPath());
      System.out.println("   file = "+url.getFile());
    }

    this.ncfile = new NetcdfFile(url);
    this.das = new NcDAS( ncfile);        // parser broken
    this.dds = new NcDDS( invDS == null ? /*extPath */ "fake" : invDS.getURLpath(), ncfile);
  }

  public dods.dap.Server.ServerDDS getDDS() { return (dods.dap.Server.ServerDDS) dds.clone(); }
  public dods.dap.DAS getDAS() { return (dods.dap.DAS) das.clone(); }
  protected dods.dap.DDS getClientDDS() { return (dods.dap.DDS) dds.clone(); }

  public void close() throws java.io.IOException {
    ncfile.close();
  }
  public boolean isClosed() { return ncfile.isClosed(); }

  // debugging
  public NetcdfFile getNetcdfFile() { return ncfile; }
}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.2  2001/10/26 19:07:10  caron
   getClientDDS()

   Revision 1.1.1.1  2001/09/26 15:34:30  caron
   checkin beta1


 */