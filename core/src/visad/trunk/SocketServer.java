//
// SocketServer.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.Vector;
import visad.util.Util;

/** SocketServer wraps around a VisAD display, providing support for
    stand-alone remote displays (i.e., not dependent on the VisAD packages)
    that communicate with the server using sockets. For an example, see
    examples/Test68.java together with the
    <a href="http://www.ssec.wisc.edu/~curtis/visad-applet.html">
    stand-alone VisADApplet</a> usable from within a web browser. */
public class SocketServer implements DisplayListener {

  /** debugging flag */
  private static final boolean DEBUG = true;

  /** the default port for server/client communication */
  private static final int DEFAULT_PORT = 4567;

  /** the port at which the server communicates with clients */
  private int port;

  /** the server's associated VisAD display */
  private DisplayImpl display;

  /** flag that signals when the display's image should be recaptured */
  private DataReferenceImpl dirtyFlag;

  /** flag that prevents getImage() calls from signaling a FRAME_DONE event */
  private boolean flag;

  /** array of image data extracted from the VisAD display */
  private byte[] pixels;

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
        synchronized (clientSockets) {
          for (int i=0; i<clientInputs.size(); i++) {
            DataInputStream in = (DataInputStream) clientInputs.elementAt(i);

            // check for client requests in the form of MouseEvent data
            try {
              if (in.available() > 0) {
                // receive the MouseEvent data
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
                  DataOutputStream out =
                    (DataOutputStream) clientOutputs.elementAt(i);
                  updateClient(out);
                }
              }
            }
            catch (IOException exc) {
              if (DEBUG) exc.printStackTrace();
            }
          }
        }
      }
    }
  };

  /** construct an SocketServer for the given VisAD display */
  public SocketServer(DisplayImpl d) throws IOException {
    this(d, DEFAULT_PORT);
  }

  /** construct an SocketServer for the given VisAD display,
      and communicate with clients using the given port */
  public SocketServer(DisplayImpl d, int port) throws IOException {
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

    // create a "dirty flag" for monitoring when clients need to be updated
    try {
      dirtyFlag = new DataReferenceImpl("dirtyFlag");
      dirtyFlag.setData(new Real(0.0));
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }

    // listen for changes to the display
    display.addDisplayListener(this);

    // construct a cell that sends the latest image to the clients
    final DisplayListener l = this;
    CellImpl cell = new CellImpl() {
      public synchronized void doAction()
        throws VisADException, RemoteException
      {
        // get the latest image
        display.removeDisplayListener(l);
        BufferedImage image = display.getImage();

        // get width and height
        w = image.getWidth();
        h = image.getHeight();

        // grab pixels from the image
        int[] pix = new int[w * h];
        image.getRGB(0, 0, w, h, pix, 0, w);

        // convert pixels to byte array
        pixels = Util.intToBytes(pix);

        synchronized (clientSockets) {
          // update all clients with latest image
          for (int i=0; i<clientSockets.size(); i++) {
            DataOutputStream out =
              (DataOutputStream) clientOutputs.elementAt(i);
            updateClient(out);
          }
        }
        display.addDisplayListener(l);
      }
    };

    // link the above triggered Cell to the dirty flag
    try {
      cell.addReference(dirtyFlag);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  /** get the socket port used by this SocketServer */
  public int getPort() {
    return port;
  }

  /** send the latest image from the display to the given output stream */
  private void updateClient(DataOutputStream out) {
    try {
      // send image width and height to the output stream
      out.writeInt(w);
      out.writeInt(h);

      // send pixel data to the output stream
      out.write(pixels);
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  /** called when the server's associated VisAD display changes */
  public void displayChanged(DisplayEvent e)
    throws VisADException, RemoteException
  {
    if (e.getId() == DisplayEvent.FRAME_DONE) {
      // signal that image needs to be recaptured
      dirtyFlag.setData(new Real(0.0));
    }
  }

  /** destroys this server and kills all associated threads */
  public void killServer() {
    // set flag to cause server's threads to stop running
    alive = false;

    // close down all sockets
    synchronized (clientSockets) {
      for (int i=0; i<clientSockets.size(); i++) {
        DataInputStream in = (DataInputStream) clientInputs.elementAt(i);
        DataOutputStream out = (DataOutputStream) clientOutputs.elementAt(i);
        Socket socket = (Socket) clientSockets.elementAt(i);
        try {
          in.close();
          out.close();
          socket.close();
          clientSockets.remove(i);
        }
        catch (IOException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
    }
    try {
      serverSocket.close();
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

}

