//
// McIDASGridDirectory.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

import edu.wisc.ssec.mcidas.*;
import visad.*;
import visad.data.units.Parser;
import visad.data.units.*;
import visad.jmet.*;


/**
 * McIDASGridDirectory for McIDAS 'grid' directory entries
 *
 * @author Tom Whittaker
 *
 */
public class McIDASGridDirectory extends visad.jmet.MetGridDirectory {

  CoordinateSystem coordSystem = null;
  GridDirectory directory = null;
  private double paramScale;

  /**
   * Construct a McIDASGridDirectory from a GridDirectory
   * @param directory  the grid directory  cannot be null
   */
  public McIDASGridDirectory(GridDirectory directory)
  {
    this.directory = directory;
    if (directory != null) setParameters();
  }
  
  /**
   * Construct a McIDASGridDirectory from the byte representation of a
   * the McIDAS grid directory block
   * @param h  header as a byte array
   */
  public McIDASGridDirectory(byte[] h) {
    int[] dirblock = new int[64];
    for (int i = 0; i < 64; i++) dirblock[i] = McIDASUtil.bytesToInteger(h,i*4);
    try {
      directory = new GridDirectory(dirblock);
    } catch (McIDASException excp) { 
      directory = null; 
    }
    if (directory != null) setParameters();
  }

  private void setParameters() {
 
    paramName = directory.getParamName();
    rows = directory.getRows();
    columns = directory.getColumns();
    levels = 1;
    validHour = directory.getForecastHour();
    referenceTime = directory.getReferenceTime();
    validTime = directory.getValidTime();
    levelValue = directory.getLevelValue();

    MetUnits mu = new MetUnits();
    String su = directory.getParamUnitName();
    String sl = directory.getLevelUnitName();
    try {
      paramUnit = Parser.parse(mu.makeSymbol(su));
    } catch (ParseException pe) {
      paramUnit = null;
    }
    try {
      levelUnit = Parser.parse(mu.makeSymbol(sl));
    } catch (ParseException pe) {
      levelUnit = null;
    }

    secondLevelValue = directory.getSecondLevelValue();
    secondTime = directory.getSecondTime();
    paramScale = directory.getParamScale();

  }

  /**
   * Get the scale of the parameter values
   * @return parameter scale  (power of 10)
   */
  public double getParamScale() {
    return paramScale;
  }

  /**
   * Get the GRIDCoordinateSystem associated with this grid
   * @return coordinate system  may be null if nav is unknown.
   */
  public CoordinateSystem getCoordinateSystem() {
    if (coordSystem == null) {
      try {
        if (directory == null) throw new Exception("null directory");
        coordSystem = new GRIDCoordinateSystem(directory);
      } catch (Exception ev) { 
        coordSystem = null;
        System.out.println("No navigation available");
      }
    }
    return coordSystem;
  }

  /**
   * Get the raw navigation block from the directory.
   * @return  raw int values  vary with grid type
   */
  public int[] getNavBlock() {
     return (directory != null) ? directory.getNavBlock() : null;
  }

  /**
   * Get the raw navigation block from the directory.
   * @return  raw int values  vary with grid type (-1 == unknown)
   */
  public int getGridType() {
    return (directory != null) ? directory.getNavType() : -1;
  }

  /**
   * Get the grid directory used to create this beast.
   * @return corresponding edu.wisc.ssec.mcidas.GridDirectory. (may be null)
   */
  public GridDirectory getGridDirectory() {
    return directory;
  }

  /**
   * Return a String representation of the McIDASGridDirectory
   */
  public String toString() {
    return new String(paramName + " "+paramUnit+" "+rows+" "+
    columns+" "+
    levelValue+" "+levelUnit+" "+
    referenceTime.toGMTString()+ " "+ (int) validHour
    + " or "+validTime.toGMTString() );
  }

}
