//
// StepWidget.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * StepWidget is a slider GUI component with
 * directional step arrows at either end.
 */
public class StepWidget extends JPanel
  implements ActionListener, ChangeListener
{

  protected static final boolean DEBUG = false;
  private static final int BUTTON_WIDTH = 25;
  private static final int BUTTON_HEIGHT = 25;
  private static final int GAP = 5;
  private static final Dimension buttonSize =
    new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);

  protected JSlider step;
  private boolean horiz;
  protected JButton forward;
  protected JButton back;

  protected int min = 1;
  protected int max = 1;
  protected int cur = 1;

  /** Constructs a StepWidget. */
  public StepWidget(boolean horizontal) {
    // determine orientation
    horiz = horizontal;
    int mainLayout, subLayout;
    int backDir, stepDir, forwardDir;
    Dimension gap;
    Component glue1, glue2, glue3, glue4;
    if (horizontal) {
      mainLayout = BoxLayout.X_AXIS;
      subLayout = BoxLayout.Y_AXIS;
      backDir = BasicArrowButton.WEST;
      stepDir = JSlider.HORIZONTAL;
      forwardDir = BasicArrowButton.EAST;
      gap = new Dimension(GAP, 0);
      glue1 = Box.createVerticalGlue();
      glue2 = Box.createVerticalGlue();
      glue3 = Box.createVerticalGlue();
      glue4 = Box.createVerticalGlue();
    }
    else {
      mainLayout = BoxLayout.Y_AXIS;
      subLayout = BoxLayout.X_AXIS;
      backDir = BasicArrowButton.NORTH;
      stepDir = JSlider.VERTICAL;
      forwardDir = BasicArrowButton.SOUTH;
      gap = new Dimension(0, GAP);
      glue1 = Box.createHorizontalGlue();
      glue2 = Box.createHorizontalGlue();
      glue3 = Box.createHorizontalGlue();
      glue4 = Box.createHorizontalGlue();
    }

    // create panels
    JPanel before = new JPanel();
    JPanel after = new JPanel();
    setLayout(new BoxLayout(this, mainLayout));
    before.setLayout(new BoxLayout(before, subLayout));
    after.setLayout(new BoxLayout(after, subLayout));

    // create components
    back = new BasicArrowButton(backDir) {
      public Dimension getPreferredSize() { return buttonSize; }
      public Dimension getMaximumSize() { return buttonSize; }
    };
    step = new JSlider(stepDir, min, max, cur);
    forward = new BasicArrowButton(forwardDir) {
      public Dimension getPreferredSize() { return buttonSize; }
      public Dimension getMaximumSize() { return buttonSize; }
    };
    step.setPaintTicks(true);
    step.setAlignmentX(JButton.LEFT_ALIGNMENT);

    // lay out components
    add(before);
    add(Box.createRigidArea(gap));
    add(step);
    add(Box.createRigidArea(gap));
    add(after);
    before.add(glue1);
    before.add(back);
    before.add(glue2);
    after.add(glue3);
    after.add(forward);
    after.add(glue4);

    // listen for GUI events
    back.addActionListener(this);
    forward.addActionListener(this);
    step.addChangeListener(this);

    // disable controls
    setEnabled(false);
  }

  /** Returns the minimum size of the widget. */
  public Dimension getMinimumSize() {
    Dimension min = getPreferredSize();
    return horiz ? new Dimension(0, min.height + 4 * GAP) :
      new Dimension(min.width, 0);
  }

  /** Returns the maximum size of the widget. */
  public Dimension getMaximumSize() {
    Dimension max = getPreferredSize();
    return horiz ? new Dimension(Integer.MAX_VALUE, max.height + 4 * GAP) :
      new Dimension(max.width, Integer.MAX_VALUE);
  }

  /** Gets the current value of the widget. */
  public int getValue() { return step.getValue(); }

  /** Gets the minimum value of the widget. */
  public int getMinimum() { return step.getMinimum(); }

  /** Gets the maximum value of the widget. */
  public int getMaximum() { return step.getMaximum(); }

  /** Sets the current value of the widget. */
  public void setValue(int cur) {
    this.cur = cur;
    step.setValue(cur);
  }

  /** Enables or disables the widget. */
  public void setEnabled(boolean enabled) {
    step.setEnabled(enabled);
    back.setEnabled(enabled);
    forward.setEnabled(enabled);
  }

  /** Sets the minimum, maximum and current values of the slider. */
  public void setBounds(int min, int max, int cur) {
    this.min = min;
    this.max = max;
    this.cur = cur;

    step.setMinimum(min);
    step.setMaximum(max);
    step.setValue(cur);

    int maj;
    if (max < 4) maj = 1;
    else if (max < 20) maj = max / 4;
    else if (max < 30) maj = max / 6;
    else maj = max / 8;

    step.setMajorTickSpacing(maj);
    step.setMinorTickSpacing(maj / 4);
    step.setPaintLabels(true);
  }

  /** Adds a ChangeListener to the widget. */
  public void addChangeListener(ChangeListener l) {
    step.addChangeListener(l);
  }

  /** Removes a ChangeListener from the widget. */
  public void removeChangeListener(ChangeListener l) {
    step.removeChangeListener(l);
  }

  /** ActionListener method used with JButtons. */
  public void actionPerformed(ActionEvent e) {
    boolean direction = (e.getSource() == back);
    if (horiz == direction) {
      // move back
      cur--;
      if (cur < min) cur = max;
    }
    else {
      // move forward
      cur++;
      if (cur > max) cur = min;
    }
    step.setValue(cur);
    updateStep();
  }

  /** ChangeListener method used with JSlider. */
  public void stateChanged(ChangeEvent e) {
    cur = step.getValue();
    updateStep();
  }

  /**
   * Takes action when the slider's current value changes.
   *
   * This method is a stub that can be overridden to define behavior.
   */
  protected void updateStep() { }

}
