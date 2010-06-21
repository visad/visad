//
// NCEPPanel.java
//

/*

The software in this file is Copyright(C) 1999 by Tom Whittaker.
It is designed to be used with the VisAD system for interactive
analysis and visualization of numerical data.

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

package visad.jmet;

import visad.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class NCEPPanel extends JPanel implements
        ActionListener, ChangeListener {
    static int instance = 0;
    private int myInstance;
    private JSlider levelSlider, speedSlider;
    private JTextField intervalText;
    private JLabel levelSliderLabel, intervalUnits;
    private int levelValue;
    private double intervalValue;
    private JComboBox paramBox;
    private DataReference ref;
    private ContourControl ci;
    private RealType Values;
    private Tuple[] tup;
    private NetcdfGrids ncg=null;
    private JLabel statLabel;
    private JTabbedPane tabby;
    private JButton butColor;
    private float[][] colorTable;
    private String paramName;
    private JCheckBox showHide;
    private DisplayImpl di;
    private ValueControl control;
    private ColorControl ccon;
    private Color nc;
    private ScalarMap map;
    private RealType enable;
    private FieldImpl field;
    private String valueName, enableName, dataName;
    private ScalarMap valueMap, valueColorMap;
    private boolean isAloft;
    private int ndx;
    private Vector paramInfo;
    private double[] pressureLevels;
    private double[][] range;
    private double cbeg;


  public static void main(String args[]) {

    JFrame f = new JFrame();
    JLabel sl = new JLabel("The status label");
    NCEPPanel ss = new NCEPPanel(true, null, sl, null, "Title");
    Container cf = f.getContentPane();

    cf.add(ss);

    sl.setAlignmentX(Component.CENTER_ALIGNMENT);
    cf.add(sl);
    f.setSize(600,600);
    f.setVisible(true);

    /*
    String filename = "97032712_eta.nc";
    NetcdfGrids ng = new NetcdfGrids(filename);
    Vector v = ng.get4dVariables();
    ss.setParams(v);
    */

  }

  /** set up a panel
  *
  * @param isAloft is false if this is the surface level, true otherwise
  * @param di is the associated DisplayImpl
  * @param statLabel is the status label from the Frame
  * @param tabby is the JTabbedPane that this may be in (if non-null)
  * @param title is the, well, title
  *
  */
  public NCEPPanel
     (boolean isAloft, DisplayImpl di, JLabel statLabel, JTabbedPane tabby, String title) {

    super();
    this.di = di;
    this.statLabel = statLabel;
    this.isAloft = isAloft;
    this.tabby = tabby;

    myInstance = getNextInstance();
    valueName = "Value"+(myInstance+1);
    enableName = "enable"+(myInstance+1);
    dataName = "data"+(myInstance+1);
    range = new double[1][2];
    range[0][0] = -20000.;
    cbeg = 0.f;
    range[0][1] = 20000.;
    colorTable = new float[3][256];

    if (di != null) try {

      Values = RealType.getRealType(valueName);
      nc = Color.white;

      if (isAloft) {
        valueMap = new ScalarMap(Values, Display.IsoContour);
        valueColorMap = new ScalarMap(Values, Display.RGB);

        di.addMap(valueMap);
        di.addMap(valueColorMap);
        ci = (ContourControl) valueMap.getControl();
        ci.setContourInterval(30.f, 0.f, 20000.f,   60.f);
        ci.enableLabels(true);
        for (int i=0; i<256; i++) {
          colorTable[0][i] = 1.0f;
          colorTable[1][i] = 1.0f;
          colorTable[2][i] = 1.0f;
        }

        ccon = (ColorControl) (valueColorMap.getControl());
        ccon.setTable(colorTable);

      } else {
        valueMap = new ScalarMap(Values, Display.RGB);
        di.addMap(valueMap );
        for (int i=0; i<256; i++) {
          colorTable[0][i] = (float)i/255.f;
          colorTable[1][i] = (float)i/255.f;
          colorTable[2][i] = (float)i/255.f;
        }

        ccon = (ColorControl) (valueMap.getControl());
        ccon.setTable(colorTable);

      }

      enable = RealType.getRealType(enableName);
      map = new ScalarMap(enable, Display.SelectValue);
      di.addMap(map);


    } catch (Exception ec) {ec.printStackTrace(); }

    setLayout (new BoxLayout(this, BoxLayout.Y_AXIS) );
    setAlignmentX(Component.CENTER_ALIGNMENT);

    TitledBorder border = new TitledBorder(
      new LineBorder(Color.black), title,
      TitledBorder.CENTER,  TitledBorder.BELOW_TOP);

    border.setTitleColor(Color.black);
    setBorder(border);

    add(Box.createRigidArea(new Dimension(10,10) ) );

    levelValue = 0;
    if (isAloft) {
      levelSliderLabel = new JLabel("Pressure level = 1000",JLabel.CENTER);
      levelSliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      levelSlider = new JSlider(JSlider.HORIZONTAL,0,100,0);
      levelSlider.setMaximumSize(new Dimension(200,50) );
      levelSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
      levelSlider.addChangeListener(this);

      add(levelSliderLabel);
      add(levelSlider);
      add(Box.createRigidArea(new Dimension(10,10) ) );
    }

    String[] s = {"Choose a data file first"};
    paramBox = new JComboBox(s);
    paramBox.setMaximumSize(new Dimension(250,50) );
    paramBox.setAlignmentX(Component.CENTER_ALIGNMENT);
    paramBox.addActionListener(this);
    paramBox.setActionCommand("paramBox");
    JLabel paramBoxLabel = new JLabel("Selected parameter:");
    paramBoxLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    add(paramBoxLabel);
    add(paramBox);

    intervalValue = 60.;
    if (isAloft) {
      add(Box.createRigidArea(new Dimension(10,10)) );
      JLabel intervalTextLabel = new JLabel("Contour interval:");
      intervalTextLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
      add(intervalTextLabel);

      JPanel p = new JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS) );
      p.setAlignmentX(Component.CENTER_ALIGNMENT);

      intervalText = new JTextField("Enter value", 8);
      intervalText.setMaximumSize(intervalText.getPreferredSize() );
      intervalText.setActionCommand("intervaltext");
      intervalText.addActionListener(this);
      intervalUnits = new JLabel("<units>");
      p.add(intervalText);
      p.add(intervalUnits);

      add(p);
    }
    add(Box.createRigidArea(new Dimension(10,10) ) );

    JPanel pb = new JPanel();
    pb.setLayout(new BoxLayout(pb, BoxLayout.X_AXIS) );
    pb.setAlignmentX(Component.CENTER_ALIGNMENT);

    butColor = new JButton("Color");
    butColor.addActionListener(this);
    butColor.setActionCommand("butColor");
    butColor.setBackground(nc);
    pb.add(butColor);

    showHide = new JCheckBox("Make visible");
    showHide.addActionListener(this);
    showHide.setActionCommand("showbutton");
    showHide.setSelected(true);
    pb.add(Box.createRigidArea(new Dimension(20,10) ) );
    pb.add(showHide);
    add(pb);

  }

  private static synchronized int getNextInstance() { return instance++; }

  public void setNetcdfGrid(NetcdfGrids n) {
    try {
      if (ref != null && di != null) di.removeReference(ref);
    } catch (Exception rmrf) {rmrf.printStackTrace(); }
    ncg = n;
    if (isAloft) {
      pressureLevels = ncg.getPressureLevels();
      int num_levels = ncg.getNumberOfLevels();
      range = new double[num_levels][2];
      levelSlider.setMinimum(0);
      levelSlider.setMaximum(num_levels-1);
      levelSlider.setValue(0);
      levelValue = 0;
      intervalValue = 60.;
      intervalText.setText("Enter value");
    }
    if (tabby != null) {
       tabby.setTitleAt(myInstance, "Data");
       tabby.setBackgroundAt(myInstance, nc);
    }
  }

  public void setParams(Vector v) {
    paramInfo = v;
    paramBox.removeAllItems();
    paramBox.addItem("-- none --");
    for (int m=0; m<paramInfo.size(); m=m+3) {
      String miName = (String) paramInfo.elementAt(m+1);
      paramBox.addItem(miName);
    }
    revalidate();
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    //System.out.println("Action cmd:"+cmd);

    if (cmd.equals("paramBox") ) {
      paramName = (String) paramBox.getSelectedItem();
      ndx = paramBox.getSelectedIndex();

      if (paramName != null && ndx > 0) {

      paramName = (String) paramInfo.elementAt( (ndx - 1)*3 );

      Thread t = new Thread() {
        public void run() {
          try {
            statLabel.setText("Reading data...please wait!");
            tup = ncg.getGrids(paramName, Values, range);
            statLabel.setText("Done reading data...");

            di.disableAction();
            FunctionType type =
                    new FunctionType(enable, tup[levelValue].getType());
            Integer1DSet set = new Integer1DSet(enable, 2);
            field = new FieldImpl(type, set);
            field.setSample(0, tup[levelValue]);
            if (ref != null) di.removeReference(ref);
            control = (ValueControl) map.getControl();
            ref = new DataReferenceImpl(dataName);

            valueMap.setRange(range[levelValue][0], range[levelValue][1]);
            if (isAloft) {
              intervalUnits.setText(
                   (String) paramInfo.elementAt( (ndx-1)*3 +2) );
              setContInterval(range[levelValue]);
            }

            ref.setData(field);
            di.addReference(ref);
            di.enableAction();
            statLabel.setText("Rendering display...please wait!");
            if (tabby != null) {
              tabby.setTitleAt(myInstance, paramName);
              tabby.setBackgroundAt(myInstance, nc);
            }

          } catch (Exception ep) {ep.printStackTrace() ;}
        }
      };

      t.start();

      }
    } else if (cmd.equals("showbutton") ) {
      try {
        if (showHide.isSelected() ) {
            control.setValue(0.0);
        } else {
            control.setValue(1.0);
        }

      } catch (Exception selb) {selb.printStackTrace(); }

    } else if (cmd.equals("intervaltext") ) {
      try {
        String v = intervalText.getText();
        intervalValue = Double.valueOf(v).doubleValue();
        if (ci != null & ref != null)
          ci.setContourInterval((float)intervalValue, (float)range[levelValue][0],
                                (float)range[levelValue][1], (float) cbeg);
          statLabel.setText("Rendering display...please wait!");

      } catch (NumberFormatException nivt) {
        statLabel.setText("Invalid number...try again!");
        getToolkit().beep();

      } catch (Exception ivt) {ivt.printStackTrace(); }

    } else if (cmd.equals("butColor")) {
      JColorChooser cc = new JColorChooser();
      nc = cc.showDialog(this, "Choose contour color", Color.white);
      if (nc != null) {
        butColor.setBackground(nc);
        if (tabby != null) tabby.setBackgroundAt(myInstance, nc);
        try {
          for (int i=0; i<256; i++) {
            if (isAloft) {
              colorTable[0][i] = (float) nc.getRed()/255.f;
              colorTable[1][i] = (float) nc.getGreen()/255.f;
              colorTable[2][i] = (float) nc.getBlue()/255.f;

            } else {
              colorTable[0][i] = ( (float) nc.getRed()/255.f)*(float)i/255.f;
              colorTable[1][i] = ((float) nc.getGreen()/255.f)*(float)i/255.f;
              colorTable[2][i] = ((float) nc.getBlue()/255.f)*(float)i/255.f;
            }

          }

          ccon.setTable(colorTable);

        } catch (Exception ce) {ce.printStackTrace(); }
      }

    }

  }
  public void stateChanged(ChangeEvent e) {
    Object source = e.getSource();

    if (source.equals(levelSlider) && ncg != null ){

      int val = levelSlider.getValue();
      if (val != levelValue)
          levelSliderLabel.setText("Pressure level = "+pressureLevels[val]);
      if (val != levelValue & !levelSlider.getValueIsAdjusting() ) {
        levelValue = val;
        if (ref != null) try {

          // WLH 27 April 99
          di.disableAction();

          valueMap.setRange(range[levelValue][0], range[levelValue][1]);
          setContInterval(range[levelValue]);
          field.setSample(0, tup[levelValue]);

          // WLH 27 April 99
          di.enableAction();

          statLabel.setText("Rendering display...please wait!");
        } catch (Exception sl) {sl.printStackTrace();}

      }

    }
  }

  void setContInterval(double[] range) {
    double cint = (range[1] - range[0]) *.11;
    long lscal = (long) (Math.log(cint)/Math.log(10.0));
    if (cint < 1.0) lscal = lscal - 1;
    double cscal = Math.pow(10., (double)lscal);
    cint = Math.floor(cint/cscal)*cscal;

    cbeg = Math.floor(range[0]/cint)*cint;
    try {
      // System.out.println("range="+range[0]+" to "+range[1]+"  cbeg="+cbeg);
      ci.setContourInterval((float)cint,
                  (float)range[0], (float)range[1], (float)cbeg );

      intervalText.setText(cint+" ");

    } catch (Exception sci) {sci.printStackTrace();}
    return ;
  }


}
