//
// TextAdapter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.text;

import java.io.IOException;
import java.io.*;
import java.util.*;
import visad.Set;

import java.net.URL;

import visad.*;
import visad.VisADException;
import visad.data.in.ArithProg;


/** this is an VisAD file adapter for comma-, tab- and blank-separated
  * ASCII text file data.  It will attempt to create a FlatField from
  * the data and descriptions given in the file and/or the constructor.
  *
  * The text files contained delimited data.  The delimiter is 
  * determined as follows:  if the file has a well-known extension
  * (.csv, .tsv, .bsv) then the delimiter is implied by the extension.
  * In all other cases, the delimiter for the data (and for the
  * "column labels") is determined by reading the first line and
  * looking, in order, for a tab, comma, or blank.  Which ever one
  * is found first is taken as the delimiter.
  *
  * Two extra pieces of information are needed:  the VisAD "MathType"
  * which is specified as a string (e.g., (x,y)->(temperature))
  * and may either be the first line of the file or passed in through
  * one of the constructors.
  *
  * The second item are the "column labels" which contain the names
  * of each field in the data.  The names of all range components
  * specified in the "MathType" must appear.  The names of domain
  * components are optional.  The values in this string are separated
  * by the delimiter, as defined above.
  *
  * See visad.data.text.README.text for more details.
  * 
  * @author Tom Whittaker
  * 
  */
public class TextAdapter {

  private FlatField ff = null;
  private Field field = null;
  private boolean debug = false;
  private String DELIM;
  private final String COMMA = ",";
  private final String SEMICOLON = ";";
  private final String TAB = "\t";
  private final String BLANK = " ";
  private boolean DOQUOTE = true;
  private boolean GOTTIME = false;


  String[] hdrNames;
  Unit[] hdrUnits;
  double[] hdrMissingValues;
  String[] hdrMissingStrings;
  String[] hdrFormatStrings;
  int[] hdrIsInterval;
  double[] hdrErrorEstimates;
  double[] rangeErrorEstimates;
  Unit[] rangeUnits;
  Set[] rangeSets;
  double[] domainErrorEstimates;
  Unit[] domainUnits;
  double[] hdrScales;
  double[] hdrOffsets;
  int[][] hdrColumns;
  int[][] values_to_index;


  /** Create a VisAD FlatField from a local Text (comma-, tab- or 
    * blank-separated values) ASCII file
    * @param filename name of local file.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public TextAdapter(String filename) throws IOException, VisADException {
    InputStream is = new FileInputStream(filename);
    DELIM = null;
    if (filename.trim().toLowerCase().endsWith(".csv")) DELIM=COMMA;
    if (filename.trim().toLowerCase().endsWith(".tsv")) DELIM=TAB;
    if (filename.trim().toLowerCase().endsWith(".bsv")) DELIM=BLANK;
    
    readit(is, null, null);
  }

  /** Create a VisAD FlatField from a local Text (comma-, tab- or 
    * blank-separated values) ASCII file
    * @param filename name of local file.
    * @param map the VisAD "MathType" as a string defining the FlatField
    * @param params the list of parameters used to define what columns
    *  of the text file correspond to what MathType parameters.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public TextAdapter(String filename, String map, String params) 
                         throws IOException, VisADException {
    InputStream is = new FileInputStream(filename);
    DELIM = null;
    if (filename.trim().toLowerCase().endsWith(".csv")) DELIM=COMMA;
    if (filename.trim().toLowerCase().endsWith(".tsv")) DELIM=TAB;
    if (filename.trim().toLowerCase().endsWith(".bsv")) DELIM=BLANK;
    readit(is, map, params);
  }

  /** Create a VisAD FlatField from a remote Text (comma-, tab- or 
    * blank-separated values) ASCII file
    *
    * @param url File URL.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public TextAdapter(URL url) throws IOException, VisADException {
    DELIM = null;
    String filename = url.getFile();
    if (filename.trim().toLowerCase().endsWith(".csv")) DELIM=COMMA;
    if (filename.trim().toLowerCase().endsWith(".tsv")) DELIM=TAB;
    if (filename.trim().toLowerCase().endsWith(".bsv")) DELIM=BLANK;
    InputStream is = url.openStream();
    readit(is, null, null);
  }

  /** Create a VisAD FlatField from a local Text (comma-, tab- or 
    * blank-separated values) ASCII file
    * @param url File URL.
    * @param map the VisAD "MathType" as a string defining the FlatField
    * @param params the list of parameters used to define what columns
    *  of the text file correspond to what MathType parameters.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public TextAdapter(URL url, String map, String params) 
                        throws IOException, VisADException {
    DELIM = null;
    String filename = url.getFile();
    if (filename.trim().toLowerCase().endsWith(".csv")) DELIM=COMMA;
    if (filename.trim().toLowerCase().endsWith(".tsv")) DELIM=TAB;
    if (filename.trim().toLowerCase().endsWith(".bsv")) DELIM=BLANK;
    InputStream is = url.openStream();
    readit(is, map, null);
  }

  void readit(InputStream is, String map, String params) 
                              throws IOException, VisADException {
    // read the ASCII file, using commas as field separators
    // first line is a header line

    ff = null;
    field = null;

    if (debug) System.out.println("####   Text Adapter v2.x running");

    BufferedReader bis = new BufferedReader(new InputStreamReader(is));

    // mapping defines how the names are mapped
    // for example:   (x,y) => (one, two, three)

    String maps = null;
    if (map == null) {
      String t;
      while (true) {
        t = bis.readLine();
        if (t == null) return;
        if (!isText(t)) return;
        if (t.startsWith("#") ||
            t.startsWith("!") || 
            t.startsWith("%") || 
            t.length() < 1) continue;
        break;
      }
      maps = t.trim();
    } else {
      maps = map;
    }

    maps = makeMT(maps);
    if (maps == null) {
      throw new visad.data.BadFormException(
        "TextAdapter: Invalid or missing MathType");
    }

    if (debug) System.out.println("Specified MathType = "+maps);

    // but first, we need to get the column headers because they
    // may have [units] associated with them.  The column headers
    // primarily define where the data are.

    String hdr = null;
    if (params == null) {

      while (true) {
        hdr = bis.readLine();
        if (hdr == null) return;
        if (!isText(hdr)) return;
        if (hdr.startsWith("#") || 
           hdr.startsWith("!") || 
           hdr.startsWith("%") || 
           hdr.length() < 1) continue;
        break;
      }
    } else {
      hdr = params;
    }

    String hdrDelim = DELIM;
    if (DELIM == null) {
      if (hdr.indexOf(BLANK) != -1) hdrDelim = BLANK; 
      if (hdr.indexOf(COMMA) != -1) hdrDelim = COMMA; 
      if (hdr.indexOf(SEMICOLON) != -1) hdrDelim = SEMICOLON; 
      if (hdr.indexOf(TAB) != -1) hdrDelim = TAB; 

      if (debug) System.out.println("Using header delimiter = "+
                                     (hdrDelim.getBytes())[0]);
    }

    StringTokenizer sthdr = new StringTokenizer(hdr,hdrDelim);
    int nhdr = sthdr.countTokens();
    hdrNames = new String[nhdr];
    hdrUnits = new Unit[nhdr];
    hdrMissingValues = new double[nhdr];
    hdrMissingStrings = new String[nhdr];
    hdrFormatStrings = new String[nhdr];
    hdrIsInterval = new int[nhdr];
    hdrErrorEstimates = new double[nhdr];
    hdrScales = new double[nhdr];
    hdrOffsets = new double[nhdr];
    hdrColumns = new int[2][nhdr];


    // pre-scan of the header names to seek out Units
    // since we cannot change a RealType once it's defined!!

    for (int i=0; i<nhdr; i++) {
      String name = sthdr.nextToken().trim();
      String hdrUnitString = null;
      hdrMissingValues[i] = Double.NaN;
      hdrMissingStrings[i] = null;
      hdrFormatStrings[i] = null;
      hdrIsInterval[i] = 0;
      hdrScales[i] = 1.0;
      hdrOffsets[i] = 0.0;
      hdrErrorEstimates[i] = 0.0;
      hdrColumns[0][i] = -1; // indicating no fixed columns
      
      int m = name.indexOf("[");

      if (m == -1) {
        hdrNames[i] = name;
        hdrUnitString = null;

      } else {
        int m2 = name.indexOf("]");
        if (m2 == -1) {
          throw new VisADException("TextAdapter: Bad [descriptor] named in:"+name);
        }

        // now parse items: unit=xxx miss=xxx interval=xxx error=xxx

        // 1. tokenize on " "
        // 2. scan each token, retokenizing on "="
        // 3. if (has no "=") && (is first one) then treat as Unit
        // 4. otherwise, look for keys "unit" "miss" "inter" "err" "scale" "offset" "pos"
      
        //    and fill in the values in array[i]

        if (m2 >= name.length()) {
          hdrNames[i] = name.substring(0,m).trim();
        } else {
          hdrNames[i] = (name.substring(0,m)+name.substring(m2+1)).trim();
        }

        String cl = name.substring(m+1,m2).trim();
        StringTokenizer stcl = new StringTokenizer(cl," ");
        int ncl = stcl.countTokens();

        if (ncl == 1 && cl.indexOf("=") == -1) {
          hdrUnitString = cl;  // backward compatible...

        } else {
          while (stcl.hasMoreTokens()) {
            String s = stcl.nextToken().trim();
            StringTokenizer sts = new StringTokenizer(s,"=");
            if (sts.countTokens() != 2) {
              throw new VisADException("TextAdapter: Invalid clause in: "+s);
            }
            String tok = sts.nextToken().trim();
            String val = sts.nextToken();
            
            // check for quoted strings
            if (val.startsWith("\"")) {

              // see if ending quote also fetched
              if (val.endsWith("\"")) {
                String v2 = val.substring(1,val.length()-1);
                val = v2;

              } else {
                // if not, then reparse stcl to suck up spaces...
                try {
                  String v2 = stcl.nextToken("\"");
                  stcl.nextToken(" ");
                  String v3 = val.substring(1)+v2;
                  val = v3;

                } catch (NoSuchElementException nse2) {
                  val="";
                }
              }
            }

            if (debug) System.out.println("####   tok = "+tok+ " val = '"+val+"'");

            if (tok.toLowerCase().startsWith("unit")) {
              hdrUnitString = val;

            } else if (tok.toLowerCase().startsWith("mis")) {
              hdrMissingStrings[i] = val.trim();
              try {
                hdrMissingValues[i] = Double.parseDouble(val);
              } catch (java.lang.NumberFormatException me) {
                hdrMissingValues[i] = Double.NaN;
              }
              
            } else if (tok.toLowerCase().startsWith("int")) {

              hdrIsInterval[i] = -1;
              if (val.toLowerCase().startsWith("t")) hdrIsInterval[i] = 1;
              if (val.toLowerCase().startsWith("f")) hdrIsInterval[i] = 0;
              if (hdrIsInterval[i] == -1) {
                throw new VisADException("TextAdapter: Value of \'interval\' must be \'true\' or \'false\'");
              }

            } else if (tok.toLowerCase().startsWith("err")) {
              hdrErrorEstimates[i] = Double.parseDouble(val);

            } else if (tok.toLowerCase().startsWith("sca")) {
              hdrScales[i] = Double.parseDouble(val);

            } else if (tok.toLowerCase().startsWith("off")) {
              hdrOffsets[i] = Double.parseDouble(val);

            } else if (tok.toLowerCase().startsWith("pos")) {
              StringTokenizer stp = new StringTokenizer(val,":");
              if (stp.countTokens() != 2) {
                throw new VisADException("TextAdapter: invalid Position parameter in:"+s);
              }
              hdrColumns[0][i] = Integer.parseInt(stp.nextToken().trim());
              hdrColumns[1][i] = Integer.parseInt(stp.nextToken().trim());

            } else if (tok.toLowerCase().startsWith("fmt")) {
              hdrFormatStrings[i] = val.trim();
            } else {
              throw new VisADException("TextAdapter: invalid token name: "+s);
            }

          }
        }

      }

      if (debug) 
            System.out.println("hdr name = "+hdrNames[i]+" units="+
             hdrUnitString+
             " miss="+hdrMissingValues[i]+" interval="+hdrIsInterval[i]+ 
             " errorest="+hdrErrorEstimates[i]+" scale="+hdrScales[i]+
             " offset="+hdrOffsets[i]+" pos="+hdrColumns[0][i]+":"+
             hdrColumns[1][i]);

      Unit u = null;
      if (hdrUnitString != null && 
                !hdrUnitString.trim().equalsIgnoreCase("null") ) {
        try {

          u = visad.data.units.Parser.parse(hdrUnitString.trim());

        } catch (Exception ue) {

          try {
            u = visad.data.units.Parser.parse(
                           hdrUnitString.trim().replace(' ','_'));
          } catch (Exception ue2) {
            System.out.println("Unit name problem:"+ue+" with: "+hdrUnitString);
            u = null;
          }
        }
      }

      if (debug) System.out.println("####   assigned Unit as u="+u);

      String rttemp = hdrNames[i].trim();
      if (rttemp.indexOf("(Text)") == -1) {

        int parenIndex = rttemp.indexOf("(");

        if (parenIndex < 0) parenIndex = rttemp.indexOf("[");
        if (parenIndex < 0) parenIndex = rttemp.indexOf("{");
        if (parenIndex < 0) parenIndex = rttemp.indexOf(" ");
        String rtname = parenIndex < 0 ? rttemp.trim() : rttemp.substring(0,parenIndex);

        RealType rt = RealType.getRealType(rtname, u, null, hdrIsInterval[i]);

        if (rt == null) {  // tried to re-use with different units
          if (debug) System.out.println("####   rt was returned as null");
          if (u != null) System.out.println("####  Could not make RealType using specified Unit ("+hdrUnitString+") for parameter name: "+rtname);
          rt = RealType.getRealType(rtname);
        }

        // get a compatible unit, if necessary

        if (rt.equals(visad.RealType.Time)) {
          GOTTIME = true;
          if (debug) System.out.println("####  found a visad.RealType.Time component");
        } else {
          GOTTIME = false;
        }


        if (u == null) u = rt.getDefaultUnit();
        if(debug) System.out.println("####  retrieve units from RealType = "+u);
      }

      hdrUnits[i] = u;
    }

    // get the MathType of the function

    MathType mt = null;
    try {
      mt = MathType.stringToType(maps);
    } catch (Exception mte) {
      System.out.println("####  Exception: "+mte);
      throw new VisADException("TextAdapter: MathType badly formed or missing: "+maps);
    }

    if (debug) {
      System.out.println(mt);
      new visad.jmet.DumpType().dumpMathType(mt,System.out);
    }

    // now get the names of the domain variables and range variables.
    String[] domainNames = null;
    String[] rangeNames = null;
    int numDom = 0;
    int numRng = 0;
    RealTupleType domType;
    TupleType rngType;

    if (mt instanceof FunctionType) {
      domType = ((FunctionType)mt).getDomain();
      numDom = domType.getDimension();
      domainNames = new String[numDom];

      for (int i=0; i<numDom; i++) {
        MathType comp = domType.getComponent(i);
        domainNames[i] = ((RealType)comp).toString().trim();
        if (debug) System.out.println("dom "+i+" = "+domainNames[i]);
      }

      rngType = (TupleType) ((FunctionType)mt).getRange();
      numRng = rngType.getDimension();
      rangeNames = new String[numRng];
      rangeSets = new Set[numRng];
      for (int i=0; i<numRng; i++) {
        MathType comp = rngType.getComponent(i);
        rangeNames[i] = (comp).toString().trim();
        if (debug) System.out.println("range "+i+" = "+rangeNames[i]);
        if (comp instanceof RealType) {
          rangeSets[i] = ((RealType) comp).getDefaultSet();
          if (rangeSets[i] == null) {
            if (comp.equals(RealType.Time)) {
              rangeSets[i] = new DoubleSet(new SetType(comp));
            } else {
              rangeSets[i] = new FloatSet(new SetType(comp));
            }
          }
        } else {
          rangeSets[i] = null;  // something else is wrong here...
        }
        if (debug) System.out.println("####  rangeSet = "+rangeSets[i]);
;
      }

    } else { 
      throw new visad.VisADException("TextAdapter: Math Type is not a simple FunctionType");
    }


// now for each header label, determine if it's a domain or
// range component -- and if so, which one.

// also, if it's a domain component, allow for name(first:last[:number])
//
// and if none of the domain components appear in the list, then
// they are computed as name(0:N-1)

    int[] domainPointer = new int[numDom];
    double[][] domainRanges = new double[3][numDom]; // min, max, numb
    boolean[] gotDomainRanges = new boolean[numDom];
    domainErrorEstimates = new double[numDom];
    domainUnits = new Unit[numDom];
    rangeErrorEstimates = new double[numRng];
    rangeUnits = new Unit[numRng];

    int countDomain = 0;

    for (int i=0; i<numDom; i++) {
      domainPointer[i] = -1;
      gotDomainRanges[i] = false;
      domainErrorEstimates[i] = Double.NaN;
      domainUnits[i] = null;
    }

    int[] rangePointer = new int[numRng];
    int countRange = 0;

    for (int i=0; i<numRng; i++) {
      rangePointer[i] = -1;
      rangeErrorEstimates[i] = Double.NaN;
      rangeUnits[i] = null;
    }

    int countValues = -1;
    values_to_index = new int[3][nhdr];

    for (int i=0; i<nhdr; i++) {
      values_to_index[0][i] = -1;  // points to domains
      values_to_index[1][i] = -1;  // points to ranges
      values_to_index[2][i] = -1;  // points to names/units/etc
      countValues ++;

      String name = hdrNames[i];

      // see if it's a domain name
      boolean gotName = false;

      // is there a "min:max" clause?
      String test_name = name;
      int n = test_name.indexOf("(");
      if (n != -1) {
        // but allow for "(Text)" 
        if ((test_name.indexOf("(Text)")) == -1) {
          test_name = name.substring(0,n).trim();
          countValues --;  // this value wont appear in data!
          countDomain --; // and is a pre-defined, linear set
        }
      }

      // try to find the column header name in the domain name list
      for (int k=0; k<numDom; k++) {

        if (test_name.equals(domainNames[k]) ) { 
          domainPointer[k] = countValues;
          domainErrorEstimates[k] = hdrErrorEstimates[i];
          domainUnits[k] = hdrUnits[i];
          gotName = true;
          countDomain ++;
          // now see if a list is given...
          if (n != -1) {

            try {

              String ss = name.substring(n+1,name.length()-1);
              StringTokenizer sct = new StringTokenizer(ss,":");
              String first = sct.nextToken().trim();
              String second = sct.nextToken().trim();
              String third = "1";
              if (sct.hasMoreTokens()) third = sct.nextToken().trim();
              domainRanges[0][k] = Double.parseDouble(first);
              domainRanges[1][k] = Double.parseDouble(second);
              domainRanges[2][k] = Double.parseDouble(third);
              gotDomainRanges[k] = true;

            } catch (Exception ef) {
              throw new VisADException(
       "TextAdapter: Error while interpreting min:max values for domain "+name);
            }

          } else if (countValues > -1) { // if no list, get from file
            values_to_index[0][countValues] = k;
            values_to_index[2][countValues] = i;
          }

          break;
       }

    } 

    if (gotName) continue;

    // or see if its a range name...

    for (int k=0; k<numRng; k++) {
      if (name.equals(rangeNames[k]) ) {
        rangePointer[k] = countValues;
        rangeErrorEstimates[k] = hdrErrorEstimates[i];
        rangeUnits[k] = hdrUnits[i];
        countRange ++;
        values_to_index[1][countValues] = k;
        values_to_index[2][countValues] = i;
        gotName = true;
      }
    }
  }


// huge debug printout...
// *****************************************************************

  if (debug) {
    System.out.println("countDom/numDom="+countDomain+" "+numDom);

    System.out.println("countRange/numRng="+countRange+" "+numRng);

    System.out.println("Domain info:");
    for (int i=0; i<numDom; i++) {
      System.out.println("Dom name / index = "+domainNames[i]+"  "+
             domainPointer[i]);

      if (gotDomainRanges[i]) {
        System.out.println("    ..."+domainRanges[0][i]+"  "+
            domainRanges[1][i]+"    "+domainRanges[2][i]);
      }
    }

    System.out.println("Range info:");
    for (int i=0; i<numRng; i++) {
      System.out.println("Rng name / index / error est = "+rangeNames[i]+"  "+
             rangePointer[i]+ "  " + rangeErrorEstimates[i] +" "+
             rangeUnits[i]);
    }

    System.out.println("values_to_index pointers = ");
    for (int i=0; i<nhdr; i++) {
      System.out.println(" inx / value = "+i+ 
              " "+values_to_index[0][i]+"    "+values_to_index[1][i]+
              " "+values_to_index[2][i]);
    }
  }

// ***************************************************************


    // for each line of text, put the values into the ArrayList
    ArrayList domainValues = new ArrayList();
    ArrayList rangeValues = new ArrayList();
    ArrayList tupleValues = new ArrayList(); 
    Tuple tuple = null;
    
    String dataDelim = DELIM;
    boolean isRaster = false;
    int numElements = 1;

    // in the 'raster array' case, the numRng value will be 1,
    // along with the countRange.  numDomain must be 2.

    // if the domain is 2D, then get values from the first
    // matching column to the end...
    if (countRange == 1 && numRng == 1 && 
                numDom == 2 && countDomain < 2) isRaster = true;

    while (true) {
      String s = bis.readLine();
      if (debug) System.out.println("read:"+s);
      if (s == null) break;
      if (!isText(s)) return;
      if (s.startsWith("#") || 
         s.startsWith("!") || 
         s.startsWith("%") || 
         s.length() < 1) continue;

      if (dataDelim == null) {
        if (s.indexOf(BLANK) != -1) dataDelim = BLANK; 
        if (s.indexOf(COMMA) != -1) dataDelim = COMMA; 
        if (s.indexOf(SEMICOLON) != -1) dataDelim = SEMICOLON; 
        if (s.indexOf(TAB) != -1) dataDelim = TAB; 

        if (debug) System.out.println("Using data delimiter = "+
                                       (dataDelim.getBytes())[0]);
      }

      StringTokenizer st = new StringTokenizer(s,dataDelim);
      int n = st.countTokens();
      if (n < 1) continue; // something is wrong if this happens!

      double [] dValues = new double[numDom];
      double [] rValues = null;
      
      Data [] tValues = null;

      if (isRaster) {

        if (debug) System.out.println("probably a raster...");
        boolean gotFirst = false;
        int rvaluePointer = 0;
        int irange = 0;
        for (int i=0; i<n; i++) {

          String sa = st.nextToken().trim();
          
          if (i >= nhdr) {  // are we past where domain would be found?

            if (!gotFirst) {
              throw new VisADException(
                        "TextAdapter: Cannot find first raster value");
            }

            rvaluePointer ++;
            rValues[rvaluePointer] = getVal(sa, irange);

          } else {  // or are we still looking for domain?
          
            if (values_to_index[0][i] != -1) {
              dValues[values_to_index[0][i]] = getVal(sa, i);
            }

            if (gotFirst) {  // already gathering data
              rvaluePointer ++;
              rValues[rvaluePointer] = getVal(sa, irange);

            } else {
               if (values_to_index[1][i] != -1) {
                 // cannot dimension the array until we have found
                 // the first set of range values!!
                 rValues = new double[n - i];
                 irange = i;
                 rValues[rvaluePointer] = getVal(sa, irange);
                 gotFirst = true;
               }
            }

          }
        }
         
      } else {  // is probably NOT a raster

        tValues = new Data[numRng];
      
        if (debug) System.out.println("probably not a raster...");
        rValues = new double[numRng];
        double thisDouble; 
        MathType thisMT;
        if (n > nhdr) n = nhdr; // in case the # tokens > # parameters

        for (int i=0; i<n; i++) {

          String sa = st.nextToken().trim();
          String sThisText;

          if (values_to_index[0][i] != -1) {
            dValues[values_to_index[0][i]] = getVal(sa, i);

          } else if (values_to_index[1][i] != -1) {

            thisMT = rngType.getComponent(values_to_index[1][i]);
            
            if (thisMT instanceof TextType) {

              // if Text, then check for quoted string
              if (sa.startsWith("\"")) {
                if (sa.endsWith("\"")) {  // if single token ends with quote
                  String sa2 = sa.substring(1,sa.length()-1);
                  sThisText = sa2;
                } else {

                  try {
                    String sa2 = st.nextToken("\"");
                    sThisText = sa.substring(1)+sa2;
                  } catch (NoSuchElementException nse) {
                    sThisText = "";
                  }
                }

                if (debug) System.out.println("####   Text value='"+sThisText+"'");

              // if not quoted, then take "as is"
              } else {
                sThisText = sa;
              }


              // now make the VisAD Data 
              try {
                tValues[values_to_index[1][i]] = 
                        new Text((TextType) thisMT, sThisText);
                if (debug) System.out.println("tValues[" + 
                          values_to_index[1][i] + "] = " + 
                          tValues[values_to_index[1][i]]);
              } catch (Exception e) {
                System.out.println(" Exception converting " + 
                                       thisMT + " to TextType " + e);
              }

              
            // if not Text, then treat as numeric
            } else {
              rValues[values_to_index[1][i]] = getVal(sa,i);
              try {
                tValues[values_to_index[1][i]] = 
                    new Real((RealType) thisMT, getVal(sa,i), hdrUnits[i]);
                if (debug) System.out.println("tValues[" + 
                    values_to_index[1][i] + "] = " + 
                    tValues[values_to_index[1][i]]);

              } catch (Exception e) {
                System.out.println(" Exception converting " + thisMT + " " + e);
              }
            }
          }
        }
      }

      try {

        if (tValues != null) tuple = new Tuple(tValues);
      } catch (visad.TypeException te) {
        // do nothing: it means they are all reals
        // tuple = new RealTuple(tValues);
        tuple = null;
      }

      domainValues.add(dValues);
      rangeValues.add(rValues);
      if (tuple != null) tupleValues.add(tuple); 
      if (isRaster) numElements = rValues.length;
    }

    int numSamples = rangeValues.size(); // # lines of data

// ***********************************************************
    if (debug) {
      try {
        System.out.println("domain size = "+domainValues.size());
        double[] dt = (double[]) domainValues.get(1);
        System.out.println("domain.array[0] = "+dt[0]);
        System.out.println("range size = "+rangeValues.size());
        System.out.println("# samples = "+numSamples);
      } catch (Exception er) {System.out.println("out range");}
    }
// ***********************************************************


    // make Linear1DSets for each possible domain component

    Linear1DSet[] lset = new Linear1DSet[numDom];
    boolean keepConstant = false;
    int numVal = numRng; 
    if (numDom == 1) numVal = numSamples;
    if (numDom == 2 && numRng == 1 && numElements > 1) numVal = numElements;
    if (numDom > 2 && numRng == 1 && numElements == 1) {
      numVal = numSamples / (2 * numDom);
      keepConstant = true;
    }

    for (int i=0; i<numDom; i++) {

      if (gotDomainRanges[i]) {
        // if domain was given with a count, use it for 'raster'-type
        if (numDom == 2 && numRng == 1 && numElements == 1) 
                                   numVal = (int) domainRanges[2][i]; 

        lset[i] = new Linear1DSet(domType.getComponent(i), domainRanges[0][i], 
                            domainRanges[1][i], numVal);

        if (debug) System.out.println("lset from domain = "+lset[i]);

      } else if (domainPointer[i] == -1 ) {
        lset[i] = new Linear1DSet(0., (double)(numVal-1), numVal);

        if (debug) System.out.println("lset from range = "+lset[i]);

      } else {
        lset[i] = null;
      }

      if (!keepConstant) numVal = numSamples; 
    }


    // now make up the actual domain sets for the function
    Set domain = null;

    if (numDom == 1) {  // for 1-D domains

      if (lset[0] == null) {
        domain = createAppropriate1DDomain(domType, numSamples, domainValues);

      } else {
        domain = lset[0];
      }

    } else if (numDom == 2) {  // for 2-D domains

      if (lset[0] != null && lset[1] != null) {
        domain = new Linear2DSet(domType, lset);

      } else {
        float[][] samples = new float[numDom][numSamples];

        for (int k = 0; k < numDom; k++) {
          if (lset[k] == null) {
            samples[k] = (getDomSamples(k, numSamples, domainValues))[0];
          } else {
            samples[k] = (lset[k].getSamples())[0];
          }

        }

        domain = (Set) new Irregular2DSet(domType, samples);
      }
        
    } else if (numDom == 3) {  // for 3-D domains
    
      if (lset[0] != null && lset[1] != null && lset[2] != null) {
        domain = new Linear3DSet(domType, lset);

      } else {
        float[][] samples = new float[numDom][numSamples];

        for (int k = 0; k < numDom; k++) {
          if (lset[k] == null) {
            samples[k] = (getDomSamples(k, numSamples, domainValues))[0];
          } else {
            samples[k] = (lset[k].getSamples())[0];
          }

        }

        domain = (Set) new Irregular3DSet(domType, samples);
      }

    } else {  // N-D domains (can only use LinearSets!!

      boolean allLinear = true;
      for (int k = 0; k<numDom; k++) {
        if (lset[k] == null) allLinear = false;
      }

      if (allLinear) {
        if (debug) System.out.println("####   Making LinearNDset");
        domain = new LinearNDSet(domType, lset);

      } else { 
        if (debug) System.out.println("####   Making IrregularSet");
        float[][] samples = new float[numDom][numSamples];

        for (int k=0; k<numDom; k++) {
          if (lset[k] == null) {
            samples[k] = (getDomSamples(k, numSamples, domainValues))[0];
          } else {
            samples[k] = (lset[k].getSamples())[0];
          }
        }

        domain = new IrregularSet(domType, samples);
      }
    }


    try {

      ff = new FlatField((FunctionType) mt, domain, 
                                null, null, rangeSets, rangeUnits);

    } catch (FieldException fe) {
      field = new FieldImpl((FunctionType) mt, domain);

    } catch (UnitException fe) {
      System.out.println("####  Problem with Units; attempting to make Field anyway");
      field = new FieldImpl((FunctionType) mt, domain);
    }
//*************************************************
    if (debug) {
      if (ff != null) {
        System.out.println("ff.Length "+ff.getLength());
        System.out.println("ff.getType "+ff.getType());
      }
      if (field != null) {
        System.out.println("field.Length "+field.getLength());
        System.out.println("field.getType "+field.getType());
      }
      System.out.println("domain = "+domain);
      System.out.println("size of a = "+numRng+" x "+(numSamples*numElements));
    }
//*************************************************

    double[][]a = new double[numRng][numSamples * numElements];
    Tuple[] at = new Tuple[numSamples];
    
    // if this is a raster then the samples are in a slightly
    // difielderent form ...

    if (isRaster) {
      int samPointer = 0;
      for (int i=0; i<numSamples; i++) {
        double[] rs = (double[])(rangeValues.get(i));
        for (int j=0; j<numElements; j++) {
          a[0][samPointer] = rs[j];
          samPointer ++;
        }
      }
    } else {
      for (int i=0; i<numSamples; i++) {
        double[] rs = (double[])(rangeValues.get(i));
        for (int j=0; j<numRng; j++) {
          a[j][i] = rs[j];
        }
        if (!tupleValues.isEmpty()) {
          at[i] = (Tuple) tupleValues.get(i); 
        }
      }
    }

// set samples
    if (debug) System.out.println("about to field.setSamples");
    try {
    if (ff != null) {
      if (debug) System.out.println("####   ff is not null");
      ff.setSamples(a, false);
      field = (Field) ff;

    } else {
      if (debug) System.out.println("####   ff is null..use FieldImpl");
      field.setSamples(at, false);
    }
    } catch (Exception ffe) {ffe.printStackTrace(); }
      

    // make up error estimates and set them
    ErrorEstimate[] es = new ErrorEstimate[numRng];
    for (int i=0; i<numRng; i++) {
      es[i] = new ErrorEstimate(a[i], rangeErrorEstimates[i], rangeUnits[i]);
    }
    try {
        ((FlatField) field).setRangeErrors(es); 
    } catch (FieldException fe) {
        if (debug) System.out.println("caught "+fe);
        // not a flatfield
        // don't setRangeErrors
    } catch (ClassCastException cce) {
        if (debug) System.out.println("caught "+cce);
        // not a flatfield
        // don't setRangeErrors
    }

    if (debug) {
      new visad.jmet.DumpType().dumpDataType(field,System.out);
      System.out.println("field = "+field);
    }

    bis.close();

  }

  // munges a pseudo MathType string into something legal

  private String makeMT(String s) {

    int k = s.indexOf("->");
    if (k < 0) {
      System.out.println("TextAdapter: invalid MathType form; -> required");
      return null;
    }

    StringBuffer sb = new StringBuffer("");
    for (int i=0; i<s.length(); i++) {
      String r = s.substring(i,i+1);
      if (!r.equals(" ") && !r.equals("\t") && !r.equals("\n")) {
              sb.append(r);
      }
    }

    String t = sb.toString();
    k = t.indexOf("->");

    if (t.charAt(k-1) != ')' ) {
      if (t.charAt(k+2) != '(' ) {
        String t2 = "("+t.substring(0,k) + ")->("+t.substring(k+2)+")";
        t = t2;
      } else {
        String t2 = "("+t.substring(0,k) + ")"+t.substring(k);
        t = t2;
      }

    } else if (t.charAt(k+2) != '(' ) {
      String t2 = t.substring(0,k+2)+"("+t.substring(k+2)+")";
      t = t2;
    }

    if (!t.startsWith("((") ) {
      String t2= "("+t+")";
      t = t2;
    }

    return t;
  }

  private static final boolean isText(String s)
  {
    final int len = (s == null ? -1 : s.length());

    if (len <= 0) {
      // well, it's not really *binary*, so pretend it's text
      return true;
    }

    for (int i = 0; i < len; i++) {
      final char ch = s.charAt(i);
      if (Character.isISOControl(ch) && !Character.isWhitespace(ch)) {
        // we might want to special-case formfeed/linefeed/newline here...
        return false;
      }
    }

    return true;
  }


  /** 
   * generate a DateTime from a string
   * @param string - Formatted date/time string
   *
   * @return - the equivalent VisAD DateTime for the string
   *
   * (lifted from au.gov.bom.aifs.common.ada.VisADXMLAdapter.java)
   */
  private static visad.DateTime makeDateTimeFromString(String string, 
                                                       String format)
    throws java.text.ParseException
  {
    visad.DateTime dt = null;
    // try to parse the string using the supplied DateTime format
    try {
      dt = visad.DateTime.createDateTime(string, format);
    } catch (VisADException e) {}
    if (dt==null) {
      throw new java.text.ParseException("Couldn't parse visad.DateTime from \""
                                          +string+"\"", -1);
    } else {
      return dt;
    }
  }

  double getVal(String s, int k) {
    int i = values_to_index[2][k];
    if (i < 0 || s == null || s.length()<1 || s.equals(hdrMissingStrings[i])) {
      return Double.NaN;
    }

    // try parsing as a double first
    if (hdrFormatStrings[i] == null) {
      // no format provided : parse as a double
      try {
        double v = Double.parseDouble(s);
        if (v == hdrMissingValues[i]) {
          return Double.NaN;
        }
        v = v * hdrScales[i] + hdrOffsets[i];
        return v;
      } catch (java.lang.NumberFormatException ne) {
        System.out.println("Invalid number format for "+s);
      }
    } else {
      // a format was specified: only support DateTime format 
      // so try to parse as a DateTime
      try{
        visad.DateTime dt = makeDateTimeFromString(s, hdrFormatStrings[i]);
        return dt.getReal().getValue();

      } catch (java.text.ParseException pe) {
        System.out.println("Invalid number/time format for "+s);
      }
    }
    return Double.NaN;
  }

  // get the samples from the ArrayList.
  float[][] getDomSamples(int comp, int numDomValues, ArrayList domValues) {
    float [][] a = new float[1][numDomValues];
    for (int i=0; i<numDomValues; i++) {
      double[] d = (double[])(domValues.get(i));
      a[0][i] = (float)d[comp];
    }
    return a;
  }

  /** get the data
  * @return a Field of the data read from the file
  *
  */
  public Field getData() {
    return field;
  }

  /**
   * Returns an appropriate 1D domain.
   *
   * @param type the math-type of the domain
   * @param numSamples the number of samples in the domain
   * @param domValues domain values are extracted from this array list.
   *
   * @return a Linear1DSet if the domain samples form an arithmetic
   *   progression, a Gridded1DDoubleSet if the domain samples are ordered
   *   but do not form an arithmetic progression, otherwise an Irregular1DSet.
   *
   * @throws VisADException there was a problem creating the domain set.
   */
  private Set createAppropriate1DDomain(MathType type, int numSamples,
                                       ArrayList domValues)
				         throws VisADException {

    if (0 == numSamples) {
      // Can't create a domain set with zero samples.
      return null;
    }

    // Extract the first element from each element of the array list.
    double[][] values = new double[1][numSamples];
    for (int i=0; i<numSamples; ++i) {
      double[] d = (double []) domValues.get(i);
      values[0][i] = d[0];
    }

    // This implementation for testing that the values are ordered
    // is based on visad.Gridded1DDoubleSet.java
    boolean ordered = true;
    boolean ascending = values[0][numSamples -1] > values[0][0];
    if (ascending) {
      for (int i=1; i<numSamples; ++i) {
        if (values[0][i] < values[0][i - 1]) {
          ordered = false;
          break;
        }
      }
    } else {
      for (int i=1; i<numSamples; ++i) {
        if (values[0][i] > values[0][i - 1]) {
          ordered = false;
          break; 
        }
      }
    }

    Set set = null;

    if (ordered) {
      ArithProg arithProg = new ArithProg();
      if (arithProg.accumulate(values[0])) {
        // The domain values form an arithmetic progression (ordered and
        // equally spaced) so use a linear set.
        set = new Linear1DSet(type, values[0][0], values[0][numSamples - 1],
          numSamples);
      } else {
        // The samples are ordered, so use a gridded set.
        set = new Gridded1DDoubleSet(type, values, numSamples);
      }
    } else {
      set = new Irregular1DSet(type, Set.doubleToFloat(values));
    }

    return set;
  }

//  uncomment to test
/*
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Must supply a filename");
      System.exit(1);
    }
    TextAdapter ta = new TextAdapter(args[0]);
    System.out.println(ta.getData().getType());
    new visad.jmet.DumpType().dumpMathType(ta.getData().getType(),System.out);
    new visad.jmet.DumpType().dumpDataType(ta.getData(),System.out);
    System.out.println("####  Data = "+ta.getData());
    System.out.println("EOF... ");
  }
*/

}
