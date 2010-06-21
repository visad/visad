// $Id: ProxySDGrid.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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
import java.io.IOException;
import java.io.EOFException;

/**
 * Proxy for a grid in another DODS dataset
 * @version $Revision: 1.3 $
 * @author jcaron
 */
public class ProxySDGrid extends SDGrid  {
  private DODSDataset df;
  private boolean debug = false, debugRead = false;

 /**
  * Constructor
  * @param AggDataset.DodsFile df: the other dataset
  * @param DGrid org: original grid
  */
  ProxySDGrid(DODSDataset df, DGrid org) {
    super(org.getName());

    try {
      int n = org.elementCount( false);

      for (int i=0; i<n; i++) {
        DArray v = (DArray) org.getVar(i);

          // should pull the maps out of a pool
        SDArray sda = new ProxySDArray( df, v);
        addVariable( sda, (i==0) ? DGrid.ARRAY : DGrid.MAPS);

        if (debug) {
          System.out.print("ProxySDGrid addVariable = ");
          v.printDecl( System.out);
        }
      }

    } catch (NoSuchVariableException e) {
      System.out.println("AggSDGrid "+e);
    }
  }

  public boolean read(String datasetName, Object specialO) throws NoSuchVariableException,
    IOException, EOFException {

    java.util.Enumeration vars = getVariables();
    while (vars.hasMoreElements()) {
      SDArray bt = (SDArray) vars.nextElement();
      bt.read( datasetName, specialO);
    }

    setRead(true);
    return(false);
  }
}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.1.1.1  2001/09/26 15:36:47  caron
   checkin beta1


 */
