 
//
// VisADSlider.java
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

// JFC packages
import com.sun.java.swing.*;
import com.sun.java.swing.event.*;

// AWT package
import java.awt.*;

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
                                                   ScalarMapListener {

  /** The default number of ticks the slider should have */
  private static final int D_TICKS = 1000;

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
  private float sMinimum;

  /** The maximum allowed slider value */
  private float sMaximum;

  /** The current slider value */
  private float sCurrent;

  /** The number of ticks in the slider */
  private int sTicks;

  /** Flags whether this VisADSlider is linked to a Real or a ScalarMap */
  private boolean smapcontrol;

  /** for compatibility purposes */
  public VisADSlider(String n, int lo, int hi, int st, double scale,
                     DataReference ref, RealType rt) throws VisADException,
                                                            RemoteException {
    this(ref, null, (float) (lo * scale), (float) (hi * scale),
                    (float) (st * scale), hi - lo + 1,
                    (ref.getData() instanceof Real) ? null : rt, n);
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
    // set up UI components
    setAlignmentX(LEFT_ALIGNMENT);   // VisADSliders default to LEFT_ALIGNMENT
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    sTicks = sliderTicks;
    slider = new JSlider(0, sTicks, sTicks / 2) {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(150, d.height);
      }
      public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        return new Dimension(150, d.height);
      }
      public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        return new Dimension(150, d.height);
      }
    };
    label = new JLabel() {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(200, d.height);
      }
      public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        return new Dimension(200, d.height);
      }
    };
    label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    add(slider);
    add(label);

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
      smapcontrol = true;
      map = smap;
      control = (ValueControl) smap.getControl();
      sRef = null;
      sName = st.getName();
      start = (float) control.getValue();

      if (min == min && max == max && start == start) {
        // do not use auto-scaling (CASE TWO)
        sMinimum = min;
        sMaximum = max;
        sCurrent = start;
        if (start < min || start > max) start = (min + max) / 2;
        smap.setRange(min, max);
        control.setValue(start);
      }
      else {
        // enable auto-scaling (CASE ONE)
        smap.addScalarMapListener(this);
      }
    }
    else {
      // this VisADSlider should link to a Real
      smapcontrol = false;
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
        sName = n;
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
        sName = realType.getName();
        sCurrent = (float) real.getValue();
        if (min != min || max != max) {
          throw new VisADException("VisADSlider: minimum and maximum " +
                                   "cannot be NaN!");
        }
        sMinimum = min;
        sMaximum = max;
        if (sCurrent < min || sCurrent > max) sCurrent = (min + max) / 2;
      }

      // watch for changes in Real, and update slider when necessary
      SliderCell cell = new SliderCell();
      if (ref instanceof RemoteDataReference) {
        RemoteCell remoteCell = new RemoteCellImpl(cell);
        remoteCell.addReference(ref);
      }
      else cell.addReference(ref);
    }

    // add listeners
    slider.addChangeListener(this);
  }

  /** called when slider is adjusted */
  public void stateChanged(ChangeEvent e) {
    double val = slider.getValue();
    sCurrent = (float) ((sMaximum - sMinimum) * (val / sTicks) + sMinimum);
    synchronized (label) {
      try {
        if (smapcontrol) control.setValue(sCurrent);
        else sRef.setData(new Real(realType, sCurrent));
      }
      catch (VisADException exc) {
        System.out.println(exc.toString());
      }
      catch (RemoteException exc) { }
      label.setText(sName + " = " + PlotText.shortString(sCurrent));
      validate();
    }
  }

  /** used for auto-scaling the minimum and maximum */
  public void mapChanged(ScalarMapEvent e) {
    double[] range = map.getRange();
    sMinimum = (float) range[0];
    sMaximum = (float) range[1];
    sCurrent = (float) control.getValue();
    if (sCurrent < sMinimum || sCurrent > sMaximum) {
      sCurrent = (sMinimum + sMaximum) / 2;
    }
    label.setText(sName + " = " + PlotText.shortString(sCurrent));
    validate();
  }

  /** This extension of CellImpl is used to link a Real and a VisADSlider */
  private class SliderCell extends CellImpl {

    /** Update slider when value of linked Real changes */
    public void doAction() throws VisADException, RemoteException {
      synchronized (label) {
        double val = ((Real) sRef.getData()).getValue();
        int ival = (int) (sTicks * ((val - sMinimum) / (sMaximum - sMinimum)));
        sCurrent = (float) ((sMaximum - sMinimum)
                          * (ival / (double) sTicks) + sMinimum);
        if (slider.getValue() != ival) {
          slider.setValue(ival);
          label.setText(sName + " = " + PlotText.shortString(sCurrent));
        }
      }
    }

  }

}

