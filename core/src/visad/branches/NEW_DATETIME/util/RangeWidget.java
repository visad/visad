
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

  private JTextField data_low, data_hi;

  private ScalarMap map;

  /** construct a RangeWidget linked to the ScalarMap smap */
  public RangeWidget(ScalarMap smap) throws VisADException {
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
    data_low = new JTextField();
    data_hi = new JTextField();
    updateTextFields(data);

    // limit JTextField heights
    Dimension msize = data_low.getMaximumSize();
    Dimension psize = data_low.getPreferredSize();
    msize.height = psize.height;
    data_low.setMaximumSize(msize);
    data_hi.setMaximumSize(msize);

    // create JPanel
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.add(new JLabel("low: "));
    p.add(data_low);
    p.add(new JLabel(" hi: "));
    p.add(data_hi);

    // lay out GUI
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(new JLabel("Range of " + map.getScalar().getName() + " mapped to " +
                   map.getDisplayScalar().getName().substring(7)));
    add(p);

    // add listeners
    map.addScalarMapListener(this);
    data_low.addActionListener(this);
    data_hi.addActionListener(this);
  }

  private void updateTextFields(double[] data) {
    data_low.setText(PlotText.shortString(data[0]));
    data_hi.setText(PlotText.shortString(data[1]));
  }

  /** handle JTextField changes */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    double[] data = new double[2];
    data[0] = Double.parseDouble(data_low.getText());
    data[1] = Double.parseDouble(data_hi.getText());
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

