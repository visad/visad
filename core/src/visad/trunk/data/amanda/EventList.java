/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.ArrayList;

public abstract class EventList
{
  private ArrayList list = new ArrayList();

  public EventList() { }

  final void add(Object o) { list.add(o); }

  final void addUnique(Object o)
  {
    final int len = list.size();
    for (int i = 0; i < len; i++) {
      if (o.equals(list.get(i))) {
        // don't add duplicates
        return;
      }
    }

    list.add(o);
  }

  public final void dump(java.io.PrintStream out)
  {
    final int num = list.size();
    for (int i = 0; i < num; i++) {
      out.println("  " + list.get(i));
    }
  }

  final Object internalGet(int i)
  {
    if (i < 0 || i >= list.size()) {
      return null;
    }

    return list.get(i);
  }

  final int size() { return list.size(); }

  public final String toString() { return Integer.toString(list.size()); }
}
