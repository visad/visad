//
// AnimationRendererJ3D.java
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

package visad.java3d;

import visad.*;
import visad.util.Delay;

import javax.media.j3d.*;

import java.rmi.*;
import java.io.IOException;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

public class AnimationRendererJ3D extends DefaultRendererJ3D {

  boolean animation1D;
  String  nameMappedToAnimation = null;

  private boolean reUseFrames = false;

  private boolean setSetOnReUseFrames = true;

  public ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowAnimationFunctionTypeJ3D(type, link, parent);
  }

  public void setReUseFrames(boolean reuse) {
    reUseFrames = reuse;
  }

  public void setReUseFrames() {
    setReUseFrames(true);
  }

  public boolean getReUseFrames() {
    return reUseFrames;
  }

  public void setSetSetOnReUseFrames(boolean ss) {
    setSetOnReUseFrames = ss;
  }

  public boolean getSetSetOnReUseFrames() {
    return setSetOnReUseFrames;
  }

  // logic to 'mark' missing frames
  private VisADBranchGroup vbranch = null;

  public void clearScene() {
    vbranch = null;
    super.clearScene();
  }

  void setVisADBranch(VisADBranchGroup branch) {
    vbranch = branch;
  }

  void markMissingVisADBranch() {
    if (vbranch != null) vbranch.scratchTime();
  }
  // end of logic to 'mark' missing frames

  public BranchGroup doTransform() throws VisADException, RemoteException {
    BranchGroup branch = getBranch();
    if (branch == null) {
      branch = new BranchGroup();
      branch.setCapability(BranchGroup.ALLOW_DETACH);
      branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
      branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      branch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
    }

    DataDisplayLink[] Links = getLinks();
    if (Links == null || Links.length == 0) {
      return null;
    }

    DataDisplayLink link = Links[0];
    ShadowTypeJ3D type = (ShadowTypeJ3D) link.getShadow();

    // initialize valueArray to missing
    int valueArrayLength = getDisplay().getValueArrayLength();
    float[] valueArray = new float[valueArrayLength];
    for (int i=0; i<valueArrayLength; i++) {
      valueArray[i] = Float.NaN;
    }

    Data data;
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
      branch = null;
      addException(
        new DisplayException("Data is null: AnimationRendererJ3D.doTransform"));
    }
    else {
      animation1D = false;
      MathType mtype = link.getType();
      if (mtype instanceof FunctionType) {
        FunctionType function = (FunctionType) mtype;
        RealTupleType functionD = function.getDomain();
        Vector scalarMaps = link.getSelectedMapVector();
        for (int kk = 0; kk < scalarMaps.size(); kk++) {
          ScalarMap scalar_map = (ScalarMap)scalarMaps.elementAt(kk);
          String scalar_name = scalar_map.getScalarName();
          if (scalar_name.equals(((RealType)functionD.getComponent(0)).getName())) {
            if (((scalar_map.getDisplayScalar()).equals(Display.Animation))&&
                 (functionD.getDimension() == 1)) {
              animation1D = true;
              nameMappedToAnimation = scalar_name;
            }
          }
        }
      }
     
      link.start_time = System.currentTimeMillis();
      link.time_flag = false;
      vbranch = null;


      if (!animation1D) {
        // TDR, 3-2003: 
        // make sure branch not live for default logic, ie. super.doTransform()
        branch = new BranchGroup();
        branch.setCapability(BranchGroup.ALLOW_DETACH);
        branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        branch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
      }

      // transform data into a depiction under branch
      try {
        type.doTransform(branch, data, valueArray,
                         link.getDefaultValues(), this);
      } catch (RemoteException re) {
        if (visad.collab.CollabUtil.isDisconnectException(re)) {
          getDisplay().connectionFailed(this, link);
          removeLink(link);
          return null;
        }
        throw re;
      }
    }
    link.clearData();
    return branch;
  }

  public Object clone() {
    return new AnimationRendererJ3D();
  }

  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    String test = "new";
    if (args.length > 0) {
      test = args[0];
      if (!(test.equals("new") || test.equals("old"))) {
        System.out.println("arg must be 'old' or 'new'");
        System.exit(0);
      }
    }
    
    int size = 80;
    int nr = size;
    int nc = size;
    int nz = size;
    double ang = 2*Math.PI/nr;

    RealType[] types = {RealType.Latitude, RealType.Longitude, RealType.Altitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType radiance = RealType.getRealType("radiance", null, null);
    RealType index    = RealType.getRealType("index", null, null);
    FunctionType image_type = new FunctionType(earth_location, radiance);

    Integer3DSet image_domain_set =
      new Integer3DSet(RealTupleType.SpatialCartesian3DTuple, nr, nc, nz);
    FunctionType field_type = new FunctionType(index, image_type);
    Integer1DSet field_domain_set = new Integer1DSet(index, 6);
    FieldImpl field = new FieldImpl(field_type, field_domain_set);

    for (int tt = 0; tt < field_domain_set.getLength(); tt++) {

    float[][] values = new float[1][nr*nc*nz];
    for ( int kk = 0; kk < nz; kk++) {
      for ( int jj = 0; jj < nc; jj++ ) {
        for ( int ii = 0; ii < nr; ii++ ) {
          int idx = kk*nr*nc + jj*nr + ii;
          values[0][idx] =
            (tt+1)*(2f*((float)Math.sin(2*ang*ii)) + 2f*((float)Math.sin(2*ang*jj))) + kk;
        }
      }
    }
    FlatField image = new FlatField(image_type, image_domain_set);
    image.setSamples(values);
    field.setSample(tt, image, false);
    }

    DisplayImplJ3D dpys = new DisplayImplJ3D("AnimationRendererJ3D Test");

    JFrame jframe  = new JFrame("AnimationRendererTest");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    jframe.setContentPane((JPanel) dpys.getComponent());
    jframe.pack();
    jframe.setVisible(true);
    ScalarMap xmap = new ScalarMap(RealType.Longitude, Display.XAxis);
    dpys.addMap(xmap);
    ScalarMap ymap = new ScalarMap(RealType.Latitude, Display.YAxis);
    dpys.addMap(ymap);
    ScalarMap zmap = new ScalarMap(RealType.Altitude, Display.ZAxis);
    dpys.addMap(zmap);
    ScalarMap rgbaMap = new ScalarMap(radiance, Display.RGBA);
    //-ScalarMap rgbaMap = new ScalarMap(RealType.Altitude, Display.RGBA);
    dpys.addMap(rgbaMap);
    ScalarMap amap = new ScalarMap(index, Display.Animation);
    dpys.addMap(amap);
  
    ScalarMap map1contour = new ScalarMap(radiance, Display.IsoContour);
    dpys.addMap(map1contour);
    ContourControl ctr_cntrl = (ContourControl) map1contour.getControl();
    ctr_cntrl.setSurfaceValue(24f);

    AnimationControl acontrol = (AnimationControl) amap.getControl();
    acontrol.setOn(false);
    acontrol.setStep(1000);

    DataReferenceImpl ref = new DataReferenceImpl("field_ref");
    ref.setData(field);
    AnimationRendererJ3D renderer = new AnimationRendererJ3D();
    renderer.setReUseFrames(true);
    if (test.equals("old")) {
      dpys.addReference(ref);
    }
    else {
      dpys.addReferences(renderer, new DataReferenceImpl[] {ref}, null);
    }

    System.out.println("replace test in 40 sec...");
    new Delay(50000);

    Linear1DSet new_set = new Linear1DSet(index, 1, 6, 6);
    FieldImpl new_field = new FieldImpl(field_type, new_set);
    for (int i=0; i<field_domain_set.getLength(); i++) {
      new_field.setSample(i,
        field.getSample((i + 1) % field_domain_set.getLength()));
    }

    if (test.equals("old")) {
      dpys.reAutoScale();
    }
    System.out.println("replace test start");
    ref.setData(new_field);
  }
}
