/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.Arrays;
import java.util.ArrayList;

public class ModuleList
{
  private ArrayList list;
  private Module[] sortedArray;

  public ModuleList()
  {
    list = null;
    sortedArray = null;
  }

  public void add(Module mod)
  {
    if (list == null) {
      list = new ArrayList();
    }

    list.add(mod);
  }

  public void dump(java.io.PrintStream out)
  {
    // if there are modules to be sorted, do it now
    if (list != null && list.size() > 0) {
      sort();
    }

    // if there are no modules, we're done dumping
    if (sortedArray == null) {
      return;
    }

    final int nMods = sortedArray.length;
    for (int i = 0; i < nMods; i++) {
      out.println(sortedArray[i]);
    }
  }

  public Module find(int number)
  {
    // if there are modules to be sorted, do it now
    if (list != null && list.size() > 0) {
      sort();
    }

    // if one or more sorted modules exist...
    if (sortedArray != null) {

      // look for the specified module number...
      int idx = Arrays.binarySearch(sortedArray, new Module(number));
      if (idx >= 0) {

        // return the desired module
        return sortedArray[idx];
      }
    }

    // couldn't find a module with that number
    return null;
  }

  public Module get(int i)
  {
    // if there are modules to be sorted, do it now
    if (list != null && list.size() > 0) {
      sort();
    }

    if (i < 0 || sortedArray == null || i >= sortedArray.length) {
      return null;
    }

    return sortedArray[i];
  }

  public final boolean isInitialized()
  {
    return (sortedArray != null || (list != null && list.size() > 0));
  }

  public int size()
  {
    int len = 0;

    if (list != null) {
      len += list.size();
    }

    if (sortedArray != null) {
      len += sortedArray.length;
    }

    return len;
  }

  private void sort()
  {
    // if some modules have been sorted...
    if (sortedArray != null) {

      // merge in previously sorted list of modules
      for (int i = 0; i < sortedArray.length; i++) {
        list.add(sortedArray[i]);
      }
    }

    // sort modules
    sortedArray = (Module[] )list.toArray(new Module[list.size()]);
    Arrays.sort(sortedArray);

    // out with the old
    list.clear();
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer("ModuleList[");

    boolean isEmpty = true;

    if (list != null && list.size() > 0) {
      buf.append("unsorted=");
      buf.append(list.size());
      isEmpty = false;
    }

    if (sortedArray != null) {
      buf.append("sorted=");
      buf.append(sortedArray.length);
      isEmpty = false;
    }

    if (isEmpty) buf.append("empty");

    buf.append(']');
    return buf.toString();
  }
}
