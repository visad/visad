//
// MeasureToolPanel.java
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
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.browser.*;
import visad.util.Util;

/**
 * MeasureToolPanel is the tool panel for
 * managing measurements between data points.
 */
public class MeasureToolPanel extends ToolPanel {

  // -- CONSTANTS --

  /** List of colors for drop-down color box. */
  private static final Color[] COLORS = {
    Color.white, Color.lightGray, Color.gray, Color.darkGray, Color.black,
    Color.red, Color.orange, Color.yellow, Color.green,
    Color.cyan, Color.blue, Color.magenta, Color.pink
  };

  /** Placeholder for measurement coordinates label. */
  private static final String COORD_LABEL =
    " (0000.000, 0000.000)-(0000.000, 0000.000)";

  /** Placeholder for measurement distance label. */
  private static final String DIST_LABEL = "distance = 0000.000 pix";


  // -- GLOBAL VARIABLES --

  /** First free id number for standard measurements. */
  static int maxId = 0;


  // -- FIELDS --

  /** New group dialog box. */
  private GroupDialog groupBox = new GroupDialog();

  /** Computation cell for linking selection with measurement object. */
  private CellImpl cell;

  /** Flag marking whether to ignore next set standard checkbox toggle. */
  private boolean ignoreNextStandard = false;

  /** Flag marking whether to ignore group list changes. */
  private boolean ignoreGroup = false;

  /** Flag marking whether set standard checkbox can be enabled. */
  private boolean stdEnabled = true;


  // -- FILE IO FUNCTIONS --

  /** Label for measurement-related controls. */
  private JLabel measureLabel;

  /** Button for saving measurements to a file. */
  private JButton saveLines;

  /** Button for restoring measurements from a file. */
  private JButton restoreLines;

  /**
   * Check box for indicating file should be saved or restored
   * using a micron-pixel conversion of the given width and height.
   */
  private DoubleTextCheckBox useMicrons;

  /** Label for distance between slices. */
  private JLabel sliceDistLabel;

  /** Text field for specifying distance (in microns) between slices. */
  private JTextField sliceDistance;


  // -- GLOBAL FUNCTIONS --

  /** Button for adding lines. */
  private JButton addLine;

  /** Button for adding points. */
  private JButton addMarker;

  /** Button for clearing all measurements. */
  private JButton clearAll;


  // -- LINE FUNCTIONS --

  /** Label for displaying measurement coordinates. */
  private JLabel measureCoord;

  /** Label for displaying measurement distance. */
  private JLabel measureDist;

  /** Button for distributing measurement object through all focal planes. */
  private JCheckBox setStandard;

  /** Button for toggling whether SHIFT + right click does a merge. */
  private JToggleButton merge;

  /** Button for removing objects. */
  private JButton removeSelected;

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


  // -- CONSTRUCTOR --

  /** Constructs a tool panel for performing measurement operations. */
  public MeasureToolPanel(BioVisAD biovis) {
    super(biovis);

    // measurements label
    measureLabel = new JLabel("Measurements:");
    measureLabel.setForeground(Color.black);
    measureLabel.setEnabled(false);
    controls.add(pad(measureLabel));
    controls.add(Box.createVerticalStrut(5));

    // save measurements button
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    saveLines = new JButton("Save");
    saveLines.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.mm.saveMeasurements();
      }
    });
    saveLines.setEnabled(false);
    p.add(saveLines);
    p.add(Box.createHorizontalStrut(5));

    // restore measurements button
    restoreLines = new JButton("Restore");
    restoreLines.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.mm.restoreMeasurements();
      }
    });
    restoreLines.setEnabled(false);
    p.add(restoreLines);
    controls.add(pad(p));

    // microns vs pixels checkbox
    useMicrons = new DoubleTextCheckBox(
      "Use microns instead of pixels", "by", "", "", false);
    final MeasureToolPanel tool = this;
    useMicrons.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { tool.updateFileButtons(); }
    });
    useMicrons.setEnabled(false);
    controls.add(pad(useMicrons));

    // slice distance label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    sliceDistLabel = new JLabel("Microns between slices: ");
    sliceDistLabel.setForeground(Color.black);
    sliceDistLabel.setEnabled(false);
    p.add(sliceDistLabel);

    // distance between slices text box
    sliceDistance = new JTextField();
    Util.adjustTextField(sliceDistance);
    sliceDistance.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { tool.updateFileButtons(); }
      public void insertUpdate(DocumentEvent e) { tool.updateFileButtons(); }
      public void removeUpdate(DocumentEvent e) { tool.updateFileButtons(); }
    });
    sliceDistance.setEnabled(false);
    p.add(sliceDistance);
    controls.add(pad(p));

    // spacing
    controls.add(Box.createVerticalStrut(15));

    // add line button
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    addLine = new JButton("New line");
    addLine.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.mm.getList().addLine();
        bio.state.saveState(false);
      }
    });
    addLine.setEnabled(false);
    p.add(addLine);
    p.add(Box.createHorizontalStrut(5));

    // add marker button
    addMarker = new JButton("New marker");
    addMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.mm.getList().addMarker();
        bio.state.saveState(false);
      }
    });
    addMarker.setEnabled(false);
    p.add(addMarker);
    p.add(Box.createHorizontalStrut(5));

    // clear all measurements button
    clearAll = new JButton("Clear all");
    clearAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int ans = JOptionPane.showConfirmDialog(tool,
          "Really clear all measurements?", "BioVisAD",
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ans != JOptionPane.YES_OPTION) return;
        bio.mm.clear();
        bio.state.saveState(false);
      }
    });
    clearAll.setEnabled(false);
    p.add(clearAll);
    controls.add(pad(p));

    // spacing
    controls.add(Box.createVerticalStrut(15));

    // merge button
    merge = new JToggleButton("Merge");
    merge.setEnabled(false);
    controls.add(pad(merge));

    // divider between global functions and measurement-specific functions
    controls.add(Box.createVerticalStrut(10));
    controls.add(new Divider());
    controls.add(Box.createVerticalStrut(10));

    // measurement information label
    measureCoord = new JLabel(" ") {
      public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int width = fm.stringWidth("    " + COORD_LABEL);
        Dimension d = super.getPreferredSize();
        return new Dimension(width, d.height);
      }
    };
    controls.add(pad(measureCoord));
    measureDist = new JLabel(" ") {
      public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int width = fm.stringWidth("    " + DIST_LABEL);
        Dimension d = super.getPreferredSize();
        return new Dimension(width, d.height);
      }
    };
    controls.add(pad(measureDist));
    controls.add(Box.createVerticalStrut(10));

    cell = new CellImpl() {
      public void doAction() { updateMeasureInfo(); }
    };

    // set standard button
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    setStandard = new JCheckBox("Set standard");
    setStandard.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (ignoreNextStandard) {
          ignoreNextStandard = false;
          return;
        }
        boolean std = setStandard.isSelected();
        if (!std) {
          int ans = JOptionPane.showConfirmDialog(tool,
            "Are you sure?", "Unset standard", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
          if (ans != JOptionPane.YES_OPTION) {
            ignoreNextStandard = true;
            setStandard.setSelected(true);
            return;
          }
        }
        MeasureThing[] things = bio.mm.pool2.getSelection();
        for (int i=0; i<things.length; i++) doStandard(things[i], std);
        bio.mm.changed = true;
        bio.state.saveState(false);
      }
    });
    setStandard.setEnabled(false);
    p.add(setStandard);
    p.add(Box.createHorizontalStrut(5));

    // remove button
    removeSelected = new JButton("Remove");
    removeSelected.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.mm.getList().removeSelected();
        bio.state.saveState(false);
        updateSelection();
      }
    });
    removeSelected.setEnabled(false);
    p.add(removeSelected);
    controls.add(pad(p));
    controls.add(Box.createVerticalStrut(5));

    // color label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    colorLabel = new JLabel("Color: ");
    colorLabel.setForeground(Color.black);
    colorLabel.setEnabled(false);
    p.add(colorLabel);

    // color list
    colorList = new JComboBox(COLORS);
    colorList.setRenderer(new ColorRenderer());
    colorList.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Color color = COLORS[colorList.getSelectedIndex()];
        MeasureThing[] things = bio.mm.pool2.getSelection();
        boolean changed = false;
        for (int i=0; i<things.length; i++) {
          if (things[i].color != color) {
            changed = true;
            things[i].color = color;
          }
        }
        if (changed) {
          bio.mm.changed = true;
          bio.state.saveState(false);
          bio.mm.pool2.refresh(true);
          bio.mm.pool3.refresh(true);
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
    groupLabel = new JLabel("Group: ");
    groupLabel.setForeground(Color.black);
    groupLabel.setEnabled(false);
    p.add(groupLabel);

    // group list
    groupList = new JComboBox();
    groupList.addItem(bio.noneGroup);
    groupList.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        MeasureGroup group = (MeasureGroup) groupList.getSelectedItem();
        MeasureThing[] things = bio.mm.pool2.getSelection();
        boolean changed = false;
        for (int i=0; i<things.length; i++) {
          if (things[i].group != group) {
            changed = true;
            things[i].group = group;
          }
        }
        if (group != null) descriptionBox.setText(group.getDescription());
        if (changed) {
          bio.mm.changed = true;
          bio.state.saveState(false);
        }
      }
    });
    groupList.setEnabled(false);
    p.add(groupList);

    // new group button
    newGroup = new JButton("New");
    newGroup.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rval = groupBox.showDialog(bio);
        if (rval == GroupDialog.APPROVE_OPTION) {
          String name = groupBox.getGroupName();
          MeasureGroup group = new MeasureGroup(bio, name);
          groupList.addItem(group);
          groupList.setSelectedItem(group);
          bio.mm.changed = true;
          bio.state.saveState(false);
        }
      }
    });
    newGroup.setEnabled(false);
    p.add(newGroup);
    controls.add(p);

    // description label
    descriptionLabel = new JLabel("Group description:");
    descriptionLabel.setAlignmentX(SwingConstants.LEFT);
    descriptionLabel.setForeground(Color.black);
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
        MeasureGroup group = (MeasureGroup) groupList.getSelectedItem();
        group.setDescription(descriptionBox.getText());
        bio.state.saveState(false);
      }
    });
    descriptionBox.setEnabled(false);
    controls.add(new JScrollPane(descriptionBox));
  }


  // -- API METHODS --

  /** Enables or disables this tool panel. */
  public void setEnabled(boolean enabled) {
    if (enabled) {
      useMicrons.setEnabled(true);
      measureLabel.setEnabled(true);
      restoreLines.setEnabled(true);
      updateFileButtons();
    }
    else {
      useMicrons.setEnabled(false);
      measureLabel.setEnabled(false);
      saveLines.setEnabled(false);
      restoreLines.setEnabled(false);
      sliceDistLabel.setEnabled(false);
      sliceDistance.setEnabled(false);
    }
    addLine.setEnabled(enabled);
    addMarker.setEnabled(enabled);
    clearAll.setEnabled(enabled);
    merge.setEnabled(enabled);
  }

  /** Enables or disables the "set standard" checkbox. */
  public void setStandardEnabled(boolean enabled) {
    stdEnabled = enabled;
    setStandard.setEnabled(bio.mm.pool2.hasSelection() && enabled);
  }

  /** Updates the selection data to match the current measurement list. */
  public void updateSelection() {
    boolean enabled = bio.mm.pool2.hasSelection();
    setStandard.setEnabled(enabled && stdEnabled);
    removeSelected.setEnabled(enabled);
    colorLabel.setEnabled(enabled);
    colorList.setEnabled(enabled);
    groupLabel.setEnabled(enabled);
    groupList.setEnabled(enabled);
    newGroup.setEnabled(enabled);
    descriptionLabel.setEnabled(enabled);
    descriptionBox.setEnabled(enabled);
    if (enabled) {
      boolean std = bio.mm.pool2.isSelectionStandard();
      if (setStandard.isSelected() != std) {
        ignoreNextStandard = true;
        setStandard.setSelected(std);
      }
      Color c = bio.mm.pool2.getSelectionColor();
      if (colorList.getSelectedItem() != c) colorList.setSelectedItem(c);
      MeasureGroup g = bio.mm.pool2.getSelectionGroup();
      if (groupList.getSelectedItem() != g) groupList.setSelectedItem(g);
    }
    synchronized (cell) {
      try {
        cell.disableAction();
        cell.removeAllReferences();
        boolean trigger = !enabled;
        if (enabled) {
          PoolPoint[] pts = bio.mm.pool2.getSelectionPts();
          for (int i=0; i<pts.length; i++) cell.addReference(pts[i].ref);
          if (pts.length == 0) trigger = true;
        }
        cell.enableAction();
        if (trigger) cell.doAction();
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
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

    // update groups
    groupList.removeAllItems();
    for (int i=0; i<bio.mm.groups.size(); i++) {
      groupList.addItem(bio.mm.groups.elementAt(i));
    }
    descriptionBox.setText("");
  }

  /** Sets the slice distance to match the specified one. */
  void setSliceDistance(double sd) {
    String dist = "" + sd;
    if (dist.equals(sliceDistance.getText())) return;
    sliceDistance.setText(dist);
  }

  /** Sets the merge toggle button's status. */
  void setMerge(boolean merge) {
    if (merge != this.merge.isSelected()) this.merge.setSelected(merge);
  }

  /** Gets the merge toggle button's status. */
  boolean getMerge() { return merge.isSelected(); }


  // -- HELPER METHODS --

  /** Updates the micron-related GUI components. */
  private void updateFileButtons() {
    boolean microns = useMicrons.isSelected();
    useMicrons.setEnabled(microns);
    sliceDistLabel.setEnabled(microns);
    sliceDistance.setEnabled(microns);
    boolean b;
    if (microns) {
      double mw = getMicronWidth();
      double mh = getMicronHeight();
      double sd = getSliceDistance();
      b = mw == mw && mh == mh && sd == sd;
    }
    else b = true;
    bio.toolView.updateAspect(!microns);
    saveLines.setEnabled(b);
    updateMeasureInfo();
  }

  /** Updates the text in the measurement information box. */
  private void updateMeasureInfo() {
    String coord = "";
    String dist = "";
    if (bio.mm.pool2.hasSingleSelection()) {
      Object thing = bio.mm.pool2.getSelection()[0];
      boolean use = useMicrons.isSelected();
      double mw = getMicronWidth();
      double mh = getMicronHeight();
      double mx = mw / bio.sm.res_x;
      double my = mw / bio.sm.res_y;
      double sd = getSliceDistance();
      boolean microns = use && mx == mx && my == my && sd == sd;
      if (!microns) mx = my = sd = 1;
      String unit = microns ? "µ" : "pix";
      if (thing instanceof MeasureLine) {
        MeasureLine line = (MeasureLine) thing;
        String vx = Convert.shortString(mx * line.ep1.x);
        String vy = Convert.shortString(my * line.ep1.y);
        String v2x = Convert.shortString(mx * line.ep2.x);
        String v2y = Convert.shortString(my * line.ep2.y);
        String d = Convert.shortString(
          BioUtil.getDistance(line.ep1.x, line.ep1.y, line.ep1.z,
          line.ep2.x, line.ep2.y, line.ep2.z, mx, my, sd));
        coord = "(" + vx + ", " + vy + ")-(" + v2x + ", " + v2y + ")";
        dist = "distance = " + d + " " + unit;
      }
      else if (thing instanceof MeasurePoint) {
        MeasurePoint point = (MeasurePoint) thing;
        String vx = Convert.shortString(mx * point.x);
        String vy = Convert.shortString(my * point.y);
        coord = "(" + vx + ", " + vy + ")";
      }
    }

    StringBuffer sb = new StringBuffer();
    int space = (COORD_LABEL.length() - coord.length()) / 2;
    for (int i=0; i<space; i++) sb.append(" ");
    String coordSpace = sb.toString();

    sb = new StringBuffer();
    space = (DIST_LABEL.length() - dist.length()) / 2;
    for (int i=0; i<space; i++) sb.append(" ");
    String distSpace = sb.toString();

    measureCoord.setText(coordSpace + coord + coordSpace);
    measureDist.setText(distSpace + dist + distSpace);
  }

  /** Sets or unsets the given measurement as standard. */
  private void doStandard(MeasureThing thing, boolean std) {
    boolean isLine = thing instanceof MeasureLine;
    int index = bio.sm.getIndex();
    int slice = bio.sm.getSlice();
    if (std) {
      // set standard
      if (thing.stdId != -1) {
        // line already standard; skip it
        return;
      }
      thing.setStdId(maxId++);
      int numSlices = bio.sm.getNumberOfSlices();
      for (int j=0; j<bio.mm.lists.length; j++) {
        MeasureList list = bio.mm.lists[j];
        boolean update = j == index;
        for (int i=0; i<numSlices; i++) {
          if (j == index && i == slice) continue;
          if (isLine) {
            MeasureLine line = new MeasureLine((MeasureLine) thing, i);
            list.addLine(line, update);
          }
          else {
            MeasurePoint point = new MeasurePoint((MeasurePoint) thing, i);
            list.addMarker(point, update);
          }
        }
      }
    }
    else {
      // unset standard
      if (thing.stdId == -1) {
        // line not standard; skip it
        return;
      }
      int stdId = thing.stdId;
      thing.setStdId(-1);
      for (int j=0; j<bio.mm.lists.length; j++) {
        MeasureList list = bio.mm.lists[j];
        boolean update = j == index;
        Vector lines = list.getLines();
        int k = 0;
        while (k < lines.size()) {
          MeasureLine line = (MeasureLine) lines.elementAt(k);
          if (line.stdId == stdId) list.removeLine(line, update);
          else k++;
        }
        Vector points = list.getPoints();
        k = 0;
        while (k < points.size()) {
          MeasurePoint point = (MeasurePoint) points.elementAt(k);
          if (point.stdId == stdId) list.removeMarker(point, update);
          else k++;
        }
      }
    }
  }

}
