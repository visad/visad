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

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import visad.*;

import java.awt.Dimension;
import visad.java2d.DisplayRendererJ2D;
import visad.util.LabeledColorWidget;
import visad.java2d.DisplayImplJ2D;

public class Test39
  extends TestSkeleton
{
  public Test39() { }

  public Test39(String[] args)
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

    ScalarMap color1map = new ScalarMap(vis_radiance, Display.RGB);
    dpys[0].addMap(color1map);

    DisplayRendererJ2D dr = (DisplayRendererJ2D )dpys[0].getDisplayRenderer();
    dr.getCanvas().setPreferredSize(new Dimension(256, 256));

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);
  }

  String getFrameTitle() { return "VisAD Color Widget in Java2D"; }

  void setupUI(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    JFrame jframe  = new JFrame(getFrameTitle() + getClientServerTitle());
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    JPanel lil_panel = new JPanel();
    lil_panel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
    lil_panel.setLayout(new BorderLayout());
    lil_panel.add("Center", dpys[0].getComponent());

    ScalarMap color1map = (ScalarMap )dpys[0].getMapVector().lastElement();
    LabeledColorWidget lw = new LabeledColorWidget(color1map);

    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.Y_AXIS));
    big_panel.add(lw);
    big_panel.add(lil_panel);

    jframe.setContentPane(big_panel);
    jframe.setSize(400, 600);
    jframe.setVisible(true);
  }

  public String toString()
  {
    return ": color array and ColorWidget in Java2D";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test39(args);
  }
}
