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

import java.awt.Component;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.rmi.RemoteException;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import visad.ScalarMap;
import visad.VisADException;

/**
 * A color widget that allows users to interactively map numeric data to
 * RGB/RGBA tuples in a <CODE>ScalarMap</CODE>, and to choose from
 * a drop-down list of canned colormap combinations.
 */
public class ChosenColorWidget
  extends JPanel
  implements ActionListener
{
  ColorMapWidget wrappedWidget;
  private float[][] original;
  private JComboBox choice;

  /**
   * This class is a glorified data structure used to link
   * an item's name and color values.
   */
  class ComboItem
  {
    private String name;
    private float[][] table;

    /**
     * Link together the specified name and color values.
     *
     * @param name Name of this table.
     * @param table Color values.
     */
    private ComboItem(String name, float[][] table)
    {
      this.name = name;
      this.table = table;
    }

    /**
     * Return the name for the benefit of the <CODE>JComboBox</CODE>.
     *
     * @return Name of this table.
     */
    public String toString() { return name; }
  }

  /**
   * Construct a <CODE>ChosenColorWidget</CODE> linked to the
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
  public ChosenColorWidget(ScalarMap smap)
    throws VisADException, RemoteException
  {
    this(new ColorMapWidget(smap));
  }

  /**
   * Wrap a <CODE>ChosenColorWidget</CODE> around the specified
   * <CODE>ColorMapWidget</CODE>.
   *
   * @param w The <CODE>ColorMapWidget</CODE>.
   */
  public ChosenColorWidget(ColorMapWidget w)
  {
    wrappedWidget = w;

    // save the original table
    original = wrappedWidget.copy_table(wrappedWidget.control.getTable());

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(wrappedWidget);
    add(buildChoices());
  }

  /**
   * Build color choice box.
   *
   * @return JPanel containing the choices or <CODE>null</CODE> if
   *         the wrapped widget's button panel was used.
   */
  private Component buildChoices()
  {
    choice = new JComboBox();
    choice.addItem(new ComboItem("Original", original));
    choice.addActionListener(this);

    boolean usedPanel = false;
    JPanel panel = wrappedWidget.getButtonPanel();
    if (panel != null) {
      panel.add(choice);
      usedPanel = true;
    }

    return (usedPanel ? (Component )panel : (Component )choice);
  }

  /**
   * Return the number of rows in the table (3 for an RGB-based table,
   * 4 for an RGBA-based table.)
   *
   * @return The number of rows.
   *
   * @exception VisADException If the size of the current table cannot
   *                           be determined.
   */
  public int getNumberOfRows()
    throws VisADException
  {
    if (original == null) {
      throw new VisADException("Unknown original table size");
    }

    return original.length;
  }

  /**
   * Return the table's "resolution" (aka the length of its rows.)
   *
   * @return The row length.
   *
   * @exception VisADException If the size of the current table cannot
   *                           be determined.
   */
  public int getRowLength()
    throws VisADException
  {
    if (original == null || original[0] == null) {
      throw new VisADException("Unknown original table size");
    }

    return original[0].length;
  }

  /**
   * Add the standard "Grey Wedge" item to the list of choices.
   *
   * @exception VisADException If the size of the current table cannot
   *                           be determined.
   */
  public void addGreyWedgeItem()
    throws VisADException
  {
    if (original == null || original[0] == null) {
      throw new VisADException("Unknown original table size");
    }

    final int num = original.length;
    final int len = original[0].length;
    float[][] table = new float[num][len];
    final float step = 1.0f / (len - 1.0f);
    float total = 0.0f;
    for (int j=0; j<len; j++) {
      table[0][j] = table[1][j] = table[2][j] = total;
      if (num > 3) {
        table[3][j] = 1.0f;
      }
      total += step;
    }

    addItem("Grey Wedge", table);
  }

  /**
   * Add a color lookup table to the list of choices.
   *
   * @param name Name of this table.
   * @param table Table of colors.
   *
   * @exception VisADException If there is a problem with the table.
   */
  public void addItem(String name, float[][] table)
    throws VisADException
  {
    if (original == null || original[0] == null) {
      throw new VisADException("Unknown original table size");
    }
    if (table == null) {
      throw new VisADException("Null table");
    }
    for (int i = table.length - 1; i >= 0; i--) {
      if (table[i] == null || table[i].length != original[i].length) {
        throw new VisADException("Table row " + i + " should have " +
                                 original[i].length + " elements");
      }
    }

    choice.addItem(new ComboItem(name, table));
  }

  /**
   * Handle selections from the <CODE>JComboBox</CODE>.
   *
   * @param evt Data from the selected choice.
   */
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getActionCommand().equals("comboBoxChanged")) {
      ComboItem item = (ComboItem )choice.getSelectedItem();
      // reset color table to chosen values
      try {
        wrappedWidget.setTable(item.table);
      } catch (RemoteException re) {
      } catch (VisADException ve) {
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

  public static void main(String[] args)
  {
    try {
      visad.RealType vis = visad.RealType.getRealType("vis");
      ScalarMap map = new ScalarMap(vis, visad.Display.RGBA);
      map.setRange(0.0f, 1.0f);

      visad.DisplayImpl dpy = new visad.java2d.DisplayImplJ2D("2d");
      dpy.addMap(map);

      javax.swing.JFrame f;
      ChosenColorWidget chosen;

      // build first widget
      chosen = new ChosenColorWidget(map);
      chosen.addGreyWedgeItem();

      // wrap frame around first widget
      f = new javax.swing.JFrame("0");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(chosen);
      f.pack();
      f.setVisible(true);

      // build second widget
      chosen = new ChosenColorWidget(new ColorMapWidget(map, null));
      final int num = chosen.getNumberOfRows();
      final int len = chosen.getRowLength();
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
      chosen.addItem("Reverse Wedge", table);

      // wrap frame around second widget
      f = new javax.swing.JFrame("1");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      f.getContentPane().add(chosen);
      f.pack();
      f.setVisible(true);

      // wrap frame around third widget
      f = new javax.swing.JFrame("!Updated");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      ColorMapWidget cmw3 = new ColorMapWidget(map, null, false);
      f.getContentPane().add(new ChosenColorWidget(cmw3));
      f.pack();
      f.setVisible(true);

      f = new javax.swing.JFrame("!Immediate");
      f.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent we) {
            System.exit(0);
          }
        });
      ColorMapWidget cmw = new ColorMapWidget(map, null, true, false);
      f.getContentPane().add(new ChosenColorWidget(cmw));
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
