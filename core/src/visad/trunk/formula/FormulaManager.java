
//
// FormulaManager.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.formula;

import java.lang.reflect.*;
import java.util.*;

/*
   Note: The FormulaManager class does not support strange and unique syntaxes,
   such as a derivative notation like: d(x)/d(y).  If you wish your application
   to support unique syntaxes, simply have the application implement a formula
   "pre-processor" that converts the "strange" syntaxes to "standard" syntax.
   For example, the VisAD Spread Sheet supports derivative notation of the form
   d(x)/d(y) by converting it to the form derive(x, y) before passing the
   formula to the manager.
*/

/** The FormulaManager class is the gateway into the visad.formula package,
    a general-purpose formula parser and evaluator.  After creating a
    FormulaManager object, programs can call the assignFormula, setValue,
    getValue, and remove methods to create, modify, and delete variables,
    respectively.  Variables update automatically when the variables upon which
    they depend change.  For examples of usage, see the FormulaTest class and
    the visad.ss.FormulaCell class.  Note that the visad.formula package is not
    dependent on other VisAD-related classes in any way, so that the package
    can be used in any Java-based program, not just those that utilize
    VisAD.<P> */
public class FormulaManager {

  /** binary operators */
  String[] bOps;

  /** binary operator precedences */
  int[] bPrec;

  /** binary method signatures */
  Method[] bMethods;

  /** unary operators */
  String[] uOps;

  /** unary operator precedences */
  int[] uPrec;

  /** unary method signatures */
  Method[] uMethods;

  /** function names */
  String[] funcs;

  /** function method signatures */
  Method[] fMethods;

  /** implicit function precedence */
  int iPrec;

  /** implicit function methods */
  Method[] iMethods;
  
  /** constant conversion method */
  Method cMethod;

  /** converts an array of strings of the form
      &quot;package.Class.method(Class, Class, ...)&quot;
      to an array of Method objects */
  public static Method[] stringsToMethods(String[] strings) {
    int len = strings.length;
    Method[] methods = new Method[len];
    for (int j=0; j<len; j++) {
      // remove spaces
      StringTokenizer t = new StringTokenizer(strings[j], " ", false);
      String s = "";
      while (t.hasMoreTokens()) s = s + t.nextToken();

      // separate into two strings
      t = new StringTokenizer(s, "(", false);
      String pre = t.nextToken();
      String post = t.nextToken();

      // separate first string into class and method strings
      t = new StringTokenizer(pre, ".", false);
      String c = t.nextToken();
      int count = t.countTokens();
      for (int i=0; i<count-1; i++) c = c + "." + t.nextToken();
      String m = t.nextToken();

      // get argument array of strings
      t = new StringTokenizer(post, ",)", false);
      count = t.countTokens();
      String[] a;
      if (count == 0) a = null;
      else a = new String[count];
      int x = 0;
      while (t.hasMoreTokens()) a[x++] = t.nextToken();

      // convert result to Method object
      Class clas = null;
      try {
        clas = Class.forName(c);
      }
      catch (ClassNotFoundException exc) {
        // ERROR: Class c does not exist!
        methods[j] = null;
        continue;
      }
      Class[] param;
      if (a == null) param = null;
      else param = new Class[a.length];
      for (int i=0; i<count; i++) {
        // hack to convert array arguments to correct form
        if (a[i].endsWith("[]")) {
          a[i] = "[L" + a[i].substring(0, a[i].length()-2);
          while (a[i].endsWith("[]")) {
            a[i] = "[" + a[i].substring(0, a[i].length()-2);
          }
          a[i] = a[i] + ";";
        }

        try {
          param[i] = Class.forName(a[i]);
        }
        catch (ClassNotFoundException exc) {
          // ERROR: Class a[i] does not exist!
          methods[j] = null;
          continue;
        }
      }
      Method method = null;
      try {
        method = clas.getMethod(m, param);
      }
      catch (NoSuchMethodException exc) {
        // ERROR: Method m does not exist!
        methods[j] = null;
        continue;
      }
      methods[j] = method;
    }
    return methods;
  }

  /** construct a new FormulaManager object */
  public FormulaManager(String[] binOps, int[] binPrec, String[] binMethods,
                        String[] unaryOps, int[] unaryPrec,
                        String[] unaryMethods, String[] functions,
                        String[] funcMethods, int implicitPrec,
                        String[] implicitMethods, String constantMethod)
                        throws FormulaException {
    bOps = binOps;
    bPrec = binPrec;
    bMethods = stringsToMethods(binMethods);
    uOps = unaryOps;
    uPrec = unaryPrec;
    uMethods = stringsToMethods(unaryMethods);
    funcs = functions;
    fMethods = stringsToMethods(funcMethods);
    iPrec = implicitPrec;
    iMethods = stringsToMethods(implicitMethods);
    String[] s = new String[1];
    s[0] = constantMethod;
    cMethod = stringsToMethods(s)[0];

    // check that parallel arrays are really parallel
    int l1 = bOps.length;
    int l2 = bPrec.length;
    int l3 = bMethods.length;
    if (l1 != l2 || l1 != l3) {
      throw new FormulaException("Binary arrays must have equal lengths");
    }
    l1 = uOps.length;
    l2 = uPrec.length;
    l3 = uMethods.length;
    if (l1 != l2 || l1 != l3) {
      throw new FormulaException("Unary arrays must have equal lengths");
    }
    l1 = funcs.length;
    l2 = fMethods.length;
    if (l1 != l2) {
      throw new FormulaException("Function arrays must have equal lengths");
    }

    // check that all operators are one character in length, all operators are
    // legal, and all duplicate operators have equal operator precedences
    for (int i=0; i<bOps.length; i++) {
      if (bOps[i].length() > 1) {
        throw new FormulaException("All operators must be one character " +
                                   "in length");
      }
      char c = bOps[i].charAt(0);
      if (c == '(' || c == ')' || c == ',' || (c >= '0' && c <= '9') ||
                                              (c >= 'a' && c <= 'z') ||
                                              (c >= 'A' && c <= 'Z')) {
        throw new FormulaException("The character \"" + c + "\" cannot be " +
                                   "used as an operator");
      }
      for (int j=i+1; j<bOps.length; j++) {
        if (bOps[i].charAt(0) == bOps[j].charAt(0) && bPrec[i] != bPrec[j]) {
          throw new FormulaException("Duplicate operators must have equal " +
                                     "operator precedences");
        }
      }
    }
    for (int i=0; i<uOps.length; i++) {
      if (uOps[i].length() > 1) {
        throw new FormulaException("All operators must be one character " +
                                   "in length");
      }
      char c = uOps[i].charAt(0);
      if (c == '(' || c == ')' || c == ',' || (c >= '0' && c <= '9') ||
                                              (c >= 'a' && c <= 'z') ||
                                              (c >= 'A' && c <= 'Z')) {
        throw new FormulaException("The character \"" + c + "\" cannot be " +
                                   "used as an operator");
      }
      for (int j=i+1; j<uOps.length; j++) {
        if (uOps[i].charAt(0) == uOps[j].charAt(0) && uPrec[i] != uPrec[j]) {
          throw new FormulaException("Duplicate operators must have equal " +
                                     "operator precedences");
        }
      }
    }

    // check that all function names start with a letter
    for (int i=0; i<functions.length; i++) {
      char c = functions[i].charAt(0);
      if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z')) {
        throw new FormulaException("All functions must begin with a letter");
      }
    }

    // check that all methods are legal
    for (int i=0; i<bMethods.length; i++) {
      if (bMethods[i] == null) {
        throw new FormulaException("The method \"" + binMethods[i] +
                                   "\" is not valid");
      }
    }
    for (int i=0; i<uMethods.length; i++) {
      if (uMethods[i] == null) {
        throw new FormulaException("The method \"" + unaryMethods[i] +
                                   "\" is not valid");
      }
    }
    for (int i=0; i<fMethods.length; i++) {
      if (fMethods[i] == null) {
        throw new FormulaException("The method \"" + funcMethods[i] +
                                   "\" is not valid");
      }
    }
    for (int i=0; i<iMethods.length; i++) {
      if (iMethods[i] == null) {
        throw new FormulaException("The method \"" + implicitMethods[i] +
                                   "\" is not valid");
      }
    }
    if (cMethod == null) {
      throw new FormulaException("The method \"" + constantMethod +
                                 "\" is not valid");
    }
  }

  /** list of all variables in this FormulaManager object */
  private Vector Vars = new Vector();

  /** returns the variable &quot;name&quot; */
  FormulaVar getVarByName(String name) throws FormulaException {
    for (int i=0; i<Vars.size(); i++) {
      FormulaVar v = (FormulaVar) Vars.elementAt(i);
      if (v.name.equals(name)) return v;
    }
    throw new FormulaException("No variable called " + name + " exists!");
  }

  /** returns the variable &quot;name&quot;, creating it if necessary */
  FormulaVar getVarByNameOrCreate(String name) throws FormulaException {
    FormulaVar v;
    try {
      v = getVarByName(name);
    }
    catch (FormulaException exc) {
      v = new FormulaVar(name, this);
      Vars.add(v);
    }
    return v;
  }

  /** assign a formula to a variable */
  public void assignFormula(String name, String formula)
                                         throws FormulaException {
    FormulaVar v = getVarByNameOrCreate(name);
    v.setFormula(formula);
  }

  /** gets the current list of errors that occurred when evaluating
      &quot;name&quot; and clears the list */
  public String[] getErrors(String name) {
    try {
      FormulaVar v = getVarByNameOrCreate(name);
      String[] s = v.getErrors();
      v.clearErrors();
      return s;
    }
    catch (FormulaException exc) {
      return null;
    }
  }

  /** checks whether it is safe to remove a variable from the database */
  public boolean canBeRemoved(String name) {
    FormulaVar v = null;
    try {
      getVarByName(name);
    }
    catch (FormulaException exc) { }
    if (v == null) return false;
    else return v.isSafeToDelete();
  }

  /** remove a variable from the database */
  public void remove(String name) throws FormulaException {
    FormulaVar v = getVarByName(name);
    if (v.isSafeToDelete()) {
      v.setValue(null);
      Vars.remove(v);
    }
    else {
      throw new FormulaException("Cannot remove variable " + name + " " +
                                 "because other variables depend on it!");
    }
  }

  /** set a variable's value directly */
  public void setValue(String name, Object value) throws FormulaException {
    FormulaVar v = getVarByNameOrCreate(name);
    v.setValue(value);
  }

  /** get a variable's current value */
  public Object getValue(String name) throws FormulaException {
    FormulaVar v = getVarByName(name);
    return v.getValue();
  }

  /** get a variable's current formula */
  public String getFormula(String name) throws FormulaException {
    FormulaVar v = getVarByName(name);
    return v.getFormula();
  }

  /** add a listener for when a variable changes */
  public void addVarChangeListener(String name, FormulaListener f)
                                   throws FormulaException {
    FormulaVar v = getVarByName(name);
    v.addListener(f);
  }

  public void removeVarChangeListener(String name, FormulaListener f)
                                      throws FormulaException {
    FormulaVar v = getVarByName(name);
    v.removeListener(f);
  }

  /** identify whether a given token is a unary operator */
  boolean isUnaryOp(String op) {
    for (int i=0; i<uOps.length; i++) {
      if (uOps[i].equals(op)) return true;
    }
    return false;
  }

  /** identify whether a given token is a binary operator */
  boolean isBinaryOp(String op) {
    for (int i=0; i<bOps.length; i++) {
      if (bOps[i].equals(op)) return true;
    }
    return false;
  }

  /** identify whether a given token is a defined function */
  boolean isFunction(String token) {
    for (int i=0; i<funcs.length; i++) {
      if (funcs[i].equals(token)) return true;
    }
    return false;
  }

  /** returns a unary operator's level of precedence */
  int getUnaryPrec(String op) {
    for (int i=0; i<uOps.length; i++) {
      if (uOps[i].equals(op)) return uPrec[i];
    }
    return -1;
  }

  /** returns a binary operator's level of precedence */
  int getBinaryPrec(String op) {
    if (op.equals("(")) return Integer.MAX_VALUE;
    if (op.equals(",")) return Integer.MAX_VALUE - 1;
    for (int i=0; i<bOps.length; i++) {
      if (bOps[i].equals(op)) return bPrec[i];
    }
    return -1;
  }

}

