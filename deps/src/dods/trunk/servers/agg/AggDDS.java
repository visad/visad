// $Id: AggDDS.java,v 1.4 2007-08-27 20:13:04 brucef Exp $
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
import thredds.catalog.*;

import dods.dap.*;
import dods.dap.Server.*;
import dods.dap.parser.ParseException;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class AggDDS extends ServerDDS {
  private static boolean debug = false, debug2 = false;
  private AggDataset aggDataset;
  private DAS das;

    // type 1 constructor
  AggDDS( AggDataset aggDataset, String datasetName, Dataset defaultDataset ) {
    super( datasetName);
    this.aggDataset = aggDataset;
    das = (DAS) defaultDataset.getDAS().clone();

    // create the connector coordinate variable
    String coordName = aggDataset.getVariableName();
    String dateFormat = aggDataset.getDateFormat();
    ArrayList aggFiles = aggDataset.getAggFiles();

    // String defaults
    int size = aggFiles.size();
    String[] coords = new String[ size];
    for (int i=0; i<size; i++) {
      AggDataset.AggFile af = (AggDataset.AggFile) aggFiles.get(i);
      coords[i] = af.getCoord();
    }

    // make dates if possible
    int[] coordVals = null;
    if (dateFormat != null) {
      coordVals = new int[size];
      SimpleDateFormat parser = new SimpleDateFormat(dateFormat);
      for (int i=0; i<size; i++) {
        try {
          Date d = parser.parse(coords[i]);
          coordVals[i] = (int) (d.getTime()/1000);
        } catch (java.text.ParseException e) {
          System.out.println(" ERROR parsing date <"+coords[i]+">");
        }
      }

      // add units to DAS
      AttributeTable at = new AttributeTable();
      try {
        at.appendAttribute("units", Attribute.STRING, "\"secs since 1970-01-01 00:00:00\"");
      } catch (Exception e) {}
      das.addAttributeTable( coordName, at);
    }

    // the coordinate array on the new dimension
    ArrayList dims = new ArrayList(1);
    dims.add( new DArrayDimension( size, coordName));
    Object data;
    if (coordVals == null) data = coords; else data = coordVals; // bug in Jbuilder
    SDArray connectCoord = new MemSDArray( coordName, dims, data);

    // add it to DDS
    addVariable( connectCoord);

    // now loop through orginal DDS and construct new DDS
    boolean isRemote = (defaultDataset instanceof DODSDataset);
    DDS orgDDS = defaultDataset.getClientDDS();
    Enumeration dodsVars = orgDDS.getVariables();
    while (dodsVars.hasMoreElements()) {
      dods.dap.BaseType bt = (dods.dap.BaseType) dodsVars.nextElement();

      // only DGrids are joined
      if (bt instanceof DGrid) {

        addVariable( new AggSDGrid( (DGrid) bt, connectCoord, aggDataset, defaultDataset));

      } else if (bt instanceof DArray) {

        if (isRemote)
          addVariable( new ProxySDArray( (DODSDataset) defaultDataset, (DArray) bt));
        else
          addVariable( (SDArray) bt); // cast here so bombs early if problem

      } else { // scalar
        addScalar( defaultDataset, bt, isRemote);
      }
    } // hasMore

  }

  // type 2 Constructor
  AggDDS( AggDataset aggDataset, String datasetName) {
    super( datasetName);
    this.aggDataset = aggDataset;

    if (debug2) System.out.println(" AggDDS Type 2 "+datasetName);
    HashSet varNames = new HashSet();
    das = new DAS();

    // loop through datasets
    Iterator aggFiles = aggDataset.getAggFiles().iterator();
    while (aggFiles.hasNext()) {
      AggDataset.AggFile af = (AggDataset.AggFile) aggFiles.next();

      try {
        Dataset ds = af.acquire(true);
        //ds.setDebug(true);

        DDS dds = ds.getClientDDS();
        boolean isRemote = (ds instanceof DODSDataset);
        if (debug2) System.out.println(" Dataset "+ds.getInternalPath());

        // extract the variables from each dataset to make the DDS
        // assume that names that match are identical
        DDS orgDDS = ds.getClientDDS();
        Enumeration dodsVars = orgDDS.getVariables();
        while (dodsVars.hasMoreElements()) {
          dods.dap.BaseType bt = (dods.dap.BaseType) dodsVars.nextElement();
          if (varNames.contains( bt.getName()))
            continue; // already got it !!

          //add it
          varNames.add( bt.getName());
          aggDataset.mapVarToFile( bt.getName(), af);
          if (debug2) System.out.println(" adding "+bt.getName()+" "+bt.getClass().getName());

          if (bt instanceof DGrid) {
            if (isRemote)
              addVariable( new ProxySDGrid( (DODSDataset) ds, (DGrid) bt));
            else // if from netcdf file, can use directly
              addVariable( (SDGrid) bt); // cast here so bombs early if problem

          } else if (bt instanceof DArray) {
            if (isRemote)
              addVariable( new ProxySDArray( (DODSDataset) ds, (DArray) bt));
            else // if from netcdf file, can use directly
              addVariable( (SDArray) bt); // cast here so bombs early if problem

          }  else { // scalar
            addScalar( ds, bt, isRemote);
          }

        } // hasMore dods variables in orgDDS

        // while we're at it, construct the DAS also
        DAS orgDAS = ds.getDAS();
        java.util.Enumeration enumx = orgDAS.getNames();
        while (enumx.hasMoreElements()) {
          String attTableName = (String) enumx.nextElement();
          if (null != das.getAttributeTable(attTableName))
            continue; // already got it.

          AttributeTable oldAT = orgDAS.getAttributeTable(attTableName);
          das.addAttributeTable(attTableName, (AttributeTable) oldAT.clone());
          //addAttributeTable( newAT, oldAT);
        } // hasMore attributes in orgDAS

      } catch (IOException ioe) {
        System.out.println("ERROR AggDDS: Failed to open Dataset "+af.getName()+" \n");
        ioe.printStackTrace();
      } finally {
        af.release();
      }

    } // loop over datasets

  }

  // type 3 constructor
  AggDDS( AggDataset aggDataset, String datasetName, Dataset defaultDataset, DArray oldConnectVar, int nelems) {
    super( datasetName);
    this.aggDataset = aggDataset;
    das = defaultDataset.getDAS();

    // first make the new connector dimension
    DArrayDimension oldConnectDim = oldConnectVar.getFirstDimension();
    DArrayDimension newConnectDim = new DArrayDimension( nelems, oldConnectDim.getName());

    // now loop through orginal DDS and construct new DDS
    boolean isRemote = (defaultDataset instanceof DODSDataset);
    DDS orgDDS = defaultDataset.getClientDDS();
    Enumeration dodsVars = orgDDS.getVariables();
    while (dodsVars.hasMoreElements()) {
      dods.dap.BaseType bt = (dods.dap.BaseType) dodsVars.nextElement();

      if (bt instanceof DGrid) {
        DGrid grid = (DGrid) bt;
        DArray da;
        try {
          da = (DArray) grid.getVar(0);
        } catch (NoSuchVariableException e) {
          throw new IllegalStateException(e.getMessage());
        }

        // create a different class for joined and unjoined case
        if (isJoinedArray(da, newConnectDim))
          addVariable( new AggSDGrid( grid, newConnectDim, aggDataset, defaultDataset));
        else if (isRemote)
          addVariable( new ProxySDGrid( (DODSDataset) defaultDataset, grid));
        else
          addVariable( (SDGrid) grid); // cast here so bombs early if problem

      } else if (bt instanceof DArray) {

        // create a different class for joined and unjoined case
        if (isJoinedArray((DArray) bt, newConnectDim))
          addVariable( new Agg3SDArray( (DArray) bt, newConnectDim, aggDataset));
        else if (isRemote)
          addVariable( new ProxySDArray( (DODSDataset) defaultDataset, (DArray) bt));
        else
          addVariable( (SDArray) bt); // cast here so bombs early if problem

      } else { // scalar
        addScalar( defaultDataset, bt, isRemote);
      }
    } // hasMore

  }

  public DAS getDAS() { return das; }

    // see if this array has the connector dimension
  private boolean isJoinedArray( DArray org, DArrayDimension joinedDD) {
    java.util.Enumeration dims = org.getDimensions();
    while (dims.hasMoreElements()) {
      DArrayDimension dd = (DArrayDimension) dims.nextElement();
      if (dd.getName().equals(joinedDD.getName()))
        return true;
    }
    return false;
  }

  private void addScalar( Dataset ds, dods.dap.BaseType bt, boolean isRemote) {
    if (bt instanceof DFloat64) {

      if (isRemote)
        addVariable (new ProxySDFloat64((DODSDataset) ds, bt.getName()));
      else
        addVariable ( (SDFloat64) bt);

    } else if (bt instanceof DFloat32) {
      if (isRemote)
        addVariable (new ProxySDFloat32((DODSDataset) ds, bt.getName()));
      else
        addVariable ( (SDFloat32) bt);

    } else if (bt instanceof DInt32) {
      if (isRemote)
        addVariable (new ProxySDInt32((DODSDataset) ds, bt.getName()));
      else
        addVariable ( (SDInt32) bt);

    } else if (bt instanceof DInt16) {
      if (isRemote)
        addVariable (new ProxySDInt16((DODSDataset) ds, bt.getName()));
      else
        addVariable ( (SDInt16) bt);

    } else if (bt instanceof DUInt32) {
      if (isRemote)
        addVariable (new ProxySDUInt32((DODSDataset) ds, bt.getName()));
      else
        addVariable ( (SDUInt32) bt);

    } else if (bt instanceof DUInt16) {
      if (isRemote)
        addVariable (new ProxySDUInt16((DODSDataset) ds, bt.getName()));
      else
        addVariable ( (SDUInt16) bt);

    } else if (bt instanceof DByte) {
      if (isRemote)
        addVariable (new ProxySDByte((DODSDataset) ds, bt.getName()));
      else
        addVariable ( (SDByte) bt);

    } else if (bt instanceof DBoolean) {
      if (isRemote)
        addVariable (new ProxySDBoolean((DODSDataset) ds, bt.getName()));
      else
        addVariable ( (SDBoolean) bt);

    } else if (bt instanceof DString) {
      if (isRemote)
        addVariable (new ProxySDString((DODSDataset) ds, bt.getName()));
      else
        addVariable ( (SDString) bt);
    }
  }

    // We need to reset the DDS to the correct underlying datasets
  void reset() throws java.io.IOException {

    Enumeration dodsVars = getVariables();
    while (dodsVars.hasMoreElements()) {
      dods.dap.BaseType bt = (dods.dap.BaseType) dodsVars.nextElement();
      AggDataset.AggFile af = aggDataset.getFileForVar( bt.getName());
      Dataset ds = af.acquire(true);
      resetVar( bt, ds, ds.getClientDDS());
    }
  }

  // We need to reset the DDS to the correct underlying datasets
  void reset(Dataset ds, DDS defaultDataset) {

    Enumeration dodsVars = getVariables();
    while (dodsVars.hasMoreElements()) {
      dods.dap.BaseType bt = (dods.dap.BaseType) dodsVars.nextElement();
      resetVar( bt, ds, defaultDataset);
    } // hasMore DODS Variables

  }

  private void resetVar( BaseType bt, Dataset ds, DDS dds) {
    // the non-aggregation variables must be reset to the defaultDatset
    if (bt instanceof DGrid) {
      DGrid grid = (DGrid) bt;
      java.util.Enumeration vars = grid.getVariables();
      while (vars.hasMoreElements()) {
        SDArray dodsArray = (SDArray) vars.nextElement();
        if (dodsArray instanceof HasProxyObject) {
          HasProxyObject po = (HasProxyObject) dodsArray;
          po.setProxy( getProxyObject(ds, dds, dodsArray.getName()));
        }
      }
    } else if (bt instanceof HasProxyObject) {
      HasProxyObject po = (HasProxyObject) bt;
      po.setProxy( getProxyObject(ds, dds, bt.getName()));
    }
  }

  private Object getProxyObject(Dataset ds, DDS defDDS, String name) {
    BaseType bt;
    try {
      bt = defDDS.getVariable(name);
    } catch (NoSuchVariableException e) {
      throw new IllegalStateException("AggDDS.getProxyObject failed "+e.getMessage());
    }

    if (bt instanceof DGrid) {
      DGrid grid = (DGrid) bt;
      try {
        bt = grid.getVar(0);
      } catch (dods.dap.NoSuchVariableException e) { // will throw te following exception
      }
    }

    if (bt instanceof HasProxyObject) {
      HasProxyObject po = (HasProxyObject) bt;
      return po.getProxy();
    }

    if (ds instanceof DODSDataset) {
      return ds;
    }

    throw new IllegalStateException("AggDDS.getProxyObject not HasProxyObject ="
            +name+" "+bt.getClass().getName()+" "+ds.getClass().getName());

  }

}

/*   private void addAttributeTable( AttributeTable newAT, AttributeTable oldAT) {
    java.util.Enumeration enum = oldAT.getNames();
    while (enum.hasMoreElements()) {
      String attName = (String) enum.nextElement();
      Attribute att = oldAT.getAttribute(attName);
      if (att.isContainer()) {
        AttributeTable oldt = att.getContainer();
        AttributeTable newt = newAT.appendContainer(attName);
        addAttributeTable( newt, oldt);
      } else {
        try {
          // lousy interface
          newAT.appendAttribute(attName, att.getType(), att.getValueAt(0));
          Attribute newAtt = newAT.getAttribute(attName);

          java.util.Enumeration values = att.getValues();
          values.nextElement(); // throw first away
          while (values.hasMoreElements())
            newAtt.appendValue((String) values.nextElement());
        } catch (AttributeExistsException e) {
          System.out.println("AggDDS addAttributeTable "+attName+" "+e.getMessage());
        } catch (AttributeBadValueException e) {
          System.out.println("AggDDS addAttributeTable "+attName+" "+e.getMessage());
        } // try
      } // not a container
    } // loop over atts
  }
*/



/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.3  2004/02/06 15:23:49  donm
   update to 1.1.4

   Revision 1.3  2002/02/25 15:50:12  caron
   byte array padding

   Revision 1.2  2001/10/26 19:07:10  caron
   getClientDDS()

   Revision 1.1.1.1  2001/09/26 15:36:47  caron
   checkin beta1


 */
