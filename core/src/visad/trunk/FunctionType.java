
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

  public String toString() {
    String t = Real ? " (Real): " : Flat ? " (Flat): " : ": ";
    return "FunctionType" + t + Domain.toString() +
             " -> " + Range.toString();
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

