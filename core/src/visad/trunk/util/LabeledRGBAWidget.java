/*

@(#) $Id: LabeledRGBAWidget.java,v 1.1 1998-02-20 16:55:29 billh Exp $

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
 * @version $Revision: 1.1 $, $Date: 1998-02-20 16:55:29 $
 * @since Visad Utility Library v0.7.1
 */

public class LabeledRGBAWidget extends Panel  {

	private Slider slider;
	
	private ColorWidget widget;
	
	private SliderLabel label;

	public LabeledRGBAWidget() {
		this(new ColorWidget(new RGBAMap()), new ArrowSlider());
	}
	
	public LabeledRGBAWidget(String name, float min, float max) {
		this(new ColorWidget(new RGBAMap()), new ArrowSlider(min, max, (min + max) / 2, name));
	}
	
	public LabeledRGBAWidget(ColorWidget c, Slider s) {
		this(c, s, new SliderLabel(s));
	}

	
	public LabeledRGBAWidget(ColorWidget c, Slider s, SliderLabel l) {
	
		widget = c;
		slider = s;
		label = l;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(widget);
		add(slider);
		add(label);
	}

  final static int TABLE_SIZE = 256;
  final static float SCALE = 1.0f / (TABLE_SIZE - 1.0f);
  ColorAlphaControl color_alpha_control;

  /** construct a LabeledRGBAWidget linked to the ColorControl
      in map (which must be to Display.RGB), with range of
      values from map.getRange() */
  public LabeledRGBAWidget(ScalarMap smap)
         throws VisADException, RemoteException {
    this(smap, smap.getRange());
  }

  /** construct a LabeledRGBAWidget linked to the ColorControl
      in map (which must be to Display.RGB), with range of
      values (min, max) */
  public LabeledRGBAWidget(ScalarMap smap, float min, float max)
         throws VisADException, RemoteException {
    this(smap, make_range(min, max));
    smap.setRange((double) min, (double) max);
  }

  private LabeledRGBAWidget(ScalarMap smap, double[] range)
         throws VisADException, RemoteException {
    this(smap.getScalar().getName(), (float) range[0], (float) range[1]);
    if (!Display.RGBA.equals(smap.getDisplayScalar())) {
      throw new DisplayException("LabeledRGBAWidget: ScalarMap must " +
                                 "be to Display.RGBA");
    }
    if (range[0] != range[0] || range[1] != range[1] ||
        Double.isInfinite(range[0]) || Double.isInfinite(range[1]) ||
        range[0] == Double.MAX_VALUE || range[1] == -Double.MAX_VALUE) {
      throw new DisplayException("LabeledRGBAWidget: bad range");
    }
    color_alpha_control = (ColorAlphaControl) smap.getControl();
 
    float[][] table = new float[4][TABLE_SIZE];
 
    ColorMap map = widget.getColorMap();
    for (int i=0; i<TABLE_SIZE; i++) {
      float[] t = map.getTuple(SCALE * i);
      table[0][i] = t[0];
      table[1][i] = t[1];
      table[2][i] = t[2];
      table[3][i] = t[3];
    }
    color_alpha_control.setTable(table);

    widget.addColorChangeListener(new ColorChangeListener() {
      public void colorChanged(ColorChangeEvent e) {
        ColorMap map_e = widget.getColorMap();
        float[][] table_e = new float[4][TABLE_SIZE];
        for (int i=0; i<TABLE_SIZE; i++) {
          float[] t = map_e.getTuple(SCALE * i);
          table_e[0][i] = t[0];
          table_e[1][i] = t[1];
          table_e[2][i] = t[2];
          table_e[3][i] = t[3];
        }
        try {
          color_alpha_control.setTable(table_e);
        }
        catch (VisADException f) {
        }
        catch (RemoteException f) {
        }
      }
    });

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
	
		LabeledRGBAWidget l = new LabeledRGBAWidget("label", 0, 1);
		
		Frame f = new Frame("Visad Widget Test");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});
		
		f.add(l);
		
		f.setSize(f.getPreferredSize());
		f.setVisible(true);
		
	}
	
}

