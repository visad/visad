//
// CommonUnit.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

/**
   CommonUnit is a class for commonly used Units
*/
public class CommonUnit extends Object {

  /** CommonUnit for plane angle, not temperature */
  public static Unit degree;

  /** CommonUnit for plane angle */
  public static Unit radian = SI.radian;

  /** CommonUnit for time */
  public static Unit second = SI.second;

  /** CommonUnit for length */
  public static Unit meter = SI.meter;

  /** CommonUnit for speed */
  public static Unit meterPerSecond =
    new DerivedUnit(new BaseUnit[] {SI.meter, SI.second},
                    new int[] {1, -1});

  /** CommonUnit for seconds since the Epoch (i.e. 1970-01-01 00:00:00Z) */
  public static Unit secondsSinceTheEpoch =
        new OffsetUnit(
            visad.data.units.UnitParser.encodeTimestamp(
                1970, 1, 1, 0, 0, 0, 0),
            SI.second);

  /** CommonUnit for all BaseUnits with exponent = zero */
  public static Unit dimensionless = new DerivedUnit();

  /** promiscuous is compatible with any Unit; useful for constants;
      not the same as null Unit, which is only compatible with
      other null Units; not the same as dimensionless, which is not
      compatible with other Units for addition and subtraction */
  public static Unit promiscuous = PromiscuousUnit.promiscuous;

  /**
   * static initializer to catch impossible but declared Exception
   */
  static {
    try {
      degree = SI.radian.scale(Math.PI/180.0, true).clone("deg");
    }
    catch (UnitException e) {}		// can't happen
  }

    /**
     * Test this class.
     *
     * @param args		Arguments (ignored).
     * @exception UnitException	A problem occurred.
     */
    public static void main(String[] args)
	throws UnitException
    {
	System.out.println(
	  "new ScaledUnit(1.0).equals(dimensionless)=" +
	  new ScaledUnit(1.0).equals(dimensionless));
	System.out.println(
	  "dimensionless.equals(new ScaledUnit(1.0))=" +
	  dimensionless.equals(new ScaledUnit(1.0)));
	System.out.println(
	  "CommonUnit.dimensionless.isConvertible(SI.radian) = " +
	   CommonUnit.dimensionless.isConvertible(SI.radian));
	System.out.println(
	  "CommonUnit.dimensionless.isConvertible(CommonUnit.degree) = " +
	   CommonUnit.dimensionless.isConvertible(CommonUnit.degree));
	System.out.println(
	  "CommonUnit.degree.isConvertible(SI.radian) = " +
	   CommonUnit.degree.isConvertible(SI.radian));
    }
}

