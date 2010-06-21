/*
 * @(#)ToolHandle.java    0.1 96/08/15 Jonathan Callahan
 * @(#)ToolHandle.java    2.3 97/10/13 Jonathan Callahan
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Point;

/**
 * A rectangular tool handle to be used by a MapTool and defined by 
 * <dl>
 * <dt><b>x, y, width, height</b>
 * <dd>the position and size of the visible handle
 * <dt><b>x_target, y_target</b>
 * <dd>the amount to to grow() the rectangle along X and Y to get a useful target area
 * <dt><b>color</b>
 * <dd>the Color used when the ToolHandle is drawn
 * <dt><b>type</b>
 * <dd>one of:  <code>NW, N, NE, W, C, C, SW, S, E</code>
 * </dl>
 *
 * @version     2.3, 13 Oct 1997
 * @author      Jonathan Callahan
 */
public class ToolHandle extends Rectangle {

  /**
   * The type of the ToolHandle.
   */
  private int type;

  /**
   * The color of the ToolHandle.
   */
  private Color color;

  /**
   * The amount to grow() the rectangle along X to increase the target area.
   */
  private int x_target=4;

  /**
   * The amount to grow() the rectangle along Y to increase the target area.
   */
  private int y_target=4;

  /**
   * Constructs a new ToolHandle.
   */
  public ToolHandle() {
  }
 
  /**
   * Constructs and initializes a ToolHandle with the specified parameters.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   * @param width the width of the ToolHandle
   * @param height the height of the ToolHandle
   * @param color the color of the ToolHandle
   * @param type the type of the ToolHandle
   */
  public ToolHandle(int x, int y, int width, int height, Color color, int type) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.color = color;
    this.type = type;
  }
 
  /**
   * Constructs a ToolHandle and initializes it with the specified parameters.
   *
   * @param width the width of the ToolHandle
   * @param height the height of the ToolHandle
   * @param color the color of the ToolHandle
   * @param type the type of the ToolHandle
   */
  public ToolHandle(int width, int height, Color color, int type) {
    this(0, 0, width, height, color, type);
  }
 
  /**
   * Constructs a ToolHandle and initializes it to a specified parameters.
   *
   * @param p the point
   * @param d dimension
   * @param color the color of the ToolHandle
   * @param type the type of the ToolHandle
   */
  public ToolHandle(Point p, Dimension d, Color color, int type) {
    this(p.x, p.y, d.width, d.height, color, type);
  }
 
  /**
   * Constructs a ToolHandle and initializes it to the specified parameters.
   * @param p the value of the x and y coordinate
   * @param color the color of the ToolHandle
   * @param type the type of the ToolHandle
   */
  public ToolHandle(Point p, Color color, int type) {
    this(p.x, p.y, 0, 0, color, type);
  }
 
  /**
   * Constructs a ToolHandle and initializes it to the specified parameters.
   *
   * @param d the value of the width and height
   * @param color the color of the ToolHandle
   * @param type the type of the ToolHandle
   */
  public ToolHandle(Dimension d, Color color, int type) {
    this(0, 0, d.width, d.height, color, type);
  }
 

  /**
   * Overrides the Rectangle.contains() method to allow for definition
   * of a target area larger than the size of the handle.
   *
   * @param x current mouse X
   * @param y current mouse Y
   */
  public boolean contains(int x, int y) {
    Rectangle target = new Rectangle(this.x, this.y, this.width, this.height);
    target.grow(x_target, y_target);
    return target.inside(x, y); //1.0
    //1.1 return target.contains(x, y);
  }

  /**
   * Sets the ToolHandle color.
   * @param color the Color used to draw the ToolHandle.
   */
  public void setColor( Color color ) {
    this.color = color;
  }

  /**
   * Returns the ToolHandle color.
   * @return the ToolHandle color.
   */
  public Color getColor() {
    return color;
  }

  /**
   * Sets the ToolHandle type.
   * @param type the <code>[ NW, N, NE, W, C ... ]</code> type of the ToolHandle.
   */
  public void set_type( int type ) {
    this.type = type;
  }

  /**
   * Returns the ToolHandle type.
   * @return the <code>[ NW, N, NE, W, C ... ]</code> type of the ToolHandle.
   */
  public int get_type() {
    return type;
  }

  /**
   * Sets the x_target of the ToolHandle.
   * @param x_target the number of pixels added to each side of the ToolHandle to get a useful target area.
   */
  public void set_x_target( int x_target ) {
    this.x_target = x_target;
  }

  /**
   * Returns the x_target of the ToolHandle.
   * @return the number of pixels added to each side of the ToolHandle to get a useful target area.
   */
  public int get_x_target() {
    return x_target;
  }

  /**
   * Sets the y_target of the ToolHandle.
   * @param y_target the number of pixels added to the top and bottom of the ToolHandle to get a useful target area.
   */
  public void set_y_target( int y_target ) {
    this.y_target = y_target;
  }

  /**
   * Returns the y_target of the ToolHandle.
   * @return the number of pixels added to the top and bottom of the ToolHandle to get a useful target area.
   */
  public int get_y_target() {
    return y_target;
  }

  /**
   * Draws a ToolHandle.
   * @param g the graphics context for the drawing operation.
   */
  public void draw(Graphics g) {
    g.setColor(color);
    g.fillRect(x, y, width, height);
  }
 
}


