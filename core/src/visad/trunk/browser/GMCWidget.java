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
import java.util.StringTokenizer;

/**
 * A widget that allows users to control graphics mode parameters.
 */
public class GMCWidget extends Widget implements ActionListener, ItemListener {

  private static final String SCALE_ENABLED = "s";
  private static final String POINT_MODE = "p";
  private static final String TEXTURE_MAPPING = "t";
  private static final String LINE_WIDTH = "lw";
  private static final String POINT_SIZE = "ps";

  Checkbox scale;
  Checkbox point;
  Checkbox texture;
  TextField lineWidth;
  TextField pointSize;

  boolean gmcScaleEnabled = false;
  boolean gmcPointMode = false;
  boolean gmcTextureMapping = false;
  float gmcLineWidth = 1.0f;
  float gmcPointSize = 1.0f;

  /**
   * Constructs a new GMCWidget from the given WidgetEvent information.
   */
  public GMCWidget(WidgetEvent e) {
    super(e);

    // set background to white
    setBackground(Color.white);

    // lay out components with GridBagLayout
    GridBagLayout gridbag = new GridBagLayout();
    setLayout(gridbag);

    // construct GUI components
    scale = new Checkbox("Enable scale", gmcScaleEnabled);
    point = new Checkbox("Point mode", gmcPointMode);
    texture = new Checkbox("Texture mapping", gmcTextureMapping);
    lineWidth = new TextField("" + gmcLineWidth);
    pointSize = new TextField("" + gmcPointSize);

    // get widget values from event
    updateWidget(e);

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

  /**
   * Programmatically sets the scale enabled checkbox.
   */
  public void setScaleEnabled(boolean se) {
    gmcScaleEnabled = se;
    scale.setState(se);
  }

  /**
   * Programmatically sets the point mode checkbox.
   */
  public void setPointMode(boolean pm) {
    gmcPointMode = pm;
    point.setState(pm);
  }

  /**
   * Programmatically sets the texture mapping checkbox.
   */
  public void setTextureMapping(boolean tm) {
    gmcTextureMapping = tm;
    texture.setState(tm);
  }

  /**
   * Programmatically sets the line width text field.
   */
  public void setLineWidth(float lw) {
    gmcLineWidth = lw;
    lineWidth.setText("" + lw);
  }

  /**
   * Programmatically sets the point size text field.
   */
  public void setPointSize(float ps) {
    gmcPointSize = ps;
    pointSize.setText("" + ps);
  }

  /**
   * Updates widget based on information from the given WidgetEvent.
   */
  public void updateWidget(WidgetEvent e) {
    if (e == null) return;
    int id = e.getId();
    if (id == WidgetEvent.CREATED) {
      String save = e.getValue();
      StringTokenizer st = new StringTokenizer(save, "\n");
      setScaleEnabled(Convert.getBoolean(st.nextToken()));
      setPointMode(Convert.getBoolean(st.nextToken()));
      setTextureMapping(Convert.getBoolean(st.nextToken()));
      setLineWidth((float) Convert.getDouble(st.nextToken()));
      setPointSize((float) Convert.getDouble(st.nextToken()));
    }
    else if (id == WidgetEvent.UPDATED) {
      String field = e.getField();
      String value = e.getValue();
      if (field.equals(SCALE_ENABLED)) {
        setScaleEnabled(Convert.getBoolean(value));
      }
      else if (field.equals(POINT_MODE)) {
        setPointMode(Convert.getBoolean(value));
      }
      else if (field.equals(TEXTURE_MAPPING)) {
        setTextureMapping(Convert.getBoolean(value));
      }
      else if (field.equals(LINE_WIDTH)) {
        setLineWidth((float) Convert.getDouble(value));
      }
      else { // field.equals(POINT_SIZE)
        setPointSize((float) Convert.getDouble(value));
      }
    }
  }

  /**
   * Handles TextField changes.
   */
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
        gmcLineWidth = lw;
        scale.requestFocus();
        notifyListeners(new WidgetEvent(
          this, WidgetEvent.UPDATED, LINE_WIDTH, "" + lw));
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
        gmcPointSize = ps;
        scale.requestFocus();
        notifyListeners(
          new WidgetEvent(this, WidgetEvent.UPDATED, POINT_SIZE, "" + ps));
      }
    }
  }

  /**
   * Handles Checkbox changes.
   */
  public void itemStateChanged(ItemEvent e) {
    Object o = e.getItemSelectable();
    boolean on = (e.getStateChange() == ItemEvent.SELECTED);
    String f;
    if (o == scale) f = SCALE_ENABLED;
    else if (o == point) f = POINT_MODE;
    else f = TEXTURE_MAPPING; // o == texture
    String v = on ? TRUE : FALSE;
    notifyListeners(new WidgetEvent(this, WidgetEvent.UPDATED, f, v));
  }

  /**
   * Tests GMCWidget.
   */
  public static void main(String[] args) {
    new GMCWidget(null).testWidget();
  }

}
