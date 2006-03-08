/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.image.BufferedImage;

import java.rmi.RemoteException;

import visad.*;

import visad.bom.ImageRendererJ3D;

import visad.java3d.DisplayImplJ3D;

public class Test73
  extends TestSkeleton
{
  private String fileName;

  public Test73() { }

  public Test73(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

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

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = new DisplayImplJ3D("display", DisplayImplJ3D.APPLETFRAME);
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    // load image from disk
    BufferedImage image = null;
    try {
      image = javax.imageio.ImageIO.read(new java.io.File(fileName));
    }
    catch (java.io.IOException exc) {
      exc.printStackTrace();
      return;
    }

    // convert image to more efficient representation (optional)
    image = ImageFlatField.make3ByteRGB(image);

    // convert image to VisAD object
    ImageFlatField ff = new ImageFlatField(image);

    // create display mappings
    RealType[] xy = ff.getDomainTypes();
    RealType[] v = ff.getRangeTypes();
    dpys[0].addMap(new ScalarMap(xy[0], Display.XAxis));
    dpys[0].addMap(new ScalarMap(xy[1], Display.YAxis));
    if (v.length == 3) {
      dpys[0].addMap(new ScalarMap(v[0], Display.Red));
      dpys[0].addMap(new ScalarMap(v[1], Display.Green));
      dpys[0].addMap(new ScalarMap(v[2], Display.Blue));
    }
    else {
      for (int i=0; i<v.length; i++) {
        dpys[0].addMap(new ScalarMap(v[i], Display.RGB));
      }
    }

    // configure display
    GraphicsModeControl gmc = dpys[0].getGraphicsModeControl();
    gmc.setTextureEnable(true);
    gmc.setScaleEnable(true);
    DataReferenceImpl ref = new DataReferenceImpl("ref");
    ref.setData(ff);
    ConstantMap zmap = new ConstantMap(0.7, Display.ZAxis);
    dpys[0].addReferences(new ImageRendererJ3D(),
      ref, new ConstantMap[] {zmap});
  }

  String getFrameTitle() { return "ImageFlatField with ImageRendererJ3D"; }

  public String toString() {
    return " file_name: ImageFlatField with ImageRendererJ3D";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test73(args);
  }
}
