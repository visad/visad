//
// TestCluster.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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
import visad.data.gif.GIFForm;

import java.rmi.*;
import java.awt.event.*;
import javax.swing.*;

/**
   TestCluster is the class for testing the visad.cluster package.<P>
*/
public class TestCluster extends Object {

  public TestCluster() {
  }

  public static void main(String[] args)
         throws RemoteException, VisADException {

    int node_divide = 2;
    int number_of_nodes = node_divide * node_divide;

    if (args == null || args.length < 2) {
      System.out.println("usage: 'java visad.cluster.TestCluster n file.gif'");
      System.out.println("  where n = 0 for client, 1 - " + number_of_nodes +
                         " for nodes");
      return;
    }
    int id = -1;
    try {
      id = Integer.parseInt(args[0]);
    }
    catch (NumberFormatException e) {
      System.out.println("usage: 'java visad.cluster.TestCluster n file.gif'");
      System.out.println("  where n = 0 for client, 1 - " + number_of_nodes +
                         " for nodes");
      return;
    }
    if (id < 0 || id > number_of_nodes) {
      System.out.println("usage: 'java visad.cluster.TestCluster n file.gif'");
      System.out.println("  where n = 0 for client, 1 - " + number_of_nodes +
                         " for nodes");
      return;
    }

    boolean client = (id == 0);

    GIFForm gif_form = new GIFForm();
    FlatField image = (FlatField) gif_form.open(args[1]);
    if (image == null) {
      System.out.println("cannot open " + args[1]);
      return;
    }
/*
new Integer2DSet(imageDomain, nelements, nlines));
MathType.stringToType("((ImageElement, ImageLine) -> ImageRadiance)");
*/

    FunctionType image_type = (FunctionType) image.getType();
    RealTupleType domain_type = image_type.getDomain();
    Linear2DSet domain_set = (Linear2DSet) image.getDomainSet();
    Linear1DSet x_set = domain_set.getX();
    Linear1DSet y_set = domain_set.getY();
    // getFirst, getLast, getStep, getLength
    int x_len = x_set.getLength();
    int y_len = y_set.getLength();
    int len = domain_set.getLength();
    Linear2DSet ps =
      new Linear2DSet(domain_type,
                      x_set.getFirst(), x_set.getLast(), node_divide,
                      y_set.getFirst(), y_set.getLast(), node_divide,
                      domain_set.getCoordinateSystem(),
                      domain_set.getSetUnits(), null);

    RemoteNodePartitionedField[] node_images =
      new RemoteNodePartitionedField[number_of_nodes];
  
    if (!client) {
      Linear2DSet[] subsets = new Linear2DSet[number_of_nodes];
  
      if (number_of_nodes == 1) {
        subsets[0] = domain_set;
        node_images[0] = new RemoteNodePartitionedFieldImpl(image);
      }
      else {
        int[] indices = new int[len];
        for (int i=0; i<len; i++) indices[i] = i;
        float[][] values = domain_set.indexToValue(indices);
        int[] ps_indices = ps.valueToIndex(values);
        float[][] firsts = new float[2][number_of_nodes];
        float[][] lasts = new float[2][number_of_nodes];
        int[][] lows = new int[2][number_of_nodes];
        int[][] his = new int[2][number_of_nodes];
        for (int j=0; j<2; j++) {
          for (int i=0; i<number_of_nodes; i++) {
            firsts[j][i] = Float.MAX_VALUE;
            lasts[j][i] = -Float.MAX_VALUE;
            lows[j][i] = len + 1;
            his[j][i] = -1;
          }
        }
        for (int i=0; i<len; i++) {
          int k = ps_indices[i];
          if (k < 0) continue;
          int[] index = {indices[i] % x_len, indices[i] / x_len};
          for (int j=0; j<2; j++) {
            if (values[j][i] < firsts[j][k]) firsts[j][k] = values[j][i];
            if (values[j][i] > lasts[j][k]) lasts[j][k] = values[j][i];
            if (index[j] < lows[j][k]) lows[j][k] = index[j];
            if (index[j] > his[j][k]) his[j][k] = index[j];
          }
        }
        int k = id - 1;
        if (his[0][k] < 0 || his[1][k] < 0) {
          throw new ClusterException("Set partition error");
        }
        subsets[k] =
          new Linear2DSet(domain_type,
                      firsts[0][k], lasts[0][k], (his[0][k] - lows[0][k] + 1),
                      firsts[1][k], lasts[1][k], (his[1][k] - lows[1][k] + 1),
                      domain_set.getCoordinateSystem(),
                      domain_set.getSetUnits(), null);
        FieldImpl subimage = (FieldImpl) image.resample(subsets[k]);
        node_images[k] = new RemoteNodePartitionedFieldImpl(subimage);
      }
      int kk = id - 1;
      String url = "///TestCluster" + kk;
      try {
        Naming.rebind(url, node_images[kk]);
      }
      catch (Exception e) {
        System.out.println("rebind " + kk + " " + e);
        return;
      }
      // just so app doesn't exit
      DisplayImpl display = new DisplayImplJ2D("dummy");
      return;
    } // end if (!client)

    for (int k=0; k<number_of_nodes; k++) {
      String url = "///TestCluster" + k;
      try {
        node_images[k] = (RemoteNodePartitionedField) Naming.lookup(url);
      }
      catch (Exception e) {
        System.out.println("lookup " + k + " " + e);
        return;
      }
    }
    
    RemoteClientPartitionedFieldImpl client_image =
      new RemoteClientPartitionedFieldImpl(image_type, domain_set);

    RemoteClusterData[] table =
      new RemoteClusterData[number_of_nodes + 1];
    for (int i=0; i<number_of_nodes; i++) {
      table[i] = node_images[i];
    }
    table[number_of_nodes] = client_image;

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

    // FunctionType image_type = (FunctionType) image.getType();
    // RealTupleType domain_type = image_type.getDomain();
    RealType element = (RealType) domain_type.getComponent(0);
    RealType line = (RealType) domain_type.getComponent(1);
    RealTupleType range_type = (RealTupleType) image_type.getRange();
    RealType red = (RealType) range_type.getComponent(0);
    display.addMap(new ScalarMap(line, Display.YAxis));
    display.addMap(new ScalarMap(element, Display.XAxis));
    ScalarMap contour_map = new ScalarMap(red, Display.IsoContour);
    display.addMap(contour_map);

    // link data to the display
    DataReferenceImpl ref = new DataReferenceImpl("image");
    // ref.setData(image);
    // display.addReference(ref);
    RemoteDataReferenceImpl remote_ref = new RemoteDataReferenceImpl(ref);
    remote_ref.setData(client_image);
    RemoteDisplayImpl remote_display = new RemoteDisplayImpl(display);
    remote_display.addReference(remote_ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test ClientRendererJ3D");
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
    panel.add(new ContourWidget(contour_map));

    // set size of JFrame and make it visible
    frame.setSize(500, 700);
    frame.setVisible(true);
  }


/*
    Real r = new Real(0);
    RemoteClientTupleImpl cd = new RemoteClientTupleImpl(new Data[] {r});
    RemoteClientTupleImpl cd2 = new RemoteClientTupleImpl(new Data[] {r});
    System.out.println(cd.equals(cd)); // true
    System.out.println(cd.equals(cd2)); // false
    System.out.println(cd.clusterDataEquals(cd)); // true
    System.out.println(cd.clusterDataEquals(cd2)); // false
    System.exit(0);
*/

}

