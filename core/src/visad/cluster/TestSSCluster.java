//
// TestSSCluster.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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
import visad.ss.*;
import visad.bom.*;
import visad.data.vis5d.Vis5DForm;

import java.util.Vector;
import java.rmi.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;

/**
   TestSSCluster is the class for testing the visad.cluster package.<P>
<PRE>
If you want to run the demo yourself, download the latest VisAD,
and also download the v5d file:

  ftp://www.ssec.wisc.edu/pub/visad-2.0/QLQ.v5d

Then run the four commands:

  java visad.cluster.TestSSCluster 1 QLQ.v5d
  java visad.cluster.TestSSCluster 2 QLQ.v5d
  java visad.cluster.TestSSCluster 3 QLQ.v5d
  java visad.cluster.TestSSCluster 4 QLQ.v5d

These create the four cluster nodes. Wait for all four to print
both messages:

  v5d_type = (Time -> (((row, col, Height) -> QL), ((row, col, Height) -> Q)))
  data ready as (Time -> ((row, col, Height) -> (QL, Q)))

Then run a fifth command for the client:

  java visad.cluster.TestSSCluster 0 QLQ.v5d

As soon as the window pops up, you can click the "Widgets" button
and when the widgets have initialized, slide the QL (cloud water)
slider over to any value between 0.5 and 1.0. When the "please
wait ..." message disappears in the 3-D window, click on the "Go"
button to animate. Change animation rate by entering a new number
of milliseonds per frame in the text box.

Click on any combination of the "Res 1", "Res 2", "Res 3" and
"Res 4" buttons to toggle between high and low resolution in each
quadrant. Its interesting to click "Res 1", "Res 2" and "Res 4"
but not "Res 3", which gives a sense of how this will be used
in practice: looking at data from all but one node at low
resolution. Of course, with a data set large enough for this to
be necessary, the low resolution will not look so blocky,

You can also click on the "Maps" button to get the SpreadSheet
user interface for setting display mappings. After you click
"Done", you'll need to click "Widgets" again to see the widgets
that correspond to your new mappings.

The display clearly shows the partition between quadrants. We
could get rid of these breaks, but at least for the demo its
nice to see where the spatial paritions are.

Note these five JVMs running on one machine will eat a lot of
memory and cycles.
</PRE>
*/
public class TestSSCluster extends FancySSCell implements ActionListener {

  private RemoteDataReferenceImpl remote_ref = null;

  public TestSSCluster(String name, Frame parent)
         throws VisADException, RemoteException {
    super(name, parent);
  }


  /**
   * override method from BasicSSCell
   */
  protected String addData(int id, Data data, ConstantMap[] cmaps,
    String source, int type, boolean notify)
    throws VisADException, RemoteException
  {
    // add Data object to cell
    DataReferenceImpl ref = new DataReferenceImpl(Name);

    // new
    if (data instanceof RemoteData) {
      remote_ref = new RemoteDataReferenceImpl(ref);
      remote_ref.setData(data);
    }
    else {
        ref.setData(data);
    }

    SSCellData cellData;
    synchronized (CellData) {
      cellData = addReferenceImpl(id, ref, cmaps, source, type, notify, true);
    }
    return cellData.getVariableName();
  }

  /**
   * override method from BasicSSCell
   */
  protected SSCellData addReferenceImpl(int id, DataReferenceImpl ref,
    ConstantMap[] cmaps, String source, int type, boolean notify,
    boolean checkErrors) throws VisADException, RemoteException
  {
    // ensure that id is valid
    if (id == 0) id = getFirstFreeId();

    // ensure that ref is valid
    if (ref == null) ref = new DataReferenceImpl(Name);

    // notify linked cells of data addition (ADD_DATA message must come first)
    // if (notify) sendMessage(ADD_DATA, source, ref.getData());

    // add data reference to cell
    SSCellData cellData =
      new SSCellData(id, this, ref, cmaps, source, type, checkErrors);
    CellData.add(cellData);

    if (!IsRemote) {
      // SERVER: add data reference to display
      if (HasMappings) VDisplay.addReference(ref, cmaps);

      // add remote data reference to servers
      synchronized (Servers) {
        RemoteDataReferenceImpl remoteRef =
          (RemoteDataReferenceImpl) cellData.getRemoteReference();
        int len = Servers.size();
        for (int i=0; i<len; i++) {
          RemoteServerImpl rs = (RemoteServerImpl) Servers.elementAt(i);
          rs.addDataReference(remoteRef);
        }
      }
    }

    return cellData;
  }

  /**
   * override method from BasicSSCell
   */
  public synchronized void setMaps(ScalarMap[] maps)
    throws VisADException, RemoteException
  {
    if (maps == null) return;

    VisADException vexc = null;
    RemoteException rexc = null;

    if (IsRemote) {
      // CLIENT: send new mappings to server
      // sendMessage(SET_MAPS, DataUtility.convertMapsToString(maps), null);
    }
    else {
      // SERVER: set up mappings

      DataReference[] dr;
      ConstantMap[][] cmaps;
      synchronized (CellData) {
        int len = CellData.size();
        dr = new DataReference[len];
        cmaps = new ConstantMap[len][];
        for (int i=0; i<len; i++) {
          SSCellData cellData = (SSCellData) CellData.elementAt(i);
          dr[i] = cellData.getReference();
          cmaps[i] = cellData.getConstantMaps();
        }
      }
      String save = getPartialSaveString();
      VDisplay.disableAction();
      clearMaps();
      for (int i=0; i<maps.length; i++) {
        if (maps[i] != null) {
          try {
            VDisplay.addMap(maps[i]);
          }
          catch (VisADException exc) {
            vexc = exc;
          }
          catch (RemoteException exc) {
            rexc = exc;
          }
        }
      }
      for (int i=0; i<dr.length; i++) {
        // determine if ImageRendererJ3D can be used
        boolean ok = false;
        Data data = dr[i].getData();
        if (data == null) {
        }
        else if (Possible3D) {
          MathType type = data.getType();
          try {
            ok = ImageRendererJ3D.isRendererUsable(type, maps);
          }
          catch (VisADException exc) {
            if (DEBUG && DEBUG_LEVEL >= 3) exc.printStackTrace();
          }
        }
        // add reference
        if (ok && Dim != JAVA2D_2D) {
          VDisplay.addReferences(new ImageRendererJ3D(), dr[i], cmaps[i]);
        }
        else {
          if (remote_ref == null) {
            VDisplay.addReference(dr[i], cmaps[i]);
          }
          else {
            RemoteVDisplay.addReference(remote_ref, cmaps[i]);
          }
        }
      }

      VDisplay.enableAction();
      setPartialSaveString(save, true);
    }
    HasMappings = true;
    if (vexc != null) throw vexc;
    if (rexc != null) throw rexc;
  }



  /**
   * override method from BasicSSCell
   */
  public synchronized boolean constructDisplay() {
    boolean success = true;
    DisplayImpl newDisplay = VDisplay;
    RemoteDisplay rmtDisplay = RemoteVDisplay;
    if (IsSlave) {
      // SLAVE: construct dummy 2-D display
      try {
        newDisplay = new DisplayImplJ2D("DUMMY");
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
        success = false;
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
        success = false;
      }
    }
    else if (!CanDo3D && Dim != JAVA2D_2D) {
      // dimension requires Java3D, but Java3D is disabled for this JVM
      success = false;
    }
    else {
      // construct display of the proper dimension
      try {
        if (IsRemote) {
          // CLIENT: construct new display from server's remote copy
          if (Dim == JAVA3D_3D) newDisplay = new DisplayImplJ3D(rmtDisplay);
          else if (Dim == JAVA2D_2D) {
            newDisplay = new DisplayImplJ2D(rmtDisplay);
          }
          else { // Dim == JAVA3D_2D
            TwoDDisplayRendererJ3D tdr = new TwoDDisplayRendererJ3D();
            newDisplay = new DisplayImplJ3D(rmtDisplay, tdr);
          }
        }
        else {
          // SERVER: construct new display and make a remote copy
          if (Dim == JAVA3D_3D) {
            ClientDisplayRendererJ3D cdr = new ClientDisplayRendererJ3D(100000);
            newDisplay = new DisplayImplJ3D(Name, cdr);
          }
          else if (Dim == JAVA2D_2D) newDisplay = new DisplayImplJ2D(Name);
          else { // Dim == JAVA3D_2D
            TwoDDisplayRendererJ3D tdr = new TwoDDisplayRendererJ3D();
            newDisplay = new DisplayImplJ3D(Name, tdr);
          }
          rmtDisplay = new RemoteDisplayImpl(newDisplay);
        }
      }
      catch (NoClassDefFoundError err) {
        if (DEBUG) err.printStackTrace();
        success = false;
      }
      catch (UnsatisfiedLinkError err) {
        if (DEBUG) err.printStackTrace();
        success = false;
      }
      catch (Exception exc) {
        if (DEBUG) exc.printStackTrace();
        success = false;
      }
    }
    if (success) {
      if (VDisplay != null) {
        try {
          VDisplay.destroy();
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
      VDisplay = newDisplay;
      RemoteVDisplay = rmtDisplay;
    }
    return success;
  }

  public static void main(String[] args)
         throws RemoteException, VisADException, IOException {

    int node_divide = 2;
    int number_of_nodes = node_divide * node_divide;

    RemoteNodeField[] node_v5ds = new RemoteNodeField[number_of_nodes];
  
    if (args == null || args.length < 2) {
      System.out.println("usage: 'java visad.cluster.TestSSCluster " +
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
      System.out.println("usage: 'java visad.cluster.TestSSCluster " +
                         "n file.v5d'");
      System.out.println("  where n = 0 for client, 1 - " + number_of_nodes +
                         " for nodes");
      return;
    }
    if (id < 0 || id > number_of_nodes) {
      System.out.println("usage: 'java visad.cluster.TestSSCluster " +
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
      // to test on a real cluster, change to something like:
      // String ipname = "node" + k + ".ncar.ucar.edu";
      // String url = "//" + ipname + "/TestVis5DCluster" + k;
      // Then start up the four server commands on machines named
      // node1.ncar.ucar.edu, node2.ncar.ucar.edu, node3.ncar.ucar.edu
      // and node1.ncar.ucar.edu (or whatever).
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

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test ClientRendererJ3D");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    TestSSCluster ss = new TestSSCluster("TestSSCluster", frame);

    ss.addData(client_v5d);
    // ss.addData(0, client_v5d, null, "", DIRECT_SOURCE, true);

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    frame.getContentPane().add(panel);

    JPanel bpanel = new JPanel();
    bpanel.setLayout(new BoxLayout(bpanel, BoxLayout.X_AXIS));

    JButton maps = new JButton("Maps");
    maps.addActionListener(ss);
    maps.setActionCommand("map");
    bpanel.add(maps);

    JButton show = new JButton("Widgets");
    show.addActionListener(ss);
    show.setActionCommand("widgets");
    bpanel.add(show);

    JButton res1 = new JButton("Res 1");
    res1.addActionListener(ss);
    res1.setActionCommand("res1");
    bpanel.add(res1);

    JButton res2 = new JButton("Res 2");
    res2.addActionListener(ss);
    res2.setActionCommand("res2");
    bpanel.add(res2);

    JButton res3 = new JButton("Res 3");
    res3.addActionListener(ss);
    res3.setActionCommand("res3");
    bpanel.add(res3);

    JButton res4 = new JButton("Res 4");
    res4.addActionListener(ss);
    res4.setActionCommand("res4");
    bpanel.add(res4);

    panel.add(ss);
    panel.add(bpanel);

    // set size of JFrame and make it visible
    frame.setSize(600, 600);
    frame.setVisible(true);
  }

  int[] res = {1, 1, 1, 1};

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("map")) {
      hideWidgetFrame();
      addMapDialog();
    }
    else if (cmd.equals("widgets")) {
      showWidgetFrame();
    }
    else if (cmd.equals("res1")) {
      flipRes(0);
    }
    else if (cmd.equals("res2")) {
      flipRes(1);
    }
    else if (cmd.equals("res3")) {
      flipRes(2);
    }
    else if (cmd.equals("res4")) {
      flipRes(3);
    }
  }

  private void flipRes(int k) {
    res[k] = 5 - res[k];
    DisplayImpl display = getDisplay();
    Vector renderers = display.getRendererVector();
    for (int i=0; i<renderers.size(); i++) {
      DataRenderer renderer = (DataRenderer) renderers.elementAt(i);
      if (renderer instanceof ClientRendererJ3D) {
        ((ClientRendererJ3D) renderer).setResolutions(res);
      }
    }
    display.reDisplayAll();
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

