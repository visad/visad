//
// MeasureToolbar.java
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

package visad.bio;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import visad.browser.*;

/** MeasureToolbar is a custom toolbar. */
public class MeasureToolbar extends JPanel implements SwingConstants {

  /** List of colors for drop-down color box. */
  private static final Color[] COLORS = {
    Color.white, Color.lightGray, Color.gray, Color.darkGray, Color.black,
    Color.red, Color.orange, Color.yellow, Color.green,
    Color.cyan, Color.blue, Color.magenta, Color.pink
  };

  /** Placeholder for measurement information label. */
  private static final String INFO_LABEL =
    "   (000.000, 000.000)-(000.000, 000.000): distance=000.000";


  /** New group dialog box. */
  private GroupDialog groupBox = new GroupDialog();

  /** Currently selected measurement object. */
  private MeasureThing thing;

  /** File series widget. */
  private FileSeriesWidget horiz;

  /** Image stack widget. */
  private ImageStackWidget vert;


  // -- GLOBAL FUNCTIONS --

  /** Button for adding lines. */
  private JButton addLine;

  /** Button for adding points. */
  private JButton addMarker;

  /** Toggle for grayscale mode. */
  private JCheckBox grayscale;

  /** Label for brightness. */
  private JLabel brightnessLabel;

  /** Slider for level of brightness. */
  private JSlider brightness;


  // -- LINE FUNCTIONS --

  /** Label for displaying measurement information. */
  private JLabel measureInfo;

  /** Button for distributing measurement object through all focal planes. */
  private JButton setStandard;

  /** Button for removing objects. */
  private JButton removeThing;

  /** Label for color list. */
  private JLabel colorLabel;

  /** List of valid colors. */
  private JComboBox colorList;

  /** Label for group list. */
  private JLabel groupLabel;

  /** List of valid groups. */
  private JComboBox groupList;

  /** Button for adding a new group to the list. */
  private JButton newGroup;

  /** Label for description box. */
  private JLabel descriptionLabel;

  /** Text area for group description. */
  private JTextArea descriptionBox;


  /** Constructs a custom measurement toolbar. */
  public MeasureToolbar(FileSeriesWidget h, ImageStackWidget v) {
    horiz = h;
    vert = v;

    // main pane with horizontal spacing
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    add(Box.createHorizontalStrut(10));
    add(pane);
    add(Box.createHorizontalStrut(10));

    // controls panel
    JPanel controls = new JPanel() {
      public Dimension getMaximumSize() { return getPreferredSize(); }
    };
    controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
    pane.add(Box.createVerticalStrut(10));
    pane.add(controls);
    pane.add(Box.createVerticalStrut(30));
    pane.add(Box.createVerticalGlue());

    // add line button
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    addLine = new JButton("New line");
    addLine.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MeasureList list = getList();
        if (list != null) list.addMeasurement();
      }
    });
    addLine.setEnabled(false);
    p.add(addLine);
    p.add(Box.createHorizontalStrut(5));

    // add marker button
    addMarker = new JButton("New marker");
    addMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MeasureList list = getList();
        if (list != null) list.addMeasurement(true);
      }
    });
    addMarker.setEnabled(false);
    p.add(addMarker);
    controls.add(pad(p));
    controls.add(Box.createVerticalStrut(5));

    // grayscale checkbox
    grayscale = new JCheckBox("Grayscale");
    grayscale.setSelected(true);
    grayscale.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean gray = grayscale.isSelected();
        vert.setGrayscale(gray);
      }
    });
    grayscale.setEnabled(false);
    controls.add(pad(grayscale));

    // brightness label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    brightnessLabel = new JLabel("Brightness: ");
    brightnessLabel.setEnabled(false);
    p.add(brightnessLabel);

    // brightness slider
    brightness = new JSlider(1, 100, 50);
    brightness.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        vert.setBrightness(brightness.getValue());
      }
    });
    brightness.setEnabled(false);
    p.add(brightness);
    controls.add(p);

    // divider between global functions and object functions
    controls.add(Box.createVerticalStrut(10));
    controls.add(new Divider());
    controls.add(Box.createVerticalStrut(10));

    // measurement information label
    measureInfo = new JLabel(" ") {
      public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int width = fm.stringWidth("    " + INFO_LABEL);
        Dimension d = super.getPreferredSize();
        return new Dimension(width, d.height);
      }
    };
    controls.add(pad(measureInfo));
    controls.add(Box.createVerticalStrut(10));

    // set standard button
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    setStandard = new JButton("Set standard");
    setStandard.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        /* CTR: TODO */ System.out.println("set standard");
      }
    });
    setStandard.setEnabled(false);
    p.add(setStandard);
    p.add(Box.createHorizontalStrut(5));

    // remove thing button
    removeThing = new JButton("Remove");
    removeThing.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getList().removeMeasurement(thing.getMeasurement());
      }
    });
    removeThing.setEnabled(false);
    p.add(removeThing);
    controls.add(pad(p));
    controls.add(Box.createVerticalStrut(5));

    // color label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    colorLabel = new JLabel("Color");
    colorLabel.setEnabled(false);
    p.add(colorLabel);

    // color list
    colorList = new JComboBox(COLORS);
    colorList.setRenderer(new ColorRenderer());
    colorList.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (thing instanceof MeasureLine) {
          MeasureLine line = (MeasureLine) thing;
          int index = colorList.getSelectedIndex();
          line.setColor(COLORS[index]);
        }
      }
    });
    colorList.setEnabled(false);
    p.add(colorList);
    controls.add(p);
    controls.add(Box.createVerticalStrut(5));

    // group label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    groupLabel = new JLabel("Group");
    groupLabel.setEnabled(false);
    p.add(groupLabel);

    // group list
    groupList = new JComboBox();
    groupList.addItem(MeasureList.DEFAULT_GROUP);
    groupList.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        LineGroup group = (LineGroup) groupList.getSelectedItem();
        thing.setGroup(group);
        descriptionBox.setText(group.getDescription());
      }
    });
    groupList.setEnabled(false);
    p.add(groupList);

    // new group button
    newGroup = new JButton("New");
    newGroup.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rval = groupBox.showDialog(null);
        if (rval == GroupDialog.APPROVE_OPTION) {
          String name = groupBox.getGroupName();
          LineGroup group = new LineGroup(name);
          groupList.addItem(group);
          groupList.setSelectedItem(group);
        }
      }
    });
    newGroup.setEnabled(false);
    p.add(newGroup);
    controls.add(p);

    // description label
    descriptionLabel = new JLabel("Description");
    descriptionLabel.setAlignmentX(LEFT);
    descriptionLabel.setEnabled(false);
    controls.add(pad(descriptionLabel));

    // description box
    descriptionBox = new JTextArea();
    descriptionBox.setRows(4);
    descriptionBox.setLineWrap(true);
    descriptionBox.setWrapStyleWord(true);
    descriptionBox.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e) { update(e); }
      public void update(DocumentEvent e) {
        LineGroup group = (LineGroup) groupList.getSelectedItem();
        group.setDescription(descriptionBox.getText());
      }
    });
    descriptionBox.setEnabled(false);
    controls.add(new JScrollPane(descriptionBox));
  }

  /** Enables various toolbar controls. */
  public void setEnabled(boolean enabled) {
    addLine.setEnabled(enabled);
    addMarker.setEnabled(enabled);
    grayscale.setEnabled(enabled);
    brightnessLabel.setEnabled(enabled);
    brightness.setEnabled(enabled);
  }

  /** Selects the given measurement object. */
  public void select(MeasureThing thing) {
    this.thing = thing;
    boolean enabled = thing != null;
    boolean line = thing instanceof MeasureLine;
    updateMeasureInfo();
    setStandard.setEnabled(enabled);
    removeThing.setEnabled(enabled);
    colorLabel.setEnabled(enabled && line);
    colorList.setEnabled(enabled && line);
    groupLabel.setEnabled(enabled);
    groupList.setEnabled(enabled);
    newGroup.setEnabled(enabled);
    descriptionLabel.setEnabled(enabled);
    descriptionBox.setEnabled(enabled);
    if (enabled) {
      Measurement m = thing.getMeasurement();
      colorList.setSelectedItem(m.getColor());
      groupList.setSelectedItem(m.getGroup());
    }
  }

  /** Updates the text in the measurement information box. */
  private void updateMeasureInfo() {
    String text = " ";
    if (thing != null) {
      Measurement m = thing.getMeasurement();
      double[][] vals = m.doubleValues();
      if (thing instanceof MeasureLine) {
        String v1x = Convert.shortString(vals[0][0]);
        String v1y = Convert.shortString(vals[1][0]);
        String v2x = Convert.shortString(vals[0][1]);
        String v2y = Convert.shortString(vals[1][1]);
        String dist = Convert.shortString(m.getDistance());
        text = "(" + v1x + ", " + v1y + ")-(" + v2x + ", " + v2y + "): " +
          "distance=" + dist;
      }
      else {
        String vx = Convert.shortString(vals[0][0]);
        String vy = Convert.shortString(vals[1][0]);
        text = "(" + vx + ", " + vy + ")";
      }
      int space = (INFO_LABEL.length() - text.length()) / 2;
      for (int i=0; i<space; i++) text = " " + text + " ";
    }
    measureInfo.setText("   " + text);
  }

  /** Gets the current measurement list from the slider widgets. */
  private MeasureList getList() {
    int index = horiz.getValue() - 1;
    int slice = vert.getValue() - 1;
    return horiz.getMatrix().getMeasureList(index, slice);
  }

  /** Pads a component or group of components with horizontal space. */
  private JPanel pad(Component c) {
    JPanel p;
    if (c instanceof JPanel) {
      p = (JPanel) c;
      p.add(Box.createHorizontalGlue(), 0);
      p.add(Box.createHorizontalStrut(5), 0);
      p.add(Box.createHorizontalGlue());
      p.add(Box.createHorizontalStrut(5));
    }
    else {
      p = new JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
      p.add(Box.createHorizontalStrut(5));
      p.add(Box.createHorizontalGlue());
      p.add(c);
      p.add(Box.createHorizontalGlue());
      p.add(Box.createHorizontalStrut(5));
    }
    return p;
  }

}
