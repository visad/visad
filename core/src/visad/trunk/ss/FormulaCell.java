
//
// FormulaCell.java
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

package visad.ss;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.StringTokenizer;
import javax.swing.*;
import visad.*;
import visad.formula.*;

/** Used by BasicSSCell to evaluate formulas.<P> */
public class FormulaCell implements FormulaListener {

  /** FormulaManager object used by all FormulaCell objects */
  static final FormulaManager fm = createManager();

  /** creates the global FormulaManager object */
  private static FormulaManager createManager() {
    // create FormulaManager object
    String[] binOps = {".", "^", "*", "/", "%", "+", "-"};
    int[] binPrec =   {200, 400, 600, 600, 600, 800, 800};
    String[] binMethods = {"visad.ss.FormulaCell.dot(visad.Tuple, visad.Real)",
                           "visad.Data.pow(visad.Data)",
                           "visad.Data.multiply(visad.Data)",
                           "visad.Data.divide(visad.Data)",
                           "visad.Data.remainder(visad.Data)",
                           "visad.Data.add(visad.Data)",
                           "visad.Data.subtract(visad.Data)"};
    String[] unaryOps = {"-"};
    int[] unaryPrec =   {500};
    String[] unaryMethods = {"visad.Data.negate()"};
    String[] functions = {"abs", "acos", "acosDegrees", "asin", "asinDegrees",
                          "atan", "atan2", "atanDegrees", "atan2Degrees",
                          "ceil", "combine", "cos", "cosDegrees", "derive",
                          "exp", "extract", "floor", "link", "log", "max",
                          "min", "negate", "rint", "round", "sin",
                          "sinDegrees", "sqrt", "tan", "tanDegrees"};
    String[] funcMethods = {"visad.Data.abs()", "visad.Data.acos()",
                            "visad.Data.acosDegrees()", "visad.Data.asin()",
                            "visad.Data.asinDegrees()", "visad.Data.atan()",
                            "visad.Data.atan2(visad.Data)",
                            "visad.Data.atanDegrees()",
                            "visad.Data.atan2Degrees(visad.Data)",
                            "visad.Data.ceil()",
                            "visad.FieldImpl.combine(visad.Field[])",
                            "visad.Data.cos()", "visad.Data.cosDegrees()",
                            "visad.ss.FormulaCell.derive(visad.Function," +
                                                        "visad.RealType)",
                            "visad.Data.exp()",
                            "visad.ss.FormulaCell.extract(visad.Field," +
                                                         "visad.Real)",
                            "visad.Data.floor()",
                            "visad.ss.FormulaCell.link(" +
                            "java.lang.reflect.Method, java.lang.Object[])",
                            "visad.Data.log()",
                            "visad.Data.max(visad.Data)",
                            "visad.Data.min(visad.Data)",
                            "visad.Data.negate()", "visad.Data.rint()",
                            "visad.Data.round()", "visad.Data.sin()",
                            "visad.Data.sinDegrees()", "visad.Data.sqrt()",
                            "visad.Data.tan()", "visad.Data.tanDegrees()"};
    int implicitPrec = 200;
    String[] implicitMethods = {"visad.ss.FormulaCell.implicit(" +
                                "visad.Function, visad.Real)",
                                "visad.Function.evaluate(visad.RealTuple)"};
    String cMethod = "visad.ss.FormulaCell.constant(java.lang.Double)";
    FormulaManager f = null;
    try {
      f = new FormulaManager(binOps, binPrec, binMethods, unaryOps, unaryPrec,
                             unaryMethods, functions, funcMethods,
                             implicitPrec, implicitMethods, cMethod);
    }
    catch (FormulaException exc) { }
    return f;
  }

  /** type for a constant */
  private static RealType CONSTANT = createConstant();

  /** creates the constant RealType */
  private static RealType createConstant() {
    RealType rt = null;
    try {
      rt = new RealType("visad/ss/FormulaCell/CONSTANT");
    }
    catch (VisADException exc) {
      rt = RealType.getRealTypeByName("visad/ss/FormulaCell/CONSTANT");
    }
    return rt;
  }

  /** this cell's formula */
  String formula;

  /** whether this cell has a big X through it */
  boolean BigX = false;

  /** container of this FormulaCell */
  BasicSSCell SSCell;

  /** whether to pop up formula evaluation errors in dialog boxes */
  boolean ShowErrors;

  /** evaluates the dot operator */
  public static Data dot(Tuple t, Real r) {
    Data d = null;
    try {
      d = t.getComponent((int) r.getValue());
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return d;
  }

  /** evaluates the derive function */
  public static Data derive(Function f, RealType rt) {
    Data val = null;
    try {
      val = f.derivative(rt, Data.NO_ERRORS);
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return val;
  }

  /** evaluates the extract function */
  public static Data extract(Field f, Real r) {
    Data d = null;
    try {
      d = f.extract((int) r.getValue());
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return d;
  }

  /** evaluates the link function */
  public static Data link(Method m, Object[] o) {
    Data ans = null;
    try {
      ans = (Data) FormulaVar.invokeMethod(m, o);
    }
    catch (ClassCastException exc) { }
    catch (IllegalAccessException exc) { }
    catch (IllegalArgumentException exc) { }
    catch (InvocationTargetException exc) { }
    return ans;
  }

  /** evaluates implicit function syntax; e.g., A1(5) */
  public static Data implicit(Function f, Real r) {
    Data value = null;
    try {
      RealType rt = (RealType) r.getType();
      if (rt.equals(FormulaCell.CONSTANT)) {
        value = ((Field) f).getSample((int) r.getValue());
      }
      else value = f.evaluate(r);
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return value;
  }

  /** constant conversion method */
  public static Real constant(Double d) {
    return (d == null) ? null
                       : new Real(FormulaCell.CONSTANT, d.doubleValue());
  }

  /** number of link variables that have been created */
  private static int linkNum = 0;

  /** does some pre-computation parsing to the formula */
  private static String preParse(String f) {
    // convert to lower case and remove spaces
    StringTokenizer t = new StringTokenizer(f, " ", false);
    String s = "";
    while (t.hasMoreTokens()) s = s + t.nextToken();
    if (s.equals("")) return s;
    String l = s.toLowerCase();

    // scan entire string
    int len = l.length();
    boolean letter = false;
    String ns = "";
    for (int i=0; i<len; i++) {
      if (!letter && i < len - 1 && l.substring(i, i+2).equals("d(")) {
        // convert d(x)/d(y) notation to standard derive(x, y) notation
        i += 2;
        int s1 = i;
        for (int paren=1; paren>0; i++) {
          // check for correct syntax
          if (i >= len) return s;
          char c = l.charAt(i);
          if (c == '(') paren++;
          if (c == ')') paren--;
        }
        int e1 = i-1;
        // check for correct syntax
        if (i > len - 3 || !l.substring(i, i+3).equals("/d(")) return s;
        i += 3;
        int s2 = i;
        for (int paren=1; paren>0; i++) {
          // check for correct syntax
          if (i >= len) return s;
          char c = l.charAt(i);
          if (c == '(') paren++;
          if (c == ')') paren--;
        }
        int e2 = i-1;
        ns = ns + "derive(" + s.substring(s1, e1) +
                        "," + s.substring(s2, e2) + ")";
        i--;
      }
      else if (!letter && i < len - 4 && l.substring(i, i+5).equals("link(")) {
        // evaluate link(code) notation and replace with link variable
        i += 5;
        int s1 = i;
        try {
          while (l.charAt(i) != '(') i++;
        }
        catch (ArrayIndexOutOfBoundsException exc) {
          // incorrect syntax
          return s;
        }
        i++;
        int e1 = i-1;
        int s2 = i;
        for (int paren=2; paren>1; i++) {
          // check for correct syntax
          if (i >= len) return s;
          char c = l.charAt(i);
          if (c == '(') paren++;
          if (c == ')') paren--;
        }
        int e2 = i-1;
        // check for correct syntax
        if (i >= len || l.charAt(i) != ')') return s;
        String[] strs = new String[1];
        strs[0] = s.substring(s1, e1) + "(";

        // parse method's arguments; determine if they are Data or RealType
        String sub = s.substring(s2, e2);
        System.out.println("sub = " + sub);
        StringTokenizer st = new StringTokenizer(sub, ",", false);
        boolean first = true;
        while (st.hasMoreTokens()) {
          String token = st.nextToken();
          if (first) first = false;
          else strs[0] = strs[0] + ",";
          RealType rt = RealType.getRealTypeByName(token);
          strs[0] = strs[0] + (rt == null ? "visad.Data" : "visad.RealType");
        }
        strs[0] = strs[0] + ")";

        // obtain Method object and store it in a link variable
        Method[] meths = FormulaManager.stringsToMethods(strs);
        String link = "link" + (++linkNum);
        // make sure linked method actually exists
        if (meths[0] == null) return s;
        try {
          fm.setValue(link, meths[0]);
        }
        catch (FormulaException exc) {
          // error setting the link variable
          return s;
        }
        ns = ns + "link(" + link + "," + s.substring(s2, e2) + ")";
      }
      else {
        // append character to new string
        ns = ns + s.charAt(i);
      }
      char c = (i < len) ? l.charAt(i) : '\0';
      letter = (c >= 'a' && c <= 'z');
    }
    return ns;
  }

  /** constructor */
  FormulaCell(BasicSSCell creator, String f, boolean verbose)
              throws FormulaException {
    super();
    SSCell = creator;
    formula = f;
    ShowErrors = verbose;
    fm.addVarChangeListener(SSCell.Name, this);
    fm.assignFormula(SSCell.Name, preParse(formula));
  }

  /** perform necessary clean-up before ceasing use of this FormulaCell */
  void dispose() {
    setX(false);
    try {
      fm.removeVarChangeListener(SSCell.Name, this);
    }
    catch (FormulaException exc) { }
  }

  /** toggles whether to display errors in pop-up dialog boxes */
  public void setShowErrors(boolean se) {
    ShowErrors = se;
  }

  /** returns this cell's current formula */
  public String getFormula() {
    return formula;
  }

  /** get new value of this cell's variable and redisplay it */
  public void variableChanged() {
    // get current value from FormulaManager object
    Data value = null;
    try {
      value = (Data) fm.getValue(SSCell.Name);
    }
    catch (ClassCastException exc) {
      if (ShowErrors) {
        showMsg("Formula evaluation error", "Final value is not of the " +
                                            "correct type.");
      }
    }
    catch (FormulaException exc) {
      if (ShowErrors) {
        showMsg("Formula evaluation error", "The formula could not be " +
                                            "evaluated:\n" + exc);
      }
    }

    if (value == null) {
      // no value; clear data
      try {
        SSCell.clearData();
      }
      catch (VisADException exc) {
        if (ShowErrors) {
          showMsg("Formula evaluation error", "Unable to clear old data.");
        }
      }
      catch (RemoteException exc) {
        if (ShowErrors) {
          showMsg("Formula evaluation error", "Unable to clear old data.");
        }
      }
      setX(true);
    }
    else {
      // update cell's data
      setX(false);
      try {
        SSCell.setData(value);
      }
      catch (VisADException exc) {
        if (ShowErrors) {
          showMsg("Formula evaluation error", "Unable to display new data.");
        }
      }
      catch (RemoteException exc) {
        if (ShowErrors) {
          showMsg("Formula evaluation error", "Unable to display new data.");
        }
      }
    }
    String[] es = fm.getErrors(SSCell.Name);
    if (ShowErrors && es != null) {
      for (int i=0; i<es.length; i++) {
        showMsg("Formula evaluation error", es[i]);
      }
    }
  }

  /** component which contains the large X */
  final JComponent BigXCanvas = new JComponent() {
    public void paint(Graphics g) {
      Dimension s = getSize();
      g.setColor(Color.white);
      g.drawLine(0, 0, s.width, s.height);
      g.drawLine(s.width, 0, 0, s.height);
    }
  };

  /** turns the large X on or off */
  void setX(boolean value) {
    if (BigX == value) return;
    BigX = value;

    // queue up action in event dispatch thread
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (BigX) SSCell.add(BigXCanvas);
        else SSCell.remove(BigXCanvas);
        SSCell.validate();
        SSCell.repaint();
      }
    });
  }

  /** pop up a message in a dialog box */
  void showMsg(String title, String msg) {
    final String t = title;
    final String m = msg;

    // queue up action in event dispatch thread
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(SSCell, m, t, JOptionPane.ERROR_MESSAGE);
      }
    });
  }

}

