//
// DisplayRealType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;

/**
   DisplayRealType is the class for display real scalar types.
   A fixed set is defined by the system, users may add others.
*/
public class DisplayRealType extends RealType {

  private boolean range;          // true if [LowValue, HiValue] range is used
  private double LowValue;        // [LowValue, HiValue] is range of values
  private double HiValue;         //   for this display scalar
  private double DefaultValue;    // default value for this display scalar

  private DisplayTupleType tuple; // tuple to which DisplayRealType belongs, or null
  private int tupleIndex;         // index within tuple
  private boolean Single;   // true if only one instance allowed in a display type

  private boolean system;   // true if this is a system intrinsic

  // true if this is actually a text type, false if it is really real
  private boolean text;     // admitedly a kludge
  private boolean circular;

  // this is tricky, since DisplayRealType is Serializable
  // this may also be unnecessary
  private static int Count = 0;   // count of DisplayRealType-s
  private transient int Index;    // index of this DisplayRealType
  // Vector of scalar names used to make sure scalar names are unique
  // (within local VM)
  private static Vector DisplayRealTypeVector = new Vector();

  /** trusted constructor for intrinsic DisplayRealType's created by system
      without range or Unit */
  DisplayRealType(String name, boolean single, double def, boolean b) {
    this(name, single, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
         def, null, b);
  }

  /** trusted constructor for intrinsic DisplayRealType's created by system
      without Unit */
  DisplayRealType(String name, boolean single, double low, double hi,
                  double def, boolean b) {
    this(name, single, low, hi, def, null, b);
  }

  /** trusted constructor for intrinsic DisplayRealType's created by system
      with Unit */
  DisplayRealType(String name, boolean single, double low, double hi,
                  double def, Unit unit, boolean b) {
    super("Display" + name, unit, b);
    system = true;
    Single = single;
    LowValue = low;
    HiValue = hi;
    range = !(Double.isInfinite(low) || Double.isNaN(low) ||
              Double.isInfinite(hi) || Double.isNaN(hi));
    DefaultValue = def;
    tuple = null;
    tupleIndex = -1;
    text = false;
    circular = false;
    synchronized (DisplayRealTypeVector) {
      Count++;
      Index = Count;
      DisplayRealTypeVector.addElement(this);
    }
  }

  /** trusted constructor for intrinsic text DisplayRealType */
  DisplayRealType(String name, boolean single,  boolean b) {
    super("Display" + name, null, b);
    system = true;
    Single = single;
    text = true;
    circular = false;
    synchronized (DisplayRealTypeVector) {
      Count++;
      Index = Count;
      DisplayRealTypeVector.addElement(this);
    }
  }

  /** construct a DisplayRealType with given name (used only for
      user interfaces), single flag (if true, this DisplayRealType
      may only occur once in a path to a terminal node, as defined
      in Appendix A), (low, hi) range of values, default value = def,
      and unit */
  public DisplayRealType(String name, boolean single, double low, double hi,
                         double def, Unit unit)
         throws VisADException {
    super("Display" + name, unit, false);
    system = false;
    Single = single;
    LowValue = low;
    HiValue = hi;
    range = !(Double.isInfinite(low) || Double.isNaN(low) ||
              Double.isInfinite(hi) || Double.isNaN(hi));
    DefaultValue = def;
    tuple = null;
    tupleIndex = -1;
    text = false;
    circular = false;
    synchronized (DisplayRealTypeVector) {
      Count++;
      Index = Count;
      if (DisplayRealType.getDisplayRealTypeByName(getName()) != null) {
        throw new TypeException("DisplayRealType: name already used");
      }
      DisplayRealTypeVector.addElement(this);
    }
  }

  /** construct a DisplayRealType with given name (used only for
      user interfaces), single flag (if true, this DisplayRealType
      may only occur once in a path to a terminal node, as defined
      in Appendix A), default value = def, and unit;
      this DisplayRealType is not scaled (no range of values) */
  public DisplayRealType(String name, boolean single, double def,
                         Unit unit) throws VisADException {
    this(name, single, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
         def, unit);
  }

  private static DisplayRealType getDisplayRealTypeByName(String name) {
    synchronized (DisplayRealTypeVector) {
      Enumeration reals = DisplayRealTypeVector.elements();
      while (reals.hasMoreElements()) {
        DisplayRealType real = (DisplayRealType) reals.nextElement();
        if (real.getName().equals(name)) {
          return real;
        }
      }
    }
    return null;
  }

  public int getIndex() {
    if (Index <= 0) {
      synchronized (DisplayRealTypeVector) {
        DisplayRealType real =
          DisplayRealType.getDisplayRealTypeByName(getName());
        if (real == null) {
          Count++;
          Index = Count;
          DisplayRealTypeVector.addElement(this);
        }
        else {
          Index = real.getIndex();
        }
      }
    }
    return Index;
  }

  public static int getCount() {
    return Count;
  }

  /** return the unique DisplayTupleType that this
      DisplayRealType is a component of, or return null
      if it is not a component of any DisplayTupleType */
  public DisplayTupleType getTuple() {
    return tuple;
  }

  /** return index of this as component of a
      DisplayTupleType */
  public int getTupleIndex() {
    return tupleIndex;
  }

  /**
   * Sets the DisplayTupleType to which this DisplayRealType will belong.
   * @param t			The DisplayTupleType of which this
   *				DisplayRealType will be a component.
   * @param i			The 0-based component-index for this
   *				DisplayRealType.
   */
  public void setTuple(DisplayTupleType t, int i, boolean c)
         throws VisADException {
    if (tuple != null) {
      throw new DisplayException("DisplayRealType " + getName() +
        " already has DisplayTupleType " + tuple);
    }
    tuple = t;
    tupleIndex = i;
    circular = c;
  }

  /** return true if this DisplayRealType is 'single' */
  public boolean isSingle() {
    return Single;
  }

  /** return default value for this DisplayRealType */
  public double getDefaultValue() {
    return DefaultValue;
  }

  /** return true is a range of values is defined for this
      DisplayRealType, and return the range in range_values[0]
      and range_values[1]; range_values must be passed in as a
      double[2] array */
  public boolean getRange(double[] range_values) {
    if (range) {
      range_values[0] = LowValue;
      range_values[1] = HiValue;
    }
    return range;
  }

  public boolean getText() {
    return text;
  }

  public boolean getCircular() {
    return circular;
  }

}

