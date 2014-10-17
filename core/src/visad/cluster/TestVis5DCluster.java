//
// TestVis5DCluster.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

package visad.cluster;

import visad.*;
import visad.java3d.*;
import visad.java2d.*;
import visad.util.ContourWidget;
import visad.util.AnimationWidget;
import visad.data.vis5d.Vis5DForm;

import java.rmi.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;

/**
   TestVis5DCluster is the class for testing the visad.cluster package.<P>
*/
public class TestVis5DCluster extends Object {

  public TestVis5DCluster() {
  }

  public static void main(String[] args)
         throws RemoteException, VisADException, IOException {

    int node_divide = 2;
    int number_of_nodes = node_divide * node_divide;

    RemoteNodeField[] node_v5ds = new RemoteNodeField[number_of_nodes];
  
    if (args == null || args.length < 2) {
      System.out.println("usage: 'java visad.cluster.TestVis5DCluster " +
                         "n file.v5d'");
      System.out.println("  where n = 0 for client, 1 - " + number_of_nodes +
                         " for nodes");
      return;
    }
    int id = -1;
    try {
      id = Integer.parseInt(args[0]);
    }
    catch (NumberFormatException e) {
      System.out.println("usage: 'java visad.cluster.TestVis5DCluster " +
                         "n file.v5d'");
      System.out.println("  where n = 0 for client, 1 - " + number_of_nodes +
                         " for nodes");
      return;
    }
    if (id < 0 || id > number_of_nodes) {
      System.out.println("usage: 'java visad.cluster.TestVis5DCluster " +
                         "n file.v5d'");
      System.out.println("  where n = 0 for client, 1 - " + number_of_nodes +
                         " for nodes");
      return;
    }

    boolean client = (id == 0);

    Vis5DForm v5d_form = new Vis5DForm();
    FieldImpl v5d = (FieldImpl) v5d_form.open(args[1]);
    if (v5d == null) {
      System.out.println("cannot open " + args[1]);
      return;
    }
  
    FunctionType v5d_type = (FunctionType) v5d.getType();
    Set time_set = v5d.getDomainSet();
    int time_length = time_set.getLength();
    MathType v5d_range_type = v5d_type.getRange();
    DataImpl v5d_range0 = (DataImpl) v5d.getSample(0);
    FunctionType grid_type = null;
    FunctionType grid_type2 = null;
    FlatField grid0 = null;
    if (v5d_range_type instanceof FunctionType) {
      grid_type = (FunctionType) v5d_range_type;
      grid_type2 = null;
      grid0 = (FlatField) v5d_range0;
    }
    else {
      grid_type =
        (FunctionType) ((TupleType) v5d_range_type).getComponent(0);
      grid_type2 =
        (FunctionType) ((TupleType) v5d_range_type).getComponent(1);
      grid0 = (FlatField) ((Tuple) v5d_range0).getComponent(0);
    }

    Gridded3DSet domain_set = (Gridded3DSet) grid0.getDomainSet();
    Gridded3DSet ps = makePS(domain_set, node_divide);

    if (!client) {
System.out.println("v5d_type = " + v5d_type);
      RealTupleType domain_type = grid_type.getDomain();
  
      RealTupleType time_tuple = v5d_type.getDomain();
      RealType time = (RealType) time_tuple.getComponent(0);
      RealType x = (RealType) domain_type.getComponent(0);
      RealType y = (RealType) domain_type.getComponent(1);
      RealType z = (RealType) domain_type.getComponent(2);
      RealType val = (RealType) grid_type.getRange();
      RealType val2 =
        (grid_type2 == null) ? null : (RealType) grid_type2.getRange();
  
  
      float[][] samples = domain_set.getSamples(false);
      int x_len = domain_set.getLength(0);
      int y_len = domain_set.getLength(1);
      int z_len = domain_set.getLength(2);
      int len = domain_set.getLength();

      Gridded3DSet[] subsets = new Gridded3DSet[number_of_nodes];
  
      int k = id - 1;

      int ik = k % node_divide;
      int ig = ik * x_len / node_divide;
      int igp = (ik + 1) * x_len / node_divide;
      if (ik == (node_divide - 1)) igp = x_len;
      int jk = k / node_divide;
      int jg = jk * y_len / node_divide;
      int jgp = (jk + 1) * y_len / node_divide;
      if (jk == (node_divide - 1)) jgp = y_len;
      int sub_x_len = igp - ig;
      int sub_y_len = jgp - jg;
      int sub_len = sub_x_len * sub_y_len * z_len;
// System.out.println("sub_len = " + sub_len + " out of len = " + len);
      float[][] sub_samples = new float[3][sub_len];
      for (int i=0; i<sub_x_len; i++) {
        for (int j=0; j<sub_y_len; j++) {
          for (int m=0; m<z_len; m++) {
            int a = i + sub_x_len * (j + sub_y_len * m);
            int b = (i + ig) + x_len * ((j + jg) + y_len * m);
            sub_samples[0][a] = samples[0][b];
            sub_samples[1][a] = samples[1][b];
            sub_samples[2][a] = samples[2][b];
          }
        }
      }

      subsets[k] =
        new Gridded3DSet(domain_type, sub_samples,
                         sub_x_len, sub_y_len, z_len,
                        domain_set.getCoordinateSystem(),
                        domain_set.getSetUnits(), null);
      RemoteNodeDataImpl[] subgrids = new RemoteNodeDataImpl[time_length];
      for (int i=0; i<time_length; i++) {
        DataImpl v5d_sample = (DataImpl) v5d.getSample(i);
        if (v5d_sample instanceof FlatField) {
          FlatField grid = (FlatField) v5d_sample;
          FlatField subgrid = (FlatField) grid.resample(subsets[k]);
          subgrids[i] = new RemoteNodePartitionedFieldImpl(subgrid);
        }
        else {
          Tuple v5d_tuple = (Tuple) v5d_sample;
          int ngrids = v5d_tuple.getDimension();
          RemoteNodeDataImpl[] subsubgrids = new RemoteNodeDataImpl[ngrids];
          FlatField[] combinegrids = new FlatField[ngrids];
          for (int j=0; j<ngrids; j++) {
            FlatField grid = (FlatField) v5d_tuple.getComponent(j);
            combinegrids[j] = (FlatField) grid.resample(subsets[k]);
            // subsubgrids[j] =
            //   new RemoteNodePartitionedFieldImpl(combinegrids[j]);
          }
          // subgrids[i] = new RemoteNodeTupleImpl(subsubgrids);
          FlatField subgrid = (FlatField) FieldImpl.combine(combinegrids);
          subgrids[i] = new RemoteNodePartitionedFieldImpl(subgrid);
        }
      }
      FunctionType new_v5d_type =
        new FunctionType(time_tuple, subgrids[0].getType());
      node_v5ds[k] = new RemoteNodeFieldImpl(new_v5d_type, time_set);
      node_v5ds[k].setSamples(subgrids, false);


      int kk = id - 1;
      String url = "///TestVis5DCluster" + kk;
      try {
        Naming.rebind(url, node_v5ds[kk]);
      }
      catch (Exception e) {
        System.out.println("rebind " + kk + " " + e);
        return;
      }
      // just so app doesn't exit
      DisplayImpl display = new DisplayImplJ2D("dummy");
      System.out.println("data ready as " + new_v5d_type);
      return;
    } // end if (!client)

    // this is all client code
    for (int k=0; k<number_of_nodes; k++) {
      String url = "///TestVis5DCluster" + k;
      try {
        node_v5ds[k] = (RemoteNodeField) Naming.lookup(url);
      }
      catch (Exception e) {
        System.out.println("lookup " + k + " " + e);
        return;
      }
    }

    v5d_type = (FunctionType) node_v5ds[0].getType();
System.out.println("data type = " + v5d_type);
    time_set = node_v5ds[0].getDomainSet();

    RemoteClientFieldImpl client_v5d =
      new RemoteClientFieldImpl(v5d_type, time_set);

    RemoteClusterData[] table =
      new RemoteClusterData[number_of_nodes + 1];
    for (int i=0; i<number_of_nodes; i++) {
      table[i] = node_v5ds[i];
    }
    table[number_of_nodes] = client_v5d;

    for (int i=0; i<table.length; i++) {
      table[i].setupClusterData(ps, table);
    }

    DisplayImpl display =
      // new DisplayImplJ3D("main_display");
      new DisplayImplJ3D("main_display", new ClientDisplayRendererJ3D(100000));

/*
    // get a list of decent mappings for this data
    MathType type = image.getType();
    ScalarMap[] maps = type.guessMaps(true);
    // add the maps to the display
    for (int i=0; i<maps.length; i++) {
      display.addMap(maps[i]);
    }
*/

    grid_type = (FunctionType) v5d_type.getRange();
    RealTupleType domain_type = grid_type.getDomain();
  
  
    RealTupleType time_tuple = v5d_type.getDomain();
    RealType time = (RealType) time_tuple.getComponent(0);
    RealType x = (RealType) domain_type.getComponent(0);
    RealType y = (RealType) domain_type.getComponent(1);
    RealType z = (RealType) domain_type.getComponent(2);
    RealTupleType val_tuple = (RealTupleType) grid_type.getRange();
    int nvals = val_tuple.getDimension();
    RealType[] vals = new RealType[nvals];
    for (int i=0; i<nvals; i++) {
      vals[i] = (RealType) val_tuple.getComponent(i);
    }

    ScalarMap animation_map = new ScalarMap(time, Display.Animation);
    display.addMap(animation_map);
    display.addMap(new ScalarMap(x, Display.XAxis));
    display.addMap(new ScalarMap(y, Display.YAxis));
    display.addMap(new ScalarMap(z, Display.ZAxis));
    ScalarMap[] contour_maps = new ScalarMap[nvals];
    for (int i=0; i<nvals; i++) {
      contour_maps[i] = new ScalarMap(vals[i], Display.IsoContour);
      display.addMap(contour_maps[i]);
    }

    // link data to the display
    DataReferenceImpl ref = new DataReferenceImpl("image");
    // ref.setData(image);
    // display.addReference(ref);
    RemoteDataReferenceImpl remote_ref = new RemoteDataReferenceImpl(ref);
    remote_ref.setData(client_v5d);
    RemoteDisplayImpl remote_display = new RemoteDisplayImpl(display);
    remote_display.addReference(remote_ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test ClientRendererJ3D");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    // panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    // panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    JPanel lpanel = new JPanel();
    lpanel.setLayout(new BoxLayout(lpanel, BoxLayout.Y_AXIS));
    // lpanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    // lpanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    JPanel rpanel = new JPanel();
    rpanel.setLayout(new BoxLayout(rpanel, BoxLayout.Y_AXIS));
    // rpanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    // rpanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    // add display to JPanel
    rpanel.add(display.getComponent());
    AnimationWidget awidget = new AnimationWidget(animation_map);
    awidget.setMaximumSize(new Dimension(400, 400));
    lpanel.add(new AnimationWidget(animation_map));
    for (int i=0; i<nvals; i++) {
      ContourWidget cwidget = new ContourWidget(contour_maps[i]);
      cwidget.setMaximumSize(new Dimension(400, 200));
      lpanel.add(new ContourWidget(contour_maps[i]));
    }

    lpanel.setMaximumSize(new Dimension(400, 600));
    panel.add(lpanel);
    panel.add(rpanel);

    // set size of JFrame and make it visible
    frame.setSize(800, 600);
    frame.setVisible(true);
  }

  private static Gridded3DSet makePS(Gridded3DSet domain_set, int node_divide)
          throws VisADException {
    int number_of_nodes = node_divide * node_divide;
    int x_len = domain_set.getLength(0);
    int y_len = domain_set.getLength(1);
    int z_len = domain_set.getLength(2);
    int len = domain_set.getLength();
    float[][] samples = domain_set.getSamples(false);
    float[][] ps_samples = new float[3][number_of_nodes];
    for (int i=0; i<node_divide; i++) {
      int ie = i * (x_len - 1) / (node_divide - 1);
      for (int j=0; j<node_divide; j++) {
        int je = j * (y_len - 1) / (node_divide - 1);
        int k = i + node_divide * j;
        int ke = ie + x_len * (je + y_len * (z_len / 2));
        ps_samples[0][k] = samples[0][ke];
        ps_samples[1][k] = samples[1][ke];
        ps_samples[2][k] = samples[2][ke];
      }
    }
    Gridded3DSet ps =
      new Gridded3DSet(domain_set.getType(), ps_samples,
                       node_divide, node_divide, 1,
                       domain_set.getCoordinateSystem(),
                       domain_set.getSetUnits(), null);
    return ps;
  }

}

