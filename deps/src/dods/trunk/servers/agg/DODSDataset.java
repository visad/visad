// $Id: DODSDataset.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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
import dods.dap.*;
import dods.dap.Server.ServerDDS;

/***************************************************************************
* This is a proxy for a dataset on another DODS server.
*
* @author John Caron
* @version $Id: DODSDataset.java,v 1.3 2004-02-06 15:23:49 donm Exp $
*/

public class DODSDataset extends Dataset {
  private static String datasetClassName = "dods";
  private static CacheDataset dsCache = new CacheDataset(new DODSFactory(), datasetClassName, 0);

  /**
   * set the size of the open dataset cache. Default is unlimited.
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
  public static class DODSFactory implements DatasetFactory {
    public Dataset factory( String extPath, String intPath, InvCatalog.Dataset invDS) throws java.io.IOException {
      // open it
      try {
        DODSDataset ds = new DODSDataset( extPath, intPath, invDS);
        return ds;
      } catch (DODSException e) {
        System.out.println("DODSFactory DODSException = "+e);
        e.printStackTrace();
        throw new java.io.IOException( e.getMessage());
      }
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
   * @param block : if true, dont return till you got it. if false, return if cannot acquire
   * @return locked dataset, or null if no room in cache for it.
  */
  public static Dataset acquire(String extPath, String intPath, InvCatalog.Dataset invDS,
         boolean block) throws java.io.IOException {
    Dataset ds = null;
    while (ds == null) {
      ds = dsCache.acquire( extPath, intPath, invDS);
      if (ds == null) {
        if (!block) return null;
        System.out.println("DODSDataset waiting = "+intPath);
        try {
          Thread.currentThread().sleep(1000);  // notify would be sweeter
        } catch (InterruptedException e) {}
      }
    }
    return ds;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  private DConnect connect;
  private DDS dds;
  private DAS das;

  private boolean debug = false, showMetadata = false;

  private DODSDataset( String extPath, String intPath, InvCatalog.Dataset invDS) throws DODSException {
    super( extPath, intPath, invDS);

    try {
      if (debug) System.out.println("DConnect to = "+intPath);
      this.connect = new DConnect(intPath, true);
    } catch (java.io.FileNotFoundException e) {
      System.out.println("FileNotFoundException on DConnect= "+intPath);
      throw new DODSException("FileNotFoundException on DConnect= "+intPath);
    }

    try {
      this.das = connect.getDAS();
      if (showMetadata) {
        System.out.println("DAS = ");
        das.print(System.out);
      }
      this.dds = connect.getDDS();
      if (showMetadata) {
        System.out.println("DDS = ");
        dds.print(System.out);
      }
      ServerVersion dodsVersion = connect.getServerVersion();
      if (showMetadata) System.out.println("dodsVersion = "+dodsVersion);

    } catch (java.lang.Exception e) {
      System.out.println("DODSDataset Exception "+e);
      e.printStackTrace();
      throw new DODSException(e.getMessage());
    }

  }
  DConnect getConnection() { return connect; }

  public dods.dap.Server.ServerDDS getDDS() { return null; }
  public dods.dap.DAS getDAS() { return (dods.dap.DAS) das.clone(); }
  protected dods.dap.DDS getClientDDS() { return (dods.dap.DDS) dds.clone(); }

  // dont have to do anything to close because theres no state, GC will clean up
  public void close() { }

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.2  2001/10/26 19:07:10  caron
   getClientDDS()

   Revision 1.1.1.1  2001/09/26 15:36:47  caron
   checkin beta1


 */