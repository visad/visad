//
// ProxyRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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
import visad.java2d.*;
import visad.util.*;

import javax.media.j3d.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;
import java.io.Serializable;

// ****    ****    **** ELIMINATE THIS ****    ****    ****
// ****    ****    **** ELIMINATE THIS ****    ****    ****
// ****    ****    **** ELIMINATE THIS ****    ****    ****
// ****    ****    **** ELIMINATE THIS ****    ****    ****
// ****    ****    **** ELIMINATE THIS ****    ****    ****
// ****    ****    **** ELIMINATE THIS ****    ****    ****
// ****    ****    **** ELIMINATE THIS ****    ****    ****
// ****    ****    **** ELIMINATE THIS ****    ****    ****
// ****    ****    **** ELIMINATE THIS ****    ****    ****
// ****    ****    **** ELIMINATE THIS ****    ****    ****
// ****    ****    **** ELIMINATE THIS ****    ****    ****
// ****    ****    **** ELIMINATE THIS ****    ****    ****

/**
   ProxyRendererJ3D is the VisAD DataRenderer for proxy clients
*/
public class ProxyRendererJ3D extends DefaultRendererJ3D {

  private DisplayImpl display = null;
  private ConstantMap[] cmaps = null;

  private DataDisplayLink link = null;
  private Data data = null;
  private boolean cluster = true;

  private RemoteClientAgentImpl[] agents = null;
  private RemoteClientAgentImpl focus_agent = null;
  private RemoteAgentContact[] contacts = null;

  private long time_out = 10000;

  private int[] resolutions = null;

  public ProxyRendererJ3D () {
    this(10000);
  }

  public ProxyRendererJ3D (long to) {
    time_out = to;
  }

  public void setResolutions(int[] rs) {
    if (rs == null) return;
    int n = rs.length;
    resolutions = new int[n];
    for (int i=0; i<n; i++) resolutions[i] = rs[i];
  }

  // dummy
  public boolean getBadScale(boolean anyBadMap) {
    return false;
  }

  // dummy
  public boolean doAction() {
    return true;
  }

  public DataShadow prepareAction(boolean go, boolean initialize,
                                  DataShadow shadow)
         throws VisADException, RemoteException {

    Data old_data = data;
    DataDisplayLink[] Links = getLinks();
    if (Links != null && Links.length > 0) {
      link = Links[0];

      // initialize cmaps if not already
      if (cmaps == null) {
        display = getDisplay();
        Vector cvector = link.getConstantMaps();
        if (cvector != null && cvector.size() > 0) {
          int clength = cvector.size();
          cmaps = new ConstantMap[clength];
          for (int i=0; i<clength; i++) {
            cmaps[i] = (ConstantMap) cvector.elementAt(i);
          }
        }
      }

      // get the data
      try {
        data = link.getData(); // PROXY
      } catch (RemoteException re) {
        if (visad.collab.CollabUtil.isDisconnectException(re)) {
          getDisplay().connectionFailed(this, link);
          removeLink(link);
          return null;
        }
        throw re;
      }
      if (data == null) {
        addException(
          new DisplayException("Data is null: ProxyRendererJ3D.doTransform"));
      }
  
      // is this cluster data?
      cluster = (data instanceof RemoteClientDataImpl);

      if (cluster && data != old_data) { // PROXY
        // send agents to nodes if data changed
        RemoteClientDataImpl rcdi = (RemoteClientDataImpl) data;
        focus_agent = new RemoteClientAgentImpl(null, -1, time_out);
        RemoteClusterData[] jvmTable = rcdi.getTable();
        int nagents = jvmTable.length - 1;
        agents = new RemoteClientAgentImpl[nagents];
        contacts = new RemoteAgentContact[nagents];
        for (int i=0; i<nagents; i++) {
          agents[i] = new RemoteClientAgentImpl(focus_agent, i);
          DefaultNodeRendererAgent node_agent =
            new DefaultNodeRendererAgent(agents[i], display.getName(), cmaps);
          contacts[i] = ((RemoteNodeData) jvmTable[i]).sendAgent(node_agent);
        }
      }
    }



// WLH new 16 April 2001
    Vector message = new Vector();
    Vector map_vector = display.getMapVector();
    Enumeration maps = map_vector.elements();
    while (maps.hasMoreElements()) {
      ScalarMap map = (ScalarMap) maps.nextElement();
      message.addElement(map);
      message.addElement(map.getControl());
    }
    Serializable[] responses =
      focus_agent.broadcastWithResponses(message, contacts); // PROXY
// System.out.println("ProxyRendererJ3D.prepareAction messages received");


    // now do usual prepareAction()
    return super.prepareAction(go, initialize, shadow);
  }

  /** create a scene graph for Data in links[0] */
  public BranchGroup doTransform() throws VisADException, RemoteException {
    if (link == null || data == null) {
      addException(
        new DisplayException("Data is null: ProxyRendererJ3D.doTransform"));
    }

    if (!cluster) {
      // not cluster data, so just do the usual
      return super.doTransform(); // PROXY (do this on user but not on client)
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

      Vector map_vector = display.getMapVector();
      Enumeration maps = map_vector.elements();
      while (maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        messages[i].addElement(map);
      }
    }

    // responses are VisADGroups
    Serializable[] responses =
      focus_agent.broadcastWithResponses(messages, contacts);
// System.out.println("ProxyRendererJ3D.doTransform messages received");

    // responses are VisADGroups
    // need to:
    // 1. rebuild images and volumes
    // 2. convert from VisADGroups to BranchGroups
    //    GeometryArray = display.makeGeometry(VisADGeometryArray)
    // 3. add them as children of branch

    // link.clearData(); ????

    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(Group.ALLOW_CHILDREN_READ);
    branch.setCapability(Group.ALLOW_CHILDREN_WRITE);
    branch.setCapability(Group.ALLOW_CHILDREN_EXTEND);

    n = responses.length;
    for (int i=0; i<n; i++) {
      if (responses[i] != null) {
        VisADSceneGraphObject vsgo = (VisADSceneGraphObject) responses[i];
        // branch.addChild(convertSceneGraph(vsgo));
      }
    }
    // if (n == 0) ShadowTypeJ3D.ensureNotEmpty(branch, display);
    return branch;
  }

  private boolean enable_spatial = true;

  public synchronized void setSpatialValues(float[][] spatial_values) {
    if (enable_spatial) super.setSpatialValues(spatial_values);
  }

  public DataShadow computeRanges(Data data, ShadowType type, DataShadow shadow) 
         throws VisADException, RemoteException {
    if (!cluster) {
      return super.computeRanges(data, type, shadow);
    }

    DataShadow[] shadows = null;
    Vector message = new Vector();
    message.addElement(type);
    if (shadow == null) {
      message.addElement(new Integer(getDisplay().getScalarCount()));
      // shadow =
      //   data.computeRanges(type, getDisplay().getScalarCount());
    }
    else {
      message.addElement(shadow);
      // shadow = data.computeRanges(type, shadow);
    }
    Serializable[] responses =
      focus_agent.broadcastWithResponses(message, contacts);
// System.out.println("ProxyRendererJ3D.computeRanges messages received");
    DataShadow new_shadow = null;
    int n = responses.length;
    for (int i=0; i<n; i++) {
      if (responses[i] != null) {
        if (new_shadow == null) {
          new_shadow = (DataShadow) responses[i];
        }
        else {
          new_shadow.merge((DataShadow) responses[i]);
        }
      }
    }
    return new_shadow;
  }

  public static void main(String args[])
         throws VisADException, RemoteException {

    DisplayImpl display =
      new DisplayImplJ3D("display", new ClientDisplayRendererJ3D());

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test ProxyRendererJ3D");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    // add display to JPanel
    panel.add(display.getComponent());

    // set size of JFrame and make it visible
    frame.setSize(500, 500);
    frame.setVisible(true);
  }

}

