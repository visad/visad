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
  private BioColorMapWidget red;

  /** Green color map widget. */
  private BioColorMapWidget green;

  /** Blue color map widget. */
  private BioColorMapWidget blue;

  /** Composite color map widget. */
  private BioColorMapWidget rgb;

  /** Toggle for grayscale mode. */
  private JCheckBox grayscale;

  /** Label for brightness. */
  private JLabel brightnessLabel;

  /** Slider for level of brightness. */
  private JSlider brightness;

  /** Button for applying color map changes. */
  private JButton applyColors;


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
        setMode(true);
        bio.sm.setMode(true);
      }
    });
    loRes.setEnabled(false);
    p.add(loRes);

    // hi-res toggle button
    hiRes = new JToggleButton("Hi-res", true);
    hiRes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setMode(false);

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
        // CTR - TODO
      }
    });
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

    // grayscale checkbox
    grayscale = new JCheckBox("Grayscale", true);
    grayscale.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) { doColorTable(); }
    });
    controls.add(pad(grayscale));

    // brightness label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    brightnessLabel = new JLabel("Brightness: ");
    p.add(brightnessLabel);

    // brightness slider
    brightness = new JSlider(1, 100, BioVisAD.NORMAL_BRIGHTNESS);
    brightness.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) { doColorTable(); }
    });
    p.add(brightness);
    controls.add(p);

    // spacing
    controls.add(Box.createVerticalStrut(5));

    // composite color map widget
    rgb = new BioColorMapWidget(bio, BioColorMapWidget.RGB);
    controls.add(pad(rgb));

    // spacing
    controls.add(Box.createVerticalStrut(5));

    // red color map widget
    red = new BioColorMapWidget(bio, BioColorMapWidget.RED);
    controls.add(pad(red));

    // green color map widget
    green = new BioColorMapWidget(bio, BioColorMapWidget.GREEN);
    controls.add(pad(green));

    // blue color map widget
    blue = new BioColorMapWidget(bio, BioColorMapWidget.BLUE);
    controls.add(pad(blue));

    // spacing
    controls.add(Box.createVerticalStrut(5));

    // color settings application button
    JButton applyColors = new JButton("Apply color settings");
    applyColors.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean changed = red.hasChanged() || green.hasChanged() ||
          blue.hasChanged() || rgb.hasChanged();
        if (changed) bio.sm.reconfigureDisplays();
      }
    });
    controls.add(pad(applyColors));
  }


  // -- API METHODS --

  /** Enables or disables this tool panel. */
  public void setEnabled(boolean enabled) {
    loRes.setEnabled(enabled);
    hiRes.setEnabled(enabled);
    anim.setEnabled(enabled && bio.sm.hasThumbnails());
    grayscale.setEnabled(enabled);
    brightnessLabel.setEnabled(enabled);
    brightness.setEnabled(enabled);
  }

  /** Switches between lo-res and hi-res mode. */
  public void setMode(boolean lowres) {
    loRes.setSelected(lowres);
    hiRes.setSelected(!lowres);
  }


  // -- INTERNAL API METHODS --

  /** Updates image color table, for grayscale and brightness adjustments. */
  void doColorTable() {
    bio.setImageColors(grayscale.isSelected(), brightness.getValue());
  }

  /** Sets the animation widget's animation control. */
  void setControl(AnimationControl control) { anim.setControl(control); }

  /** Refreshes the color combo boxes to contain the current range types. */
  void refreshColorWidgets() {
    red.refreshTypes();
    green.refreshTypes();
    blue.refreshTypes();
    rgb.refreshTypes();
  }

  /** Gets the current color maps indicated by the color combo boxes. */
  ScalarMap[] getColorMaps() throws VisADException {
    RealType[] rt = {
      red.getSelectedItem(),
      green.getSelectedItem(),
      blue.getSelectedItem(),
      rgb.getSelectedItem()
    };
    ScalarMap[] maps = new ScalarMap[rt.length];
    for (int i=0; i<rt.length; i++) {
      maps[i] = rt[i] == null ?
        new ConstantMap(0.0, COLOR_TYPES[i]) :
        new ScalarMap(rt[i], COLOR_TYPES[i]);
    }
    return maps;
  }

}
