
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
import java.awt.*;
import java.awt.event.*;

/* JFC packages */
import javax.swing.*;
import javax.swing.event.*;

/* RMI classes */
import java.rmi.RemoteException;

/* VisAD packages */
import visad.*;

/** 
 * A widget that allows users to control aspects of animation (stop/start,
 * step, animation speed and direction).  It is initialized with the state 
 * of the AnimationControl for the ScalarMap used in the constructor. Once this 
 * widget is constructed, it should be used to control animation instead 
 * of using methods in AnimationControl.  Once constructed, changes made
 * using AnimationControl methods will not be reflected in this widget.
 */
public class AnimationWidget extends JPanel implements ActionListener,
                                                       ChangeListener,
                                                       ControlListener,
                                                       ScalarMapListener {

  private boolean aDir;
  private boolean aAnim;
  private int aMs;

  private JRadioButton forward;
  private JRadioButton reverse;
  private JButton onOff;
  private JButton step;
  private JTextField ms;
  private JSlider TimeSlider;

  private AnimationControl control;

  /** 
   * construct an AnimationWidget linked to the Control in smap
   * (which must be to Display.Animation) with auto-detecting ms/frame 
   *
   * @param	smap	Display.Animation ScalarMap
   */
  public AnimationWidget(ScalarMap smap) throws VisADException,
                                                RemoteException {
    this(smap, -1);
  }

  /** 
   * construct an AnimationWidget linked to the Control in smap
   * (which must be to Display.Animation) with specified ms/frame 
   *
   * @param     smap    Display.Animation ScalarMap
   * @param     st      animation speed (ms/frame).  If value is negative, 
   *                    the default speed set in the Control is used.
   */
  public AnimationWidget(ScalarMap smap, int st) throws VisADException,
                                                        RemoteException {
    // verify scalar map
    if (!Display.Animation.equals(smap.getDisplayScalar())) {
      throw new DisplayException("AnimationWidget: ScalarMap must " +
                                 "be to Display.Animation");
    }

    // set control and get startup values.
    control = (AnimationControl) smap.getControl();
    aDir = control.getDirection();
    aAnim = control.getOn();
    aMs = (st > 0) ? st : (int) control.getStep();
    control.setStep(aMs);
/* DRM 1999-05-19  Initialize with values from control.
    control.setDirection(aDir);
    control.setOn(aAnim);
*/

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
    forward = new JRadioButton("Forward", aDir);
    reverse = new JRadioButton("Reverse", !aDir);
    onOff = new JButton(aAnim ? "Stop" : "Go");
    step = new JButton("Step");
    ms = new JTextField(""+aMs);

    // WLH 2 Dec 98
    Dimension msize = ms.getMaximumSize();
    Dimension psize = ms.getPreferredSize();
    msize.height = psize.height;
    ms.setMaximumSize(msize);

    JLabel msLabel = new JLabel("ms/frame");
    TimeSlider = new JSlider(1, 1, 1);   /* DRM 1999-05-19 */

    // set up JComponents
    Color fore = msLabel.getForeground();
    forward.setForeground(fore);
    reverse.setForeground(fore);
    onOff.setForeground(fore);
    step.setForeground(fore);
    step.setEnabled(!aAnim);
    ms.setForeground(fore);
    TimeSlider.setPaintTicks(true);

    // make sure onOff button stays the same size to avoid ...'s
    // use step maximum size since "Step" has same size as "Stop"
    onOff.setMaximumSize(step.getMaximumSize());

    // group JRadioButtons
    ButtonGroup group = new ButtonGroup();
    group.add(forward);
    group.add(reverse);

    // add listeners
    control.addControlListener(this);
    smap.addScalarMapListener(this);
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
    TimeSlider.addChangeListener(this);

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
    add(TimeSlider);
  }

  /** 
   * ActionListener method used with JTextField and JButtons 
   */
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
        // slider will adjust automatically with ControlListener
        control.takeStep();
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  }

  /** 
   * ChangeListener method used with JSlider.
   */
  public void stateChanged(ChangeEvent e) {
    if (!TimeSlider.getValueIsAdjusting()) {
      try {
        control.setCurrent(TimeSlider.getValue()-1);  /* DRM 1999-05-19 */
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  }

  /** 
   * ControlListener method used for programmatically moving JSlider 
   */
  public void controlChanged(ControlEvent e) {
    TimeSlider.setValue(control.getCurrent()+1);  /* DRM 1999-05-19 */
  }

  /** 
   * ScalarMapListener method used to recompute JSlider bounds 
   */
  public void mapChanged(ScalarMapEvent e) {
    if (control.getSet() != null) {
      try {
        int max = control.getSet().getLength();  /* DRM 1999-05-19 */
        TimeSlider.setMaximum(max);
        TimeSlider.setMinimum(1);
        
        int maj;
        if (max < 20) maj = max/4;
        else if (max < 30) maj = max/6;
        else maj = max/8;
        TimeSlider.setMajorTickSpacing(maj);
        TimeSlider.setMinorTickSpacing(maj/4);
        TimeSlider.setPaintLabels(true);
      }
      catch (VisADException exc) { }
    }
  }

  /** 
   * Work-around for Swing bug where pack() doesn't display slider labels;
   * actually, it still won't, but window will be the right size 
   */
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    return new Dimension(d.width, d.height+18);
  }

}

