//
// ExceptionStack.java
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

package visad.data.fits;

import java.util.Vector;
import java.util.Enumeration;

import visad.VisADException;

public class ExceptionStack
	extends VisADException
{
  Vector vec;

  public ExceptionStack()
  {
    super("Exception stack thrown");
    vec = new Vector();
  }

  public ExceptionStack(Exception e)
  {
    super("Exception stack thrown");
    vec = new Vector();
    vec.addElement(e);
  }

  public void addException(Exception e)
  {
    vec.addElement(e);
  }

  public int depth()
  {
    return vec.size();
  }

  public Enumeration exceptions()
  {
    return vec.elements();
  }

/*
  public String getMessage()
  {
    StringBuffer buf = new StringBuffer();

    Enumeration en = vec.elements();
    while (en.hasMoreElements()) {
      Exception e = (Exception )en.nextElement();

      buf.append(e.getMessage());
      buf.append('\n');
    }

    // delete final newline
    buf.setLength(buf.length()-1);

    return buf.toString();
  }

  public String getLocalizedMessage()
  {
    StringBuffer buf = new StringBuffer();

    Enumeration en = vec.elements();
    while (en.hasMoreElements()) {
      Exception e = (Exception )en.nextElement();

      buf.append(e.getLocalizedMessage());
      buf.append('\n');
    }

    // delete final newline
    buf.setLength(buf.length()-1);

    return buf.toString();
  }
*/

  public void printStackTrace()
  {
    printStackTrace(System.err);
  }

  public void printStackTrace(java.io.PrintStream ps)
  {
    Enumeration en = vec.elements();
    while (en.hasMoreElements()) {
      Exception e = (Exception )en.nextElement();

      e.printStackTrace(ps);
    }
    super.printStackTrace(ps);
  }

  public void printStackTrace(java.io.PrintWriter pw)
  {
    Enumeration en = vec.elements();
    while (en.hasMoreElements()) {
      Exception e = (Exception )en.nextElement();

      e.printStackTrace(pw);
    }
    super.printStackTrace(pw);
  }
}
