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
public class ViewToolPanel extends ToolPanel {

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

  /** Toggle for lo-res image display. */
  private JToggleButton loRes;

  /** Toggle for hi-res image display. */
  private JToggleButton hiRes;

  /** Toggle for auto-switching between low and high resolutions. */
  private JCheckBox autoSwitch;

  /** Animation widget. */
  private BioAnimWidget anim;

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
        bio.toolRender.volume.setEnabled(b);
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
    preview.setEnabled(okay3d);
    controls.add(pad(preview));

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

    // divider between resolution functions and misc functions
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
        bio.toolMeasure.setEnabled(!ps);
        bio.mm.pool2.setSlice(ps ? -1 : bio.sm.getSlice());
      }
    });
    planeSelect.setEnabled(false);
    p.add(planeSelect);

    // continuous update checkbox
    sliceUpdate = new JCheckBox("Update continuously", false);
    sliceUpdate.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        bio.sm.setPlaneContinuous(sliceUpdate.isSelected());
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
    loRes.setEnabled(b);
    hiRes.setEnabled(b);
    autoSwitch.setEnabled(b);
    anim.setEnabled(b);
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

  /** Sets the animation widget's animation control. */
  void setControl(AnimationControl control) { anim.setControl(control); }

  /** Updates x and y slice resolution text fields. */
  void setSliceRange(int x, int y) {
    sliceResX.setText("" + x);
    sliceResY.setText("" + y);
  }

}
