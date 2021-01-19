//
// SetType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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
   SetType is the VisAD data type for subsets of R^n for n>0.<P>
*/
public class SetType extends MathType {

  private RealTupleType Domain;

  /** type must be a RealType or a RealTupleType */
  public SetType(MathType type) throws VisADException {
    super();
    if (type instanceof RealType) {
      RealType[] types = {(RealType) type};
      Domain = new RealTupleType(types);
    }
    else if (type instanceof RealTupleType) {
      Domain = (RealTupleType) type;
    }
    else {
      throw new TypeException("SetType: Domain must be RealType or RealTupleType");
    }
  }

  public boolean equals(Object type) {
    if (!(type instanceof SetType)) return false;
    return Domain.equals(((SetType) type).getDomain());
  }

  public boolean equalsExceptName(MathType type) {
    if (!(type instanceof SetType)) return false;
    return Domain.equalsExceptName(((SetType) type).getDomain());
  }

  /*- TDR May 1998  */
  public boolean equalsExceptNameButUnits( MathType type ) {
    if (!(type instanceof SetType)) return false;
    return Domain.equalsExceptNameButUnits(((SetType) type).getDomain());
  }

  /*- TDR June 1998  */
  public MathType cloneDerivative( RealType d_partial )
         throws VisADException
  {
    throw new UnimplementedException("SetType: cloneDerivative");
  }

  /*- TDR July 1998  */
  public MathType binary( MathType type, int op, Vector names )
         throws VisADException
  {
    throw new UnimplementedException("SetType.binary");
  }

  /*- TDR July 1998  */
  public MathType unary( int op, Vector names )
         throws VisADException
  {
    return this; // WLH 3 April 2003
  }

/* WLH 5 Jan 2000
  public String toString() {
    return "Set" + Domain.toString();
  }
*/

  public String prettyString(int indent) {
    // return toString();  WLH 5 Jan 2000
    String s = Domain.toString();
    if (s.lastIndexOf("(") < 0) {
      return "Set(" + s + ")";
    }
    else {
      return "Set" + s;
    }
  }

  public Data missingData() throws VisADException {
    return new DoubleSet(this);
  }

  /** if the domain passed to constructor was a RealType,
      getDomain returns a RealTupleType with that RealType
      as its single component */
  public RealTupleType getDomain() {
    return Domain;
  }

  public ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    return link.getRenderer().makeShadowSetType(this, link, parent);
  }

}

