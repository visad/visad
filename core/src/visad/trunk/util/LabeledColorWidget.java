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

import visad.ScalarMap;
import visad.VisADException;

/**
 * A color widget that allows users to interactively map numeric data to
 * RGB/RGBA tuples in a <CODE>ScalarMap</CODE>.
 */
public class LabeledColorWidget
  extends Panel
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

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(wrappedWidget);

    Panel buttons = buildButtons();
    if (buttons != null) {
      add(buttons);
    }
  }

  /**
   * Build "Reset" and "Grey Scale" button panel.
   *
   * @return Panel containing the buttons or <CODE>null</CODE> if
   *         the wrapped widget's button panel was used.
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

    boolean newPanel = false;
    Panel panel = wrappedWidget.getButtonPanel();
    if (panel == null) {
      panel = new Panel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      newPanel = true;
    }

    panel.add(reset, 0);
    panel.add(grey, 1);

    return (newPanel ? panel : null);
  }

  /**
   * Handle button presses.
   *
   * @param evt Data from the changed <CODE>Button</CODE>.
   */
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getActionCommand().equals("reset")) {
      // reset color table to original values
      wrappedWidget.setTable(original);
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
        wrappedWidget.setTable(grey);
      }
    }
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
      f.getContentPane().add(new LabeledColorWidget(map));
      f.pack();
      f.setVisible(true);

      f = new javax.swing.JFrame("1");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(new LabeledColorWidget(map, null));
      f.pack();
      f.setVisible(true);

      f = new javax.swing.JFrame("!Updated");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(new LabeledColorWidget(map, null, false));
      f.pack();
      f.setVisible(true);

      f = new javax.swing.JFrame("!Immediate");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      ColorMapWidget cmw = new ColorMapWidget(map, null, false, false);
      f.getContentPane().add(new LabeledColorWidget(cmw));
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
