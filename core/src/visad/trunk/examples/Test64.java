/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Component;
import java.awt.event.*;
import java.rmi.RemoteException;
import javax.swing.*;
import visad.*;
import visad.java2d.DisplayImplJ2D;

public class Test64
  extends UISkeleton
{
  public Test64() { }

  public Test64(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = new DisplayImplJ2D("display1");
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType count = new RealType("count", null, null);
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);

    int size = 64;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);

    dpys[0].addMap(new ScalarMap(count, Display.YAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.XAxis));

    dpys[0].addMap(new ConstantMap(0.0, Display.Red));
    dpys[0].addMap(new ConstantMap(1.0, Display.Green));
    dpys[0].addMap(new ConstantMap(0.0, Display.Blue));

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    dpys[0].addReference(ref_histogram1, null);
  }

  String getFrameTitle() { return "VisAD slave display (Java2D)"; }

  Component getSpecialComponent(DisplayImpl[] dpys)
    throws RemoteException, VisADException
  {
    RemoteDisplayImpl rdi = new RemoteDisplayImpl(dpys[0]);
    RemoteSlaveDisplayImpl rsdi = new RemoteSlaveDisplayImpl(rdi);
    return rsdi.getComponent();
  }

  void setupUI(DisplayImpl[] dpys)
    throws RemoteException, VisADException
  {
    super.setupUI(dpys);
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    for (int i=0; i<dpys.length; i++) {
      Component comp = dpys[i].getComponent();
      panel.add(comp);
    }

    JFrame jframe = new JFrame(getFrameTitle() + getClientServerTitle());
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    jframe.setContentPane(panel);
    jframe.pack();
    jframe.setVisible(true);
  }

  public String toString() { return ": slave display with Java2D"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test64(args);
  }
}
