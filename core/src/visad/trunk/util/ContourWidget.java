
//
// ContourWidget.java
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

  private String name;

  private JTextField Interval;
  private JTextField Base;
  private JLabel SurfaceLabel;
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

    // verify scalar map
    if (!Display.IsoContour.equals(smap.getDisplayScalar())) {
      throw new DisplayException("ContourWidget: ScalarMap must " +
                                 "be to Display.IsoContour");
    }
    name = smap.getScalar().getName();

    // set up control
    control = (ContourControl) smap.getControl();
    control.enableLabels(false);
    control.enableContours(true);

    // create JPanels
    JPanel top = new JPanel();
    JPanel mid = new JPanel();

    // set up layouts
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    mid.setLayout(new BoxLayout(mid, BoxLayout.X_AXIS));

    // create JComponents
    Contours = new JCheckBox("contours", true);
    Labels = new JCheckBox("labels", false);
    Dashed = new JCheckBox("dashed lines below base", false);
    JLabel intLabel = new JLabel("interval:");
    Interval = new JTextField("---");
    JLabel baseLabel = new JLabel("base:");
    Base = new JTextField("---");
/* WLH 20 Aug 98
    SurfaceLabel = new JLabel("surface value: ------------");
*/
    SurfaceLabel = new JLabel(name + " = 0.0                           ");
    Surface = new JSlider();
    ContourRangeWidget crw = new ContourRangeWidget(smap, cLo, cHi,
                                                    this, update);
    if (!update) updateWidget();

    // set label foregrounds
    intLabel.setForeground(Color.black);
    baseLabel.setForeground(Color.black);
    SurfaceLabel.setForeground(Color.black);

    // align JComponents
    Dashed.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
    SurfaceLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

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
    top.add(Contours);
    top.add(Labels);
    mid.add(intLabel);
    mid.add(Interval);
    mid.add(Box.createRigidArea(new Dimension(10, 0)));
    mid.add(baseLabel);
    mid.add(Base);
    add(top);
    add(Dashed);
    add(mid);
    add(SurfaceLabel);
    add(Surface);
    add(crw);
  }

  private double sliderScale;

  void setSliderBounds(float min, float max) {
    sliderScale = 1000/(max-min);
    Surface.setMinimum((int) (sliderScale*min));
    Surface.setMaximum((int) (sliderScale*max));
  }

  void setMinMax(float min, float max) throws VisADException,
                                              RemoteException {
    cLo = min;
    cHi = max;
    updateWidget();
  }

  void detectValues(double[] range) throws VisADException, RemoteException {
    boolean[] bval = new boolean[2];
    float[] fval = new float[5];
    control.getMainContours(bval, fval);
    cSurface = fval[0];
    cInterval = fval[1];
    cLo = fval[2];
    cHi = fval[3];
    cBase = fval[4];
    if (cSurface != cSurface) cSurface = (float) range[0];
    if (cSurface == cSurface) cSurface = Math.round(1000*cSurface)/1000;
    if (cInterval == cInterval) cInterval = Math.round(1000*cInterval)/1000;
    if (cBase == cBase) cBase = Math.round(1000*cBase)/1000;
  }

  synchronized void updateWidget() throws VisADException, RemoteException {
    if (cSurface == cSurface) {
      control.setSurfaceValue(cSurface);
      Surface.setEnabled(true);
      Surface.setValue((int) (sliderScale*cSurface));

/*
      String surfString;
      if (cSurface < 10000000 && cSurface > -1000000) {
        surfString = ""+cSurface;
        int maxLen = surfString.length();
        maxLen = maxLen < 8 ? maxLen : 8;
        surfString = surfString.substring(0, maxLen);
      }
      else surfString = ""+Math.round(cSurface);
*/
      String surfString = PlotText.shortString((double) cSurface);
      SurfaceLabel.setText(name + " = " + surfString);
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
    cSurface = (float) (Surface.getValue()/sliderScale);
/*
    String surfString;
    if (cSurface < 10000000 && cSurface > -1000000) {
      surfString = ""+cSurface;
      int maxLen = surfString.length();
      maxLen = maxLen < 8 ? maxLen : 8;
      surfString = surfString.substring(0, maxLen);
    }
    else surfString = ""+Math.round(cSurface);
    SurfaceLabel.setText(name + " = " + surfString);
*/
    String surfString = PlotText.shortString((double) cSurface);
    SurfaceLabel.setText(name + " = " + surfString);

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
      super(smap, Math.round(100*min)/100, Math.round(100*max)/100);
      pappy = dad;

      // set auto-scaling enabled (listen for new min and max)
      if (update) smap.addScalarMapListener(this);
    }
  
    /** ScalarMapListener method used with delayed auto-scaling. */
    public void mapChanged(ScalarMapEvent e) {
      ScalarMap s = e.getScalarMap();
      double[] range = s.getRange();
      try {
        pappy.detectValues(range);
        float min = (float) range[0];
        float max = (float) range[1];
        pappy.setMinMax(min, max);
        pappy.setSliderBounds(min, max);
        setBounds(min, max);
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

