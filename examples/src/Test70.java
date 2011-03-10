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

import java.rmi.RemoteException;
import java.text.*;
import java.awt.Font;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test70
  extends UISkeleton
{
  private boolean sphere;

  public Test70() { }

  public Test70(String[] args)
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
    dpys[0] = new DisplayImplJ3D("display");
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    RealType text = RealType.getRealType("text");
    RealType[] time = {RealType.Time};
    RealTupleType time_type = new RealTupleType(time);
    RealType[] mtypes = {RealType.Latitude, RealType.Longitude, text};
    RealTupleType text_tuple = new RealTupleType(mtypes);
    FunctionType text_function = new FunctionType(RealType.Time, text_tuple);

    // double[] values = {1.23, 0.001, -23.4, 0.0, 10};
    double[] values = {1.23, 0.001, Double.NaN, 0.0, 10};

    int ntimes1 = values.length;
    Set time_set =
      new Linear1DSet(time_type, 0.0, (double) (ntimes1 - 1.0), ntimes1);

    FlatField text_field = new FlatField(text_function, time_set);

    for (int i=0; i<ntimes1; i++) {
      Real[] td = {new Real(RealType.Latitude, 30.0 * i - 60.0),
                   new Real(RealType.Longitude, 60.0 * (ntimes1 - i) - 120.0),
                   new Real(text, values[i])};

      RealTuple tt = new RealTuple(td);
      text_field.setSample(i, tt);
    }

    ScalarMap tmap = new ScalarMap(text, Display.Text);
    dpys[0].addMap(tmap);
    TextControl tcontrol = (TextControl) tmap.getControl();
    tcontrol.setSphere(sphere);
    tcontrol.setCenter(true);
    tcontrol.setNumberFormat(new DecimalFormat());
    Font txtfont = new Font("Arial", Font.PLAIN, 4);
    tcontrol.setFont(txtfont);
    // tcontrol.setRotation(10.0);
    if (sphere) {
      dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
      dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
    }
    else {
      dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
      dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    }
    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.Green));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));

    DataReferenceImpl ref_text_field =
      new DataReferenceImpl("ref_text_field");
    ref_text_field.setData(text_field);
    dpys[0].addReference(ref_text_field, null);
  }

  String getFrameTitle() { return "text from RealType in Java3D"; }

  public String toString() { return " sphere: text from RealType in Java3D"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test70(args);
  }
}
