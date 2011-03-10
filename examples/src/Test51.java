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

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Graphics;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.image.BufferedImage;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.*;

public class Test51
  extends TestSkeleton
{
  public Test51() { }

  public Test51(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
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
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.RGB));
    dpys[0].addMap(new ScalarMap(vis_radiance, Display.ZAxis));

    GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
    // mode.setTextureEnable(false);
    mode.setTextureEnable(true);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);
  }

  String getFrameTitle() { return "capture image in Java3D"; }

  void setupUI(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    JFrame jframe  = new JFrame(getFrameTitle() + getClientServerTitle());
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe.setContentPane((JPanel) dpys[0].getComponent());
    jframe.pack();
    jframe.setVisible(true);

    JFrame jframe1 = new JFrame("captured image from Java3D");
    jframe1.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    JPanel panel1 = new JPanel();
    panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
    panel1.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel1.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    jframe1.setContentPane(panel1);
    jframe1.pack();
    jframe1.setSize(jframe.getSize().width, jframe.getSize().height);
    jframe1.setVisible(true);

    while (true) {
/*
VisADCanvasJ3D canvas =
  ((DisplayRendererJ3D) dpys[0].getDisplayRenderer()).getCanvas();
Screen3D scr = canvas.getScreen3D();
System.out.println(scr.getPhysicalScreenHeight() + " " +
                   scr.getPhysicalScreenWidth() + " " +
                   scr.getSize().getHeight() + " " +
                   scr.getSize().getWidth());
// prints 0.28899555555555556 0.3612444444444445 1024.0 1280.0
*/
      Graphics gp = panel1.getGraphics();
      BufferedImage image = dpys[0].getImage();
      gp.drawImage(image, 0, 0, panel1);
      gp.dispose();
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
      }
    }
  }

  public String toString() { return ": image capture in Java3D"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test51(args);
  }
}
