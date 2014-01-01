//
// ContourWidget.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

/* AWT packages */
import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/* JFC packages */
import javax.swing.*;
import javax.swing.event.*;

/* RMI classes */
import java.rmi.RemoteException;

/* VisAD packages */
import visad.*;

/** A widget that allows users to control iso-contours.<P> */
public class ContourWidget
  extends JPanel
  implements ActionListener, ChangeListener, ItemListener, ControlListener,
             ScalarMapListener
{

  /** This ContourRangeSlider's associated Control. */
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
  private ContourRangeSlider ContourRange;

  private JCheckBox Fill;

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
    // verify scalar map
    if (!Display.IsoContour.equals(smap.getDisplayScalar())) {
      throw new DisplayException("ContourWidget: ScalarMap must " +
                                 "be to Display.IsoContour");
    }
    name = smap.getScalarName();

    // get control settings
    control = (ContourControl) smap.getControl();

    boolean[] flags = new boolean[2];
    float[] values = new float[5];
    control.getMainContours(flags, values);

    // initialize flags from control settings
    boolean contourFlag, labelFlag, dashFlag, fillFlag;
    contourFlag = flags[0];
    labelFlag = flags[1];
    dashFlag = (values[1] < 0.0f);

    fillFlag = control.contourFilled();

    // use either parameter value or (if param val is NaN) control value
    boolean setSurface = false;
    boolean setInterval = false;
    if (surf == surf) {
      cSurface = surf;
      setSurface = true;
    } else {
      cSurface = values[0];
    }
    if (interv == interv) {
      cInterval = interv;
      setInterval = true;
    } else {
      cInterval = values[1];
    }
    if (min == min) {
      cLo = min;
      setInterval = true;
    } else {
      cLo = values[2];
    }
    if (max == max) {
      cHi = max;
      setInterval = true;
    } else {
      cHi = values[3];
    }
    if (ba == ba) {
      cBase = ba;
      setInterval = true;
    } else {
      cBase = values[4];
    }

    // create JPanels
    JPanel top = new JPanel();
    JPanel mid = new JPanel();
    JPanel low = new JPanel();

    // set up layouts
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    mid.setLayout(new BoxLayout(mid, BoxLayout.X_AXIS));
    low.setLayout(new BoxLayout(low, BoxLayout.X_AXIS));

    // create JComponents
    Contours = new JCheckBox("contours", contourFlag);
    Labels = new JCheckBox("labels", labelFlag);
    Dashed = new JCheckBox("dashed lines below base", dashFlag);
    JLabel intLabel = new JLabel("interval:");
    Interval = new JTextField("---");
    Fill  = new JCheckBox("fill", fillFlag);

    // WLH 2 Dec 98
    Dimension msize = Interval.getMaximumSize();
    Dimension psize = Interval.getPreferredSize();
    msize.height = psize.height;
    Interval.setMaximumSize(msize);

    JLabel baseLabel = new JLabel("base:");
    Base = new JTextField("---");

    // WLH 2 Dec 98
    msize = Base.getMaximumSize();
    psize = Base.getPreferredSize();
    msize.height = psize.height;
    Base.setMaximumSize(msize);

    SurfaceLabel = new JLabel(name + " = ---");
    Surface = new JSlider();
    ContourRange = new ContourRangeSlider(smap, cLo, cHi, this, update);
    if (!update) {
      if (setSurface) {
        control.setSurfaceValue(cSurface);
      }
      if (setInterval) {
        control.setContourInterval(cInterval, cLo, cHi, cBase);
      }
      updateWidgetSurface();
      updateWidgetRange();
    }

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
    Dimension d = new Dimension(Integer.MAX_VALUE,
                                SurfaceLabel.getMaximumSize().height);
    SurfaceLabel.setMaximumSize(d);
    SurfaceLabel.setPreferredSize(d);
    Surface.addChangeListener(this);
    Labels.addItemListener(this);
    Contours.addItemListener(this);
    Fill.addItemListener(this);
    Dashed.addItemListener(this);
    control.addControlListener(this);
    smap.addScalarMapListener(this);

    // set up JComponents' tool tips
    Contours.setToolTipText("Toggle contours");
    Labels.setToolTipText("Toggle iso-contour labels (2-D only)");
    Fill.setToolTipText("Solid filled contours (2-D only)");
    Dashed.setToolTipText("Toggle dashed lines below base value (2-D only)");
    String s = "Specify the iso-contouring interval (2-D only)";
    intLabel.setToolTipText(s);
    Interval.setToolTipText(s);
    String t = "Specify the iso-contouring base value (2-D only)";
    baseLabel.setToolTipText(t);
    Base.setToolTipText(t);
    String u = "Specify the iso-level value (3-D only)";
    SurfaceLabel.setToolTipText(u);
    Surface.setToolTipText(u);

    // lay out JComponents
    top.add(Contours);
    top.add(Labels);
    top.add(Fill);
    mid.add(intLabel);
    mid.add(Interval);
    mid.add(Box.createRigidArea(new Dimension(10, 0)));
    mid.add(baseLabel);
    mid.add(Base);
    low.add(Box.createRigidArea(new Dimension(10, 0)));
    low.add(SurfaceLabel);
    add(top);
    add(Dashed);
    add(mid);
    add(low);
    add(Surface);
    add(ContourRange);
  }

  private double sliderScale;

  void setSliderBounds(float min, float max) {
    sliderScale = 1000/(max-min);
    Surface.setMinimum((int) (sliderScale*min));
    Surface.setMaximum((int) (sliderScale*max));
  }

  void setMinMax(float min, float max) throws VisADException,
                                              RemoteException {
    if (!Util.isApproximatelyEqual(cLo, min) ||
        !Util.isApproximatelyEqual(cHi, max))
    {
      cLo = min;
      cHi = max;
      control.setContourLimits(cLo, cHi);
    }
  }

  private void detectValues(double[] range) throws VisADException,
                                              RemoteException {
    boolean[] bval = new boolean[2];
    float[] fval = new float[5];
    control.getMainContours(bval, fval);

    boolean setSurface = false;
    if (fval[0] == fval[0] && !Util.isApproximatelyEqual(cSurface, fval[0])) {
      cSurface = fval[0];
      setSurface = true;
    } else if (!Util.isApproximatelyEqual(cSurface, (float )range[0])) {
      cSurface = (float )range[0];
      setSurface = true;
    }
    if (setSurface) {
      control.setSurfaceValue(cSurface);
    }

    if (!Util.isApproximatelyEqual(cInterval, fval[1]) ||
        !Util.isApproximatelyEqual(cLo, fval[2]) ||
        !Util.isApproximatelyEqual(cHi, fval[3]) ||
        !Util.isApproximatelyEqual(cBase, fval[4]))
    {
      cInterval = fval[1];
      cLo = fval[2];
      cHi = fval[3];
      cBase = fval[4];
      control.setContourInterval(cInterval, cLo, cHi, cBase);
    }

    updateWidgetSurface();
    updateWidgetRange();
  }

  synchronized private void updateWidgetSurface()
          throws VisADException, RemoteException
  {
    if (cSurface == cSurface) {
      Surface.setEnabled(true);

      int val;
      double tmp = sliderScale * cSurface;
      if (tmp < 0) {
        val = (int )(tmp - 0.5);
      } else {
        val = (int )(tmp + 0.5);
      }
      Surface.setValue(val);
      SurfaceLabel.setText(name + " = " + PlotText.shortString(cSurface));
    }
    else {
      Surface.setEnabled(false);
      SurfaceLabel.setText(name + " = ---");
    }
  }

  synchronized private void updateWidgetRange()
          throws VisADException, RemoteException
  {
    if (cInterval == cInterval && cBase == cBase) {
      Interval.setEnabled(true);
      Interval.setText(PlotText.shortString(Math.abs(cInterval)));
      Base.setEnabled(true);
      Base.setText("" + PlotText.shortString(cBase));
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
        Interval.setText(PlotText.shortString(Math.abs(cInterval)));
      }
      if (interv == interv && interv >= 0.0f) {
        if (cInterval < 0.0f) interv = -interv;
        try {
          control.setContourInterval(interv, cLo, cHi, cBase);
          cInterval = interv;
        }
        catch (VisADException exc) {
          Interval.setText(PlotText.shortString(Math.abs(cInterval)));
        }
        catch (RemoteException exc) {
          Interval.setText(PlotText.shortString(Math.abs(cInterval)));
        }
      }
      else Interval.setText(PlotText.shortString(Math.abs(cInterval)));
    }
    if (cmd.equals("base")) {
      float ba = Float.NaN;
      try {
        ba = Float.valueOf(Base.getText()).floatValue();
      }
      catch (NumberFormatException exc) {
        Base.setText(PlotText.shortString(cBase));
      }
      if (ba == ba) {
        try {
          control.setContourInterval(cInterval, cLo, cHi, ba);
          cBase = ba;
        }
        catch (VisADException exc) {
          Base.setText(PlotText.shortString(cBase));
        }
        catch (RemoteException exc) {
          Base.setText(PlotText.shortString(cBase));
        }
      }
    }
  }

  /** ChangeListener method for JSlider. */
  public void stateChanged(ChangeEvent e) {
    float newVal = (float) (Surface.getValue()/sliderScale);
    if (!Util.isApproximatelyEqual(cSurface, newVal)) {
      cSurface = newVal;
      SurfaceLabel.setText(name + " = " + PlotText.shortString(cSurface));

      try {
        control.setSurfaceValue(cSurface);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
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
    if (o == Fill) {
      try {
        control.setContourFill(on);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  }

  /** ControlListener method for ContourControl. */
  public void controlChanged(ControlEvent e)
        throws VisADException, RemoteException {
    boolean[] bvals = new boolean[2];
    float[] fvals = new float[5];
    control.getMainContours(bvals, fvals);

    if (Contours.isSelected() != bvals[0]) {
      Contours.setSelected(bvals[0]);
    }
    if (Labels.isSelected() != bvals[1]) {
      Labels.setSelected(bvals[1]);
    }

    float interval = fvals[1];
    boolean dashedState = (interval < 0.0f);

    if (Dashed.isSelected() != dashedState) {
      Dashed.setSelected(dashedState);
    }

    float cInt;
    if (dashedState != (cInterval < 0.0f)) {
      cInt = -cInterval;
    } else {
      cInt = cInterval;
    }
    if (!Util.isApproximatelyEqual(interval, cInt)) {
      if (interval < 0.0f) interval = -interval;
      Interval.setText(PlotText.shortString(interval));
      cInterval = fvals[1];
    }
    if (!Util.isApproximatelyEqual(fvals[4], cBase)) {
      Base.setText(PlotText.shortString(fvals[4]));
      cBase = fvals[4];
    }

    if (!Util.isApproximatelyEqual(fvals[0], cSurface)) {
      cSurface = fvals[0];
      updateWidgetSurface();
    }
    if (!Util.isApproximatelyEqual(fvals[2], cLo) ||
        !Util.isApproximatelyEqual(fvals[3], cHi)) {
      cLo = fvals[2];
      cHi = fvals[3];
      updateWidgetRange();
      ContourRange.setValues(cLo, cHi);
    }
  }

  private Dimension prefSize = null;

  /** Make ContourWidget appear decent-sized */
  public Dimension getPreferredSize() {
    if (prefSize == null) {
      prefSize = new Dimension(300, super.getPreferredSize().height);
    }
    return prefSize;
  }

  /** Set ContourWidget size */
  public void setPreferredSize(Dimension dim) { prefSize = dim; }

  /** Do-nothing method; <CODE>ContourRangeSlider</CODE> handles map data */
  public void mapChanged(ScalarMapEvent e) { }

  /** Deal with changes to the <CODE>ScalarMap</CODE>  control */
  public void controlChanged(ScalarMapControlEvent evt)
    throws RemoteException, VisADException
  {
    int id = evt.getId();
    if (control != null && (id == ScalarMapEvent.CONTROL_REMOVED ||
                            id == ScalarMapEvent.CONTROL_REPLACED))
    {
      evt.getControl().removeControlListener(this);
    }

    if (id == ScalarMapEvent.CONTROL_REPLACED ||
        id == ScalarMapEvent.CONTROL_ADDED)
    {
      control = (ContourControl )(evt.getScalarMap().getControl());
      controlChanged(new ControlEvent(control));
      control.addControlListener(this);
    }
  }

  /** Subclass of RangeSlider for selecting min and max values.<P> */
  class ContourRangeSlider extends RangeSlider implements ScalarMapListener {

    ContourWidget pappy;

    ContourRangeSlider(ScalarMap smap, float min, float max, ContourWidget dad,
                       boolean update) throws VisADException, RemoteException {
      super(RangeSlider.nameOf(smap), min, max);
      pappy = dad;

      // set auto-scaling enabled (listen for new min and max)
      if (update) smap.addScalarMapListener(this);
    }

    /** ScalarMapListener method used with delayed auto-scaling. */
    public void mapChanged(ScalarMapEvent e) {
      ScalarMap s = e.getScalarMap();
      ContourControl cc = (ContourControl )s.getControl();
      double[] range = s.getRange();

      try {

        minLimit = (float) range[0];
        maxLimit = (float) range[1];

        if (!Util.isApproximatelyEqual(cLo, minLimit) ||
            !Util.isApproximatelyEqual(cHi, maxLimit))
        {
          cLo = minLimit;
          cHi = maxLimit;
        }

        pappy.setSliderBounds(minLimit, maxLimit);

        pappy.detectValues(range);

        float[] lhb = new float[3];
        boolean[] dashes = new boolean[1];
        float[] lvls = cc.getLevels(lhb, dashes);
        setValues(lhb[0], lhb[1]);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }

    /** Do-nothing method; <CODE>ContourWidget</CODE> handles map control */
    public void controlChanged(ScalarMapControlEvent evt) { }

    /** tell parent when the value changes */
    public void valuesUpdated() {
      try {
        pappy.setMinMax(minValue, maxValue);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }

  }

}

