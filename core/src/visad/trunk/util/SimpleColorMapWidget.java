/*

@(#) $Id: SimpleColorMapWidget.java,v 1.28 1999-11-16 22:25:24 dglo Exp $

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

import visad.*;
import java.rmi.RemoteException;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.Vector;

import javax.swing.*;

/**
 * A color widget that allows users to interactively map numeric data to
 * RGB/RGBA color maps.
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.28 $, $Date: 1999-11-16 22:25:24 $
 * @since Visad Utility Library v0.7.1
 */
public class LabeledColorWidget
  extends Panel
  implements ActionListener, ColorChangeListener, ControlListener,
             ScalarMapListener
{

  private final int TABLE_SIZE;
  private final float SCALE;

  private ArrowSlider slider;

  private ColorWidget widget;

  private SliderLabel label;

  private float[][] orig_table;

  BaseColorControl control;

  private int components;

  /**
   * Construct a <CODE>LabeledColorWidget</CODE> linked to the
   * color control in the <CODE>ScalarMap</CODE> (which must be to either
   * <CODE>Display.RGB</CODE> or </CODE>Display.RGBA</CODE> and already
   * have been added to a <CODE>DIsplay</CODE>).
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
  public LabeledColorWidget(ScalarMap smap)
    throws VisADException, RemoteException
  {
    this(smap, null, true);
  }

  /**
   * This method is deprecated, since <CODE>min</CODE> and <CODE>max</CODE>
   * are ignored.
   *
   * @param smap <CODE>ScalarMap</CODE> to which this widget is bound.
   * @param min Ignored value.
   * @param max Ignored value.
   *
   * @exception RemoteException If there is an RMI-related problem.
   * @exception VisADException If there is a problem initializing the
   *                           widget.
   *
   * @deprecated - 'min' and 'max' are ignored
   */
  public LabeledColorWidget(ScalarMap smap, float min, float max)
    throws VisADException, RemoteException
  {
    this(smap, null, true);
  }

  /**
   * Construct a <CODE>LabeledColorWidget</CODE> linked to the
   * color control in the <CODE>ScalarMap</CODE> (which must be to either
   * <CODE>Display.RGB</CODE> or </CODE>Display.RGBA</CODE> and already
   * have been added to a <CODE>DIsplay</CODE>).
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
  public LabeledColorWidget(ScalarMap smap, float[][] table)
    throws VisADException, RemoteException
  {
    this(smap, table, true);
  }

  /**
   * This method is deprecated, since <CODE>min</CODE> and <CODE>max</CODE>
   * are ignored.
   *
   * @param smap <CODE>ScalarMap</CODE> to which this widget is bound.
   * @param min Ignored value.
   * @param max Ignored value.
   * @param table Initial color lookup table.
   *
   * @exception RemoteException If there is an RMI-related problem.
   * @exception VisADException If there is a problem initializing the
   *                           widget.
   *
   * @deprecated - 'min' and 'max' are ignored
   */
  public LabeledColorWidget(ScalarMap smap, float min, float max,
                            float[][] table)
    throws VisADException, RemoteException
  {
    this(smap, table, true);
  }

  /**
   * Construct a <CODE>LabeledColorWidget</CODE> linked to the
   * color control in the <CODE>ScalarMap</CODE> (which must be to either
   * <CODE>Display.RGB</CODE> or </CODE>Display.RGBA</CODE> and already
   * have been added to a <CODE>DIsplay</CODE>).
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
   * @param in_table Initial color lookup table.
   * @param update <CODE>true</CODE> if the slider should follow the
   *               <CODE>ScalarMap</CODE>'s range.
   *
   * @exception RemoteException If there is an RMI-related problem.
   * @exception VisADException If there is a problem initializing the
   *                           widget.
   */
  public LabeledColorWidget(ScalarMap smap, float[][] in_table, boolean update)
    throws VisADException, RemoteException
  {
    Control ctl = smap.getControl();
    if (!(ctl instanceof BaseColorControl)) {
      throw new DisplayException("LabeledColorWidget: ScalarMap must " +
                                 "be Display.RGB or Display.RGBA");
    }

    control = (BaseColorControl )ctl;
    components = control.getNumberOfComponents();

    String name = smap.getScalar().getName();
    float[][] table = table_reorg(in_table);

    double[] range = smap.getRange();
    float min = (float )range[0];
    float max = (float )(range[1] + 1.0);

    // set up user interface
    ColorWidget c = new ColorWidget(new BaseRGBMap(table, components > 3));
    ArrowSlider s = new ArrowSlider(min, max, (min + max) / 2, name);
    SliderLabel l = new SliderLabel(s);
    widget = c;
    slider = s;
    label = l;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(widget);
    add(slider);
    add(label);
    add(buildButtons());

    // enable auto-scaling
    if (update) {
      smap.addScalarMapListener(this);
    } else {
      smap.setRange(min, max);
      updateWidget(min, max);
    }

    // set up color table
    ColorMap map = widget.getColorMap();
    TABLE_SIZE = map.getMapResolution();
    SCALE = 1.0f / (TABLE_SIZE - 1.0f);
    if (table == null) {
      in_table = control.getTable();
      table = table_reorg(in_table);
    } else {
      control.setTable(in_table);
    }
    orig_table = copy_table(in_table);
    ((BaseRGBMap) map).setValues(table);
    widget.addColorChangeListener(this);
    control.addControlListener(this);
  }

  /**
   * Build "Reset" and "Grey Scale" button panel.
   *
   * @return Panel containing the buttons.
   */
  private Panel buildButtons()
  {
    Button reset = new Button("Reset") {
        public Dimension getMinimumSize() {
          return new Dimension(0, 18);
        }
        public Dimension getPreferredSize() {
          return new Dimension(0, 18);
        }
        public Dimension getMaximumSize() {
          return new Dimension(Integer.MAX_VALUE, 18);
        }
      };
    reset.setActionCommand("reset");
    reset.addActionListener(this);

    Button grey = new Button("Grey Scale") {
        public Dimension getMinimumSize() {
          return new Dimension(0, 18);
        }
        public Dimension getPreferredSize() {
          return new Dimension(0, 18);
        }
        public Dimension getMaximumSize() {
          return new Dimension(Integer.MAX_VALUE, 18);
        }
      };
    grey.setActionCommand("grey");
    grey.addActionListener(this);

    Panel panel = new Panel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(reset);
    panel.add(grey);

    return panel;
  }

  private Dimension maxSize = null;

  /**
   * Get maximum size of this widget.
   *
   * @return The maximum size stored in a <CODE>Dimension</CODE> object.
   */
  public Dimension getMaximumSize()
  {
    if (maxSize != null) return maxSize;
    else return super.getMaximumSize();
  }

  /**
   * Set maximum size of this widget.
   *
   * @param size Maximum size.
   */
  public void setMaximumSize(Dimension size)
  {
    maxSize = size;
  }

  /**
   * Internal convenience routine used to update the slider.
   *
   * @param min Minimum value for slider.
   * @param max Maximum value for slider.
   */
  private void updateWidget(float min, float max)
  {
    float val = slider.getValue();
    if (val != val || val <= min || val >= max) val = (min+max)/2;
    slider.setBounds(min, max, val);
  }

  /**
   * Handle button presses.
   *
   * @param evt Data from the changed <CODE>Button</CODE>.
   */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals("reset")) {
      // reset color table to original values
      try {
        float[][] table = copy_table(orig_table);
        control.setTable(table);
        ((BaseRGBMap) widget.getColorMap()).setValues(table_reorg(table));
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
    else if (e.getActionCommand().equals("grey")) {
      // reset color table to grey wedge
      if (orig_table != null && orig_table[0] != null) {
        int len = orig_table[0].length;
        float[][] table = new float[orig_table.length][len];
        float a = 1.0f / (len - 1.0f);
        for (int j=0; j<len; j++) {
          table[0][j] = table[1][j] = table[2][j] = j * a;
          if (components > 3) {
            table[3][j] = 1.0f;
          }
        }
        try {
          control.setTable(table);
          ((BaseRGBMap) widget.getColorMap()).setValues(table_reorg(table));
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }
    }
  }

  /**
   * Forward changes from the <CODE>ColorWidget</CODE> to the
   * <CODE>Control</CODE> associated with this widget's
   * <CODE>ScalarMap</CODE>.
   *
   * @param evt Data from the changed <CODE>ColorWidget</CODE>.
   */
  public void colorChanged(ColorChangeEvent e)
  {
    ColorMap map_e = widget.getColorMap();
    float[][] table_e = new float[components][TABLE_SIZE];
    for (int i=0; i<TABLE_SIZE; i++) {
      float[] t = map_e.getTuple(SCALE * i);
      table_e[0][i] = t[0];
      table_e[1][i] = t[1];
      table_e[2][i] = t[2];
      if (components > 3) {
        table_e[3][i] = t[3];
      }
    }
    try {
      control.setTable(table_e);
    }
    catch (VisADException f) { }
    catch (RemoteException f) { }
  }

  /**
   * If the color data in the <CODE>Control</CODE> associated with this
   * widget's <CODE>ScalarMap</CODE> has changed, update the data in
   * the <CODE>ColorMap</CODE> associated with this widget's
   * <CODE>ColorWidget</CODE>.
   *
   * @param evt Data from the changed <CODE>Control</CODE>.
   */
  public void controlChanged(ControlEvent e)
    throws VisADException, RemoteException
  {
    float[][] table = control.getTable();

    ColorMap map_e = widget.getColorMap();
    boolean identical = true;
    for (int i=0; i<TABLE_SIZE; i++) {
      float[] t = map_e.getTuple(SCALE * i);
      if (Math.abs(table[0][i] - t[0]) > 0.0001 ||
          Math.abs(table[1][i] - t[1]) > 0.0001 ||
          Math.abs(table[2][i] - t[2]) > 0.0001 ||
          (components > 3 && Math.abs(table[3][i] - t[3]) > 0.0001))
      {
        identical = false;
        break;
      }
    }
    if (!identical) {
      ((BaseRGBMap) map_e).setValues(table_reorg(table));
    }
  }

  /**
   * If the <CODE>ScalarMap</CODE> changes, update the slider with
   * the new range.
   *
   * @param evt Data from the changed <CODE>ScalarMap</CODE>.
   */
  public void mapChanged(ScalarMapEvent e)
  {
    ScalarMap s = e.getScalarMap();
    double[] range = s.getRange();
    updateWidget((float) range[0], (float) range[1]);
  }

  /**
   * Utility routine used to make a copy of a 2D <CODE>float</CODE> array.
   *
   * @param table Table to copy.
   *
   * @return The new copy.
   */
  private static float[][] copy_table(float[][] table)
  {
    if (table == null || table[0] == null) return null;
    final int dim = table.length;
    int len = table[0].length;
    float[][] new_table = new float[dim][len];
    try {
      for (int i=0; i<dim; i++) {
        System.arraycopy(table[i], 0, new_table[i], 0, len);
      }
      return new_table;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  /**
   * Utility routine used convert a table with dimensions <CODE>[X][Y]</CODE>
   * to a table with dimensions <CODE>[Y][X]</CODE>.
   *
   * @param table Table to reorganize.
   *
   * @return The reorganized table.
   */
  private static float[][] table_reorg(float[][] table)
  {
    if (table == null || table[0] == null) return null;
    try {
      final int dim = table.length;
      int len = table[0].length;
      float[][] out = new float[len][dim];
      for (int i=0; i<len; i++) {
        out[i][0] = table[0][i];
        out[i][1] = table[1][i];
        out[i][2] = table[2][i];
        if (dim > 3) {
          out[i][3] = table[3][i];
        }
      }
      return out;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  /**
   * Returns the <CODE>ColorWidget</CODE> used by this widget.
   *
   * @return The <CODE>ColorWidget</CODE>.
   */
  public ColorWidget getColorWidget()
  {
    return widget;
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    RealType visRadiance = new RealType("visRadiance", null, null);
    ScalarMap map = new ScalarMap(visRadiance, Display.RGBA);

    DisplayImpl dpy = new visad.java2d.DisplayImplJ2D("2d");
    dpy.addMap(map);

    JFrame f;

    f = new JFrame("VisAD LabeledColorWidget 0");
    f.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
    f.getContentPane().add(new LabeledColorWidget(map));
    f.pack();
    f.setVisible(true);

    f = new JFrame("VisAD LabeledColorWidget 1");
    f.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
    f.getContentPane().add(new LabeledColorWidget(map));
    f.pack();
    f.setVisible(true);
  }
}
