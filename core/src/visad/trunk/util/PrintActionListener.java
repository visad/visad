//
// PrintActionListener.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import visad.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.event.*;
import javax.swing.*;

/** PrintActionListener is an ActionListener which is used to
* print the contents of the VisAD DisplayImpl.  A
* simple way to use this is:
*
*<pre>
*  DisplayImpl di = new DisplayImpl(...);
*  ...
*  JButton pb = new JButton("Print Me");
*  pb.addActionListener(new PrintActionListener(di));
*</pre>
*
*/
public class PrintActionListener
     implements ActionListener {

  DisplayImpl display = null;
  private boolean showDialog=true;

  /** ActionListener for printing the contents of the VisAD display
  *
  * @param dim the DisplayImpl to print when the actionPerformed is
  * called
  *
  */
  public PrintActionListener(DisplayImpl dim) {
    display = dim;
  }

  /** set whether the PrintDialog should be used or not
  *
  * @param value set to 'true' to use PrintDialog or to
  *  'false' to supress the dialog and just print.  The
  *  default is 'true'.
  *
  */
  public void setShowDialog(boolean value) {
    showDialog = value;
  }

  /** get the value of the showDialog state
  *
  * @return 'true' is PrintDialog will be used; 'false' if
  *  supressed.
  *
  */
  public boolean getShowDialog() {
    return showDialog;
  }

  /**
   * Set the display to which this action will listen
   *
   * @param dim  DisplayImpl to be printed
   */
  public void setDisplay(DisplayImpl dim)
  {
      display = dim;
  }

 /**
  * Return the display
  *
  * @return the display associated with this ActionListener
  */
 public DisplayImpl getDisplay()
 {
     return display;
 }

 /**
  *cause the printout of the DisplayImpl; if the dialog is
  * enabled, it will pop up to solicit information from the
  * user.  If the dialog is disabled, just print...
  *
  * @param e the ActionEvent which is ignored...
  *
  */
  public void actionPerformed(ActionEvent e) {

    Thread t = new Thread() {
      public void run() {

        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(display.getPrintable());
        if (!showDialog || (showDialog && printJob.printDialog()) ) {
          try {
            printJob.print();
          }
          catch (Exception pe) {
            pe.printStackTrace();
          }
        }
      }

    };

    t.start();
  }

}
