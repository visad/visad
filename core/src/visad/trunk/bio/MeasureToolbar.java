//
// MeasureToolbar.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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
import visad.browser.Divider;

/** MeasureToolbar is a custom toolbar. */
public class MeasureToolbar extends JPanel implements SwingConstants {

  /** List of colors for drop-down color box. */
  private static final Color[] COLORS = {
    Color.white, Color.lightGray, Color.gray, Color.darkGray, Color.black,
    Color.red, Color.orange, Color.yellow, Color.green,
    Color.cyan, Color.blue, Color.magenta, Color.pink
  };
    

  /** New group dialog box. */
  private GroupDialog groupBox = new GroupDialog();

  /** Currently selected line. */
  private MeasureLine line;

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

  /** Label for contrast. */
  private JLabel contrastLabel;

  /** Slider for level of brightness. */
  private JSlider brightness;

  /** Slider for level of contrast. */
  private JSlider contrast;


  // -- LINE FUNCTIONS --

  /** Button for removing lines. */
  private JButton removeLine;

  /** Button for distributing line throughout all focal planes. */
  private JButton setStandard;

  /** Toggle for writing measurement to file. */
  private JCheckBox measureNow;

  /** Label for group list. */
  private JLabel groupLabel;

  /** List of valid groups. */
  private JComboBox groupList;

  /** Button for adding a new group to the list. */
  private JButton newGroup;

  /** Label for color list. */
  private JLabel colorLabel;

  /** List of valid colors. */
  private JComboBox colorList;

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

    // add line button
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    addLine = new JButton("New line");
    addLine.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getList().addMeasurement();
      }
    });
    addLine.setEnabled(false);
    p.add(addLine);

    // add marker button
    addMarker = new JButton("New marker");
    addMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getList().addMeasurement(true);
      }
    });
    addMarker.setEnabled(false);
    p.add(addMarker);
    pane.add(pad(p));

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
    pane.add(pad(grayscale));

    // brightness label
    JPanel p2 = new JPanel();
    p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
    brightnessLabel = new JLabel("Brightness: ");
    brightnessLabel.setEnabled(false);
    p2.add(brightnessLabel);

    // contrast label
    contrastLabel = new JLabel("Contrast: ");
    contrastLabel.setEnabled(false);
    p2.add(contrastLabel);
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.add(p2);

    // brightness slider
    p2 = new JPanel();
    p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
    brightness = new JSlider(1, 100, 50);
    brightness.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        vert.setBrightness(brightness.getValue());
      }
    });
    brightness.setEnabled(false);
    p2.add(brightness);

    // contrast slider
    contrast = new JSlider(1, 100, 50);
    contrast.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        vert.setContrast(contrast.getValue());
      }
    });
    contrast.setEnabled(false);
    p2.add(contrast);
    p.add(p2);
    pane.add(p);

    // divider between global functions and line functions
    pane.add(Box.createVerticalStrut(10));
    pane.add(new Divider());
    pane.add(Box.createVerticalStrut(10));

    // remove line button
    removeLine = new JButton("Remove line");
    removeLine.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getList().removeMeasurement(line.getMeasurement());
      }
    });
    removeLine.setEnabled(false);
    pane.add(pad(removeLine));

    // set standard button
    setStandard = new JButton("Set standard");
    setStandard.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        /* CTR: TODO */ System.out.println("set standard");
      }
    });
    setStandard.setEnabled(false);
    pane.add(pad(setStandard));

    // measure now button
    measureNow = new JCheckBox("Measure now");
    measureNow.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        /* CTR: TODO */ System.out.println("measure now");
      }
    });
    measureNow.setEnabled(false);
    pane.add(pad(measureNow));

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
        line.setGroup(group);
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
    pane.add(p);

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
        int index = colorList.getSelectedIndex();
        line.setColor(COLORS[index]);
      }
    });
    colorList.setEnabled(false);
    p.add(colorList);
    pane.add(p);

    // description label
    descriptionLabel = new JLabel("Description");
    descriptionLabel.setAlignmentX(LEFT);
    descriptionLabel.setEnabled(false);
    pane.add(pad(descriptionLabel));

    // description box
    descriptionBox = new JTextArea();
    descriptionBox.setRows(4);
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
    pane.add(new JScrollPane(descriptionBox));

    // some extra vertical space
    pane.add(Box.createVerticalStrut(30));
    pane.add(Box.createVerticalGlue());
  }

  /** Enables various toolbar controls. */
  public void setEnabled(boolean enabled) {
    addLine.setEnabled(enabled);
    addMarker.setEnabled(enabled);
    grayscale.setEnabled(enabled);
    brightnessLabel.setEnabled(enabled);
    contrastLabel.setEnabled(enabled);
    brightness.setEnabled(enabled);
    contrast.setEnabled(enabled);
  }

  /** Selects the given measurement line. */
  public void select(MeasureLine line) {
    this.line = line;
    boolean enabled = line != null;
    removeLine.setEnabled(enabled);
    setStandard.setEnabled(enabled);
    measureNow.setEnabled(enabled);
    groupLabel.setEnabled(enabled);
    groupList.setEnabled(enabled);
    newGroup.setEnabled(enabled);
    colorLabel.setEnabled(enabled);
    colorList.setEnabled(enabled);
    descriptionLabel.setEnabled(enabled);
    descriptionBox.setEnabled(enabled);
    if (enabled) {
      Measurement m = line.getMeasurement();
      colorList.setSelectedItem(m.color);
      groupList.setSelectedItem(m.group);
    }
  }

  /** Gets the current measurement list from the slider widgets. */
  private MeasureList getList() {
    int index = horiz.getValue() - 1;
    int slice = vert.getValue() - 1;
    return horiz.matrix.getMeasureList(index, slice);
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
