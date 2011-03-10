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

import visad.data.DefaultFamily;

import visad.java3d.DisplayImplJ3D;

public class Test71
  extends UISkeleton
{
  class SwitchGIFs
    implements ActivityHandler
  {
    SwitchGIFs(LocalDisplay dpy) { toggleDisplay(dpy, true); }

    public void busyDisplay(LocalDisplay dpy) { toggleDisplay(dpy, false); }

    public void idleDisplay(LocalDisplay dpy) { toggleDisplay(dpy, true); }

    private void toggleDisplay(LocalDisplay dpy, boolean showFirstGIF)
    {
      java.util.Vector v = dpy.getRenderers();

      final int size = v.size();
      if (size != 2) {
        System.err.println("Expected 2 DataRenderers, but Display has " +
                           size);
        return;
      }

      ((DataRenderer )v.get(0)).toggle(showFirstGIF);
      ((DataRenderer )v.get(1)).toggle(!showFirstGIF);
    }
  }

  private String file1;
  private String file2;

  public Test71() { }

  public Test71(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { file1 = file2 = null; }

  public int checkKeyword(String testName, int argc, String[] args)
  {
    if (file1 == null) {
      file1 = args[argc];
    } else if (file2 == null) {
      file2 = args[argc];
    } else {
      System.err.println(testName + ": Ignoring extra filename \"" +
                         args[argc] + "\"");
    }

    return 1;
  }

  public String keywordUsage()
  {
    return super.keywordUsage() + " file1 file2";
  }

  public boolean finalizeArgs(String progName)
  {
    if (file1 == null) {
      System.err.println(progName + ": Please specify two files");
      return false;
    }

    if (file2 == null) {
      System.err.println(progName + ": Please specify both files");
      return false;
    }

    return true;
  }

  private DataReferenceImpl loadFile(DefaultFamily df, String fileName,
                                     String refName)
    throws RemoteException, VisADException
  {
    if (fileName == null) {
      return null;
    }

    Data data = (FlatField )df.open(fileName);

    DataReferenceImpl ref = new DataReferenceImpl(refName);
    ref.setData(data);

    return ref;
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
    DefaultFamily df = new DefaultFamily("loader");

    DataReference ref1 = loadFile(df, file1, "img1");
    if (ref1 == null) {
      System.err.println("\"" + file1 + "\" is not a valid file");
      System.exit(1);
      return;
    }

    DataReference ref2 = loadFile(df, file2, "img2");
    if (ref2 == null) {
      System.err.println("\"" + file2 + "\" is not a valid file");
      System.exit(1);
      return;
    }

    FlatField img1 = (FlatField )ref1.getData();
    FlatField img2 = (FlatField )ref2.getData();

/*
    if (!img1.getType().equals(img2.getType())) {
      System.err.println("Incompatible file types:");
      System.err.println("  " + file1 + ": " + img1.getType());
      System.err.println("  " + file2 + ": " + img2.getType());
      System.exit(1);
      return;
    }
*/

    // compute ScalarMaps from type components
    FunctionType ftype = (FunctionType) img1.getType();
    RealTupleType dtype = ftype.getDomain();
    RealTupleType rtype = (RealTupleType) ftype.getRange();

    /* map domain elements to spatial axes */
    final int dLen = dtype.getDimension();
    for (int i = 0; i < dLen; i++) {
      ScalarType scalT;
      DisplayRealType dpyRT;

      switch (i) {
      case 0: dpyRT = Display.XAxis; break;
      case 1: dpyRT = Display.YAxis; break;
      case 2: dpyRT = Display.ZAxis; break;
      default: dpyRT = null; break;
      }

      if (dpyRT != null) {
        dpys[0].addMap(new ScalarMap((RealType )dtype.getComponent(i), dpyRT));
      }
    }

    /* map range elements to colors */
    final int rLen = rtype.getDimension();
    for (int i = 0; i < rLen; i++) {
      ScalarType scalT;
      DisplayRealType dpyRT;

      switch (i) {
      case 0: dpyRT = Display.Red; break;
      case 1: dpyRT = Display.Green; break;
      case 2: dpyRT = Display.Blue; break;
      default: dpyRT = null; break;
      }

      if (dpyRT != null) {
        dpys[0].addMap(new ScalarMap((RealType )rtype.getComponent(i), dpyRT));
      }
    }

    dpys[0].addReference(ref1, null);
    dpys[0].addReference(ref2, null);

    dpys[0].addActivityHandler(new SwitchGIFs(dpys[0]));
  }

  String getFrameTitle() { return "Idle/Busy test"; }

  public String toString()
  {
    return " gif_file gif_file: Idle/Busy test";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test71(args);
  }
}
