/*

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 1998 Nick Rasmussen
VisAD is Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.rmi.RemoteException;

import javax.swing.BoxLayout;

import visad.BaseColorControl;
import visad.Control;
import visad.ControlEvent;
import visad.ControlListener;
import visad.DisplayException;
import visad.ScalarMap;
import visad.ScalarMapEvent;
import visad.ScalarMapListener;
import visad.VisADException;

/**
 * A color widget that allows users to interactively map numeric data to
 * RGB/RGBA tuples in a <CODE>ScalarMap</CODE>.
 */
public class ColorMapWidget
  extends SimpleColorMapWidget
  implements ColorChangeListener, ControlListener, ScalarMapListener
{
  private Panel buttonPanel = null;

  BaseColorControl control;

  /**
   * Construct a <CODE>LabeledColorWidget</CODE> linked to the
   * color control in the <CODE>ScalarMap</CODE> (which must be to either
   * <CODE>Display.RGB</CODE> or </CODE>Display.RGBA</CODE> and already
   * have been added to a <CODE>Display</CODE>).
   * It will be labeled with the name of the <CODE>ScalarMap</CODE>'s
   * RealType and linked to the <CODE>ScalarMap</CODE>'s color control.
   * The range of <CODE>RealType</CODE> values mapped to color is taken
   * from the <CODE>ScalarMap's</CODE> range - this allows a color widget
   * to be used with a range of values defined by auto-scaling from
   * displayed data.
   *
   * @param smap <CODE>ScalarMap</CODE> to which this widget is bound.
   *
   * @exception RemoteException If there is an RMI-related problem.
   * @exception VisADException If there is a problem initializing the
   *                           widget.
   */
  public ColorMapWidget(ScalarMap smap)
    throws VisADException, RemoteException
  {
    this(smap, null, true);
  }

  /**
   * Construct a <CODE>LabeledColorWidget</CODE> linked to the
   * color control in the <CODE>ScalarMap</CODE> (which must be to either
   * <CODE>Display.RGB</CODE> or </CODE>Display.RGBA</CODE> and already
   * have been added to a <CODE>Display</CODE>).
   * It will be labeled with the name of the <CODE>ScalarMap</CODE>'s
   * RealType and linked to the <CODE>ScalarMap</CODE>'s color control.
   * The range of <CODE>RealType</CODE> values mapped to color is taken
   * from the <CODE>ScalarMap's</CODE> range - this allows a color widget
   * to be used with a range of values defined by auto-scaling from
   * displayed data.
   *
   * The initial color table (if non-null)
   * should be a <CODE>float[resolution][dimension]</CODE>, where
   * <CODE>dimension</CODE> is either
   * <CODE>3</CODE> for <CODE>Display.RGB</CODE> or
   * <CODE>4</CODE> for <CODE>Display.RGB</CODE>) with values
   * between <CODE>0.0f</CODE> and <CODE>1.0f</CODE>.
   *
   * @param smap <CODE>ScalarMap</CODE> to which this widget is bound.
   * @param table Initial color lookup table.
   *
   * @exception RemoteException If there is an RMI-related problem.
   * @exception VisADException If there is a problem initializing the
   *                           widget.
   */
  public ColorMapWidget(ScalarMap smap, float[][] table)
    throws VisADException, RemoteException
  {
    this(smap, table, true);
  }

  /**
   * Construct a <CODE>LabeledColorWidget</CODE> linked to the
   * color control in the <CODE>ScalarMap</CODE> (which must be to either
   * <CODE>Display.RGB</CODE> or </CODE>Display.RGBA</CODE> and already
   * have been added to a <CODE>Display</CODE>).
   * It will be labeled with the name of the <CODE>ScalarMap</CODE>'s
   * RealType and linked to the <CODE>ScalarMap</CODE>'s color control.
   * The range of <CODE>RealType</CODE> values mapped to color is taken
   * from the <CODE>ScalarMap's</CODE> range - this allows a color widget
   * to be used with a range of values defined by auto-scaling from
   * displayed data.
   *
   * The initial color table (if non-null)
   * should be a <CODE>float[resolution][dimension]</CODE>, where
   * <CODE>dimension</CODE> is either
   * <CODE>3</CODE> for <CODE>Display.RGB</CODE> or
   * <CODE>4</CODE> for <CODE>Display.RGB</CODE>) with values
   * between <CODE>0.0f</CODE> and <CODE>1.0f</CODE>.
   *
   * @param smap <CODE>ScalarMap</CODE> to which this widget is bound.
   * @param table Initial color lookup table.
   * @param update <CODE>true</CODE> if the slider should follow the
   *               <CODE>ScalarMap</CODE>'s range.
   *
   * @exception RemoteException If there is an RMI-related problem.
   * @exception VisADException If there is a problem initializing the
   *                           widget.
   */
  public ColorMapWidget(ScalarMap smap, float[][] table, boolean update)
    throws VisADException, RemoteException
  {
    super(smap.getScalar().getName(), table,
          (float )smap.getRange()[0], (float )smap.getRange()[1] + 1.0f);

    // make sure we're using a valid scalarmap
    Control ctl = smap.getControl();
    if (!(ctl instanceof BaseColorControl)) {
      throw new DisplayException(getClass().getName() + ": ScalarMap must " +
                                 "be Display.RGB or Display.RGBA");
    }

    // save the control
    control = (BaseColorControl )ctl;

    // set up color table
    if (table == null) {
      float[][] t = table_reorg(control.getTable());
      ColorMap colorMap = new BaseRGBMap(t, t[0].length > 3);
      colorWidget.setColorMap(colorMap);
    } else {
      control.setTable(table);
      ((BaseRGBMap )colorWidget.getColorMap()).setValues(table);
    }

    // listen for changes
    control.addControlListener(this);
    colorWidget.addColorChangeListener(this);
    if (update) {
      smap.addScalarMapListener(this);
    }
  }

  /**
   * Forward changes from the <CODE>ColorWidget</CODE> to the
   * <CODE>Control</CODE> associated with this widget's
   * <CODE>ScalarMap</CODE>.
   *
   * @param evt Data from the changed <CODE>ColorWidget</CODE>.
   */
  public void colorChanged(ColorChangeEvent evt)
  {
    updateControlFromColorMap();
  }

  /**
   * Update the <CODE>Control</CODE> associated with this widget's
   * <CODE>ScalarMap</CODE> with the data from this widget's
   * <CODE>ColorWidget</CODE>.
   */
  private void updateControlFromColorMap()
  {
    // get the colormap
    ColorMap map_e = colorWidget.getColorMap();
    if (map_e == null) {
      return;
    }

    // stash some map-related constants
    final int dim = map_e.getMapDimension();
    final int res = map_e.getMapResolution();
    final float scale = 1.0f / (res - 1.0f);

    // construct a new table from the map data
    float[][] table_e = new float[dim][res];
    for (int i=0; i<res; i++) {
      float[] t = map_e.getTuple(scale * i);
      table_e[0][i] = t[0];
      table_e[1][i] = t[1];
      table_e[2][i] = t[2];
      if (dim > 3) {
        table_e[3][i] = t[3];
      }
    }

    // save the new table to the Control
    try {
      control.setTable(table_e);
    } catch (VisADException ve) {
    } catch (RemoteException re) {
    }
  }

  /**
   * If the color data in the <CODE>Control</CODE> associated with this
   * widget's <CODE>ScalarMap</CODE> has changed, update the data in
   * the <CODE>ColorMap</CODE> associated with this widget's
   * <CODE>ColorWidget</CODE>.
   *
   * @param evt Data from the changed <CODE>Control</CODE>.
   */
  public void controlChanged(ControlEvent evt)
  {
    // get the colormap
    ColorMap map_e = colorWidget.getColorMap();
    if (map_e == null) {
      return;
    }

    // stash some map-related constants
    final int dim = map_e.getMapDimension();
    final int res = map_e.getMapResolution();
    final float scale = 1.0f / (res - 1.0f);

    // grab the Control's table
    float[][] table = control.getTable();

    boolean identical = true;
    if (table == null || table.length != dim) {
      // table is fundamentally different
      identical = false;
    } else {
      for (int i = 0; i < dim; i++) {
        if (table[i].length != res) {
          // table resolution changed
          identical = false;
          break;
        }
      }

      // if the basic table shape is unchanged...
      if (identical) {
        for (int i=0; i<res; i++) {
          float[] t = map_e.getTuple(scale * i);
          if (Math.abs(table[0][i] - t[0]) > 0.0001 ||
              Math.abs(table[1][i] - t[1]) > 0.0001 ||
              Math.abs(table[2][i] - t[2]) > 0.0001 ||
              (dim > 3 && Math.abs(table[3][i] - t[3]) > 0.0001))
          {
            // table data changed
            identical = false;
            break;
          }
        }
      }
    }

    // if the table has changed...
    if (!identical) {
      ((BaseRGBMap )map_e).setValues(table_reorg(table));
    }
  }

  /**
   * If the <CODE>ScalarMap</CODE> changes, update the slider with
   * the new range.
   *
   * @param evt Data from the changed <CODE>ScalarMap</CODE>.
   */
  public void mapChanged(ScalarMapEvent evt)
  {
    double[] range = evt.getScalarMap().getRange();
    updateSlider((float) range[0], (float) range[1]);
  }

  public static void main(String[] args)
  {
    try {
      visad.RealType vis = new visad.RealType("vis", null, null);
      ScalarMap map = new ScalarMap(vis, visad.Display.RGBA);
      map.setRange(0.0f, 1.0f);

      visad.DisplayImpl dpy = new visad.java2d.DisplayImplJ2D("2d");
      dpy.addMap(map);

      javax.swing.JFrame f;

      f = new javax.swing.JFrame("0");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(new ColorMapWidget(map));
      f.pack();
      f.setVisible(true);

      f = new javax.swing.JFrame("1");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(new ColorMapWidget(map, null));
      f.pack();
      f.setVisible(true);

      f = new javax.swing.JFrame("!Updated");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(new ColorMapWidget(map, null, false));
      f.pack();
      f.setVisible(true);

      try { Thread.sleep(5000); } catch (InterruptedException ie) { }

      map.setRange(-10.0f, 10.0f);
    } catch (RemoteException re) {
      re.printStackTrace();
    } catch (VisADException ve) {
      ve.printStackTrace();
    }
  }
}
