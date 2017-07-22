//
// TextureFillRendererJ3D.java
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

package visad.bom;

import visad.*;
import visad.java3d.*;

import org.jogamp.java3d.*;

import java.rmi.*;
import java.io.IOException;
import java.awt.event.*;
import javax.swing.*;

/**
   TextureFillRendererJ3D is the VisAD class for rendering Sets (usually
   Irregular2DSets) filled with a cross hatch pattern via texture mapping
*/
public class TextureFillRendererJ3D extends DefaultRendererJ3D {

  // MathTypes that data must equalsExceptNames()
  private static MathType set_type;

  // initialize above MathType
  static {
    try {
      set_type = MathType.stringToType("Set(X, Y)");
    }
    catch (VisADException e) {
      throw new VisADError(e.getMessage());
    }
  }

  // texture pattern will repeat 2 * scale times across box
  private float scale = 10.0f;

  // texture data defining repeating pattern
  private int texture_width = 0;
  private int texture_height = 0;
  private int[] texture = null;

  // true for smooth texture
  private boolean smooth = false;

  /** texture pattern will repreat 2 * s times across box */
  public void setScale(float s) {
    scale = s;
  }

  public float getScale() {
    return scale;
  }

  /** define texture pattern as a w * h rectangle of ints (RGB values);
      note w and h must be powers of 2, and t.length must be w * h */
  public void setTexture(int w, int h, int[] t) throws VisADException {
    int ww = 1;
    while (ww < w) ww *= 2;
    int hh = 1;
    while (hh < h) hh *= 2;
    if (ww != w || hh != h || t == null || t.length != w * h) {
      throw new VisADException("bad params");
    }
    texture_width = w;
    texture_height = h;
    texture = t;
  }

  public int getTextureWidth() {
    return texture_width;
  }

  public int getTextureHeight() {
    return texture_height;
  }

  public int[] getTexture() {
    return texture;
  }

  /** set s = true to smooth texture */
  public void setSmooth(boolean s) {
    smooth = s;
  }

  public boolean getSmooth() {
    return smooth;
  }

  /** determine whether the given MathType is usable with TextureFillRendererJ3D */
  public static boolean isSetType(MathType type) {
    return (set_type.equalsExceptName(type));
  }

  /** determine whether the given MathType and collection of ScalarMaps
      meets the criteria to use TextureFillRendererJ3D. Throw a VisADException
      if ImageRenderer cannot be used, otherwise return true. */
  public static boolean isRendererUsable(MathType type, ScalarMap[] maps)
    throws VisADException
  {
    RealTupleType domain = null;
    RealType x = null, y = null;
    RealType rx = null, ry = null;

    // must be a function
    if (!(type instanceof SetType)) {
      throw new VisADException("Not a SetType");
    }
    SetType set = (SetType) type;
    domain = set.getDomain();

    // extract x and y from domain
    x = (RealType) domain.getComponent(0);
    y = (RealType) domain.getComponent(1);

    // WLH 19 July 2000
    CoordinateSystem cs = domain.getCoordinateSystem();
    if (cs != null) {
      RealTupleType rxy = cs.getReference();
      rx = (RealType) rxy.getComponent(0);
      ry = (RealType) rxy.getComponent(1);
    }

    // verify that collection of ScalarMaps is legal
    boolean bx = false, by = false;
    boolean brx = false, bry = false; // WLH 19 July 2000
    Boolean latlon = null;
    DisplayRealType spatial = null;

    for (int i=0; i<maps.length; i++) {
      ScalarMap m = maps[i];
      ScalarType md = m.getScalar();
      DisplayRealType mr = m.getDisplayScalar();
      boolean ddx = md.equals(x);
      boolean ddy = md.equals(y);
      boolean ddrx = md.equals(rx);
      boolean ddry = md.equals(ry);

      // spatial mapping
      if (ddx || ddy || ddrx || ddry) {
        if (ddx && bx || ddy && by || ddrx && brx || ddry && bry) {
          throw new VisADException("Duplicate spatial mappings");
        }
        if (((ddx || ddy) && (brx || bry)) ||
            ((ddrx || ddry) && (bx || by))) {
          throw new VisADException("reference and non-reference spatial mappings");
        }
        RealType q = (ddx ? x : null);
        if (ddy) q = y;
        if (ddrx) q = rx;
        if (ddry) q = ry;

        boolean ll;
        if (mr.equals(Display.XAxis) || mr.equals(Display.YAxis) ||
          mr.equals(Display.ZAxis))
        {
          ll = false;
        }
        else if (mr.equals(Display.Latitude) || mr.equals(Display.Longitude) ||
          mr.equals(Display.Radius))
        {
          ll = true;
        }
        else throw new VisADException("Illegal domain mapping");

        if (latlon == null) {
          latlon = new Boolean(ll);
          spatial = mr;
        }
        else if (latlon.booleanValue() != ll) {
          throw new VisADException("Multiple spatial coordinate systems");
        }
        // two mappings to the same spatial DisplayRealType are not allowed
        else if (spatial == mr) {
          throw new VisADException(
            "Multiple mappings to the same spatial DisplayRealType");
        }

        if (ddx) bx = true;
        else if (ddy) by = true;
        else if (ddrx) brx = true;
        else if (ddry) bry = true;
      }

      // illegal ScalarMap involving this MathType
      else if (ddx || ddy || ddrx || ddry)
      {
        throw new VisADException("Illegal mapping: " + m);
      }
    }

    // return true if all conditions for TextureFillRendererJ3D are met
    if (!((bx && by) || (brx && bry))) {
      throw new VisADException("Insufficient mappings");
    }
    return true;
  }

  // factory for ShadowFunctionType that defines unique behavior
  // for TextureFillRendererJ3D
  public ShadowType makeShadowSetType(
         SetType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowTextureFillSetTypeJ3D(type, link, parent);
  }

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
        new DisplayException("Data is null: DefaultRendererJ3D.doTransform"));
    }
    else {
      // check MathType of non-null data, to make sure it is a single-band
      // image or a sequence of single-band images
      MathType mtype = link.getType();
      if (!isSetType(mtype)) {
        throw new BadMappingException("must be set");
      }
      link.start_time = System.currentTimeMillis();
      link.time_flag = false;
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
    return new TextureFillRendererJ3D();
  }

  /** run 'java visad.bom.TextureFillRendererJ3D smooth' */
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    // create a DataReference for set
    final DataReference set_ref = new DataReferenceImpl("set");

    // create a Display using Java3D
    DisplayImpl display = new DisplayImplJ3D("set display");

    RealType x = RealType.getRealType("x");
    RealType y = RealType.getRealType("y");
    RealTupleType xy = new RealTupleType(x, y);


    int points = 23;
    float[][] samples = new float[2][points];

    for (int i=0; i<points; i++) {
      samples[0][i] = (float) Math.random();
      samples[1][i] = (float) Math.random();
    }

/*
    int points = 3;
    float[][] samples = {{1.0f, 1.0f, -1.0f}, {1.0f, -1.0f, 1.0f}};
*/

    Set set = new Irregular2DSet(xy, samples);
    set_ref.setData(set);

    display.addMap(new ScalarMap(x, Display.XAxis));
    display.addMap(new ScalarMap(y, Display.YAxis));

    // link the Display to set_ref
    TextureFillRendererJ3D renderer = new TextureFillRendererJ3D();
    int width = 8;
    int height = width;
    int half = width / 2;
    int halfm = half - 1;
    int halfp = half + 1;
    int[] texture = new int[width * height];
    int m = 0;
    int t = ((255 << 24) | (255 << 16) | (255 << 8) | 255);
    // int t = ((255 << 16) | (255 << 8) | 255);
    // int t = ((127 << 24) | (127 << 16) | (127 << 8) | 127);
    for (int i=0; i<width; i++) {
      for (int j=0; j<height; j++) {
        if ((i == half && halfm <= j && j <= halfp) ||
            (j == half && halfm <= i && i <= halfp)) {
          texture[m] = t;
        }
        else {
          texture[m] = 0; 
        }
        m++;
      }
    }
    renderer.setTexture(width, height, texture);
    renderer.setScale(10.0f);
    renderer.setSmooth( (args.length > 0) );

    display.addReferences(renderer, set_ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("TextureFillRendererJ3D test");
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

