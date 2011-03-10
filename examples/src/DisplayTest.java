/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;

import visad.VisADException;

/**
    DisplayTest is the general class for testing Displays.<P>
*/
public class DisplayTest {

  static int no_self = 0;

  private static TestSkeleton getTestClass(int num)
  {
    String caseName;
    if (num < 10) {
      caseName = "Test0" + num;
    } else {
      caseName = "Test" + num;
    }

    TestSkeleton skel = null;
    try {
      Class caseClass = Class.forName(caseName);
      try {
        skel = (TestSkeleton )caseClass.newInstance();
      } catch (ClassCastException e1) {
      } catch (InstantiationException e2) {
      } catch (IllegalAccessException e3) {
      }
    } catch (ClassNotFoundException e) {
    }

    return skel;
  }

  /** run 'java visad.java3d.DisplayImplJ3D to test list options */
  public static void main(String[] args)
    throws RemoteException, VisADException
  {

    int caseNum = -1;
    TestSkeleton skel = null;
    if (args.length > 0) {
      try {
        caseNum = Integer.parseInt(args[0]);
      } catch(NumberFormatException e) {
        System.err.println("Bad DisplayTest \"" + caseNum + "\"");
        caseNum = -1;
      }

      if (caseNum != -1) {
        skel = getTestClass(caseNum);
      }
    }

    if (skel != null) {
      String[] nargs = new String[args.length - 1];
      for (int i = 1; i < args.length; i++) {
        nargs[i-1] = args[i];
      }

      skel.processArgs(nargs);
      System.out.println(" " + caseNum + skel);
      skel.startThreads();
    } else {
      System.out.println("To test VisAD's displays, run");
      System.out.println("  java DisplayTest N, where N =");

      for (int n = 0; true; n++) {
        skel = getTestClass(n);
        if (skel == null) {
          break;
        }

        System.out.println(" " + n + skel);
      }
    }
  }
}
