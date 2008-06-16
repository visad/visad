//
// TextAdapter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

 private static final String ATTR_COLSPAN = "colspan";
 private static final String ATTR_VALUE   = "value";
 private static final String ATTR_OFFSET = "off";
 private static final String ATTR_ERROR  = "err";
 private static final String ATTR_SCALE = "sca";
 private static final String ATTR_POSITION ="pos";
 private static final String ATTR_FORMAT = "fmt";
 private static final String ATTR_TIMEZONE = "tz";
 private static final String ATTR_UNIT= "unit";
 private static final String ATTR_MISSING = "mis";
 private static final String ATTR_INTERVAL = "int";


  private static final String COMMA = ",";
  private static final String SEMICOLON = ";";
  private static final String TAB = "\t";
  private static final String BLANK = " ";
  private static final String BLANK_DELIM = "\\s+";



  private FlatField ff = null;
  private Field field = null;
  private boolean debug = false;
  private String DELIM;
  private boolean DOQUOTE = true;
  private boolean GOTTIME = false;


  HeaderInfo []infos;

  double[] rangeErrorEstimates;
  Unit[] rangeUnits;
  Set[] rangeSets;
  double[] domainErrorEstimates;
  Unit[] domainUnits;


  int[][] hdrColumns;
  int[][] values_to_index;


  private boolean onlyReadOneLine = false;


  /** Create a VisAD FlatField from a local Text (comma-, tab- or 
    * blank-separated values) ASCII file
    * @param filename name of local file.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public TextAdapter(String filename) throws IOException, VisADException {
    this(filename, null, null);
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
    DELIM = getDelimiter(filename);
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
    this(url, null, null);
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
    DELIM = getDelimiter(url.getFile());
    InputStream is = url.openStream();
    readit(is, map, params);
  }


  /** Create a VisAD FlatField from a local Text (comma-, tab- or 
    * blank-separated values) ASCII file
    * @param inputStream The input stream to read from
    * @param delimiter the delimiter
    * @param map the VisAD "MathType" as a string defining the FlatField
    * @param params the list of parameters used to define what columns
    *  of the text file correspond to what MathType parameters.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public TextAdapter(InputStream inputStream, String delimiter, String map, String params) 
                         throws IOException, VisADException {
      this(inputStream, delimiter, map,params,false);
  }


  /** Create a VisAD FlatField from a local Text (comma-, tab- or 
    * blank-separated values) ASCII file
    * @param inputStream The input stream to read from
    * @param delimiter the delimiter
    * @param map the VisAD "MathType" as a string defining the FlatField
    * @param params the list of parameters used to define what columns
    *  of the text file correspond to what MathType parameters.
    * @param onlyReadOneLine If true then only read one line of data. This is used so client code can
    * read the meta data.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */

  public TextAdapter(InputStream inputStream, String delimiter, String map, String params,boolean onlyReadOneLine) 
                         throws IOException, VisADException {
    this.onlyReadOneLine = onlyReadOneLine;
    DELIM = delimiter;
    readit(inputStream, map, params);
  }


  public static  String getDelimiter(String filename) {
    if(filename == null) return null;
    filename = filename.trim().toLowerCase();
    if (filename.endsWith(".csv")) return COMMA;
    if (filename.endsWith(".tsv")) return TAB;
    if (filename.endsWith(".bsv")) return BLANK;    
    return null;
  }


  /**
   * Is the given text line a comment
   *
   * @return is it a comment line
   */
  public static boolean isComment(String line) {
    return (line.startsWith("#") || 
            line.startsWith("!") || 
            line.startsWith("%") || 
            line.length() < 1);
  }

    public static  String readLine(BufferedReader bis) 
        throws IOException {
      while (true) {
        String line = bis.readLine();
        if (line == null) return null;
        if (!isText(line)) return null;
        if (isComment(line)) continue;
        return line;
      }
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
      maps = readLine(bis);
      if(maps != null) {
          maps = maps.trim();
      }
    } else {
      maps = map;
    }

    if (maps != null) {
       maps = makeMT(maps);
    }
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
      hdr = readLine(bis);
    } else {
      hdr = params;
    }

    String hdrDelim = DELIM;
    if (DELIM == null) {
      if (hdr.indexOf(BLANK) != -1) hdrDelim = BLANK_DELIM; 
      if (hdr.indexOf(COMMA) != -1) hdrDelim = COMMA; 
      if (hdr.indexOf(SEMICOLON) != -1) hdrDelim = SEMICOLON; 
      if (hdr.indexOf(TAB) != -1) hdrDelim = TAB; 

      if (debug) System.out.println("Using header delimiter = "+
                                     (hdrDelim.getBytes())[0]);
    }

    String[] sthdr = hdr.split(hdrDelim);
    int nhdr = sthdr.length;
    infos    = new HeaderInfo[nhdr];
    for(int i=0;i<infos.length;i++) {
      infos[i] = new HeaderInfo();
    }
    Real[] prototypeReals = new Real[nhdr];
    hdrColumns = new int[2][nhdr];
    int numHdrValues=0;

    // pre-scan of the header names to seek out Units
    // since we cannot change a RealType once it's defined!!

    for (int i=0; i<nhdr; i++) {
      String name = sthdr[i].trim();
      String hdrUnitString = null;
      hdrColumns[0][i] = -1; // indicating no fixed columns
      
      int m = name.indexOf("[");

      if (m == -1) {
          infos[i].name = name;
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
          infos[i].name = name.substring(0,m).trim();
        } else {
          infos[i].name = (name.substring(0,m)+name.substring(m2+1)).trim();
        }

        String cl = name.substring(m+1,m2).trim();
        String[] stcl = cl.split(BLANK_DELIM);
        int ncl = stcl.length;

        if (ncl == 1 && cl.indexOf("=") == -1) {
          hdrUnitString = cl;  // backward compatible...

        } else {
          for (int l = 0; l  < ncl; l++) {
            String s = stcl[l];
            String[] sts = s.split("=");
            if (sts.length != 2) {
              throw new VisADException("TextAdapter: Invalid clause in: "+s);
            }
            String tok = sts[0];
            String val = sts[1];
            
            // check for quoted strings
            if (val.startsWith("\"")) {

              // see if ending quote also fetched
              if (val.endsWith("\"")) {
                String v2 = val.substring(1,val.length()-1);
                val = v2;

              } else {
                // if not, then reparse stcl to suck up spaces...
                try {
                  String v2="";
                  for (int q=l+1; q < ncl; q++) {
                      String  vTmp = stcl[q];
                      // find next token that has a " in it
                      int pos = vTmp.indexOf("\"");
                      l++;
                      if (pos < 0) {  // no "
                          v2 = v2+" "+vTmp;
                      } else {
                          v2 = v2+" "+vTmp.substring(0,pos);
                          break;
                      }
                  }
                  String v3 = val.substring(1)+v2;
                  val = v3;

                //} catch (NoSuchElementException nse2) {
                } catch (ArrayIndexOutOfBoundsException nse2) {
                  val="";
                }
              }
            }

            if (debug) System.out.println("####   tok = "+tok+ " val = '"+val+"'");

            if (tok.toLowerCase().startsWith(ATTR_UNIT)) {
              hdrUnitString = val;

            } else if (tok.toLowerCase().startsWith(ATTR_MISSING)) {
                infos[i].missingString = val.trim();
              try {
                infos[i].missingValue = Double.parseDouble(val);
              } catch (java.lang.NumberFormatException me) {
                  infos[i].missingValue = Double.NaN;
              }
            } else if (tok.toLowerCase().startsWith(ATTR_INTERVAL)) {
              infos[i].isInterval = -1;
              if (val.toLowerCase().startsWith("t")) infos[i].isInterval = 1;
              if (val.toLowerCase().startsWith("f")) infos[i].isInterval = 0;
              if (infos[i].isInterval == -1) {
                throw new VisADException("TextAdapter: Value of \'interval\' must be \'true\' or \'false\'");
              }
            } else if (tok.toLowerCase().startsWith(ATTR_ERROR)) {
                infos[i].errorEstimate = Double.parseDouble(val);
            } else if (tok.toLowerCase().startsWith(ATTR_SCALE)) {
                infos[i].scale = Double.parseDouble(val);
            } else if (tok.toLowerCase().startsWith(ATTR_OFFSET)) {
              infos[i].offset = Double.parseDouble(val);
            } else if (tok.toLowerCase().startsWith(ATTR_VALUE)) {
              infos[i].fixedValue = val.trim();
              numHdrValues++;
            } else if (tok.toLowerCase().startsWith(ATTR_COLSPAN)) {
              infos[i].colspan = (int)Double.parseDouble(val.trim());
            } else if (tok.toLowerCase().startsWith(ATTR_POSITION)) {
              String[] stp = val.split(":");
              if (stp.length != 2) {
                throw new VisADException("TextAdapter: invalid Position parameter in:"+s);
              }
              hdrColumns[0][i] = Integer.parseInt(stp[0].trim());
              hdrColumns[1][i] = Integer.parseInt(stp[1].trim());

            } else if (tok.toLowerCase().startsWith(ATTR_FORMAT)) {
                infos[i].formatString = val.trim();
            } else if (tok.toLowerCase().startsWith(ATTR_TIMEZONE)) {
                infos[i].tzString = val.trim();
            } else {
              throw new VisADException("TextAdapter: invalid token name: "+s);
            }

          }
        }

      }


      if (debug) 
            System.out.println("hdr name = "+infos[i]+" units="+
             hdrUnitString+
             " miss="+infos[i].missingValue+" interval="+infos[i].isInterval+ 
             " errorest="+infos[i].errorEstimate+" scale="+infos[i].scale+
             " offset="+infos[i].offset+" pos="+hdrColumns[0][i]+":"+
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


      String rttemp = infos[i].name.trim();
      if (rttemp.indexOf("(Text)") == -1) {

        int parenIndex = rttemp.indexOf("(");

        if (parenIndex < 0) parenIndex = rttemp.indexOf("[");
        if (parenIndex < 0) parenIndex = rttemp.indexOf("{");
        if (parenIndex < 0) parenIndex = rttemp.indexOf(" ");
        String rtname = parenIndex < 0 ? rttemp.trim() : rttemp.substring(0,parenIndex);


        RealType rt = RealType.getRealType(rtname, u, null, infos[i].isInterval);

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

      infos[i].unit = u;
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

      String name = infos[i].name;


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
          domainErrorEstimates[k] = infos[i].errorEstimate;
          domainUnits[k] = infos[i].unit;
          gotName = true;
          countDomain ++;
          // now see if a list is given...
          if (n != -1) {

            try {

              String ss = name.substring(n+1,name.length()-1);
              String[] sct = ss.split(":");
              String first = sct[0].trim();
              String second = sct[1].trim();
              String third = "1";
              if (sct.length == 3) third = sct[2].trim();
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
        rangeErrorEstimates[k] = infos[i].errorEstimate;
        rangeUnits[k] = infos[i].unit;
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
    boolean tryToMakeTuple = true;
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

    int index;
    while (true) {
      String s = bis.readLine();
      if (debug) System.out.println("read:"+s);
      if (s == null) break;
      if (!isText(s)) return;
      if (isComment(s)) continue;
      if (dataDelim == null) {
        if (s.indexOf(BLANK) != -1) dataDelim = BLANK_DELIM; 
        if (s.indexOf(COMMA) != -1) dataDelim = COMMA; 
        if (s.indexOf(SEMICOLON) != -1) dataDelim = SEMICOLON; 
        if (s.indexOf(TAB) != -1) dataDelim = TAB; 

        if (debug) System.out.println("Using data delimiter = "+
                                       (dataDelim.getBytes())[0]);
      }

      
      if((index=s.indexOf("="))>=0) {
        String name  = s.substring(0,index).trim();
        String value  = s.substring(index+1).trim();
        boolean foundIt = false;
        for(int paramIdx=0;paramIdx<infos.length;paramIdx++) {
            if(infos[paramIdx].isParam(name)) {
                if(infos[paramIdx].fixedValue==null) {
                    numHdrValues++;
                }
                infos[paramIdx].fixedValue = value;
                foundIt = true;
                break;
            }
        }
        if(!foundIt) {
           throw new VisADException(
                    "TextAdapter: Cannot find field with name:" +name +" from line:" + s);
        }
        continue;
      }



      String[] st = s.split(dataDelim);
      int n = st.length;
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

          String sa = st[i];
          
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
        n +=numHdrValues;

        int l = 0;   // token counter
        for (int i=0; i<nhdr; i++) {   // loop over the columns
          String sa;
          if(infos[i].fixedValue!=null) {
            sa = infos[i].fixedValue;
          }  else if (l >= st.length) {   // more params than tokens
            sa = "";                    // need to have a missing value
          } else {
            sa = st[l++].trim();
            int moreColumns = infos[i].colspan-1;
            while (moreColumns>0) {
                sa = sa + " " + st[l++].trim();
                moreColumns--;
            }
          }

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
                  // TODO:  work on this
                  try {
                    String delim = 
                        dataDelim.equals(BLANK_DELIM) ? BLANK : dataDelim;
                    String sa2="";
                    for (int q=l; q < st.length; q++) {
                        String  saTmp = st[q];
                        // find next token that has a " in it
                        int pos = saTmp.indexOf("\"");
                        l++;
                        if (pos < 0) {  // no dataDelim
                            sa2 = sa2+delim+saTmp;
                        } else {
                            sa2 = sa2+saTmp.substring(0,pos);
                            //st[l] = saTmp.substring(pos+1);
                            break;
                        }
                    }

                    //sThisText = sa.substring(1)+sa2;
                    sThisText = sa.substring(1)+delim+sa2;
                  //} catch (NoSuchElementException nse) {
                  } catch (ArrayIndexOutOfBoundsException nse) {
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
              double value = getVal(sa,i);
              rValues[values_to_index[1][i]] = value;
              try {
                  if(prototypeReals[i]==null) {
                      prototypeReals[i] =    new Real((RealType) thisMT, getVal(sa,i), infos[i].unit);
                  }
                  tValues[values_to_index[1][i]] = 
                      prototypeReals[i].cloneButValue(value);
                  if(debug)System.out.println("tValues[" + 
                    values_to_index[1][i] + "] = " + 
                    tValues[values_to_index[1][i]]);

              } catch (Exception e) {
                System.out.println(" Exception converting " + thisMT + " " + e);
              }
            }
          }
        }
      }

      if(tryToMakeTuple) {
        try {
          if (tValues != null) tuple = new Tuple(tValues);
        } catch (visad.TypeException te) {
          // do nothing: it means they are all reals
          // tuple = new RealTuple(tValues);
          tuple = null;
          tryToMakeTuple = false; 
        } catch(NullPointerException npe) {
            for(int i=0;i<tValues.length;i++) {
                if(tValues[i] == null) {
                    throw new IllegalArgumentException("An error occurred reading column number:" + (i+1));
                }
            }
            throw npe;
        }
      }

      domainValues.add(dValues);
      rangeValues.add(rValues);
      if (tuple != null) tupleValues.add(tuple); 
      if (isRaster) numElements = rValues.length;

      if(onlyReadOneLine) break;

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
        //      System.out.println("TextAdapter: invalid MathType form; -> required");
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
                                                       String format, String tz)
    throws java.text.ParseException
  {
    visad.DateTime dt = null;
    // try to parse the string using the supplied DateTime format
    try {
      if(dateParsers!=null) {
         for(int i=0;i<dateParsers.size();i++) {
             DateParser dateParser = (DateParser) dateParsers.get(i);
             dt = dateParser.createDateTime(string, format, TimeZone.getTimeZone(tz));
             if(dt !=null) {
                 return dt;
             }
          }
       }
       dt = visad.DateTime.createDateTime(string, format, TimeZone.getTimeZone(tz));
    } catch (VisADException e) {}
    if (dt==null) {
      throw new java.text.ParseException("Couldn't parse visad.DateTime from \""
                                          +string+"\"", -1);
    } else {
      return dt;
    }
  }

  /** This list of DateFormatter-s will be checked when we are making a DateTime wiht a given format */
  private static List dateParsers;

  /** used to allow applications to define their own date parsing */
  public static interface DateParser {
        /** If this particular DateParser does not know how to handle the give  format then this method should return null */
      public DateTime createDateTime(String value, String format, TimeZone timezone) throws VisADException;
  }


  /** used to allow applications to define their own date parsing */
  public static void addDateParser(DateParser dateParser) {
      if(dateParsers==null) {
          dateParsers  = new ArrayList();
      }
      dateParsers.add(dateParser);
  }



  double getVal(String s, int k) {
    int i = values_to_index[2][k];
    if (i < 0 || s == null || s.length()<1 || s.equals(infos[i].missingString)) {
      return Double.NaN;
    }

    // try parsing as a double first
    if (infos[i].formatString == null) {
      // no format provided : parse as a double
      try {
        double v;
        try {
          v = Double.parseDouble(s);
        } catch (java.lang.NumberFormatException nfe1) {
            //If units are degrees then try to decode this as a lat/lon
            // We should probably not rely on throwing an exception to handle this but...
            if(infos[i].unit !=null && Unit.canConvert(infos[i].unit, visad.CommonUnit.degree)) {
                v=decodeLatLon(s);
            } else {
                throw nfe1;
            }
            if(v!=v) throw new java.lang.NumberFormatException(s);
        }
        if (v == infos[i].missingValue) {
          return Double.NaN;
        }
        v = v * infos[i].scale + infos[i].offset;
        return v;
      } catch (java.lang.NumberFormatException ne) {
        System.out.println("Invalid number format for "+s);
      }
    } else {
      // a format was specified: only support DateTime format 
      // so try to parse as a DateTime
      try{
        visad.DateTime dt = makeDateTimeFromString(s, infos[i].formatString, infos[i].tzString);
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


    private static class HeaderInfo {
        String  name;
        Unit    unit;
        double  missingValue = Double.NaN;
        String  missingString;
        String  formatString;
        String  tzString = "GMT";
        int     isInterval = 0;
        double  errorEstimate=0;
        double  scale=1.0;
        double  offset=0.0;
        String  fixedValue;
        int     colspan = 1;

        public boolean isParam(String param) {
            return name.equals(param)  || name.equals(param+"(Text)");
        }
        public String toString() {
            return name;
        }


    }



//  uncomment to test


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




    /**
     * A cut-and-paste from the IDV Misc method
     * Decodes a string representation of a latitude or longitude and
     * returns a double version (in degrees).  Acceptible formats are:
     * <pre>
     * +/-  ddd:mm, ddd:mm:, ddd:mm:ss, ddd::ss, ddd.fffff ===>   [+/-] ddd.fffff
     * +/-  ddd, ddd:, ddd::                               ===>   [+/-] ddd
     * +/-  :mm, :mm:, :mm:ss, ::ss, .fffff                ===>   [+/-] .fffff
     * +/-  :, ::                                          ===>       0.0
     * Any of the above with N,S,E,W appended
     * </pre>
     *
     * @param latlon  string representation of lat or lon
     * @return the decoded value in degrees
     */
    public static double decodeLatLon(String latlon) {
        // first check to see if there is a N,S,E,or W on this
        latlon = latlon.trim();
        int    dirIndex    = -1;
        int    southOrWest = 1;
        double value       = Double.NaN;
        if (latlon.indexOf("S") > 0) {
            southOrWest = -1;
            dirIndex    = latlon.indexOf("S");
        } else if (latlon.indexOf("W") > 0) {
            southOrWest = -1;
            dirIndex    = latlon.indexOf("W");
        } else if (latlon.indexOf("N") > 0) {
            dirIndex = latlon.indexOf("N");
        } else if (latlon.indexOf("E") > 0) {
            dirIndex = latlon.indexOf("E");
        }
        if (dirIndex > 0) {
            latlon = latlon.substring(0, dirIndex).trim();
        }

        // now see if this is a negative value
        if (latlon.indexOf("-") == 0) {
            southOrWest *= -1;
            latlon      = latlon.substring(latlon.indexOf("-") + 1).trim();
        }

        if (latlon.indexOf(":") >= 0) {  //have something like DD:MM:SS, DD::, DD:MM:, etc
            int    firstIdx = latlon.indexOf(":");
            String hours    = latlon.substring(0, firstIdx);
            String minutes  = latlon.substring(firstIdx + 1);
            String seconds  = "";
            if (minutes.indexOf(":") >= 0) {
                firstIdx = minutes.indexOf(":");
                String temp = minutes.substring(0, firstIdx);
                seconds = minutes.substring(firstIdx + 1);
                minutes = temp;
            }
            try {

                value = (hours.equals("") == true)
                        ? 0
                        : Double.parseDouble(hours);
                if ( !minutes.equals("")) {
                    value += Double.parseDouble(minutes) / 60.;
                }
                if ( !seconds.equals("")) {
                    value += Double.parseDouble(seconds) / 3600.;
                }
            } catch (NumberFormatException nfe) {
                value = Double.NaN;
            }
        } else {  //have something like DD.ddd
            try {
                value = Double.parseDouble(latlon);
            } catch (NumberFormatException nfe) {
                value = Double.NaN;
            }
        }
        return value * southOrWest;
    }


}
