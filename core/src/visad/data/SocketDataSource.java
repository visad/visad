//
// SocketDataSource.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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
import java.net.Socket;
import java.rmi.RemoteException;
import visad.*;

/**
 * A class for linking a socket stream to a DataReference. Whenever the source
 * data changes, the source presumably sends the change through the linked
 * socket, and the DataReference is set to point at the new data.
 */
public class SocketDataSource extends LinkedDataSource {

  /**
   * The socket connection for this SocketDataSource.
   */
  protected Socket socket;

  /**
   * The output stream for the socket connection.
   */
  protected ObjectOutputStream out;
  
  /**
   * The input stream for the socket connection.
   */
  protected ObjectInputStream in;

  /**
   * Code for monitoring socket for incoming source data changes.
   */
  protected Runnable comm = new Runnable() {
    public void run() {
      Object o;

      // read objects until stream closes
      while (true) {
        o = null;
        try {
          o = in.readObject();
        }
        catch (ClassNotFoundException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (IOException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        if (o == null) break;

        // process object
        if (o instanceof DataImpl) {
          // object is updated data
          try {
            dataChanged((Data) o);
          }
          catch (VisADException exc) {
            if (DEBUG) exc.printStackTrace();
          }
          catch (RemoteException exc) {
            if (DEBUG) exc.printStackTrace();
          }
        }
      }

      // socket has died; shut everything down
      try {
        dataChanged(null);
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      try {
        in.close();
      }
      catch (IOException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      try {
        out.close();
      }
      catch (IOException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      try {
        socket.close();
      }
      catch (IOException exc) {
        if (DEBUG) exc.printStackTrace();
      }
    }
  };

  /**
   * Construct a SocketDataSource with the given name.
   */
  public SocketDataSource(String name) {
    super(name);
  }

  /**
   * Link to the given socket, updating the local data whenever an
   * update event is sent through that socket.
   */
  public synchronized void open(String id)
    throws IOException, VisADException, RemoteException
  {
    // parse socket URL
    int index = id.indexOf(":");
    if (index < 0) throw new VisADException("malformed socket URL: " + id);
    String host = id.substring(0, index);
    String p = id.substring(index + 1);
    int port = -1;
    try {
      port = Integer.parseInt(p);
    }
    catch (NumberFormatException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    if (port < 0) throw new VisADException("invalid socket port: " + p);

    // open the socket
    socket = new Socket(host, port);
    out = new ObjectOutputStream(socket.getOutputStream());
    in = new ObjectInputStream(socket.getInputStream());

    // set up socket input thread
    Thread t = new Thread(comm);
    t.start();
  }

  /**
   * Return the socket connection for this SocketDataSource.
   */
  public Socket getSocket() {
    return socket;
  }

  /**
   * Writes the specified object out to the socket.
   */
  public void writeObject(Object o) {
    try {
      out.writeObject(o);
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

}

