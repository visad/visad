
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

// JFC classes
import com.sun.java.swing.JComponent;
import com.sun.java.swing.JOptionPane;

// AWT packages
import java.awt.*;

// RMI classes
import java.rmi.RemoteException;

// VisAD packages
import visad.*;

/** FormulaCell is a VisAD computational cell that is used by BasicSSCell
    to evaluate formulas.<P> */
class FormulaCell extends CellImpl {

  // formula for this FormulaCell
  String IFormula = "";
  String[] PFormula = null;
  boolean BigX = false;
  boolean Illegal = false;

  // container of this FormulaCell
  BasicSSCell SSCell;
  boolean ShowErrors;

  private static final RealType CONSTANT = createConstant();

  private static RealType createConstant() {
    RealType r;
    try {
      r = new RealType("VISAD_CONSTANT");
    }
    catch (VisADException exc) {
      r = RealType.getRealTypeByName("VISAD_CONSTANT");
    }
    return r;
  }

  FormulaCell(BasicSSCell creator, String formula, boolean verbose)
              throws VisADException, RemoteException {
    super();
    SSCell = creator;
    IFormula = formula;
    ShowErrors = verbose;

    // convert expression to postfix
    PFormula = Formula.toPostfix(formula);
    if (PFormula == null) {
      Illegal = true;
      setX(true);
      if (ShowErrors) {
        JOptionPane.showMessageDialog(SSCell,
          "This formula's syntax is incorrect.  Make sure that " +
          "parentheses are balanced, and that function and derivative " +
          "syntax is correct.", "Error evaluating formula",
          JOptionPane.ERROR_MESSAGE);
      }
      return;
    }

    // make sure formula is not dependent on cells that are
    // dependent on this cell (loop detection)
    for (int i=0; i<PFormula.length; i++) {
      String token = PFormula[i];
      if (Formula.getTokenType(token) == Formula.VARIABLE_TOKEN) {
        BasicSSCell panel = BasicSSCell.getSSCellByName(token);
        if (panel != null && panel.isDependentOn(SSCell.Name)) {
          Illegal = true;
          setX(true);
          if (ShowErrors) {
            JOptionPane.showMessageDialog(SSCell,
              "This formula depends on itself, creating an infinite loop, " +
              "and thus cannot be evaluated.", "Error evaluating formula",
              JOptionPane.ERROR_MESSAGE);
          }
          return;
        }
      }
    }

    // add a reference for every variable
    for (int i=0; i<PFormula.length; i++) {
      String token = PFormula[i];
      if (Formula.getTokenType(token) == Formula.VARIABLE_TOKEN) {
        Double d = null;
        try {
          d = Double.valueOf(token);
        }
        catch (NumberFormatException exc) { }

        if (d == null && token.charAt(0) != '~') {
          // token is the name of a BasicSSCell
          BasicSSCell panel = BasicSSCell.getSSCellByName(token);
          if (panel != null) {
            DataReferenceImpl dataRef = panel.getDataRef();
            try {
              addReference(dataRef);
            }
            catch (TypeException exc) { }
          }
          else {
            Illegal = true;
            setX(true);
            if (ShowErrors) {
              JOptionPane.showMessageDialog(SSCell,
                "The cell \"" + token + "\" does not exist.",
                "Error evaluating formula", JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }
    }
  }

  public void setShowErrors(boolean se) {
    ShowErrors = se;
  }

  public String getFormula() {
    return IFormula;
  }

  public void doAction() {
    Data value = null;
    Data[] stack = new Data[PFormula.length];
    int sp = 0;
    if (!Illegal) {
      setX(false);
      try {
        for (int i=0; i<PFormula.length; i++) {
          String token = PFormula[i];
          int type = Formula.getTokenType(token);
    
          if (type == Formula.OPERATOR_TOKEN || type == Formula.BINARY_TOKEN) {
            if (sp < 2) throw new VisADException(
              "Error evaluating binary operation \"" + token + "\".  " +
              "There are not enough Data objects on the stack.");
            Data val2 = stack[--sp];
            Data val1 = stack[--sp];
            value = null;
            if (token.equals("+")) value = val1.add(val2);
            else if (token.equals("-")) value = val1.subtract(val2);
            else if (token.equals("*")) value = val1.multiply(val2);
            else if (token.equals("/")) value = val1.divide(val2);
            else if (token.equals("^")) value = val1.pow(val2);
            else if (token.equals(".")) {
              int v = (int) (((Real) val2).getValue());
              value = ((Tuple) val1).getComponent(v);
            }
            else if (token.equals("@")) {
              // evaluate syntax:  val1(val2)
              if (val2 instanceof Real) {
                RealType rt = (RealType) val2.getType();
                if (rt.equals(FormulaCell.CONSTANT)) {
                  int v = (int) (((Real) val2).getValue());
                  value = ((Field) val1).getSample(v);
                }
                else {
                  value = ((Function) val1).evaluate((Real) val2);
                }
              }
              else value = ((Function) val1).evaluate((RealTuple) val2);
            }
            else if (token.equalsIgnoreCase("d")) {
              value = ((Function) val1).derivative((RealType) val2.getType(),
                                                   Data.NO_ERRORS);
            }
            else if (token.equalsIgnoreCase("max")) value = val1.max(val2);
            else if (token.equalsIgnoreCase("min")) value = val1.min(val2);
            else if (token.equalsIgnoreCase("atan2")) value = val1.atan2(val2);
            else if (token.equalsIgnoreCase("atan2Degrees")) {
              value = val1.atan2Degrees(val2);
            }
            else if (token.equals("%")) value = val1.remainder(val2);
            if (value == null) {
              throw new VisADException(
                "Could not perform \"" + token + "\" binary operation.");
            }
            stack[sp++] = value;
          }
          else if (type == Formula.UNARY_TOKEN) {
            if (sp < 1) throw new VisADException(
              "Error evaluating unary operation \"" +
              (token.equals("&") ? "-" : token) + "\".  " +
              "There are not enough Data objects on the stack.");
            Data val = stack[--sp];
            value = null;
            if (token.equals("&")) value = val.negate();
            else if (token.equalsIgnoreCase("abs")) value = val.abs();
            else if (token.equalsIgnoreCase("acos")) value = val.acos();
            else if (token.equalsIgnoreCase("acosDegrees")) {
              value = val.acosDegrees();
            }
            else if (token.equalsIgnoreCase("asin")) value = val.asin();
            else if (token.equalsIgnoreCase("asinDegrees")) {
              value = val.asinDegrees();
            }
            else if (token.equalsIgnoreCase("atan")) value = val.atan();
            else if (token.equalsIgnoreCase("atanDegrees")) {
              value = val.atanDegrees();
            }
            else if (token.equalsIgnoreCase("ceil")) value = val.ceil();
            else if (token.equalsIgnoreCase("cos")) value = val.cos();
            else if (token.equalsIgnoreCase("cosDegrees")) {
              value = val.cosDegrees();
            }
            else if (token.equalsIgnoreCase("exp")) value = val.exp();
            else if (token.equalsIgnoreCase("floor")) value = val.floor();
            else if (token.equalsIgnoreCase("log")) value = val.log();
            else if (token.equalsIgnoreCase("rint")) value = val.rint();
            else if (token.equalsIgnoreCase("round")) value = val.round();
            else if (token.equalsIgnoreCase("sin")) value = val.sin();
            else if (token.equalsIgnoreCase("sinDegrees")) {
              value = val.sinDegrees();
            }
            else if (token.equalsIgnoreCase("sqrt")) value = val.sqrt();
            else if (token.equalsIgnoreCase("tan")) value = val.tan();
            else if (token.equalsIgnoreCase("tanDegrees")) {
              value = val.tanDegrees();
            }
            else if (token.equalsIgnoreCase("negate")) value = val.negate();
            if (value == null) {
              throw new VisADException(
                "Could not perform \"" + token + "\" unary operation.");
            }
            stack[sp++] = value;
          }
          else {  // type == Formula.VARIABLE_TOKEN
            Double d;
            try {
              d = Double.valueOf(token);
            }
            catch (NumberFormatException exc) {
              d = null;
            }
            if (d != null) {
              // token is a constant
              value = new Real(FormulaCell.CONSTANT, d.doubleValue());
            }
            else if (token.charAt(0) == '~') {
              // token is the name of a RealType
              String t = token.substring(1, token.length());
              RealType r = RealType.getRealTypeByName(t);
              if (r == null) throw new VisADException(
                "\"" + t + "\" is not a valid RealType.");
              value = new Real(r);
            }
            else {
              // token is the name of a BasicSSCell
              BasicSSCell panel = BasicSSCell.getSSCellByName(token);
              if (panel == null) throw new VisADException(
                "\"" + token + "\" is not a valid cell name.");
              DataReferenceImpl dataRef = panel.getDataRef();
              if (dataRef == null) throw new VisADException(
                "Cell " + token + " has no data.");
              value = dataRef.getData();
              if (value == null) throw new VisADException(
                "Cell " + token + " has no data.");
            }
            stack[sp++] = value;
          }
        }
      }
      catch (ClassCastException exc) {
        value = null;
        if (ShowErrors) {
          JOptionPane.showMessageDialog(SSCell,
            "One or more data objects are not of the correct type for " +
            "an operation specified in the formula.",
            "Formula evaluation error", JOptionPane.ERROR_MESSAGE);
        }
      }
      catch (VisADException exc) {
        value = null;
        if (ShowErrors) {
          JOptionPane.showMessageDialog(SSCell, exc.toString(),
            "Formula evaluation error", JOptionPane.ERROR_MESSAGE);
        }
      }
      catch (RemoteException exc) {
        value = null;
        if (ShowErrors) {
          JOptionPane.showMessageDialog(SSCell, exc.toString(),
            "Formula evaluation error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }

    if (value == null) {
      try {
        SSCell.clearData();
      }
      catch (VisADException exc) {
        if (ShowErrors) {
          JOptionPane.showMessageDialog(SSCell, "Unable to clear old data.",
            "Formula evaluation error", JOptionPane.ERROR_MESSAGE);
        }
      }
      catch (RemoteException exc) {
        if (ShowErrors) {
          JOptionPane.showMessageDialog(SSCell, "Unable to clear old data.",
            "Formula evaluation error", JOptionPane.ERROR_MESSAGE);
        }
      }
      setX(true);
    }
    else {
      if (sp != 1) {  // something went wrong
        value = null;
        setX(true);
      }
      else value = stack[--sp];  // get final value from stack

      try {
        // update SSCell's Data
        SSCell.setData(value);
      }
      catch (VisADException exc) {
        if (ShowErrors) {
          JOptionPane.showMessageDialog(SSCell, "Unable to display new data.",
            "Formula evaluation error", JOptionPane.ERROR_MESSAGE);
        }
      }
      catch (RemoteException exc) {
        if (ShowErrors) {
          JOptionPane.showMessageDialog(SSCell, "Unable to display new data.",
            "Formula evaluation error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  final JComponent BigXCanvas = new JComponent() {
    public void paint(Graphics g) {
      Dimension s = getSize();
      g.setColor(Color.white);
      g.drawLine(0, 0, s.width, s.height);
      g.drawLine(s.width, 0, 0, s.height);
    }
  };

  void setX(boolean value) {
    if (BigX == value) return;
    BigX = value;
    if (BigX) SSCell.add(BigXCanvas);
    else SSCell.remove(BigXCanvas);
    SSCell.validate();
    SSCell.paint(SSCell.getGraphics());
  }

}

