//
// ExportDialog.java
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
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import visad.VisADException;
import visad.browser.Divider;
import visad.data.*;
import visad.util.*;

/** ExportDialog provides a set of options for exporting a 4-D data series. */
public class ExportDialog extends JPanel
  implements ActionListener, DocumentListener
{

  // -- CONSTANTS --

  /** Return value if approve (ok) is chosen. */
  public static final int APPROVE_OPTION = 1;

  /** Return value if cancel is chosen. */
  public static final int CANCEL_OPTION = 2;

  /** Flag indicating whether QuickTime support is available. */
  private static final boolean CAN_DO_QT = Util.canDoQuickTime();


  // -- FIELDS --

  /** BioVisAD frame. */
  private BioVisAD bio;

  /** Currently visible dialog. */
  private JDialog dialog;

  /** Return value of dialog. */
  private int rval;


  // -- GUI COMPONENTS --

  private JCheckBox doColors, doAlign, timeOnly, sliceOnly;
  private JRadioButton picFormat, tiffFormat, qtFormat;
  private SeriesChooser chooser;
  private DoubleTextCheckBox altRes, exportTimesteps, exportSlices;
  private JButton ok;



  // -- CONSTRUCTOR --

  /** Creates a file series chooser dialog. */
  public ExportDialog(BioVisAD biovis) {
    bio = biovis;

    // main components
    chooser = new SeriesChooser();
    chooser.end.setEnabled(false);
    chooser.type.setEnabled(false);
    chooser.start.getDocument().addDocumentListener(this);
    chooser.start.addActionListener(this);
    chooser.treatTimestep.addActionListener(this);
    chooser.treatSlice.addActionListener(this);
    ok = new JButton("Ok");
    ok.setMnemonic('o');
    ok.setActionCommand("ok");
    ok.addActionListener(this);
    JButton cancel = new JButton("Cancel");
    cancel.setMnemonic('c');
    cancel.setActionCommand("cancel");
    cancel.addActionListener(this);

    // options
    doColors = new JCheckBox("Save color adjustments", true);
    doAlign = new JCheckBox("Save alignment", true);
    timeOnly = new JCheckBox("Save current timestep only");
    sliceOnly = new JCheckBox("Save current slice only");
    timeOnly.addActionListener(this);
    sliceOnly.addActionListener(this);

    // output format
    JLabel outputLabel = new JLabel("Output format:");
    outputLabel.setForeground(Color.black);
    picFormat = new JRadioButton("Bio-Rad PIC", true);
    tiffFormat = new JRadioButton("Multi-page TIFF");
    qtFormat = new JRadioButton("QuickTime movie");
    qtFormat.setEnabled(CAN_DO_QT);
    ButtonGroup group = new ButtonGroup();
    group.add(picFormat);
    group.add(tiffFormat);
    group.add(qtFormat);
    picFormat.setActionCommand("picFormat");
    tiffFormat.setActionCommand("tiffFormat");
    qtFormat.setActionCommand("qtFormat");
    picFormat.addActionListener(this);
    tiffFormat.addActionListener(this);
    qtFormat.addActionListener(this);

    // additional options
    altRes = new DoubleTextCheckBox(
      "Use alternate resolution", "by", "", "", false);
    exportTimesteps = new DoubleTextCheckBox(
      "Only export timesteps", "through", "", "", false);
    exportSlices = new DoubleTextCheckBox(
      "Only export slices", "through", "", "", false);
    exportTimesteps.addActionListener(this);
    exportSlices.addActionListener(this);

    // lay out output format
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel top = new JPanel();
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    JPanel topLeft = new JPanel();
    topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
    topLeft.add(outputLabel);
    topLeft.add(picFormat);
    topLeft.add(tiffFormat);
    topLeft.add(qtFormat);
    top.add(topLeft);

    // lay out options
    JPanel topRight = new JPanel();
    topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));
    topRight.add(doColors);
    topRight.add(doAlign);
    topRight.add(timeOnly);
    topRight.add(sliceOnly);
    top.add(topRight);
    add(top);

    // divider
    add(Box.createVerticalStrut(5));
    add(new Divider());
    add(Box.createVerticalStrut(5));

    // lay out additional options
    JPanel mid = new JPanel();
    mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
    mid.add(altRes);
    mid.add(exportSlices);
    mid.add(exportTimesteps);
    add(mid);

    // divider
    add(Box.createVerticalStrut(5));
    add(new Divider());
    add(Box.createVerticalStrut(5));

    // lay out series chooser
    add(chooser);

    // lay out buttons
    JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(ok);
    bottom.add(cancel);
    add(ToolPanel.pad(bottom));

  }


  // -- API METHODS --

  /** Displays a dialog using this series chooser. */
  public int showDialog() {
    dialog = new JDialog(bio, "Save file series", true);
    dialog.getRootPane().setDefaultButton(ok);
    chooser.clearFields();

    int maxSlice = bio.sm.getNumberOfSlices();
    int maxIndex = bio.sm.getNumberOfIndices();
    if (maxSlice > 1 || maxIndex > 1) {
      chooser.start.setText("1");
      chooser.end.setText("" + maxIndex);
    }

    dialog.setContentPane(this);
    dialog.pack();
    Util.centerWindow(dialog);
    dialog.setVisible(true);
    return rval;
  }

  /** Exports the current dataset according to the dialog settings. */
  public void export() {
    int first = -1, last = -1;
    try { first = Integer.parseInt(chooser.start.getText()); }
    catch (NumberFormatException exc) { }
    try { last = Integer.parseInt(chooser.end.getText()); }
    catch (NumberFormatException exc) { }
    if (first < 0 && last >= 0) chooser.start.setText("1");

    // default export values
    int minSlice = 1;
    int maxSlice = bio.sm.getNumberOfSlices();
    int minIndex = 1;
    int maxIndex = bio.sm.getNumberOfIndices();
    int resX = bio.sm.res_x;
    int resY = bio.sm.res_y;

    // extract export parameters from GUI
    boolean colors = doColors.isSelected();
    boolean align = doAlign.isSelected();
    if (timeOnly.isSelected()) minIndex = maxIndex = bio.sm.getIndex() + 1;
    if (sliceOnly.isSelected()) minSlice = maxSlice = bio.sm.getSlice() + 1;
    File[] series = chooser.getSeries();
    boolean filesAsSlices = chooser.getFilesAsSlices();
    if (altRes.isSelected()) {
      try {
        int x = Integer.parseInt(altRes.getFirstValue());
        int y = Integer.parseInt(altRes.getSecondValue());
        resX = x;
        resY = y;
      }
      catch (NumberFormatException exc) { }
    }
    if (exportTimesteps.isSelected()) {
      try {
        int min = Integer.parseInt(exportTimesteps.getFirstValue());
        int max = Integer.parseInt(exportTimesteps.getSecondValue());
        minIndex = min;
        maxIndex = max;
      }
      catch (NumberFormatException exc) { }
    }
    if (exportSlices.isSelected()) {
      try {
        int min = Integer.parseInt(exportSlices.getFirstValue());
        int max = Integer.parseInt(exportSlices.getSecondValue());
        minSlice = min;
        maxSlice = max;
      }
      catch (NumberFormatException exc) { }
    }

    // confirm export parameters
    int numSlices = maxSlice - minSlice + 1;
    int numIndices = maxIndex - minIndex + 1;
    String s = "Export slice" + (numSlices == 1 ? " #" + minSlice :
      "s " + minSlice + " through " + maxSlice) + " at timestep" +
      (numIndices == 1 ? " #" + minIndex : "s " + minIndex + " through " +
      maxIndex) + " to file" + (series.length == 1 ? " " + series[0] :
      "s " + series[0] + " through " + series[series.length - 1]) + "?";
    int ans = JOptionPane.showConfirmDialog(bio, s, "BioVisAD",
      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (ans != JOptionPane.YES_OPTION) return;

    // export data series
    // CTR - TODO - export data series
  }


  // -- INTERNAL API METHODS --

  /** Handles button press events. */
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command.equals("ok")) {
      rval = APPROVE_OPTION;
      dialog.setVisible(false);
    }
    else if (command.equals("cancel")) {
      rval = CANCEL_OPTION;
      dialog.setVisible(false);
    }
    else if (command.equals("picFormat")) {
      chooser.type.setSelectedIndex(0);
    }
    else if (command.equals("tiffFormat")) {
      chooser.type.setSelectedIndex(1);
    }
    else if (command.equals("qtFormat")) {
      chooser.type.setSelectedIndex(2);
    }
    else { computeEnd(); }
  }

  public void changedUpdate(DocumentEvent e) { computeEnd(); }
  public void insertUpdate(DocumentEvent e) { computeEnd(); }
  public void removeUpdate(DocumentEvent e) { computeEnd(); }


  // -- HELPER METHODS --

  private void computeEnd() {
    int startVal = 1;
    try { startVal = Integer.parseInt(chooser.start.getText()); }
    catch (NumberFormatException exc) { }

    int minSlice = 1;
    int maxSlice = bio.sm.getNumberOfSlices();
    int minIndex = 1;
    int maxIndex = bio.sm.getNumberOfIndices();

    if (timeOnly.isSelected()) minIndex = maxIndex = bio.sm.getIndex() + 1;
    if (sliceOnly.isSelected()) minSlice = maxSlice = bio.sm.getSlice() + 1;
    boolean filesAsSlices = chooser.getFilesAsSlices();
    if (exportTimesteps.isSelected()) {
      try {
        int min = Integer.parseInt(exportTimesteps.getFirstValue());
        int max = Integer.parseInt(exportTimesteps.getSecondValue());
        minIndex = min;
        maxIndex = max;
      }
      catch (NumberFormatException exc) { }
    }
    if (exportSlices.isSelected()) {
      try {
        int min = Integer.parseInt(exportSlices.getFirstValue());
        int max = Integer.parseInt(exportSlices.getSecondValue());
        minSlice = min;
        maxSlice = max;
      }
      catch (NumberFormatException exc) { }
    }

    if (filesAsSlices && minSlice == maxSlice ||
      !filesAsSlices && minIndex == maxIndex)
    {
      chooser.end.setText("" + startVal);
    }
    else {
      int q = filesAsSlices ?
        maxSlice - minSlice : maxIndex - minIndex;
      chooser.end.setText("" + (startVal + q));
    }
  }

  /*
  public void exportSliceTIFF() {
    exportData(new TiffForm(), new String[] {"tif", "tiff"},
      "TIFF stacks", false);
  };

  public void exportSlicePIC() {
    exportData(new BioRadForm(), new String[] {"pic"},
      "Bio-Rad PIC files", false);
  }

  public void exportSliceQT() {
    exportData(new QTForm(), new String[] {"mov", "qt"},
      "QuickTime movies", false);
  }

  public void exportTimeTIFF() {
    exportData(new TiffForm(), new String[] {"tif", "tiff"},
      "TIFF stacks", true);
  };

  public void exportTimePIC() {
    exportData(new BioRadForm(), new String[] {"pic"},
      "Bio-Rad PIC files", true);
  }

  public void exportTimeQT() {
    exportData(new QTForm(), new String[] {"mov", "qt"},
      "QuickTime movies", true);
  }
  */

  /**
   * Exports a slice animation sequence using the given file form.
   * If stack is true, the current image stack is exported.
   * If stack is false, the current slice animation sequence is exported.
   */
  private void exportData(BioVisAD biovis, Form saver, String[] exts,
    String desc, boolean stack)
  {
    final BioVisAD bio = biovis;
    final Form fsaver = saver;
    final String[] fexts = exts;
    final String fdesc = desc;
    final boolean fstack = stack;
    Util.invoke(false, new Runnable() {
      public void run() {
        JFileChooser fileBox = new JFileChooser();
        fileBox.setFileFilter(new ExtensionFileFilter(fexts, fdesc));
        int rval = fileBox.showSaveDialog(bio);
        if (rval == JFileChooser.APPROVE_OPTION) {
          bio.setWaitCursor(true);
          String file = fileBox.getSelectedFile().getPath();
          try {
            if (fstack) bio.sm.exportImageStack(fsaver, file);
            else bio.sm.exportSliceAnimation(fsaver, file);
          }
          catch (VisADException exc) { exc.printStackTrace(); }
          bio.setWaitCursor(false);
        }
      }
    });
  }

}
