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
import java.rmi.RemoteException;
import visad.*;

/** StepWidget is a GUI component for stepping through a time stack. */
public class StepWidget extends JPanel implements ActionListener,
  ChangeListener, ControlListener, ScalarMapListener
{
  private static final boolean DEBUG = true;

  private JSlider step;
  private JButton back;
  private JButton forward;

  private AnimationControl control;

  /** Constructs a StepWidget linked to the AnimationControl in smap. */
  public StepWidget(ScalarMap smap) throws VisADException, RemoteException {
    // verify scalar map
    if (!Display.Animation.equals(smap.getDisplayScalar())) {
      throw new DisplayException("StepWidget: " +
        "ScalarMap must be to Display.Animation");
    }

    // lay out components
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    step = new JSlider(1, 1, 1);
    back = new JButton("<");
    forward = new JButton(">");
    step.setPaintTicks(true);
    step.setAlignmentX(JButton.CENTER_ALIGNMENT);
    add(step);
    add(back);
    add(forward);

    // get control startup values
    control = (AnimationControl) smap.getControl();
    fixControlUI();

    // add listeners
    if (control != null) control.addControlListener(this);
    smap.addScalarMapListener(this);
    back.addActionListener(this);
    forward.addActionListener(this);
    step.addChangeListener(this);
  }

  private void fixControlUI() {
    // update slider ticks
    int max = 1;
    int cur = 1;
    if (control != null) {
      try {
        Set set = control.getSet();
        if (set != null) max = set.getLength();
      }
      catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
      cur = control.getCurrent() + 1;
      if (cur < 1) cur = 1;
      else if (cur > max) cur = max;
    }

    step.setMaximum(max);
    step.setMinimum(1);
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
    if (o == back) {
      // move back one step
      try {
        control.setDirection(false);
        control.takeStep();
      }
      catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
      catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
    }
    if (o == forward) {
      // move forward one step
      try {
        control.setDirection(true);
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
        if (control.getCurrent() != cur) {
          control.setCurrent(cur);
        }
      }
    }
    catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
    catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
  }

  /** ControlListener method used for programmatically moving JSlider. */
  public void controlChanged(ControlEvent e) {
    if (control != null) {
      int val = control.getCurrent() + 1;
      if (step.getValue() != val) {
        step.setValue(val);
      }
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
