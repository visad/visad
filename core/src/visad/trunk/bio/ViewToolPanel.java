//
// ViewToolPanel.java
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
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.browser.Divider;
import visad.util.Util;

/**
 * ViewToolPanel is the tool panel for
 * adjusting viewing parameters.
 */
public class ViewToolPanel extends ToolPanel implements ItemListener {

  // -- CONSTANTS --

  private static final DisplayRealType[] COLOR_TYPES = {
    Display.Red, Display.Green, Display.Blue, Display.RGB
  };


  // -- GUI COMPONENTS --

  /** Toggle for 2-D display mode. */
  private JCheckBox twoD;

  /** Button for zooming in on 2-D display. */
  private JButton zoomIn2;

  /** Button for resetting zoom on 2-D display. */
  private JButton zoomReset2;

  /** Button for zooming out on 2-D display. */
  private JButton zoomOut2;

  /** Toggle for 3-D display mode. */
  private JCheckBox threeD;

  /** Button for zooming in on 3-D display. */
  private JButton zoomIn3;

  /** Button for resetting zoom on 3-D display. */
  private JButton zoomReset3;

  /** Button for zooming out on 3-D display. */
  private JButton zoomOut3;

  /** Toggle for preview displays. */
  private JCheckBox preview;

  /** Toggle for using micron information to compute Z aspect ratio. */
  private JCheckBox zAspect;

  /** Toggle for lo-res image display. */
  private JToggleButton loRes;

  /** Toggle for hi-res image display. */
  private JToggleButton hiRes;

  /** Toggle for auto-switching between low and high resolutions. */
  private JCheckBox autoSwitch;

  /** Animation widget. */
  private BioAnimWidget anim;

  /** Label for brightness. */
  private JLabel brightnessLabel;

  /** Slider for level of brightness. */
  private JSlider brightness;

  /** Label for current brightness value. */
  private JLabel brightnessValue;

  /** Label for contrast. */
  private JLabel contrastLabel;

  /** Slider for level of contrast. */
  private JSlider contrast;

  /** Label for current contrast value. */
  private JLabel contrastValue;

  /** Red color map widget. */
  private BioColorWidget red;

  /** Green color map widget. */
  private BioColorWidget green;

  /** Blue color map widget. */
  private BioColorWidget blue;

  /** Toggle for composite coloring. */
  private JCheckBox composite;

  /** Toggle for colorizing image stack based on slice level. */
  private JCheckBox colorize;

  /** Toggle for whether 2-D plane is user-selected arbitrarily. */
  private JCheckBox planeSelect;

  /** Labels for arbitrary slice resolution. */
  private JLabel sliceResLabel1, sliceResLabel2;

  /** Text fields for arbitrary slice resolution. */
  private JTextField sliceResX, sliceResY;

  /** Toggle for whether arbitrary slice is continuously recomputed. */
  private JCheckBox sliceUpdate;


  // -- OTHER FIELDS --

  /** Should changes to the color components be ignored? */
  private boolean ignore = false;


  // -- CONSTRUCTOR --

  /** Constructs a tool panel for adjusting viewing parameters. */
  public ViewToolPanel(BioVisAD biovis) {
    super(biovis);

    // 2-D checkbox
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    twoD = new JCheckBox("2-D", true);
    twoD.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean b = twoD.isSelected();
        bio.set2D(b);
        zoomIn2.setEnabled(b);
        zoomReset2.setEnabled(b);
        zoomOut2.setEnabled(b);
      }
    });
    p.add(twoD);

    // 2-D zoom in button
    zoomIn2 = new JButton("Zoom in");
    zoomIn2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { bio.setZoom(false, 1.5); }
    });
    p.add(zoomIn2);

    // 2-D zoom reset button
    zoomReset2 = new JButton("Reset");
    zoomReset2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { bio.resetZoom(false); }
    });
    p.add(zoomReset2);

    // 2-D zoom out button
    zoomOut2 = new JButton("Zoom out");
    zoomOut2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { bio.setZoom(false, 0.667); }
    });
    p.add(zoomOut2);
    controls.add(pad(p));

    // 3-D checkbox
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    boolean okay3d = bio.display3 != null;
    threeD = new JCheckBox("3-D", okay3d);
    threeD.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean b = threeD.isSelected();
        bio.set3D(b);
        zoomIn3.setEnabled(b);
        zoomReset3.setEnabled(b);
        zoomOut3.setEnabled(b);
      }
    });
    threeD.setEnabled(okay3d);
    p.add(threeD);

    // 3-D zoom in button
    zoomIn3 = new JButton("Zoom in");
    zoomIn3.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { bio.setZoom(true, 2); }
    });
    zoomIn3.setEnabled(okay3d);
    p.add(zoomIn3);

    // 3-D zoom reset button
    zoomReset3 = new JButton("Reset");
    zoomReset3.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { bio.resetZoom(true); }
    });
    zoomReset3.setEnabled(okay3d);
    p.add(zoomReset3);

    // 3-D zoom out button
    zoomOut3 = new JButton("Zoom out");
    zoomOut3.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { bio.setZoom(true, 0.5); }
    });
    zoomOut3.setEnabled(okay3d);
    p.add(zoomOut3);
    controls.add(pad(p));

    // Preview checkbox
    preview = new JCheckBox("Previous/next preview displays", false);
    preview.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean b = preview.isSelected();
        bio.setPreview(b);
      }
    });
    preview.setEnabled(false);
    controls.add(pad(preview));

    // spacing
    controls.add(Box.createVerticalStrut(5));

    // Z-aspect toggle
    zAspect = new JCheckBox("Use micron information for Z-scale", true);
    zAspect.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) { updateAspect(true); }
    });
    zAspect.setEnabled(false);
    controls.add(pad(zAspect));

    // divider between display functions and resolution functions
    controls.add(Box.createVerticalStrut(10));
    controls.add(new Divider());
    controls.add(Box.createVerticalStrut(10));

    // lo-res toggle button
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    loRes = new JToggleButton("Lo-res", false);
    loRes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { bio.sm.setMode(true); }
    });
    loRes.setEnabled(false);
    p.add(loRes);

    // hi-res toggle button
    hiRes = new JToggleButton("Hi-res", true);
    hiRes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // don't animate in hi-res mode
        AnimationControl control = anim.getControl();
        try { if (control != null) control.setOn(false); }
        catch (VisADException exc) { exc.printStackTrace(); }
        catch (RemoteException exc) { exc.printStackTrace(); }
        bio.sm.setMode(false);
      }
    });
    hiRes.setEnabled(false);
    p.add(hiRes);
    controls.add(pad(p));

    // auto-switch checkbox
    autoSwitch = new JCheckBox("Auto-switch resolutions", true);
    autoSwitch.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        bio.sm.setAutoSwitch(autoSwitch.isSelected());
      }
    });
    autoSwitch.setEnabled(false);
    controls.add(pad(autoSwitch));

    // spacing
    controls.add(Box.createVerticalStrut(5));

    // animation widget
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    anim = new BioAnimWidget(bio);
    anim.setEnabled(false);
    p.add(anim);
    controls.add(pad(p));

    // divider between resolution functions and color functions
    controls.add(Box.createVerticalStrut(10));
    controls.add(new Divider());
    controls.add(Box.createVerticalStrut(10));

    // brightness label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    brightnessLabel = new JLabel("Brightness: ");
    brightnessLabel.setForeground(Color.black);
    brightnessLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
    p.add(brightnessLabel);

    // brightness slider
    brightness = new JSlider(0, BioVisAD.COLOR_DETAIL,
      BioVisAD.NORMAL_BRIGHTNESS);
    brightness.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) { doColorTable(); }
    });
    brightness.setAlignmentY(JSlider.TOP_ALIGNMENT);
    p.add(brightness);

    // current brightness value
    brightnessValue = new JLabel("" + BioVisAD.NORMAL_BRIGHTNESS);
    Dimension colorValueSize =
      new JLabel("" + BioVisAD.COLOR_DETAIL).getPreferredSize();
    brightnessValue.setPreferredSize(colorValueSize);
    brightnessValue.setAlignmentY(JLabel.TOP_ALIGNMENT);
    p.add(brightnessValue);
    controls.add(pad(p));

    // contrast label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    contrastLabel = new JLabel("Contrast: ");
    contrastLabel.setForeground(Color.black);
    contrastLabel.setPreferredSize(brightnessLabel.getPreferredSize());
    contrastLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
    p.add(contrastLabel);

    // contrast slider
    contrast = new JSlider(0, BioVisAD.COLOR_DETAIL,
      BioVisAD.NORMAL_CONTRAST);
    contrast.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) { doColorTable(); }
    });
    contrast.setAlignmentY(JSlider.TOP_ALIGNMENT);
    contrast.setMajorTickSpacing(BioVisAD.COLOR_DETAIL / 4);
    contrast.setMinorTickSpacing(BioVisAD.COLOR_DETAIL / 16);
    contrast.setPaintTicks(true);
    p.add(contrast);

    // current contrast value
    contrastValue = new JLabel("" + BioVisAD.NORMAL_CONTRAST);
    contrastValue.setPreferredSize(colorValueSize);
    contrastValue.setAlignmentY(JLabel.TOP_ALIGNMENT);
    p.add(contrastValue);
    controls.add(pad(p));

    // spacing
    controls.add(Box.createVerticalStrut(5));

    // red color map widget
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    red = new BioColorWidget(bio, BioColorWidget.RED);
    red.addItemListener(this);
    p.add(red);

    // green color map widget
    green = new BioColorWidget(bio, BioColorWidget.GREEN);
    green.addItemListener(this);
    p.add(green);

    // blue color map widget
    blue = new BioColorWidget(bio, BioColorWidget.BLUE);
    blue.addItemListener(this);
    p.add(blue);
    controls.add(pad(p));

    // composite checkbox
    composite = new JCheckBox("Composite image coloring", false);
    composite.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean b = !composite.isSelected();
        red.setEnabled(b);
        green.setEnabled(b);
        blue.setEnabled(b);
        doColorTable();
      }
    });
    controls.add(pad(composite));

    // colorize across slice level checkbox
    colorize = new JCheckBox("Colorize image stack across slices", false);
    colorize.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) { doColorTable(); }
    });
    colorize.setEnabled(false);
    controls.add(pad(colorize));

    // divider between color functions and misc functions
    controls.add(Box.createVerticalStrut(10));
    controls.add(new Divider());
    controls.add(Box.createVerticalStrut(10));

    // plane selector checkbox
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    planeSelect = new JCheckBox("Arbitrary data slice", false);
    planeSelect.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean ps = planeSelect.isSelected();
        sliceResLabel1.setEnabled(ps);
        sliceResX.setEnabled(ps);
        sliceResLabel2.setEnabled(ps);
        sliceResY.setEnabled(ps);
        sliceUpdate.setEnabled(ps);
        bio.sm.setPlaneSelect(ps);
        bio.vert.setEnabled(!ps);
      }
    });
    planeSelect.setEnabled(false);
    p.add(planeSelect);

    // continuous update checkbox
    sliceUpdate = new JCheckBox("Update continuously", false);
    sliceUpdate.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        bio.sm.setPlaneUpdate(sliceUpdate.isSelected());
      }
    });
    sliceUpdate.setEnabled(false);
    p.add(Box.createHorizontalStrut(10));
    p.add(sliceUpdate);
    controls.add(p);

    // arbitrary slice resolution
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    sliceResLabel1 = new JLabel("Slice resolution: ");
    sliceResX = new JTextField();
    sliceResLabel2 = new JLabel(" by ");
    sliceResY = new JTextField();
    DocumentListener doc = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e) { update(e); }
      public void update(DocumentEvent e) {
        String sx = sliceResX.getText();
        String sy = sliceResY.getText();
        try {
          int resx = Integer.parseInt(sx);
          int resy = Integer.parseInt(sy);
          bio.sm.setSliceRange(resx, resy);
        }
        catch (NumberFormatException exc) { }
      }
    };
    Util.adjustTextField(sliceResX);
    Util.adjustTextField(sliceResY);
    sliceResX.getDocument().addDocumentListener(doc);
    sliceResY.getDocument().addDocumentListener(doc);
    sliceResLabel1.setForeground(Color.black);
    sliceResLabel2.setForeground(Color.black);
    sliceResLabel1.setEnabled(false);
    sliceResLabel2.setEnabled(false);
    sliceResX.setEnabled(false);
    sliceResY.setEnabled(false);
    p.add(sliceResLabel1);
    p.add(sliceResX);
    p.add(sliceResLabel2);
    p.add(sliceResY);
    controls.add(p);
  }


  // -- API METHODS --

  /** Enables or disables this tool panel. */
  public void setEnabled(boolean enabled) {
    boolean b = enabled && bio.sm.hasThumbnails();
    zAspect.setEnabled(enabled);
    loRes.setEnabled(b);
    hiRes.setEnabled(b);
    autoSwitch.setEnabled(b);
    anim.setEnabled(b);
    brightnessLabel.setEnabled(enabled);
    brightness.setEnabled(enabled);
    contrastLabel.setEnabled(enabled);
    contrast.setEnabled(enabled);
    planeSelect.setEnabled(enabled && bio.sm.getNumberOfSlices() > 1);
    b = enabled && planeSelect.isSelected();
    sliceResLabel1.setEnabled(b);
    sliceResX.setEnabled(b);
    sliceResLabel2.setEnabled(b);
    sliceResY.setEnabled(b);
  }

  /** Switches between lo-res and hi-res mode. */
  public void setMode(boolean lowres) {
    loRes.setSelected(lowres);
    hiRes.setSelected(!lowres);
  }


  // -- INTERNAL API METHODS --

  /** ItemListener method for handling color mapping changes. */
  public void itemStateChanged(ItemEvent e) { doColorTable(); }

  /** Updates image color table, for brightness and color adjustments. */
  void doColorTable() {
    if (ignore) return;
    int bright = brightness.getValue();
    int cont = contrast.getValue();
    boolean comp = composite.isSelected();
    bio.setImageColors(bright, cont, comp, red.getSelectedItem(),
      green.getSelectedItem(), blue.getSelectedItem());
    brightnessValue.setText("" + bright);
    contrastValue.setText("" + cont);
  }

  /** Sets the animation widget's animation control. */
  void setControl(AnimationControl control) { anim.setControl(control); }

  /** Chooses most desirable range types for color widgets. */
  void guessTypes() {
    red.removeItemListener(this);
    green.removeItemListener(this);
    blue.removeItemListener(this);
    red.guessType();
    green.guessType();
    blue.guessType();
    red.addItemListener(this);
    green.addItemListener(this);
    blue.addItemListener(this);
  }

  /** Updates x and y slice resolution text fields. */
  void setSliceRange(int x, int y) {
    sliceResX.setText("" + x);
    sliceResY.setText("" + y);
  }

  /** Updates color components to match those specified. */
  void setColors(int bright, int cont, RealType r, RealType g, RealType b) {
    ignore = true;
    brightness.setValue(bright);
    contrast.setValue(cont);
    red.setSelectedItem(r);
    green.setSelectedItem(g);
    blue.setSelectedItem(b);
    ignore = false;
    doColorTable();
  }

  /** Updates the Z-aspect and micron information. */
  void updateAspect(boolean force) {
    boolean doAspect = zAspect.isEnabled() && zAspect.isSelected();
    if (!doAspect && !force) return;

    boolean microns = bio.toolMeasure.getUseMicrons();
    double mw = bio.toolMeasure.getMicronWidth();
    double mh = bio.toolMeasure.getMicronHeight();
    double sd = bio.toolMeasure.getSliceDistance();
    if (doAspect && microns && mw == mw && mh == mh && sd == sd) {
      int slices = bio.sm.getNumberOfSlices();
      bio.setAspect(mw, mh, slices * sd);
    }
    else if (force) bio.setAspect(bio.sm.res_x, bio.sm.res_y, Double.NaN);
  }

}
