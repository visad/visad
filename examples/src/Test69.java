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
import java.awt.*;

import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.util.HersheyFont;

public class Test69
  extends UISkeleton
{
  private boolean sphere;
  private String hfont;

  public Test69() { }

  public Test69(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { 
    sphere = false;
    hfont = null;
  }

 public int checkKeyword(String testName, int argc, String[] args)
  {
    if ((args[argc].length() >= 3 && "sphere".startsWith(args[argc])) ||
         "1".equals(args[argc])) {
      sphere = true;
      return 1;
    }

    hfont = args[argc];
    return 1;
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = new DisplayImplJ3D("display");
    // dpys[0] = new DisplayImplJ2D("display");
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
                                   "0123456789  - + = / [ ] ( ) { }",
                                   "á é í ó ú ñ Á É Í Ó Ú Ñ"};
    if (sphere) {
      names = new String[] {"", "", "a b c d e f g h i j k l m n o p q " +
                            "r s t u v w x y z A B C D E F G H I J K L " +
                            "M N O P Q R S T U V W X Y Z 0 1 2 3 4 5 6 " +
                            "7 8 9   T H I S   I S N ' T   Y O U R   G " +
                            "R A N D F A T H E R ' S   M C I D A S", "", ""};
    }
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
    TextControl tcontrol = (TextControl) tmap.getControl();
    if (hfont == null) {
      Font font = new Font("Serif", Font.PLAIN, 60);
      tcontrol.setFont(font);
    } else {
      HersheyFont font = new HersheyFont(hfont);
      tcontrol.setFont(font);
    }
    tcontrol.setSphere(sphere);
    tcontrol.setCenter(true);
    tcontrol.setSize(2.0);
    if (sphere) {
      tcontrol.setRotation(10.0);
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

  String getFrameTitle() { return "text with font in Java3D"; }

  public String toString() { return " [sphere  <HersheyFontName>]: text with font in Java3D"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test69(args);
  }
}
