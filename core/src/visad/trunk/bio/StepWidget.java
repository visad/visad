//
// StepWidget.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.rmi.RemoteException;
import visad.*;

/** StepWidget is a GUI component for stepping through a time stack. */
public class StepWidget extends JPanel implements ActionListener,
  ChangeListener, ControlListener, ScalarMapListener
{
  private static final boolean DEBUG = true;
  private static final int BUTTON_WIDTH = 25;
  private static final int BUTTON_HEIGHT = 25;
  private static final int GAP = 5;

  private JSlider step;
  private JButton up;
  private JButton down;

  private ScalarMap smap;
  private AnimationControl control;

  /** Constructs a StepWidget. */
  public StepWidget() throws VisADException, RemoteException {
    // create panels
    JPanel top = new JPanel();
    JPanel bottom = new JPanel();
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));

    // create components
    up = new BasicArrowButton(BasicArrowButton.NORTH) {
      public Dimension getPreferredSize() {
        return new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
      }
      public Dimension getMaximumSize() {
        return new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
      }
    };
    step = new JSlider(JSlider.VERTICAL, 1, 1, 1);
    down = new BasicArrowButton(BasicArrowButton.SOUTH) {
      public Dimension getPreferredSize() {
        return new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
      }
      public Dimension getMaximumSize() {
        return new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
      }
    };
    step.setPaintTicks(true);
    step.setAlignmentX(JButton.LEFT_ALIGNMENT);

    // lay out components
    add(top);
    add(Box.createRigidArea(new Dimension(0, GAP)));
    add(step);
    add(Box.createRigidArea(new Dimension(0, GAP)));
    add(bottom);
    top.add(Box.createHorizontalGlue());
    top.add(up);
    top.add(Box.createHorizontalGlue());
    bottom.add(Box.createHorizontalGlue());
    bottom.add(down);
    bottom.add(Box.createHorizontalGlue());

    // listen for button press events
    step.addChangeListener(this);
    up.addActionListener(this);
    down.addActionListener(this);

    // disable controls
    step.setEnabled(false);
    up.setEnabled(false);
    down.setEnabled(false);
  }

  /** Returns the minimum size of the StepWidget. */
  public Dimension getMinimumSize() {
    Dimension min = getPreferredSize();
    return new Dimension(min.width, 0);
  }

  /** Returns the maximum size of the StepWidget. */
  public Dimension getMaximumSize() {
    Dimension max = getPreferredSize();
    return new Dimension(max.width, Integer.MAX_VALUE);
  }

  /** Links the StepWidget with the given scalar map. */
  public void setMap(ScalarMap smap) throws VisADException, RemoteException {
    // verify scalar map
    if (smap != null && !Display.Animation.equals(smap.getDisplayScalar())) {
      throw new DisplayException("StepWidget: " +
        "ScalarMap must be to Display.Animation");
    }

    // remove old listeners
    if (this.smap != null) smap.removeScalarMapListener(this);
    if (control != null) control.removeControlListener(this);

    // get control values
    this.smap = smap;
    control = (AnimationControl) smap.getControl();
    fixControlUI();

    // add listeners
    if (control != null) control.addControlListener(this);
    if (smap != null) smap.addScalarMapListener(this);
  }

  private void fixControlUI() {
    // update slider ticks
    int max = 1;
    int cur = 1;
    if (control == null) {
      // disable controls
      step.setEnabled(false);
      up.setEnabled(false);
      down.setEnabled(false);
    }
    else {
      step.setEnabled(true);
      up.setEnabled(true);
      down.setEnabled(true);
      try {
        Set set = control.getSet();
        if (set != null) max = set.getLength();
      }
      catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
      cur = control.getCurrent() + 1;
      if (cur < 1) cur = 1;
      else if (cur > max) cur = max;
    }
    step.setMinimum(1);
    step.setMaximum(max);
    step.setValue(cur);

    int maj;
    if (max < 20) maj = max / 4;
    else if (max < 30) maj = max / 6;
    else maj = max / 8;

    step.setMajorTickSpacing(maj);
    step.setMinorTickSpacing(maj / 4);
    step.setPaintLabels(true);
  }

  /** ActionListener method used with JButtons. */
  public void actionPerformed(ActionEvent e) {
    if (control == null) {
      if (DEBUG) System.out.println("null AnimationControl");
      return;
    }
    Object o = e.getSource();
    if (o == up) {
      // move up one step
      try {
        control.setDirection(true);
        control.takeStep();
      }
      catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
      catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
    }
    if (o == down) {
      // move down one step
      try {
        control.setDirection(false);
        control.takeStep();
      }
      catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
      catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
    }
  }

  /** ChangeListener method used with JSlider. */
  public void stateChanged(ChangeEvent e) {
    try {
      if (control != null) {
        int cur = step.getValue() - 1;
        if (control.getCurrent() != cur) control.setCurrent(cur);
      }
    }
    catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
    catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
  }

  /** ControlListener method used for programmatically moving JSlider. */
  public void controlChanged(ControlEvent e) {
    if (control != null) {
      int val = control.getCurrent() + 1;
      if (step.getValue() != val) step.setValue(val);
    }
  }

  /** ScalarMapListener method used to recompute JSlider bounds. */
  public void mapChanged(ScalarMapEvent e) {
    fixControlUI();
  }

  /** ScalarMapListener method used to detect new AnimationControl. */
  public void controlChanged(ScalarMapControlEvent evt) {
    int id = evt.getId();
    if (id == ScalarMapEvent.CONTROL_REMOVED ||
      id == ScalarMapEvent.CONTROL_REPLACED)
    {
      evt.getControl().removeControlListener(this);
      if (id == ScalarMapEvent.CONTROL_REMOVED) control = null;
    }

    if (id == ScalarMapEvent.CONTROL_REPLACED ||
      id == ScalarMapEvent.CONTROL_ADDED)
    {
      control = (AnimationControl) evt.getScalarMap().getControl();
      fixControlUI();
      if (control != null) control.addControlListener(this);
    }
  }

}
