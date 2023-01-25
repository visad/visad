/*

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2023 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.rmi.RemoteException;

import visad.ScalarMap;
import visad.VisADException;

/**
 * A color widget that allows users to interactively map numeric data to
 * RGB/RGBA tuples in a <CODE>ScalarMap</CODE>.
 */
public class LabeledColorWidget
  extends JPanel
  implements ActionListener
{
  ColorMapWidget wrappedWidget;
  private float[][] original;
  private float[][] grey = null;

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
    this(new ColorMapWidget(smap, in_table, update));
  }

  /**
   *
   */
  public LabeledColorWidget(ColorMapWidget w)
  {
    wrappedWidget = w;

    // save the initial table
    original = wrappedWidget.copy_table(wrappedWidget.control.getTable());

    setLayout( new BorderLayout(5,5));
    add("Center",wrappedWidget);

    JPanel buttons = buildButtons();
    if (buttons != null) {
      add("South",buttons);
    }
  }

  /**
   * Build "Reset" and "Grey Scale" button panel.
   *
   * @return JPanel containing the buttons or <CODE>null</CODE> if
   *         the wrapped widget's button panel was used.
   */
  private JPanel buildButtons()
  {
    JButton reset = new JButton("Reset");
    reset.setActionCommand("reset");
    reset.addActionListener(this);

    JButton grey = new JButton("Grey Scale");
    grey.setActionCommand("grey");
    grey.addActionListener(this);

    boolean newPanel = false;
    JPanel panel = wrappedWidget.getButtonPanel();
    if (panel == null) {
      panel = new JPanel();
      panel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
      newPanel = true;
    }

    panel.add(reset, 0);
    panel.add(grey, 1);

    return (newPanel ? panel : null);
  }

  /**
   * Handle button presses.
   *
   * @param evt Data from the changed <CODE>JButton</CODE>.
   */
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getActionCommand().equals("reset")) {
      // reset color table to original values
      try {
        wrappedWidget.setTable(original);
      } catch (RemoteException re) {
      } catch (VisADException ve) {
      }
    } else if (evt.getActionCommand().equals("grey")) {
      // reset color table to grey wedge
      if (grey == null) {
        if (original != null && original[0] != null) {
          final int num = original.length;
          final int len = original[0].length;
          grey = new float[num][len];
          float a = 1.0f / (len - 1.0f);
          for (int j=0; j<len; j++) {
            grey[0][j] = grey[1][j] = grey[2][j] = j * a;
            if (num > 3) {
              grey[3][j] = 1.0f;
            }
          }
        }
      }

      if (grey != null) {
        try {
          wrappedWidget.setTable(grey);
        } catch (RemoteException re) {
        } catch (VisADException ve) {
        }
      }
    }
  }

  /**
   * Stub routine which calls <CODE>ColorMapWidget.getMaximumSize()</CODE>.
   *
   * @return Maximum size in <CODE>Dimension</CODE>.
   */
  public Dimension getMaximumSize()
  {
    return wrappedWidget.getMaximumSize();
  }

  /**
   * Stub routine which calls <CODE>ColorMapWidget.setMaximumSize()</CODE>.
   *
   * @param size Maximum size.
   */
  public void setMaximumSize(Dimension size)
  {
    wrappedWidget.setMaximumSize(size);
  }

  /**
   * Stub routine which calls <CODE>ColorMapWidget.getMinimumSize()</CODE>.
   *
   * @return Minimum size in <CODE>Dimension</CODE>.
   */
  public Dimension getMinimumSize()
  {
    return wrappedWidget.getMinimumSize();
  }

  /**
   * Stub routine which calls <CODE>ColorMapWidget.setMinimumSize()</CODE>.
   *
   * @param size Minimum size.
   */
  public void setMinimumSize(Dimension size)
  {
    wrappedWidget.setMinimumSize(size);
  }

  /**
   * Stub routine which calls <CODE>ColorMapWidget.getPreferredSize()</CODE>.
   *
   * @return Preferred size in <CODE>Dimension</CODE>.
   */
  public Dimension getPreferredSize()
  {
    return wrappedWidget.getPreferredSize();
  }

  /**
   * Stub routine which calls <CODE>ColorMapWidget.setPreferredSize()</CODE>.
   *
   * @param size Preferred size.
   */
  public void setPreferredSize(Dimension size)
  {
    wrappedWidget.setPreferredSize(size);
  }

  public BaseRGBMap getBaseMap() { return wrappedWidget.baseMap; }
  public ColorPreview getPreview() { return wrappedWidget.preview; }
  public ArrowSlider getSlider() { return wrappedWidget.slider; }

  /**
   * Use a new table of color values.
   * If immediate mode is off, changes to the associated color
   * control are not applied until the Apply button is clicked.
   *
   * @param table New color values.
   */
  public void setTable(float[][] table) { wrappedWidget.setTableView(table); }

  /**
   * Gets the widget's current table. If immediate mode is
   * off, it may not match the linked color control's table.
   */
  public float[][] getTable() { return wrappedWidget.getTableView(); }

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
      f.getContentPane().add(new LabeledColorWidget(visMap));
      f.pack();
      f.setVisible(true);

      f = new javax.swing.JFrame("1");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(new LabeledColorWidget(visMap, null));
      f.pack();
      f.setVisible(true);

      f = new javax.swing.JFrame("!Updated");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(new LabeledColorWidget(visMap, null, false));
      f.pack();
      f.setVisible(true);

      f = new javax.swing.JFrame("!Immediate");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      ColorMapWidget cmw = new ColorMapWidget(visMap, null, false, false);
      f.getContentPane().add(new LabeledColorWidget(cmw));
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
