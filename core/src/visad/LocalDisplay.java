//
// LocalDisplay.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

import java.awt.Component;
import java.awt.Container;

import java.awt.image.BufferedImage;

import java.rmi.RemoteException;

import java.util.Vector;

/**
   LocalDisplay is the VisAD interface for local instances of displays.
   It declares the methods which are used by applications.
*/
public interface LocalDisplay
  extends Display
{
  /** add a display activity handler */
  void addActivityHandler(ActivityHandler ah)
    throws VisADException;

  /** remove a display activity handler */
  void removeActivityHandler(ActivityHandler ah)
    throws VisADException;

  /** add a DisplayListener */
  void addDisplayListener(DisplayListener listener);

  /** add a MessageListener */
  void addMessageListener(MessageListener listener);

  /** link refs to this Display using the non-default renderer;
      must be local DataRendererImpls;
      this method may only be invoked after all links to ScalarMaps
      have been made;
      the maps[i] array applies only to rendering refs[i];
  */
  void replaceReferences(RemoteDisplay rDpy, DataRenderer renderer,
                         DataReference[] refs, ConstantMap[][] constant_maps)
         throws VisADException, RemoteException;

  /** link refs to this Display using the non-default renderer;
      must be local DataRendererImpls;
      this method may only be invoked after all links to ScalarMaps
      have been made;
      the maps[i] array applies only to rendering refs[i];
  */
  void addReferences(DataRenderer renderer,
                             DataReference[] refs,
                             ConstantMap[][] constant_maps)
         throws VisADException, RemoteException;

  /** return the java.awt.Component (e.g., JPanel or AppletPanel)
      this Display uses; returns null for an offscreen Display */
  Component getComponent();

  /** return the DisplayRenderer associated with this Display */
  DisplayRenderer getDisplayRenderer();

  /**
   * Returns the list of DataRenderer-s.
   * @return			The list of DataRenderer-s.
   */
  Vector getRenderers();

  /**
   * Returns a clone of the list of DataRenderer-s.  A clone is returned
   * to avoid concurrent access problems by the Display thread.
   * @return			A clone of the list of DataRenderer-s.
   */
  Vector getRendererVector();
  
  /** only called for Control objects associated with 'single'
      DisplayRealType-s */
  Control getControl(Class c);

  /** find specified occurance for Control object of the specified class */
  Control getControl(Class c, int inst);

  /** find all Control objects of the specified class */
  Vector getControls(Class c);

  /** return the GraphicsModeControl associated with this Display */
  GraphicsModeControl getGraphicsModeControl();

  /** return a captured image of the display */
  BufferedImage getImage();

  /** return a Vector of the ScalarMap-s associated with this Display */
  Vector getMapVector()
         throws VisADException, RemoteException;

  /** return the ProjectionControl associated with this Display */
  ProjectionControl getProjectionControl();

  /** get a GUI component containing this Display's Control widgets,
      creating the widgets as necessary */
  Container getWidgetPanel();

  double[] make_matrix(double rotx, double roty, double rotz,
                       double scale,
		       double transx, double transy, double transz);

  double[] multiply_matrix(double[] a, double[] b);

  /**
   * Removes a DisplayListener.
   * @param listener		The listener to be removed.  Nothing happens
   *				if the listener isn't registered with this
   *				instance.
   */
  void removeDisplayListener(DisplayListener listener);

  /**
   * Removes a MessageListener.
   *
   * @param listener The listener to be removed.  Nothing happens if
   *		     the listener isn't registered with this instance.
   */
  void removeMessageListener(MessageListener listener);
}
