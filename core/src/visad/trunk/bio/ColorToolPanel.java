//
// ColorToolPanel.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.browser.*;
import visad.util.*;

/**
 * ColorToolPanel is the tool panel for
 * adjusting viewing parameters.
 */
public class ColorToolPanel extends ToolPanel
  implements DocumentListener, ItemListener
{

  // -- CONSTANTS --

  /** Maximum alpha exponent. */
  private static final int MAX_POWER = 8;


  // -- GUI COMPONENTS --

  /** Label for brightness. */
  private JLabel brightnessLabel;

  /** Slider for level of brightness. */
  private JSlider brightness;

  /** Label for current brightness value. */
  private JLabel brightnessValue;

  /** Label for contrast. */
  private JLabel contrastLabel;

  /** Slider for level of contrast. */
  private JSlider contrast;

  /** Label for current contrast value. */
  private JLabel contrastValue;

  /** Option for RGB color model. */
  private JRadioButton rgb;

  /** Option for HSV color model. */
  private JRadioButton hsv;

  /** Red/hue color map widget. */
  private BioColorWidget red;

  /** Green/saturation color map widget. */
  private BioColorWidget green;

  /** Blue/value color map widget. */
  private BioColorWidget blue;

  /** Toggle for composite coloring. */
  private JCheckBox composite;

  /** Label for alpha. */
  private JLabel alphaLabel;

  /** Slider for alpha function. */
  private JSlider alpha;

  /** Label for current alpha value. */
  private JLabel alphaValue;

  /** Combo box for choosing color widgets. */
  private JComboBox selector;

  /** Panel for fixed color scaling options. */
  private JPanel fixedPanel;

  /** Option for fixed color scaling. */
  private JCheckBox fixed;

  /** Text field for low color scale value. */
  private JTextField loVal;

  /** Label for fixed color scale. */
  private JLabel toLabel;

  /** Text field for high color scale value. */
  private JTextField hiVal;


  // -- OTHER FIELDS --

  /** Number of components in the tool panel. */
  private int cc = 0;

  /** Should changes to the color components be ignored? */
  private boolean ignore = false;

  /** Should changes to the color range be ignored? */
  private boolean ignoreColorRange = false;


  // -- CONSTRUCTOR --

  /** Constructs a tool panel for adjusting viewing parameters. */
  public ColorToolPanel(VisBio biovis) {
    super(biovis);

    // brightness label
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    brightnessLabel = new JLabel("Brightness: ");
    brightnessLabel.setForeground(Color.black);
    brightnessLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
    brightnessLabel.setDisplayedMnemonic('b');
    String brightnessToolTip = "Adjusts the brightness of the displays";
    brightnessLabel.setToolTipText(brightnessToolTip);
    p.add(brightnessLabel);

    // brightness slider
    brightness = new JSlider(0, VisBio.COLOR_DETAIL,
      VisBio.NORMAL_BRIGHTNESS);
    brightness.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) { doColorTable(); }
    });
    brightness.setAlignmentY(JSlider.TOP_ALIGNMENT);
    brightnessLabel.setLabelFor(brightness);
    brightness.setToolTipText(brightnessToolTip);
    p.add(brightness);

    // current brightness value
    brightnessValue = new JLabel("" + VisBio.NORMAL_BRIGHTNESS);
    Dimension labelSize =
      new JLabel("." + VisBio.COLOR_DETAIL).getPreferredSize();
    brightnessValue.setPreferredSize(labelSize);
    brightnessValue.setAlignmentY(JLabel.TOP_ALIGNMENT);
    brightnessValue.setToolTipText("Current brightness value");
    p.add(brightnessValue);
    controls.add(pad(p));
    cc++;

    // contrast label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    contrastLabel = new JLabel("Contrast: ");
    contrastLabel.setForeground(Color.black);
    contrastLabel.setPreferredSize(brightnessLabel.getPreferredSize());
    contrastLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
    contrastLabel.setDisplayedMnemonic('c');
    String contrastToolTip = "Adjusts the contrast of the displays";
    contrastLabel.setToolTipText(contrastToolTip);
    p.add(contrastLabel);

    // contrast slider
    contrast = new JSlider(0, VisBio.COLOR_DETAIL,
      VisBio.NORMAL_CONTRAST);
    contrast.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) { doColorTable(); }
    });
    contrast.setAlignmentY(JSlider.TOP_ALIGNMENT);
    contrast.setMajorTickSpacing(VisBio.COLOR_DETAIL / 4);
    contrast.setMinorTickSpacing(VisBio.COLOR_DETAIL / 16);
    contrast.setPaintTicks(true);
    contrastLabel.setLabelFor(contrast);
    contrast.setToolTipText(contrastToolTip);
    p.add(contrast);

    // current contrast value
    contrastValue = new JLabel("" + VisBio.NORMAL_CONTRAST);
    contrastValue.setPreferredSize(labelSize);
    contrastValue.setAlignmentY(JLabel.TOP_ALIGNMENT);
    contrastValue.setToolTipText("Current contrast value");
    p.add(contrastValue);
    controls.add(pad(p));
    cc++;

    // spacing
    controls.add(Box.createVerticalStrut(5));
    cc++;

    // color model label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    JLabel colorModel = new JLabel("Color model: ");
    colorModel.setForeground(Color.black);
    p.add(colorModel);

    // RGB color model option
    ButtonGroup group = new ButtonGroup();
    rgb = new JRadioButton("RGB", true);
    rgb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        red.setModel(BioColorWidget.RGB);
        green.setModel(BioColorWidget.RGB);
        blue.setModel(BioColorWidget.RGB);
        guessTypes();
        doColorTable();
      }
    });
    rgb.setMnemonic('r');
    rgb.setToolTipText("Switches to a Red-Green-Blue color model");
    group.add(rgb);
    p.add(rgb);

    // HSV color model option
    hsv = new JRadioButton("HSV");
    hsv.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        red.setModel(BioColorWidget.HSV);
        green.setModel(BioColorWidget.HSV);
        blue.setModel(BioColorWidget.HSV);
        guessTypes();
        doColorTable();
      }
    });
    hsv.setMnemonic('s');
    hsv.setToolTipText("Switches to a Hue-Saturation-Value color model");
    group.add(hsv);
    p.add(hsv);
    controls.add(pad(p));
    cc++;

    // spacing
    controls.add(Box.createVerticalStrut(5));
    cc++;

    // red/hue color map widget
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    red = new BioColorWidget(bio, 0);
    red.addItemListener(this);
    red.setMnemonic('e');
    red.setToolTipText("Range mapping to Red/Hue color component");
    p.add(red);

    // green/saturation color map widget
    green = new BioColorWidget(bio, 1);
    green.addItemListener(this);
    green.setMnemonic('n');
    green.setToolTipText("Range mapping to Green/Saturation color component");
    p.add(green);

    // blue/value color map widget
    blue = new BioColorWidget(bio, 2);
    blue.addItemListener(this);
    blue.setMnemonic('u');
    blue.setToolTipText("Range mapping to Blue/Value color component");
    p.add(blue);
    controls.add(pad(p));
    cc++;

    // composite checkbox
    composite = new JCheckBox("Composite image coloring", false);
    composite.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean b = !composite.isSelected();
        red.setEnabled(b);
        green.setEnabled(b);
        blue.setEnabled(b);
        doColorTable();
      }
    });
    composite.setMnemonic('i');
    composite.setToolTipText("Combines range values " +
      "into a composite color table");
    controls.add(pad(composite));
    cc++;

    // alpha label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    alphaLabel = new JLabel("Alpha: ");
    alphaLabel.setForeground(Color.black);
    alphaLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
    alphaLabel.setPreferredSize(brightnessLabel.getPreferredSize());
    alphaLabel.setDisplayedMnemonic('a');
    String alphaToolTip = "Adjusts transparency when rendering as a volume";
    alphaLabel.setToolTipText(alphaToolTip);
    alphaLabel.setEnabled(false);
    p.add(alphaLabel);

    // alpha slider
    alpha = new JSlider(0, VisBio.COLOR_DETAIL, VisBio.COLOR_DETAIL / 2);
    alpha.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) { doAlpha(false); }
    });
    alpha.setAlignmentY(JSlider.TOP_ALIGNMENT);
    alpha.setMajorTickSpacing(VisBio.COLOR_DETAIL / 4);
    alpha.setMinorTickSpacing(VisBio.COLOR_DETAIL / 16);
    alpha.setPaintTicks(true);
    alphaLabel.setLabelFor(alpha);
    alpha.setToolTipText(alphaToolTip);
    alpha.setEnabled(false);
    p.add(alpha);

    // current alpha value
    alphaValue = new JLabel("1.0");
    alphaValue.setPreferredSize(labelSize);
    alphaValue.setAlignmentY(JLabel.TOP_ALIGNMENT);
    alphaValue.setToolTipText("Current transparency function exponent");
    alphaValue.setEnabled(false);
    p.add(alphaValue);
    controls.add(pad(p));
    cc++;

    // divider between display functions and resolution functions
    controls.add(Box.createVerticalStrut(10));
    controls.add(new Divider());
    controls.add(Box.createVerticalStrut(10));
    cc += 3;

    // color widget selector label
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    JLabel selLabel = new JLabel("Color table: ");
    selLabel.setDisplayedMnemonic('t');
    String selToolTip = "List of color tables for color components";
    selLabel.setToolTipText(selToolTip);
    selLabel.setForeground(Color.black);
    p.add(selLabel);

    // color widget selector
    //BaseRGBMap.USE_COLOR_CURSORS = true;
    selector = new JComboBox();
    selector.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int ndx = selector.getSelectedIndex();
        for (int i=cc; i<controls.getComponentCount(); i++) {
          controls.getComponent(i).setVisible(i == ndx + cc);
        }
        if (ndx >= 0) {
          ignoreColorRange = true;
          fixed.setSelected(bio.sm.isFixedColorRange(ndx));
          double[] d = bio.sm.getColorRange(ndx);
          loVal.setText("" + d[0]);
          hiVal.setText("" + d[1]);
          ignoreColorRange = false;
        }
      }
    });
    selLabel.setLabelFor(selector);
    selector.setToolTipText(selToolTip);
    p.add(selector);
    controls.add(pad(p));
    cc++;

    // spacing
    controls.add(Box.createVerticalStrut(5));
    cc++;

    // fixed color scaling option
    fixedPanel = new JPanel();
    fixedPanel.setLayout(new BoxLayout(fixedPanel, BoxLayout.X_AXIS));
    fixedPanel.setVisible(false);
    fixed = new JCheckBox("Fixed color range: ", true);
    fixed.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean b = fixed.isSelected();
        loVal.setEnabled(b);
        toLabel.setEnabled(b);
        hiVal.setEnabled(b);
        updateColorRange();
      }
    });
    fixed.setMnemonic('f');
    fixed.setToolTipText("Fixes color range between the given values");
    fixedPanel.add(fixed);

    // low color scale value text field
    loVal = new JTextField("0");
    adjustTextField(loVal);
    loVal.getDocument().addDocumentListener(this);
    loVal.setToolTipText("Minimum color range value");
    fixedPanel.add(loVal);

    // color scale label
    toLabel = new JLabel(" to ");
    toLabel.setForeground(Color.black);
    fixedPanel.add(toLabel);

    // high color scale value text field
    hiVal = new JTextField("255");
    adjustTextField(hiVal);
    hiVal.getDocument().addDocumentListener(this);
    hiVal.setToolTipText("Maximum color range value");
    fixedPanel.add(hiVal);
    controls.add(pad(fixedPanel, false, true));
    cc++;

    // spacing
    controls.add(Box.createVerticalStrut(5));
    cc++;

    // placeholder for the color tables to be added later
    controls.add(Box.createRigidArea(new Dimension(310, 290)));
  }


  // -- API METHODS --

  /** Initializes this tool panel. */
  public void init() {
    guessTypes();
    doColorTable();
    bio.sm.syncColors();
  }

  /** Enables or disables this tool panel. */
  public void setEnabled(boolean enabled) {
    brightnessLabel.setEnabled(enabled);
    brightness.setEnabled(enabled);
    contrastLabel.setEnabled(enabled);
    contrast.setEnabled(enabled);
    fixed.setEnabled(enabled);
    loVal.setEnabled(enabled);
    toLabel.setEnabled(enabled);
    hiVal.setEnabled(enabled);
  }

  /** Adds a widget to the tool panel. */
  public void addWidget(String s, JComponent c) {
    fixedPanel.setVisible(true);
    selector.addItem(s);
    c.setVisible(selector.getItemCount() == 1);
    controls.add(c);
  }

  /** Removes all widgets from the tool panel. */
  public void removeAllWidgets() {
    selector.removeAllItems();
    int size = controls.getComponentCount();
    for (int i=controls.getComponentCount(); i>cc; i--) controls.remove(cc);
    fixedPanel.setVisible(false);
  }


  // -- INTERNAL API METHODS --

  /** DocumentListener method for handling text field changes. */
  public void changedUpdate(DocumentEvent e) { updateColorRange(); }

  /** DocumentListener method for handling text field additions. */
  public void insertUpdate(DocumentEvent e) { updateColorRange(); }

  /** DocumentListener method for handling text field deletions. */
  public void removeUpdate(DocumentEvent e) { updateColorRange(); }

  /** ItemListener method for handling color mapping changes. */
  public void itemStateChanged(ItemEvent e) { doColorTable(); }

  /** Chooses most desirable types for range mappings. */
  void guessTypes() {
    red.removeItemListener(this);
    green.removeItemListener(this);
    blue.removeItemListener(this);
    red.guessType();
    green.guessType();
    blue.guessType();
    red.addItemListener(this);
    green.addItemListener(this);
    blue.addItemListener(this);
  }

  /** Updates image color table, for brightness and color adjustments. */
  void doColorTable() {
    if (ignore) return;
    int bright = brightness.getValue();
    int cont = contrast.getValue();
    int model = rgb.isSelected() ? 0 : 1;
    boolean comp = composite.isSelected();
    bio.setImageColors(bright, cont, model, comp, red.getSelectedItem(),
      green.getSelectedItem(), blue.getSelectedItem());
    brightnessValue.setText("" + bright);
    contrastValue.setText("" + cont);
  }

  /** Updates image alpha, for transparency in volume rendering. */
  void doAlpha(boolean solid) {
    // [0, 0.5] -> [N, 1]
    // [0.5, 1] -> [1, 1/N]
    double value = (double) alpha.getValue() / VisBio.COLOR_DETAIL;
    boolean invert = value > 0.5;
    if (invert) value = 1 - value;
    double pow = (MAX_POWER - 1) * 2 * (0.5 - value) + 1;
    if (invert) pow = 1 / pow;
    float[] alphaTable = new float[VisBio.COLOR_DETAIL];
    for (int i=0; i<VisBio.COLOR_DETAIL; i++) {
      double inc = (double) i / (VisBio.COLOR_DETAIL - 1);
      alphaTable[i] = (float) Math.pow(inc, pow);
    }
    LabeledColorWidget[] widgets = bio.sm.getColorWidgets();
    for (int j=0; j<widgets.length; j++) {
      float[][] table = widgets[j].getTable();
      if (table.length < 4) continue;
      if (solid) Arrays.fill(table[3], 1.0f);
      else {
        int len = alphaTable.length < table[3].length ?
          alphaTable.length : table[3].length;
        System.arraycopy(alphaTable, 0, table[3], 0, len);
      }
      widgets[j].setTable(table);
    }
    bio.state.saveState();

    String s = "" + pow;
    if (s.length() > 4) s = s.substring(0, 4);
    alphaValue.setText(s);

    alphaLabel.setEnabled(!solid);
    alpha.setEnabled(!solid);
    alphaValue.setEnabled(!solid);
  }

  /** Updates color components to match those specified. */
  void setColors(int bright, int cont, int model, boolean comp,
    RealType r, RealType g, RealType b)
  {
    ignore = true;
    brightness.setValue(bright);
    contrast.setValue(cont);
    rgb.setSelected(model == 0);
    hsv.setSelected(model == 1);
    red.setModel(model);
    green.setModel(model);
    blue.setModel(model);
    composite.setSelected(comp);
    red.setSelectedItem(r);
    green.setSelectedItem(g);
    blue.setSelectedItem(b);
    ignore = false;
    doColorTable();
    bio.sm.syncColors();
  }

  /** Updates color range components to match current range values. */
  void updateColorRangeFields() {
    final int ndx = selector.getSelectedIndex();
    if (ndx >= 0) {
      Util.invoke(false, new Runnable() {
        public void run() {
          ignoreColorRange = true;
          fixed.setSelected(bio.sm.isFixedColorRange(ndx));
          double[] d = bio.sm.getColorRange(ndx);
          loVal.setText(Convert.shortString(d[0]));
          hiVal.setText(Convert.shortString(d[1]));
          ignoreColorRange = false;
        }
      });
    }
  }


  // -- HELPER METHODS --

  /** Adjusts dimensional layout preferences of a text field. */
  private void adjustTextField(JTextField field) {
    Util.adjustTextField(field);
    Dimension psize = field.getPreferredSize();
    if (psize.width < 40) psize.width = 40;
    field.setPreferredSize(psize);
  }

  /** Refreshes the color range values. */
  private void updateColorRange() {
    if (ignoreColorRange) return;
    int ndx = selector.getSelectedIndex();
    boolean dyn = !fixed.isSelected();
    double lo = Double.NaN;
    double hi = Double.NaN;
    try {
      lo = Double.parseDouble(loVal.getText());
      hi = Double.parseDouble(hiVal.getText());
    }
    catch (NumberFormatException exc) { }
    if (lo < hi) bio.sm.setColorRange(ndx, dyn, lo, hi);
  }

}
