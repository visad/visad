//
// TextEditor.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Font;
import java.io.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.event.*;
import javax.swing.undo.*;

/** A general-purpose editor for reading and writing text files. */
public class TextEditor extends JScrollPane implements UndoableEditListener {

  /** monospaced font */
  public static final Font MONO = new Font("monospaced", Font.PLAIN, 12);

  /** debugging flag */
  public static final boolean DEBUG = false;

  /** main text area */
  protected JTextArea text;

  /** file chooser dialog box */
  private JFileChooser fileChooser;

  /** undo manager */
  protected UndoManager undo = new UndoManager();

  /** file being edited */
  protected File currentFile;

  /** whether the text has changed since last save */
  protected boolean changed = false;


  /** constructs a TextEditor */
  public TextEditor() {
    this(null);
  }

  /** constructs a TextEditor containing text from the given filename */
  public TextEditor(String filename) {
    super();
    text = new JTextArea();

    // load file, if any
    try {
      openFile(filename);
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
    }

    // setup text area
    text.setFont(MONO);
    setViewportView(text);

    // provide support for undo and redo
    addUndoableEditListener(this);

    // setup file chooser dialog box
  }

  /** Create and initialize the the file chooser */
  protected JFileChooser doMakeFileChooser() {
       JFileChooser tmpChooser = new JFileChooser(System.getProperty("user.dir"));
       tmpChooser.addChoosableFileFilter(
         new ExtensionFileFilter("txt", "Text files"));
       return tmpChooser;
  }


  /** Create if needed and return the file chooser */
  protected JFileChooser getFileChooser() {
    if(fileChooser == null) {
       fileChooser = doMakeFileChooser();
    }
    return fileChooser;
  }

  /** starts from scratch with a blank document */
  public void newFile() {
    currentFile = null;
    setText("");
    undo.discardAllEdits();
    changed = false;
  }

  /** opens the given file */
  public void openFile(String filename) throws IOException {
    File file = (filename == null ? null : new File(filename));
    openFile(file);
  }

  /** opens the given file */
  public void openFile(File file) throws IOException {
    String fileText;
    if (file == null) fileText = "";
    else {
      int len = (int) file.length();
      byte[] bytes = new byte[len];
      FileInputStream in = new FileInputStream(file);
      in.read(bytes);
      in.close();
      fileText = new String(bytes);
    }
    currentFile = file;
    setText(fileText);
    changed = false;
  }

  /** saves the given file */
  public void saveFile(String filename) throws IOException {
    File file = (filename == null ? null : new File(filename));
    saveFile(file);
  }

  /** saves the given file */
  public void saveFile(File file) throws IOException {
    byte[] bytes = getText().getBytes();
    FileOutputStream out = new FileOutputStream(file);
    out.write(bytes);
    out.close();
    currentFile = file;
    changed = false;
  }
 
  /** pops up a dialog box for the user to select a file to open */
  public boolean openDialog() {
    getFileChooser().setDialogType(JFileChooser.OPEN_DIALOG);
    if (getFileChooser().showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
      // user has canceled request
      return false;
    }
    try {
      openFile(getFileChooser().getSelectedFile());
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
      return false;
    }
    return true;
  }

  /** pops up a dialog box for the user to select a file to save */
  public boolean saveDialog() {
    getFileChooser().setDialogType(JFileChooser.SAVE_DIALOG);
    if (getFileChooser().showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
      // user has canceled request
      return false;
    }
    try {
      saveFile(getFileChooser().getSelectedFile());
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
      return false;
    }
    return true;
  }

  /** saves the file under its current name */
  public boolean saveFile() {
    boolean success = false;
    if (currentFile == null) success = saveDialog();
    else {
      try {
        saveFile(currentFile);
        success = true;
      }
      catch (IOException exc) {
        // display error box
        JOptionPane.showMessageDialog(this, "Could not save the file.",
          "VisAD Text Editor", JOptionPane.ERROR_MESSAGE);
      }
    }
    return success;
  }

  /** undoes the last edit */
  public void undo() throws CannotUndoException {
    undo.undo();
    changed = true;
  }

  /** redoes the last undone edit */
  public void redo() throws CannotRedoException {
    undo.redo();
    changed = true;
  }

  /** cuts the selected text to the clipboard */
  public void cut() {
    text.cut();
  }

  /** copies the selected text to the clipboard */
  public void copy() {
    text.copy();
  }

  /** pastes the clipboard into the text document */
  public void paste() {
    text.paste();
  }

 /**
  * Provide direct access to the text component
  *
  * @return the text component
  */
 public JTextComponent getTextComponent() {
     return text;
 }


  /** returns a string containing the text of the document */
  public String getText() {
    return text.getText();
  }

  /** sets the text of this document to the current string */
  public void setText(String text) {
    this.text.setText(text);
  }
  
  /**
   * Insert the given text at the caret
   *
   * @param textToInsert the text
   */
  public void insertText(String textToInsert) {
    int    pos = this.text.getCaretPosition();
    String t   = this.text.getText();
    t = t.substring(0, pos) + textToInsert + t.substring(pos);
    this.text.setText(t);
    this.text.setCaretPosition(pos + textToInsert.length());
  }


  /** returns the filename being edited */
  public String getFilename() {
    return (currentFile == null ? null : currentFile.getPath());
  }
  
  /** returns the file being edited */
  public File getFile() {
    return currentFile;
  }

  /** returns whether an undo command is possible */
  public boolean canUndo() {
    return undo.canUndo();
  }

  /** returns whether a redo command is possible */
  public boolean canRedo() {
    return undo.canRedo();
  }
  
  /** returns the name of the undo command */
  public String getUndoName() {
    return undo.getUndoPresentationName();
  }

  /** returns the name of the redo command */
  public String getRedoName() {
    return undo.getRedoPresentationName();
  }
  
  /** returns whether the document has changed since the last save */
  public boolean hasChanged() {
    return changed;
  }

  /** handle undoable edits */
  public void undoableEditHappened(UndoableEditEvent e) {
    if (!e.getEdit().isSignificant()) return;
    undo.addEdit(e.getEdit());
    changed = true;
  }

  /** add an undoable edit listener */
  public void addUndoableEditListener(UndoableEditListener l) {
    text.getDocument().addUndoableEditListener(l);
  }

  /** remove an undoable edit listener */
  public void removeUndoableEditListener(UndoableEditListener l) {
    text.getDocument().removeUndoableEditListener(l);
  }

}
