/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.amanda;

public class Module
  extends Point
  implements Comparable
{
  private int number;
  private int string, stringOrder;

  Module(int number)
  {
    this(number, Float.NaN, Float.NaN, Float.NaN, -1, -1);
  }

  Module(int number, float x, float y, float z, int string, int stringOrder)
  {
    super(x, y, z);

    this.number = number;
    this.string = string;
    this.stringOrder = stringOrder;
  }

  public int compareTo(Object obj)
  {
    if (!(obj instanceof Module)) {
      return getClass().toString().compareTo(obj.getClass().toString());
    }

    return compareTo((Module )obj);
  }

  public int compareTo(Module mod)
  {
    return (number - mod.number);
  }

  public boolean equals(Object o) { return (compareTo(o) == 0); }

  int getNumber() { return number; }

  public String toString()
  {
    return "Module#" + number + super.toString() +
      "Str#" + string + "/" + stringOrder;
  }
}
