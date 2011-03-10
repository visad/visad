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

import java.awt.BorderLayout;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.util.ContourWidget;

public class Test05
  extends UISkeleton
{
  private boolean uneven;

  public Test05() { }

  public Test05(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { uneven = false; }

  public int checkKeyword(String testName, int argc, String[] args)
  {
    uneven = true;
    return 1;
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = new DisplayImplJ3D("display");
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

    int size = 64;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.Green));
    dpys[0].addMap(new ScalarMap(vis_radiance, Display.RGB));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.ZAxis));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap map1contour;
    map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
    dpys[0].addMap(map1contour);
    if (uneven) {
      ContourControl control = (ContourControl) map1contour.getControl();
      float[] levs = {10.0f, 12.0f, 14.0f, 16.0f, 24.0f, 32.0f, 40.0f};
      control.setLevels(levs, 15.0f, true);
      control.enableLabels(true);
    }

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);
  }

  private String getFrameTitle0() { return "regular contours in Java3D"; }

  private String getFrameTitle1() { return "VisAD contour controls"; }

  void setupUI(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    JFrame jframe  = new JFrame(getFrameTitle0() + getClientServerTitle());
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe.setContentPane((JPanel) dpys[0].getComponent());
    jframe.pack();
    jframe.setVisible(true);

    if (!uneven) {
      ScalarMap map1contour = (ScalarMap )dpys[0].getMapVector().lastElement();
      ContourWidget cw = new ContourWidget(map1contour);

      JPanel big_panel = new JPanel();
      big_panel.setLayout(new BorderLayout());
      big_panel.add("Center", cw);

      JFrame jframe2  = new JFrame(getFrameTitle1());
      jframe2.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
      });

      jframe2.setContentPane(big_panel);
      jframe2.pack();
      jframe2.setVisible(true);
    }
  }

  public String toString()
  {
    return " uneven: colored 2-D contours from regular grids and ContourWidget";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test05(args);
  }
}
