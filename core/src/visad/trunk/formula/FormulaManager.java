//
// FormulaManager.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

package visad.formula;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;

/** The FormulaManager class is the gateway into the visad.formula package,
    a general-purpose formula parser and evaluator.  Variables update
    automatically when the variables upon which they depend change.
    For an example of usage, see the FormulaUtil.createStandardManager()
    method.<P> */
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

  /** formula text pre-parsing method.  For an application to support unusual
      non-standard syntaxes, such as brackets (e.g., VAR[0]), that application
      should supply a &quot;pre-parsing&quot; method in the preParseMethod
      argument, which converts non-standard syntax to standard syntax (e.g.,
      VAR[0] could become getElement(VAR, 0)).  The supplied method should be
      a static method taking a String argument (the formula) and a
      FormulaManager argument (this FormulaManager).  If the application does
      not need such functionality, the preParseMethod argument can be null. */
  Method ppMethod;

  /** construct a new FormulaManager object */
  public FormulaManager(String[] binOps, int[] binPrec, String[] binMethods,
    String[] unaryOps, int[] unaryPrec, String[] unaryMethods,
    String[] functions, String[] funcMethods, int implicitPrec,
    String[] implicitMethods, String preParseMethod) throws FormulaException
  {
    bOps = binOps;
    bPrec = binPrec;
    bMethods = FormulaUtil.stringsToMethods(binMethods);
    uOps = unaryOps;
    uPrec = unaryPrec;
    uMethods = FormulaUtil.stringsToMethods(unaryMethods);
    funcs = functions;
    fMethods = FormulaUtil.stringsToMethods(funcMethods);
    iPrec = implicitPrec;
    iMethods = FormulaUtil.stringsToMethods(implicitMethods);
    String[] s = new String[1];
    if (preParseMethod == null) ppMethod = null;
    else {
      String[] pps = new String[] {preParseMethod};
      Method[] ppm = FormulaUtil.stringsToMethods(pps);
      ppMethod = ppm[0];
    }

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
  }

  /** add a variable to the database that uses tr as its ThingReference */
  public void createVar(String name, ThingReference tr) throws VisADException {
    FormulaVar v;
    try {
      v = getVarByName(name);
    }
    catch (FormulaException exc) {
      v = null;
    }
    if (v != null) {
      throw new FormulaException("The variable " + name + " already exists.");
    }
    Vars.add(new FormulaVar(name, this, tr));
  }

  /** assign a formula to a variable */
  public void assignFormula(String name, String formula)
    throws VisADException
  {
    FormulaVar v = getVarByNameOrCreate(name);
    v.setFormula(formula);
  }

  /** blocks until this variable's formula is finished computing */
  public void waitForFormula(String name) throws VisADException {
    FormulaVar v = getVarByName(name);
    v.waitForFormula();
  }

  /** set a variable to auto-update its formula based on a Text object
      referenced by a ThingReference (useful for remote formula updates) */
  public void setTextRef(String name, ThingReference textRef)
    throws VisADException, RemoteException
  {
    FormulaVar v = getVarByNameOrCreate(name);
    v.setTextRef(textRef);
  }

  /** get the current list of errors that occurred when evaluating
      &quot;name&quot; and clear the list */
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
    catch (VisADException exc) {
      return null;
    }
  }

  /** check whether it is safe to remove a variable from the database */
  public boolean canBeRemoved(String name) throws FormulaException {
    FormulaVar v = getVarByName(name);
    return !v.othersDepend();
  }

  /** check whether a given variable is currently in the database */
  public boolean exists(String name) {
    boolean exists = false;
    try {
      FormulaVar v = getVarByName(name);
      exists = true;
    }
    catch (FormulaException exc) { }
    return exists;
  }

  /** remove a variable from the database */
  public void remove(String name) throws FormulaException {
    if (canBeRemoved(name)) Vars.remove(getVarByName(name));
    else {
      throw new FormulaException("Cannot remove variable " + name + " " +
                                 "because other variables depend on it!");
    }
  }

  /** set a variable's value directly */
  public void setThing(String name, Thing t)
    throws VisADException, RemoteException
  {
    FormulaVar v = getVarByNameOrCreate(name);
    v.setThing(t);
  }

  /** set a variable's ThingReference */
  public void setReference(String name, ThingReference tr)
    throws VisADException
  {
    FormulaVar v = getVarByNameOrCreate(name);
    v.setReference(tr);
  }

  /** get a variable's current value */
  public Thing getThing(String name) throws FormulaException {
    FormulaVar v = getVarByName(name);
    return v.getThing();
  }

  /** get a variable's associated ThingReference */
  public ThingReference getReference(String name) throws FormulaException {
    FormulaVar v = getVarByName(name);
    return v.getReference();
  }

  /** get a variable's current formula */
  public String getFormula(String name) throws FormulaException {
    FormulaVar v = getVarByName(name);
    return v.getFormula();
  }

  /** list of all variables in this FormulaManager object */
  private Vector Vars = new Vector();

  /** return the variable &quot;name&quot; */
  FormulaVar getVarByName(String name) throws FormulaException {
    for (int i=0; i<Vars.size(); i++) {
      FormulaVar v = (FormulaVar) Vars.elementAt(i);
      if (v.name.equalsIgnoreCase(name)) return v;
    }
    throw new FormulaException("The variable " + name + " does not exist.");
  }

  /** return the variable &quot;name&quot;, creating it if necessary */
  FormulaVar getVarByNameOrCreate(String name) throws VisADException {
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
      if (funcs[i].equalsIgnoreCase(token)) return true;
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

