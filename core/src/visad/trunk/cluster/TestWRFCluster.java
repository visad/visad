//
// TestWRFCluster.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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
import visad.data.netcdf.Plain;

import java.util.Vector;
import java.rmi.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;

/**
   TestWRFCluster is the class for testing the visad.cluster package.<P>
<PRE> Run:
   java visad.cluster.TestWRFCluster 1 /home3/billh/wrf/wrfout_01_000000_0000
   java visad.cluster.TestWRFCluster 2 /home3/billh/wrf/wrfout_01_000000_0001
   java visad.cluster.TestWRFCluster 3 /home3/billh/wrf/wrfout_01_000000_0002
   java visad.cluster.TestWRFCluster 4 /home3/billh/wrf/wrfout_01_000000_0003
   java visad.cluster.TestWRFCluster 0 doll doll doll doll
   java visad.cluster.TestWRFCluster 0 demedici demedici demedici demedici
</PRE>
*/
public class TestWRFCluster extends FancySSCell implements ActionListener {

  private RemoteDataReferenceImpl remote_ref = null;

  public TestWRFCluster(String name, Frame parent)
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

    RemoteNodeField[] node_wrfs = new RemoteNodeField[number_of_nodes];
  
    if (args == null || args.length < 1) {
      System.out.println("usage: 'java visad.cluster.TestWRFCluster n file'");
      System.out.println("            for nodes where n = 1 - " + number_of_nodes);
      System.out.println("       'java visad.cluster.TestWRFCluster 0 " +
                         "node1 node2 node3 node4' for client");
      System.exit(0);
    }
    int pid = -1;
    try {
      pid = Integer.parseInt(args[0]);
    }
    catch (NumberFormatException e) {
      System.out.println("usage: 'java visad.cluster.TestWRFCluster n file'");
      System.out.println("            for nodes where n = 1 - " + number_of_nodes);
      System.out.println("       'java visad.cluster.TestWRFCluster 0 " +
                         "node1 node2 node3 node4' for client");
      System.exit(0);
    }
    if (pid < 0 || pid > number_of_nodes) {
      System.out.println("usage: 'java visad.cluster.TestWRFCluster n file'");
      System.out.println("            for nodes where n = 1 - " + number_of_nodes);
      System.out.println("       'java visad.cluster.TestWRFCluster 0 " +
                         "node1 node2 node3 node4' for client");
      System.exit(0);
    }
    if (pid > 0 && args.length < 2) {
      System.out.println("usage: 'java visad.cluster.TestWRFCluster n file'");
      System.out.println("            for nodes where n = 1 - " + number_of_nodes);
      System.out.println("       'java visad.cluster.TestWRFCluster 0 " +
                         "node1 node2 node3 node4' for client");
      System.exit(0);
    }


    boolean client = (pid == 0);

    if (!client) {

      Plain plain = new Plain();
      FieldImpl wrf = (FieldImpl) plain.open(args[1]);
      if (wrf == null) {
        System.out.println("cannot open " + args[1]);
        return;
      }
    
      FunctionType wrf_type = (FunctionType) wrf.getType();
System.out.println("wrf_type = " + wrf_type);

      Set time_set = wrf.getDomainSet();
      int time_length = time_set.getLength();
      RealTupleType wrf_domain_type = wrf_type.getDomain();
      RealType time_type = (RealType) wrf_domain_type.getComponent(0);
      TupleType wrf_range_type = (TupleType) wrf_type.getRange();
      FunctionType wrf_non_stag_type =
        (FunctionType) wrf_range_type.getComponent(3);
      RealTupleType wrf_non_stag_domain_type = wrf_non_stag_type.getDomain();
      RealTupleType wrf_non_stag_Range_type =
        (RealTupleType) wrf_non_stag_type.getRange();
      FunctionType wrf_surface_type =
        (FunctionType) wrf_range_type.getComponent(5);

      // construct navigated WRF MathType and FieldImpl
      // note SpatialEarth3DTuple = (Longitude, Latitude, Altitude)
      RealTupleType nav_wrf_grid_domain_type = RealTupleType.SpatialEarth3DTuple;
      FunctionType nav_wrf_grid_type =
        new FunctionType(nav_wrf_grid_domain_type, wrf_non_stag_Range_type);
      FunctionType nav_wrf_type = new FunctionType(time_type, nav_wrf_grid_type);
      FieldImpl nav_wrf = new FieldImpl(nav_wrf_type, time_set);
      Gridded3DSet nav_grid_set = null;
      int nrows = 0, ncols = 0, nlevs = 0, grid_size = 0;
      int nvert = 0;
      for (int i=0; i<time_length; i++) {
        Tuple wrf_step = (Tuple) wrf.getSample(i);
        FlatField wrf_non_stag = (FlatField) wrf_step.getComponent(3);
        FlatField wrf_surface = (FlatField) wrf_step.getComponent(5);
        // if (nav_grid_set == null) {
          FlatField wrf_vert = (FlatField) wrf_step.getComponent(6);
          float[][] wrf_vert_samples = wrf_vert.getFloats(false);
/*
          nvert = wrf_vert_samples[0].length;
          float[] height = new float[nvert];
          for (int j=0; j<nvert; j++) {
            height[j] = 0.5f * (wrf_vert_samples[0][j] + wrf_vert_samples[1][j]);
// System.out.println("height["+ j + "] = " +  height[j]);
          }
*/
          Gridded3DSet wrf_grid_set = (Gridded3DSet) wrf_non_stag.getDomainSet();
          ncols = wrf_grid_set.getLength(0);
          nrows = wrf_grid_set.getLength(1);
          nlevs = wrf_grid_set.getLength(2);
          float[][] wrf_surface_samples = wrf_surface.getFloats(false);
          float[] lats = wrf_surface_samples[9];
          float[] lons = wrf_surface_samples[10];
          if (lats.length != nrows * ncols) {
            throw new ClusterException("lats.length = " + lats.length +
               " != " + nrows + " * " + ncols);
          }
/*
          if (nvert != nlevs) {
            throw new ClusterException("nvert = " + nvert +
               " != " + nlevs + " = nlevs");
          }
*/
/*
int k = 0;
for (int r=0; r<nrows; r++) {
  for (int c=0; c<ncols; c++) {
    System.out.println("row = " + r + " col = " + c + " lat = " +
                       lats[k] + " lon = " +lons[k]);
    k++;
  }
}
*/
          float[][] range_values = wrf_non_stag.getFloats(false);
          grid_size = nrows * ncols * nlevs;
          float[][] nav_wrf_samples = new float[3][grid_size];
          nav_wrf_samples[0] = new float[grid_size];
          nav_wrf_samples[1] = new float[grid_size];
          for (int lev=0; lev<nlevs; lev++) {
            int base = lev * nrows * ncols;
            for (int rc=0; rc<nrows*ncols; rc++) {
              nav_wrf_samples[0][base+rc] = lons[rc];
              nav_wrf_samples[1][base+rc] = lats[rc];
              // nav_wrf_samples[2][base+rc] = lev;
            }
          }
          nav_wrf_samples[2] = range_values[11]; // Z
          nav_grid_set =
            new Gridded3DSet(nav_wrf_grid_domain_type, nav_wrf_samples,
                             ncols, nrows, nlevs, null, null, null,
                             false,  // no copy
                             false); // no consistency test
        // } // end if (nav_grid_set == null)
        FlatField nav_wrf_grid = new FlatField(nav_wrf_grid_type, nav_grid_set);
        nav_wrf_grid.setSamples(range_values, false);
        nav_wrf.setSample(i, nav_wrf_grid);
System.out.println("done with time step " + i);
      } // end for (int i=0; i<time_length; i++)

System.out.println("pid = " + pid);

      RemoteNodeFieldImpl node_data = new RemoteNodeFieldImpl(nav_wrf);

      int kk = pid - 1;
System.out.println("kk = " + kk);
      String url = "///TestWRFCluster" + kk;
      try {
        Naming.rebind(url, node_data);
      }
      catch (Exception e) {
        System.out.println("rebind " + kk + " " + e);
        return;
      }
      // just so app doesn't exit
      CellImpl cell = new CellImpl() {
        public void doAction() throws VisADException, RemoteException {
        }
      };
      System.out.println("data ready as " + nav_wrf_type);
      return;
    } // end if (!client)

    // this is all client code
    if (args.length != 5) {
      System.out.println("usage: 'java visad.cluster.TestWRFCluster n file'");
      System.out.println("            for nodes where n = 1 - " + number_of_nodes);
      System.out.println("       'java visad.cluster.TestWRFCluster 0 " +
                         "node1 node2 node3 node4' for client");
      System.exit(0);
    }

    for (int k=0; k<number_of_nodes; k++) {
      String url = "//" + args[1+k] + "/TestWRFCluster" + k;
      try {
        node_wrfs[k] = (RemoteNodeField) Naming.lookup(url);
      }
      catch (Exception e) {
        System.out.println("lookup " + k + " " + e);
        return;
      }
    }

    FunctionType nav_wrf_type = (FunctionType) node_wrfs[0].getType();
System.out.println("data type = " + nav_wrf_type);
    Set time_set = node_wrfs[0].getDomainSet();

    RemoteClientFieldImpl client_wrf =
      new RemoteClientFieldImpl(nav_wrf_type, time_set);

    RemoteClusterData[] table =
      new RemoteClusterData[number_of_nodes + 1];
    for (int i=0; i<number_of_nodes; i++) {
      table[i] = node_wrfs[i];
    }
    table[number_of_nodes] = client_wrf;

    for (int i=0; i<table.length; i++) {
      table[i].setupClusterData(null, table);
    }

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test ClientRendererJ3D");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    TestWRFCluster ss = new TestWRFCluster("TestWRFCluster", frame);

    ss.addData(client_wrf);
    // ss.addData(0, client_wrf, null, "", DIRECT_SOURCE, true);

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

}

/*
java visad.jmet.DumpType wrfout_01_000000_0000

VisAD Data analysis
  FieldImpl of length = 5
(Time -> (((west_east_stag, south_north, bottom_top) -> RHO_U),  ****0****
          ((west_east, south_north_stag, bottom_top) -> RHO_V),  ****1****
          ((west_east, south_north, bottom_top_stag) -> RW),     ****2****
          ((west_east, south_north, bottom_top) -> (RRP, RR, TKE,
                                                    RTP, TP, QVAPOR,
                                                    QCLOUD, QRAIN, PP,
                                                    RTB, RRB, Z, PB)),  ****3****
          ((west_east, south_north, soil_layers) -> TSLB),       ****4****
          ((west_east, south_north) -> (DZETADZ,  **0** 
 MAPFAC_M,  **1**
 HGT,       **2** 
 TSK,       **3**
 RAINC,     **4** 
 RAINNC,    **5**
 RAINCV,    **6**   
 GSW,       **7**   
 GLW,       **8**   
 XLAT,      **9**                  ****
 XLONG,     **10**                 ****
 LU_INDEX,  **11**
 TMN,       **12**
 XLAND,     **13**
 HFX,       **14**
 QFX,       **15**
 SNOWC)),   **16**                                ****5****
          (bottom_top -> (FZM, FZP)),             ****6****
          (ext_scalar -> (ZETATOP, ITIMESTEP))))  ****7****


wrfout_01_000000_0000
west_east: 0 - 20
south_north: 0 - 17
bottom_top: 0 - 22



wrfout_01_000000_0001
west_east: 0 - 20
south_north: 0 - 15
bottom_top: 0 - 22
*/

