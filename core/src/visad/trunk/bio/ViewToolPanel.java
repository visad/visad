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

  /** Toggle for 3-D bounding box. */
  private JCheckBox box3;

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

  /** Toggle for 3-D volume rendering. */
  private JCheckBox doVolume;

  /** Label for current volume rendering resolution value. */
  private JLabel volumeValue;

  /** Slider for volume rendering resolution. */
  private JSlider volumeRes;

  /** Toggle for whether 2-D plane is user-selected arbitrarily. */
  private JCheckBox doSlice;

  /** Label for current arbitrary slice resolution value. */
  private JLabel sliceValue;

  /** Slider for arbitrary slice resolution. */
  private JSlider sliceRes;

  /** Toggle for whether arbitrary slice is continuously recomputed. */
  private JCheckBox sliceContinuous;


  // -- OTHER FIELDS --

  /** Maximum resolution for volume rendering. */
  private int maxVolRes;


  // -- CONSTRUCTOR --

  /** Constructs a tool panel for adjusting viewing parameters. */
  public ViewToolPanel(VisBio biovis) {
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

    // Bounding box checkbox
    box3 = new JCheckBox("3-D bounding box", okay3d);
    box3.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try { bio.display3.getDisplayRenderer().setBoxOn(box3.isSelected()); }
        catch (VisADException exc) { exc.printStackTrace(); }
        catch (RemoteException exc) { exc.printStackTrace(); }
      }
    });
    box3.setEnabled(okay3d);
    controls.add(pad(box3));

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

    // volume rendering checkbox
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    doVolume = new JCheckBox("3-D volume rendering: ", false);
    doVolume.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean b = doVolume.isSelected();
        volumeValue.setEnabled(b);
        volumeRes.setEnabled(b);
        bio.setVolume(b);
      }
    });
    doVolume.setEnabled(false);
    p.add(doVolume);

    // current volume value
    int detail = VisBio.RESOLUTION_DETAIL;
    int normal = detail / 2;
    volumeValue = new JLabel("");
    Dimension d = doVolume.getPreferredSize();
    //volumeValue.setPreferredSize(new Dimension(detail - d.width, d.height));
    volumeValue.setEnabled(false);
    p.add(volumeValue);
    controls.add(pad(p));

    // volume slider
    volumeRes = new JSlider(0, detail, normal);
    volumeRes.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        doVolumeRes(!volumeRes.getValueIsAdjusting());
      }
    });
    volumeRes.setEnabled(false);
    volumeRes.setMajorTickSpacing(detail / 4);
    volumeRes.setMinorTickSpacing(detail / 16);
    volumeRes.setPaintTicks(true);
    controls.add(pad(volumeRes));

    // arbitrary slice checkbox
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    doSlice = new JCheckBox("Arbitrary data slice: ", false);
    doSlice.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean b = doSlice.isSelected();
        sliceValue.setEnabled(b);
        sliceRes.setEnabled(b);
        sliceContinuous.setEnabled(b);
        bio.sm.setPlaneSelect(b);
        bio.vert.setEnabled(!b);
        bio.toolMeasure.setEnabled(!b);
        bio.mm.pool2.setSlice(b ? -1 : bio.sm.getSlice());
      }
    });
    doSlice.setEnabled(false);
    p.add(doSlice);

    // current slice value
    sliceValue = new JLabel("");
    d = doSlice.getPreferredSize();
    //sliceValue.setPreferredSize(new Dimension(detail - d.width, d.height));
    sliceValue.setEnabled(false);
    p.add(sliceValue);
    controls.add(pad(p));

    // slice slider
    sliceRes = new JSlider(0, detail, normal);
    sliceRes.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        doSliceRes(!sliceRes.getValueIsAdjusting());
      }
    });
    sliceRes.setEnabled(false);
    sliceRes.setMajorTickSpacing(detail / 4);
    sliceRes.setMinorTickSpacing(detail / 16);
    sliceRes.setPaintTicks(true);
    controls.add(pad(sliceRes));

    // continuous update checkbox
    sliceContinuous = new JCheckBox("Update slice continuously", false);
    sliceContinuous.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        bio.sm.setPlaneContinuous(sliceContinuous.isSelected());
      }
    });
    sliceContinuous.setEnabled(false);
    controls.add(pad(sliceContinuous));
  }


  // -- API METHODS --

  /** Initializes this tool panel. */
  public void init() {
    maxVolRes = bio.sm.res_x;
    if (bio.sm.res_y > maxVolRes) maxVolRes = bio.sm.res_y;
    int slices = bio.sm.getNumberOfSlices();
    if (slices > maxVolRes) maxVolRes = slices;
    int volVal = maxVolRes < 64 ? maxVolRes : 64;
    double volPercent = (double) volVal / maxVolRes;
    volumeRes.setValue((int) (volPercent * VisBio.RESOLUTION_DETAIL));
    int max = bio.sm.res_x < bio.sm.res_y ? bio.sm.res_x : bio.sm.res_y;
    int sliceVal = max < 64 ? max : 64;
    double slicePercent = (double) sliceVal / max;
    sliceRes.setValue((int) (slicePercent * VisBio.RESOLUTION_DETAIL));
  }

  /** Enables or disables this tool panel. */
  public void setEnabled(boolean enabled) {
    boolean b = enabled && bio.sm.hasThumbnails();
    loRes.setEnabled(b);
    hiRes.setEnabled(b);
    autoSwitch.setEnabled(b);
    anim.setEnabled(b);
    b = enabled && bio.sm.getNumberOfSlices() > 1;
    doVolume.setEnabled(b && bio.display3 != null);
    doSlice.setEnabled(b && bio.display3 != null);
  }

  /** Switches between lo-res and hi-res mode. */
  public void setMode(boolean lowres) {
    loRes.setSelected(lowres);
    hiRes.setSelected(!lowres);
  }


  // -- INTERNAL API METHODS --

  /** Sets the animation widget's animation control. */
  void setControl(AnimationControl control) { anim.setControl(control); }


  // -- HELPER METHODS --

  /** Recomputes volume resolution based on slider value. */
  private void doVolumeRes(boolean go) {
    int value = volumeRes.getValue();
    double percent = (double) value / VisBio.RESOLUTION_DETAIL;
    int res = (int) (percent * maxVolRes);
    if (res < 2) res = 2;
    volumeValue.setText(res + " x " + res + " x " + res);
    if (go) bio.sm.setVolumeResolution(res);
  }

  /** Recomputes slice resolution based on slider value. */
  private void doSliceRes(boolean go) {
    int value = sliceRes.getValue();
    double percent = (double) value / VisBio.RESOLUTION_DETAIL;
    int x = (int) (percent * bio.sm.res_x);
    int y = (int) (percent * bio.sm.res_y);
    if (x < 2) x = 2;
    if (y < 2) y = 2;
    sliceValue.setText(x + " x " + y);
    if (go) bio.sm.setSliceRange(x, y);
  }

}
