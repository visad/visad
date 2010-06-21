/*
 * @(#)MapRegion.java    3.0 99/09/08 Jonathan Callahan
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
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;

//import dods.clients.importwizard.TMAP.map.MapGrid;

/**
 * A region on the map which can be drawn and which may
 * listen to mouse events.
 *
 * The <code>abstract</code> method which makes this class
 * abstract is the <code>draw(Graphics g)</code> method.
 *
 * @version     3.0 Sept 09 1999
 * @author      Jonathan Callahan
 */
public abstract class MapRegion extends Rectangle {

  /**
   * Current "user" value assocaited with MapRegion.x.
   */
  public double user_X;

  /**
   * Current "user" values assocaited with MapRegion.y.
   */
  public double user_Y;

  /**
   * The grid on which this tool acts.  This is just a reference to 
   * the MapGrid associated with the MapCanvas.
   */
  public MapGrid grid;
 
  /**
   * The region which is sensitive to mouse events.
   */
  protected Polygon sensitiveArea;
 
  /**
   * The area of the map canvas occupied by the map. 
   */
  protected Rectangle canvas_clipRect;
 
  /**
   * The color of the tool.
   */
  protected Color color;
 
 
  /**
   * Constructs a new MapRegion.
   */
  public MapRegion() {
    setLocation(1,1);
  }
 
  /**
   * Constructs and initializes a MapRegion with the specified parameters.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param color the color of the MapRegion
   */
  public MapRegion(int x, int y, Color color) {
    setLocation(x,y);
    this.color = color;
  }

  /**
   * Constructs and initializes a MapRegion with the specified parameters.
   * @param x the x coordinate in  "user" values
   * @param y the y coordinate in  "user" values
   * @param color the color of the MapRegion
   */
  public MapRegion(double x, double y, Color color) {
    setLocation(1,1);
    user_X = x;
    user_Y = y;
    this.color = color;
  }


  /**
   * Returns the String representation of the tool's values.
   */
  public String toString() {
    StringBuffer sbuf = new StringBuffer(super.toString());
    return sbuf.toString();
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
   * Moves the region to a new x, y location interpreting
   * x and y as pixels.
   * @see Rectangle
   */
  public void setLocation(int x, int y) {
    this.x = x;
    this.y = y;
  }


  /**
   * Moves the region to a new x, y location interpreting
   * x and y as user values on the associated grid.
   * @see Rectangle
   */
  public void setUserLocation() {
    setLocation(grid.userToPixel_X(user_X),grid.userToPixel_Y(user_Y));
  }

  /**
   * Moves the region to a new x, y location interpreting
   * x and y as user values on the associated grid.
   * @see Rectangle
   */
  public void setUserLocation(double x, double y) {
    user_X = x;
    user_Y = y;
    setLocation(grid.userToPixel_X(x),grid.userToPixel_Y(y));
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
    this.color = color;
  }


//----------------------------------------------------------//
//--------------------  Action methods  --------------------//
//----------------------------------------------------------//


  /**
   * Draws a MapRegion.
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
    return Cursor.DEFAULT_CURSOR;
  }


  /**
   * Notifies tool of a mouseDown event.
   * @param mouse_x current mouse X
   * @param mouse_y current mouse Y
   */
  public void mouseDown(int mouse_x, int mouse_y) {
  }


  /**
   * Notifies tool of a mouseUp event.
   * @param x current mouseX
   * @param y current mouseY
   */
  public void mouseUp(int mouse_x, int mouse_y) {
  }


}

