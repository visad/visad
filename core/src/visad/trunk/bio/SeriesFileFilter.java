//
// SeriesFileFilter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import java.io.*;
import javax.swing.filechooser.FileFilter;

/** A file filter for identifying data series. */
public class SeriesFileFilter extends FileFilter implements FilenameFilter {

  /** Returns whether the series file filter accepts the given file. */
  public boolean accept(File dir, String name) {
    return accept(new File(dir, name));
  }

  /** Returns whether the series file filter accepts the given file. */
  public boolean accept(File f) {
    if (f.isDirectory()) return true;
    String name = f.getName();
    int dot = name.lastIndexOf(".");
    if (dot >= 0) name = name.substring(0, dot);
    int len = name.length();
    if (len < 1) return false;
    char last = name.charAt(len - 1);
    return (last >= '0' && last <= '9');
  }

  /** Returns the description of the series file filter. */
  public String getDescription() { return "Multiple file data series"; }

}
