/*

@(#) $Id: SliderLabel.java,v 1.12 2000-03-14 17:18:40 dglo Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2017 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;

/**
 * A label that can be attached to any slider showing the current value,
 * and optionally, the bounds.
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.12 $, $Date: 2000-03-14 17:18:40 $
 * @since Visad Utility Library v0.7.1
 */

public class SliderLabel extends JPanel implements SliderChangeListener {

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

  /** widget sizes */
  Dimension minSize = null;
  Dimension prefSize = null;
  Dimension maxSize = null;

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
    // redraw
    validate();
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
        if (drawmin == null) drawmin = "null";
        if (drawmax == null) drawmax = "null";
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
    if (drawval == null) drawval = "null";
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

  /** Return the preferred sise of the SliderLabel */
  public Dimension getPreferredSize() {
    if (prefSize == null) {
      prefSize = new Dimension(256, 18);
    }
    return prefSize;
  }

  /** Set the preferred size of the SliderLabel */
  public void setPreferredSize(Dimension dim) { prefSize = dim; }

  /** Return the maximum size of the SliderLabel */
  public Dimension getMaximumSize() {
    if (maxSize == null) {
      maxSize = new Dimension(Integer.MAX_VALUE, 18);
    }
    return maxSize;
  }

  /** Set the preferred size of the SliderLabel */
  public void setMaximumSize(Dimension dim) { maxSize = dim; }

  /** Return the minimum size of the SliderLabel */
  public Dimension getMinimumSize() {
    if (minSize == null) {
      minSize = new Dimension(100, 18);
    }
    return minSize;
  }

  /** Set the preferred size of the SliderLabel */
  public void setMinimumSize(Dimension dim) { minSize = dim; }

  /** for debugging purposes */
  public static void main(String[] argc) {

    Slider slider = new ArrowSlider();
    SliderLabel label = new SliderLabel(slider, "test");

    javax.swing.JFrame f;
    f = new javax.swing.JFrame("Visad Slider Label");
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
