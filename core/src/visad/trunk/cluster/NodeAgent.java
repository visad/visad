//
// NodeAgent.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

package visad.cluster;

import java.rmi.*;
import java.io.Serializable;

/**
   NodeAgent is the abstract super-class for agents sent from
   client to nodes.<P>
*/
public abstract class NodeAgent extends Object
       implements Serializable, Runnable {

  /** object of agent */
  private Object object = null;

  /** source of agent */
  private RemoteClientAgent source = null;

  /** RemoteAgentContact for communicating back to client */
  RemoteAgentContactImpl contact = null;

  /** NodeAgent is Serializable, mark as transient */
  private transient Thread agentThread;

  /** message from client, if non-null */
  Serializable message = null;

  public NodeAgent(RemoteClientAgent s) {
    source = s;
  }

  // should only one NodeAgent of this class exist on a
  // RemoteNodeDataImpl's agents Vector?
  public boolean onlyOne() {
    return true;
  }

  public Object getObject() {
    return object;
  }

  public Thread getAgentThread() {
    return agentThread;
  }

  // message from client
  public synchronized void sendToNode(Serializable me) {
// System.out.println("NodeAgent.sendToNode " + me);
    message = me;
    notify();
  }

  // called from run() methods of sub-classes
  public synchronized Serializable getMessage() {
// System.out.println("NodeAgent.getMessage enter");
    while (message == null) {
      try {
        wait();
      }
      catch (InterruptedException e) {
      }
    }
    Serializable me = message;
    message = null;
// System.out.println("NodeAgent.getMessage " + me);
    return me;
  }

  public void sendToClient(Serializable message) {
    try {
      source.sendToClient(message);
    }
    catch (RemoteException e) {
      System.out.println("unable to send: " + message);
    }
  }

  /** create and start Thread, and return contact */
  public RemoteAgentContactImpl getRemoteAgentContact(Object obj) {
// System.out.println("NodeAgent.getRemoteAgentContact start Thread");
    object = obj;
    agentThread = new Thread(this);
    agentThread.start();
    try {
      contact = new RemoteAgentContactImpl(this);
      return contact;
    }
    catch (RemoteException e) {
      return null;
    }
  }

  public void stop() {
    sendToNode("stop");
    agentThread = null;
  }

  public abstract void run();
/*
    Thread me = Thread.currentThread();
    while (agentThread == me) {
    }
*/

}

