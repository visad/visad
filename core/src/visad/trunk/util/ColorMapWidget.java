/*

@(#) $Id: ColorMapWidget.java,v 1.3 1998-06-24 14:14:27 billh Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 1998 Nick Rasmussen
VisAD is Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

package visad.util;

import visad.*;
import java.rmi.RemoteException;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.Vector;

import java.awt.swing.*;

/** 
 * A color widget that allows users to interactively map numeric data to
 * RGB tuples based on the Vis5D color widget
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.3 $, $Date: 1998-06-24 14:14:27 $
 * @since Visad Utility Library v0.7.1
 */

public class LabeledRGBWidget extends Panel  {

	private Slider slider;
	
	private ColorWidget widget;
	
	private SliderLabel label;

	public LabeledRGBWidget() {
		this(new ColorWidget(), new ArrowSlider());
	}
	
	public LabeledRGBWidget(String name, float min, float max) {
		this(new ColorWidget(), new ArrowSlider(min, max, (min + max) / 2, name));
	}
	
	public LabeledRGBWidget(String name, float min, float max, float[][] table) {
		this(new ColorWidget(new RGBMap(table)), new ArrowSlider(min, max, (min + max) / 2, name));
	}
	
	public LabeledRGBWidget(ColorWidget c, Slider s) {
		this(c, s, new SliderLabel(s));
	}

	
	public LabeledRGBWidget(ColorWidget c, Slider s, SliderLabel l) {
	
		widget = c;
		slider = s;
		label = l;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(widget);
		add(slider);
		add(label);
	}

  ColorControl colorcontrol;

  /** construct a LabeledRGBWidget linked to the ColorControl
      in map (which must be to Display.RGB), with range of
      values from map.getRange() */
  public LabeledRGBWidget(ScalarMap smap)
         throws VisADException, RemoteException {
    this(smap, smap.getRange(), null);
  }

  /** construct a LabeledRGBWidget linked to the ColorControl
      in map (which must be to Display.RGB), with range of
      values (min, max) */
  public LabeledRGBWidget(ScalarMap smap, float min, float max)
         throws VisADException, RemoteException {
    this(smap, make_range(min, max), null);
    smap.setRange((double) min, (double) max);
  }

  /** construct a LabeledRGBWidget linked to the ColorControl
      in map (which must be to Display.RGB), with range of
      values (min, max), and initial color table in format
      float[TABLE_SIZE][3] with values between 0.0f and 1.0f */
  public LabeledRGBWidget(ScalarMap smap, float min, float max, float[][] table)
         throws VisADException, RemoteException {
    this(smap, make_range(min, max), table);
    smap.setRange((double) min, (double) max);
  }

  private LabeledRGBWidget(ScalarMap smap, double[] range, float[][] in_table)
         throws VisADException, RemoteException {
    this(smap.getScalar().getName(), (float) range[0], (float) range[1],
         table_reorg(in_table));
    if (!Display.RGB.equals(smap.getDisplayScalar())) {
      throw new DisplayException("LabeledRGBWidget: ScalarMap must " +
                                 "be to Display.RGB");
    }
    if (range[0] != range[0] || range[1] != range[1] ||
        Double.isInfinite(range[0]) || Double.isInfinite(range[1]) ||
        range[0] == Double.MAX_VALUE || range[1] == -Double.MAX_VALUE) {
      throw new DisplayException("LabeledRGBWidget: bad range");
    }
    colorcontrol = (ColorControl) smap.getControl();
 
    ColorMap map = widget.getColorMap();
    final int TABLE_SIZE = map.getMapResolution(); 
    final float SCALE = 1.0f / (TABLE_SIZE - 1.0f);

    float[][] table = new float[3][TABLE_SIZE];
 
    for (int i=0; i<TABLE_SIZE; i++) {
      float[] t = map.getRGBTuple(SCALE * i);
      table[0][i] = t[0];
      table[1][i] = t[1];
      table[2][i] = t[2];
    }
    colorcontrol.setTable(table);

    widget.addColorChangeListener(new ColorChangeListener() {
      public void colorChanged(ColorChangeEvent e) {
        ColorMap map_e = widget.getColorMap();
        float[][] table_e = new float[3][TABLE_SIZE];
        for (int i=0; i<TABLE_SIZE; i++) {
          float[] t = map_e.getRGBTuple(SCALE * i);
          table_e[0][i] = t[0];
          table_e[1][i] = t[1];
          table_e[2][i] = t[2];
        }
        try {
          colorcontrol.setTable(table_e);
        }
        catch (VisADException f) {
        }
        catch (RemoteException f) {
        }
      }
    });

  }

  private static float[][] table_reorg(float[][] table) {
    if (table == null || table[0] == null) return null;
    try {
      int len = table[0].length;
      float[][] out = new float[len][3];
      for (int i=0; i<len; i++) {
        out[i][0] = table[0][i];
        out[i][1] = table[1][i];
        out[i][2] = table[2][i];
      }
      return out;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  private static double[] make_range(float min, float max) {
    double[] range = {(double) min, (double) max};
    return range;
  }
		
        /** Returns the ColorMap that the color wdget is curently pointing to */
        public ColorWidget getColorWidget() {
                return widget;
        }
	
	/** for debugging purposes */
	public static void main(String[] argc) {
	
		LabeledRGBWidget l = new LabeledRGBWidget("label", 0, 1);
		
		Frame f = new Frame("Visad Widget Test");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});
		
		f.add(l);
		
		f.setSize(f.getPreferredSize());
		f.setVisible(true);
		
	}
	
}

