//
// SocketSlaveDisplay.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import visad.browser.Convert;

/** A SocketSlaveDisplay server wraps around a VisAD display, providing support
    for stand-alone remote displays (i.e., not dependent on the VisAD packages)
    that communicate with the server using sockets. For an example, see
    examples/Test68.java together with the stand-alone VisAD applet at
    examples/VisADApplet.java, usable from witin a web browser. */
public class SocketSlaveDisplay implements RemoteSlaveDisplay {

  /** debugging flag */
  private static final boolean DEBUG = false;

  /** the default port for server/client communication */
  private static final int DEFAULT_PORT = 4567;

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

  /** thread monitoring incoming clients */
  private Thread connectThread = null;

  /** thread monitoring communication between server and clients */
  private Thread commThread = null;

  /** whether the server is still alive */
  private boolean alive = true;

  /** contains the code for monitoring incoming clients */
  private Runnable connect = new Runnable() {
    public void run() {
      while (alive) {
        try {
          // wait for a new socket to connect
          Socket socket = serverSocket.accept();
          if (!alive) break;
          synchronized (clientSockets) {
            if (socket != null) {
              // add client to the list
              clientSockets.add(socket);

              // add client's input and output streams to the list
              DataInputStream in =
                new DataInputStream(socket.getInputStream());
              clientInputs.add(in);
              DataOutputStream out =
                new DataOutputStream(socket.getOutputStream());
              clientOutputs.add(out);
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
        synchronized (clientSockets) {
          for (int i=0; i<clientInputs.size(); i++) {
            DataInputStream in = (DataInputStream) clientInputs.elementAt(i);

            // check for client requests in the form of MouseEvent data
            try {
              if (in.available() > 0) {
                silence = false;
                // receive the client data
                int eventType = in.readInt();

                if (eventType == 0) { // 0 = MouseEvent
                  int id = in.readInt();
                  long when = in.readLong();
                  int mods = in.readInt();
                  int x = in.readInt();
                  int y = in.readInt();
                  int clicks = in.readInt();
                  boolean popup = in.readBoolean();

                  if (id >= 0) {
                    // construct resulting MouseEvent and process it
                    Component c = display.getComponent();
                    MouseEvent me =
                      new MouseEvent(c, id, when, mods, x, y, clicks, popup);
                    MouseBehavior mb = display.getMouseBehavior();
                    MouseHelper mh = mb.getMouseHelper();
                    mh.processEvent(me);
                  }
                  else {
                    // client has requested a refresh
                    updateClient(i);
                  }
                }
                else if (eventType == 1) { // 1 = message
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
                      try {
                        control.setSaveString(save);
                      }
                      catch (VisADException exc) {
                        if (DEBUG) exc.printStackTrace();
                      }
                      catch (RemoteException exc) {
                        if (DEBUG) exc.printStackTrace();
                      }
                    }
                    else {
                      if (DEBUG) System.err.println("Warning: " +
                        "ignoring change to unknown control from client");
                    }
                  }
                }
                else { // Unknown
                  if (DEBUG) System.err.println("Warning: " +
                    "ignoring unknown event type from client");
                }
              }
            }
            catch (SocketException exc) {
              // there is a problem with this socket, so kill it
              killSocket(i);
              break;
            }
            catch (IOException exc) {
              if (DEBUG) exc.printStackTrace();
            }
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
    connectThread = new Thread(connect);
    connectThread.start();

    // create a thread for client/server communication
    commThread = new Thread(comm);
    commThread.start();

    // register socket server as a "slaved display"
    display.addSlave(this);
  }

  /** get the socket port used by this SocketSlaveDisplay */
  public int getPort() {
    return port;
  }

  /** send the latest display image to the given client */
  private void updateClient(int i) {
    DataOutputStream out = (DataOutputStream) clientOutputs.elementAt(i);
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
        killSocket(i);
      }
      catch (IOException exc) {
        if (DEBUG) exc.printStackTrace();
      }
    }
    else if (DEBUG) System.err.println("Null pixels!");
  }

  /** send a message to the given client */
  private void updateClient(int i, String message) {
    DataOutputStream out = (DataOutputStream) clientOutputs.elementAt(i);
    try {
      // send message to the output stream
      out.writeInt(-1); // special code of width -1 indicates message
      out.writeInt(message.length());
      out.writeChars(message);
    }
    catch (SocketException exc) {
      // there is a problem with this socket, so kill it
      killSocket(i);
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  /** display automatically calls sendImage when its content changes */
  public synchronized void sendImage(int[] pixels, int width, int height,
    int type) throws RemoteException
  {
    // Note: The pixels array is RLE-encoded. The client applet decodes it.

    // convert pixels to byte array
    pix = Convert.intToBytes(pixels);
    w = width;
    h = height;

    // update all clients with the new image
    synchronized (clientSockets) {
      for (int i=0; i<clientSockets.size(); i++) updateClient(i);
    }
  }

  /** Send the given message to this slave display */
  public synchronized void sendMessage(String message) throws RemoteException {
    synchronized (clientSockets) {
      for (int i=0; i<clientSockets.size(); i++) updateClient(i, message);
    }
  }

  /** shuts down the given socket, and removes it from the socket vector */
  private void killSocket(int i) {
    DataInputStream in = (DataInputStream) clientInputs.elementAt(i);
    DataOutputStream out = (DataOutputStream) clientOutputs.elementAt(i);
    Socket socket = (Socket) clientSockets.elementAt(i);

    // shut down socket input stream
    try {
      in.close();
    }
    catch (IOException exc) { }

    // shut down socket output stream
    try {
      out.close();
    }
    catch (IOException exc) { }

    // shut down socket itself
    try {
      socket.close();
    }
    catch (IOException exc) { }

    // remove socket from socket vectors
    clientSockets.remove(i);
    clientInputs.remove(i);
    clientOutputs.remove(i);
  }

  /** destroys this server and kills all associated threads */
  public void killServer() {
    // set flag to cause server's threads to stop running
    alive = false;

    // shut down all client sockets
    synchronized (clientSockets) {
      while (clientSockets.size() > 0) killSocket(0);
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

