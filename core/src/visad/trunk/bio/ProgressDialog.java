//
// ProgressDialog.java
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
import javax.swing.*;
import visad.VisADException;
import visad.util.Util;

/**
 * ProgressDialog is a modal dialog displaying a progress bar.
 * It pops up on construction, and disappears when dismissed from
 * another Thread with kill().
 */
public class ProgressDialog extends JDialog {

  // -- CONSTANTS --

  /** Minimum width of the progress bar onscreen. */
  private static final int MINIMUM_WIDTH = 300;

 
  // -- GUI COMPONENTS --

  /** Progress bar. */
  private JProgressBar bar;

  /** Information label. */
  private JLabel label;


  // -- OTHER FIELDS --

  /** Exception to throw when checkException() is called. */
  private VisADException exc;


  // -- CONSTRUCTORS --

  /** Constructs a new instance of ProgressDialog as child of a frame. */
  public ProgressDialog(Frame owner, String info) {
    super(owner, "Please wait...", true);
    doGUI(info);
  }

  /** Constructs a new instance of ProgressDialog as child of a dialog. */
  public ProgressDialog(Dialog owner, String info) {
    super(owner, "Please wait...", true);
    doGUI(info);
  }

  /** Does layout of the progress dialog's interface. */
  private void doGUI(String info) {
    // lay out components
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    setContentPane(pane);

    // info label
    label = new JLabel(info + "...");
    pane.add(ToolPanel.pad(label));

    // progress bar
    bar = new JProgressBar();
    bar.setStringPainted(true);
    bar.setString("0%");
    pane.add(bar);

    // finish layout
    pack();
    setResizable(false);
    Util.centerWindow(this);
  }


  // -- INTERNAL API METHODS --

  /** Keeps progress dialog from closing due to user intervention. */
  public void setVisible(boolean visible) { }

  /** Makes progress dialog decently sized. */
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    return new Dimension(
      d.width > MINIMUM_WIDTH ? d.width : MINIMUM_WIDTH, d.height);
  }


  // -- API METHODS --

  /** Sets the progress amount of the progress dialog. */
  public void setPercent(int value) {
    bar.setString(value + "%");
    bar.setValue(value);
  }

  /** Sets the information label to the specified string. */
  public void setText(String info) { label.setText(info + "..."); }

  /** Sets the exception to be thrown when checkException() is called. */
  public void setException(VisADException exc) { this.exc = exc; }

  /** Throws the exception registered with setException(), if any. */
  public void checkException() throws VisADException {
    if (exc != null) throw exc;
  }

  /**
   * Displays the dialog and runs the given code in a separate thread.
   * Blocks until the dialog is hidden. To accomplish this, the last line
   * of the given Runnable object's code should be a call to kill().
   */
  public void go(Runnable r) {
    Thread t = new Thread(r);
    t.start();
    show();
  }
  
  /** Hides the progress dialog. */
  public void kill() { super.setVisible(false); }


  // -- MAIN --

  /** Launches the ProgressDialog GUI. */
  public static void main(String[] args) throws Exception {
    final ProgressDialog dialog =
      new ProgressDialog((Frame) null, "Testing ProgressDialog");
    dialog.go(new Runnable() {
      public void run() {
        int percent = 0;
        while (percent <= 100) {
          try { Thread.sleep((int) (Math.random() * 200)); }
          catch (InterruptedException exc) { exc.printStackTrace(); }
          dialog.setPercent(++percent);
          if (percent == 90) dialog.setText("Almost done");
        }
        dialog.kill();
      }
    });
    dialog.checkException();
    System.exit(0);
  }

}
