
//
// RangeWidget.java
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

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;

/** A widget that allows users to specify a ScalarMap's range scaling */
public class RangeWidget extends JPanel implements ActionListener,
                                                   ScalarMapListener {

  private JTextField so_min, so_max;
  private JTextField data_min, data_max;
  private JTextField display_min, display_max;

  private ScalarMap map;

  /** construct a RangeWidget linked to the ScalarMap smap */
  public RangeWidget(ScalarMap smap) throws VisADException, RemoteException {
    // verify scalar map
    map = smap;
    if (map == null) {
      throw new VisADException("RangeWidget: ScalarMap cannot be null");
    }
    double[] so = new double[2];
    double[] data = new double[2];
    double[] display = new double[2];
    boolean scale = map.getScale(so, data, display);
    if (!scale) {
      throw new VisADException("RangeWidget: ScalarMap must have " +
                               "linearly scalable range");
    }

    // create JTextFields
    so_min = new JTextField();
    so_max = new JTextField();
    data_min = new JTextField();
    data_max = new JTextField();
    display_min = new JTextField();
    display_max = new JTextField();
    updateTextFields(so, data, display);

    // limit JTextField heights
    Dimension msize = so_min.getMaximumSize();
    Dimension psize = so_min.getPreferredSize();
    msize.height = psize.height;
    so_min.setMaximumSize(msize);
    so_max.setMaximumSize(msize);
    data_min.setMaximumSize(msize);
    data_max.setMaximumSize(msize);
    display_min.setMaximumSize(msize);
    display_max.setMaximumSize(msize);

    // lay out GUI in grid format
    setLayout(new GridLayout(4, 3));
    add(Box.createRigidArea(new Dimension(1, 1)));
    add(new JLabel("  min:  "));
    add(new JLabel("  max:  "));
    add(new JLabel("so:"));
    add(so_min);
    add(so_max);
    add(new JLabel("data:"));
    add(data_min);
    add(data_max);
    add(new JLabel("display:  "));
    add(display_min);
    add(display_max);

    // add listeners
    map.addScalarMapListener(this);
    so_min.addActionListener(this);
    so_max.addActionListener(this);
    data_min.addActionListener(this);
    data_max.addActionListener(this);
    display_min.addActionListener(this);
    display_max.addActionListener(this);
  }

  private void updateTextFields(double[] so, double[] data, double[] display) {
    so_min.setText(PlotText.shortString(so[0]));
    so_max.setText(PlotText.shortString(so[1]));
    data_min.setText(PlotText.shortString(data[0]));
    data_max.setText(PlotText.shortString(data[1]));
    display_min.setText(PlotText.shortString(display[0]));
    display_max.setText(PlotText.shortString(display[1]));
  }

  /** handle JTextField changes */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    double[] so = new double[2];
    double[] data = new double[2];
    double[] display = new double[2];
    so[0] = Double.parseDouble(so_min.getText());
    so[1] = Double.parseDouble(so_max.getText());
    data[0] = Double.parseDouble(data_min.getText());
    data[1] = Double.parseDouble(data_max.getText());
    display[0] = Double.parseDouble(display_min.getText());
    display[1] = Double.parseDouble(display_max.getText());
    try {
      map.setRange(data[0], data[1]);
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
  }

  /** handle ScalarMap changes */
  public void mapChanged(ScalarMapEvent e) {
    double[] so = new double[2];
    double[] data = new double[2];
    double[] display = new double[2];
    map.getScale(so, data, display);
    updateTextFields(so, data, display);
  }

}

