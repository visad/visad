
//
// Formula.java
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

// utility classes
import java.util.StringTokenizer;

/** The Formula class is used to analyze mathematical formulas,
    including converting them to postfix notation for easy
    evaluation using a stack.<P> */
public class Formula {

  public static final int OPERATOR_TOKEN = 1;
  public static final int BINARY_TOKEN = 2;
  public static final int UNARY_TOKEN = 3;
  public static final int VARIABLE_TOKEN = 4;

  /** An array of one-character operators.
      You can add to the list with the addOperators method. */
  String[] Operators       = {"^","*","/","%","+","-",",","(","$","#"};

  /** An array of operator precedences, matching the Operators array. */
  int[] OperatorPrecedence = {20, 40, 40, 40, 60, 60, 96, 97, 98, 99};

  /** These binary functions have precedence level 0, as a convention.
      You can add to the list with the addFunctions method. */
  String[] BinaryFunctions = {"max", "min", "atan2", "atan2Degrees"};

  /** These unary functions have precendence level 1, as a convention.
      You can add to the list with the addFunctions method. */
  String[] UnaryFunctions = {"abs", "acos", "acosDegrees", "asin",
                             "asinDegrees", "atan", "atanDegrees", "ceil",
                             "cos", "cosDegrees", "exp", "floor", "log",
                             "rint", "round", "sin", "sinDegrees", "sqrt",
                             "tan", "tanDegrees", "negate"};

  /** After constructing a Formula object, you can change the values of
      the Operators, OperatorPrecedence, BinaryFunctions, and UnaryFunctions
      variables (or use the defaults).  Then call the postfix method to
      convert an infix expression to a postfix one. */
  public Formula() { }

  /** Adds operators to the default operator list.
      The default operators and their precedences are:
      <dd>^ = 20 (power)
      <dd>* = 40 (multiply)
      <dd>/ = 40 (divide)
      <dd>% = 40 (remainder)
      <dd>+ = 60 (addition)
      <dd>- = 60 (subtraction)
      <dd>, = 96 (reserved)
      <dd>( = 97 (reserved)
      <dd>$ = 98 (reserved)
      <dd># = 99 (reserved)
      <dd>) = N/A (reserved)
      <br>The newPrec array specifies the precedences of the new operators
      in the corresponding newOps array.  Make sure all new operators have
      precedence between 2 and 95, and that all operators are one character
      in length. */
  public void addOperators(String[] newOps, int[] newPrec) {
    int newOpLen = Math.min(newOps.length, newPrec.length);

    String[] oldOps = Operators;
    int[] oldPrec = OperatorPrecedence;
    int oldOpLen = oldOps.length;
    Operators = new String[oldOpLen + newOpLen];
    OperatorPrecedence = new int[oldOpLen + newOpLen];
    System.arraycopy(oldOps, 0, Operators, 0, oldOpLen);
    System.arraycopy(oldPrec, 0, OperatorPrecedence, 0, oldOpLen);
    System.arraycopy(newOps, 0, Operators, oldOpLen, newOpLen);
    System.arraycopy(newPrec, 0, OperatorPrecedence, oldOpLen, newOpLen);
  }

  /** Adds functions to the default function list.  Specify new binary
      functions in newBinaryFuncs, and new unary functions in newUnaryFuncs
      (set an array to null if it's not needed). */
  public void addFunctions(String[] newBinaryFuncs, String[] newUnaryFuncs) {
    if (newBinaryFuncs != null) {
      String[] oldFuncs = BinaryFunctions;
      int oldLen = oldFuncs.length;
      int newLen = newBinaryFuncs.length;
      BinaryFunctions = new String[oldLen + newLen];
      System.arraycopy(oldFuncs, 0, BinaryFunctions, 0, oldLen);
      System.arraycopy(newBinaryFuncs, 0, BinaryFunctions, oldLen, newLen);
    }
    if (newUnaryFuncs != null) {
      String[] oldFuncs = UnaryFunctions;
      int oldLen = oldFuncs.length;
      int newLen = newUnaryFuncs.length;
      UnaryFunctions = new String[oldLen + newLen];
      System.arraycopy(oldFuncs, 0, UnaryFunctions, 0, oldLen);
      System.arraycopy(newUnaryFuncs, 0, UnaryFunctions, oldLen, newLen);
    }
  }

  /** Indicates whether a token is an operator, a binary
      function, or a unary function, or a variable. */
  public int getTokenType(String token) {
    for (int i=0; i<Operators.length; i++) {
      if (token.equalsIgnoreCase(Operators[i])) return OPERATOR_TOKEN;
    }
    for (int i=0; i<BinaryFunctions.length; i++) {
      if (token.equalsIgnoreCase(BinaryFunctions[i])) return BINARY_TOKEN;
    }
    for (int i=0; i<UnaryFunctions.length; i++) {
      if (token.equalsIgnoreCase(UnaryFunctions[i])) return UNARY_TOKEN;
    }
    return VARIABLE_TOKEN;
  }

  /** Converts an infix string to an array of tokens (Strings)
      in postfix notation. */
  public String[] toPostfix(String str) {
    String infix;

    // convert string to char array
    char[] charStr = str.toCharArray();

    // remove spaces and check parentheses
    int numSpaces = 0;
    int leftParen = 0;
    int rightParen = 0;
    for (int i=0; i<charStr.length; i++) {
      if (charStr[i] == ' ') numSpaces++;
      if (charStr[i] == '(') leftParen++;
      if (charStr[i] == ')') rightParen++;
    }
    if (leftParen != rightParen) return null;
    int j = 0;
    char[] exp = new char[charStr.length-numSpaces];
    for (int i=0; i<charStr.length; i++) {
      if (charStr[i] != ' ') exp[j++] = charStr[i];
    }
    infix = new String(exp)+"$";

    // tokenize string
    String ops = "";
    for (int i=0; i<Operators.length; i++) ops = ops+Operators[i];
    ops = ops+")";
    StringTokenizer tokenizer = new StringTokenizer(infix, ops, true);
    int numTokens = tokenizer.countTokens();

    // set up String stacks
    String[] opStack = new String[numTokens];
    String[] funcStack = new String[numTokens];
    String[] pfix = new String[numTokens];
    opStack[0] = "#";
    int opPt = 1;
    int funcPt = 0;
    int pfixlen = 0;

    // convert to postfix
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();

      if (token.equals("(")) {
        // push left paren onto operator stack
        opStack[opPt++] = token;
      }
      else if (token.equals(")")) {
        // pop all operators off stack until left paren reached
        String op = opStack[--opPt];
        while (!op.equals("(")) {
          pfix[pfixlen++] = op.equals(",") ? funcStack[--funcPt] : op;
          op = opStack[--opPt];
        }
      }
      else {
        // get token's operator precedence
        int prec = getPrecLevel(token);

        if (prec > 0) { // token is an operator or a unary function
          // pop operators with greater precedence off stack onto pfix
          String op = opStack[opPt-1];
          while (getPrecLevel(op) <= prec) {
            opPt--;
            pfix[pfixlen++] = op;
            op = opStack[opPt-1];
          }
          // push token onto operator stack
          opStack[opPt++] = token;
        }
        else if (prec == 0) { // token is a binary function
          // push token onto function stack
          funcStack[funcPt++] = token;
        }
        else { // token is a variable or a constant
          // append token to pfix
          pfix[pfixlen++] = token;
        }
      }
    }

    // return postfix array of tokens
    String[] postfix = new String[pfixlen];
    System.arraycopy(pfix, 0, postfix, 0, pfixlen);
    return postfix;
  }

  /** returns a token's level of precedence, or -1 if there is an error. */
  int getPrecLevel(String str) {
    for (int i=0; i<Operators.length; i++) {
      if (Operators[i].equalsIgnoreCase(str)) return OperatorPrecedence[i];
    }
    for (int i=0; i<UnaryFunctions.length; i++) {
      if (UnaryFunctions[i].equalsIgnoreCase(str)) return 1;
    }
    for (int i=0; i<BinaryFunctions.length; i++) {
      if (BinaryFunctions[i].equalsIgnoreCase(str)) return 0;
    }
    return -1;
  }
}

