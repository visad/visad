
//
// FormulaTest.java
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

import java.io.*;
import java.util.StringTokenizer;

/** Tests the formula package.<P> */
public class FormulaTest {
  /** Type 'java visad.formula.FormulaTest' to test the formula package */
  public static void main(String[] argv) {
    // create FormulaManager object
    String[] binOps = {"+", "-", "*", "/", "%", "^", "."};
    int[] binPrec =   {600, 600, 500, 500, 500, 300, 500};
    String[] binMethods = {"visad.formula.FormulaTest.add(" +
                           "java.lang.Double, java.lang.Double)",
                           "visad.formula.FormulaTest.subtract(" +
                           "java.lang.Double, java.lang.Double)",
                           "visad.formula.FormulaTest.multiply(" +
                           "java.lang.Double, java.lang.Double)",
                           "visad.formula.FormulaTest.divide(" +
                           "java.lang.Double, java.lang.Double)",
                           "visad.formula.FormulaTest.remainder(" +
                           "java.lang.Double, java.lang.Double)",
                           "visad.formula.FormulaTest.power(" +
                           "java.lang.Double, java.lang.Double)",
                           "visad.formula.FormulaTest.multiply(" +
                           "java.lang.Double, java.lang.Double)"};
    String[] unaryOps = {"-", "*"};
    int[] unaryPrec =   {400, 100};
    String[] unaryMethods = {"visad.formula.FormulaTest.negate(" +
                             "java.lang.Double)",
                             "visad.formula.FormulaTest.sqr(" +
                             "java.lang.Double)"};
    String[] functions = {"abs", "sqrt", "min", "max"};
    String[] funcMethods = {"visad.formula.FormulaTest.abs(" +
                            "java.lang.Double)",
                            "visad.formula.FormulaTest.sqrt(" +
                            "java.lang.Double)",
                            "visad.formula.FormulaTest.min(" +
                            "java.lang.Double[])",
                            "visad.formula.FormulaTest.max(" +
                            "java.lang.Double[])"};
    int implicitPrec = 200;
    String[] implicitMethods = {"visad.formula.FormulaTest.multiply(" +
                                "java.lang.Double, java.lang.Double)"};
    FormulaManager f = null;
    try {
      f = new FormulaManager(binOps, binPrec, binMethods, unaryOps, unaryPrec,
                             unaryMethods, functions, funcMethods,
                             implicitPrec, implicitMethods);
    }
    catch (FormulaException exc) {
      System.out.println("Error creating FormulaManager object:\n" + exc);
      System.exit(1);
    }

    // print out helpful information
    System.out.print("Binary operators: ");
    for (int i=0; i<binOps.length; i++) System.out.print(binOps[i] + " ");
    System.out.println();
    System.out.print("Unary operators: ");
    for (int i=0; i<unaryOps.length; i++) System.out.print(unaryOps[i] + " ");
    System.out.println();
    System.out.print("Functions: ");
    for (int i=0; i<functions.length; i++) {
      System.out.print(functions[i] + " ");
    }
    System.out.println();
    System.out.println("In addition, implicit functions (e.g., a(b)) are " +
                       "supported.\n");
    System.out.println("To assign a value to a variable, type " +
                       "\"var = expr\"");
    System.out.println("To view the current value of a variable, type " +
                       "\"var\"\n");
    System.out.println("Press RETURN on a blank line to stop\n");

    // set up input stream variables
    InputStreamReader isr = new InputStreamReader(System.in);
    char[] nul = new char[80];
    for (int i=0; i<80; i++) nul[i] = '\0';
    char[] buf = new char[80];

    // input loop
    while (true) {
      try {
        System.arraycopy(nul, 0, buf, 0, 80);
        isr.read(buf, 0, 80);
        // remove spaces and stuff after \n
        String s = new String(buf);
        int index = s.indexOf('\n');
        if (index > 0) s = s.substring(0, index-1);
        StringTokenizer t = new StringTokenizer(s, " ", false);
        s = "";
        while (t.hasMoreTokens()) s = s + t.nextToken();
        if (s.equals("")) break;
        index = s.indexOf('=');
        if (index < 0) {
          // variable look-up operation
          try {
            Double val = (Double) f.getValue(s);
            String formula = f.getFormula(s);
            System.out.print(s + " = ");
            if (formula != null) System.out.print(formula + " = ");
            if (val != null) System.out.println(val.doubleValue());
            else System.out.println("null");
          }
          catch (FormulaException exc) {
            System.out.println("The variable \"" + s + "\" does not exist");
          }
        }
        else {
          // variable assignment operation
          String v = s.substring(0, index);
          String e = s.substring(index+1);
          boolean eflag = false;
          try {
            f.assignFormula(v, e);
          }
          catch (FormulaException exc) {
            System.out.println("\"" + v + "\" is an illegal variable name");
            eflag = true;
          }
          if (!eflag) {
            String[] sl = f.getErrors(v);
            if (sl != null) {
              System.out.println("Errors occurred while evaluating formula:");
              for (int i=0; i<sl.length; i++) {
                System.out.println("  - " + sl[i]);
              }
            }
          }
        }
      }
      catch (IOException exc) {
        System.out.println("Error getting user input");
      }
    }
  }

  // The methods below are called by the FormulaManager object created above

  public static Double add(Double a, Double b) {
    return new Double(a.doubleValue() + b.doubleValue());
  }

  public static Double subtract(Double a, Double b) {
    return new Double(a.doubleValue() - b.doubleValue());
  }

  public static Double multiply(Double a, Double b) {
    return new Double(a.doubleValue() * b.doubleValue());
  }

  public static Double divide(Double a, Double b) {
    return new Double(a.doubleValue() / b.doubleValue());
  }

  public static Double remainder(Double a, Double b) {
    return new Double(Math.IEEEremainder(a.doubleValue(), b.doubleValue()));
  }

  public static Double negate(Double a) {
    return new Double(-a.doubleValue());
  }

  public static Double power(Double a, Double b) {
    return new Double(Math.pow(a.doubleValue(), b.doubleValue()));
  }

  public static Double abs(Double a) {
    return new Double(Math.abs(a.doubleValue()));
  }

  public static Double sqrt(Double a) {
    return new Double(Math.sqrt(a.doubleValue()));
  }

  public static Double sqr(Double a) {
    double d = a.doubleValue();
    return new Double(d*d);
  }

  public static Double min(Double[] a) {
    int index = 0;
    for (int i=1; i<a.length; i++) {
      if (a[i].doubleValue() < a[index].doubleValue()) index = i;
    }
    return a[index];
  }

  public static Double max(Double[] a) {
    int index = 0;
    for (int i=1; i<a.length; i++) {
      if (a[i].doubleValue() > a[index].doubleValue()) index = i;
    }
    return a[index];
  }

}

