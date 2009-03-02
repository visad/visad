//
// DateTime.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
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
    extends     Real
{

    /** This is around so we can use a different date formatter */
    private static Class dateFormatClass;


    /**
     * Default Time Format Pattern (yyyy-MM-dd HH:mm:ss)
     */
    public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Default Time Zone (GMT)
     */
    public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT");

    // Time related variables
    private static String formatPattern = DEFAULT_TIME_FORMAT;
    private static TimeZone timeZone = DEFAULT_TIMEZONE;

    //Don't create this right away now
    private GregorianCalendar utcCalendar;

    private static final double secondsPerDay = (double) (24 * 60 * 60);

    /**
     * Construct a DateTime object and initialize it using a VisAD Real.
     * Unless the units of the Real specify otherwise, the Real's value
     * is assumed to be seconds since the Epoch (i.e. 1970-01-01 00:00:00Z).
     *
     * @param   real            Real value in a temporal unit.
     *
     * @throws  VisADException  unit conversion problem
     */
    public DateTime(Real real)
            throws VisADException
    {
        super( RealType.Time,
               real.getValue(CommonUnit.secondsSinceTheEpoch),
               CommonUnit.secondsSinceTheEpoch);

        //We create and set the utcCalendar when we need it
        // set up in terms of java date
	//        utcCalendar.setTime(new Date(Math.round(getValue()*1000.)));
    }

    /**
     * Construct a DateTime object and initialize it with the seconds since
     * January 1, 1970 00:00:00Z.
     *
     * @param  seconds  number of seconds since 1970-01-01 00:00:00Z.
     *
     * @throws  VisADException  unit conversion problem
     */
    public DateTime(double seconds)
            throws VisADException
    {
        super(RealType.Time,
              seconds,
              CommonUnit.secondsSinceTheEpoch,null);
        //        this(seconds, CommonUnit.secondsSinceTheEpoch);
    }

    /**
     * Construct a DateTime object from a tim value and a Unit
     * @param  timeValue  value of time in timeUnits
     * @param  timeUnits  units of value
     */
     public DateTime(double timeValue, Unit timeUnits) 
            throws VisADException {
         this(new Real(RealType.Time, timeValue, timeUnits));
     }


    /**
     * Construct a DateTime object and initialize it with a Java date.
     *
     * @param  date  date object
     *
     * @throws  VisADException  unit conversion problem
     */
    public DateTime(Date date)
            throws VisADException
    {
	//Just go directly to the super with the Date value
        super( RealType.Time,
	       date.getTime()/1000.,
               CommonUnit.secondsSinceTheEpoch, null);
    }

    /**
     * Construct a DateTime object and initialize it to the current date/time.
     *
     * @throws  VisADException  unit conversion problem
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
     * @param   year            year - use negative year to indicated BC
     * @param   day             day of the year
     * @param   seconds         seconds in the day
     *
     * @throws  VisADException  invalid day or seconds.  Days must be
     *                          greater than zero and seconds must be greater
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
     * @param   year            year - use negative year to indicated BC
     * @param   day             day of the year
     * @param   seconds         seconds in the day
     *
     * @throws  VisADException  invalid day or seconds.  Days must be
     *                          greater than zero and seconds must be greater
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
        int dayLimit = getCalendar().isLeapYear(year) ? 366 : 365;
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
        cal.setTimeZone(DEFAULT_TIMEZONE);        // use GMT as default
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

        return new Real( RealType.Time,
                         cal.getTime().getTime()/1000.,
                         CommonUnit.secondsSinceTheEpoch );
    }

    /**
     * Get a Real representing the number of seconds since * the epoch.
     *
     * @return this object
     */
    public Real getReal()
    {
        return this;
    }


    /** 
     * Create, if needed, and return the utcCalendar 
     *
     * @returns The Calendar object to use
     */
    private Calendar getCalendar() 
    {
	if(utcCalendar == null) {
	    utcCalendar =new GregorianCalendar(DEFAULT_TIMEZONE);
	    utcCalendar.setTime(new Date(Math.round(getValue()*1000.)));
	}	    
	return utcCalendar;
    }


    /**
     * Return a string representation of this DateTime.  Unless the
     * setFormatPattern() and/or setTimeZone() methods were used, the
     * default it the ISO 8601 complete date plus hours, minutes and seconds
     * and a time zone of UTC.
     * See <a href="http://www.w3.org/TR/NOTE-datetime">
     *      http://www.w3.org/TR/NOTE-datetime</a>
     * @see #setFormatPattern
     * @see #setFormatTimeZone
     *
     * @return  String representing the date/time.  Default is
     *          <nobr>yyyy-MM-dd HH:mm:ssZ</nobr> (ex: 1999-05-04 15:27:08Z)
     */
    public String toString()
    {
        String pat = formatPattern;
        if (formatPattern.equals(DEFAULT_TIME_FORMAT)) {
            if (timeZone.equals(DEFAULT_TIMEZONE)) {
                pat = DEFAULT_TIME_FORMAT+"'Z'";
            } else {
                pat = pat + " z";
            }
            if (getCalendar().get(Calendar.ERA) == GregorianCalendar.BC) {
                pat = pat + " 'BCE'";
            }
        }
        return formattedString(pat, timeZone);
    }

    /**
     * Gets a string that represents just the value portion of this
     * DateTime -- but with full semantics.
     *
     * @return  String representing the date/time in the form
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
     * @param   pattern         time format string
     * @param   timezone        time zone to use
     * @return  String representing the date/time in the form specified
     *          by the pattern.
     */
    public String formattedString(String pattern, TimeZone timezone)
    {
        StringBuffer buf = new StringBuffer();
        DateFormat sdf = null;
        if(dateFormatClass!=null) {
            try {
                sdf = (DateFormat) dateFormatClass.newInstance();
            } catch(Exception ie) {
                throw new IllegalStateException("Error creating a DateFormat from:" + dateFormatClass.getName() +" " + ie);
            }
        } else {
            sdf = new SimpleDateFormat();
        }
        sdf.setTimeZone(timezone);
        if (pattern != null&& sdf instanceof SimpleDateFormat) {

            ((SimpleDateFormat)sdf).applyPattern(pattern);
        }
        return (sdf.format(getCalendar().getTime(), buf,
                                new FieldPosition(0))).toString();
    }

    /**
     * Return a string representing the "date" portion of this DateTime
     *
     * @return  String representing the date (UTC) in the form
     *          yyyy-MM-dd (ex: 1999-05-04).
     */
    public String dateString()
    {
        String pattern =
                  (getCalendar().get(Calendar.ERA) == GregorianCalendar.BC)
                            ? "yyyy-MM-dd 'BCE'"
                            : "yyyy-MM-dd";
        return formattedString(pattern, DEFAULT_TIMEZONE);
    }

    /**
     * Return a string representing the "time" portion of this DateTime
     *
     * @return  String representing the time (UTC) in the form
     *          HH:mm:ssZ (ex: 15:27:08Z)
     */
    public String timeString()
    {
        return formattedString("HH:mm:ss'Z'", DEFAULT_TIMEZONE);
    }

    /** 
     * You can override the DateFormat by specifying a class. 
     *
     * @param dateFormatClass The class. This must be derived from java.text.DateFormat 
     */
    public static void setDateFormatClass(Class dateFormatClass) {
        if(!java.text.DateFormat.class.isAssignableFrom(dateFormatClass)) {
            throw new IllegalArgumentException("Not a DateFormat class: " + dateFormatClass.getName());
        }
        DateTime.dateFormatClass = dateFormatClass;
    }

    /**
     * Set the format of the output of the toString() method.  All DateTime
     * objects created in the JVM once this is set will use the new format
     * so be very careful in your use of this method.
     *
     * The pattern uses the time format syntax of java.text.SimpleDateFormat.
     * @see java.text.SimpleDateFormat
     * If you want to use a time zone other than the default,
     * @see #setFormatTimeZone
     *
     * @param   pattern         time format string
     */
    public static void setFormatPattern(String pattern)
    {
        formatPattern = pattern;
    }

    /**
     * Return the format pattern used in the output of the toString() method.
     * The pattern uses the time format syntax of java.text.SimpleDateFormat.
     * @see java.text.SimpleDateFormat
     *
     * @return  time format pattern
     */
    public static String getFormatPattern()
    {
        return formatPattern;
    }

    /**
     * Set the TimeZone of the output of the toString() method.  All DateTime
     * objects created in the JVM once this is set will use the new TimeZone
     * so be very careful in your use of this method.
     *
     * The time zone is any of the valid java.util.TimeZone values.
     * @see java.util.TimeZone
     *
     * @param   tz              time zone
     */
    public static void setFormatTimeZone(TimeZone tz)
    {
        timeZone = tz;
    }

    /**
     * Return the TimeZone used in the output of the toString() method.
     *
     * @return  time zone
     */
    public static TimeZone getFormatTimeZone()
    {
        return timeZone;
    }

    /**
     * Reset the format of the output of the toString() method to the default -
     * <nobr>yyyy-MM-dd HH:mm:ssZ</nobr> (ex: 1999-05-04 15:27:08Z) and
     * the TimeZone to UTC.
     */
    public static void resetFormat()
    {
        formatPattern = DEFAULT_TIME_FORMAT;
        timeZone = DEFAULT_TIMEZONE;
    }

    /**
     * Create a DateTime object from a string specification
     * @param  dateString  date string specification in format pattern
     *                     defined for DateTime in this JVM
     *
     * @throws  VisADException  formatting problem
     * @see #setFormatPattern
     */
    public static DateTime createDateTime(String dateString)
        throws VisADException
    {
        return createDateTime(dateString, formatPattern, DEFAULT_TIMEZONE);
    }

    /**
     * Create a DateTime object from a string specification using the
     * supplied pattern and default timezone.
     * @param  dateString  date string specification
     * @param  format string
     *
     * @throws  VisADException  formatting problem
     */
    public static DateTime createDateTime(String dateString, String format)
        throws VisADException
    {
        return createDateTime(dateString, format, DEFAULT_TIMEZONE);
    }

    /**
     * Create a DateTime object from a string specification using the
     * supplied pattern and timezone.
     * @param  dateString     date string specification
     * @param  formatPattern  format string
     * @param  timezone       TimeZone to use
     *
     * @throws  VisADException  formatting problem
     */
    public static DateTime createDateTime(String dateString, 
                                        String format, 
                                        TimeZone timezone)
        throws VisADException
    {
        Date d;
        try {
          SimpleDateFormat sdf = new SimpleDateFormat();
          sdf.setTimeZone(timezone);
          sdf.applyPattern(format);
          d = sdf.parse(dateString);
        } catch (ParseException pe) {
            throw new VisADException("invalid date string: " + dateString);
        }
        return new DateTime(d);
    }

    /**
     *  Implement Comparable interface
     *
     * @param   oo      Object for comparison - should be DateTime
     */
    public int compareTo(Object oo)
    {
        return super.compareTo(oo);
    }

    /**
     * Create a Gridded1DDoubleSet from an array of DateTimes
     *
     * @param  times  array of DateTimes.  Array cannot be null or only
     *                have one entry.
     *
     * @return Gridded1DDouble set representing the array
     * @throws VisADException  couldn't create the GriddedDoubleSet
     */
    public static Gridded1DDoubleSet makeTimeSet(DateTime[] times)
        throws VisADException
    {
        Arrays.sort(times);
        double[][] timeValues = new double[1][times.length];
        for (int i = 0; i < times.length; i++) 
            timeValues[0][i] = times[i].getValue(CommonUnit.secondsSinceTheEpoch);
        return new Gridded1DDoubleSet(RealType.Time, timeValues, times.length);
    }

    /**
     * Create a Gridded1DDoubleSet from an array of doubles of seconds
     * since the epoch.
     *
     * @param  times  array of times in seconds since the epoch. Array 
     *                cannot be null or only have one entry.
     *
     * @return set representing the array as a Gridded1DDoubleSet
     * @throws VisADException  couldn't create the GriddedDoubleSet
     */
    public static Gridded1DDoubleSet makeTimeSet(double[] times)
        throws VisADException
    {
        Arrays.sort(times);
        double[][] alltimes = new double[1][times.length];
        for (int i = 0; i < times.length; i++) alltimes[0][i] = times[i];
        return new Gridded1DDoubleSet(RealType.Time, alltimes, times.length);
    }

    /**
     * Create an array of DateTimes from a Gridded1DSet of times.
     *
     * @param  timeSet   Gridded1DSet of times
     *
     * @throws VisADException  invalid time set or couldn't create DateTimes
     */
    public static DateTime[] timeSetToArray(Gridded1DSet timeSet)
        throws VisADException
    {
        Unit unit = timeSet.getSetUnits()[0];
        if (!Unit.canConvert(unit, CommonUnit.secondsSinceTheEpoch)) {
            throw new VisADException( "Invalid Units for timeSet");
        }
        double[][] values;
        if (!unit.equals(CommonUnit.secondsSinceTheEpoch)) 
            values = Unit.convertTuple(timeSet.getDoubles(), new Unit[] {unit},
                              new Unit[] {CommonUnit.secondsSinceTheEpoch}, false);
        else
            values = timeSet.getDoubles();
        DateTime[] times = new DateTime[timeSet.getLength()];

        for (int i = 0; i < timeSet.getLength(); i++)
            times[i] = new DateTime(values[0][i]);
        return times;
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
        a = DateTime.createDateTime(a.toString());
        System.out.println("\nUsing createDateTime with string of current Date(): " + a);

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
