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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

import java.rmi.RemoteException;

import java.util.Enumeration;

import visad.*;

import visad.java2d.DisplayImplJ2D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.util.TextControlWidget;

public class Test44
  extends UISkeleton
{
  public Test44() { }

  public Test44(String[] args)
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
    TextType text = new TextType("text");
    RealType[] time = {RealType.Time};
    RealTupleType time_type = new RealTupleType(time);
    MathType[] mtypes = {RealType.Latitude, RealType.Longitude, text};
    TupleType text_tuple = new TupleType(mtypes);
    FunctionType text_function = new FunctionType(RealType.Time, text_tuple);

    String[] names = {"aaa", "bbbb", "ccccc", "defghi"};
    int ntimes1 = names.length;
    Set time_set =
      new Linear1DSet(time_type, 0.0, (double) (ntimes1 - 1.0), ntimes1);

    FieldImpl text_field = new FieldImpl(text_function, time_set);

    for (int i=0; i<ntimes1; i++) {
      Data[] td = {new Real(RealType.Latitude, (double) i),
                   new Real(RealType.Longitude, (double) (ntimes1 - i)),
                   new Text(text, names[i])};

      Tuple tt = new Tuple(text_tuple, td);
      text_field.setSample(i, tt);
    }

    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.Green));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap text_map = new ScalarMap(text, Display.Text);
    dpys[0].addMap(text_map);

    DataReferenceImpl ref_text_field =
      new DataReferenceImpl("ref_text_field");
    ref_text_field.setData(text_field);
    dpys[0].addReference(ref_text_field, null);
  }

  String getFrameTitle() {
    return "text in Java2D with interactive settings";
  }

  Component getSpecialComponent(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    boolean foundCtrl = false;
   TextControl text_control = null;
    Enumeration en = dpys[0].getMapVector().elements();
    while (en.hasMoreElements()) {
      ScalarMap sm = (ScalarMap )en.nextElement();

      Control ctrl = sm.getControl();
      if (ctrl != null && ctrl instanceof TextControl) {
        text_control = (TextControl) ctrl;
        // text_control.setSize(0.75);
        // text_control.setJustification(TextControl.Justification.RIGHT);
        // text_control.setRotation(10.0);
        // text_control.setAutoSize(true);
        foundCtrl = true;
      }
    }

    if (!foundCtrl) {
      System.err.println("Didn't find a TextControl for this display!");
      System.err.println("Don't be surprised if things don't work...");
    }

    // SL 16 July 2003
    //    return null;
    JFrame jframe = new JFrame("VisAD font Selection Widget");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    jframe.setContentPane(new TextControlWidget(text_control));
    jframe.pack();
    jframe.setVisible(true);

    return null;
  }

  public String toString() {
    return ": text in Java2D with interactive settings";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test44(args);
  }
}
