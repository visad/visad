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
import visad.browser.Convert;

/**
 * ViewToolPanel is the tool panel for
 * adjusting viewing parameters.
 */
public class ViewToolPanel extends ToolPanel implements SwingConstants {

  // -- CONSTANTS --

  /** Starting brightness value. */
  private static final int NORMAL_BRIGHTNESS = 50;


  // -- GUI COMPONENTS --

  /** Toggle for 2-D display mode. */
  private JCheckBox twoD;

  /** Toggle for 3-D display mode. */
  private JCheckBox threeD;

  /** Toggle for lo-res image display. */
  private JToggleButton loRes;

  /** Toggle for hi-res image display. */
  private JToggleButton hiRes;

  /** Toggle for auto-resolution switching mode. */
  private JCheckBox autoResSwitch;
  
  /** Toggle for grayscale mode. */
  private JCheckBox grayscale;

  /** Label for brightness. */
  private JLabel brightnessLabel;

  /** Slider for level of brightness. */
  private JSlider brightness;


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
    threeD = new JCheckBox("3-D", false);
    threeD.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        bio.set3D(threeD.isSelected());
      }
    });
    p.add(threeD);
    controls.add(pad(p));

    // lo-res toggle button
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    loRes = new JToggleButton("Lo-res", true);
    loRes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loRes.setSelected(true);
        hiRes.setSelected(false);
        // CTR: TODO: implement lo res toggle
      }
    });
    p.add(loRes);

    // hi-res toggle button
    hiRes = new JToggleButton("Hi-res", false);
    hiRes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loRes.setSelected(false);
        hiRes.setSelected(true);
        // CTR: TODO: implement hi res toggle
      }
    });
    p.add(hiRes);
    controls.add(pad(p));

    // auto-resolution switching checkbox
    autoResSwitch = new JCheckBox("Auto-res switching", true);
    autoResSwitch.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean res = autoResSwitch.isSelected();
        // CTR: TODO: implement auto-res checkbox
      }
    });
    controls.add(pad(autoResSwitch));

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
    brightness = new JSlider(1, 100, 50);
    brightness.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) { doColorTable(); }
    });
    p.add(brightness);
    controls.add(p);
  }

  /** Enables or disables this tool panel. */
  public void setEnabled(boolean enabled) {
    loRes.setEnabled(enabled);
    hiRes.setEnabled(enabled);
    autoResSwitch.setEnabled(enabled);
    grayscale.setEnabled(enabled);
    brightnessLabel.setEnabled(enabled);
    brightness.setEnabled(enabled);
  }

  /** Updates image color table, for grayscale and brightness adjustments. */
  private void doColorTable() {
    float[][] table = grayscale.isSelected() ?
      ColorControl.initTableGreyWedge(new float[3][256]) :
      ColorControl.initTableVis5D(new float[3][256]);

    // apply brightness (actually gamma correction)
    double gamma = 1.0 -
      (1.0 / NORMAL_BRIGHTNESS) * (brightness.getValue() - NORMAL_BRIGHTNESS);
    for (int i=0; i<256; i++) {
      table[0][i] = (float) Math.pow(table[0][i], gamma);
      table[1][i] = (float) Math.pow(table[1][i], gamma);
      table[2][i] = (float) Math.pow(table[2][i], gamma);
    }

    // get color controls
    ColorControl cc2 = (ColorControl)
      bio.display2.getControl(ColorControl.class);
    ColorControl cc3 = bio.display3 == null ? null :
      (ColorControl) bio.display3.getControl(ColorControl.class);

    // set color tables
    try {
      if (cc2 != null) cc2.setTable(table);
      if (cc3 != null) cc3.setTable(table);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

}
