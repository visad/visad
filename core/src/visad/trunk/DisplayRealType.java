
//
// DisplayRealType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

  private boolean System;   // true if this is a system intrinsic

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
    System = true;
    Single = single;
    LowValue = low;
    HiValue = hi;
    range = !(Double.isInfinite(low) || Double.isNaN(low) ||
              Double.isInfinite(hi) || Double.isNaN(hi));
    DefaultValue = def;
    tuple = null;
    tupleIndex = -1;
    synchronized (DisplayRealTypeVector) {
      Count++;
      Index = Count;
      DisplayRealTypeVector.addElement(this);
    }
  }

  /** public constructor for user-defined DisplayRealType's */
  public DisplayRealType(String name, boolean single, double low, double hi,
                         double def, Unit unit)
         throws VisADException {
    super("Display" + name, unit, null);
    System = false; 
    Single = single;
    LowValue = low;
    HiValue = hi;
    range = !(Double.isInfinite(low) || Double.isNaN(low) ||
              Double.isInfinite(hi) || Double.isNaN(hi));
    DefaultValue = def;
    tuple = null;
    tupleIndex = -1;
    synchronized (DisplayRealTypeVector) {
      Count++;
      Index = Count;
      if (DisplayRealType.getDisplayRealTypeByName(getName()) != null) {
        throw new TypeException("DisplayRealType: name already used");
      }
      DisplayRealTypeVector.addElement(this);
    }
  }

  /** public constructor for user-defined DisplayRealType's */
  public DisplayRealType(String name, boolean single, double def,
                         Unit unit) throws VisADException {
    this(name, single, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
         def, unit);
  }

  private static DisplayRealType getDisplayRealTypeByName(String name) {
    Enumeration reals = DisplayRealTypeVector.elements();
    while (reals.hasMoreElements()) {
      DisplayRealType real = (DisplayRealType) reals.nextElement();
      if (real.getName().equals(name)) {
        return real;
      }
    }
    return null;
  }

  int getIndex() {
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

  public DisplayTupleType getTuple() {
    return tuple;
  }

  public int getTupleIndex() {
    return tupleIndex;
  }

  public void setTuple(DisplayTupleType t, int i) {
    tuple = t;
    tupleIndex = i;
  }

  public boolean isSingle() {
    return Single;
  }

  public double getDefaultValue() {
    return DefaultValue;
  }

  public boolean getRange(double[] range_values) {
    if (range) {
      range_values[0] = LowValue;
      range_values[1] = HiValue;
    }
    return range;
  }

}

