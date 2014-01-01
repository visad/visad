//
// FormulaVar.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.*;
import visad.*;

/** Represents a variable.<P> */
public class FormulaVar extends ActionImpl {

  /** for testing purposes */
  public static boolean DEBUG = false;

  /** constant tag */
  public static RealType CONSTANT =
    RealType.getRealType("visad/formula/constant");

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

  /** for synchronization */
  private Object Lock = new Object();

  /** whether the formula is currently being computed */
  private boolean computing = false;

  /** constructor without specified ThingReference */
  FormulaVar(String n, FormulaManager f) throws VisADException {
    this(n, f, null);
  }

  /** constructor with specified ThingReference */
  FormulaVar(String n, FormulaManager f, ThingReference t)
    throws VisADException
  {
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

  /** vector of variables that require this variable (depend on it) */
  private Vector required = new Vector();

  /** return whether any other variables depend on this one */
  boolean othersDepend() {
    return !required.isEmpty();
  }

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
  private void setDependentOn(FormulaVar v) {
    if (!depend.contains(v)) {
      depend.add(v);
      try {
        addReference(v.getReference());
        v.required.add(this);
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
      }
    }
  }

  /** clear this variable's dependency list */
  private void clearDependencies() {
    int len = depend.size();
    for (int i=0; i<len; i++) {
      FormulaVar v = (FormulaVar) depend.elementAt(i);
      v.required.remove(this);
    }
    depend.removeAllElements();
    try {
      removeAllReferences();
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  /** rebuild this variable's dependency list, then recompute this variable */
  private void rebuildDependencies() throws FormulaException {
    disableAction();
    synchronized (Lock) {
      clearDependencies();
      if (formula != null) {
        if (postfix == null) {
          try {
            // compute postfix expression
            Object[] o = new Object[2];
            o[0] = formula;
            o[1] = fm;
            String pf = formula;
            try {
              // run formula through user's pre-processing function
              pf = (String) FormulaUtil.invokeMethod(fm.ppMethod, o);
            }
            catch (IllegalAccessException exc) {
              if (DEBUG) exc.printStackTrace();
              evalError("Preparsing access exception", exc);
            }
            catch (IllegalArgumentException exc) {
              if (DEBUG) exc.printStackTrace();
              evalError("Preparsing argument exception", exc);
            }
            catch (InvocationTargetException exc) {
              Throwable t = exc.getTargetException();
              if (DEBUG) t.printStackTrace();
              evalError("Preparsing exception", t);
            }
            postfix = new Postfix(pf, fm);
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
                    evalError("Internal VisAD error", exc);
                  }
                  if (v != null) {
                    if (v.isDependentOn(this)) {
                      clearDependencies();
                      throw new FormulaException(
                        "This formula creates an infinite loop");
                    }
                    setDependentOn(v);
                  }
                }
              }
            }
          }
          catch (FormulaException exc) {
            evalError("Syntax error in formula", exc);
            try {
              tref.setThing(null);
            }
            catch (VisADException exc2) {
              evalError("Internal VisAD error", exc2);
            }
            catch (RemoteException exc2) {
              evalError("Internal remote error", exc2);
            }
          }
        }
      }
    }
    enableAction();
    if (depend.isEmpty()) {
      // formula has no dependencies; trigger doAction manually
      doAction();
    }
  }

  /** set the formula for this variable */
  void setFormula(String f) throws FormulaException {
    formula = f;
    if (textRef != null) {
      Text text = new Text(formula);
      try {
        textRef.setThing(text);
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
      }
    }
    postfix = null;
    computing = true;
    rebuildDependencies();
  }

  /** waits for this formula to recompute */
  void waitForFormula() {
    synchronized (Lock) {
      if (computing) {
        try {
          Lock.wait();
        }
        catch (InterruptedException exc) { }
      }
    }
  }

  /** reference to a Text object equal to this variable's formula */
  ThingReference textRef = null;

  /** cell for recomputing formula from referenced Text object */
  CellImpl textCell = new CellImpl() {
    public void doAction() {
      boolean textChanged = false;
      if (textRef != null) {
        try {
          Thing thing = textRef.getThing();
          if (thing instanceof Text) {
            Text t = (Text) thing;
            String newForm = t.getValue();
            if (newForm == null) newForm = "";
            if (!newForm.equals(formula)) {
              textChanged = true;
              setFormula(newForm);
            }
          }
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
    }
  };

  RemoteCellImpl rtCell = null;

  /** set the formula to be dependent on a Text object referenced by tr */
  void setTextRef(ThingReference tr) throws VisADException, RemoteException {
    if (textRef == tr) return;
    if (textRef != null) {
      if (textRef instanceof RemoteDataReference) {
        rtCell.removeReference(textRef);
        rtCell = null;
      }
      else textCell.removeReference(textRef);
    }
    textRef = tr;
    if (textRef != null) {
      if (textRef instanceof RemoteDataReference) {
        rtCell = new RemoteCellImpl(textCell);
        rtCell.addReference(textRef);
      }
      else textCell.addReference(textRef);
    }
  }

  /** set the Thing for this variable directly */
  void setThing(Thing t) throws VisADException, RemoteException {
    synchronized (Lock) {
      formula = null;
      postfix = null;
      clearDependencies();
      if (t == null || t != tref.getThing()) tref.setThing(t);
    }
  }

  /** set the ThingReference for this variable */
  void setReference(ThingReference tr) {
    if (tref == tr) return;
    synchronized (Lock) {
      int len = required.size();
      for (int i=0; i<len; i++) {
        FormulaVar v = (FormulaVar) required.elementAt(i);
        try {
          v.removeReference(tref);
          v.addReference(tr);
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
      tref = tr;
    }
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

  /** add an error to the list of errors that have occurred during formula
      evaluation, appending the given exception's message if any */
  private void evalError(String s, Throwable t) {
    String msg = (t == null ? null : t.getMessage());
    evalError(s + (msg == null ? "" : ": " + msg));
  }

  /** recompute this variable */
  public void doAction() {
    synchronized (Lock) {
      try {
        if (postfix != null) tref.setThing(compute(postfix));
      }
      catch (VisADException exc) {
        evalError("Could not store final value in variable");
      }
      catch (RemoteException exc) {
        evalError("Could not store final value in variable (remote)");
      }
      computing = false;
      Lock.notifyAll();
    }
  }

  /** used by compute method for convenience */
  private Thing popStack(Stack s) {
    if (s.empty()) {
      evalError("Syntax error in formula (stack empty)");
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
      String op = "\"" + token + "\"";
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
                ans = (Thing) FormulaUtil.invokeMethod(fm.bMethods[j], o);
              }
              catch (IllegalAccessException exc) {
                if (DEBUG) exc.printStackTrace();
                evalError(
                  "Cannot access binary method for operator " + op, exc);
              } // no access
              catch (IllegalArgumentException exc) {
                if (DEBUG) exc.printStackTrace();
                evalError(
                  "Invalid argument to binary method for operator " + op, exc);
              } // wrong type of method
              catch (InvocationTargetException exc) {
                Throwable t = exc.getTargetException();
                if (DEBUG) t.printStackTrace();
                evalError("Binary method for operator " + op +
                  " threw an exception", t);
              } // method threw exception
            }
          }
        }
        if (ans == null) {
          evalError("Could not evaluate binary operator " + op);
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
                ans = (Thing) FormulaUtil.invokeMethod(fm.uMethods[j], o);
              }
              catch (IllegalAccessException exc) {
                if (DEBUG) exc.printStackTrace();
                evalError(
                  "Cannot access unary method for operator " + op, exc);
              } // no access
              catch (IllegalArgumentException exc) {
                if (DEBUG) exc.printStackTrace();
                evalError(
                  "Invalid argument to unary method for operator " + op, exc);
              } // wrong type of method
              catch (InvocationTargetException exc) {
                Throwable t = exc.getTargetException();
                if (DEBUG) t.printStackTrace();
                evalError("Unary method for operator " + op +
                  " threw an exception", t);
              } // method threw exception
            }
          }
        }
        if (ans == null) {
          evalError("Could not evaluate unary operator " + op);
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
          catch (ClassCastException exc) {
            if (DEBUG) exc.printStackTrace();
          }
          if (num < 0) {
            evalError("Syntax error in formula (invalid function arg length)");
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
                  ans = (Thing) FormulaUtil.invokeMethod(fm.fMethods[j], o);
                }
                catch (IllegalAccessException exc) {
                  if (DEBUG) exc.printStackTrace();
                  evalError("Cannot access method for function " + op, exc);
                } // no access
                catch (IllegalArgumentException exc) {
                  if (DEBUG) exc.printStackTrace();
                  evalError(
                    "Invalid argument to method for function " + op, exc);
                } // wrong type of method
                catch (InvocationTargetException exc) {
                  Throwable t = exc.getTargetException();
                  if (DEBUG) t.printStackTrace();
                  evalError(
                    "Method for function " + op + " threw an exception", t);
                } // method threw exception
              }
            }
          }
          if (ans == null) {
            evalError("Could not evaluate function " + op);
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
          catch (NumberFormatException exc) {
            if (DEBUG) exc.printStackTrace();
          }
          if (num <= 0) {
            evalError("Syntax error in formula (invalid implicit arg length)");
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
                  ans = (Thing) FormulaUtil.invokeMethod(fm.iMethods[j], o);
                }
                catch (IllegalAccessException exc) {
                  if (DEBUG) exc.printStackTrace();
                  evalError("Cannot access method for implicit function", exc);
                } // no access
                catch (IllegalArgumentException exc) {
                  if (DEBUG) exc.printStackTrace();
                  evalError(
                    "Invalid argument to method for implicit function", exc);
                } // wrong type of method
                catch (InvocationTargetException exc) {
                  Throwable t = exc.getTargetException();
                  if (DEBUG) t.printStackTrace();
                  evalError(
                    "Method for implicit function threw an exception", t);
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
            evalError(op + " is an illegal variable name");
            stack.push(null);
          }
          catch (VisADException exc) {
            evalError("Internal error", exc);
            stack.push(null);
          }
          if (v != null) {
            ThingReference r = v.getReference();
            Thing t = null;
            if (r != null) {
              try {
                t = r.getThing();
              }
              catch (VisADException exc) {
                if (DEBUG) exc.printStackTrace();
              }
              catch (RemoteException exc) {
                if (DEBUG) exc.printStackTrace();
              }
            }
            if (t == null) {
              evalError("Variable " + op + " has no value");
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
      evalError("Syntax error in formula (leftover objects on stack)");
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
        evalError("The answer could not be converted to local data");
        answer = null;
      }
      catch (RemoteException exc) {
        evalError("The answer could not be converted to local data (remote)");
        answer = null;
      }
    }
    return answer;
  }

}

