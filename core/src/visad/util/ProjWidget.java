//
// ProjWidget.java
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

import javax.swing.*;
import java.rmi.RemoteException;
import visad.*;

/** A widget that allows users to save and restore different projections.<P> */
public class ProjWidget extends JPanel {

  /** This ProjWidget's associated control */
  ProjectionControl control;

  JComboBox savedViewList;
  JButton save;

  double[][] savedViews;

  /** Constructs a ProjWidget linked to the ProjectionControl pc */
  public ProjWidget(ProjectionControl pc) {
    control = pc;

    // get initial view from control
    savedViews = new double[1][];
    savedViews[0] = control.getMatrix();

    // set up layouts
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    // construct JComboBox
    savedViewList = new JComboBox();
    savedViewList.setEditable(false);
    savedViewList.addItem("Position 1");
    savedViewList.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = savedViewList.getSelectedIndex();
        try {
          control.setMatrix(savedViews[index]);
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }
    });

    // construct JButton
    save = new JButton("Save");
    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int num = savedViews.length;
        double[][] d = new double[num+1][];
        System.arraycopy(savedViews, 0, d, 0, num);
        d[num] = control.getMatrix();
        savedViews = d;
        savedViewList.addItem("Position " + (num + 1));
        savedViewList.setSelectedIndex(num);
      }
    });

    // lay out JComponents
    add(savedViewList);
    add(Box.createRigidArea(new Dimension(5, 0)));
    add(save);
  }

}

