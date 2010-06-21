/*
 * @(#)XcYTool.java    0.1 96/08/15 Jonathan Callahan
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

//import dods.clients.importwizard.TMAP.map.XYTool;

/**
 * A rectangular map tool defined by x, y, width and height.
 *
 * This tool draws a rubber-band box on the screen.
 *
 * @version     0.1, 15 Aug 1996
 * @author      Jonathan Callahan
 */
public class XcYTool extends XYTool {
 
  /**
   * Constructs and initializes an XcYTool with the specified parameters.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param width the width of the XcYTool
   * @param height the height of the XcYTool
   */
  public XcYTool(int x, int y, int width, int height, Color color) {
    super(x, y, width, height, color);
  }
 
  /**
   * Constructs an XcYTool and initializes it to the specified rectangle.
   * @param rect the rectangle of the XcYTool
   */
  public XcYTool(Rectangle rect, Color color) {
    this(rect.x, rect.y, rect.width, rect.height, color);
  }
 
  /**
   * Draws an XcYTool.
   *
   * This method overrides the <code>abstract</code> method in
   * MapTool and allows us to instantiate an XcYTool.
   * @param g the graphics context for the drawing operation.
   */
  public void draw(Graphics g) {
    int i=0;
    int [] x_array = {0, 0, 0, 0, 0, 0, 0};
    int [] y_array = {0, 0, 0, 0, 0, 0, 0};
    int poly_x, poly_y, poly_num=7;
    int poly_width=3, poly_height=5;
 
    g.setColor(color);

    g.drawLine(x, y+height/2, x+width, y+height/2);

    if ( drawHandles ) {
      handle[0].draw(g);
      handle[1].draw(g);
      handle[2].draw(g);
      handle[4].draw(g);
      handle[5].draw(g);
      handle[6].draw(g);
    }

//     for (i=0; i<numHandles; i++)
//       handle[i].draw(g);
 
    g.drawLine(x, y, x, y+height);
    poly_x = x;
    poly_y = y+height/2;
    x_array[0] =poly_x-poly_width;
    x_array[1] =poly_x+poly_width;
    x_array[2] =poly_x;
    x_array[3] =poly_x+poly_width;
    x_array[4] =poly_x-poly_width;
    x_array[5] =poly_x;
    x_array[6] =poly_x-poly_width;
    y_array[0] =poly_y-poly_height;
    y_array[1] =poly_y-poly_height;
    y_array[2] =poly_y;
    y_array[3] =poly_y+poly_height;
    y_array[4] =poly_y+poly_height;
    y_array[5] =poly_y;
    y_array[6] =poly_y-poly_height;
    g.fillPolygon(x_array, y_array, poly_num);
 
    g.drawLine(x+width/2, y, x+width/2, y+height);
    poly_x = x+width/2;
    x_array[0] =poly_x-poly_width;
    x_array[1] =poly_x+poly_width;
    x_array[2] =poly_x;
    x_array[3] =poly_x+poly_width;
    x_array[4] =poly_x-poly_width;
    x_array[5] =poly_x;
    x_array[6] =poly_x-poly_width;
    g.fillPolygon(x_array, y_array, poly_num);
 
    g.drawLine(x+width, y, x+width, y+height);
    poly_x = x+width;
    x_array[0] =poly_x-poly_width;
    x_array[1] =poly_x+poly_width;
    x_array[2] =poly_x;
    x_array[3] =poly_x+poly_width;
    x_array[4] =poly_x-poly_width;
    x_array[5] =poly_x;
    x_array[6] =poly_x-poly_width;
    g.fillPolygon(x_array, y_array, poly_num);

  }
 
}
 
 
