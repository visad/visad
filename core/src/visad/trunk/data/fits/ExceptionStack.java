//
// ExceptionStack.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
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

    Enumeration enum = vec.elements();
    while (enum.hasMoreElements()) {
      Exception e = (Exception )enum.nextElement();

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

    Enumeration enum = vec.elements();
    while (enum.hasMoreElements()) {
      Exception e = (Exception )enum.nextElement();

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
    Enumeration enum = vec.elements();
    while (enum.hasMoreElements()) {
      Exception e = (Exception )enum.nextElement();

      e.printStackTrace(ps);
    }
    super.printStackTrace(ps);
  }

  public void printStackTrace(java.io.PrintWriter pw)
  {
    Enumeration enum = vec.elements();
    while (enum.hasMoreElements()) {
      Exception e = (Exception )enum.nextElement();

      e.printStackTrace(pw);
    }
    super.printStackTrace(pw);
  }
}
