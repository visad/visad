
//
// FormulaVar.java
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

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.*;
import visad.*;

/** Represents a variable.<P> */
public class FormulaVar extends ActionImpl {

  /** for testing purposes */
  private static boolean DEBUG = true;

  /** constant tag */
  public static RealType CONSTANT = createConstant();

  /** create constant/fconst tag */
  private static RealType createConstant() {
    RealType rt = null;
    String s = "visad/formula/constant";
    try {
      rt = new RealType(s);
    }
    catch (VisADException exc) {
      rt = RealType.getRealTypeByName(s);
    }
    return rt;
  }

  /** associated FormulaManager object */
  private FormulaManager fm;

  /** name of this variable */
  String name;

  /** formula associated with this variable, if any */
  private String formula;

  /** formula in postfix notation, if it has been converted */
  private Postfix postfix;

  /** reference of this variable */
  private ThingReference tref;

  /** list of errors that have occurred during computation, in String form */
  private Vector errors = new Vector();

  /** constructor without specified ThingReference */
  FormulaVar(String n, FormulaManager f) throws VisADException {
    this(n, f, null);
  }

  /** constructor with specified ThingReference */
  FormulaVar(String n, FormulaManager f, ThingReference t)
                                         throws VisADException {
    super(n);
    fm = f;
    name = n;
    tref = (t == null) ? new ThingReferenceImpl(name) : t;
    for (int i=0; i<fm.bOps.length; i++) {
      if (name.indexOf(fm.bOps[i]) >= 0) {
        throw new FormulaException("variable names cannot contain operators");
      }
    }
    for (int i=0; i<fm.uOps.length; i++) {
      if (name.indexOf(fm.uOps[i]) >= 0) {
        throw new FormulaException("variable names cannot contain operators");
      }
    }
  }

  /** vector of variables on which this one depends */
  private Vector depend = new Vector();

  /** return whether this variable depends on v */
  boolean isDependentOn(FormulaVar v) {
    if (v == this || depend.contains(v)) return true;
    for (int i=0; i<depend.size(); i++) {
      FormulaVar vi = (FormulaVar) depend.elementAt(i);
      if (vi.isDependentOn(v)) return true;
    }
    return false;
  }

  /** add a dependency for this variable */
  void setDependentOn(FormulaVar v) {
    if (!depend.contains(v)) {
      depend.add(v);
      try {
        addReference(v.getReference());
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  }

  /** clear this variable's dependency list */
  private void clearDependencies() {
    depend.removeAllElements();
    try {
      removeAllReferences();
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
  }

  /** flag for whether doAction should actually do anything */
  private boolean reallyDoIt = true;

  /** rebuild this variable's dependency list, then recompute this variable */
  private void rebuildDependencies() throws FormulaException {
    reallyDoIt = false;
    clearDependencies();
    if (formula != null) {
      if (postfix == null) {
        try {
          // compute postfix expression
          postfix = new Postfix(formula, fm);
          int len = (postfix.tokens == null ? 0 : postfix.tokens.length);
          for (int i=0; i<len; i++) {
            String token = postfix.tokens[i];
            if (postfix.codes[i] == Postfix.OTHER) {
              Double d = null;
              try {
                d = Double.valueOf(token);
              }
              catch (NumberFormatException exc) { }
              if (d == null) {
                // token is a variable name
                FormulaVar v = null;
                try {
                  v = fm.getVarByNameOrCreate(token);
                }
                catch (FormulaException exc) {
                  evalError("\"" + token + "\" is an illegal variable name");
                }
                catch (VisADException exc) {
                  evalError("Internal error: " + exc);
                }
                if (v != null) {
                  if (v.isDependentOn(this)) {
                    clearDependencies();
                    throw new FormulaException("This formula creates " + 
                                               "an infinite loop");
                  }
                  setDependentOn(v);
                }
              }
            }
          }
        }
        catch (FormulaException exc) {
          evalError("Syntax error in formula: " + exc);
          try {
            reallyDoIt = true;
            tref.setThing(null);
          }
          catch (VisADException exc2) {
            evalError("Internal error: " + exc2);
          }
          catch (RemoteException exc2) {
            evalError("Internal error: " + exc2);
          }
        }
      }
    }
    reallyDoIt = true;
  }

  /** set the formula for this variable */
  void setFormula(String f) throws FormulaException {
    formula = f;
    postfix = null;
    rebuildDependencies();
  }

  /** set the Thing for this variable directly */
  void setThing(Thing t) throws VisADException, RemoteException {
    if (tref.getThing() == t) return;
    formula = null;
    postfix = null;
    clearDependencies();
    tref.setThing(t);
  }

  /** get the Thing for this variable */
  Thing getThing() {
    try {
      return tref.getThing();
    }
    catch (VisADException exc) {
      return null;
    }
    catch (RemoteException exc) {
      return null;
    }
  }

  /** get the ThingReference for this variable */
  ThingReference getReference() {
    return tref;
  }

  /** get the formula for this variable */
  String getFormula() {
    return formula;
  }

  /** get an array of Strings representing any errors that have occurred
      during formula evaluation */
  String[] getErrors() {
    synchronized (errors) {
      int len = errors.size();
      if (len == 0) return null;
      String[] s = new String[len];
      for (int i=0; i<len; i++) {
        s[i] = (String) errors.elementAt(i);
      }
      return s;
    }
  }

  /** clear the list of errors that have occurred during formula evaluation */
  void clearErrors() {
    synchronized (errors) {
      errors.clear();
    }
  }

  /** add an error to the list of errors that have occurred during
      formula evaluation */
  private void evalError(String s) {
    synchronized (errors) {
      errors.add(s);
    }
  }

  /** recompute this variable */
  public void doAction() {
    if (reallyDoIt) {
      try {
        tref.setThing(compute(postfix));
      }
      catch (VisADException exc) {
        evalError("Could not store final value in variable");
      }
      catch (RemoteException exc) {
        evalError("Could not store final value in variable");
      }
    }
  }

  /** used by compute method for convenience */
  private Thing popStack(Stack s) {
    if (s.empty()) {
      evalError("Syntax error in formula (a)");
      return null;
    }
    else return (Thing) s.pop();
  }

  /** compute the solution to this variable's postfix formula */
  private Thing compute(Postfix formula) {
    if (formula.tokens == null) return null;
    int len = formula.tokens.length;
    Stack stack = new Stack();
    for (int i=0; i<len; i++) {
      String token = formula.tokens[i];
      int code = formula.codes[i];
      if (code == Postfix.BINARY) {
        Object[] o = new Object[2];
        o[1] = popStack(stack);
        o[0] = popStack(stack);
        Thing ans = null;
        if (o[0] != null && o[1] != null) {
          for (int j=0; j<fm.bMethods.length; j++) {
            // support for overloaded operators
            if (ans == null && fm.bOps[j].equals(token)) {
              try {
                ans = FormulaUtil.invokeMethod(fm.bMethods[j], o);
              }
              catch (IllegalAccessException exc) {
                if (DEBUG) System.out.println(exc.toString());
              } // no access
              catch (IllegalArgumentException exc) {
                if (DEBUG) System.out.println(exc.toString());
              } // wrong type of method
              catch (InvocationTargetException exc) {
                if (DEBUG) System.out.println(exc.toString());
              } // method threw exception
            }
          }
        }
        if (ans == null) {
          evalError("Could not evaluate binary operator \"" + token +"\"");
          stack.push(null);
        }
        else stack.push(ans);
      }
      else if (code == Postfix.UNARY) {
        Object[] o = new Object[1];
        o[0] = popStack(stack);
        Thing ans = null;
        if (o[0] != null) {
          for (int j=0; j<fm.uMethods.length; j++) {
            // support for overloaded operators
            if (ans == null && fm.uOps[j].equals(token)) {
              try {
                ans = FormulaUtil.invokeMethod(fm.uMethods[j], o);
              }
              catch (IllegalAccessException exc) {
                if (DEBUG) System.out.println(exc.toString());
              } // no access
              catch (IllegalArgumentException exc) {
                if (DEBUG) System.out.println(exc.toString());
              } // wrong type of method
              catch (InvocationTargetException exc) {
                if (DEBUG) System.out.println(exc.toString());
              } // method threw exception
            }
          }
        }
        if (ans == null) {
          evalError("Could not evaluate unary operator \"" + token + "\"");
          stack.push(null);
        }
        else stack.push(ans);
      }
      else if (code == Postfix.FUNC) {
        Thing ans = null;
        if (fm.isFunction(token)) {
          // defined function - token is the function name
          int num = -1;
          try {
            Real r = (Real) popStack(stack);
            num = (int) r.getValue();
          }
          catch (ClassCastException exc) { }
          if (num < 0) {
            evalError("Syntax error in formula (b)");
            num = 1;
          }
          Object[] o;
          if (num > 0) o = new Object[num];
          else o = null;
          boolean eflag = false;
          for (int j=num-1; j>=0; j--) {
            o[j] = popStack(stack);
            if (o[j] == null) eflag = true;
          }
          if (!eflag) {
            for (int j=0; j<fm.funcs.length; j++) {
              // support for overloaded defined functions
              if (ans == null && fm.funcs[j].equalsIgnoreCase(token)) {
                try {
                  ans = FormulaUtil.invokeMethod(fm.fMethods[j], o);
                }
                catch (IllegalAccessException exc) {
                  if (DEBUG) System.out.println(exc.toString());
                } // no access
                catch (IllegalArgumentException exc) {
                  if (DEBUG) System.out.println(exc.toString());
                } // wrong type of method
                catch (InvocationTargetException exc) {
                  if (DEBUG) System.out.println(exc.toString());
                } // method threw exception
              }
            }
          }
          if (ans == null) {
            evalError("Could not evaluate function \"" + token + "\"");
            stack.push(null);
          }
          else stack.push(ans);
        }
        else {
          // implicit function - token is a non-negative integer
          int num = 0;
          try {
            num = Integer.parseInt(token) + 1;
          }
          catch (NumberFormatException exc) { }
          if (num <= 0) {
            evalError("Syntax error in formula (c)");
            num = 1;
          }
          Object[] o = new Object[num];
          boolean eflag = false;
          for (int j=num-1; j>=0; j--) {
            o[j] = popStack(stack);
            if (o[j] == null) eflag = true;
          }
          if (!eflag) {
            for (int j=0; j<fm.iMethods.length; j++) {
              // support for overloaded implicit functions
              if (ans == null) {
                try {
                  ans = FormulaUtil.invokeMethod(fm.iMethods[j], o);
                }
                catch (IllegalAccessException exc) {
                  if (DEBUG) System.out.println(exc.toString());
                } // no access
                catch (IllegalArgumentException exc) {
                  if (DEBUG) System.out.println(exc.toString());
                } // wrong type of method
                catch (InvocationTargetException exc) {
                  if (DEBUG) System.out.println(exc.toString());
                } // method threw exception
              }
            }
          }
          if (ans == null) {
            evalError("Could not evaluate implicit function");
            stack.push(null);
          }
          else stack.push(ans);
        }
      }
      else { // code == Postfix.OTHER
        Double d = null;
        try {
          d = Double.valueOf(token);
        }
        catch (NumberFormatException exc) { }
        if (d == null) {
          // token is a variable name
          FormulaVar v = null;
          try {
            v = fm.getVarByNameOrCreate(token);
          }
          catch (FormulaException exc) {
            evalError("\"" + token + "\" is an illegal variable name");
            stack.push(null);
          }
          catch (VisADException exc) {
            evalError("Internal error: " + exc);
            stack.push(null);
          }
          if (v != null) {
            ThingReference r = v.getReference();
            Thing t = null;
            if (r != null) {
              try {
                t = r.getThing();
              }
              catch (VisADException exc) { }
              catch (RemoteException exc) { }
            }
            if (t == null) {
              evalError("Variable \"" + token + "\" has no value");
              stack.push(null);
            }
            else stack.push(t);
          }
        }
        else {
          // token is a constant
          if (code == Postfix.OTHER) {
            // convert constant to Real object with "CONSTANT" RealType
            stack.push(new Real(CONSTANT, d.doubleValue()));
          }
          else {
            // constant is a function counter
            stack.push(new Real(d.doubleValue()));
          }
        }
      }
    }

    // return the final answer
    Thing answer = popStack(stack);
    if (!stack.empty()) {
      evalError("Syntax error in formula (d)");
    }
    // return answer in local form
    if (answer instanceof Data) {
      try {
        answer = ((Data) answer).local();
        // remove "constant" tag if final answer is a constant
        if (answer instanceof Real && ((Real) answer).getType() == CONSTANT) {
          answer = new Real(((Real) answer).getValue());
        }
      }
      catch (VisADException exc) {
        evalError("The answer could not be converted to local data.");
        answer = null;
      }
      catch (RemoteException exc) {
        evalError("The answer could not be converted to local data.");
        answer = null;
      }
    }
    return answer;
  }

}

