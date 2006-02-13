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

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.util.ContourWidget;
import visad.util.LabeledRGBAWidget;

public class Test37
  extends TestSkeleton
{
  private boolean reverse;
  ScalarMap rgbaMap;

  public Test37() { }

  public Test37(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { reverse = false; }

  public int checkKeyword(String testName, int argc, String[] args)
  {
    reverse = true;
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
    RealType[] types = {RealType.Latitude, RealType.Longitude, RealType.Altitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);
    RealType[] typesxx = {RealType.Longitude, RealType.Latitude};
    RealTupleType earth_locationxx = new RealTupleType(typesxx);
    FunctionType image_tuplexx = new FunctionType(earth_locationxx, radiance);

    int size = 64;
    FlatField imaget1;
    if (!reverse) {
      imaget1 = FlatField.makeField(image_tuple, size, false);
    }
    else {
      imaget1 = FlatField.makeField(image_tuplexx, size, false);
    }

    double first = 0.0;
    double last = size - 1.0;
    double step = 1.0;
    double half = 0.5 * last;

    int nr = size;
    int nc = size;
    double ang = 2*Math.PI/nr;
    float[][] locs = new float[3][nr*nc];
    for ( int jj = 0; jj < nc; jj++ ) {
      for ( int ii = 0; ii < nr; ii++ ) {
        int idx = jj*nr + ii;
        locs[0][idx] = ii;
        locs[1][idx] = jj;
        locs[2][idx] =
           2f*((float)Math.sin(2*ang*ii)) + 2f*((float)Math.sin(2*ang*jj));
      }
    }
    Gridded3DSet d_set =
      new Gridded3DSet(RealTupleType.SpatialCartesian3DTuple, locs, nr, nc);
    imaget1 = new FlatField(image_tuple, d_set);
    FlatField.fillField(imaget1, step, half);


    ScalarMap xmap = new ScalarMap(RealType.Longitude, Display.XAxis);
    dpys[0].addMap(xmap);
    ScalarMap ymap = new ScalarMap(RealType.Latitude, Display.YAxis);
    dpys[0].addMap(ymap);
    ScalarMap zmap = new ScalarMap(RealType.Altitude, Display.ZAxis);
    dpys[0].addMap(zmap);
    rgbaMap = new ScalarMap(vis_radiance, Display.RGBA);
    dpys[0].addMap(rgbaMap);
    zmap.setRange(-20, 20);
   
    ScalarMap map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
    dpys[0].addMap(map1contour);
    ContourControl ctr_cntrl = (ContourControl) map1contour.getControl();

    GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
    mode.setScaleEnable(true);
    mode.setPointSize(2);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);
  }

  private String getFrameTitle0() { return "regular contours in Java3D"; }

  private String getFrameTitle1() { return "VisAD contour controls"; }

  void setupUI(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    JFrame jframe  = new JFrame(getFrameTitle0() + getClientServerTitle());
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe.setContentPane((JPanel) dpys[0].getComponent());
    jframe.pack();
    jframe.setVisible(true);

    ScalarMap map1contour = (ScalarMap )dpys[0].getMapVector().lastElement();
    ContourWidget cw = new ContourWidget(map1contour);

    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BorderLayout());
    big_panel.add("Center", cw);
  

    JFrame jframe2  = new JFrame(getFrameTitle1());
    jframe2.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe2.setContentPane(big_panel);
    jframe2.pack();
    jframe2.setVisible(true);

    LabeledRGBAWidget caw = new LabeledRGBAWidget(rgbaMap);
    JPanel big_panel2 = new JPanel();
    big_panel2.setLayout(new BorderLayout());
    big_panel2.add("Center", caw);


    JFrame jframe3  = new JFrame(getFrameTitle1());
    jframe3.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    jframe3.setContentPane(big_panel2);
    jframe3.pack();
    jframe3.setVisible(true);

  }

  public String toString()
  {
    return ": colored contours from regular grids and ContourWidget in Java3D";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test37(args);
  }
}
