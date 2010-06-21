package dods.clients.importwizard.TMAP.convert;

import java.util.StringTokenizer;

/**
 * A class for conversions between <tt>double</tt> and
 * <tt>String</tt> along a Latitude axis.  The axis will have
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
public class ConvertLatitude extends Convert {

  public static final int M90_90 = 0;
  public static final int N_S = 1;
  public static final int SPACE_N_S = 2;
  public static final int SPACE_NORTH_SOUTH = 3;

  /**
   * The strings recognized as valid units.  (always lower case)
   *
   */
  private String recognizedUnits[] = {
    "deg", "degrees"
  };


  /**
   * Creates a <B>ConvertLatitude</B> object.
   *
   */
  public ConvertLatitude() {
    setRange(-90.0, 90.0);
  }


  /**
   * Creates a <B>ConvertLatitude</B> object with a predefined
   * outputStyle.
   *
   * @param	<TT>style</TT>	one of the supported output styles.
   */
  public ConvertLatitude(int style) {
    this();
    this.outputStyle = style;
  }

  /**
   * Sets the output style for the toString() method.
   *
   * Supported output styles are:<BR>
   * <UL>
   *    <LI>M90_90[default] (-15.25)
   *    <LI>N_S (15.25S)
   *    <LI>SPACE_N_S (15.25 S)
   *    <LI>SPACE_NORTH_SOUTH (15.25 South)
   * </UL>
   * @param	<TT>style</TT>	one of the supported output styles.
   */
  public void setOutputStyle(int style) {
    this.outputStyle = style;
  }


  /**
   * Converts a latitude string to a double value.
   *
   * Examples of
   * acceptable strings are:<BR>
   * &quot45&quot;<BR>
   * &quot;45.5&quot;<BR>
   * &quot;-45&quot;<BR>
   * &quot;45N&quot;<BR>
   * &quot;-45S&quot;  (same as 45N)<BR>
   * &quot;45 54S&quot;  (= 45 degrees, 54 minutes South)<BR>
   * &quot;45 54.5&quot;  (= 45 degrees, 54.5 minutes North)<BR>
   * &quot;45 54 30&quot;  (= 45 degrees, 54 minutes, 30 seconds North)<BR>
   * &quot;455430N&quot;  (= 45 degrees, 54 minutes, 30 seconds North (USGS format))<BR>
   *
   * @param	<TT>s</TT>	String representing the Latitude.
   * @return	The double represented by the String.
   * @exception 	IllegalArgumentException	if <TT>s</TT> cannot be interpreted.
   *
   */
  public double toDouble(String s) throws IllegalArgumentException {
    StringTokenizer st, check;
    String lats = new String(s);     // so we don't corrupt s
    String deg, min, sec;
    double lat = 0;
    int Hemi = 1;

    lats = lats.trim();
    lats = lats.toUpperCase();

    // Check if we have "N" or "S" at the end.
    // If yes split it off as well as any whitespace.
    //
    if (lats.endsWith("N")) {
      Hemi = 1;
      lats = lats.substring(0,lats.length()-1).trim();
    }
    else if (lats.endsWith("S")) {
      Hemi = -1;
      lats = lats.substring(0,lats.length()-1).trim();
    }

    // this hack makes sure we only
    // have "good" characters
    check = new StringTokenizer(lats," +-.0123456789",false);
    if (check.countTokens() > 0) throw new IllegalArgumentException("Bad character in string: \"" + s + "\".");

    // break up by spaces
    st = new StringTokenizer(lats," ",false);
    switch(st.countTokens()) {

      // only one token
      //
    case (1):

      if ( lats.length() > 4 && lats.indexOf('.') == -1 ) {
	// several digits and no decimal point => format = ddmmss
	//
	deg = lats.substring(0,lats.length()-4);
	min = lats.substring(lats.length()-4,lats.length()-2);
	sec = lats.substring(lats.length()-2,lats.length()-0);
	try { 
	  lat = Double.valueOf(deg).doubleValue();
	  lat += Double.valueOf(min).doubleValue() / 60.0;
	  lat += Double.valueOf(sec).doubleValue() / 3600.0;
	} catch (NumberFormatException e) {
	  throw  new IllegalArgumentException(e.toString());
	}

      } else {
	// format = deg with decimals
	//
	try { 
	  lat = Double.valueOf(lats).doubleValue();
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
	    lat = Double.valueOf(deg).doubleValue();
	    lat += Double.valueOf(min).doubleValue() / 60.;
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
	    lat = Double.valueOf(deg).doubleValue();
	    lat += Double.valueOf(min).doubleValue() / 60.0;
	    lat += Double.valueOf(sec).doubleValue() / 3600.0;
      } catch (NumberFormatException e) {
	    throw  new IllegalArgumentException(e.toString());
      }

      break;

    default:
      throw  new IllegalArgumentException();
    }

    lat *= Hemi;
    try {
      lat = rangeTest(lat);
    } finally {
      ;
    }

    return lat;

  }


  /**
   * Converts a <tt>double<tt> value to a Latitude <tt>String<tt>.
   *
   * @param val the string to be converted.
   * @return String the value converted to a String.
   */
  public String toString(double val) throws IllegalArgumentException {
    
    StringBuffer sbuf = new StringBuffer("");
 
    try {
      val = rangeTest(val);
    } finally {
      ;
    }

    if ( val < 0.0 ) {

      switch (outputStyle) {
      case N_S:
	    sbuf.append(-val);
	    sbuf.append("S");
	    break;
      case SPACE_N_S:
	    sbuf.append(-val);
	    sbuf.append(" S");
	    break;
      case SPACE_NORTH_SOUTH:
	    sbuf.append(-val);
	    sbuf.append(" South");
	    break;
      default:
	    sbuf.append(val);
	    break;
      }

    } else {

      switch (outputStyle) {
      case N_S:
	    sbuf.append(val);
	    sbuf.append("N");
	    break;
      case SPACE_N_S:
	    sbuf.append(val);
	    sbuf.append(" N");
	    break;
      case SPACE_NORTH_SOUTH:
	    sbuf.append(val);
	    sbuf.append(" North");
	    break;
      default:
	    sbuf.append(val);
	    break;
      }

    }
 
    return sbuf.toString();
  } 
  
}
