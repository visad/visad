//
// ShapeSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.hdfeos;

import java.util.*;

public class ShapeSet
{
  private Vector S_Set;
  private VariableSet c_vars;

  ShapeSet( VariableSet varSet )
  {
    boolean found;

    S_Set = new Vector();
    c_vars = new VariableSet();

    for ( Enumeration e = varSet.getEnum(); e.hasMoreElements(); )
    {
      Variable var = (Variable) e.nextElement();
      if ( var.isCoordVar() )
      {
        c_vars.add(var);
      }
      else
      {
        int count = S_Set.size();

        if ( count == 0 )
        {
          found = true;
          Shape s_obj  = new Shape( var );
          S_Set.addElement( s_obj );
        }
        else
        {
          found = false;

          for ( int ii = 0; ii < count; ii++ )
          {
            Shape s_obj = (Shape)S_Set.elementAt(ii);
            if( s_obj.memberOf( var ) )
            {
              s_obj.addVariable( var );
              found = true;
            }
          }
        }

        if ( !found )
        {
          Shape s_obj = new Shape( var );
          S_Set.addElement( s_obj );
        }
      }
    }
  }

  public Shape getElement( int ii )
  {
    Shape obj = (Shape)S_Set.elementAt( ii );
    return obj;
  }

  public int getSize()
  {
    int size = S_Set.size();
    return size;
  }

  Enumeration getEnum()
  {
    Enumeration e = S_Set.elements();
    return e;
  }

  public boolean isMemberOf( NamedDimension dim )
  {
    DimensionSet d_set;

    for ( int ii = 0; ii < this.getSize(); ii++ )
    {
      d_set = (this.getElement(ii)).getShape();

      if ( d_set.isMemberOf( dim ) ) {
        return true;
      }
    }
    return false;
  }

  public VariableSet get1DVariables()
  {
    return c_vars;
  }

  public String toString()
  {
    String str = " Shapes in this set:   \n";

    for ( int ii = 0; ii < this.getSize(); ii++ )
    {
      str = str + (this.getElement(ii)).toString() + "\n";
    }
    str = str + " - - - - - - - - - - - - - - - - - - \n";

    return str;
  }
}
