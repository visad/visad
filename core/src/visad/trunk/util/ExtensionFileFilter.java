//
// ExtensionFileFilter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

/** A file filter based on file extensions, for use with a JFileChooser. */
public class ExtensionFileFilter extends FileFilter {
  
  /** list of valid extensions */
  private String[] exts;

  /** description */
  private String desc;

  /** construct a new filter that accepts the given filter */
  public ExtensionFileFilter(String extension, String description) {
    this(new String[] {extension}, description);
  }

  /** construct a new filter that accepts the given filters */
  public ExtensionFileFilter(String[] extensions, String description) {
    exts = new String[extensions.length];
    System.arraycopy(extensions, 0, exts, 0, extensions.length);
    desc = description;
  }

  /** accept files with the proper extentions */
  public boolean accept(File f) {
    if (f.isDirectory()) return true;

    String name = f.getName();
    int index = name.lastIndexOf('.');
    String ext = index < 0 ? "" : name.substring(index + 1);

    for (int i=0; i<exts.length; i++) {
      if (ext.equalsIgnoreCase(exts[i])) return true;
    }

    return false;
  }
    
  /** return the filter's description */
  public String getDescription() {
    return desc;
  }
}
