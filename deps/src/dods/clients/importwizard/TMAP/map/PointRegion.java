/*
 * @(#)PointRegion.java    3.0 99/09/08 Jonathan Callahan
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

//import dods.clients.importwizard.TMAP.map.MapRegion;

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
public class PointRegion extends MapRegion {

  public int radius=1;

  /**
   * Constructs a new PointRegion.
   * @param x the x coordinate in pixel values
   * @param y the y coordinate in pixel values
   * @param color the color of the MapRegion
   */
  public PointRegion(int x, int y, Color color) {
    setLocation(x,y);
    this.width = 2*radius+1;
    this.height = 2*radius*1;
    this.color = color;
  }

  /**
   * Constructs a new PointRegion.
   * @param x the x coordinate in "user" values
   * @param y the y coordinate in "user" values
   * @param color the color of the MapRegion
   */
  public PointRegion(double x, double y, Color color) {
    setLocation(1,1);
    user_X = x;
    user_Y = y;
    this.width = 2*radius+1;
    this.height = 2*radius*1;
    this.color = color;
  }


  /**
   * Draws a PointRegion.
   *
   * This method overrides the <code>abstract</code> method in
   * MapRegion and allows us to instantiate an PointRegion.
   * @param g the graphics context for the drawing operation.
   */
  public void draw(Graphics g) {
    g.setColor(color);
    g.fillOval(x-radius, y-radius, 2*radius+1, 2*radius+1);
  }

}

