
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

import java.lang.reflect.*;
import java.util.*;

/** represents a variable */
class FormulaVar {
  /** associated FormulaManager object */
  private FormulaManager fm;

  /** name of this variable */
  String name;

  /** formula associated with this variable, if any */
  private String formula;

  /** formula in postfix notation, if it has been converted */
  private Postfix postfix;

  /** current value of this variable, if any */
  private Object value;

  /** variables which depend on this one */
  private Vector Tdepend = new Vector();

  /** variables on which this one depends */
  private Vector Idepend = new Vector();

  /** constructor */
  FormulaVar(String n, FormulaManager f) throws FormulaException {
    fm = f;
    name = n;
    if (name.equalsIgnoreCase("link")) {
      throw new FormulaException("\"link\" keyword is reserved");
    }
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

  /** register this variable as depending on another one */
  private void setDependentOn(FormulaVar v) {
    // add variable only if it's not already on the list
    if (v != null && !isDependentOn(v)) {
      v.Tdepend.add(this);
      Idepend.add(v);
    }
  }

  /** clears this variable's dependencies */
  private void clearDependencies() {
    for (int i=0; i<Idepend.size(); i++) {
      FormulaVar v = (FormulaVar) Idepend.elementAt(i);
      v.Tdepend.remove(this);
    }
    Idepend.clear();
  }

  /** check if this variable depends on another one */
  boolean isDependentOn(FormulaVar v) {
    for (int i=0; i<Idepend.size(); i++) {
      if ((FormulaVar) Idepend.elementAt(i) == v) return true;
    }
    return false;
  }

  /** check if any other variables depend on this one */
  boolean isSafeToDelete() {
    return Tdepend.isEmpty();
  }

  /** sets the formula for this variable */
  void setFormula(String f) throws FormulaException {
    formula = f;
    postfix = null;
    value = null;
    clearDependencies();
    recompute();
  }

  /** sets the value for this variable directly */
  void setValue(Object v) {
    value = v;
    formula = null;
    postfix = null;
    clearDependencies();
  }

  /** gets the value for this variable */
  Object getValue() {
    return value;
  }

  /** gets the formula for this variable */
  String getFormula() {
    return formula;
  }

  /** tells this variable to recompute itself */
  void recompute() throws FormulaException {
    // recompute this variable
    if (formula == null) return;
    if (postfix == null) postfix = new Postfix(formula, fm);
    value = compute(postfix);

    // recompute all variables which depend on this one
    for (int i=0; i<Tdepend.size(); i++) {
      FormulaVar v = (FormulaVar) Tdepend.elementAt(i);
      v.recompute();
    }
  }

  /** used by compute method for convenience */
  private static Object popStack(Stack s) throws FormulaException {
    if (s.empty()) {
      throw new FormulaException("formula evaluation error: stack empty!");
    }
    else return s.pop();
  }

  /** attempts to invoke a Method with the given Object arguments */
  private static Object invokeMethod(Method m, Object[] o)
                                     throws IllegalAccessException,
                                            IllegalArgumentException,
                                            InvocationTargetException {
    Object obj;
    Object[] args;
    Class[] c = m.getParameterTypes();
    int num = (o == null) ? 0 : o.length;
    int len = -1;
    int a = -1;
    if (c != null) {
      len = c.length;
      for (int i=0; i<len; i++) {
        if (c[i].isArray()) a = i;
      }
    }
    if (Modifier.isStatic(m.getModifiers())) {
      // static method
      obj = null;
      if (num > 0) {
        if (a < 0) {
          args = new Object[num];
          System.arraycopy(o, 0, args, 0, num);
        }
        else {
          // compress some of the arguments into array form
          args = new Object[len];
          if (a > 0) System.arraycopy(o, 0, args, 0, a);
          Object array = Array.newInstance(c[a].getComponentType(), num-len+1);
          System.arraycopy(o, a, array, 0, num-len+1);
          args[a] = array;
          if (a < len-1) System.arraycopy(o, num-len+a+1, args, a+1, len-a-1);
        }
      }
      else args = null;
    }
    else {
      // object method
      if (num > 0) obj = o[0];
      else obj = null;
      if (num > 1) {
        if (a < 0) {
          args = new Object[num-1];
          System.arraycopy(o, 1, args, 0, num-1);
        }
        else {
          // compress some of the arguments into array form
          args = new Object[len];
          if (a > 0) System.arraycopy(o, 1, args, 0, a);
          Object array = Array.newInstance(c[a].getComponentType(), num-len);
          System.arraycopy(o, a+1, array, 0, num-len);
          args[a+1] = array;
          if (a < len-1) System.arraycopy(o, num-len+a+1, args, a+1, len-a-1);
        }
      }
      else args = null;
    }
    return m.invoke(obj, args);
  }

  /** computes the solution to this variable's postfix formula */
  private Object compute(Postfix formula) throws FormulaException {
    if (formula.tokens == null) return null;
    int len = formula.tokens.length;
    Stack stack = new Stack();
    for (int i=0; i<len; i++) {
      String token = formula.tokens[i];
      int code = formula.codes[i];
      if (token.equals("link")) {
        // special case - built-in link function
        token = formula.tokens[++i];
        // CTR: DO THIS: link function
        /* CTR: TEMP */ return null;
      }
      else if (code == Postfix.BINARY) {
        // CTR: DO THIS: overloaded operator support is VERY flaky, check it
        Object[] o = new Object[2];
        o[1] = popStack(stack);
        o[0] = popStack(stack);
        Object ans = null;
        for (int j=0; j<fm.bMethods.length; j++) {
          if (ans == null && fm.bOps[j].equals(token)) {
            try {
              ans = invokeMethod(fm.bMethods[j], o);
            }
            catch (IllegalAccessException exc) {
              /* CTR: TEMP */ System.out.println(exc);
            } // no access
            catch (IllegalArgumentException exc) {
              /* CTR: TEMP */ System.out.println(exc);
            } // wrong type of method
            catch (InvocationTargetException exc) {
              /* CTR: TEMP */ System.out.println(exc);
            } // method threw exception
          }
        }
        if (ans == null) {
          throw new FormulaException("formula evaluation error: " +
                                     "could not evaluate binary " +
                                     "operator \"" + token + "\"");
        }
        else stack.push(ans);
      }
      else if (code == Postfix.UNARY) {
        // CTR: DO THIS: overloaded operator support is VERY flaky, check it
        Object[] o = new Object[1];
        o[0] = popStack(stack);
        Object ans = null;
        for (int j=0; j<fm.uMethods.length; j++) {
          if (ans == null && fm.uOps[j].equals(token)) {
            try {
              ans = invokeMethod(fm.uMethods[j], o);
            }
            catch (IllegalAccessException exc) {
              /* CTR: TEMP */ System.out.println(exc);
            } // no access
            catch (IllegalArgumentException exc) {
              /* CTR: TEMP */ System.out.println(exc);
            } // wrong type of method
            catch (InvocationTargetException exc) {
              /* CTR: TEMP */ System.out.println(exc);
            } // method threw exception
          }
        }
        if (ans == null) {
          throw new FormulaException("formula evaluation error: " +
                                     "could not evaluate unary " +
                                     "operator \"" + token + "\"");
        }
        else stack.push(ans);
      }
      else if (code == Postfix.FUNC) {
        // CTR: DO THIS: overloaded function support is VERY flaky, check it
        Object ans = null;
        if (fm.isFunction(token)) {
          // defined function - token is the function name
          int num = -1;
          try {
            Double d = (Double) popStack(stack);
            num = d.intValue();
          }
          catch (ClassCastException exc) { }
          if (num < 0) {
            throw new FormulaException("formula evaluation error: non-" +
                                       "negative number expected on stack");
          }
          Object[] o;
          if (num > 0) o = new Object[num];
          else o = null;
          for (int j=0; j<num; j++) o[j] = popStack(stack);
          for (int j=0; j<fm.funcs.length; j++) {
            if (ans == null && fm.funcs[j].equals(token)) {
              try {
                ans = invokeMethod(fm.fMethods[j], o);
              }
              catch (IllegalAccessException exc) {
                /* CTR: TEMP */ System.out.println(exc);
              } // no access
              catch (IllegalArgumentException exc) {
                /* CTR: TEMP */ System.out.println(exc);
              } // wrong type of method
              catch (InvocationTargetException exc) {
                /* CTR: TEMP */ System.out.println(exc);
              } // method threw exception
            }
          }
          if (ans == null) {
            throw new FormulaException("formula evaluation error: " +
                                       "could not evaluate function " +
                                       "\"" + token + "\"");
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
            throw new FormulaException("formula evaluation error: non-" +
                                       "negative integer expected on stack");
          }
          Object[] o = new Object[num];
          for (int j=0; j<num; j++) o[j] = popStack(stack);
          for (int j=0; j<fm.iMethods.length; j++) {
            if (ans == null) {
              try {
                ans = invokeMethod(fm.iMethods[j], o);
              }
              catch (IllegalAccessException exc) {
                /* CTR: TEMP */ System.out.println(exc);
              } // no access
              catch (IllegalArgumentException exc) {
                /* CTR: TEMP */ System.out.println(exc);
              } // wrong type of method
              catch (InvocationTargetException exc) {
                /* CTR: TEMP */ System.out.println(exc);
              } // method threw exception
            }
          }
          if (ans == null) {
            throw new FormulaException("formula evaluation error: could " +
                                       "not evaluate implicit function"); 
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
          FormulaVar v = fm.getVarByName(token);
          Object o = v.getValue();
          if (o == null) {
            throw new FormulaException("formula evaluation error: " +
                                       "variable \"" + token + "\" " +
                                       "has no value!");
          }
          else {
            stack.push(o);
            if (v.isDependentOn(this)) {
              throw new FormulaException("formula evaluation error: " +
                                         "infinite loop detected!");
            }
            setDependentOn(v);
          }
        }
        else stack.push(d);
      }
    }

    // return the final answer
    Object answer = popStack(stack);
    if (!stack.empty()) {
      throw new FormulaException("formula evaluation error: " +
                                 "stack should be empty!");
    }
    return answer;
  }

}

