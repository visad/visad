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

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;

public class Test58
  extends UISkeleton
  implements DisplayListener
{

  ProjectionControl control;

  public Test58() { }

  public Test58(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
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
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.Green));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));

    GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
    mode.setScaleEnable(true);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);
  }

  String getFrameTitle() { return "fly-through in Java2D"; }

  void setupUI(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    JFrame jframe  = new JFrame(getFrameTitle() + getClientServerTitle());
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe.setContentPane((JPanel) dpys[0].getComponent());
    jframe.pack();
    jframe.setVisible(true);

    if (!isClient()) {
      /* change aspect ratio */
      control = dpys[0].getProjectionControl();
      // control.setAspect(new double[] {1.0, 0.5});
      control.setAspectCartesian(new double[] {1.0, 0.5});

      dpys[0].addDisplayListener(this);
      rotate(dpys[0]);
    }
  }

  public void displayChanged(DisplayEvent e)
    throws RemoteException, VisADException {
    if (e.getId() == DisplayEvent.FRAME_DONE) {
      rotate((LocalDisplay) e.getDisplay());
    }
  }

  public void rotate(LocalDisplay display)
    throws RemoteException, VisADException {
    double[] matrix = control.getMatrix();
    double[] mult = display.make_matrix(0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0);
    control.setMatrix(display.multiply_matrix(mult, matrix));
  }

  public String toString()
  {
    return ": scripted fly-through & aspect ratio in Java2D";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test58(args);
  }
}
