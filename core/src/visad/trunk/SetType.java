
//
// SetType.java
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
   SetType is the VisAD data type for subsets of R^n for n>0.<P>
*/
public class SetType extends MathType {

  private RealTupleType Domain;

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
    throw new UnimplementedException("binary");
  }

  /*- TDR July 1998  */
  public MathType unary( int op, Vector names )
         throws VisADException
  {
    throw new UnimplementedException("binary");
  }

  public String toString() {
    return "SetType: " + Domain.toString();
  }

  public Data missingData() throws VisADException {
    return new DoubleSet(this);
  }

  public RealTupleType getDomain() {
    return Domain;
  }

  public ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    return link.getRenderer().makeShadowSetType(this, link, parent);
  }

}

