//
// DefaultNodeRendererAgent.java
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
import visad.java3d.*;
import java.rmi.*;
import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;

/**
   DefaultNodeRendererAgent is the class for agents sent from
   client to nodes to return VisADSceneGraphObjects from
   NodeRendererJ3Ds.<P>
*/
public class DefaultNodeRendererAgent extends NodeAgent {

  /** fields from constructor on client */
  private String rmtDpyName = null; // display name on client
  private ConstantMap[] cmaps = null; // ConstantMaps for data on client

  /** fields on node */
  private RemoteNodeDataImpl data = null; // data on node

  /** fields constructed on node */
  private DisplayImplJ3D display = null;
  private NodeDisplayRendererJ3D ndr = null;
  private DataReferenceImpl ref = null;
  private NodeRendererJ3D nr = null;
  private RemoteDisplayImpl remote_display = null;

  public DefaultNodeRendererAgent(RemoteClientAgent source, String name,
                                  ConstantMap[] cms) {
    super(source);
    rmtDpyName = name;
    if (rmtDpyName == null) rmtDpyName = "null";
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

    try {
      // construct collaborative display but without links to
      // data on client; client does not listen to node events;
      // nodes do not listen to client REFERENCE_ADD events;
      // nodes do listen to AUTO_SCALE events
      ndr = new NodeDisplayRendererJ3D();
// System.out.println("DefaultNodeRendererAgent.run after new NodeDisplayRendererJ3D");

      String name = rmtDpyName + ".remote";
      display = new DisplayImplJ3D(name, ndr, DisplayImplJ3D.TRANSFORM_ONLY);
// System.out.println("DefaultNodeRendererAgent.run after new DisplayImplJ3D");

      ref = new DataReferenceImpl("dummy");
      RemoteDataReferenceImpl remote_ref = new RemoteDataReferenceImpl(ref);
      remote_ref.setData(data);
// System.out.println("DefaultNodeRendererAgent.run after setData");
      nr = new NodeRendererJ3D(this);
      remote_display = new RemoteDisplayImpl(display);
// System.out.println("DefaultNodeRendererAgent.run after new RemoteDisplayImpl");


      remote_display.addReferences(nr, ref, cmaps);
// System.out.println("DefaultNodeRendererAgent.run after addReferences");


    }
    catch (VisADException e) {
      DisplayImpl.printStack("ex " + e);
      return;
    }
    catch (RemoteException e) {
      DisplayImpl.printStack("ex " + e);
      return;
    }

    Thread me = Thread.currentThread();
// System.out.println("DefaultNodeRendererAgent.run " + me + " " + getAgentThread());
    while (getAgentThread() == me) {
// System.out.println("DefaultNodeRendererAgent.run before getMessage call");
      Serializable message = getMessage();

      Serializable response = null;
      if (message instanceof String) {
        String smessage = (String) message;
        if (smessage.equals("stop")) {
          return;
        }
        else if (smessage.equals("transform")) {
// System.out.println("DefaultNodeRendererAgent.run trigger " + display.getName());
          nr.enableTransform();
          display.reDisplayAll();
          // NodeRendererJ3D.doTransform() calls
          // sendToClient(branch) for this, so no response
          response = "none";
        }
      }
      else if (message instanceof Vector) {
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
            DisplayImpl.printStack("ex " + e);
            return;
          }
          catch (RemoteException e) {
            DisplayImpl.printStack("ex " + e);
            return;
          }
        }
        else if (first instanceof ScalarMap) {
// System.out.println("DefaultNodeRendererAgent.run first is ScalarMap");
          try {
            display.removeReference(ref);
            display.clearMaps();
            int m = vmessage.size();
            for (int i=0; i<m; i+=2) {
              ScalarMap map = (ScalarMap) vmessage.elementAt(i);
              Control control = (Control) vmessage.elementAt(i + 1);
              ScalarMap new_map =
                new ScalarMap(map.getScalar(), map.getDisplayScalar());
              display.addMap(new_map);
              double[] range = map.getRange();
              if (!Display.Animation.equals(new_map.getDisplayScalar())) {
                new_map.setRange(range[0], range[1]);
              }
              Control new_control = new_map.getControl();
              if (new_control != null) new_control.syncControl(control);
            }
            nr = new NodeRendererJ3D(this);
            remote_display.addReferences(nr, ref, cmaps);
          }
          catch (VisADException e) {
            DisplayImpl.printStack("ex " + e);
            return;
          }
          catch (RemoteException e) {
            DisplayImpl.printStack("ex " + e);
            return;
          }
          response = "normal";
// System.out.println("DefaultNodeRendererAgent.run ScalarMap response");
        }
        else if (first instanceof String) {
          String sfirst = (String) first;
          if (sfirst.equals("transform")) {
            display.disableAction();
            int resolution = ((Integer) vmessage.elementAt(1)).intValue();
            nr.setResolution(resolution);
            int m = vmessage.size();
            Vector map_vector = display.getMapVector();
            if (map_vector.size() != (m - 2)) {
              System.out.println("ERROR1 " + map_vector.size() +
                                 " != " + (m - 2));
              return;
            }
            Enumeration maps = map_vector.elements();
            for (int i=2; i<m; i++) {
              ScalarMap map1 = (ScalarMap) vmessage.elementAt(i);
              ScalarMap map2 = (ScalarMap) maps.nextElement();
              if (!map1.getScalar().equals(map2.getScalar()) ||
                  !map1.getDisplayScalar().equals(map2.getDisplayScalar()) ) {
                System.out.println("ERROR2 " + map1 + " != " + map2);
              }
              double[] range = map1.getRange();
              if (!Display.Animation.equals(map2.getDisplayScalar()) &&
                  !Display.IsoContour.equals(map2.getDisplayScalar())) {
                try {
                  map2.setRange(range[0], range[1]);
                }
                catch (VisADException e) {
                  DisplayImpl.printStack("ex " + e);
                  return;
                }
                catch (RemoteException e) {
                  DisplayImpl.printStack("ex " + e);
                  return;
                }
              }
            }
            nr.enableTransform();
            display.reDisplayAll();
            display.enableAction();
            // NodeRendererJ3D.doTransform() calls
            // sendToClient(branch) for this, so no reponse
            response = "none";
          }
        }
      }
      if (response == null) response = "error";
      if (!response.equals("none")) sendToClient(response);
    } // end while (getAgentThread() == me)
  }

}

