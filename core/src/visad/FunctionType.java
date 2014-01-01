//
// FunctionType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

  /** domain must be a RealType or a RealTupleType;
      range may be any MathType */
  public FunctionType(MathType domain, MathType range) throws VisADException {
    super();
    if (domain == null) {
      throw new TypeException("domain is null");
    }
    if (range == null) {
      throw new TypeException("range is null");
    }
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
            j++;
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

  /** if the domain passed to constructor was a RealType,
      getDomain returns a RealTupleType with that RealType
      as its single component */
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

  /**
   * Returns the hash code of this instance.  If {@link #equals(Object type)},
   * then <code>{@link #hashCode()} == type.hashCode()</code>.
   *
   * @return			The hash code of this instance.
   */
  public int hashCode()
  {
    return Domain.hashCode() ^ Range.hashCode();
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
    if (type == null) {
      throw new TypeException("FunctionType.binary: type may not be null" );
    }
    if (equalsExceptName(type)) {
      return new FunctionType(Domain,
             Range.binary(((FunctionType)type).getRange(), op, names));
    }
    else if (type instanceof RealType ||
             getRange().equalsExceptName(type)) {
      return new FunctionType(Domain, Range.binary(type, op, names));
    }
    else if (type instanceof FunctionType &&
             ((FunctionType) type).getRange().equalsExceptName(this)) {
      return new FunctionType(((FunctionType) type).getDomain(),
        ((FunctionType) type).getRange().binary(this, DataImpl.invertOp(op), names));
    }
    else {
      throw new TypeException("FunctionType.binary: types don't match");
    }
/* WLH 10 Sept 98
    MathType m_type = (type instanceof FunctionType) ?
                      ((FunctionType)type).getRange() : type;
    return (MathType) new FunctionType( Domain, Range.binary( m_type, op, names ));
*/
  }

  /*- TDR July 1998  */
  public MathType unary( int op, Vector names )
         throws VisADException
  {
    return (MathType) new FunctionType( Domain, Range.unary( op, names ));
  }

/* WLH 5 Jan 2000
  public String toString() {
    String t = Real ? " (Real): " : Flat ? " (Flat): " : ": ";
    return "FunctionType" + t + Domain.toString() +
             " -> " + Range.toString();
  }
*/

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
    Set domainSet = new SingletonSet(tuple);
    return getFlat()
        ? new FlatField(this, domainSet)
        : new FieldImpl(this, domainSet);
  }

  public ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    return (ShadowType)
      link.getRenderer().makeShadowFunctionType(this, link, parent);
  }

}

