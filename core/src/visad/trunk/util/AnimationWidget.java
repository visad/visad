
//
// AnimationWidget.java
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

/* AWT packages */
import java.awt.event.*;

/* JFC packages */
import com.sun.java.swing.*;

/* RMI classes */
import java.rmi.RemoteException;

/* VisAD packages */
import visad.*;

/** A widget that allows users to control animation. */
public class AnimationWidget extends JPanel implements ActionListener {

  private boolean aDir;
  private boolean aAnim;
  private int aMs;

  private JRadioButton forward;
  private JRadioButton reverse;
  private JButton onOff;
  private JButton step;
  private JTextField ms;

  private AnimationControl control;

  /** construct an AnimationWidget linked to the Control in the map
      (which must be to Display.Animation) with auto-detecting ms/frame. */
  public AnimationWidget(ScalarMap smap) throws VisADException,
                                                RemoteException {
    this(smap, -1);
  }

  /** construct an AnimationWidget linked to the Control in the map
      (which must be to Display.Animation) with specified ms/frame. */
  public AnimationWidget(ScalarMap smap, int st) throws VisADException,
                                                        RemoteException {
    // verify scalar map
    if (!Display.Animation.equals(smap.getDisplayScalar())) {
      throw new DisplayException("AnimationWidget: ScalarMap must " +
                                 "be to Display.Animation");
    }

    // set control
    control = (AnimationControl) smap.getControl();
    aDir = true;
    aAnim = true;
    aMs = (st > 0) ? st : (int) control.getStep();
    control.setDirection(aDir);
    control.setOn(aAnim);
    control.setStep(aMs);

    // create JPanels
    JPanel top = new JPanel();
    JPanel bottom = new JPanel();
    JPanel left = new JPanel();
    JPanel right = new JPanel();

    // set up layouts
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

    // create JComponents
    forward = new JRadioButton("Forward", true);
    reverse = new JRadioButton("Reverse");
    onOff = new JButton("Stop");
    step = new JButton("Step");
    step.setEnabled(false);
    ms = new JTextField(""+aMs);
    JLabel msLabel = new JLabel("ms/frame");

    // group JRadioButtons
    ButtonGroup group = new ButtonGroup();
    group.add(forward);
    group.add(reverse);

    // add listeners
    forward.addActionListener(this);
    forward.setActionCommand("forward");
    reverse.addActionListener(this);
    reverse.setActionCommand("reverse");
    onOff.addActionListener(this);
    onOff.setActionCommand("go");
    step.addActionListener(this);
    step.setActionCommand("step");
    ms.addActionListener(this);
    ms.setActionCommand("ms");

    // align JComponents
    left.setAlignmentX(JPanel.CENTER_ALIGNMENT);
    left.setAlignmentY(JPanel.TOP_ALIGNMENT);
    right.setAlignmentX(JPanel.CENTER_ALIGNMENT);
    right.setAlignmentY(JPanel.TOP_ALIGNMENT);
    onOff.setAlignmentX(JButton.CENTER_ALIGNMENT);
    step.setAlignmentX(JButton.CENTER_ALIGNMENT);
    ms.setAlignmentY(JTextField.CENTER_ALIGNMENT);
    msLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

    // lay out JComponents
    left.add(forward);
    left.add(reverse);
    right.add(onOff);
    right.add(step);
    top.add(left);
    top.add(right);
    add(top);
    bottom.add(ms);
    bottom.add(msLabel);
    add(bottom);
  }

  /** ActionListener method used with JTextField and JButtons. */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("forward")) {
      try {
        control.setDirection(true);
        aDir = true;
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
    if (cmd.equals("reverse")) {
      try {
        control.setDirection(false);
        aDir = false;
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
    if (cmd.equals("ms") || (cmd.equals("go") && !aAnim)) {
      int fr = -1;
      try {
        fr = Integer.parseInt(ms.getText());
      }
      catch (NumberFormatException exc) {
        ms.setText(""+aMs);
      }
      if (fr > 0) {
        try {
          control.setStep(fr);
          aMs = fr;
          if (aDir) forward.requestFocus();
          else reverse.requestFocus();
        }
        catch (VisADException exc) {
          ms.setText(""+aMs);
        }
        catch (RemoteException exc) {
          ms.setText(""+aMs);
        }
      }
      else ms.setText(""+aMs);
    }
    if (cmd.equals("go")) {
      try {
        control.setOn(!aAnim);
        aAnim = !aAnim;
        if (aAnim) {
          onOff.setText("Stop");
          step.setEnabled(false);
        }
        else {
          onOff.setText("Go");
          step.setEnabled(true);
        }
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
    if (cmd.equals("step")) {
      try {
        control.takeStep();
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  }

}

