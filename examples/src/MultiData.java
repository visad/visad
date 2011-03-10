//
// MultiData.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Dimension;
import java.awt.event.*;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.swing.*;
import visad.*;
import visad.data.*;
import visad.java3d.DisplayImplJ3D;

/** Demonstrates how to switch multiple data objects
    between a single display. */
public class MultiData extends JFrame {

  /** imports data objects from the given list of files */
  private static Data[] loadData(String[] files)
    throws VisADException, RemoteException, BadFormException, IOException
  {
    DefaultFamily loader = new DefaultFamily("loader");
    Data[] d = new Data[files.length];
    for (int i=0; i<files.length; i++) {
      d[i] = loader.open(files[i]);
    }
    return d;
  }

  /** sets up the given display to use the specified data reference and data */
  private static void setupDisplay(Display display, DataReferenceImpl ref,
    Data data) throws VisADException, RemoteException
  {
    // get a list of decent mappings for this data
    MathType type = data.getType();
    ScalarMap[] maps = type.guessMaps(true);

    // add the maps to the display
    for (int i=0; i<maps.length; i++) {
      display.addMap(maps[i]);
    }

    // add the data reference to the display
    ref.setData(data);
    display.addReference(ref);
  }

  /** constructs a new MultiData application */
  public MultiData(String[] files) throws Exception {
    // load all data files and set up VisAD display
    final Data[] data = loadData(files);
    final DisplayImplJ3D display = new DisplayImplJ3D("display");
    final DataReferenceImpl ref = new DataReferenceImpl("ref");
    setupDisplay(display, ref, data[0]);

    // construct user interface
    setTitle("Multiple data viewer");
    JPanel pane = new JPanel();
    setContentPane(pane);
    pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
    JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    left.add(display.getComponent());
    final JComboBox combo = new JComboBox();
    combo.setEditable(false);
    for (int i=0; i<files.length; i++) {
      combo.addItem(files[i]);
    }
    left.add(combo);
    pane.add(left);
    final JPanel widgetPanel = new JPanel();
    widgetPanel.setPreferredSize(new Dimension(400, 0));
    widgetPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
    widgetPanel.add(display.getWidgetPanel());
    pane.add(widgetPanel);

    // update the GUI whenever the user selects new data from the list
    combo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = combo.getSelectedIndex();
        try {
          // remove the old widgets from the user interface
          widgetPanel.remove(0);
          // remove the old data from the display
          display.removeReference(ref);
          display.clearMaps();
          // add the new data to the display
          try {
            setupDisplay(display, ref, data[index]);
          }
          catch (VisADException exc) {
            exc.printStackTrace();
          }
          catch (RemoteException exc) {
            exc.printStackTrace();
          }
          // add the new widgets to the user interface and refresh it
          widgetPanel.add(display.getWidgetPanel());
          widgetPanel.validate();
          widgetPanel.repaint();
        }
        catch (VisADException exc) {
          exc.printStackTrace();
        }
        catch (RemoteException exc) {
          exc.printStackTrace();
        }
      }
    });
  }

  /** tests the MultiData application */
  public static void main(String[] argv) throws Exception {
    if (argv.length < 2) {
      System.err.println("Please specify at least two data files " +
        "on the command line.");
      System.exit(1);
    }
    MultiData md = new MultiData(argv);
    md.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    md.setSize(700, 400);
    md.setVisible(true);
  }

}

