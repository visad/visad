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
import java.io.File;
import java.rmi.RemoteException;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.browser.*;
import visad.util.*;

/**
 * MeasureToolPanel is the tool panel for
 * managing measurements between data points.
 */
public class MeasureToolPanel extends ToolPanel {

  // -- CONSTANTS --

  /** List of colors for drop-down color box. */
  private static final Color[] COLORS = {
    Color.white, Color.red, Color.orange, Color.green,
    Color.cyan, Color.blue, Color.magenta, Color.pink,
    Color.lightGray, Color.gray, Color.darkGray, Color.black
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
  private boolean ignoreStandard = false;

  /** Flag marking whether to ignore group list changes. */
  private boolean ignoreGroup = false;

  /** Flag marking whether set standard checkbox can be enabled. */
  private boolean stdEnabled = true;


  // -- GLOBAL FUNCTIONS --

  /** Button for adding lines. */
  private JButton addLine;

  /** Button for adding points. */
  private JButton addMarker;

  /** Button for toggling whether SHIFT + right click does a merge. */
  private JToggleButton merge;

  /** Button for clearing all measurements. */
  private JButton clearAll;

  /** Button for exporting measurements to Excel-friendly text format. */
  private JButton export;

  /** File chooser for exporting measurements. */
  private JFileChooser exportBox;

  /** Checkbox for snapping measurement endpoints to nearest slice. */
  private JCheckBox snap;


  // -- LINE FUNCTIONS --

  /** Label for displaying measurement coordinates. */
  private JLabel measureCoord;

  /** Label for displaying measurement distance. */
  private JLabel measureDist;

  /** Button for standalone measurement (this focal plane/timestep only). */
  private JRadioButton single;

  /**
   * Button for distributing measurement through
   * all focal planes of all timesteps.
   */
  private JRadioButton standard2D;

  /** Button for distributing measurement through all timesteps. */
  private JRadioButton standard3D;

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
  public MeasureToolPanel(VisBio biovis) {
    super(biovis);

    // add line button
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    addLine = new JButton("New line");
    addLine.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.mm.getList().addLine();
        bio.state.saveState();
      }
    });
    addLine.setMnemonic('l');
    addLine.setToolTipText("Adds a line to the current slice");
    addLine.setEnabled(false);
    p.add(addLine);
    p.add(Box.createHorizontalStrut(5));

    // add marker button
    addMarker = new JButton("New marker");
    addMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.mm.getList().addMarker();
        bio.state.saveState();
      }
    });
    addMarker.setMnemonic('k');
    addMarker.setToolTipText("Adds a marker to the current slice");
    addMarker.setEnabled(false);
    p.add(addMarker);
    controls.add(pad(p));

    // spacing
    controls.add(Box.createVerticalStrut(5));

    // merge button
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    merge = new JToggleButton("Merge");
    merge.setMnemonic('m');
    merge.setToolTipText("Allows for merging multiple measurement endpoints");
    merge.setEnabled(false);
    p.add(merge);
    p.add(Box.createHorizontalStrut(5));

    // remove button
    removeSelected = new JButton("Remove");
    removeSelected.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.mm.getList().removeSelected();
        bio.state.saveState();
        updateSelection();
      }
    });
    removeSelected.setMnemonic('r');
    removeSelected.setToolTipText("Removes the selected measurements");
    removeSelected.setEnabled(false);
    p.add(removeSelected);
    p.add(Box.createHorizontalStrut(5));

    // clear all measurements button
    final MeasureToolPanel tool = this;
    clearAll = new JButton("Clear all");
    clearAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int ans = JOptionPane.showConfirmDialog(tool,
          "Really clear all measurements?", "VisBio",
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ans != JOptionPane.YES_OPTION) return;
        bio.mm.clear();
        bio.state.saveState();
      }
    });
    clearAll.setMnemonic('a');
    clearAll.setToolTipText("Removes all measurements");
    clearAll.setEnabled(false);
    p.add(clearAll);
    controls.add(pad(p));

    // spacing
    controls.add(Box.createVerticalStrut(5));

    // export measurements button
    export = new JButton("Export measurements");
    final Component panel = this;
    export.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rval = exportBox.showSaveDialog(panel);
        if (rval != JFileChooser.APPROVE_OPTION) return;
        File file = exportBox.getSelectedFile();
        if (file.getName().indexOf(".") < 0) {
          file = new File(file.getAbsolutePath() + ".txt");
        }
        bio.mm.export(file);
      }
    });
    export.setMnemonic('x');
    export.setToolTipText(
      "Exports measurements to Excel-friendly text format");
    export.setEnabled(false);
    controls.add(pad(export));

    // export measurements file chooser
    exportBox = new JFileChooser();
    exportBox.addChoosableFileFilter(new ExtensionFileFilter(
      "txt", "VisBio measurements"));

    // snap to slices checkbox
    snap = new JCheckBox("Snap endpoints to nearest slice", true);
    snap.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean b = snap.isSelected();
        bio.sm.setSnap(b);
      }
    });
    snap.setMnemonic('p');
    snap.setToolTipText("Prevents measurement endpoints " +
      "from lying between slices.");
    snap.setEnabled(false);
    controls.add(pad(snap));

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
    measureCoord.setToolTipText("Coordinates " +
      "of the current measurement");
    controls.add(pad(measureCoord));
    measureDist = new JLabel(" ") {
      public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int width = fm.stringWidth("    " + DIST_LABEL);
        Dimension d = super.getPreferredSize();
        return new Dimension(width, d.height);
      }
    };
    measureDist.setToolTipText("Distance between " +
      "the current line's endpoints");
    controls.add(pad(measureDist));
    controls.add(Box.createVerticalStrut(10));

    cell = new CellImpl() {
      public void doAction() { updateMeasureInfo(); }
    };

    // single measurement button
    ButtonGroup group = new ButtonGroup();
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    single = new JRadioButton("Single", true);
    single.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (!single.isSelected()) return;
        if (ignoreStandard) {
          ignoreStandard = false;
          return;
        }
        int stdType = MeasureThing.STD_SINGLE;
        MeasureThing[] things = bio.mm.pool2.getSelection();
        for (int i=0; i<things.length; i++) {
          int type = things[i].stdType;
          if (type != MeasureThing.STD_SINGLE) {
            stdType = type;
            break;
          }
        }
        if (stdType == MeasureThing.STD_SINGLE) return; // no change
        int ans = JOptionPane.showConfirmDialog(tool,
          "Are you sure?", "Unset standard", JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE);
        if (ans != JOptionPane.YES_OPTION) {
          ignoreStandard = true;
          if (stdType == MeasureThing.STD_2D) standard2D.setSelected(true);
          else if (stdType == MeasureThing.STD_3D) {
            standard3D.setSelected(true);
          }
          return;
        }
        for (int i=0; i<things.length; i++) {
          doStandard(things[i], MeasureThing.STD_SINGLE);
        }
        bio.mm.changed = true;
        bio.state.saveState();
      }
    });
    group.add(single);
    single.setMnemonic('s');
    single.setToolTipText("Sets selected measurements " +
      "to this slice & timestep only");
    single.setEnabled(false);
    p.add(single);
    p.add(Box.createHorizontalStrut(5));

    // 2-D standard button
    standard2D = new JRadioButton("2-D standard");
    standard2D.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (!standard2D.isSelected()) return;
        if (ignoreStandard) {
          ignoreStandard = false;
          return;
        }
        MeasureThing[] things = bio.mm.pool2.getSelection();
        for (int i=0; i<things.length; i++) {
          doStandard(things[i], MeasureThing.STD_2D);
        }
        bio.mm.changed = true;
        bio.state.saveState();
      }
    });
    group.add(standard2D);
    standard2D.setMnemonic('2');
    standard2D.setToolTipText("Distributes selected " +
      "measurements across all slices & timesteps");
    standard2D.setEnabled(false);
    p.add(standard2D);
    p.add(Box.createHorizontalStrut(5));

    // 3-D standard button
    standard3D = new JRadioButton("3-D standard");
    standard3D.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (!standard3D.isSelected()) return;
        if (ignoreStandard) {
          ignoreStandard = false;
          return;
        }
        MeasureThing[] things = bio.mm.pool2.getSelection();
        for (int i=0; i<things.length; i++) {
          doStandard(things[i], MeasureThing.STD_3D);
        }
        bio.mm.changed = true;
        bio.state.saveState();
      }
    });
    group.add(standard3D);
    standard3D.setMnemonic('3');
    standard3D.setToolTipText("Distributes selected " +
      "measurements across all slices & timesteps");
    standard3D.setEnabled(false);
    p.add(standard3D);
    controls.add(pad(p));
    controls.add(Box.createVerticalStrut(5));

    // color label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    colorLabel = new JLabel("Color: ");
    colorLabel.setForeground(Color.black);
    colorLabel.setDisplayedMnemonic('c');
    String colorToolTip = "Changes the color of the selected measurements";
    colorLabel.setToolTipText(colorToolTip);
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
            things[i].setColor(color);
          }
        }
        if (changed) {
          bio.mm.changed = true;
          bio.state.saveState();
          bio.mm.pool2.refresh(true);
          if (bio.mm.pool3 != null) bio.mm.pool3.refresh(true);
        }
      }
    });
    colorLabel.setLabelFor(colorList);
    colorList.setToolTipText(colorToolTip);
    colorList.setEnabled(false);
    p.add(colorList);
    controls.add(p);
    controls.add(Box.createVerticalStrut(5));

    // group label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    groupLabel = new JLabel("Group: ");
    groupLabel.setForeground(Color.black);
    groupLabel.setDisplayedMnemonic('g');
    String groupToolTip = "Changes the group of the selected measurements";
    groupLabel.setToolTipText(groupToolTip);
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
          bio.state.saveState();
        }
      }
    });
    groupLabel.setLabelFor(groupList);
    groupList.setToolTipText(groupToolTip);
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
          bio.state.saveState();
        }
      }
    });
    newGroup.setMnemonic('n');
    newGroup.setToolTipText("Creates a new measurement group");
    newGroup.setEnabled(false);
    p.add(newGroup);
    controls.add(p);

    // description label
    descriptionLabel = new JLabel("Group description:");
    descriptionLabel.setAlignmentX(SwingConstants.LEFT);
    descriptionLabel.setForeground(Color.black);
    descriptionLabel.setDisplayedMnemonic('d');
    String descriptionToolTip =
      "Edits the description of the current measurement group";
    descriptionLabel.setToolTipText(descriptionToolTip);
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
        bio.state.saveState();
      }
    });
    descriptionLabel.setLabelFor(descriptionBox);
    descriptionBox.setToolTipText(descriptionToolTip);
    descriptionBox.setEnabled(false);
    controls.add(new JScrollPane(descriptionBox));
  }


  // -- API METHODS --

  /** Initializes this tool panel. */
  public void init() { }

  /** Enables or disables this tool panel. */
  public void setEnabled(boolean enabled) {
    addLine.setEnabled(enabled);
    addMarker.setEnabled(enabled);
    merge.setEnabled(enabled);
    clearAll.setEnabled(enabled);
    export.setEnabled(enabled);
    snap.setEnabled(enabled);
  }

  /** Updates the selection data to match the current measurement list. */
  public void updateSelection() {
    boolean enabled = bio.mm.pool2.hasSelection();
    boolean b = enabled && stdEnabled;
    single.setEnabled(b);
    if (!enabled) standard2D.setEnabled(false);
    standard3D.setEnabled(b);
    updateRemove();
    colorLabel.setEnabled(enabled);
    colorList.setEnabled(enabled);
    groupLabel.setEnabled(enabled);
    groupList.setEnabled(enabled);
    newGroup.setEnabled(enabled);
    descriptionLabel.setEnabled(enabled);
    descriptionBox.setEnabled(enabled);
    if (enabled) {
      int stdType = bio.mm.pool2.getSelectionStandardType();
      if (stdType == MeasureThing.STD_SINGLE && !single.isSelected()) {
        ignoreStandard = true;
        single.setSelected(true);
      }
      else if (stdType == MeasureThing.STD_2D && !standard2D.isSelected()) {
        ignoreStandard = true;
        standard2D.setSelected(true);
      }
      else if (stdType == MeasureThing.STD_3D && !standard3D.isSelected()) {
        ignoreStandard = true;
        standard3D.setSelected(true);
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
          for (int i=0; i<pts.length; i++) {
            if (pts[i] != null) cell.addReference(pts[i].ref);
          }
          if (pts.length == 0) trigger = true;
        }
        cell.enableAction();
        if (trigger) cell.doAction();
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
  }


  // -- INTERNAL API METHODS --

  /** Updates GUI to match internal information. */
  void updateInfo(boolean microns, double mw, double mh, double sd) {
    // update micron information
    bio.toolAlign.updateInfo(microns, mw, mh, sd);

    // update groups
    groupList.removeAllItems();
    for (int i=0; i<bio.mm.groups.size(); i++) {
      groupList.addItem(bio.mm.groups.elementAt(i));
    }
    descriptionBox.setText("");
  }

  /** Sets the merge toggle button's status. */
  void setMerge(boolean merge) {
    if (merge != this.merge.isSelected()) this.merge.setSelected(merge);
  }

  /** Gets the merge toggle button's status. */
  boolean getMerge() { return merge.isSelected(); }

  /** Updates the micron-related GUI components. */
  void updateFileButtons() {
    boolean microns = bio.toolAlign.getUseMicrons();
    boolean b;
    if (microns) {
      double mw = bio.toolAlign.getMicronWidth();
      double mh = bio.toolAlign.getMicronHeight();
      double sd = bio.toolAlign.getSliceDistance();
      b = mw == mw && mh == mh && sd == sd;
    }
    else b = true;
    bio.toolAlign.updateAspect(!microns);
    updateMeasureInfo();
  }


  // -- HELPER METHODS --

  /** Updates the text in the measurement information box. */
  private void updateMeasureInfo() {
    String coord = "";
    String dist = "";
    if (bio.mm.pool2.hasSingleSelection()) {
      Object thing = bio.mm.pool2.getSelection()[0];
      boolean use = bio.toolAlign.getUseMicrons();
      double mw = bio.toolAlign.getMicronWidth();
      double mh = bio.toolAlign.getMicronHeight();
      double mx = mw / bio.sm.res_x;
      double my = mw / bio.sm.res_y;
      double sd = bio.toolAlign.getSliceDistance();
      boolean microns = use && mx == mx && my == my && sd == sd;
      if (!microns) mx = my = sd = 1;
      String unit = microns ? "µ" : "pix";
      if (thing instanceof MeasureLine) {
        MeasureLine line = (MeasureLine) thing;
        String vx = Convert.shortString(mx * line.ep1.x);
        String vy = Convert.shortString(my * line.ep1.y);
        String v2x = Convert.shortString(mx * line.ep2.x);
        String v2y = Convert.shortString(my * line.ep2.y);
        double[] p = {line.ep1.x, line.ep1.y, line.ep1.z};
        double[] q = {line.ep2.x, line.ep2.y, line.ep2.z};
        double[] m = {mx, my, sd};
        String d = Convert.shortString(BioUtil.getDistance(p, q, m));
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

    // update 2-D and 3-D standard choices
    if (bio.mm.pool2.hasSelection()) {
      boolean std2d = true;
      MeasureThing[] selection = bio.mm.pool2.getSelection();
      for (int i=0; i<selection.length; i++) {
        MeasureThing m = selection[i];
        if (m instanceof MeasureLine) {
          MeasureLine line = (MeasureLine) m;
          if (line.ep1.z != line.ep2.z || line.ep1.z != (int) line.ep1.z) {
            // cannot set multi-slice lines to 2-D standard
            std2d = false;
          }
          MeasurePoint pt1 = line.ep1;
        }
      }
      standard2D.setEnabled(std2d);
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

  /** Updates the remove button. */
  private void updateRemove() {
    MeasureThing[] selThings = bio.mm.pool2.getSelection();
    boolean noStd = selThings.length > 0;
    for (int i=0; i<selThings.length; i++) {
      if (selThings[i].stdId >= 0) {
        noStd = false;
        break;
      }
    }
    removeSelected.setEnabled(noStd);
  }

  /** Sets or unsets the given measurement as standard. */
  private void doStandard(MeasureThing thing, int std) {
    boolean isLine = thing instanceof MeasureLine;
    int index = bio.sm.getIndex();
    int slice = bio.sm.getSlice();
    if (std == MeasureThing.STD_SINGLE) {
      // unset standard
      undoStandard(thing);
    }
    else if (std == MeasureThing.STD_2D) {
      // set 2-D standard
      int id;
      if (thing.stdType == MeasureThing.STD_2D) {
        // line already standard; skip it
        return;
      }
      else if (thing.stdType == MeasureThing.STD_3D) {
        id = thing.stdId;
        undoStandard(thing);
      }
      else id = maxId++; // thing.stdType == MeasureThing.STD_SINGLE
      thing.setStandard(MeasureThing.STD_2D, id);
      int numSlices = bio.sm.getNumberOfSlices();
      boolean trans = bio.sm.align.getMode() == AlignmentPlane.APPLY_MODE;
      for (int j=0; j<bio.mm.lists.length; j++) {
        MeasureList list = bio.mm.lists[j];
        boolean update = j == index;
        for (int i=0; i<numSlices; i++) {
          if (j == index && i == slice) continue;
          if (isLine) {
            MeasureLine line;
            if (trans) {
              MeasureLine l = (MeasureLine) thing;
              double[] v1 = {l.ep1.x, l.ep1.y, i};
              double[] v2 = {l.ep2.x, l.ep2.y, i};
              v1 = bio.sm.align.transform(v1, index, j);
              v2 = bio.sm.align.transform(v2, index, j);
              line = new MeasureLine(l, v1, v2);
            }
            else line = new MeasureLine((MeasureLine) thing, i);
            list.addLine(line, update);
          }
          else {
            MeasurePoint point;
            if (trans) {
              MeasurePoint p = (MeasurePoint) thing;
              double[] v = {p.x, p.y, p.z};
              v = bio.sm.align.transform(v, index, j);
              point = new MeasurePoint(p, v);
            }
            else point = new MeasurePoint((MeasurePoint) thing, i);
            list.addMarker(point, update);
          }
        }
      }
    }
    else if (std == MeasureThing.STD_3D) {
      // set 3-D standard
      int id;
      if (thing.stdType == MeasureThing.STD_3D) {
        // line already standard; skip it
        return;
      }
      else if (thing.stdType == MeasureThing.STD_2D) {
        id = thing.stdId;
        undoStandard(thing);
      }
      else id = maxId++; // thing.stdType == MeasureThing.STD_SINGLE
      thing.setStandard(MeasureThing.STD_3D, id);
      boolean trans = bio.sm.align.getMode() == AlignmentPlane.APPLY_MODE;
      for (int j=0; j<bio.mm.lists.length; j++) {
        if (j == index) continue;
        MeasureList list = bio.mm.lists[j];
        if (isLine) {
          MeasureLine line;
          if (trans) {
            MeasureLine l = (MeasureLine) thing;
            double[] v1 = {l.ep1.x, l.ep1.y, l.ep1.z};
            double[] v2 = {l.ep2.x, l.ep2.y, l.ep2.z};
            v1 = bio.sm.align.transform(v1, index, j);
            v2 = bio.sm.align.transform(v2, index, j);
            line = new MeasureLine(l, v1, v2);
          }
          else line = new MeasureLine((MeasureLine) thing);
          list.addLine(line, false);
        }
        else {
          MeasurePoint point;
          if (trans) {
            MeasurePoint p = (MeasurePoint) thing;
            double[] v = {p.x, p.y, p.z};
            v = bio.sm.align.transform(v, index, j);
            point = new MeasurePoint(p, v);
          }
          else point = new MeasurePoint((MeasurePoint) thing);
          list.addMarker(point, false);
        }
      }
    }
    updateRemove();
  }

  /** Unsets the given measurement as standard. */
  private void undoStandard(MeasureThing thing) {
    if (thing.stdType == MeasureThing.STD_SINGLE) {
      // line not standard; skip it
      return;
    }
    int index = bio.sm.getIndex();
    int stdId = thing.stdId;
    thing.setStandard(MeasureThing.STD_SINGLE, -1);
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
