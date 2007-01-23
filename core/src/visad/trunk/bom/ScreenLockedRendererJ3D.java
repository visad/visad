//
// ScreenLockedRendererJ3D.java
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

package visad.bom;

// Java
import java.rmi.RemoteException;
import javax.swing.JFrame;

// Java3D
import javax.media.j3d.*;

// VisAD
import visad.ConstantMap;
import visad.DataReferenceImpl;
import visad.DelaunayCustom;
import visad.Display;
import visad.FunctionType;
import visad.GraphicsModeControl;
import visad.Gridded2DSet;
import visad.Irregular2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.Text;
import visad.TextControl;
import visad.TextType;
import visad.RealTupleType;
import visad.ScalarMap;
import visad.VisADException;
import visad.java3d.DefaultRendererJ3D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DisplayRendererJ3D;

/**
 * This renderer locks text to its initial position on the
 * screen.
 *
 * The render only works if you have a domain tuple of the form:
 * (latitude, longitude, text)
 * or a function type of the form:
 * ((latitude, longitude)->(text))
 */
public class ScreenLockedRendererJ3D extends DefaultRendererJ3D 
{
  boolean initWithProj = false;

  /**
   * Default constructor.
   */
  public ScreenLockedRendererJ3D()
  {
    super();
  }

  public ScreenLockedRendererJ3D(boolean initWithProj) {
    this();
    this.initWithProj = initWithProj;
  }

  public void addSwitch(DisplayRendererJ3D displayRenderer,
                         BranchGroup branch) {
    if (initWithProj) {
      displayRenderer.addLockedSceneGraphComponent(branch, initWithProj);
    }
    else {
      displayRenderer.addLockedSceneGraphComponent(branch);
    }
  }


  /**
   * This is used for function types of the form:
   * ((latitude, longitude)->(text))
   */
/*
  public ShadowType makeShadowFunctionType(FunctionType type, 
    DataDisplayLink link, ShadowType parent) 
    throws RemoteException, VisADException
  {
    return new ShadowScreenLockedFunctionTypeJ3D(type, link, parent);
  }
*/

  /**
   * This is used for tuples of the form:
   * (latitude, longitude, text)
   */
/*
  public ShadowType makeShadowTupleType(TupleType type, DataDisplayLink link,
    ShadowType parent) 
    throws RemoteException, VisADException 
  { 
    return new ShadowScreenLockedTupleTypeJ3D(type, link, parent);
  }
*/
  
  
/*
  public ShadowType makeShadowSetType(SetType type, DataDisplayLink link,
    ShadowType parent)
    throws RemoteException, VisADException 
  {
    return new ShadowScreenLockedSetTypeJ3D(type, link, parent);
  }
*/

  /**
   * Used for testing.
   * Creates a display with a red square and labels at each
   * corner of the square.  The square rotates, and moves as
   * you would expect, but the text is locked to its original
   * position on the screen.
   */
  public static final void main(String [] args)
    throws VisADException, RemoteException
  {
    final DisplayImplJ3D display = new DisplayImplJ3D("display");
    final DisplayRendererJ3D renderer = 
      (DisplayRendererJ3D) display.getDisplayRenderer();
    renderer.setBoxOn(false);
    renderer.setBackgroundColor(0.0f, 0.2f, 1.0f);

    final GraphicsModeControl gmc = 
      (GraphicsModeControl) display.getGraphicsModeControl();
    gmc.setScaleEnable(false);
    gmc.setProjectionPolicy(DisplayImplJ3D.PARALLEL_PROJECTION);

    final RealTupleType domainType = new RealTupleType(RealType.Latitude,
      RealType.Longitude);
    final TextType textType = TextType.getTextType("text");
    final FunctionType functionType = new FunctionType(domainType, textType);

    // The domain samples make up a square. (clockwise order).
    float [][] domainSamples = new float[2][4];
    domainSamples[0][0] = 0f; 
    domainSamples[1][0] = 0f;
    domainSamples[0][1] = 10f;
    domainSamples[1][1] = 0f;
    domainSamples[0][2] = 10f;
    domainSamples[1][2] = 10f;
    domainSamples[0][3] = 0f;
    domainSamples[1][3] = 10f;

    // Created the filled sqaure.
    final Gridded2DSet domainSet2 = new Gridded2DSet(domainType, 
      domainSamples, 4);
    final Irregular2DSet filledSet = DelaunayCustom.fill(domainSet2);
    final DataReferenceImpl unlockedDataRef = new DataReferenceImpl(
      "unlocked_data_ref");
    unlockedDataRef.setData(filledSet);

    final ScalarMap latMap = new ScalarMap(RealType.Latitude, Display.YAxis);
    final ScalarMap lonMap = new ScalarMap(RealType.Longitude, Display.XAxis);
    final ScalarMap textMap = new ScalarMap(textType, Display.Text);

    display.addMap(latMap);
    display.addMap(lonMap);
    display.addMap(textMap);

    // Center the square in the display.
    latMap.setRange(0, 10);
    lonMap.setRange(0, 10);
    textMap.setRange(0, 10);

    // Center the labels on the corners of the square.
    final TextControl textControl = (TextControl) textMap.getControl();
    textControl.setCenter(true);
    Text text = new Text(textType, "Screen Locked 1");
    final DataReferenceImpl lockedDataRef1 = new DataReferenceImpl(
      "locked_data_ref");
    lockedDataRef1.setData(text);
    display.addReferences(new ScreenLockedRendererJ3D(), lockedDataRef1,
    	new ConstantMap[]{
		new ConstantMap(-1.0, Display.XAxis),
		new ConstantMap(1.0, Display.YAxis),
		new ConstantMap(-0.2, Display.ZAxis)});

    text = new Text(textType, "Screen Locked 2");
    DataReferenceImpl lockedDataRef2 = new DataReferenceImpl(
      "locked_data_ref");
    lockedDataRef2.setData(text);
    display.addReferences(new ScreenLockedRendererJ3D(), lockedDataRef2,
    	new ConstantMap[]{
		new ConstantMap(1.0, Display.XAxis),
		new ConstantMap(-1.0, Display.YAxis),
		new ConstantMap(-0.2, Display.ZAxis)});

    // Color the square red.
    display.addReference(unlockedDataRef, new ConstantMap [] { 
      new ConstantMap(0.0, Display.Green),
      new ConstantMap(0.0, Display.Blue),
      new ConstantMap(-1.0, Display.ZAxis)});

    // Display the frame.
    final JFrame frame = new JFrame("ScreenLockedRendererJ3D");
    frame.getContentPane().add(display.getComponent());
    frame.setSize(400, 400);
    frame.setVisible(true);
  }

} // class ScreenLockedRendererJ3D

