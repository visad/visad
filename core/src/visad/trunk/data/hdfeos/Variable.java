//
// Variable.java
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


public class Variable
{
  String  name;
  int rank;
  int type;
  DimensionSet  dimSet;
  Calibration calibration;
  boolean coordVar = false;

  Variable( String name,
            DimensionSet dimSet,
            int rank,
            int type,
            Calibration calibration )
  throws HdfeosException
  {
    if ( dimSet.getSize() != rank )
    {
      throw new HdfeosException(" rank and DimensionSet length don't match");
    }

    this.name = name;
    this.dimSet = dimSet;
    this.type = type;
    this.rank = rank;
    this.calibration = calibration;

    if ( (rank == 1) &&
         (name.equalsIgnoreCase((dimSet.getElement(0)).getName())) )
    {
      coordVar = true;
    }
  }

  public String getName()
  {
    String name = this.name;
    return name;
  }

  public int getRank()
  {
    return rank;
  }

  public Calibration getCalibration()
  {
    return calibration;
  }

  public boolean equals( Variable obj )
  {
    if( this.name.equals( obj.getName()) )
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public DimensionSet getDimSet()
  {
    return dimSet;
  }

  public NamedDimension getDim( int ii )
  {
    return dimSet.getElement( ii );
  }

  public int getNumberType()
  {
    return this.type;
  }

  public boolean isCoordVar()
  {
    return coordVar;
  }

  public String toString()
  {
    String str = "Variable:  "+name+"\n"+
                 "    rank:  "+rank+"\n"+
                 "    type:  "+type+"\n"+"  "+dimSet.toString()+"\n";
    return str;
  }
}
