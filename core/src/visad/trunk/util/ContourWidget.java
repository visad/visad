/*
VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 1998 Nick Rasmussen
VisAD is Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
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
import com.sun.java.swing.*;
import com.sun.java.swing.event.*;

/* RMI classes */
import java.rmi.RemoteException;

/* VisAD packages */
import visad.*;

/** A widget that allows users to control iso-contours.<P> */
public class ContourWidget extends JPanel implements ActionListener,
                                                     ChangeListener,
                                                     ItemListener {

  /** This ContourRangeWidget's associated Control. */
  private ContourControl control;

  private float cInterval;
  private float cBase;
  private float cSurface;
  private float cLo;
  private float cHi;

  private JTextField Interval;
  private JTextField Base;
  private JSlider Surface;
  private JCheckBox Labels;
  private JCheckBox Contours;
  private JCheckBox Dashed;

  /** construct a ContourWidget linked to the Control in the map
      (which must be to Display.IsoContour), with default interval,
      base, min, max, and surface value, and auto-scaling min and max. */
  public ContourWidget(ScalarMap smap) throws VisADException, RemoteException {
    this(smap, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, true);
  }

  /** construct a ContourWidget linked to the Control in the map
      (which must be to Display.IsoContour), with specified surface
      value, and default interval, min, max, and base, and auto-scaling
      min and max. */
  public ContourWidget(ScalarMap smap, float surf) throws VisADException,
                                                          RemoteException {
    this(smap, Float.NaN, Float.NaN, Float.NaN, Float.NaN, surf, true);
  }

  /** construct a ContourWidget linked to the Control in the map
      (which must be to Display.IsoContour), with specified interval
      and base, default surface value, min, and max, and auto-scaling
      min and max. */
  public ContourWidget(ScalarMap smap, float interv, float min, float max,
                       float ba) throws VisADException, RemoteException {
    this(smap, interv, min, max, ba, Float.NaN, true);
  }

  /** construct a ContourWidget linked to the Control in the map
      (which must be to Display.IsoContour), with specified interval,
      minimum, maximum, base, surface value, and auto-scale behavior. */
  public ContourWidget(ScalarMap smap, float interv, float min, float max,
                       float ba, float surf, boolean update)
                       throws VisADException, RemoteException {
    cInterval = interv;
    cLo = min;
    cHi = max;
    cBase = ba;
    cSurface = surf;

    // set up control
    control = (ContourControl) smap.getControl();
    control.enableLabels(false);
    control.enableContours(true);

    // create JPanels
    JPanel top1 = new JPanel();
    JPanel top2 = new JPanel();
    JPanel mid = new JPanel();

    // set up layouts
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    top1.setLayout(new BoxLayout(top1, BoxLayout.X_AXIS));
    top2.setLayout(new BoxLayout(top2, BoxLayout.X_AXIS));
    mid.setLayout(new BoxLayout(mid, BoxLayout.X_AXIS));

    // create JComponents
    JLabel intLabel = new JLabel("interval:");
    Interval = new JTextField("---");
    JLabel baseLabel = new JLabel("base:");
    Base = new JTextField("---");
    JLabel surfLabel = new JLabel("surface value:");
    Surface = new JSlider();
    Labels = new JCheckBox("labels", false);
    Contours = new JCheckBox("contours", true);
    Dashed = new JCheckBox("dashed lines below base", false);
    ContourRangeWidget crw = new ContourRangeWidget(smap, cLo, cHi,
                                                    this, update);
    updateWidget();

    // set label foregrounds
    intLabel.setForeground(Color.black);
    baseLabel.setForeground(Color.black);
    surfLabel.setForeground(Color.black);

    // align JComponents
    Dashed.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);

    // add listeners
    Interval.addActionListener(this);
    Interval.setActionCommand("interval");
    Base.addActionListener(this);
    Base.setActionCommand("base");
    Surface.addChangeListener(this);
    Labels.addItemListener(this);
    Contours.addItemListener(this);
    Dashed.addItemListener(this);

    // lay out JComponents
    top1.add(intLabel);
    top1.add(Interval);
    top1.add(baseLabel);
    top1.add(Base);
    top2.add(surfLabel);
    top2.add(Surface);
    mid.add(Labels);
    mid.add(Contours);
    add(top1);
    add(top2);
    add(mid);
    add(Dashed);
    add(crw);
  }

  void setSliderBounds(int min, int max) {
    Surface.setMinimum(min);
    Surface.setMaximum(max);
  }

  void setMinMax(float min, float max) throws VisADException,
                                              RemoteException {
    cLo = min;
    cHi = max;
    updateWidget();
  }

  void detectValues() throws VisADException, RemoteException {
    boolean[] bval = new boolean[2];
    float[] fval = new float[5];
    control.getMainContours(bval, fval);
    cSurface = fval[0];
    cInterval = fval[1];
    cLo = fval[2];
    cHi = fval[3];
    cBase = fval[4];
  }

  void updateWidget() throws VisADException, RemoteException {
    if (cSurface == cSurface) {
      control.setSurfaceValue(cSurface);
      Surface.setEnabled(true);
      Surface.setValue((int) cSurface);
    }
    else Surface.setEnabled(false);
    if (cInterval == cInterval && cLo == cLo && cHi == cHi && cBase == cBase) {
      control.setContourInterval(cInterval, cLo, cHi, cBase);
      Interval.setEnabled(true);
      Interval.setText(""+Math.abs(cInterval));
      Base.setEnabled(true);
      Base.setText(""+cBase);
    }
    else {
      Interval.setEnabled(false);
      Interval.setText("---");
      Base.setEnabled(false);
      Base.setText("---");
    }
  }

  /** ActionListener method for JTextFields. */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("interval")) {
      float interv = Float.NaN;
      try {
        interv = Float.valueOf(Interval.getText()).floatValue();
      }
      catch (NumberFormatException exc) {
        Interval.setText(""+Math.abs(cInterval));
      }
      if (interv == interv && interv >= 0.0f) {
        if (cInterval < 0.0f) interv = -interv;
        try {
          control.setContourInterval(interv, cLo, cHi, cBase);
          cInterval = interv;
        }
        catch (VisADException exc) {
          Interval.setText(""+Math.abs(cInterval));
        }
        catch (RemoteException exc) {
          Interval.setText(""+Math.abs(cInterval));
        }
      }
      else Interval.setText(""+Math.abs(cInterval));
    }
    if (cmd.equals("base")) {
      float ba = Float.NaN;
      try {
        ba = Float.valueOf(Base.getText()).floatValue();
      }
      catch (NumberFormatException exc) {
        Base.setText(""+cBase);
      }
      if (ba == ba) {
        try {
          control.setContourInterval(cInterval, cLo, cHi, ba);
          cBase = ba;
        }
        catch (VisADException exc) {
          Base.setText(""+cBase);
        }
        catch (RemoteException exc) {
          Base.setText(""+cBase);
        }
      }
    }
  }

  /** ChangeListener method for JSlider. */
  public void stateChanged(ChangeEvent e) {
    cSurface = Surface.getValue();
    try {
      control.setSurfaceValue(cSurface);
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
  }

  /** ItemListener method for JCheckBoxes. */
  public void itemStateChanged(ItemEvent e) {
    Object o = e.getItemSelectable();
    boolean on = (e.getStateChange() == ItemEvent.SELECTED);
    if (o == Labels) {
      try {
        control.enableLabels(on);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
    if (o == Contours) {
      try {
        control.enableContours(on);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
    if (o == Dashed) {
      cInterval = -cInterval;
      try {
        control.setContourInterval(cInterval, cLo, cHi, cBase);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  }

  /** Subclass of RangeSlider for selecting min and max values.<P> */
  class ContourRangeWidget extends RangeSlider implements ScalarMapListener {

    ContourWidget pappy;

    ContourRangeWidget(ScalarMap smap, float min, float max, ContourWidget dad,
                       boolean update) throws VisADException, RemoteException {
      super(min, max);
      pappy = dad;

      // set auto-scaling enabled (listen for new min and max)
      if (update) smap.addScalarMapListener(this);
    }
  
    /** ScalarMapListener method used with delayed auto-scaling. */
    public void mapChanged(ScalarMapEvent e) {
      ScalarMap s = e.getScalarMap();
      double[] range = s.getRange();
      try {
        pappy.detectValues();
        pappy.setMinMax((float) range[0], (float) range[1]);
        pappy.setSliderBounds((int) range[0], (int) range[1]);
        setBounds((float) range[0], (float) range[1]);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  
    /** Recomputes percent variables, updates control, then paints. */
    void percPaint() {
      super.percPaint();
      try {
        pappy.setMinMax(0.01f*minPercent*(maxVal-minVal)+minVal,
                        0.01f*maxPercent*(maxVal-minVal)+minVal);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }

  }

}

