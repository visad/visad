//
// DefaultRendererJ3D.java
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

package visad.java3d;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.media.j3d.BranchGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;

import visad.AnimationControl;
import visad.ContourControl;
import visad.Data;
import visad.DataDisplayLink;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayException;
import visad.DisplayImpl;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.Integer3DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.VisADException;


/**
   DefaultRendererJ3D is the VisAD class for the default graphics
   rendering algorithm under Java3D.<P>
*/
public class DefaultRendererJ3D extends RendererJ3D {

  DataDisplayLink link = null;

  /** this is the default DataRenderer used by the addReference method
      for DisplayImplJ3D */
  public DefaultRendererJ3D () {
    super();
  }

  public void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (links == null || links.length != 1) {
      throw new DisplayException("DefaultRendererJ3D.setLinks: must be " +
                                 "exactly one DataDisplayLink");
    }
    super.setLinks(links, d);
    link = links[0];
  }

  /** create a BranchGroup scene graph for Data in links[0] */
  public BranchGroup doTransform() throws VisADException, RemoteException {
    if (link == null) return null;
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND); // BMF
    ShadowTypeJ3D type = (ShadowTypeJ3D) link.getShadow();


    /** TDR, if a scalarMap to Animation, make make the Data's
        node live, ie add it to Display via setBranchEarly. */
    boolean isAnimation = false;
    java.util.Vector scalarMaps = link.getSelectedMapVector();
    for (int kk = 0; kk < scalarMaps.size(); kk++) {
      ScalarMap scalarMap = (ScalarMap) scalarMaps.elementAt(kk);
      if ( (scalarMap.getDisplayScalar()).equals(Display.Animation) ) {
              isAnimation = true;
      }
    }
    if (isAnimation) setBranchEarly(branch);

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
        new DisplayException("Data is null: DefaultRendererJ3D.doTransform"));
    }
    else {
      link.start_time = System.currentTimeMillis();
      link.time_flag = false;
      type.preProcess();
      boolean post_process;
      try {
        post_process =
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
      if (post_process) type.postProcess(branch);
    }
    
    link.clearData();
    return branch;
  }

  public void addSwitch(DisplayRendererJ3D displayRenderer,
                        BranchGroup branch) {
    displayRenderer.addSceneGraphComponent(branch);
  }

  public DataDisplayLink getLink() {
    return link;
  }

  public void clearScene() {
    link = null;
    super.clearScene();
  }

  public Object clone() throws CloneNotSupportedException {
    return new DefaultRendererJ3D();
  }

  public static void main(String args[]) throws VisADException,
      RemoteException, IOException {

    String test = "new";
    if (args.length > 0) {
      test = args[0];
      if (!test.equals("default")) {
        System.out.println("args: 'default' for Default logic, None for Animation");
        System.exit(0);
      }
    }

    int size = 160;
    int nr = size;
    int nc = size;
    int nz = size;
    double ang = 2 * Math.PI / nr;

    RealType[] types = { 
        RealType.Latitude, 
        RealType.Longitude,
        RealType.Altitude 
    };
    RealTupleType earth_location = new RealTupleType(types);
    RealType radiance = RealType.getRealType("radiance", null, null);
    RealType index = RealType.getRealType("index", null, null);
    FunctionType image_type = new FunctionType(earth_location, radiance);

    Integer3DSet image_domain_set = new Integer3DSet(
        RealTupleType.SpatialCartesian3DTuple, nr, nc, nz);
    FunctionType field_type = new FunctionType(index, image_type);
    Integer1DSet field_domain_set = new Integer1DSet(index, 6);
    FieldImpl field = new FieldImpl(field_type, field_domain_set);

    FlatField image = null;
    for (int tt = 0; tt < field_domain_set.getLength(); tt++) {
      float[][] values = new float[1][nr * nc * nz];
      for (int kk = 0; kk < nz; kk++) {
        for (int jj = 0; jj < nc; jj++) {
          for (int ii = 0; ii < nr; ii++) {
            int idx = kk * nr * nc + jj * nr + ii;
            values[0][idx] = 
              (tt + 1)*(2f*((float) Math.sin(2*ang*ii)) + 
                  2f*((float) Math.sin(2 * ang * jj))) + kk;
          }
        }
      }
      image = new FlatField(image_type, image_domain_set);
      image.setSamples(values);
      field.setSample(tt, image, false);
    }

    DisplayImplJ3D dpys = new DisplayImplJ3D("AnimationRendererJ3D Test");

    JFrame jframe = new JFrame("AnimationRendererTest");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
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
    dpys.addMap(rgbaMap);
    
    ScalarMap amap = new ScalarMap(index, Display.Animation);
    dpys.addMap(amap);

    ScalarMap map1contour = new ScalarMap(radiance, Display.IsoContour);
    dpys.addMap(map1contour);
    ContourControl ctr_cntrl = (ContourControl) map1contour.getControl();
    ctr_cntrl.setSurfaceValue(24f);

    AnimationControl acontrol = (AnimationControl) amap.getControl();
    //acontrol.setOn(true);
    acontrol.setStep(500);

    DataReferenceImpl ref = new DataReferenceImpl("field_ref");

    if (test.equals("default")) {
      ref.setData(image);
    } else {
      ref.setData(field);
    }
    
    dpys.addReference(ref);
  }
  
}

