
//
// MathType.java
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

package visad;

import java.rmi.*;
import java.util.Vector;

/** 
(Fulker)
   MathType is the superclass for VisAD's hierarchy of mathematical types.
   It encompasses the mathematical concepts of scalars, tuples (i.e., 
   n-dimensional vectors), functions, and certain forms of sets.<p>

   VisAD <b>Data</b> objects are finite approximations to math objects.  
   Every Data object possesses a MathType, which characterizes the 
   mathematical object that the data approximate.  This MathType is not 
   synonymous with the class of the Data object, even though the class
   names for a Data object and its corresponding MathType object (Set 
   and SetType, e.g.) may be similar.<p>

   MathType objects are immutable; one implication is that the setDefaultSet 
   method (in RealTupleType) can be invoked only <b>prior</b> to using the 
   related getDefaultSet method.<p>
(/Fulker)<p>

   MathType is the superclass of the VisAD hierarchy of data types.
   MathType objects are immutable; note that there is a setDefaultSet method
   in RealTupleType, but if t is a RealTupleType then t.setDefaultSet cannot
   be called after t.getDefaultSet has been called.<P>

   VisAD Data objects are finite approximations to mathematical objects.
   Every Data object includes a MathType, which is the mathematical type
   of the mathematical object it approximates.  This MathType is not
   synonymous with the class of the Data object.<P>

*/
public abstract class MathType extends Object implements java.io.Serializable {

  /** true if this MathType is defined by the system */
  boolean SystemIntrinsic;

  public MathType() {
    super();
    SystemIntrinsic = false;
  }

  MathType(boolean b) {
    super();
    SystemIntrinsic = true;
  }

  /** 
(Fulker)
   Check for equality of MathType, including equality of scalar names.
   All MathType objects are named or are built up of named MathTypes.
   For example, a ScalarType object might be named "Pressure," and a
   FunctionType object might map a "Time" domain onto a range named 
   "Temperature."  Therefore MathType objects have methods to test for
   two types of equality; both compare the underlying mathematical types,
   but only this method tests to be sure that all of the names match.  
   Such tests are useful because some operations on Data objects 
   (differencing, for example) make sense only when those objects 
   approximate MathTypes that are identical in the named as well as the
   mathematical sense.<p>
(/Fulker)<p>

  check for equality of data types, including equality of scalar names
  for example, real types for "pressure" and "temperature" are not equal

  */
  public abstract boolean equals(Object type);

  /** check for equality of data types, excluding equality of scalar names
      for example, real types for "pressure" and "temperature" are equal */
  public abstract boolean equalsExceptName(MathType type);

  /* TDR - May 1998.  As above, except units must be convertible */
  public abstract boolean equalsExceptNameButUnits( MathType type )
           throws VisADException;

  /* TDR - June 1998           */
  public abstract MathType cloneDerivative( RealType d_partial )
           throws VisADException;

  /* TDR - July 1998  */
  public abstract MathType binary( MathType type, int op, Vector names )
         throws VisADException;

  /* TDR - July 1998 */
  public abstract MathType unary( int op, Vector names )
         throws VisADException;

  public abstract Data missingData() throws VisADException, RemoteException;

  public abstract ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
           throws VisADException, RemoteException;

  public abstract String toString();

  public String prettyString() {
    return prettyString(0);
  }

  public abstract String prettyString(int indent);

  /** Guesses at a set of &quot;default&quot; mappings for this MathType.
      Intuitively, first we look for a FunctionType with domain dimension 3,
      then a nested group of FunctionTypes with 'cumulative' domain
      dimension 3.  Next we look for a FunctionType or nested group of
      FunctionTypes with domain dimension 2.  Last, we look for a
      FunctionType with domain dimension 1.  Nested groups of FunctionTypes
      may be nested with TupleTypes, which is indicated by some of the
      MathType templates.
  */
  public ScalarMap[] guessMaps(boolean threeD) {
    MathType m = this;

    // set up aliases for "time" RealType to be mapped to Animation
    // NOTE: other acceptable 1-D function domains could be added here,
    //       by allocating a larger DataStruct array, then specifying
    //              ds[*] = new DataStruct("X");
    //       where "X" is the name of the 1-D function domain RealType
    //       that is desired to be automatically mapped to Animation.
    DataStruct[] ds = new DataStruct[3];
    ds[0] = new DataStruct("time");
    ds[1] = new DataStruct("Time");
    ds[2] = new DataStruct("TIME");

    // find a FunctionType whose 1-D domain is a RealType matching above
    findTimeFunction(m, ds);
    int timeFunc = -1;
    for (int i=0; i<ds.length; i++) {
      if (ds[i].fvalid && ds[i].funcs.size() > 0) {
        timeFunc = i;
        break;
      }
    }

    // compile a list of FunctionTypes to search through for template matching
    Vector flist = new Vector();
    if (timeFunc < 0) buildFunctionList(m, flist);
    else {
      // found a "time" RealType; only search ranges of "time" FunctionTypes
      for (int i=0; i<ds[timeFunc].funcs.size(); i++) {
        FunctionType f = (FunctionType) ds[timeFunc].funcs.elementAt(i);
        buildFunctionList(f.getRange(), flist);
      }
    }

    // look for matches between templates and FunctionTypes list
    int numfuncs = flist.size();
    for (int template=(threeD ? 0 : 4); template<8; template++) {
      for (int fi=0; fi<numfuncs; fi++) {
        FunctionType ft = (FunctionType) flist.elementAt(fi);
        switch (template) {
          case 0: // 3-D ONLY
            //   ((x, y, z) -> (..., a, ...))
            //   ((x, y, z) -> a)
            // x -> X, y -> Y, z -> Z, a -> IsoContour
            if (!ft.getFlat()) break;
            RealTupleType domain = ft.getDomain();
            if (domain.getDimension() != 3) break;
            MathType range = ft.getRange();
            RealType x, y, z;
            try {
              x = (RealType) domain.getComponent(0);
              y = (RealType) domain.getComponent(1);
              z = (RealType) domain.getComponent(2);
            }
            catch (VisADException exc) {
              break;
            }
            RealType a;
            if (range instanceof RealType) {
              a = (RealType) range;
              try {
                ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 4 : 5];
                smaps[0] = new ScalarMap(x, Display.XAxis);
                smaps[1] = new ScalarMap(y, Display.YAxis);
                smaps[2] = new ScalarMap(z, Display.ZAxis);
                smaps[3] = new ScalarMap(a, Display.IsoContour);
                if (timeFunc >= 0) {
                  Object o = ds[timeFunc].funcs.elementAt(0);
                  RealTupleType rtt = ((FunctionType) o).getDomain();
                  RealType time = (RealType) rtt.getComponent(0);
                  smaps[4] = new ScalarMap(time, Display.Animation);
                }
                return smaps;
              }
              catch (VisADException exc) { }
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType mt;
                try {
                  mt = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (mt instanceof RealType) {
                  a = (RealType) mt;
                  try {
                    ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 4 : 5];
                    smaps[0] = new ScalarMap(x, Display.XAxis);
                    smaps[1] = new ScalarMap(y, Display.YAxis);
                    smaps[2] = new ScalarMap(z, Display.ZAxis);
                    smaps[3] = new ScalarMap(a, Display.IsoContour);
                    if (timeFunc >= 0) {
                      Object o = ds[timeFunc].funcs.elementAt(0);
                      RealTupleType rtt = ((FunctionType) o).getDomain();
                      RealType time = (RealType) rtt.getComponent(0);
                      smaps[4] = new ScalarMap(time, Display.Animation);
                    }
                    return smaps;
                  }
                  catch (VisADException exc) {
                    break;
                  }
                }
              }
            }
            break;

          case 1: // 3-D ONLY
            //   (z -> ((x, y) -> (..., a, ...)))
            //   (z -> (..., ((x, y) -> (..., a, ...)), ...))
            //   (z -> ((x, y) -> a))
            //   (z -> (..., ((x, y) -> a), ...))
            // x -> X, y -> Y, z -> Z, a -> IsoContour
            domain = ft.getDomain();
            if (domain.getDimension() != 1) break;
            try {
              z = (RealType) domain.getComponent(0);
            }
            catch (VisADException exc) {
              break;
            }
            range = ft.getRange();
            FunctionType rf = null;
            if (range instanceof FunctionType) {
              rf = (FunctionType) range;
              if (!rf.getFlat() || rf.getDomain().getDimension() != 2) break;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  rf = null;
                  break;
                }
                if (ttci instanceof FunctionType) {
                  FunctionType ftci = (FunctionType) ttci;
                  if (ftci.getFlat() && ftci.getDomain().getDimension() == 2) {
                    rf = ftci;
                  }
                  break;
                }
              }
            }
            if (rf == null) break;
            RealTupleType rfd = rf.getDomain();
            try {
              x = (RealType) rfd.getComponent(0);
              y = (RealType) rfd.getComponent(1);
            }
            catch (VisADException exc) {
              break;
            }
            range = rf.getRange();
            a = null;
            if (range instanceof RealType) {
              a = (RealType) range;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (ttci instanceof RealType) {
                  a = (RealType) ttci;
                  break;
                }
              }
            }
            if (a == null) break;
            try {
              ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 4 : 5];
              smaps[0] = new ScalarMap(x, Display.XAxis);
              smaps[1] = new ScalarMap(y, Display.YAxis);
              smaps[2] = new ScalarMap(z, Display.ZAxis);
              smaps[3] = new ScalarMap(a, Display.IsoContour);
              if (timeFunc >= 0) {
                Object o = ds[timeFunc].funcs.elementAt(0);
                RealTupleType rtt = ((FunctionType) o).getDomain();
                RealType time = (RealType) rtt.getComponent(0);
                smaps[4] = new ScalarMap(time, Display.Animation);
              }
              return smaps;
            }
            catch (VisADException exc) {
              break;
            }

          case 2: // 3-D ONLY
            //   ((x, y) -> (z -> (..., a, ...)))
            //   ((x, y) -> (..., (z -> (..., a, ...)), ...))
            //   ((x, y) -> (z -> a))
            //   ((x, y) -> (..., (z -> a), ...))
            // x -> X, y -> Y, z -> Z, a -> RGB
            //
            domain = ft.getDomain();
            if (domain.getDimension() != 2) break;
            try {
              x = (RealType) domain.getComponent(0);
              y = (RealType) domain.getComponent(1);
            }
            catch (VisADException exc) {
              break;
            }
            range = ft.getRange();
            rf = null;
            if (range instanceof FunctionType) {
              rf = (FunctionType) range;
              if (!rf.getFlat() || rf.getDomain().getDimension() != 1) break;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  rf = null;
                  break;
                }
                if (ttci instanceof FunctionType) {
                  FunctionType ftci = (FunctionType) ttci;
                  if (ftci.getFlat() && ftci.getDomain().getDimension() == 1) {
                    rf = ftci;
                  }
                  break;
                }
              }
            }
            if (rf == null) break;
            rfd = rf.getDomain();
            try {
              z = (RealType) rfd.getComponent(0);
            }
            catch (VisADException exc) {
              break;
            }
            range = rf.getRange();
            a = null;
            if (range instanceof RealType) {
              a = (RealType) range;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (ttci instanceof RealType) {
                  a = (RealType) ttci;
                  break;
                }
              }
            }
            if (a == null) break;
            try {
              ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 4 : 5];
              smaps[0] = new ScalarMap(x, Display.XAxis);
              smaps[1] = new ScalarMap(y, Display.YAxis);
              smaps[2] = new ScalarMap(z, Display.ZAxis);
              smaps[3] = new ScalarMap(a, Display.IsoContour);
              if (timeFunc >= 0) {
                Object o = ds[timeFunc].funcs.elementAt(0);
                RealTupleType rtt = ((FunctionType) o).getDomain();
                RealType time = (RealType) rtt.getComponent(0);
                smaps[4] = new ScalarMap(time, Display.Animation);
              }
              return smaps;
            }
            catch (VisADException exc) {
              break;
            }

          case 3: // 3-D ONLY
            //   (x -> (y -> (z -> (..., a, ...))))
            //   (x -> (..., (y -> (z -> (..., a, ...))), ...))
            //   (x -> (y -> (..., z -> (..., a, ...)), ...))
            //   (x -> (..., (y -> (..., (z -> (..., a, ...)), ...)), ...))
            //   (x -> (y -> (z -> a)))
            //   (x -> (..., (y -> (z -> a)), ...))
            //   (x -> (y -> (..., (z -> a), ...)))
            //   (x -> (..., (y -> (..., (z -> a), ...)), ...))
            // x -> X, y -> Y, z -> Z, a -> RGB
            domain = ft.getDomain();
            if (domain.getDimension() != 1) break;
            try {
              x = (RealType) domain.getComponent(0);
            }
            catch (VisADException exc) {
              break;
            }
            // find nested "y"
            range = ft.getRange();
            rf = null;
            if (range instanceof FunctionType) {
              rf = (FunctionType) range;
              if (rf.getDomain().getDimension() != 1) break;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  rf = null;
                  break;
                }
                if (ttci instanceof FunctionType) {
                  FunctionType ftci = (FunctionType) ttci;
                  if (ftci.getDomain().getDimension() == 1) {
                    rf = ftci;
                  }
                  break;
                }
              }
            }
            if (rf == null) break;
            rfd = rf.getDomain();
            try {
              y = (RealType) rfd.getComponent(0);
            }
            catch (VisADException exc) {
              break;
            }
            // find nested "z"
            range = rf.getRange();
            rf = null;
            if (range instanceof FunctionType) {
              rf = (FunctionType) range;
              if (rf.getDomain().getDimension() != 1) break;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  rf = null;
                  break;
                }
                if (ttci instanceof FunctionType) {
                  FunctionType ftci = (FunctionType) ttci;
                  if (ftci.getDomain().getDimension() == 1) {
                    rf = ftci;
                  }
                  break;
                }
              }
            }
            if (rf == null) break;
            rfd = rf.getDomain();
            try {
              z = (RealType) rfd.getComponent(0);
            }
            catch (VisADException exc) {
              break;
            }
            // find nested "a"
            range = rf.getRange();
            a = null;
            if (range instanceof RealType) {
              a = (RealType) range;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (ttci instanceof RealType) {
                  a = (RealType) ttci;
                  break;
                }
              }
            }
            if (a == null) break;
            try {
              ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 4 : 5];
              smaps[0] = new ScalarMap(x, Display.XAxis);
              smaps[1] = new ScalarMap(y, Display.YAxis);
              smaps[2] = new ScalarMap(z, Display.ZAxis);
              smaps[3] = new ScalarMap(a, Display.IsoContour);
              if (timeFunc >= 0) {
                Object o = ds[timeFunc].funcs.elementAt(0);
                RealTupleType rtt = ((FunctionType) o).getDomain();
                RealType time = (RealType) rtt.getComponent(0);
                smaps[4] = new ScalarMap(time, Display.Animation);
              }
              return smaps;
            }
            catch (VisADException exc) {
              break;
            }

          case 4: // 2-D or 3-D
            //   ((x, y) -> (..., r, ..., g, ..., b, ...))
            // x -> X, y -> Y, r -> Red, g -> Green, b -> Blue
            //   ((x, y) -> (..., a, ...))
            //   ((x, y) -> a)
            // x -> X, y -> Y, a -> RGB
            if (!ft.getFlat()) break;
            domain = ft.getDomain();
            if (domain.getDimension() != 2) break;
            try {
              x = (RealType) domain.getComponent(0);
              y = (RealType) domain.getComponent(1);
            }
            catch (VisADException exc) {
              break;
            }
            range = ft.getRange();
            RealType[] rgb = new RealType[3];
            int rgbc = 0;
            if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (ttci instanceof RealType) {
                  rgb[rgbc++] = (RealType) ttci;
                }
              }
            }
            else if (range instanceof RealType) {
              rgb[rgbc++] = (RealType) range;
            }
            if (rgbc == 0) break;
            if (rgbc < 3) {
              try {
                ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 3 : 4];
                smaps[0] = new ScalarMap(x, Display.XAxis);
                smaps[1] = new ScalarMap(y, Display.YAxis);
                smaps[2] = new ScalarMap(rgb[0], Display.RGB);
                if (timeFunc >= 0) {
                  Object o = ds[timeFunc].funcs.elementAt(0);
                  RealTupleType rtt = ((FunctionType) o).getDomain();
                  RealType time = (RealType) rtt.getComponent(0);
                  smaps[3] = new ScalarMap(time, Display.Animation);
                }
                return smaps;
              }
              catch (VisADException exc) { }
            }
            else {
              try {
                ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 5 : 6];
                smaps[0] = new ScalarMap(x, Display.XAxis);
                smaps[1] = new ScalarMap(y, Display.YAxis);
                smaps[2] = new ScalarMap(rgb[0], Display.Red);
                smaps[3] = new ScalarMap(rgb[1], Display.Green);
                smaps[4] = new ScalarMap(rgb[2], Display.Blue);
                if (timeFunc >= 0) {
                  Object o = ds[timeFunc].funcs.elementAt(0);
                  RealTupleType rtt = ((FunctionType) o).getDomain();
                  RealType time = (RealType) rtt.getComponent(0);
                  smaps[5] = new ScalarMap(time, Display.Animation);
                }
              }
              catch (VisADException exc) { }
            }
            break;

          case 5: // 2-D or 3-D
            //   (x -> (y -> (..., a, ...)))
            //   (x -> (y -> a))
            // 3-D: x -> X, y -> Y, a -> Z
            // 2-D: x -> X, y -> Y, a -> RGB
            //
            break;

          case 6: // 2-D or 3-D
            //   (x -> (..., a, ...))
            //   (x -> a)
            // x -> X, a -> Y
            //
            break;
        }
      }
    }

    return null;
  }

  /** used by guessMaps to recursively build a list of FunctionTypes to
      attempt template matching with */
  private void buildFunctionList(MathType mt, Vector list) {
    if (mt instanceof TupleType) {
      TupleType tt = (TupleType) mt;
      for (int i=0; i<tt.getDimension(); i++) {
        try {
          buildFunctionList(tt.getComponent(i), list);
        }
        catch (VisADException exc) { }
      }
    }
    else if (mt instanceof SetType) {
      SetType st = (SetType) mt;
      buildFunctionList(st.getDomain(), list);
    }
    else if (mt instanceof FunctionType) list.addElement(mt);
    return;
  }

  /** used by guessMaps to recursively find a "time" RealType inside
      a 1-D function domain */
  private void findTimeFunction(MathType mt, DataStruct[] info) {
    if (mt instanceof TupleType) {
      TupleType tt = (TupleType) mt;
      // search each element of the tuple
      for (int i=0; i<tt.getDimension(); i++) {
        MathType tc = null;
        try {
          tc = tt.getComponent(i);
        }
        catch (VisADException exc) { }
        findTimeFunction(tc, info);
      }
    }
    else if (mt instanceof SetType) {
      SetType st = (SetType) mt;
      // search set's domain
      findTimeFunction(st.getDomain(), info);
    }
    else if (mt instanceof FunctionType) {
      FunctionType ft = (FunctionType) mt;
      RealTupleType domain = ft.getDomain();
      MathType range = ft.getRange();
      RealType rtc0 = null;
      try {
        rtc0 = (RealType) domain.getComponent(0);
      }
      catch (VisADException exc) { }
      boolean found = false;
      if (rtc0 != null && domain.getDimension() == 1) {
        // search function's domain for RealType with matching name
        String rtname = rtc0.getName();
        for (int i=0; i<info.length; i++) {
          if (rtname.equals(info[i].name)) {
            info[i].funcs.addElement(ft);
            found = true;
          }
        }
      }
      // search function's domain
      if (!found) findTimeFunction(domain, info);
      // search function's range
      findTimeFunction(range, info);
    }
    else if (mt instanceof RealType) {
      RealType rt = (RealType) mt;
      String rtname = rt.getName();
      for (int i=0; i<info.length; i++) {
        // invalidate RealTypes not in a 1-D function domain
        if (rtname.equals(info[i].name)) info[i].fvalid = false;
      }
    }
    return;
  }

  /** run 'java visad.MathType' to test MathType.prettyString()
      and MathType.guessMaps() */
  public static void main(String args[])
         throws VisADException, RemoteException {
    RealType X = new RealType("Xxxxxx", null, null);
    RealType Y = new RealType("Yyyyyy", null, null);
    RealType Z = new RealType("Zzzzzz", null, null);
 
    RealType A = new RealType("Aaaaaa", null, null);
    RealType B = new RealType("Bbbbbb", null, null);
 
    RealType[] domain2d = {X, Y};
    RealTupleType Domain2d = new RealTupleType(domain2d);
 
    RealType[] range2d = {A, B};
    RealTupleType Range2d = new RealTupleType(range2d);
 
    FunctionType Field2d1 = new FunctionType(Domain2d, A);
    FunctionType Field2d2 = new FunctionType(Domain2d, Range2d);
    FunctionType Field2d3 = new FunctionType(Domain2d, B);
    FunctionType function = new FunctionType(X, Field2d2);
    MathType[] littles = {Range2d, Field2d1, function};
    TupleType little = new TupleType(littles);
    FunctionType little_function = new FunctionType(X, little);
    SetType set = new SetType(Domain2d);
    MathType[] types = {Range2d, little_function, Field2d1, Field2d2,
                        function, set, Field2d3};
    TupleType tuple = new TupleType(types);
    FunctionType big_function = new FunctionType(Range2d, tuple);

    System.out.println(big_function.prettyString());
  }

  /** used by guessMaps to store miscellaneous information
      throughout findTimeFunction's recursive calls */
  private class DataStruct {
    /** whether this DataStruct's funcs are valid */
    boolean fvalid = true;
    /** The name of the RealType in the domain of the FunctionTypes in funcs */
    String name;
    /** FunctionType objects with 1-D domains which match name */
    Vector funcs = new Vector();

    /** constructor */
    DataStruct(String s) { name = s; }
  }

}

