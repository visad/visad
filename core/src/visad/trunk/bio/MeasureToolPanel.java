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
import java.io.*;
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.browser.*;
import visad.util.Util;

/**
 * MeasureToolPanel is the tool panel for
 * managing measurements between data points.
 */
public class MeasureToolPanel extends ToolPanel implements SwingConstants {

  // -- CONSTANTS --

  /** List of colors for drop-down color box. */
  private static final Color[] COLORS = {
    Color.white, Color.lightGray, Color.gray, Color.darkGray, Color.black,
    Color.red, Color.orange, Color.yellow, Color.green,
    Color.cyan, Color.blue, Color.magenta, Color.pink
  };

  /** Placeholder for measurement information label. */
  private static final String INFO_LABEL =
    "   (000.000, 000.000)-(000.000, 000.000): distance=000.000";


  // -- GLOBAL VARIABLES --

  /** First free id number for standard measurements. */
  static int maxId = 0;


  // -- FIELDS --

  /** File chooser for loading and saving data. */
  private JFileChooser fileBox = Util.getVisADFileChooser();

  /** New group dialog box. */
  private GroupDialog groupBox = new GroupDialog();

  /** Currently selected measurement object. */
  private MeasureThing thing;

  /** Computation cell for linking selection with measurement object. */
  private CellImpl cell;

  /** Flag marking whether to ignore next set standard checkbox toggle. */
  private boolean ignoreNextStandard = false;

  /** Flag marking whether to ignore group list changes. */
  private boolean ignoreGroup = false;

  /** Flag marking whether set standard checkbox can be enabled. */
  private boolean stdEnabled = true;


  // -- FILE IO FUNCTIONS --
  
  /** Button for saving measurements to a file. */
  private JButton saveLines;

  /** Button for restoring measurements from a file. */
  private JButton restoreLines;

  /**
   * Check box for indicating file should be saved or restored
   * using a micron-pixel conversion.
   */
  private JCheckBox useMicrons;

  /** Label for microns per pixel. */
  private JLabel mPixLabel;

  /** Text field for specifying microns per pixel. */
  private JTextField micronsPerPixel;

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

  /** Label for displaying measurement information. */
  private JLabel measureInfo;

  /** Button for distributing measurement object through all focal planes. */
  private JCheckBox setStandard;

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


  // -- CONSTRUCTOR --

  /** Constructs a tool panel for performing measurement operations. */
  public MeasureToolPanel(BioVisAD biovis) {
    super(biovis);

    // save measurements button
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    saveLines = new JButton("Save measurements");
    saveLines.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveMeasurements(useMicrons.isSelected());
      }
    });
    p.add(saveLines);
    p.add(Box.createHorizontalStrut(5));

    // restore measurements button
    restoreLines = new JButton("Restore measurements");
    restoreLines.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        restoreMeasurements(useMicrons.isSelected());
      }
    });
    p.add(restoreLines);
    controls.add(pad(p));

    // microns vs pixels checkbox
    useMicrons = new JCheckBox("Use microns instead of pixels", false);
    final MeasureToolPanel tool = this;
    useMicrons.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean microns = useMicrons.isSelected();
        mPixLabel.setEnabled(microns);
        micronsPerPixel.setEnabled(microns);
        sliceDistLabel.setEnabled(microns);
        sliceDistance.setEnabled(microns);
        tool.updateFileButtons();
      }
    });
    controls.add(pad(useMicrons));

    // microns per pixel label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    mPixLabel = new JLabel("Microns per pixel: ");
    mPixLabel.setEnabled(false);
    p.add(mPixLabel);

    // microns per pixel text box
    micronsPerPixel = new JTextField();
    micronsPerPixel.setEnabled(false);
    micronsPerPixel.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { tool.updateFileButtons(); }
      public void insertUpdate(DocumentEvent e) { tool.updateFileButtons(); }
      public void removeUpdate(DocumentEvent e) { tool.updateFileButtons(); }
    });
    p.add(micronsPerPixel);
    controls.add(pad(p));

    // slice distance label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    sliceDistLabel = new JLabel("Microns between slices: ");
    sliceDistLabel.setEnabled(false);
    p.add(sliceDistLabel);

    // distance between slices text box
    sliceDistance = new JTextField();
    sliceDistance.setEnabled(false);
    sliceDistance.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { tool.updateFileButtons(); }
      public void insertUpdate(DocumentEvent e) { tool.updateFileButtons(); }
      public void removeUpdate(DocumentEvent e) { tool.updateFileButtons(); }
    });
    p.add(sliceDistance);
    controls.add(pad(p));

    // divider between file IO functions and other functions
    controls.add(Box.createVerticalStrut(10));
    controls.add(new Divider());
    controls.add(Box.createVerticalStrut(10));

    // add line button
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    addLine = new JButton("New line");
    addLine.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.getList().addMeasurement();
      }
    });
    addLine.setEnabled(false);
    p.add(addLine);
    p.add(Box.createHorizontalStrut(5));

    // add marker button
    addMarker = new JButton("New marker");
    addMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.getList().addMeasurement(1);
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
          "Are you sure?", "Clear all measurements",
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ans != JOptionPane.YES_OPTION) return;
        bio.clear();
      }
    });
    clearAll.setEnabled(false);
    p.add(clearAll);
    controls.add(pad(p));
    controls.add(Box.createVerticalStrut(5));

    // measurement information label
    measureInfo = new JLabel(" ") {
      public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int width = fm.stringWidth("    " + INFO_LABEL);
        Dimension d = super.getPreferredSize();
        return new Dimension(width, d.height);
      }
    };
    cell = new CellImpl() {
      public void doAction() {
        synchronized (cell) {
          String text = " ";
          if (thing != null) {
            Measurement m = thing.getMeasurement();
            double[][] vals = m.doubleValues();
            if (thing.getLength() == 2) {
              String v1x = Convert.shortString(vals[0][0]);
              String v1y = Convert.shortString(vals[1][0]);
              String v2x = Convert.shortString(vals[0][1]);
              String v2y = Convert.shortString(vals[1][1]);
              String dist = Convert.shortString(m.getDistance());
              text = "(" + v1x + ", " + v1y + ")-" +
                "(" + v2x + ", " + v2y + "): distance=" + dist;
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
      }
    };
    controls.add(pad(measureInfo));
    controls.add(Box.createVerticalStrut(10));

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
        Measurement m = thing.getMeasurement();
        int index = bio.getIndex();
        int slice = bio.getSlice();
        if (std) {
          // set standard
          m.stdId = maxId++;
          int numSlices = bio.getNumberOfSlices();
          for (int j=0; j<bio.lists.length; j++) {
            for (int i=0; i<numSlices; i++) {
              if (j == index && i == slice) continue;
              bio.lists[j].addMeasurement(new Measurement(m, i), j == index);
            }
          }
        }
        else {
          // unset standard
          int ans = JOptionPane.showConfirmDialog(tool,
            "Are you sure?", "Unset standard", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
          if (ans != JOptionPane.YES_OPTION) {
            ignoreNextStandard = true;
            setStandard.setSelected(true);
            return;
          }
          int stdId = m.stdId;
          m.stdId = -1;
          for (int j=0; j<bio.lists.length; j++) {
            Measurement[] mlist = bio.lists[j].getMeasurements();
            for (int k=0; k<mlist.length; k++) {
              if (mlist[k].stdId == stdId) {
                bio.lists[j].removeMeasurement(mlist[k]);
              }
            }
          }
        }
      }
    });
    setStandard.setEnabled(false);
    p.add(setStandard);
    p.add(Box.createHorizontalStrut(5));

    // remove thing button
    removeThing = new JButton("Remove");
    removeThing.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bio.getList().removeMeasurement(thing.getMeasurement());
      }
    });
    removeThing.setEnabled(false);
    p.add(removeThing);
    controls.add(pad(p));
    controls.add(Box.createVerticalStrut(5));

    // color label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    colorLabel = new JLabel("Color: ");
    colorLabel.setEnabled(false);
    p.add(colorLabel);

    // color list
    colorList = new JComboBox(COLORS);
    colorList.setRenderer(new ColorRenderer());
    colorList.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        int index = colorList.getSelectedIndex();
        thing.setColor(COLORS[index]);
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
    groupLabel.setEnabled(false);
    p.add(groupLabel);

    // group list
    groupList = new JComboBox();
    groupList.addItem(new MeasureGroup(bio, "NONE"));
    groupList.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (ignoreGroup) return;
        MeasureGroup group = (MeasureGroup) groupList.getSelectedItem();
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
          MeasureGroup group = new MeasureGroup(bio, name);
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
        MeasureGroup group = (MeasureGroup) groupList.getSelectedItem();
        group.setDescription(descriptionBox.getText());
      }
    });
    descriptionBox.setEnabled(false);
    controls.add(new JScrollPane(descriptionBox));
  }

  /** Enables or disables this tool panel. */
  public void setEnabled(boolean enabled) {
    // CTR: TODO: file IO stuff should be affected by this
    addLine.setEnabled(enabled);
    addMarker.setEnabled(enabled);
    clearAll.setEnabled(enabled);
  }

  /** Enables or disables the "set standard" checkbox. */
  public void setStandardEnabled(boolean enabled) {
    stdEnabled = enabled;
    setStandard.setEnabled(thing != null && enabled);
  }

  /** Selects the given measurement object. */
  public void select(MeasureThing thing) {
    this.thing = thing;
    boolean enabled = thing != null;
    boolean line = enabled && thing.getLength() == 2;
    setStandard.setEnabled(enabled && stdEnabled);
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
      ignoreNextStandard = true;
      setStandard.setSelected(m.stdId >= 0);
      colorList.setSelectedItem(m.getColor());
      groupList.setSelectedItem(m.getGroup());
    }
    synchronized (cell) {
      try {
        cell.disableAction();
        cell.removeAllReferences();
        if (enabled) {
          PoolPoint[] pts = thing.getPoints();
          for (int i=0; i<pts.length; i++) cell.addReference(pts[i].ref);
        }
        cell.enableAction();
        if (!enabled) cell.doAction();
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
  }

  /** Gets the micron distance between pixels entered by the user. */
  public double getMicronsPerPixel() {
    double d;
    try { d = Double.parseDouble(micronsPerPixel.getText()); }
    catch (NumberFormatException exc) { d = Double.NaN; }
    return d;
  }

  /** Gets the micron distance between slices entered by the user. */
  public double getSliceDistance() {
    double d;
    try { d = Double.parseDouble(sliceDistance.getText()); }
    catch (NumberFormatException exc) { d = Double.NaN; }
    return d;
  }

  /** Restores a saved set of measurements. */
  public void restoreMeasurements(boolean microns) {
    final MeasureToolPanel measureTools = this;
    final boolean fmicrons = microns;
    Util.invoke(false, new Runnable() {
      public void run() {
        bio.setWaitCursor(true);
        // get file name from file dialog
        fileBox.setDialogType(JFileChooser.OPEN_DIALOG);
        if (fileBox.showOpenDialog(bio) != JFileChooser.APPROVE_OPTION) {
          bio.setWaitCursor(false);
          return;
        }
      
        // make sure file exists
        File f = fileBox.getSelectedFile();
        if (!f.exists()) {
          JOptionPane.showMessageDialog(bio,
            f.getName() + " does not exist", "Cannot load file",
            JOptionPane.ERROR_MESSAGE);
          bio.setWaitCursor(false);
          return;
        }
      
        // restore measurements
        try {
          MeasureDataFile mdf = new MeasureDataFile(f);
          if (fmicrons) {
            double mpp = measureTools.getMicronsPerPixel();
            double sd = measureTools.getSliceDistance();
            bio.lists = mdf.read(mpp, sd);
          }
          else bio.lists = mdf.read();
        }
        catch (IOException exc) { exc.printStackTrace(); }
        catch (VisADException exc) { exc.printStackTrace(); }
        bio.setWaitCursor(false);
      }
    });
  }

  /** Saves a set of measurements. */
  public void saveMeasurements(boolean microns) {
    final MeasureToolPanel measureTools = this;
    final boolean fmicrons = microns;
    Util.invoke(false, new Runnable() {
      public void run() {
        bio.setWaitCursor(true);
        // get file name from file dialog
        fileBox.setDialogType(JFileChooser.SAVE_DIALOG);
        if (fileBox.showSaveDialog(bio) != JFileChooser.APPROVE_OPTION) {
          bio.setWaitCursor(false);
          return;
        }
    
        // save measurements
        File f = fileBox.getSelectedFile();
        try {
          MeasureDataFile mdf = new MeasureDataFile(f);
          if (fmicrons) {
            double mpp = measureTools.getMicronsPerPixel();
            double sd = measureTools.getSliceDistance();
            mdf.write(bio.lists, mpp, sd);
          }
          else mdf.write(bio.lists);
        }
        catch (IOException exc) { exc.printStackTrace(); }
        bio.setWaitCursor(false);
      }
    });
  }

  /** Updates the group list to match the master MeasureGroup list. */
  void updateGroupList() {
    ignoreGroup = true;
    groupList.removeAllItems();
    int size = bio.groups.size();
    for (int i=0; i<size; i++) {
      MeasureGroup group = (MeasureGroup) bio.groups.elementAt(i);
      groupList.addItem(group);
    }
    ignoreGroup = false;
  }

  /** Updates the micron-related menu items. */
  private void updateFileButtons() {
    boolean b;
    if (useMicrons.isSelected()) {
      b = false;
      try {
        double d1 = Double.parseDouble(micronsPerPixel.getText());
        double d2 = Double.parseDouble(sliceDistance.getText());
        if (d1 == d1 && d1 > 0 && d2 == d2 && d2 > 0) b = true;
      }
      catch (NumberFormatException exc) { }
    }
    else b = true;
    saveLines.setEnabled(b);
    restoreLines.setEnabled(b);
  }

  /** Updates the text in the measurement information box. */
  private void updateMeasureInfo() {
    String text = " ";
    if (thing != null) {
      Measurement m = thing.getMeasurement();
      double[][] vals = m.doubleValues();
      if (thing.getLength() == 2) {
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

}
