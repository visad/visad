/*

@(#) $Id: WidgetTest.java,v 1.2 1998-02-20 16:55:31 billh Exp $

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
 * @version $Revision 1.2 $, $Date: 1998-02-20 16:55:31 $
 * @since Visad Utility Library v0.7.1
 */

public class WidgetTest extends Applet  {
	
	/** for debugging purposes */
	public static void main(String[] argc) {
	
		Slider slider = new BarSlider();
		//Slider slider = new ArrowSlider();
		SliderLabel label = new SliderLabel(slider, "value");
		
		ColorWidget widget = new ColorWidget();
		
		Frame f = new Frame("Visad Widget Test");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});
		
		f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
		f.add(widget);
		f.add(slider);
		f.add(label);
		
		f.setSize(f.getPreferredSize());
		f.setVisible(true);
		
	}
	
}
