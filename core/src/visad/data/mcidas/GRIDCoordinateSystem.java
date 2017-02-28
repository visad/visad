//
// GRIDCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.mcidas;

import visad.*;
import edu.wisc.ssec.mcidas.*;
import java.awt.geom.Rectangle2D;

/**
 * GRIDCoordinateSystem is the VisAD CoordinateSystem class
 * for conversions to/from (Latitude, Longitude) and Cartesian (col,row),
 * and with Latitude and Longitude in degrees.
 */
public class GRIDCoordinateSystem
    extends visad.georef.MapProjection {

  private GRIDnav gnav = null;
  private int rows;
  private int columns;
  private int[] dirBlock;

  private static Unit[] coordinate_system_units =
    {null, null};

  /** 
    * create a GRID coordinate system from a GridDirectory
    *
    * @param gridDirectory  directory to use
    */
  public GRIDCoordinateSystem(GridDirectory gridDirectory)
        throws VisADException
  {
    this(gridDirectory.getDirBlock());
  }

  /** 
    * create a GRID coordinate system from the GRID's
    * directory block;
    *
    * @param dirBlock the grid's directory block
    */
  public GRIDCoordinateSystem(int[] dirBlock)
        throws VisADException
  {
    super(RealTupleType.LatitudeLongitudeTuple, coordinate_system_units);
    rows = dirBlock[GridDirectory.ROWS_INDEX];
    columns = dirBlock[GridDirectory.COLS_INDEX];
    try
    {
      gnav = new GRIDnav(dirBlock);
      gnav.setStart(0,0);
      gnav.setFlipRowCoordinates(rows);
    }
    catch (McIDASException excp)
    {
      throw new VisADException("Grid cannot be navigated", excp);
    }
    this.dirBlock = dirBlock;
  }

  /**
   * Converts grid xy (col,row) to latitude/longitude
   * @param rowcol  array containing the col/row pairs
   * @return array containing the corresponding lat/lon pairs
   * @throws  VisADException if input is invalid or there is no nav module
   */
  public double[][] toReference(double[][] rowcol) 
        throws VisADException {
    if (rowcol == null || rowcol.length != 2) {
      throw new CoordinateSystemException("GRIDCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    if (gnav == null) {
      throw new CoordinateSystemException("GRID navigation data not available");
    }
    return gnav.toLatLon(rowcol);
  }

  /**
   * Converts lat/lon to grid xy (col,row)
   * @param  latlon  array containing the corresponding lat/lon pairs
   * @return array containing the col/row pairs
   * @throws  VisADException if input is invalid or there is no nav module
   */
  public double[][] fromReference(double[][] latlon) 
        throws VisADException {
    if (latlon == null || latlon.length != 2) {
      throw new CoordinateSystemException("GRIDCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    if (gnav == null) {
      throw new CoordinateSystemException("GRID navigation data not available");
    }
    return gnav.toRowCol(latlon);
  }

  /**
   * Get the bounds for this grid
   */
  public Rectangle2D getDefaultMapArea() 
  { 
      return new Rectangle2D.Double(0.0, 0.0, columns, rows);
  }

  /**
   * Get the directory block used to initialize this GRIDCoordinateSystem
   */
  public int[] getDirBlock()
  { 
      return dirBlock;
  }

  /**
   * Determines whether or not the <code>Object</code> in question is
   * the same as this <code>AREACoordinateSystem</code>.  The specified
   * <code>Object</code> is equal to this <CODE>GRIDCoordinateSystem</CODE>
   * if it is an instance of <CODE>GRIDCoordinateSystem</CODE> and it has
   * the same navigation module and default map area as this one.
   *
   * @param obj the Object in question
   */
  public boolean equals(Object obj)
  {
    if (!(obj instanceof GRIDCoordinateSystem))
        return false;
    GRIDCoordinateSystem that = (GRIDCoordinateSystem) obj;
    return this == that ||
           (gnav.equals(that.gnav) && 
           this.rows == that.rows &&
           this.columns == that.columns);
  }
}
