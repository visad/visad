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
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import visad.util.Util;

/** A checkbox with two text fields. */
public class DoubleTextCheckBox extends JPanel
  implements DocumentListener, ItemListener
{

  // -- FIELDS --

  private Vector listeners;
  private String cmd;
  private JCheckBox box;
  private JLabel label;
  private JTextField field1, field2;


  // -- CONSTRUCTOR --

  /** Creates a DoubleTextCheckBox. */
  public DoubleTextCheckBox(String label1, String label2,
    String value1, String value2, boolean checked)
  {
    super();
    listeners = new Vector();
    cmd = "";
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    box = new JCheckBox(label1, checked);
    box.addItemListener(this);
    label = new JLabel(label2);
    label.setForeground(Color.black);
    field1 = new WideTextField(value1);
    field2 = new WideTextField(value2);
    field1.getDocument().addDocumentListener(this);
    field2.getDocument().addDocumentListener(this);
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
    boolean enabled = box.isEnabled() && box.isSelected();
    label.setEnabled(enabled);
    field1.setEnabled(enabled);
    field2.setEnabled(enabled);
  }


  // -- API METHODS --

  /** Gets whether the checkbox is selected. */
  public boolean isSelected() { return box.isSelected(); }

  /** Enables or disables the checkbox. */
  public void setEnabled(boolean enabled) {
    box.setEnabled(enabled);
    updateGUI();
  }

  /** Gets the value of the first text field. */
  public String getFirstValue() { return field1.getText(); }

  /** Gets the value of the second text field. */
  public String getSecondValue() { return field2.getText(); }

  /** Sets whether the checkbox is selected. */
  public void setSelected(boolean selected) { box.setSelected(selected); }

  /** Sets the values of the text fields. */
  public void setValues(String text1, String text2) {
    field1.setText(text1);
    field2.setText(text2);
  }

  /** Sets the mnemonic of the widget. */
  public void setMnemonic(char m) { box.setMnemonic(m); }

  /** Sets the tool tips for the widget. */
  public void setToolTipText(String checkbox, String text1, String text2) {
    box.setToolTipText(checkbox);
    field1.setToolTipText(text1);
    field2.setToolTipText(text2);
  }

  /** Adds an item listener to the check box. */
  public void addActionListener(ActionListener l) { listeners.add(l); }

  /** Removes an item listener from the check box. */
  public void removeActionListener(ActionListener l) { listeners.remove(l); }

  /** Sets the action command for the action listeners. */
  public void setActionCommand(String cmd) { this.cmd = cmd; }


  // -- INTERNAL API METHODS --

  /** ItemListener method triggered when check box state changes. */
  public void itemStateChanged(ItemEvent e) {
    updateGUI();
    notifyListeners();
  }

  public void changedUpdate(DocumentEvent e) { notifyListeners(); }
  public void insertUpdate(DocumentEvent e) { notifyListeners(); }
  public void removeUpdate(DocumentEvent e) { notifyListeners(); }


  // -- HELPER METHODS --

  private void notifyListeners() {
    ActionEvent e = new ActionEvent(this, 0, cmd);
    int size = listeners.size();
    for (int i=0; i<size; i++) {
      ActionListener l = (ActionListener) listeners.elementAt(i);
      l.actionPerformed(e);
    }
  }


  // -- HELPER CLASSES --

  private class WideTextField extends JTextField {

    private static final int MIN_WIDTH = 30;

    public WideTextField(String value) { super(value); }

    public Dimension getMinimumSize() {
      Dimension d = super.getMinimumSize();
      d.width = MIN_WIDTH;
      return d;
    }

    public Dimension getPreferredSize() {
      Dimension d = super.getMinimumSize();
      if (d.width < MIN_WIDTH) d.width = MIN_WIDTH;
      return d;
    }

  }

}
