package dods.clients.importwizard.TMAP.convert;

import java.util.StringTokenizer;

/**
 * A class for conversions between <tt>double</tt> and
 * <tt>String</tt> along a length axis.  The axis will have
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
public class ConvertLength extends Convert {

  /**
   * The strings recognized as valid units.  (always lower case)
   *
   */
  private String recognizedUnits[] = {
    "m", "meter", "meters"
  };


  /**
   * Creates a <B>Convert</B> object.
   *
   */
  public ConvertLength() {
  }


  /**
   * Converts a <tt>String<tt> value to a <tt>double<tt>.
   *
   * @param s the string to be converted.
   */
  public double toDouble(String s) throws IllegalArgumentException {

    StringTokenizer st, check;
    String val_s, unit_s;
    String len_s = new String(s);     // so we don't corrupt s
    double len = 0;

    len_s = len_s.trim();
    len_s = len_s.toUpperCase();

    // this hack makes sure we only
    // have "good" characters
    check = new StringTokenizer(len_s," +-.0123456789",false);
    if (check.countTokens() > 0) throw new IllegalArgumentException("Bad character in string: \"" + s + "\".");

    // break up by spaces
    st = new StringTokenizer(len_s," ",false);
    switch(st.countTokens()) {

      // only one token: check the value, assume default units
      //
    case (1):
      val_s = st.nextToken();

      try { 
	len = Double.valueOf(val_s).doubleValue();
      } catch (NumberFormatException e) {
	throw  new IllegalArgumentException(e.toString());
      }
      break;

      // two tokens: check the value and check the units
      //
    case (2):
      val_s = st.nextToken();
      unit_s = st.nextToken();

      unitTest(unit_s);

      try { 
	len = Double.valueOf(val_s).doubleValue();
      } catch (NumberFormatException e) {
	throw  new IllegalArgumentException(e.toString());
      }
      
      break;
      
    default:
      throw  new IllegalArgumentException("Too many spaces in input: \"" + s + "\".");

    }

      // Now check the range
      //
    try {
      len = rangeTest(len);
    } finally {
      ;
    }

    return len;
  }
 
  
  /**
   * Converts a <tt>double<tt> value to a <tt>String<tt>.
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

    sbuf.append(val);

    return sbuf.toString();
  }
 
  
}
