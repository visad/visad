package edu.wisc.ssec.mcidas.adde;
import java.util.*;
import java.lang.*;
import java.io.*;

/** Helper class 
  * to interpret the band information 
  * from the ADDE SATBANDS file returned by servers
*/

public class AddeSatBands {
  String[] c;

  public AddeSatBands(String[] cards) {
    c = cards;
  }

  /** given a sensor and a cal type, return a list of bands possible
  */
  public String[][] getBandDescr(int sensor, String cal) {
    if (c == null) return null;
    int gotit = -1;
    Vector v = new Vector();
    for (int i=0; i<c.length; i++) {
      if ( ! c[i].startsWith("Sat ")) continue;
      StringTokenizer st = new StringTokenizer(c[i]," ");
      String temp = st.nextToken();  // throw away the key
      int m = st.countTokens();
      for (int k=0; k<m; k++) {
        int ss = Integer.parseInt(st.nextToken().trim());
        if (ss == sensor) {
          gotit = i;
          break;
        }
      }

      if (gotit != -1) break;
    }

    if (gotit == -1) return null;

    // now look for Cal
    int gotCal = -1;
    for (int i=gotit; i<c.length; i++) {
      if ( ! c[i].startsWith("Cal ")) continue;
      String calVal = c[i].substring(4).trim();
      if (calVal.equals(cal)) {
        gotCal = i;
        break;
      }

    }

    if (gotCal == -1) return null;
    gotCal++;

    for (int i=gotCal; i<c.length; i++) {
      if (c[i].startsWith("C") || c[i].startsWith("S") || c[i].startsWith("E")) break;
      if (c[i].startsWith("B") ) continue;
      String b = c[i].substring(0,2);
      String d = c[i].substring(4);
      v.addElement(b);
      v.addElement(d);
    }

    int num = v.size()/2;
    String[][] s = new String[2][num];
    for (int i=0; i<num; i++) {
      s[0][i] = (String) v.elementAt(2*i);
      s[1][i] = (String) v.elementAt(2*i+1);
    }

    return s;
  }

  public static void main(String[] a) {
    try {
      DataInputStream das = new DataInputStream(new FileInputStream("/src/edu/wisc/ssec/mcidas/adde/satband.txt"));

      Vector v = new Vector();
      while(true) {
        String s = das.readLine();
        if (s == null) break;
        v.addElement(s);
      }

      das.close();
      int num = v.size();
      System.out.println("size of input file = "+num);

      String sat = "12";
      String cal = "MSAT";
      if (a != null && a.length > 1) {
        sat = a[0];
        cal = a[1];
      }
      
      String[] sv = new String[num];
      for (int i=0; i<num; i++) { sv[i] = (String) v.elementAt(i); }
      AddeSatBands asb = new AddeSatBands(sv);
      String[][] f = asb.getBandDescr(Integer.parseInt(sat), cal);
      System.out.println("return from addesatbands");

      int numb = f[0].length;
      System.out.println("length of return = "+numb);
      for (int i=0; i<numb; i++) {
        System.out.println("band = value -> "+f[0][i]+" = "+f[1][i]);
      }

    } catch (Exception e) {System.out.println(e);}

  }
}
