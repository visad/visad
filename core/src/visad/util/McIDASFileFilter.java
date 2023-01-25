//
// McIDASFileFilter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * A file filter for McIDAS area and map files, for use with a JFileChooser.
 *
 * @deprecated use FormFileFilter (see Util.getVisADFileChooser)
 */
public class McIDASFileFilter extends FileFilter {
  
  /** construct a new filter for McIDAS AREA files */
  public McIDASFileFilter() { }

  /** accept files with the proper filename prefix */
  public boolean accept(File f) {
    if (f.isDirectory()) return true;
    String name = f.getName();
    return name.startsWith("AREA") || name.endsWith("area") ||
      name.startsWith("OUTL");
  }
    
  /** return the filter's description */
  public String getDescription() {
    return "McIDAS area and map files (AREA*, *area, OUTL*)";
  }
}
