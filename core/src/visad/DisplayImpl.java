//
// DisplayImpl.java
//

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

package visad;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import visad.browser.Convert;
import visad.browser.Divider;
import visad.collab.ControlMonitorEvent;
import visad.collab.DisplayMonitor;
import visad.collab.DisplayMonitorImpl;
import visad.collab.DisplaySync;
import visad.collab.DisplaySyncImpl;
import visad.collab.MonitorEvent;
import visad.util.AnimationWidget;
import visad.util.ContourWidget;
import visad.util.GMCWidget;
import visad.util.LabeledColorWidget;
import visad.util.RangeWidget;
import visad.util.SelectRangeWidget;
import visad.util.VisADSlider;

/**
   DisplayImpl is the abstract VisAD superclass for display
   implementations.  It is runnable.<P>

   DisplayImpl is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class DisplayImpl extends ActionImpl implements LocalDisplay {

  /** instance variables */

  /**
   * a Vector of ScalarMap objects;
   *   does not include ConstantMap objects 
   */
  private Vector MapVector = new Vector();

  /** a Vector of ConstantMap objects */
  private Vector ConstantMapVector = new Vector();

  /**
   * a Vector of RealType (and TextType) objects occuring
   *   in MapVector 
   */
  private Vector RealTypeVector = new Vector();

  /** a Vector of DisplayRealType objects occuring in MapVector */
  private Vector DisplayRealTypeVector = new Vector();

  /**
   * list of Control objects linked to ScalarMap objects in MapVector;
   *   the Control objects may be linked to UI widgets, or just computed 
   */
  private Vector ControlVector = new Vector();

  /** ordered list of DataRenderer objects that render Data objects */
  private Vector RendererVector = new Vector();

  /**
   * list of objects interested in learning when DataRenderers
   *   are deleted from this Display 
   */
  private Vector RendererSourceListeners = new Vector();

  /**
   * list of objects interested in learning when Data objects
   *   are deleted from this Display 
   */
  private Vector RmtSrcListeners = new Vector();

  /**
   * list of objects interested in receiving messages
   *   from this Display 
   */
  private Vector MessageListeners = new Vector();

  /** DisplayRenderer object for background and metadata rendering */
  private DisplayRenderer displayRenderer;

  /**
   * Component where data depictions are rendered;
   *   must be set by concrete subclass constructor;
   *   may be null for off-screen displays 
   */
  Component component;

  /**           */
  private ComponentChangedListener componentListener = null;

  /**
   * set to indicate need to compute ranges of RealType-s
   *   and sampling for Animation 
   */
  private boolean initialize = true;

  /**
   * set to indicate that ranges should be auto-scaled
   *   every time data are displayed 
   */
  private boolean always_initialize = false;

  /** set to re-display all linked Data */
  private boolean redisplay_all = false;


  /**
   * length of ValueArray of distinct DisplayRealType values;
   *   one per Single DisplayRealType that occurs in a ScalarMap,
   *   plus one per ScalarMap per non-Single DisplayRealType;
   *   ScalarMap.valueIndex is an index into ValueArray 
   */
  private int valueArrayLength;

  /** mapping from ValueArray to DisplayScalar */
  private int[] valueToScalar;

  /** mapping from ValueArray to MapVector */
  private int[] valueToMap;

  /** Vector of DisplayListeners */
  private final transient Vector ListenerVector = new Vector();

  /**           */
  private Object mapslock = new Object();

  // WLH 16 March 99

  /**           */
  private MouseBehavior mouse = null;

  // objects which monitor and synchronize with remote displays

  /**           */
  private transient DisplayMonitor displayMonitor = null;

  /**           */
  private transient DisplaySync displaySync = null;

  // activity monitor

  /**           */
  private transient DisplayActivity displayActivity = null;

  // Support for printing

  /**           */
  private Printable printer;

  /** has this display been destroyed           */
  private boolean destroyed = false;

  /**
   * construct a DisplayImpl with given name and DisplayRenderer
   * @param name String name for DisplayImpl (used for debugging)
   * @param renderer DisplayRenderer that controls aspects of the
   *                 display not specific to any particular Data
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public DisplayImpl(String name, DisplayRenderer renderer)
          throws VisADException, RemoteException {
    super(name);
    // put system intrinsic DisplayRealType-s in DisplayRealTypeVector
    for (int i = 0; i < DisplayRealArray.length; i++) {
      DisplayRealTypeVector.addElement(DisplayRealArray[i]);
    }

    displayMonitor = new DisplayMonitorImpl(this);
    displaySync = new DisplaySyncImpl(this);

    if (renderer != null) {
      displayRenderer = renderer;
    }
    else {
      displayRenderer = getDefaultDisplayRenderer();
    }
    displayRenderer.setDisplay(this);

    // initialize ScalarMap's, ShadowDisplayReal's and Control's
    clearMaps();
  }

  /**
   * construct a DisplayImpl collaborating with the given RemoteDisplay,
   * and with the given DisplayRenderer
   * @param rmtDpy RemoteDisplay to collaborate with
   * @param renderer DisplayRenderer that controls aspects of the
   *                 display not specific to any particular Data
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public DisplayImpl(RemoteDisplay rmtDpy, DisplayRenderer renderer)
          throws VisADException, RemoteException {
    // this(rmtDpy, renderer, false);


    super(rmtDpy.getName() + ".remote"); // WLH 11 April 2001

    // get class used for remote display
    String className = rmtDpy.getDisplayClassName();
    Class rmtClass;
    try {
      rmtClass = Class.forName(className);
    }
    catch (ClassNotFoundException cnfe) {
      throw new DisplayException("Cannot find remote display class " +
                                 className);
    }

    // make sure this display class
    // is compatible with the remote display class
    if (!rmtClass.isInstance(this)) {
      throw new DisplayException("Cannot construct " + getClass().getName() +
                                 " from remote " + className);
    }

    // put system intrinsic DisplayRealType-s in DisplayRealTypeVector
    for (int i = 0; i < DisplayRealArray.length; i++) {
      DisplayRealTypeVector.addElement(DisplayRealArray[i]);
    }

    displayMonitor = new DisplayMonitorImpl(this);
    displaySync = new DisplaySyncImpl(this);

    if (renderer != null) {
      displayRenderer = renderer;
    }
    else {
      try {
        String name = rmtDpy.getDisplayRendererClassName();
        Object obj = Class.forName(name).newInstance();
        displayRenderer = (DisplayRenderer)obj;
      }
      catch (Exception e) {
        displayRenderer = getDefaultDisplayRenderer();
      }
    }
    displayRenderer.setDisplay(this);

    // initialize ScalarMap's, ShadowDisplayReal's and Control's
    clearMaps();
  }

  // suck in any remote ScalarMaps

  /**
   * 
   *
   * @param rmtDpy 
   */
  void copyScalarMaps(RemoteDisplay rmtDpy) {
    Vector m;
    try {
      m = rmtDpy.getMapVector();
    }
    catch (Exception e) {
      System.err.println("Couldn't copy ScalarMaps");
      return;
    }

    Enumeration me = m.elements();
    while(me.hasMoreElements()) {
      ScalarMap sm = (ScalarMap)me.nextElement();
      try {
        addMap((ScalarMap)sm.clone());
      }
      catch (DisplayException de) {
        try {
          addMap(new ScalarMap(sm.getScalar(), sm.getDisplayScalar()));
        }
        catch (Exception e) {
          System.err.println("Couldn't copy remote ScalarMap " + sm);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * copy ConstantMaps from RemoteDisplay to this
   * @param rmtDpy RemoteDisplay to get ConstantMaps from
   */
  void copyConstantMaps(RemoteDisplay rmtDpy) {
    Vector c;
    try {
      c = rmtDpy.getConstantMapVector();
    }
    catch (Exception e) {
      System.err.println("Couldn't copy ConstantMaps");
      return;
    }

    Enumeration ce = c.elements();
    while(ce.hasMoreElements()) {
      ConstantMap cm = (ConstantMap)ce.nextElement();
      try {
        addMap((ConstantMap)cm.clone());
      }
      catch (DisplayException de) {
        try {
          addMap(new ConstantMap(cm.getConstant(), cm.getDisplayScalar()));
        }
        catch (Exception e) {
          System.err.println("Couldn't copy remote ConstantMap " + cm);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * copy GraphicsModeControl settings from RemoteDisplay to this
   * @param rmtDpy RemoteDisplay to get GraphicsModeControl settings from
   */
  void copyGraphicsModeControl(RemoteDisplay rmtDpy) {
    GraphicsModeControl rc;
    try {
      getGraphicsModeControl().syncControl(rmtDpy.getGraphicsModeControl());
    }
    catch (UnmarshalException ue) {
      System.err.println("Couldn't copy remote GraphicsModeControl");
      return;
    }
    catch (java.rmi.ConnectException ce) {
      System.err.println("Couldn't copy remote GraphicsModeControl");
      return;
    }
    catch (Exception e) {
      e.printStackTrace();
      return;
    }

  }

  /**
   * copy DataReferences from RemoteDisplay to this
   * @param rmtDpy RemoteDisplay to get DataReferences from
   * @param localRefs array of DataReferences: don't get any
   *                  DataReference from rmtDpy that has the same
   *                  name as a DataReference in localRefs
   */
  private void copyRefLinks(RemoteDisplay rmtDpy, DataReference[] localRefs) {

    Vector ml;
    if (rmtDpy == null) return;
    try {
      ml = rmtDpy.getReferenceLinks();
    }
    catch (UnmarshalException ue) {
      System.err.println("Couldn't copy remote DataReferences");
      return;
    }
    catch (java.rmi.ConnectException ce) {
      System.err.println("Couldn't copy remote DataReferences");
      return;
    }
    catch (Exception e) {
      e.printStackTrace();
      return;
    }

    String[] refNames;
    if (localRefs == null) {
      refNames = null;
    }
    else {
      refNames = new String[localRefs.length];
      for (int i = 0; i < refNames.length; i++) {
        try {
          refNames[i] = localRefs[i].getName();
        }
        catch (VisADException ve) {
          refNames[i] = null;
        }
        catch (RemoteException re) {
          refNames[i] = null;
        }
      }
    }

    Enumeration mle = ml.elements();
    if (mle.hasMoreElements()) {

      DataRenderer dr = displayRenderer.makeDefaultRenderer();
      String defaultClass = dr.getClass().getName();

      while(mle.hasMoreElements()) {
        RemoteReferenceLink link = (RemoteReferenceLink)mle.nextElement();

        // get reference to Data object
        RemoteDataReference ref;
        try {
          ref = link.getReference();
        }
        catch (Exception e) {
          System.err.println("Couldn't copy remote DataReference");
          ref = null;
        }

        if (ref != null && refNames != null) {
          String rName;
          try {
            rName = ref.getName();
          }
          catch (VisADException ve) {
            System.err.println("Couldn't get DataReference name");
            rName = null;
          }
          catch (RemoteException re) {
            System.err.println("Couldn't get remote DataReference name");
            rName = null;
          }

          if (rName != null) {
            for (int i = 0; i < refNames.length; i++) {
              if (rName.equals(refNames[i])) {
                ref = null;
                break;
              }
            }
          }
        }

        if (ref != null) {

          // build array of ConstantMap values
          ConstantMap[] cm = null;
          try {
            Vector v = link.getConstantMapVector();
            int len = v.size();
            if (len > 0) {
              cm = new ConstantMap[len];
              for (int i = 0; i < len; i++) {
                cm[i] = (ConstantMap)v.elementAt(i);
              }
            }
          }
          catch (Exception e) {
            System.err.println(
              "Couldn't copy ConstantMaps" + " for remote DataReference");
          }

          // get proper DataRenderer
          DataRenderer renderer;
          try {
            String newClass = link.getRendererClassName();
            if (newClass.equals(defaultClass)) {
              renderer = null;
            }
            else {
              Object obj = Class.forName(newClass).newInstance();
              renderer = (DataRenderer)obj;
            }
          }
          catch (Exception e) {
            System.err.println(
              "Couldn't copy remote DataRenderer name" + "; using " +
              defaultClass);
            renderer = null;
          }

          // build RemoteDisplayImpl to which reference is attached
          try {
            RemoteDisplayImpl rd = new RemoteDisplayImpl(this);

            // if this reference uses the default renderer...
            if (renderer == null) {
              rd.addReference(ref, cm);
            }
            else {
              rd.addReferences(renderer, ref, cm);
            }
          }
          catch (Exception e) {
            System.err.println("Couldn't add remote DataReference " + ref);
          }
        }
      }
    }

  }

  /**
   * copy Data from RemoteDisplay to this
   * @param rmtDpy RemoteDisplay to get Data from
   *
   * @throws RemoteException 
   * @throws VisADException 
   */
  protected void syncRemoteData(RemoteDisplay rmtDpy)
          throws VisADException, RemoteException {
    copyScalarMaps(rmtDpy);
    copyConstantMaps(rmtDpy);
    copyGraphicsModeControl(rmtDpy);
    copyRefLinks(rmtDpy, null);

    notifyAction();

    waitForTasks();

    // only add remote display as listener *after* we've synced
    displayMonitor.addRemoteListener(rmtDpy);
    initializeControls();
  }

  // get current state of all controls from remote display(s)

  /**
   * 
   */
  private void initializeControls() {
    ListIterator iter = ControlVector.listIterator();
    while(iter.hasNext()) {
      try {
        Control ctl = (Control)iter.next();
        ControlMonitorEvent evt;
        evt = new ControlMonitorEvent(MonitorEvent.CONTROL_INIT_REQUESTED,
                                      (Control)ctl.clone());
        displayMonitor.notifyListeners(evt);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /** RemoteDisplayImpl to this for use with remote DisplayListeners */
  private RemoteDisplayImpl rd = null;

  /**
   * Notify this instance's {@link DisplayListener}s.
   *
   * @param  id  type of DisplayEvent that is to be sent
   * @param  x  the horizontal x coordinate for the mouse location in
   *            the display component
   * @param  y  the vertical y coordinate for the mouse location in
   *            the display component
   * @throws VisADException     if a VisAD failure occurs.
   * @throws RemoteException    if a Java RMI failure occurs.
   */
  public void notifyListeners(int id, int x, int y)
          throws VisADException, RemoteException {
    notifyListeners(null, id, x, y);
    //    notifyListeners(new DisplayEvent(this, id, x, y));
  }

  /**
   * Notify this instance's {@link DisplayListener}s.
   *
   * @param evt                 The {@link DisplayEvent} to be passed to the
   *                            {@link DisplayListener}s.
   * @throws VisADException     if a VisAD failure occurs.
   * @throws RemoteException    if a Java RMI failure occurs.
   */
  public void notifyListeners(DisplayEvent evt)
          throws VisADException, RemoteException {

    synchronized (eventStatus) {
      if (!eventStatus[evt.getId()]) return; // ignore disabled events
    }
    notifyListeners(evt, 0, 0, 0);
  }



  /**
   * Notify this instance's {@link DisplayListener}s. This method creates a runnable that actually
   * does the notification. It invokes the runnable directly if its in the Swing event dispatch thread.
   * Else, it invokes the runnable in a separate thread.
   *
   * @param evt                 The {@link DisplayEvent} to be passed to the
   *                            {@link DisplayListener}s. If this is null then construct the
   *                             event from the other parameters
   * @param  id  type of DisplayEvent that is to be sent
   * @param  x  the horizontal x coordinate for the mouse location in
   *            the display component
   * @param  y  the vertical y coordinate for the mouse location in
   *            the display component
   */
  private void notifyListeners(final DisplayEvent evt, final int id,
                               final int x, final int y) {
    Runnable runnable = new Runnable() {
      public void run() {
        try {
          DisplayEvent displayEvent = evt;
          if (displayEvent == null) {
            displayEvent = new DisplayEvent(DisplayImpl.this, id, x, y);
          }
          for (Enumeration listeners =
                  ((Vector)ListenerVector.clone()).elements();
                  listeners.hasMoreElements(); ) {
            DisplayListener listener =
              (DisplayListener)listeners.nextElement();
            if (listener instanceof Remote) {
              if (rd == null) {
                rd = new RemoteDisplayImpl(DisplayImpl.this);
              }
              listener.displayChanged(displayEvent.cloneButDisplay(rd));
            }
            else {
              listener.displayChanged(
                displayEvent.cloneButDisplay(DisplayImpl.this));
            }
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    };

    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    }
    else {
      SwingUtilities.invokeLater(runnable);
    }
  }


  /**
   * add a DisplayListener
   * @param listener DisplayListener to add
   */
  public void addDisplayListener(DisplayListener listener) {
    ListenerVector.addElement(listener);
  }

  /**
   * remove a DisplayListener
   * @param listener DisplayListener to remove
   */
  public void removeDisplayListener(DisplayListener listener) {
    ListenerVector.removeElement(listener);
  }

  /**
   * @return the java.awt.Component (e.g., JPanel or AppletPanel)
   *         this DisplayImpl uses; returns null for an offscreen
   *         DisplayImpl
   */
  public Component getComponent() {
    return component;
  }

  /**
   * set the java.awt.Component this DisplayImpl uses
   * @param c Component to set
   */
  public void setComponent(Component c) {
    if (c != null) {
      // lazy initialization
      if (componentListener == null) {
        componentListener = new ComponentChangedListener(this);
      }
      c.addComponentListener(componentListener);
    }
    // in case setComponent is called multiple times
    if (component != null) {
      if (componentListener != null) {
        component.removeComponentListener(componentListener);
      }
    }
    component = c;
  }

  /**
   * request auto-scaling of ScalarMap ranges the next time
   * Data are transformed into scene graph elements
   */
  public void reAutoScale() {
    initialize = true;
// printStack("reAutoScale");
  }

  /**
   * if auto is true, re-apply auto-scaling of ScalarMap ranges
   * every time Display is triggered
   * @param a flag indicating whether to always re-apply auto-scaling
   */
  public void setAlwaysAutoScale(boolean a) {
    always_initialize = a;
  }

  /**
   * request all linked Data to be re-transformed into scene graph
   * elements
   */
  public void reDisplayAll() {
    redisplay_all = true;
// printStack("reDisplayAll");
    notifyAction();
  }


  // CTR - begin code for slaved displays

  /** Internal list of slaves linked to this display. */
  private Vector Slaves = new Vector();

  /**
   * link a slave display to this
   * @param display RemoteSlaveDisplay to link
   */
  public void addSlave(RemoteSlaveDisplay display) {
    if (!Slaves.contains(display)) Slaves.add(display);
  }

  /**
   * remove a link between a slave display and this
   * @param display RemoteSlaveDisplay to remove
   */
  public void removeSlave(RemoteSlaveDisplay display) {
    if (Slaves.contains(display)) Slaves.remove(display);
  }

  /**
   * remove all links to slave displays
   */
  public void removeAllSlaves() {
    Slaves.removeAllElements();
  }

  /**
   * @return flag indicating whether there are any slave displays
   *         linked to this display
   */
  public boolean hasSlaves() {
    return !Slaves.isEmpty();
  }

  /**
   * update all linked slave displays with the given image
   * @param img BufferedImage to send to all linked slave displays
   */
  public void updateSlaves(BufferedImage img) {
    // extract pixels from image
    int width = img.getWidth();
    int height = img.getHeight();
    int type = img.getType();
    int[] pixels = new int[width * height];
    img.getRGB(0, 0, width, height, pixels, 0, width);

    // encode pixels with RLE
    int[] encoded = Convert.encodeRLE(pixels);

    synchronized (Slaves) {
      // send encoded pixels to each slave
      for (int i = 0; i < Slaves.size(); i++) {
        RemoteSlaveDisplay d = (RemoteSlaveDisplay)Slaves.elementAt(i);
        try {
          d.sendImage(encoded, width, height, type);
        }
        catch (java.rmi.ConnectException exc) {
          // remote slave client has died; remove it from list
          Slaves.remove(i--);
        }
        catch (RemoteException exc) {
        }
      }
    }
  }

  /**
   * update all linked slave display with the given message
   * @param message String to send to all linked slave displays
   */
  public void updateSlaves(String message) {
    synchronized (Slaves) {
      // send message to each slave
      for (int i = 0; i < Slaves.size(); i++) {
        RemoteSlaveDisplay d = (RemoteSlaveDisplay)Slaves.elementAt(i);
        try {
          d.sendMessage(message);
        }
        catch (java.rmi.ConnectException exc) {
          // remote slave client has died; remove it from list
          Slaves.remove(i--);
        }
        catch (RemoteException exc) {
        }
      }
    }
  }

  // CTR - end code for slaved displays


  /** Enabled status flag for each DisplayEvent type. */
  private final boolean[] eventStatus = {
    true, // (not used)
    true, // MOUSE_PRESSED
    true, // FRAME_DONE
    true, // TRANSFORM_DONE
    true, // MOUSE_PRESSED_LEFT
    true, // MOUSE_PRESSED_CENTER
    true, // MOUSE_PRESSED_RIGHT
    true, // MOUSE_RELEASED
    true, // MOUSE_RELEASED_LEFT
    true, // MOUSE_RELEASED_CENTER
    true, // MOUSE_RELEASED_RIGHT
    true, // MAP_ADDED
    true, // MAPS_CLEARED
    true, // REFERENCE_ADDED
    true, // REFERENCE_REMOVED
    true, // DESTROYED
    true, // KEY_PRESSED
    true, // KEY_RELEASED
    false, // MOUSE_DRAGGED
    false, // MOUSE_ENTERED
    false, // MOUSE_EXITED
    false, // MOUSE_MOVED
    false, // WAIT_ON
    false, // WAIT_OFF
    true, // MAP_REMOVED
    false, // COMPONENT_RESIZED
  };

  /**
   * Enables reporting of a DisplayEvent of a given type
   * when it occurs in this display.
   *
   * @param id DisplayEvent type to enable.  Valid types are:
   *          <UL>
   *          <LI>DisplayEvent.FRAME_DONE
   *          <LI>DisplayEvent.TRANSFORM_DONE
   *          <LI>DisplayEvent.MOUSE_PRESSED
   *          <LI>DisplayEvent.MOUSE_PRESSED_LEFT
   *          <LI>DisplayEvent.MOUSE_PRESSED_CENTER
   *          <LI>DisplayEvent.MOUSE_PRESSED_RIGHT
   *          <LI>DisplayEvent.MOUSE_RELEASED_LEFT
   *          <LI>DisplayEvent.MOUSE_RELEASED_CENTER
   *          <LI>DisplayEvent.MOUSE_RELEASED_RIGHT
   *          <LI>DisplayEvent.MAP_ADDED
   *          <LI>DisplayEvent.MAPS_CLEARED
   *          <LI>DisplayEvent.REFERENCE_ADDED
   *          <LI>DisplayEvent.REFERENCE_REMOVED
   *          <LI>DisplayEvent.DESTROYED
   *          <LI>DisplayEvent.KEY_PRESSED
   *          <LI>DisplayEvent.KEY_RELEASED
   *          <LI>DisplayEvent.MOUSE_DRAGGED
   *          <LI>DisplayEvent.MOUSE_ENTERED
   *          <LI>DisplayEvent.MOUSE_EXITED
   *          <LI>DisplayEvent.MOUSE_MOVED
   *          <LI>DisplayEvent.WAIT_ON
   *          <LI>DisplayEvent.WAIT_OFF
   *          <LI>DisplayEvent.MAP_REMOVED
   *          <LI>DisplayEvent.COMPONENT_RESIZED
   *          </UL>
   */
  public void enableEvent(int id) {

    if (id < 1 || id >= eventStatus.length) return;

    synchronized (eventStatus) {
      eventStatus[id] = true;
    }
  }

  /**
   * Disables reporting of a DisplayEvent of a given type
   * when it occurs in this display.
   *
   * @param id DisplayEvent type to disable.  Valid types are:
   *          <UL>
   *          <LI>DisplayEvent.FRAME_DONE
   *          <LI>DisplayEvent.TRANSFORM_DONE
   *          <LI>DisplayEvent.MOUSE_PRESSED
   *          <LI>DisplayEvent.MOUSE_PRESSED_LEFT
   *          <LI>DisplayEvent.MOUSE_PRESSED_CENTER
   *          <LI>DisplayEvent.MOUSE_PRESSED_RIGHT
   *          <LI>DisplayEvent.MOUSE_RELEASED_LEFT
   *          <LI>DisplayEvent.MOUSE_RELEASED_CENTER
   *          <LI>DisplayEvent.MOUSE_RELEASED_RIGHT
   *          <LI>DisplayEvent.MAP_ADDED
   *          <LI>DisplayEvent.MAPS_CLEARED
   *          <LI>DisplayEvent.REFERENCE_ADDED
   *          <LI>DisplayEvent.REFERENCE_REMOVED
   *          <LI>DisplayEvent.DESTROYED
   *          <LI>DisplayEvent.KEY_PRESSED
   *          <LI>DisplayEvent.KEY_RELEASED
   *          <LI>DisplayEvent.MOUSE_DRAGGED
   *          <LI>DisplayEvent.MOUSE_ENTERED
   *          <LI>DisplayEvent.MOUSE_EXITED
   *          <LI>DisplayEvent.MOUSE_MOVED
   *          <LI>DisplayEvent.WAIT_ON
   *          <LI>DisplayEvent.WAIT_OFF
   *          <LI>DisplayEvent.MAP_REMOVED
   *          <LI>DisplayEvent.COMPONENT_RESIZED
   *          </UL>
   */
  public void disableEvent(int id) {

    if (id < 1 || id >= eventStatus.length) return;

    synchronized (eventStatus) {
      eventStatus[id] = false;
    }
  }

  /**
   * @param id DisplayEvent type
   * @return flag indicating whether a DisplayEvent of a given
   *         type is eported when it occurs in this display.
   */
  public boolean isEventEnabled(int id) {

    if (id < 1 || id >= eventStatus.length) {
      return false;
    }
    else {
      synchronized (eventStatus) {
        return eventStatus[id];
      }
    }
  }

  /**
   * Link a reference to this Display.
   * This method may only be invoked after all links to
   * {@link visad.ScalarMap ScalarMaps}
   * have been made.
   *
   * @param ref data reference
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data reference to the remote display.
   */
  public void addReference(ThingReference ref)
          throws VisADException, RemoteException {
    if (!(ref instanceof DataReference)) {
      throw new ReferenceException("DisplayImpl.addReference: ref " +
                                   "must be DataReference");
    }
    if (displayRenderer == null) return;
    addReference((DataReference)ref, null);
  }

  /**
   * Replace remote reference with local reference.
   *
   * @param rDpy Remote display.
   * @param ref Local reference which will replace the previous
   *            reference.
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data reference to the remote display.
   *
   * @see visad.DisplayImpl#addReference(visad.ThingReference)
   */
  public void replaceReference(RemoteDisplay rDpy, ThingReference ref)
          throws VisADException, RemoteException {
    if (!(ref instanceof DataReference)) {
      throw new ReferenceException("DisplayImpl.replaceReference: ref " +
                                   "must be DataReference");
    }
    if (displayRenderer == null) return;
    replaceReference(rDpy, (DataReference)ref, null);
  }

  /**
   * Add a link to a DataReference object
   *
   * @param link The link to the DataReference.
   *
   * @exception VisADException If referenced data is null
   *                           or if a link already exists.
   * @exception RemoteException If a link could not be made
   *                            within the remote display.
   */
  void addLink(DataDisplayLink link) throws VisADException, RemoteException {
    if (displayRenderer == null) return;
    addLink(link, true);
  }

  /**
   * Add a link to a DataReference object
   *
   * @param link The link to the DataReference.
   * @param syncRemote <tt>true</tt> if this is not just
   *                   a local link.
   *
   * @exception VisADException If referenced data is null
   *                           or if a link already exists.
   * @exception RemoteException If a link could not be made
   *                            within the remote display.
   */
  private void addLink(DataDisplayLink link, boolean syncRemote)
          throws VisADException, RemoteException {
    if (displayRenderer == null) return;
// System.out.println("addLink " + getName() + " " +
//                    link.getData().getType()); // IDV
    super.addLink((ReferenceActionLink)link);
    if (syncRemote) {
      notifyListeners(
        new DisplayReferenceEvent(this, DisplayEvent.REFERENCE_ADDED, link));
    }
  }

  /**
   * Link a reference to this Display.
   * <tt>ref</tt> must be a local
   * {@link visad.DataReferenceImpl DataReferenceImpl}.
   * The {@link visad.ConstantMap ConstantMap} array applies only
   * to the rendering reference.
   *
   * @param ref data reference
   * @param constant_maps array of {@link visad.ConstantMap ConstantMaps}
   *                      associated with the data reference
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data reference to the remote display.
   *
   * @see <a href="http://www.ssec.wisc.edu/~billh/guide.html#6.1">Section 6.1 of the Developer's Guide</a>
   */
  public void addReference(DataReference ref, ConstantMap[] constant_maps)
          throws VisADException, RemoteException {
    if (!(ref instanceof DataReferenceImpl)) {
      throw new RemoteVisADException("DisplayImpl.addReference: requires " +
                                     "DataReferenceImpl");
    }
    if (displayRenderer == null) return;
    if (findReference(ref) != null) {
      throw new TypeException("DisplayImpl.addReference: link already exists");
    }
    DataRenderer renderer = displayRenderer.makeDefaultRenderer();
    DataDisplayLink[] links = {new DataDisplayLink(ref, this, this,
                                constant_maps, renderer, getLinkId())};
    addLink(links[0]);
    renderer.setLinks(links, this);
    synchronized (mapslock) {
      RendererVector.addElement(renderer);
    }

    initialize |= computeInitialize();

// printStack("addReference");
    notifyAction();
  }

  /**
   * Replace remote reference with local reference.
   *
   * @param rDpy Remote display.
   * @param ref Local reference which will replace the previous
   *            reference.
   * @param constant_maps array of {@link visad.ConstantMap ConstantMaps}
   *                      associated with the data reference
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data reference to the remote display.
   *
   * @see visad.DisplayImpl#addReference(visad.DataReference, visad.ConstantMap[])
   */
  public void replaceReference(RemoteDisplay rDpy, DataReference ref,
                               ConstantMap[] constant_maps)
          throws VisADException, RemoteException {
    if (displayRenderer == null) return;
    replaceReferences(
      rDpy, null, new DataReference[] {ref}, new ConstantMap[][] {
      constant_maps
    });
  }

  /**
   * decide whether an autoscale is needed 
   *
   * @return 
   */
  private boolean computeInitialize() {
    boolean init = false;
    for (Iterator iter = ((java.util.List)MapVector.clone()).iterator();
            !init && iter.hasNext();
            init |= ((ScalarMap)iter.next()).doInitialize()) {}
    if (!init) {
      AnimationControl control =
        (AnimationControl)getControl(AnimationControl.class);
      if (control != null) {
        init |= (control.getSet() == null && control.getComputeSet());
      }
    }
    return init;
  }

  /**
   * Link a RemoteDataReference to this Display.
   * The {@link visad.ConstantMap ConstantMap} array applies only
   * to the rendering reference.
   * For use by addReference() method of RemoteDisplay that adapts this.
   *
   * @param ref remote data reference
   * @param display RemoteDisplay adapting this
   * @param constant_maps array of {@link visad.ConstantMap ConstantMaps}
   *                      associated with the data reference
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data reference to the remote display.
   *
   * @see <a href="http://www.ssec.wisc.edu/~billh/guide.html#6.1">Section 6.1 of the Developer's Guide</a>
   */
  void adaptedAddReference(RemoteDataReference ref, RemoteDisplay display,
                           ConstantMap[] constant_maps)
          throws VisADException, RemoteException {
    if (findReference(ref) != null) {
      throw new TypeException("DisplayImpl.adaptedAddReference: " +
                              "link already exists");
    }
    if (displayRenderer == null) return;
    DataRenderer renderer = displayRenderer.makeDefaultRenderer();
    DataDisplayLink[] links = {new DataDisplayLink(ref, this, display,
                                constant_maps, renderer, getLinkId())};
    addLink(links[0]);
    renderer.setLinks(links, this);
    synchronized (mapslock) {
      RendererVector.addElement(renderer);
    }

    initialize |= computeInitialize();

// printStack("adaptedAddReference");
    notifyAction();
  }

  /**
   * Link a reference to this Display using a non-default renderer.
   * <tt>ref</tt> must be a local
   * {@link visad.DataReferenceImpl DataReferenceImpl}.
   * This is a method of {@link visad.DisplayImpl DisplayImpl} and
   * {@link visad.RemoteDisplayImpl RemoteDisplayImpl} rather
   * than {@link visad.Display Display}
   *
   * @param renderer logic to render this data
   * @param ref data reference
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data reference to the remote display.
   *
   * @see <a href="http://www.ssec.wisc.edu/~billh/guide.html#6.1">Section 6.1 of the Developer's Guide</a>
   */
  public void addReferences(DataRenderer renderer, DataReference ref)
          throws VisADException, RemoteException {
    addReferences(renderer, new DataReference[] {ref}, null);
  }

  /**
   * Replace remote reference with local reference using
   * non-default renderer.
   *
   * @param rDpy Remote display.
   * @param renderer logic to render this data
   * @param ref Local reference which will replace the previous
   *            reference.
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data reference to the remote display.
   *
   * @see visad.DisplayImpl#addReferences(visad.DataRenderer, visad.DataReference)
   */
  public void replaceReferences(RemoteDisplay rDpy, DataRenderer renderer,
                                DataReference ref)
          throws VisADException, RemoteException {
    replaceReferences(rDpy, renderer, new DataReference[] {ref}, null);
  }

  /**
   * Link a reference to this Display using a non-default renderer.
   * <tt>ref</tt> must be a local
   * {@link visad.DataReferenceImpl DataReferenceImpl}.
   * This is a method of {@link visad.DisplayImpl DisplayImpl} and
   * {@link visad.RemoteDisplayImpl RemoteDisplayImpl} rather
   * than {@link visad.Display Display}
   *
   * @param renderer logic to render this data
   * @param ref data reference
   * @param constant_maps array of {@link visad.ConstantMap ConstantMaps}
   *                      associated with the data reference
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data reference to the remote display.
   *
   * @see <a href="http://www.ssec.wisc.edu/~billh/guide.html#6.1">Section 6.1 of the Developer's Guide</a>
   */
  public void addReferences(DataRenderer renderer, DataReference ref,
                            ConstantMap[] constant_maps)
          throws VisADException, RemoteException {
    addReferences(renderer, new DataReference[] {ref}, new ConstantMap[][] {
      constant_maps
    });
  }

  /**
   * Replace remote reference with local reference using
   * non-default renderer.
   *
   * @param rDpy Remote display.
   * @param renderer logic to render this data
   * @param ref Local reference which will replace the previous
   *            reference.
   * @param constant_maps array of {@link visad.ConstantMap ConstantMaps}
   *                      associated with the data reference
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data reference to the remote display.
   *
   * @see visad.DisplayImpl#addReferences(visad.DataRenderer, visad.DataReference, visad.ConstantMap[])
   */
  public void replaceReferences(RemoteDisplay rDpy, DataRenderer renderer,
                                DataReference ref,
                                ConstantMap[] constant_maps)
          throws VisADException, RemoteException {
    replaceReferences(
      rDpy, renderer, new DataReference[] {ref}, new ConstantMap[][] {
      constant_maps
    });
  }

  /**
   * Link references to this display using a non-default renderer.
   * <tt>refs</tt> must be local
   * {@link visad.DataReferenceImpl DataReferenceImpls}.
   * This is a method of {@link visad.DisplayImpl DisplayImpl} and
   * {@link visad.RemoteDisplayImpl RemoteDisplayImpl} rather
   * than {@link visad.Display Display}
   *
   * @param renderer logic to render this data
   * @param refs array of data references
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data references to the remote display.
   *
   * @see <a href="http://www.ssec.wisc.edu/~billh/guide.html#6.1">Section 6.1 of the Developer's Guide</a>
   */
  public void addReferences(DataRenderer renderer, DataReference[] refs)
          throws VisADException, RemoteException {
    addReferences(renderer, refs, null);
  }

  /**
   * Replace remote references with local references.
   *
   * @param rDpy Remote display.
   * @param renderer logic to render this data
   * @param refs array of local data references
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data references to the remote display.
   *
   * @see visad.DisplayImpl#addReferences(visad.DataRenderer, visad.DataReference[])
   */
  public void replaceReferences(RemoteDisplay rDpy, DataRenderer renderer,
                                DataReference[] refs)
          throws VisADException, RemoteException {
    replaceReferences(rDpy, renderer, refs, null);
  }

  /**
   * Link references to this display using the non-default renderer.
   * <tt>refs</tt> must be local
   * {@link visad.DataReferenceImpl DataReferenceImpls}.
   * The <tt>maps[i]</tt> array applies only to rendering <tt>refs[i]</tt>.
   * This is a method of {@link visad.DisplayImpl DisplayImpl} and
   * {@link visad.RemoteDisplayImpl RemoteDisplayImpl} rather
   * than {@link visad.Display Display}
   *
   * @param renderer logic to render this data
   * @param refs array of data references
   * @param constant_maps array of {@link visad.ConstantMap ConstantMaps}
   *                      associated with data references
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data references to the remote display.
   *
   * @see <a href="http://www.ssec.wisc.edu/~billh/guide.html#6.1">Section 6.1 of the Developer's Guide</a>
   */
  public void addReferences(DataRenderer renderer, DataReference[] refs,
                            ConstantMap[][] constant_maps)
          throws VisADException, RemoteException {
    addReferences(renderer, refs, constant_maps, true);
  }

  /**
   * Link references to this display using the non-default renderer.
   * <tt>refs</tt> must be local
   * {@link visad.DataReferenceImpl DataReferenceImpls}.
   * The <tt>maps[i]</tt> array applies only to rendering <tt>refs[i]</tt>.
   * This is a method of {@link visad.DisplayImpl DisplayImpl} and
   * {@link visad.RemoteDisplayImpl RemoteDisplayImpl} rather
   * than {@link visad.Display Display}
   *
   * @param renderer logic to render this data
   * @param refs array of data references
   * @param constant_maps array of {@link visad.ConstantMap ConstantMaps}
   *                      associated with data references.
   * @param syncRemote <tt>true</tt> if this data should be forwarded
   *                   to the remote display.
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters
   * @exception RemoteException if there was a problem adding the
   *                            data references to the remote display.
   *
   * @see <a href="http://www.ssec.wisc.edu/~billh/guide.html#6.1">Section 6.1 of the Developer's Guide</a>
   */
  private void addReferences(DataRenderer renderer, DataReference[] refs,
                             ConstantMap[][] constant_maps,
                             boolean syncRemote)
          throws VisADException, RemoteException {
    if (displayRenderer == null) return;
    // N.B. This method is called by all replaceReference() methods
    if (refs.length < 1) {
      throw new DisplayException("DisplayImpl.addReferences: must have at " +
                                 "least one DataReference");
    }
    if (constant_maps != null && refs.length != constant_maps.length) {
      throw new DisplayException("DisplayImpl.addReferences: constant_maps " +
                                 "length must match refs length");
    }
    if (!displayRenderer.legalDataRenderer(renderer)) {
      throw new DisplayException("DisplayImpl.addReferences: illegal " +
                                 "DataRenderer class");
    }
    DataDisplayLink[] links = new DataDisplayLink[refs.length];
    for (int i = 0; i < refs.length; i++) {
      if (!(refs[i] instanceof DataReferenceImpl)) {
        throw new RemoteVisADException("DisplayImpl.addReferences: requires " +
                                       "DataReferenceImpl");
      }
      if (findReference(refs[i]) != null) {
        throw new TypeException("DisplayImpl.addReferences: link already exists");
      }
      if (constant_maps == null) {
        links[i] = new DataDisplayLink(refs[i], this, this, null, renderer,
                                       getLinkId());
      }
      else {
        links[i] = new DataDisplayLink(refs[i], this, this, constant_maps[i],
                                       renderer, getLinkId());
      }
      addLink(links[i], syncRemote);
    }
    renderer.setLinks(links, this);
    synchronized (mapslock) {
      RendererVector.addElement(renderer);
    }

    initialize |= computeInitialize();

// printStack("addReferences");
    notifyAction();
  }

  /**
   * Replace remote references with local references.
   *
   * @param rDpy Remote display.
   * @param renderer logic to render this data
   * @param refs array of data references
   * @param constant_maps array of {@link visad.ConstantMap ConstantMaps}
   *                      associated with data references.
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters.
   * @exception RemoteException if there was a problem adding the
   *                            data references to the remote display.
   *
   * @see visad.DisplayImpl#addReferences(visad.DataRenderer, visad.DataReference[], visad.ConstantMap[][])
   */
  public void replaceReferences(RemoteDisplay rDpy, DataRenderer renderer,
                                DataReference[] refs,
                                ConstantMap[][] constant_maps)
          throws VisADException, RemoteException {
    if (displayRenderer == null) return;
    if (renderer == null) {
      renderer = displayRenderer.makeDefaultRenderer();
    }

    removeAllReferences();
    addReferences(renderer, refs, constant_maps, false);
    copyRefLinks(rDpy, refs);
  }

  /**
   * Link references to this display using the non-default renderer.
   * <tt>refs</tt> may be a mix of local
   * {@link visad.DataReferenceImpl DataReferenceImpls} and
   * {@link visad.RemoteDataReference RemoteDataReferences}.
   * The <tt>maps[i]</tt> array applies only to rendering <tt>refs[i]</tt>.
   * For use by addReferences() method of RemoteDisplay that adapts this.
   *
   * @param renderer logic to render this data
   * @param refs array of data references
   * @param display RemoteDisplay adapting this
   * @param constant_maps array of {@link visad.ConstantMap ConstantMaps}
   *                      associated with data references.
   *
   * @exception VisADException if there was a problem with one or more
   *                           parameters
   * @exception RemoteException if there was a problem adding the
   *                            data references to the remote display.
   *
   * @see <a href="http://www.ssec.wisc.edu/~billh/guide.html#6.1">Section 6.1 of the Developer's Guide</a>
   */
  void adaptedAddReferences(DataRenderer renderer, DataReference[] refs,
                            RemoteDisplay display,
                            ConstantMap[][] constant_maps)
          throws VisADException, RemoteException {
    if (displayRenderer == null) return;
    if (refs.length < 1) {
      throw new DisplayException("DisplayImpl.addReferences: must have at " +
                                 "least one DataReference");
    }
    if (constant_maps != null && refs.length != constant_maps.length) {
      throw new DisplayException("DisplayImpl.addReferences: constant_maps " +
                                 "length must match refs length");
    }
    if (!displayRenderer.legalDataRenderer(renderer)) {
      throw new DisplayException("DisplayImpl.addReferences: illegal " +
                                 "DataRenderer class");
    }
    DataDisplayLink[] links = new DataDisplayLink[refs.length];
    for (int i = 0; i < refs.length; i++) {
      if (findReference(refs[i]) != null) {
        throw new TypeException("DisplayImpl.addReferences: link already exists");
      }
      if (refs[i] instanceof DataReferenceImpl) {
        // refs[i] is local
        if (constant_maps == null) {
          links[i] = new DataDisplayLink(refs[i], this, this, null, renderer,
                                         getLinkId());
        }
        else {
          links[i] = new DataDisplayLink(refs[i], this, this,
                                         constant_maps[i], renderer,
                                         getLinkId());
        }
      }
      else {
        // refs[i] is remote
        if (constant_maps == null) {
          links[i] = new DataDisplayLink(refs[i], this, display, null,
                                         renderer, getLinkId());
        }
        else {
          links[i] = new DataDisplayLink(refs[i], this, display,
                                         constant_maps[i], renderer,
                                         getLinkId());
        }
      }
      addLink(links[i]);
    }
    renderer.setLinks(links, this);
    synchronized (mapslock) {
      RendererVector.addElement(renderer);
    }

    initialize |= computeInitialize();

// printStack("adaptedAddReferences");
    notifyAction();
  }

  /**
   * remove link to ref, which must be a local DataReferenceImpl;
   * if ref was added as part of a DataReference  array passed to
   * addReferences(), remove links to all of them
   * @param ref ThingReference to remove
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void removeReference(ThingReference ref)
          throws VisADException, RemoteException {
    if (!(ref instanceof DataReferenceImpl)) {
      throw new RemoteVisADException("ActionImpl.removeReference: requires " +
                                     "DataReferenceImpl");
    }
    adaptedDisplayRemoveReference((DataReference)ref);
    notifyListeners(new DisplayEvent(this, DisplayEvent.REFERENCE_REMOVED));
  }

  /**
   * remove DataDisplayLinks from this DisplayImpl
   * @param links array of DataDisplayLinks to remove
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void removeLinks(DataDisplayLink[] links)
          throws RemoteException, VisADException {
    if (displayRenderer == null) return;
    for (int i = links.length - 1; i >= 0; i--) {
      if (links[i] != null) {
        links[i].clearMaps();
      }
    }

    super.removeLinks(links);
  }

  /**
   * remove link to a DataReference;
   * uses by removeReference() method of RemoteActionImpl that
   * adapts this ActionImpl;
   * because DataReference array input to adaptedAddReferences
   * may be a mix of local and remote, we tolerate either here
   * @param ref DataReference to remove
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void adaptedDisplayRemoveReference(DataReference ref)
          throws VisADException, RemoteException {
    if (displayRenderer == null) return;
    DataDisplayLink link = (DataDisplayLink)findReference(ref);
    // don't throw an Exception if link is null: users may try to
    // remove all DataReferences added by a call to addReferences
    if (link == null) return;
    DataRenderer renderer = link.getRenderer();
    DataDisplayLink[] links = renderer.getLinks();
    synchronized (mapslock) {
      renderer.clearAVControls();
      renderer.clearScene();
      RendererVector.removeElement(renderer);
    }
    removeLinks(links);
  }

  /**
   * remove all links to DataReferences.
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void removeAllReferences() throws VisADException, RemoteException {

    if (displayRenderer == null) return;
    Vector temp = (Vector)RendererVector.clone();

    synchronized (mapslock) {
      Iterator renderers = temp.iterator();
      while(renderers.hasNext()) {
        DataRenderer renderer = (DataRenderer)renderers.next();
        renderer.clearAVControls();
        DataDisplayLink[] links = renderer.getLinks();
        renderers.remove();
        removeLinks(links);
        renderer.clearScene();
      }
      RendererVector.removeAllElements();

      initialize = true;
// printStack("removeAllReferences");
      
      notifyListeners(new DisplayEvent(this, DisplayEvent.REFERENCE_REMOVED));
    }
  }

  /**
   * trigger possible re-transform of linked Data
   * used by Controls to notify this DisplayImpl that they
   * have changed
   */
  public void controlChanged() {
    notifyAction();
  }

  /**
   * over-ride ActionImpl.checkTicks() to always return true,
   * since DisplayImpl always runs doAction to find out if any
   * linked Data needs to be re-transformed
   * @return true
   */
  public boolean checkTicks() {
    return true;
  }

  /**
   * has this display been destroyed
   *
   * @return  has this display been destroyed
   */
  public boolean isDestroyed() {
    return destroyed;
  }

  /**
   * destroy this display: clear all references to objects
   * (so they can be garbage collected), stop all Threads
   * and remove all links
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void destroy() throws VisADException, RemoteException {
    if (destroyed) return;
    destroyed = true;
    VisADException thrownVE = null;
    RemoteException thrownRE = null;

    if (mapslock == null) mapslock = new Object();
    synchronized (mapslock) {
      stop();

      if (displayActivity != null) {
        displayActivity.destroy();
      }

      // tell everybody we're going away
      notifyListeners(new DisplayEvent(this, DisplayEvent.DESTROYED));

      // remove all listeners
      synchronized (ListenerVector) {
        ListenerVector.removeAllElements();
      }

      try {
        removeAllReferences();
      }
      catch (RemoteException re) {
        thrownRE = re;
      }
      catch (VisADException ve) {
        thrownVE = ve;
      }

      try {
        clearMaps();
      }
      catch (RemoteException re) {
        thrownRE = re;
      }
      catch (VisADException ve) {
        thrownVE = ve;
      }

      AnimationControl control =
        (AnimationControl)getControl(AnimationControl.class);
      if (control != null) {
        control.stop();
      }

      if (thrownVE != null) {
        throw thrownVE;
      }
      if (thrownRE != null) {
        throw thrownRE;
      }

      // get rid of dangling references
      /* done in clearMaps()
          verify (RendererVector == null)
          MapVector.removeAllElements();
          ConstantMapVector.removeAllElements();
          RealTypeVector.removeAllElements();
      */
      DisplayRealTypeVector.removeAllElements();
      ControlVector.removeAllElements();
      RendererSourceListeners.removeAllElements();
      RmtSrcListeners.removeAllElements();
      MessageListeners.removeAllElements();
      ListenerVector.removeAllElements();
      Slaves.removeAllElements();
      displayRenderer = null; // this disables most DisplayImpl methods
      if (component != null) {
        component.removeComponentListener(componentListener);
      }
      componentListener = null;
      component = null;
      mouse = null;
      displayMonitor = null;
      displaySync = null;
      displayActivity = null;
      printer = null;
      rd = null;
      widgetPanel = null;
    } // end synchronized (mapslock)
  }

  /**
   * Check if any Data need re-transform, and if so, do it.
   * Check if auto-scaling is needed for any ScalarMaps, and
   * if so, do it. This method does the real work of DisplayImpl.
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void doAction() throws VisADException, RemoteException {

    if (displayRenderer == null) return;
    if (mapslock == null) return;
    // put a try/finally block around the setWaitFlag(true), so that we unset
    // the flag before exiting even if an Exception or Error is thrown
    try {
// System.out.println("DisplayImpl call setWaitFlag(true)");
      displayRenderer.setWaitFlag(true);
      synchronized (mapslock) {
        if (RendererVector == null || displayRenderer == null) {
// System.out.println("DisplayImpl call setWaitFlag(false)");
          if (displayRenderer != null) displayRenderer.setWaitFlag(false);
          return;
        }
        // set tickFlag-s in changed Control-s
        // clone MapVector to avoid need for synchronized access
        Vector tmap = (Vector)MapVector.clone();
        Enumeration maps = tmap.elements();
        while(maps.hasMoreElements()) {
          ScalarMap map = (ScalarMap)maps.nextElement();
          map.setTicks();
        }

        // set ScalarMap.valueIndex-s and valueArrayLength
        int n = getDisplayScalarCount();
        int[] scalarToValue = new int[n];
        for (int i = 0; i < n; i++)
          scalarToValue[i] = -1;
        valueArrayLength = 0;
        maps = tmap.elements();
        while(maps.hasMoreElements()) {
          ScalarMap map = ((ScalarMap)maps.nextElement());
          DisplayRealType dreal = map.getDisplayScalar();
          map.setValueIndex(valueArrayLength);
          valueArrayLength++;
        }

        // set valueToScalar and valueToMap arrays
        valueToScalar = new int[valueArrayLength];
        valueToMap = new int[valueArrayLength];
        for (int i = 0; i < tmap.size(); i++) {
          ScalarMap map = (ScalarMap)tmap.elementAt(i);
          DisplayRealType dreal = map.getDisplayScalar();
          valueToScalar[map.getValueIndex()] = getDisplayScalarIndex(dreal);
          valueToMap[map.getValueIndex()] = i;
        }

        // invoke each DataRenderer (to prepare associated Data objects
        // for transformation)
        // clone RendererVector to avoid need for synchronized access
        Vector temp = ((Vector)RendererVector.clone());
        Enumeration renderers = temp.elements();
        boolean go = false;
        if (initialize) {
          renderers = temp.elements();
          while(!go && renderers.hasMoreElements()) {
            DataRenderer renderer = (DataRenderer)renderers.nextElement();
            go |= renderer.checkAction();
          }
        }
/*
System.out.println("initialize = " + initialize + " go = " + go +
                     " redisplay_all = " + redisplay_all);
*/
        if (redisplay_all) {
          go = true;
// System.out.println("redisplay_all = " + redisplay_all + " go = " + go);
          redisplay_all = false;
        }

        if (!initialize || go) {
          boolean lastinitialize = initialize;
          displayRenderer.prepareAction(temp, tmap, go, initialize);

          // WLH 10 May 2001
          boolean anyBadMap = false;
          maps = tmap.elements();
          while(maps.hasMoreElements()) {
            ScalarMap map = ((ScalarMap)maps.nextElement());
            if (map.badRange()) {
              anyBadMap = true;
              // System.out.println("badRange " + map);
            }
          }

          renderers = temp.elements();
          boolean badScale = false;
          while(renderers.hasMoreElements()) {
            DataRenderer renderer = (DataRenderer)renderers.nextElement();
            boolean badthis = renderer.getBadScale(anyBadMap);
            badScale |= badthis;
/*
            if (badthis) {
              DataDisplayLink[] links = renderer.getLinks();
              System.out.println("badthis " +
                                 links[0].getThingReference().getName());
            }
*/
          }
          initialize = badScale;
          if (always_initialize) initialize = true;

          if (initialize && !lastinitialize) {
            displayRenderer.prepareAction(temp, tmap, go, initialize);
          }

          boolean transform_done = false;

// System.out.println("DisplayImpl.doAction transform");
// int i = 0;
          boolean any_exceptions = false;
          renderers = temp.elements();
          while(renderers.hasMoreElements()) {
// System.out.println("DisplayImpl invoke renderer.doAction " + i);
// i++;
            DataRenderer renderer = (DataRenderer)renderers.nextElement();

            boolean this_transform = renderer.doAction();
            transform_done |= this_transform;
            any_exceptions |= !renderer.getExceptionVector().isEmpty();
/*
            if (this_transform) {
              DataDisplayLink[] links = renderer.getLinks();
              System.out.println("transform " + getName() + " " +
                                 links[0].getThingReference().getName());
            }
*/
          }
          if (transform_done) {
// System.out.println(getName() + " invoked " + i + " renderers");
            AnimationControl control =
              (AnimationControl)getControl(AnimationControl.class);
            if (control != null) {
              control.init();
            }
            synchronized (ControlVector) {
              Enumeration controls = ControlVector.elements();
              while(controls.hasMoreElements()) {
                Control cont = (Control)controls.nextElement();
                if (ValueControl.class.isInstance(cont)) {
                  ((ValueControl)cont).init();
                }
              }
            }
          }

          if (transform_done || any_exceptions) {
            notifyListeners(DisplayEvent.TRANSFORM_DONE, 0, 0);
          }

        }

        // clear tickFlag-s in Control-s
        maps = tmap.elements();
        while(maps.hasMoreElements()) {
          ScalarMap map = (ScalarMap)maps.nextElement();
          map.resetTicks();
        }
      } // end synchronized (mapslock)
    }
    finally {
// System.out.println("DisplayImpl call setWaitFlag(false)");
      if (displayRenderer != null) displayRenderer.setWaitFlag(false);
    }

  }

  /**
   * @return the default DisplayRenderer for this DisplayImpl
   */
  protected abstract DisplayRenderer getDefaultDisplayRenderer();

  /**
   * @return the DisplayRenderer associated with this DisplayImpl
   */
  public DisplayRenderer getDisplayRenderer() {
    return displayRenderer;
  }

  /**
   * Returns a clone of the list of DataRenderer-s.  A clone is returned
   * to avoid concurrent access problems by the Display thread.
   * @return                    A clone of the list of DataRenderer-s.
   * @see #getRenderers()
   */
  public Vector getRendererVector() {
    return (Vector)RendererVector.clone();
  }

  /**
   * @return the number of DisplayRealTypes in ScalarMaps
   *         linked to this DisplayImpl
   */
  public int getDisplayScalarCount() {
    return DisplayRealTypeVector.size();
  }

  /**
   * get the DisplayRealType with the given index
   * @param index index into Vector of DisplayRealTypes
   * @return the indexed DisplayRealType
   */
  public DisplayRealType getDisplayScalar(int index) {
    return (DisplayRealType)DisplayRealTypeVector.elementAt(index);
  }

  /**
   * get the index for the given DisplayRealType
   * @param dreal DisplayRealType to search for
   * @return the index of dreal in Vector of DisplayRealTypes
   */
  public int getDisplayScalarIndex(DisplayRealType dreal) {
    int dindex;
    synchronized (DisplayRealTypeVector) {
      DisplayTupleType tuple = dreal.getTuple();
      if (tuple != null) {
        int n = tuple.getDimension();
        for (int i = 0; i < n; i++) {
          try {
            DisplayRealType ereal = (DisplayRealType)tuple.getComponent(i);
            int eindex = DisplayRealTypeVector.indexOf(ereal);
            if (eindex < 0) {
              DisplayRealTypeVector.addElement(ereal);
            }
          }
          catch (VisADException e) {
          }
        }
      }
      dindex = DisplayRealTypeVector.indexOf(dreal);
      if (dindex < 0) {
        DisplayRealTypeVector.addElement(dreal);
        dindex = DisplayRealTypeVector.indexOf(dreal);
      }
    }
    return dindex;
  }

  /**
   * @return the number of ScalarTypes in ScalarMaps
   *         linked to this DisplayImpl
   */
  public int getScalarCount() {
    return RealTypeVector.size();
  }

  /**
   * get the ScalarType with the given index
   * @param index index into Vector of ScalarTypes
   * @return the indexed ScalarType
   */
  public ScalarType getScalar(int index) {
    return (ScalarType)RealTypeVector.elementAt(index);
  }

  /**
   * get the index for the given ScalarType
   * @param real ScalarType to search for
   * @return the index of real in Vector of ScalarTypes
   * @throws RemoteException an RMI error occurred
   */
  public int getScalarIndex(ScalarType real) throws RemoteException {
    return RealTypeVector.indexOf(real);
  }

  /**
   * add a ScalarMap to this Display, assuming a local source
   * @param map ScalarMap to add
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void addMap(ScalarMap map) throws VisADException, RemoteException {
    addMap(map, VisADEvent.LOCAL_SOURCE);
  }

  /**
   * add a ScalarMap to this Display
   * @param map ScalarMap to add
   * @param remoteId remote source for collab, or VisADEvent.LOCAL_SOURCE
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void addMap(ScalarMap map, int remoteId)
          throws VisADException, RemoteException {

    if (displayRenderer == null) return;
    synchronized (mapslock) {
      int index;
      if (!RendererVector.isEmpty()) {
        ScalarType st = map.getScalar();
        if (st != null) {
          Vector temp = (Vector)RendererVector.clone();
          Iterator renderers = temp.iterator();
          while(renderers.hasNext()) {
            DataRenderer renderer = (DataRenderer)renderers.next();
            DataDisplayLink[] links = renderer.getLinks();
            for (int i = 0; i < links.length; i++) {
              if (MathType.findScalarType(links[i].getType(), st)) {
/* WLH relax addMap() & clearMap() 17 Dec 2002
                throw new DisplayException("DisplayImpl.addMap(): " +
                            "ScalarType may not occur in any DataReference");
*/
                DataReference ref = links[i].getDataReference();
                if (ref != null) ref.incTick();
              }
            }
          }
        }
      }
      DisplayRealType type = map.getDisplayScalar();
      if (!displayRenderer.legalDisplayScalar(type)) {
        throw new BadMappingException("DisplayImpl.addMap: " +
                                      map.getDisplayScalar() +
                                      " illegal for this DisplayRenderer");
      }
      if ((Display.LineWidth.equals(type) ||
           Display.PointSize.equals(type) ||
           Display.PointMode.equals(type) ||
           Display.LineStyle.equals(type) ||
           Display.TextureEnable.equals(type) ||
           Display.MissingTransparent.equals(type) ||
           Display.PolygonMode.equals(type) ||
           Display.CurvedSize.equals(type) ||
           Display.ColorMode.equals(type) ||
           Display.PolygonOffset.equals(type) ||
           Display.PolygonOffsetFactor.equals(type) ||
           Display.AdjustProjectionSeam.equals(type) ||
           Display.Texture3DMode.equals(type) ||
           Display.CacheAppearances.equals(type) ||
           Display.MergeGeometries.equals(type)) && !(map
           instanceof ConstantMap)) {
        throw new BadMappingException("DisplayImpl.addMap: " +
                                      map.getDisplayScalar() +
                                      " for ConstantMap only");
      }
// System.out.println("addMap " + getName() + " " + map.getScalar() +
//                    " -> " + map.getDisplayScalar()); // IDV
      map.setDisplay(this);

      if (map instanceof ConstantMap) {
        synchronized (ConstantMapVector) {
          Enumeration maps = ConstantMapVector.elements();
          while(maps.hasMoreElements()) {
            ConstantMap map2 = (ConstantMap)maps.nextElement();
            if (map2.getDisplayScalar().equals(map.getDisplayScalar())) {
              throw new BadMappingException("Display.addMap: two ConstantMaps " +
                                            "have the same DisplayScalar");
            }
          }
          ConstantMapVector.addElement(map);
        }
        if (!RendererVector.isEmpty()) {
          reDisplayAll(); // WLH 2 April 2002
        }
      }
      else { // !(map instanceof ConstantMap)
        // add to RealTypeVector and set ScalarIndex
        ScalarType real = map.getScalar();
        DisplayRealType dreal = map.getDisplayScalar();
        synchronized (MapVector) {
          Enumeration maps = MapVector.elements();
          while(maps.hasMoreElements()) {
            ScalarMap map2 = (ScalarMap)maps.nextElement();
            if (real.equals(map2.getScalar()) &&
                dreal.equals(map2.getDisplayScalar()) &&
                !dreal.equals(Display.Shape)) {
              throw new BadMappingException("Display.addMap: two ScalarMaps " +
                      "with the same RealType & DisplayRealType");
            }
            if (dreal.equals(Display.Animation) &&
                map2.getDisplayScalar().equals(Display.Animation)) {
              throw new BadMappingException("Display.addMap: two RealTypes " +
                                            "are mapped to Animation");
            }
          }
          MapVector.addElement(map);
          needWidgetRefresh = true;
        }
        synchronized (RealTypeVector) {
          index = RealTypeVector.indexOf(real);
          if (index < 0) {
            RealTypeVector.addElement(real);
            index = RealTypeVector.indexOf(real);
          }
        }
        map.setScalarIndex(index);
        map.setControl();
        // WLH 18 June 2002
        if (!RendererVector.isEmpty() && map.doInitialize()) {
          reAutoScale();
        }
      } // end !(map instanceof ConstantMap)
      addDisplayScalar(map);
      notifyListeners(
        new DisplayMapEvent(this, DisplayEvent.MAP_ADDED, map, remoteId));

      // make sure we monitor all changes to this ScalarMap
      map.addScalarMapListener(displayMonitor);
    }

  }

  /**
   * remove a ScalarMap from this Display, assuming a local source
   * @param map ScalarMap to remove
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void removeMap(ScalarMap map)
          throws VisADException, RemoteException {
    removeMap(map, VisADEvent.LOCAL_SOURCE);
  }

  /**
   * remove a ScalarMap from this Display
   * @param map ScalarMap to add
   * @param remoteId remote source for collab, or VisADEvent.LOCAL_SOURCE
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void removeMap(ScalarMap map, int remoteId)
          throws VisADException, RemoteException {

    if (displayRenderer == null) return;
// System.out.println("removeMap " + getName() + " " + map.getScalar() +
//                    " -> " + map.getDisplayScalar()); // IDV
    synchronized (mapslock) {
      // can have multiple equals() maps to Shape, so test for ==
      int index = MapVector.indexOf(map);
      while(index >= 0 && map != MapVector.elementAt(index)) {
        index = MapVector.indexOf(map, index + 1);
      }
      if (index < 0) {
        throw new BadMappingException("Display.removeMap: " + map + " not " +
                                      "in Display " + getName());
      }

      //Remove the control from the ControlVector
      Control control = map.getControl();
      synchronized (ControlVector) {
        if (control != null && ControlVector.contains(control)) {
          ControlVector.remove(control);
          control.removeControlListener((ControlListener)displayMonitor);
          control.setInstanceNumber(-1);
          control.setIndex(-1);
          for (int i = 0; i < ControlVector.size(); i++) {
            Control ctl = (Control)ControlVector.get(i);
            ctl.setIndex(i);
          }
        }
      }

      MapVector.removeElementAt(index);
      ScalarType real = map.getScalar();
      if (real != null) {
        Enumeration maps = MapVector.elements();
        boolean any = false;
        while(maps.hasMoreElements()) {
          ScalarMap map2 = (ScalarMap)maps.nextElement();
          if (real.equals(map2.getScalar())) any = true;
        }
        if (!any) {
          // if real is not used by any other ScalarMap, remove it
          // and adjust ScalarIndex of all other ScalarMaps
          RealTypeVector.removeElement(real);

          maps = MapVector.elements();
          while(maps.hasMoreElements()) {
            ScalarMap map2 = (ScalarMap)maps.nextElement();
            ScalarType real2 = map2.getScalar();
            int index2 = RealTypeVector.indexOf(real2);
            if (index2 < 0) {
              throw new BadMappingException("Display.removeMap: impossible 1");
            }
            map2.setScalarIndex(index2);
          }
        } // end if (!any)
      } // end if (real != null)

      // trigger events
      if (map instanceof ConstantMap) {
        if (!RendererVector.isEmpty()) {
          reDisplayAll();
        }
      }
      else { // !(map instanceof ConstantMap)
        if (!RendererVector.isEmpty()) {
          ScalarType st = map.getScalar();
          if (st != null) { // not necessary for !(map instanceof ConstantMap)
            Vector temp = (Vector)RendererVector.clone();
            Iterator renderers = temp.iterator();
            while(renderers.hasNext()) {
              DataRenderer renderer = (DataRenderer)renderers.next();
              DataDisplayLink[] links = renderer.getLinks();
              for (int i = 0; i < links.length; i++) {
                if (MathType.findScalarType(links[i].getType(), st)) {
                  DataReference ref = links[i].getDataReference();
                  if (ref != null) ref.incTick();
                }
              }
            }
          }
        }
        // add DRM 2003-02-21
        if (map.getAxisScale() != null) {
          DisplayRenderer displayRenderer = getDisplayRenderer();
          displayRenderer.clearScale(map.getAxisScale());

          Enumeration maps = MapVector.elements();
          while(maps.hasMoreElements()) {
            ScalarMap map2 = (ScalarMap)maps.nextElement();
            AxisScale axisScale = map2.getAxisScale();
            if (axisScale != null) {
              displayRenderer.clearScale(axisScale);
              axisScale.setAxisOrdinal(-1);
            }
          }
          maps = MapVector.elements();
          while(maps.hasMoreElements()) {
            ScalarMap map2 = (ScalarMap)maps.nextElement();
            AxisScale axisScale = map2.getAxisScale();
            if (axisScale != null) {
              map2.makeScale();
            }
          }


        }
        needWidgetRefresh = true;
      } // end !(map instanceof ConstantMap)
      notifyListeners(
        new DisplayMapEvent(this, DisplayEvent.MAP_REMOVED, map, remoteId));
      map.nullDisplay(); // ??
    } // end synchronized (mapslock)

  }

  /**
   * add a ScalarType from a ScalarMap from this Display
   * @param map ScalarMap whose ScalarType to add
   */
  void addDisplayScalar(ScalarMap map) {
    int index;

    if (displayRenderer == null) return;
    DisplayRealType dreal = map.getDisplayScalar();
    synchronized (DisplayRealTypeVector) {
      DisplayTupleType tuple = dreal.getTuple();
      if (tuple != null) {
        int n = tuple.getDimension();
        for (int i = 0; i < n; i++) {
          try {
            DisplayRealType ereal = (DisplayRealType)tuple.getComponent(i);
            int eindex = DisplayRealTypeVector.indexOf(ereal);
            if (eindex < 0) {
              DisplayRealTypeVector.addElement(ereal);
            }
          }
          catch (VisADException e) {
          }
        }
      }
      index = DisplayRealTypeVector.indexOf(dreal);
      if (index < 0) {
        DisplayRealTypeVector.addElement(dreal);
        index = DisplayRealTypeVector.indexOf(dreal);
      }
    }
    map.setDisplayScalarIndex(index);
  }

  /**
   * remove all ScalarMaps linked this display;
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void clearMaps() throws VisADException, RemoteException {
    if (displayRenderer == null) return;
// System.out.println("clearMaps " + getName() + "\n"); // IDV
    synchronized (mapslock) {
      if (!RendererVector.isEmpty()) {
/* WLH relax addMap() & clearMap() 17 Dec 2002
        throw new DisplayException("DisplayImpl.clearMaps: RendererVector " +
                                   "must be empty");
*/
        reDisplayAll();
      }

      Enumeration maps;

      synchronized (MapVector) {
        maps = MapVector.elements();
        while(maps.hasMoreElements()) {
          ScalarMap map = (ScalarMap)maps.nextElement();
          map.nullDisplay();
          map.removeScalarMapListener(displayMonitor);
        }
        MapVector.removeAllElements();
        needWidgetRefresh = true;
      }
      synchronized (ConstantMapVector) {
        maps = ConstantMapVector.elements();
        while(maps.hasMoreElements()) {
          ConstantMap map = (ConstantMap)maps.nextElement();
          map.nullDisplay();
          map.removeScalarMapListener(displayMonitor);
        }
        ConstantMapVector.removeAllElements();
      }

      synchronized (ControlVector) {
        // clear Control-s associated with this Display
        maps = ControlVector.elements();
        while(maps.hasMoreElements()) {
          Control ctl = (Control)maps.nextElement();
          ctl.removeControlListener((ControlListener)displayMonitor);
          ctl.setInstanceNumber(-1);
        }
        ControlVector.removeAllElements();
        // one each GraphicsModeControl and ProjectionControl always exists
        Control control = (Control)getGraphicsModeControl();
        if (control != null) addControl(control);
        control = (Control)getProjectionControl();
        if (control != null) addControl(control);
        // don't forget RendererControl
        control = (Control)displayRenderer.getRendererControl();
        if (control != null) addControl(control);
      }
      // clear RealType-s from RealTypeVector
      // removeAllElements is synchronized
      RealTypeVector.removeAllElements();
      synchronized (DisplayRealTypeVector) {
        // clear DisplayRealType-s from DisplayRealTypeVector
        DisplayRealTypeVector.removeAllElements();
        // put system intrinsic DisplayRealType-s in DisplayRealTypeVector
        for (int i = 0; i < DisplayRealArray.length; i++) {
          DisplayRealTypeVector.addElement(DisplayRealArray[i]);
        }
      }
      displayRenderer.clearAxisOrdinals();
      displayRenderer.setAnimationString(new String[] {null, null});
    }

    notifyListeners(new DisplayEvent(this, DisplayEvent.MAPS_CLEARED));
  }

  /**
   * @return clone of Vector of ScalarMaps linked to this DisplayImpl
   *         (doesn't include ConstantMaps)
   */
  public Vector getMapVector() {
    return (Vector)MapVector.clone();
  }

  /**
   * @return clone of Vector of ConstantMaps linked to this DisplayImpl
   */
  public Vector getConstantMapVector() {
    return (Vector)ConstantMapVector.clone();
  }

  /**
   * Get the instance number of this <CODE>Control</CODE>
   * in the internal <CODE>ControlVector</CODE>.
   *
   * @param ctl <CODE>Control</CODE> to look for.
   *
   * @return Instance number (<CODE>-1</CODE> if not found.)
   */
  private int getInstanceNumber(Control ctl) {
    Class ctlClass = ctl.getClass();
    int num = 0;
    Enumeration en = ControlVector.elements();
    while(en.hasMoreElements()) {
      Control c = (Control)en.nextElement();
      if (ctlClass.isInstance(c)) {
        if (ctl == c) {
          return num;
        }
        num++;
      }
    }

    return -1;
  }

  /**
   * Return the ID used to identify the collaborative connection to
   * the specified remote display.<br>
   * <br>
   * <b>WARNING!</b>  Due to limitations in the Java RMI implementation,
   * this only works with an exact copy of the RemoteDisplay used to
   * create the collaboration link.
   *
   * @param rmtDpy the specified remote display.
   * @return <tt>DisplayMonitor.UNKNOWN_LISTENER_ID</tt> if not found;
   *         otherwise, returns the ID.
   * @throws RemoteException an RMI error occurred
   */
  public int getConnectionID(RemoteDisplay rmtDpy) throws RemoteException {
    if (displayMonitor == null) return DisplayMonitor.UNKNOWN_LISTENER_ID;
    return displayMonitor.getConnectionID(rmtDpy);
  }

  /**
   * add a Control to this DisplayImpl
   * @param control Control to add
   */
  public void addControl(Control control) {
    if (displayRenderer == null) return;
    if (control != null && !ControlVector.contains(control)) {
      ControlVector.addElement(control);
      control.setIndex(ControlVector.indexOf(control));
      control.setInstanceNumber(getInstanceNumber(control));
      control.addControlListener((ControlListener)displayMonitor);
    }
  }

  /**
   * get a linked Control with the given Class;
   * only called for Control objects associated with 'single'
   * DisplayRealTypes
   * @param c sub-Class of Control to search for
   * @return linked Control with Class c, or null
   */
  public Control getControl(Class c) {
    return getControl(c, 0);
  }

  /**
   * get ordinal instance of linked Control object of the
   * specified class
   * @param c sub-Class of Control to search for
   * @param inst ordinal instance number
   * @return linked Control with Class c, or null
   */
  public Control getControl(Class c, int inst) {
    return getControls(c, null, inst);
  }

  /**
   * get all linked Control objects of the specified Class
   * @param c sub-Class of Control to search for
   * @return Vector of linked Controls with Class c
   */
  public Vector getControls(Class c) {
    Vector v = new Vector();
    getControls(c, v, -1);
    return v;
  }

  /**
   * Internal method which does the bulk of the work for both
   * <CODE>getControl()</CODE> and <CODE>getControls()</CODE>.
   * If <CODE>v</CODE> is non-null, adds all <CODE>Control</CODE>s
   *  of the specified <CODE>Class</CODE> to that <CODE>Vector</CODE>.
   * Otherwise, returns <CODE>inst</CODE> instance of the
   *  <CODE>Control</CODE> matching the specified <CODE>Class</CODE>,
   *  or <CODE>null</CODE> if no <CODE>Control</CODE> matching the
   *  criteria is found.
   *
   * @param ctlClass 
   * @param v 
   * @param inst 
   *
   * @return 
   */
  private Control getControls(Class ctlClass, Vector v, int inst) {
    if (displayRenderer == null) return null;
    if (ctlClass == null) {
      return null;
    }

    GraphicsModeControl gmc = getGraphicsModeControl();
    if (ctlClass.isInstance(gmc)) {
      if (v == null) {
        return gmc;
      }
      v.addElement(gmc);
    }
    else {
      synchronized (ControlVector) {
        Enumeration en = ControlVector.elements();
        while(en.hasMoreElements()) {
          Control c = (Control)en.nextElement();
          if (ctlClass.isInstance(c)) {
            if (v != null) {
              v.addElement(c);
            }
            else if (c.getInstanceNumber() == inst) {
              return c;
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * @return the total number of controls used by this display
   */
  public int getNumberOfControls() {
    return ControlVector.size();
  }

  /**
   * @return clone of Vector of Controls linked to this DisplayImpl
   * @deprecated - DisplayImpl shouldn't expose itself at this level
   */
  public Vector getControlVector() {
    return (Vector)ControlVector.clone();
  }

  /** whether the Control widget panel needs to be reconstructed */
  private boolean needWidgetRefresh = true;

  /** this Display's associated panel of Control widgets */
  private JPanel widgetPanel = null;

  /**
   * get a GUI component containing this Display's Control widgets;
   * create the widgets as necessary
   * @return Container of widget panel
   */
  public Container getWidgetPanel() {
    if (displayRenderer == null) return null;
    if (needWidgetRefresh) {
      synchronized (MapVector) {
        // construct widget panel if needed
        if (widgetPanel == null) {
          widgetPanel = new JPanel();
          widgetPanel.setLayout(new BoxLayout(widgetPanel, BoxLayout.Y_AXIS));
        }
        else
          widgetPanel.removeAll();

        if (getLinks().size() > 0) {
          // GraphicsModeControl widget
          GMCWidget gmcw = new GMCWidget(getGraphicsModeControl());
          addToWidgetPanel(gmcw, false);
        }

        for (int i = 0; i < MapVector.size(); i++) {
          ScalarMap sm = (ScalarMap)MapVector.elementAt(i);

          DisplayRealType drt = sm.getDisplayScalar();
          try {
            double[] a = new double[2];
            double[] b = new double[2];
            double[] c = new double[2];
            boolean scale = sm.getScale(a, b, c);
            if (scale) {
              // ScalarMap range widget
              RangeWidget rw = new RangeWidget(sm);
              addToWidgetPanel(rw, true);
            }
          }
          catch (VisADException exc) {
          }
          try {
            if (drt.equals(Display.RGB) || drt.equals(Display.RGBA)) {
              // ColorControl widget
              try {
                LabeledColorWidget lw = new LabeledColorWidget(sm);
                addToWidgetPanel(lw, true);
              }
              catch (VisADException exc) {
              }
              catch (RemoteException exc) {
              }
            }
            else if (drt.equals(Display.SelectValue)) {
              // ValueControl widget
              VisADSlider vs = new VisADSlider(sm);
              vs.setAlignmentX(JPanel.CENTER_ALIGNMENT);
              addToWidgetPanel(vs, true);
            }
            else if (drt.equals(Display.SelectRange)) {
              // RangeControl widget
              SelectRangeWidget srw = new SelectRangeWidget(sm);
              addToWidgetPanel(srw, true);
            }
            else if (drt.equals(Display.IsoContour)) {
              // ContourControl widget
              ContourWidget cw = new ContourWidget(sm);
              addToWidgetPanel(cw, true);
            }
            else if (drt.equals(Display.Animation)) {
              // AnimationControl widget
              AnimationWidget aw = new AnimationWidget(sm);
              addToWidgetPanel(aw, true);
            }
          }
          catch (VisADException exc) {
          }
          catch (RemoteException exc) {
          }
        }
      }
      needWidgetRefresh = false;
    }
    return widgetPanel;
  }

  /**
   * add a component to the widget panel 
   *
   * @param c 
   * @param divide 
   */
  private void addToWidgetPanel(Component c, boolean divide) {
    if (displayRenderer == null) return;
    if (divide) widgetPanel.add(new Divider());
    widgetPanel.add(c);
  }

  /**
   * @return length of valueArray passed to ShadowType.doTransform()
   */
  public int getValueArrayLength() {
    return valueArrayLength;
  }

  /**
   * @return int[] array mapping from valueArray indices to
   *         ScalarType Vector indices
   */
  public int[] getValueToScalar() {
    return valueToScalar;
  }

  /**
   * @return int[] array mapping from valueArray indices to
   *         ScalarMap Vector indices
   */
  public int[] getValueToMap() {
    return valueToMap;
  }

  /**
   * @return the ProjectionControl associated with this DisplayImpl
   */
  public abstract ProjectionControl getProjectionControl();

  /**
   * @return the GraphicsModeControl associated with this DisplayImpl
   */
  public abstract GraphicsModeControl getGraphicsModeControl();

  /**
   * wait for millis milliseconds
   * @param millis number of milliseconds to wait
   * @deprecated Use <CODE>new visad.util.Delay(millis)</CODE> instead.
   */
  public static void delay(int millis) {
    new visad.util.Delay(millis);
  }

  /**
   * print a stack dump with the given message
   * @param message String to print with stack dump
   */
  public static void printStack(String message) {
    try {
      throw new DisplayException("printStack: " + message);
    }
    catch (DisplayException e) {
      e.printStackTrace();
    }
  }

  /**
   * test for equality between this and the given Object
   * given their complexity, its reasonable that DisplayImpl
   * objects are only equal to themselves
   * @param obj Object to test for equality with this
   * @return flag indicating whether this is equal to obj
   */
  public boolean equals(Object obj) {
    return (obj == this);
  }

  /**
   * Returns the list of DataRenderer-s.  NOTE: The actual list is returned
   * rather than a copy.  If a copy is desired, then use
   * <code>getRendererVector()</code>.
   * @return                    The list of DataRenderer-s.
   * @see #getRendererVector()
   */
  public Vector getRenderers() {
    return (Vector)RendererVector.clone();
  }

  /**
   * Return the API used for this display
   *
   * @return  the mode being used (UNKNOWN, JPANEL, APPLETFRAME,
   *                               OFFSCREEN, TRANSFORM_ONLY)
   * @throws  VisADException
   */
  public int getAPI() throws VisADException {
    throw new VisADException("No API specified");
  }

  /**
   * @return the <CODE>DisplayMonitor</CODE> associated with this
   * <CODE>Display</CODE>.
   */
  public DisplayMonitor getDisplayMonitor() {
    return displayMonitor;
  }

  /**
   * @return the <CODE>DisplaySync</CODE> associated with this
   * <CODE>Display</CODE>.
   */
  public DisplaySync getDisplaySync() {
    return displaySync;
  }

  /**
   * set given MouseBehavior
   * @param m MouseBehavior to set
   */
  public void setMouseBehavior(MouseBehavior m) {
    mouse = m;
  }

  /**
   * @return the MouseBehavior used for this Display
   */
  public MouseBehavior getMouseBehavior() {
    return mouse;
  }

  /**
   * make projection matrix from given arguments
   * @param rotx rotation about x axis
   * @param roty rotation about y axis
   * @param rotz rotation about z axis
   * @param scale linear scale factor
   * @param transx translation along x axis
   * @param transy translation along y axis
   * @param transz translation along z axis
   *
   * @return projection matrix
   */
  
  public double[] make_matrix(double rotx, double roty, double rotz,
                              double scale, double transx, double transy,
                              double transz) {
    if (mouse != null) {
      return mouse.make_matrix(
               rotx, roty, rotz, scale, transx, transy, transz);
    }
    else {
      return null;
    }
  }

  /**
   * multiply matrices
   * @param a first operand matrix
   * @param b second operand matrix
   * @return product matrix
   */
  public double[] multiply_matrix(double[] a, double[] b) {
    if (mouse != null && a != null && b != null) {
      return mouse.multiply_matrix(a, b);
    }
    else {
      return null;
    }
  }

  /**
   * get a BufferedImage of this Display, without synchronizing
   * (assume the application has made sure Data have been
   * transformed and rendered)
   * @return a captured image of this Display
   */
  public BufferedImage getImage() {
    return getImage(false);
  }

  /**
   * get a BufferedImage of this Display
   * @param sync if true, ensure that all linked Data have been
   *        transformed and rendered
   * @return a captured image of this Display
   */
  public BufferedImage getImage(boolean sync) {
    if (displayRenderer == null) return null;
    Thread thread = Thread.currentThread();
    String name = thread.getName();
    if (thread.equals(getCurrentActionThread()) ||
        name.startsWith("J3D-Renderer") ||
        name.startsWith("AWT-EventQueue")) {
      throw new VisADError("cannot call getImage() from Thread: " + name);
    }
    if (sync) new Syncher(this);
    return displayRenderer.getImage();
  }

  /**
   * @return a String representation of this Display
   */
  public String toString() {
    return toString("");
  }

  /**
   * @param pre String added to start of each line
   * @return a String representation of this Display
   *         indented by pre (a string of blanks)
   */
  public String toString(String pre) {
    String s = pre + "Display\n";
    Enumeration maps = MapVector.elements();
    while(maps.hasMoreElements()) {
      ScalarMap map = (ScalarMap)maps.nextElement();
      s = s + map.toString(pre + "    ");
    }
    maps = ConstantMapVector.elements();
    while(maps.hasMoreElements()) {
      ConstantMap map = (ConstantMap)maps.nextElement();
      s = s + map.toString(pre + "    ");
    }
    return s;
  }

  /**
   * 
   *
   * @throws Throwable 
   */
  protected void finalize() throws Throwable {
    if (!destroyed) destroy();
  }

  /**
   * Class used to ensure that all linked Data have been
   *   transformed and rendered, used by getImage() 
   */
  public class Syncher extends Object implements DisplayListener {

    /**           */
    private ProjectionControl control;

    /**           */
    int count;

    /**
     * construct a Syncher for the given DisplayImpl
     * @param display DisplayImpl for this Syncher
     */
    Syncher(DisplayImpl display) {
      try {
        synchronized (this) {
          control = display.getProjectionControl();
          count = -1;
          display.disableAction();
          display.addDisplayListener(this);
          display.reDisplayAll();
          display.enableAction();
          this.wait();
        }
      }
      catch (InterruptedException e) {
      }
      display.removeDisplayListener(this);
    }

    /**
     * process DisplayEvent
     * @param e DisplayEvent to process
     *
     * @throws RemoteException 
     * @throws VisADException 
     */
    public void displayChanged(DisplayEvent e)
            throws VisADException, RemoteException {
      if (e.getId() == DisplayEvent.TRANSFORM_DONE) {
        count = 2;
        control.setMatrix(control.getMatrix());
      }
      else if (e.getId() == DisplayEvent.FRAME_DONE) {
        if (count > 0) {
          control.setMatrix(control.getMatrix());
          count--;
        }
        else if (count == 0) {
          synchronized (this) {
            this.notify();
          }
          count--;
        }
      }
    }
  }

  /**
   * Return the Printable object to be used by a PrinterJob.  This can
   * be used as follows:
   * <pre>
   *    PrinterJob printJob = PrinterJob.getPrinterJob();
   *    PageFormat pf = printJob.defaultPage();
   *    printJob.setPrintable(display.getPrintable(), pf);
   *    if (printJob.printDialog()) {
   *        try {
   *            printJob.print();
   *        }
   *        catch (Exception pe) {
   *            pe.printStackTrace();
   *        }
   *    }
   * </pre>
   *
   * @return printable object
   */
  public Printable getPrintable() {
    if (printer == null) printer = new Printable() {
      public int print(Graphics g, PageFormat pf, int pi)
              throws PrinterException {
        if (pi >= 1) {
          return Printable.NO_SUCH_PAGE;
        }
        BufferedImage image = DisplayImpl.this.getImage();
        g.drawImage(
          image, (int)pf.getImageableX(), (int)pf.getImageableY(),
          DisplayImpl.this.component);
        return Printable.PAGE_EXISTS;
      }
    };
    return printer;
  }

  /**
   * handle DisconnectException for the given ReferenceActionLink
   * @param raLink ReferenceActionLink with DisconnectException
   */
  void handleRunDisconnectException(ReferenceActionLink raLink) {
    if (!(raLink instanceof DataDisplayLink)) {
      return;
    }

    DataDisplayLink link = (DataDisplayLink)raLink;
  }

  /**
   * Notify this Display that a connection to a remote server has failed
   * @param renderer DataRenderer with failure
   * @param link DataDisplayLink with failure
   */
  public void connectionFailed(DataRenderer renderer, DataDisplayLink link) {
    try {
      removeLinks(new DataDisplayLink[] {link});
    }
    catch (VisADException ve) {
      ve.printStackTrace();
    }
    catch (RemoteException re) {
      re.printStackTrace();
    }

    if (renderer != null) {
      DataDisplayLink[] links = renderer.getLinks();
      if (links.length <= 1) {
        deleteRenderer(renderer);
      }
    }

    Enumeration en = RmtSrcListeners.elements();
    while(en.hasMoreElements()) {
      RemoteSourceListener l = (RemoteSourceListener)en.nextElement();
      l.dataSourceLost(link.getName());
    }
  }

  /**
   * Inform <tt>listener</tt> of deleted {@link DataRenderer}s.
   *
   * @param listener Object to add.
   */
  public void addRendererSourceListener(RendererSourceListener listener) {
    RendererSourceListeners.addElement(listener);
  }

  /**
   * Remove <tt>listener</tt> from the {@link DataRenderer} deletion list.
   *
   * @param listener Object to remove.
   */
  public void removeRendererSourceListener(RendererSourceListener listener) {
    RendererSourceListeners.removeElement(listener);
  }

  /**
   * Stop using a {@link DataRenderer}.
   *
   * @param renderer Renderer to delete
   */
  private void deleteRenderer(DataRenderer renderer) {
    RendererVector.removeElement(renderer);

    Enumeration en = RendererSourceListeners.elements();
    while(en.hasMoreElements()) {
      ((RendererSourceListener)en.nextElement()).rendererDeleted(renderer);
    }
  }

  /**
   * @deprecated
   *
   * @param listener 
   */
  public void addDataSourceListener(RemoteSourceListener listener) {
    addRemoteSourceListener(listener);
  }

  /**
   * @deprecated
   *
   * @param listener 
   */
  public void removeDataSourceListener(RemoteSourceListener listener) {
    removeRemoteSourceListener(listener);
  }

  /**
   * Inform <tt>listener</tt> of changes in the availability
   * of remote data/collaboration sources.
   *
   * @param listener Object to send change notifications.
   */
  public void addRemoteSourceListener(RemoteSourceListener listener) {
    RmtSrcListeners.addElement(listener);
  }

  /**
   * Remove <tt>listener</tt> from the remote source notification list.
   *
   * @param listener Object to be removed.
   */
  public void removeRemoteSourceListener(RemoteSourceListener listener) {
    RmtSrcListeners.removeElement(listener);
  }

  /**
   * Inform {@link RemoteSourceListener}s that the specified collaborative
   * connection has been lost.<br>
   * <br>
   * <b>WARNING!</b>  This should only be called from within the
   * visad.collab package!
   *
   * @param id ID of lost connection.
   */
  public void lostCollabConnection(int id) {
    Enumeration en = RmtSrcListeners.elements();
    while(en.hasMoreElements()) {
      ((RemoteSourceListener)en.nextElement()).collabSourceLost(id);
    }
  }

  /**
   * Forward messages to the specified <tt>listener</tt>
   *
   * @param listener New message receiver.
   */
  public void addMessageListener(MessageListener listener) {
    MessageListeners.addElement(listener);
  }

  /**
   * Remove <tt>listener</tt> from the message list.
   *
   * @param listener Object to remove.
   */
  public void removeMessageListener(MessageListener listener) {
    MessageListeners.removeElement(listener);
  }

  /**
   * Send a message to all </tt>MessageListener</tt>s.
   *
   * @param msg Message being sent.
   *
   * @throws RemoteException 
   */
  public void sendMessage(MessageEvent msg) throws RemoteException {
    RemoteException exception = null;
    Enumeration en = MessageListeners.elements();
    while(en.hasMoreElements()) {
      MessageListener l = (MessageListener)en.nextElement();
      try {
        l.receiveMessage(msg);
      }
      catch (RemoteException re) {
        if (visad.collab.CollabUtil.isDisconnectException(re)) {
          // remote side disconnected; forget about it
          MessageListeners.removeElement(l);
        }
        else {
          // save this exception for later
          exception = re;
        }
      }
    }

    if (exception != null) {
      throw exception;
    }
  }

  /**
   * set aspect ratio of XAxis, YAxis & ZAxis in ScalarMaps rather
   * than matrix (i.e., don't distort text fonts); called by
   * ProjectionControl.setAspectCartesian()
   * @param aspect ratios; 3 elements for Java3D, 2 for Java2D
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void setAspectCartesian(double[] aspect)
          throws VisADException, RemoteException {
    if (displayRenderer == null) return;
    if (mapslock == null) return;
    synchronized (mapslock) {
      // clone MapVector to avoid need for synchronized access
      Vector tmap = (Vector)MapVector.clone();
      Enumeration maps = tmap.elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap)maps.nextElement();
        map.setAspectCartesian(aspect);
      }

      tmap = (Vector)ConstantMapVector.clone();
      maps = tmap.elements();
      while(maps.hasMoreElements()) {
        ConstantMap map = (ConstantMap)maps.nextElement();
        map.setAspectCartesian(aspect);
      }

      // resize box
      getDisplayRenderer().setBoxAspect(aspect);

      // reAutoScale(); ??
      reDisplayAll();
    } // end synchronized (mapslock)
  }

  /**
   * Add a busy/idle activity handler.
   *
   * @param ah Activity handler.
   *
   * @throws VisADException If the handler couldn't be added.
   */
  public void addActivityHandler(ActivityHandler ah) throws VisADException {
    if (displayRenderer == null) return;
    if (displayActivity == null) {
      displayActivity = new DisplayActivity(this);
    }

    displayActivity.addHandler(ah);
  }

  /**
   * Remove a busy/idle activity handler.
   *
   * @param ah Activity handler.
   *
   * @throws VisADException If the handler couldn't be removed.
   */
  public void removeActivityHandler(ActivityHandler ah)
          throws VisADException {
    if (displayRenderer == null) return;
    if (displayActivity == null) {
      displayActivity = new DisplayActivity(this);
    }

    displayActivity.removeHandler(ah);
  }

  /**
   * Indicate to activity monitor that the Display is busy.
   */
  public void updateBusyStatus() {
    if (displayActivity != null) {
      displayActivity.updateBusyStatus();
    }
  }

  /** Class for listening to component events */
  private class ComponentChangedListener extends ComponentAdapter {

    /** the listener's display */
    DisplayImpl display;

    /**
     * Create a listener for the display
     *
     * @param d 
     */
    public ComponentChangedListener(DisplayImpl d) {
      display = d;
    }

    /**
     * Invoked when the component has been resized.
     * @param ce  ComponentEvent fired.
     */
    public void componentShown(ComponentEvent ce) {}

    /**
     * Invoked when the component has been made invisible.
     * @param ce  ComponentEvent fired.
     */
    public void componentHidden(ComponentEvent ce) {}

    /**
     * Invoked when the component has been moved.
     * @param ce  ComponentEvent fired.
     */
    public void componentMoved(ComponentEvent ce) {}

    /**
     * Invoked when the component has been resized.
     * @param ce  ComponentEvent fired.
     */
    public void componentResized(ComponentEvent ce) {
      Component component = ce.getComponent();
      Dimension d = component.getSize();
      try {
        notifyListeners(
          new DisplayEvent(display, DisplayEvent.COMPONENT_RESIZED, d.width,
                           d.height));
      }
      catch (VisADException ve) {
        System.err.println("Couldn't notify listeners of resize event");
      }
      catch (RemoteException re) {
        System.err.println(
          "Couldn't notify listeners of remote resize event");
      }
    }

  }
}

