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
import visad.java3d.*;
import visad.java3d.DisplayImplJ3D;

import javax.media.j3d.*;
import java.io.IOException;

// JFC packages
import javax.swing.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;


public class TestIDesk extends Object {

  private static int tracker_shmkey, controller_shmkey;

  public static void main(String args[])
         throws IOException, VisADException, RemoteException {

    if (args.length != 2) {
      System.err.println("must have 2 integer arguments");
      System.exit(1);
    }

    try {
      tracker_shmkey = Integer.parseInt(args[0]);
      controller_shmkey = Integer.parseInt(args[1]);
    }
    catch (NumberFormatException exc) {
      System.err.println("args must be integers " + args[0] + " " + args[1]);
      System.exit(1);
    }

    // set up display
    GraphicsEnvironment ge =
      GraphicsEnvironment.getLocalGraphicsEnvironment();

    GraphicsDevice gd = ge.getDefaultScreenDevice();

    GraphicsConfigTemplate3D gct3d = new GraphicsConfigTemplate3D();
    gct3d.setStereo(GraphicsConfigTemplate3D.REQUIRED);

    GraphicsConfiguration config =
    gct3d.getBestConfiguration(gd.getConfigurations());

    if (config == null) {
      System.err.println("Unable to find a Stereo visual");
      System.exit(1);
    }

    ImmersaDeskDisplayRendererJ3D display_renderer =
      new ImmersaDeskDisplayRendererJ3D(tracker_shmkey, controller_shmkey);
    DisplayImplJ3D display =
      new DisplayImplJ3D("display1", display_renderer, config);

    // set up data
    final RealType ir_radiance = RealType.getRealType("ir_radiance");
    final RealType count = RealType.getRealType("count", CommonUnit.second);
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);
    final RealType vis_radiance = RealType.getRealType("vis_radiance");

    int size = 64;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);
    Real direct = new Real(ir_radiance, 2.0);
    Real[] reals3 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                     new Real(vis_radiance, 1.0)};
    RealTuple direct_tuple = new RealTuple(reals3);

    // link data to display
    display.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    display.addMap(new ScalarMap(ir_radiance, Display.XAxis));
    display.addMap(new ScalarMap(count, Display.YAxis));
    display.addMap(new ScalarMap(count, Display.Green));

    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setPointSize(5.0f);
    mode.setPointMode(false);

    DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
    ref_direct.setData(direct);
    DataReference[] refs1 = {ref_direct};
    display.addReferences(new DirectManipulationRendererJ3D(), refs1, null);

    DataReferenceImpl ref_direct_tuple =
      new DataReferenceImpl("ref_direct_tuple");
    ref_direct_tuple.setData(direct_tuple);
    DataReference[] refs2 = {ref_direct_tuple};
    display.addReferences(new DirectManipulationRendererJ3D(), refs2, null);

    DataReferenceImpl ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    DataReference[] refs3 = {ref_histogram1};
    display.addReferences(new DirectManipulationRendererJ3D(), refs3, null);

    // create JFrame and JPanel for display
    JFrame frame = new JFrame("VisAD ImmersaDesk Test");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in frame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    panel.add(display.getComponent());

    int WIDTH = 1280;
    int HEIGHT = 1024;

    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);
    frame.setVisible(true);
  }

}

