import java.util.*;
import java.lang.*;
import java.io.*;

/** Helper class 
  * to interpret the band information 
  * from the ADDE SATBANDS file returned by servers
*/

public class AddeSatBands {
  Vector c;

  public AddeSatBands(Vector cards) {
    c = cards;
  }

  /** given a sensor and a cal type, return a list of bands possible
  */
  public String[][] getBandDescr(int sensor, String cal) {
    int gotit = -1;
    Vector v = new Vector();
    for (int i=0; i<c.size(); i++) {
      String s = (String) c.elementAt(i);
      if ( ! s.startsWith("Sat ")) continue;
      StringTokenizer st = new StringTokenizer(s," ");
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
    for (int i=gotit; i<c.size(); i++) {
      String s = (String) c.elementAt(i);
      if ( ! s.startsWith("Cal ")) continue;
      String calVal = s.substring(4).trim();
      if (calVal.equals(cal)) {
        gotCal = i;
        break;
      }

    }

    if (gotCal == -1) return null;
    gotCal++;

    for (int i=gotCal; i<c.size(); i++) {
      String s = (String) c.elementAt(i);
      if (s.startsWith("C") || s.startsWith("S") || s.startsWith("E")) break;
      if (s.startsWith("B") ) continue;
      String b = s.substring(0,2);
      String c = s.substring(4);
      v.addElement(b);
      v.addElement(c);
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
      DataInputStream das = new DataInputStream(new FileInputStream("satband.txt"));

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

      System.out.println("calling addesatbands");
      AddeSatBands asb = new AddeSatBands(v);
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
