
//
// TextType.java
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
   TextType is the VisAD scalar data type for text string variables.<P>
*/
public class TextType extends ScalarType {

  public final static TextType Generic = new TextType("GENERIC_TEXT", true);

  public TextType(String name) throws VisADException {
    super(name);
  }

  TextType(String name, boolean b) {
    super(name, b);
  }

  public boolean equals(Object type) {
    if (!(type instanceof TextType)) return false;
    return (this == type);
  }

  public boolean equalsExceptName(MathType type) {
    return (type instanceof TextType);
  }

  /*- TDR May 1998  */
  public boolean equalsExceptNameButUnits( MathType type )
         throws VisADException
  {
    throw new UnimplementedException("TextType: equalsExceptNameButUnits");
  }

  /*- TDR June 1998  */
  public MathType cloneDerivative( RealType d_partial )
         throws VisADException
  {
    throw new UnimplementedException("TexType: cloneDerivative");
  }

  /*- TDR July 1998  */
  public MathType binary( MathType type, int op, Vector names )
         throws VisADException
  {
    throw new UnimplementedException("TextType: binary");
  }

  /*- TDR July 1998  */
  public MathType unary( int op, Vector names )
         throws VisADException
  {
    throw new UnimplementedException("TextType: unary");
  }
  
  public String toString() {
    return getName() + "(Text)";
  }

  public Data missingData() throws VisADException {
    return new Text(this);
  }

  public ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    return link.getRenderer().makeShadowTextType(this, link, parent);
  }

}

