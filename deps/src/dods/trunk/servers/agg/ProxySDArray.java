// $Id: ProxySDArray.java,v 1.4 2007-08-27 20:13:04 brucef Exp $
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

import dods.dap.Server.*;
import dods.dap.*;

import java.io.*;
import java.util.*;

/**
 * Proxy for an array in another (DODS) dataset.
 * If its 1D, read the entire array the first time and cache it.
 *
 * @version $Revision: 1.4 $
 * @author jcaron
 */
public class ProxySDArray extends SDArray implements HasProxyObject {
  private DODSDataset df;
  private boolean isString;
  private PrimitiveVector cachedData = null;
  private int ndims = 0;

  private boolean debug = false, debugRead = false;

 /**
  * Constructor
  * @param AggDataset.DodsFile df: the other dataset
  * @param DArray org: proxy for this array in df
  */
  ProxySDArray(DODSDataset df, DArray org) {
    super(org.getName());
    this.df = df;

    // set dimensions
    java.util.Enumeration dims = org.getDimensions();
    while (dims.hasMoreElements()) {
      DArrayDimension dd = (DArrayDimension) dims.nextElement();
      appendDim(dd.getSize(), dd.getName());
      ndims++;
    }

    // set the data type
    PrimitiveVector pv = org.getPrimitiveVector();
    addVariable( assignBaseType( pv.getTemplate()));
    isString = (pv.getTemplate() instanceof DString);
  }

  /* ProxySDArray(String name) {
    super(name);
  } */

    /** get/set the underlying proxy */
  public void setProxy(Object v) { this.df = (DODSDataset) v; }
  public Object getProxy() { return df; }

  /** Read the value from original dataset (parameters are ignored).*/
  public boolean read(String datasetName, Object specialO) throws IOException, EOFException {

    // if (cachedData == null) {
      // read the data
      PrimitiveVector data = read( this.getDimensions(), df.getConnection());

      // needs to be transferred to this pv
      PrimitiveVector pv = getPrimitiveVector();

      if (!isString)
        pv.setInternalStorage( data.getInternalStorage());
      else {  // array of strings
        BaseTypePrimitiveVector btdata = (BaseTypePrimitiveVector) data;
        BaseTypePrimitiveVector btpv = (BaseTypePrimitiveVector) pv;
        int nelems = data.getLength();
        btpv.setLength( nelems);
        for (int i=0; i<nelems; i++) {
          DString val = (DString) btdata.getValue(i);
          btpv.setValue( i, new AggSDString(val)); // wrap each one
        }
      }
    // }

    /* if you have filled cached Data, now subset if needed
    if (cachedData != null) {
      System.out.println("use cachedData ");

      // full data is alread in cachedData, just have to subset it
      try {
        PrimitiveVector subset = cachedData.subset( getStart(0), getStop(0), getStride(0));
        getPrimitiveVector().setInternalStorage( subset.getInternalStorage());
      } catch (dods.dap.Server.InvalidParameterException e) { }
    } */

    setRead(true);
    return (false);
  }

  /** this does the actual work of reading from the remote  dataset.
   *  it uses the projection info stored in the DArrayDimension's
   */
   private PrimitiveVector read(Enumeration dims, DConnect connect) throws IOException, EOFException {
     // create the constraint expression
    StringBuffer buff = new StringBuffer(100);
    buff.setLength(0);
    buff.append('?');
    buff.append( getName());

    // if 1D, read all of it
    // otherwise add the element constraints
    // if (ndims > 1) {
      // loop through the dimensions
      while (dims.hasMoreElements()) {
        DArrayDimension dd = (DArrayDimension) dims.nextElement();
        buff.append("[");
        buff.append(dd.getStart());
        buff.append(':');
        if (dd.getStride() > 1) {
          buff.append(dd.getStride());
          buff.append(':');
        }
        buff.append(dd.getStop());
        buff.append("]");
      }
    // }
    if (debugRead) {
      System.out.println("ProxySDArray read "+getName()+" : connect to = "+ df.getConnection().URL());
      System.out.println("ProxySDArray read: constraint = "+ buff);
    }

      // read the data
    dods.dap.DataDDS dataDDS;
    try {
      dataDDS = connect.getData(buff.toString(), null);
    } catch (DODSException e) {
      System.out.println("ProxySDArray read getData failed = "+e);
      throw new IOException(e.getMessage());
    } catch (dods.dap.parser.ParseException e) {
      System.out.println("ProxySDArray read getData failed = "+e);
      throw new IOException(e.getMessage());
    }
    if (debugRead)  {
      System.out.println("ProxySDArray dataDDS returned == ");
      dataDDS.print(System.out);
    }

    // get data
    Enumeration enumx = dataDDS.getVariables();
    BaseType bt = (BaseType) enumx.nextElement();

    DVector dv;
    if (bt instanceof DGrid) {
      DGrid grid = (DGrid) bt;
      try {
        dv = (DVector) grid.getVariable(grid.getName());
      } catch (NoSuchVariableException e) {
        throw new IllegalStateException(e.getMessage());
      }
    } else
      dv = (DVector) bt;

    boolean checkTypes = true;
    if (checkTypes) {
      BaseType proxyBT = getPrimitiveVector().getTemplate(); // proxy var
      BaseType dataBT = dv.getPrimitiveVector().getTemplate(); // proxy var
      if (!proxyBT.getName().equals( dataBT.getName())) {
        System.out.println(" ProxySDArray wrong data type proxy="+proxyBT.getName()+" data="+dataBT.getName());
        System.out.println("  dataRequest = "+buff);
        System.out.println("  dataDDS = ");
        dataDDS.print(System.out);
        throw new IOException(" ProxySDArray wrong data type proxy="+proxyBT.getName()+" data="+dataBT.getName());
      }
    }

    /* if (ndims <= 1) {
      cachedData = (PrimitiveVector) dv.getPrimitiveVector().clone();
    } */

    return dv.getPrimitiveVector();
  }

  /**
   * Override so we can cache the whole array, and subset it locally.
   *
   * @param dataset
   * @param sink
   * @param ce
   * @param specialO
   * @throws NoSuchVariableException
   * @throws SDODSException
   * @throws IOException
   *
  public void serialize(String dataset, DataOutputStream sink, CEEvaluator ce, Object specialO)
    throws NoSuchVariableException,SDODSException, IOException {

    if (!ce.evalClauses(specialO)) return;

    if(!isRead())
      read(dataset, specialO);

    PrimitiveVector vals = getPrimitiveVector();

    if (cachedData) {
      // full data is in the PrimitiveVector, just have to subset it
      int stride0 = (getStride(0) > 1) ? getStride(0) : 1;
      int length = 1 + (getStop(0) - getStart(0)) / stride0;
      sink.writeInt(length);

      if(vals instanceof BaseTypePrimitiveVector){
        BaseTypePrimitiveVector bvals = (BaseTypePrimitiveVector) vals;
        for(int i=getStart(0); i<=getStop(0); i+=stride0) {
          ServerMethods sm = (ServerMethods) bvals.getValue(i);
          sm.serialize(dataset,sink,ce, specialO);
        }
      } else {
        sink.writeInt(length);
        vals.externalize( sink, getStart(0), getStop(0), stride0);
      }

    } else {
      // just the wanted data is in the PrimitiveVector

      int length = vals.getLength();
      sink.writeInt(length);

      if(vals instanceof BaseTypePrimitiveVector){
        BaseTypePrimitiveVector bvals = (BaseTypePrimitiveVector) vals;
        for(int i=0; i<length ;i++){
          ServerMethods sm = (ServerMethods) bvals.getValue(i);
          sm.serialize(dataset,sink,ce, specialO);
        }
      } else {
        sink.writeInt(length);
        vals.externalize(sink);
      }
    }
  } */


  /*
   * The following is logically seperate from ProxySDArray. We get DXXXX types and we need
   *  to convert them to SDXXX types.
   */
  static public BaseType assignBaseType( BaseType bt) {
    if (bt instanceof DFloat64)
       return new AggSDFloat64(bt.getName());
    else if (bt instanceof DFloat32)
       return new AggSDFloat32(bt.getName());
    else if (bt instanceof DInt32)
       return new AggSDInt32(bt.getName());
    else if (bt instanceof DInt16)
       return new AggSDInt16(bt.getName());
    else if (bt instanceof DUInt32)
       return new AggSDUInt32(bt.getName());
    else if (bt instanceof DUInt16)
       return new AggSDUInt16(bt.getName());
    else if (bt instanceof DByte)
       return new AggSDByte(bt.getName());
    else if (bt instanceof DBoolean)
       return new AggSDBoolean(bt.getName());
     else if (bt instanceof DString)
       return new AggSDString(bt.getName());
    else
      throw new UnsupportedOperationException("ProxySDArray assignBaseType = " + bt.getName());
  }

  /////////////////////
  // these are all dummys, used only to assign a variable type
  // these apparently have to be concrete SD subclasses
  // LOOK: how come we cant use Proxy<type>  instead ?
  static class AggSDFloat64 extends SDFloat64 {
    AggSDFloat64( String name) { super(name); }
    public boolean read(String datasetName, Object specialO) throws IOException {
      setRead(true);
      return (false);
    }
  }

  static class AggSDFloat32 extends SDFloat32 {
    AggSDFloat32( String name) { super(name); }
    public boolean read(String datasetName, Object specialO) throws IOException {
      setRead(true);
      return (false);
    }
  }

  static class AggSDInt32 extends SDInt32 {
    AggSDInt32( String name) { super(name); }
    public boolean read(String datasetName, Object specialO) throws IOException {
      setRead(true);
      return (false);
    }
  }

  static class AggSDInt16 extends SDInt16 {
    AggSDInt16( String name) { super(name); }
    public boolean read(String datasetName, Object specialO) throws IOException {
      setRead(true);
      return (false);
    }
  }

  static class AggSDUInt32 extends SDUInt32 {
    AggSDUInt32( String name) { super(name); }
    public boolean read(String datasetName, Object specialO) throws IOException {
      setRead(true);
      return (false);
    }
  }

  static class AggSDUInt16 extends SDUInt16 {
    AggSDUInt16( String name) { super(name); }
    public boolean read(String datasetName, Object specialO) throws IOException {
      setRead(true);
      return (false);
    }
  }

  static class AggSDByte extends SDByte {
    AggSDByte( String name) { super(name); }
    public boolean read(String datasetName, Object specialO) throws IOException {
      setRead(true);
      return (false);
    }
  }

  static class AggSDBoolean extends SDBoolean {
    AggSDBoolean( String name) { super(name); }
    public boolean read(String datasetName, Object specialO) throws IOException {
      setRead(true);
      return (false);
    }
  }

  static class AggSDString extends SDString {
    AggSDString( String name) { super(name); }
    AggSDString( String name, String val) {
      super(name);
      setValue( val);
    }
    AggSDString( DString val) { setValue( val.getValue()); }
    public boolean read(String datasetName, Object specialO) throws IOException {
      setRead(true);
      return (false);
    }
  }

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.3  2004/02/06 15:23:49  donm
   update to 1.1.4

   Revision 1.2  2002/02/25 15:45:16  caron
   cache 1D arrays; efficient Coord vars

   Revision 1.1.1.1  2001/09/26 15:36:47  caron
   checkin beta1


 */
