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

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import visad.browser.Divider;

/** MeasureToolbar is a custom toolbar. */
public class MeasureToolbar extends JPanel implements SwingConstants {

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
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // add line button
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    addLine = new JButton("New line");
    addLine.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = horiz.getValue() - 1;
        int slice = vert.getValue() - 1;
        MeasureMatrix mm = horiz.getMatrix();
        MeasureList list = mm.getMeasureList(index, slice);
        list.addMeasurement();
      }
    });
    addLine.setEnabled(false);
    p.add(addLine);

    // add marker button
    addMarker = new JButton("New marker");
    addMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = horiz.getValue() - 1;
        int slice = vert.getValue() - 1;
        MeasureMatrix mm = horiz.getMatrix();
        MeasureList list = mm.getMeasureList(index, slice);
        list.addMeasurement(true);
      }
    });
    addMarker.setEnabled(false);
    p.add(addMarker);
    add(pad(p));

    // grayscale checkbox
    grayscale = new JCheckBox("Grayscale");
    grayscale.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        /* CTR: TODO */ System.out.println("grayscale");
      }
    });
    grayscale.setEnabled(false);
    add(pad(grayscale));

    // divider between global functions and line functions
    add(Box.createVerticalStrut(10));
    add(new Divider());
    add(Box.createVerticalStrut(10));

    // remove line button
    removeLine = new JButton("Remove line");
    removeLine.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        /* CTR: TODO */ System.out.println("remove line");
      }
    });
    removeLine.setEnabled(false);
    add(pad(removeLine));

    // set standard button
    setStandard = new JButton("Set standard");
    setStandard.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        /* CTR: TODO */ System.out.println("set standard");
      }
    });
    setStandard.setEnabled(false);
    add(pad(setStandard));

    // measure now button
    measureNow = new JCheckBox("Measure now");
    measureNow.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        /* CTR: TODO */ System.out.println("measure now");
      }
    });
    measureNow.setEnabled(false);
    add(pad(measureNow));

    // group label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    groupLabel = new JLabel("Group");
    groupLabel.setEnabled(false);
    p.add(groupLabel);

    // group list
    groupList = new JComboBox();
    groupList.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        /* CTR: TODO */ System.out.println("group list");
      }
    });
    groupList.setEnabled(false);
    p.add(groupList);

    // new group button
    newGroup = new JButton("New");
    newGroup.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        /* CTR: TODO */ System.out.println("new group");
      }
    });
    newGroup.setEnabled(false);
    p.add(newGroup);
    add(p);

    // color label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    colorLabel = new JLabel("Color");
    colorLabel.setEnabled(false);
    p.add(colorLabel);

    // color list
    colorList = new JComboBox();
    colorList.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        /* CTR: TODO */ System.out.println("color list");
      }
    });
    colorList.setEnabled(false);
    p.add(colorList);
    add(p);

    // description label
    descriptionLabel = new JLabel("Description");
    descriptionLabel.setAlignmentX(LEFT);
    descriptionLabel.setEnabled(false);
    add(descriptionLabel);

    // description box
    descriptionBox = new JTextArea();
    descriptionBox.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e) { update(e); }
      public void update(DocumentEvent e) {
        /* CTR: TODO */ System.out.println("description changed");
      }
    });
    descriptionBox.setEnabled(false);
    add(new JScrollPane(descriptionBox));
  }

  /** Enables various toolbar controls. */
  public void setEnabled(boolean enabled) {
    addLine.setEnabled(enabled);
    addMarker.setEnabled(enabled);
    grayscale.setEnabled(enabled);
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
  }

  /** Pads a component or group of components with horizontal space. */
  private JPanel pad(JComponent c) {
    JPanel p;
    if (c instanceof JPanel) {
      p = (JPanel) c;
      p.add(Box.createHorizontalGlue(), 0);
      p.add(Box.createHorizontalGlue());
    }
    else {
      p = new JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
      p.add(Box.createHorizontalGlue());
      p.add(c);
      p.add(Box.createHorizontalGlue());
    }
    return p;
  }

}
