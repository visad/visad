//
// MetGrid.java
//

/*

The software in this file is Copyright(C) 2019 by Tom Whittaker.
It is designed to be used with the VisAD system for interactive
analysis and visualization of numerical data.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.jmet;

import visad.*;
import java.util.*;

/**
 * defines a 2D grid for meteorology.  This needs to be expanded
 *
 * @author Tom Whittaker
 */
public class MetGrid {

  protected MetGridDirectory dir;
  protected FlatField data;
  protected CoordinateSystem coordinateSystem;
  protected Date referenceTime;
  protected Date validTime;

  RealTupleType grid_domain;
  Integer2DSet dom_set;

  /**
  * @param d - the MetGridDirectory for this data
  * @param gpData - the array of data values, ordered
  * as (x,y) with x varying fastest.
  *
  */
  public MetGrid( MetGridDirectory d, double[] gpData) {

    coordinateSystem = d.getCoordinateSystem();
    dir = d;

    try {
      RealType Nx = RealType.getRealType("Nx");
      RealType  Ny = RealType.getRealType("Ny");

      String param = d.getParamName().trim();
      RealType value_type = RealType.getRealType(param);

      RealType [] domain_components = {Nx,Ny};
      grid_domain =
             new RealTupleType(domain_components, coordinateSystem, null);
      dom_set = new Integer2DSet(grid_domain, dir.getColumns(), dir.getRows());
      FunctionType grid_type = new FunctionType(grid_domain, value_type);
      data = new FlatField(grid_type, dom_set);
      double [][] gdata = new double[1][];
      gdata[0] = gpData;
      data.setSamples(gdata,false);

    } catch (Exception e) {System.out.println(e);}

  }

  /** return the DomainSet for this grid
  *
  */
  public Integer2DSet getDomainSet() {
    return dom_set;
  }

  /** return the MetGridDirectory for this grid
  */
  public MetGridDirectory getGridDirectory() {
    return dir;
  }

  /** return the CoordinateSystem for this grid (could also get from
  *   the MetGridDirectory, too)
  */
  public CoordinateSystem getCoordinateSystem() {
    return coordinateSystem;
  }

  /** return the DataImpl (FlatField) for this grid
  */
  public DataImpl getData() {
    return data;
  }


}
