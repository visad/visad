// $Id: ProxySDBoolean.java,v 1.4 2007-08-27 20:13:04 brucef Exp $
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
//import java.io.IOException;
//import java.io.EOFException;

/**
 * Proxy for a variable in another dataset
 * @version $Revision: 1.4 $
 * @author jcaron
 */
public class ProxySDBoolean extends SDBoolean implements HasProxyObject {
  private DODSDataset df;
  private boolean debugRead = true;

 /**
  * Constructor
  * @param SDFloat64 proxy: the netcdf Variable
  */
  ProxySDBoolean(DODSDataset df, String name) {
      super(name);
      this.df = df;
  }

      /** get/set the underlying proxy */
  public void setProxy(Object v) { this.df = (DODSDataset) v; }
  public Object getProxy() { return df; }


  /** Read the value from original dataset (parameters are ignored).*/
  public boolean read(String datasetName, Object specialO) throws java.io.IOException {

      // read the data
    dods.dap.DataDDS dataDDS;
    try {
      dataDDS = df.getConnection().getData("?"+getName(), null);
    } catch (Exception e) {
      System.out.println("ProxySDFloat64 read getData failed = "+e);
      return false;
    }
    if (debugRead)  {
      System.out.println("ProxySDFloat64 dataDDS return");
      dataDDS.print(System.out);
    }

    // get data
    java.util.Enumeration enumx = dataDDS.getVariables();
    DBoolean v = (DBoolean) enumx.nextElement(); // better only be one! better be DVector!
    setValue( v.getValue());

    setRead(true);
    return (false);
  }

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.3  2004/02/06 15:23:49  donm
   update to 1.1.4

   Revision 1.1.1.1  2001/09/26 15:36:47  caron
   checkin beta1


 */