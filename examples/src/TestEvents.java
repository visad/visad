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

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

import visad.BaseColorControl;
import visad.ColorAlphaControl;
import visad.Control;
import visad.ConstantMap;
import visad.Display;
import visad.DisplayImpl;
import visad.LocalDisplay;
import visad.RealType;
import visad.RemoteDisplayImpl;
import visad.ScalarMap;
import visad.VisADException;

import visad.collab.DisplaySyncImpl;

import visad.java2d.DisplayImplJ2D;

import visad.util.Delay;

/**
 * Start up a bunch of collaborative clients, then have the
 * server and clients go through a number of rounds where
 * they all tweak their controls to trigger a bunch of events
 * and, after a predetermined amount of time, check to make
 * sure everyone is synchronized.<br>
 * <br>
 * <table>
 * <tr><th>Option</th><th>Description</th></tr>
 * <tr><td>-c&nbsp;<i>num</i></td><td>Number of clients</td></tr>
 * <tr><td>-d&nbsp;<i>num</i></td><td>Seconds to delay * 100</td></tr>
 * <tr><td>-e&nbsp;<i>num</i></td><td>Number of events per round</td></tr>
 * <tr><td>-r&nbsp;<i>num</i></td><td>Number of rounds</td></tr>
 * </table>
 */
public class TestEvents
{
  private static Object waiter = new Object();

  private static java.util.Random rand = null;

  private static RealType mapType = null;

  private int numClients = 6;
  private int numRounds = 6;
  private int numEvents = 6;

  private long randSeed = -1;
  private int delayUsecs = 150;
  private boolean verbose = false;

  public TestEvents(String[] args)
  {
    if (!processArgs(args)) {
      System.err.println("Exiting...");
      System.exit(1);
    }

    if (rand == null) {
      if (randSeed == -1) {
        rand = new Random();
      } else {
        rand = new Random(randSeed);
      }
    }

    mapType = RealType.getRealType("Map");

    DisplayImpl server = createServer();
    if (server == null) {
      System.err.println("Couldn't create server!");
      System.exit(1);
      return;
    }

    Bundle[] bundle = createBundle(server, numClients);
    if (bundle == null) {
      System.exit(1);
      return;
    }

    boolean result = true;
    for (int i = 0; i < numRounds; i++) {
      if (verbose) {
        System.err.println("--- Round "+i);
        System.err.flush();
      }

      for (int j = 0; j < numEvents; j++) {
        //new Delay(1000);
        synchronized (waiter) { waiter.notifyAll(); }
        Thread.yield();
      }

      for (int d = 0; d < 10; d++) {
        new Delay(delayUsecs);
        if (verbose) {
          System.err.println("++ Finished Delay#" + d + ":" +
                             System.currentTimeMillis());
          System.err.flush();
        }
      }

      result &= compareAll(bundle);
    }

    StringBuffer dangle = null;
    for (int i = 0; i < bundle.length; i++) {
      DisplayImpl dpy = (DisplayImpl )bundle[i].getDisplay();
      DisplaySyncImpl dsi = (DisplaySyncImpl )dpy.getDisplaySync();
      if (dsi.isThreadRunning()) {
        if (dangle == null) {
          dangle = new StringBuffer(dsi.getName());
        } else {
          dangle.append(", ");
          dangle.append(dsi.getName());
        }
        result = false;
      }
    }
    if (dangle != null) {
      dangle.insert(0, "Yikes ... thread running for ");
      String dangleStr = dangle.toString();
      System.out.println(dangleStr);
      System.err.println(dangleStr);
    }

    System.out.flush();
    System.err.flush();

    if (!result) {
      System.exit(1);
      return;
    }

    System.out.println("Happy happy, joy joy!");
    System.exit(0);
  }

  private boolean compareAll(Bundle[] bundle)
  {
    StringBuffer badList = null;
    final TriggerControl tc = bundle[0].getTriggerControl();
    final BaseColorControl cc = bundle[0].getColorControl();

    boolean allTcCmp = true;
    boolean allCcCmp = true;

    for (int i = 1; i < bundle.length; i++) {
      final TriggerControl btc = bundle[i].getTriggerControl();
      final BaseColorControl bcc = bundle[i].getColorControl();

      final boolean tcCmp = btc.equals(tc);
      final boolean ccCmp = bcc.equals(cc);

      if (!tcCmp || !ccCmp) {
        DisplayImpl dpy = (DisplayImpl )bundle[i].getDisplay();
        if (badList == null) {
          badList = new StringBuffer();
        } else {
          badList.append(", ");
        }

        badList.append(dpy.getName());
        badList.append('=');

        if (!tcCmp) {
          badList.append(btc);
          allTcCmp = false;

          if (!ccCmp) {
            badList.append(',');
            badList.append(bcc);
            allCcCmp = false;
          }
        } else if (!ccCmp) {
          badList.append(bcc);
          allCcCmp = false;
        }
      }
    }

    final boolean matched = (badList == null);
    if (!matched) {
      badList.insert(0, ", got ");
      if (!allCcCmp) {
        badList.insert(0, cc);
      }
      if (!allTcCmp) {
        if (!allCcCmp) {
          badList.insert(0, ',');
        }
        badList.insert(0, tc);
      }
      badList.insert(0, "Wanted ");

      String badStr = badList.toString();

      System.err.println(badStr);
      System.out.println(badStr);
    }

    return matched;
  }

  private DisplayImpl createServer()
  {
    DisplayImplJ2D dpy;
    try {
      dpy = new DisplayImplJ2D("root");
    } catch (RemoteException re) {
      return null;
    } catch (VisADException ve) {
      return null;
    }

    return dpy;
  }

  private Bundle[] createBundle(DisplayImpl server, int num)
  {
    Bundle[] bundle = new Bundle[num+1];

    try {
      bundle[0] = new Bundle(server, false);
    } catch (RemoteException re) {
      System.err.println("Couldn't create server wrapper");
      re.printStackTrace();
      return null;
    } catch (VisADException ve) {
      System.err.println("Couldn't create server wrapper");
      ve.printStackTrace();
      return null;
    }

    for (int i = 0; i < num; i++) {
      try {
        bundle[i+1] = new Bundle(server);
      } catch (RemoteException re) {
        System.err.println("Couldn't create client #" + i + "!");
        re.printStackTrace();
        return null;
      } catch (VisADException ve) {
        System.err.println("Couldn't create client #" + i + "!");
        ve.printStackTrace();
        return null;
      }
    }

    return bundle;
  }

  private static final TriggerControl getTriggerCtl(DisplayImpl dpy)
  {
    Class tClass;
    try {
      tClass = Class.forName(TriggerControl.class.getName());
    } catch (ClassNotFoundException cnfe) {
      tClass = null;
    }

    return (TriggerControl )dpy.getControl(tClass, 0);
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
            System.err.println(progName +
                               ": Missing number of clients for \"-c\"");
            usage = true;
          } else {
            try {
              numClients = Integer.parseInt(str);
            } catch (NumberFormatException nfe) {
              System.err.println(progName +
                                 ": Bad number of clients \"" + str + "\"");
              numClients = 2;
              usage = true;
            }

            if (numClients < 1) {
              System.err.println(progName +
                                 ": Need at least one client!");
              usage = true;
            }
          }
          break;
        case 'd':
          str = (args[i].length() > 2 ? args[i].substring(2) :
                 ((i + 1) < args.length ? args[++i] : null));
          if (str == null) {
            System.err.println(progName +
                               ": Missing delay usecs for \"-d\"");
            usage = true;
          } else {
            try {
              delayUsecs = Integer.parseInt(str);
            } catch (NumberFormatException nfe) {
              System.err.println(progName +
                                 ": Bad delay usecs \"" + str + "\"");
              delayUsecs = 100;
              usage = true;
            }

            if (delayUsecs < 1) {
              System.err.println(progName +
                                 ": Delay usecs needs to be" +
                                 " greater than zero!");
              usage = true;
            }
          }
          break;
        case 'e':
          str = (args[i].length() > 2 ? args[i].substring(2) :
                 ((i + 1) < args.length ? args[++i] : null));
          if (str == null) {
            System.err.println(progName +
                               ": Missing number of events for \"-e\"");
            usage = true;
          } else {
            try {
              numEvents = Integer.parseInt(str);
            } catch (NumberFormatException nfe) {
              System.err.println(progName +
                                 ": Bad number of events \"" + str + "\"");
              numEvents = 3;
              usage = true;
            }

            if (numEvents < 1) {
              System.err.println(progName +
                                 ": Need at least one event!");
              usage = true;
            }
          }
          break;
        case 'r':
          str = (args[i].length() > 2 ? args[i].substring(2) :
                 ((i + 1) < args.length ? args[++i] : null));
          if (str == null) {
            System.err.println(progName +
                               ": Missing  rounds for \"-r\"");
            usage = true;
          } else {
            try {
              numRounds = Integer.parseInt(str);
            } catch (NumberFormatException nfe) {
              System.err.println(progName +
                                 ": Bad number of rounds \"" + str + "\"");
              numRounds = 1;
              usage = true;
            }

            if (numRounds < 1) {
              System.err.println(progName +
                                 ": Need at least one round!");
              usage = true;
            }
          }
          break;
        case 's':
          str = (args[i].length() > 2 ? args[i].substring(2) :
                 ((i + 1) < args.length ? args[++i] : null));
          if (str == null) {
            System.err.println(progName +
                               ": Missing random seed value for \"-s\"");
            usage = true;
          } else {
            try {
              randSeed = Long.parseLong(str);
            } catch (NumberFormatException nfe) {
              System.err.println(progName +
                                 ": Bad random seed value \"" + str + "\"");
              usage = true;
            }
          }
          break;
        case 'v':
          verbose = true;
          break;
        default:
          System.err.println(progName +
                             ": Unknown option \"-" + ch + "\"");
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
                         " [-c numClients]" +
                         " [-d delayUSecs]" +
                         " [-e numEvents]" +
                         " [-r numRounds]" +
                         " [-s randomSeed]" +
                         " [-v(erbose)]" +
                         "");
    }

    return !usage;
  }

  class Bundle
    extends Thread
  {
    private DisplayImplJ2D dpy;
    private TriggerControl tc;
    private ColorAlphaControl cac;

    public Bundle(DisplayImpl server)
      throws RemoteException, VisADException
    {
      this(server, true);
    }

    public Bundle(DisplayImpl server, boolean buildClient)
      throws RemoteException, VisADException
    {
      if (!buildClient) {
        dpy = (DisplayImplJ2D )server;

        ScalarMap map = new ScalarMap(mapType, Display.RGBA);
        dpy.addMap(map);
      } else {
        RemoteDisplayImpl rmtdpy = new RemoteDisplayImpl(server);
        dpy = new DisplayImplJ2D(rmtdpy);
      }

      tc = new TriggerControl(dpy);
      dpy.addControl(tc);

      ScalarMap map = findMap(dpy, Display.RGBA);
      cac = (ColorAlphaControl )map.getControl();

      start();
    }

    private ScalarMap findMap(LocalDisplay dpy, RealType displayScalar)
    {
      if (displayScalar == null) {
        return null;
      }

      java.util.Iterator maps;

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

    public DisplayImpl getDisplay() { return dpy; }

    private float[][] getRandomTable(int w, int h)
    {
      float[][] t = new float[w][h];
      for (int i = 0; i < w; i++) {
        for (int j = 0; j < h; j++) {
          t[i][j] = rand.nextFloat();
        }
      }
      return t;
    }

    public BaseColorControl getColorControl() { return cac; }
    public TriggerControl getTriggerControl() { return tc; }

    public void run()
    {
      while (true) {
        synchronized (waiter) {
          try { waiter.wait(); } catch (InterruptedException ie) { }
        }

        try {
          tc.fire();
          cac.setTable(getRandomTable(cac.getNumberOfComponents(),
                                      cac.getNumberOfColors()));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  interface Revolver
  {
    void triggered();
  }

  private static int masterTriggerNum = 1;

  class TriggerControl
    extends Control
  {
    private ArrayList list;
    private int num, val;

    public TriggerControl(DisplayImpl dpy)
    {
      super(dpy);

      list = new ArrayList();
      num = masterTriggerNum++;
      val = 0;
    }

    public void addListener(Revolver rvlvr)
    {
      synchronized (list) {
        list.add(rvlvr);
      }
    }

    private final void delay()
    {
      int dLen = rand.nextInt() % 50;
      dLen = (dLen < 0 ? -dLen : (dLen == 0 ? 1 : dLen));
      new Delay(dLen);
    }

    public void fire()
      throws RemoteException, VisADException
    {
      val += num;
      changeControl(true);
      notifyListeners();
    }

    public String getSaveString() { return Integer.toString(val); }

    public int getValue() { return val; }

    public void notifyListeners()
    {
      ListIterator iter = list.listIterator();
      while (iter.hasNext()) {
        Revolver r = (Revolver )iter.next();
        r.triggered();
      }
    }

    public void setSaveString(String save)
      throws VisADException, RemoteException
    {
      if (save == null) {
        throw new VisADException("Invalid save string");
      }

      try {
        val = Integer.parseInt(save);
      } catch (NumberFormatException nfe) {
        throw new VisADException("Bad TriggerControl save string \"" + save +
                                 "\"");
      }
    }

    public void syncControl(Control ctl)
      throws VisADException
    {
      if (ctl == null) {
        throw new VisADException("Cannot synchronize " +
                                 this.getClass().getName() +
                                 " with null Control object");
      }

      if (!(ctl instanceof TriggerControl)) {
        throw new VisADException("Cannot synchronize " +
                                 this.getClass().getName() +
                                 " with " + ctl.getClass().getName());
      }

      TriggerControl tc = (TriggerControl )ctl;

      boolean changed = false;

      if (val != tc.val) {
        changed = true;
        val = tc.val;
        notifyListeners();
      }

      if (changed) {
        try {
          changeControl(true);
        } catch (RemoteException re) {
          throw new VisADException("Could not indicate that control" +
                                   " changed: " + re.getMessage());
        }
      }
    }

    public boolean equals(Object o)
    {
      if (!super.equals(o)) {
        return false;
      }

      TriggerControl tc = (TriggerControl )o;

      if (val != tc.val) {
        return false;
      }

      return true;
    }

    public String toString()
    {
      StringBuffer buf = new StringBuffer("TriggerControl[#");
      buf.append(num);
      buf.append('=');
      buf.append(val);
      buf.append(']');
      return buf.toString();
    }
  }

  public static void main(String[] args)
  {
    new TestEvents(args);
  }
}
