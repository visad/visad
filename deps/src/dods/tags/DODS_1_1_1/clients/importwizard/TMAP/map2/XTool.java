/*
 * @(#)XTool.java    0.1 97/01/28 Jonathan Callahan
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
import java.awt.Graphics;
import java.awt.Rectangle;

//import dods.clients.importwizard.TMAP.map.MapTool;

/**
 * A rectangular map tool defined by x, y, width and height.
 *
 * This tool draws a rubber-band box on the screen.
 *
 * @version     0.1, 15 Aug 1996
 * @author      Jonathan Callahan
 */
public class XTool extends MapTool {
 
  /**
   * Constructs and initializes an XTool with the specified parameters.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param width the width of the XTool
   * @param height the height of the XTool
   */
  public XTool(int x, int y, int width, int height, Color color) {
    numHandles = 3;
    mouseDownHandle = E;
    snapMid_Y = false;
    needsRange_Y = false;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.color = color;
    this.saveHandles();		
  }
 
  /**
   * Constructs an XTool and initializes it to the specified rectangle.
   * @param rect the rectangle of the XTool
   */
  public XTool(Rectangle rect, Color color) {
    this(rect.x, rect.y, rect.width, rect.height, color);
  }
 

  /**
   * Draws an XTool.
   *
   * This method overrides the <code>abstract</code> method in
   * MapTool and allows us to instantiate an XTool.
   * @param g the graphics context for the drawing operation.
   */
  public void draw(Graphics g) {
    int i=0;
    g.setColor(color);
    g.drawLine(x, y+height/2, x+width, y+height/2);
    if ( drawHandles ) {
      for (i=0; i<numHandles; i++)
        handle[i].draw(g);
    }
  }
 
 
  /**
   * Saves the current positions of all the handles.
   */
  protected void saveHandles() {
    handle = new ToolHandle[numHandles];
    handle[0]  = new ToolHandle(x-hw/2, y+height/2-hh/2, hw, hh, color, W);
    handle[1]  = new ToolHandle(x+width/2-hw/2, y+height/2-hh/2, hw, hh, color, C);
    handle[2]  = new ToolHandle(x+width-hw/2, y+height/2-hh/2, hw, hh, color, E);
  }


  /**
   * Adjust the width or height for those tools which don't
   * specify an extent of width or height.
   *
   * XTool, and decendents need to adjust the height.
   */
  public void adjustWidthHeight() {

    if ( this.y + height/2 < boundingRect.y ) {
      this.height = 0;
      this.y = boundingRect.y;
    } else if ( this.y < boundingRect.y ) {
      this.height = 2 * (this.y + this.height/2 - boundingRect.y);
      this.y = boundingRect.y;
    } else if ( this.y + height/2 > boundingRect.y + boundingRect.height ) {
      this.height = 0;
      this.y = boundingRect.y + boundingRect.height;
    } else if ( this.y + height > boundingRect.y + boundingRect.height ) {
      this.height = 2 * (boundingRect.y + boundingRect.height - (this.y + this.height/2));
      this.y = boundingRect.y+boundingRect.height-this.height;
    }

  }

 

  public void bump_against_sides(int mouse_x, int mouse_y) {
    // left edge against left edge of the boundingRect
    if ( (mouse_x-width/2) < boundingRect.x ) {
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
    if ( mouse_y < boundingRect.y+hh/2 ) {
      this.pan_down = false;
      this.pan_down_fast = false;
      if (top_edge_scroll) {
        this.pan_up = true;
        if ( mouse_y == boundingRect.y )
          this.pan_up_fast = true;
        else
          this.pan_up_fast = false;
      } else {
        this.pan_up = false;
        this.pan_up_fast = false;
      }
      y = boundingRect.y;
      height = 0;
    // bottom edge against bottom edge of the boundingRect
    } else if ( mouse_y > boundingRect.y+boundingRect.height-hh/2 ) {
      this.pan_up = false;
      this.pan_up_fast = false;
      if (bottom_edge_scroll) {
        this.pan_down = true;
        if ( mouse_y == boundingRect.y+boundingRect.height )
          this.pan_down_fast = true;
        else
          this.pan_down_fast = false;
      } else {
        this.pan_down = false;
        this.pan_down = false;
      }
      y = boundingRect.y+boundingRect.height;
      height = 0;
    } else {
      this.pan_down = false;
      this.pan_up = false;
      this.pan_down_fast = false;
      this.pan_up_fast = false;
      y = mouse_y-height/2;
    }

  }
 


}
