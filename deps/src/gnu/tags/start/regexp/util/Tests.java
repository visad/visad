/*
 * gnu/regexp/util/Tests.java -- Simple testsuite for gnu.regexp package
 * Copyright (C) 1998 Wes Biggs
 *
 * This file is in the public domain.  However, the gnu.regexp library
 * proper is licensed under the terms of the GNU Library General Public
 * License (see the file LICENSE for details).
 */

package gnu.regexp.util;
import gnu.regexp.*;

/**
 * This is a very basic testsuite application for gnu.regexp.
 *
 * @author <A HREF="mailto:wes@cacas.org">Wes Biggs</A>
 * @version 1.01
 */
public class Tests {
  private Tests() { }

  private static void check(REMatch m, String expect, int x) {
    if ((m == null) || !m.toString().equals(expect)) System.out.print("Failed");
    else System.out.print("Passed");
    System.out.println(" test #"+x);
  }

  /**
   * Runs the testsuite.  No command line arguments are necessary. 
   *
   * @exception REException An error occurred compiling a regular expression.
   */
  public static void main(String[] argv) throws REException {
    RE e;

    e = new RE("(.*)z");
    check(e.getMatch("xxz"),"xxz",1);

    e = new RE(".*z");
    check(e.getMatch("xxz"),"xxz",2);
    
    e = new RE("(x|xy)z");
    check(e.getMatch("xz"),"xz",3);
    check(e.getMatch("xyz"),"xyz",4);

    e = new RE("(x)+z");
    check(e.getMatch("xxz"),"xxz",5);

    e = new RE("abc");
    check(e.getMatch("xyzabcdef"),"abc",6);

    e = new RE("^start.*end$");
    check(e.getMatch("start here and go to the end"),"start here and go to the end",7);

    e = new RE("(x|xy)+z");
    check(e.getMatch("xxyz"),"xxyz",8);

    e = new RE("type=([^ \t]+)[ \t]+exts=([^ \t\n\r]+)");
    check(e.getMatch("type=text/html  exts=htm,html"),"type=text/html  exts=htm,html",9);

    e = new RE("(x)\\1");
    check(e.getMatch("zxxz"),"xx", 10);

    e = new RE("(x*)(y)\\2\\1");
    check(e.getMatch("xxxyyxx"),"xxyyxx",11);

    e = new RE("[-go]+");
    check(e.getMatch("go-go"),"go-go",12);

    e = new RE("[\\w-]+");
    check(e.getMatch("go-go"),"go-go",13);

    e = new RE("^start.*?end");
    check(e.getMatch("start here and end in the middle, not the very end"),"start here and end",14);
    
    e = new RE("\\d\\s\\w\\n\\r");
    check(e.getMatch("  9\tX\n\r  "),"9\tX\n\r",15);

    e = new RE("zow",RE.REG_ICASE);
    check(e.getMatch("ZoW"),"ZoW",16);

    e = new RE("(\\d+)\\D*(\\d+)\\D*(\\d)+");
    check(e.getMatch("size--10 by 20 by 30 feet"),"10 by 20 by 30",17);

    e = new RE("(ab)(.*?)(d)");
    REMatch m = e.getMatch("abcd");
    check(m,"abcd",18);
    System.out.println(((m.toString(2).equals("c")) ? "Pass" : "Fail") 
		       + "ed test #19");
  }
}      
    



