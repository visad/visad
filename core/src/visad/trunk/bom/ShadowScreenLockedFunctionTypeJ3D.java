//
// ShadowScreenLockedFunctionTypeJ3D.java
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

package visad.bom;

// Java
import java.rmi.RemoteException;

// VisAD
import visad.ControlEvent;
import visad.ControlListener;
import visad.DataDisplayLink;
import visad.GraphicsModeControl;
import visad.MathType;
import visad.ShadowType;
import visad.VisADException;
import visad.VisADGeometryArray;
import visad.java3d.ShadowFunctionTypeJ3D;
import visad.java3d.MouseBehaviorJ3D;
import visad.java3d.ProjectionControlJ3D;
import visad.java3d.DisplayImplJ3D;

// Java 3D
import javax.media.j3d.Group;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Transform3D;
import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;


/**
 * This renderer locks text to the screen.  The display can be
 * panned and zoomed, but the text stays locked to its
 * initial position.
 */
public class ShadowScreenLockedFunctionTypeJ3D extends ShadowFunctionTypeJ3D 
{

  private TransformGroup transformGroup = null;
  private ProjectionControlListener projectionControlListener = null;


  public ShadowScreenLockedFunctionTypeJ3D(MathType type, DataDisplayLink link, 
    ShadowType parent) 
    throws RemoteException, VisADException 
  { 
    super(type, link, parent);

    transformGroup = new TransformGroup(); 
    transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ); 
    transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); 
    transformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_READ);

    // Add our listener to the projection control.
    DisplayImplJ3D display = (DisplayImplJ3D) getDisplay(); 
    ProjectionControlJ3D control = (ProjectionControlJ3D) 
      display.getProjectionControl();
    projectionControlListener = new ProjectionControlListener();
    control.addControlListener(projectionControlListener);

  }


  /** 
   * Adds the text to the scene graph hierarchy.
   * Create a new transform group and place the text under this
   * transform group.  This allows us to set the scale, translation
   * and rotation of the text independently of the rest of the nodes
   * in the scene graph.
   */
  public boolean addTextToGroup(Object group, VisADGeometryArray geomArray, 
    GraphicsModeControl control, float alpha, float [] color)
    throws VisADException
  {
    ((Group) group).addChild(transformGroup);
    return super.addTextToGroup(transformGroup, geomArray, control, alpha, 
      color);
  }

  /**
   * When the projection control matrix is changed, this listener
   * will undo the scale, translation and rotation of the text
   * thus locking it to its initial position on the screen.
   */
  private class ProjectionControlListener implements ControlListener
  {

    private boolean first = true;
    private double initialScale;
    private double initialXTrans;
    private double initialYTrans;
    private double initialZTrans;
  
    private double [] rotation = null;
    private double [] translation = null;
    private double [] scale = null;


    /**
     * Default constructor.
     */
    public ProjectionControlListener()
    {
      rotation = new double[ 3 ]; 
      translation = new double[ 3 ]; 
      scale = new double[ 1 ]; 
    }


    /**
     * Undo all scale, translation and rotation transformations.
     * @param event contains information about this event
     */
    public void controlChanged(ControlEvent event)
    { 
      ProjectionControlJ3D control = 
        (ProjectionControlJ3D) event.getControl();
      double [] projectionControlMatrix = control.getMatrix();
      control = null;
      MouseBehaviorJ3D.unmake_matrix(rotation, scale, translation, 
    	  projectionControlMatrix);

      if (first) { 
        // This is the first time through, so keep a copy
        // of our initial scale and translation.
        // We assume that there is no rotation.
        initialScale = scale[0];
        initialXTrans = translation[0];
        initialYTrans = translation[1];
        initialZTrans = translation[2];
        first = false; 
        projectionControlMatrix = null;
        return;
      }
     
      Transform3D transform = new Transform3D();
      transformGroup.getTransform(transform); 

      // Get the rotation.

      double [][] matrix = new double[4][4];

      int k = 0;
      for ( int i = 0; i < 4; ++i ) {
        for ( int j = 0; j < 4; ++j ) {
          matrix[i][j] = projectionControlMatrix[k++];
        }
      }

      projectionControlMatrix = null;

      Matrix3d rotationMatrix = new Matrix3d();
      rotationMatrix.m00 = matrix[0][0];
      rotationMatrix.m01 = matrix[0][1];
      rotationMatrix.m02 = matrix[0][2];
      rotationMatrix.m10 = matrix[1][0];
      rotationMatrix.m11 = matrix[1][1];
      rotationMatrix.m12 = matrix[1][2];
      rotationMatrix.m20 = matrix[2][0];
      rotationMatrix.m21 = matrix[2][1];
      rotationMatrix.m22 = matrix[2][2];

      matrix = null;

      // Undo the rotaion by finding the inverse of the rotation matrix.
      rotationMatrix.invert();
      rotationMatrix.normalize();

      Transform3D rotationTransform = new Transform3D();
      rotationTransform.set(rotationMatrix);

      // Undo the scaling.
      final double newScale = initialScale / scale[0];

      // Undo the translation by translating back to our initial location.
      Vector3d translationVector = new Vector3d();
      translationVector.x = -1 * (translation[0] - initialXTrans) / scale[0];
      translationVector.y = -1 * (translation[1] - initialYTrans) / scale[0];
      translationVector.z = -1 * (translation[2] - initialZTrans) / scale[0];

      Transform3D scaleTranslationTransform = new Transform3D();
      scaleTranslationTransform.set(newScale, translationVector );

      transform = rotationTransform;
      transform.mul( scaleTranslationTransform ); 
  
      transformGroup.setTransform(transform);
  
      rotationTransform = null;
      scaleTranslationTransform = null;
      transform = null;
    }

  } // class ShadowScreenLockedFunctionTypeJ3D.ProjectionControlListener

}

