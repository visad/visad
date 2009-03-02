// DoubleTuple.java
//
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, 
Tommy Jasmin and Jeff McWhirter.

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


import visad.*;

import java.rmi.RemoteException;

import java.util.Arrays;


/**
 * This provides a LoCal RealTuple that can hold numeric values without
 * taking the hit that having lots and lots of Real objects around.
 *
 * @author MetApps Development Team
 * @version $Revision: 1.4 $ $Date: 2009-03-02 23:35:41 $
 */
public class DoubleTuple extends RealTuple {

  /** The numeric values */
  double[] doubles;

  /** The tuple type */
  RealTupleType tt;

  /** The units for the numeric values */
  Unit[] units;

  /** Holds the components as we create them */
  Data[] components;

  /**
   * Construct a new DoubleTuple of generic values
   *
   * @param doubles  the values
   *
   * @throws VisADException  problem creating MathType
   */
  public DoubleTuple(double[] doubles) throws VisADException {
    this(makeGenericTypes(doubles.length), doubles, null);
  }

  /**
   * Construct a new DoubleTuple
   *
   * @param type The type
   * @param doubles The reals
   */
  public DoubleTuple(RealTupleType type, double[] doubles) {
    this(type, doubles, null);
  }

  /**
   * Construct a new DoubleTuple
   *
   * @param type The type
   * @param doubles The reals
   * @param units The units for the reals (may be null)
   */
  public DoubleTuple(RealTupleType type, double[] doubles, Unit[] units) {
    super(type);
    this.tt = type;
    this.doubles = doubles;
    this.units = units;

    if (units == null) {
      this.units = type.getDefaultUnits();
    }
  }

  /**
   * Make a RealTupleType of RealType.Generic for num elements
   *
   * @param num  number of elements
   *
   * @return corresponding RealTupleType
   *
   * @throws VisADException   unable to create the RealTupleType
   */
  private static RealTupleType makeGenericTypes(int num)
          throws VisADException {
    RealType[] types = new RealType[num];
    Arrays.fill(types, RealType.Generic);

    return new RealTupleType(types);
  }

  /**
   * Get the i'th component. This creates it if needed and stores
   * it in the components array
   *
   * @param i Which one
   *
   * @return The component
   *
   * @throws RemoteException On badness
   * @throws VisADException On badness
   */
  public Data getComponent(int i) throws VisADException, RemoteException {
    if (components == null) {
      components = new Data[getDimension()];
    }

    if (0 <= i && i < getDimension()) {
      if (components[i] == null) {
        components[i] = getComponentInner(i);

        if (components[i] != null) {
          ((DataImpl) components[i]).setParent(this);
        }
      }

      return components[i];
    }
    else {
      throw new TypeException("Tuple: component index out of range: "+i);
    }

  }

  /**
   * Actually get the component
   *
   * @param i index
   *
   * @return The component
   *
   * @throws RemoteException On badness
   * @throws VisADException
   */
  private Data getComponentInner(int i)
          throws VisADException, RemoteException {
    //System.err.println ("get component:" +tt.getComponent(i));
    if ((doubles != null)) {
      if ((units == null) || (units[i] == null)) {
        return new Real((RealType) tt.getComponent(i), doubles[i]);
      }
      else {
        return new Real((RealType) tt.getComponent(i), doubles[i], units[i]);
      }
    }

    return null;

  }


  /**
   * Create, if needed, and return the component array.
   *
   * @return components
   */
  public Data[] getComponents() {
    try {
      //Create the array and populate it if needed
      if (components == null) {
        components = new Data[getDimension()];
      }

      for (int i = 0; i < getDimension(); i++) {
        components[i] = getComponent(i);
      }

      return components;
    } catch (Exception exc) {
      exc.printStackTrace();

      throw new IllegalStateException("Error making component array:"+exc);
    }
  }

  /**
   * Get Units of Real components
   *
   * @return the units of the components
   */
  public Unit[] getTupleUnits() {
    return Unit.copyUnitsArray(units);
  }

  /**
   * Get the values of the Real components
   * @return double array of the values of each Real component
   */
  public double[] getValues() {
    if (doubles == null) {
      return null;
    }

    return (double[]) doubles.clone();
  }


  /**
   * Indicates if this Tuple is identical to an object.
   *
   * @param obj         The object.
   * @return            <code>true</code> if and only if the object is
   *                    a Tuple and both Tuple-s have identical component
   *                    sequences.
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof DoubleTuple)) {
      return false;
    }

    DoubleTuple that = (DoubleTuple) obj;

    return Arrays.equals(doubles, that.doubles) && tt.equals(that.tt)
           && Arrays.equals(units, that.units);

  }


  /**
   * Returns the hash code of this object.
   * @return            The hash code of this object.
   */
  public int hashCode() {
    int hashCode = 0;
    if (doubles != null) {
      hashCode ^= doubles.hashCode();
    }

    hashCode ^= tt.hashCode();

    return hashCode;
  }


  /**
   * run 'java visad.DoubleTuple' to test the RealTuple class
   *
   * @param args  ignored
   *
   * @throws RemoteException  Java RMI problem
   * @throws VisADException   Unable to create the VisAD objects
   */
  public static void main(String args[])
          throws VisADException, RemoteException {

    byte b = 10;
    Real w = new Real(b);

    Real[] reals1 = {new Real(1), new Real(2), new Real(3)};
    //RealTuple rt1    = new RealTuple(reals1);
    DoubleTuple rt1 = new DoubleTuple(new double[] {1, 2, 3});
    DoubleTuple rt2 = new DoubleTuple(new double[] {6, 5, 4});

    System.out.println("rt1 = "+rt1+"\nrt2 = "+rt2);

    System.out.println("rt1 + rt2 = "+rt1.add(rt2));
    System.out.println("rt1 - rt2 = "+rt1.subtract(rt2));
    System.out.println("rt1 * rt2 = "+rt1.multiply(rt2));
    System.out.println("rt1 / rt2 = "+rt1.divide(rt2));
    System.out.println("sqrt(rt1) = "+rt1.sqrt());

    System.out.println("rt1 + w = "+rt1.add(w));
    System.out.println("rt1 - w = "+rt1.subtract(w));
    System.out.println("rt1 * w = "+rt1.multiply(w));
    System.out.println("rt1 / w = "+rt1.divide(w));

    System.out.println("w + rt2 = "+w.add(rt2));
    System.out.println("w - rt2 = "+w.subtract(rt2));
    System.out.println("w * rt2 = "+w.multiply(rt2));
    System.out.println("w / rt2 = "+w.divide(rt2));

    RealTupleType tt = RealTupleType.LatitudeLongitudeAltitude;
    Unit foot = CommonUnit.meter.scale(.3048);  // approximate
    try {
      foot = visad.data.units.Parser.parse("foot");
    } catch (visad.data.units.ParseException pe) {}

    DoubleTuple one = new DoubleTuple(tt, new double[] {40.5, -105.0, 5338},
                                      new Unit[] {CommonUnit.degree,
            CommonUnit.degree, foot});
    System.out.println("one = "+one.toString());
    visad.util.DataUtility.isSerializable(one, true);
    visad.util.Util.printArray("one.values", one.getValues());
    visad.util.Util.printArray("one.units", one.getTupleUnits());
    System.out.println("one.cs = "+one.getCoordinateSystem());
    DoubleTuple two = new DoubleTuple(tt, new double[] {45, -75.0, 400});
    System.out.println("two = "+two.toString());
    System.out.println("one - two = "+one.subtract(two));
    System.out.println("one + two = "+one.add(two));
  }

  /* Here's the output:

  rt1 = (1.0, 2.0, 3.0)
  rt2 = (6.0, 5.0, 4.0)
  rt1 + rt2 = (7.0, 7.0, 7.0)
  rt1 - rt2 = (-5.0, -3.0, -1.0)
  rt1 * rt2 = (6.0, 10.0, 12.0)
  rt1 / rt2 = (0.16666666666666666, 0.4, 0.75)
  sqrt(rt1) = (1.0, 1.4142135623730951, 1.7320508075688772)
  rt1 + w = (11.0, 12.0, 13.0)
  rt1 - w = (-9.0, -8.0, -7.0)
  rt1 * w = (10.0, 20.0, 30.0)
  rt1 / w = (0.1, 0.2, 0.3)
  w + rt2 = (16.0, 15.0, 14.0)
  w - rt2 = (4.0, 5.0, 6.0)
  w * rt2 = (60.0, 50.0, 40.0)
  w / rt2 = (1.6666666666666667, 2.0, 2.5)
  one = (40.5, -105.0, 5338.0)
  one.values: [0]: 40.5 [1]: -105.0 [2]: 5338.0
  one.units: [0]: deg [1]: deg [2]: international foot
  one.cs = null
  two = (45.0, -75.0, 400.0)
  one - two = (-4.5, -30.0, 4025.6640419947507)
  one + two = (85.5, -180.0, 6650.335958005249)
  */

}

