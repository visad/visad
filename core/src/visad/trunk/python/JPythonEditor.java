//
// JPythonEditor.java
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

package visad.python;

import java.lang.reflect.*;
import javax.swing.JOptionPane;
import visad.VisADException;
import visad.formula.FormulaUtil;
import visad.util.*;

/** An editor for writing and executing JPython code in Java runtime. */
public class JPythonEditor extends CodeEditor {

  /** text to be prepended to all JPython documents */
  private static final String PREPENDED_TEXT =
    "from visad.python.JPythonMethods import *";

  /** name of JPython interpreter class */
  private static final String interp = "org.python.util.PythonInterpreter";

  /** JPython interpreter class */
  private static final Class interpClass = constructInterpClass();

  /** obtains the JPython interpreter class */
  private static Class constructInterpClass() {
    Class c = null;
    try {
      c = Class.forName(interp);
    }
    catch (ClassNotFoundException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    return c;
  }

  /** names of useful methods from JPython interpreter class */
  private static final String[] methodNames = {
    interp + ".eval(java.lang.String)",
    interp + ".exec(java.lang.String)",
    interp + ".execfile(java.lang.String)",
    interp + ".set(java.lang.String, org.python.core.PyObject)",
    interp + ".get(java.lang.String)"
  };

  /** useful methods from JPython interpreter class */
  private static final Method[] methods =
    FormulaUtil.stringsToMethods(methodNames);

  /** method for evaluating a line of JPython code */
  private static final Method eval = methods[0];

  /** method for executing a line of JPython code */
  private static final Method exec = methods[1];

  /** method for executing a document containing JPython code */
  private static final Method execfile = methods[2];

  /** method for setting a JPython variable's value */
  private static final Method set = methods[3];

  /** method for getting a JPython variable's value */
  private static final Method get = methods[4];

  /** PythonInterpreter object */
  protected Object python = null;

  /** flag indicating whether to warn before auto-saving */
  protected boolean warnBeforeSave = true;


  /** constructs a JPythonEditor */
  public JPythonEditor() throws VisADException {
    this(null);
  }

  /** constructs a JPythonEditor containing text from the given filename */
  public JPythonEditor(String filename) throws VisADException {
    super(filename);

    // construct a JPython interpreter
    try {
      python = interpClass.newInstance();
    }
    catch (NullPointerException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (IllegalAccessException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (InstantiationException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    if (python == null) throw new VisADException("JPython library not found");

    // add JPython files to file chooser dialog box
    fileChooser.addChoosableFileFilter(
      new ExtensionFileFilter("py", "JPython source code"));
  }

  /** adjusts the line number of the given error to match the displayed text,
      and highlights that line of code to indicate the source of error */
  private String handleError(String err) {
    int index = err.indexOf("line ") + 5;
    if (index < 0) return err; 
    int comma = err.indexOf(",", index);
    int lineNum = -1;
    try {
      lineNum = Integer.parseInt(err.substring(index, comma));
    }
    catch (NumberFormatException exc) {
      return err;
    }
    lineNum--;
    highlightLine(lineNum);
    return err.substring(0, index) + lineNum + err.substring(comma);
  }

  /** evaluates a line of JPython code */
  public Object eval(String line) throws VisADException {
    try {
      return eval.invoke(python, new Object[] {line});
    }
    catch (IllegalAccessException exc) {
      throw new VisADException(exc.toString());
    }
    catch (IllegalArgumentException exc) {
      throw new VisADException(exc.toString());
    }
    catch (InvocationTargetException exc) {
      throw new VisADException(exc.getTargetException().toString());
    }
  }

  /** executes a line of JPython code */
  public void exec(String line) throws VisADException {
    try {
      exec.invoke(python, new Object[] {line});
    }
    catch (IllegalAccessException exc) {
      throw new VisADException(exc.toString());
    }
    catch (IllegalArgumentException exc) {
      throw new VisADException(exc.toString());
    }
    catch (InvocationTargetException exc) {
      throw new VisADException(exc.getTargetException().toString());
    }
  }

  /** executes the document as JPython source code */
  public void execfile(String filename) throws VisADException {
    try {
      execfile.invoke(python, new Object[] {filename});
    }
    catch (IllegalAccessException exc) {
      throw new VisADException(exc.toString());
    }
    catch (IllegalArgumentException exc) {
      throw new VisADException(exc.toString());
    }
    catch (InvocationTargetException exc) {
      String error = exc.getTargetException().toString();
      error = handleError(error);
      throw new VisADException(error);
    }
  }

  /** sets a JPython variable's value */
  public void set(String name, Object value) throws VisADException {
    try {
      set.invoke(python, new Object[] {name, value});
    }
    catch (IllegalAccessException exc) {
      throw new VisADException(exc.toString());
    }
    catch (IllegalArgumentException exc) {
      throw new VisADException(exc.toString());
    }
    catch (InvocationTargetException exc) {
      throw new VisADException(exc.getTargetException().toString());
    }
  }

  /** gets a JPython variable's value */
  public Object get(String name) throws VisADException {
    try {
      return get.invoke(python, new Object[] {name});
    }
    catch (IllegalAccessException exc) {
      throw new VisADException(exc.toString());
    }
    catch (IllegalArgumentException exc) {
      throw new VisADException(exc.toString());
    }
    catch (InvocationTargetException exc) {
      throw new VisADException(exc.getTargetException().toString());
    }
  }

  /** returns a string containing the text of the document */
  public String getText() {
    return PREPENDED_TEXT +
      System.getProperty("line.separator") + super.getText();
  }

  /** sets the text of the document to the current string */
  public void setText(String text) {
    if (text.startsWith(PREPENDED_TEXT)) {
      // strip off prepended text
      text = text.substring(PREPENDED_TEXT.length()).trim();
    }
    super.setText(text);
  }

  /** sets whether editor should warn user before auto-saving */
  public void setWarnBeforeSave(boolean warn) {
    warnBeforeSave = warn;
  }

  /** executes the JPython script */
  public void run() throws VisADException {
    if (hasChanged()) {
      if (warnBeforeSave) {
        int ans = JOptionPane.showConfirmDialog(this,
          "A save is required before execution. Okay to save?",
          "VisAD JPython Editor", JOptionPane.YES_NO_OPTION);
        if (ans != JOptionPane.YES_OPTION) return;
      }
      boolean success = saveFile();
      if (!success) return;
    }
    execfile(getFilename());
  }

  /** compiles the JPython script to a Java class */
  public void compile() throws VisADException {
    throw new VisADException("Not yet implemented!");
  }
  
}
