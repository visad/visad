package dods.clients.importwizard.TMAP.convert;

import java.util.StringTokenizer;

/**
 * A class for conversions between <tt>double</tt> and
 * <tt>String</tt> along a Longitude axis.  The axis will have 
 * a range of acceptable values.  This package is designed
 * to be hooked up with TextInputFields for region specification
 * on database servers.
 *
 * @version	0.1,   Sep 03, 1997
 * @author	Jonathan Callahan
 *
 * This class may change substantially when ported to JDK 1.1 which
 * contains a java.text.Format class.  In the future, Convert and its
 * subclasses may extend that class.
 *
 * This class may change substantially when ported to JDK 1.1 which
 * contains a java.text.Format class.  In the future, Convert and its
 * subclasses may extend that class.
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
public class ConvertLongitude extends Convert {

  public static final int M180_180 = 0;
  public static final int ZERO_360 = 1;
  public static final int E_W = 2;
  public static final int SPACE_E_W = 3;
  public static final int SPACE_EAST_WEST = 4;

  /**
   * The strings recognized as valid units.  (always lower case)
   *
   */
  private String recognizedUnits[] = {
    "deg", "degrees"
  };


  /**
   * Creates a <B>Convert</B> object.
   *
   */
  public ConvertLongitude() {
    setRange(-180.0, 180.0);
  }


  /**
   * Creates a <B>ConvertLongitude</B> object with a predefined
   * outputStyle.
   *
   * @param	<TT>style</TT>	one of the supported output styles.
   */
  public ConvertLongitude(int style) {
    this();
    this.outputStyle = style;
  }

  /**
   * Sets the output style for the toString() method.
   *
   * Supported output styles are:<BR>
   * <UL>
   *    <LI>default (-115.25)
   *    <LI>ZERO_360 (244.75)
   *    <LI>E_W (115.25W)
   *    <LI>SPACE_E_W (115.25 W)
   *    <LI>SPACE_EAST_WEST (115.25 West)
   * </UL>
   */
  public void setOutputStyle(int style) {
    outputStyle = style;
  }


  /**
   * Sets the valid range for the <b>ConvertLongitude</b> object.
   * All values are forced to be in the [-360:360] range.  The 
   * range may have (hi < lo) as in [150:-90] but if more than one
   * world is specified, (abs(hi-lo) > 360), all values will
   * be converted to [-180:180].  When exactly one world is 
   * specified then the lo and hi values will be used such that
   * (lo < hi).
   *
   * Thus (20:380)  --> [-340:20]
   *  and (20:378)  --> [-340:18]
   *  but (20:18)   --> [20:18]
   *
   * Keep (150:-70) --> [150:-170]
   *      (40:280)  --> [40:280]
   *
   * @param lo the lowest acceptable value.
   * @param hi the highest acceptable value.
   */
  public void setRange(double lo, double hi) throws IllegalArgumentException {
    double rlo = lo;
    double rhi = hi;

    // Something like [20:380] will be converted to [-340:20]
    if ( rlo > 0 && rhi > 360 ) {
      System.out.println("ConvertLongitude: setRange(" + lo + ", " + hi +
                         ") subtracting 360 to fit within internal [-360:360] range.");
      rlo -= 360;
      rhi -= 360;
    } else if ( rlo < -360 && rhi < 0 ) {
      System.out.println("ConvertLongitude: setRange(" + lo + ", " + hi +
                         ") adding 360 to fit within internal [-360:360] range.");
      rlo += 360;
      rhi += 360;
    }

    // Preserve -360 and 360 but modulo anything outside that range
    if ( rlo < -360 || rlo > 360.0 ) {
      rlo = rlo%360;
    }
    if ( rhi < -360 || rhi > 360.0 ) {
      rhi = rhi%360;
    }

    if ( Math.abs(hi-lo) > 360.0 ) {
      // Something is screwy so force conversion to [-180:180]
      if (rlo < -180) rlo += 360;
      if (rhi < -180) rhi += 360;
      if (rlo >  180) rlo -= 360;
      if (rhi >  180) rhi -= 360;
    } else if ( Math.abs(hi-lo) == 360.0 ) {
      // put rlo before rhi if it's the whole world
      if (rhi < rlo) {
        range[LO] = rhi;
        range[HI] = rlo;
      }
    } 

    range[LO] = rlo;
    range[HI] = rhi;

  }


  /**
   * Converts a Longitude string to a double value.
   *
   * Acceptable strings are:<BR>
   * &quot45&quot;<BR>
   * &quot;45.5&quot;<BR>
   * &quot;-45&quot;<BR>
   * &quot;45E&quot;<BR>
   * &quot;-45W&quot;  (same as 45E)<BR>
   * &quot;45 54W&quot;  (= 45 degrees, 54 minutes West)<BR>
   * &quot;45 54.5&quot;  (= 45 degrees, 54.5 minutes East)<BR>
   * &quot;45 54 30&quot;  (= 45 degrees, 54 minutes, 30 seconds East)<BR>
   * &quot;455430N&quot;  (= 45 degrees, 54 minutes, 30 seconds East (USGS format))<BR>
   *
   * @param	<TT>s</TT>	String representing the Longitude.
   * @return	The double represented by the String.
   * @exception 	IllegalArgumentException	if <TT>s</TT> cannot be interpreted.
   *
   */
  public double toDouble(String s) throws IllegalArgumentException {
    StringTokenizer st, check;
    double lon = 0;
    int Hemi = 1;
    String deg, min, sec;


    String lons = new String(s);     // so we don't corrupt s
    lons = lons.trim();
    lons = lons.toUpperCase();

    // Check if we have "E" or "W" at the end.
    // If yes split it off as well as any whitespace.
    //
    if (lons.endsWith("E")) {
      Hemi = 1;
      lons = lons.substring(0,lons.length()-1).trim();
    }
    else if (lons.endsWith("W")) {
      Hemi = -1;
      lons = lons.substring(0,lons.length()-1).trim();
    }

    // this hack makes sure we only
    // have "good" characters
    check = new StringTokenizer(lons," +-.0123456789",false);
    if (check.countTokens() > 0) throw new IllegalArgumentException("Bad character in string: \"" + s + "\".");

    // break up by spaces
    st = new StringTokenizer(lons," ",false);
    switch(st.countTokens()) {

      // only one token
      //
    case (1):

      if ( lons.length() > 4 && lons.indexOf('.') == -1 ) {
	// several digits and no decimal point => format = ddmmss
	//
	deg = lons.substring(0,lons.length()-4);
	min = lons.substring(lons.length()-4,lons.length()-2);
	sec = lons.substring(lons.length()-2,lons.length()-0);
	try { 
	  lon = Double.valueOf(deg).doubleValue();
	  lon += Double.valueOf(min).doubleValue() / 60.0;
	  lon += Double.valueOf(sec).doubleValue() / 3600.0;
	} catch (NumberFormatException e) {
	  throw  new IllegalArgumentException(e.toString());
	}

      } else {
	// format = deg with decimals
	//
	try { 
	  lon = Double.valueOf(lons).doubleValue();
	} catch (NumberFormatException e) {
	  throw  new IllegalArgumentException(e.toString());
	}
      }
      break;

      // two tokens => format = "deg mm.m"
      //
    case (2):

      deg = st.nextToken();
      min = st.nextToken(); 
      try { 
	lon = Double.valueOf(deg).doubleValue();
	lon += Double.valueOf(min).doubleValue() / 60.;
      } catch (NumberFormatException e) {
	throw  new IllegalArgumentException(e.toString());
      }
      break;

      // three tokens => format = "deg min sec"
      //
    case (3):

      deg = st.nextToken();
      min = st.nextToken(); 
      sec = st.nextToken(); 
      try { 
	lon = Double.valueOf(deg).doubleValue();
	lon += Double.valueOf(min).doubleValue() / 60.0;
	lon += Double.valueOf(sec).doubleValue() / 3600.0;
      } catch (NumberFormatException e) {
	throw  new IllegalArgumentException(e.toString());
      }

      break;

    default:
      throw  new IllegalArgumentException();
    }

    lon *= Hemi;
    try {
      lon = rangeTest(lon);
    } finally {
      ;
    }

    return lon;

  }


  /**
   * Converts a <tt>double</tt> value to a Longitude <tt>String</tt>.
   *
   * @param val double value.
   * @return String the string representation of the input.
   */
  public String toString(double val) {

    StringBuffer sbuf = new StringBuffer("");
 
    try {
      val = rangeTest(val);
	} catch (IllegalArgumentException e) {
      System.out.println("toString(" + val + "," + "): " + e);
    }

    if ( val < 0.0 ) {

      switch (outputStyle) {
      case ZERO_360:
        sbuf.append(val+360);
        break;
      case E_W:
        sbuf.append(-val);
        sbuf.append("W");
        break;
      case SPACE_E_W:
        sbuf.append(-val);
        sbuf.append(" W");
        break;
      case SPACE_EAST_WEST:
        sbuf.append(-val);
        sbuf.append(" West");
        break;
      default:
        sbuf.append(val);
        break;
      }

    } else {

      switch (outputStyle) {
      case ZERO_360:
        sbuf.append(val);
        break;
      case E_W:
        sbuf.append(val);
        sbuf.append("E");
        break;
      case SPACE_E_W:
        sbuf.append(val);
        sbuf.append(" E");
        break;
      case SPACE_EAST_WEST:
        sbuf.append(val);
        sbuf.append(" East");
        break;
      default:
        sbuf.append(val);
        break;
      }

    }
 
    return sbuf.toString();
  }
 

  /**
   * Returns the nearest value within the range.
   *
   * @param	<TT>lon</TT>	The value of the Longitude.
   * @return the nearest value within the range.
   */

  public double getNearestValue(double val) {
	try { 
      return rangeTest(val);
	} catch (IllegalArgumentException e) {
      System.out.println("getNearestValue(" + val + "): " + e);
      if ( range[LO] < range[HI]) {
        if ( val < range[LO] ) 
          return range[LO];
        else
          return range[HI];
      } else {
        if ( val > range[LO] ) 
          return range[LO];
        else
          return range[HI];
      }
	}
  }


  /**
   * Returns the intersection of the incoming range within the 
   * internal range.  An error is returned if there is no intersection.
   *
   * @param <TT>val_lo</TT> The lo value of the range to be tested.
   * @param <TT>val_hi</TT> The hi value of the range to be tested.
   * @return the nearest value within the range.
   * @exception IllegalArgumentException <TT>range</TT> is outside
   * the internally defined range.
   */
  public double [] intersectRange(double val_lo, double val_hi) throws IllegalArgumentException {
    double vlo = val_lo;
    double vhi = val_hi;
    double rlo = range[LO];
    double rhi = range[HI];
    double [] return_vals = {rlo, rhi};

    // Preserve -360 and 360 but modulo anything outside that range
    if ( vlo < -360 || vlo > 360.0 )
      vlo = vlo%360;

    if ( vhi < -360 || vhi > 360.0 )
      vhi = vhi%360;

    // If the inCOMING range represents the entire globe,
    // then return the internal range values.

    if ( Math.abs(vhi-vlo) == 360.0 ) {
      return return_vals;
    }

    // If the inTERNAL range represents the entire globe, 
    // then every incoming range is valid and must only
    // be adjusted to fit into the internal range.

    if ( Math.abs(rhi-rlo) == 360.0 ) {
      try {
        return_vals[LO] = rangeTest(vlo);
        return_vals[HI] = rangeTest(vhi);
        return return_vals;
	  } catch (IllegalArgumentException e) {
	    throw  new IllegalArgumentException("This should never happen! " + e.toString());
	  }
    }

    // We have a sub-global internal range and the
    // incoming range is also sub-global.
    // Convert everything to [-180:180]

    if (vlo > 180)
      vlo -=360;
    else if (vlo < -180)
      vlo +=360;

    if (vhi > 180)
      vhi -=360;
    else if (vhi < -180)
      vhi +=360;

    if (rlo > 180)
      rlo -=360;
    else if (rlo < -180)
      rlo +=360;

    if (rhi > 180)
      rhi -=360;
    else if (rhi < -180)
      rhi +=360;

    // Adjust ranges that straddle the dateline
    // Now everything will be [-180:540] but we are
    // guaranteed that (lo < hi)

    if ( rhi < rlo )
      rhi += 360.0;

    if ( vhi < vlo )
      vhi += 360.0;

    // Now slide the values over by 360 if necessary so that
    // we are guaranteed to have an overlap if one exists.
    //
    // e.g. range=[-180:-150], vals=[-140:-150]-->[-140:410]
    // We need to change vals again to vals=[-500:-150].
    // 
    // For double overlap situations: r=[-150:150], v=[120:-120],
    // the region returned will always be on the right hand side
    // of the internal range.

    if ( vlo > rhi ) {
      vlo -= 360.0;
      vhi -= 360.0;
    } else if ( vhi < rlo ) {
      vlo += 360.0;
      vhi += 360.0;
    }

    // Now find the overlap.

    if ( vlo > rhi || vhi < rlo ) {
      throw new IllegalArgumentException("incoming range [" + val_lo + ", " +
val_hi + "] does not intersect range[" + range[LO] + "," + range[HI] + "].");
    } 

    // Now we are guaranteed some overlap.

    if ( vlo > rlo ) {
      try {
        return_vals[LO] = rangeTest(vlo);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("This should never happen! " + e.toString());
      }
    } else {
      return_vals[LO] = range[LO];
    }

    if ( vhi < rhi ) {
      try {
        return_vals[HI] = rangeTest(vhi);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("This should never happen! " + e.toString());
      }
    } else {
      return_vals[HI] = range[HI];
    }

   return return_vals;
 
  }


  /**
   * Tests a value against the range.
   *
   * @param	<TT>lon</TT>	The value of the Longitude.
   * @return The value passed in, possibly shifted by 360 deg
   *         to fit within the range.
   * @exception 	IllegalArgumentException	if <TT>val</TT> is 
   * outisde the specified range.
   */
  protected double rangeTest(double val) throws IllegalArgumentException {
    double return_val = val;
    double newval = val;
    double rlo = range[LO];
    double rhi = range[HI];
    boolean point_range = false;

    if ( rlo == rhi )
      point_range = true;

    // Preserve -360 and 360 but modulo anything outside that range
    if ( return_val < -360 || return_val > 360.0 ) {
      return_val = return_val%360;
    }
    if ( newval < -360 || newval > 360.0 ) {
      newval = newval%360;
    }

    // Convert everything to [-180:180]
    if (newval > 180)
      newval -=360;
    else if (newval < -180)
      newval +=360;

    if (rlo > 180)
      rlo -=360;
    else if (rlo < -180)
      rlo +=360;

    if (rhi > 180)
      rhi -=360;
    else if (rhi < -180)
      rhi +=360;

    // Adjust rhi as necessary
    if ( rhi == rlo && !point_range )
      rhi +=360;

    // Adjust ranges that straddle the dateline
    if ( rhi < rlo ) {
      rhi += 360.0;
    }

    // Give newval one chance to get within the range.
    if ( newval < rlo ) {
      newval += 360.0;
    }

    if ( newval < rlo || newval > rhi ) {
      throw new IllegalArgumentException("value [" + val + "] outside of range[" +
					 range[LO] + "," + range[HI] + "].");
    } else {
      // We know everything is [-360:360] and that
      // the value is in the range.  So this should
      // cover everything
      if ( range[HI] < range[LO] ) { // i.e. [150:-150]
        if (return_val < (range[LO]-360))
          return_val += 360;
        else if (return_val > (range[HI]+360))
          return_val -= 360;
      } else {
        if (return_val < range[LO])
          return_val += 360;
        else if (return_val > range[HI])
          return_val -= 360;
      }

    } 

    return return_val;
  }


}
