
//
// BasicSSCell.java
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

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.rmi.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import visad.*;
import visad.data.*;
import visad.data.netcdf.Plain;
import visad.data.visad.VisADForm;
import visad.formula.*;
import visad.java2d.*;
import visad.java3d.*;

/** BasicSSCell represents a single spreadsheet display cell.  BasicSSCells
    can be added to a VisAD user interface to provide some of the capabilities
    presented in the VisAD SpreadSheet program.  Other capabilities, like the
    file loader and data mapping dialog boxes, are available only with a
    FancySSCell.<P> */
public class BasicSSCell extends JPanel {

  /** list of SSCells on this JVM */
  static Vector SSCellVector = new Vector();

  /** counter for the number of cells currently saving data */
  static int Saving = 0;

  /** name of this BasicSSCell */
  String Name;

  /** URL from where data was imported, if any */
  URL Filename = null;

  /** RMI address from where data was imported, if any */
  String RMIAddress = null;

  /** formula of this BasicSSCell, if any */
  String Formula = "";

  /** BasicSSCell's associated VisAD Display */
  DisplayImpl VDisplay;

  /** BasicSSCell's associated VisAD DataReference */
  DataReferenceImpl DataRef;

  /** BasicSSCell's associated VisAD RemoteDataReference */
  RemoteDataReferenceImpl RemoteDataRef;

  /** BasicSSCell's associated VisAD DisplayPanel */
  JPanel VDPanel;

  /** constant for use with Dimension2D variable */
  static final int JAVA3D_3D = 1;

  /** constant for use with Dimension2D variable */
  static final int JAVA2D_2D = 2;

  /** constant for use with Dimension2D variable */
  static final int JAVA3D_2D = 3;

  /** FormulaManager object used by all BasicSSCells with formulas */
  static final FormulaManager fm = createManager();

  /** creates the global FormulaManager object */
  private static FormulaManager createManager() {
    String[] binOps = {".", "^", "*", "/", "%", "+", "-"};
    int[] binPrec =   {200, 400, 600, 600, 600, 800, 800};
    String[] binMethods = {"visad.ss.BasicSSCell.dot(visad.Tuple, visad.Real)",
                           "visad.Data.pow(visad.Data)",
                           "visad.Data.multiply(visad.Data)",
                           "visad.Data.divide(visad.Data)",
                           "visad.Data.remainder(visad.Data)",
                           "visad.Data.add(visad.Data)",
                           "visad.Data.subtract(visad.Data)"};
    String[] unaryOps = {"-"};
    int[] unaryPrec =   {500};
    String[] unaryMethods = {"visad.Data.negate()"};
    String[] functions = {"abs", "acos", "acosDegrees", "asin", "asinDegrees",
                          "atan", "atan2", "atanDegrees", "atan2Degrees",
                          "ceil", "combine", "cos", "cosDegrees", "derive",
                          "domainMultiply", "domainFactor", "exp", "extract",
                          "floor", "getSample", "linkx", "log", "max", "min",
                          "negate", "rint", "round", "sin", "sinDegrees",
                          "sqrt", "tan", "tanDegrees"};
    String[] funcMethods = {"visad.Data.abs()", "visad.Data.acos()",
                            "visad.Data.acosDegrees()", "visad.Data.asin()",
                            "visad.Data.asinDegrees()", "visad.Data.atan()",
                            "visad.Data.atan2(visad.Data)",
                            "visad.Data.atanDegrees()",
                            "visad.Data.atan2Degrees(visad.Data)",
                            "visad.Data.ceil()",
                            "visad.FieldImpl.combine(visad.Field[])",
                            "visad.Data.cos()", "visad.Data.cosDegrees()",
                            "visad.ss.BasicSSCell.derive(visad.Function," +
                                                        "visad.ss.SSRealType)",
                            "visad.FieldImpl.domainMultiply()",
                            "visad.ss.BasicSSCell.factor(visad.FieldImpl,"
                                                       +"visad.ss.SSRealType)",
                            "visad.Data.exp()",
                            "visad.ss.BasicSSCell.extract(visad.Field," +
                                                         "visad.Real)",
                            "visad.Data.floor()",
                            "visad.ss.BasicSSCell.brackets(visad.Field," +
                                                          "visad.Real)",
                            "visad.ss.BasicSSCell.link(visad.ss.SSMethod," +
                                                      "java.lang.Object[])",
                            "visad.Data.log()",
                            "visad.Data.max(visad.Data)",
                            "visad.Data.min(visad.Data)",
                            "visad.Data.negate()", "visad.Data.rint()",
                            "visad.Data.round()", "visad.Data.sin()",
                            "visad.Data.sinDegrees()", "visad.Data.sqrt()",
                            "visad.Data.tan()", "visad.Data.tanDegrees()"};
    int implicitPrec = 200;
    String[] implicitMethods = {"visad.ss.BasicSSCell.implicit(" +
                                "visad.Function, visad.Real)",
                                "visad.Function.evaluate(visad.RealTuple)"};
    FormulaManager f;
    try {
      f = new FormulaManager(binOps, binPrec, binMethods, unaryOps, unaryPrec,
                             unaryMethods, functions, funcMethods,
                             implicitPrec, implicitMethods);
    }
    catch (FormulaException exc) {
      return null;
    }
    return f;
  }

  /** whether the DisplayPanel is 2-D or 3-D, Java2D or Java3D */
  int Dimension2D = -1;

  /** this BasicSSCell's DisplayListener */
  DisplayListener DListen = null;

  /** whether the BasicSSCell contains any data */
  boolean HasData = false;

  /** whether the display panel is currently onscreen */
  boolean HasDisplay = false;

  /** whether the BasicSSCell has an associated formula */
  boolean HasFormula = false;

  /** whether the BasicSSCell has mappings from Data to Display */
  boolean HasMappings = false;

  /** whether formula errors are reported in a dialog box */
  boolean ShowFormulaErrors = true;

  /** whether this cell has a big X through it */
  boolean BigX = false;

  /** construct a new BasicSSCell with the given name */
  public BasicSSCell(String name) throws VisADException, RemoteException {
    if (name == null) {
      throw new VisADException("BasicSSCell: name cannot be null");
    }
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (name.equalsIgnoreCase(panel.Name)) {
        throw new VisADException("BasicSSCell: name already used");
      }
    }
    Name = name;
    SSCellVector.addElement(this);
    CellImpl ucell = new CellImpl() {
      public void doAction() {
        // redisplay this cell's data when it changes
        Data value = null;
        try {
          value = (Data) fm.getThing(Name);
        }
        catch (ClassCastException exc) {
          if (ShowFormulaErrors) {
            showMsg("Formula evaluation error",
                    "Final value is not of the correct type.");
          }
        }
        catch (FormulaException exc) {
          if (ShowFormulaErrors) {
            showMsg("Formula evaluation error",
                    "The formula could not be evaluated.");
          }
        }

        if (value == null) {
          // no value; clear display
          HasData = false;
          try {
            clearDisplay();
          }
          catch (VisADException exc) {
            if (ShowFormulaErrors) {
              showMsg("Formula evaluation error",
                      "Unable to clear old data.");
            }
            setX(true);
          }
          catch (RemoteException exc) {
            if (ShowFormulaErrors) {
              showMsg("Formula evaluation error",
                      "Unable to clear old data.");
            }
            setX(true);
          }
        }
        else {
          // update cell's data
          HasData = true;
          setX(false);
          if (!HasDisplay) {
            add(VDPanel);
            validate();
            HasDisplay = true;
          }
        }
        String[] es = fm.getErrors(Name);
        if (ShowFormulaErrors && es != null) {
          for (int i=0; i<es.length; i++) {
            showMsg("Formula evaluation error", es[i]);
          }
          setX(true);
        }
        notifyListeners(SSCellChangeEvent.DATA_CHANGE);
      }
    };
    DataRef = new DataReferenceImpl(name);
    RemoteDataRef = new RemoteDataReferenceImpl(DataRef);
    fm.createVar(Name, DataRef);
    ucell.addReference(DataRef);
    setDimension(JAVA2D_2D);
    VDPanel = (JPanel) VDisplay.getComponent();
    setPreferredSize(new Dimension(0, 0));
    setBackground(Color.black);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
  }

  /** list of SSCellListeners to be notified of changes */
  private Vector list = new Vector();

  /** add an SSCellListener to be notified of changes */
  public void addSSCellChangeListener(SSCellListener l) {
    if (!list.contains(l)) list.add(l);
  }

  /** remove an SSCellListener */
  public void removeListener(SSCellListener l) {
    if (list.contains(l)) list.remove(l);
  }

  /** remove all SSCellListeners */
  public void removeAllListeners() {
    list.removeAllElements();
  }

  /** notify SSCellListeners that change occurred */
  private void notifyListeners(int changeType) {
    SSCellChangeEvent e = new SSCellChangeEvent(this, changeType);
    for (int i=0; i<list.size(); i++) {
      SSCellListener l = (SSCellListener) list.elementAt(i);
      l.ssCellChanged(e);
    }
  }

  /** construct a BasicSSCell with the given name and data string */
  public BasicSSCell(String name, String info) throws VisADException,
                                                      RemoteException {
    this(name);
    if (info != null) setSSCellString(info);
  }

  /** return the BasicSSCell object with the specified display */
  public static BasicSSCell getSSCellByDisplay(Display d) {
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (d == (Display) panel.VDisplay) return panel;
    }
    return null;
  }

  /** return the BasicSSCell object with the specified name */
  public static BasicSSCell getSSCellByName(String name) {
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (name.equalsIgnoreCase(panel.Name)) return panel;
    }
    return null;
  }

  /** obtain a Vector of RealTypes consisting of all ScalarTypes
      present in data's MathType; return the number of duplicate
      ScalarTypes found */
  public static int getRealTypes(Data data, Vector v) {
    MathType dataType;
    try {
      dataType = data.getType();
    }
    catch (RemoteException exc) {
      return -1;
    }
    catch (VisADException exc) {
      return -1;
    }
    int[] i = new int[1];
    i[0] = 0;

    if (dataType instanceof FunctionType) {
      parseFunction((FunctionType) dataType, v, i);
    }
    else if (dataType instanceof SetType) {
      parseSet((SetType) dataType, v, i);
    }
    else if (dataType instanceof TupleType) {
      parseTuple((TupleType) dataType, v, i);
    }
    else parseScalar((ScalarType) dataType, v, i);

    return i[0];
  }

  /** used by getRealTypes */
  private static void parseFunction(FunctionType mathType, Vector v, int[] i) {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    parseTuple((TupleType) domain, v, i);

    // extract range
    MathType range = mathType.getRange();
    if (range instanceof FunctionType) {
      parseFunction((FunctionType) range, v, i);
    }
    else if (range instanceof SetType) {
      parseSet((SetType) range, v, i);
    }
    else if (range instanceof TupleType) {
      parseTuple((TupleType) range, v, i);
    }
    else parseScalar((ScalarType) range, v, i);

    return;
  }

  /** used by getRealTypes */
  private static void parseSet(SetType mathType, Vector v, int[] i) {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    parseTuple((TupleType) domain, v, i);

    return;
  }

  /** used by getRealTypes */
  private static void parseTuple(TupleType mathType, Vector v, int[] i) {
    // extract components
    for (int j=0; j<mathType.getDimension(); j++) {
      MathType cType = null;
      try {
        cType = mathType.getComponent(j);
      }
      catch (VisADException exc) { }

      if (cType != null) {
        if (cType instanceof FunctionType) {
          parseFunction((FunctionType) cType, v, i);
        }
        else if (cType instanceof SetType) {
          parseSet((SetType) cType, v, i);
        }
        else if (cType instanceof TupleType) {
          parseTuple((TupleType) cType, v, i);
        }
        else parseScalar((ScalarType) cType, v, i);
      }
    }
    return;
  }

  /** used by getRealTypes */
  private static void parseScalar(ScalarType mathType, Vector v, int[] i) {
    if (mathType instanceof RealType) {
      if (v.contains(mathType)) i[0]++;
      v.addElement(mathType);
    }
  }

  /** return true if any BasicSSCell is currently saving data */
  public static boolean isSaving() {
    return Saving > 0;
  }

  /** evaluates the dot operator */
  public static Data dot(Tuple t, Real r) {
    Data d = null;
    try {
      d = t.getComponent((int) r.getValue());
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return d;
  }

  /** evaluate the derive function */
  public static Data derive(Function f, SSRealType rt) {
    Data val = null;
    try {
      val = f.derivative(rt.getRealType(), Data.NO_ERRORS);
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return val;
  }

  /** evaluate the domainFactor function */
  public static Field factor(FieldImpl f, SSRealType rt) {
    Field val = null;
    try {
      val = f.domainFactor(rt.getRealType());
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return val;
  }

  /** evaluate the extract function */
  public static Data extract(Field f, Real r) {
    Data d = null;
    try {
      d = f.extract((int) r.getValue());
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return d;
  }

  /** evaluate the link function */
  public static Data link(SSMethod m, Object[] o) {
    Data ans = null;
    if (o != null) {
      for (int i=0; i<o.length; i++) {
        // convert SSRealTypes to RealTypes
        if (o[i] instanceof SSRealType) {
          o[i] = ((SSRealType) o[i]).getRealType();
        }
      }
    }
    try {
      ans = (Data) FormulaUtil.invokeMethod(m.getMethod(), o);
    }
    catch (ClassCastException exc) { }
    catch (IllegalAccessException exc) { }
    catch (IllegalArgumentException exc) { }
    catch (InvocationTargetException exc) { }
    return ans;
  }

  /** evaluate implicit function syntax; e.g., A1(5) or A1(A2) */
  public static Data implicit(Function f, Real r) {
    Data value = null;
    try {
      value = f.evaluate(r);
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return value;
  }

  /** evaluate the bracket function; e.g., A1[5] or A1[A2] */
  public static Data brackets(Field f, Real r) {
    Data value = null;
    try {
      RealType rt = (RealType) r.getType();
      value = f.getSample((int) r.getValue());
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return value;
  }

  /** number of link variables that have been created */
  private static int linkNum = 0;

  /** do some pre-computation parsing to a formula */
  private static String preParse(String f) {
    // remove spaces
    StringTokenizer t = new StringTokenizer(f, " ", false);
    String s = "";
    while (t.hasMoreTokens()) s = s + t.nextToken();
    if (s.equals("")) return s;

    // multi-pass pre-parse sequence
    String os;
    do {
      os = s;
      s = preParseOnce(os);
    }
    while (!s.equals(os));
    return s;
  }

  /** used by preParse */
  private static String preParseOnce(String s) {
    // convert to lower case
    String l = s.toLowerCase();

    // scan entire string
    int len = l.length();
    boolean letter = false;
    String ns = "";
    for (int i=0; i<len; i++) {
      if (!letter && i < len - 1 && l.substring(i, i+2).equals("d(")) {
        // convert d(x)/d(y) notation to standard derive(x, y) notation
        i += 2;
        int s1 = i;
        for (int paren=1; paren>0; i++) {
          // check for correct syntax
          if (i >= len) return s;
          char c = l.charAt(i);
          if (c == '(') paren++;
          if (c == ')') paren--;
        }
        int e1 = i-1;
        // check for correct syntax
        if (i > len - 3 || !l.substring(i, i+3).equals("/d(")) return s;
        i += 3;
        int s2 = i;
        for (int paren=1; paren>0; i++) {
          // check for correct syntax
          if (i >= len) return s;
          char c = l.charAt(i);
          if (c == '(') paren++;
          if (c == ')') paren--;
        }
        int e2 = i-1;
        ns = ns + "derive(" + s.substring(s1, e1) +
                        "," + s.substring(s2, e2) + ")";
        i--;
      }
      else if (!letter && i < len - 4 && l.substring(i, i+5).equals("link(")) {
        // evaluate link(code) notation and replace with link variable
        i += 5;
        int s1 = i;
        try {
          while (l.charAt(i) != '(') i++;
        }
        catch (ArrayIndexOutOfBoundsException exc) {
          // incorrect syntax
          return s;
        }
        i++;
        int e1 = i-1;
        int s2 = i;
        for (int paren=2; paren>1; i++) {
          // check for correct syntax
          if (i >= len) return s;
          char c = l.charAt(i);
          if (c == '(') paren++;
          if (c == ')') paren--;
        }
        int e2 = i-1;
        // check for correct syntax
        if (i >= len || l.charAt(i) != ')') return s;
        String[] strs = new String[1];
        strs[0] = s.substring(s1, e1) + "(";

        // parse method's arguments; determine if they are Data or RealType
        String sub = s.substring(s2, e2);
        StringTokenizer st = new StringTokenizer(sub, ",", false);
        boolean first = true;
        while (st.hasMoreTokens()) {
          String token = st.nextToken();
          if (first) first = false;
          else strs[0] = strs[0] + ",";
          RealType rt = RealType.getRealTypeByName(token);
          strs[0] = strs[0] + (rt == null ? "visad.Data"
                                          : "visad.RealType");
        }
        strs[0] = strs[0] + ")";

        // obtain Method object and store it in a link variable
        Method[] meths = FormulaUtil.stringsToMethods(strs);
        String link = "link" + (++linkNum);
        // make sure linked method actually exists
        if (meths[0] == null) return s;
        try {
          fm.setThing(link, new SSMethod(meths[0]));
        }
        // catch any errors setting the link variable
        catch (FormulaException exc) {
          return s;
        }
        catch (VisADException exc) {
          return s;
        }
        catch (RemoteException exc) {
          return s;
        }
        ns = ns + "linkx(" + link + "," + s.substring(s2, e2) + ")";
      }
      else if (!letter) {
        int j = i;
        char c = l.charAt(j++);
        while (j < len && ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))) {
          c = l.charAt(j++);
        }
        // check for end-of-string
        if (j == len) return ns + s.substring(i, len);
        if (c == '[') {
          // convert x[y] notation to standard getSample(x, y) notation
          int k = j;
          for (int paren=1; paren>0; k++) {
            // check for correct syntax
            if (k >= len) return s;
            c = l.charAt(k);
            if (c == '[') paren++;
            if (c == ']') paren--;
          }
          ns = ns + "getSample(" + s.substring(i, j-1) +
                             "," + s.substring(j, k-1) + ")";
          i = k-1;
        }
        else ns = ns + s.charAt(i);
      }
      else {
        // append character to new string
        ns = ns + s.charAt(i);
      }
      char c = (i < len) ? l.charAt(i) : '\0';
      letter = (c >= 'a' && c <= 'z');
    }
    return ns;
  }

  /** change the BasicSSCell's name */
  public void setCellName(String name) throws VisADException {
    if (name == null) {
      throw new VisADException("BasicSSCell: name cannot be null");
    }
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (name.equalsIgnoreCase(panel.Name) && panel != this) {
        throw new VisADException("BasicSSCell: name already used");
      }
    }
    Name = name;
  }

  /** reconstruct this BasicSSCell using the specified info string */
  public void setSSCellString(String info) throws VisADException,
                                                  RemoteException {
    // extract filename from info string
    if (!info.substring(0, 11).equals("filename = ")) {
      throw new VisADException("Invalid info string!");
    }
    int i=10;
    char c = '*';
    while (c != '\n') c = info.charAt(++i);
    String filename = info.substring(11, i++);
    if (filename.equals("null")) filename = null;

    // extract RMI address from info string
    if (!info.substring(i, i+6).equals("rmi = ")) {
      throw new VisADException("Invalid info string!");
    }
    i += 5;
    int oi = i + 1;
    c = '*';
    while (c != '\n') c = info.charAt(++i);
    String rmi = info.substring(oi, i++);
    if (rmi.equals("null")) rmi = null;

    // extract formula from info string
    if (!info.substring(i, i+10).equals("formula = ")) {
      throw new VisADException("Invalid info string!");
    }
    i += 9;
    oi = i + 1;
    c = '*';
    while (c != '\n') c = info.charAt(++i);
    String formula = info.substring(oi, i++);

    // extract dimension from info string
    if (!info.substring(i, i+6).equals("dim = ")) {
      throw new VisADException("Invalid info string!");
    }
    i += 5;
    oi = i + 1;
    c = '*';
    while (c != '\n') c = info.charAt(++i);
    String b = info.substring(oi, i++);
    int dim = -1;
    try {
      dim = Integer.parseInt(b);
    }
    catch (NumberFormatException exc) { }
    if (dim != JAVA3D_3D && dim != JAVA2D_2D && dim != JAVA3D_2D) {
      throw new VisADException("Invalid info string!");
    }

    // extract mappings from info string
    if (!info.substring(i, i+7).equals("maps = ")) {
      throw new VisADException("Invalid info string!");
    }
    Vector dnames = new Vector();
    Vector rnames = new Vector();
    i += 6;
    c = '*';
    while (c != '\n') {
      c = '*';
      oi = i + 1;
      while (c != ' ' && c != '\n') c = info.charAt(++i);
      if (c != '\n') {
        String dname = info.substring(oi, i++);
        dnames.add(dname);
        c = '*';
        oi = i;
        while (c != ' ' && c != '\n') c = info.charAt(++i);
        try {
          String s = (String) info.substring(oi, i);
          int q = Integer.parseInt(s);
          rnames.add(new Integer(q));
        }
        catch (NumberFormatException exc) {
          throw new VisADException("Invalid info string!");
        }
      }
    }

    // clear old stuff from cell
    clearCell();

    // set up dimension
    setDimension(dim);

    // set up filename
    if (filename != null) {
      URL u = null;
      try {
        u = new URL(filename);
      }
      catch (MalformedURLException exc) {
        throw new VisADException(exc.toString());
      }
      try {
        loadData(u);
      }
      catch (IOException exc) {
        throw new VisADException(exc.toString());
      }
    }

    // set up RMI address
    if (rmi != null) {
      try {
        loadRMI(rmi);
      }
      catch (MalformedURLException exc) {
        throw new VisADException(exc.toString());
      }
      catch (NotBoundException exc) {
        throw new VisADException(exc.toString());
      }
      catch (AccessException exc) {
        throw new VisADException(exc.toString());
      }
    }

    // set up formula
    if (!formula.equals("")) setFormula(formula);

    // set up mappings
    int len = dnames.size();
    if (len > 0) {
      ScalarMap[] maps = new ScalarMap[len];
      for (int j=0; j<len; j++) {
        ScalarType domain = ScalarType.getScalarTypeByName(
                            (String) dnames.elementAt(j));
        int q = ((Integer) rnames.elementAt(j)).intValue();
        DisplayRealType range = Display.DisplayRealArray[q];
        maps[j] = new ScalarMap(domain, range);
      }
      setMaps(maps);
    }
  }

  /** return the data string necessary to reconstruct this cell */
  public String getSSCellString() {
    String s = "filename = " + (Filename == null ?
                               "null" : Filename.toString()) + "\n";
    s = s + "rmi = " + RMIAddress + "\n";
    s = s + "formula = " + Formula + "\n";
    s = s + "dim = " + Dimension2D + "\n";
    s = s + "maps = ";
    ScalarMap[] maps = null;
    if (VDisplay != null) {
      Vector mapVector = VDisplay.getMapVector();
      int mvs = mapVector.size();
      if (mvs > 0) {
        for (int i=0; i<mvs; i++) {
          ScalarMap m = (ScalarMap) mapVector.elementAt(i);
          ScalarType domain = m.getScalar();
          DisplayRealType range = m.getDisplayScalar();
          int q = -1;
          for (int j=0; j<Display.DisplayRealArray.length; j++) {
            if (range.equals(Display.DisplayRealArray[j])) q = j;
          }
          if (i > 0) s = s + " ";
          s = s + domain.getName() + " " + q;
        }
        s = s + "\n";
      }
      else s = s + "null\n";
    }
    else s = s + "null\n";
    return s;
  }

  /** set up the DisplayListener for this cell */
  public void setDisplayListener(DisplayListener d) {
    DListen = d;
    if (d != null) VDisplay.addDisplayListener(d);
  }

  /** map RealTypes to the display according to the specified ScalarMaps */
  public void setMaps(ScalarMap[] maps) throws VisADException,
                                               RemoteException {
    if (maps == null) return;
    clearMaps();
    VisADException vexc = null;
    RemoteException rexc = null;
    for (int i=0; i<maps.length; i++) {
      try {
        VDisplay.addMap(maps[i]);
      }
      catch (VisADException exc) {
        vexc = exc;
      }
      catch (RemoteException exc) {
        rexc = exc;
      }
    }
    VDisplay.addReference(DataRef);
    HasMappings = true;
    if (vexc != null) throw vexc;
    if (rexc != null) throw rexc;
  }

  /** whether other cells are dependent on this one */
  public boolean othersDepend() {
    try {
      return !fm.canBeRemoved(Name);
    }
    catch (FormulaException exc) {
      return false;
    }
  }

  /** clear this cell's mappings */
  public void clearMaps() throws VisADException, RemoteException {
    if (HasMappings) {
      VDisplay.removeReference(DataRef);
      VDisplay.clearMaps();
      HasMappings = false;
    }
  }

  /** clear this cell's display */
  public void clearDisplay() throws VisADException, RemoteException {
    clearMaps();
    setX(false);
    if (HasDisplay) {
      remove(VDPanel);
      validate();
      HasDisplay = false;
    }
  }

  /** clear this cell completely */
  public void clearCell() throws VisADException, RemoteException {
    setFormula(null);
    Filename = null;
    RMIAddress = null;
    clearDisplay();
    setData(null);
  }

  /** set this cell's Data to data */
  public void setData(Data data) throws VisADException, RemoteException {
    fm.setThing(Name, data);

    if (data == null) HasData = false;
    else {
      HasData = true;
      // add this Data's RealTypes to FormulaManager variable registry
      Vector v = new Vector();
      getRealTypes(data, v);
      int len = v.size();
      for (int i=0; i<len; i++) {
        RealType rt = (RealType) v.elementAt(i);
        fm.setThing(rt.getName(), new SSRealType(rt));
      }
    }
  }

  /** set the BasicSSCell to 2-D or 3-D display with Java2D or Java3D */
  public void setDimension(boolean twoD, boolean java2d)
                           throws VisADException, RemoteException {
    int dim;
    if (!twoD && java2d) return;
    if (!twoD && !java2d) dim = JAVA3D_3D;
    else if (twoD && java2d) dim = JAVA2D_2D;
    else dim = JAVA3D_2D;  // twoD && !java2d
    setDimension(dim);
  }

  private void setDimension(int dim) throws VisADException, RemoteException {
    if (Dimension2D == dim) return;
    Dimension2D = dim;

    // save current mappings for restoration after dimension switch
    ScalarMap[] maps = null;
    if (VDisplay != null) {
      Vector mapVector = VDisplay.getMapVector();
      int mvs = mapVector.size();
      if (mvs > 0) {
        maps = new ScalarMap[mvs];
        for (int i=0; i<mvs; i++) {
          maps[i] = (ScalarMap) mapVector.elementAt(i);
        }
      }
    }

    // remove listener temporarily
    if (DListen != null) VDisplay.removeDisplayListener(DListen);

    // clear display completely
    clearDisplay();

    // switch display dimension
    if (Dimension2D == JAVA3D_3D) {
      VDisplay = new DisplayImplJ3D(Name);
    }
    else if (Dimension2D == JAVA2D_2D) {
      VDisplay = new DisplayImplJ2D(Name);
    }
    else {  // Dimension2D == JAVA3D_2D
      VDisplay = new DisplayImplJ3D(Name, new TwoDDisplayRendererJ3D());
    }

    // reinitialize display
    VDPanel = (JPanel) VDisplay.getComponent();
    if (HasData) {
      add(VDPanel);
      validate();
      HasDisplay = true;
    }

    // put listener back
    if (DListen != null) VDisplay.addDisplayListener(DListen);

    // put mappings back
    if (maps != null) {
      try {
        setMaps(maps);
      }
      catch (VisADException exc) { }
    }
    notifyListeners(SSCellChangeEvent.DIMENSION_CHANGE);
  }

  /** set the BasicSSCell's formula */
  public void setFormula(String f) throws VisADException, RemoteException {
    String nf = (f == null ? "" : f);
    if (Formula.equals(nf)) return;
    HasData = false;
    HasFormula = false;
    Formula = "";
    String formula = preParse(nf);
    fm.assignFormula(Name, formula);
    HasFormula = !formula.equals("");
    Formula = nf;
  }

  /** return whether the BasicSSCell is in 2-D display mode */
  public int getDimension() {
    return Dimension2D;
  }

  /** return the associated DataReference object */
  public DataReferenceImpl getDataRef() {
    return DataRef;
  }

  /** return the associated RemoteDataReference object */
  public RemoteDataReferenceImpl getRemoteDataRef() {
    return RemoteDataRef;
  }

  /** return the file name from which the associated Data came, if any */
  public URL getFilename() {
    return Filename;
  }

  /** return the RMI address from which the associated Data came, if any */
  public String getRMIAddress() {
    return RMIAddress;
  }

  /** return the formula for this BasicSSCell, if any */
  public String getFormula() {
    return Formula;
  }

  /** used by toggleWait */
  private JPanel pWait = null;

  /** used by toggleWait */
  private boolean waiting = false;

  /** used by loadData and loadRMI */
  private void toggleWait() {
    if (pWait == null) {
      pWait = new JPanel();
      pWait.setBackground(Color.black);
      pWait.setLayout(new BoxLayout(pWait, BoxLayout.X_AXIS));
      pWait.add(Box.createHorizontalGlue());
      pWait.add(new JLabel("Please wait..."));
      pWait.add(Box.createHorizontalGlue());
    }
    if (waiting) {
      remove(pWait);
      repaint();
      waiting = false;
    }
    else {
      add(pWait);
      validate();
      repaint();
      waiting = true;
    }
  }

  /** import a data object from a given URL */
  public void loadData(URL u) throws BadFormException, IOException,
                                     VisADException, RemoteException {
    if (u == null) return;
    clearDisplay();
    setFormula(null);
    Filename = null;
    RMIAddress = null;
    toggleWait();

    Data data = null;
    try {
      // file detection -- only necessary because some Data Forms
      //                   lack open(URL) capability
      String s = u.toString();
      boolean f = false;
      String file = null;
      if (s.length() >= 6 && s.substring(0, 6).equalsIgnoreCase("file:/")) {
        f = true;
        file = s.substring(6);
      }
      DefaultFamily loader = new DefaultFamily("loader");
      if (f) data = loader.open(file);
      else data = loader.open(u);
      loader = null;
    }
    finally {
      toggleWait();
    }
    if (data != null) {
      setData(data);
      Filename = u;
    }
    else setData(null);
  }

  /** import a data object from a given RMI address, and automatically
      update this cell whenever the remote data object changes */
  public void loadRMI(String s) throws MalformedURLException,
                                       NotBoundException,
                                       AccessException,
                                       RemoteException,
                                       VisADException {
    // example of RMI address: rmi://www.myaddress.com/MyServer/A1
    if (s == null || !s.startsWith("rmi://")) {
      throw new VisADException("RMI address must begin with \"rmi://\"");
    }
    clearDisplay();
    setFormula(null);
    Filename = null;
    RMIAddress = null;
    toggleWait();

    try {
      int len = s.length();
      int end = s.lastIndexOf("/");
      if (end < 6) end = len;
      String server = s.substring(4, end);
      String object = (end < len-1) ? s.substring(end+1, len) : "";
      RemoteServer rs = null;
      rs = (RemoteServer) Naming.lookup(server);
      RemoteDataReference ref = rs.getDataReference(object);
      if (ref == null) {
        throw new VisADException("Could not import remote object called " +
                                 "\"" + object + "\"");
      }
      final RemoteDataReference rref = ref;
      final BasicSSCell cell = this;
      CellImpl lcell = new CellImpl() {
        public void doAction() {
          // update local data when remote data changes
          try {
            cell.setData(rref.getData().local());
          }
          catch (NullPointerException exc) {
            if (ShowFormulaErrors) {
              showMsg("RMI error", "Remote data is null");
            }
          }
          catch (VisADException exc) {
            if (ShowFormulaErrors) {
              showMsg("RMI error",
                      "An error occurred when updating the remote data");
            }
          }
          catch (RemoteException exc) {
            if (ShowFormulaErrors) {
              showMsg("RMI error",
                      "Unable to import updated remote data");
            }
          }
        }
      };
      RemoteCellImpl rcell = new RemoteCellImpl(lcell);
      rcell.addReference(ref);
    }
    finally {
      toggleWait();
    }
    RMIAddress = s;
  }

  /** export a data object to a given file name, in netCDF format */
  public void saveData(File f, boolean netcdf) throws BadFormException,
                                                      IOException,
                                                      VisADException,
                                                      RemoteException {
    if (f == null || !HasData) return;
    Saving++;
    if (netcdf) {
      Plain saver = new Plain();
      saver.save(f.getPath(), DataRef.getData(), true);
      saver = null;
    }
    else {
      VisADForm saver = new VisADForm();
      saver.save(f.getPath(), DataRef.getData(), true);
      saver = null;
    }
    Saving--;
  }

  public boolean hasData() {
    return HasData;
  }

  public boolean hasFormula() {
    return HasFormula;
  }

  public boolean hasMappings() {
    return HasMappings;
  }

  /** specify whether formula errors should be reported in a dialog box */
  public void setShowFormulaErrors(boolean sfe) {
    ShowFormulaErrors = sfe;
  }

  /** return whether formula errors are reported in a dialog box */
  public boolean getShowFormulaErrors() {
    return ShowFormulaErrors;
  }

  /** component that contains the large X */
  final JComponent BigXCanvas = new JComponent() {
    public void paint(Graphics g) {
      Dimension s = getSize();
      g.setColor(Color.white);
      g.drawLine(0, 0, s.width, s.height);
      g.drawLine(s.width, 0, 0, s.height);
    }
  };

  /** turns the large X on or off */
  void setX(boolean value) {
    if (BigX == value) return;
    BigX = value;

    // queue up action in event dispatch thread
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (BigX) add(BigXCanvas);
        else remove(BigXCanvas);
        validate();
        repaint();
      }
    });
  }

  /** pop up a message in a dialog box */
  void showMsg(String title, String msg) {
    final BasicSSCell c = this;
    final String t = title;
    final String m = msg;

    // queue up action in event dispatch thread
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(c, m, t, JOptionPane.ERROR_MESSAGE);
      }
    });
  }

  // WLH 24 March 99
  /** add a variable */
  public static void createVar(String name, ThingReference tr) throws VisADException {
    fm.createVar(name, tr);
  }

}

