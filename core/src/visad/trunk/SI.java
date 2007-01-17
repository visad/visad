//
// SI.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

import java.io.Serializable;

/**
 * A class that represents the SI system of units.
 *
 * @author Steven R. Emmerson
 *
 * This is part of Steve Emerson's Unit package that has been
 * incorporated into VisAD.
 */
public final class SI
    implements Serializable
{
    /*
     * The base units of the SI system of units:
     */

    /**
     * Base unit of electric current.
     * The ampere is that constant current which, if maintained
     * in two straight parallel conductors of infinite length,
     * of negligible circular cross section, and placed 1 meter
     * apart in vacuum, would produce between these conductors a
     * force equal to 2 x 10^-7 newton per meter of length.
     */
    public static /*final*/ BaseUnit	ampere;

    /**
     * Base unit of luminous intensity.
     * The candela is the luminous intensity, in a given
     * direction, of a source that emits monochromatic
     * radiation of frequency 540 x 10^12 hertz and that has a
     * radiant intensity in that direction of (1/683) watt per
     * steradian.
     */
    public static /*final*/ BaseUnit	candela;

    /**
     * Base unit of thermodynamic temperature.
     * The kelvin, unit of thermodynamic temperature, is the
     * fraction 1/273.16 of the thermodynamic temperature of the
     * triple point of water.
     */
    public static /*final*/ BaseUnit	kelvin;

    /**
     * Base unit of mass.
     * The kilogram is the unit of mass; it is equal to the mass
     * of the international prototype of the kilogram.
     */
    public static /*final*/ BaseUnit	kilogram;

    /**
     * Base unit of length.
     * The meter is the length of the path travelled by light
     * in vacuum during a time interval of 1/299 792 458 of a
     * second.
     */
    public static /*final*/ BaseUnit	meter;

    /**
     * Base unit of time.
     * The second is the duration of 9 192 631 770 periods of
     * the radiation corresponding to the trasition between
     * the two hyperfine levels of the ground state of the
     * cesium-133 atom.
     */
    public static /*final*/ BaseUnit	second;

    /**
     * Base unit of amount of substance.
     * The mole is the amount of substance of a system which
     * contains as many elementary entities as there are atoms
     * in 0.012 kilogram of carbon 12.
     */
    public static /*final*/ BaseUnit	mole;

    /**
     * Base unit of angular measure.
     * The radian is the plane angle between two radii of a
     * circle that cut off on the circumference an arc equal in
     * length to the radius.  This unit is dimensionless.
     */
    public static /*final*/ BaseUnit	radian;

    /**
     * Base unit of solid angle.
     * The steradian is the solid angle that, having its vertex
     * in the center of a sphere, cuts off an area of the surface
     * equal to that of a square with sides of length equal to the
     * radius of the sphere.  This unit is dimensionless.
     */
    public static /*final*/ BaseUnit	steradian;

    static
    {
	try
	{
	    /**
	     * Base unit of electric current.
	     * The ampere is that constant current which, if maintained
	     * in two straight parallel conductors of infinite length,
	     * of negligible circular cross section, and placed 1 meter
	     * apart in vacuum, would produce between these conductors a
	     * force equal to 2 x 10^-7 newton per meter of length.
	     */
	    ampere = BaseUnit.addBaseUnit("ElectricCurrent", "ampere", "A");

	    /**
	     * Base unit of luminous intensity.
	     * The candela is the luminous intensity, in a given
	     * direction, of a source that emits monochromatic
	     * radiation of frequency 540 x 10^12 hertz and that has a
	     * radiant intensity in that direction of (1/683) watt per
	     * steradian.
	     */
	    candela =
	      BaseUnit.addBaseUnit("LuminousIntensity", "candela", "cd");

	    /**
	     * Base unit of thermodynamic temperature.
	     * The kelvin, unit of thermodynamic temperature, is the
	     * fraction 1/273.16 of the thermodynamic temperature of the
	     * triple point of water.
	     */
	    kelvin = BaseUnit.addBaseUnit("Temperature", "kelvin", "K");

	    /**
	     * Base unit of mass.
	     * The kilogram is the unit of mass; it is equal to the mass
	     * of the international prototype of the kilogram.
	     */
	    kilogram = BaseUnit.addBaseUnit("Mass", "kilogram", "kg");

	    /**
	     * Base unit of length.
	     * The meter is the length of the path travelled by light
	     * in vacuum during a time interval of 1/299 792 458 of a
	     * second.
	     */
	    meter = BaseUnit.addBaseUnit("Length", "meter", "m");

	    /**
	     * Base unit of time.
	     * The second is the duration of 9 192 631 770 periods of
	     * the radiation corresponding to the trasition between
	     * the two hyperfine levels of the ground state of the
	     * cesium-133 atom.
	     */
	    second = BaseUnit.addBaseUnit("Time", "second", "s");

	    /**
	     * Base unit of amount of substance.
	     * The mole is the amount of substance of a system which
	     * contains as many elementary entities as there are atoms
	     * in 0.012 kilogram of carbon 12.
	     */
	    mole = BaseUnit.addBaseUnit("AmountOfSubstance", "mole", "mol");

	    /**
	     * Base unit of angular measure.
	     * The radian is the plane angle between two radii of a
	     * circle that cut off on the circumference an arc equal in
	     * length to the radius.  This unit is dimensionless.
	     */
	    radian = BaseUnit.addBaseUnit("Angle", "radian", "rad", true);

	    /**
	     * Base unit of solid angle.
	     * The steradian is the solid angle that, having its vertex
	     * in the center of a sphere, cuts off an area of the surface
	     * equal to that of a square with sides of length equal to the
	     * radius of the sphere.  This unit is dimensionless.
	     */
	    steradian =
		BaseUnit.addBaseUnit("SolidAngle", "steradian", "sr", true);
	}
	catch (UnitException e) {}
    }

    private SI() {
    }

    /**
     * Test this class.
     *
     * @param args		Arguments (ignored).
     */
    public static void main (String[] args)
    {
	System.out.println("ampere      = \"" + ampere + "\"");
	System.out.println("candela     = \"" + candela + "\"");
	System.out.println("kelvin      = \"" + kelvin + "\"");
	System.out.println("kilogram    = \"" + kilogram + "\"");
	System.out.println("meter       = \"" + meter + "\"");
	System.out.println("second      = \"" + second + "\"");
	System.out.println("mole        = \"" + mole + "\"");
	System.out.println("radian      = \"" + radian + "\"");
	System.out.println("steradian   = \"" + steradian + "\"");
    }
}
