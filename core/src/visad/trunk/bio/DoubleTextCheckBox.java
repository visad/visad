//
// DoubleTextCheckBox.java
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
import javax.swing.*;
import visad.util.Util;

/** A checkbox with two text fields. */
public class DoubleTextCheckBox extends JPanel implements ItemListener {

  // -- FIELDS --

  private JCheckBox box;
  private JLabel label;
  private JTextField field1, field2;


  // -- CONSTRUCTOR --

  /** Creates a DoubleTextCheckBox. */
  public DoubleTextCheckBox(String label1, String label2,
    String value1, String value2, boolean checked)
  {
    super();
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    box = new JCheckBox(label1, checked);
    box.addItemListener(this);
    label = new JLabel(label2);
    label.setForeground(Color.black);
    field1 = new JTextField(value1);
    field2 = new JTextField(value2);
    Util.adjustTextField(field1);
    Util.adjustTextField(field2);
    updateGUI();

    add(box);
    add(Box.createHorizontalGlue());
    add(Box.createHorizontalStrut(5));
    add(field1);
    add(Box.createHorizontalStrut(5));
    add(label);
    add(Box.createHorizontalStrut(5));
    add(field2);
  }

  /** Refreshes the state of the widget. */
  public void updateGUI() {
    boolean checked = box.isSelected();
    label.setEnabled(checked);
    field1.setEnabled(checked);
    field2.setEnabled(checked);
  }


  // -- API METHODS --

  /** Gets whether the checkbox is selected. */
  public boolean isSelected() { return box.isSelected(); }

  /** Gets the value of the first text field. */
  public String getFirstValue() { return field1.getText(); }

  /** Gets the value of the second text field. */
  public String getSecondValue() { return field2.getText(); }

  /** Sets the values of the text fields. */
  public void setValues(String text1, String text2) {
    field1.setText(text1);
    field2.setText(text2);
  }


  // -- INTERNAL API METHODS --

  /** ItemListener method triggered when check box state changes. */
  public void itemStateChanged(ItemEvent e) { updateGUI(); }

}
