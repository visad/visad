//
// AlignToolPanel.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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
import visad.browser.*;
import visad.util.Util;

/**
 * AlignToolPanel is the tool panel for
 * managing image alignment, spacing and drift correction.
 */
public class AlignToolPanel extends ToolPanel {

  // -- MICRON FUNCTIONS --

  /**
   * Check box for indicating file should be saved or restored
   * using a micron-pixel conversion of the given width and height.
   */
  private DoubleTextCheckBox useMicrons;

  /** Label for distance between slices. */
  private JLabel sliceDistLabel;

  /** Text field for specifying distance (in microns) between slices. */
  private JTextField sliceDistance;

  /** Toggle for using micron information to compute Z aspect ratio. */
  private JCheckBox zAspect;


  // -- DRIFT CORRECTION FUNCTIONS --

  /** Toggle for drift correction. */
  private JCheckBox drift;

  /** Toggle for drift correction size and shape lock. */
  private JToggleButton driftLock;


  // -- CONSTRUCTOR --

  /** Constructs a tool panel for performing measurement operations. */
  public AlignToolPanel(VisBio biovis) {
    super(biovis);

    // microns vs pixels checkbox
    useMicrons = new DoubleTextCheckBox(
      "Use microns instead of pixels", "by", "", "", false);
    useMicrons.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean b = useMicrons.isSelected();
        sliceDistLabel.setEnabled(b);
        sliceDistance.setEnabled(b);
        bio.toolMeasure.updateFileButtons();
      }
    });
    useMicrons.setEnabled(false);
    controls.add(pad(useMicrons));

    // slice distance label
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    sliceDistLabel = new JLabel("Microns between slices: ");
    sliceDistLabel.setForeground(Color.black);
    sliceDistLabel.setEnabled(false);
    p.add(sliceDistLabel);

    // distance between slices text box
    sliceDistance = new JTextField();
    Util.adjustTextField(sliceDistance);
    sliceDistance.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        bio.toolMeasure.updateFileButtons();
      }
      public void insertUpdate(DocumentEvent e) {
        bio.toolMeasure.updateFileButtons();
      }
      public void removeUpdate(DocumentEvent e) {
        bio.toolMeasure.updateFileButtons();
      }
    });
    sliceDistance.setEnabled(false);
    p.add(sliceDistance);
    controls.add(pad(p));

    // Z-aspect toggle
    zAspect = new JCheckBox("Use micron information for Z-scale", true);
    zAspect.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) { updateAspect(true); }
    });
    zAspect.setEnabled(false);
    controls.add(pad(zAspect));

    // divider between micron functions and drift correction functions
    controls.add(Box.createVerticalStrut(10));
    controls.add(new Divider());
    controls.add(Box.createVerticalStrut(10));

    // drift correction checkbox
    drift = new JCheckBox("Drift correction", false);
    drift.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean doDrift = drift.isSelected();
        bio.sm.setAlignStacks(doDrift);
      }
    });
    drift.setEnabled(false);
    controls.add(pad(drift));

    // drift correction size & shape lock
    driftLock = new JToggleButton("Lock size and shape", false);
    driftLock.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.sm.align.setMode(!driftLock.isSelected());
      }
    });
    driftLock.setEnabled(false);
    controls.add(pad(driftLock));
  }


  // -- API METHODS --

  /** Initializes this tool panel. */
  public void init() { }

  /** Enables or disables this tool panel. */
  public void setEnabled(boolean enabled) {
    if (enabled) {
      useMicrons.setEnabled(true);
      zAspect.setEnabled(true);
      bio.toolMeasure.updateFileButtons();
      drift.setEnabled(true);
      driftLock.setEnabled(true);
    }
    else {
      useMicrons.setEnabled(false);
      sliceDistLabel.setEnabled(false);
      sliceDistance.setEnabled(false);
      zAspect.setEnabled(false);
      drift.setEnabled(false);
      driftLock.setEnabled(true);
    }
  }

  /** Gets whether microns should be used instead of pixels. */
  public boolean getUseMicrons() { return useMicrons.isSelected(); }

  /** Gets the image width in microns entered by the user. */
  public double getMicronWidth() {
    double d = Convert.getDouble(useMicrons.getFirstValue());
    return d <= 0 ? Double.NaN : d;
  }

  /** Gets the image height in microns entered by the user. */
  public double getMicronHeight() {
    double d = Convert.getDouble(useMicrons.getSecondValue());
    return d <= 0 ? Double.NaN : d;
  }

  /** Gets the micron distance between slices entered by the user. */
  public double getSliceDistance() {
    double d = Convert.getDouble(sliceDistance.getText());
    return d <= 0 ? Double.NaN : d;
  }


  // -- INTERNAL API METHODS --

  /** Updates GUI to match internal information. */
  void updateInfo(boolean microns, double mx, double my, double sd) {
    double mw = bio.sm.res_x * mx;
    double mh = bio.sm.res_y * my;

    // update micron info
    if (microns) useMicrons.setValues("" + mw, "" + mh);
    else useMicrons.setValues("", "");
    sliceDistance.setText(microns ? "" + sd : "");
    useMicrons.setSelected(microns);
  }

  /** Sets the slice distance to match the specified one. */
  void setSliceDistance(double sd) {
    String dist = "" + sd;
    if (dist.equals(sliceDistance.getText())) return;
    sliceDistance.setText(dist);
  }

  void setDriftLock(boolean lock) { driftLock.setSelected(lock); }


  // -- HELPER METHODS --

  /** Updates the Z-aspect and micron information. */
  void updateAspect(boolean force) {
    boolean doAspect = zAspect.isEnabled() && zAspect.isSelected();
    if (!doAspect && !force) return;

    boolean microns = getUseMicrons();
    double mw = getMicronWidth();
    double mh = getMicronHeight();
    double sd = getSliceDistance();
    if (doAspect && microns && mw == mw && mh == mh && sd == sd) {
      int slices = bio.sm.getNumberOfSlices();
      bio.setAspect(mw, mh, slices * sd);
    }
    else if (force) bio.setAspect(bio.sm.res_x, bio.sm.res_y, Double.NaN);
  }

}
