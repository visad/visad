//
// DataBrowser.java
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
import java.io.IOException;
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.data.DefaultFamily;
import visad.data.dods.DODSForm;
import visad.java3d.DisplayImplJ3D;
import visad.util.*;

/** DataBrowser is a tool for browsing biological DODS data online. */
public class DataBrowser extends JFrame
  implements ActionListener, ChangeListener
{

  // -- CONSTANTS --

  /** Application title. */
  protected static final String TITLE = "DataBrowser";


  // -- DISPLAYS --

  /** VisAD 3-D display. */
  DisplayImpl display3;


  // -- SLIDER WIDGETS --

  /** Widget for stepping through the timestep indices. */
  StepWidget horiz;


  // -- GUI COMPONENTS --

  /** Panel containing all components. */
  private JPanel pane;

  /** Field containing DODS URL. */
  private JTextField addressBar;

  /** Label containing memory usage information. */
  private JLabel memoryLabel;

  /** Panel containing VisAD displays. */
  private JPanel displayPane;


  // -- CONSTRUCTOR --

  /** Constructs a new instance of DataBrowser. */
  public DataBrowser() throws VisADException, RemoteException {
    if (!Util.canDoJava3D()) throw new VisADException("Java3D required");
    setTitle(TITLE);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    // lay out components
    pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    setContentPane(pane);

    // address bar
    addressBar = new JTextField();
    addressBar.addActionListener(this);
    Util.adjustTextField(addressBar);
    pane.add(addressBar);

    // display panel
    displayPane = new JPanel();
    displayPane.setLayout(new BoxLayout(displayPane, BoxLayout.Y_AXIS));
    displayPane.setAlignmentX(CENTER_ALIGNMENT);
    pane.add(displayPane);

    // 3-D display
    display3 = new DisplayImplJ3D("display3");
    doDisplay(display3);
    display3.getGraphicsModeControl().setLineWidth(2.0f);
    displayPane.add(display3.getComponent());

    // horizontal slider
    horiz = new StepWidget(true);
    horiz.addChangeListener(this);
    pane.add(horiz);

    // memory usage label
    JPanel memoryPane = new JPanel();
    pane.add(memoryPane);
    memoryLabel = new JLabel("Memory: total xxxx MB; used xxxx MB (xxx%)");
    Thread t = new Thread(new Runnable() {
      public void run() {
        while (true) {
          // update memory usage information twice per second
          double total = (double) Runtime.getRuntime().totalMemory();
          double free = (double) Runtime.getRuntime().freeMemory();
          double used = total - free;
          int percent = (int) (100 * (used / total));
          int tt = (int) (total / 1000000);
          int u = (int) (used / 1000000);
          final String s = "Memory: total " + tt + " MB; " +
            "used " + u + " MB (" + percent + "%)";
          Util.invoke(false, new Runnable() {
            public void run() { memoryLabel.setText(s); }
          });
          try { Thread.sleep(500); }
          catch (InterruptedException exc) { }
        }
      }
    });
    t.start();
    memoryPane.add(memoryLabel);
    Dimension msize = memoryPane.getMaximumSize();
    Dimension psize = memoryPane.getPreferredSize();
    msize.height = psize.height;
    memoryPane.setMaximumSize(msize);
  }


  // -- API METHODS --

  /** Adjusts the aspect ratio of the displays. */
  public void setAspect(double x, double y, double z) {
    double d = x > y ? x : y;
    double xasp = x / d;
    double yasp = y / d;
    double zasp = z == z ? z / d : 1.0;
    ProjectionControl pc3 = display3.getProjectionControl();
    try { pc3.setAspect(new double[] {xasp, yasp, zasp}); }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }


  // -- INTERNAL API METHODS --

  /** Listens for address bar changes. */
  public void actionPerformed(ActionEvent e) {
    String url = addressBar.getText();
    DataImpl data = null;
    System.out.print("Loading " + url + "... ");
    try {
      if (url.startsWith("http:")) {
        DODSForm dods = DODSForm.dodsForm();
        data = dods.open(url);
      }
      else {
        DefaultFamily loader = new DefaultFamily("loader");
        data = loader.open(url);
      }
      System.out.println("done.");
      System.out.println("MathType = " + data.getType());
    }
    catch (IOException exc) {
      System.out.println("IOException:");
      exc.printStackTrace();
    }
    catch (VisADException exc) {
      System.out.println("VisADException:");
      exc.printStackTrace();
    }

    try {
      display3.removeAllReferences();
      display3.clearMaps();
      ScalarMap[] maps = data.getType().guessMaps(true);
      for (int i=0; i<maps.length; i++) display3.addMap(maps[i]);
      DataReferenceImpl ref = new DataReferenceImpl("ref");
      ref.setData(data);
      display3.addReference(ref);

      if (data instanceof FieldImpl) {
        int len = ((FieldImpl) data).getLength();
        horiz.setBounds(1, len, 1);
        horiz.setEnabled(true);
      }
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Listens for file series widget changes. */
  public void stateChanged(ChangeEvent e) {
    int max = horiz.getMaximum();
    int cur = horiz.getValue();
    try {
      AnimationControl control =
        (AnimationControl) display3.getControl(AnimationControl.class);
      control.setCurrent(cur - 1);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    setTitle(
      TITLE + " - (" + cur + "/" + max + ")");
  }

  /** Toggles the cursor between hourglass and normal pointer mode. */
  void setWaitCursor(boolean wait) {
    setCursor(wait ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) :
      Cursor.getDefaultCursor());
  }


  // -- HELPER METHODS --

  /** Configures the given display to DataBrowser standards. */
  private void doDisplay(DisplayImpl display)
    throws VisADException, RemoteException
  {
    display.getMouseBehavior().getMouseHelper().setFunctionMap(new int[][][] {
      {{MouseHelper.DIRECT, MouseHelper.DIRECT},
       {MouseHelper.DIRECT, MouseHelper.DIRECT}},
      {{MouseHelper.CURSOR_TRANSLATE, MouseHelper.CURSOR_ZOOM},
       {MouseHelper.CURSOR_ROTATE, MouseHelper.NONE}},
      {{MouseHelper.ROTATE, MouseHelper.ZOOM},
       {MouseHelper.TRANSLATE, MouseHelper.NONE}}
    });
    display.getGraphicsModeControl().setColorMode(
      GraphicsModeControl.SUM_COLOR_MODE);
  }

  /** Sets the window to a reasonable size. */
  private void doPack() { setSize(300, 390); }

  /** Quits the program. */
  private void quit() { System.exit(0); }


  // -- MAIN --

  /** Launches the DataBrowser GUI. */
  public static void main(String[] args) throws Exception {
    final DataBrowser db = new DataBrowser();
    db.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { db.quit(); }
    });
    db.doPack();
    Util.centerWindow(db);
    db.show();
  }

}
