 
//
// VisADSlider.java
//


package visad.util;

import visad.*;

import java.util.*;
import java.rmi.*;

// import com.sun.java.swing.*;
// import com.sun.java.swing.event.*;

import java.awt.swing.*;
import java.awt.swing.event.*;

import java.awt.*;
import java.awt.event.*;

/**
   VisADSlider extends JPanel; it combines a JSlider and a JLabel
   and links them to a Real, via a DataReference.
*/
public class VisADSlider extends JPanel {

  private JSlider slider;
  private JLabel slider_label;
  private ChangeListener listener;
  private DataReference ref;
  private RealType type;
  private String name;
  private double scale;
  private int low;
  private int hi;

  private SliderCell cell;
  private int lastCellValue;
  private int lastSliderValue;

  private String head;

  private int first;

  public VisADSlider(String n, int l, int h, int start,
                     double sc, DataReference r, RealType t)
         throws VisADException, RemoteException {

    ref = r;
    type = t;
    name = n;
    low = l;
    hi = h;
    scale = sc;
    first = -2; // what an ugly hack work-around for jdk1.2beta2 bug

    lastCellValue = low - 1;
    lastSliderValue = low - 1;

    Data real = ref.getData();
    if (real != null && (real instanceof Real)) {
      double v = ((Real) real).getValue() / scale;
      if (v == v && low <= v && v <= hi) start = (int) v;
    }

    slider = new JSlider(JSlider.HORIZONTAL, low, hi, start);
    double val = scale * slider.getValue();
    head = "         ";
    slider_label = new JLabel(name + " = " + shortString(val) + head);
 
    listener = new SliderListener();
    slider.addChangeListener(listener);
 
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setAlignmentY(JPanel.TOP_ALIGNMENT);
    setAlignmentX(JPanel.LEFT_ALIGNMENT);
    slider.setMaximumSize(new Dimension(150, 20));
    add(slider);
    add(slider_label);
    ref.setData(new Real(type, val));

    cell = new SliderCell();
    if (ref instanceof RemoteDataReference) {
      RemoteCell remote_cell = new RemoteCellImpl(cell);
      remote_cell.addReference(ref);
    }
    else {
      cell.addReference(ref);
    }
  }

  /** make a short string for value for use in slider label */
  public static String shortString(double val) {
    String s = null;
    int is = (val < 0.0) ? -1 : 1;
    val = Math.abs(val);
    int i = (int) (1000 * val);
    int i1000 = i / 1000;
    int i1 = i - 1000 * i1000;
    String s1000 = (is > 0) ? Integer.toString(i1000) :
                              "-" + Integer.toString(i1000);
    if (i1 == 0) {
      s = s1000;
    }
    else {
      String s1 = Integer.toString(i1);
      if (s1.length() == 3) {
        s = s1000 + "." + s1;
      }
      else if (s1.length() == 2) {
        s = s1000 + ".0" + s1;
      }
      else {
        s = s1000 + ".00" + s1;
      }
    }
    return s;
  }
 
  /** slider state changes trigger changes to Real data object
      via DataReference */
  private class SliderListener implements ChangeListener {
 
    public void stateChanged(ChangeEvent e) {
      JSlider s1 = (JSlider)e.getSource();
      int ival = s1.getValue();
      if (ival == lastCellValue) {
        // don't respond to slider state changes triggered
        // by SliderCell
        lastCellValue = low - 1; // but only ignore first
      }
      else { // ival != lastCellValue
        lastSliderValue = ival;
if (first > 0) {
  // hack for JDK 1.2 with Java3D
  update(getGraphics());
}
else {
  first++;
}
        try {
          double val = scale * ival;
          ref.setData(new Real(type, val));
          head = "";
          slider_label.setText(name + " = " + shortString(val));
        }
        catch (VisADException ex) {
        }
        catch (RemoteException ex) {
        }
      }
    }
  }

  /** changes to ref Real value trigger changes to slider */
  private class SliderCell extends CellImpl {

    public void doAction() throws VisADException, RemoteException {
      double val = ((Real) ref.getData()).getValue();
      int ival = (int) (val / scale);
      ival = Math.min(Math.max(ival, low), hi);
      if (ival == lastSliderValue) {
        // don't respond to Real value chnages triggered
        // by stateChanged
      }
      else {
        lastCellValue = ival;
        slider.setValue(ival);
if (first > 0) {
  // hack for JDK 1.2 with Java3D
  update(getGraphics());
}
else {
  first++;
}
        slider_label.setText(name + " = " + shortString(val) + head);
      }
    }
  }

}

