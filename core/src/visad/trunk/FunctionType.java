
//
// FunctionType.java
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

import java.rmi.*;
import java.util.Vector;

/**
   FunctionType is the VisAD data type for functions.<P>

   A Function domain type may be either a RealType (for a function with
   domain = R) or a RealTupleType (for a function with domain = R^n for
   n > 0).<P>
*/
public class FunctionType extends MathType {

  private RealTupleType Domain;
  private MathType Range;
  private RealTupleType FlatRange;
  private boolean Real;  // true if range is RealType or RealTupleType
  private boolean Flat;  // true if Real or if range is Flat TupleType

  /** this is an array of RealType-s that are RealType 
      components of Range or RealType components of
      RealTupleType components of Range;
      a non_realType and non-TupleType Range is marked by null;
      components of a TupleType Range that are neither
      RealType nor RealTupleType are ignored */
  private RealType[] realComponents;

  /** array of TextType Range components */
  private TextType[] textComponents;
  /** array of component indices of TextType Range components */
  private int[] textIndices;

  public final static FunctionType REAL_1TO1_FUNCTION =
    new FunctionType(RealType.Generic, RealType.Generic, true);
  private static RealType[] real3 =
    {RealType.Generic, RealType.Generic, RealType.Generic};
  public final static FunctionType REAL_1TO3_FUNCTION =
    new FunctionType(RealType.Generic, new RealTupleType(real3, true), true);
  private static RealType[] real4 =
    {RealType.Generic, RealType.Generic, RealType.Generic, RealType.Generic};
  public final static FunctionType REAL_1TO4_FUNCTION =
    new FunctionType(RealType.Generic, new RealTupleType(real4, true), true);

  public FunctionType(MathType domain, MathType range) throws VisADException {
    super();
    if (!(domain instanceof RealTupleType || domain instanceof RealType)) {
      throw new TypeException("FunctionType: domain must be RealTupleType" +
                              " or RealType");
    }
    Domain = makeFlat(domain);

    Real = range instanceof RealType ||
           range instanceof RealTupleType;
    Flat = Real ||
           (range instanceof TupleType && ((TupleType) range).getFlat());
    Range = range;
    FlatRange = Flat ? makeFlat(range) : null;
    realComponents = getComponents(Range);
    makeTextComponents();
  }

  /** trusted constructor for initializers */
  FunctionType(MathType domain, MathType range, boolean b) {
    super(b);
    Domain = makeFlatTrusted(domain);
    Real = range instanceof RealType ||
           range instanceof RealTupleType;
    Flat = Real ||
           (range instanceof TupleType && ((TupleType) range).getFlat());
    Range = range;
    FlatRange = Flat ? makeFlatTrusted(range) : null;
    realComponents = getComponents(Range);
    makeTextComponents();
  }

  private void makeTextComponents() {
    int n = 0;
    textComponents = null;
    textIndices = null;
    if (Range instanceof TextType) {
      textComponents = new TextType[] {(TextType) Range};
      textIndices = new int[] {0};
      n = 1;
    }
    else if (Range instanceof TupleType) {
      try {
        for (int i=0; i<((TupleType) Range).getDimension(); i++) {
          if (((TupleType) Range).getComponent(i) instanceof TextType) n++;
        }
        if (n == 0) return;
        textComponents = new TextType[n];
        textIndices = new int[n];
        int j = 0;
        for (int i=0; i<((TupleType) Range).getDimension(); i++) {
          if (((TupleType) Range).getComponent(i) instanceof TextType) {
            textComponents[j] = (TextType) ((TupleType) Range).getComponent(i);
            textIndices[j] = i;
          }
        }
      }
      catch (VisADException e) {
        textComponents = null;
        textIndices = null;
      }
    }
  }

  public TextType[] getTextComponents() {
    return textComponents;
  }

  public int[] getTextIndices() {
    return textIndices;
  }

  private static RealType[] getComponents(MathType type) {
    RealType[] reals;
    if (type instanceof RealType) {
      RealType[] r = {(RealType) type};
      return r;
    }
    else if (type instanceof TupleType) {
      return ((TupleType) type).getRealComponents();
    }
    else {
      return null;
    }
  }

  private static RealTupleType makeFlat(MathType type) throws VisADException {
    if (type instanceof RealTupleType) {
      return (RealTupleType) type;
    }
    else if (type instanceof RealType) {
      RealType[] types = {(RealType) type};
      return new RealTupleType(types, null, ((RealType) type).getDefaultSet());
    }
    else if (type instanceof TupleType && ((TupleType) type).getFlat()) {
      return new RealTupleType(((TupleType) type).getRealComponents());
    }
    else {
      throw new TypeException("FunctionType: illegal input to makeFlat");
    }
  }

  private static RealTupleType makeFlatTrusted(MathType type) {
    if (type instanceof RealTupleType) {
      return (RealTupleType) type;
    }
    else if (type instanceof RealType) {
      RealType[] types = {(RealType) type};
      return new RealTupleType(types, true);
    }
    else if (type instanceof TupleType && ((TupleType) type).getFlat()) {
      return new RealTupleType(((TupleType) type).getRealComponents(), true);
    }
    else {
      return null;
    }
  }

  public RealTupleType getDomain() {
    return Domain;
  }

  public MathType getRange() {
    return Range;
  }

  public boolean getFlat() {
    return Flat;
  }

  public boolean getReal() {
    return Real;
  }

  public RealTupleType getFlatRange() {
    return FlatRange;
  }

  public RealType[] getRealComponents() {
    return realComponents;
  }

  public boolean equals(Object type) {
    if (!(type instanceof FunctionType)) return false;
    return (Domain.equals(((FunctionType) type).getDomain()) &&
            Range.equals(((FunctionType) type).getRange()));
  }

  public boolean equalsExceptName(MathType type) {
    if (!(type instanceof FunctionType)) return false;
    return (Domain.equalsExceptName(((FunctionType) type).getDomain()) &&
            Range.equalsExceptName(((FunctionType) type).getRange()));
  }

  /*- TDR May 1998    */
  public boolean equalsExceptNameButUnits(MathType type) throws VisADException {
    if (!(type instanceof FunctionType)) return false;
    return (Domain.equalsExceptNameButUnits(((FunctionType) type).getDomain()) &&
            Range.equalsExceptNameButUnits(((FunctionType) type).getRange()));
  }

  /*- TDR June 1998   */
  public MathType cloneDerivative( RealType d_partial )
         throws VisADException
  {
    return (MathType) new FunctionType( Domain, Range.cloneDerivative(d_partial));
  }

  /*- TDR July 1998  */
  public MathType binary( MathType type, int op, Vector names )
         throws VisADException
  {
    MathType m_type = (type instanceof FunctionType) ? 
                      ((FunctionType)type).getRange() : type;
    return (MathType) new FunctionType( Domain, Range.binary( m_type, op, names ));
  }

  /*- TDR July 1998  */
  public MathType unary( int op, Vector names )
         throws VisADException
  {
    return (MathType) new FunctionType( Domain, Range.unary( op, names ));
  }


  public String toString() {
    String t = Real ? " (Real): " : Flat ? " (Flat): " : ": ";
    return "FunctionType" + t + Domain.toString() +
             " -> " + Range.toString();
  }

  public String prettyString(int indent) {
    String ds = "(" + Domain.prettyString(indent) + " -> ";
    int n = ds.length();
    String rs = Range.prettyString(indent + n) + ")";
    return ds + rs;
  }

  public Data missingData() throws VisADException, RemoteException {
    int n = Domain.getDimension();
    double[] values = new double[n];
    for (int i=0; i<n; i++) values[i] = 0.0;
    RealTuple tuple = new RealTuple(Domain, values);
    return new FieldImpl(this, new SingletonSet(tuple));
  }

  public ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    return (ShadowType)
      link.getRenderer().makeShadowFunctionType(this, link, parent);
  }

}

