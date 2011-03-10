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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.media.j3d.*;

import visad.*;
import visad.data.mcidas.*;
import visad.java3d.*;
import visad.bom.*;

public class IDeskMcIDAS implements DisplayListener {

  // run 'java IDeskMcIDAS AREA2001 OUTLSUPW tracker_key controller_key'
  public static void main (String[] args)
         throws VisADException, RemoteException, IOException {

    IDeskMcIDAS idesk = new IDeskMcIDAS(args);
  }

  public IDeskMcIDAS(String[] args)
         throws VisADException, RemoteException, IOException {
    int tracker_shmkey = 4148;
    int controller_shmkey = 4147;
    if (args.length >= 3) tracker_shmkey = Integer.parseInt(args[3]);
    if (args.length >= 4) controller_shmkey = Integer.parseInt(args[4]);

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

    // read McIDAS AREA file
    AreaAdapter areaAdapter = new AreaAdapter(args[0]);
    Data image = areaAdapter.getData();

    // get type of image radiance
    FunctionType imageFunctionType = (FunctionType) image.getType();
    RealType radianceType = (RealType)
      ((RealTupleType) imageFunctionType.getRange()).getComponent(0);

    // define display coordinates
    display.addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
    display.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
    ScalarMap rgbMap = new ScalarMap(radianceType, Display.RGB);
    display.addMap(rgbMap);

    // read McIDAS map file
    BaseMapAdapter baseMapAdapter = new BaseMapAdapter(args[1]);
    Data map = baseMapAdapter.getData();

    // link map to display
    DataReference maplinesRef = new DataReferenceImpl("MapLines");
    maplinesRef.setData(map);
    ConstantMap[] maplinesConstantMap = new ConstantMap[]
      {new ConstantMap(1.002, Display.Radius),
       new ConstantMap(0.0, Display.Blue)};
    display.addReference(maplinesRef, maplinesConstantMap);

    // link image to display
    DataReferenceImpl imageRef = new DataReferenceImpl("ImageRef");
    imageRef.setData(image);
    display.addReferences(new ImageRendererJ3D(), imageRef);

    display.addDisplayListener(this);
    rotate_x(display);

    // create frame (window) on screen
    JFrame frame = new JFrame("Satellite Display");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    // add 3-D display to panel in frame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(display.getComponent());

    // finish off frame
    frame.getContentPane().add(panel);
    int WIDTH = 1280;
    int HEIGHT = 1024;

    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);
    frame.setVisible(true);
  }

  public void displayChanged(DisplayEvent e)
    throws RemoteException, VisADException {
    if (e.getId() == DisplayEvent.FRAME_DONE) {
      rotate((LocalDisplay) e.getDisplay());
    }
  }

  public void rotate(LocalDisplay display)
    throws RemoteException, VisADException {
    ProjectionControl control = display.getProjectionControl();
    double[] matrix = control.getMatrix();
    double[] mult = display.make_matrix(0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0);
    control.setMatrix(display.multiply_matrix(mult, matrix));
  }

  public void rotate_x(LocalDisplay display)
    throws RemoteException, VisADException {
    ProjectionControl control = display.getProjectionControl();
    double[] matrix = control.getMatrix();
    double[] mult = display.make_matrix(90.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);
    control.setMatrix(display.multiply_matrix(mult, matrix));
  }

}

