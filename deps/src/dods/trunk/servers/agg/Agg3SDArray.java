// $Id: Agg3SDArray.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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
 * Aggregates an SDArray distributed across multiple datasets (type 3), where
 *  the connect dimension already exists.
 *
 * @version $Revision: 1.3 $
 * @author jcaron
 */
public class Agg3SDArray extends SDArray  {
  private boolean debug = false, debugRead = false, debugReadIndiv = false;
  private AggDataset aggD;

 /**
  * Constructor
  *
  * @param DArray template: template for Array, except for connectDim
  * @param DArrayDimension connectDim: dim to connect on
  * @param AggDataset aggD: need access to the AggDataset
  */
  Agg3SDArray(DArray template, DArrayDimension connectDim, AggDataset aggD) {
    super(template.getName());
    this.aggD = aggD;

    // set dimensions
    java.util.Enumeration dims = template.getDimensions();
    while (dims.hasMoreElements()) {
      DArrayDimension dd = (DArrayDimension) dims.nextElement();
      if (dd.getName().equals(connectDim.getName()))
        appendDim(connectDim.getSize(), connectDim.getName());
      else {
        appendDim(dd.getSize(), dd.getName());
      }
    }

    // set the data type
    PrimitiveVector pv = template.getPrimitiveVector();
    addVariable( ProxySDArray.assignBaseType( pv.getTemplate()));
  }

  public boolean read(String datasetName, Object specialO) throws IOException, EOFException {
    try {
      System.out.println("Agg3SDArray  DONT CALL THIS DIRECTLY "+getName());
      throw new IllegalStateException();
    } catch (Exception e) { e.printStackTrace(); }

    setRead(true);
    return (false);
  }

  public void serialize(String dataset, DataOutputStream sink, CEEvaluator ce, Object specialO)
    throws NoSuchVariableException, SDODSException, IOException {

    if (!ce.evalClauses(specialO))
      return; // not in the CE

    // calc total number elements in this projection
    int total_length = 1;
    for (int j=0; j< numDimensions(); j++) {
      int length = 1 + (getStop(j) - getStart(j)) / getStride(j);
      total_length *= length;
    }
    sink.writeInt(total_length);
    sink.writeInt(total_length);
    if (debug) System.out.println("Agg3SDArray serialize length = "+total_length);

    if (debugRead) {
      System.out.println("---"+getName()+" == "+ this);
      System.out.println(" Agg3SDArray start = "+ getStart(0)+ "; stop  = "+getStop(0)+
      "; stride "+getStride(0));
    }

    // iterate over the actual datasets to get the component Arrays
    int count = 0;
    Iterator dfiles = aggD.getAggFiles().iterator();
    AggDataset.AggFile af = null;
    while(dfiles.hasNext()) {
      af = (AggDataset.AggFile) dfiles.next();
      if (!af.isNeeded(getStart(0), getStride(0), getStop(0)))
        continue;

      // need to use this one
      SDArray sda = null;
      try {
        af.acquire(true);
        //if (!af.isLocked()) System.out.println(" HEY!!!! Agg3SDArray not locked == "+af.getName());

        sda = af.getVariable( getName());
        sda = (SDArray) sda.clone();

        // set the projection on the outer dimension
        if (af.setProjection0( sda, getStart(0), getStride(0), getStop(0))) {

          if (debugReadIndiv) System.out.println("  read from "+af.getName());

          // set the projection on the inner dimensions
          for (int j=1; j< numDimensions(); j++)
            sda.setProjection( j, getStart(j), getStride(j), getStop(j));

          // read the array, write it out
          sda.read(null, null);
          PrimitiveVector data = sda.getPrimitiveVector();

          if (data instanceof BytePrimitiveVector) { // have to avoid padding in BytePrimitiveVector
            BytePrimitiveVector bdata = (BytePrimitiveVector) data;
            for (int i=0; i<bdata.getLength(); i++)
              sink.writeByte(bdata.getValue(i));
          } else {
            data.externalize(sink);  // everyone else is ok
          }

        }

      } catch (RuntimeException e) {
        System.out.println(" Agg3SDArray read from "+af.getName()+" "+getName()+" "+e);
        e.printStackTrace();
        System.out.println("  sda = ");
        sda.printDecl(System.out);

        //System.out.println(" locked "+af.isLocked());
        // aggD.check( af); // debug
        throw new IOException( e.getMessage());

      } finally {
        // release file lock
        if (af != null) af.release();
      }

    } // loop over AggFiles


    // pad bytes if needed
    if (getPrimitiveVector() instanceof BytePrimitiveVector) {
      int modFour = total_length % 4;
      // pad out to a multiple of four bytes
      int pad = (modFour != 0) ? (4-modFour) : 0;
      for(int i=0; i<pad; i++)
        sink.writeByte(0);
    }

    if (debugRead) System.out.println("---"+getName()+" Agg3SDArray done ");

  }


}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.3  2002/02/25 15:50:12  caron
   byte array padding

   Revision 1.2  2001/10/26 19:07:10  caron
   getClientDDS()

   Revision 1.1.1.1  2001/09/26 15:36:46  caron
   checkin beta1


 */
