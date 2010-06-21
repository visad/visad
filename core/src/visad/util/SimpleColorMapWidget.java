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

import java.rmi.RemoteException;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import visad.BaseColorControl;
import visad.Control;
import visad.DisplayException;
import visad.VisADException;

/**
 * A "simple" color widget that allows users to interactively map numeric
 * data to RGB/RGBA color maps.
 */
public class SimpleColorMapWidget
  extends JPanel
{
  BaseRGBMap baseMap;
  ColorPreview preview;
  ArrowSlider slider;

  private SliderLabel label;

  /**
   * Construct a <CODE>SimpleColorMapWidget</CODE>.
   *
   * The initial color table (if non-null)
   * should be a <CODE>float[resolution][dimension]</CODE>, where
   * <CODE>dimension</CODE> is either
   * <CODE>3</CODE> for <CODE>Display.RGB</CODE> or
   * <CODE>4</CODE> for <CODE>Display.RGB</CODE>) with values
   * between <CODE>0.0f</CODE> and <CODE>1.0f</CODE>.
   *
   * @param name Name used for slider label.
   * @param in_table Initial table of color values.
   * @param min Minimum value for slider.
   * @param max Maximum value for slider.
   *
   * @deprecated Use SimpleColorMapWidget(String, BaseColorControl, float, float)
   */
  public SimpleColorMapWidget(String name, float[][] in_table,
                              float min, float max)
    throws RemoteException, VisADException
  {
    float[][] table;
    if (in_table != null && in_table[0] != null &&
        in_table.length >= 3 && in_table.length <= 4 &&
        in_table[0].length > 4)
    {
      table = table_reorg(in_table);
    } else {
      table = in_table;
    }

    if (table != null && (table[0] == null || table[0].length < 3 ||
                          table[0].length > 4))
    {
      throw new VisADException("Bad initial table");
    }

    if (table == null) {
      baseMap = new BaseRGBMap(false);
    } else {
      baseMap = new BaseRGBMap(table);
    }

    finishInit(name, min, max);
  }

  /**
   * Construct a <CODE>SimpleColorMapWidget</CODE>.
   *
   * The initial color table (if non-null)
   * should be a <CODE>float[resolution][dimension]</CODE>, where
   * <CODE>dimension</CODE> is either
   * <CODE>3</CODE> for <CODE>Display.RGB</CODE> or
   * <CODE>4</CODE> for <CODE>Display.RGB</CODE>) with values
   * between <CODE>0.0f</CODE> and <CODE>1.0f</CODE>.
   *
   * @param name Name used for slider label.
   * @param ctl Control to which this widget is attached.
   * @param min Minimum value for slider.
   * @param max Maximum value for slider.
   */
  public SimpleColorMapWidget(String name, Control ctl,
                              float min, float max)
    throws RemoteException, VisADException
  {
    if (ctl == null) {
      throw new DisplayException(getClass().getName() + ": Null control");
    }

    if (!(ctl instanceof BaseColorControl)) {
      throw new DisplayException(getClass().getName() + ": Control must " +
                                 "be BaseColorControl, not " +
                                 ctl.getClass().getName());
    }

    baseMap = new BaseRGBMap((BaseColorControl )ctl);

    finishInit(name, min, max);
  }

  private void finishInit(String name, float min, float max)
  {
    // set up user interface
    preview = new ColorPreview(baseMap);
    slider = new ArrowSlider(min, max, (min + max) / 2, name);
    label = new SliderLabel(slider);

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // set min/max bounds for slider
    updateSlider(min, max);

    rebuildGUI();
  }

  void rebuildGUI()
  {
    removeAll();
    add(baseMap);
    add(preview);
    add(slider);
    add(label);
  }

  /**
   * Internal convenience routine used to update the slider.
   *
   * @param min Minimum value for slider.
   * @param max Maximum value for slider.
   */
  void updateSlider(float min, float max)
  {
    float val = slider.getValue();
    if (val != val || val <= min || val >= max) {
      val = (min + max) / 2;
    }
    slider.setBounds(min, max, val);
  }

  public BaseRGBMap getBaseMap() { return baseMap; }
  public ColorPreview getPreview() { return preview; }
  public ArrowSlider getSlider() { return slider; }

  /**
   * Use a new table of color values.
   *
   * @param table New color values.
   */
  public void setTable(float[][] table)
    throws RemoteException, VisADException
  {
    float[][] newTable = copy_table(table);
    baseMap.setValues(newTable);
  }

  /**
   * Utility routine used to make a copy of a 2D <CODE>float</CODE> array.
   *
   * @param table Table to copy.
   *
   * @return The new copy.
   */
  static float[][] copy_table(float[][] table)
  {
    if (table == null || table[0] == null) {
      return null;
    }

    final int dim = table.length;
    int len = table[0].length;

    float[][] new_table = new float[dim][len];
    try {
      for (int i=0; i<dim; i++) {
        System.arraycopy(table[i], 0, new_table[i], 0, len);
      }
      return new_table;
    } catch (ArrayIndexOutOfBoundsException obe) {
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
  static float[][] table_reorg(float[][] table)
  {
    if (table == null || table[0] == null) {
      return null;
    }

    final int dim = table.length;
    int len = table[0].length;

    float[][] out = new float[len][dim];
    try {
      for (int i=0; i<len; i++) {
        out[i][0] = table[0][i];
        out[i][1] = table[1][i];
        out[i][2] = table[2][i];
        if (dim > 3) {
          out[i][3] = table[3][i];
        }
      }
    } catch (ArrayIndexOutOfBoundsException obe) {
      out = null;
    }

    return out;
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    try {
      javax.swing.JFrame f;
      SimpleColorMapWidget simple;

      f = new javax.swing.JFrame("Empty SimpleColorMapWidget");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent e) {
            System.exit(0);
          }
        });
      simple = new SimpleColorMapWidget("Foo", (float [][])null, 0.0f, 1.0f);
      f.getContentPane().add(simple);
      f.pack();
      f.setVisible(true);

      final int num = 4;
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

      f = new javax.swing.JFrame("Full SimpleColorMapWidget");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent e) {
            System.exit(0);
          }
        });
      simple = new SimpleColorMapWidget("Foo", table, 0.0f, 1.0f);
      f.getContentPane().add(simple);
      f.pack();
      f.setVisible(true);
    } catch (VisADException ve) {
      ve.printStackTrace();
    }
  }
}
