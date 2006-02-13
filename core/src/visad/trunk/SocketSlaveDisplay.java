//
// SocketSlaveDisplay.java
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

package visad;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.net.*;
import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import visad.browser.*;

/** A SocketSlaveDisplay server wraps around a VisAD display, providing support
    for stand-alone remote displays (i.e., not dependent on the VisAD packages)
    that communicate with the server using sockets. For an example, see
    examples/Test68.java together with the stand-alone VisAD applet
    visad.browser.VisADApplet, usable from within a web browser. */
public class SocketSlaveDisplay implements RemoteSlaveDisplay {

  /** debugging flag */
  private static final boolean DEBUG = false;

  /** the default port for server/client communication */
  private static final int DEFAULT_PORT = 4567;

  /** list of control classes that support socket-based collaboration */
  private static final Class[] supportedControls = {
    GraphicsModeControl.class, ContourControl.class
  };

  /** the port at which the server communicates with clients */
  private int port;

  /** the server's associated VisAD display */
  private DisplayImpl display;

  /** flag that prevents getImage() calls from signaling a FRAME_DONE event */
  private boolean flag;

  /** array of image data extracted from the VisAD display */
  private byte[] pix;

  /** height of image */
  private int h;

  /** width of image */
  private int w;

  /** the server's associated socket */
  private ServerSocket serverSocket;

  /** vector of client sockets connected to the server */
  private Vector clientSockets = new Vector();

  /** vector of client sockets' input streams */
  private Vector clientInputs = new Vector();

  /** vector of client sockets' output streams */
  private Vector clientOutputs = new Vector();

  /** vector of client socket ids */
  private Vector clientIds = new Vector();

  /** thread monitoring incoming clients */
  private Thread connectThread = null;

  /** thread monitoring communication between server and clients */
  private Thread commThread = null;

  /** whether the server is still alive */
  private boolean alive = true;

  /** next available client ID number */
  private int clientID = 0;

  /** this socket slave display */
  private final SocketSlaveDisplay socketSlave = this;

  /** contains the code for monitoring incoming clients */
  private Runnable connect = new Runnable() {
    public void run() {
      while (alive) {
        try {
          // wait for a new socket to connect
          Socket socket = serverSocket.accept();
          if (!alive) break;
          if (socket != null) {
            synchronized (clientSockets) {
              // add client to the list
              clientSockets.add(socket);

              // add client's input and output streams to the list
              DataInputStream in =
                new DataInputStream(socket.getInputStream());
              clientInputs.add(in);
              DataOutputStream out =
                new DataOutputStream(socket.getOutputStream());
              clientOutputs.add(out);

              // assign client an ID number
              out.writeInt(++clientID);
              clientIds.add(new Integer(clientID));
            }
          }
        }
        catch (IOException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
    }
  };

  /** contains the code for monitoring server/client communication */
  private Runnable comm = new Runnable() {
    public void run() {
      while (alive) {
        boolean silence = true;
        Object[] sockets, inputs, outputs, cids;
        synchronized (clientSockets) {
          sockets = clientSockets.toArray();
          inputs = clientInputs.toArray();
          outputs = clientOutputs.toArray();
          cids = clientIds.toArray();
        }
        for (int i=0; i<sockets.length; i++) {
          Socket socket = (Socket) sockets[i];
          DataInputStream in = (DataInputStream) inputs[i];
          DataOutputStream out = (DataOutputStream) outputs[i];
          int cid = ((Integer) cids[i]).intValue();

          // check for client requests in the form of MouseEvent data
          try {
            if (in.available() > 0) {
              silence = false;
              // receive the client data
              int sid = in.readInt();
              if (DEBUG && sid != cid) {
                System.err.println("Warning: client #" + cid + " believes " +
                  "its ID number is " + sid);
              }
              int eventType = in.readInt();

              if (eventType == VisADApplet.REFRESH) {
                // send latest display image to the client
                updateClient(socket, in, out);

                // send latest supported control states to the client
                for (int j=0; j<supportedControls.length; j++) {
                  Class c = supportedControls[j];
                  Vector v = display.getControls(c);

                  // send control state message to each client
                  for (int k=0; k<v.size(); k++) {
                    Control control = (Control) v.elementAt(k);
                    String message = c.getName() + "\n" +
                      k + "\n" + control.getSaveString();
                    updateClient(message, socket, in, out);
                  }
                }

                // send latest ScalarMap states to the client
                Vector maps = display.getMapVector();
                for (int j=0; j<maps.size(); j++) {
                  ScalarMap map = (ScalarMap) maps.elementAt(j);
                  ScalarType scalar = map.getScalar();
                  DisplayRealType displayScalar = map.getDisplayScalar();
                  double[] range = map.getRange();
                  String message = "visad.ScalarMap\n" +
                    scalar.getName() + " " + displayScalar.getName() + " " +
                    range[0] + " " + range[1];
                  updateClient(message, socket, in, out);
                }
              }
              else if (eventType == VisADApplet.MOUSE_EVENT) {
                int mid = in.readInt();
                long when = in.readLong();
                int mods = in.readInt();
                int x = in.readInt();
                int y = in.readInt();
                int clicks = in.readInt();
                boolean popup = in.readBoolean();

                // construct resulting MouseEvent and process it
                Component c = display.getComponent();
                MouseEvent me =
                  new MouseEvent(c, mid, when, mods, x, y, clicks, popup);
                MouseBehavior mb = display.getMouseBehavior();
                MouseHelper mh = mb.getMouseHelper();
                mh.processEvent(me, cid);
              }
              else if (eventType == VisADApplet.MESSAGE) {
                int len = in.readInt();
                char[] c = new char[len];
                for (int j=0; j<len; j++) c[j] = in.readChar();
                String message = new String(c);
                StringTokenizer st = new StringTokenizer(message, "\n");
                String controlClass = st.nextToken();
                int index = Convert.getInt(st.nextToken());
                String save = st.nextToken();
                Class cls = null;
                try {
                  cls = Class.forName(controlClass);
                }
                catch (ClassNotFoundException exc) {
                  if (DEBUG) exc.printStackTrace();
                }
                if (cls != null) {
                  Control control = display.getControl(cls, index);
                  if (control != null) {
                    // verify that control state has actually changed
                    if (!save.equals(control.getSaveString())) {
                      try {
                        synchronized (socketSlave) {
                          // temporarily disable control change notification
                          display.removeSlave(socketSlave);
                          control.setSaveString(save);
                          display.addSlave(socketSlave);
                        }
                        // notify clients of change
                        for (int k=0; k<sockets.length; k++) {
                          // skip event source client
                          int kid = ((Integer) cids[k]).intValue();
                          if (kid != cid) {
                            Socket ksocket = (Socket) sockets[k];
                            DataInputStream kin = (DataInputStream) inputs[k];
                            DataOutputStream kout =
                              (DataOutputStream) outputs[k];
                            updateClient(message, ksocket, kin, kout);
                          }
                        }
                      }
                      catch (VisADException exc) {
                        if (DEBUG) exc.printStackTrace();
                      }
                      catch (RemoteException exc) {
                        if (DEBUG) exc.printStackTrace();
                      }
                    }
                  }
                  else {
                    if (DEBUG) System.err.println("Warning: ignoring " +
                      "change to unknown control from client #" + cid);
                  }
                }
              }
              else { // Unknown event type
                if (DEBUG) System.err.println("Warning: " +
                  "ignoring unknown event type from client #" + cid);
              }
            }
          }
          catch (SocketException exc) {
            // there is a problem with this socket, so kill it
            killSocket(socket, in, out);
            break;
          }
          catch (IOException exc) {
            if (DEBUG) exc.printStackTrace();
          }
        }
        if (silence) {
          try {
            Thread.sleep(200);
          }
          catch (InterruptedException exc) { }
        }
      }
    }
  };

  /** construct a SocketSlaveDisplay for the given VisAD display */
  public SocketSlaveDisplay(DisplayImpl d) throws IOException {
    this(d, DEFAULT_PORT);
  }

  /** construct a SocketSlaveDisplay for the given VisAD display,
      and communicate with clients using the given port */
  public SocketSlaveDisplay(DisplayImpl d, int port) throws IOException {
    display = d;
    this.port = port;

    // create a server socket at the given port
    serverSocket = new ServerSocket(port);

    // create a thread that listens for connecting clients
    connectThread = new Thread(connect,
      "SocketSlaveDisplay-Connect-" + display.getName());
    connectThread.start();

    // create a thread for client/server communication
    commThread = new Thread(comm,
      "SocketSlaveDisplay-Comm-" + display.getName());
    commThread.start();

    // register socket server as a slaved display
    display.addSlave(this);
  }

  /** get the socket port used by this SocketSlaveDisplay */
  public int getPort() {
    return port;
  }

  /** send the latest display image to the given socket */
  private void updateClient(Socket socket, DataInputStream in,
    DataOutputStream out)
  {
    if (pix != null) {
      try {
        // send image width, height and array length to the output stream
        out.writeInt(w);
        out.writeInt(h);
        out.writeInt(pix.length);

        // send pixel data to the output stream
        out.write(pix);
      }
      catch (SocketException exc) {
        // there is a problem with this socket, so kill it
        killSocket(socket, in, out);
      }
      catch (IOException exc) {
        if (DEBUG) exc.printStackTrace();
      }
    }
    else if (DEBUG) System.err.println("Null pixels!");
  }

  /** send a message to the given client */
  private void updateClient(String message, Socket socket,
    DataInputStream in, DataOutputStream out)
  {
    try {
      // send message to the output stream
      out.writeInt(-1); // special code of width -1 indicates message
      out.writeInt(message.length());
      out.writeChars(message);
    }
    catch (SocketException exc) {
      // there is a problem with this socket, so kill it
      killSocket(socket, in, out);
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  /** display automatically calls sendImage when its content changes */
  public synchronized void sendImage(int[] pixels, int width, int height,
    int type) throws RemoteException
  {
    // convert pixels to byte array
    pix = Convert.intToBytes(pixels);
    w = width;
    h = height;

    // update all clients with the new image
    int numSockets;
    Object[] sockets, inputs, outputs;
    synchronized (clientSockets) {
      sockets = clientSockets.toArray();
      inputs = clientInputs.toArray();
      outputs = clientOutputs.toArray();
    }
    for (int i=0; i<sockets.length; i++) {
      updateClient((Socket) sockets[i], (DataInputStream) inputs[i],
        (DataOutputStream) outputs[i]);
    }
  }

  /** send the given message to this slave display */
  public synchronized void sendMessage(String message) throws RemoteException {
    Object[] sockets, inputs, outputs;
    synchronized (clientSockets) {
      sockets = clientSockets.toArray();
      inputs = clientInputs.toArray();
      outputs = clientOutputs.toArray();
    }
    for (int i=0; i<sockets.length; i++) {
      updateClient(message, (Socket) sockets[i],
        (DataInputStream) inputs[i], (DataOutputStream) outputs[i]);
    }
  }

  /** shut down the given socket */
  private void killSocket(Socket socket, DataInputStream in,
    DataOutputStream out)
  {
    // shut down socket input stream
    try {
      in.close();
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
    }

    // shut down socket output stream
    try {
      out.close();
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
    }

    // shut down socket itself
    try {
      socket.close();
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
    }

    // remove socket from socket vectors
    synchronized (clientSockets) {
      int index = clientSockets.indexOf(socket);
      clientSockets.removeElementAt(index);
      clientInputs.removeElementAt(index);
      clientOutputs.removeElementAt(index);
      clientIds.removeElementAt(index);
    }
  }

  /** destroy this server and kills all associated threads */
  public void killServer() {
    // set flag to cause server's threads to stop running
    alive = false;

    // shut down all client sockets
    while (true) {
      Socket socket = null;
      DataInputStream in = null;
      DataOutputStream out = null;
      synchronized (clientSockets) {
        if (clientSockets.size() > 0) {
          socket = (Socket) clientSockets.elementAt(0);
          in = (DataInputStream) clientInputs.elementAt(0);
          out = (DataOutputStream) clientOutputs.elementAt(0);
        }
      }
      if (socket == null) break;
      else killSocket(socket, in, out);
    }

    // shut down server socket
    try {
      serverSocket.close();
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

}

