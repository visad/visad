//
// ScaleTest.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.text.*;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.data.units.*;
import visad.java2d.*;
import visad.java3d.*;
import visad.util.*;

/**
 * Class to demostrate how to programatically control the
 * AxisScales for ScalarMaps
 * @author  Don Murray, Unidata
 */
public class ScaleTest extends JFrame {

    DisplayImpl display;
    ScalarMap tMap;
    ScalarMap tdMap;
    ScalarMap timeMap;
    RealType temp;
    RealType dewpoint;

    /**
     * Construct the ScaleTest.
     * @param  do3D  true to use a 3D display
     * @throws VisADException  problem creating a VisAD object
     * @throws RemoteException  problem creating a remote object
     */
    public ScaleTest(boolean do3D)
        throws VisADException, RemoteException
    {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        if (do3D)
        {
            display = new DisplayImplJ3D("Display");
              //new TwoDDisplayRendererJ3D());
        }
        else
        {
            display = new DisplayImplJ2D("Display");
        }
        GraphicsModeControl gmc = display.getGraphicsModeControl();
        gmc.setScaleEnable(true);
        // uncomment this if you want to show the GMC widget
        //JFrame frame = new JFrame("GMC control");
        //frame.getContentPane().add(new GMCWidget(gmc));
        //frame.pack();
        // set that aspect
        ProjectionControl pc = display.getProjectionControl();
        pc.setAspectCartesian((do3D == true) 
                                ? new double[] {.8, 1.0, 1.0 }
                                : new double[] {.8, 1.0 });
        Unit cel = null;
        try
        {
            cel = Parser.parse("degC");
            temp = RealType.getRealType("Temperature", SI.kelvin);
            //temp = RealType.getRealType("Temperature", cel);
            dewpoint = RealType.getRealType("DewPoint", cel);
        }
        catch (Exception e) {e.printStackTrace();}
        tMap = new ScalarMap(temp, Display.YAxis);
        tMap.setOverrideUnit(cel);
        tdMap = new ScalarMap(dewpoint,  
                             (do3D == true) ? Display.ZAxis : Display.YAxis);
        timeMap = new ScalarMap(RealType.Time, Display.XAxis);
        // user defined labels
        Hashtable timeLabels = new Hashtable();
        timeLabels.put(new Double(0), "First");
        timeLabels.put(new Double(10), "Last");
        timeMap.getAxisScale().setLabelTable(timeLabels);
        Hashtable tdLabels = new Hashtable();
        tdLabels.put(new Double(-20), "Low");
        tdLabels.put(new Double(5), "High");
        tdMap.getAxisScale().setLabelTable(tdLabels);

        //  orientation
        tdMap.getAxisScale().setSide(AxisScale.SECONDARY);

        // minor ticks
        tdMap.getAxisScale().setMinorTickSpacing(2.5);

        // format labelling
        AxisScale tScale = tMap.getAxisScale();
        DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance();
        formatter.applyPattern("0.0E0");
        tScale.setNumberFormat(formatter);
        // calculate labels from user input (NB: format must be set first)
        //tScale.createStandardLabels(50, 10, 50, 40);

        // set title
        tScale.setTitle(tScale.getTitle() + " (" + cel + ")");
        display.addMap(tMap);
        display.addMap(tdMap);
        display.addMap(timeMap);
        DataReference ref = new DataReferenceImpl("data");
        float[][] timeVals = 
            new float[][] { { 0.f, 2.f, 4.f, 6.f, 8.f, 10.f} };
        Gridded1DSet timeSet = new Gridded1DSet(RealType.Time, timeVals, 6);
        float[][] tVals =   // values in K, display in Cel
            new float[][] { { 294.15f, 326.15f, 310.15f, 
                              278.15f, 278.15f, 293.15f}};
        float[][] tdVals = new float[][] { { 1.f, 3.f, 7.f, -15.f, -22.f,4.f}};
        FunctionType fieldType1 = 
            new FunctionType(RealType.Time, temp);
        FlatField field1 = new FlatField(fieldType1, timeSet);
        field1.setSamples(tVals);
        FunctionType fieldType2 = 
            new FunctionType(RealType.Time, dewpoint);
        FlatField field2 = new FlatField(fieldType2, timeSet);
        field2.setSamples(tdVals);
        ref.setData(new Tuple(new FlatField[] { field1, field2 }));
        display.addReference(ref);
        // add some controls
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(new ScaleControlPanel(tMap));
        left.add(new ScaleControlPanel(tdMap));
        left.add(new ScaleControlPanel(timeMap));
        JButton print = new JButton("Print Me");
        print.addActionListener(new visad.util.PrintActionListener(display));
        left.add(print);
        Container mainPanel = getContentPane();
        mainPanel.setLayout(new GridLayout(1,2));
        mainPanel.add(left);
        mainPanel.add(display.getComponent());
        pack();
        // uncomment if you want GMC frame
        //frame.show();
    }

    /** Run using java ScaleTest */
    public static void main(String[] args)
        throws Exception
    {
        ScaleTest frame = new ScaleTest(args.length > 0);
        frame.show();
    }

    /**
     *  Class for creating a FontSelector
     */
    class FontSelector extends JComboBox
    {
        /**
         *  Construct a FontSelector
         */
        FontSelector()
        {
            GraphicsEnvironment ge =
               GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font[] fonts = ge.getAllFonts();

           // this will put all available fonts in a platform independent way

           // mind you, not all of them work for me

            for(int i=0;i<fonts.length;i++){
              String fn = fonts[i].getFontName();
              addItem( fn );
            }
            addItem("HersheyFont.futural");
            addItem("HersheyFont.futuram");
            addItem("HersheyFont.cursive");
            addItem("HersheyFont.timesr");
            addItem("HersheyFont.timesrb");
            addItem("HersheyFont.rowmans");
            addItem("HersheyFont.rowmant");
        }
    }

    /**
     *  Class for creating a UI for controlling each AxisScale
     */
    class ScaleControlPanel extends JPanel
    {
        JSlider labelSize;
        AxisScale scale;
        ScalarMap myMap;

        /**
         *  Construct a ScaleControlPanel for the AxisScale associated
         *  with the specified ScalarMap.
         *  @param  map  map with the scale.
         */
        ScaleControlPanel(ScalarMap map)
        {
            myMap = map;
            scale = map.getAxisScale();
            if (scale == null) return;
            setLayout(new GridLayout(0,2));
            String scalarName = map.getScalarName();
            setBorder(
                BorderFactory.createTitledBorder( 
                    scalarName + " AxisScale Control"));

            // Title control
            JLabel label = new JLabel("Title: ");
            add(label);
            JTextField nameInput = new JTextField(scalarName);
            nameInput.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scale.setTitle(((JTextField)e.getSource()).getText());
                  //myMap.setScalarName(((JTextField)e.getSource()).getText());
                }
            });
            add(nameInput);

            // Font/size control
            label = new JLabel("Title/Label Size:");
            add(label);
            labelSize = new JSlider(0, 48, 12);
            labelSize.setPaintTicks(true);
            //labelSize.setSnapToTicks(true);
            labelSize.setMinorTickSpacing(2);
            labelSize.setMajorTickSpacing(10);
            labelSize.setPaintLabels(true);
            labelSize.setExtent(2);
            labelSize.addChangeListener(new ChangeListener() {
              public void stateChanged(ChangeEvent e) {
                 JSlider slider = (JSlider) e.getSource();
                 if (!slider.getValueIsAdjusting()) {
                     scale.setLabelSize(slider.getValue());
                 }
              }
            });
            add(labelSize);
            label = new JLabel("Label Font:");
            add(label);
            FontSelector fontSelector = new FontSelector();
            fontSelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    String fontName = 
                      (String) ((FontSelector)e.getSource()).getSelectedItem();

                    if (fontName.startsWith("Hershey")) {
                      HersheyFont font = new HersheyFont( 
                          fontName.substring(fontName.indexOf(".")+1) );
                      scale.setFont(font);
                      labelSize.setValue(12);  // HersheyFonts are 12 point

                    } else {
                      Font font = Font.decode(fontName);
                      int fSize=labelSize.getValue();

                      // this will derive a new font based on the old 
                      // labelSize.value this leaves the Slider in the 
                      //current font size
                      font = font.deriveFont((float) fSize);
                      scale.setFont(font);
                      labelSize.setValue(font.getSize());
                    }

                }
            });
            add(fontSelector);

            //  Color selector
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.add(new JLabel("Color: "));
            JPanel q = new JPanel();
            JButton color = new JButton("");
            color.setSize(16,16);
            color.setBackground(scale.getColor());
            color.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JButton button = (JButton) e.getSource();
                    Color newColor =
                        JColorChooser.showDialog(
                            ScaleControlPanel.this,
                            "Set AxisScale Color",
                            button.getBackground());
                    if (newColor != null)
                    {
                        scale.setColor(newColor);
                        button.setBackground(newColor);
                    }
                }
            });
            q.add(color);
            p.add(q);
            add(p);
            // Snap to box
            p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JCheckBox snapToBox = 
                new JCheckBox("Snap to Box", scale.getSnapToBox());
            snapToBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scale.setSnapToBox(((JCheckBox)e.getSource()).isSelected());
                }
            });
            p.add(snapToBox);

            // Visibility
            JCheckBox visible = 
                new JCheckBox("Visible", scale.isVisible());
            visible.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scale.setVisible(((JCheckBox)e.getSource()).isSelected());
                }
            });
            p.add(visible);
            add(p);

            // Visibility
            JCheckBox labelAll = 
                new JCheckBox("Label All", scale.getLabelAllTicks());
            labelAll.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scale.setLabelAllTicks(((JCheckBox)e.getSource()).isSelected());
                }
            });
            p.add(labelAll);
            add(p);

            // Side control
            p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.add(new JLabel("Axis side: "));
            JRadioButton primary = 
              new JRadioButton("Primary", (scale.getSide() == scale.PRIMARY));
            primary.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scale.setSide(scale.PRIMARY);
                }
            });
            p.add(primary);
            JRadioButton secondary = 
              new JRadioButton("Secondary", 
                                (scale.getSide() == scale.SECONDARY));
            secondary.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scale.setSide(scale.SECONDARY);
                }
            });
            p.add(secondary);
            ButtonGroup group = new ButtonGroup();
            group.add(primary);
            group.add(secondary);
            add(p);

            // Tick orientation
            p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.add(new JLabel("Tick orient: "));
            JRadioButton prime = new JRadioButton("Primary", true);
            prime.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scale.setTickOrientation(scale.PRIMARY);
                }
            });
            p.add(prime);
            JRadioButton second = new JRadioButton("Secondary");
            second.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scale.setTickOrientation(scale.SECONDARY);
                }
            });
            p.add(second);
            ButtonGroup group2 = new ButtonGroup();
            group2.add(prime);
            group2.add(second);
            add(p);
        }
    }
}
