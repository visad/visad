// $Id: NcSDString.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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

import dods.dap.Server.*;
import dods.dap.*;
import dods.servers.agg.HasProxyObject;

import java.io.IOException;
import java.io.EOFException;

import ucar.ma2.*;
import ucar.nc2.*;

/**
 * Wraps a netcdf scalar or 1D char variable.
 * @version $Revision: 1.3 $
 * @author jcaron
 */
public class NcSDString extends SDString implements HasProxyObject {
  private Variable ncVar;
  private String val = null;

 /**
  * Constructor
  * @param ncVar: the netcdf Variable
  */
  NcSDString(Variable ncVar) {
    super(ncVar.getName());
    this.ncVar = ncVar;
  }


 /**
  * Constructor
  * @param name: name of variable
  * @param val: the value.
  */
  NcSDString(String name, String val) {
    super(name);
    this.val = val;
    if (val != null)
      setValue(val);
  }

      /** get/set the underlying proxy */
  public void setProxy(Object v) { this.ncVar = (Variable) v; }
  public Object getProxy() { return ncVar; }

  /** Read the value (parameters are ignored).*/
  public boolean read(String datasetName, Object specialO) throws IOException {
    if (val == null) {
      // read first time
      if (ncVar.getRank() == 0) {
        // scalar char - convert to a String
        ArrayChar.D0 a = (ArrayChar.D0) ncVar.read();
        byte[] b = new byte[1];
        b[0] = (byte) a.get();
        val = new String(b);
      } else {
        // 1D
        ArrayChar.D1 a = (ArrayChar.D1) ncVar.read();
        val = a.getString(a.getIndex()); // fetches the entire String
      }
    }

    setValue( val);
    setRead(true);
    return (false);
  }

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.1.1.1  2001/09/26 15:34:30  caron
   checkin beta1


 */
