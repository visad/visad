//
// SSCellChangeEvent.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

package visad.ss;

/**
 * Event class for SSCell changes.
 */
public class SSCellChangeEvent {

  /**
   * Indicates that the cell's data has changed.
   */
  public static int DATA_CHANGE = 0;

  /**
   * Indicates that the cell's display has changed.
   */
  public static int DISPLAY_CHANGE = 1;

  /**
   * Indicates that the cell's dimension has changed.
   */
  public static int DIMENSION_CHANGE = 2;

  /**
   * The cell that changed.
   */
  private BasicSSCell SSCell;

  /**
   * The type of change that occurred.
   */
  private int ChangeType;

  /**
   * If data changed, the variable name of that data.
   */
  private String VarName;

  /**
   * Constructs an SSCellChangeEvent.
   */
  public SSCellChangeEvent(BasicSSCell ssCell, int changeType) {
    this(ssCell, changeType, null);
  }

  public SSCellChangeEvent(BasicSSCell ssCell, int changeType,
    String varName)
  {
    SSCell = ssCell;
    ChangeType = changeType;
    VarName = varName;
  }

  /**
   * Gets the cell that changed.
   */
  public BasicSSCell getSSCell() {
    return SSCell;
  }

  /**
   * Gets the type of change that occurred.
   */
  public int getChangeType() {
    return ChangeType;
  }

  /**
   * Gets the variable name for the data that has changed.
   */
  public String getVariableName() {
    return VarName;
  }

}
