/*

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2019 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import java.rmi.RemoteException;

import visad.BaseColorControl;
import visad.Control;
import visad.ControlEvent;
import visad.ControlListener;
import visad.DisplayException;
import visad.ScalarMap;
import visad.ScalarMapEvent;
import visad.ScalarMapControlEvent;
import visad.ScalarMapListener;
import visad.VisADException;

/**
 * A color widget that allows users to interactively map numeric data to
 * RGB/RGBA tuples in a <CODE>ScalarMap</CODE>.
 */
public class ColorMapWidget
  extends SimpleColorMapWidget
  implements ActionListener, ControlListener, ScalarMapListener
{
  private JPanel buttonPanel = null;
  private float[][] undoTable = null;

  BaseColorControl control, realControl;

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
    this(smap, null, true, true);
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
   * @param smap <CODE>ScalarMap</CODE> to which this widget is bound.
   * @param immediate <CODE>true</CODE> if changes are immediately
   *                  propagated to the associated <CODE>Control</CODE>.
   *
   * @exception RemoteException If there is an RMI-related problem.
   * @exception VisADException If there is a problem initializing the
   *                           widget.
   */
  public ColorMapWidget(ScalarMap smap, boolean immediate)
    throws VisADException, RemoteException
  {
    this(smap, null, true, immediate);
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
    this(smap, table, true, true);
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
    this(smap, table, update, true);
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
   * @param immediate <CODE>true</CODE> if changes are immediately
   *                  propagated to the associated <CODE>Control</CODE>.
   *
   * @exception RemoteException If there is an RMI-related problem.
   * @exception VisADException If there is a problem initializing the
   *                           widget.
   */
  public ColorMapWidget(ScalarMap smap, float[][] table, boolean update,
                        boolean immediate)
    throws VisADException, RemoteException
  {
    super(smap.getScalarName(), smap.getControl(),
          (float )smap.getRange()[0], (float )smap.getRange()[1] + 1.0f);

    // make sure we're using a valid scalarmap
    Control ctl = smap.getControl();
    if (!(ctl instanceof BaseColorControl)) {
      throw new DisplayException(getClass().getName() + ": ScalarMap must " +
                                 "be Display.RGB or Display.RGBA");
    }

    // save the control
    if (immediate) {
      control = (BaseColorControl )ctl;
      realControl = null;
    } else {
      realControl = (BaseColorControl )ctl;
      control = new BaseColorControl(realControl.getDisplay(),
                                     realControl.getNumberOfComponents());
      control.syncControl(realControl);

      // use the shadow control, not the real control
      baseMap = new BaseRGBMap(control);
      preview.setMap(baseMap);
      rebuildGUI();
    }

    // set up color table
    if (table != null) {
      control.setTable(table);
    }

    // if not in immediate mode, build "Apply" and "Undo" buttons
    if (!immediate) {
      buttonPanel = buildButtons();
      add(buttonPanel);
    }

    // listen for changes
    if (realControl != null) {
      realControl.addControlListener(this);
    } else {
      control.addControlListener(this);
    }
    if (update) {
      smap.addScalarMapListener(this);
    }
  }

  /**
   * Use a new table of color values.
   * If immediate mode is off, changes to the associated color
   * control are not applied until the Apply button is clicked.
   *
   * @param table New color values.
   */
  public void setTableView(float[][] table) {
    try { control.setTable(table); }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /**
   * Gets the widget's current table. If immediate mode is
   * off, it may not match the linked color control's table.
   */
  public float[][] getTableView() { return control.getTable(); }

  /**
   * Build "Apply" and "Undo" button panel.
   *
   * @return JPanel containing the buttons.
   */
  private JPanel buildButtons()
  {
    JButton apply = new JButton("Apply");
    apply.setActionCommand("apply");
    apply.addActionListener(this);

    JButton undo = new JButton("Undo");
    undo.setActionCommand("undo");
    undo.addActionListener(this);

    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
    panel.add(apply);
    panel.add(undo);

    // save initial table in case they immediately hit "undo"
    undoTable = control.getTable();

    return panel;
  }

  /**
   * Return the panel in which the buttons are stored.
   */
  public JPanel getButtonPanel() { return buttonPanel; }

  /**
   * Handle button presses.
   *
   * @param evt Data from the changed <CODE>JButton</CODE>.
   */
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getActionCommand().equals("apply")) {
      undoTable = realControl.getTable();
      try {
        realControl.syncControl(control);
      } catch (VisADException ve) {
      }
    } else if (evt.getActionCommand().equals("undo")) {
      try {
        realControl.setTable(undoTable);
      } catch (VisADException ve) {
      } catch (RemoteException re) {
      }
    }
  }

  /**
   * Forward changes from the <CODE>Control</CODE> associated with
   * this widget's <CODE>ScalarMap</CODE> to the internal
   * shadow <CODE>Control</CODE>.
   *
   * @param evt Data from the changed <CODE>Control</CODE>.
   */
  public void controlChanged(ControlEvent evt)
    throws RemoteException, VisADException
  {
    if (realControl == evt.getControl()) {
      control.syncControl(realControl);
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

  /**
   * ScalarMapListener method used to detect new control.
   */
  public void controlChanged(ScalarMapControlEvent evt)
    throws RemoteException, VisADException
  {
    int id = evt.getId();
    if (id == ScalarMapEvent.CONTROL_REMOVED ||
        id == ScalarMapEvent.CONTROL_REPLACED)
    {
      if (realControl != null) {
        evt.getControl().removeControlListener(this);
      }
    }

    if (id == ScalarMapEvent.CONTROL_REPLACED ||
        id == ScalarMapEvent.CONTROL_ADDED)
    {
      BaseColorControl ctl;
      ctl = (BaseColorControl )(evt.getScalarMap().getControl());
      if (realControl != null) {
        realControl = ctl;
        realControl.addControlListener(this);
      }
    }
  }

  public static void main(String[] args)
  {
    try {
      visad.RealType vis = visad.RealType.getRealType("vis");
      ScalarMap visMap = new ScalarMap(vis, visad.Display.RGBA);
      visMap.setRange(0.0f, 1.0f);

      visad.RealType ir = visad.RealType.getRealType("ir");
      ScalarMap irMap = new ScalarMap(vis, visad.Display.RGB);
      irMap.setRange(0.0f, 1.0f);

      visad.DisplayImpl dpy = new visad.java2d.DisplayImplJ2D("2d");
      dpy.addMap(visMap);
      dpy.addMap(irMap);

      javax.swing.JFrame f;

      f = new javax.swing.JFrame("0");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(new ColorMapWidget(visMap));
      f.pack();
      f.setVisible(true);

      f = new javax.swing.JFrame("1");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(new ColorMapWidget(visMap, false));
      f.pack();
      f.setVisible(true);

      f = new javax.swing.JFrame("!Updated");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(new ColorMapWidget(visMap, null, false, true));
      f.pack();
      f.setVisible(true);

      final int num = 3;
      final int len = 256;
      float[][] table = new float[num][len];
      final float step = 1.0f / (len - 1.0f);
      float total = 1.0f;
      for (int j=0; j<len; j++) {
        table[0][j] = table[1][j] = table[2][j] = total;
        if (num > 3) {
          table[3][j] = 1.0f;
        }
        total -= step;
      }

      f = new javax.swing.JFrame("Table");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(new ColorMapWidget(irMap, table));
      f.pack();
      f.setVisible(true);

      try { Thread.sleep(5000); } catch (InterruptedException ie) { }

      visMap.setRange(-10.0f, 10.0f);
    } catch (RemoteException re) {
      re.printStackTrace();
    } catch (VisADException ve) {
      ve.printStackTrace();
    }
  }
}
