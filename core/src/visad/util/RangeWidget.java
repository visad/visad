//
// RangeWidget.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.util;

import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.rmi.RemoteException;

import javax.swing.*;

import visad.*;
import visad.browser.Convert;

/** A widget that allows users to specify a ScalarMap's range scaling */
public class RangeWidget extends JPanel implements ActionListener,
                                                   ScalarMapListener {

  private JTextField dataLow, dataHi;

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
    dataLow = new JTextField();
    dataHi = new JTextField();
    updateTextFields(data);

    // limit JTextField heights
    Dimension msize = dataLow.getMaximumSize();
    Dimension psize = dataLow.getPreferredSize();
    msize.height = psize.height;
    dataLow.setMaximumSize(msize);
    dataHi.setMaximumSize(msize);

    // create JPanel
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.add(new JLabel("low: "));
    p.add(dataLow);
    p.add(new JLabel(" hi: "));
    p.add(dataHi);

    // lay out GUI
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(new JLabel("Range of " + map.getScalarName() + " mapped to " +
                   map.getDisplayScalar().getName().substring(7)));
    add(p);

    // add listeners
    map.addScalarMapListener(this);
    dataLow.addActionListener(this);
    dataHi.addActionListener(this);
  }

  private void updateTextFields(double[] data) {
    // do not update range with truncated values
    dataLow.removeActionListener(this);
    dataHi.removeActionListener(this);

    if (data[0] < data[1]) {
      dataLow.setText(Convert.shortString(data[0], Convert.ROUND_DOWN));
      dataHi.setText(Convert.shortString(data[1], Convert.ROUND_UP));
    }
    else {
      dataLow.setText(Convert.shortString(data[0], Convert.ROUND_UP));
      dataHi.setText(Convert.shortString(data[1], Convert.ROUND_DOWN));
    }

    dataLow.addActionListener(this);
    dataHi.addActionListener(this);
  }

  private void updateScalarMap(double[] data) {
    try {
      map.setRange(data[0], data[1]);
    }
    catch (VisADException exc) {
// exc.printStackTrace();
    }
    catch (RemoteException exc) {
// exc.printStackTrace();
    }
  }

  /** handle JTextField changes */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    double[] data = new double[2];
    data[0] = Double.parseDouble(dataLow.getText());
    data[1] = Double.parseDouble(dataHi.getText());
// System.out.println("actionPerformed " + data[0] + " " + data[1]);
    updateScalarMap(data);
  }

  /** handle ScalarMap changes */
  public void mapChanged(ScalarMapEvent e) {
    double[] so = new double[2];
    double[] data = new double[2];
    double[] display = new double[2];
    map.getScale(so, data, display);
    updateTextFields(data);
  }

  /**
   * Don't care about <CODE>ScalarMap</CODE> control changes.
   */
  public void controlChanged(ScalarMapControlEvent evt) { }
}
