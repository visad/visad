//
// UserRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

/*      PROXY

UserRendererJ3D like ClientRendererJ3D, but ?

  ClientRendererJ3D <--> nodes

or

  UserRendererJ3D   <--> RemoteProxyAgentImpl <--> nodes
                         RemoteProxyAgent

no Java3D on nodes or proxy
RemoteClientDataImpl on Client or Proxy, not on User
UserDummyDataImpl extends DataImpl
  getType() from adaptedRemoteClientData
  RemoteCellImpl triggered by adaptedRemoteClientData
      calls notifyReferences()

UserDisplayRendererJ3D extends DefaultDisplayRendererJ3D
  like ClientDisplayRendererJ3D

*/

package visad.cluster;

import visad.*;
import visad.java3d.*;

import javax.media.j3d.*;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;
import java.io.Serializable;

/**
   UserRendererJ3D is the VisAD DataRenderer for remote users
   connecting to a cluster via a proxy on the client.
*/
public class UserRendererJ3D extends DefaultRendererJ3D {

  // no UserDummyDataImpl variable
  // data access via RemoteProxyAgent
  private RemoteProxyAgent agent = null;

  private DisplayImpl display = null;
  private ConstantMap[] cmaps = null;

  private DataDisplayLink link = null;

  private long time_out = 10000;

  private int[] resolutions = null;

  public UserRendererJ3D() {
    this(null, 10000);
  }

  public UserRendererJ3D(RemoteProxyAgent a) {
    this(a, 10000);
  }

  public UserRendererJ3D(RemoteProxyAgent a, long to) {
    agent = a;
    time_out = to;
  }

  public void setResolutions(int[] rs) throws RemoteException {
    try {
      agent.setResolutions(rs);
    }
    catch (RemoteException re) {
      if (visad.collab.CollabUtil.isDisconnectException(re)) {
        getDisplay().connectionFailed(this, link);
        removeLink(link);
        return;
      }
      throw re;
    }
  }

  public DataShadow prepareAction(boolean go, boolean initialize,
                                  DataShadow shadow)
         throws VisADException, RemoteException {

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
    }

    Vector map_vector = display.getMapVector();
    int n = map_vector.size();
    ScalarMap[] maps = new ScalarMap[n];
    Control[] controls = new Control[n];
    for (int i=0; i<n; i++) {
      maps[i] = (ScalarMap) map_vector.elementAt(i);
      controls[i] = maps[i].getControl();
    }

    Serializable[] responses =
      agent.prepareAction(go, initialize, shadow, cmaps, maps, controls,
                          display.getName(), time_out);

    // now do usual prepareAction()
    return super.prepareAction(go, initialize, shadow);
  }

  /** create a scene graph for Data in links[0] */
  public BranchGroup doTransform() throws VisADException, RemoteException {

    Serializable[] responses = null;
    try {
      // responses are VisADGroups
      responses = agent.doTransform();
    }
    catch (DisplayException e) {
      addException(e);
    }
// System.out.println("UserRendererJ3D.doTransform messages received");

    if (link == null) {
      addException(
        new DisplayException("Data is null: UserRendererJ3D.doTransform"));
      responses = null;
    }

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

    int n = (responses == null) ? 0 : responses.length;
    for (int i=0; i<n; i++) {
      if (responses[i] != null) {
        VisADSceneGraphObject vsgo = (VisADSceneGraphObject) responses[i];
        branch.addChild(convertSceneGraph(vsgo));
      }
    }
    if (n == 0) ShadowTypeJ3D.ensureNotEmpty(branch, display);
    return branch;
  }

  public void setSpatialValues(float[][] spatial_values) {
    super.setSpatialValues(spatial_values);
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

    Vector message = new Vector();
    message.addElement(type);
    if (shadow == null) {
      message.addElement(new Integer(getDisplay().getScalarCount()));
    }
    else {
      message.addElement(shadow);
    }
// PROXY: Vector of ShadowType, (Integer or DataShadow)
    Serializable[] responses =
      agent.computeRanges(message);
// System.out.println("UserRendererJ3D.computeRanges messages received");
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
    return new UserRendererJ3D(agent, time_out);
  }

  public static void main(String args[])
         throws VisADException, RemoteException {

    DisplayImpl display =
      new DisplayImplJ3D("display", new ClientDisplayRendererJ3D());

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test UserRendererJ3D");
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

