
//
// TupleType.java
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

import java.rmi.*;

/**
   TupleType is the general VisAD data type for vectors.<P>
*/
public class TupleType extends MathType {

  /** tupleComponents must be accessible to subclasses */
  private MathType[] tupleComponents;

  private boolean Flat; // true if all components are RealType or RealTupleType
  /** realComponents contains all RealType components and RealType components
      of RealTupleType components */
  final private RealType[] realComponents;
  /** range of tupleComponents[i] in realComponents; NEVER USED */
  private int[] lows, his;

  public TupleType(MathType[] types) throws VisADException {
    super();
    boolean allReal = true;
    Flat = true;
    int n = types.length;
    int nFlat = 0;
    tupleComponents = new MathType[n];
    if (n < 1) throw new TypeException("TupleType: # components must be > 0");
    for (int i=0; i<n; i++) {
      if (types[i] == null) {
        throw new TypeException("TupleType: components must be non-null");
      }
      tupleComponents[i] = types[i];
      if (!(types[i] instanceof RealType)) allReal = false;
      if (types[i] instanceof RealType) {
        nFlat++;
      }
      else if (types[i] instanceof RealTupleType) {
        nFlat += ((RealTupleType) types[i]).getDimension();
      }
      else {
        Flat = false;
      }
    }
    if (allReal && !(this instanceof RealTupleType)) {
      throw new TypeException("TupleType: all components are RealType," +
                              " must use RealTupleType");
    }
    realComponents = new RealType[nFlat];
    lows = new int[n];
    his = new int[n];
    int j = 0;
    for (int i=0; i<n; i++) {
      lows[i] = j;
      if (types[i] instanceof RealType) {
        realComponents[j] = (RealType) types[i];
        j++;
      }
      else if (types[i] instanceof RealTupleType) {
        int m = ((RealTupleType) types[i]).getDimension();
        for (int k=0; k<m; k++) {
          realComponents[j] = (RealType)
            ((RealTupleType) types[i]).getComponent(k);
          j++;
        }
      } 
      his[i] = j;
    } 
  }

  /** trusted constructor for initializers (RealTupleType and DisplayTupleType) */
  TupleType(RealType[] types, boolean b) {
    super(b);
    boolean allReal = true;
    Flat = true;
    int n = types.length;
    int nFlat = 0;
    tupleComponents = new MathType[n];
    for (int i=0; i<n; i++) {
      tupleComponents[i] = types[i];
      nFlat++;
    }
    realComponents = new RealType[nFlat];
    lows = new int[n];
    his = new int[n];
    int j = 0;
    for (int i=0; i<n; i++) {
      lows[i] = j;
      if (types[i] instanceof RealType) {
        realComponents[j] = (RealType) types[i];
        j++;
      }
      his[i] = j;
    }
  }

  /** get number of components */
  public int getDimension() {
    return tupleComponents.length;
  }

  public RealType[] getRealComponents() {
    return realComponents;
  }

  public MathType getComponent(int i) throws VisADException {
    if (0 <= i || i < tupleComponents.length) {
      return tupleComponents[i];
    }
    else {
      throw new TypeException("TupleType: component index out of range");
    }
  }

  public boolean equals(Object type) {
    if (!(type instanceof TupleType)) return false;
    try {
      int n = tupleComponents.length;
      if (n != ((TupleType) type).getDimension()) return false;
      for (int i=0; i<n; i++) {
        if (!tupleComponents[i].equals(
                     ((TupleType) type).getComponent(i))) return false;
      }
    }
    catch (VisADException e) {
      return false;
    }
    return true;
  }

  public boolean equalsExceptName(MathType type) {
    if (!(type instanceof TupleType)) return false;
    try {
      int n = tupleComponents.length;
      if (n != ((TupleType) type).getDimension()) return false;
      boolean flag = true;
      for (int i=0; i<n; i++) {
        flag = flag && tupleComponents[i].equalsExceptName(
                              ((TupleType) type).getComponent(i) );
      }
      return flag;
    }
    catch (VisADException e) {
      return false;
    }
  }

  public boolean getFlat() {
    return Flat;
  }

  public String toString() {
    String t = "(" + tupleComponents[0].toString();
    for (int i=1; i<tupleComponents.length; i++) {
      t = t + ", " + tupleComponents[i].toString();
    }
    return t + ")";
  }

  public Data missingData() {
    return new Tuple(this);
  }

  ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    return new ShadowTupleType(this, link, parent);
  }

}

