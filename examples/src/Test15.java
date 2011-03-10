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

import java.rmi.Naming;
import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test15
  extends TestSkeleton
{
  private String domain;
  private RemoteServer remote_obj = null;

  boolean hasClientServerMode() { return false; }

  public Test15() { }

  public Test15(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { domain = null; }

  public int checkKeyword(String testName, int argc, String[] args)
  {
    if (domain == null) {
      domain = args[argc];
    } else {
      System.err.println(testName + ": Ignoring extra domain \"" +
                         args[argc] + "\"");
    }

    return 1;
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];

    try {
      System.out.println("RemoteClientTestImpl.main: begin remote activity");
      System.out.println("  to " + domain);

      if (domain == null) {
        domain = "///RemoteServerTest";
      }
      else {
        domain = "//" + domain + "/RemoteServerTest";
      }
      RemoteServer remote_obj = (RemoteServer) Naming.lookup(domain);

      System.out.println("connected");

      RemoteDisplay rmtDpy = remote_obj.getDisplay(0);
      dpys[0] = new DisplayImplJ3D(rmtDpy);
    }
    catch (Exception e) {
      System.out.println("collaboration client exception: " + e.getMessage());
      e.printStackTrace(System.out);
    }

    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
  }

  public String toString()
  {
    return " ip.name: collaborative direct manipulation client" +
                "\n\tsecond parameter is server IP name";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test15(args);
  }
}
