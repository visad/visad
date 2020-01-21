//
// RunJPython.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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
import visad.VisADException;
import visad.formula.FormulaUtil;

/** A stand-alone wrapper for launching JPython code. */
public class RunJPython {

  /** debugging flag */
  private static final boolean DEBUG = false;

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


  /** constructs a RunJPython object */
  public RunJPython() throws VisADException {
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
    if (python == null) {
      throw new VisADException("JPython library not found - " +
        "install Jython from http://jython.sourceforge.net/");
    }
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
      throw new VisADException(exc.getTargetException().toString());
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

  /** launches a JPython script */
  public static void main(String[] args) throws VisADException {
    if (args.length < 1) return;
    try { new RunJPython().execfile(args[0]); }
    catch (VisADException exc) { exc.printStackTrace(System.out); }
  }

}
