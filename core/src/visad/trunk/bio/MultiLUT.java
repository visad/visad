//
// MultiLUT.java"
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import visad.*;
import visad.data.*;
import visad.util.*;
import visad.java3d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.io.IOException;
import java.rmi.RemoteException;

public class MultiLUT extends Object implements ActionListener {

  private static final int NFILES = 17;

  private float[][] data_values = null;
  private float[][] values = null;

  private FlatField data = null;

  private DataReferenceImpl[] value_refs = null;
  private DataReferenceImpl[] hue_refs = null;

  private int npixels = 0;

  /**
      run with 'java -mx256m MultiLUT'
      in directory with SPB1.PIC, SPB2.PIC, ..., SPB17.PIC
  */
  public static void main(String args[])
         throws IOException, VisADException, RemoteException {

    MultiLUT ml = new MultiLUT();
    ml.go(args);
  }

  public void go(String args[])
         throws IOException, VisADException, RemoteException {

    RealTupleType domain = null;
    RealType element = null, line = null, value = null;
    Unit unit = null;
    String name = null;
    Set set = null;
    RealType[] value_types = new RealType[NFILES];
    values = new float[NFILES][];

    DefaultFamily loader = new DefaultFamily("loader");

    for (int i=0; i<NFILES; i++) {
      Tuple tuple = (Tuple) loader.open("SPB" + (i+1) + ".PIC");
      FieldImpl field = (FieldImpl) tuple.getComponent(0);
      FlatField ff = (FlatField) field.getSample(0);
      set = ff.getDomainSet();
      if (i == 0) {
        FunctionType func = (FunctionType) ff.getType();
        domain = func.getDomain();
        element = (RealType) domain.getComponent(0);
        line = (RealType) domain.getComponent(1);
        value = (RealType) func.getRange();
        unit = value.getDefaultUnit();
        name = value.getName();
      }
      value_types[i] = RealType.getRealType(name + (i+1), unit);
      float[][] temps = ff.getFloats(false);
      values[i] = temps[0];
      // System.out.println("data " + i + " type: " + value_types[i]);
    }

    npixels = values[0].length;

    RealTupleType range = new RealTupleType(value_types);
    FunctionType big_func = new FunctionType(domain, range);
    FlatField big_data = new FlatField(big_func, set);
    big_data.setSamples(values, false);

    // RealType value = RealType.getRealType("value");
    RealType hue = RealType.getRealType("hue");
    RealTupleType new_range = new RealTupleType(value, hue);
    FunctionType new_func = new FunctionType(domain, range);
    data = new FlatField(new_func, set);
    data_values = new float[2][npixels];
    DataReferenceImpl ref1 = new DataReferenceImpl("ref1");
    ref1.setData(data);

    // System.out.println("data type: " + new_func);

    DisplayImplJ3D display1 =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap xmap = new ScalarMap(element, Display.XAxis);
    display1.addMap(xmap);
    ScalarMap ymap = new ScalarMap(line, Display.YAxis);
    display1.addMap(ymap);
    ScalarMap vmap = new ScalarMap(value, Display.Value);
    display1.addMap(vmap);
    ScalarMap hmap = new ScalarMap(hue, Display.Hue);
    display1.addMap(hmap);
    display1.addMap(new ConstantMap(1.0, Display.Saturation));
    display1.addReference(ref1);

    RealType channel = RealType.getRealType("channel");
    RealType point = RealType.getRealType("point");
    RealType intensity = RealType.getRealType("intensity");
    FunctionType spectrum = new FunctionType(channel, intensity);
    FunctionType spectra = new FunctionType(point, spectrum);
    DataReferenceImpl ref2 = new DataReferenceImpl("ref2");

    DisplayImplJ3D display2 =
      new DisplayImplJ3D("display2");
    ScalarMap xmap2 = new ScalarMap(channel, Display.XAxis);
    display2.addMap(xmap2);
    ScalarMap ymap2 = new ScalarMap(intensity, Display.YAxis);
    display2.addMap(ymap2);
    ScalarMap zmap2 = new ScalarMap(point, Display.ZAxis);
    display2.addMap(zmap2);
    display2.addReference(ref2);

    DataReferenceImpl go_ref = new DataReferenceImpl("go");
    VisADSlider[] value_sliders = new VisADSlider[NFILES];
    VisADSlider[] hue_sliders = new VisADSlider[NFILES];
    DataReferenceImpl[] value_refs = new DataReferenceImpl[NFILES];
    DataReferenceImpl[] hue_refs = new DataReferenceImpl[NFILES];


    JFrame frame = new JFrame("VisAD MultiLUT");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    int WIDTH = 1200;
    int HEIGHT = 1000;

    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);
    frame.setVisible(true);

    // create JPanel in frame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    left.setAlignmentY(JPanel.TOP_ALIGNMENT);
    left.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    JPanel center = new JPanel();
    center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
    center.setAlignmentY(JPanel.TOP_ALIGNMENT);
    center.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    JPanel right = new JPanel();
    right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
    right.setAlignmentY(JPanel.TOP_ALIGNMENT);
    right.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    panel.add(left);
    panel.add(center);
    panel.add(right);

    for (int i=0; i<NFILES; i++) {
      value_refs[i] = new DataReferenceImpl("value" + i);
      value_sliders[i] = new VisADSlider("value" + i, -100, 100, 100, 0.01,
                                         value_refs[i], RealType.Generic);
      left.add(value_sliders[i]);
      hue_refs[i] = new DataReferenceImpl("hue" + i);
      hue_sliders[i] = new VisADSlider("hue" + i, -100, 100, 100, 0.01,
                                       hue_refs[i], RealType.Generic);
      center.add(value_sliders[i]);
    }

    right.add(display1.getComponent());
    right.add(display2.getComponent());

    // "GO" button for applying computation in sliders
    JButton compute = new JButton("Compute");
    compute.addActionListener(this);
    compute.setActionCommand("compute");
    right.add(compute);


  }

  /** Handles button press events. */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("compute")) {
      try {
        float[] value_weights = new float[NFILES];
        float[] hue_weights = new float[NFILES];
        for (int i=0; i<NFILES; i++) {
          value_weights[i] = (float) ((Real) value_refs[i].getData()).getValue();
          hue_weights[i] = (float) ((Real) hue_refs[i].getData()).getValue();
        }
        for (int j=0; j<npixels; j++) {
          float v = 0, h = 0;
          for (int i=0; i<NFILES; i++) {
            v += value_weights[i] * values[i][j];
            h += hue_weights[i] * values[i][j];
          }
          data_values[0][j] = v;
          data_values[1][j] = h;
        }
        data.setSamples(data_values, false);
      }
      catch (VisADException ex) {
        System.out.println( ex.getMessage() );
      }
      catch (RemoteException ex) {
      }
    }
  }

}

