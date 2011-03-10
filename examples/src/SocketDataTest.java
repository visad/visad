//
// SocketDataTest.java
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

import java.awt.event.*;
import java.io.IOException;
import java.rmi.*;
import javax.swing.*;
import visad.*;
import visad.data.*;
import visad.java3d.DisplayImplJ3D;
import visad.util.Util;

/** A simple test for collaboration using a socket data server */
public class SocketDataTest extends JFrame implements ActionListener {

  /** true if server, false if client */
  private boolean server;

  /** data reference pointing to the data */
  private DataReferenceImpl ref;

  /** display that shows the data */
  private DisplayImpl disp;

  /** dialog for loading data files */
  private JFileChooser dialog;

  /** builds the GUI */
  private void constructGUI(String arg, boolean enableButtons) {
    // construct JFileChooser dialog for later use
    dialog = Util.getVisADFileChooser();

    // construct main window
    JPanel pane = new JPanel();
    setContentPane(pane);
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.add(disp.getComponent());
    JPanel buttons = new JPanel();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    JButton load = new JButton("Load data");
    JButton reset = new JButton("Reset to default");
    load.addActionListener(this);
    load.setActionCommand("load");
    load.setEnabled(enableButtons);
    reset.addActionListener(this);
    reset.setActionCommand("reset");
    reset.setEnabled(enableButtons);
    buttons.add(load);
    buttons.add(reset);
    pane.add(buttons);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });
    setTitle("SocketDataTest " + (server ? "server" : "client") + ": " + arg);
    pack();
    setVisible(true);
  }

  /** usage message */
  private static final String usage =
    "Usage: java SocketDataTest [-s port] [-c ip.address:port]";

  /** runs the test */
  public static void main(String[] argv)
    throws VisADException, RemoteException, IOException
  {
    if (argv.length < 2) {
      System.err.println("Not enough arguments.");
      System.err.println(usage);
      System.exit(1);
    }
    String sc = argv[0];
    String arg = argv[1];
    boolean serv = false;
    if (sc.equalsIgnoreCase("-s")) serv = true;
    else if (!sc.equalsIgnoreCase("-c")) {
      System.err.println("Please specify either -s or -c");
      System.err.println(usage);
      System.exit(2);
    }
    SocketDataTest test = new SocketDataTest(serv, arg);
  }

  /** constructs a new SocketDataTest display */
  public SocketDataTest(boolean serv, String arg)
    throws VisADException, RemoteException, IOException
  {
    server = serv;

    // construct 3-D display
    disp = new DisplayImplJ3D("disp");

    if (server) {
      // determine socket port
      int port = -1;
      try {
        port = Integer.parseInt(arg);
      }
      catch (NumberFormatException exc) { }
      if (port < 0 || port > 9999) {
        System.err.println("Invalid port: " + arg);
        System.exit(3);
      }

      // construct socket data server at given port
      ref = new DataReferenceImpl("ref");
      disp.addReference(ref);
      SocketDataServer server = new SocketDataServer(port, ref);
      loadData(null);

      // construct GUI
      constructGUI(arg, true);
    }
    else {
      // open data from socket source
      SocketDataSource source = new SocketDataSource("SocketDataTest");
      source.open(arg);
      ref = source.getReference();
      disp.addReference(ref);
      CellImpl mapsCell = new CellImpl() {
        public synchronized void doAction()
          throws VisADException, RemoteException
        {
          // auto-detect maps when source data changes
          Data data = ref.getData();
          if (data != null) setMaps(data);
        }
      };  
      mapsCell.addReference(ref);

      // construct GUI
      constructGUI(arg, false);
    }
  }

  /** loads a data set from the given file, or reverts back to the default */
  private void loadData(String file)
    throws VisADException, RemoteException
  {
    Data data = null;

    if (file == null) {
      // revert to default data set
      int size = 64;
      RealType ir_radiance = RealType.getRealType("ir_radiance");
      RealType vis_radiance = RealType.getRealType("vis_radiance");
      RealType[] types = {RealType.Latitude, RealType.Longitude};
      RealType[] types2 = {vis_radiance, ir_radiance};
      RealTupleType earth_location = new RealTupleType(types);
      RealTupleType radiance = new RealTupleType(types2);
      FunctionType image_tuple = new FunctionType(earth_location, radiance);
      data = FlatField.makeField(image_tuple, size, false);
    }
    else {
      // load data set from the given file
      DefaultFamily loader = new DefaultFamily("loader");
      try {
        data = loader.open(file);
      }
      catch (BadFormException exc) {
        throw new VisADException(exc.getMessage());
      }
    }

    // set up mappings and data reference
    if (data != null) {
      setMaps(data);
      ref.setData(data);
    }
  }

  /** sets the current data sets mappings */
  private void setMaps(Data data) throws VisADException, RemoteException {
    // guess good mappings
    ScalarMap[] maps = data.getType().guessMaps(true);

    // set the mappings
    disp.removeReference(ref);
    disp.clearMaps();
    for (int i=0; i<maps.length; i++) disp.addMap(maps[i]);
    disp.addReference(ref);
  }

  /** handles button clicks */
  public synchronized void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    try {
      if (cmd.equals("load")) {
        // load new data set
        int returnVal = dialog.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          loadData(dialog.getSelectedFile().getAbsolutePath());
        }
      }
      else if (cmd.equals("reset")) {
        // reset to default data set
        loadData(null);
      }
    }
    catch (VisADException exc) {
      exc.printStackTrace();
    }
    catch (RemoteException exc) {
      exc.printStackTrace();
    }
  }

}

