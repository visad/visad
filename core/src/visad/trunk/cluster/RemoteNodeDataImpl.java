//
// RemoteNodeDataImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;
import java.rmi.*;

/**
   RemoteNodeData is the class for cluster node
   VisAD data objects.<P>
*/
public class RemoteNodeDataImpl extends RemoteClusterDataImpl
       implements RemoteNodeData {

  Vector agents = new Vector();

  public RemoteNodeDataImpl() throws RemoteException {
  }

  public RemoteAgentContact sendAgent(NodeAgent agent)
         throws RemoteException {
    synchronized (agents) {
      if (agent.onlyOne()) {
        Class agent_class = agent.getClass();
        int nagents = agents.size();
        for (int i=nagents-1; i>=0; i--) {
          NodeAgent ag = (NodeAgent) agents.elementAt(i);
          if (agent_class.equals(ag.getClass())) {
            agents.removeElementAt(i);
            ag.stop();
          }
        }
      }
      agents.addElement(agent);
    }
    return agent.getRemoteAgentContact(this);
  }

}

