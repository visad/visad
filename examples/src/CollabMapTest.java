//
// CollabMapTest.java
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
import java.net.MalformedURLException;
import java.rmi.*;
import java.util.Vector;
import javax.swing.*;
import visad.*;
import visad.data.*;
import visad.java3d.DisplayImplJ3D;
import visad.ss.MappingDialog;

/** A simple test for collaborative ScalarMap editing */
public class CollabMapTest extends JFrame implements ActionListener {

  /** true if server, false if client */
  private boolean server;

  /** data reference pointing to the data */
  private DataReference ref;

  /** display that shows the data */
  private DisplayImpl disp;

  /** builds the GUI */
  private void constructGUI(String arg, boolean enableButtons) {
    JPanel pane = new JPanel();
    setContentPane(pane);
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.add(disp.getComponent());
    JPanel buttons = new JPanel();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    JButton detect = new JButton("Detect maps");
    JButton edit = new JButton("Edit maps");
    JButton clear = new JButton("Clear maps");
    detect.addActionListener(this);
    detect.setActionCommand("detect");
    detect.setEnabled(enableButtons);
    edit.addActionListener(this);
    edit.setActionCommand("edit");
    edit.setEnabled(enableButtons);
    clear.addActionListener(this);
    clear.setActionCommand("clear");
    clear.setEnabled(enableButtons);
    buttons.add(detect);
    buttons.add(edit);
    buttons.add(clear);
    pane.add(buttons);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });
    setTitle("CollabMapTest " + (server ? "server" : "client") + ": " + arg);
    pack();
    setVisible(true);
  }

  /** usage message */
  private static final String usage =
    "Usage: java CollabMapTest [-s filename] [-c address]";

  /** runs the test */
  public static void main(String[] argv)
    throws VisADException, RemoteException
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
    CollabMapTest test = new CollabMapTest(serv, arg);
  }

  /** constructs a new CollabMapTest display */
  public CollabMapTest(boolean serv, String arg)
    throws VisADException, RemoteException
  {
    server = serv;

    if (server) {
      // load data
      DefaultFamily loader = new DefaultFamily("loader");
      Data data = null;
      try {
        data = loader.open(arg);
      }
      catch (BadFormException exc) {
        System.err.println("The specified data file could not be loaded. " +
          "The file is missing, corrupt, or of the wrong type.");
        exc.printStackTrace();
        throw new VisADException(exc.getMessage());
      }

      // set up data reference
      ref = new DataReferenceImpl("ref");
      ref.setData(data);

      // construct 3-D display
      disp = new DisplayImplJ3D("disp");
      disp.addReference(ref);

      // set up server
      RemoteServerImpl rs = new RemoteServerImpl();
      try {
        Naming.rebind("///CollabMapTest", rs);
      }
      catch (ConnectException exc) {
        System.err.println("Please run rmiregistry first.");
        throw new VisADException(exc.getMessage());
      }
      catch (MalformedURLException exc) {
        exc.printStackTrace();
        throw new VisADException(exc.getMessage());
      }
      RemoteDisplayImpl remote = new RemoteDisplayImpl(disp);
      rs.addDisplay(remote);

      // construct GUI
      constructGUI(arg, true);
    }
    else {
      // set up server
      RemoteServer rs;
      try {
        rs = (RemoteServer) Naming.lookup("//" + arg + "/CollabMapTest");
      }
      catch (NotBoundException exc) {
        System.err.println("The specified address is not " +
          "running a CollabMapTest server!");
        throw new VisADException(exc.getMessage());
      }
      catch (MalformedURLException exc) {
        System.err.println("The specified address is not valid!");
        throw new VisADException(exc.getMessage());
      }
      RemoteDisplay remote = rs.getDisplay(0);

      // construct 3-D display
      disp = new DisplayImplJ3D(remote);

      // grab data reference from cloned display
      Vector links = disp.getLinks();
      ReferenceActionLink link = (ReferenceActionLink) links.elementAt(0);
      DataReference ref = (DataReference) link.getThingReference();

      // construct GUI
      constructGUI(arg, false);
    }
  }

  /** sets the display to use the given mappings */
  private void setMaps(ScalarMap[] maps)
    throws VisADException, RemoteException
  {
    disp.removeReference(ref);
    disp.clearMaps();
    for (int i=0; i<maps.length; i++) disp.addMap(maps[i]);
    disp.addReference(ref);
  }

  /** handles button clicks */
  public synchronized void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("detect")) {
      // detect maps
      try {
        setMaps(ref.getData().getType().guessMaps(true));
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
    else if (cmd.equals("edit")) {
      // edit maps
      try {
        Vector mapVector = disp.getMapVector();
        int len = mapVector.size();
        ScalarMap[] maps = (len > 0 ? new ScalarMap[len] : null);
        for (int i=0; i<len; i++) maps[i] = (ScalarMap) mapVector.elementAt(i);
        MappingDialog dialog =
          new MappingDialog(this, ref.getData(), maps, true, true);
        dialog.display();
        if (!dialog.okPressed()) return;
        setMaps(dialog.getMaps());
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
    else if (cmd.equals("clear")) {
      // clear maps
      try {
        disp.removeReference(ref);
        disp.clearMaps();
        disp.addReference(ref);
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
  }

}

