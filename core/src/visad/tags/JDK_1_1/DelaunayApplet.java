
//
// DelaunayApplet.java
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

package visad;

import java.awt.*;
import java.applet.*;

/**
   DelaunayApplet provides a graphical demonstration of implemented Delaunay
   triangulation algorithms of various types, in the 2D case.<BR>
   Supported HTML parameters are:
   <DT>points
   <DD>     The number of points to triangulate.
   <DT>type
   <DD>     The triangulation method to use (clarkson, fast, watson).
   <DT>label
   <DD>     Specifies labeling with points or numbers (points, numbers).
   <DD>     Leave it blank to display only triangles.<BR>
   See the file delaunay.html for an example.<P>
*/
public class DelaunayApplet extends Applet {

  float[][] apSamples;
  int[][] apTri;
  String label;

  public void init() {
    String type;
    int t = 0;

    type = this.getParameter("type");
    if (type == null) {
      System.out.println("DelaunayApplet: No triangulation type specified!");
      System.out.println("DelaunayApplet: Specify a type in the "
                                        +"\"type\" parameter");
    }
    if (!type.equals("clarkson") && !type.equals("fast")
                                 && !type.equals("watson")) {
      if (type != null) System.out.println("DelaunayApplet: "
                                          +"Invalid type: "+type);
      System.out.println("DelaunayApplet: Only these types are supported: "
                                        +"clarkson, fast, watson");
      System.exit(1);
    }

    label = this.getParameter("label");

    int num_points = 0;
    try {
      num_points = Integer.parseInt(this.getParameter("points"));
    }
    catch (NumberFormatException e) {
      System.out.println("DelaunayApplet: error reading \"points\" "
                                        +"parameter:\n"+e);
      System.exit(2);
    }
    apSamples = new float[2][num_points];

    for (int i=0; i<num_points; i++) {
      apSamples[0][i] = (float) (500*Math.random());
      apSamples[1][i] = (float) (500*Math.random());
    }
    System.out.print("Triangulating "+num_points+" points with ");
    if (type.equals("clarkson")) {
      System.out.println("the Clarkson algorithm.");
      try {
        long start = System.currentTimeMillis();
        DelaunayClarkson ApCD = new DelaunayClarkson(apSamples);
        long end = System.currentTimeMillis();
        float time = (float) (end-start)/1000;
        System.out.println("Operation took "+time+" seconds.");
        apTri = ApCD.Tri;
      }
      catch (VisADException v) {
        System.out.println("DelaunayApplet: "+v);
        System.exit(3);
      }
    }
    else if (type.equals("fast")) {
      System.out.println("the Fast algorithm.");
      try {
        long start = System.currentTimeMillis();
        DelaunayFast ApFD = new DelaunayFast(apSamples);
        long end = System.currentTimeMillis();
        float time = (float) (end-start)/1000;
        System.out.println("Operation took "+time+" seconds.");
        apTri = ApFD.Tri;
      }
      catch (VisADException v) {
        System.out.println("DelaunayApplet: "+v);
        System.exit(3);
      }
    }
    else if (type.equals("watson")) {
      System.out.println("the Watson algorithm.");
      try {
        long start = System.currentTimeMillis();
        DelaunayWatson ApWD = new DelaunayWatson(apSamples);
        long end = System.currentTimeMillis();
        float time = (float) (end-start)/1000;
        System.out.println("Operation took "+time+" seconds.");
        apTri = ApWD.Tri;
      }
      catch (VisADException v) {
        System.out.println("DelaunayApplet: "+v);
        System.exit(3);
      }
    }
  }

  public void paint(Graphics gr) {
    if (label.equals("numbers")) {
      for (int i=0; i<apSamples[0].length; i++) {
        gr.drawString(String.valueOf(i), (int) apSamples[0][i],
                                         (int) apSamples[1][i]);
      }
    }
    if (label.equals("points")) {
      for (int i=0; i<apSamples[0].length; i++) {
        gr.drawRect((int) apSamples[0][i]-2, (int) apSamples[1][i]-2, 4, 4);
      }
    }

    for (int i=0; i<apTri.length; i++) {
      gr.drawLine((int) apSamples[0][apTri[i][0]],
                  (int) apSamples[1][apTri[i][0]],
                  (int) apSamples[0][apTri[i][1]],
                  (int) apSamples[1][apTri[i][1]]);
      gr.drawLine((int) apSamples[0][apTri[i][1]],
                  (int) apSamples[1][apTri[i][1]],
                  (int) apSamples[0][apTri[i][2]],
                  (int) apSamples[1][apTri[i][2]]);
      gr.drawLine((int) apSamples[0][apTri[i][2]],
                  (int) apSamples[1][apTri[i][2]],
                  (int) apSamples[0][apTri[i][0]],
                  (int) apSamples[1][apTri[i][0]]);
    }
  }

}

