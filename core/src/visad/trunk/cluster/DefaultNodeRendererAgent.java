//
// DefaultNodeRendererAgent.java
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
import visad.java3d.*;
import java.rmi.*;
import java.util.Vector;
import java.io.Serializable;

/**
   DefaultNodeRendererAgent is the class for agents sent from
   client to nodes to return VisADSceneGraphObjects from
   NodeRendererJ3Ds.<P>
*/
public class DefaultNodeRendererAgent extends NodeAgent {

  /** fields from constructor on client */
  private RemoteDisplay rmtDpy = null; // display on client
  private ConstantMap[] cmaps = null; // ConstantMaps for data on client

  /** fields on node */
  private RemoteNodeDataImpl data = null; // data on node

  /** fields constructed on node */
  private DisplayImplJ3D display = null;
  private NodeDisplayRendererJ3D ndr = null;
  private DataReferenceImpl ref = null;
  private NodeRendererJ3D nr = null;

  public DefaultNodeRendererAgent(RemoteClientAgent source, RemoteDisplay rd,
                                  ConstantMap[] cms) {
    super(source);
    rmtDpy = rd;
    cmaps = cms;
  }

  public void run() {
    Object o = (RemoteNodeDataImpl) getObject();
    if (o == null || !(o instanceof RemoteNodeDataImpl)) {
      System.out.println("DefaultNodeRendererAgent cannot run: " +
                         "object must be RemoteNodeDataImpl " + o);
      return;
    }
    data = (RemoteNodeDataImpl) o;

    if (rmtDpy == null) {
      System.out.println("DefaultNodeRendererAgent cannot run: " +
                         "RemoteDisplay is null ");
      return;
    }

    try {
      // construct collaborative display but without links to
      // data on client; client does not listen to node events;
      // nodes do not listen to client REFERENCE_ADD events;
      // nodes do listen to AUTO_SCALE events
      ndr = new NodeDisplayRendererJ3D();
      display = new DisplayImplJ3D(rmtDpy, ndr, null);

      ref = new DataReferenceImpl("dummy");
      ref.setData(data);
      nr = new NodeRendererJ3D(this);
      display.addReferences(nr, ref, cmaps);
    }
    catch (VisADException e) {
      System.out.println("DefaultNodeRendererAgent cannot run: " + e.toString());
      return;
    }
    catch (RemoteException e) {
      System.out.println("DefaultNodeRendererAgent cannot run: " + e.toString());
      return;
    }

    Thread me = Thread.currentThread();
    while (getAgentThread() == me) {
      Serializable message = getMessage();

      if (message instanceof String &&
          ((String) message).equals("stop")) return;

      Serializable response = null;
      if (message instanceof Vector) {
        Vector vmessage = (Vector) message;
        Object first = vmessage.elementAt(0);
        if (first instanceof ShadowType) {
          // if first element is ShadowType must be computeRanges message
          ShadowType type = (ShadowType) first;
          Object second = vmessage.elementAt(1);
          try {
            if (second instanceof DataShadow) {
              DataShadow shadow = (DataShadow) second;
              response = data.computeRanges(type, shadow);
            }
            else if (second instanceof Integer) {
              int scalar_count = ((Integer) second).intValue();
              response = data.computeRanges(type, scalar_count);
            }
          }
          catch (VisADException e) {
          }
          catch (RemoteException e) {
          }
        }
      }
      if (response == null) response = "error";
      sendToClient(response);
    }
  }

}

