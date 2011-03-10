//
// Stream2DTest.java
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

import visad.*;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Stream2DTest {

public static void main(String[] args)
       throws VisADException, RemoteException
{
  int nr = 50;
  int nc = 50;

  DisplayImpl dpy = new DisplayImplJ3D("display");

  RealType u_wind = RealType.getRealType("u_wind");
  RealType v_wind = RealType.getRealType("v_wind");

  RealTupleType uv = new RealTupleType(u_wind, v_wind);


  FunctionType f_type =
    new FunctionType(RealTupleType.SpatialCartesian2DTuple, uv);

  Integer2DSet d_set =
    new Integer2DSet(RealTupleType.SpatialCartesian2DTuple, nr, nc);

  FlatField uv_field =
    new FlatField(f_type, d_set);

  float[][] uv_values = new float[2][nr*nc];
  
  double ang = 2*Math.PI/nr;

  for ( int jj = 0; jj < nc; jj++ ) {
    for ( int ii = 0; ii < nr; ii++ ) {
      int idx = jj*nr + ii;
      uv_values[0][idx] = 5 + -20f*((float) Math.cos(0.5*ang*ii));
      uv_values[1][idx] = -10f*((float) Math.cos(0.5*ang*ii));
    }
  }

  uv_field.setSamples(uv_values, false);
                                      

  int[] numl = new int[1];
  int maxv = 1000;
  int max_lines = 100;
  int[][] n_verts = new int[1][];
  float[][][] vr = new float[1][max_lines][maxv];
  float[][][] vc = new float[1][max_lines][maxv];


  Stream2D.stream(uv_values[0], uv_values[1], nr, nc, 1f, 1, 1f,
                  vr, vc, n_verts, numl, d_set, 1f, 3f, 0, 1f);

  
  ScalarMap xmap = new ScalarMap(RealType.XAxis, Display.XAxis);
  ScalarMap ymap = new ScalarMap(RealType.YAxis, Display.YAxis);
  dpy.addMap(xmap);
  dpy.addMap(ymap);

  ScalarMap flowx = new ScalarMap(u_wind, Display.Flow1X);
  ScalarMap flowy = new ScalarMap(v_wind, Display.Flow1Y);
  dpy.addMap(flowx);
  dpy.addMap(flowy);

  FlowControl flow_cntrl = (FlowControl) flowx.getControl();
  flow_cntrl.setFlowScale(0.04f);

  flow_cntrl = (FlowControl) flowy.getControl();
  flow_cntrl.setFlowScale(0.04f);

  DataReferenceImpl ref = new DataReferenceImpl("wind");
  ref.setData(uv_field);

  dpy.addReference(ref);


  Gridded2DSet[] gsets = new Gridded2DSet[numl[0]];
  for ( int s_idx = 0; s_idx < numl[0]; s_idx++ ) {
    float[][] strm_values = new float[2][n_verts[0][s_idx]];
    System.arraycopy(vc[0][s_idx], 0, strm_values[0], 0, n_verts[0][s_idx]);
    System.arraycopy(vr[0][s_idx], 0, strm_values[1], 0, n_verts[0][s_idx]);

    gsets[s_idx] =
      new Gridded2DSet(RealTupleType.SpatialCartesian2DTuple, strm_values, n_verts[0][s_idx]);
  }

  UnionSet uset = new UnionSet(gsets);
  DataReferenceImpl strm_ref = new DataReferenceImpl("stream");
  strm_ref.setData(uset);

  ConstantMap[] strm_cm =
    new ConstantMap[]
  {
    new ConstantMap(0.1, Display.Red),
    new ConstantMap(0.8, Display.Green),
    new ConstantMap(0.1, Display.Blue),
    new ConstantMap(1.5, Display.LineWidth)
  };

  dpy.addReference(strm_ref, strm_cm);


  JFrame jframe  = new JFrame();
  jframe.addWindowListener(new WindowAdapter() {
    public void windowClosing(WindowEvent e) {System.exit(0);}
  });

  jframe.setContentPane((JPanel) dpy.getComponent());
  jframe.setSize(500, 500);
  jframe.setVisible(true);
}

}

