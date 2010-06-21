// $Id: NcDDS.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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

import ucar.ma2.*;
import ucar.nc2.*;

import dods.dap.*;
import dods.dap.Server.*;

import java.io.IOException;
import java.util.*;

/** NcDDS is a specialization of ServerDDS for netcdf files.
 *
 *   @version $Revision: 1.3 $
 *   @author jcaron
 */

public class NcDDS extends ServerDDS implements Cloneable {
  private HashMap dimHash = new HashMap(50);

  /** Constructor */
  NcDDS( String name, NetcdfFile ncfile) {
    super(name);

    // get coordinate variables
    Iterator iterD = ncfile.getDimensionIterator();
    while (iterD.hasNext()) {
      Dimension dim = (Dimension) iterD.next();
      Variable cv = dim.getCoordinateVariable();
      if (cv != null)
        dimHash.put( dim.getName(), new NcSDArray( cv, createVariable(cv)));
    }

    Iterator iter = ncfile.getVariableIterator();
    while (iter.hasNext()) {
      Variable v = (Variable) iter.next();

      if (v.isCoordinateVariable()) // coordinate variables
        addVariable((BaseType) dimHash.get(v.getName()));
      else if (v.getRank() == 0)  // scalar
        addVariable( createVariable( v));
      else if (v.getElementType() == char.class) { // String
        BaseType bt;
        if (v.getRank() > 1)
          bt = new NcSDCharArray(v);
        else
          bt = new NcSDString(v);
        addVariable( bt);
      } else  // non-char multidim array
        addVariable( createArray( v));
    }
  }

  private BaseType createVariable( Variable v) {
    Class c = v.getElementType();
    if (c == double.class)
       return new NcSDFloat64(v);
    else if (c == float.class)
      return new NcSDFloat32(v);
    else if (c == int.class)
      return new NcSDInt32(v);
    else if (c == short.class)
      return new NcSDInt16(v);
    else if (c == byte.class)
      return new NcSDByte(v);
    else if (c == char.class)
      return new NcSDString(v);
    else
      throw new UnsupportedOperationException("NcDDS class = "+c.getName());
  }

  private BaseType createArray( Variable v) {
    // all dimensions must have coord vars to be a grid
    boolean isGrid = true;
    Iterator iter = v.getDimensions().iterator();
    while (iter.hasNext()) {
      Dimension dim = (Dimension) iter.next();
      isGrid &= (dim.getCoordinateVariable() != null);
    }

    NcSDArray arr = new NcSDArray( v, createVariable(v));
    if (!isGrid)
      return arr;

    ArrayList list = new ArrayList();
    list.add( arr);
    iter = v.getDimensions().iterator();
    while (iter.hasNext()) {
      Dimension dim = (Dimension) iter.next();
      list.add (dimHash.get( dim.getName()));
    }

    return new NcSDGrid( v.getName(), list);
  }

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.1.1.1  2001/09/26 15:34:30  caron
   checkin beta1


 */
