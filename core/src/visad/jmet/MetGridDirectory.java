//
// MetGridDirectory.java
//

/*

The software in this file is Copyright(C) 2017 by Tom Whittaker.
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
import java.util.*;
import visad.*;
import visad.Unit;

/**
 * defines an abstract grid directory (meta data)
 *
 * @author Tom Whittaker
 */
public abstract class MetGridDirectory {
  protected String paramName;
  protected String paramLongName;
  protected Unit paramUnit;
  protected Date referenceTime;
  protected Date validTime;
  protected Date secondTime;
  protected double validHour;
  protected CoordinateSystem coordSystem;
  protected double levelValue;
  protected Unit levelUnit;
  protected String levelName;
  protected double secondLevelValue;
  protected String secondLevelName;
  protected int rows, columns, levels;
  protected RealType xType, yType;

  public String getParamName() {
    return paramName;
  }

  public String getParamLongName() {
    return paramLongName;
  }

  public Unit getParamUnit() {
    return paramUnit;
  }

  public Date getReferenceTime() {
    return referenceTime;
  }

  public Date getValidTime() {
    return validTime;
  }

  public Date getSecondTime() {
    return secondTime;
  }

  public double getValidHour() {
    return validHour;
  }

  public abstract CoordinateSystem getCoordinateSystem();

  public double getLevelValue() {
    return levelValue;
  }

  public Unit getLevelUnit() {
    return levelUnit;
  }

  public String getLevelName() {
    return levelName;
  }

  public double getSecondLevelValue() {
    return secondLevelValue;
  }

  public String getSecondLevelName() {
    return secondLevelName;
  }

  public int getRows() {
    return rows;
  }

  public int getColumns() {
    return columns;
  }

  public int getLevels() {
    return levels;
  }

}
