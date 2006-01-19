//
// GMCWidget.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

  Checkbox scale;
  Checkbox point;
  Checkbox texture;
  TextField lineWidth;
  TextField pointSize;

  boolean gmcScaleEnable;
  boolean gmcPointMode;
  boolean gmcTextureEnable;
  float gmcLineWidth;
  float gmcPointSize;
  int gmcTransparencyMode;
  int gmcProjectionPolicy;
  int gmcPolygonMode;
  boolean gmcMissingTransparent;
  int gmcCurvedSize;

  /**
   * Constructs a new GMCWidget.
   */
  public GMCWidget() {
    // lay out components with GridBagLayout
    GridBagLayout gridbag = new GridBagLayout();
    setLayout(gridbag);

    // construct GUI components
    scale = new Checkbox("Enable scale", gmcScaleEnable);
    point = new Checkbox("Point mode", gmcPointMode);
    texture = new Checkbox("Texture mapping", gmcTextureEnable);
    lineWidth = new TextField(Convert.shortString(gmcLineWidth));
    pointSize = new TextField(Convert.shortString(gmcPointSize));

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
    addComponent(new Label("Line width:"), gridbag, 0, 1, 1, 1, 0.0, 0.0);
    addComponent(lineWidth, gridbag, 1, 1, 1, 1, 1.0, 0.0);
    addComponent(new Label("Point size:"), gridbag, 2, 1, 1, 1, 0.0, 0.0);
    addComponent(pointSize, gridbag, 3, 1, 1, 1, 1.0, 0.0);
  }

  /**
   * Gets the value of the line width text field.
   */
  public float getLineWidth() {
    return gmcLineWidth;
  }

  /**
   * Programmatically sets the line width text field.
   */
  public void setLineWidth(float lw) {
    gmcLineWidth = lw;
    lineWidth.setText(Convert.shortString(lw));
  }

  /**
   * Gets the value of the point size text field.
   */
  public float getPointSize() {
    return gmcPointSize;
  }
  
  /**
   * Programmatically sets the point size text field.
   */
  public void setPointSize(float ps) {
    gmcPointSize = ps;
    pointSize.setText(Convert.shortString(ps));
  }

  /**
   * Gets the value of the point mode checkbox;
   */
  public boolean getPointMode() {
    return gmcPointMode;
  }
  
  /**
   * Programmatically sets the point mode checkbox.
   */
  public void setPointMode(boolean pm) {
    gmcPointMode = pm;
    point.setState(pm);
  }

  /**
   * Gets the value of the texture enable checkbox.
   */
  public boolean getTextureEnable() {
    return gmcTextureEnable;
  }
  
  /**
   * Programmatically sets the texture mapping checkbox.
   */
  public void setTextureEnable(boolean tm) {
    gmcTextureEnable = tm;
    texture.setState(tm);
  }

  /**
   * Gets the value of the scale enable checkbox.
   */
  public boolean getScaleEnable() {
    return gmcScaleEnable;
  }
  
  /**
   * Programmatically sets the scale enabled checkbox.
   */
  public void setScaleEnable(boolean se) {
    gmcScaleEnable = se;
    scale.setState(se);
  }

  /**
   * Gets the transparency mode.
   */
  public int getTransparencyMode() {
    return gmcTransparencyMode;
  }

  /**
   * Sets the transparency mode.
   */
  public void setTransparencyMode(int tm) {
    gmcTransparencyMode = tm;
  }

  /**
   * Gets the projection policy.
   */
  public int getProjectionPolicy() {
    return gmcProjectionPolicy;
  }

  /**
   * Sets the projection policy.
   */
  public void setProjectionPolicy(int pp) {
    gmcProjectionPolicy = pp;
  }

  /**
   * Gets the polygon mode.
   */
  public int getPolygonMode() {
    return gmcPolygonMode;
  }

  /**
   * Sets the polygon mode.
   */
  public void setPolygonMode(int pm) {
    gmcPolygonMode = pm;
  }

  /**
   * Gets whether missing values are transparent.
   */
  public boolean getMissingTransparent() {
    return gmcMissingTransparent;
  }

  /**
   * Sets whether missing values are transparent.
   */
  public void setMissingTransparent(boolean mt) {
    gmcMissingTransparent = mt;
  }

  /**
   * Gets the curved size.
   */
  public int getCurvedSize() {
    return gmcCurvedSize;
  }

  /**
   * Sets the curved size.
   */
  public void setCurvedSize(int cs) {
    gmcCurvedSize = cs;
  }

  /**
   * Gets a string representing this widget's current state.
   */
  public String getSaveString() {
    return "" + gmcLineWidth + " " + gmcPointSize + " " + gmcPointMode + " " +
      gmcTextureEnable + " " + gmcScaleEnable + " " +
      gmcTransparencyMode + " " + gmcProjectionPolicy + " " +
      gmcPolygonMode + " " + gmcMissingTransparent + " " + gmcCurvedSize;
  }

  /**
   * Reconstructs this widget's state using the specified save string.
   */
  public void setSaveString(String save) {
    if (save == null) {
      if (DEBUG) System.err.println("Invalid save string");
      return;
    }
    StringTokenizer st = new StringTokenizer(save);
    int numTokens = st.countTokens();
    if (numTokens < 10) {
      System.out.println("Invalid save string");
      return;
    }

    // determine graphics mode settings
    float lw = Convert.getFloat(st.nextToken());
    float ps = Convert.getFloat(st.nextToken());
    boolean pm = Convert.getBoolean(st.nextToken());
    boolean te = Convert.getBoolean(st.nextToken());
    boolean se = Convert.getBoolean(st.nextToken());
    int tm = Convert.getInt(st.nextToken());
    int pp = Convert.getInt(st.nextToken());
    int pm2 = Convert.getInt(st.nextToken());
    boolean mt = Convert.getBoolean(st.nextToken());
    int cs = Convert.getInt(st.nextToken());

    // reset graphics mode settings
    setLineWidth(lw);
    setPointSize(ps);
    setPointMode(pm);
    setTextureEnable(te);
    setScaleEnable(se);
    setTransparencyMode(tm);
    setProjectionPolicy(pp);
    setPolygonMode(pm2);
    setMissingTransparent(mt);
    setCurvedSize(cs);
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
        lineWidth.setText(Convert.shortString(gmcLineWidth));
      }
      if (lw == lw) {
        setLineWidth(lw);
        scale.requestFocus();
        notifyListeners(new WidgetEvent(this));
      }
    }
    else if (source == pointSize) {
      float ps = Float.NaN;
      try {
        ps = Float.valueOf(pointSize.getText()).floatValue();
      }
      catch (NumberFormatException exc) {
        pointSize.setText(Convert.shortString(gmcPointSize));
      }
      if (ps == ps) {
        setPointSize(ps);
        scale.requestFocus();
        notifyListeners(new WidgetEvent(this));
      }
    }
  }

  /**
   * Handles Checkbox changes.
   */
  public void itemStateChanged(ItemEvent e) {
    Object source = e.getItemSelectable();
    boolean on = (e.getStateChange() == ItemEvent.SELECTED);
    if (source == scale) setScaleEnable(on);
    else if (source == point) setPointMode(on);
    else if (source == texture) setTextureEnable(on);
    notifyListeners(new WidgetEvent(this));
  }

  /**
   * Tests GMCWidget.
   */
  public static void main(String[] args) {
    new GMCWidget().testWidget();
  }

}
