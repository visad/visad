
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

/**
   DateTime is a class of objects for holding date and time information.
   DateTime objects are immutable.<P>
*/
public class DateTime extends Object implements java.io.Serializable {

  private int Year;        // year AD, 1 - Integer.MAX_VALUE
                   // negative to indicate missing
  private int DayOfYear;   // Julian day, between 1 and 366
  private double Seconds;  // since midnght
  private int Month;       // 1 - 12
  private int DayOfMonth;  // 1 - 31
  private int DayOfWeek;   // 1 (Sunday) - 7 (Saturday)
  private int Hour;        // 0 - 23
  private int Minute;      // 0 - 59
  private int Second;      // 0 - 59

  private double SecondsSinceMidnight01Jan0001; // Double.NaN to indicate missing

  // SecondsSinceMidnight01Jan0001 and Real.Value are
  // seconds since midnight 1 January 1
  // (double is 1 sign, 11 exp, 52 mant -> 4 x 10^15; 2000 yrs -> 6 x 10^10 secs)

  private static final double seconds_per_day = (double) (24 * 60 * 60);
  // these sould be private
  private static final int days1583 = (int)
    (fromYearDaySecondsTrusted(1583, 1, 0.0).getValue() / seconds_per_day);
  private static final int days1600 = (int)
    (fromYearDaySecondsTrusted(1600, 1, 0.0).getValue() / seconds_per_day);

  private static final int[] dds =
    {31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365,   // normal year
     31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366,   // leap year
     31, 59, 90, 120, 151, 181, 212, 243, 273, 294, 324, 355};  // 1582

  private static final String[] month_names =
    {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
     "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

  private static final String[] day_names =
    {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

  public DateTime(Real real) throws VisADException {
    if (real.isMissing()) {
      Year = -1;
      SecondsSinceMidnight01Jan0001 = Double.NaN;
      return;
    }
    if (real.getValue() < 0.0) {
      throw new VisADException("DateTime: bad Real date / time");
    }
    SecondsSinceMidnight01Jan0001 = real.getValue();
    int days = (int) (SecondsSinceMidnight01Jan0001 / seconds_per_day);
    Seconds = SecondsSinceMidnight01Jan0001 - days * seconds_per_day;

    int secs = (int) Seconds;
    Hour = secs / 3600;
    secs = secs - 3600 * Hour;
    Minute = secs / 60;
    Second = secs - 60 * Minute;

    if (days < days1583) {
      Year = 1 + (4 * days + 3) / 1461; // seems to work
    }
    else if (days <= days1600) {
      Year = 1583 + (4 * (days - days1583) + 1) / 1461; // seems to work
    }
    else {
      int d1600 = days - days1600;
      int cent4s = d1600 / (400 * 365 + 97);
      int d400 = d1600 - cent4s * (400 * 365 + 97);
      int cents = d400 / (100 * 365 + 24);
      int d100 = d400 - cents * (100 * 365 + 24);
      Year = 1600 + 400 * cent4s + 100 * cents + (4 * d100) / 1461; // seems to work
    }
    DayOfYear = (int) ((SecondsSinceMidnight01Jan0001 -
                        fromYearDaySeconds(Year, 1, 0.0).getValue()) /
                       seconds_per_day) + 1;
    DayOfWeek = 1 + (days + 6) % 7;
    boolean leap = ((Year % 400) == 0) ||
                   ( ( (Year <= 1582) || ((Year % 100) != 0) ) &&
                     ((Year % 4) == 0) );
    int im = leap ? 12 : 0;
    if (Year == 1582) im = 24;
    for (int i=im; i<im+12; i++) {
      if (DayOfYear <= dds[i]) {
        Month = i - im + 1;
        if (Month > 1) DayOfMonth = DayOfYear - dds[i-1];
        else DayOfMonth = DayOfYear;
        if (Year == 1582 && Month == 10 && DayOfMonth > 4) DayOfMonth += 10;
        break;
      }
    }
    return;
  }

  public DateTime(int year, int day, double seconds) throws VisADException {
    this(fromYearDaySeconds(year, day, seconds));
  }

  public static Real fromYearDaySeconds(int year, int day, double seconds)
                     throws VisADException {
    if (year <= 0) {
      throw new VisADException("DateTime.fromYearDaySeconds: bad year");
    }
    boolean leap = ((year % 400) == 0) ||
                   ( ( (year <= 1582) || ((year % 100) != 0) ) &&
                     ((year % 4) == 0) );
    int day_limit = (year == 1582) ? 355 : (leap ? 366 : 365);

    if (day > day_limit || day < 1) {
      throw new VisADException("DateTime.fromYearDaySeconds: bad day");
    }
    if (seconds > seconds_per_day || seconds < 0.0) {
      throw new VisADException("DateTime.fromYearDaySeconds: bad seconds");
    }
    return fromYearDaySecondsTrusted(year, day, seconds);
  }

  /** trusted method for initializers */
  private static Real fromYearDaySecondsTrusted(int year, int day, double seconds) {
    int days;
    boolean leap = ((year % 400) == 0) ||
                   ( ( (year <= 1582) || ((year % 100) != 0) ) &&
                     ((year % 4) == 0) );
    if (year <= 1582) {
      // blissfully ignorant time before 1582
      days = 365 * (year - 1) + (year - 1) / 4 + day - 1;
    }
    else {
      days = (355 + 365 * 1581 + 1581 / 4) + // days before 1-1-1583
             365 * (year - 1583) + // days in whole years since 1582
             (year - 1581) / 4 -   // leap days since 1582
             (year - 1501) / 100 + // century skipped leap days since 1582
             (year - 1201) / 400 + // except every fourth century since 1582
             day - 1;              // plus day of year
    }
    return new Real( days * seconds_per_day + seconds );
  }

  public Real getReal() {
    return new Real(SecondsSinceMidnight01Jan0001);
  }

  public double getValue() {
    return SecondsSinceMidnight01Jan0001;
  }

  public boolean isMissing() {
    return (Year < 0);
  }

  public String toString() {
    return timeString() + " " + dateString();
  }

  public String dateString() {
    return
           paddedIntString(DayOfMonth, 2, " ") + " " +
           month_names[Month-1] + " " +
           paddedIntString(Year, 4, "0");
/* WLH 30 April 99
           paddedIntString(Year, 4, "0") +
           " (" + day_names[DayOfWeek-1] + ")";
*/
  }

  public String timeString() {
    return paddedIntString(Hour, 2, "0") + ":" +
           paddedIntString(Minute, 2, "0") + ":" +
           paddedIntString(Second, 2, "0");
           // paddedIntString(Second, 2, "0") + " GMT"; WLH 30 April 99
  }

  static String paddedIntString(int val, int len, String pad) {
    String s = String.valueOf(val);
    int l = s.length();
    if (l == len) return s;
    else if (l > len) return s.substring(l-len);
    else {
      for (int i=0; i<len-l; i++) s = pad+s;
      return s;
    }
  }

  /** run 'java visad.DateTime' to test the DateTime class */
  public static void main(String args[]) throws VisADException {

    int seconds_per_day = 24 * 60 * 60;
    int days;
    Real r;
    DateTime a;

    a = new DateTime(1996, 1, 0.0);
    for (int i = 0; i<731; i++) {
      r = a.getReal();
      days = (int) (r.getValue() / seconds_per_day);
      System.out.println(a);

      r = new Real(r.getValue() + seconds_per_day);
      a = new DateTime(r);
    }
  }

/* Here's the output:

iris 235% java visad.DateTime
00:00:00 GMT  1 Jan 1996 (Mon)
00:00:00 GMT  2 Jan 1996 (Tue)
00:00:00 GMT  3 Jan 1996 (Wed)
00:00:00 GMT  4 Jan 1996 (Thu)
00:00:00 GMT  5 Jan 1996 (Fri)
00:00:00 GMT  6 Jan 1996 (Sat)
00:00:00 GMT  7 Jan 1996 (Sun)
00:00:00 GMT  8 Jan 1996 (Mon)

. . .

00:00:00 GMT 26 Dec 1997 (Fri)
00:00:00 GMT 27 Dec 1997 (Sat)
00:00:00 GMT 28 Dec 1997 (Sun)
00:00:00 GMT 29 Dec 1997 (Mon)
00:00:00 GMT 30 Dec 1997 (Tue)
00:00:00 GMT 31 Dec 1997 (Wed)
iris 236%

*/

}

