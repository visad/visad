//
// FormFileFilter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
import visad.data.FormFileInformer;

/**
 * A file filter based on a file form adapter's isThisType(String) method,
 * for use with a JFileChooser.
 */
public class FormFileFilter extends FileFilter
  implements java.io.FileFilter, Comparable
{

  // -- Fields --

  /** Associated file form implementing the FormFileAdapter interface. */
  private FormFileInformer form;

  /** Description. */
  private String desc;


  // -- Constructors --

  /** Constructs a new filter that accepts the given extension. */
  public FormFileFilter(FormFileInformer form, String description) {
    this.form = form;
    desc = description;
  }


  // -- FileFilter API methods --

  /** Accepts files with the proper extensions. */
  public boolean accept(File f) {
    if (f.isDirectory()) return true;
    return form.isThisType(f.getPath());
  }

  /** return the filter's description */
  public String getDescription() {
    return desc;
  }


  // -- Comparable API methods --

  /** Compares two FileFilter objects alphanumerically. */
  public int compareTo(Object o) {
    return desc.compareTo(((FileFilter) o).getDescription());
  }

}
