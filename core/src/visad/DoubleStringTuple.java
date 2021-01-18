// DoubleStringTuple.java
//
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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


import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This provides a LoCal Tuple that can hold numeric and string values without
 * taking the hit that having slots and lots of Real and Text objects around.
 *
 * @author MetApps Development Team
 * @version $Revision: 1.6 $ $Date: 2009-12-01 14:59:41 $
 */
public class DoubleStringTuple extends Tuple {

  private Data[] prototypes; 

  /** The string values */
  String[] strings;

  /** The numeric values */
  double[] doubles;

  /** The tuple type */
  TupleType tt;

  /** The units for the numeric values */
  Unit[] units;

  /** Holds the components as we create them */
  Data[] components;

  /** my size */
  int size;


  /**
   * Construct a DoubleStringTuple
   *
   * @param type The type
   * @param units The units for the reals
   * @param doubles The reals
   * @param strings The strings
   */
  public DoubleStringTuple(TupleType type, double[] doubles,
                           String[] strings, Unit[] units) {
      this(type, null,doubles, strings, units);
  }

  public DoubleStringTuple(TupleType type, Data[] prototypes,double[] doubles,
                           String[] strings, Unit[] units) {
    super(type);
    this.tt = type;
    this.prototypes = prototypes;
    this.units = units;
    this.doubles = doubles;
    this.strings = strings;
    size = 0;

    if (doubles != null) {
      size += doubles.length;
    }

    if (strings != null) {
      size += strings.length;
    }
  }


  /**
   * Check if there is no Data in this Tuple.
   * @return true if there is no data.
   */
  public boolean isMissing() {
      return doubles == null && strings == null;
  }


  /**
   * Make a tuple type from lists of scalar types
   *
   * @param numericTypes   List of RealTypes
   * @param stringTypes   List of TextTypes
   *
   * @return TupleType of the lists
   *
   * @throws RemoteException   Java RMI problem
   * @throws VisADException  unable to create TupleType
   */
  public static TupleType makeTupleType(List numericTypes, List stringTypes)
          throws VisADException {
    List allTypes = new ArrayList();
    allTypes.addAll(numericTypes);
    allTypes.addAll(stringTypes);
    MathType[] tmp =
      (MathType[]) allTypes.toArray(new MathType[allTypes.size()]);

    return new TupleType(tmp);
  }


  /**
   * Get the i'th component. This creates it if needed and
   * stores it in the components array
   *
   * @param i Which one
   *
   * @return The component
   *
   * @throws RemoteException   Java RMI problem
   * @throws VisADException  unable to create TupleType
   */
  public Data getComponent(int i) throws VisADException, RemoteException {
      //    System.err.println ("DoubleStringTuple.getComponent:" + i);
    if (components == null) {
	components = new Data[size];
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
   * @param i i
   *
   * @return The component
   *
   * @throws RemoteException On badness
   * @throws VisADException
   */
  private Data getComponentInner(int i)
          throws VisADException, RemoteException {
      //      System.err.println ("get component:" + i +"  doubles:" + doubles.length);
    //System.err.println ("get component:" +tt.getComponent(i));
    if ((doubles != null) && (i < doubles.length)) {
       if(prototypes!=null) {
	   return ((Real)prototypes[i]).cloneButValue(doubles[i]);
      }
      if ((units == null) || (units[i] == null)) {
        return new Real((RealType) tt.getComponent(i), doubles[i]);
      }
      else {
        return new Real((RealType) tt.getComponent(i), doubles[i], units[i]);
      }
    }

    int idx = i-doubles.length;

    return new Text((TextType) tt.getComponent(i), strings[idx]);

  }


  /**
   * Create, if needed, and return the component array.
   *
   * @return components
   */
 public Data[] getComponents(boolean copy) {
    try {
	//       System.err.println ("DoubleStringTuple.getComponents");
      //Create the array and populate it if needed
      if (components == null) {
	  components = new Data[size];
      }
      for (int i = 0; i < size; i++) {
        components[i] = getComponent(i);
      }

      return components;
    } catch (Exception exc) {
      exc.printStackTrace();

      throw new IllegalStateException("Error making component array:"+exc);
    }
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

    if (!(obj instanceof DoubleStringTuple)) {
      return false;
    }

    DoubleStringTuple that = (DoubleStringTuple) obj;

    return Arrays.equals(doubles, that.doubles)
           && Arrays.equals(strings, that.strings) && tt.equals(that.tt)
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

    if (strings != null) {
      hashCode ^= strings.hashCode();
    }

    hashCode ^= tt.hashCode();

    return hashCode;
  }

  /**
   * run 'java visad.DoubleStringTuple' to test the RealTuple class
   *
   * @param args  ignored
   *
   * @throws RemoteException  Java RMI problem
   * @throws VisADException   Unable to create the VisAD objects
   */
  public static void main(String args[])
          throws VisADException, RemoteException {
    List reals = new ArrayList();
    reals.add(RealType.Latitude);
    reals.add(RealType.Longitude);
    reals.add(RealType.Altitude);
    List strings = new ArrayList();
    strings.add(TextType.getTextType("id"));
    double[] vals = new double[] {40, -105, 5337};
    String[] svals = new String[] {"Boulder"};
    Unit foot = CommonUnit.meter.scale(.3048);  // approximate
    try {
      foot = visad.data.units.Parser.parse("foot");
    } catch (visad.data.units.ParseException pe) {}

    Unit[] units = new Unit[] {CommonUnit.degree, CommonUnit.degree, foot};
    DoubleStringTuple dst =
      new DoubleStringTuple(DoubleStringTuple.makeTupleType(reals, strings),
                            vals, svals, units);
    System.out.println(dst);
    System.out.println("serializable? "+
                       visad.util.DataUtility.isSerializable(dst));
  }

}
