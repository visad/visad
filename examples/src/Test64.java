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

import java.awt.*;
import java.awt.event.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import javax.swing.*;
import visad.*;

public class Test64
  extends TestSkeleton
{
  private String domain;

  boolean hasClientServerMode() { return false; }

  public Test64() { }

  public Test64(String args[])
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
    return null;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
  }

  void setupUI(LocalDisplay[] dpys) throws VisADException, RemoteException {
    JFrame jframe  = new JFrame("Remote slave display client");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    jframe.setContentPane(p);

    try {
      System.out.print("Connecting to ");
      if (domain == null) {
        System.out.print("localhost...");
        domain = "///RemoteSlaveDisplayTest";
      }
      else {
        System.out.print(domain + "...");
        domain = "//" + domain + "/RemoteSlaveDisplayTest";
      }
      RemoteServer server = (RemoteServer) Naming.lookup(domain);
      RemoteDisplay[] rmt_dpys = server.getDisplays();
      RemoteDisplay display = rmt_dpys[0];
      RemoteSlaveDisplayImpl rsdi = new RemoteSlaveDisplayImpl(display);
      p.add(rsdi.getComponent());
      System.out.println("connected");
    }
    catch (java.rmi.ConnectException e) {
      System.out.println("couldn't connect!");
      System.out.println("Make sure there is a server running at the " +
                         "specified IP address.");
      System.exit(1);
    }
    catch (Exception e) {
      System.out.println("slave display client exception: " + e.getMessage());
      e.printStackTrace();
      System.exit(2);
    }

    jframe.pack();
    jframe.setVisible(true);
  }

  public String toString()
  {
    return " [ip.name]: remote slave display client" +
                "\n\tsecond parameter is server IP name (default = localhost)";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test64(args);
  }
}
