
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

/** The Formula class is used to convert formulas to postfix notation.<P> */
public class Formula {

  public static final int OPERATOR_TOKEN = 1;
  public static final int BINARY_TOKEN = 2;
  public static final int UNARY_TOKEN = 3;
  public static final int VARIABLE_TOKEN = 4;

  // Binary operators that can be used in the infix formula
  private static String[] Operators
                 = {"@",".","^","&","*","/","%","+","-",",","(","{","$","#"};
  // Operator precedences of binary operators
  private static int[] OperatorPrecedence
                 = {10, 20, 40, 50, 60, 60, 60, 80, 80, 96, 97, 97, 98, 99};
  // Binary functions that can be used in the infix formula
  private static String[] BinaryFunctions
                 = {"max", "min", "atan2", "atan2Degrees", "extract"};
  // Unary functions that can be used in the infix formula
  private static String[] UnaryFunctions
                 = {"abs", "acos", "acosDegrees", "asin", "asinDegrees",
                    "atan", "atanDegrees", "ceil", "cos", "cosDegrees",
                    "exp", "floor", "log", "rint", "round", "sin", "d",
                    "sinDegrees", "sqrt", "tan", "tanDegrees", "negate"};

  /** indicates whether a token is an operator, a binary
      function, or a unary function, or a variable */
  public static int getTokenType(String token) {
    if (token.equals("d")) return OPERATOR_TOKEN;
    if (token.equals("&")) return UNARY_TOKEN;
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

  /** converts an infix string to an array of tokens (Strings) in
      postfix notation */
  public static String[] toPostfix(String str) {
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
    int newlen = charStr.length-numSpaces;
    if (newlen == 0) return null;
    char[] exp = new char[newlen];
    for (int i=0; i<charStr.length; i++) {
      if (charStr[i] != ' ') exp[j++] = charStr[i];
    }
    infix = new String(exp)+"$";

    // tokenize string
    String ops = "";
    for (int i=0; i<Operators.length; i++) ops = ops + Operators[i];
    ops = ops + ")";
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

    // detect "implicit operator" functions (e.g., x(y)) and unary minus
    boolean implicit = false;

    // flag for unary functions
    boolean unary = false;

    // convert to postfix
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();

      // solve derivatives recursively; their syntax is unique
      if (token.equals("d")) {
        String d = "";
        int dparen = 0;
        String t;
        do {
          t = tokenizer.nextToken();
          if (t.equals("(")) dparen++;
          if (t.equals(")")) dparen--;
          d = d + t;
        } while (dparen > 0);
        String[] s = toPostfix(d);
        if (s == null) return null;
        t = tokenizer.nextToken();
        if (!t.equals("/")) return null;
        t = tokenizer.nextToken();
        if (!t.equals("d")) return null;
        t = tokenizer.nextToken();
        if (!t.equals("(")) return null;
        String type = tokenizer.nextToken();
        t = tokenizer.nextToken();
        if (!t.equals(")")) return null;
        for (int i=0; i<s.length; i++) {
          pfix[pfixlen++] = s[i];
        }
        pfix[pfixlen++] = "~" + type;
        pfix[pfixlen++] = "d";
      }
      else if (token.equals("(")) {
        // push left paren onto operator stack
        opStack[opPt++] = implicit ? "{" : token;
        if (unary) opStack[opPt++] = ",";
        implicit = false;
        unary = false;
      }
      else if (token.equals(")")) {
        // pop all operators off stack until left paren reached
        if (opPt < 1) return null;
        String op = opStack[--opPt];
        while (!op.equals("(") && !op.equals("{")) {
          if (op.equals(",")) {
            if (funcPt < 1) return null;
            pfix[pfixlen++] = funcStack[--funcPt];
          }
          else pfix[pfixlen++] = op;
          if (opPt < 1) return null;
          op = opStack[--opPt];
        }
        if (op.equals("{")) {
          // pop operators with greater precedence off stack onto pfix
          if (opPt < 1) return null;
          op = opStack[opPt-1];
          int prec = getPrecLevel("@");
          while (getPrecLevel(op) <= prec) {
            opPt--;
            pfix[pfixlen++] = op;
            if (opPt < 1) return null;
            op = opStack[opPt-1];
          }
          //opStack[opPt++] = "@";
          pfix[pfixlen++] = "@";
        }
        implicit = true;
        unary = false;
      }
      else {
        // detect unary minus
        if (!implicit && token.equals("-")) token = "&";

        // get token's operator precedence
        int prec = getPrecLevel(token);

        if (prec > 0) { // token is an operator or a unary function
          // pop operators with greater precedence off stack onto pfix
          if (opPt < 1) return null;
          String op = opStack[opPt-1];
          while (getPrecLevel(op) <= prec) {
            opPt--;
            pfix[pfixlen++] = op;
            if (opPt < 1) return null;
            op = opStack[opPt-1];
          }
          // push token onto operator stack
          if (prec > 1) {
            opStack[opPt++] = token;
            unary = false;
          }
          else {
            // push unary function onto function stack, not operator stack
            funcStack[funcPt++] = token;
            unary = true;
          }
          implicit = false;
        }
        else if (prec == 0) { // token is a binary function
          // push token onto function stack
          funcStack[funcPt++] = token;
          implicit = false;
          unary = false;
        }
        else { // token is a variable or a constant
          // append token to pfix
          pfix[pfixlen++] = token;
          implicit = true;
          unary = false;
        }
      }
    }

    // return postfix array of tokens
    String[] postfix = new String[pfixlen];
    System.arraycopy(pfix, 0, postfix, 0, pfixlen);
    return postfix;
  }

  /** returns a token's level of precedence, or -1 if there is an error */
  private static int getPrecLevel(String str) {
    for (int i=0; i<Operators.length; i++) {
      if (Operators[i].equals(str)) return OperatorPrecedence[i];
    }
    for (int i=0; i<UnaryFunctions.length; i++) {
      if (UnaryFunctions[i].equalsIgnoreCase(str)) return 1;
    }
    for (int i=0; i<BinaryFunctions.length; i++) {
      if (BinaryFunctions[i].equalsIgnoreCase(str)) return 0;
    }
    return -1;
  }

  /** for testing the toPostfix() method */
  public static void main(String[] argv) {
    if (argv.length < 1) {
      System.out.println("Usage: java visad.ss.Formula <expr>\n"
                        +"where <expr> is an infix formula expression.\n");
      System.exit(0);
    }
    System.out.println("Infix:    " + argv[0]);
    String[] output = Formula.toPostfix(argv[0]);
    System.out.print("Postfix:  ");
    if (output == null) System.out.println("<UNABLE TO CONVERT>");
    else {
      for (int i=0; i<output.length; i++) {
        System.out.print(output[i]+" ");
      }
      System.out.println();
    }
  }

}

