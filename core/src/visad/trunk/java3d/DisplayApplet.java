
//
// DisplayApplet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
 
package visad.java3d;
 
import visad.*;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;

import javax.media.j3d.*;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.applet.Applet;

public class DisplayApplet extends Applet {

  private DisplayImplJ3D display;
  private DisplayRendererJ3D renderer;

  public DisplayApplet(DisplayImplJ3D d) {
    display = d;
    renderer = (DisplayRendererJ3D) display.getDisplayRenderer();
    setLayout(new BorderLayout());
    Canvas3D canvas = new VisADCanvas3D(renderer, this); // J3D
    add("Center", canvas);
 
    UniverseBuilder universe = new UniverseBuilder(canvas); // J3D
    BranchGroup scene = renderer.createSceneGraph(universe.view, canvas); // J3D
    universe.addBranchGraph(scene); // J3D
  }

  /** get values from an image at URL spec'ed by string */
  double[] getValues(String string, int size) {
 
    URL url = null;
    try {
      url = new URL(string);
    }
    catch (MalformedURLException e) {
      System.out.println("MalformedURLException");
      return null;
    }
    // System.out.println("url = " + url);
    Object object = null;
    try {
      object = url.getContent();
    }
    catch (java.io.IOException e) {
      System.out.println("IOException = " + e);
      return null;
    }
    // System.out.println("object.getClass = " + object.getClass());
    if (object == null) {
      System.out.println("object is null");
    }
 
    ImageProducer producer = (ImageProducer) object;
 
    int[] pix = new int[size * size];
    double[] data = new double[size * size];
 
    java.awt.image.ColorModel cm = java.awt.image.ColorModel.getRGBdefault();
 
    java.awt.image.PixelGrabber pg =
      new java.awt.image.PixelGrabber(producer, 0, 0, size, size, pix, 0, size);
 
    // System.out.println("pg created");
 
    pg.setColorModel(cm); // unnecessary
 
    try { pg.grabPixels(); }
    catch (InterruptedException e) {
      System.out.println("Bad grabPixels");
      return null;
    }
 
    // System.out.println("grabPixels");
 
    try { while ((pg.status() & this.ALLBITS) == 0) Thread.sleep(1); }
    catch (InterruptedException e) {
      System.out.println("Bad status");
      return null;
    }
 
    for (int i=0; i<size*size; i++) {
      data[i] = pix[i] & 255;
    }
    return data;
  }

}

