//
// ClientRendererJ3D.java
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
import visad.java2d.*;

import javax.media.j3d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.rmi.*;
import java.io.Serializable;

/**
   ClientRendererJ3D is the VisAD DataRenderer for cluster clients
*/
public class ClientRendererJ3D extends DefaultRendererJ3D {

  DisplayImpl display = null;
  RemoteDisplay rdisplay = null;
  ConstantMap[] cmaps = null;

  DataDisplayLink link = null;
  Data data = null;
  ClientDisplayRendererJ3D cdr = null;
  boolean cluster = true;

  RemoteClientRendererAgentImpl[] agents = null;
  RemoteAgentContact[] contacts = null;

  public ClientRendererJ3D () {
  }

  public DataShadow prepareAction(boolean go, boolean initialize,
                                  DataShadow shadow)
         throws VisADException, RemoteException {

    Data old_data = data;
    DataDisplayLink[] Links = getLinks();
    if (Links != null && Links.length > 0) {
      link = Links[0];

      // initialize rdisplay and cmaps if not already
      if (rdisplay == null) {
        display = getDisplay();
        rdisplay = new RemoteDisplayImpl(display);
        Vector cvector = link.getConstantMaps();
        if (cvector != null && cvector.size() > 0) {
          int clength = cvector.size();
          cmaps = new ConstantMap[clength];
          for (int i=0; i<clength; i++) {
            cmaps[i] = (ConstantMap) cvector.elementAt(i);
          }
        }
        cdr = (ClientDisplayRendererJ3D) display.getDisplayRenderer();
      }

      // get the data
      try {
        data = link.getData();
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
          new DisplayException("Data is null: ClientRendererJ3D.doTransform"));
      }
  
      // is this cluster data?
      cluster = (data instanceof RemoteClientDataImpl);

      if (cluster && data != old_data) {
        // send agents to nodes if data changed
        RemoteClusterData[] jvmTable = ((RemoteClientDataImpl) data).getTable();
        int nagents = jvmTable.length - 1;
        agents = new RemoteClientRendererAgentImpl[nagents];
        contacts = new RemoteAgentContact[nagents];
        for (int i=0; i<nagents; i++) {
          agents[i] = new RemoteClientRendererAgentImpl(this);
          DefaultNodeRendererAgent node_agent =
            new DefaultNodeRendererAgent(agents[i], rdisplay, cmaps);
          contacts[i] = ((RemoteNodeData) jvmTable[i]).sendAgent(node_agent);
        }
      }
    }
    // now do usual prepareAction()
    return super.prepareAction(go, initialize, shadow);
  }

  /** create a scene graph for Data in links[0] */
  public BranchGroup doTransform() throws VisADException, RemoteException {
    if (link == null || data == null) {
      addException(
        new DisplayException("Data is null: ClientRendererJ3D.doTransform"));
    }

    if (!cluster) {
      // not cluster data, so just do the usual
      return super.doTransform();
    }

/*
    ShadowTypeJ3D type = (ShadowTypeJ3D) link.getShadow();

    // initialize valueArray to missing
    int valueArrayLength = getDisplay().getValueArrayLength();
    float[] valueArray = new float[valueArrayLength];
    for (int i=0; i<valueArrayLength; i++) {
      valueArray[i] = Float.NaN;
    }

    link.start_time = System.currentTimeMillis();
    link.time_flag = false;
    type.preProcess();

    boolean post_process;
    try {
      // transform data into a depiction under branch
      post_process = type.doTransform(branch, data, valueArray,
                                      link.getDefaultValues(), this);
    } catch (RemoteException re) {
      if (visad.collab.CollabUtil.isDisconnectException(re)) {
        getDisplay().connectionFailed(this, link);
        removeLink(link);
        return null;
      }
      throw re;
    }

    if (post_process) type.postProcess(branch);

    link.clearData();
*/

    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(Group.ALLOW_CHILDREN_READ);
    branch.setCapability(Group.ALLOW_CHILDREN_WRITE);
    branch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    return branch;
  }

  public DataShadow computeRanges(Data data, ShadowType type, DataShadow shadow) 
         throws VisADException, RemoteException {
    if (!cluster) {
      return super.computeRanges(data, type, shadow);
    }
    int nagents = agents.length;
    for (int i=0; i<nagents; i++) {
      // note ShadowType and DataShadow are both Serializable
      // message should really be (computeRanges, type, shadow)
      contacts[i].sendToNode(null);
    }

    if (shadow == null) {
      shadow =
        data.computeRanges(type, getDisplay().getScalarCount());
    }
    else {
      shadow = data.computeRanges(type, shadow);
    }
    return shadow;
  }

  public static void main(String args[])
         throws VisADException, RemoteException {

    DisplayImpl display =
      new DisplayImplJ3D("display", new ClientDisplayRendererJ3D(),
                         DisplayImplJ3D.TRANSFORM_ONLY);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test ClientRendererJ3D");
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
    // panel.add(display.getComponent());

    // set size of JFrame and make it visible
    frame.setSize(500, 500);
    frame.setVisible(true);
  }

}

