/*

@(#) $Id: LabeledRGBAWidget.java,v 1.5 1998-08-10 13:45:42 billh Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 1998 Nick Rasmussen
VisAD is Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
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

import com.sun.java.swing.*;

/** 
 * A color widget that allows users to interactively map numeric data to
 * RGBA tuples based on the Vis5D color widget
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.5 $, $Date: 1998-08-10 13:45:42 $
 * @since Visad Utility Library v0.7.1
 */
public class LabeledRGBAWidget extends Panel implements ActionListener,
                                                        ScalarMapListener {

  private ArrowSlider slider;

  private ColorWidget widget;

  private SliderLabel label;

  private float[][] orig_table;

  /* CTR: 30 Jul 1998
  public LabeledRGBAWidget() {
    this(new ColorWidget(new RGBAMap()), new ArrowSlider());
  }

  public LabeledRGBAWidget(String name, float min, float max) {
    this(new ColorWidget(new RGBAMap()), new ArrowSlider(min, max, (min + max) / 2, name));
  }

  public LabeledRGBAWidget(String name, float min, float max, float[][] table) {
    this(new ColorWidget(new RGBAMap(table)), new ArrowSlider(min, max, (min + max) / 2, name));
  }

  public LabeledRGBAWidget(ColorWidget c, Slider s) {
    this(c, s, new SliderLabel(s));
  }

  public LabeledRGBAWidget(ColorWidget c, Slider s, SliderLabel l) {

    widget = c;
    slider = s;
    label = l;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(widget);
    add(slider);
    add(label);
  }
  */

  ColorAlphaControl color_alpha_control;

  /** construct a LabeledRGBAWidget linked to the ColorControl
      in map (which must be to Display.RGBA), with range of
      values from map.getRange() */
  public LabeledRGBAWidget(ScalarMap smap)
         throws VisADException, RemoteException {
    this(smap, smap.getRange(), null);
  }

  /** construct a LabeledRGBAWidget linked to the ColorControl
      in map (which must be to Display.RGBA), with range of
      values (min, max) */
  public LabeledRGBAWidget(ScalarMap smap, float min, float max)
         throws VisADException, RemoteException {
    this(smap, make_range(min, max), null);
    smap.setRange((double) min, (double) max);
  }

  /** construct a LabeledRGBAWidget linked to the ColorControl
      in map (which must be to Display.RGBA), with range of
      values (min, max), and initial color table in format
      float[TABLE_SIZE][4] with values between 0.0f and 1.0f */
  public LabeledRGBAWidget(ScalarMap smap, float min, float max, float[][] table)
         throws VisADException, RemoteException {
    this(smap, make_range(min, max), table);
    smap.setRange((double) min, (double) max);
  }

  private LabeledRGBAWidget(ScalarMap smap, double[] range, float[][] in_table)
                                      throws VisADException, RemoteException {

    /* CTR: 30 Jul 1998: consolidated constructor code */

    String name = smap.getScalar().getName();
    float min = (float) range[0];
    float max = (float) range[1];
    float[][] table = table_reorg(in_table);

    if (min != min || max != max) {
      // fake min and max
      min = 0.0f;
      max = 1.0f;
      // listen for real min and max
      smap.addScalarMapListener(this);
    }

    ColorWidget c = new ColorWidget(new RGBAMap(table));
    ArrowSlider s = new ArrowSlider(min, max, (min + max) / 2, name);
    SliderLabel l = new SliderLabel(s);

    widget = c;
    slider = s;
    label = l;
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

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(widget);
    add(slider);
    add(label);
    add(reset);

    /* CTR: end of consolidated code */

    /* CTR: 30 Jul 1998
    this(smap.getScalar().getName(), (float) range[0], (float) range[1],
         table_reorg(in_table));
    */
    if (!Display.RGBA.equals(smap.getDisplayScalar())) {
      throw new DisplayException("LabeledRGBAWidget: ScalarMap must " +
                                 "be to Display.RGBA");
    }
    if (range[0] != range[0] || range[1] != range[1] ||
        Double.isInfinite(range[0]) || Double.isInfinite(range[1]) ||
        range[0] == Double.MAX_VALUE || range[1] == -Double.MAX_VALUE) {
      throw new DisplayException("LabeledRGBAWidget: bad range");
    }
    color_alpha_control = (ColorAlphaControl) smap.getControl();
 
    ColorMap map = widget.getColorMap();
    final int TABLE_SIZE = map.getMapResolution();
    final float SCALE = 1.0f / (TABLE_SIZE - 1.0f);

    /* CTR: 30 Jul 1998 */
    if (table == null) {
      table = new float[4][TABLE_SIZE];

      for (int i=0; i<TABLE_SIZE; i++) {
        float[] t = map.getTuple(SCALE * i);
        table[0][i] = t[0];
        table[1][i] = t[1];
        table[2][i] = t[2];
        table[3][i] = t[3];
      }
      color_alpha_control.setTable(table);
      orig_table = copy_table(table);
    }
    else {
      color_alpha_control.setTable(in_table);
      orig_table = copy_table(in_table);
    }

    widget.addColorChangeListener(new ColorChangeListener() {
      public void colorChanged(ColorChangeEvent e) {
        ColorMap map_e = widget.getColorMap();
        float[][] table_e = new float[4][TABLE_SIZE];
        for (int i=0; i<TABLE_SIZE; i++) {
          float[] t = map_e.getTuple(SCALE * i);
          table_e[0][i] = t[0];
          table_e[1][i] = t[1];
          table_e[2][i] = t[2];
          table_e[3][i] = t[3];
        }
        try {
          color_alpha_control.setTable(table_e);
        }
        catch (VisADException f) {
        }
        catch (RemoteException f) {
        }
      }
    });
  }

  private Dimension d = null;

  public Dimension getMaximumSize() {
    if (d != null) return d;
    else return super.getMaximumSize();
  }

  public void setMaximumSize(Dimension dd) {
    d = dd;
  }

  /** ScalarMapListener method used with delayed auto-scaling. */
  public void mapChanged(ScalarMapEvent e) {
    ScalarMap s = e.getScalarMap();
    double[] range = s.getRange();
    double val = slider.getValue();
    if (val <= range[0] || val >= range[1]) {
      val = (range[0]+range[1])/2;
      
    }
    slider.setBounds((float) range[0], (float) range[1], (float) val);
  }

  /** ActionListener method used with resetting color table. */
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("reset")) {
      // reset color table to original values
      try {
        float[][] table = copy_table(orig_table);
        color_alpha_control.setTable(table);
        ((RGBAMap) widget.getColorMap()).setValues(table_reorg(table));
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  }

  private static float[][] copy_table(float[][] table) {
    if (table == null || table[0] == null) return null;
    int len = table[0].length;
    float[][] new_table = new float[4][len];
    try {
      for (int i=0; i<4; i++) {
        System.arraycopy(table[i], 0, new_table[i], 0, len);
      }
      return new_table;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  private static float[][] table_reorg(float[][] table) {
    if (table == null || table[0] == null) return null;
    try {
      int len = table[0].length;
      float[][] out = new float[len][4];
      for (int i=0; i<len; i++) {
        out[i][0] = table[0][i];
        out[i][1] = table[1][i];
        out[i][2] = table[2][i];
        out[i][3] = table[3][i];
      }
      return out;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  private static double[] make_range(float min, float max) {
    double[] range = {(double) min, (double) max};
    return range;
  }

  /** Returns the ColorMap that the color widget is currently pointing to */
  public ColorWidget getColorWidget() {
    return widget;
  }

  /* CTR: 30 Jul 1998
  / * * for debugging purposes * /
  public static void main(String[] argc) {

    LabeledRGBAWidget l = new LabeledRGBAWidget("label", 0, 1);

    Frame f = new Frame("Visad Widget Test");
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    f.add(l);

    f.setSize(f.getPreferredSize());
    f.setVisible(true);

  }
  */
  
}

