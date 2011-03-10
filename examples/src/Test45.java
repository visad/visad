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

// AWT packages
import java.awt.*;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.util.TextControlWidget;

public class Test45
  extends UISkeleton
{
  private boolean sphere;
  private TextControl tcontrol;

  public Test45() { }

  public Test45(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { sphere = false; }

  public int checkKeyword(String testName, int argc, String[] args)
  {
    sphere = true;
    return 1;
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];
    // SL 16 July 2003
    //    dpys[0] = new DisplayImplJ3D("display");
    dpys[0] = new DisplayImplJ3D("display", DisplayImplJ3D.APPLETFRAME);

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

    String[] names = new String[] {"a b c d e f g h i j k l m",
                                   "nopqrstuvwxyz",
                                   "A B C D E F G H I J K L M",
                                   "NOPQRSTUVWXYZ",
                                   "0123456789  - + = / [ ] ( ) { }"};

    int ntimes1 = names.length;
    Set time_set =
      new Linear1DSet(time_type, 0.0, (double) (ntimes1 - 1.0), ntimes1);

    FieldImpl text_field = new FieldImpl(text_function, time_set);

    for (int i=0; i<ntimes1; i++) {
      Data[] td = {new Real(RealType.Latitude, 30.0 * i - 60.0),
                   new Real(RealType.Longitude, 60.0 * (ntimes1 - i) - 120.0),
                   new Text(text, names[i])};

      Tuple tt = new Tuple(text_tuple, td);
      text_field.setSample(i, tt);
    }

    ScalarMap tmap = new ScalarMap(text, Display.Text);
    dpys[0].addMap(tmap);
    tcontrol = (TextControl) tmap.getControl();
    tcontrol.setSphere(sphere);
    tcontrol.setCenter(true);
    if (sphere) {
      dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
      dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
    }
    else {
      dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
      dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
      tcontrol.setAutoSize(true);
    }
    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.Green));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));

    DataReferenceImpl ref_text_field =
      new DataReferenceImpl("ref_text_field");
    ref_text_field.setData(text_field);
    dpys[0].addReference(ref_text_field, null);
  }

  // SL 16 July 2003
  Component getSpecialComponent(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(new TextControlWidget(tcontrol));
    return panel;
  }

  String getFrameTitle() {
    return "text in Java3D with interactive settings";
  }

  public String toString() {
    return " sphere: text in Java3D with interactive settings";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test45(args);
  }
}
