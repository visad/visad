 
//
// VisADSlider.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
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
import java.awt.Graphics;

import com.sun.java.swing.BoxLayout;
import com.sun.java.swing.JLabel;
import com.sun.java.swing.JPanel;
import com.sun.java.swing.JSlider;

import com.sun.java.swing.event.ChangeEvent;
import com.sun.java.swing.event.ChangeListener;

import java.rmi.RemoteException;

import visad.CellImpl;
import visad.Data;
import visad.DataReference;
import visad.PlotText;
import visad.Real;
import visad.RealType;
import visad.RemoteCell;
import visad.RemoteCellImpl;
import visad.RemoteDataReference;
import visad.VisADException;

/**
   VisADSlider extends JPanel; it combines a JSlider and a JLabel
   and links them to a Real, via a DataReference.
*/
public class VisADSlider extends JPanel {

  private JSlider slider;
  private JLabel name_label;
  private JLabel value_label;		// alos used as synchronization var
  private ChangeListener listener;
  private DataReference ref;
  private RealType type;
  private String name;
  private double scale;
  private int low;
  private int hi;

  private SliderCell cell;
  private int lastCellValue;
  private int lastSliderValue;
  private boolean lastCellValueState;

  private String head;

  private int first;

  public VisADSlider(String n, int l, int h, int start,
                     double sc, DataReference r, RealType t)
         throws VisADException, RemoteException {

    ref = r;
    type = t;
    name = n;
    low = l;
    hi = h;
    scale = sc;
    first = -2; // what an ugly hack work-around for jdk1.2beta2 bug

    lastCellValue = low - 1;
    lastSliderValue = low - 1;
    lastCellValueState = false;

    Data real = ref.getData();
    boolean real_value = false;
    if (real != null && (real instanceof Real)) {
      double v = ((Real) real).getValue() / scale;
      if (v == v && low <= v && v <= hi) {
        start = (int) v;
        real_value = true;
        lastSliderValue = start;
      }
    }

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setAlignmentY(JPanel.TOP_ALIGNMENT);
    setAlignmentX(JPanel.LEFT_ALIGNMENT);

    slider = new JSlider(JSlider.HORIZONTAL, low, hi, start);
    slider.setMaximumSize(new Dimension(150, 20));

    listener = new SliderListener();
    slider.addChangeListener(listener);
 
    add(slider);

    name_label = new JLabel(name + " = ");
    add(name_label);

    double val = scale * slider.getValue();
    head = "         ";
    value_label = new JLabel(PlotText.shortString(val) + head);
    add(value_label);
 
    if (!real_value) {
      ref.setData(new Real(type, val));
    }

    cell = new SliderCell();
    if (ref instanceof RemoteDataReference) {
      RemoteCell remote_cell = new RemoteCellImpl(cell);
      remote_cell.addReference(ref);
    }
    else {
      cell.addReference(ref);
    }
  }

  /** slider state changes trigger changes to Real data object
      via DataReference */
  private class SliderListener implements ChangeListener {
 
    public void stateChanged(ChangeEvent e) {
      JSlider s1 = (JSlider)e.getSource();
      int ival = s1.getValue();
      synchronized (value_label) {
        if (ival == lastCellValue) {
          // don't respond to slider state changes triggered
          // by SliderCell
          if (!lastCellValueState) {
            lastCellValueState = true;
          }
        }
        else { // ival != lastCellValue
          if (lastCellValueState) {
            lastCellValue = low - 1;
          }
          lastSliderValue = ival;
          if (first > 0) {
            try {
              double val = scale * ival;
              ref.setData(new Real(type, val));
              head = "";
              value_label.setText(PlotText.shortString(val));
            }
            catch (VisADException ex) {
            }
            catch (RemoteException ex) {
            }
// hack for JDK 1.2 with Java3D
repaint();
Graphics g = getGraphics();
if (g != null) {
  update(g);
  g.dispose();
}
          }
          else {
            first++;
          }
        }
      }
    }
  }

  /** changes to ref Real value trigger changes to slider */
  private class SliderCell extends CellImpl {

    public void doAction() throws VisADException, RemoteException {
      synchronized (value_label) {
        double val = ((Real) ref.getData()).getValue();
        int ival = (int) (val / scale);
        ival = Math.min(Math.max(ival, low), hi);
        if (ival == lastSliderValue) {
          // don't respond to Real value changes triggered
          // by stateChanged
        }
        else {
          lastCellValue = ival;
          lastCellValueState = false;
          slider.setValue(ival);
if (first > 0) {
  // hack for JDK 1.2 with Java3D
  repaint();
  Graphics g = getGraphics();
  if (g != null) {
    update(g);
    g.dispose();
  }
}
else {
  first++;
}
          value_label.setText(PlotText.shortString(val) + head);
        }
      }
    }
  }
}
