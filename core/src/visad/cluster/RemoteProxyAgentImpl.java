//
// RemoteProxyAgentImpl.java
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

package visad.cluster;

import visad.*;
import java.util.Vector;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;

/**
   RemoteProxyAgentImpl is the class for agents on proxy clients
*/
public class RemoteProxyAgentImpl extends UnicastRemoteObject
       implements RemoteProxyAgent {

  private RemoteClientDataImpl rcdi = null;

  private boolean sent_node_agents = false;

  private RemoteClientAgentImpl[] agents = null;
  private RemoteClientAgentImpl focus_agent = null;
  private RemoteAgentContact[] contacts = null;

  private int[] resolutions = null;

  private ConstantMap[] cmaps = null;
  private ScalarMap[] maps = null;
  private Control[] controls = null;

  public RemoteProxyAgentImpl(RemoteClientDataImpl r) throws RemoteException {
    rcdi = r;
  }

  public RemoteClientData getRemoteClientData() throws RemoteException {
    return rcdi;
  }

  public void setResolutions(int[] rs) {
    if (rs == null) return;
    int n = rs.length;
    resolutions = new int[n];
    for (int i=0; i<n; i++) resolutions[i] = rs[i];
  }

  public Serializable[] prepareAction(boolean go, boolean initialize,
                                  DataShadow shadow, ConstantMap[] cms,
                                  ScalarMap[] ms, Control[] cos,
                                  String name, long time_out)
         throws VisADException, RemoteException {

    cmaps = cms;
    maps = ms;
    controls = cos;

    if (!sent_node_agents) {
      // send agents to nodes if data changed
      focus_agent = new RemoteClientAgentImpl(null, -1, time_out);
      RemoteClusterData[] jvmTable = rcdi.getTable();
      int nagents = jvmTable.length - 1;
      agents = new RemoteClientAgentImpl[nagents];
      contacts = new RemoteAgentContact[nagents];
      for (int i=0; i<nagents; i++) {
        agents[i] = new RemoteClientAgentImpl(focus_agent, i);
        DefaultNodeRendererAgent node_agent =
          new DefaultNodeRendererAgent(agents[i], name, cmaps);
        contacts[i] = ((RemoteNodeData) jvmTable[i]).sendAgent(node_agent);
      }
      sent_node_agents = true;
    }

    Vector message = new Vector();
    for (int i=0; i<maps.length; i++) {
      message.addElement(maps[i]);
      message.addElement(controls[i]);
    }
    Serializable[] responses =
      focus_agent.broadcastWithResponses(message, contacts); // PROXY
// System.out.println("RemoteProxyAgentImpl.prepareAction messages received");
    return responses;
  }


  public Serializable[] doTransform() throws VisADException, RemoteException {

    if (rcdi == null) {
      throw new DisplayException("Data is null");
    }

    int n = contacts.length;
    Vector[] messages = new Vector[n];

    if (resolutions == null || resolutions.length != n) {
      resolutions = new int[n];
      for (int i=0; i<n; i++) resolutions[i] = 1;
    }

    for (int i=0; i<n; i++) {
      // String message = "transform";
      messages[i] = new Vector();
      messages[i].addElement("transform");

      messages[i].addElement(new Integer(resolutions[i]));

      int m = maps.length;
      for (int j=0; j<m; j++) {
        messages[i].addElement(maps[j]);
      }
    }

    // responses are VisADGroups
    Serializable[] responses =
      focus_agent.broadcastWithResponses(messages, contacts);
// System.out.println("ProxyRendererJ3D.doTransform messages received");

    return responses;
  }

  public Serializable[] computeRanges(Vector message)
         throws VisADException, RemoteException {

    Serializable[] responses =
      focus_agent.broadcastWithResponses(message, contacts);
// System.out.println("RemoteProxyAgentImpl.computeRanges messages received");
    return responses;
  }

}

