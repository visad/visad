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

import java.awt.event.*;
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.browser.Divider;

/**
 * ViewToolPanel is the tool panel for
 * adjusting viewing parameters.
 */
public class ViewToolPanel extends ToolPanel {

  // -- CONSTANTS --

  private static final DisplayRealType[] COLOR_TYPES = {
    Display.Red, Display.Green, Display.Blue, Display.RGB
  };


  // -- GUI COMPONENTS --

  /** Toggle for 2-D display mode. */
  private JCheckBox twoD;

  /** Toggle for 3-D display mode. */
  private JCheckBox threeD;

  /** Toggle for lo-res image display. */
  private JToggleButton loRes;

  /** Toggle for hi-res image display. */
  private JToggleButton hiRes;

  /** Toggle for auto-switching between low and high resolutions. */
  private JCheckBox autoSwitch;
  
  /** Animation widget. */
  private BioAnimWidget anim;

  /** Red color map widget. */
  private BioColorWidget red;

  /** Green color map widget. */
  private BioColorWidget green;

  /** Blue color map widget. */
  private BioColorWidget blue;

  /** Label for brightness. */
  private JLabel brightnessLabel;

  /** Slider for level of brightness. */
  private JSlider brightness;

  /** Label for contrast. */
  private JLabel contrastLabel;

  /** Slider for level of contrast. */
  private JSlider contrast;

  /** Toggle for whether 2-D plane is user-selected arbitrarily. */
  private JCheckBox planeSelect;

  /** Labels for arbitrary slice resolution. */
  private JLabel sliceResLabel1, sliceResLabel2;

  /** Text fields for arbitrary slice resolution. */
  private JTextField sliceResX, sliceResY;

  /** Toggle for whether arbitrary slice is continuously recomputed. */
  private JCheckBox sliceUpdate;


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
        bio.set2D(twoD.isSelected());
      }
    });
    p.add(twoD);

    // 3-D checkbox
    boolean okay3d = bio.display3 != null;
    threeD = new JCheckBox("3-D", okay3d);
    threeD.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        bio.set3D(threeD.isSelected());
      }
    });
    p.add(threeD);
    threeD.setEnabled(okay3d);
    controls.add(pad(p));

    // lo-res toggle button
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    loRes = new JToggleButton("Lo-res", false);
    loRes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.sm.setMode(true);
      }
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
    p.add(hiRes);
    hiRes.setEnabled(false);
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

    // divider between resolution functions and animation functions
    controls.add(Box.createVerticalStrut(10));
    controls.add(new Divider());
    controls.add(Box.createVerticalStrut(10));

    // animation widget
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    anim = new BioAnimWidget(bio);
    anim.setEnabled(false);
    p.add(anim);
    controls.add(pad(p));

    // divider between animation functions and color functions
    controls.add(Box.createVerticalStrut(10));
    controls.add(new Divider());
    controls.add(Box.createVerticalStrut(10));

    // brightness label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    JPanel colorLabels = new JPanel();
    colorLabels.setLayout(new BoxLayout(colorLabels, BoxLayout.Y_AXIS));
    brightnessLabel = new JLabel("Brightness: ");
    colorLabels.add(brightnessLabel);

    // contrast label
    contrastLabel = new JLabel("Contrast: ");
    colorLabels.add(contrastLabel);
    p.add(colorLabels);

    // brightness slider
    JPanel colorSliders = new JPanel();
    colorSliders.setLayout(new BoxLayout(colorSliders, BoxLayout.Y_AXIS));
    brightness = new JSlider(0, BioVisAD.COLOR_DETAIL,
      BioVisAD.NORMAL_BRIGHTNESS);
    brightness.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) { doColorTable(); }
    });
    colorSliders.add(brightness);

    // contrast slider
    contrast = new JSlider(0, BioVisAD.COLOR_DETAIL,
      BioVisAD.NORMAL_CONTRAST);
    contrast.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) { doColorTable(); }
    });
    colorSliders.add(contrast);
    p.add(colorSliders);
    controls.add(p);

    // spacing
    controls.add(Box.createVerticalStrut(5));

    // red color map widget
    red = new BioColorWidget(bio, BioColorWidget.RED);
    red.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) { doColorTable(); }
    });
    controls.add(pad(red));

    // green color map widget
    green = new BioColorWidget(bio, BioColorWidget.GREEN);
    green.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) { doColorTable(); }
    });
    controls.add(pad(green));

    // blue color map widget
    blue = new BioColorWidget(bio, BioColorWidget.BLUE);
    blue.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) { doColorTable(); }
    });
    controls.add(pad(blue));

    // divider between color functions and slice functions
    controls.add(Box.createVerticalStrut(10));
    controls.add(new Divider());
    controls.add(Box.createVerticalStrut(10));

    // plane selector checkbox
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
    controls.add(pad(planeSelect));

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
    sliceResX.getDocument().addDocumentListener(doc);
    sliceResY.getDocument().addDocumentListener(doc);
    sliceResLabel1.setEnabled(false);
    sliceResLabel2.setEnabled(false);
    sliceResX.setEnabled(false);
    sliceResY.setEnabled(false);
    p.add(sliceResLabel1);
    p.add(sliceResX);
    p.add(sliceResLabel2);
    p.add(sliceResY);
    controls.add(p);

    // continuous update checkbox
    sliceUpdate = new JCheckBox("Update slice continuously", false);
    sliceUpdate.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        bio.sm.setPlaneUpdate(sliceUpdate.isSelected());
      }
    });
    sliceUpdate.setEnabled(false);
    controls.add(pad(sliceUpdate));
  }


  // -- API METHODS --

  /** Enables or disables this tool panel. */
  public void setEnabled(boolean enabled) {
    boolean b = enabled && bio.sm.hasThumbnails();
    loRes.setEnabled(b);
    hiRes.setEnabled(b);
    autoSwitch.setEnabled(b);
    anim.setEnabled(b);
    brightnessLabel.setEnabled(enabled);
    brightness.setEnabled(enabled);
    contrastLabel.setEnabled(enabled);
    contrast.setEnabled(enabled);
    planeSelect.setEnabled(enabled);
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

  /** Updates image color table, for brightness and color adjustments. */
  void doColorTable() {
    bio.setImageColors(brightness.getValue(), contrast.getValue(),
      red.getSelectedItem(), green.getSelectedItem(), blue.getSelectedItem());
  }

  /** Sets the animation widget's animation control. */
  void setControl(AnimationControl control) { anim.setControl(control); }

  /** Chooses most desirable range types for color widgets. */
  void guessTypes() {
    red.guessType();
    green.guessType();
    blue.guessType();
  }

  /** Updates x and y slice resolution text fields. */
  void setSliceRange(int x, int y) {
    sliceResX.setText("" + x);
    sliceResY.setText("" + y);
  }

}
