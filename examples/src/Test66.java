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

import java.awt.Component;
import java.awt.Container;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import visad.ConstantMap;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.LocalDisplay;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.VisADException;

import visad.java3d.DisplayImplJ3D;

//import visad.util.AnimationWidget;
//import visad.util.ColorWidget;
import visad.util.ContourWidget;
import visad.util.GMCWidget;
import visad.util.LabeledColorWidget;
import visad.util.ProjWidget;
import visad.util.RangeWidget;
import visad.util.SelectRangeWidget;

public class Test66
  extends TestSkeleton
{
  public Test66() { }

  public Test66(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys;
    dpys = new DisplayImpl[2];
    dpys[0] = new DisplayImplJ3D("D0");
    dpys[1] = new DisplayImplJ3D("D1");
    return dpys;
  }

  private void setupDisplayZero(LocalDisplay dpy,
                                RealType visRadiance, RealType irRadiance)
    throws RemoteException, VisADException
  {
    dpy.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpy.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));

    ScalarMap vz = new ScalarMap(visRadiance, Display.ZAxis);
    dpy.addMap(vz);
    vz.setUnderscoreToBlank(true);

    dpy.addMap(new ConstantMap(0.5, Display.Red));
    dpy.addMap(new ScalarMap(visRadiance, Display.Green));
    dpy.addMap(new ConstantMap(0.5, Display.Blue));

    ScalarMap isr = new ScalarMap(irRadiance, Display.SelectRange);
    dpy.addMap(isr);
    isr.setUnderscoreToBlank(true);
    ScalarMap irgb = new ScalarMap(irRadiance, Display.RGBA);
    dpy.addMap(irgb);
    irgb.setUnderscoreToBlank(true);

    dpy.getGraphicsModeControl().setScaleEnable(true);
  }

  private void setupDisplayOne(LocalDisplay dpy,
                                RealType visRadiance, RealType irRadiance)
    throws RemoteException, VisADException
  {
    dpy.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpy.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));

    dpy.addMap(new ConstantMap(0.5, Display.Red));
    dpy.addMap(new ScalarMap(irRadiance, Display.Green));
    dpy.addMap(new ConstantMap(0.5, Display.Blue));

    ScalarMap vic = new ScalarMap(visRadiance, Display.IsoContour);
    dpy.addMap(vic);
    vic.setUnderscoreToBlank(true);
    ScalarMap irgb = new ScalarMap(irRadiance, Display.RGB);
    dpy.addMap(irgb);
    irgb.setUnderscoreToBlank(true);

    dpy.getGraphicsModeControl().setScaleEnable(true);
  }

  private void addData(LocalDisplay[] dpys,
                       RealType visRadiance, RealType irRadiance)
    throws RemoteException, VisADException
  {
    RealType[] llTypes = {RealType.Latitude, RealType.Longitude};
    RealTupleType earthLoc = new RealTupleType(llTypes);

    RealType[] radTypes = {visRadiance, irRadiance};
    RealTupleType radTuple = new RealTupleType(radTypes);

    FunctionType imageFunc = new FunctionType(earthLoc, radTuple);
    FlatField data = FlatField.makeField(imageFunc, 64, false);

    DataReferenceImpl dataRef = new DataReferenceImpl("data");
    dataRef.setData(data);

    for (int i = 0; i < dpys.length; i++) {
      dpys[i].addReference(dataRef);
    }
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    RealType visRadiance = RealType.getRealType("vis_radiance");
    RealType irRadiance = RealType.getRealType("ir_radiance");

    setupDisplayZero(dpys[0], visRadiance, irRadiance);
    setupDisplayOne(dpys[1], visRadiance, irRadiance);

    addData(dpys, visRadiance, irRadiance);
  }

  private void addWidget(Container cont, Component comp)
  {
    cont.add(new JLabel(comp.getClass().getName()));
    cont.add(comp);
  }

  private Component displayZeroUI(LocalDisplay dpy)
    throws RemoteException, VisADException
  {
    Vector v = dpy.getMapVector();
    int vSize = v.size();

    ScalarMap rgbaMap = (ScalarMap )v.elementAt(vSize - 1);
    ScalarMap selectMap = (ScalarMap )v.elementAt(vSize - 2);

    JPanel widgets = new JPanel();
    widgets.setLayout(new BoxLayout(widgets, BoxLayout.Y_AXIS));

    addWidget(widgets, new LabeledColorWidget(rgbaMap));
    addWidget(widgets, new SelectRangeWidget(selectMap));
    addWidget(widgets, new GMCWidget(dpy.getGraphicsModeControl()));
    addWidget(widgets, new ProjWidget(dpy.getProjectionControl()));

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    panel.add(widgets);
    panel.add(dpy.getComponent());

    return panel;
  }

  private Component displayOneUI(LocalDisplay dpy)
    throws RemoteException, VisADException
  {
    Vector v = dpy.getMapVector();
    int vSize = v.size();

    ScalarMap rgbMap = (ScalarMap )v.elementAt(vSize - 1);
    ScalarMap contourMap = (ScalarMap )v.elementAt(vSize - 2);

    JPanel widgets = new JPanel();
    widgets.setLayout(new BoxLayout(widgets, BoxLayout.Y_AXIS));

    addWidget(widgets, new RangeWidget(rgbMap));
    addWidget(widgets, new LabeledColorWidget(rgbMap));
    addWidget(widgets, new ContourWidget(contourMap));

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    panel.add(widgets);
    panel.add(dpy.getComponent());

    return panel;
  }

  private Container buildContent(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    panel.add(displayZeroUI(dpys[0]));
    panel.add(displayOneUI(dpys[1]));

    return panel;
  }

  String getFrameTitle() { return "Test all widgets"; }

  void setupUI(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    JFrame jframe  = new JFrame(getFrameTitle() + getClientServerTitle());
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe.setContentPane(buildContent(dpys));
    jframe.pack();
    jframe.setVisible(true);
  }

  public String toString() { return ": Test all widgets"; }

  /** main method for standalone testing */
  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test66(args);
  }
}
