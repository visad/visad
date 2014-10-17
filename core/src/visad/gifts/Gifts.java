//
// Gifts.java
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

package visad.gifts;

import visad.*;
import visad.java3d.DisplayImplJ3D;
import visad.java2d.DisplayImplJ2D;
import visad.data.mcidas.BaseMapAdapter;
import visad.data.mcidas.AreaForm;
import visad.bom.WindPolarCoordinateSystem;
import visad.bom.BarbRendererJ2D;
import visad.util.SelectRangeWidget;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.rmi.*;
import java.io.*;

public class Gifts
       implements ItemListener, ScalarMapListener
{
    // brightness of AREA image
    private static final int BRIGHT = 3;

    String filename = null;
    DataImpl data;
    MathType type;
    MathType range;
    Set domainSet = null;
    MathType R_type;
    MathType D_type;
    RealType xaxis = null;
    RealType yaxis = null;
    RealType zaxis = null;
    RealType flowx;
    RealType flowy;
    EarthVectorType flowxy = null;
    RealType flow_degree;
    RealType flow_speed;
    RealTupleType domain;
    RealTupleType reference;
    CoordinateSystem coord_sys;
    FunctionType f_type;
    DisplayImpl display;
    DisplayImpl display2;
    float lonmin, lonmax;
    float latmin, latmax;
    double[] x_range;
    double[] y_range;
    double[][] dir_spd = new double[2][];
    double[][] uv = new double[2][];
    boolean slice3d = true;
    ConstantMap[] map_constMap, map_constMap2, img_constMap, rect_constMap;
    ConstantMap[][] wnd_constMap, clone_wnd_constMap;
    DataReference maplines_ref, rect_ref;
    DataReference[] winds_ref;
    DataReference[] clone_winds_ref;
    DataReference image_ref;
    ScalarMap xmap1, xmap2, xmap3;
    ScalarMap ymap1, ymap2, ymap3;
    ScalarMap zmap, zmap2;
    ScalarMap flowx_map, flowx_map2;
    ScalarMap flowy_map, flowy_map2;
    ScalarMap sel_map, rgb_map, slice_map;
    boolean xmapEvent = false;
    boolean ymapEvent = false;
    boolean firstEvent = false;
    BaseMapAdapter baseMap;
    JCheckBox multi_color;

  public static void main(String[] args)
         throws VisADException, RemoteException, IOException 
  {
    Gifts gifts = new Gifts(args); // WLH
  }

  public Gifts(String[] args) // WLH
         throws VisADException, RemoteException, IOException
  {
    if (args.length > 0) slice3d = false; // WLH
    flowx = new RealType("flowx", CommonUnit.meterPerSecond, null);
    flowy = new RealType("flowy", CommonUnit.meterPerSecond, null);
    flow_degree = new RealType("flow_degree", CommonUnit.degree, null);
    flow_speed = new RealType("flow_speed", CommonUnit.meterPerSecond, null);
    maplines_ref = new DataReferenceImpl("maplines");
    image_ref = new DataReferenceImpl("image_ref");

/**------  main gui panel  ------*/

    Border etchedBorder =
           new CompoundBorder(new EtchedBorder(),
                              new EmptyBorder(5, 5, 5, 5));

    JFrame frame = new JFrame("Gifts");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
    big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);


/**------   File I/O, data initialization    -------*/

    baseMap = new BaseMapAdapter("./data/OUTLHRES");

    if ( baseMap.isEastPositive() ) 
    {
      baseMap.setEastPositive(true);
    }


/**------   Get wind data   ----*/

    TextForm form = new TextForm(false, "lon", "lat");

    String[][] filenames = { { "./data/REALGI250",
                               "./data/REALGI300",
                               "./data/REALGI350",
                               "./data/REALGI400",
                               "./data/REALGI430",
                               "./data/REALGI475",
                               "./data/REALGI500",
                               "./data/REALGI570",
                               "./data/REALGI620",
                               "./data/REALGI670",
                               "./data/REALGI700",
                               "./data/REALGI780",
                               "./data/REALGI850",
                               "./data/REALGI920",
                               "./data/REALGI950",
                               "./data/REALGI1000" },
                             { "./data/REALGR250",
                               "./data/REALGR300",
                               "./data/REALGR350",
                               "./data/REALGR400",
                               "./data/REALGR430",
                               "./data/REALGR475",
                               "./data/REALGR500",
                               "./data/REALGR570",
                               "./data/REALGR620",
                               "./data/REALGR670",
                               "./data/REALGR700",
                               "./data/REALGR780",
                               "./data/REALGR850",
                               "./data/REALGR920",
                               "./data/REALGR950",
                               "./data/REALGR1000" },
                             { "./data/TRUTH250",
                               "./data/TRUTH300",
                               "./data/TRUTH350",
                               "./data/TRUTH400",
                               "./data/TRUTH430",
                               "./data/TRUTH475",
                               "./data/TRUTH500",
                               "./data/TRUTH570",
                               "./data/TRUTH620",
                               "./data/TRUTH670",
                               "./data/TRUTH700",
                               "./data/TRUTH780",
                               "./data/TRUTH850",
                               "./data/TRUTH920",
                               "./data/TRUTH950",
                               "./data/TRUTH1000" } };

    DataImpl[] winds = getWinds(filenames, form);
    int len = winds.length;
    winds_ref = new DataReference[len];
    for (int i=0; i<len; i++) {
      winds_ref[i] = new DataReferenceImpl("winds_ref" + i);
      winds_ref[i].setData(winds[i]);
    }


/**-----   Get image data  ------*/

    AreaForm area_form = new AreaForm();
    FieldImpl image = (FieldImpl) area_form.open("./data/AREA5800");
    float[][] img_floats = image.getFloats(false);
    for (int i=0; i<img_floats.length; i++) {
      for (int j=0; j<img_floats[i].length; j++) img_floats[i][j] *= BRIGHT;
    }
    image.setSamples(img_floats);

    FunctionType image_type = (FunctionType)image.getType();
    RealTupleType rtt = (RealTupleType) image_type.getRange();
    RealType image_range = (RealType) rtt.getComponent(0);
    image_ref.setData(image);


/**------  winds/image display panel set-up  ----*/

    map_constMap = new ConstantMap[] {
                       new ConstantMap(0., Display.Red),
                       new ConstantMap(1., Display.Green),
                       new ConstantMap(0., Display.Blue),
                       new ConstantMap(-.99, Display.ZAxis) };
    map_constMap2 = new ConstantMap[] {
                       new ConstantMap(0., Display.Red),
                       new ConstantMap(1., Display.Green),
                       new ConstantMap(0., Display.Blue),
                       new ConstantMap(-.99, Display.ZAxis) };

    wnd_constMap = new ConstantMap[][] {
                       { new ConstantMap(1., Display.Red),
                         new ConstantMap(1., Display.Green),
                         new ConstantMap(0., Display.Blue) },
                       { new ConstantMap(0., Display.Red),
                         new ConstantMap(1., Display.Green),
                         new ConstantMap(1., Display.Blue) },
                       { new ConstantMap(1., Display.Red),
                         new ConstantMap(0., Display.Green),
                         new ConstantMap(1., Display.Blue) } };

    clone_wnd_constMap = new ConstantMap[][] {
                       { new ConstantMap(1., Display.Red),
                         new ConstantMap(1., Display.Green),
                         new ConstantMap(0., Display.Blue) },
                       { new ConstantMap(0., Display.Red),
                         new ConstantMap(1., Display.Green),
                         new ConstantMap(1., Display.Blue) },
                       { new ConstantMap(1., Display.Red),
                         new ConstantMap(0., Display.Green),
                         new ConstantMap(1., Display.Blue) } };

    img_constMap = new ConstantMap[] { new ConstantMap(-1., Display.ZAxis) };

    display = new DisplayImplJ3D("display");
    DisplayRenderer d1_render = display.getDisplayRenderer();
    d1_render.setBackgroundColor(0.5f, 0.5f, 0.5f);
    d1_render.setBoxColor(0f, 0f, 0f);
    d1_render.setCursorColor(0f, 0f, 0f);

    JPanel l_panel = new JPanel();
    l_panel.setLayout(new BoxLayout(l_panel, BoxLayout.Y_AXIS));
    l_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    JPanel s_panel = new JPanel();
    s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
    s_panel.add(display.getComponent());
    s_panel.setBorder(etchedBorder);
    l_panel.add(s_panel);

    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setScaleEnable(true);

    xmap1 = new ScalarMap(xaxis, Display.XAxis);
    xmap1.setScaleColor(new float[] {0f, 0f, 0f});
    xmap1.addScalarMapListener(this);
    display.addMap(xmap1);
    ymap1 = new ScalarMap(yaxis, Display.YAxis);
    ymap1.setScaleColor(new float[] {0f, 0f, 0f});
    ymap1.addScalarMapListener(this);
    display.addMap(ymap1);
    zmap = new ScalarMap(zaxis, Display.ZAxis);
    zmap.setScaleColor(new float[] {0f, 0f, 0f});
    display.addMap(zmap);
    zmap.setRange(1000., 200.);
    flowx_map = new ScalarMap(flowx, Display.Flow1X);
    display.addMap(flowx_map);
    flowx_map.setRange(-1.0, 1.0);
    flowy_map = new ScalarMap(flowy, Display.Flow1Y);
    display.addMap(flowy_map);
    flowy_map.setRange(-1.0, 1.0);
    FlowControl flow_control = (FlowControl) flowy_map.getControl();
    flow_control.setFlowScale(0.005f);

    // set up checkbox buttons data selector panel
    JPanel option_panel = new JPanel();
    option_panel.setLayout(new BoxLayout(option_panel, BoxLayout.X_AXIS));
    multi_color = new JCheckBox("Multi-color", false);
    multi_color.addItemListener(this);
    multi_color.setEnabled(true);
    JCheckBox jcb_gifts = new JCheckBox("GIFTS", true);
    jcb_gifts.addItemListener(this);
    JCheckBox jcb_goes = new JCheckBox("GOES", false);
    jcb_goes.addItemListener(this);
    JCheckBox jcb_truth = new JCheckBox("TRUTH", false);
    jcb_truth.addItemListener(this);

    option_panel.add(multi_color);
    option_panel.add(Box.createRigidArea(new Dimension(15, 0)));
    option_panel.add(jcb_gifts);
    option_panel.add(jcb_goes);
    option_panel.add(jcb_truth);
    l_panel.add(option_panel);

    sel_map = new ScalarMap(zaxis, Display.SelectRange);
    display.addMap(sel_map);

    rgb_map = new ScalarMap(image_range, Display.RGB);
    display.addMap(rgb_map);


    SelectRangeWidget sw = new SelectRangeWidget(sel_map);
    s_panel = new JPanel();
    s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
    s_panel.setBorder(etchedBorder);
    s_panel.add(sw);
    l_panel.add(s_panel);

/**-----   slice display set-up   ----*/

    map_constMap[0] =  new ConstantMap(0., Display.Blue);
    map_constMap[1] =  new ConstantMap(0., Display.Red);
    map_constMap[2] =  new ConstantMap(1., Display.Green); 
    map_constMap[3] =  new ConstantMap(-.99, Display.ZAxis);
    map_constMap2[0] =  new ConstantMap(0., Display.Blue);
    map_constMap2[1] =  new ConstantMap(0., Display.Red);
    map_constMap2[2] =  new ConstantMap(1., Display.Green); 
    map_constMap2[3] =  new ConstantMap(-.99, Display.ZAxis);

    DataImpl[] new_winds = cloneWinds(winds);
    TupleType tt = (TupleType) new_winds[0].getType();
    FunctionType ft = (FunctionType) tt.getComponent(0);
    TupleType new_range = (TupleType) ft.getRange();

    int index = new_range.getIndex("Latitude_winds");
    RealType yaxis_winds = (RealType) new_range.getComponent(index);
    index = new_range.getIndex("Longitude_winds");
    RealType xaxis_winds = (RealType) new_range.getComponent(index);

    if ( !slice3d ) {
      display2 = new DisplayImplJ2D("slice_display"); 
    }
    else {
      display2 = new DisplayImplJ3D("slice_display");
    }
    DisplayRenderer d2_render = display2.getDisplayRenderer();
    d2_render.setBackgroundColor(0.5f, 0.5f, 0.5f);
    d2_render.setBoxColor(0f, 0f, 0f);
    d2_render.setCursorColor(0f, 0f, 0f);

    JPanel r_panel = new JPanel();
    r_panel.setLayout(new BoxLayout(r_panel, BoxLayout.Y_AXIS));

    s_panel = new JPanel();
    s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
    s_panel.add(display2.getComponent());
    s_panel.setBorder(etchedBorder);
    r_panel.add(s_panel);

    mode = display2.getGraphicsModeControl();
    mode.setScaleEnable(true);

    if ( !slice3d) {
      ymap2 = new ScalarMap(zaxis, Display.YAxis);
      ymap2.setScaleColor(new float[] {0f, 0f, 0f});
      display2.addMap(ymap2);
      ymap2.setRange(1000., 200.);
    }
    else {
      ymap2 = new ScalarMap(yaxis, Display.YAxis);
      ymap2.setScaleColor(new float[] {0f, 0f, 0f});
      ymap3 = new ScalarMap(yaxis_winds, Display.YAxis);
      ymap3.setScaleColor(new float[] {0f, 0f, 0f});
      display2.addMap(ymap2);
      display2.addMap(ymap3);

      zmap2 = new ScalarMap(zaxis, Display.ZAxis);
      display2.addMap(zmap2);
      zmap2.setRange(1000., 200.);
    }

    xmap2 = new ScalarMap(xaxis, Display.XAxis);
    xmap2.setScaleColor(new float[] {0f, 0f, 0f});
    xmap3 = new ScalarMap(xaxis_winds, Display.XAxis);
    xmap3.setScaleColor(new float[] {0f, 0f, 0f});
    display2.addMap(xmap2);
    display2.addMap(xmap3);

    ScalarMap flowx_map2 = new ScalarMap(flowx, Display.Flow1X);
    display2.addMap(flowx_map2);
    flowx_map2.setRange(-1.0, 1.0);
    ScalarMap flowy_map2 = new ScalarMap(flowy, Display.Flow1Y);
    display2.addMap(flowy_map2);
    flowy_map2.setRange(-1.0, 1.0);
    flow_control = (FlowControl) flowy_map2.getControl();
    if (!slice3d) {
      flow_control.setFlowScale(0.045f); // WLH
    }
    else {
      flow_control.setFlowScale(0.005f);
    }

    ScalarMap slice_map = new ScalarMap(yaxis_winds, Display.SelectRange);
    display2.addMap(slice_map);
    final RangeControl control = (RangeControl) slice_map.getControl();

    // set up white rectangle in left-hand display
    final RealType rect_x = RealType.getRealTypeByName("Longitude");
    final RealType rect_y = RealType.getRealTypeByName("Latitude");
    final RealTupleType rect_type = new RealTupleType(rect_x, rect_y);
    rect_ref = new DataReferenceImpl("rect_ref");
    double[] x_range = xmap1.getRange();
    double[] y_range = ymap1.getRange();
    float xmn = (float) x_range[0];
    float xmx = (float) x_range[1];
    float ymn = (float) y_range[0];
    float ymx = (float) y_range[1];
    float[][] samps = { {xmn, xmn, xmx, xmx, xmn},
                        {ymn, ymx, ymx, ymn, ymn} };
    rect_ref.setData(new Gridded2DSet(rect_type, samps, 5));
    control.addControlListener(new ControlListener() {
      public void controlChanged(ControlEvent e) {
        // draw red rectangle in left-hand display
        double[] xr = xmap1.getRange();
        float[] yr = control.getRange();
        float xmin = (float) xr[0];
        float xmax = (float) xr[1];
        float ymin = yr[0];
        float ymax = yr[1];
        float[][] samples = { {xmin, xmin, xmax, xmax, xmin},
                              {ymin, ymax, ymax, ymin, ymin} };
        try {
          Gridded2DSet set = new Gridded2DSet(rect_type, samples, 5);
          rect_ref.setData(set);
        }
        catch (VisADException exc) { /* CTR: TEMP */ exc.printStackTrace(); }
        catch (RemoteException exc) { /* CTR: TEMP */ exc.printStackTrace(); }
      }
    });
    rect_constMap = new ConstantMap[] {
                        new ConstantMap(1, Display.Red),
                        new ConstantMap(1, Display.Green),
                        new ConstantMap(1, Display.Blue),
                        new ConstantMap(-.99, Display.ZAxis) };

    // finish left-hand display set-up
    display.addReference(rect_ref, rect_constMap);
    display.addReference(image_ref, img_constMap);
    display.addReference(maplines_ref, map_constMap);
    display.addReference(winds_ref[0], wnd_constMap[0]);

    SelectRangeWidget slice_sw = new SelectRangeWidget(slice_map);
    s_panel = new JPanel();
    s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
    s_panel.setBorder(etchedBorder);
    s_panel.add(slice_sw);
    r_panel.add(s_panel);

    len = new_winds.length;
    clone_winds_ref = new DataReferenceImpl[len];
    for (int i=0; i<len; i++) {
      clone_winds_ref[i] = new DataReferenceImpl("slice_ref" + i);
      clone_winds_ref[i].setData(new_winds[i]);
    }

    if ( !slice3d ) {
      display2.addReferences(new BarbRendererJ2D(), clone_winds_ref[0],
                             clone_wnd_constMap[0]);
    }
    else {
      display2.addReference(clone_winds_ref[0], clone_wnd_constMap[0]);
      display2.addReference(maplines_ref, map_constMap2);
    }
    

    big_panel.add(l_panel);
    big_panel.add(r_panel);

    frame.getContentPane().add(big_panel);
    frame.pack();
    frame.setVisible(true);
  }

  DataImpl[] getWinds( String[][] filenames, TextForm form )
             throws VisADException, RemoteException, IOException
  {
    int n_groups = filenames.length;
    Tuple[] winds = new Tuple[n_groups];

    for (int g=0; g<n_groups; g++) {
      int n_files = filenames[g].length;
      Data[] field_s = new Data[n_files];

      flowxy = new EarthVectorType(flowx, flowy);
      coord_sys = new WindPolarCoordinateSystem(flowxy);

      for ( int kk = 0; kk < n_files; kk++ )
      {
        data = form.open( filenames[g][kk] );
        type = data.getType();

        FunctionType ft = (FunctionType) type;
        RealTupleType range_tuple = (RealTupleType) ft.getRange();
        int index = range_tuple.getIndex("Longitude");
        xaxis = (RealType) range_tuple.getComponent(index);
        index = range_tuple.getIndex("Latitude");
        yaxis = (RealType) range_tuple.getComponent(index);
        index = range_tuple.getIndex("hpa");
        zaxis = (RealType) range_tuple.getComponent(index);


        int tup_dim = range_tuple.getDimension();
        int n_samples = ((FlatField)data).getLength();
        double[][] new_values = new double[tup_dim][];
        double[][] values = ((FlatField)data).getValues();
        MathType[] types = new MathType[tup_dim];
        int new_dim = 0;
        for ( int ii = 0; ii < tup_dim; ii++ )
        {
          RealType comp = (RealType)range_tuple.getComponent(ii);
          String name = comp.getName();
          if ( name.equals("spd") ) {
            dir_spd[1] = values[ii];
          }
          else if ( name.equals("dir") ) {
            dir_spd[0] = values[ii];
          }
          else {
            types[new_dim] = comp;
            new_values[new_dim] = values[ii];
            new_dim++;
          }
        }

        uv = coord_sys.toReference(dir_spd);

        int idx = new_dim;
        int idx2 = new_dim;

        types[idx++] = flowxy;
        new_values[idx2++] = uv[0];
        new_values[idx2++] = uv[1];

        MathType[] new_types = new MathType[idx];
        System.arraycopy(types, 0, new_types, 0, idx);
        MathType new_range = new TupleType(new_types);

        FunctionType func_type = new FunctionType(ft.getDomain(), new_range);
        Set set = ((FlatField) data).getDomainSet();
        FlatField new_field = new FlatField(func_type, set);

        new_field.setSamples(new_values);
        field_s[kk] = new_field;
      }

      winds[g] = new Tuple(field_s);
    }
    return winds;
  }

  // this table may be overkill, but hey, it works
  float[][] table = {
    {0.363f, 0.364f, 0.364f, 0.365f, 0.365f, 0.366f, 0.366f, 0.367f,
     0.368f, 0.368f, 0.369f, 0.369f, 0.37f, 0.37f, 0.371f, 0.372f,
     0.372f, 0.373f, 0.374f, 0.374f, 0.375f, 0.376f, 0.376f, 0.377f,
     0.378f, 0.378f, 0.379f, 0.38f, 0.381f, 0.381f, 0.382f, 0.383f,
     0.384f, 0.385f, 0.386f, 0.387f, 0.387f, 0.388f, 0.389f, 0.39f,
     0.391f, 0.392f, 0.393f, 0.394f, 0.395f, 0.397f, 0.398f, 0.399f,
     0.4f, 0.401f, 0.402f, 0.404f, 0.405f, 0.406f, 0.408f, 0.409f,
     0.41f, 0.412f, 0.413f, 0.415f, 0.416f, 0.418f, 0.42f, 0.421f,
     0.423f, 0.425f, 0.425f, 0.425f, 0.426f, 0.426f, 0.426f, 0.427f,
     0.427f, 0.427f, 0.427f, 0.428f, 0.428f, 0.429f, 0.430f, 0.431f,
     0.432f, 0.433f, 0.434f, 0.435f, 0.436f, 0.437f, 0.438f, 0.439f,
     0.440f, 0.441f, 0.442f, 0.443f, 0.444f, 0.445f, 0.45f, 0.451f,
     0.452f, 0.453f, 0.454f, 0.455f, 0.456f, 0.457f, 0.458f, 0.459f,
     0.46f, 0.461f, 0.462f, 0.463f, 0.464f, 0.465f, 0.466f, 0.47f,
     0.471f, 0.472f, 0.473f, 0.474f, 0.475f, 0.476f, 0.477f, 0.478f,
     0.48f, 0.481f, 0.482f, 0.483f, 0.485f, 0.489f, 0.495f, 0.497f,
     0.506f, 0.518f, 0.53f, 0.542f, 0.554f, 0.566f, 0.577f, 0.589f,
     0.6f, 0.611f, 0.622f, 0.632f, 0.642f, 0.652f, 0.661f, 0.671f,
     0.679f, 0.688f, 0.696f, 0.704f, 0.712f, 0.719f, 0.727f, 0.733f,
     0.74f, 0.746f, 0.753f, 0.758f, 0.764f, 0.77f, 0.775f, 0.78f,
     0.785f, 0.789f, 0.794f, 0.798f, 0.803f, 0.807f, 0.81f, 0.814f,
     0.818f, 0.821f, 0.825f, 0.828f, 0.831f, 0.834f, 0.837f, 0.84f,
     0.843f, 0.846f, 0.848f, 0.851f, 0.853f, 0.856f, 0.858f, 0.86f,
     0.862f, 0.864f, 0.866f, 0.868f, 0.87f, 0.872f, 0.874f, 0.876f,
     0.878f, 0.879f, 0.881f, 0.883f, 0.884f, 0.886f, 0.887f, 0.889f,
     0.89f, 0.891f, 0.893f, 0.894f, 0.895f, 0.897f, 0.898f, 0.899f,
     0.9f, 0.901f, 0.902f, 0.904f, 0.905f, 0.906f, 0.907f, 0.908f,
     0.909f, 0.91f, 0.911f, 0.912f, 0.912f, 0.913f, 0.914f, 0.915f,
     0.916f, 0.917f, 0.918f, 0.918f, 0.919f, 0.92f, 0.921f, 0.921f,
     0.922f, 0.923f, 0.923f, 0.924f, 0.925f, 0.925f, 0.926f, 0.927f,
     0.927f, 0.928f, 0.929f, 0.929f, 0.93f, 0.93f, 0.931f, 0.931f,
     0.932f, 0.933f, 0.933f, 0.934f, 0.934f, 0.935f, 0.935f, 0.936f},

    {0.032f, 0.034f, 0.036f, 0.037f, 0.04f, 0.042f, 0.044f, 0.046f,
     0.049f, 0.051f, 0.054f, 0.057f, 0.059f, 0.062f, 0.066f, 0.069f,
     0.072f, 0.076f, 0.079f, 0.083f, 0.087f, 0.091f, 0.095f, 0.099f,
     0.104f, 0.108f, 0.113f, 0.118f, 0.123f, 0.129f, 0.134f, 0.14f,
     0.145f, 0.151f, 0.158f, 0.164f, 0.17f, 0.177f, 0.184f, 0.191f,
     0.198f, 0.206f, 0.213f, 0.221f, 0.229f, 0.237f, 0.246f, 0.254f,
     0.263f, 0.272f, 0.281f, 0.29f,0.3f, 0.31f, 0.319f, 0.329f, 0.34f,
     0.35f, 0.36f, 0.371f, 0.382f, 0.393f, 0.404f, 0.415f, 0.427f,
     0.438f, 0.45f, 0.461f, 0.473f, 0.485f, 0.497f, 0.509f, 0.522f,
     0.534f, 0.546f, 0.559f, 0.571f, 0.583f, 0.596f, 0.608f, 0.621f,
     0.633f, 0.646f, 0.658f, 0.67f, 0.683f, 0.695f, 0.707f, 0.719f,
     0.731f, 0.743f, 0.754f, 0.766f, 0.777f, 0.789f, 0.8f, 0.811f,
     0.821f, 0.832f, 0.842f, 0.852f, 0.862f, 0.871f, 0.881f, 0.89f,
     0.898f, 0.907f, 0.915f, 0.922f, 0.93f, 0.937f, 0.944f, 0.95f,
     0.956f, 0.962f, 0.967f, 0.972f, 0.977f, 0.981f, 0.984f, 0.988f,
     0.991f, 0.993f, 0.995f, 0.997f, 0.998f, 0.999f, 0.999f, 0.999f,
     0.999f, 0.998f, 0.997f, 0.995f, 0.993f, 0.991f, 0.988f, 0.984f,
     0.981f, 0.977f, 0.972f, 0.967f, 0.962f, 0.956f, 0.95f, 0.944f,
     0.937f, 0.93f, 0.922f, 0.915f, 0.907f, 0.898f, 0.89f, 0.881f,
     0.871f, 0.862f, 0.852f, 0.842f, 0.832f, 0.821f, 0.811f, 0.8f,
     0.789f, 0.777f, 0.766f, 0.754f, 0.743f, 0.731f, 0.719f, 0.707f,
     0.695f, 0.683f, 0.67f, 0.658f, 0.646f, 0.633f, 0.621f, 0.608f,
     0.596f, 0.583f, 0.571f, 0.559f, 0.546f, 0.534f, 0.522f, 0.509f,
     0.497f, 0.485f, 0.473f, 0.461f, 0.45f, 0.438f, 0.427f, 0.415f,
     0.404f, 0.393f, 0.382f, 0.371f, 0.36f, 0.35f, 0.34f, 0.329f,
     0.319f, 0.31f, 0.3f, 0.29f, 0.281f, 0.272f, 0.263f, 0.254f,
     0.246f, 0.237f, 0.229f, 0.221f, 0.213f, 0.206f, 0.198f, 0.191f,
     0.184f, 0.177f, 0.17f, 0.164f, 0.158f, 0.151f, 0.145f, 0.14f,
     0.134f, 0.129f, 0.123f, 0.118f, 0.113f, 0.108f, 0.104f, 0.099f,
     0.095f, 0.091f, 0.087f, 0.083f, 0.079f, 0.076f, 0.072f, 0.069f,
     0.065f, 0.062f, 0.059f, 0.057f, 0.054f, 0.051f, 0.049f, 0.046f,
     0.044f, 0.042f, 0.04f, 0.037f, 0.036f, 0.034f, 0.032f},

    {0.936f, 0.935f, 0.935f, 0.934f, 0.934f, 0.933f, 0.933f, 0.932f,
     0.931f, 0.931f, 0.93f, 0.93f, 0.929f, 0.929f, 0.928f, 0.927f,
     0.927f, 0.926f, 0.925f, 0.925f, 0.924f, 0.923f, 0.923f, 0.922f,
     0.921f, 0.921f, 0.92f, 0.919f, 0.918f, 0.918f, 0.917f, 0.916f,
     0.915f, 0.914f, 0.913f, 0.912f, 0.912f, 0.911f, 0.91f, 0.909f,
     0.908f, 0.907f, 0.906f, 0.905f, 0.904f, 0.902f, 0.901f, 0.9f,
     0.899f, 0.898f, 0.897f, 0.895f, 0.894f, 0.893f, 0.891f, 0.89f,
     0.889f, 0.887f, 0.886f, 0.884f, 0.883f, 0.881f, 0.879f, 0.878f,
     0.876f, 0.874f, 0.872f, 0.87f, 0.868f, 0.866f, 0.864f, 0.862f,
     0.86f, 0.858f, 0.856f, 0.853f, 0.851f, 0.848f, 0.846f, 0.843f,
     0.84f, 0.837f, 0.834f, 0.831f, 0.828f, 0.825f, 0.821f, 0.818f,
     0.814f, 0.81f, 0.807f, 0.803f, 0.798f, 0.794f, 0.789f, 0.785f,
     0.78f, 0.775f, 0.77f, 0.764f, 0.758f, 0.753f, 0.746f, 0.74f,
     0.733f, 0.727f, 0.719f, 0.712f, 0.704f, 0.696f, 0.688f, 0.679f,
     0.671f, 0.661f, 0.652f, 0.642f, 0.632f, 0.622f, 0.611f, 0.6f,
     0.589f, 0.577f, 0.566f, 0.554f, 0.542f, 0.53f, 0.518f, 0.506f,
     0.493f, 0.481f, 0.469f, 0.457f, 0.445f, 0.433f, 0.422f, 0.41f,
     0.399f, 0.388f, 0.377f, 0.367f, 0.357f, 0.347f, 0.338f, 0.328f,
     0.32f, 0.311f, 0.303f, 0.295f, 0.287f, 0.28f, 0.272f, 0.266f,
     0.259f, 0.253f, 0.246f, 0.241f, 0.235f, 0.229f, 0.224f, 0.219f,
     0.214f, 0.21f, 0.205f, 0.201f, 0.196f, 0.192f, 0.189f, 0.185f,
     0.181f, 0.178f, 0.174f, 0.171f, 0.168f, 0.165f, 0.162f, 0.159f,
     0.156f, 0.153f, 0.151f, 0.148f, 0.146f, 0.143f, 0.141f, 0.139f,
     0.137f, 0.135f, 0.133f, 0.131f, 0.129f, 0.127f, 0.125f, 0.123f,
     0.121f, 0.12f, 0.118f, 0.116f, 0.115f, 0.113f, 0.112f, 0.11f,
     0.109f, 0.108f, 0.106f, 0.105f, 0.104f, 0.102f, 0.101f, 0.1f,
     0.099f, 0.098f, 0.097f, 0.095f, 0.094f, 0.093f, 0.092f, 0.091f,
     0.09f, 0.089f, 0.088f, 0.087f, 0.087f, 0.086f, 0.085f, 0.084f,
     0.083f, 0.082f, 0.081f, 0.081f, 0.08f, 0.079f, 0.078f, 0.078f,
     0.077f, 0.076f, 0.076f, 0.075f, 0.074f, 0.074f, 0.073f, 0.072f,
     0.072f, 0.071f, 0.07f, 0.07f, 0.069f, 0.069f, 0.068f, 0.068f,
     0.067f, 0.066f, 0.066f, 0.065f, 0.065f, 0.064f, 0.064f, 0.063f}
  };

  int numSelected = 1;
  boolean gifts_checked = true;
  boolean goes_checked = false;
  boolean truth_checked = false;

  public void itemStateChanged(ItemEvent e) {
    JCheckBox source = (JCheckBox) e.getItemSelectable();
    String text = source.getText();
    boolean checked = (e.getStateChange() == ItemEvent.SELECTED);

    if (source == multi_color) {
      try {
        display.removeAllReferences();
        display2.removeAllReferences();
        if (checked) {
          // add "rainbow" RGB maps
          ScalarMap map = new ScalarMap(zaxis, Display.RGB);
          display.addMap(map);
          ScalarMap map2 = new ScalarMap(zaxis, Display.RGB);
          display2.addMap(map2);

          // set up color table
          ((ColorControl) map.getControl()).setTable(table);
          ((ColorControl) map2.getControl()).setTable(table);
        }
        else {
          // remove "rainbow" RGB map
          display.clearMaps();
          display.addMap(xmap1);
          display.addMap(ymap1);
          display.addMap(zmap);
          display.addMap(flowx_map);
          display.addMap(flowy_map);
          display.addMap(sel_map);
          display.addMap(rgb_map);
          display2.clearMaps();
          display2.addMap(xmap2);
          display2.addMap(xmap3);
          display2.addMap(ymap2);
          display2.addMap(ymap3);
          display2.addMap(zmap2);
          display2.addMap(flowx_map2);
          display2.addMap(flowy_map2);
          display2.addMap(slice_map);
        }

        // add old references back
        display.addReference(rect_ref, rect_constMap);
        display.addReference(image_ref, img_constMap);
        display.addReference(maplines_ref, map_constMap);
        int i = -1;
        if (gifts_checked) i = 0;
        else if (goes_checked) i = 1;
        else if (truth_checked) i = 2;
        if (i >= 0) {
          display.addReference(winds_ref[i], wnd_constMap[i]);
          if ( !slice3d ) {
            display2.addReferences(new BarbRendererJ2D(), clone_winds_ref[i],
                                   clone_wnd_constMap[i]);
          }
          else {
            display2.addReference(clone_winds_ref[i], clone_wnd_constMap[i]);
            display2.addReference(maplines_ref, map_constMap2);
          }
        }
      }
      catch (VisADException exc) { /* CTR: TEMP */ exc.printStackTrace(); }
      catch (RemoteException exc) { /* CTR: TEMP */ exc.printStackTrace(); }
    }
    else if (multi_color.isSelected()) {
      // turn off multi-color display first
      multi_color.setSelected(false);
    }

    int i = -1;
    if (text.equals("GIFTS")) {
      i = 0;
      gifts_checked = checked;
    }
    else if (text.equals("GOES")) {
      i = 1;
      goes_checked = checked;
    }
    else if (text.equals("TRUTH")) {
      i = 2;
      truth_checked = checked;
    }
    if (i >= 0) {
      try {
        if (checked) {
          display.addReference(winds_ref[i], wnd_constMap[i]);
          if ( !slice3d ) {
            display2.addReferences(new BarbRendererJ2D(), clone_winds_ref[i],
                                   clone_wnd_constMap[i]);
          }
          else {
            display2.addReference(clone_winds_ref[i], clone_wnd_constMap[i]);
          }
          numSelected++;
        }
        else {
          display.removeReference(winds_ref[i]);
          display2.removeReference(clone_winds_ref[i]);
          numSelected--;
        }
      }
      catch (VisADException exc) { /* CTR: TEMP */ exc.printStackTrace(); }
      catch (RemoteException exc) { /* CTR: TEMP */ exc.printStackTrace(); }

      multi_color.setEnabled(numSelected == 1);
    }
  }

  public void mapChanged(ScalarMapEvent e)
       throws VisADException, RemoteException
  {
    if ( xmap1.equals(e.getScalarMap()) ) { 
      xmapEvent = true;
    }
    else if ( ymap1.equals(e.getScalarMap()) ) {
      ymapEvent = true;
    }
    if (( xmapEvent && ymapEvent ) && !(firstEvent) ) {
      x_range = xmap1.getRange();
      y_range = ymap1.getRange();
      latmin = (float)y_range[0];
      latmax = (float)y_range[1];
      lonmin = (float)x_range[0];
      lonmax = (float)x_range[1];
      baseMap.setLatLonLimits(latmin, latmax, lonmin, lonmax);
      DataImpl map = baseMap.getData();
      maplines_ref.setData(map);
      firstEvent = true;
    }
  }

  public void controlChanged(ScalarMapControlEvent e) { }

  DataImpl[] cloneWinds(DataImpl[] winds)
             throws VisADException, RemoteException
  {
    int n_groups = winds.length;
    DataImpl[] cloned_winds = new DataImpl[n_groups];

    RealType Latitude_winds = new RealType("Latitude_winds",
                                  CommonUnit.degree, null);
    RealType Longitude_winds = new RealType("Longitude_winds",
                                   CommonUnit.degree, null);

    for (int g=0; g<n_groups; g++) {
      int n_comps = ((TupleType)winds[g].getType()).getDimension();
      MathType[] new_type_s = new MathType[n_comps];

      for ( int ii = 0; ii < n_comps; ii++ ) {
        Data d = ((Tuple) winds[g]).getComponent(ii);
        FunctionType org_type = (FunctionType) d.getType();
        TupleType org_range = (TupleType)org_type.getRange();
        int n_range = org_range.getDimension();
        MathType[] new_range = new MathType[n_range];
        for ( int jj = 0; jj < n_range; jj++ ) {
          MathType r_type = org_range.getComponent(jj);
          if (r_type instanceof RealType) {
            if ( ((RealType)r_type).getName().equals("Latitude") ) {
              new_range[jj] = Latitude_winds;
            }
            else if ( ((RealType)r_type).getName().equals("Longitude") ) {
              new_range[jj] = Longitude_winds;
            }
            else {
              new_range[jj] = r_type;
            }
          }
          else {
            new_range[jj] = r_type;
          }
        }
        new_type_s[ii] = new FunctionType(org_type.getDomain(), 
                             new TupleType(new_range) );
      }
      TupleType new_type = new TupleType(new_type_s);

      cloned_winds[g] = (DataImpl) winds[g].changeMathType(new_type);
    }

    return cloned_winds;
  }
}
