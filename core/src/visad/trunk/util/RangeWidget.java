
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

  private JTextField data_min, data_max;

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
    data_min = new JTextField();
    data_max = new JTextField();
    updateTextFields(data);

    // limit JTextField heights
    Dimension msize = data_min.getMaximumSize();
    Dimension psize = data_min.getPreferredSize();
    msize.height = psize.height;
    data_min.setMaximumSize(msize);
    data_max.setMaximumSize(msize);

    // create JPanel
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.add(new JLabel("min: "));
    p.add(data_min);
    p.add(new JLabel(" max: "));
    p.add(data_max);

    // lay out GUI
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(new JLabel(map.toString().trim()));
    add(p);

    // add listeners
    map.addScalarMapListener(this);
    data_min.addActionListener(this);
    data_max.addActionListener(this);
  }

  private void updateTextFields(double[] data) {
    data_min.setText(PlotText.shortString(data[0]));
    data_max.setText(PlotText.shortString(data[1]));
  }

  /** handle JTextField changes */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    double[] data = new double[2];
    data[0] = Double.parseDouble(data_min.getText());
    data[1] = Double.parseDouble(data_max.getText());
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
    updateTextFields(data);
  }

}

