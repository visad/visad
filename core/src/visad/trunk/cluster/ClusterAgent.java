//
// ClusterAgent.java
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

package visad.cluster;

import visad.*;
import java.rmi.*;

/**
   ClusterAgent is the agent sent from client to nodes.<P>
*/
public abstract class ClusterAgent extends Object
       implements java.io.Serializable, Runnable {

  /** source of agent */
  private RemoteClientData source = null;

  /** RemoteAgentContact for communicating back to client */
  RemoteAgentContactImpl contact = null;

  /** ClusterAgent is Serializable, mark as transient */
  private transient Thread agentThread;

  public ClusterAgent(RemoteClientData s) {
    source = s;
  }

  /** create and start Thread, and return contact */
  public RemoteAgentContactImpl getRemoteAgentContact() {
    agentThread = new Thread(this);
    agentThread.start();
    try {
      contact = new RemoteAgentContactImpl();
      return contact;
    }
    catch (RemoteException e) {
      return null;
    }
  }

  public void stop() {
    agentThread = null;
  }

  public abstract void run();
/*
    Thread me = Thread.currentThread();
    while (agentThread == me) {
    }
*/

}

