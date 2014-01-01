//
// Shape.java
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

package visad.data.hdfeos;

public class Shape
{
  private DimensionSet  dimSet;
  private VariableSet  varSet;

  public Shape( Variable var  )
  {
    varSet = new VariableSet();
    varSet.add( var );

    dimSet = var.getDimSet();
  }

  public void addVariable( Variable var )
  {
    varSet.add( var );
  }

  public DimensionSet getShape()
  {
    return dimSet;
  }

  public VariableSet getVariables()
  {
    return varSet;
  }

  public int getNumberOfVars()
  {
    return varSet.getSize();
  }

  public Variable getVariable( int index )
  {
    return varSet.getElement( index );
  }

  public boolean memberOf( Variable var )
  {
    DimensionSet d_set = var.getDimSet();

    if( this.dimSet.sameSetSameOrder( d_set ) )
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public Variable isCoordVar( int index )
  {
    if ( dimSet.getSize() == 1 )
    {
      return varSet.isCoordVar( index );
    }
    else
    {
      return null;
    }
  }

  public String toString()
  {
    String str = dimSet.toString()+"  Variables: \n";

    for( int ii = 0; ii < varSet.getSize(); ii++ )
    {
      str = str + "       "+(varSet.getElement(ii)).getName() + "\n";
    }
      str = str + "- - - - - - - - - - - - - - - - - - - \n";

    return str;
  }
}
