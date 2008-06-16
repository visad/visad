//
// RemoteClientAgentImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;

/**
   RemoteClientAgentImpl is the class for agents on the
   client, which typically send NodeAgents to each node.<P> 
*/
public class RemoteClientAgentImpl extends UnicastRemoteObject
       implements RemoteClientAgent {

  // null indicates this is the focus_agent
  private RemoteClientAgentImpl focus_agent;

  private int index = -1;

  private boolean not_all;
  Serializable[] responses = null;

  private long time_out = 10000;

  public RemoteClientAgentImpl(RemoteClientAgentImpl fa, int ind)
         throws RemoteException {
    this(fa, ind, 10000);
  }

  public RemoteClientAgentImpl(RemoteClientAgentImpl fa, int ind, long to)
         throws RemoteException {
    focus_agent = fa;
    index = ind;
    time_out = to;
  }

  public void sendToClient(Serializable message) throws RemoteException {
    if (focus_agent != null) {
      focus_agent.sendToClient(index, message);
    }
  }

  // should be called only for focus_agent
  public void sendToClient(int ind, Serializable message)
         throws RemoteException {
// System.out.println("RemoteClientAgentImpl.sendToClient " + ind + " " + message);
    if (0 <= ind && ind < responses.length) {
      responses[ind] = message;
      boolean all = true;
      for (int i=0; i<responses.length; i++) {
        if (responses[i] == null) all = false;
      }
      if (all) {
        synchronized (this) {
          not_all = false;
          notify();
        }
      }
    }
  }

  public Serializable[] broadcastWithResponses(Serializable message,
                                               RemoteAgentContact[] contacts)
         throws VisADException, RemoteException {
    return broadcastWithResponses(new Serializable[] {message}, contacts);
  }

  public Serializable[] broadcastWithResponses(Serializable[] messages,
                                               RemoteAgentContact[] contacts)
         throws VisADException, RemoteException {
    int nagents = contacts.length;
    responses = new Serializable[nagents];
    not_all = true;
    for (int i=0; i<nagents; i++) {
      int im = (messages.length == 1) ? 0 : i;
        Serializable message = messages[im];
// System.out.println("RemoteClientAgentImpl.broadcastWithResponses " +
//                    i + " " + message);
      responses[i] = null;
      contacts[i].sendToNode(message);
    }

    long start_time = System.currentTimeMillis();
    while (not_all) {
      synchronized (this) {
        try {
          wait(time_out); // wait for at most time_out ms
        }
        catch (InterruptedException e) {
        }
        long time = System.currentTimeMillis();
        if (time > start_time + time_out) {
          not_all = false;
System.out.println("RemoteClientAgentImpl.broadcastWithResponses time out");
        }
      }
    }
    for (int i=0; i<responses.length; i++) {
      if (responses[i] instanceof String &&
          ((String) responses[i]).equals("error")) {
        throw new ClusterException("error from node " + i);
      }
    }
// System.out.println("RemoteClientAgentImpl.broadcastWithResponses " +
//                    "return responses");
    return responses;
  }

}

