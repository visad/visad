/*
 * @(#)MapGrid.java
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
import java.awt.Rectangle;
//import dods.clients.importwizard.TMAP.map.MapConstants;

/**
 * A grid for use with the MapCanvas.
 * <p>
 * A MapGrid contains all the information for the conversion of pixels
 * to user values and vice versa.  Associating a grid with a map also
 * allows MapTools to work in snap-to-grid mode.
 *
 *  version     2.3, 23 Jun 1997
 * @version     3.0, 16 Nov 1999
 * @author      Jonathan Callahan (NOAA/OAR/PMEL/TMAP)
 */

public class MapGrid extends Object implements MapConstants {
  
  /**
   * The data domain along X [in user coordinates].
   */
  public double [] domain_X = new double[2];

  /**
   * The data domain along Y [in user coordinates].
   */
  public double [] domain_Y = new double[2];

  /**
   * "user" value of the leftmost pixel in the base image.
   */
  public double x_start=0.0;

  /**
   * "user" value of the topmost pixel in the base image.
   */
  public double y_start=0.0;

  /**
   * Full "user" domain of x in the base image.
   */
  public double x_factor=1.0;

  /**
   * Full "user" domain of y in the base image.
   */
  public double y_factor=1.0;
	
  /**
   * The grid spacing (in "user" coordinates) in the x dimension.
   */
  public double delta_X=1.0;

  /**
   * The grid spacing (in "user" coordinates) in the y dimension.
   */
  public double delta_Y=1.0;

  /**
   * The X axis type.
   * @see MapConstants
   */
  public int x_type=LONGITUDE_AXIS;

  /**
   * The Y axis type.
   * @see MapConstants
   */
  public int y_type=LATITUDE_AXIS;

  /**
   * Flag determining whether the X axis is modulo or not (eg. longitude).
   */
  public boolean modulo_X = true;

  /**
   * The rectangle (in pixels) of the  base image.
   * This is a reference to the imageRect in the MapCanvas and
   * will be updated by zoom/pan/scrolling.
   * <p>
   * Do not alter this property inside of MapGrid.java!
   */
  public Rectangle imageRect;

  /**
   * The MapCanvas width is needed in userToPixel_X().
   */
  private int canvasWidth;

  /**
   * Constructs a new MapGrid.
   */
  public MapGrid() {
  }
 
  /**
   * Constructs and initializes a MapGrid with the specified parameters.
   * @param x_lo the "user" value of the low end of the data domain along x.
   * @param x_hi the "user" value of the high end of the data domain along x.
   * @param y_lo the "user" value of the low end of the data domain along y.
   * @param y_hi the "user" value of the high end of the data domain along y.
   *
   * The <b>data domain</b> is that region represented by the underlying base image.
   */
  public MapGrid(double x_lo, double x_hi, double y_lo, double y_hi) {
    this.setDomain_X(x_lo, x_hi);
    this.setDomain_Y(y_lo, y_hi);
  }


  /**
   * Sets canvasWidth.
   * @param width the width associated with the MapCanvas.
   */
  public void setCanvasWidth(int width) {
    canvasWidth = width;
  }


  /**
   * Sets the domain of X of the grid (in "user" coordinates).
   * This should coincide with the domain specified
   * by the underlying basemap in MapCanvas.
   * @param lo the "user" value of the low end of the data domain along X.
   * @param hi the "user" value of the high end of the data domain along X.
   *
   * A default delta_X will be calculated.  This is overridden by
   * the tool whenever MapTool.setDelta_X() is used to assign a
   * specific delta_X to a tool.
   */
  public void setDomain_X(double lo, double hi) {

    double new_delta = Math.abs(hi-lo);

    domain_X[LO] = lo;
    domain_X[HI] = hi;
    x_start = lo;
    x_factor = hi - lo;

    if ( x_type == LONGITUDE_AXIS && (hi - lo) != 360.0 )
      modulo_X = false;
    else
      modulo_X = true;

    if ( new_delta > 180 ) new_delta = 2.0;
    else if ( new_delta > 90 ) new_delta = 1.0;
    else if ( new_delta > 45 ) new_delta = 0.5;
    else if ( new_delta > 18 ) new_delta = 0.2;
    else if ( new_delta > 9 ) new_delta = 0.1;
    else if ( new_delta > 4.5 ) new_delta = 0.05;
    else if ( new_delta > 1.8 ) new_delta = 0.02;
    else if ( new_delta > 0.9 ) new_delta = 0.01;
    else if ( new_delta > 0.45 ) new_delta = 0.005;
    else if ( new_delta > 0.18 ) new_delta = 0.002;
    else new_delta = 0.001;

    setDelta_X(new_delta);
  }


  /**
   * Sets the domain of Y of the grid (in "user" coordinates).
   * This should coincide with the domain specified
   * by the underlying basemap in MapCanvas.
   * @param lo the "user" value of the low end of the data domain along Y.
   * @param hi the "user" value of the high end of the data domain along Y.
   *
   * A default delta_Y will be calculated.  This is overridden by
   * the tool whenever MapTool.setDelta_Y() is used to assign a
   * specific delta_Y to a tool.
   */
  public void setDomain_Y(double lo, double hi) {

    double new_delta = hi - lo ;

    domain_Y[LO] = lo;
    domain_Y[HI] = hi;
    y_start = lo;
    y_factor = hi - lo;

    if ( new_delta > 180 ) new_delta = 2.0;
    else if ( new_delta > 90 ) new_delta = 1.0;
    else if ( new_delta > 45 ) new_delta = 0.5;
    else if ( new_delta > 18 ) new_delta = 0.2;
    else if ( new_delta > 9 ) new_delta = 0.1;
    else if ( new_delta > 4.5 ) new_delta = 0.05;
    else if ( new_delta > 1.8 ) new_delta = 0.02;
    else if ( new_delta > 0.9 ) new_delta = 0.01;
    else if ( new_delta > 0.45 ) new_delta = 0.005;
    else if ( new_delta > 0.18 ) new_delta = 0.002;
    else new_delta = 0.001;

    setDelta_Y(new_delta);
  }


  /**
   * Sets delta_X which is used in grid snapping.
   * @param delta the "user" spacing of grid cells along the X axis.
   */
  public void setDelta_X(double delta) {
    delta_X = delta;
  }

  /**
   * Gets delta_X which is used in grid snapping.
   */
  public double getDelta_X() {
    return delta_X;
  }


  /**
   * Sets delta_Y which is used in grid snapping.
   * @param delta the "user" spacing of grid cells along the Y axis.
   */
  public void setDelta_Y(double delta) {
    delta_Y = delta;
  }

  /**
   * Gets delta_Y which is used in grid snapping.
   */
  public double getDelta_Y() {
    return delta_Y;
  }


//=========================================================
//
// X conversions.
//
//=========================================================

  /**
   * Converts a pixel value into a "user" value.
   * @param pixel_x the X value in pixels.
   * @return a "user" X value associated with the input.
   */
  public double pixelToUser_X(int pixel_x) {
    double user_x=0.0;

    /*
     * We need to take special care when the image is scrolling and
     * pixel_x is beyond the edge of the initial image.
     *
     * In that case we need to subtract "imageRect.width" from the number of pixels
     * we calculate before we apply "x_factor".
     */

    if ( pixel_x > (imageRect.x+(imageRect.width-1)) )
      user_x = (double)(pixel_x-imageRect.x-(imageRect.width-1)) *
               (x_factor/(double)(imageRect.width-1)) + x_start;
    else
      user_x = (double)(pixel_x-imageRect.x) *
               (x_factor/(double)(imageRect.width-1)) + x_start;

    return(user_x);
  }


  /**
   * Converts a "user" value into a pixel value.
   * @param user_x the X value in "user" units.
   * @return a pixel X value associated with the input.
   */
  public int userToPixel_X(double user_x) {
    int pixel_x=0;

    pixel_x = (int)( (user_x - x_start) *
              ((double)(imageRect.width-1)/x_factor) + (double)imageRect.x );

    // We need to make sure pixel_x is in the MapCanvas area if possible.
    // This means it may need to be drawn on the first or second image
    // depending on the value of imageRect.x when scrolling is in effect.
    //
    if ( pixel_x < 0 && pixel_x+imageRect.width < canvasWidth )
      pixel_x += imageRect.width;

    if ( pixel_x > canvasWidth && pixel_x-imageRect.width >= 0 )
      pixel_x -= imageRect.width;

    return(pixel_x);
  }


  /**
   * Converts a "user" range along X to a width in pixels.
   * @param range_x a "user" range.
   * @return pixels the equivalent value in pixels.
   */
  public int rangeToPixels_X(double range_x) {
    int pixels = 0;

    pixels = (int)( range_x * ((double)(imageRect.width-1)/x_factor) );

    return pixels;
  }


  /**
   * Returns the X pixel value nearest the closest X grid point.
   * @param pixel_x the current X position in pixels.
   * @param style which nearby gridpoint to snap to [SNAP_ON, SNAP_MID]
   * @return a new X position in pixels.
   */
  public int snap_X(int pixel_x, int style) {
    return snap_X(pixel_x, style, 0);
  }


  /**
   * Returns the X pixel value nearest the closest X grid point.
   * @param pixel_x the current mouse X position.
   * @param style which nearby gridpoint to snap to [SNAP_ON, SNAP_MID]
   * @param shift number of grid cells to shift the result
   * @return a new X position in pixels.
   */
  public int snap_X(int pixel_x, int style, int shift) {
    double return_val=0.0, user_val=0.0;

    /*
     * Note that we cannot use the pixelToUser_X() or userToPixel_X()
     * methods because we NEED to know whether the "special care" mentioned
     * below is taken in order to undo the calculation properly.
     * This problem doesn't happen with snap_Y().
     *
     * Using the pixelToUser_X() and userToPixel_X() methods results in
     * the tool jumping from the right edge to the left of the image depending
     * on how far past the MapCanvas boundary the mouse is positioned.
     * ==============================================================
     * We need to take special care when the image is scrolling and
     * pixel_x is beyond the edge of the initial image.
     *
     * In that case we need to subtract "imageRect.width" from the number of pixels
     * we calculate before we apply "x_factor".
     */

    // Check which image the pixel is on and calculate
    // user_val accordingly.
    //
    if ( pixel_x > (imageRect.x+imageRect.width) )
      user_val = (double)(pixel_x-imageRect.x -imageRect.width) *
                 (x_factor/(double)imageRect.width) + x_start;
    else
      user_val = (double)(pixel_x-imageRect.x) *
                 (x_factor/(double)imageRect.width) + x_start;

    // Snap to the nearest grid point.
    //
    return_val = snapUser_X(user_val, style, shift);
 
    // Undo the top calculation
    //
    if ( pixel_x > (imageRect.x + imageRect.width) )
      return_val = (return_val - x_start) *
                   ((double)imageRect.width/x_factor) +
                   (double)(imageRect.x + imageRect.width);
    else
      return_val = (return_val - x_start) *
                   ((double)imageRect.width/x_factor) +
                   (double)imageRect.x;

    return ( (int)return_val );

  }


  /**
   * Returns the X "user" value nearest the closest X grid point.
   * @param user_x the "user" X position.
   * @param style which nearby gridpoint to snap to [SNAP_ON, SNAP_MID]
   * @return a new value for user_x.
   */
  public double snapUser_X(double user_x, int style) {
    return snapUser_X(user_x, style, 0);
  }


  /**
   * Returns the X "user" value nearest the closest X grid point.
   * @param user_x the "user" X position.
   * @param style which nearby gridpoint to snap to [SNAP_ON, SNAP_MID]
   * @param shift number of grid cells to shift the result
   * @return a new value for user_x.
   */
  public double snapUser_X(double user_x, int style, int shift) {
    double test_val=0.0, return_val=0.0, mid_adjust=0.0;

    if ( style == SNAP_MID ) mid_adjust = delta_X/2.0;

    // Test for the nearest grid point
    //
    test_val = user_x / delta_X;

    if ( test_val >= 0 ) {

      if ( (test_val - (int)test_val) < 0.5 )
        return_val = ((int)(test_val)+shift) * delta_X + mid_adjust;
      else
        return_val = ((int)(test_val)+shift+1) * delta_X - mid_adjust;

    } else {

      if ( ((int)test_val - test_val) < 0.5 )
        return_val = ((int)(test_val)+shift) * delta_X - mid_adjust;
      else
        return_val = ((int)(test_val)+shift-1) * delta_X + mid_adjust;

    }

    return ( return_val );
  }

//=========================================================
//
// Y conversions.
//
//=========================================================

  /**
   * Converts a pixel value into a "user" value.
   * (Note that pixel values increase from top to bottom, whereas
   * user values increase from bottom to top.)
   * @param pixel_y the pixel's y value.
   * @return a "user" Y value associated with the input.
   */
  public double pixelToUser_Y(int pixel_y) {
    double user_y=0.0;

    user_y = ((imageRect.height-1)-(pixel_y-imageRect.y)) *
             (y_factor/(imageRect.height-1)) + y_start;

    return(user_y);
  }


  /**
   * Converts a "user" value into a pixel value.
   * (Note that pixel values increase from top to bottom, whereas
   * user values increase from bottom to top.)
   * @param user_y the "user" y value.
   * @return a pixel Y value associated with the input.
   */
  public int userToPixel_Y(double user_y) {
    int pixel_y=0;

    pixel_y = (int)( imageRect.y - ( (user_y - y_start) *
              ((imageRect.height-1)/y_factor) - (imageRect.height-1)) );

    return(pixel_y);
  }


  /**
   * Converts a "user" range along Y to a width in pixels.
   * @param range_y a "user" range.
   * @return pixels the equivalent value in pixels.
   */
  public int rangeToPixels_Y(double range_y) {
    int pixels = 0;

    pixels = (int)( range_y * ((double)(imageRect.height-1)/y_factor) );

    return pixels;
  }


  /**
   * Returns the Y pixel value nearest the closest Y grid point.
   * @param pixel_y the current Y position in pixels.
   * @param style which nearby gridpoint to snap to [SNAP_ON, SNAP_MID]
   * @return a new Y position in p ixels.
   */
  public int snap_Y(int pixel_y, int style) {
    return snap_Y(pixel_y, style, 0);
  }


  /**
   * Returns the Y pixel value nearest the closest Y grid point.
   * @param pixel_y the current mouse Y position.
   * @param style which nearby gridpoint to snap to [SNAP_ON, SNAP_MID]
   * @param shift number of grid cells to shift the result
   * @return a new Y position in pixels.
   */
  public int snap_Y(int pixel_y, int style, int shift) {
    double return_val=0.0, user_val=0.0;

    user_val = this.pixelToUser_Y(pixel_y);
    return_val = snapUser_Y(user_val, style, shift);

    return ( this.userToPixel_Y(return_val) );

  }


  /**
   * Returns the Y "user" value nearest the closest Y grid point.
   * @param user_y the "user" Y position.
   * @param style which nearby gridpoint to snap to [SNAP_ON, SNAP_MID]
   * @return a new Y position in p ixels.
   */
  public double snapUser_Y(double user_y, int style) {
    return snapUser_Y(user_y, style, 0);
  }


  /**
   * @param user_y the "user" Y position.
   * @param style which nearby gridpoint to snap to [SNAP_ON, SNAP_MID]
   * @param shift number of grid cells to shift the result
   * @return a new value for user_y.
   */
  public double snapUser_Y(double user_y, int style, int shift) {
    double test_val=0.0, return_val=0.0, mid_adjust=0.0;

    if ( style == SNAP_MID ) mid_adjust = delta_Y/2.0;

    // Test for the nearest grid point
    //
    test_val = user_y / delta_Y;

    if ( test_val >= 0 ) {

      if ( (test_val - (int)test_val) < 0.5 )
        return_val = ((int)(test_val)+shift) * delta_Y + mid_adjust;
      else
        return_val = ((int)(test_val)+shift+1) * delta_Y - mid_adjust;

    } else {

      if ( ((int)test_val - test_val) < 0.5 )
        return_val = ((int)(test_val)+shift) * delta_Y - mid_adjust;
      else
        return_val = ((int)(test_val)+shift-1) * delta_Y + mid_adjust;

    }

    return ( return_val );
  }

}
