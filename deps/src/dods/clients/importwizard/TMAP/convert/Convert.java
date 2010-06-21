package dods.clients.importwizard.TMAP.convert;

/**
 * An abstract class for conversions between <tt>double</tt> and
 * <tt>String</tt> along an axis.  The axis will have 
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
 */
public abstract class Convert {

  protected static final int LO = 0;
  protected static final int HI = 1;

  /**
   * The output style as defined by individual Convert objects.
   */
  protected int outputStyle = 0;

  /**
   * The range within which a value is acceptable.
   * <p>
   * This can be used if you have a region of interest and you
   * wish to do bounds checking at the level of the input before
   * passing to value to any other routines.
   */
  protected double [] range = {-1.0, 1.0};

  /**
   * The base units in which all <tt>double</tt> values will be expressed.
   *
   */
  protected String units = new String("");

  /**
   * The strings recognized as valid units.  (always lower case)
   * Standard units are specified in individual subclasses.
   */
  protected String recognizedUnits[];


  /**
   * Creates a <B>Convert</B> object.
   *
   */
  public Convert() {
  }


  /**
   * Returns a String describing the base units.  For example:
   * <tt>m, km, ft, sec, msec, deg E, etc.</tt>
   *
   */
  public String getUnits() {
    return units;
  }

  
  /**
   * Sets the valid range for the <b>Convert</b> object.
   * range[LO] < range[HI] regardless of the order in which
   * they are passed in.
   *
   * @param lo the lowest acceptable value.
   * @param hi the highest acceptable value.
   */
  public void setRange(double lo, double hi) throws IllegalArgumentException {
    if (hi < lo) {
      System.out.println("Convert:setRange(" + lo + "," + hi +
                         ") -- swapping lo and hi limits");
      range[LO] = hi;
      range[HI] = lo;
    }
    range[LO] = lo;
    range[HI] = hi;
  }


  /**
   * Returns the LO [0] or HI [1] value from the range of acceptable values.
   * 
   * @param index the LO [0] or HI [1] index.
   * @return      the requested value.
   *
   */
  public double getRange(int index) {
    return range[index];
  }
  
  /**
   * Returns the LO [0] and HI [1] values of the range
   * as an array of doubles.
   *
   * @return the lo and hi valus of the range.
   */
  public double [] getRange() {
    double [] return_vals = {range[LO], range[HI]};
    return return_vals;
  }


  /**
   * Sets the String describing the base units.  For example:
   * <tt>m, km, ft, sec, msec, deg E, etc.</tt>
   * Standard units are specified in individual subclasses.
   *
   * @param u the string describing the base units.
   * @exception IllegalArgumentException the unit string is not recognized.
   */
  public void setUnits(String u) throws IllegalArgumentException {
    units = new String(unitTest(u));
  }
  

  /**
   * Converts a <tt>double<tt> value to a <tt>String<tt>.
   *
   * This is the <code>abstract</code> method which makes this class abstract.
   *
   * @param string the string to be converted.
   */
  public abstract double toDouble(String s) throws IllegalArgumentException;
 
  
  /**
   * Prints out the internal properties.
   *
   * @return String the internal properties of this converter.
   */
  public String toString() {
    StringBuffer sbuf = new StringBuffer("");
    sbuf.append("Convert:range = [" + range[LO] + ", " + range[HI] + "]");
    return sbuf.toString();
  }


  /**
   * Converts a <tt>String<tt> value to a <tt>double<tt>.
   *
   * This is the <code>abstract</code> method which makes this class abstract.
   *
   * @param val the string to be converted.
   */
  public abstract String toString(double val) throws IllegalArgumentException;
 
  
  /**
   * Returns the nearest value within the range.
   *
   * @param	<TT>val</TT>	The value passed in.
   * @return the nearest value within the range.
   */
  public double getNearestValue(double val) {
    try {
      return rangeTest(val);
    } catch (IllegalArgumentException e) {
      if ( val < range[LO] ) {
        val = range[LO];
      } else {
        val = range[HI];
      }
      System.out.println("Convert:getNearestValue(" + val + "): " + e +
      ", returning " + val);
      return val;
    }
  }


  /**
   * Returns the nearest value within the range.
   * If the value is completely outside the range, the lo_hi
   * parameter is used to determine which end of the range
   * to return.
   *
   * @param <TT>val</TT>    The value passed in.
   * @param lo_hi which end of the range to return.
   * @return the nearest value within the range.
   */
  public double getNearestValue(double val, int lo_hi) {
    try {
      return rangeTest(val);
    } catch (IllegalArgumentException e) {
      System.out.println("Convert:getNearestValue(" + val + "," + lo_hi + "): " + e +
      ", returning " + range[lo_hi]);
      return range[lo_hi];
    }
  }


  /**
   * Returns the intersection of the incoming range within the 
   * internal range.  An error is returned if there is no intersection.
   *
   * @param	<TT>val_lo</TT>	The lo value of the range to be tested.
   * @param	<TT>val_hi</TT>	The hi value of the range to be tested.
   * @return the nearest value within the range.
   * @exception IllegalArgumentException <TT>range</TT> is outside
   * the internally defined range.
   */
  public double [] intersectRange(double val_lo, double val_hi) throws IllegalArgumentException {
    double [] return_vals = {range[LO], range[HI]};
    double [] vals = {val_lo, val_hi};

    if ( val_hi < val_lo ) {
      vals[LO] = val_hi;
      vals[HI] = val_lo;
    }

    if ( vals[LO] > range[HI] ) {
      throw new IllegalArgumentException("incoming range [" + val_lo + ", " +
val_hi + "] does not intersect range[" + range[LO] + "," + range[HI] + "].");
    } else if ( vals[LO] > range[LO] ) {
      return_vals[LO] = vals[LO];
    } else {
      return_vals[LO] = range[LO];
    }

    if ( vals[HI] < range[LO] ) {
      throw new IllegalArgumentException("incoming range [" + val_lo + ", " +
val_hi + "] does not intersect range[" + range[LO] + "," + range[HI] + "].");
    } else if ( vals[HI] < range[HI] ) {
      return_vals[HI] = vals[HI];
    } else {
      return_vals[HI] = range[HI];
    }

    return return_vals;
  }


  /*
   * Tests a value against the range.
   *
   * @param <tt>val</tt>
   * @return The value passed in.
   * @exception IllegalArgumentException <TT>val</TT> is 
   * outisde the specified range.
   */
  protected double rangeTest(double val) throws IllegalArgumentException {
    if ( val < range[LO] || val > range[HI] ) {
      throw new IllegalArgumentException("value [" + val + "] outside of range[" +
					 range[LO] + "," + range[HI] + "].");
    }
    return val;
  }


  /*
   * Test the String describing the base units against the 
   * <tt>recognizedUnits</tt> array.
   *
   * @param u the string describing the base units.
   * @exception IllegalArgumentException the unit string is not recognized.
   */
  protected String unitTest(String u) throws IllegalArgumentException {

    String s = new String(u);     // so we don't corrupt u
    s = s.trim();
    s = s.toLowerCase();

    for (int i=1; i<recognizedUnits.length; i++) {
      if ( s.equalsIgnoreCase(recognizedUnits[i]) ) {
	return s;
      }
    }

    throw new IllegalArgumentException("Unit \"" + s + "\" not recognized.");
  }
  

}
