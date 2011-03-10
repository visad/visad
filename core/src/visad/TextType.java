//
// TextType.java
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
import java.util.Vector;

/**
   TextType is the VisAD scalar data type for text string variables.<P>
*/
public class TextType extends ScalarType {

  public final static TextType Generic = new TextType("GENERIC_TEXT", true);

  /** name of type (two TextTypes are equal if their names are equal) */
  public TextType(String name) throws VisADException {
    super(name);
  }

  TextType(String name, boolean b) {
    super(name, b);
  }

  public boolean equals(Object type) {
    if (!(type instanceof TextType)) return false;
    return getName().equals(((TextType )type).getName());
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
    if (type == null) {
      throw new TypeException("TextType.binary: type may not be null" );
    }
    if (type instanceof TextType) {
      return this;
    }
    else {
      throw new TypeException("TextType.binary: types don't match" );
    }
/* WLH 10 Sept 98
    throw new UnimplementedException("TextType: binary");
*/
  }

  /*- TDR July 1998  */
  public MathType unary( int op, Vector names )
         throws VisADException
  {
    throw new UnimplementedException("TextType: unary");
  }

  /** create a new TextType, or return it if it already exists */
  public static synchronized TextType getTextType(String name) {
    TextType result = getTextTypeByName(name);
    if (result == null) {
      try {
        result = new TextType(name);
      }
      catch (VisADException e) {
        result = null;
      }
    }
    return result;
  }

  /** return any TextType constructed in this JVM with name,
      or null */
  public static synchronized TextType getTextTypeByName(String name) {
    ScalarType text = ScalarType.getScalarTypeByName(name);
    if (!(text instanceof TextType)) {
      return null;
    }
    return (TextType) text;
  }

/* WLH 5 Jan 2000
  public String toString() {
    return getName() + "(Text)";
  }
*/

  public String prettyString(int indent) {
    // return toString();  WLH 5 Jan 2000
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

