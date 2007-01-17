//
// TextFrame.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/** A GUI frame for editing text files. */
public class TextFrame extends GUIFrame implements UndoableEditListener {

  /** main frame component */
  protected TextEditor textPane;

  /** frame title */
  private String title = "VisAD Text Editor";

  /** constructs a TextFrame */
  public TextFrame() {
    this((String) null);
  }

  /** constructs a TextFrame containing text from the given filename */
  public TextFrame(String filename) {
    this(new TextEditor(filename));
  }

  /** constructs a TextFrame from the given TextEditor object */
  public TextFrame(TextEditor textEdit) {
    textPane = textEdit;
    textPane.addUndoableEditListener(this);

    // setup menu bar
    addMenuItem("File", "New", "fileNew", 'n');
    addMenuItem("File", "Open...", "fileOpen", 'o');
    addMenuItem("File", "Save", "fileSave", 's', false);
    addMenuItem("File", "Save as...", "fileSaveAs", 'a');
    addMenuItem("File", "Exit", "fileExit", 'x');
    addMenuItem("Edit", "Undo", "editUndo", 'u', false);
    addMenuItem("Edit", "Redo", "editRedo", 'r', false);
    addMenuItem("Edit", "Cut", "editCut", 't');
    addMenuItem("Edit", "Copy", "editCopy", 'c');
    addMenuItem("Edit", "Paste", "editPaste", 'p');

    // finish UI setup
    layoutGUI();
  }

  /** sets up the GUI */
  protected void layoutGUI() {
    setContentPane(textPane);
    setTitle(title);
  }

  /** sets the text editor's title bar text */
  public void setTitle(String title) {
    this.title = title;
    refreshTitleBar();
  }

  /** gets the text editor's title bar text */
  public String getTitle() {
    return title;
  }

  /** refreshes the Edit Undo and Edit Redo menu items */
  private void refreshUndoMenuItems() {
    JMenuItem editUndo = getMenuItem("Edit", "Undo");
    JMenuItem editRedo = getMenuItem("Edit", "Redo");
    editUndo.setEnabled(textPane.canUndo());
    editUndo.setText(textPane.getUndoName());
    editRedo.setEnabled(textPane.canRedo());
    editRedo.setText(textPane.getRedoName());
  }

  /** refreshes the File Save menu item */
  private void refreshSaveMenuItem(boolean dirty) {
    JMenuItem fileSave = getMenuItem("File", "Save");
    fileSave.setEnabled(dirty);
  }

  /** refreshes the frame's title bar */
  private void refreshTitleBar() {
    String filename = textPane.getFilename();
    super.setTitle(title + (filename == null ? "" : " - " + filename));
  }

  /** refreshes Edit Undo, Edit Redo and File Save */
  private void refreshMenuItems(boolean dirty) {
    refreshUndoMenuItems();
    refreshSaveMenuItem(dirty);
  }

  /** prompts user to save the document before it is discarded */
  protected boolean askSaveChanges(boolean allowCancel) {
    int ans = JOptionPane.showConfirmDialog(this,
      "Save changes to the file?", getTitle(), allowCancel ?
      JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION);
    if (ans == JOptionPane.YES_OPTION) fileSave();
    return (ans != JOptionPane.CANCEL_OPTION);
  }

  /** displays an error message in an error box */
  public void showError(String msg) {
    // strip off carriage returns
    int i = 0;
    int len = msg.length();
    StringBuffer sb = new StringBuffer(len);
    while (true) {
      int index = msg.indexOf((char) 13, i);
      if (index < 0) break;
      sb.append(msg.substring(i, index));
      i = index + 1;
    }
    sb.append(msg.substring(i));
    msg = sb.toString();
    JOptionPane.showMessageDialog(this, msg, getTitle(),
      JOptionPane.ERROR_MESSAGE);
  }

  public void fileNew() {
    if (textPane.hasChanged() && !askSaveChanges(true)) return;
    textPane.newFile();
    refreshMenuItems(false);
    refreshTitleBar();
  }

  public void fileOpen() {
    if (textPane.hasChanged() && !askSaveChanges(true)) return;
    textPane.openDialog();
    refreshMenuItems(false);
    refreshTitleBar();
  }

  /** @return true if save was successful */
  public boolean fileSave() {
    boolean success = textPane.saveFile();
    if (success) refreshSaveMenuItem(false);
    return success;
  }

  /** @return true if save was successful */
  public boolean fileSaveAs() {
    boolean success = textPane.saveDialog();
    if (success) {
      refreshSaveMenuItem(false);
      refreshTitleBar();
    }
    return success;
  }

  public void fileExit() {
    if (textPane.hasChanged()) askSaveChanges(false);
    System.exit(0);
  }

  public void editUndo() {
    textPane.undo();
    refreshMenuItems(true);
  }

  public void editRedo() {
    textPane.redo();
    refreshMenuItems(true);
  }

  public void editCut() {
    textPane.cut();
    refreshMenuItems(true);
  }

  public void editCopy() {
    textPane.copy();
    refreshUndoMenuItems();
  }

  public void editPaste() {
    textPane.paste();
    refreshMenuItems(true);
  }

  /** updates menu items when undoable action occurs */
  public void undoableEditHappened(UndoableEditEvent e) {
    if (!e.getEdit().isSignificant()) return;

    // refresh menu items when an undoable event occurs
    refreshMenuItems(true);
  }

  /** returns the main text editor pane */
  public TextEditor getTextPane() {
    return textPane;
  }

  /** tests the TextFrame class */
  public static void main(String[] args) {
    final TextFrame frame = new TextFrame();

    // close program if frame is closed
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        frame.fileExit();
      }
    });

    // display frame onscreen
    frame.setBounds(100, 100, 500, 800);
    frame.setVisible(true);
  }

}
