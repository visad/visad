/*
 * @(#)MapConstants.java
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

/**
 * Constants which are used with the tmap.map package.
 * <p>
 * Each applet which uses this package will want to implement
 * this interface to access the <code>user_x</code> and <code>user_y</code>
 * arrays in MapCanvas.
 * <p>
 * An example from an applet:
 * <pre>

 ...
 
  public boolean mouseDrag(Event evt, int x, int y) {
        text_label.setText("from:  " + map.user_x[LO] + ", " + map.user_y[LO] +
        " to:  " + map.user_x[HI] + ", " + map.user_y[HI]);
  }

...
</pre>
 *
 * @version     1.0, 26 Sep 1996
 * @author      Jonathan Callahan
 */

public interface MapConstants {

  /**
   * A predefined constant for the left/bottom of a MapTool.
   */
  public static final int LO = 0;

  /**
   * A predefined constant for the right/top of a MapTool.
   */
  public static final int HI = 1;

  /**
   * A predefined constant for the center of a MapTool.
   */
  public static final int MID = 2;

  /**
   * A predefined constant for the center of a MapTool.
   */
  public static final int PT = 2;


  /**
   * A predefined constant for affecting how snapping occurs.
   * @see MapGrid
   */
  public static final int SNAP_ON = 0;

  /**
   * A predefined constant for affecting how snapping occurs.
   * @see MapGrid
   */
  public static final int SNAP_MID = 1;



  /**
   * A predefined constant for specification of an axis type.
   * @see MapGrid
   */
  public static final int LONGITUDE_AXIS = 0;

  /**
   * A predefined constant for specification of an axis type.
   * @see MapGrid
   */
  public static final int LATITUDE_AXIS = 1;

  /**
   * A predefined constant for specification of an axis type.
   * @see MapGrid
   */
  public static final int DEPTH_AXIS = 2;

  /**
   * A predefined constant for specification of an axis type.
   * @see MapGrid
   */
  public static final int HEIGHT_AXIS = 3;

  /**
   * A predefined constant for specification of an axis type.
   * @see MapGrid
   */
  public static final int TIME_AXIS = 4;



}
