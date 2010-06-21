// $Id: AggSDGrid.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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
 * Aggregates an SDGrid distributed across multiple datasets (type 1 or 3)
 *
 * @version $Revision: 1.3 $
 * @author jcaron
 */
public class AggSDGrid extends SDGrid {
  private boolean debug = false, debugRead = false;

  // type 1 aggregation
  public AggSDGrid( DGrid template, SDArray connectCoord, AggDataset aggDS, Dataset defDS) {
    super(template.getName());

    try {
      int n = template.elementCount( false);
      DArray v = (DArray) template.getVar(0);
      addVariable( new Agg1SDArray( v, connectCoord, aggDS), DGrid.ARRAY );

      addVariable( connectCoord, DGrid.MAPS);

      // should pull the maps out of a pool ??
      boolean isRemote = (defDS instanceof DODSDataset); // BARF
      for (int i=1; i<n; i++) {
        DArray map = (DArray) template.getVar(i);

        if (isRemote)
          addVariable( new ProxySDArray( (DODSDataset) defDS, map), DGrid.MAPS);
        else
          addVariable( (SDArray) map, DGrid.MAPS); // cast here so bombs early if bad
      }

    } catch (NoSuchVariableException e) {
      System.out.println("AggSDGrid "+e);
    }

  }

  // type 3 aggregation
  public AggSDGrid( DGrid template, DArrayDimension connectDD, AggDataset aggDS, Dataset defDS) {
    super(template.getName());

    try {
      int n = template.elementCount( false);

      DArray v = (DArray) template.getVar(0);
      addVariable( new Agg3SDArray( v, connectDD, aggDS), DGrid.ARRAY );

      // should pull the maps out of a pool ??
      boolean isRemote = (defDS instanceof DODSDataset); // BARF
      for (int i=1; i<n; i++) {
        DArray map = (DArray) template.getVar(i);

        if (map.getName().equals( connectDD.getName()))
          addVariable( new Agg3SDArray( map, connectDD, aggDS), DGrid.MAPS);
        else if (isRemote)
          addVariable( new ProxySDArray( (DODSDataset) defDS, map), DGrid.MAPS);
        else
          addVariable( (SDArray) map, DGrid.MAPS); // cast here so bombs early if bad

      }

    } catch (NoSuchVariableException e) {
      System.out.println("AggSDGrid "+e);
    }

  }

  public boolean read(String datasetName, Object specialO) throws NoSuchVariableException,
    IOException, EOFException {

    // dont do nuthin

    setRead(true);
    return (false);
  }

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.1.1.1  2001/09/26 15:36:47  caron
   checkin beta1


 */
