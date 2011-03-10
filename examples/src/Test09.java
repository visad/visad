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

import visad.*;

import visad.data.gif.GIFForm;

import visad.java2d.DisplayImplJ2D;

public class Test09
  extends UISkeleton
{
  private String fileName;

  public Test09() { }

  public Test09(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { fileName = null; }

  public int checkKeyword(String testName, int argc, String[] args)
  {
    if (fileName == null) {
      fileName = args[argc];
    } else {
      System.err.println(testName + ": Ignoring extra filename \"" +
                         args[argc] + "\"");
    }

    return 1;
  }

  public String keywordUsage()
  {
    return super.keywordUsage() + " file";
  }

  public boolean finalizeArgs(String mainName)
  {
    if (fileName == null) {
      System.err.println(mainName + ": No filename specified!");
      return false;
    }

    return true;
  }

  private DataReferenceImpl loadFile()
    throws RemoteException, VisADException
  {
    GIFForm gif_form = new GIFForm();
    FlatField img = (FlatField) gif_form.open(fileName);

    DataReferenceImpl ref = new DataReferenceImpl("image");
    ref.setData(img);

    return ref;
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
    DataReference ref = loadFile();
    if (ref == null) {
      System.err.println("must specify GIF or JPEG file name");
      System.exit(1);
      return;
    }

    FlatField img = (FlatField )ref.getData();

    // compute ScalarMaps from type components
    FunctionType ftype = (FunctionType) img.getType();
    RealTupleType dtype = ftype.getDomain();
    RealTupleType rtype9 = (RealTupleType) ftype.getRange();
    dpys[0].addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                  Display.XAxis));
    dpys[0].addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                  Display.YAxis));
    dpys[0].addMap(new ScalarMap((RealType) rtype9.getComponent(0),
                                   Display.Red));
    dpys[0].addMap(new ScalarMap((RealType) rtype9.getComponent(1),
                                   Display.Green));
    dpys[0].addMap(new ScalarMap((RealType) rtype9.getComponent(2),
                                   Display.Blue));

    dpys[0].addReference(ref, null);
  }

  String getFrameTitle() { return "GIF / JPEG in Java2D"; }

  public String toString()
  {
    return " file_name: GIF / JPEG reader using Java2D";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test09(args);
  }
}
