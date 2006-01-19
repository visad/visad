//
// ClientRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

/**
   ClientRendererJ3D is the VisAD DataRenderer for cluster clients
*/
public class ClientRendererJ3D extends DefaultRendererJ3D {

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

  public ClientRendererJ3D () {
    this(10000);
  }

  public ClientRendererJ3D (long to) {
    time_out = to;
  }

  public void setResolutions(int[] rs) {
    if (rs == null) return;
    int n = rs.length;
    resolutions = new int[n];
    for (int i=0; i<n; i++) resolutions[i] = rs[i];
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
          new DisplayException("Data is null: ClientRendererJ3D.doTransform"));
      }
  
      // is this cluster data?
      cluster = (data instanceof RemoteClientDataImpl);

/*
      if (cluster) {
        Set partitionSet = ((RemoteClientDataImpl) data).getPartitionSet();
      }
*/

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
// System.out.println("ClientRendererJ3D.prepareAction messages received");


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
// System.out.println("ClientRendererJ3D.doTransform messages received");

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
        branch.addChild(convertSceneGraph(vsgo));
      }
    }
    if (n == 0) ShadowTypeJ3D.ensureNotEmpty(branch, display);
    return branch;
  }

  private boolean enable_spatial = true;

  public synchronized void setSpatialValues(float[][] spatial_values) {
    if (enable_spatial) super.setSpatialValues(spatial_values);
  }

  /* convert from VisAD scene graph to Java3D scene graph
     and rebuild images and volumes */
  public Node convertSceneGraph(VisADSceneGraphObject scene)
         throws VisADException {
    if (scene instanceof VisADSwitch) {
      VisADSwitch Vswit = (VisADSwitch) scene;
      BranchGroup branch = new BranchGroup();
      branch.setCapability(BranchGroup.ALLOW_DETACH);
      branch.setCapability(Group.ALLOW_CHILDREN_READ);
      branch.setCapability(Group.ALLOW_CHILDREN_WRITE);
      branch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    
      Switch swit = new Switch();
      swit.setCapability(Switch.ALLOW_SWITCH_READ);
      swit.setCapability(Switch.ALLOW_SWITCH_WRITE);
      swit.setCapability(BranchGroup.ALLOW_DETACH);
      swit.setCapability(Group.ALLOW_CHILDREN_READ);
      swit.setCapability(Group.ALLOW_CHILDREN_WRITE);

      int n = Vswit.numChildren();
      Set set = Vswit.getSet();
      // set != null for Animation
      // set == null for volume rendering

      if (set != null) {
        // Switch for Animation or SelectValue
        for (int i=0; i<n; i++) {
          VisADSceneGraphObject vsgo = Vswit.getChild(i);
          swit.addChild((Node) convertSceneGraph(vsgo));
        }

        RealType real = (RealType)
          ((SetType) set.getType()).getDomain().getComponent(0);
        AVControl control = null;
        Vector mapVector = display.getMapVector();
        Enumeration maps = mapVector.elements();
        while (maps.hasMoreElements()) {
          ScalarMap map = (ScalarMap )maps.nextElement();
          if (real.equals(map.getScalar())) {
            DisplayRealType dreal = map.getDisplayScalar();
            if (dreal.equals(Display.Animation) ||
                dreal.equals(Display.SelectValue)) {
              control = (AVControl) map.getControl();
              break;
            }
          } // end if (values != null && && real.equals(map.getScalar()))
        }
        if (control == null) {
          throw new ClusterException("AVControl is null");
        }

        // from ShadowFunctionOrSetTypeJ3D.addSwitch()
        ((AVControlJ3D) control).addPair(swit, set, this);
        ((AVControlJ3D) control).init();
      }
      else { // if (set == null)
        // Switch for volume rendering
        // see visad.java3d.ShadowFunctionOrSetTypeJ3D.textureStackToGroup()
        // and visad.cluster.ShadowNodeFunctionTypeJ3D.textureStackToGroup()
        if (Vswit.numChildren() != 3) {
          throw new ClusterException("VisADSwitch for volume render " +
                                     "must have 3 children");
        }

        VisADGroup VbranchX =
          (VisADGroup) ((VisADSwitch) scene).getChild(0);
        VisADGroup VbranchY =
          (VisADGroup) ((VisADSwitch) scene).getChild(1);
        VisADGroup VbranchZ =
          (VisADGroup) ((VisADSwitch) scene).getChild(2);

        int nX = VbranchX.numChildren();
        OrderedGroup branchX = new OrderedGroup();
        branchX.setCapability(Group.ALLOW_CHILDREN_READ);
        VisADAppearance[] appearanceX = new VisADAppearance[nX];
        for (int i=0; i<nX; i++) {
          VisADAppearance appearance = (VisADAppearance) VbranchX.getChild(i);
          branchX.addChild((Shape3D) convertSceneGraph(appearance));
        }
        OrderedGroup branchXrev = new OrderedGroup();
        branchXrev.setCapability(Group.ALLOW_CHILDREN_READ);
        for (int i=nX-1; i>=0; i--) {
          VisADAppearance appearance = (VisADAppearance) VbranchX.getChild(i);
          branchXrev.addChild((Shape3D) convertSceneGraph(appearance));
        }

        int nY = VbranchY.numChildren();
        OrderedGroup branchY = new OrderedGroup();
        branchY.setCapability(Group.ALLOW_CHILDREN_READ);
        VisADAppearance[] appearanceY = new VisADAppearance[nY];
        for (int i=0; i<nY; i++) {
          VisADAppearance appearance = (VisADAppearance) VbranchY.getChild(i);
          branchY.addChild((Shape3D) convertSceneGraph(appearance));
        }
        OrderedGroup branchYrev = new OrderedGroup();
        branchYrev.setCapability(Group.ALLOW_CHILDREN_READ);
        for (int i=nY-1; i>=0; i--) {
          VisADAppearance appearance = (VisADAppearance) VbranchY.getChild(i);
          branchYrev.addChild((Shape3D) convertSceneGraph(appearance));
        }

        int nZ = VbranchZ.numChildren();
        OrderedGroup branchZ = new OrderedGroup();
        branchZ.setCapability(Group.ALLOW_CHILDREN_READ);
        VisADAppearance[] appearanceZ = new VisADAppearance[nZ];
        for (int i=0; i<nZ; i++) {
          VisADAppearance appearance = (VisADAppearance) VbranchZ.getChild(i);
          branchZ.addChild((Shape3D) convertSceneGraph(appearance));
        }
        OrderedGroup branchZrev = new OrderedGroup();
        branchZrev.setCapability(Group.ALLOW_CHILDREN_READ);
        for (int i=nZ-1; i>=0; i--) {
          VisADAppearance appearance = (VisADAppearance) VbranchZ.getChild(i);
          branchZrev.addChild((Shape3D) convertSceneGraph(appearance));
        }
        swit.addChild(branchX);
        swit.addChild(branchY);
        swit.addChild(branchZ);
        swit.addChild(branchXrev);
        swit.addChild(branchYrev);
        swit.addChild(branchZrev);

        ProjectionControlJ3D control =
          (ProjectionControlJ3D) display.getProjectionControl();
        control.addPair(swit, this);
      }
      branch.addChild(swit);
      return branch;
    }
    else if (scene instanceof VisADGroup) {
      VisADGroup group = (VisADGroup) scene;
      BranchGroup branch = new BranchGroup();
      branch.setCapability(BranchGroup.ALLOW_DETACH);
      branch.setCapability(Group.ALLOW_CHILDREN_READ);
      branch.setCapability(Group.ALLOW_CHILDREN_WRITE);
      branch.setCapability(Group.ALLOW_CHILDREN_EXTEND);

      int n = group.numChildren();
      for (int i=0; i<n; i++) {
        VisADSceneGraphObject vsgo = group.getChild(i);
        branch.addChild((Node) convertSceneGraph(vsgo));
      }
      ShadowTypeJ3D.ensureNotEmpty(branch, display);
      return branch;
    }
    else if (scene instanceof VisADAppearance) {
      VisADAppearance appearance = (VisADAppearance) scene;
      GraphicsModeControl mode = display.getGraphicsModeControl();
      VisADGeometryArray vga = appearance.array;
      GeometryArray array = ((DisplayImplJ3D) display).makeGeometry(vga);
      if (array == null) return null;
      BufferedImage image = null;
      if (appearance.image_pixels != null) {
        image = new BufferedImage(appearance.image_width, appearance.image_height,
                                  appearance.image_type);
        image.setRGB(0, 0, appearance.image_width, appearance.image_height,
                     appearance.image_pixels, 0, appearance.image_width);
/* OR:
        ColorModel colorModel = ColorModel.getRGBdefault();
        WritableRaster raster =
          colorModel.createCompatibleWritableRaster(appearance.image_width, 
                                                    appearance.image_height);
        DataBuffer db = raster.getDataBuffer();
        if (!(db instanceof DataBufferInt)) {
          throw new UnimplementedException("getRGBdefault isn't DataBufferInt");
        }
        image = new BufferedImage(colorModel, raster, false, null);
        int[] intData = ((DataBufferInt)db).getData();
        System.arraycopy(appearance.image_pixels, 0, intData, 0, intData.length);
*/
      }
      if (image != null) {
        // need to do Texture stuff
        Appearance appearance_j3d =
          makeTextureAppearance(appearance, mode, array);

        // create TextureAttributes
        TextureAttributes texture_attributes = new TextureAttributes();
        texture_attributes.setTextureMode(TextureAttributes.MODULATE);
        texture_attributes.setPerspectiveCorrectionMode(
                              TextureAttributes.NICEST);
        appearance_j3d.setTextureAttributes(texture_attributes);

        int transparencyMode = mode.getTransparencyMode();

        Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                                          appearance.texture_width,
                                          appearance.texture_height);
        texture.setCapability(Texture.ALLOW_IMAGE_READ);
        ImageComponent2D image2d =
          new ImageComponent2D(ImageComponent.FORMAT_RGBA, image);
        image2d.setCapability(ImageComponent.ALLOW_IMAGE_READ);
        texture.setImage(0, image2d);
        // this from textureStackToGroup,
        // but not in textureToGroup
        if (transparencyMode == TransparencyAttributes.FASTEST) {
          texture.setMinFilter(Texture.BASE_LEVEL_POINT);
          texture.setMagFilter(Texture.BASE_LEVEL_POINT);
        }
        else {
          texture.setBoundaryModeS(Texture.CLAMP);
          texture.setBoundaryModeT(Texture.CLAMP);
          texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
          texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
        }
        texture.setEnable(true);
        appearance_j3d.setTexture(texture);
        appearance_j3d.setCapability(Appearance.ALLOW_TEXTURE_READ);

        Shape3D shape = new Shape3D(array, appearance_j3d);
        shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
        return shape;
      }
      else {
        Appearance appearance_j3d =
          makeAppearance(appearance, mode, array);
        Shape3D shape = new Shape3D(array, appearance_j3d);
        return shape;
      }
    }
    else {
      throw new VisADException("unknown scene " + scene);
    }
  }

  private Appearance makeTextureAppearance(VisADAppearance appearance,
                           GraphicsModeControl mode, GeometryArray array) {
    TransparencyAttributes c_alpha = null;
    if (appearance.alpha == 1.0f) {
      // constant opaque alpha = NONE
      c_alpha = null;
    }
    else if (appearance.alpha == appearance.alpha) {
      c_alpha = new TransparencyAttributes(TransparencyAttributes.BLENDED,
                                           appearance.alpha);
    }
    else {
      c_alpha = new TransparencyAttributes();
      c_alpha.setTransparencyMode(TransparencyAttributes.BLENDED);
    }
    ColoringAttributes c_color = null;
    if (appearance.red == appearance.red &&
        appearance.green == appearance.green &&
        appearance.blue == appearance.blue) {
      c_color = new ColoringAttributes();
      c_color.setColor(appearance.red, appearance.green, appearance.blue);
    }
    return ShadowTypeJ3D.staticMakeAppearance(mode, c_alpha, null,
                                              array, false);
  }

  private Appearance makeAppearance(VisADAppearance appearance,
                           GraphicsModeControl mode, GeometryArray array) {
    TransparencyAttributes c_alpha = null;
    if (appearance.alpha == 1.0f) {
      // constant opaque alpha = NONE
      c_alpha = new TransparencyAttributes(TransparencyAttributes.NONE, 0.0f);
    }
    else if (appearance.alpha == appearance.alpha) {
      c_alpha = new TransparencyAttributes(mode.getTransparencyMode(),
                                           appearance.alpha);
    }
    else {
      c_alpha = new TransparencyAttributes(mode.getTransparencyMode(), 0.0f);
    }
    ColoringAttributes c_color = null;
    if (appearance.red == appearance.red &&
        appearance.green == appearance.green &&
        appearance.blue == appearance.blue) {
      c_color = new ColoringAttributes();
      c_color.setColor(appearance.red, appearance.green, appearance.blue);
    }
    return ShadowTypeJ3D.staticMakeAppearance(mode, c_alpha, c_color,
                                              array, false);
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
// System.out.println("ClientRendererJ3D.computeRanges messages received");
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

  public Object clone() {
    return new ClientRendererJ3D(time_out);
  }

  public static void main(String args[])
         throws VisADException, RemoteException {

    DisplayImpl display =
      new DisplayImplJ3D("display", new ClientDisplayRendererJ3D());

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
    panel.add(display.getComponent());

    // set size of JFrame and make it visible
    frame.setSize(500, 500);
    frame.setVisible(true);
  }

}

