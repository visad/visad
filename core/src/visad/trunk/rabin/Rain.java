
//
// Rain.java
//

package visad.rabin;

// import needed classes
import visad.*;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.util.VisADSlider;
import visad.data.vis5d.Vis5DForm;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


public class Rain {

  static final int N_COLUMNS = 3;
  static final int N_ROWS = 4;
  static final JPanel[] row_panels =
    new JPanel[N_ROWS];
  static final JPanel[][] cell_panels =
    new JPanel[N_ROWS][N_COLUMNS];
  static final DataReferenceImpl[][] cell_refs =
    new DataReferenceImpl[N_ROWS][N_COLUMNS];
  static final CellImpl[][] cells =
    new CellImpl[N_ROWS][N_COLUMNS];
  static final DisplayImpl[][] displays =
    new DisplayImpl[N_ROWS][N_COLUMNS];
  static final boolean[][] color_maps =
    new boolean[N_ROWS][N_COLUMNS];
  static final Real ten = new Real(10.0);
  static final Real one = new Real(1.0);
  static final Real three = new Real(3.0);
  static final Real fifty_three = new Real(53.0);

  /** the width and height of the UI frame */
  static final int WIDTH = 1100;
  static final int HEIGHT = 900;

  static DataReference ref300 = null;
  static DataReference ref1_4 = null;

  // type 'java Rain' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    ref300 = new DataReferenceImpl("num300");
    ref1_4 = new DataReferenceImpl("num1_4");

    if (args == null || args.length < 1) {
      System.out.println("run 'java visad.rabin.Rain file.v5d'");
    }
    Vis5DForm form = new Vis5DForm();
    FieldImpl vis5d = null;
    try {
      vis5d = (FieldImpl) form.open(args[0]);
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
      return;
    }
    if (vis5d == null) {
      System.out.println("bad Vis5D file read");
      return;
    }

    FunctionType vis5d_type = (FunctionType) vis5d.getType();
    System.out.println(vis5d_type);
    RealType time = (RealType) vis5d_type.getDomain().getComponent(0);
    FunctionType grid_type = (FunctionType) vis5d_type.getRange();
    RealTupleType domain = grid_type.getDomain();
    RealType x_domain = (RealType) domain.getComponent(0);
    RealType y_domain = (RealType) domain.getComponent(1);
    RealTupleType range = (RealTupleType) grid_type.getRange();
    int dim = range.getDimension();
    RealType[] range_types = new RealType[dim];
    for (int i=0; i<dim; i++) {
      range_types[i] = (RealType) range.getComponent(i);
    }

/*
    DataReference vis5d_ref = new DataReferenceImpl("vis5d_ref");
    vis5d_ref.setData(vis5d);
*/
 
    //
    // construct JFC user interface
    //
 
    // create a JFrame
    JFrame frame = new JFrame("Vis5D");
    WindowListener l = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    };
    frame.addWindowListener(l);
    frame.setSize(WIDTH, HEIGHT);
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);
 
    // create big_panel JPanel in frame
    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
    big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(big_panel);
 
    JPanel left_panel = new JPanel();
    left_panel.setLayout(new BoxLayout(left_panel, BoxLayout.Y_AXIS));
    left_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    left_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(left_panel);

    JPanel display_panel = new JPanel();
    display_panel.setLayout(new BoxLayout(display_panel, BoxLayout.Y_AXIS));
    display_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    display_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(display_panel);

    // create row JPanels
    for (int i=0; i<N_ROWS; i++) {
      row_panels[i] = new JPanel();
      row_panels[i].setLayout(new BoxLayout(row_panels[i],
                                            BoxLayout.X_AXIS));
      row_panels[i].setAlignmentY(JPanel.TOP_ALIGNMENT);
      row_panels[i].setAlignmentX(JPanel.LEFT_ALIGNMENT);
      display_panel.add(row_panels[i]);

      // create cell JPanels
      for (int j=0; j<N_COLUMNS; j++) {
        cell_panels[i][j] = new JPanel();
        cell_panels[i][j].setLayout(new BoxLayout(cell_panels[i][j],
                                                 BoxLayout.Y_AXIS));
        cell_panels[i][j].setAlignmentY(JPanel.TOP_ALIGNMENT);
        cell_panels[i][j].setAlignmentX(JPanel.LEFT_ALIGNMENT);
        row_panels[i].add(cell_panels[i][j]);
        cell_refs[i][j] = new DataReferenceImpl("cell_" + i + "_" + j);
        displays[i][j] = new DisplayImplJ3D("display_" + i + "_" + j,
                                            new TwoDDisplayRendererJ3D());
        displays[i][j].addMap(new ScalarMap(x_domain, Display.XAxis));
        displays[i][j].addMap(new ScalarMap(y_domain, Display.YAxis));
        color_maps[i][j] = false;
        JPanel d_panel = (JPanel) displays[i][j].getComponent();
        Border etchedBorder10 =
          new CompoundBorder(new EtchedBorder(),
                             new EmptyBorder(10, 10, 10, 10));
        d_panel.setBorder(etchedBorder10);
        cell_panels[i][j].add(d_panel);
/* ??
        for (int i=0; i<dim; i++) {
          displays[i][j].addMap(new ScalarMap(range_types[i], Display.RGB));
        }
*/
      } // end for (int j=0; j<N_ROWS; j++)
    } // end for (int i=0; i<N_COLUMNS; i++)

    VisADSlider slider300 = new VisADSlider("num300", 0, 600, 300, 1.0,
                                            ref300, RealType.Generic);
    VisADSlider slider1_4 = new VisADSlider("num1_4", 0, 280, 140, 0.01,
                                            ref1_4, RealType.Generic);
    left_panel.add(slider300);
    left_panel.add(slider1_4);

    // cell A1
    displays[0][0].addMap(new ScalarMap(range_types[0], Display.Red));
    displays[0][0].addMap(new ScalarMap(range_types[1], Display.Green));
    displays[0][0].addMap(new ScalarMap(range_types[2], Display.Blue));
    displays[0][0].addMap(new ScalarMap(time, Display.Animation));
    cell_refs[0][0].setData(vis5d);
    displays[0][0].addReference(cell_refs[0][0]);
    color_maps[0][0] = true;

    // cell B1
    cells[0][1] = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        Field field = (Field) cell_refs[0][0].getData();
        if (field != null) {
          cell_refs[0][1].setData(field.getSample(0));
        }
      }
    };
    cells[0][1].addReference(cell_refs[0][0]);

    displays[0][1].addMap(new ScalarMap(range_types[0], Display.Red));
    displays[0][1].addMap(new ScalarMap(range_types[1], Display.Green));
    displays[0][1].addMap(new ScalarMap(range_types[2], Display.Blue));
    displays[0][1].addReference(cell_refs[0][1]);
    color_maps[0][1] = true;

    // cell C1
    cells[0][2] = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        FlatField field = baseCell(cell_refs[0][1], 0);
        if (field != null) {
          cell_refs[0][2].setData(field);
          FunctionType type = (FunctionType) field.getType();
          if (!color_maps[0][2] && type != null) {
            RealType rt = (RealType) type.getRange();
            displays[0][2].addMap(new ScalarMap(rt, Display.RGB));
            displays[0][2].addReference(cell_refs[0][2]);
            color_maps[0][2] = true;
          }
        }
      }
    };
    cells[0][2].addReference(cell_refs[0][1]);
    cells[0][2].addReference(ref300);
    cells[0][2].addReference(ref1_4);

    // cell A2
    cells[1][0] = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        FlatField field = baseCell(cell_refs[0][1], 1);
        if (field != null) {
          cell_refs[1][0].setData(field);
          FunctionType type = (FunctionType) field.getType();
          if (!color_maps[1][0] && type != null) {
            RealType rt = (RealType) type.getRange();
            displays[1][0].addMap(new ScalarMap(rt, Display.RGB));
            displays[1][0].addReference(cell_refs[1][0]);
            color_maps[1][0] = true;
          }
        }
      }
    };
    cells[1][0].addReference(cell_refs[0][1]);
    cells[1][0].addReference(ref300);
    cells[1][0].addReference(ref1_4);

    // cell B2
    cells[1][1] = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        FlatField field = baseCell(cell_refs[0][1], 2);
        if (field != null) {
          cell_refs[1][1].setData(field);
          FunctionType type = (FunctionType) field.getType();
          if (!color_maps[1][1] && type != null) {
            RealType rt = (RealType) type.getRange();
            displays[1][1].addMap(new ScalarMap(rt, Display.RGB));
            displays[1][1].addReference(cell_refs[1][1]);
            color_maps[1][1] = true;
          }
        }
      }
    };
    cells[1][1].addReference(cell_refs[0][1]);
    cells[1][1].addReference(ref300);
    cells[1][1].addReference(ref1_4);

    // cell C2
    cells[1][2] = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        FlatField field = baseCell(cell_refs[0][1], 3);
        if (field != null) {
          cell_refs[1][2].setData(field);
          FunctionType type = (FunctionType) field.getType();
          if (!color_maps[1][2] && type != null) {
            RealType rt = (RealType) type.getRange();
            displays[1][2].addMap(new ScalarMap(rt, Display.RGB));
            displays[1][2].addReference(cell_refs[1][2]);
            color_maps[1][2] = true;
          }
        }
      }
    };
    cells[1][2].addReference(cell_refs[0][1]);
    cells[1][2].addReference(ref300);
    cells[1][2].addReference(ref1_4);

    // cell A3
    cells[2][0] = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        FlatField field = baseCell(cell_refs[0][1], 4);
        if (field != null) {
          cell_refs[2][0].setData(field);
          FunctionType type = (FunctionType) field.getType();
          if (!color_maps[2][0] && type != null) {
            RealType rt = (RealType) type.getRange();
            displays[2][0].addMap(new ScalarMap(rt, Display.RGB));
            displays[2][0].addReference(cell_refs[2][0]);
            color_maps[2][0] = true;
          }
        }
      }
    };
    cells[2][0].addReference(cell_refs[0][1]);
    cells[2][0].addReference(ref300);
    cells[2][0].addReference(ref1_4);

    // cell B3
    cells[2][1] = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        FlatField field = baseCell(cell_refs[0][1], 5);
        if (field != null) {
          cell_refs[2][1].setData(field);
          FunctionType type = (FunctionType) field.getType();
          if (!color_maps[2][1] && type != null) {
            RealType rt = (RealType) type.getRange();
            displays[2][1].addMap(new ScalarMap(rt, Display.RGB));
            displays[2][1].addReference(cell_refs[2][1]);
            color_maps[2][1] = true;
          }
        }
      }
    };
    cells[2][1].addReference(cell_refs[0][1]);
    cells[2][1].addReference(ref300);
    cells[2][1].addReference(ref1_4);

    // cell C3
    cells[2][2] = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        FlatField fieldC1 = (FlatField) cell_refs[0][2].getData();
        FlatField fieldA2 = (FlatField) cell_refs[1][0].getData();
        FlatField fieldB2 = (FlatField) cell_refs[1][1].getData();
        FlatField fieldC2 = (FlatField) cell_refs[1][2].getData();
        FlatField fieldA3 = (FlatField) cell_refs[2][0].getData();
        FlatField fieldB3 = (FlatField) cell_refs[2][1].getData();
        if (fieldC1 != null && fieldA2 != null && fieldB2 != null &&
            fieldC2 != null && fieldA3 != null && fieldB3 != null) {
          FlatField field = (FlatField) fieldC1.add(fieldA2);
          field = (FlatField) field.add(fieldB2);
          field = (FlatField) field.add(fieldC2);
          field = (FlatField) field.add(fieldA3);
          field = (FlatField) field.multiply(ten);
          fieldB3 = (FlatField) fieldB3.multiply(three);
          field = (FlatField) field.add(fieldB3);
          field = (FlatField) field.divide(fifty_three);

          cell_refs[2][2].setData(field);
          FunctionType type = (FunctionType) field.getType();
          if (!color_maps[2][2] && type != null) {
            RealType rt = (RealType) type.getRange();
            displays[2][2].addMap(new ScalarMap(rt, Display.RGB));
            displays[2][2].addReference(cell_refs[2][2]);
            color_maps[2][2] = true;
          }
        }
      }
    };
    cells[2][2].addReference(cell_refs[0][2]);
    cells[2][2].addReference(cell_refs[1][0]);
    cells[2][2].addReference(cell_refs[1][1]);
    cells[2][2].addReference(cell_refs[1][2]);
    cells[2][2].addReference(cell_refs[2][0]);
    cells[2][2].addReference(cell_refs[2][1]);

    // cell A4
    cells[3][0] = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        FlatField field = (FlatField) cell_refs[0][1].getData();
        if (field != null) {
          field = (FlatField) field.extract(6);
          cell_refs[3][0].setData(field);
          FunctionType type = (FunctionType) field.getType();
          if (!color_maps[3][0] && type != null) {
            RealType rt = (RealType) type.getRange();
            displays[3][0].addMap(new ScalarMap(rt, Display.RGB));
            displays[3][0].addReference(cell_refs[3][0]);
            color_maps[3][0] = true;
          }
        }
      }
    };
    cells[3][0].addReference(cell_refs[0][1]);

    // cell B4
    cells[3][1] = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        FlatField field = (FlatField) cell_refs[0][1].getData();
        if (field != null) {
          field = (FlatField) field.extract(7);
          cell_refs[3][1].setData(field);
          FunctionType type = (FunctionType) field.getType();
          if (!color_maps[3][1] && type != null) {
            RealType rt = (RealType) type.getRange();
            displays[3][1].addMap(new ScalarMap(rt, Display.RGB));
            displays[3][1].addReference(cell_refs[3][1]);
            color_maps[3][1] = true;
          }
        }
      }
    };
    cells[3][1].addReference(cell_refs[0][1]);

    // cell C4
    cells[3][2] = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        FlatField field = (FlatField) cell_refs[0][1].getData();
        if (field != null) {
          field = (FlatField) field.extract(8);
          cell_refs[3][2].setData(field);
          FunctionType type = (FunctionType) field.getType();
          if (!color_maps[3][2] && type != null) {
            RealType rt = (RealType) type.getRange();
            displays[3][2].addMap(new ScalarMap(rt, Display.RGB));
            displays[3][2].addReference(cell_refs[3][2]);
            color_maps[3][2] = true;
          }
        }
      }
    };
    cells[3][2].addReference(cell_refs[0][1]);

    // make the JFrame visible
    frame.setVisible(true);
  }

  public static FlatField baseCell(DataReferenceImpl ref, int component)
         throws VisADException, RemoteException {
    FlatField field = (FlatField) ref.getData();
    if (field != null) {
      field = (FlatField) field.extract(component);
      field = (FlatField) field.divide(ten);
      field = (FlatField) ten.pow(field);
      field = (FlatField) field.divide(ref300.getData());
      field = (FlatField) field.pow(one.divide(ref1_4.getData()));
    }
    return field;
  }

}

