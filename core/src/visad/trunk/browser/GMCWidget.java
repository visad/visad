//
// GMCWidget.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.browser;

import java.awt.*;
import java.awt.event.*;

/** A widget that allows users to control graphics mode parameters. */
public class GMCWidget extends Widget implements ActionListener, ItemListener {

  Checkbox scale;
  Checkbox point;
  Checkbox texture;
  TextField lineWidth;
  TextField pointSize;

  float gmcLineWidth;
  float gmcPointSize;

  /** Constructs a new GMCWidget from the given WidgetEvent information */
  public GMCWidget(WidgetEvent e) {
    super(e);

    // get widget values from event
    boolean s = false;
    boolean p = false;
    boolean t = false;
    gmcLineWidth = 1.0f;
    gmcPointSize = 1.0f;
    /* CTR: TODO */

    // set background to white
    setBackground(Color.white);

    // lay out components with GridBagLayout
    GridBagLayout gridbag = new GridBagLayout();
    setLayout(gridbag);

    // construct GUI components
    scale = new Checkbox("Enable scale", s);
    point = new Checkbox("Point mode", p);
    texture = new Checkbox("Texture mapping", t);
    lineWidth = new TextField("" + gmcLineWidth);
    pointSize = new TextField("" + gmcPointSize);

    // add listeners
    scale.addItemListener(this);
    point.addItemListener(this);
    texture.addItemListener(this);
    lineWidth.addActionListener(this);
    pointSize.addActionListener(this);

    // lay out Components
    addComponent(scale, gridbag, 0, 0, 1, 1, 0.0, 0.0);
    addComponent(point, gridbag, 1, 0, 1, 1, 0.0, 0.0);
    addComponent(texture, gridbag, 2, 0, 2, 1, 0.0, 0.0);
    addComponent(new Label("Point size:"), gridbag, 0, 1, 1, 1, 0.0, 0.0);
    addComponent(lineWidth, gridbag, 1, 1, 1, 1, 1.0, 0.0);
    addComponent(new Label("Line width:"), gridbag, 2, 1, 1, 1, 0.0, 0.0);
    addComponent(pointSize, gridbag, 3, 1, 1, 1, 1.0, 0.0);
  }

  /** update widget based on information from the given WidgetEvent */
  public void updateWidget(WidgetEvent e) {
    /* CTR: TODO */
  }

  /** Handles TextField changes */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    Object source = e.getSource();
    if (source == lineWidth) {
      float lw = Float.NaN;
      try {
        lw = Float.valueOf(lineWidth.getText()).floatValue();
      }
      catch (NumberFormatException exc) {
        lineWidth.setText("" + gmcLineWidth);
      }
      if (lw == lw) {
        /* CTR: TODO: send out WidgetEvent to listeners */
        gmcLineWidth = lw;
        scale.requestFocus();
      }
    }
    else if (source == pointSize) {
      float ps = Float.NaN;
      try {
        ps = Float.valueOf(pointSize.getText()).floatValue();
      }
      catch (NumberFormatException exc) {
        pointSize.setText("" + gmcPointSize);
      }
      if (ps == ps) {
        /* CTR: TODO: send out WidgetEvent to listeners */
        gmcPointSize = ps;
        scale.requestFocus();
      }
    }
  }

  /** Handles Checkbox changes */
  public void itemStateChanged(ItemEvent e) {
    Object o = e.getItemSelectable();
    boolean on = (e.getStateChange() == ItemEvent.SELECTED);
    /* CTR: TODO: send out WidgetEvent to listeners */
  }

  /** Tests GMCWidget */
  public static void main(String[] args) {
    /* CTR: TODO: fix this WidgetEvent sloppiness */
    WidgetEvent e = new WidgetEvent(null, 0);
    new GMCWidget(e).testWidget();
  }

}
