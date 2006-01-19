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

package visad.install;

import java.io.File;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Path
{
  private ArrayList path;

  public Path(String pathStr)
    throws IllegalArgumentException
  {
    if (pathStr == null) {
      throw new IllegalArgumentException("Null path string");
    }
    if (pathStr.length() == 0) {
      throw new IllegalArgumentException("Empty path string");
    }

    StringTokenizer tok = new StringTokenizer(pathStr, File.pathSeparator);

    final int numTokens = tok.countTokens();
    if (numTokens == 0) {
      throw new IllegalArgumentException("Empty path string");
    }

    path = new ArrayList();

    int i = 0;
    while (tok.hasMoreTokens()) {
      path.add(tok.nextToken());
    }
  }

  public ArrayList find(String file)
  {
    if (file == null || file.length() == 0) {
      return null;
    }

    final int pathLen = path.size();

    ArrayList list = null;
    for (int i = 0; i < pathLen; i++) {
      File f = new File((String )path.get(i), file);
      if (f.isFile()) {
        if (list == null) {
          list = new ArrayList();
        }

        list.add(f);
      }
    }

    if (list == null) {
      return null;
    }

    return list;
  }

  public ArrayList findMatch(String file)
  {
    if (file == null || file.length() == 0) {
      return null;
    }

    final int pathLen = path.size();

    ArrayList list = null;
    for (int i = 0; i < pathLen; i++) {
      String pElem = (String )path.get(i);

      if (pElem.endsWith(File.separator + file)) {
        File f = new File(pElem);
        if (f.exists()) {
          if (list == null) {
            list = new ArrayList();
          }

          list.add(f);
        }
      }
    }

    return list;
  }

  public String toString()
  {
    if (path == null || path.size() == 0) {
      return null;
    }

    StringBuffer buf = new StringBuffer((String )path.get(0));
    for (int i = 1; i < path.size(); i++) {
      buf.append(File.pathSeparator);
      buf.append(path.get(i));
    }

    return buf.toString();
  }

  public static final void main(String[] args)
    throws IllegalArgumentException
  {
    if (args.length < 2) {
      System.err.println("Usage: java Path pathString file [file ...]");
      System.exit(1);
      return;
    }

    Path p = new Path(args[0]);
    for (int i = 1; i < args.length; i++) {
      ArrayList l = p.findMatch(args[i]);
      if (l == null) {
        l = p.find(args[i]);
        if (l == null) {
          System.err.println("Couldn't find \"" + args[i] + "\"");
          continue;
        }
      }

      System.out.println(args[i] + ":");
      for (int j = 0; j < l.size(); j++) {
        System.out.println("  " + l.get(j));
      }
    }

    System.exit(0);
  }
}
