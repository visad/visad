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
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.Vector;
import java.rmi.*;
import java.io.Serializable;

/**
   ClientRendererJ3D is the VisAD DataRenderer for cluster clients
*/
public class ClientRendererJ3D extends DefaultRendererJ3D {

  private DisplayImpl display = null;
  private RemoteDisplay rdisplay = null;
  private ConstantMap[] cmaps = null;

  private DataDisplayLink link = null;
  private Data data = null;
  private ClientDisplayRendererJ3D cdr = null;
  private boolean cluster = true;

  private RemoteClientRendererAgentImpl[] agents = null;
  private RemoteClientAgentImpl focus_agent = null;
  private RemoteAgentContact[] contacts = null;

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
        RemoteClientDataImpl rcdi = (RemoteClientDataImpl) data;
        focus_agent = new RemoteClientAgentImpl(null, -1);
        RemoteClusterData[] jvmTable = rcdi.getTable();
        int nagents = jvmTable.length - 1;
        agents = new RemoteClientRendererAgentImpl[nagents];
        contacts = new RemoteAgentContact[nagents];
        for (int i=0; i<nagents; i++) {
          agents[i] = new RemoteClientRendererAgentImpl(focus_agent, i);
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

    String message = "transform";
    Serializable[] responses =
      focus_agent.broadcastWithResponses(message, contacts);

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
    return branch;
  }

  public SceneGraphObject convertSceneGraph(VisADSceneGraphObject scene)
         throws VisADException {
    if (scene instanceof VisADSwitch) {
      VisADSwitch swit = (VisADSwitch) scene;
      Set set = swit.getSet();
      int n = swit.numChildren();

    }
    else if (scene instanceof VisADGroup) {

      // see ShadowTypeJ3D.ensureNotEmpty(Group)
    }
    else { // scene instanceof VisADAppearance
      VisADAppearance appearance = (VisADAppearance) scene;
      VisADGeometryArray vga = appearance.array;
      GeometryArray array = ((DisplayImplJ3D) display).makeGeometry(vga);
      if (array == null) return null;
      BufferedImage image = (BufferedImage) appearance.image;
      if (image != null) {
      }
      else {
      }
    }
return null;
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
      shadow =
        data.computeRanges(type, getDisplay().getScalarCount());
    }
    else {
      message.addElement(shadow);
      shadow = data.computeRanges(type, shadow);
    }
    Serializable[] responses =
      focus_agent.broadcastWithResponses(message, contacts);
    DataShadow new_shadow = (DataShadow) responses[0];
    int n = responses.length;
    for (int i=1; i<n; i++) {
      new_shadow.merge((DataShadow) responses[i]);
    }
    return new_shadow;
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

