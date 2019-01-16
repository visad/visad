//
// SocketDataServer.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;

/**
 * A class for exporting data to a socket stream. Whenever the given
 * DataReference's data changes, it is sent across the socket to any clients
 * that are listening.
 */
public class SocketDataServer {

  /**
   * The main socket for this SocketDataServer.
   */
  protected ServerSocket socket;

  /**
   * List of client sockets listening to this SocketDataServer.
   */
  protected Vector sockets = new Vector();

  /**
   * List of output streams for client sockets.
   */
  protected Vector outs = new Vector();

  /**
   * The socket's port.
   */
  protected int port;

  /**
   * DataReference whose data is linked to the socket stream.
   */
  protected DataReferenceImpl ref;

  /**
   * Whether the server is still active.
   */
  protected boolean alive = true;

  /**
   * Code for monitoring incoming clients.
   */
  private Runnable connect = new Runnable() {
    public void run() {
      while (alive) {
        try {
          // wait for a new socket to connect
          Socket s = socket.accept();
          if (!alive) break;
          synchronized (sockets) {
            if (s != null) {
              // add client to the list
              sockets.add(s);

              // add client's input and output streams to the list
              ObjectOutputStream out =
                new ObjectOutputStream(s.getOutputStream());
              outs.add(out);

              // send the current data to the client
              Data data = SocketDataServer.this.ref.getData();
              out.writeObject(data);
            }
          }
        }
        catch (IOException exc) { }
      }
    }
  };

  /**
   * Cell for monitoring data changes.
   */
  private CellImpl commCell = new CellImpl() {
    public synchronized void doAction()
      throws VisADException, RemoteException
    {
      // send new data to each client using its socket
      synchronized (sockets) {
        Data data = SocketDataServer.this.ref.getData();
        int i = 0;
        while (i < sockets.size()) {
          Socket s = (Socket) sockets.elementAt(i);
          ObjectOutputStream out = (ObjectOutputStream) outs.elementAt(i);
          try {
            out.writeObject(data);
            i++;
          }
          catch (IOException exc) {
            // something wrong with this socket; kill it
            killSocket(i);
          }
        }
      }
    }
  };

  /**
   * Construct a SocketDataServer with the given port and data reference.
   */
  public SocketDataServer(int port, DataReferenceImpl ref)
    throws VisADException, IOException
  {
    this.port = port;
    this.ref = ref;

    // create a server socket at the given port
    socket = new ServerSocket(port);

    // monitor incoming client socket connections
    Thread connectThread = new Thread(connect);
    connectThread.start();

    // monitor data changes
    commCell.addReference(ref);
  }

  /**
   * Shut down the given socket, and removes it from the socket vector.
   */
  private void killSocket(int i) {
    ObjectOutputStream out = (ObjectOutputStream) outs.elementAt(i);
    Socket s = (Socket) sockets.elementAt(i);

    // shut down socket output stream
    try {
      out.close();
    }
    catch (IOException exc) { }

    // shut down socket itself
    try {
      s.close();
    }
    catch (IOException exc) { }

    // remove socket from socket vectors
    sockets.remove(i);
    outs.remove(i);
  }

  /** destroys this server and kills all associated threads */
  public void killServer() {
    // set flag to cause server's threads to stop running
    alive = false;

    // shut down all client sockets
    synchronized (sockets) {
      while (sockets.size() > 0) killSocket(0);
    }

    // shut down server socket
    try {
      socket.close();
    }
    catch (IOException exc) { }
  }

}

