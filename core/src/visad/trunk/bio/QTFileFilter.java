//
// QTFileFilter.java
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

import java.io.File;

/** A file filter for finding QTJava.zip, for use with OptionDialog. */
public class QTFileFilter extends javax.swing.filechooser.FileFilter
  implements java.io.FileFilter
{

  /** Absolute path of QTJava.zip file to ignore. */
  private String ignore;

  /** Constructs a new QTJava.zip file filter. */
  public QTFileFilter(String ignore) { this.ignore = ignore; }

  /** accept files with the proper extensions */
  public boolean accept(File f) {
    return f.isDirectory() ||
     (f.getName().equals("QTJava.zip") && !f.getAbsolutePath().equals(ignore));
  }

  /** return the filter's description */
  public String getDescription() { return "QTJava.zip"; }

}
