 
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

import visad.*;

import java.util.*;
import java.rmi.*;

// import com.sun.java.swing.*;
// import com.sun.java.swing.event.*;

import java.awt.swing.*;
import java.awt.swing.event.*;

import java.awt.*;
import java.awt.event.*;

/**
   VisADSlider extends JPanel; it combines a JSlider and a JLabel
   and links them to a Real, via a DataReference.
*/
public class VisADSlider extends JPanel {

  private JSlider slider;
  private JLabel slider_label;
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

    Data real = ref.getData();
    if (real != null && (real instanceof Real)) {
      double v = ((Real) real).getValue() / scale;
      if (v == v && low <= v && v <= hi) start = (int) v;
    }

    slider = new JSlider(JSlider.HORIZONTAL, low, hi, start);
    double val = scale * slider.getValue();
    head = "         ";
    slider_label = new JLabel(name + " = " + PlotText.shortString(val) + head);
 
    listener = new SliderListener();
    slider.addChangeListener(listener);
 
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setAlignmentY(JPanel.TOP_ALIGNMENT);
    setAlignmentX(JPanel.LEFT_ALIGNMENT);
    slider.setMaximumSize(new Dimension(150, 20));
    add(slider);
    add(slider_label);
    ref.setData(new Real(type, val));

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
      if (ival == lastCellValue) {
        // don't respond to slider state changes triggered
        // by SliderCell
        lastCellValue = low - 1; // but only ignore first
      }
      else { // ival != lastCellValue
        lastSliderValue = ival;
        if (first > 0) {
          try {
            double val = scale * ival;
            ref.setData(new Real(type, val));
            head = "";
            slider_label.setText(name + " = " + PlotText.shortString(val));
          }
          catch (VisADException ex) {
          }
          catch (RemoteException ex) {
          }
// hack for JDK 1.2 with Java3D
update(getGraphics());
        }
        else {
          first++;
        }
      }
    }
  }

  /** changes to ref Real value trigger changes to slider */
  private class SliderCell extends CellImpl {

    public void doAction() throws VisADException, RemoteException {
      double val = ((Real) ref.getData()).getValue();
      int ival = (int) (val / scale);
      ival = Math.min(Math.max(ival, low), hi);
      if (ival == lastSliderValue) {
        // don't respond to Real value chnages triggered
        // by stateChanged
      }
      else {
        lastCellValue = ival;
        slider.setValue(ival);
if (first > 0) {
  // hack for JDK 1.2 with Java3D
  update(getGraphics());
}
else {
  first++;
}
        slider_label.setText(name + " = " + PlotText.shortString(val) + head);
      }
    }
  }

}

