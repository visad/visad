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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import visad.*;

import visad.java2d.DisplayImplJ2D;

import visad.java3d.DisplayImplJ3D;

import visad.util.AnimationWidget;
import visad.util.ClientServer;
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
  private DataReferenceImpl[] dpyRefs = null;
  private Data alphaData0 = null;
  private Data betaData0 = null;
  private Data betaData1 = null;
  private ConstantMap[] betaConstMaps = null;

  private JPanel dpyPanel = null;
  private JButton switchDim = null;
  private JButton switchData = null;

  private boolean startServer = false;
  private String hostName = null;

  private static final int maximumWaitTime = 60;

  private boolean dpy3D = false;
  private boolean dpyBeta = false;

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

    String className = getClass().getName();
    int pt = className.lastIndexOf('.');
    final int ds = className.lastIndexOf('$');
    if (ds > pt) {
      pt = ds;
    }
    String progName = className.substring(pt == -1 ? 0 : pt + 1);

    for (int i = 0; args != null && i < args.length; i++) {
      if (args[i].length() > 0 && args[i].charAt(0) == '-') {
        char ch = args[i].charAt(1);

        String str, result;

        switch (ch) {
        case 'c':
          str = (args[i].length() > 2 ? args[i].substring(2) :
                 ((i + 1) < args.length ? args[++i] : null));
          if (str == null) {
            System.err.println(progName + ": Missing hostname for \"-c\"");
            usage = true;
          } else if (startServer) {
            System.err.println(progName +
                               ": Cannot specify both '-c' and '-s'!");
            usage = true;
          } else {
            hostName = str;
          }
          break;
        case 's':
          if (hostName != null) {
            System.err.println(progName +
                               ": Cannot specify both '-c' and '-s'!");
            usage = true;
          } else {
            startServer = true;
          }
          break;
        default:
          System.err.println(progName + ": Unknown option \"-" + ch + "\"");
          usage = true;
          break;
        }
      } else {
        System.err.println(progName + ": Unknown keyword \"" + args[i] + "\"");
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

  LocalDisplay setupClientData()
    throws RemoteException, VisADException
  {
    RemoteServer client;
    try {
      client = ClientServer.connectToServer(hostName, getClass().getName(),
                                            true);
    } catch (VisADException ve) {
      System.err.println(ve.getMessage());
      System.exit(1);
      client = null;
    }

    LocalDisplay[] dpys = ClientServer.getClientDisplays(client);
    if (dpys == null) {
      throw new VisADException("No remote displays found!");
    }
    if (dpys.length != 1) {
      throw new VisADException("Multiple remote displays found!");
    }

    return dpys[0];
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
      if (!startServer) {
        server = null;
      } else {
        server = ClientServer.startServer(getClass().getName());

        // add display to server
        if (dpy != null) {
          server.addDisplay(new RemoteDisplayImpl(dpy));
        }
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

  private static ScalarMap findMap(LocalDisplay dpy, RealType displayScalar)
  {
    if (displayScalar == null) {
      return null;
    }

    Iterator maps;

    try {
      maps = dpy.getMapVector().iterator();
    } catch (Exception e) {
      maps = null;
    }

    if (maps != null) {
      while (maps.hasNext()) {
        ScalarMap smap = (ScalarMap )maps.next();
        if (displayScalar.equals(smap.getDisplayScalar())) {
          return smap;
        }
      }
    }

    try {
      maps = dpy.getConstantMapVector().iterator();
    } catch (Exception e) {
      maps = null;
    }

    if (maps != null) {
      while (maps.hasNext()) {
        ConstantMap cmap = (ConstantMap )maps.next();
        if (displayScalar.equals(cmap.getDisplayScalar())) {
          return cmap;
        }
      }
    }

    return null;
  }

  private void buildMaps(LocalDisplay dpy)
    throws RemoteException, VisADException
  {
    RealType dom0 = RealType.getRealType("dom0");
    RealType dom1 = RealType.getRealType("dom1");

    dpy.addMap(new ScalarMap(RealType.Latitude, Display.XAxis));
    dpy.addMap(new ScalarMap(RealType.Longitude, Display.YAxis));

    dpy.addMap(new ScalarMap(dom1, Display.Green));
    dpy.addMap(new ConstantMap(0.5, Display.Blue));
    dpy.addMap(new ConstantMap(0.5, Display.Red));
    dpy.addMap(new ScalarMap(dom0, Display.IsoContour));
    dpy.addMap(new ScalarMap(dom1, Display.SelectRange));
    dpy.addMap(new ScalarMap(dom1, Display.RGBA));
    dpy.addMap(new ScalarMap(RealType.Time, Display.Animation));
  }

  private DataImpl buildAlphaRefZero()
    throws RemoteException, VisADException
  {
    RealType dom0 = RealType.getRealType("dom0");
    RealType dom1 = RealType.getRealType("dom1");

    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earthLocation = new RealTupleType(types);

    RealType[] viTypes = {dom0, dom1};
    RealTupleType viTT = new RealTupleType(viTypes);
    FunctionType viFunc = new FunctionType(earthLocation, viTT);

    RealType[] ivTypes = {dom1, dom0};
    RealTupleType ivTT = new RealTupleType(ivTypes);
    FunctionType ivFunc = new FunctionType(earthLocation, ivTT);

    RealType[] time = {RealType.Time};
    RealTupleType timeType = new RealTupleType(time);
    FunctionType timeVI = new FunctionType(timeType, viFunc);
    FunctionType timeIV = new FunctionType(timeType, ivFunc);

    int size = 64;
    int ntimes = 4;

    // 2 May 99, 15:51:00
    double start = new DateTime(1999, 122, 57060).getValue();
    Set timeSet = new Linear1DSet(timeType, start, start + 3000.0, ntimes);
    double[][] times =
      {{start, start + 600.0, start + 1200.0,
        start + 1800.0, start + 2400.0, start + 3000.0}};
    Set timeGrid = new Gridded1DDoubleSet(timeType, times, times[0].length);

    FieldImpl setSequence = new FieldImpl(timeVI, timeSet);
    FieldImpl gridSequence = new FieldImpl(timeIV, timeGrid);
    FlatField flatVI = FlatField.makeField(viFunc, size, false);
    FlatField flatIV = FlatField.makeField(ivFunc, size, false);
    Real[] reals = {new Real(dom0, (float) size / 4.0f),
                    new Real(dom1, (float) size / 8.0f)};
    RealTuple val = new RealTuple(reals);
    for (int i=0; i<ntimes; i++) {
      setSequence.setSample(i, flatVI);
      flatVI = (FlatField) flatVI.add(val);
    }
    for (int i=0; i<times[0].length; i++) {
      gridSequence.setSample(i, flatIV);
      flatIV = (FlatField) flatIV.add(val);
    }
    FieldImpl[] images = {setSequence, gridSequence};
    return new Tuple(images);
  }

  void setupAlphaRefs(LocalDisplay dpy)
    throws RemoteException, VisADException
  {
    if (alphaData0 == null) {
      alphaData0 = buildAlphaRefZero();
    }

    dpyRefs = new DataReferenceImpl[1];

    dpyRefs[0] = new DataReferenceImpl("dpyRef0");
    dpyRefs[0].setData(alphaData0);
    dpy.addReference(dpyRefs[0], null);
  }

  private DataImpl buildBetaRefZero()
    throws RemoteException, VisADException
  {
    RealType dom0 = RealType.getRealType("dom0");
    RealType dom1 = RealType.getRealType("dom1");

    int isize = 16;

    RealTupleType revLocation;
    revLocation = new RealTupleType(RealType.Longitude, RealType.Latitude);

    RealTupleType domTypes = new RealTupleType(dom0, dom1);

    FunctionType func = new FunctionType(revLocation, domTypes);

    FlatField flat = FlatField.makeField(func, isize, false);

    double[][] vals = new double[2][isize * isize];
    for (int i=0; i<isize; i++) {
      for (int j=0; j<isize; j++) {
        vals[0][j + isize * i] = (i + 1) * (j + 1);
        vals[1][j + isize * i] = ((double )i * 1.2) * ((double )j / 1.2);
      }
    }

    return flat;
  }

  private DataImpl buildBetaRefOne()
    throws RemoteException, VisADException
  {
    RealType dom0 = RealType.getRealType("dom0");
    RealType dom1 = RealType.getRealType("dom1");

    int isize = 16;

    RealTupleType earthLocation;
    earthLocation = new RealTupleType(RealType.Latitude, RealType.Longitude);

    RealTupleType domTypes = new RealTupleType(dom0, dom1);

    FunctionType func = new FunctionType(earthLocation, domTypes);

    FlatField flat = FlatField.makeField(func, isize, false);

    double[][] vals = new double[2][isize * isize];
    for (int i=0; i<isize; i++) {
      for (int j=0; j<isize; j++) {
        vals[0][j + isize * i] = (i + 1) * (j + 1);
        vals[1][j + isize * i] = ((double )i * 1.2) * ((double )j / 1.2);
      }
    }

    flat.setSamples(vals, false);
    return flat;
  }

  void setupBetaRefs(LocalDisplay dpy)
    throws RemoteException, VisADException
  {
    if (betaData0 == null) {
      betaData0 = buildBetaRefZero();
    }
    if (betaData1 == null) {
      betaData1 = buildBetaRefOne();
    }
    if (betaConstMaps == null) {
      betaConstMaps = new ConstantMap[3];
      betaConstMaps[0] = new ConstantMap(1.0, Display.Blue);
      betaConstMaps[1] = new ConstantMap(1.0, Display.Red);
      betaConstMaps[2] = new ConstantMap(0.0, Display.Green);
    }

    dpyRefs = new DataReferenceImpl[2];

    dpyRefs[0] = new DataReferenceImpl("dpyRef0");
    dpyRefs[0].setData(betaData0);
    dpy.addReference(dpyRefs[0], null);


    dpyRefs[1] = new DataReferenceImpl("dpyRef1");
    dpyRefs[1].setData(betaData1);
    dpy.addReference(dpyRefs[1], betaConstMaps);
  }

  void setupServerData(LocalDisplay dpy)
    throws RemoteException, VisADException
  {
    buildMaps(dpy);

    setupAlphaRefs(dpy);
  }

  void switchDisplay(boolean rerollData)
  {
    // make sure we know which display we're switching
    if (display == null) {
      System.err.println("No display found!");
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
      if (dpy3D) {
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
    if (dpyRefs == null || rerollData) {
      if (dpyBeta) {
        try {
          setupBetaRefs(newDpy);
        } catch (Exception e) {
          System.err.println("Couldn't re-init beta refs: " +
                             e.getClass().getName() + ": " + e.getMessage());
e.printStackTrace();
        }
      } else {
        try {
          setupAlphaRefs(newDpy);
        } catch (Exception e) {
          System.err.println("Couldn't re-init alpha refs: " +
                             e.getClass().getName() + ": " + e.getMessage());
e.printStackTrace();
        }
      }
    } else {
      for (int i = 0; i < dpyRefs.length; i++) {
        try {
          newDpy.addReference(dpyRefs[i], null);
        } catch (Exception e) {
          System.err.println("Couldn't re-add " + dpyRefs[i] + ": " +
                             e.getClass().getName() + ": " + e.getMessage());
e.printStackTrace();
        }
      }
    }

    display = newDpy;
    dpyPanel.add(display.getComponent());
  }

  private Component buildSwitchButtons()
  {
    JPanel buttons = new JPanel();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

    switchDim = new JButton("Change to 3D");
    switchDim.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          dpy3D = !dpy3D;
          switchDisplay(false);
          if (dpy3D) {
            switchDim.setText("Change to 2D");
          } else {
            switchDim.setText("Change to 3D");
          }
        }
      });
    buttons.add(switchDim);

    switchData = new JButton("Change Data");
    switchData.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          dpyBeta = !dpyBeta;
          switchDisplay(true);
        }
      });
    buttons.add(switchData);

    return buttons;
  }

  Component getSpecialComponent(LocalDisplay dpy)
    throws RemoteException, VisADException
  {
    Vector v = dpy.getMapVector();

    ScalarMap rgbaMap = findMap(dpy, Display.RGBA);

    JPanel widgets = new JPanel();
    widgets.setLayout(new BoxLayout(widgets, BoxLayout.Y_AXIS));

    dpyPanel = new JPanel();
    dpyPanel.add(dpy.getComponent());

    widgets.add(dpyPanel);

    widgets.add(new AnimationWidget(findMap(dpy, Display.Animation), 500));
    widgets.add(new ContourWidget(findMap(dpy, Display.IsoContour)));
    widgets.add(new GMCWidget(dpy.getGraphicsModeControl()));
    widgets.add(new LabeledColorWidget(rgbaMap));
    widgets.add(new ProjWidget(dpy.getProjectionControl()));
    widgets.add(new RangeWidget(rgbaMap));
    widgets.add(new SelectRangeWidget(findMap(dpy, Display.SelectRange)));

    if (isServer() || isStandalone()) {
      widgets.add(buildSwitchButtons());
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
