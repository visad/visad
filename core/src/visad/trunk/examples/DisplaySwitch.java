/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import visad.*;

import visad.java2d.DisplayImplJ2D;

import visad.java3d.DisplayImplJ3D;

import visad.util.AnimationWidget;
import visad.util.ContourWidget;
import visad.util.GMCWidget;
import visad.util.LabeledColorWidget;
import visad.util.ProjWidget;
import visad.util.RangeWidget;
import visad.util.SelectRangeWidget;

public class DisplaySwitch
  extends Thread
{
  private DisplayImpl display = null;
  private DataReferenceImpl dpyRef = null;
  private JPanel dpyPanel = null;
  private JButton change = null;

  private boolean startServer = false;
  private String hostName = null;

  private static final int maximumWaitTime = 60;

  public DisplaySwitch(String[] args)
    throws RemoteException, VisADException
  {
    if (!processArgs(args)) {
      System.err.println("Exiting...");
      System.exit(1);
    }
    startThreads();
  }

  public boolean processArgs(String[] args)
  {
    boolean usage = false;

    for (int argc = 0; argc < args.length; argc++) {
      if (args[argc].startsWith("-") && args[argc].length() == 2) {
        if (argc >= args.length) {
          System.err.println("Missing argument for \"" + args[argc] + "\"\n");
          usage = true;
        } else {
          char ch = args[argc].charAt(1);

          String str, result;

          switch (ch) {
          case 'c':
            if (startServer) {
              System.err.println("Cannot specify both '-c' and '-s'!");
              usage = true;
            } else {
              ++argc;
              if (argc >= args.length) {
                System.err.println("Missing hostname for '-c'");
                usage = true;
              } else {
                hostName = args[argc];
              }
            }
            break;
          case 's':
            if (hostName != null) {
              System.err.println("Cannot specify both '-c' and '-s'!");
              usage = true;
            } else {
              startServer = true;
            }
            break;
          default:
            System.err.println(getClass().getName() +
                               ": Unknown option \"-" + ch + "\"");
            usage = true;
            break;
          }
        }
      } else {
        System.err.println(getClass().getName() + ": Unknown keyword \"" +
                           args[argc] + "\"");
        usage = true;
      }
    }

    if (usage) {
      System.err.println("Usage: " + getClass().getName() +
                         " [-c(lient) hostname] [-s(erver)]");
    }

    return !usage;
  }

  boolean isServer() { return (startServer && hostName == null); }
  boolean isClient() { return (!startServer && hostName != null); }
  boolean isStandalone() { return (!startServer && hostName == null); }

  String getClientServerTitle()
  {
    if (isServer()) {
      return " server";
    } else if (isClient()) {
      return " client";
    } else if (isStandalone()) {
      return " standalone";
    }
    return " unknown";
  }

  RemoteServer getClientServer()
    throws RemoteException, VisADException
  {
    RemoteServer client = null;
    String domain = "//" + hostName + "/" + getClass().getName();

    int loops = 0;
    while (client == null && loops < maximumWaitTime) {

      // try to reconnect to the server after the first loop
      if (loops > 0) {
        try {
          client = (RemoteServer )Naming.lookup(domain);
        } catch (NotBoundException nbe) {
          client = null;
        } catch (Exception e) {
          throw new VisADException ("Cannot connect to server on \"" +
                                    hostName + "\" (" +
                                    e.getClass().getName() + ": " +
                                    e.getMessage() + ")");
        }
      }

      // try to get first display from remote server
      RemoteDisplay rmtDpy;
      try {
        if (client != null) {
          rmtDpy = client.getDisplay(0);
        }
      } catch (java.rmi.ConnectException ce) {
        client = null;
      }

      // if we didn't get the display, print a message and wait a bit
      if (client == null) {
        if (loops == 0) {
          System.err.print("Client waiting for server ");
        } else {
          System.err.print(".");
        }

        try { sleep(1000); } catch (InterruptedException ie) { }

        loops++;
      }
    }

    if (loops == maximumWaitTime) {
      System.err.println(" giving up!");
      System.exit(1);
    } else if (loops > 0) {
      System.err.println(". connected");
    }

    return client;
  }

  RemoteDisplay[] getClientDisplays(RemoteServer client)
    throws RemoteException, VisADException
  {
    // fail if there's no remote server
    if (client == null) {
      return null;
    }

    RemoteDisplay[] rmtDpys = null;

    int loops = 0;
    while (rmtDpys == null && loops < maximumWaitTime) {

      try {
        rmtDpys = client.getDisplays();
      } catch (java.rmi.ConnectException ce) {
      }

      // if we didn't get the display, print a message and wait a bit
      if (rmtDpys == null) {
        if (loops == 0) {
          System.err.print("Client waiting for server displays ");
        } else {
          System.err.print(".");
        }

        try { sleep(1000); } catch (InterruptedException ie) { }

        loops++;
      }
    }

    if (loops == maximumWaitTime) {
      System.err.println(" giving up!");
      System.exit(1);
    } else if (loops > 0) {
      System.err.println(". ready");
    }

    return rmtDpys;
  }

  private static LocalDisplay wrapRemoteDisplay(RemoteDisplay rmtDpy)
    throws RemoteException, VisADException
  {
    String className = rmtDpy.getDisplayClassName();
    Class dpyClass;
    try {
      dpyClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new VisADException("Couldn't create " + className);
    }

    Class[] params = new Class[1];
    try {
      params[0] = Class.forName("visad.RemoteDisplay");
    } catch (ClassNotFoundException e) {
      throw new VisADException("Yikes! Couldn't find visad.RemoteDisplay!");
    }

    java.lang.reflect.Constructor cons;
    try {
      cons = dpyClass.getConstructor(params);
    } catch (NoSuchMethodException e) {
      throw new VisADException(className + " has no RemoteDisplay" +
                               " constructor");
    }

    DisplayImpl dpy;

    Object[] cargs = new Object[1];
    cargs[0] = rmtDpy;
    try {
      dpy = (DisplayImpl )cons.newInstance(cargs);
    } catch (Exception e) {
      throw new VisADException("Couldn't create local shadow for " +
                               rmtDpy + ": " + e.getClass().getName() +
                               ": " + e.getMessage());
    }

    return dpy;
  }

  LocalDisplay setupClientData()
    throws RemoteException, VisADException
  {
    RemoteServer client = getClientServer();
    RemoteDisplay[] rmtDpy = getClientDisplays(client);
    if (rmtDpy == null) {
      throw new VisADException("No RemoteDisplays found!");
    }
    if (rmtDpy.length != 1) {
      throw new VisADException("Multiple RemoteDisplays found!");
    }

    LocalDisplay dpy = wrapRemoteDisplay(rmtDpy[0]);

    return dpy;
  }

  RemoteServerImpl setupServer(DisplayImpl dpy)
    throws RemoteException, VisADException
  {
    // create new server
    RemoteServerImpl server;
    boolean registryStarted = false;
    while (true) {
      boolean success = true;
      try {
        server = new RemoteServerImpl();
        String domain = "//:/" + getClass().getName();
        Naming.rebind(domain, server);
        break;
      } catch (java.rmi.ConnectException ce) {
        if (!registryStarted) {
          LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
          registryStarted = true;
        } else {
          success = false;
        }
      } catch (Exception e) {
        success = false;
      }
      if (!success) {
        throw new VisADException("Cannot set up server" +
                                 " (rmiregistry may not be running)");
      }
    }

    // add display to server
    if (dpy != null) {
      server.addDisplay(new RemoteDisplayImpl(dpy));
    }

    return server;
  }

  public void startThreads()
    throws RemoteException, VisADException
  {
    LocalDisplay local;
    if (isClient()) {
      local = setupClientData();
    } else {
      DisplayImpl dpy = setupServerDisplay();

      RemoteServerImpl server;
      if (startServer) {
        server = setupServer(dpy);
      } else {
        server = null;
      }

      local = dpy;
      setupServerData(local);
    }

    setupUI(local);
  }

//////////////////////////////////////////////////////////////////////////////

  DisplayImpl setupServerDisplay()
    throws RemoteException, VisADException
  {
    display = new DisplayImplJ2D("display");
    return display;
  }

  void setupServerData(LocalDisplay dpy)
    throws RemoteException, VisADException
  {
    RealType[] time = {RealType.Time};
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);
    RealType[] types4 = {ir_radiance, vis_radiance};
    RealTupleType ecnaidar = new RealTupleType(types4);
    FunctionType image_bumble = new FunctionType(earth_location, ecnaidar);
    RealTupleType time_type = new RealTupleType(time);
    FunctionType time_images = new FunctionType(time_type, image_tuple);
    FunctionType time_bee = new FunctionType(time_type, image_bumble);

    int size = 64;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);
    FlatField wasp = FlatField.makeField(image_bumble, size, false);

    int ntimes1 = 4;
    int ntimes2 = 6;

    // different time extents test
    // 2 May 99, 15:51:00
    double start = new DateTime(1999, 122, 57060).getValue();
    Set time_set =
      new Linear1DSet(time_type, start, start + 3000.0, ntimes1);
    double[][] times =
      {{start, start + 600.0, start + 1200.0,
        start + 1800.0, start + 2400.0, start + 3000.0}};
    Set time_hornet = new Gridded1DDoubleSet(time_type, times, 6);

    FieldImpl image_sequence = new FieldImpl(time_images, time_set);
    FieldImpl image_stinger = new FieldImpl(time_bee, time_hornet);
    FlatField temp = imaget1;
    FlatField tempw = wasp;
    Real[] reals = {new Real(vis_radiance, (float) size / 4.0f),
                    new Real(ir_radiance, (float) size / 8.0f)};
    RealTuple val = new RealTuple(reals);
    for (int i=0; i<ntimes1; i++) {
      image_sequence.setSample(i, temp);
      temp = (FlatField) temp.add(val);
    }
    for (int i=0; i<ntimes2; i++) {
      image_stinger.setSample(i, tempw);
      tempw = (FlatField) tempw.add(val);
    }
    FieldImpl[] images = {image_sequence, image_stinger};
    Tuple big_tuple = new Tuple(images);

    dpy.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpy.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    dpy.addMap(new ScalarMap(ir_radiance, Display.Green));
    dpy.addMap(new ConstantMap(0.5, Display.Blue));
    dpy.addMap(new ConstantMap(0.5, Display.Red));
    dpy.addMap(new ScalarMap(vis_radiance, Display.IsoContour));
    dpy.addMap(new ScalarMap(ir_radiance, Display.SelectRange));
    dpy.addMap(new ScalarMap(ir_radiance, Display.RGBA));
    ScalarMap map1animation;
    map1animation = new ScalarMap(RealType.Time, Display.Animation);
    dpy.addMap(map1animation);

    dpyRef = new DataReferenceImpl("dpyRef");
    dpyRef.setData(big_tuple);
    dpy.addReference(dpyRef, null);
  }

  void switchDisplay()
  {
    // make sure we know which display we're switching
    if (display == null) {
      System.err.println("No display found!");
      return;
    }

    // make sure we know what kind of display we're switching to
    boolean threeD = false;
    if (display instanceof DisplayImplJ2D) {
      threeD = true;
    } else if (!(display instanceof DisplayImplJ3D)) {
      System.err.println("Unknown dimension for " +
                         display.getClass().getName());
      return;
    }

    // grab some info from the current display
    String name = display.getName();
    int api;
    try {
      api = display.getAPI();
    } catch (VisADException ve) {
      api = 0;
    }

    // try to create a new display in a different dimension
    DisplayImpl newDpy;
    try {
      if (threeD) {
        newDpy = new DisplayImplJ3D(name, api);
      } else {
        newDpy = new DisplayImplJ2D(name, api);
      }
    } catch (Exception e) {
      System.err.println("Couldn't create new display!");
      return;
    }

    // save the old maps
    Vector sMaps = display.getMapVector();
    Vector cMaps = display.getConstantMapVector();

    // destroy the old display
    try {
      dpyPanel.remove(display.getComponent());
      display.removeAllReferences();
      display.clearMaps();
      display = null;
    } catch (Exception e) {
      System.err.println("Ignoring " + e.getClass().getName() +
                         ": " + e.getMessage());
      // ignore any errors due to clearing out the old display
    }

    // add maps to new display
    if (sMaps != null) {
      int len = sMaps.size();
      for (int i = 0; i < len; i++) {
        ScalarMap map = (ScalarMap )sMaps.elementAt(i);
        try {
          newDpy.addMap(map);
        } catch (Exception e) {
          System.err.println("Couldn't re-add ScalarMap " + map + ": " +
                             e.getClass().getName() + ": " + e.getMessage());
e.printStackTrace();
        }
      }
    }
    if (cMaps != null) {
      int len = cMaps.size();
      for (int i = 0; i < len; i++) {
        ConstantMap map = (ConstantMap )cMaps.elementAt(i);
        try {
          newDpy.addMap(map);
        } catch (Exception e) {
          System.err.println("Couldn't re-add ConstantMap " + map + ": " +
                             e.getClass().getName() + ": " + e.getMessage());
e.printStackTrace();
        }
      }
    }
    try {
      newDpy.addReference(dpyRef, null);
    } catch (Exception e) {
      System.err.println("Couldn't re-add " + dpyRef + ": " +
                         e.getClass().getName() + ": " + e.getMessage());
    }

    if (threeD) {
      change.setText("Change to 2D");
    } else {
      change.setText("Change to 3D");
    }

    display = newDpy;
    dpyPanel.add(display.getComponent());
  }

  Component getSpecialComponent(LocalDisplay dpy)
    throws RemoteException, VisADException
  {
    Vector v = dpy.getMapVector();
    int vSize = v.size();

    ScalarMap animMap = (ScalarMap )v.elementAt(vSize - 1);
    ScalarMap rgbaMap = (ScalarMap )v.elementAt(vSize - 2);
    ScalarMap selectMap = (ScalarMap )v.elementAt(vSize - 3);
    ScalarMap contourMap = (ScalarMap )v.elementAt(vSize - 4);

    JPanel widgets = new JPanel();
    widgets.setLayout(new BoxLayout(widgets, BoxLayout.Y_AXIS));

    dpyPanel = new JPanel();
    dpyPanel.add(dpy.getComponent());

    widgets.add(dpyPanel);

    widgets.add(new AnimationWidget(animMap, 500));
    widgets.add(new ContourWidget(contourMap));
    widgets.add(new GMCWidget(dpy.getGraphicsModeControl()));
    widgets.add(new LabeledColorWidget(rgbaMap));
    widgets.add(new ProjWidget(dpy.getProjectionControl()));
    widgets.add(new RangeWidget(rgbaMap));
    widgets.add(new SelectRangeWidget(selectMap));

    if (isServer() || isStandalone()) {
      change = new JButton("Change to 3D");
      change.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            switchDisplay();
          }
        });
      widgets.add(change);
    }

    return widgets;
  }

  void setupUI(LocalDisplay dpy)
    throws RemoteException, VisADException
  {
    Component special = getSpecialComponent(dpy);

    Container content;
    if (special instanceof Container) {
      content = (Container )special;
    } else {
      JPanel wrapper = new JPanel();
      wrapper.setLayout(new BorderLayout());
      wrapper.add("Center", special);
      content = wrapper;
    }

    JFrame jframe = new JFrame(getFrameTitle() + getClientServerTitle());
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    jframe.setContentPane(content);
    jframe.pack();
    jframe.setVisible(true);
  }

  String getFrameTitle() { return "2d/3d display switch"; }

  public String toString() { return ": Changing between 2d and 3d display"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new DisplaySwitch(args);
  }
}
