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
import visad.util.Util;

/**
 * ProgressDialog is a modal dialog displaying a progress bar.
 * It pops up on construction, and disappears when dismissed from
 * another Thread with hide().
 */
public class ProgressDialog extends JDialog {

  // -- CONSTANTS --

  /** Minimum width of the progress bar onscreen. */
  private static final int MINIMUM_WIDTH = 300;

 
  // -- GUI COMPONENTS --

  /** Progress bar. */
  private JProgressBar bar;


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
    JLabel label = new JLabel(info + "...");
    pane.add(ToolPanel.pad(label));

    // progress bar
    bar = new JProgressBar();
    bar.setStringPainted(true);
    bar.setString("0%");
    pane.add(bar);

    // finish layout
    pack();
    Util.centerWindow(this);
  }


  // -- INTERNAL API METHODS --

  /** Keeps progress dialog from closing due to user intervention. */
  public void hide() { }

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

  /** Hides the progress dialog. */
  public void kill() { super.hide(); }


  // -- MAIN --

  /** Launches the ProgressDialog GUI. */
  public static void main(String[] args) throws Exception {
    final ProgressDialog dialog =
      new ProgressDialog((Frame) null, "Testing ProgressDialog");
    Thread t = new Thread(new Runnable() {
      public void run() {
        int percent = 0;
        while (percent <= 100) {
          try { Thread.sleep((int) (Math.random() * 200)); }
          catch (InterruptedException exc) { exc.printStackTrace(); }
          dialog.setPercent(++percent);
        }
        dialog.kill();
      }
    });
    t.start();
    dialog.show();
    System.exit(0);
  }

}
