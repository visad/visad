//
// Postfix.java
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

import java.util.*;

/** represents a formula in postfix notation.<P> */
class Postfix {

  /** code for binary operator */
  static final int BINARY = 0;

  /** code for unary operator */
  static final int UNARY = 1;

  /** code for function name */
  static final int FUNC = 2;

  /** code for constant that represents number of function arguments */
  static final int FUNCCONST = 3;

  /** code for variable, constant, or other */
  static final int OTHER = 4;

  /** String representation of an implicit function */
  private static final String IMPLICIT = " ";

  /** postfix tokens */
  String[] tokens = null;

  /** postfix codes representing token types */
  int[] codes = null;

  /** construct a Postfix object by converting infix formula */
  Postfix(String formula, FormulaManager fm) throws FormulaException {
    // convert expression to postfix notation
    String[] postfix = null;
    int[] pfixcode = null;
    String infix;

    // convert string to char array
    char[] charStr = formula.toCharArray();

    // remove spaces and check parentheses
    int numSpaces = 0;
    int paren = 0;
    for (int i=0; i<charStr.length; i++) {
      if (charStr[i] == ' ') numSpaces++;
      if (charStr[i] == '(') paren++;
      if (charStr[i] == ')') paren--;
      if (paren < 0) {
        throw new FormulaException("Unable to convert to postfix notation: " +
                                   "illegal placement of parentheses");
      }
    }
    if (paren != 0) {
      throw new FormulaException("Unable to convert to postfix notation: " +
                                 "parentheses are mismatched!");
    }
    int j = 0;
    int newlen = charStr.length - numSpaces;
    if (newlen == 0) return;
    char[] exp = new char[newlen];
    for (int i=0; i<charStr.length; i++) {
      if (charStr[i] != ' ') exp[j++] = charStr[i];
    }
    infix = new String(exp);

    // tokenize string
    String ops = "(,)";
    for (int i=0; i<fm.uOps.length; i++) ops = ops + fm.uOps[i];
    for (int i=0; i<fm.bOps.length; i++) ops = ops + fm.bOps[i];
    StringTokenizer tokenizer = new StringTokenizer(infix, ops, true);
    int numTokens = tokenizer.countTokens();

    // set up stacks
    String[] funcStack = new String[numTokens];    // function stack
    String[] opStack = new String[numTokens];      // operator stack
    int[] opCodes = new int[numTokens];            // operator code stack
    String[] pfix = new String[numTokens];         // final postfix ordering
    int[] pcode = new int[numTokens];              // final postfix codes
    int opPt = 0;                                  // pointer into opStack
    int funcPt = 0;                                // pointer into funcStack
    int pfixlen = 0;                               // pointer into pfix

    // flag for detecting unary operators
    boolean unary = true;

    // flag for detecting no-argument functions (e.g., x())
    boolean zero = false;

    // flag for detecting floating point numbers
    boolean numeral = false;

    // convert to postfix
    String ntoken = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
    String token = ntoken;
    while (token != null) {
      ntoken = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;

      if (token.equals(")")) {
        // right paren - pop ops until left paren reached (inclusive)
        if (opPt < 1) {
          throw new FormulaException("Unable to convert to postfix " +
                                     "notation: operator stack " +
                                     "unexpectedly empty");
        }
        int opcode = opCodes[--opPt];
        String op = opStack[opPt];
        while (!op.equals("(")) {
          pcode[pfixlen] = opcode;
          pfix[pfixlen++] = "" + op;
          if (opPt < 1) {
            throw new FormulaException("Unable to convert to postfix " +
                                       "notation: operator stack " +
                                       "unexpectedly empty");
          }
          opcode = opCodes[opPt-1];
          op = opStack[--opPt];
        }
        if (opcode == FUNC) {
          if (funcPt < 1) {
            throw new FormulaException("Unable to convert to postfix " +
                                       "notation: function stack " +
                                       "unexpectedly empty");
          }
          String f = funcStack[--funcPt];
          boolean implicit;
          if (zero) {
            implicit = f.equals(IMPLICIT);
            pcode[pfixlen] = implicit ? FUNC : FUNCCONST;
            pfix[pfixlen++] = "0";
          }
          else {
            int n = 1;
            while (f.equals(",")) {
              n++;
              if (funcPt < 1) {
                throw new FormulaException("Unable to convert to postfix " +
                                           "notation: function stack " +
                                           "unexpectedly empty");
              }
              f = funcStack[--funcPt];
            }
            implicit = f.equals(IMPLICIT);
            pcode[pfixlen] = implicit ? FUNC : FUNCCONST;
            pfix[pfixlen++] = "" + n;
          }
          if (!implicit) {
            pcode[pfixlen] = FUNC;
            pfix[pfixlen++] = f;
          }
        }
        unary = false;
        zero = false;
        numeral = false;
      }
      if (token.equals("(")) {
        // left paren - push onto operator stack
        opCodes[opPt] = OTHER;
        opStack[opPt++] = "(";
        unary = true;
        zero = false;
        numeral = false;
      }
      else if (token.equals(",")) {
        // comma - pop ops until left paren reached (exclusive), push comma
        if (opPt < 1) {
          throw new FormulaException("Unable to convert to postfix " +
                                     "notation: operator stack " +
                                     "unexpectedly empty");
        }
        int opcode = opCodes[opPt-1];
        String op = opStack[opPt-1];
        while (!op.equals("(")) {
          pcode[pfixlen] = opcode;
          pfix[pfixlen++] = "" + op;
          opPt--;
          if (opPt < 1) {
            throw new FormulaException("Unable to convert to postfix " +
                                       "notation: operator stack " +
                                       "unexpectedly empty");
          }
          opcode = opCodes[opPt-1];
          op = opStack[opPt-1];
        }
        funcStack[funcPt++] = ",";
        unary = true;
        zero = false;
        numeral = false;
      }
      else if ((unary && fm.isUnaryOp(token)) || fm.isBinaryOp(token)) {
        int num = -1;
        if (numeral && token.equals(".") && ntoken != null) {
          // special case for detecting floating point numbers
          try {
            num = Integer.parseInt(ntoken);
          }
          catch (NumberFormatException exc) { }
        }
        if (num > 0) {
          pfix[pfixlen-1] = pfix[pfixlen-1] + "." + ntoken;
          token = ntoken;
          ntoken = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
          unary = false;
          zero = false;
          numeral = false;
        }
        else {
          // operator - pop ops with higher precedence, push op
          boolean isUnary = (unary && fm.isUnaryOp(token));
          int prec = (isUnary ? fm.getUnaryPrec(token)
                              : fm.getBinaryPrec(token));
          String sop;
          int scode;
          if (opPt < 1) {
            sop = null;
            scode = 0;
          }
          else {
            sop = opStack[opPt-1];
            scode = opCodes[opPt-1];
          }
          while (sop != null &&
                 prec >= (scode == UNARY ? fm.getUnaryPrec(sop)
                                         : fm.getBinaryPrec(sop))) {
            opPt--;
            pcode[pfixlen] = scode;
            pfix[pfixlen++] = "" + sop;
            if (opPt < 1) {
              sop = null;
              scode = 0;
            }
            else {
              sop = opStack[opPt-1];
              scode = opCodes[opPt-1];
            }
          }
          opCodes[opPt] = (isUnary ? UNARY : BINARY);
          opStack[opPt++] = token;
          unary = true;
          zero = false;
          numeral = false;
        }
      }
      else if (ntoken != null && ntoken.equals("(")) {
        // function - push function name and left paren
        if (fm.isFunction(token)) funcStack[funcPt++] = token;
        else {
          // implicit function - append token to postfix expression
          funcStack[funcPt++] = IMPLICIT;
          if (!token.equals(")")) {
            pcode[pfixlen] = OTHER;
            pfix[pfixlen++] = token;
          }
          // pop ops with higher precedence
          String sop;
          int scode;
          if (opPt < 1) {
            sop = null;
            scode = 0;
          }
          else {
            sop = opStack[opPt-1];
            scode = opCodes[opPt-1];
          }
          while (sop != null &&
                 fm.iPrec >= (scode == UNARY ? fm.getUnaryPrec(sop)
                                             : fm.getBinaryPrec(sop))) {
            opPt--;
            pcode[pfixlen] = scode;
            pfix[pfixlen++] = "" + sop;
            if (opPt < 1) {
              sop = null;
              scode = 0;
            }
            else {
              sop = opStack[opPt-1];
              scode = opCodes[opPt-1];
            }
          }
        }
        opCodes[opPt] = FUNC;
        opStack[opPt++] = "(";
        token = ntoken;
        ntoken = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
        unary = true;
        zero = true;
        numeral = false;
      }
      else if (!token.equals(")")) {
        // variable - append token to postfix expression
        pcode[pfixlen] = OTHER;
        pfix[pfixlen++] = token;
        unary = false;
        zero = false;
        try {
          int num = Integer.parseInt(token);
          numeral = true;
        }
        catch (NumberFormatException exc) {
          numeral = false;
        }
      }

      token = ntoken;
    }
    // pop remaining ops from stack
    while (opPt > 0) {
      pcode[pfixlen] = opCodes[opPt-1];
      pfix[pfixlen++] = "" + opStack[--opPt];
    }

    // make sure stacks are empty
    if (opPt != 0 || funcPt != 0) {
      throw new FormulaException("Unable to convert to postfix notation: " +
                                 "stacks are not empty");
    }

    // return postfix array of tokens
    tokens = new String[pfixlen];
    codes = new int[pfixlen];
    System.arraycopy(pfix, 0, tokens, 0, pfixlen);
    System.arraycopy(pcode, 0, codes, 0, pfixlen);
  }

}

