/*
 * @(#)XYTool.java    0.1 96/08/15 Jonathan Callahan
 * @(#)XYTool.java    2.3 97/10/13 Jonathan Callahan
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
 * @version     2.3, 13 Aug 1997
 * @author      Jonathan Callahan
 */
public class XYTool extends MapTool {
 
  /**
   * Constructs and initializes an XYTool with the specified parameters.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param width the width of the XYTool
   * @param height the height of the XYTool
   * @param color the color of the XYTool
   */
  public XYTool(int x, int y, int width, int height, Color color) {
    super(x, y, width, height, color);
  }
 
  /**
   * Constructs an XYTool and initializes it to the specified rectangle.
   * @param rect the rectangle of the XYTool
   * @param color the color of the XYTool
   */
  public XYTool(Rectangle rect, Color color) {
    this(rect.x, rect.y, rect.width, rect.height, color);
  }
 
  /**
   * Draws an XYTool.
   *
   * This method overrides the <code>abstract</code> method in
   * MapTool and allows us to instantiate an XYTool.
   * @param g the graphics context for the drawing operation.
   */
  public void draw(Graphics g) {
    int i=0;
    g.setColor(color);
    g.drawRect(x, y, width, height);
    // IE5 chooses not to darw rectangles of zero width or height.
    // So we need to draw one vertical and one horizontal line
    // in case our rectange has zero width or height.
    g.drawLine(x,y,x+width,y);
    g.drawLine(x,y,x,y+height);
    if ( drawHandles ) {
      for (i=0; i<numHandles; i++)
        handle[i].draw(g);
    }
  }
 
}
 
 
