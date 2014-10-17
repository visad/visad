/*
@(#) $Id: ColorWidget.java,v 1.15 2002-06-28 15:59:02 tomw Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2014 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.util;

import java.awt.Dimension;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.applet.Applet;

import java.rmi.RemoteException;

import java.util.Vector;

import javax.swing.*;

import visad.VisADException;

/**
 * A color widget that allows users to interactively map numeric data to
 * RGBA tuples based on the Vis5D color widget
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.15 $, $Date: 2002-06-28 15:59:02 $
 * @since Visad Utility Library, 0.5
 */

public class ColorWidget extends Applet implements ColorChangeListener {

  /** The visibility of the preview panel at the botom of the widget */
  private boolean previewVisible;

  /** The ColorMap associsted with this color widget */
  private ColorMap map;

  /** The ColorPreview associated with this color widget */
  private ColorPreview colorPreview;

  /* / * * The Event Queue for mouse events */

  /** Construct a color widget with a ColorPreview and the default ColorMap */
  public ColorWidget()
    throws RemoteException, VisADException
  {
    this(true);
  }

  /** Construct a color widget with the default ColorMap
   * @param preview indicates wether or not the preview bar at the
   * bottom of the widget should be present
   */
  public ColorWidget(boolean preview)
    throws RemoteException, VisADException
  {
    this(new RGBMap(), preview);
  }

  /** Construct a color widget with a ColorPreview and the specified ColorMap
   * @param map the ColorMap for the widget to use
   */
  public ColorWidget(ColorMap map) {
    this(map, true);
  }

  /** Construct a color widget with the desired ColorMap and ColorPreview visibility
   * @param map the ColorMap for the widget to use
   * @param preview indicates wether or not the preview bar at the
   * bottom of the widget should be present
   */
  public ColorWidget(ColorMap map, boolean preview) {
    this.map = map;

    previewVisible = preview;
    if (preview) {
      colorPreview = new ColorPreview(map);
    }
    //setLayout(new WidgetLayout(this));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setColorMap(map);
  }

  /** main method for standalone testing */
  public static void main(String[] argv)
    throws RemoteException, VisADException
  {

    JFrame frame;
    frame = new JFrame("VisAD Color Widget");
    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
      });

    ColorWidget w = new ColorWidget(new RGBAMap());

    frame.getContentPane().add(w);
    frame.setSize(w.getPreferredSize());
    frame.setVisible(true);

  }

  /** The vector containing the ColorChangeListeners */
  private Vector listeners = new Vector();
  private Object listeners_lock = new Object();

  /** Add a ColorChangeListener to the listeners list */
  // public synchronized void addColorChangeListener(ColorChangeListener c) {
  public void addColorChangeListener(ColorChangeListener c) {
    synchronized (listeners_lock) {
      if (!listeners.contains(c)) {
        listeners.addElement(c);
      }
    }
  }

  /** Remove a ColorChangeListener from the listeners list */
  // public synchronized void removeColorChangeListener(ColorChangeListener c) {
  public void removeColorChangeListener(ColorChangeListener c) {
    synchronized (listeners_lock) {
      if (listeners.contains(c)) {
        listeners.removeElement(c);
      }
    }
  }

  /** Notify the ColorChangeListerers that the color widget has changed */
  // protected synchronized void notifyListeners(ColorChangeEvent e) {
  protected void notifyListeners(ColorChangeEvent e) {
    Vector listeners_clone = null;
    synchronized (listeners_lock) {
      listeners_clone = (Vector) listeners.clone();
    }
    for (int i = 0; i < listeners_clone.size(); i++) {
      ColorChangeListener c =
        (ColorChangeListener) listeners_clone.elementAt(i);
      c.colorChanged(e);
    }
  }

  /** Listen to the ColorMap and re-dispatch the ColorChangeEvents to
   * the ColorChangeListeners listening to the widget
   */
  public void colorChanged(ColorChangeEvent e) {
    notifyListeners(e);
  }

  /** Set the ColorWidget to listen to a specific ColorMap */
  public void setColorMap(ColorMap map) {
    if (this.map != null) {
      this.map.removeColorChangeListener(this);
    }

    this.map = map;

    map.addColorChangeListener(this);

    removeAll();
    add(map);
    if (previewVisible) {
      if (colorPreview == null) {
        colorPreview = new ColorPreview(map);
      }
      add(colorPreview);
    }
    validate();

    int res = map.getMapResolution();
    notifyListeners(new ColorChangeEvent(map, 0, res));
  }

  /** Make the preview bar at the bottom of the widget visible */
  public void showPreview() {

    if (previewVisible) return;
    previewVisible = true;
    this.setColorMap(map);
  }

  /** Hide the preview bar at the bottom of the widget */
  public void hidePreview() {

    if (!previewVisible) return;

    previewVisible = false;
    this.setColorMap(map);
  }

  /** Returns the ColorMap that the color wdget is curently pointing to */
  public ColorMap getColorMap() {
    return map;
  }

  /** Analyses the visible components and determines the preferred size */
  public Dimension getPreferredSize() {
    Dimension d = map.getPreferredSize();
    if (previewVisible) {
      Dimension p = colorPreview.getPreferredSize();
      Dimension n = new Dimension(d.width, d.height + p.height);
      d = n;
    }
    return d;
  }
}
