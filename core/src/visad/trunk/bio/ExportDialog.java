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
import java.rmi.RemoteException;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.browser.Divider;
import visad.data.*;
import visad.data.biorad.BioRadForm;
import visad.data.qt.QTForm;
import visad.data.tiff.TiffForm;
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


  // -- FIELDS --

  /** VisBio frame. */
  private VisBio bio;

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
  public ExportDialog(VisBio biovis) {
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
    doColors = new JCheckBox("Save color adjustments");
    doColors.setEnabled(false);
    doAlign = new JCheckBox("Save alignment");
    doAlign.setEnabled(false);
    timeOnly = new JCheckBox("Save current timestep only");
    timeOnly.setActionCommand("timeOnly");
    timeOnly.addActionListener(this);
    sliceOnly = new JCheckBox("Save current slice only");
    sliceOnly.setActionCommand("sliceOnly");
    sliceOnly.addActionListener(this);

    // output format
    JLabel outputLabel = new JLabel("Output format:");
    outputLabel.setForeground(Color.black);
    picFormat = new JRadioButton("Bio-Rad PIC", true);
    tiffFormat = new JRadioButton("Multi-page TIFF");
    qtFormat = new JRadioButton("QuickTime movie");
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
    exportTimesteps.setActionCommand("exportTimesteps");
    exportTimesteps.addActionListener(this);
    exportSlices = new DoubleTextCheckBox(
      "Only export slices", "through", "", "", false);
    exportSlices.setActionCommand("exportSlices");
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
    int ndx = picFormat.isSelected() ? 0 : tiffFormat.isSelected() ? 1 : 2;
    chooser.type.setSelectedIndex(ndx);

    int maxSlice = bio.sm.getNumberOfSlices();
    int maxIndex = bio.sm.getNumberOfIndices();
    if (maxSlice > 1 || maxIndex > 1) {
      chooser.start.setText("1");
      chooser.end.setText("" + maxIndex);
    }

    qtFormat.setEnabled(bio.options.isQTEnabled());
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
    int min_slice = 1;
    int max_slice = bio.sm.getNumberOfSlices();
    int min_index = 1;
    int max_index = bio.sm.getNumberOfIndices();
    final int rx = bio.sm.res_x;
    final int ry = bio.sm.res_y;

    // extract export parameters from GUI
    final boolean colors = doColors.isSelected();
    final boolean align = doAlign.isSelected();
    final boolean singleTime = timeOnly.isSelected();
    final boolean singleSlice = sliceOnly.isSelected();
    if (singleTime) min_index = max_index = bio.sm.getIndex() + 1;
    if (singleSlice) min_slice = max_slice = bio.sm.getSlice() + 1;
    final File[] series = chooser.getSeries();
    final boolean filesAsSlices = chooser.getFilesAsSlices();
    boolean doAltRes = altRes.isSelected();
    int altX = rx;
    int altY = ry;
    try {
      altX = Integer.parseInt(altRes.getFirstValue());
      altY = Integer.parseInt(altRes.getSecondValue());
    }
    catch (NumberFormatException exc) { }
    final int resX = doAltRes ? altX : rx;
    final int resY = doAltRes ? altY : ry;
    if (exportTimesteps.isSelected()) {
      try {
        int min = Integer.parseInt(exportTimesteps.getFirstValue());
        int max = Integer.parseInt(exportTimesteps.getSecondValue());
        min_index = min;
        max_index = max;
      }
      catch (NumberFormatException exc) { }
    }
    if (exportSlices.isSelected()) {
      try {
        int min = Integer.parseInt(exportSlices.getFirstValue());
        int max = Integer.parseInt(exportSlices.getSecondValue());
        min_slice = min;
        max_slice = max;
      }
      catch (NumberFormatException exc) { }
    }

    // extract export format
    Form form;
    if (picFormat.isSelected()) form = new BioRadForm();
    else if (tiffFormat.isSelected()) form = new TiffForm();
    else if (qtFormat.isSelected()) form = new QTForm();
    else {
      JOptionPane.showMessageDialog(bio, "Invalid file format",
        "Export error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // confirm export parameters
    final int minSlice = min_slice - 1;
    final int maxSlice = max_slice - 1;
    final int minIndex = min_index - 1;
    final int maxIndex = max_index - 1;
    final int numSlices = maxSlice - minSlice + 1;
    final int numIndices = maxIndex - minIndex + 1;
    final boolean arbSlice = singleSlice && bio.sm.getPlaneSelect();
    String s = "Export " + (arbSlice ? "arbitrary slice" : "slice" +
      (numSlices == 1 ? " #" + min_slice : "s " + min_slice + " through " +
      max_slice)) + " at timestep" + (numIndices == 1 ? " #" + min_index :
      "s " + min_index + " through " + max_index) + " to file" +
      (series.length == 1 ? " " + series[0] : "s " + series[0] +
      " through " + series[series.length - 1]) + "?";
    int ans = JOptionPane.showConfirmDialog(bio, s, "VisBio",
      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (ans != JOptionPane.YES_OPTION) return;

    // export data series
    final ProgressDialog dialog = new ProgressDialog(bio, "Exporting");
    final Form saver = form;
    Thread t = new Thread(new Runnable() {
      public void run() {
        File[] infiles = bio.sm.getSeries();
        boolean sliceSeries = bio.sm.getFilesAsSlices();
        boolean needReload = !sliceSeries;
        if (needReload) {
          try { bio.sm.purgeData(true); }
          catch (VisADException exc) { exc.printStackTrace(); }
          catch (RemoteException exc) { exc.printStackTrace(); }
        }
        for (int i=0; i<series.length; i++) {
          dialog.setText("Exporting " + series[i].getName());
          DataImpl data;
          try {
            FlatField[] images;

            if (arbSlice) {
              // data to export is arbitrary slice
              if (sliceSeries) {
                // loaded dataset is a slice series
                images = new FlatField[1];
                images[0] = (FlatField) bio.sm.arb.extractSlice((FieldImpl)
                  bio.sm.getField().domainMultiply(), resX, resY, rx, ry);
              }
              else {
                // loaded dataset is a timestep series
                if (filesAsSlices) {
                  // compile slice across timesteps
                  images = new FlatField[numIndices];
                  for (int j=0; j<numIndices; j++) {
                    File f = infiles[minIndex + j];
                    FieldImpl timestep = BioUtil.loadData(f, true);
                    images[j] = (FlatField) bio.sm.arb.extractSlice((FieldImpl)
                      timestep.domainMultiply(), resX, resY, rx, ry);
                    float percent = (float) (j + 1) / numIndices;
                    dialog.setPercent((int) (100 * percent));
                  }
                }
                else {
                  // compile timestep across slices
                  images = new FlatField[1];
                  File f = infiles[minIndex + i];
                  FieldImpl timestep = BioUtil.loadData(f, true);
                  images[0] = (FlatField) bio.sm.arb.extractSlice((FieldImpl)
                    timestep.domainMultiply(), resX, resY, rx, ry);
                }
              }
            }
            else if (sliceSeries) {
              // loaded dataset is a slice series
              FieldImpl field = bio.sm.getField();
              if (filesAsSlices) {
                // single slice, all timesteps
                images = new FlatField[1];
                images[0] = (FlatField) field.getSample(minSlice + i);
              }
              else {
                // single timestep, all slices
                images = new FlatField[numSlices];
                for (int j=0; j<numSlices; j++) {
                  images[j] = (FlatField) field.getSample(minSlice + j);
                  float percent = (float)
                    (numSlices * i + (j + 1)) / (series.length * numSlices);
                  dialog.setPercent((int) (100 * percent));
                }
              }
            }
            else {
              // loaded dataset is a timestep series
              if (filesAsSlices) {
                // compile slice across timesteps
                images = new FlatField[numIndices];
                for (int j=0; j<numIndices; j++) {
                  File f = infiles[minIndex + j];
                  FieldImpl timestep = BioUtil.loadData(f, true);
                  images[j] = (FlatField) timestep.getSample(minSlice + i);
                  float percent = (float)
                    (numIndices * i + (j + 1)) / (series.length * numIndices);
                  dialog.setPercent((int) (100 * percent));
                }
              }
              else {
                // compile timestep across slices
                images = new FlatField[numSlices];
                File f = infiles[minIndex + i];
                FieldImpl timestep = BioUtil.loadData(f, true);
                for (int j=0; j<numSlices; j++) {
                  images[j] = (FlatField) timestep.getSample(minSlice + j);
                  float percent = (float)
                    (numSlices * i + (j + 1)) / (series.length * numSlices);
                  dialog.setPercent((int) (100 * percent));
                }
              }
            }
            for (int j=0; j<images.length; j++) {
              // resample images to proper size if necessary
              GriddedSet set = (GriddedSet) images[j].getDomainSet();
              int[] l = set.getLengths();
              if (l[0] != resX || l[1] != resY) {
                Set nset = new Linear2DSet(set.getType(),
                  0, l[0] - 1, resX, 0, l[1] - 1, resY);
                images[j] = (FlatField) images[j].resample(nset);
              }
            }
            data = BioUtil.makeStack(images);
            if (colors) {
              //
            }
            if (align) {
              //
            }

            // save image stack data to file
            saver.save(series[i].getPath(), data, true);
          }
          catch (VisADException exc) { dialog.setException(exc); }
          catch (Exception exc) {
            dialog.setException(new VisADException(
              exc.getClass() + ": " + exc.getMessage()));
          }
          float percent = (float) (i + 1) / series.length;
          dialog.setPercent((int) (100 * percent));
        }

        dialog.setText("Finishing");
        if (needReload) {
          try { bio.sm.setFile(false); }
          catch (VisADException exc) { exc.printStackTrace(); }
          catch (RemoteException exc) { exc.printStackTrace(); }
        }
        dialog.kill();
      }
    });
    t.start();
    dialog.show();
    try { dialog.checkException(); }
    catch (VisADException exc) {
      JOptionPane.showMessageDialog(bio,
        "Cannot export data\n" + exc.getMessage(),
        "Export error", JOptionPane.ERROR_MESSAGE);
    }
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
    else {
      if (command.equals("sliceOnly")) {
        exportSlices.setEnabled(!sliceOnly.isSelected());
      }
      else if (command.equals("timeOnly")) {
        exportTimesteps.setEnabled(!timeOnly.isSelected());
      }
      else if (command.equals("exportSlices")) {
        sliceOnly.setEnabled(!exportSlices.isSelected());
      }
      else if (command.equals("exportTimesteps")) {
        timeOnly.setEnabled(!exportTimesteps.isSelected());
      }
      computeEnd();
    }
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

}
