// $Id: Agg1SDArray.java,v 1.4 2007-08-27 20:13:04 brucef Exp $
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
 * Aggregates an SDArray distributed across multiple datasets (type 1), where
 * the outer dimension is synthesized, and there is one AggFile per step.
 *
 * @version $Revision: 1.4 $
 * @author jcaron
 */
public class Agg1SDArray extends SDArray  {
  private boolean debugRead = false, debugWrite = false;

  private AggDataset aggD;

 /**
  * Constructor
  * @param DArray template: template for Array, except for connectCoord
  * @param SDArray connectCoord:  coord dim that is added
  * @param AggDataset aggD: need access to the AggDataset
  */
  Agg1SDArray(DArray template, SDArray connectCoord, AggDataset aggD) {
    super(template.getName());
    this.aggD = aggD;

    // connect dim always first
    java.util.Enumeration enumx = connectCoord.getDimensions();
    DArrayDimension dd = (DArrayDimension) enumx.nextElement();
    appendDim(dd.getSize(), dd.getName());

    // other dimensions
    java.util.Enumeration dims = template.getDimensions();
    while (dims.hasMoreElements()) {
      dd = (DArrayDimension) dims.nextElement();
      appendDim(dd.getSize(), dd.getName());
    }

    // set the data type
    PrimitiveVector pv = template.getPrimitiveVector();
    addVariable( ProxySDArray.assignBaseType( pv.getTemplate()));
  }

  public boolean read(String datasetName, Object specialO) throws IOException, EOFException {
    try {
      System.out.println("Agg1SDArray  DONT CALL THIS DIRECTLY "+getName());
      throw new IllegalStateException();
    } catch (Exception e) { e.printStackTrace(); }

    setRead(true);
    return (false);
  }

  public void serialize(String dataset, DataOutputStream sink, CEEvaluator ce, Object specialO)
    throws NoSuchVariableException, SDODSException, IOException {

    if(!ce.evalClauses(specialO))
      return;

    // calc total number elements in this projection
    int total_length = 1;
    for (int j=0; j< numDimensions(); j++) {
      int length = 1 + (getStop(j) - getStart(j)) / getStride(j);
      total_length *= length;
    }
    sink.writeInt(total_length);
    sink.writeInt(total_length);

    // ALWAYS JOINED ON FIRST DIMENSION !!!
    if (debugWrite) {
      System.out.println("---Agg1SDArray "+getName()+" = "+ getTypeName());
      System.out.println(" start = "+ getStart(0)+ "; stop  = "+getStop(0)+
      "; stride  = "+getStride(0));
      System.out.println(" serialize length = "+total_length);
    }

    //iterate over the actual datasets to get the component Arrays
    ArrayList dfiles = aggD.getAggFiles();
    int stride0 = (getStride(0) > 1) ? getStride(0) : 1;
    for ( int i=getStart(0); i<=getStop(0); i+=stride0) {
      AggDataset.AggFile af = (AggDataset.AggFile) dfiles.get(i);
      if (debugRead) System.out.println("  acquire file "+ af.getName());

      try {
        af.acquire(true);
        SDArray sda = af.getVariable( getName());

        // set the projection on the inner dimensions
        for (int j=1; j< numDimensions(); j++) {
          if (debugRead) System.out.println("   dim "+ j+" "+ getStart(j)+ ":"+getStride(j)+
            ":"+getStop(j));
          sda.setProjection( j-1, getStart(j), getStride(j), getStop(j));
        }

        // read the array, write it out
        sda.read(null, null);
        PrimitiveVector data = sda.getPrimitiveVector();

        if (data instanceof BytePrimitiveVector) { // have to avoid padding in BytePrimitiveVector
          BytePrimitiveVector bdata = (BytePrimitiveVector) data;
          for(int ix=0; ix<bdata.getLength(); ix++)
            sink.writeByte(bdata.getValue(ix));
        } else {
          data.externalize(sink);  // everyone else is ok
        }

        if (debugWrite) {
          System.out.println("   data len read= "+ data.getLength());
          System.out.println("   data type = "+ data.getClass().getName());
        }

      } finally {
        // release file lock
        if (af != null) af.release();
      }

    }

    // pad bytes if needed
    if (getPrimitiveVector() instanceof BytePrimitiveVector) {
      int modFour = total_length % 4;
      // pad out to a multiple of four bytes
      int pad = (modFour != 0) ? (4-modFour) : 0;
      for(int ix=0; ix<pad; ix++)
        sink.writeByte(0);
    }


    if (debugWrite) System.out.println("---"+getName()+" Agg1SDArray done ");
  }

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.3  2004/02/06 15:23:49  donm
   update to 1.1.4

   Revision 1.2  2002/02/25 15:50:12  caron
   byte array padding

   Revision 1.1.1.1  2001/09/26 15:36:46  caron
   checkin beta1


 */