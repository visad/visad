//
// VisADSlider.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

// JFC packages
import javax.swing.*;
import javax.swing.event.*;

// AWT class
import java.awt.Dimension;

// RMI class
import java.rmi.RemoteException;

// VisAD package
import visad.*;

/** VisADSlider combines a JSlider and a JLabel and links them to either a
    Real (via a DataReference) or a ScalarMap that maps to
    Display.SelectValue.  Changes in the slider will reflect the Real or
    ScalarMap linked to it.  If no bounds are specified, they will be
    detected from the ScalarMap and the slider will auto-scale.  Note that
    a slider linked to a Real cannot auto-scale, because it has no way to
    detect the bounds.<P> */
public class VisADSlider extends JPanel implements ChangeListener,
  ControlListener, ScalarMapListener
{

  /** The default number of ticks the slider should have */
  private static final int D_TICKS = 1000;

  /** Default width of the slider in pixels */
  private static final int SLIDER_WIDTH = 150;

  /** Default width of the label in pixels */
  private static final int LABEL_WIDTH = 200;

  /** Work-around for JFC JLabel craziness */
  private String head = "         ";

  /** The JSlider that forms part of the VisADSlider's UI */
  private JSlider slider;

  /** The JLabel that forms part of the VisADSlider's UI */
  private JLabel label;

  /** The ScalarMap that is linked to this VisADSlider (null if none) */
  private ScalarMap map;

  /** The ValueControl that this VisADSlider utilizes (null if none) */
  private ValueControl control;

  /** The DataReference that is linked to this VisADSlider (null if none) */
  private DataReference sRef;

  /** The type of the linked Real (null if none) */
  private RealType realType;

  /** The name of the variable being modified by this VisADSlider */
  private String sName;

  /** The minimum allowed slider value */
  private double sMinimum;

  /** The maximum allowed slider value */
  private double sMaximum;

  /** The current slider value */
  private double sCurrent;

  /** The number of ticks in the slider */
  private int sTicks;

  /** Flags whether this VisADSlider is linked to a Real or a ScalarMap */
  private boolean smapControl;

  /** <CODE>true</CODE> if the widget will auto-scale */
  private boolean autoScale;

  /** JSlider values range between low and hi (with initial value
      st) and are multiplied by scale to create Real values
      of RealType rt referenced by ref */
  public VisADSlider(String n, int lo, int hi, int st, double scale,
                     DataReference ref, RealType rt) throws VisADException,
                                                            RemoteException {
    this(ref, null, (float) (lo * scale), (float) (hi * scale),
                    (float) (st * scale), hi - lo,
                    (ref == null || ref.getData() instanceof Real) ? null : rt,
                    n);
  }

  /** construct a VisADSlider from a ScalarMap that maps to
      Display.SelectValue, with auto-scaling minimum and maximum bounds */
  public VisADSlider(ScalarMap smap) throws VisADException, RemoteException {
    // CASE ONE
    this(null, smap, Float.NaN, Float.NaN, Float.NaN, D_TICKS, null, null);
  }

  /** construct a VisADSlider from a ScalarMap that maps to
      Display.SelectValue, with minimum and maximum bounds min and max,
      and no auto-scaling */
  public VisADSlider(ScalarMap smap, float min, float max)
                     throws VisADException, RemoteException {
    // CASE TWO
    this(null, smap, min, max, Float.NaN, D_TICKS, null, null);
  }

  /** construct a VisADSlider by creating a Real and linking it to r,
      using RealType rt and name n, with minimum and maximum bounds
      min and max, and starting value start */
  public VisADSlider(DataReference ref, float min, float max, float start,
                     RealType rt, String n) throws VisADException,
                                                   RemoteException {
    // CASE THREE
    this(ref, null, min, max, start, D_TICKS, rt, n);
  }

  /** construct a VisADSlider from an existing Real pointed to by r,
      with minimum and maximum bounds min and max */
  public VisADSlider(DataReference ref, float min, float max)
                     throws VisADException, RemoteException {
    // CASE FOUR
    this(ref, null, min, max, Float.NaN, D_TICKS, null, null);
  }

  /** complete constructor */
  private VisADSlider(DataReference ref, ScalarMap smap, float min, float max,
                      float start, int sliderTicks, RealType rt, String n)
                      throws VisADException, RemoteException {
    // set up some UI components
    setAlignmentX(LEFT_ALIGNMENT);   // VisADSliders default to LEFT_ALIGNMENT
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    sTicks = sliderTicks;
    Dimension d;
    slider = new JSlider(0, sTicks, sTicks / 2);
    d = slider.getMinimumSize();
    slider.setMinimumSize(new Dimension(SLIDER_WIDTH, d.height));
    d = slider.getPreferredSize();
    slider.setPreferredSize(new Dimension(SLIDER_WIDTH, d.height));
    d = slider.getMaximumSize();
    slider.setMaximumSize(new Dimension(SLIDER_WIDTH, d.height));

    // by default, don't auto-scale
    autoScale = false;

    // set up internal components
    if (ref == null) {
      // this VisADSlider should link to a ScalarMap
      if (smap == null) {
        throw new VisADException("VisADSlider: must specify either a " +
                                 "DataReference or a ScalarMap!");
      }
      DisplayRealType drt = smap.getDisplayScalar();
      if (drt != Display.SelectValue) {
        throw new VisADException("VisADSlider: ScalarMap must be to " +
                                 "Display.SelectValue!");
      }
      ScalarType st = smap.getScalar();
      if (!(st instanceof RealType)) {
        throw new VisADException("VisADSlider: ScalarMap must be from " +
                                 "a RealType!");
      }
      smapControl = true;
      map = smap;
      control = (ValueControl) smap.getControl();
      if (control == null) {
        throw new VisADException("VisADSlider: ScalarMap must be addMap'ed " +
                                 "to a Display");
      }
      sRef = null;
      // sName = st.getName();
      sName = smap.getScalarName();
      start = (float) control.getValue();

      if (min == min && max == max && start == start) {
        // do not use auto-scaling (CASE TWO)
        sMinimum = min;
        sMaximum = max;
        sCurrent = start;
        initLabel();
        smap.setRange(min, max);
        if (start < min || start > max) {
          start = (min + max) / 2;
          control.setValue(start);
        }
      }
      else {
        // enable auto-scaling (CASE ONE)
        autoScale = true;
        initLabel();
      }
      control.addControlListener(this);
      smap.addScalarMapListener(this);
    }
    else {
      // this VisADSlider should link to a Real
      smapControl = false;
      map = null;
      control = null;
      if (ref == null) {
        throw new VisADException("VisADSlider: DataReference " +
                                 "cannot be null!");
      }
      sRef = ref;
      Data data = ref.getData();
      if (data == null) {
        // the Real must be created (CASE THREE)
        if (rt == null) {
          throw new VisADException("VisADSlider: RealType cannot be null!");
        }
        if (n == null) {
          throw new VisADException("VisADSlider: name cannot be null!");
        }
        realType = rt;
        if (min != min || max != max || start != start) {
          throw new VisADException("VisADSlider: min, max, and start " +
                                   "cannot be NaN!");
        }
        sMinimum = min;
        sMaximum = max;
        sCurrent = (start < min || start > max) ? (min + max) / 2 : start;
        sRef.setData(new Real(realType, sCurrent));
      }
      else {
        // the Real already exists (CASE FOUR)
        if (!(data instanceof Real)) {
          throw new VisADException("VisADSlider: DataReference " +
                                   "must point to a Real!");
        }
        Real real = (Real) data;
        realType = (RealType) real.getType();
        sCurrent = (float) real.getValue();
        if (min != min || max != max) {
          throw new VisADException("VisADSlider: minimum and maximum " +
                                   "cannot be NaN!");
        }
        sMinimum = min;
        sMaximum = max;
        if (sCurrent < min || sCurrent > max) sCurrent = (min + max) / 2;
      }
      sName = (n != null) ? n : realType.getName();
      initLabel();

      // watch for changes in Real, and update slider when necessary
      CellImpl cell = new CellImpl() {
        public void doAction() throws VisADException, RemoteException {
          // update slider when value of linked Real changes
          if (sRef != null) {
            double val;
            try {
              val = ((Real) sRef.getData()).getValue();
              if (!Util.isApproximatelyEqual(sCurrent, val)) updateSlider(val);
            } catch (RemoteException re) {
              if (visad.collab.CollabUtil.isDisconnectException(re)) {
                // Remote data server went away
                sRef = null;
              }
              throw re;
            }
          }
        }
      };

      if (ref instanceof RemoteDataReference) {
        RemoteCell remoteCell = new RemoteCellImpl(cell);
        remoteCell.addReference(ref);
      }
      else cell.addReference(ref);
    }

    // add UI components
    add(slider);
    add(label);

    // add listeners
    slider.addChangeListener(this);

    // do initial update of the slider
    updateSlider(start);
  }

  /** sets up the JLabel */
  private void initLabel() {
    Dimension d;
    label = new JLabel(sName + " = " + PlotText.shortString(sCurrent) + head);
    d = label.getMinimumSize();
    label.setMinimumSize(new Dimension(LABEL_WIDTH, d.height));
    d = label.getPreferredSize();
    label.setPreferredSize(new Dimension(LABEL_WIDTH, d.height));
    d = label.getMaximumSize();
    label.setMaximumSize(new Dimension(LABEL_WIDTH, d.height));
    label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
  }

  /** called when slider is adjusted */
  public synchronized void stateChanged(ChangeEvent e) {
    head = "";
    try {
      double val = slider.getValue();
      double cur = (sMaximum - sMinimum) * (val / sTicks) + sMinimum;
      if (!Util.isApproximatelyEqual(sCurrent, cur)) {
        if (smapControl) control.setValue(cur);
        else if (sRef != null) {
          try {
            sRef.setData(new Real(realType, cur));
          } catch (RemoteException re) {
            if (visad.collab.CollabUtil.isDisconnectException(re)) {
              // Remote data server went away
              sRef = null;
            }
            throw re;
          }
        }
      }
    }
    catch (VisADException exc) {
      exc.printStackTrace();
    }
    catch (RemoteException exc) {
      exc.printStackTrace();
    }
  }

  /** used for auto-scaling the minimum and maximum */
  public void mapChanged(ScalarMapEvent e) {
    if (!autoScale) return;
    double[] range = map.getRange();
    sMinimum = (float) range[0];
    sMaximum = (float) range[1];
    sCurrent = (float) control.getValue();
    if (sCurrent < sMinimum || sCurrent > sMaximum) {
      sCurrent = (sMinimum + sMaximum) / 2;
    }
    updateSlider(sCurrent);
  }

  /**
   * ScalarMapListener method used to detect new control.
   */
  public void controlChanged(ScalarMapControlEvent evt) {
    int id = evt.getId();
    if (id == ScalarMapEvent.CONTROL_REMOVED ||
        id == ScalarMapEvent.CONTROL_REPLACED)
    {
      control = null;
    }

    if (id == ScalarMapEvent.CONTROL_REPLACED ||
        id == ScalarMapEvent.CONTROL_ADDED)
    {
      control = (ValueControl) evt.getScalarMap().getControl();
    }
  }

  /** Update slider when value of linked ValueControl changes */
  public void controlChanged(ControlEvent e)
    throws VisADException, RemoteException
  {
    double cur = control.getValue();
    if (!Util.isApproximatelyEqual(sCurrent, cur)) {
      updateSlider(control.getValue());
    }
  }

  /** Update the slider's value */
  private synchronized void updateSlider(double value) {
    int ival = (int) (sTicks * ((value - sMinimum) / (sMaximum - sMinimum)));
    if (Math.abs(slider.getValue() - ival) > 1) {
      slider.removeChangeListener(this);
      slider.setValue(ival);
      slider.addChangeListener(this);
    }
    sCurrent = value;
    label.setText(sName + " = " + PlotText.shortString(sCurrent) + head);
  }

}

