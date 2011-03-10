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

import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;
import visad.bom.ImageRendererJ3D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.util.CursorUtil;

public class Test73 extends TestSkeleton implements DisplayListener {
  private String fileName;
  private boolean norm;
  private ImageFlatField ff;

  public Test73() { }

  public Test73(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { fileName = null; norm = false; }

  public int checkOption(String progName, char ch, String arg) {
    if (ch == 'n') {
      norm = true;
      return 1;
    }

    return 0;
  }


  public int checkKeyword(String testName, int argc, String[] args) {
    if (fileName == null) fileName = args[argc];
    else {
      System.err.println(testName +
        ": Ignoring extra filename \"" + args[argc] + "\"");
    }

    return 1;
  }

  public String keywordUsage() {
    return super.keywordUsage() + " [-n(ormalize)] file";
  }

  public boolean finalizeArgs(String mainName) {
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
    dpys[0] = new DisplayImplJ3D("display",
      new TwoDDisplayRendererJ3D(), DisplayImplJ3D.APPLETFRAME);
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
    if (norm) image = ImageFlatField.make3ByteRGB(image);

    // convert image to VisAD object
    ff = new ImageFlatField(image);

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
    dpys[0].addReferences(new ImageRendererJ3D(), ref, null);
    dpys[0].addDisplayListener(this);
  }

  public void displayChanged(DisplayEvent e) {
    int id = e.getId();
    if (id == DisplayEvent.FRAME_DONE) {
      // check for active cursor
      Display display = e.getDisplay();
      if (!(display instanceof DisplayImpl)) return;
      DisplayImpl d = (DisplayImpl) display;
      DisplayRenderer dr = d.getDisplayRenderer();
      Vector cursorStringVector = dr.getCursorStringVector();
      if (cursorStringVector == null || cursorStringVector.size() == 0) return;

      // get cursor value
      double[] cur = dr.getCursor();
      if (cur == null || cur.length == 0 || cur[0] != cur[0]) return;

      // get range values at the given cursor location
      double[] domain = CursorUtil.cursorToDomain(d, cur);
      double[] range = null;
      try {
        range = CursorUtil.evaluate(ff, domain);
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }

      System.out.print("Cursor =");
      for (int i=0; i<2; i++) System.out.print(" " + domain[i]);
      System.out.print(" ->");
      if (range == null) System.out.println(" null");
      else {
        for (int i=0; i<range.length; i++) System.out.print(" " + range[i]);
        System.out.println();
      }
    }
  }

  String getFrameTitle() { return "ImageFlatField with ImageRendererJ3D"; }

  public String toString() {
    return " [-n(ormalize)] file_name: ImageFlatField";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test73(args);
  }

}
