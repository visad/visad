
//
// DateTime.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.text.FieldPosition;

/**
 * DateTime is a class of objects for holding date and time information.
 * DateTime objects are immutable.<P>
 *
 * Internally, the object uses seconds since the epoch 
 * (1970-01-01 00:00:00Z) as the temporal reference.
 * @see java.lang.System#currentTimeMillis()
 * 
 */
public final class 
DateTime 
    extends	Real
{

    // Time related variables
    private static final double secondsPerDay = (double) (24 * 60 * 60);
    private static final GregorianCalendar utcCalendar =
                          new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    private static final String isoTimeFmtPattern = "yyyy-MM-dd HH:mm:ss'Z'";

/*
    // Initialize the unit used to be seconds since 1970-01-01 00:00:00Z
    private static final Unit secondsSinceTheEpoch =
        new OffsetUnit(
            visad.data.netcdf.units.UnitParser.encodeTimestamp(
                1970, 1, 1, 0, 0, 0, 0),
            SI.second);
*/

    /**
     * Construct a DateTime object and initialize it using a VisAD Real.
     * Unless the units of the Real specify otherwise, the Real's value
     * is assumed to be seconds since the Epoch (i.e. 1970-01-01 00:00:00Z).
     *
     * @param	real		Real value in a temporal unit.  If the unit is
     *				not an Offset unit, then it is assumed that the
     *				temporal origin is the beginning of the Epoch
     *				(i.e. 1970-01-01 00:00:00Z).
     *
     * @throws	VisADException	unit conversion problem
     */
    public DateTime(Real real) 
            throws VisADException
    {
	super( RealType.DateTime,
	       real.isMissing()
		   ? Double.NaN
/*
		   : real.getValue(CommonUnit.secondsSinceTheEpoch),
*/
		   : real.getUnit() instanceof OffsetUnit
		       ? real.getValue(CommonUnit.secondsSinceTheEpoch)
		       : real.getValue(SI.second),
	       CommonUnit.secondsSinceTheEpoch);

        // set up in terms of java date
        utcCalendar.setTime(new Date(Math.round(getValue()*1000.)));
    }

    /**
     * Construct a DateTime object and initialize it with the seconds since
     * January 1, 1970 00:00:00Z.
     *
     * @param  seconds  number of seconds since 1970-01-01 00:00:00Z.
     *
     * @throws	VisADException	unit conversion problem
     */
    public DateTime(double seconds) 
            throws VisADException
    {
        this(new Real(RealType.Time, 
                      seconds, 
                      CommonUnit.secondsSinceTheEpoch));
    }

    /**
     * Construct a DateTime object and initialize it with a Java date.
     *
     * @param  date  date object
     *
     * @throws	VisADException	unit conversion problem
     */
    public DateTime(Date date) 
            throws VisADException
    {
        this(new Real(RealType.Time, 
                      date.getTime()/1000., 
                      CommonUnit.secondsSinceTheEpoch));
    }

    /**
     * Construct a DateTime object and initialize it to the current date/time.
     *
     * @throws	VisADException	unit conversion problem
     */
    public DateTime() 
            throws VisADException
    {
        this(new Real(RealType.Time, 
                      System.currentTimeMillis()/1000.,
                      CommonUnit.secondsSinceTheEpoch));
    }

    /**
     * Construct a DateTime object initialized with a year, day of
     * the year, and seconds in the day.
     *
     * @param	year		year - use negative year to indicated BC
     * @param	day		day of the year
     * @param	seconds		seconds in the day
     *
     * @throws  VisADException	invalid day or seconds.  Days must be
     *				greater than zero and seconds must be greater
     *                          than zero and less than or equal to the 
     *                          seconds in a day.
     */
    public DateTime(int year, int day, double seconds) 
            throws VisADException
    {
        this(fromYearDaySeconds(year, day, seconds));
    }

    /**
     * Return a Real object whose value is the seconds since the Epoch
     * initialized with a year, day of the year, and seconds in the day.
     *
     * @param	year		year - use negative year to indicated BC
     * @param	day		day of the year
     * @param	seconds		seconds in the day
     *
     * @throws  VisADException	invalid day or seconds.  Days must be
     *				greater than zero and seconds must be greater
     *                          than zero and less than or equal to the 
     *                          seconds in a day.
     */
    public static Real fromYearDaySeconds(int year, int day, double seconds)
            throws VisADException
    {

/*  Comment out for now - may want to revisit  DRM - 1999-05-06
    Handle in trusted method to allow for BC years (year <= 0).
        if (year < 1)
        {
            throw new VisADException(
                              "DateTime.fromYearDaySeconds: invalid year");
        }
        int dayLimit = utcCalendar.isLeapYear(year) ? 366 : 365;
*/

        // require positive day
        if (day < 1) 
        {
            throw new VisADException(
                              "DateTime.fromYearDaySeconds: invalid day");
        }

        // require positive seconds and no more than are in a day.
        if (seconds > secondsPerDay || seconds < 0.0) {
            throw new VisADException(
                              "DateTime.fromYearDaySeconds: invalid seconds");
        }
        return fromYearDaySecondsTrusted(year, day, seconds);
    }

    /** trusted method for initializers */
    private static Real fromYearDaySecondsTrusted(int year, 
                                                  int day, 
                                                  double seconds) 
             throws VisADException
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (year == 0) year = -1;                // set to 1 BC
        cal.set(Calendar.ERA, year < 0 
                                ? GregorianCalendar.BC
                                : GregorianCalendar.AD);
        cal.set(Calendar.YEAR, Math.abs(year));
    
        /* 
           allow us to specify # of days since the year began without having
           worry about leap years and seconds since the day began, instead
           of in the minute.  Saves on some calculations.
        */
        cal.setLenient(true);         

        cal.set(Calendar.DAY_OF_YEAR, day);
        int temp = (int) Math.round(seconds * 1000);
        int secs = temp/1000;
        int millis = temp%1000;
        cal.set(Calendar.SECOND, secs);
        cal.set(Calendar.MILLISECOND, millis);

        return new Real( RealType.DateTime, 
                         cal.getTime().getTime()/1000.,
                         CommonUnit.secondsSinceTheEpoch );
    }

    /**
     * Get a Real representing the number of seconds since * the epoch.
     *
     * @return Real this object
     */
    public Real getReal() 
    {
        return this;
    }

    /**
     * Return a string representation of this DateTime using
     * ISO 8601 complete date plus hours, minutes and seconds.
     * See <a href="http://www.w3.org/TR/NOTE-datetime">
     *      http://www.w3.org/TR/NOTE-datetime</a>
     *
     * @return	String representing the date/time in the form 
     *          <nobr>yyyy-MM-dd HH:mm:ssZ</nobr> (ex: 1999-05-04 15:27:08Z)
     */
    public String toString() 
    {
        String pattern = 
                  (utcCalendar.get(Calendar.ERA) == GregorianCalendar.BC)
                            ? isoTimeFmtPattern + " 'BCE'"
                            : isoTimeFmtPattern;
        return formattedString(pattern, TimeZone.getTimeZone("GMT"));
    }

    /**
     * Gets a string that represents the just the value portion of this
     * DateTime -- but with full semantics.
     *
     * @return	String representing the date/time in the form 
     *          <nobr>yyyy-MM-dd HH:mm:ssZ</nobr> (ex: 1999-05-04 15:27:08Z)
     */
    public String toValueString() {
	return toString();
    }

    /**
     * Return a string representation of this DateTime from a user
     * specified format.  The pattern uses the time format syntax
     * of java.text.SimpleDateFormat and the time zone is any of the
     * valid java.util.TimeZone values.
     * @see java.text.SimpleDateFormat
     * @see java.util.TimeZone
     *
     * @param	pattern		time format string
     * @param	timezone	time zone to use
     * @return	String representing the date/time in the form specified
     *          by the pattern.
     */
    public String formattedString(String pattern, TimeZone timezone)
    {
        StringBuffer buf = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(timezone);
        if (pattern != null) sdf.applyPattern(pattern);
        return (sdf.format(utcCalendar.getTime(), buf, 
                                new FieldPosition(0))).toString();
    }

    /**
     * Return a string representing the "date" portion of this DateTime
     *
     * @return	String representing the date in the form 
     *          yyyy-MM-dd (ex: 1999-05-04)
     */
    public String dateString() 
    {
        String pattern = 
                  (utcCalendar.get(Calendar.ERA) == GregorianCalendar.BC)
                            ? "yyyy-MM-dd 'BCE'"
                            : "yyyy-MM-dd";
        return formattedString(pattern, TimeZone.getTimeZone("GMT"));
    }

    /**
     * Return a string representing the "time" portion of this DateTime
     *
     * @return	String representing the time in the form 
     *          HH:mm:ssZ (ex: 15:27:08Z)
     */
    public String timeString() 
    {
        return formattedString("HH:mm:ss'Z'", TimeZone.getTimeZone("GMT"));
    }

    /**
     *  Implement Comparable interface
     *
     * @param	oo	Object for comparison - should be DateTime
     */
    public int compareTo(Object oo)
    {
        return compareTo( (DateTime) oo );
    }

    /** 
     * run 'java visad.DateTime' to test the DateTime class 
     */
    public static void main(String args[]) throws VisADException {

        Real r;
        DateTime a;

        System.out.println(
                    "\nInitialized using DateTime(1959, 284, 36600.):");
        a = new DateTime(1959, 284, 36600.);

        System.out.println(
                      "\n\ttoString()        = " + a + 
                      "\n\tdateString()      = " + a.dateString() +
                      "\n\ttimeString()      = " + a.timeString() +
                      "\n\tformattedString() = " +
                      a.formattedString("(EEE) dd-MMM-yy hh:mm:SS.sss z",
                                               TimeZone.getTimeZone("EST")) +
                      "\n\t  (using pattern " + 
                      "'(EEE) dd-MMM-yy hh:mm:SS.sss z' and timezone 'EST')");

        // test using DateTime(Real r) where r has correct units and type
        System.out.println("\nIncrementing 5 times by 20 days each time:\n");
        for (int i = 0; i < 5; i++) 
        {
              r = new Real(RealType.Time, 
                           a.getValue() + 20 * secondsPerDay,
                           CommonUnit.secondsSinceTheEpoch);
              a = new DateTime(r);
              System.out.println("\t" + a);
         }

        // test for backward compatibility
        System.out.println("\nInitialized using Real of RealType.Time" +
                           " but no Unit (backward compatibility):");
	r = new Real(RealType.Time, a.getValue() + secondsPerDay);
	a = new DateTime(r);
	System.out.println("\n\t" + a);

        //  try BC date
        System.out.println(
                    "\nInitialized with a BCE date DateTime(-5, 196, 24493.):");
        a = new DateTime(-5, 193, 24493.);
        System.out.println(
                      "\n\ttoString()        = " + a + 
                      "\n\tdateString()      = " + a.dateString() +
                      "\n\ttimeString()      = " + a.timeString());

        // test using Date() values
        Date date = new Date();
        a = new DateTime(date);
        System.out.println("\nInitialized with current Date(): " + a);
        a = new DateTime(date.getTime()/1000.);
        System.out.println(
                  "\nInitialized with current seconds since the epoch: " 
                            + a + "\n");

    }

/* Here's the output:

java visad.DateTime

Initialized using DateTime(1959, 284, 36600.):

        toString()        = 1959-10-11 10:10:00Z
        dateString()      = 1959-10-11
        timeString()      = 10:10:00Z
        formattedString() = (Sun) 11-Oct-59 06:10:00.000 EDT
          (using pattern '(EEE) dd-MMM-yy hh:mm:SS.sss z' and timezone 'EST')

Incrementing 5 times by 20 days each time:

        1959-10-31 10:10:00Z
        1959-11-20 10:10:00Z
        1959-12-10 10:10:00Z
        1959-12-30 10:10:00Z
        1960-01-19 10:10:00Z

Initialized using Real of RealType.Time but no Unit (backward compatibility):

        1960-01-20 10:10:00Z

Initialized with a BCE date DateTime(-5, 196, 24493.):

        toString()        = 0005-07-11 06:48:13Z BCE
        dateString()      = 0005-07-11 BCE
        timeString()      = 06:48:13Z

Initialized with current Date(): 1999-05-06 23:01:41Z

Initialized with current seconds since the epoch: 1999-05-06 23:01:41Z

*/

}
