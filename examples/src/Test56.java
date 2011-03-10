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

import visad.java2d.DirectManipulationRendererJ2D;
import visad.java2d.DisplayImplJ2D;

public class Test56
  extends UISkeleton
{
  private String domain;

  boolean hasClientServerMode() { return false; }

  public Test56() { }

  public Test56(String[] args)
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
    dpys[0] = new DisplayImplJ2D("display");
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
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

      RemoteDataReference histogram_ref = remote_obj.getDataReference(0);
      RemoteDataReference direct_ref = remote_obj.getDataReference(1);
      RemoteDataReference direct_tuple_ref = remote_obj.getDataReference(2);

      RealTupleType dtype;
      dtype = (RealTupleType) direct_tuple_ref.getData().getType();

      dpys[0].addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                    Display.XAxis));
      dpys[0].addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                    Display.YAxis));

      GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
      mode.setPointSize(5.0f);
      mode.setPointMode(false);

      if (!(dpys[0] instanceof DisplayImpl)) {
        throw new VisADException("Need DisplayImpl for collaborative client test");
      }
      DisplayImpl di = (DisplayImpl )dpys[0];

      RemoteDisplayImpl remote_display1 = new RemoteDisplayImpl(di);
      DataReference[] refs151 = {histogram_ref};
      remote_display1.addReferences(new DirectManipulationRendererJ2D(),
                                    refs151, null);

      DataReference[] refs152 = {direct_ref};
      remote_display1.addReferences(new DirectManipulationRendererJ2D(),
                                    refs152, null);

      DataReference[] refs153 = {direct_tuple_ref};
      remote_display1.addReferences(new DirectManipulationRendererJ2D(),
                                    refs153, null);
    }
    catch (Exception e) {
      System.out.println("collaboration client exception: " + e.getMessage());
      e.printStackTrace(System.out);
      System.exit(1);
    }
  }

  public String toString()
  {
    return " ip.name: collaborative direct manipulation client in Java2D" +
                "\n\tsecond parameter is server IP name";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test56(args);
  }
}
