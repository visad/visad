
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

// AWT packages
import java.awt.*;

// RMI classes
import java.rmi.RemoteException;

// VisAD packages
import visad.*;

/** FormulaCell is a VisAD computational cell that is used by BasicSSCell
    to evaluate formulas. */
class FormulaCell extends CellImpl {

  // formula for this FormulaCell
  String IFormula = "";
  String[] PFormula = null;
  Formula f = new Formula();
  boolean BigX = false;
  boolean Illegal = false;

  // container of this FormulaCell
  BasicSSCell SSCell;

  FormulaCell(BasicSSCell creator, String formula) throws VisADException,
                                                   RemoteException {
    super();
    SSCell = creator;
    IFormula = formula;

    // check parentheses
    int lParen = 0;
    int rParen = 0;
    for (int i=0; i<IFormula.length(); i++) {
      char c = IFormula.charAt(i);
      if (c == '(') lParen++;
      if (c == ')') rParen++;
    }
    if (lParen != rParen) {
      Illegal = true;
      setX(true);
      return;
    }

    // convert expression to postfix
    PFormula = f.toPostfix(formula);
    if (PFormula == null) {
      Illegal = true;
      setX(true);
      return;
    }

    // make sure formula is not dependent on cells that are
    // dependent on this cell (loop detection)
    for (int i=0; i<PFormula.length; i++) {
      String token = PFormula[i];
      if (f.getTokenType(token) == Formula.VARIABLE_TOKEN) {
        BasicSSCell panel = BasicSSCell.getSSCellByName(token);
        if (panel != null && panel.isDependentOn(SSCell.Name)) {
          Illegal = true;
          setX(true);
          return;
        }
      }
    }

    // add a reference for every variable
    for (int i=0; i<PFormula.length; i++) {
      String token = PFormula[i];
      if (f.getTokenType(token) == Formula.VARIABLE_TOKEN) {
        Double d = null;
        try {
          d = Double.valueOf(token);
        }
        catch (NumberFormatException exc) { }

        if (d == null) {
          // token is the name of an BasicSSCell
          BasicSSCell panel = BasicSSCell.getSSCellByName(token);
          if (panel != null) {
            DataReferenceImpl dataRef = panel.getDataRef();
            if (dataRef != null && dataRef.getData() != null) {
              try {
                addReference(dataRef);
              }
              // addReference throws a TypeException if the reference was
              // added previously; i.e., no need to add a reference twice.
              catch (TypeException exc) { }
            }
            else {
              Illegal = true;
              setX(true);
              return;
            }
          }
          else {
            Illegal = true;
            setX(true);
            return;
          }
        }
      }
    }
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
          int type = f.getTokenType(token);
    
          if (type == Formula.OPERATOR_TOKEN || type == Formula.BINARY_TOKEN) {
            Data val2 = stack[--sp];
            Data val1 = stack[--sp];
            value = null;
            if (token.equals("+")) value = val1.add(val2);
            else if (token.equals("-")) value = val1.subtract(val2);
            else if (token.equals("*")) value = val1.multiply(val2);
            else if (token.equals("/")) value = val1.divide(val2);
            else if (token.equals("^")) value = val1.pow(val2);
            else if (token.equalsIgnoreCase("max")) value = val1.max(val2);
            else if (token.equalsIgnoreCase("min")) value = val1.min(val2);
            else if (token.equalsIgnoreCase("atan2")) value = val1.atan2(val2);
            else if (token.equalsIgnoreCase("atan2Degrees")) {
              value = val1.atan2Degrees(val2);
            }
            else if (token.equals("%")) value = val1.remainder(val2);
            if (value == null) {
              System.out.println("BINARY OPERATION FAILED ON TOKEN: "+token); /* CTR: TEMP */
              throw new VisADException(token);
            }
            stack[sp++] = value;
          }
          else if (type == Formula.UNARY_TOKEN) {
            Data val = stack[--sp];
            value = null;
            if (token.equalsIgnoreCase("abs")) value = val.abs();
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
              System.out.println("UNARY OPERATION FAILED ON TOKEN: "+token); /* CTR: TEMP */
              throw new VisADException(token);
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
              value = new Real(d.doubleValue());
            }
            else {
              // token is the name of an BasicSSCell
              BasicSSCell panel = BasicSSCell.getSSCellByName(token);
              if (panel == null) {
                System.out.println("COULDN'T FIND TOKEN: "+token); /* CTR: TEMP */
                throw new VisADException(token);
              }
              DataReferenceImpl dataRef = panel.getDataRef();
              if (dataRef == null) {
                System.out.println("CELL "+token+" HAS NO DATA REFERENCE"); /* CTR: TEMP */
                throw new VisADException(token);
              }
              value = dataRef.getData();
              if (value == null) {
                System.out.println("DATA REFERENCE FOR CELL "+token+" POINTS TO NULL DATA"); /* CTR: TEMP */
                throw new VisADException(token);
              }
            }
            stack[sp++] = value;
          }
        }
      }
      catch (VisADException exc) {
        value = null;
        System.out.println("VisADException: "+exc.toString()); /* CTR: TEMP */
      }
      catch (RemoteException exc) {
        value = null;
        System.out.println("RemoteException: "+exc.toString()); /* CTR: TEMP */
      }
    }

    if (value == null) {
      try {
        SSCell.clearData();
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
      setX(true);
    }
    else {
      // get final value from stack
      value = stack[--sp];

      // if stack is not empty, something went wrong
      if (sp != 0) {
        value = null;
        setX(true);
      }
      if (sp != 0) System.out.println("WARNING: STACK NOT EMPTY"); /* CTR: TEMP */

      try {
        // update SSCell's Data
        SSCell.setData(value);
      }
      catch (VisADException exc) {
        System.out.println("VisADException: "+exc.toString()); /* CTR: TEMP */
      }
      catch (RemoteException exc) { }
    }
  }

  final Canvas BigXCanvas = new Canvas() {
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
  }

}

