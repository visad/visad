/*
 * @(#)MapTool.java    3.0 99/09/06 Jonathan Callahan
 *   Moved user_X and user_Y from MapCanvas to the MapTool.
 * @(#)MapTool.java    2.3 97/10/13 Jonathan Callahan
 * @(#)MapTool.java    1.0 96/09/26 Jonathan Callahan
 *
 *
 *  This software was developed by the Thermal Modeling and Analysis
 *  Project(TMAP) of the National Oceanographic and Atmospheric
 *  Administration's (NOAA) Pacific Marine Environmental Lab(PMEL),
 *  hereafter referred to as NOAA/PMEL/TMAP.
 *
 *  Access and use of this software shall impose the following
 *  obligations and understandings on the user. The user is granted the
 *  right, without any fee or cost, to use, copy, modify, alter, enhance
 *  and distribute this software, and any derivative works thereof, and
 *  its supporting documentation for any purpose whatsoever, provided
 *  that this entire notice appears in all copies of the software,
 *  derivative works and supporting documentation.  Further, the user
 *  agrees to credit NOAA/PMEL/TMAP in any publications that result from
 *  the use of this software or in any product that includes this
 *  software. The names TMAP, NOAA and/or PMEL, however, may not be used
 *  in any advertising or publicity to endorse or promote any products
 *  or commercial entity unless specific written permission is obtained
 *  from NOAA/PMEL/TMAP. The user also understands that NOAA/PMEL/TMAP
 *  is not obligated to provide the user with any support, consulting,
 *  training or assistance of any kind with regard to the use, operation
 *  and performance of this software nor to provide the user with any
 *  updates, revisions, new versions or "bug fixes".
 *
 *  THIS SOFTWARE IS PROVIDED BY NOAA/PMEL/TMAP "AS IS" AND ANY EXPRESS
 *  OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL NOAA/PMEL/TMAP BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 *  RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
 *  CONTRACT, NEGLIGENCE OR OTHER TORTUOUS ACTION, ARISING OUT OF OR IN
 *  CONNECTION WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */

package dods.clients.importwizard.TMAP.map;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Point;

//import dods.clients.importwizard.TMAP.map.MapConstants;
//import dods.clients.importwizard.TMAP.map.MapGrid;
//import dods.clients.importwizard.TMAP.map.ToolHandle;
import dods.clients.importwizard.TMAP.convert.*;
//import dods.clients.importwizard.TMAP.convert.ConvertLength;
//import dods.clients.importwizard.TMAP.convert.ConvertLongitude;

/**
 * A rectangular map tool defined by x, y, width and height.
 * This abstract superclass defines a rectangular tool with
 * nine ToolHandles (small rectangles) which allow
 * reshaping and repositioning of the tool on a MapCanvas.
 *
 * The <code>abstract</code> method which makes this class
 * abstract is the <code>draw(Graphics g)</code> method.
 *
 * @version     3.0 Sept 09 1999
 * @author      Jonathan Callahan
 */
public abstract class MapTool extends Rectangle 
	implements MapConstants
{

  /*
   * These constants are for internal use only and are
   * not part of the API.
   */
  static final int NW = 0;
  static final int N  = 1;
  static final int NE = 2;
  static final int E  = 3;
  static final int SE = 4;
  static final int S  = 5;
  static final int SW = 6;
  static final int W  = 7;
  static final int C  = 8;

  /*
   * These flags are set when the tool bumps up against the edges and are watched by the
   * MapScroller which then takes the appropriate action. (Internal, not in API.)
   */
  boolean pan_down = false;
  boolean pan_down_fast = false;
  boolean pan_left = false;
  boolean pan_left_fast = false;
  boolean pan_right = false;
  boolean pan_right_fast = false;
  boolean pan_up = false;
  boolean pan_up_fast = false;

  /*
   * Additional flags set in applyClipRect which tell bump_against_sides()
   * whether scrolling should be invoked. (Internal, not in API.)
   */
  protected boolean left_edge_scroll = true;
  protected boolean right_edge_scroll = true;
  protected boolean top_edge_scroll = true;
  protected boolean bottom_edge_scroll = true;

  /**
   * Current "user" values for the left, middle and right edges of this tool.
   * <p>
   * Access these values with <code>LO</code>, <code>MID</code> 
   * or <code>HI</code> as in: <code>user_X[LO]</code>.<br>
   * <b>DO NOT SET THESE VALUES.</b>  They should only be read.
   */
  public double [] user_X = new double[3];

  /**
   * Current "user" values for the bottom, middle and top edges of this tool.
   * <p>
   * Access these values with <code>LO</code>, <code>MID</code>
   * or <code>HI</code> as in: <code>user_Y[LO]</code>.<br>
   * <b>DO NOT SET THESE VALUES.</b>  They should only be read.
   */
  public double [] user_Y = new double[3];


  /**
   * Range for this tool in "user" values.  The tool will be
   * restricted to movement within this range.
   * <p>
   * Access these values with <code>LO</code>
   * or <code>HI</code> as in: <code>range_X[LO]</code>.<br>
   */
  public double [] range_X = new double[2];

  /**
   * Range for this tool in "user" values.  The tool will be
   * restricted to movement within this range.
   * <p>
   * Access these values with <code>LO</code>
   * or <code>HI</code> as in: <code>range_Y[LO]</code>.<br>
   */
  public double [] range_Y = new double[2];


  /**
   * Delta for this tool in "user" values.  This determins
   * the grid spacing for this tool along the X axis and is
   * used for grid snapping.
   *
   * A value of 0.0 tells the MapCanvas to use the default
   * value calculated by MapGrid.setDomain_X.
   */
  public double delta_X = 0.0;

  /**
   * Delta for this tool in "user" values.  This determins
   * the grid spacing for this tool along the Y axis and is
   * used for grid snapping.
   *
   * A value of 0.0 tells the MapCanvas to use the default
   * value calculated by MapGrid.setDomain_Y.
   */
  public double delta_Y = 0.0;


  /**
   * Whether the tool handles should be drawn or not.
   */
  protected boolean drawHandles = false;
 
  /**
   * Whether the tool snaps to the underlying grid or not.
   */
  protected boolean snap_X = false;
 
  /**
   * Whether the tool snaps to the underlying grid or not.
   */
  protected boolean snap_Y = false;
 
  /**
   * Whether or not the tool should always have a range of
   * values along the X axis.
   */
  public boolean needsRange_X = true;
 
  /**
   * Whether or not the tool should always have a range of
   * values along the Y axis.
   */
  public boolean needsRange_Y = true;
 
  /**
   * The grid on which this tool acts.  This is just a reference to 
   * the MapGrid associated with the MapCanvas.
   */
  public MapGrid grid;
 
  /**
   * The bounding rectangle in which this tool moves freely.
   *
   * The boundingRect is the intersection of the
   * canvas_clipRect and the tool X and Y ranges.  The
   * boundingRect defines the area of free movement
   * before the tool either 1) bumps against a side or 
   * 2) causes the image to scroll.
   */
  protected Rectangle boundingRect;
 
  /**
   * The area of the map canvas occupied by the map.  The
   * bounding rectangle will be some rectangular subset of this area.
   */
  protected Rectangle canvas_clipRect;
 
  /**
   * The color of the tool.
   */
  protected Color color;
 
  /**
   * Returns <code>true</code> if the tool is "active".
   * An "active" tool is one that is currently being dragged
   * or resized by the mouse.
   */
  protected boolean active = false;
 
  /**
   * The array of handles for this tool.
   */
  protected ToolHandle [] handle;
 
  /**
   * The number of handles in this tool.
   */
  protected int numHandles = 9;
 
  /**
   * The type of the active handle (eg. <code>NW</code>).
   */
  private int selectedHandle;
 
  /**
   * The handle type to return when mouseDown doesn't activate 
   * one of the handles.(eg. <code>NW</code>).
   */
  protected int mouseDownHandle = NW;
 
  /**
   * If a tool has an extent along an axis, then it should snap to grid
   * midpoints on that axis IF the CENTER handle is selected AND the
   * extent is an odd number of grid cells.  Tools which have no extent
   * along an axis should always snap to grid points.
   */
  boolean snapMid_X = true;
  boolean snapMid_Y = true;
 
  /**
   * The width for the handles of this tool.
   */
  int hw=5;
 
  /**
   * The height for the handles of this tool.
   */
  int hh=5;
 
  /**
   * Constructs a new MapTool.
   */
  public MapTool() {
  }
 
  /**
   * Constructs and initializes a MapTool with the specified parameters.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param width the width of the MapTool
   * @param height the height of the MapTool
   * @param color the color of the MapTool
   */
  public MapTool(int x, int y, int width, int height, Color color) {
    setBounds(x, y, width,  height);
    setColor(color);
  }
 
  /**
   * Constructs a MapTool and initializes it with the specified parameters.
   * @param rect the rectangle of the MapTool
   * @param color the color of the MapTool
   */
  public MapTool(Rectangle rect, Color color) {
    this(rect.x, rect.y, rect.width, rect.height, color);
  }
 
  /**
   * Constructs a MapTool and initializes it with the specified  parameters.
   * @param width the width of the MapTool
   * @param height the height of the MapTool
   * @param color the color of the MapTool
   */
  public MapTool(int width, int height, Color color) {
    this(0, 0, width, height, color);
  }
 
  /**
   * Constructs a MapTool and initializes it with a specified parameters.
   * @param p the point
   * @param d dimension
   * @param color the color of the MapTool
   */
  public MapTool(Point p, Dimension d, Color color) {
    this(p.x, p.y, d.width, d.height, color);
  }
 
  /**
   * Constructs a MapTool and initializes it with the specified parameters.
   * @param p the value of the x and y coordinate
   * @param color the color of the MapTool
   */
  public MapTool(Point p, Color color) {
    this(p.x, p.y, 0, 0, color);
  }
 
  /**
   * Constructs a MapTool and initializes it with the specified parameters.
   * @param d the value of the width and height
   * @param color the color of the MapTool
   */
  public MapTool(Dimension d, Color color) {
    this(0, 0, d.width, d.height, color);
  }

  /**
   * Returns the String representation of the tool's values.
   */
  public String toString() {
    StringBuffer sbuf = new StringBuffer(super.toString());
    sbuf.append(numHandles + " handles");
    if (active)
      sbuf.append(", tool is active");
    else
      sbuf.append(", tool is inactive");
    sbuf.append(", selected handle = " + selectedHandle);

    return sbuf.toString();
  }

  /**
   * Returns the state of the tool.
   * @return the state of the tool.
   */
  public boolean is_active() {
    return active;
  }

  /**
   * Sets the "snap to grid" state.
   * When snap_X or snap_Y is <code>true</code>, MapTools will snap to gridpoints along these axes where gridpoints are defined by the MapGrid associated with the MapCanvas.
   * @param snap_X whether snapping is in effect for the X axis.
   * @param snap_Y whether snapping is in effect for the Y axis.
   */
  public void setSnapping(boolean snap_X, boolean snap_Y) {
    this.snap_X = snap_X;
    this.snap_Y = snap_Y;
  }


  /**
   * Gets snap_X for this tool.
   */
   public boolean getSnap_X() {
     return snap_X;
   }


  /**
   * Gets snap_Y for this tool.
   */
   public boolean getSnap_Y() {
     return snap_Y;
   }


 
  /**
   * Returns the grid on which this tool acts.
   * @return the MapGrid on which this tool acts.
   */
  public MapGrid getGrid() {
    return grid;
  }

 
  /**
   * Sets the grid on which this tool acts.
   * @param new_grid the new grid
   */
  public void setGrid(MapGrid grid) {
    this.grid = grid;
  }

 
  /**
   * Sets the X range of this tool in "user" coordinates.
   * <p>
   * Note: If you wish to use setRange_X() to restrict the movement of the tool
   * then you <b>must</b> set <code>modulo_X = false</code> in the associated
   * MapGrid.  Otherwise the mapGrid boundingRect
   * restricting the tool's movement will default to the entire globe.
   * @param lo the "user" value of the low end of the data range along X.
   * @param hi the "user" value of the high end of the data range along X.
   */
  public void setRange_X(double lo, double hi) {
    range_X[LO] = lo;
    range_X[HI] = hi;
  }

  /**
   * Sets the Y range of this tool in "user" coordinates.
   * @param lo the "user" value of the low end of the data range along Y.
   * @param hi the "user" value of the highe end of the data range along Y.
   */
  public void setRange_Y(double lo, double hi) {
    range_Y[LO] = lo;
    range_Y[HI] = hi;
  }


  /**
   * Sets delta_X for this tool in "user" coordinates.  This delta_X
   * value will override the default value calculated in 
   * MapGrid.setDomain_X.
   * @param delta the "user" value of of the grid spacing along X.
   */
  public void setDelta_X(double delta) {
    delta_X = delta;
  }

  /**
   * Gets delta_X for this tool.
   */
   public double getDelta_X() {
     return delta_X;
   }


  /**
   * Sets delta_Y for this tool in "user" coordinates.  This delta_X
   * value will override the default value calculated in 
   * MapGrid.setDomain_Y.
   * @param delta the "user" value of of the grid spacing along Y.
   */
  public void setDelta_Y(double delta) {
    delta_Y = delta;
  }

  /**
   * Gets delta_Y for this tool.
   */
   public double getDelta_Y() {
     return delta_Y;
   }


  /**
   * Resizes the tool to the intersection of the current tool 
   * rectangle and the specified rectangle.
   *
   * @param x the x location in pixels
   * @param y the y location in pixels
   * @param width the width in pixels
   * @param height the height in pixels
   * @see Rectangle
   */
  public void intersect(int x, int y, int width , int height) {
    Rectangle rect = new Rectangle(x, y, width, height);
    Rectangle newRect = super.intersection(rect);
    this.setBounds(newRect);
  }


  /**
   * Moves the tool to a new x, y location interpreting
   * x and y as pixels.
   * @param x the x location in pixels
   * @param y the y location in pixels
   * @see Rectangle
   */
  public void setLocation(int x, int y) {
    this.x = x;
    this.y = y;
    this.saveHandles();
  }


  /**
   * Moves the tool to a new x, y location interpreting
   * x and y as user values on this tools grid.
   * @param x the x location in user values
   * @param y the y location in user values
   * @see Rectangle
// JC_TODO: This method should do the same checks found in 
// JC_TODO: setUserBounds().
   */
  public void setUserLocation(double x, double y) {
    setLocation(grid.userToPixel_X(x), grid.userToPixel_Y(y));
  }


  /**
   * Reshapes the tool using a rectangle specified in pixel values
   * @param rect a rectangle specified in pixels
   * @see Rectangle
   */
  public void setBounds(Rectangle rect) {
    setBounds(rect.x, rect.y, rect.width, rect.height);
  }


  /**
   * Reshapes the tool using pixel values
   * @param x the x location in pixels
   * @param y the y location in pixels
   * @param width the width in pixels
   * @param height the height in pixels
   * @see Rectangle
   */
  public void setBounds(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.saveHandles();
  }


  /**
   * Reshapes the tool using user values
   * @param left the left in user values
   * @param right the right in user values
   * @param bottom the bottom location in user values
   * @param top the top location in user values
   * @see Rectangle
   *
   * This method of reshaping the tool does checks against the
   * tool's associated grid to make sure the tool stays within
   * the range and domain.
   */
  public void setUserBounds(double left, double right, double bottom, double top) {

    int width=0, height=0;

    // First check:
    // keep the top and bottom, left and right values within the domain of X and Y.
    //
    if ( top < grid.domain_Y[LO] ) { top = grid.domain_Y[LO]; }
    if ( top > grid.domain_Y[HI] ) { top = grid.domain_Y[HI]; }
    if ( bottom < grid.domain_Y[LO] ) { bottom = grid.domain_Y[LO]; }
    if ( bottom > grid.domain_Y[HI] ) { bottom = grid.domain_Y[HI]; }
    if ( grid.modulo_X ) {
      while (left < grid.domain_X[LO]) { left += grid.x_factor; }
      while (left > grid.domain_X[HI]) { left -= grid.x_factor; }
      while (right < grid.domain_X[LO]) { right += grid.x_factor; }
      while (right > grid.domain_X[HI]) { right -= grid.x_factor; }
    } else {
      if (left < grid.domain_X[LO]) { left = grid.domain_X[LO]; }
      if (left > grid.domain_X[HI]) { left = grid.domain_X[HI]; }
      if (right < grid.domain_X[LO]) { right = grid.x_factor; }
      if (right > grid.domain_X[HI]) { right = grid.domain_X[HI]; }
    }

    // Second check:
    // IF the X axis type is LONGITUDE_AXIS
    // AND the tool needs a range of X
    // AND left == right
    // THEN make the width 360 degrees
    //
    if ( (grid.x_type == LONGITUDE_AXIS) && needsRange_X && (left == right) ) {
      right = left + 360.0;
    }

    // Third check:
    // (right < left) is only valid for LONGITUDE_AXIS
    //
    if ( right < left ) {
      if ( grid.x_type != LONGITUDE_AXIS )
    System.out.println("ERROR in MapTool.java:serUserBounds(): " + right + " < " + left);
      if ( grid.modulo_X )
    width = (int) ( (grid.x_factor - (left-right)) * ((grid.imageRect.width-1)/grid.x_factor) );
      else
    width = (int) ( (left-right) * ((grid.imageRect.width-1)/grid.x_factor) );
    } else
      width = (int) ( (right-left) * ((grid.imageRect.width-1)/grid.x_factor) );

    height = grid.userToPixel_Y(bottom) - grid.userToPixel_Y(top);
 
    setBounds(grid.userToPixel_X(left), grid.userToPixel_Y(top), width, height);
    setUser_XY(left, right, bottom, top);
  }


  /**
   * Gets the color for this tool.
   * @return the Color of this tool.
   */
  public Color getColor() {
    return color;
  }


  /**
   * Sets the color for this tool.
   * @param color the Color of this tool.
   */
  public void setColor(Color color) {
    int i=0;

    this.color = color;
    for (i=0; i<numHandles; i++)
      handle[i].setColor(color);
  }


  /**
   * Gets the rectangle for this tool.
   * @return the Rectangle of this tool.
   */
  public Rectangle getRectangle() {
    Rectangle rect = new Rectangle(x,y,width,height);
    return rect;
  }


  /**
   * Saves the current positions of all the handles.
   */
  protected void saveHandles() {
    handle = new ToolHandle[numHandles];
    handle[0] = new ToolHandle(x-hw/2, y-hh/2, hw, hh, color, NW);
    handle[1]  = new ToolHandle(x+width/2-hw/2, y-hh/2, hw, hh, color, N);
    handle[2] = new ToolHandle(x+width-hw/2, y-hh/2, hw, hh, color, NE);
    handle[3]  = new ToolHandle(x+width-hw/2, y+height/2-hh/2, hw, hh, color, E);
    handle[4] = new ToolHandle(x+width-hw/2, y+height-hh/2, hw, hh, color, SE);
    handle[5]  = new ToolHandle(x+width/2-hw/2, y+height-hh/2, hw, hh, color, S);
    handle[6] = new ToolHandle(x-hw/2, y+height-hh/2, hw, hh, color, SW);
    handle[7]  = new ToolHandle(x-hw/2, y+height/2-hh/2, hw, hh, color, W);
    handle[8]  = new ToolHandle(x+width/2-hw/2, y+height/2-hh/2, hw, hh, color, C);
  }


  /**
   * Checks to make sure that tools which require ranges
   * along X or Y do indeed describe.
   * @returns 0 if the tool was unaltered,
   *          1 if the X axis was expanded,
   *          2 if the Y axis was expaneded.
   */
  protected int check_for_zero_range() {
    int return_val = 0;

    if ( width == 0 && needsRange_X ) {
      return_val += 1;
      if ( snap_X ) {
        int new_x = grid.snap_X(x, SNAP_ON, 1);
        if ( new_x > grid.userToPixel_X(range_X[HI]) )
          new_x = grid.snap_X(x, SNAP_ON, -1);
        if ( new_x < grid.userToPixel_X(range_X[LO]) )
          new_x = x;
        if ( new_x < x ) {
          width = x - new_x;
          x = new_x;
        } else if ( new_x > x ) {
          width = new_x - x;
        }
      } else {
        if ( x == boundingRect.x + boundingRect.width ) {
          x = x-1; width=1;
        } else {
          width=1;
        }
      }
    }

    // For the Y axis remember that pixel values increase
    // from top to bottom while user values increase from
    // bottom to top.
    // If one grid cell above and below are both outside
    // the range, return the original Y value.
    //
    if ( height == 0 && needsRange_Y ) {
      return_val += 2;
      if ( snap_Y ) {
        int new_y = grid.snap_Y(y, SNAP_ON, 1);
        if ( new_y < grid.userToPixel_Y(range_Y[HI]) )
          new_y = grid.snap_Y(y, SNAP_ON, -1);
        if ( new_y > grid.userToPixel_Y(range_Y[LO]) )
          new_y = y;
        if ( new_y < y ) {
          height = y - new_y;
          y = new_y;
        } else if ( new_y > y ) {
          height = new_y - y;
        }
      } else {
        if ( y == boundingRect.y + boundingRect.height ) {
          y = y-1; height=1;
        } else {
          height=1;
        }
      }
    }

    return return_val;

  }


  /**
   * Applies the canvas_clipRect after potential scrolling
   * so that a new boundingRect can be calculated for this tool.
   *
   * @param rect the MapCanvas clipRect rectangle.
   */
  public void applyClipRect(Rectangle rect) {
    applyClipRect(rect.x, rect.y, rect.width, rect.height);
  }


  /**
   * Sets the bounding rectangle for this tool.  MapTools are always constrained to remain
   * within the bounding rectangle.  This behavior allows for a tool to be used to select
   * data within a specified range which may be smaller than the domain represented by
   * the base image used in the MapCanvas.
   * @param c_r_x the x coordinate of the boundingRect
   * @param c_r_y the y coordinate of the boundingRect
   * @param c_r_width the width of the boundingRect.
   * @param c_r_height the height of the boundingRect.
   */
  public void applyClipRect(int c_r_x, int c_r_y, int c_r_width, int c_r_height) {
    canvas_clipRect = new Rectangle(c_r_x, c_r_y, c_r_width, c_r_height);

    // range_ values
    int r_xlo = grid.userToPixel_X(range_X[LO]);
    int r_xhi = grid.userToPixel_X(range_X[HI]);
    int r_ylo = grid.userToPixel_Y(range_Y[HI]);
    int r_yhi = grid.userToPixel_Y(range_Y[LO]);

    // clipRect values
    int c_r_xlo = c_r_x;
    int c_r_xhi = c_r_x + c_r_width;
    int c_r_ylo = c_r_y;
    int c_r_yhi = c_r_y + c_r_height;

    // new boundingRect values
    //
    // If the base image doesn't represent a domain which is modulo_X
    //    * r_lo is guaranteed to be on the left
    //    * set the bounding box to be the intersection of r_ and c_r_ values.
    // If the domain is modulo_X but we have a restricted range
    //    * set the bounding box to be the intersection of r_ and c_r_ values.
    //    If the range straddles the domain boundaries (e.g. dom=-180:180, rng=160E:90W)
    //      * test to make sure r_xlo/hi isn't off the right/left edge
    // If the domain is modulo_X and the range equals the domain
    //    use the c_r_ values.

    int b_r_xlo = c_r_xlo;
    int b_r_xhi = c_r_xhi;

    if ( !grid.modulo_X ) {

      if (c_r_xlo > r_xlo) {
        b_r_xlo = c_r_xlo;
        left_edge_scroll = true;
      } else {
        b_r_xlo = r_xlo;
        left_edge_scroll = false;
      }
      if (c_r_xhi < r_xhi) {
        b_r_xhi = c_r_xhi;
        right_edge_scroll = true;
      } else {
        b_r_xhi = r_xhi;
        right_edge_scroll = false;
      }

    } else { // The domain is modulo_X

      if ( Math.abs(range_X[HI]-range_X[LO]) < Math.abs(grid.domain_X[HI]-grid.domain_X[LO]) ) {

        // If the range is a subset of the domain, the
        // test is as expected. 

        if (c_r_xlo > r_xlo) {
          b_r_xlo = c_r_xlo;
          left_edge_scroll = true;
        } else {
          b_r_xlo = r_xlo;
          left_edge_scroll = false;
        }
        if (c_r_xhi < r_xhi) {
          b_r_xhi = c_r_xhi;
          right_edge_scroll = true;
        } else {
          b_r_xhi = r_xhi;
          right_edge_scroll = false;
        }

        // If the range straddles the domain boundaries, an 
        // additional check is needed.
        if ( (range_X[HI]-range_X[LO]) < 0 ) {
          if (b_r_xlo > c_r_xhi) {
            b_r_xlo = c_r_xlo;
            left_edge_scroll = true;
          }
          if (b_r_xhi < c_r_xlo) {
            b_r_xhi = c_r_xhi;
            right_edge_scroll = true;
          }
        }

      } else { // The range equals the domain and is modulo_X

        b_r_xlo = c_r_xlo;
        left_edge_scroll = true;
        b_r_xhi = c_r_xhi;
        right_edge_scroll = true;

      }

    }

    int b_r_ylo = c_r_ylo;
    int b_r_yhi = c_r_yhi;
    if (c_r_ylo > r_ylo) {
      b_r_ylo = c_r_ylo;
      top_edge_scroll = true;
    } else {
      b_r_ylo = r_ylo;
      top_edge_scroll = false;
    }
    if (c_r_yhi < r_yhi) {
      b_r_yhi = c_r_yhi;
      bottom_edge_scroll = true;
    } else {
      b_r_yhi = r_yhi;
      bottom_edge_scroll = false;
    }

    int b_r_width = b_r_xhi - b_r_xlo;
    int b_r_height = b_r_yhi - b_r_ylo;

 
    boundingRect = new Rectangle(b_r_xlo, b_r_ylo, b_r_width, b_r_height);
  }


  /**
   * Reshapes the tool.
   * @param mouse_x new mouse X position for one of the handles
   * @param mouse_y new mouse Y position for one of the handles
   */
  public void handle_reshape(int mouse_x, int mouse_y) {
    boolean handle_overtook_opposite_side = false;

    // First, deal with the new X position

    // Snap to grid if necessary
    if ( snap_X ) {
      mouse_x = grid.snap_X(mouse_x, SNAP_ON);
      if ( mouse_x > boundingRect.x+boundingRect.width )
        mouse_x = grid.snap_X(mouse_x, SNAP_ON, -1);
      else if ( mouse_x < boundingRect.x )  
        mouse_x = grid.snap_X(mouse_x, SNAP_ON, 1);
    }

    if ( mouse_x > x + width ) {
      switch (selectedHandle) {
      case NE:
      case E:
      case SE:
	// x = x;
	width = mouse_x - x;
	break;
      case NW:
      case W:
      case SW:
	handle_overtook_opposite_side = true;
	x = x + width; // left edge = old right edge
	width = mouse_x - x; // width = new width - new left edge
	break;
      }
    } else if ( mouse_x < x ) {
      switch (selectedHandle) {
      case NE:
      case E:
      case SE:
	handle_overtook_opposite_side = true;
	width = x - mouse_x; // width = old left edge - new width
	x = mouse_x; // left edge = new width
	break;
      case NW:
      case W:
      case SW:
	// width = old width + ( old left edge - new width )
	width = width + (x - mouse_x);
	x = mouse_x; // left edge = new width
	break;
      }
    } else {
      switch (selectedHandle) {
      case NE:
      case E:
      case SE:
	// x = x;
	width = mouse_x - x;
	break;
      case NW:
      case W:
      case SW:
	// width = old width + ( old left edge - new width )
	width = width + (x - mouse_x);
	x = mouse_x; // left edge = new width
	break;
      }
    }
	
    if ( handle_overtook_opposite_side ) {
      if ( selectedHandle == NE ) selectedHandle = NW;
      else if ( selectedHandle == E ) selectedHandle = W;
      else if ( selectedHandle == SE ) selectedHandle = SW;
      else if ( selectedHandle == NW ) selectedHandle = NE;
      else if ( selectedHandle == W ) selectedHandle = E;
      else if ( selectedHandle == SW ) selectedHandle = SE;
      handle_overtook_opposite_side = false;
    }

    // Now, deal with the new Y position

    // Snap to grid if necessary
    if ( snap_Y ) {
	  mouse_y = grid.snap_Y(mouse_y, SNAP_ON);
      if ( mouse_y > boundingRect.y+boundingRect.height )
        mouse_y = grid.snap_Y(mouse_y, SNAP_ON, 1);
      else if ( mouse_y < boundingRect.y )  
        mouse_y = grid.snap_Y(mouse_y, SNAP_ON, -1);
    }

    if ( mouse_y > y + height ) {
      switch (selectedHandle) {
      case NW:
      case N:
      case NE:
	handle_overtook_opposite_side = true;
	y = y + height; // top edge = old bottom edge
	height = mouse_y - y; // height = new height - new top edge
	break;
      case SW:
      case S:
      case SE:
	// y = y;
	height = mouse_y - y;
	break;
      }
    } else if ( mouse_y < y ) {
      switch (selectedHandle) {
      case NW:
      case N:
      case NE:
	// height = old height + ( old top edge - new height )
	height = height + (y - mouse_y);
	y = mouse_y; // top edge = new height
	break;
      case SW:
      case S:
      case SE:
	handle_overtook_opposite_side = true;
	height = y - mouse_y; // height = old top edge - new height
	y = mouse_y; // top edge - new height
	break;
      }
    } else {
      switch (selectedHandle) {
      case NW:
      case N:
      case NE:
	// height = old height + ( old top edge - new height )
	height = height + (y - mouse_y);
	y = mouse_y; // top edge = new height
	break;
      case SW:
      case S:
      case SE:
	// y = y;
	height = mouse_y - y;
	break;
      }
    }

    if ( handle_overtook_opposite_side ) {
      if ( selectedHandle == NW ) selectedHandle = SW;
      else if ( selectedHandle == N ) selectedHandle = S;
      else if ( selectedHandle == NE ) selectedHandle = SE;
      else if ( selectedHandle == SW ) selectedHandle = NW;
      else if ( selectedHandle == S ) selectedHandle = N;
      else if ( selectedHandle == SE ) selectedHandle = NE;
      handle_overtook_opposite_side = false;
    }

  }


  /**
   * Sets the user_X/Y values for this tool.
   */
  public void setUser_XY() {
    setUser_X();
    setUser_Y();
  }


  /**
   * Sets the user_X/Y values for this tool.
   */
  public void setUser_XY(double Xlo, double Xhi, double Ylo, double Yhi) {
    setUser_X(Xlo,Xhi);
    setUser_Y(Ylo,Yhi);
  }


  /**
   * Sets the user_X values for this tool.
   */
  public void setUser_X() {

    double x_lo=0.0, x_hi=0.0, x_range=0.0;
    Convert XConvert = new ConvertLongitude();

    if ( grid.x_type != LONGITUDE_AXIS )
      XConvert = new ConvertLength();

    user_X[LO] = grid.pixelToUser_X(x);
    user_X[HI] = grid.pixelToUser_X(x+width);
    user_X[PT] = grid.pixelToUser_X(x+width/2+1);

    if ( snap_X ) {
      user_X[LO] = grid.snapUser_X(user_X[LO], SNAP_ON, 0);
      user_X[HI] = grid.snapUser_X(user_X[HI], SNAP_ON, 0);
      user_X[PT] = grid.snapUser_X(user_X[PT], SNAP_ON, 0);
    }

    // First check
    // It's quite possible for the pixelToUser_ code or the
    // snapUser_ code to return a value which lies outside
    // the range.  This can happen when there are fewer pixels
    // than grid points.  If we've gotten outside the range, 
    // get back in.
    //
    // Don't forget that we might be a Longitude axes where
    // (hi < lo) is acceptable for the range or the user values. 
    // Use the logic in ConvertLongitude to check for this.

    XConvert.setRange(range_X[LO], range_X[HI]);
    user_X[LO] = XConvert.getNearestValue(user_X[LO], LO);
    user_X[HI] = XConvert.getNearestValue(user_X[HI], HI);

    // The following logic is needed in order to prevent whole-world
    // selections from appearing as single-point selections.
    if ( grid.modulo_X && (grid.x_type == LONGITUDE_AXIS) ) {
      if ( (user_X[HI] == user_X[LO]) && (width > 0) ) {
        user_X[HI] = user_X[LO] + (grid.domain_X[HI]-grid.domain_X[LO]);
      }
    }

    // Second check
    // It seems that low pixel resolution can result in the following
    // annoying bug:  A whole world selection may be slightly larger
    // than that, appearing as "140 E" to "142 E" for exampe, which
    // specifies a longitude range of 2 degrees rather than 360 which
    // the user expects.
    x_lo = user_X[LO];
    x_hi = user_X[HI];
    if ( grid.modulo_X && (grid.x_type == LONGITUDE_AXIS) ) {
      if ( x_lo < 0 ) x_lo += 360;
      if ( x_hi < 0 ) x_hi += 360;
      if ( x_hi < x_lo ) x_hi += 360;
    }
    x_range = (x_hi - x_lo);
    if ( grid.rangeToPixels_X(x_range) < (width/2) ) {
      user_X[HI] = user_X[LO] + (grid.domain_X[HI]-grid.domain_X[LO]);
    }
      

  }


  /**
   * Sets the user_Y values for this tool.
   */
  public void setUser_Y() {

    // We need to "invert" the user_Y values because the tools consider
    // the top of the screen to be "y=0" and we want to make the bottom
    // of the map to be "y=0"

    user_Y[HI] = grid.pixelToUser_Y(y);
    user_Y[LO] = grid.pixelToUser_Y(y+height);
    user_Y[PT] = grid.pixelToUser_Y(y+height/2+1);
    if ( snap_Y ) {
      user_Y[LO] = grid.snapUser_Y(user_Y[LO], SNAP_ON);
      user_Y[HI] = grid.snapUser_Y(user_Y[HI], SNAP_ON);
      user_Y[PT] = grid.snapUser_Y(user_Y[PT], SNAP_ON);
    }

    // First check
    // It's quite possible for the pixelToUser_ code or the
    // snapUser_ code to return a value which lies outside
    // the range.  This can happen when there are fewer pixels
    // than grid points.  If we've gotten outside the range, 
    // get back in.
    if ( user_Y[LO] < range_Y[LO] ) user_Y[LO] = range_Y[LO];
    if ( user_Y[HI] > range_Y[HI] ) user_Y[HI] = range_Y[HI];
  }


  /**
   * Sets the user_X values for this tool.
   * @param lo the lower value in "user" units.
   * @param hi the higher value in "user" units.
   *
   * This method sets the user values directly rather than going through
   * the interpolation in the tool.  Whenever map.reshape_tool() or
   * map.center_tool() is called, this method should be used because
   * we don't want to allow the possibility of the grid altering
   * the user values simply because the image has fewer pixels
   * than grid points.
   */
  public void setUser_X(double lo, double hi) {

    // The logic below for lo > hi is the following:
    //
    // [ (lo as a percentage of the way around) - (hi ditto) ] / 2.0 + 50%
    // gives the percentage of the way around from the start.
    //

    user_X[LO] = lo;
    user_X[HI] = hi;
    if ( hi >= lo ) {
      user_X[PT] = lo + (hi - lo)/2.0;
    } else {
      if ( grid.x_type != LONGITUDE_AXIS ) {
    System.out.println("ERROR in set_user_values: non-modulo_X and hi(" + hi + ") < lo (" + lo + ")");
      } else {
    user_X[PT] = grid.x_factor / 2.0 + grid.domain_X[LO] +
      (( (lo-grid.domain_X[LO])/grid.x_factor - (hi-grid.domain_X[LO])/grid.x_factor )/2.0) * grid.x_factor;
      }

    }
  }


  /**
   * Sets the user_Y values for this tool.
   * @param lo the lower value in "user" units.
   * @param hi the higher value in "user" units.
   *
   * This method sets the user values directly rather than going through
   * the interpolation in the tool.  Whenever map.reshape_tool() or
   * map.center_tool() is called, this method should be used because
   * we don't want to allow the possibility of the grid altering
   * the user values simply because the image has fewer pixels
   * than grid points.
   */
  public void setUser_Y(double lo, double hi) {
    user_Y[LO] = lo;
    user_Y[HI] = hi;
    user_Y[PT] = lo + (hi - lo)/2.0;
  }


  /**
   * Adjust the width or height for those tools which don't
   * specify an extent of width or height.
   *
   * XTool, YTool, PTTool and decendents retain a width and
   * height but these may need to be adjusted as the tool is
   * moved near the sides.
   */
  public void adjustWidthHeight() {
    // No adjustment for tools which specify an XY region.
  }


//----------------------------------------------------------//
//--------------------  Action methods  --------------------//
//----------------------------------------------------------//


  /**
   * Draws a MapTool.
   * This is the <code>abstract</code> method which makes this class abstract.
   * @param g the graphics context for the drawing operation.
   */
  public abstract void draw(Graphics g);
 

  /**
   * Notifies tool of a mouseMove event.  Returns <code>Frame.MOVE_CURSOR</code> if the
   * mouse moves of the center tool handle.
   * @param mouse_x current mouse X
   * @param mouse_y current mouse Y
   * @return the type of cursor to display.
   */
  public int mouseMove(int mouse_x, int mouse_y) {
    
    int i=0;
    
    for (i=0; i<numHandles; i++) {
      if ( handle[i].contains(mouse_x,mouse_y) ) {
	if ( handle[i].get_type() == C )
	  //1.1.x return Frame.MOVE_CURSOR;
	  return Cursor.MOVE_CURSOR;
      }
    }
    //1.1.x return Frame.DEFAULT_CURSOR;
    return Cursor.DEFAULT_CURSOR;
  }


  /**
   * Notifies tool of a mouseDown event.
   * @param mouse_x current mouse X
   * @param mouse_y current mouse Y
   */
  public void mouseDown(int mouse_x, int mouse_y) {
    
    int i=0;

    // Go through all the handles to see where the mouse is.
    // If several ToolHandle target areas overlap, set the selectedHandle
    // to 'C' if the mouse is over the center handle target area.
    // Otherwise use the last handle in the list whose target area includes
    // the current mouse position.
    //
    for (i=0; i<numHandles; i++) {
      if ( handle[i].contains(mouse_x,mouse_y) ) {
        active = true;
        selectedHandle = handle[i].get_type();
        if ( selectedHandle == C ) break;
      }
    }

    // Recalculate the bounding rectangle
    // JC_TODO: This shouldn't be necessary
    //
    this.applyClipRect(this.canvas_clipRect);

    if ( active == true ) {

      // do nothing

    } else {

      if ( snap_X )
        mouse_x = grid.snap_X(mouse_x, SNAP_ON);
      if ( snap_Y )
        mouse_y = grid.snap_Y(mouse_y, SNAP_ON);

      // Keep the mouse within the bounding rectangle
      //
      mouse_x = (mouse_x < boundingRect.x) ? boundingRect.x : mouse_x;
      mouse_x = (mouse_x > boundingRect.x+boundingRect.width) ? (boundingRect.x+boundingRect.width) : mouse_x;
      mouse_y = (mouse_y < boundingRect.y) ? boundingRect.y : mouse_y;
      mouse_y = (mouse_y > boundingRect.y+boundingRect.height) ? (boundingRect.y+boundingRect.height) : mouse_y;
      x = mouse_x;
      y = mouse_y;
      width = 0;
      height = 0;
      active = true;
      selectedHandle = mouseDownHandle;
      saveHandles();
    }

  }


  /**
   * Notifies tool of a mouseDrag event.
   * @param mouse_x current mouse X
   * @param mouse_y current mouse Y
   */
  public void mouseDrag(int mouse_x, int mouse_y) {

    if ( active ) {

      // Recalculate the bounding rectangle because of potential scrolling
      this.applyClipRect(this.canvas_clipRect);

      // Keep the mouse within the bounding rectangle
      mouse_x = (mouse_x < boundingRect.x) ? boundingRect.x : mouse_x;
      mouse_x = (mouse_x > boundingRect.x+boundingRect.width) ? 
                (boundingRect.x+boundingRect.width) : mouse_x;
      mouse_y = (mouse_y < boundingRect.y) ? boundingRect.y : mouse_y;
      mouse_y = (mouse_y > boundingRect.y+boundingRect.height) ? 
                (boundingRect.y+boundingRect.height) : mouse_y;

      if ( selectedHandle == C )
        this.bump_against_sides(mouse_x,mouse_y);
      else
        this.handle_reshape(mouse_x,mouse_y);

      // We use setUser_XY() here because the user is modifying
      // the shape of the tool and it's appropriate to calculate
      // new user_X/Y values based on the image.
      this.saveHandles();
      this.setUser_XY();

    }

  }


  /**
   * Notifies tool of a mouseUp event.
   * @param x current mouseX
   * @param y current mouseY
   */
  public void mouseUp(int mouse_x, int mouse_y) {
    int i=0, new_mouse=0;
    double user_range=0.0;

    if ( active ) {

      
      if ( selectedHandle == C ) {

        // Recalculate the bounding rectangle because of potential scrolling

        this.applyClipRect(this.canvas_clipRect);

        // This is for the special case where a non-XY tool has been
        // moved close to the edge.  These tools are drawn in the 
        // center of the width or height range and this range must
        // shrink near the boundaries.

        adjustWidthHeight();

        // This is for the special case where you have scrolled to the top of the map
        // and the range is less than the full map.  If you didn't move the mouse
        // after you went outside the canvas rectangle, you will have scrolled to
        // top of the map.  Now we need to reposition the tool so that it is within
        // the appropriate boundingRect as defined by the Grid.range.

        if ( this.x < boundingRect.x ) {
          this.x = boundingRect.x;
        } else if ( this.x > boundingRect.x + boundingRect.width ) {
	      this.x = boundingRect.x+boundingRect.width-this.width;
        }
        if ( this.y < boundingRect.y ) {
          this.y = boundingRect.y;
        } else if ( this.y > boundingRect.y + boundingRect.height ) {
          this.y = boundingRect.y+boundingRect.height-this.height;
        }

        // This is for the special case where you are snpping to grid and
        // you have moved the tool by the central handle.  If you dragged
        // the handle all the way to the edge, you will have "snapped" to
        // the edge of the boundingRect even though that may not represent
        // a gridpoint.  (This behavior is needed to allow scrolling to 
        // happen.)  Now we need to snap to the next gridpoint inwards.

        if ( snap_X ) {
 
          // 0) check for odd (SNAP_MID) or even (SNAP_ON) number of grid cells
          // 1) snap 
          // 3) make sure the left and right edges are within boundingRect

          user_range = grid.pixelToUser_X(this.x+this.width) - 
                       grid.pixelToUser_X(this.x);

          // Center handle and ODD number of grid cells
          if ( ((int)(user_range/grid.delta_X))%2 == 1 && snapMid_X ) {

 	        mouse_x = grid.snap_X(mouse_x, SNAP_MID);
            new_mouse = mouse_x;
            while ( mouse_x+this.width/2 > boundingRect.x+boundingRect.width ) {
              mouse_x = grid.snap_X(--new_mouse, SNAP_MID);
            } 
            while ( mouse_x-this.width/2 < boundingRect.x ) {
              mouse_x = grid.snap_X(++new_mouse, SNAP_MID);
            }
            this.x = mouse_x - this.width/2;

          // Center handle and EVEN number of grid cells
          } else {

 	        mouse_x = grid.snap_X(mouse_x, SNAP_ON);
            new_mouse = mouse_x;
            while ( mouse_x+this.width/2 > boundingRect.x+boundingRect.width ) {
              mouse_x = grid.snap_X(--new_mouse, SNAP_ON);
            } 
            while ( mouse_x-this.width/2 < boundingRect.x ) {
              mouse_x = grid.snap_X(++new_mouse, SNAP_ON);
            }
            this.x = mouse_x - this.width/2;

          }
 
        }

        if ( snap_Y ) {
 
          user_range = grid.pixelToUser_Y(this.y+this.height) - 
                       grid.pixelToUser_Y(this.y);
          user_range = Math.abs(user_range);

          // Center handle and ODD number of grid cells
          if ( ((int)(user_range/grid.delta_Y))%2 == 1 && snapMid_Y ) {

 	        mouse_y = grid.snap_Y(mouse_y, SNAP_MID);
            new_mouse = mouse_y;
            while ( mouse_y+this.height/2 > boundingRect.y+boundingRect.height ) {
              mouse_y = grid.snap_Y(--new_mouse, SNAP_MID);
            } 
            while ( mouse_y-this.height/2 < boundingRect.y ) {
              mouse_y = grid.snap_Y(++new_mouse, SNAP_MID);
            }
            this.y = mouse_y - this.height/2;

          // Center handle and EVEN number of grid cells
          } else {

 	        mouse_y = grid.snap_Y(mouse_y, SNAP_ON);
            new_mouse = mouse_y;
            while ( mouse_y+this.height/2 > boundingRect.y+boundingRect.height ) {
              mouse_y = grid.snap_Y(--new_mouse, SNAP_ON);
            } 
            while ( mouse_y-this.height/2 < boundingRect.y ) {
              mouse_y = grid.snap_Y(++new_mouse, SNAP_ON);
            }
            this.y = mouse_y - this.height/2;

          }
 
        }

      }

      // We need to check for zero width/height selections.
      // If we have zero width or height in conflict with 
      // the needs of the tool, we must reset the tool width/height
      // or x/y to maintain the minimum delta_X/Y.
      //
      check_for_zero_range();

      active = false;
      pan_left = false;
      pan_right = false;
      pan_left_fast = false;
      pan_right_fast = false;
      pan_up = false;
      pan_down = false;
      pan_up_fast = false;
      pan_down_fast = false;

      // We use setUser_XY() here because the user is modifying
      // the shape of the tool and it's appropriate to calculate
      // new user_X/Y values based on the image.
      this.saveHandles();
      this.setUser_XY();
    }
  }


  /**
   * Allows movement of the tool within the bounding rectangle specified in applyClipRect().
   * @param x current mouseX
   * @param y current mouseY
   */
  public void bump_against_sides(int mouse_x, int mouse_y) {
      
    double user_range = 0.0;

   /*
    * If you are snapping to the grid and the tool encompasses
    * an odd number of grid cells along an axis, then you should
    * snap to the grid cell MIDpoints along that axis. Otherwise,
    * snap ONto the gridpoints themselves.
    */

    if ( snap_X ) {
      user_range = grid.pixelToUser_X(this.x+this.width) - 
                   grid.pixelToUser_X(this.x);
      if ( ((int)(user_range/grid.delta_X))%2 == 1 && snapMid_X )
 	    mouse_x = grid.snap_X(mouse_x, SNAP_MID);
      else
 	    mouse_x = grid.snap_X(mouse_x, SNAP_ON);
    }
       
    if ( snap_Y ) {
      user_range = grid.pixelToUser_Y(this.y+this.height) - 
                    grid.pixelToUser_Y(this.y);
      user_range = Math.abs(user_range);
      if ( ((int)(user_range/grid.delta_Y))%2 == 1 && snapMid_Y )
        mouse_y = grid.snap_Y(mouse_y, SNAP_MID);
      else
        mouse_y = grid.snap_Y(mouse_y, SNAP_ON);
    }

    /*
     * Now we're done with snapping and we can decide whether
     * scrolling should be performed.
     */
      
    // left edge against left edge of the boundingRect
    if ( (mouse_x-width/2) < (boundingRect.x) ) {
      this.pan_right = false;
      this.pan_right_fast = false;
      if (left_edge_scroll) {
        this.pan_left = true;
        if ( mouse_x < boundingRect.x+2 )
  	  this.pan_left_fast = true;
        else
	  this.pan_left_fast = false;
      } else {
        this.pan_left = false;
        this.pan_left_fast = false;
      }
      x = boundingRect.x;
    // right edge against right edge of the boundingRect
    } else if ( (mouse_x-width/2+width) > (boundingRect.x+boundingRect.width) ) {
      this.pan_left = false;
      this.pan_left_fast = false;
      if (right_edge_scroll) {
        this.pan_right = true;
        if ( mouse_x > boundingRect.x+boundingRect.width-2 )
	  this.pan_right_fast = true;
        else
	  this.pan_right_fast = false;
      } else {
        this.pan_right = false;
        this.pan_right_fast = false;
      }
      x = (boundingRect.x+boundingRect.width) - width;
    } else {
      this.pan_left = false;
      this.pan_right = false;
      this.pan_left_fast = false;
      this.pan_right_fast = false;
      x = mouse_x-width/2;
    }

    // top edge against top edge of the boundingRect
    if ( (mouse_y-height/2) < (boundingRect.y) ) {
      this.pan_down = false;
      this.pan_down_fast = false;
      if (top_edge_scroll) {
        this.pan_up = true;
        if ( mouse_y < boundingRect.y+2 )
	  this.pan_up_fast = true;
        else
	  this.pan_up_fast = false;
      } else {
        this.pan_up = false;
        this.pan_up_fast = false;
      }
      y = boundingRect.y;
    // bottom edge against bottom edge of the boundingRect
    } else if ( (mouse_y-height/2+height) > (boundingRect.y+boundingRect.height) ) {
      this.pan_up = false;
      this.pan_up_fast = false;
      if (bottom_edge_scroll) {
        this.pan_down = true;
        if ( mouse_y > boundingRect.y+boundingRect.height-2 )
	  this.pan_down_fast = true;
        else
	  this.pan_down_fast = false;
      } else {
        this.pan_down = false;
        this.pan_down = false;
      }
      y = (boundingRect.y+boundingRect.height) - height;
    } else {
      this.pan_down = false;
      this.pan_up = false;
      this.pan_down_fast = false;
      this.pan_up_fast = false;
      y = mouse_y-height/2;
    }

  }


}

