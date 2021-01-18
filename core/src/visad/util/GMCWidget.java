//
// GMCWidget.java
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

package visad.util;

/* AWT packages */
import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/* JFC packages */
import javax.swing.*;

/* RMI classes */
import java.rmi.RemoteException;

/* VisAD packages */
import visad.*;

/** A widget that allows users to control graphics mode parameters.<P> */
public class GMCWidget extends JPanel implements ActionListener,
                                                 ItemListener,
                                                 ControlListener {

  /** This GMCWidget's associated control */
  GraphicsModeControl control;

  JCheckBox scale;
  JCheckBox point;
  JCheckBox texture;
  JTextField lineWidth;
  JTextField pointSize;

  float gmcLineWidth;
  float gmcPointSize;

  /** Constructs a GMCWidget linked to the GraphicsModeControl gmc */
  public GMCWidget(GraphicsModeControl gmc) {
    control = gmc;

    // create JPanels
    JPanel top = new JPanel();
    JPanel bot = new JPanel();

    // set up layouts
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    bot.setLayout(new BoxLayout(bot, BoxLayout.X_AXIS));

    // auto-detect values from control if possible
    boolean s = false;
    boolean p = false;
    boolean t = false;
    if (control != null) {
      s = control.getScaleEnable();
      p = control.getPointMode();
      t = control.getTextureEnable();
    }
    gmcLineWidth = 1.0f;
    gmcPointSize = 1.0f;
    if (control != null) {
      gmcLineWidth = control.getLineWidth();
      gmcPointSize = control.getPointSize();
    }

    // construct JComponents
    scale = new JCheckBox("Enable scale", s);
    point = new JCheckBox("Point mode", p);
    texture = new JCheckBox("Texture mapping", t);
    lineWidth = new JTextField(PlotText.shortString(gmcLineWidth));

    // WLH 2 Dec 98
    Dimension msize = lineWidth.getMaximumSize();
    Dimension psize = lineWidth.getPreferredSize();
    msize.height = psize.height;
    lineWidth.setMaximumSize(msize);

    pointSize = new JTextField(PlotText.shortString(gmcPointSize));

    // WLH 2 Dec 98
    msize = pointSize.getMaximumSize();
    psize = pointSize.getPreferredSize();
    msize.height = psize.height;
    pointSize.setMaximumSize(msize);

    JLabel lwLabel = new JLabel("Line width:");
    JLabel psLabel = new JLabel("Point size:");

    // set label colors
    lwLabel.setForeground(Color.black);
    psLabel.setForeground(Color.black);

    // add listeners
    scale.addItemListener(this);
    point.addItemListener(this);
    texture.addItemListener(this);
    lineWidth.addActionListener(this);
    lineWidth.setActionCommand("line");
    pointSize.addActionListener(this);
    pointSize.setActionCommand("point");
    control.addControlListener(this);

    // lay out JComponents
    top.add(scale);
    top.add(point);
    top.add(texture);
    bot.add(lwLabel);
    bot.add(lineWidth);
    bot.add(psLabel);
    bot.add(pointSize);
    add(top);
    add(bot);
  }

  /** Handles JTextField changes */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("line")) {
      float lw = Float.NaN;
      try {
        lw = Float.valueOf(lineWidth.getText()).floatValue();
      }
      catch (NumberFormatException exc) {
        lineWidth.setText(PlotText.shortString(gmcLineWidth));
      }
      if (lw == lw && control != null) {
        try {
          control.setLineWidth(lw);
          gmcLineWidth = lw;
          scale.requestFocus();
        }
        catch (VisADException exc) {
          lineWidth.setText(PlotText.shortString(gmcLineWidth));
        }
        catch (RemoteException exc) {
          lineWidth.setText(PlotText.shortString(gmcLineWidth));
        }
      }
    }
    else if (cmd.equals("point")) {
      float ps = Float.NaN;
      try {
        ps = Float.valueOf(pointSize.getText()).floatValue();
      }
      catch (NumberFormatException exc) {
        pointSize.setText(PlotText.shortString(gmcPointSize));
      }
      if (ps == ps && control != null) {
        try {
          control.setPointSize(ps);
          gmcPointSize = ps;
          scale.requestFocus();
        }
        catch (VisADException exc) {
          pointSize.setText(PlotText.shortString(gmcPointSize));
        }
        catch (RemoteException exc) {
          pointSize.setText(PlotText.shortString(gmcPointSize));
        }
      }
    }
  }

  /** Handles JCheckBox changes */
  public void itemStateChanged(ItemEvent e) {
    if (control != null) {
      Object o = e.getItemSelectable();
      boolean on = (e.getStateChange() == ItemEvent.SELECTED);
      try {
        if (o == scale) control.setScaleEnable(on);
        else if (o == point) control.setPointMode(on);
        else if (o == texture) control.setTextureEnable(on);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  }

  /** Handles GraphicsModeControl changes */
  public void controlChanged(ControlEvent e) {
    if (control == null) {
      return;
    }

    if (control.getScaleEnable() != scale.isSelected()) {
      scale.setSelected(control.getScaleEnable());
    }
    if (control.getPointMode() != point.isSelected()) {
      point.setSelected(control.getPointMode());
    }
    if (control.getTextureEnable() != texture.isSelected()) {
      texture.setSelected(control.getTextureEnable());
    }

    float val;

    val = control.getLineWidth();
    if (!Util.isApproximatelyEqual(val, gmcLineWidth)) {
      lineWidth.setText(PlotText.shortString(val));
      gmcLineWidth = val;
    }
    val = control.getPointSize();
    if (!Util.isApproximatelyEqual(val, gmcPointSize)) {
      pointSize.setText(PlotText.shortString(val));
      gmcPointSize = val;
    }
  }
}

