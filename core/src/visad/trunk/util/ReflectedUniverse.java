//
// ReflectedUniverse.java
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

package visad.util;

import visad.VisADException;
import java.lang.reflect.*;
import java.util.*;

/**
 * A general-purpose reflection wrapper class. See visad.data.tiff.TiffForm,
 * visad.data.jai.JAIForm, and visad.data.qt.QTForm for examples of usage.
 */
public class ReflectedUniverse {

  /** Hashtable containing all variables present in the universe. */
  private Hashtable variables = new Hashtable();

  public ReflectedUniverse() { }

  /** Executes a command in the universe. */
  public Object exec(String command) throws VisADException {
    command = command.trim();
    if (command.startsWith("import ")) {
      // command is an import statement
      command = command.substring(7).trim();
      int dot = command.lastIndexOf(".");
      String varName = dot < 0 ? command : command.substring(dot + 1);
      Class c;
      try {
        c = Class.forName(command);
      }
      catch (ClassNotFoundException exc) {
        throw new VisADException("No such class: " + command);
      }
      setVar(varName, c);
      return null;
    }

    // get variable where results of command should be stored
    int eqIndex = command.indexOf("=");
    String target = null;
    if (eqIndex >= 0) {
      target = command.substring(0, eqIndex).trim();
      command = command.substring(eqIndex + 1).trim();
    }

    // parse parentheses
    int leftParen = command.indexOf("(");
    if (leftParen < 0 || leftParen != command.lastIndexOf("(") ||
      command.indexOf(")") != command.length() - 1)
    {
      throw new VisADException("invalid parentheses");
    }

    // parse arguments
    String arglist = command.substring(leftParen + 1);
    StringTokenizer st = new StringTokenizer(arglist, "(,)");
    int len = st.countTokens();
    Object[] args = new Object[len];
    Class[] argClasses = new Class[len];
    for (int i=0; i<len; i++) {
      String arg = st.nextToken().trim();
      args[i] = getVar(arg);
      argClasses[i] = args[i].getClass();
    }
    command = command.substring(0, leftParen);

    Object result;
    if (command.startsWith("new ")) {
      // command is a constructor call
      String className = command.substring(4).trim();
      Object var = getVar(className);
      if (!(var instanceof Class)) {
        throw new VisADException("not a class: " + className);
      }
      Class cl = (Class) var;

      // Search for a constructor that matches the arguments. Unfortunately,
      // calling cl.getConstructor(argClasses) does not work, because
      // getConstructor() is not flexible enough to detect when the arguments
      // are subclasses of the constructor argument classes, making a brute
      // force search through all public constructors necessary.
      Constructor constructor = null;
      Constructor[] c = cl.getConstructors();
      for (int i=0; i<c.length; i++) {
        Class[] params = c[i].getParameterTypes();
        if (params.length == args.length) {
          boolean match = true;
          for (int j=0; j<params.length; j++) {
            if (!params[j].isInstance(args[j])) {
              match = false;
              break;
            }
          }
          if (match) {
            constructor = c[i];
            break;
          }
        }
      }
      if (constructor == null) {
        throw new VisADException("No such constructor");
      }

      // invoke constructor
      try {
        result = constructor.newInstance(args);
      }
      catch (Exception exc) {
        throw new VisADException("Cannot instantiate object");
      }
    }
    else {
      // command is a method call
      int dot = command.indexOf(".");
      if (dot < 0) throw new VisADException("syntax error");
      String varName = command.substring(0, dot).trim();
      String methodName = command.substring(dot + 1).trim();
      Object var = getVar(varName);
      Class varClass = var instanceof Class ? (Class) var : var.getClass();

      // Search for a method that matches the arguments. Unfortunately,
      // calling varClass.getMethod(methodName, argClasses) does not work,
      // because getMethod() is not flexible enough to detect when the
      // arguments are subclasses of the method argument classes, making a
      // brute force search through all public methods necessary.
      Method method = null;
      Method[] m = varClass.getMethods();
      for (int i=0; i<m.length; i++) {
        if (methodName.equals(m[i].getName())) {
          Class[] params = m[i].getParameterTypes();
          if (params.length == args.length) {
            boolean match = true;
            for (int j=0; j<params.length; j++) {
              if (!params[j].isInstance(args[j])) {
                match = false;
                break;
              }
            }
            if (match) {
              method = m[i];
              break;
            }
          }
        }
      }
      if (method == null) {
        throw new VisADException("No such method: " + methodName);
      }

      // invoke method
      try {
        result = method.invoke(var, args);
      }
      catch (Exception exc) {
        throw new VisADException("Cannot execute method: " + methodName);
      }
    }

    // assign result to proper variable
    if (target != null) setVar(target, result);
    return result;
  }

  /** Registers a variable in the universe. */
  public void setVar(String varName, Object obj) {
    variables.put(varName, obj);
  }

  /** Returns the value of a variable or field in the universe. */
  public Object getVar(String varName) throws VisADException {
    int dot = varName.indexOf(".");
    if (dot >= 0) {
      // get field value of variable
      Object var = variables.get(varName.substring(0, dot).trim());
      Class varClass = var instanceof Class ? (Class) var : var.getClass();
      String fieldName = varName.substring(dot + 1).trim();
      Field field;
      try {
        field = varClass.getField(fieldName);
      }
      catch (NoSuchFieldException exc) {
        throw new VisADException("No such field: " + varName);
      }
      Object fieldVal;
      try {
        fieldVal = field.get(var);
      }
      catch (Exception exc) {
        throw new VisADException("Cannot get field value: " + varName);
      }
      return fieldVal;
    }
    else {
      // get variable
      Object var = variables.get(varName);
      return var;
    }
  }

}
