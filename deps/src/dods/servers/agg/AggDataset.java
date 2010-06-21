// $Id: AggDataset.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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

import dods.servers.netcdf.NcDataset;
import thredds.catalog.InvCatalog;
import thredds.catalog.*;

import dods.dap.*;
import dods.dap.Server.*;
import dods.dap.parser.ParseException;

import java.io.*;
import java.util.*;


/***************************************************************************
* This creates a logical dataset, by combining a list of Datasets specified in
*  an "aggregation" element of an InvCatalog.Dataset. The Datasets may be other
*  DODS Datasets (DODSDataset) or local netcdf files (NcDataset). Since they all
*  implement Dataset interface, this is a Composite design pattern.
*
* Currently can do:
*   Type 1: combine datasets, creating a new coordinate variable, one dataset per coord
*   Type 2: combine all the variables in a set of datasets
*   Type 3: combine datasets based on an existing coordinate variable,
*
* @author John Caron
* @version $Id: AggDataset.java,v 1.3 2004-02-06 15:23:49 donm Exp $
*/

public class AggDataset extends Dataset {
  private static String datasetClassName = "Agg";
  private static CacheDataset dsCache = new CacheDataset(new AggFactory(), datasetClassName, 10);
  private static boolean debugAcquire = true;

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
  public static class AggFactory implements DatasetFactory {
    public Dataset factory( String extPath, String intPath, InvCatalog.Dataset invDS) throws java.io.IOException {
      // open it
      AggDataset ds = new AggDataset( extPath, intPath, invDS);
      return ds;
    }
  }

  /**
   * This finds the named dataset and gets a lock on it. This is the only way to obtain
   * an AggDataset object.
   * WARNING: you better call ds.release() when you are done or you are SOL!!!
   *
   * @param extPath : external URL of dataset
   * @param intPath : internal URL of dataset
   * @param invDS : InvCatalog.Dataset object
   * @return locked dataset
  */
  public static Dataset acquire(String extPath, String intPath, InvCatalog.Dataset invDS) throws java.io.IOException {
    AggDataset ds = null;
    while (ds == null) {
      ds = (AggDataset) dsCache.acquire( extPath, intPath, invDS);
      if (ds == null) {
        System.out.println("AggDataset waiting = "+intPath+" "+Thread.currentThread());
        try {
          Thread.currentThread().sleep(1000);  // notify would be sweeter
        } catch (InterruptedException e) {}
      }
    }

    if (debugAcquire) {
      if (!ds.isLockedByMe())
        throw new RuntimeException("ERROR AggDataset trackAcquire 1!! "+intPath+" "+Thread.currentThread());
    }

    ds.acquireComponents();
    ds.resetDDS();
    return ds;
  }

  /**
   * Release the lock on this dataset.
   */
  public void release() {
    releaseComponents();  // release components before relinquishing the lock
    super.release();
  }


  ///////////////////////////////////////////////////////////////////////////////
  // a bit tricky - we need to acquire all component AggFiles that are marked "retain"
  // if we fail, we need to back off and release everything, then try again.
  private void acquireComponents() {
    boolean success;

    while (true) {
      success = true;

      // acquire all datasets marked "retain"
      for (int i=0; i<aggFiles.size(); i++) {
        AggFile af = (AggFile) aggFiles.get(i);
        if (!af.retain) continue;
        try {
          if (null == af.acquire(false)) {
            success = false;
            break;
          }
        } catch (IOException ioe) {
          System.out.println("ERROR AggDataset: Failed to open Dataset "+af.getName()+" \n"+ioe);
          success = false;
          break;
        }
      }

      if (success) return;  // got em all

      // try it again
      releaseComponents();
      System.out.println("AggDataset waiting on Component "+getInternalPath()+" "+Thread.currentThread());
      try { Thread.currentThread().sleep(1000); }
      catch (InterruptedException e) {}
    }
  }

  // release all component files
  private void releaseComponents() {
    // release all datasets
    for (int i=0; i<aggFiles.size(); i++) {
      AggFile af = (AggFile) aggFiles.get(i);
      af.releaseUnconditionally();
    }
  }

  /* void check( AggFile af) {
    System.out.println("AggDataset "+ getInternalPath()+" file = "+af.getName());
    System.out.println("  "+ af.ds.hashCode()+" "+af.ds.isLockedByMe()+" "+Thread.currentThread());
    super.check();
  } */

  ///////////////////////////////////////////////////////////////////////////////
  private AggDDS dds = null;
  private DAS das = null;
  private AggServerConfig.Aggregation agg;
  private ArrayList aggFiles = new ArrayList(); // AggFile objects
  private AggFile defaultFile = null; // the "representative" dataset
  private int type = 0;
  private HashMap varMap = new HashMap(50);

  private final boolean debug = false, showAggDDS = false, debugReadIndividualDatasets = false;

  private AggDataset(  String extPath, String intPath, InvCatalog.Dataset invDS) throws java.io.IOException {
    super( extPath, intPath, invDS);

    agg = (AggServerConfig.Aggregation) invDS.getUserObj();
    if ((agg == null) || (agg.size() == 0)) {
      throw new IllegalStateException("AggDataset catalog has no aggregation files "+invDS.getName());
    }

    // run through each file, wrap in an AggFile object
    ArrayList dataFiles = agg.getDataFiles();
    for (int i=0; i<dataFiles.size(); i++) {
      AggServerConfig.DataFile dataFile = (AggServerConfig.DataFile) dataFiles.get(i);
      if (debug) System.out.println(" AggDataset File: "+dataFile.getURL());

      // wrap it in an AggFile
      AggFile af = new AggFile( dataFile);
      aggFiles.add(af);

      // defaultFile should be a local file for efficiency, if possible
      if ((dataFile.getServerType() == ServerType.netCDF) && (defaultFile == null))
        defaultFile = af;
    }

    // get agg type
    String aggType = agg.getAggType();
    if (aggType.equals("1"))
      type = 1;
    else if (aggType.equals("2"))
      type = 2;
    else if (aggType.equals("3"))
      type = 3;
    else
      throw new IllegalStateException("AggDataset "+invDS.getName()+" has illegal type "+aggType);

    // get default dataset
    Dataset defaultDataset = null; // the "representative" dataset
    if (type != 2) {
      if (defaultFile == null)
        defaultFile = ((AggFile)aggFiles.get(0));
      defaultFile.retain = true;
      defaultDataset = defaultFile.acquire(true); // needed now to derive DDS, DAS
    }

    // type specific processing
    if (type == 1) {
      dds = new AggDDS( this, "fake" /* extPath */, defaultDataset);
      das = ((AggDDS)dds).getDAS();

    } else if (type == 2) {

      // retain all - optimize later
      for (int i=0; i<aggFiles.size(); i++) {
        AggFile af = (AggFile) aggFiles.get(i);
        af.retain = true;
      }
      dds = new AggDDS( this, "fake" /* extPath */);
      das = ((AggDDS)dds).getDAS();

    } else if (type == 3) {
      String connectVariableName = agg.getVariableName();
      dds = processType3(connectVariableName, defaultDataset);
      das = defaultDataset.getDAS();
    }

    if (showAggDDS) dds.print(System.out);
  }

  private AggDDS processType3(String connectVariableName, Dataset defaultDataset) {
    DArray connectVar = null;
    int nelems = 0;

    // read all the datasets
    for (int i=0; i<aggFiles.size(); i++) {
      AggFile af = (AggFile) aggFiles.get(i);

      Dataset ds = null;
      try {
        ds = af.acquire(true);
        DDS dds = ds.getClientDDS();

        af.aggStart = nelems; // track the index for the connect variable
        connectVar = (DArray) dds.getVariable( connectVariableName);
        int size = connectVar.getFirstDimension().getSize();
        nelems += size;
        af.aggEnd = af.aggStart + size;
        if (debug) System.out.println("  nelems = "+nelems+ " from "+af.aggStart+" to "+af.aggEnd);
      } catch (NoSuchVariableException e) {
        System.out.println("ERROR: No such variable ");
        dds.print( System.out);
        throw new IllegalStateException("No such Variable " + connectVariableName+" in file "+af.getName());
      } catch (IOException ioe) {
        System.out.println("ERROR AggDataset: Failed to open Dataset "+af.getName()+" \n"+ioe);
      } finally {
        af.release();
      }
    }

    // synthesize the DDS
    return new AggDDS( this, "fake" /* extPath */, defaultDataset, connectVar, nelems);
  }

  private void resetDDS() throws java.io.IOException {
    if (type != 2) {
      Dataset ds = defaultFile.acquire(true);
      dds.reset(ds, ds.getClientDDS()); // default dataset
    } else
      dds.reset();
  }

  ///////////////////////////// public accessors /////////////////////////////////////
  public dods.dap.Server.ServerDDS getDDS() { return (dods.dap.Server.ServerDDS) dds.clone(); }
  public DAS getDAS() { return (DAS) das.clone(); }
  public ArrayList getAggFiles() { return aggFiles; }
  protected dods.dap.DDS getClientDDS() { return (dods.dap.DDS) dds.clone(); }

  public String getVariableName() { return agg.getVariableName(); }
  public String getDateFormat() { return agg.getDateFormat(); }

  // dont have to do anything to close because theres no state, GC will clean up
  public void close() { }

  public void mapVarToFile( String varName, AggFile af) { varMap.put( varName, af); }
  public AggFile getFileForVar (String varName) { return (AggFile) varMap.get( varName); }


  ///////////////////////////// AggFile /////////////////////////////////////
  // wrap the dataset; one for each file in the aggregation
  // these objects are completely contained in the AggDataset, so there can be no
  // thread contention (if the AggDatasets are properly acquired).
  //
  public class AggFile {
    private AggServerConfig.DataFile dataFile;
    private String coord;
    private boolean retain = false;    // retain for the duration of the access

    private Dataset ds = null;
    private int aggStart = 0, aggEnd = 0; // index in aggregated dataset;
                                          // aggStart <= i < aggEnd

    private AggFile( AggServerConfig.DataFile dataFile) {
      this.dataFile = dataFile;
      this.coord = dataFile.getCoord();
    }
    String getName() { return dataFile.getURL(); }
    String getCoord() { return dataFile.getCoord(); }

    public int getSize() { return aggEnd - aggStart; }

    Dataset acquire(boolean block) throws java.io.IOException {
      if (isLockedByMe())
        return ds;

      if (dataFile.getServerType() == ServerType.netCDF) {
        ds = NcDataset.acquire( "fake", dataFile.getURL(), null, block); // HEY! what about invDS ???
      } else {
        ds = DODSDataset.acquire( "fake", dataFile.getURL(), null, block);
      }

      if (debugAcquire && !isLockedByMe())
        throw new RuntimeException("ERROR AggDataset trackAcquire 3!! "+dataFile.getURL()+" "+Thread.currentThread());
      return ds;
    }

    void release() {
      if (!retain) {
        releaseUnconditionally();
    } else if (debugAcquire && !isLockedByMe())
      throw new RuntimeException("ERROR AggDataset trackAcquire 4!! "+dataFile.getURL()+" "+Thread.currentThread());
    }

    private void releaseUnconditionally () {
      if ((ds != null) && isLockedByMe()) ds.release();
      ds = null;
    }

    private boolean isLockedByMe() {
      if (ds != null) return ds.isLockedByMe();
      return false;
    }

    /* Gets an SDArray out of the dataset, using the variable name.
     * The variable must be an SDArray, SDGrid or a DGrid.
     * If its a DGrid, extracts the DArray and wraps it in a ProxySDArray,
     * because we need an SDArray, not a DArray.
     */
    SDArray getVariable( String name) {
      if (debugAcquire && !isLockedByMe())
        throw new RuntimeException("ERROR AggDataset trackRelease 10!! "+dataFile.getURL()+" "+Thread.currentThread());

      BaseType bt;
      try {
        bt = ds.getClientDDS().getVariable(name);
      } catch (NoSuchVariableException e) {
        throw new IllegalStateException("AggFile.getVariable "+e.getMessage());
      }
      if (bt instanceof SDArray)
        return (SDArray) bt;

      if (bt instanceof DGrid) {
        DGrid grid = (DGrid) bt;
        DArray da;
         try {
          da = (DArray) grid.getVariable(grid.getName());
        } catch (NoSuchVariableException e) {
          throw new IllegalStateException("AggFile.getVariable 2"+e.getMessage());
        }
        if (da instanceof SDArray)
          return (SDArray) da;
        bt = da;
      }

      return new ProxySDArray((DODSDataset) ds, (DArray) bt); // BARF
    }

    boolean isNeeded( int wantStart, int wantStride, int wantStop) {
      wantStop++; // work in exclusive intervals

      // deal with strides
      if (wantStride < 1)
        wantStride = 1;
      else {
        // make sure wantStart is not skipped by the stride
        while (wantStart < aggStart)
          wantStart += wantStride;
      }

      if (wantStart >= wantStop)
        return false;
      if ((wantStart >= aggEnd) || (wantStop < aggStart))
        return false;

      return true;
    }

    // wantStart, wantStop is the index in aggregated dataset, inclusive
    // if this overlaps, set the projection of the first dimension
    // if no overlap, return false
    boolean setProjection0( SDArray sda, int wantStart, int wantStride, int wantStop) {
      if (debugAcquire && !isLockedByMe())
        throw new RuntimeException("ERROR AggDataset trackRelease 10!! "+dataFile.getURL()+" "+Thread.currentThread());

      wantStop++; // work in exclusive intervals

      // deal with strides
      if (wantStride < 1)
        wantStride = 1;
      else {
        // make sure wantStart is not skipped by the stride
        while (wantStart < aggStart)
          wantStart += wantStride;
      }

      if (wantStart >= wantStop)
        return false;
      if ((wantStart >= aggEnd) || (wantStop < aggStart))
        return false;

      int start = Math.max( aggStart, wantStart) - aggStart;
      int stop = Math.min( aggEnd, wantStop) - aggStart - 1;
      if (debugReadIndividualDatasets) {
        System.out.println("  AggFile "+getInternalPath()+" "+sda.getName());
        System.out.println("    wantStart = "+ wantStart+" wantStride "+wantStride+" wantStop "+wantStop);
        System.out.println("    get from = "+ start+" to "+stop);
      }

      try {
        sda.setProjection(0, start, wantStride, stop);
      } catch (InvalidParameterException e) {
        throw new IllegalStateException( e.getMessage());
      }

      return true;
    }
  }

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.2  2001/10/26 19:07:10  caron
   getClientDDS()

   Revision 1.1.1.1  2001/09/26 15:36:46  caron
   checkin beta1


 */

