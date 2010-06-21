// $Id: MemSDArray.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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
 * An SDArray with its values stored in memory
 * @version $Revision: 1.3 $
 * @author jcaron
 */
public class MemSDArray extends SDArray  {
  private boolean isString;
  private boolean debug = false;

 /**
  * Constructor
  * @param String name: name of variable
  * @param Iterator dims: DArrayDimension collection
  * @param BaseType bt: type of array
  * @param Object data: primivite java array of data
  */
  MemSDArray(String name, Collection dims, Object data ) {
    super(name);

    // set dimensions
    Iterator iter = dims.iterator();
    while (iter.hasNext()) {
      DArrayDimension dd = (DArrayDimension) iter.next();
      appendDim(dd.getSize(), dd.getName());
    }

    // set the data type // KLUDGE
    // why "add a variable" ?? why ProxySDArray ?? this seems WRONG
    isString = (data instanceof String[]);
    if (isString) {
      addVariable( new ProxySDArray.AggSDString(name));
    } else {
      addVariable( new ProxySDArray.AggSDInt32(name)); //// YOWWW!!!
    }

    // create primitive vector based on variable type, i think
    // transfer data
    PrimitiveVector pv = getPrimitiveVector();

    if (!isString)
      pv.setInternalStorage( data);
    else {  // array of strings
      String[] sdata = (String[]) data;
      BaseTypePrimitiveVector btpv = (BaseTypePrimitiveVector) pv;
      btpv.setLength( sdata.length);
      for (int i=0; i<sdata.length; i++) {
        btpv.setValue( i, new ProxySDArray.AggSDString("",sdata[i])); // wrap each one
      }
    }
  }

  /** Read the value from original dataset (parameters are ignored).*/
  public boolean read(String datasetName, Object specialO) throws IOException, EOFException {
    setRead(true);
    return (false);
  }

  public void serialize(String dataset, DataOutputStream sink, CEEvaluator ce, Object specialO)
      throws NoSuchVariableException,SDODSException, IOException {

    if (!ce.evalClauses(specialO))
      return; // not in the CE

    // calc total number elements in this projection
    int total_length = 1;
    for (int j=0; j< numDimensions(); j++) {
      int length = 1 + (getStop(j) - getStart(j)) / getStride(j);
      total_length *= length;
    }

    if (debug) {
      System.out.println("---MemSDArray "+getName()+" serialize length = "+total_length);
      for (int j=0; j< numDimensions(); j++) {
        System.out.println(" start = "+ getStart(j)+ "; stop  = "+getStop(j)+
        "; stride "+getStride(j));
      }
    }

    PrimitiveVector data = getPrimitiveVector().subset(getStart(0), getStop(0), getStride(0));
    if (debug) {
      System.out.println("   PrimitiveVector len = "+ data.getLength());
      System.out.println("   PrimitiveVector type = "+ data.getClass().getName());
    }

    sink.writeInt(total_length);
    sink.writeInt(total_length);
    data.externalize( sink);
  }


}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.2  2002/02/25 15:47:32  caron
   add serialize(), default doesnt handle subsets

   Revision 1.1.1.1  2001/09/26 15:36:47  caron
   checkin beta1


 */
