//
// OptionDialog.java
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

import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import visad.browser.Divider;
import visad.util.Util;

/** OptionDialog is a dialog of configuring VisBio options. */
public class OptionDialog extends JPanel implements ActionListener {

  // -- GLOBALS --

  /** Flag indicating whether QuickTime support is available. */
  static boolean CAN_DO_QT = Util.canDoQuickTime();


  // -- FIELDS --

  /** VisBio frame. */
  private VisBio bio;

  /** Currently visible dialog. */
  private JDialog dialog;

  /** Return value of dialog. */
  private int rval;

  /** Whether to ask about QuickTime support on program start. */
  private boolean qtAuto;


  // -- GUI COMPONENTS --

  private JCheckBox enableQT;
  private JCheckBox floating;
  private JButton ok, cancel;


  // -- CONSTRUCTOR --

  /** Creates a file series chooser dialog. */
  public OptionDialog(VisBio biovis) {
    bio = biovis;

    // enable QuickTime option
    enableQT = new JCheckBox("Enable QuickTime support");
    enableQT.setMnemonic('q');
    enableQT.setActionCommand("enableQT");
    enableQT.addActionListener(this);

    // floating control windows option
    floating = new JCheckBox("Floating control windows");
    floating.setMnemonic('f');

    // ok button
    ok = new JButton("Ok");
    ok.setMnemonic('o');
    ok.setActionCommand("ok");
    ok.addActionListener(this);

    // cancel button
    cancel = new JButton("Cancel");
    cancel.setMnemonic('c');
    cancel.setActionCommand("cancel");
    cancel.addActionListener(this);

    // lay out options
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(ToolPanel.pad(enableQT, false, true));
    add(ToolPanel.pad(floating, false, true));

    // lay out buttons
    JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(ok);
    bottom.add(cancel);
    add(ToolPanel.pad(bottom));

    readIni();
  }


  // -- API METHODS --

  /** Displays the option dialog. */
  public int showDialog() {
    dialog = new JDialog(bio, "VisBio Options", true);
    dialog.getRootPane().setDefaultButton(ok);
    dialog.setContentPane(this);
    dialog.pack();
    Util.centerWindow(dialog);
    readIni();
    dialog.setVisible(true);
    return rval;
  }

  /** Whether QuickTime support is enabled. */
  public boolean isQTEnabled() { return enableQT.isSelected(); }

  /** Whether to ask about QuickTime support on startup. */
  public boolean isQTAuto() { return qtAuto; }

  /** Whether floating control windows are enabled. */
  public boolean isFloating() { return floating.isSelected(); }


  // -- INTERNAL API METHODS --

  /** Handles button press events. */
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command.equals("enableQT")) {
      if (enableQT.isSelected() && !CAN_DO_QT) searchQT();
    }
    else if (command.equals("ok")) {
      // save options
      writeIni();
      dialog.setVisible(false);
      bio.setFloating(floating.isSelected());
    }
    else if (command.equals("cancel")) {
      // discard changes
      dialog.setVisible(false);
    }
  }

  /** FileFilter method for accepting pathnames. */
  public boolean accept(File pathname) {
    return pathname.isDirectory() ||
      pathname.getName().equals("QTJava.zip");
  }

  /** Location of QTJava.zip file, used in searchQT(). */
  private File qtjava;

  /** Searches for QTJava.zip to enable QuickTime support. */
  synchronized void searchQT() {
    String[] options = {"Let VisBio search", "Locate it myself", "Cancel"};
    int ans = JOptionPane.showOptionDialog(this,
      "To enable QuickTime support, VisBio needs to locate QuickTime on " +
      "your system by finding a file called QTJava.zip.", "VisBio", -1,
      JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

    qtjava = null;
    String ignore = new File("QTJava.zip").getAbsolutePath();
    final QTFileFilter filter = new QTFileFilter(ignore);
    if (ans == 0) { // Let VisBio search
      String os = System.getProperty("os.name");
      if (os.startsWith("Windows")) {
        // Check Windows system folders first
        File[] system = {
          new File("C:\\WINNT\\system32"),
          new File("C:\\WINDOWS\\system32"),
          new File("C:\\WINDOWS\\SYSTEM")
        };
        for (int i=0; i<system.length; i++) {
          qtjava = searchQT(system[i], filter);
          if (qtjava != null) break;
        }
      }
      if (qtjava == null) {
        final ProgressDialog searching =
          new ProgressDialog(dialog, "Searching");
        Thread t = new Thread(new Runnable() {
          public void run() {
            File[] roots = File.listRoots();
            for (int i=0; i<roots.length; i++) {
              searching.setText("Searching " + roots[i].getAbsolutePath());
              qtjava = searchQT(roots[i], filter);
              if (qtjava != null) break;
              searching.setPercent(100 * (i + 1) / roots.length);
            }
            searching.kill();
          }
        });
        t.start();
        searching.show();
      }
    }
    else if (ans == 1) { // Locate it myself
      JFileChooser fileBox = new JFileChooser();
      fileBox.setFileFilter(filter);
      while (true) {
        int returnVal = fileBox.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File file = fileBox.getSelectedFile();
          if (file != null && !file.getAbsolutePath().equals(ignore) &&
            !file.isDirectory() && file.getName().equals("QTJava.zip"))
          {
            qtjava = file;
            break;
          }
        }
        else break;
      }
    }
    if (qtjava != null) {
      // copy QTJava.zip to current directory
      File dest = new File("QTJava.zip");
      try {
        FileInputStream fin = new FileInputStream(qtjava);
        FileOutputStream fout = new FileOutputStream(dest);
        byte[] buff = new byte[8192];
        while (true) {
          int num = fin.read(buff);
          if (num == -1) break;
          fout.write(buff, 0, num);
        }
        fin.close();
        fout.close();
        JOptionPane.showMessageDialog(this,
          "Using " + qtjava + ". Please restart VisBio to enable QuickTime.",
          "VisBio", JOptionPane.INFORMATION_MESSAGE);
        enableQT.setSelected(true);
      }
      catch (IOException exc) {
        JOptionPane.showMessageDialog(this,
          "Error copying " + qtjava + " to " + dest.getAbsolutePath() +
          ". QuickTime support will remain disabled.",
          "VisBio", JOptionPane.INFORMATION_MESSAGE);
        enableQT.setSelected(false);
      }
    }
    else {
      JOptionPane.showMessageDialog(this,
        "QTJava.zip not found. Make sure you have QuickTime installed. " +
        "See Help, QuickTime for more information.",
        "VisBio", JOptionPane.INFORMATION_MESSAGE);
      enableQT.setSelected(false);
    }
  }

  /** Reads configuration in from visbio.ini. */
  void readIni() {
    File ini = new File("visbio.ini");
    qtAuto = !CAN_DO_QT;
    if (!ini.exists()) {
      enableQT.setSelected(CAN_DO_QT);
      return;
    }
    try {
      BufferedReader fin = new BufferedReader(new FileReader(ini));
      while (true) {
        String line = fin.readLine();
        if (line == null) break;
        int eq = line.indexOf("=");
        if (eq < 0) continue;
        String var = line.substring(0, eq).trim();
        String expr = line.substring(eq + 1).trim();
        if (var.equalsIgnoreCase("qt")) {
          boolean qt = expr.equalsIgnoreCase("true");
          enableQT.setSelected(qt && CAN_DO_QT);
          qtAuto = qt && !CAN_DO_QT;
        }
        if (var.equalsIgnoreCase("float")) {
          boolean fl = expr.equalsIgnoreCase("true");
          floating.setSelected(fl);
        }
      }
      fin.close();
    }
    catch (IOException exc) { exc.printStackTrace(); }
  }

  /** Writes configuration out to visbio.ini. */
  void writeIni() {
    try {
      PrintWriter fout = new PrintWriter(new FileWriter("visbio.ini"));
      fout.println("qt=" + enableQT.isSelected());
      fout.println("float=" + floating.isSelected());
      fout.close();
    }
    catch (IOException exc) { exc.printStackTrace(); }
  }


  // -- HELPER METHODS --

  /** Recursively searches the given folder for QTJava.zip. */
  private File searchQT(File dir, FileFilter filter) {
    if (!dir.exists()) return null;
    File[] list = dir.listFiles(filter);
    for (int i=0; i<list.length; i++) {
      if (!list[i].isDirectory()) return list[i];
    }
    for (int i=0; i<list.length; i++) {
      File f = searchQT(list[i], filter);
      if (f != null) return f;
    }
    return null;
  }

}
