//
// TupleType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;
import java.util.*;

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

  /** array of component types */
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

  /** get array of components */
  public MathType[] getComponents() {
    return tupleComponents;
  }

  public RealType[] getRealComponents() {
    return realComponents;
  }

  public int getNumberOfRealComponents() {
    return realComponents.length;
  }

  /**
   * A wrapper around {@link #getComponent(int) getComponent} for JPython.
   *
   * @return The requested Data object.
   */
  public MathType __getitem__(int index) throws VisADException {
    return getComponent(index);
  }

  /**
   * A wrapper around {@link #getComponents() getComponents} for JPython.
   *
   * @return The number of components of this TupleType
   */
  public int __len__() {
    return tupleComponents.length;
  }

  /** return component for i between 0 and getDimension() - 1 */
  public MathType getComponent(int i) throws VisADException {
    if (0 <= i && i < tupleComponents.length) {
      return tupleComponents[i];
    }
    else {
      throw new TypeException("TupleType: component index out of range");
    }
  }

  /** return index of first RealType component with name;
      if no such component, return -1 */
  public int getIndex(String name) {
    return getIndex(RealType.getRealTypeByName(name));
  }

  /** return index of first component with type;
      if no such component, return -1 */
  public int getIndex(MathType type) {
    for (int i=0; i<tupleComponents.length; i++) {
      if (tupleComponents[i].equals(type)) {
        return i;
      }
    }
    return -1;
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

  public int hashCode() {
    return Arrays.asList(tupleComponents).hashCode();
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

  /*- TDR June 1998    */
  public boolean equalsExceptNameButUnits(MathType type) {
    if (!(type instanceof TupleType)) return false;
    try {
      int n = tupleComponents.length;
      if (n != ((TupleType) type).getDimension()) return false;
      boolean flag = true;
      for (int i=0; i<n; i++) {
        flag = flag && tupleComponents[i].equalsExceptNameButUnits(
                              ((TupleType) type).getComponent(i) );
      }
      return flag;
    }
    catch (VisADException e) {
      return false;
    }
  }

  /*- TDR June 1998  */
  public MathType cloneDerivative( RealType d_partial )
         throws VisADException
  {
    int n_comps = tupleComponents.length;
    MathType[] new_types = new MathType[ n_comps ];
    boolean allReal = true;

    for ( int ii = 0; ii < n_comps; ii++ ) {
      new_types[ii] = (this.getComponent(ii)).cloneDerivative( d_partial );
      if (!(new_types[ii] instanceof RealType) ) {
        allReal = false;
      }
    }
    if ( allReal ) {
      RealType[] r_types = new RealType[ n_comps ];
      for ( int ii = 0; ii < n_comps; ii++ ) {
        r_types[ii] = (RealType) new_types[ii];
      }
      return new RealTupleType( r_types );
    }
    else {
      return new TupleType( new_types );
    }
  }

  /*- TDR July 1998  */
  public MathType binary( MathType type, int op, Vector names )
         throws VisADException
  {
    if (type == null) {
      throw new TypeException("TupleType.binary: type may not be null" );
    }
    if (type instanceof RealTupleType) {
      throw new TypeException("TupleType.binary: types don't match" );
    }
    else if (type instanceof TupleType) {
      int n_comps = tupleComponents.length;
      MathType[] new_types = new MathType[ n_comps ];
      for ( int ii = 0; ii < n_comps; ii++ ) {
        MathType type_component = ((TupleType) type).getComponent(ii);
        new_types[ii] = (this.getComponent(ii)).binary( type_component, op, names );
      }
      return new TupleType( new_types );
    }
    else if (type instanceof RealType) {
      int n_comps = tupleComponents.length;
      MathType[] new_types = new MathType[ n_comps ];
      for ( int ii = 0; ii < n_comps; ii++ ) {
        new_types[ii] = (this.getComponent(ii)).binary( type, op, names );
      }
      return new TupleType( new_types );
    }
    else if (type instanceof FunctionType &&
             ((FunctionType) type).getRange().equalsExceptName(this)) {
      return new FunctionType(((FunctionType) type).getDomain(),
        ((FunctionType) type).getRange().binary(this, DataImpl.invertOp(op), names));
    }
    else {
      throw new TypeException("TupleType.binary: types don't match" );
    }
/* WLH 10 Sept 98
    int n_comps = tupleComponents.length;
    MathType[] new_types = new MathType[ n_comps ];
    for ( int ii = 0; ii < n_comps; ii++ ) {
      new_types[ii] = (this.getComponent(ii)).binary( type, op, names );
    }

    return new TupleType( new_types );
*/
  }

  /*- TDR July 1998  */
  public MathType unary( int op, Vector names )
         throws VisADException
  {
    int n_comps = tupleComponents.length;
    MathType[] new_types = new MathType[ n_comps ];
    for ( int ii = 0; ii < n_comps; ii++ ) {
      new_types[ii] = (this.getComponent(ii)).unary( op, names );
    }

    return new TupleType( new_types );
  }


  /**
   * Is this a "flat" tuple?
   *
   * @return true if all components are RealType or RealTupleType.
   */
  public boolean getFlat() {
    return Flat;
  }

/* WLH 5 Jan 2000
  public String toString() {
    String t = "(" + tupleComponents[0].toString();
    for (int i=1; i<tupleComponents.length; i++) {
      t = t + ", " + tupleComponents[i].toString();
    }
    return t + ")";
  }
*/

  public String prettyString(int indent) {
    int n = tupleComponents.length;
    String[] cs = new String[n];
    int[] lens = new int[n];
    int maxlen = 0;
    int sumlen = 0;
    for (int i=0; i<n; i++) {
      cs[i] = tupleComponents[i].prettyString(indent+1);
      lens[i] = cs[i].length();
      if (lens[i] > maxlen) maxlen = lens[i];
      sumlen += lens[i];
    }
    if (sumlen + indent <= 72) {
      String s = "(" + cs[0];
      for (int i=1; i<n; i++) {
        s = s + ", " + cs[i];
      }
      return s + ")";
    }
    else {
      String blanks = "";
      for (int j=0; j<indent+1; j++) blanks = blanks + " ";
      String s = "(" + cs[0];
      if (1 < n) s = s + ",";
      for (int i=1; i<n; i++) {
        s = s + "\n" + blanks + cs[i];
        if (i+1 < n) s = s + ",";
      }
      return s + ")";
    }
  }

  public Data missingData() {
    return new Tuple(this);
  }

  public ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    return link.getRenderer().makeShadowTupleType(this, link, parent);
  }

}

