/*

@(#) $Id: SliderLabel.java,v 1.2 1998-02-20 16:55:30 billh Exp $

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

/**
 * A label that can be attached to any slider showing the current value,
 * and optionally, the bounds.
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.2 $, $Date: 1998-02-20 16:55:30 $
 * @since Visad Utility Library v0.7.1
 */

public class SliderLabel extends Panel implements SliderChangeListener {

	/** The slider to which the label is attached */
	private Slider slider;

	/** The label representing the slider's variable */
	private String label;

	/** Whether or not the range values are visible */
	private boolean rangeVisible;

	/** The backround color of the panel */
	private Color background;
	
	/** The text color of the panel */
	private Color text;


	/** Construct a SliderLabel from the given slider */
	public SliderLabel(Slider slider) {
		this(slider, slider.getName());
	}

	/** Construct a SliderLabel with the given background and text colors */
	public SliderLabel(Slider slider, Color background, Color text) {
		this(slider, slider.getName(), background, text);
	}
	
	/** Construct a SliderLabel with the given label, background and text colors */
	public SliderLabel(Slider slider, String label, Color background, Color text) {
		this(slider, label, true, background, text);
	}

	/** Construct a slider label with the given slider and label */
	public SliderLabel(Slider slider, String label) {
		this(slider, label, true);
	}
		
	/** Construct a slider label with the given slider, label and range visibility */
	public SliderLabel(Slider slider, String label, boolean rangeVisible) {
		this(slider, label, rangeVisible, Color.black, Color.white);
	}
	
	/** Construct a slider label with the given slider, label and range visibility */
	public SliderLabel(Slider slider, String label, boolean rangeVisible,
						Color background, Color text) {
	
		this.slider = slider;
		this.label = label;
		this.rangeVisible = rangeVisible;
		this.background = background;
		this.text = text;
		
		slider.addSliderChangeListener(this);
		
	}
	
	/** Listen for slider change events */
	public void sliderChanged(SliderChangeEvent e) {
		if (e.type != e.VALUE_CHANGE) {
			rangeChanged = true;
		}
// won't update on repaint, so hit it with a big hammer
// update(getGraphics());
// but update isn't right, so hit it harder
paint(getGraphics());
		repaint();
	}
	
	private boolean rangeChanged;
	private String drawmin;
	private String drawmax;
	private String drawval;
		
	/** Update the panel */
	public void update(Graphics g) {
	
		FontMetrics fm = g.getFontMetrics();
		
		if (rangeVisible) {
			if (rangeChanged) {
				g.setColor(background);
				g.drawString(drawmin, 3, getBounds().height - 1 - fm.getDescent());
				g.drawString(drawmax, getBounds().width - 4 - fm.stringWidth(drawmax), 
								getBounds().height - 1 - fm.getDescent());
				rangeChanged = false;
			}
			g.setColor(text);
			
			String min = Float.toString(slider.getMinimum());
			g.drawString(min, 3, getBounds().height - 1 - fm.getDescent());
			drawmin = min;
			
			String max = Float.toString(slider.getMaximum());
			g.drawString(max, getBounds().width - 4 - fm.stringWidth(max), 
							getBounds().height - 1 - fm.getDescent());
			drawmax = max;
		}
		
		g.setColor(background);
		g.drawString(drawval, getBounds().width / 2 - fm.stringWidth(drawval) / 2 + 3,
							getBounds().height - 1 - fm.getDescent());
		
		g.setColor(text);
		//String val = new String(label + " = " + (slider.getValue() - (slider.getValue() % 0.01)));
		String val = new String(label + " = " + (slider.getValue()));
		g.drawString(val, getBounds().width / 2 - fm.stringWidth(val) / 2 + 3,
							getBounds().height - 1 - fm.getDescent());
							
		drawval = val;
	}

	/** Draw the panel */
	public void paint(Graphics g) {
		g.setColor(background);
		g.fillRect(0, 0, getBounds().width, getBounds().height);
		
		g.setColor(text);
		
		FontMetrics fm = g.getFontMetrics();
		
		if (rangeVisible) {
			
			String min = Float.toString(slider.getMinimum());
			g.drawString(min, 3, getBounds().height - 1 - fm.getDescent());
			drawmin = min;
			
			String max = Float.toString(slider.getMaximum());
			g.drawString(max, getBounds().width - 4 - fm.stringWidth(max), 
							getBounds().height - 1 - fm.getDescent());
			drawmax = max;
		}
		
		//String val = new String(label + " = " + (slider.getValue() - (slider.getValue() % 0.01)));
		String val = new String(label + " = " + (slider.getValue()));
		g.drawString(val, getBounds().width / 2 - fm.stringWidth(val) / 2 + 3,
							getBounds().height - 1 - fm.getDescent());
							
		drawval = val;
	}
		
	/** Return the preferred Size of the SliderLabel */
	public Dimension getPreferredSize() {
		return new Dimension(256, 18);
	}
	
	/** Return the minimum Size of the SliderLabel */
	public Dimension getMinimumSize() {
		return new Dimension(100, 18);
	}
	
	/** Return the maximum Size of the SliderLabel */
	public Dimension getMaximumSize() {
		return new Dimension(Integer.MAX_VALUE, 18);
	}
	
	/** for debugging purposes */
	public static void main(String[] argc) {
	
		Slider slider = new ArrowSlider();
		SliderLabel label = new SliderLabel(slider, "test");
		
		Frame f = new Frame("Visad Slider Label");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});
		
		f.setLayout(new BorderLayout());
		f.add(label, "South");
		f.add(slider, "North");
		
		int height = slider.getPreferredSize().height + label.getPreferredSize().height;
		int width = Math.max(slider.getPreferredSize().width, label.getPreferredSize().height);
					
		f.setSize(new Dimension(width, height + 27));
		f.setVisible(true);
		
	}
	
}
