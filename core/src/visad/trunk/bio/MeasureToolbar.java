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
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
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

  /** First free id number for standard measurements. */
  static int maxId = 0;


  /** New group dialog box. */
  private GroupDialog groupBox = new GroupDialog();

  /** Currently selected measurement object. */
  private MeasureThing thing;

  /** Measurement frame. */
  private MeasureFrame frame;

  /** File series widget. */
  private FileSeriesWidget horiz;

  /** Image stack widget. */
  private ImageStackWidget vert;

  /** Computation cell for linking selection with measurement object. */
  private CellImpl cell;

  /** Flag marking whether to ignore next set standard checkbox toggle. */
  boolean ignoreNextStandard = false;

  /** Flag marking whether to ignore group list changes. */
  private boolean ignoreGroup = false;


  // -- GLOBAL FUNCTIONS --

  /** Label for microns per pixel. */
  private JLabel mPixLabel;

  /** Text field for specifying microns per pixel. */
  private JTextField micronsPerPixel;

  /** Label for distance between slices. */
  private JLabel sliceDistLabel;

  /** Text field for specifying distance (in microns) between slices. */
  private JTextField sliceDistance;

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


  /** Constructs a custom measurement toolbar. */
  public MeasureToolbar(MeasureFrame f,
    FileSeriesWidget h, ImageStackWidget v)
  {
    frame = f;
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

    // microns per pixel label
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    mPixLabel = new JLabel("Microns per pixel: ");
    p.add(mPixLabel);

    // microns per pixel text box
    micronsPerPixel = new JTextField();
    final MeasureToolbar toolbar = this;
    micronsPerPixel.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { toolbar.updateMenuItems(); }
      public void insertUpdate(DocumentEvent e) { toolbar.updateMenuItems(); }
      public void removeUpdate(DocumentEvent e) { toolbar.updateMenuItems(); }
    });
    p.add(micronsPerPixel);
    controls.add(pad(p));

    // slice distance label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    sliceDistLabel = new JLabel("Microns between slices: ");
    p.add(sliceDistLabel);

    // distance between slices text box
    sliceDistance = new JTextField();
    sliceDistance.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { toolbar.updateMenuItems(); }
      public void insertUpdate(DocumentEvent e) { toolbar.updateMenuItems(); }
      public void removeUpdate(DocumentEvent e) { toolbar.updateMenuItems(); }
    });
    p.add(sliceDistance);
    controls.add(pad(p));
    controls.add(Box.createVerticalStrut(10));

    // add line button
    p = new JPanel();
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
    cell = new CellImpl() {
      public void doAction() {
        synchronized (cell) {
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
        int index = horiz.getValue() - 1;
        int slice = vert.getValue() - 1;
        if (std) {
          // set standard
          m.setStdId(maxId++);
          MeasureList[][] lists = horiz.getMatrix().getMeasureLists();
          for (int j=0; j<lists.length; j++) {
            for (int i=0; i<lists[j].length; i++) {
              if (j == index && i == slice) continue;
              lists[j][i].addMeasurement((Measurement) m.clone(), false);
            }
          }
        }
        else {
          // unset standard
          int ans = JOptionPane.showConfirmDialog(toolbar, "Are you sure?",
            "Unset standard", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
          if (ans != JOptionPane.YES_OPTION) {
            ignoreNextStandard = true;
            setStandard.setSelected(true);
            return;
          }
          int stdId = m.getStdId();
          m.setStdId(-1);
          MeasureList[][] lists = horiz.getMatrix().getMeasureLists();
          for (int j=0; j<lists.length; j++) {
            for (int i=0; i<lists[j].length; i++) {
              if (j == index && i == slice) continue;
              Measurement[] mlist = lists[j][i].getMeasurements();
              for (int k=0; k<mlist.length; k++) {
                if (mlist[k].getStdId() == stdId) {
                  lists[j][i].removeMeasurement(mlist[k], false);
                  break;
                }
              }
            }
          }
        }
      }
    });
    setStandard.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
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
    colorLabel = new JLabel("Color: ");
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
    groupLabel = new JLabel("Group: ");
    groupLabel.setEnabled(false);
    p.add(groupLabel);

    // group list
    groupList = new JComboBox();
    groupList.addItem(new LineGroup("NONE"));
    groupList.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (ignoreGroup) return;
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

    updateMenuItems();
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
      ignoreNextStandard = true;
      setStandard.setSelected(m.getStdId() >= 0);
      colorList.setSelectedItem(m.getColor());
      groupList.setSelectedItem(m.getGroup());
    }
    synchronized (cell) {
      try {
        cell.disableAction();
        cell.removeAllReferences();
        if (enabled) {
          DataReference[] refs = thing.getReferences();
          for (int i=0; i<refs.length; i++) cell.addReference(refs[i]);
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
    try {
      d = Double.parseDouble(micronsPerPixel.getText());
    }
    catch (NumberFormatException exc) { d = Double.NaN; }
    return d;
  }

  /** Gets the micron distance between slices entered by the user. */
  public double getSliceDistance() {
    double d;
    try {
      d = Double.parseDouble(sliceDistance.getText());
    }
    catch (NumberFormatException exc) { d = Double.NaN; }
    return d;
  }

  /** Updates the group list to match the static LineGroup list. */
  void updateGroupList() {
    ignoreGroup = true;
    groupList.removeAllItems();
    int size = LineGroup.groups.size();
    for (int i=0; i<size; i++) {
      LineGroup group = (LineGroup) LineGroup.groups.elementAt(i);
      groupList.addItem(group);
    }
    ignoreGroup = false;
  }

  /** Updates the micron-related menu items. */
  private void updateMenuItems() {
    boolean b = false;
    try {
      double d1 = Double.parseDouble(micronsPerPixel.getText());
      double d2 = Double.parseDouble(sliceDistance.getText());
      if (d1 == d1 && d1 > 0 && d2 == d2 && d2 > 0) b = true;
    }
    catch (NumberFormatException exc) { }
    frame.getMenuItem("File", "Restore lines (microns)...").setEnabled(b);
    frame.getMenuItem("File", "Save lines (microns)...").setEnabled(b);
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
